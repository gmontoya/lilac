#!/usr/bin/env python

import collections
import string
import logging
import os
import sys
import urllib
import socket
import parseEndpoints, parseQuery, parseQuery1_1, services
import Tree
from itertools import combinations, permutations
from multiprocessing import Queue
from ANAPSID.Planner.Plan import contactProxy
from Tree import Node, Leaf
from utils import *
from services import Service, Argument, Triple, Filter, Optional, UnionBlock, JoinBlock, Query
from FedraSourceSelection import FedraSourceSelection
from FedraQueryRewriter import FedraQueryRewriter, getConnected

from DawSourceSelection import DawSourceSelection

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
handler = logging.FileHandler('.decompositions.log')
handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
handler.setFormatter(formatter)
logger.addHandler(handler)

confFile = os.getcwd()+'/confFile'

def decomposeQuery (l, q, d, c):
    genPred = readGeneralPredicates(os.path.join(os.path.split(os.path.split(__file__)[0])[0],
                                                               'Catalog','generalPredicates'))
    prefixes = getPrefs(q.prefs)
    #print "ln" + str(q.body) + str(type(q.body))
   
    return decomposeUnionBlock(q.body, l, genPred, prefixes, d, c)

def decomposeUnionBlock(ub, l, genPred, prefixes, decomposition, c):

    r = []
    for jb in ub.triples:
        pjb = decomposeJoinBlock(jb, l, genPred, prefixes, decomposition, c)
        if pjb:
            r.append(pjb)
    if r:
        return UnionBlock(r)
    else:
        return None

def decomposeJoinBlock(jb, l, genPred, prefixes, decomposition, c):
    # jb == joinblock
    # l == list de endpoints
    tl = []
    sl = []
    fl = []
    #print "len jb.triples" + str(len(jb.triples))
    
    for bgp in jb.triples:
        if isinstance(bgp, Triple):
            tl.append(bgp)
        elif isinstance(bgp, Filter):
            fl.append(bgp)
        elif isinstance(bgp, Optional):
            sl.append(Optional(decomposeUnionBlock(bgp.bgg, l, genPred, prefixes, decomposition, c)))
        elif isinstance(bgp, UnionBlock):
            pub = decomposeUnionBlock(bgp, l, genPred, prefixes, decomposition, c)
            if pub:
                sl.append(pub)
        elif isinstance(bgp, JoinBlock):
            pub = decomposeJoinBlock(bgp, l, genPred, prefixes, decomposition, c)
            if pub:
                sl.append(pub)
    #print 'fl'
    #print fl
    replace = False 
    if tl:
        #confFile=os.getcwd()+'/confFile'
        selectedSources = {}
        props = {}
        if os.path.isfile(confFile):
            with open(confFile, 'r') as f:
                for line in f:
                    line = line.strip()
                    k, v = line.split("=", 1)
                    props[k.strip()] = v.strip()
            #print props['SourceSelectionStrategy']
        if 'SourceSelectionStrategy' in props:
            sss = props['SourceSelectionStrategy']
        else:
            sss = 'engine'
        if (sss == 'FedraQR'):
            fqr = FedraQueryRewriter(tl, l, props, prefixes)
            fqr.performSourceSelection()
            selectedSources = fqr.getSelectedSources()
            options = fqr.getOptions()
            #print 'FedraQR selected sources: \n'+str(selectedSources)
            #print 'FedraQR options: \n'+str(options)
            gs = getGroupsFedraQR(l, tl, prefixes, selectedSources, options)
            #print 'groups fedra: '+str(gs)
        else:
            if (sss == 'Fedra'):
                fss = FedraSourceSelection(tl, l, props, prefixes)
                fss.performSourceSelection()
                selectedSources = fss.getSelectedSources()
                #print 'Fedra selected sources: \n'+str(selectedSources)
                replace = True
            else:
                for tp in tl:
                    ss = set(search(l, tp.predicate, prefixes))
                    selectedSources[tp] = ss
                if (sss == 'DAW'):
                    dss = DawSourceSelection(selectedSources, props, prefixes)
                    dss.performSourceSelection()
                    selectedSources = dss.getSelectedSources()
                    replace = True
                    print 'DAW selected sources: \n'+str(selectedSources)
                #print 'selected sources: '+str(selectedSources)
            gs = getGroups(l, tl, genPred, prefixes, decomposition, c, selectedSources, replace)
        #print l
        #print 'gs'
        #print gs
        if gs:
            gs.extend(sl)
            sl = gs
        else:
            return None

    fl1=includeFilter(sl, fl)
    fl=list(set(fl) - set(fl1))
    #print "sl" + str(sl) + str(len(sl))
    #print "fl" + str(fl)
    if sl:
      if (len(sl)==1 and isinstance(sl[0],UnionBlock) and fl!=[]):
        sl[0]=updateFilters(sl[0],fl) 
      j=JoinBlock(sl,filters=fl)
      #print "filters" + str(j.filters)
      return j
    else:
        return None

def updateFilters(node,filters):
   return UnionBlock(node.triples,filters)

def getGroupsFedraQR(l, tl, prefixes, selectedSources, options):
    union = set()
    exclusive = set()
    covered = set()
    endpoints = collections.defaultdict(list)
    exclusiveSources = set()
    for t in tl:
        sources = selectedSources[t]
        if len(sources) > 1:
            union.add(t)
            for ss in sources:
                for s in ss:
                    if s in options:
                        set2 = set(options[s])
                    else:
                        set2 = set()
                    endpoints[s] = set2
        else:
            exclusive.add(t)
    #print 'endpoints'
    #print endpoints
    listOperators = []
    for t in union:
        r = transform(t, endpoints, covered, selectedSources)
        if r == None:
            return []
        listOperators.append(r)
    #print 'list of operators'
    #print listOperators
    endpoints = collections.defaultdict(list)
    added = set()
    for e in exclusive:
        sourcesAux = selectedSources[e]
        for s in sourcesAux:
            sources = s
            break
        for endpoint in sources:
            if endpoint in endpoints:
                ss = endpoints[endpoint]
            else:
                ss = set()
            ss.add(e)
            endpoints[endpoint] = ss
    #print 'endpoints'
    #print endpoints
    #print 'listOperators: '+str(listOperators)
    listExclusiveOperators = []
    for endpoint in endpoints: 
        triples0 = endpoints[endpoint]
        connectedTriples = getConnectedTriples(triples0)
        aux = []
        for triples in connectedTriples:
            #print 'type(triples)'+str(type(triples))
            if not(triples.issubset(covered)):
                x = Service('<'+endpoint+'>', UnionBlock([JoinBlock(list(triples))]))
                #print 'type(x) '+str(type(x))
                aux.append(x)
        #aux = [Service(endpoint, triples) for triples in connectedTriples if not(triples.issubset(covered))]

        #print 'aux '+str(aux)
        #print 'type(aux)'+str(type(aux))
        listExclusiveOperators.extend(aux)
        for triples in connectedTriples:
            for tp in triples:
                if not(tp in covered):
                    covered.add(tp)
    #print 'listEO: '+str(listExclusiveOperators)
    if (len(listExclusiveOperators) > 0) and (len(listOperators) > 0):
        listOperators.insert(0, listExclusiveOperators)
    elif (len(listExclusiveOperators) > 0):
        listOperators = listExclusiveOperators
    elif (len(listExclusiveOperators) == 0) and (len(listOperators) == 0):
        listOperators = None
    return listOperators

def transform(t, endpoints, covered, selectedSources):
    sources = selectedSources[t]
    if sources != None:
        args = []
        ts = None
        alreadyIncludedSources = set()
        for ss in sources:
            s = None
            max = 0
            for sAux in ss:
                if sAux in endpoints:
                    size = len(endpoints[sAux])
                else:
                    size = 0
                if (s == None) or (size > max):
                    max = size
                    s = sAux
            if s in alreadyIncludedSources:
                continue
            alreadyIncludedSources.add(s)
            if ts == None:
                ts = set(endpoints[s])
            else:
                ts = { x for x in ts if x in endpoints[s] }
            tl = []
            for tp in endpoints[s]:
                tl.append(tp)
            tl.append(t)
            aux = JoinBlock([Service('<'+s+'>', UnionBlock([JoinBlock(list(tl))]))]) 
            args.append(aux)
        if ts != None:
            for tp in ts:
                covered.add(tp)
        if len(args) > 1:
            return UnionBlock(args)
        elif len(args) == 1:
            return aux[0]
    return None

def getConnectedTriples(triples):

    connected = []
    for t in triples:
        c = getConnected(t, triples)
        toRemove = []
        add = True
        for d in connected:
            if c.issuperset(d):
                toRemove.append(d)
            elif d.issuperset(c):
                add = False
                break
        for d in toRemove:
            connected.remove(d)
        if add and not(c in connected):
            connected.append(c)
    return connected

def getGroups(l, tl, genPred, prefixes, decomposition, c, selectedSources, replace):

    if decomposition == "EG":
        (g, f) = getExclusiveGroups(l, tl, prefixes, selectedSources)
        if g and f:
            f.insert(0, g)
            return f
        elif g:
            return [g]
        elif f:
            return f
    elif decomposition == "SSGS":
        r = getStarsS(l, tl, genPred, prefixes, c, selectedSources)
        # print r
        if r:
            return [r]
        else:
            return r
    elif decomposition == "SSGM":
        (g, f) = getStarsM(l, tl, genPred, prefixes, c, selectedSources, replace)
        if g and f:
            f.insert(0, g)
            return f
        elif g:
            #return [g]
            return g
        elif f:
            return f
    return []

def includeFilter(jb_triples, fl):
    fl1=[]
    for jb in jb_triples:
      if isinstance(jb, list):
        for f in fl:
            fl2=includeFilterAux(f, jb)
            fl1=fl1+fl2
      elif (isinstance(jb,UnionBlock)):
        for f in fl:
            fl2=includeFilterUnionBlock(jb,f)
            fl1=fl1+fl2
      elif (isinstance(jb,Service)):
        for f in fl:
            fl2=includeFilterAuxS(f, jb.triples,jb)
            fl1=fl1+fl2
    return fl1

def includeFilterUnionBlock(jb,f):
    fl1=[]
    for jbJ in jb.triples:
      for jbUS in jbJ.triples:
        if isinstance(jbUS,Service):
           vars_s = set(jbUS.getVars())
           vars_f = f.getVars()
           if set(vars_s) & set(vars_f) == set(vars_f):
              jbUS.include_filter(f)
              fl1=fl1 + [f]
    return fl1 

def includeFilterAuxS(f, sl,sr):
    fl1=[]
    serviceFilter=False
    for s in sl:
      vars_s = set()
      if (isinstance(s,Triple)):
          vars_s.update(set(getVars(s)))
      else:
          for t in s.triples:
              vars_s.update(set(getVars(t)))
      vars_f = f.getVars()
      if set(vars_s) & set(vars_f) == set(vars_f):
         serviceFilter=True
    if serviceFilter:
         sr.include_filter(f)
         fl1=fl1 + [f]
    return fl1


def includeFilterAux(f, sl):
    fl1=[]
    for s in sl:
      vars_s = set()
      for t in s.triples:
        #print "t" + str(t)   
        vars_s.update(set(getVars(t)))
      vars_f = f.getVars()
      #print "vars_f" +str(vars_f)
      if set(vars_s) & set(vars_f) == set(vars_f):
         s.include_filter(f)
         fl1=fl1 + [f]
    return fl1
def makePlanQuery(q, plan):
    x = makePlanUnionBlock(q.body, plan)
    return x

def makePlanUnionBlock(ub, plan):
    r = []
    #print "makePlanUnionBlock " + " ".join(map(str,ub.triples))
    for jb in ub.triples:
        r.append(makePlanJoinBlock(jb, plan))
    return UnionBlock(r,ub.filters)

def makePlanJoinBlock(jb, plan):
    sl = []
    ol = []
    for bgp in jb.triples:
        #print 'bgp: ', bgp
        #print type(bgp)
        if type(bgp) == list:
            sl.extend(bgp)
        elif isinstance(bgp, Optional):
            ol.append(Optional(makePlanUnionBlock(bgp.bgg, plan)))
        elif isinstance(bgp, UnionBlock):
            sl.append(makePlanUnionBlock(bgp, plan))
        elif isinstance(bgp, JoinBlock):
            sl.append(makePlanJoinBlock(bgp, plan))
        elif isinstance(bgp, Service):
            sl.append(bgp)
        #print 'sl: ', sl
    pl = makePlanAux(sl, plan,jb.filters)
    if ol:
       pl = [pl]
       pl.extend(ol)
    return JoinBlock(pl,filters=jb.filters)

###############################################################################
# Making Unitary Star (used in previous version)
###############################################################################
def getUnitaryStars(l, tl, genPred, prefixes):

    (qcl0, qcl1) = assignEndpoint(tl, l, genPred, prefixes)

    views0 = []
    views1 = []
    for cl in qcl0:
        l0 = qcl0[cl]
        vs = formStars2(l0)
        serv = [Service(cl, view) for view in vs]
        views0 = views0 + serv
    for t in qcl1:
        eps = qcl1[t]
        elems = [JoinBlock([Service(ep, t)]) for ep in eps]
        ub = UnionBlock(elems)
        views1 = views1 + [ub]
    return (views0, views1)

def formStars2(l):

    v = []
    for e in l:
        v = v+[[e]]
    return v

def sameNameSpace(c, predicateList):

    q = 0
    for p in predicateList:
        if shareNS(p, c):
            q = q + 1
    return q

def shareNS(p, c):

    return nameSpace(p) == nameSpace(c)

def nameSpace(uri):

    tail = string.lstrip(uri, "<http://")
    pos = string.find(tail, "/")
    return tail[0:pos]

def readGeneralPredicates(fileName):

    f = open(fileName, 'r')
    l = []
    l0 = f.readline()
    while not l0 == '':
        l0 = string.rstrip(l0, '\n')
        l.append(l0)
        l0 = f.readline()
    f.close()
    return l

# def getUri(p, prefs):
#     hasPrefix = prefix(p)
#     if hasPrefix:
#         (pr, su) = hasPrefix
#         n = prefs[pr]
#         n = n[:-1]+su+">"
#         return n
#     return p.name

# returns the list of endpoints that provides predicate 'pred'
# def search(l, pred, prefs):

#     r = []
#     p = getUri(pred, prefs)
#     for (epn, epl) in l:
#         if p in epl or (not pred.constant):
#             r.append(epn)

#     return r

# return the list of endpoints that are relevant providers of 'name'
def domainProviders (l, name, genPred, prefs):
    r = []
    for (epn, epl) in l:
        if relevant(epl, name, genPred, prefs):
            r.append(epn)
    return r

def getVars(sg):

    s = []
    if not sg.subject.constant:
        s.append(sg.subject.name)
    if not sg.theobject.constant:
        s.append(sg.theobject.name)
    return s

# return a list with the other elements of 'triples' that shares at least one variable
# with 'triple'
def potentialStar(triple, triples):

    vs = getVars(triple)
    ps = []
    for t0 in triples:
        if triple == t0:
            continue
        vs0 = getVars(t0)
        if len(set(vs) & set(vs0)) > 0:
            ps.append(t0)

    return ps

def potentialStarS(triple, triples):

    ps = []
    for t0 in triples:
        if triple == t0:
            continue
        if ((not triple.subject.constant) and (not t0.subject.constant)
            and (triple.subject.name == t0.subject.name)):
          ps.append(t0)
    return ps

def potentialStarC(triple, triples):

    ps = []
    for t0 in triples:
        if triple == t0:
            continue
        if (((not triple.subject.constant) and (not t0.subject.constant) and (triple.subject.name == t0.subject.name))
              or
            ((not triple.subject.constant) and (not t0.theobject.constant) and (triple.subject.name == t0.theobject.name))
              or
            ((not triple.theobject.constant) and (not t0.subject.constant) and (triple.theobject.name == t0.subject.name))
              or
            ((not triple.theobject.constant) and (not t0.theobject.constant) and (triple.theobject.name == t0.theobject.name))):
            ps.append(t0)
    return ps


# returns the list of endpoints that offers the triples contained in 'ps'
# 'at' is a dictionary that contains the information about already assigned
# triples.
def getEndpoints(ps, at):

    r = []
    for t in ps:
        for ep in at:
            if t in at[ep]:
                r.append(ep)
    return r

def getMostCommon(es):

    if es == []:
        return []
    ec = [(es.count(e), e) for e in es]
    ec.sort()
    (c, v) = ec[-1]
    mc = [elem for (cant, elem) in ec if cant == c]

    return list(set(mc))
#MEV added prefs
def relevant(ls, pred, gps,prefs):
    p = getUri(pred, prefs) 
    sns = sameNameSpace(p, ls)
    gns = len(set(ls)-set(gps))
    return sns >= gns*0.5

def isURI(name):
    return string.find(name, '<http://') == 0 and string.rfind(name, '>') == len(name)-1

def getPrefs(ps):
    prefDict = dict()
    for p in ps:
         pos = p.find(":")
         c = p[0:pos].strip()
         v = p[(pos+1):len(p)].strip()
         prefDict[c] = v
    return prefDict

# def prefix(p):
#     s = p.name
#     pos = s.find(":")
#     if (not (s[0] == "<")) and pos > -1:
#         return (s[0:pos].strip(), s[(pos+1):].strip())

#     return None

def assignEndpoint2(tl, l, prefixes, selectedSources):
    qcl0 = collections.defaultdict(list)
    qcl1 = collections.defaultdict(list)

    for sg in tl:
        eps0 = list(selectedSources[sg]) #['<'+ss+'>' for ss in selectedSources[sg]] #search(l, sg, prefixes)
        if eps0 == []:
            return ([], [])
        elif len(eps0) == 1:
            p = eps0[0]
            qcl0[p].append(sg)
        else:
            qcl1[sg].extend(eps0)
    return (qcl0, qcl1)

def assignEndpointS(tl, l, genPred, prefixes, c, selectedSources):

    qcl = collections.defaultdict(list)
    ts = list(tl)

    for sg in tl:
        eps0 = list(selectedSources[sg]) #['<'+ss+'>' for ss in selectedSources[sg]] #search(l, sg, prefixes)
        ll = [ (k,m) for (k,m) in l if k in eps0 ]
        eps = eps0
        if eps == []:
            return []
        elif len(eps) == 1:
            p = eps[0]
            qcl[p].append(sg)
            ts.remove(sg)
            continue
        elif (not (getUri(sg.predicate, prefixes) in genPred)) and (sg.predicate.constant):
            ps = domainProviders(ll, sg.predicate, genPred, prefixes)
            if (len(ps) == 1):
                p = ps[0]
                qcl[p].append(sg)
                ts.remove(sg)
                continue
            elif (len(ps) > 1):
                eps = ps
        ps = []
        on = sg.theobject.name
        sn = sg.subject.name
        isLink = (getUri(sg.predicate, prefixes) in ['<http://www.w3.org/2002/07/owl#sameAs>', '<http://www.w3.org/2000/01/rdf-schema#seeAlso>'])
        if ((not isLink) and (isURI(getUri(sg.theobject, prefixes) or sg.theobject.constant))):
            ps = domainProviders(ll, sg.theobject, genPred, prefixes)
        if ((not isLink) and (isURI(getUri(sg.subject, prefixes) or sg.subject.constant))):
            ps.extend(domainProviders(ll, sg.subject, genPred, prefixes))
        ps = [e for e in ps if e in eps0]
        if (len(ps) == 1):
            p = ps[0]
            qcl[p].append(sg)
            ts.remove(sg)
            continue
        elif len(ps) > 1:
            eps = ps

    for sg in ts:
        ps = list(selectedSources[sg]) #['<'+ss+'>' for ss in selectedSources[sg]] #search(l, sg, prefixes)
        eps0 = ps
        ll = [ (k,m) for (k,m) in l if k in eps0 ]
        ps1 = []
        if (not (getUri(sg.predicate, prefixes) in genPred)) and sg.predicate.constant:
            ps1 = domainProviders(ll, sg.predicate, genPred, prefixes)
        if len(ps1) > 0:
            ps = ps1
        on = sg.theobject.name
        sn = sg.subject.name
        ps2 = []
        isLink = (getUri(sg.predicate, prefixes) in ['<http://www.w3.org/2002/07/owl#sameAs>', '<http://www.w3.org/2000/01/rdf-schema#seeAlso>'])
        if ((not isLink) and (isURI(getUri(sg.theobject, prefixes) or sg.theobject.constant))):
            ps2.extend(domainProviders(ll, sg.theobject, genPred, prefixes))
        if ((not isLink) and (isURI(getUri(sg.subject, prefixes) or sg.subject.constant))):
            ps2.extend(domainProviders(ll, sg.subject, genPred, prefixes))
        ps2 = [e for e in ps2 if e in eps0]
        if len(ps1) == 0 and len(ps2) > 0:
            ps = ps2
        else:
            ps.extend(ps2)
        ps3 = getMostCommon(getEndpoints(potentialStarS(sg, tl), qcl))
        ps3 = [e for e in ps3 if e in eps0]

        if len(ps3) == 1:
            p = ps3[0]
            qcl[p].append(sg)
            continue
        if len(ps1) == 0 and len(ps2) == 0 and len(ps3) > 0:
            ps = ps3
        else:
            ps.extend(ps3)
        p = selectCurrentBest(ps, sg, qcl, prefixes, genPred, c)
        if len(p) == 0:
            print "there are no options for " + tr(sg.predicate)
            print "triples "+str(tl)
            print "ps: "+str(ps)
            print "qcl: "+str(qcl)
            print "c: "+str(c)
        p = p[0]
        qcl[p].append(sg)
    return qcl

def useSelectedSources(selectedSources, qcl0, qcl1):

    for tp in selectedSources:
        endpoints = selectedSources[tp]
        if len(endpoints) > 1:
            qcl1[tp].extend(endpoints)
        elif len(endpoints) == 1:
            for e in endpoints:
                break
            qcl0[e].append(tp)            

def assignEndpointM(tl, l, genPred, prefixes, c, selectedSources, replace):
    qcl0 = collections.defaultdict(list)
    qcl1 = collections.defaultdict(list)
    ts = list(tl)
    if replace:
        useSelectedSources(selectedSources, qcl0, qcl1)
        return (qcl0, qcl1)
    for sg in tl:
        eps0 = list(selectedSources[sg]) #['<'+ss+'>' for ss in selectedSources[sg]] #search(l, sg, prefixes)
        eps = eps0
        if eps == []:
            return ([], [])
        elif len(eps) == 1:
            p = eps[0]
            qcl0[p].append(sg)
            ts.remove(sg)
            continue
    for sg in ts:
        ps = list(selectedSources[sg]) #['<'+ss+'>' for ss in selectedSources[sg]] #search(l, sg, prefixes)
        eps0 = ps
        ll = [ (k,m) for (k,m) in l if k in eps0 ]
        ps1 = []
        if (not (getUri(sg.predicate, prefixes) in genPred)) and sg.predicate.constant:
           ps1 = domainProviders(ll, sg.predicate, genPred, prefixes)
        if len(ps1) > 0:
            ps = ps1
        on = sg.theobject.name
        sn = sg.subject.name
        ps2 = []
        isLink = (getUri(sg.predicate, prefixes) in ['<http://www.w3.org/2002/07/owl#sameAs>', '<http://www.w3.org/2000/01/rdf-schema#seeAlso>'])
        if ((not isLink) and (isURI(getUri(sg.theobject, prefixes) or sg.theobject.constant))): 
            ps2.extend(domainProviders(ll, sg.theobject, genPred, prefixes))
        if ((not isLink) and (isURI(getUri(sg.subject, prefixes) or sg.subject.constant))): 
            ps2.extend(domainProviders(ll, sg.subject, genPred, prefixes))
        ps2 = [e for e in ps2 if e in eps0]
        ps.extend(ps2)
        ps3 = getMostCommon(getEndpoints(potentialStarS(sg, tl), qcl0))
        ps3 = [e for e in ps3 if e in eps0]
        if len(ps1) == 0 and len(ps2) == 0 and len(ps3) > 0:
           ps = ps3
        else:
           ps.extend(ps3)
        p = selectCurrentBest(ps, sg, qcl0, prefixes, genPred, c)
        if len(p) == 1:
             p = p[0]
             qcl0[p].append(sg)
        elif len(p) == 0:
             p = eps0[0]
             qcl0[p].append(sg)
        else:
             qcl1[sg].extend(p)
    return (qcl0, qcl1)

def selectCurrentBest(options, triple, qcl, ps, genPred, c):

    added = False
    currentOptions = []
    for ep in qcl:
       if ep in options:
           nl = list(qcl[ep])
           if shareWithAny(triple, nl):
               nl.append(triple)
               if test(ep, nl, ps, c):
                   currentOptions.append(ep)
                   added = True
    if not added or not (getUri(triple.predicate, ps) in genPred or not triple.predicate.constant):
      for ep in options:
       nl = [triple]
       #Avoid ask's of non-instantiated triple patterns
       if not(triple.subject.constant or triple.predicate.constant or triple.theobject.constant) and not ep in currentOptions:
       #    print triple.subject.name, triple.predicate.name, triple.theobject.name
           currentOptions.append(ep)
       elif test(ep, nl, ps, c) and not ep in currentOptions:
           #print "yes"
           currentOptions.append(ep)
    return currentOptions

def getQuery(ts, ps):

    q = "ASK { "
    for t in ts:
        p = getUri(t.predicate, ps)
        s = getUri(t.subject, ps)
        o = getUri(t.theobject, ps)
        q = q + s + " " + p + " " + o + " ."
    q = q + " }"
    return q

# def test(endpoint, triples, ps, c):

#     query = getQuery(triples, ps)
#     server = endpoint[1:-1]
#     q = Queue()
#     b = c(server, query, q)
#     return b

def getExclusiveGroups(l, tl, prefixes, selectedSources):
    (qcl0, qcl1) = assignEndpoint2(tl, l, prefixes, selectedSources)
    views0 = []
    views1 = []
    for cl in qcl0:
        l0 = qcl0[cl]
        serv = Service(cl, UnionBlock([JoinBlock(l0)]))
        views0 = views0 + [serv]
    for t in qcl1:
        eps = qcl1[t]
        elems = [JoinBlock([Service(ep, UnionBlock([JoinBlock([t])]))]) for ep in eps]
        ub = UnionBlock(elems)
        views1 = views1 + [ub]
    return (views0, views1)

def getStarsS(l, tl, genPred, prefixes, c, selectedSources):

    qcl = assignEndpointS(tl, l, genPred, prefixes, c, selectedSources)
    views = []
    for cl in qcl:
        l0 = qcl[cl]
        vs = formStars(l0)
        serv = [Service(cl, UnionBlock([JoinBlock(view)])) for view in vs]
        views = views + serv
    return postp2(views)

def getStarsM(l, tl, genPred, prefixes, c, selectedSources, replace):
    (qcl0, qcl1) = assignEndpointM(tl, l, genPred, prefixes, c, selectedSources, replace)
    views0 = []
    views1 = []
    #print qcl0
    #print qcl1
    for cl in qcl0:
        l0 = qcl0[cl]
        vs = formStars(l0)
        serv = [Service(cl, UnionBlock([JoinBlock(view)])) for view in vs] # UnionBlock([JoinBlock(view)]))
        views0 = views0 + serv
    for t in qcl1:
        eps = qcl1[t]
        elems = [JoinBlock([Service(ep, UnionBlock([JoinBlock([t])]))]) for ep in eps] # UnionBlock([JoinBlock([t])])
        ub = UnionBlock(elems)
        views1 = views1 + [ub]
    return (postp2(views0), views1)

def postp2(ss):

   r = []

   for s in ss:
       #print 's', s
       #print 'r', r
       (subsets, supersets) = check(s, r)
       if len(subsets) > 0:
           for t in subsets:
               r.remove(t)
           r.append(s)
       elif len(supersets) == 0 and len(subsets) == 0:
           r.append(s)
   return r

def check(s, sl):

    subsets = []
    supersets = []

    for e in sl:
        if subListB(s.triples, e.triples):
           supersets.append(e)
        elif subListB(e.triples, s.triples):
           subsets.append(e)

    return (subsets, supersets)

def takeBest(p, cs):
    ps = []
    for e in p:
        if type(e) == list:
            pss = [i for ee in e for i in potentialStarC(ee, cs)]
            ps.append((len(set(pss)), e))
        else:
            ps.append((len(potentialStarC(e, cs)), e))
    ps.sort()
    if len(ps) > 0:
        (c, v) = ps[-1]
        return v
    else:
        return None

###############################################################################
# Making stars
###############################################################################
def formStars(l):

    cs = []
    v = []
    for e in l:
        if e.theobject.constant:
            cs = cs +[e]
        else:
            v = v+[[e]]

    r = False

    while not r:
        r = True
        nv = []
        p = []
        for a in v:
          for b in v:
            if a == b:
                continue
            if everyoneShareOneVar(a,b) and (not manyEqual(a, b)):
                p.append(b)
          b = takeBest(p, cs)
          if b:
            d = a + b
            nv.append(d)
            v.remove(a)
            v.remove(b)
            r = False
            break
        v = v + nv

    r = False
    ae = []
    for (a, b) in combinations(v, r=2):
        if subList(a, b):
            if not a in ae:
                ae.append(a)
        elif subList(b, a):
            if not b in ae:
                ae.append(b)
    for e in ae:
        v.remove(e)
    addConstants(v, cs)
    postp(v)

    return v

def postp(v):

    e = []
    aux = v
    for x in v:
        if len(x) ==  1 :
            aux.remove(x)
            b = include(x[0], aux)
            if b:
                e.append(x)
            else:
                aux.append(x)

def include(x, v):
    v0 = x.subject
    v1 = x.theobject
    for e in v:
        if inAnyOne(v0, e):
            e.append(x)
            return True
        elif inAnyOne(v1, e):
            e.append(x)
            return True
    return False

def addConstants(vs, cs):

    ti = []
    for c in cs:

        done = False
        for v in vs:
            if inAnyOne(c.subject, v):
                if not c in v:
                    v.insert(0, c)
                done = True
        if not done and not c in ti:
            ti.append(c)

    for x in ti:
        vs.append([x])

def everyoneShareOneVar(a, b):

    for e in a:
        if inEveryOne(e.subject, a) and inEveryOne(e.subject, b):
            if (not (not e.theobject.constant and inEveryOne(e.theobject, a)
                   and inEveryOne(e.theobject, b))):
                return True
        if (not e.theobject.constant and inEveryOne(e.theobject, a)
               and inEveryOne(e.theobject, b)):
            if not (inEveryOne(e.subject, a) and inEveryOne(e.subject, b)):
                return True
    return False

def manyEqual(a, b):

    c = list(a)
    d = list(b)
    c.extend(d)

    for (e, f, g) in permutations(c, 3):
        if samePattern(e, f) and samePattern(f, g):
            return True
    return False

def samePattern(a, b):
    return (((not a.subject.constant) and (not b.subject.constant))
            and
           ((not a.theobject.constant) and (not b.theobject.constant))
            and
           (((not a.predicate.constant) and (not b.predicate.constant))
             or
            (a.predicate == b.predicate)))

def shareWithAny(t, l):
    v0 = t.subject
    v1 = t.theobject
    if inAnyOne(v0, l):
        return True
    elif inAnyOne(v1, l):
        return True
    return False

def inAnyOne(v, a):

    b = False
    for e in a:

        b = e.subject == v or e.theobject == v

        if b:
            break
    return b

def inEveryOne(v, a):

    b = True
    for e in a:
        b = e.subject == v or e.theobject == v
        if not b:
            break
    return b

def subListB(a, b):
    a = a.triples
    b = b.triples
    for e in a:
        if not e in b:
            return False
    return True

def subList(a, b):

    for e in a:
        if not e in b:
            return False
    return True
###############################################################################

def decompose(qString, eFile, decomposition, contact):

    with open(eFile) as efile:
        endpointList = parseEndpoints.parse(efile)
    query = parseQuery.parse(qString)
    groups = decomposeQuery (endpointList, query, decomposition, contact)
    if groups == None:
        return None
    if groups == []:
        return None
    query.body = groups
    #print type(groups)
    query.join_vars = query.getJoinVars()
    #print query.join_vars
    logger.info('Decomposition Obtained')
    logger.info(query)
    #print 'decomposition'
    #print query
    return query

def makeBushyTree(ls,filters=[]):

    return Tree.makeBushyTree(ls,filters)

def makeNaiveTree(ls):

    return Tree.makeNaiveTree(ls)

def makeLeftLinealTree(ls):

    return Tree.makeLLTree(ls)

def makePlan(qString, eFile, decomposition, plan, contact, cf):
    global confFile
    confFile = cf
    q = decompose(qString, eFile, decomposition, contact)
    if (q == None):
      return None
    q.body = makePlanQuery(q, plan)
    return q

def makePlan2(qString, plan):

    q = parseQuery1_1.parse(qString)
    q.body = makePlanQuery(q, plan)
    return q

def makePlanAux(ls, plan,filters=[]):
    if plan == "b":
        return makeBushyTree(ls,filters)
    elif plan == "naive":
        return makeNaiveTree(ls)
    elif plan == "ll":
        return makeLeftLinealTree(ls)
    elif (plan == "d") or (plan == "p"):
        return ls
    return None
