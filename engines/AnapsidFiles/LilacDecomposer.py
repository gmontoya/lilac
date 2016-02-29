from utils import *
from collections import OrderedDict
import sys
import os
import parseQuery
from SPARQLWrapper import SPARQLWrapper, JSON
from SPARQLWrapper.Wrapper import QueryResult
import rdflib
from rdflib.plugins.sparql import prepareQuery
from rdflib.term import URIRef, Literal, Variable
import parseEndpoints, parseQuery
from services import Argument, Triple
import random

def obtainInstance(candidateSources, elements, collections):
        endpoints = {}
        for t in candidateSources:
            fs = candidateSources[t]
            elements.add(t)
            for f in fs:
                for e in f:
                    if e in endpoints:
                        ts = endpoints[e]
                    else:
                        ts = set()
                    ts.add(t)
                    endpoints[e] = ts
        for e in endpoints:
            i = 0
            triples = endpoints[e]
            triplesAux = set(triples)
            while len(triplesAux) > 0:
                for x in triplesAux:
                    ts = x
                    break
                c = getConnected(ts, triples)
                collections[(e, i)] = c
                for x in c:
                    triplesAux.remove(x)
                i = i + 1

def getConnected(t, triples):
        #print 'getConnected. t: '+str(t)+' triples: '+str(triples)
        triplesAux = set(triples)
        connected = set()
        connected.add(t)
        size = 0
        while size != len(connected):
            size = len(connected)
            toRemove = set()
            for tAux in triplesAux:
                if joinAny(tAux, connected):
                    connected.add(tAux)
                    toRemove.add(tAux)
            for tAux in toRemove:
                triplesAux.remove(tAux)
        return connected

def joinAny(t, triples):
    #print 'inside joinAny'
    joinExists = False
    for tAux in triples:
        if join(t, tAux):
            joinExists = True
            break
    return joinExists

def join(t1, t2):
    varsT1 = set()
    varsT2 = set()

    varsT1.add(t1.subject.name)
    if not(t1.predicate.constant):
        varsT1.add(t1.predicate.name)
    varsT1.add(t1.theobject.name)

    varsT2.add(t2.subject.name)
    if not(t2.predicate.constant):
        varsT2.add(t2.predicate.name)
    varsT2.add(t2.theobject.name)

    varsT1.intersection_update(varsT2)
    return (len(varsT1) > 0)

def getMinimalSetCovering(elements, collections, r):
        elementsToCover = { x for x in elements }
        selected = {}
        while len(elementsToCover) > 0:
            c = []
            for k in collections:
                if (len(c) == 0): 
                    c.append(k)
                else:
                    tmp1 = { x for x in elementsToCover if x in collections[c[0]] }
                    tmp2 = { x for x in elementsToCover if x in collections[k] }
                    if (len(tmp1) < len(tmp2)):
                        c = [ k ]
                    elif len(tmp1) == len(tmp2):
                        c.append(k)
            if r:
                e = random.choice(c)
            else: 
                e = c[0]
            elementsToCover = { x for x in elementsToCover if not(x in collections[e])}
            selected[e] = collections[e]
            del collections[e]
        return selected

def select(candidateSources, selected):
        ss = {}
        ssAux = {}
        for s in selected:
            endpoint = s[0]
            #print 'endpoint: '+str(endpoint)
            triples = selected[s]
            for t in triples:
                if t in ssAux:
                    endpoints = ssAux[t]
                else:
                    endpoints = set()
                if not(endpoint in endpoints):
                    endpoints.add(endpoint)
                ssAux[t] = endpoints
        #print 'ssAux: '+str(ssAux)
        for t in candidateSources:
            fs = candidateSources[t]
            tSelected = ssAux[t]
            es = set()
            for f in fs:
                newSelected = set(tSelected)
                newSelected.intersection_update(f)
                es.add(frozenset(newSelected))
            ss[t] = es
        return ss

def filter(candidateSources, bgp):
    result = {}
    for t in candidateSources:
        if t in bgp:
            result[t] = candidateSources[t]
    return result

def update(candidateSources, selected):
    for t in candidateSources:
        if t in selected:
            candidateSources[t] = selected[t]


def getTriples(query):
        a = query.algebra
        ts = getTriplesRec(a)
        return ts

def getTriplesRec(a):
        #print str(a.name)
        #print str(type(a))
        if (a.name == 'Union') or (a.name == 'Join') or (a.name == 'LeftJoin'):
            return getTriplesRec(a.p1) + getTriplesRec(a.p2)
        elif (a.name == 'Minus'):
            return getTriplesRec(a.p1)
        elif (a.name == 'BGP'):
            return a.triples
        elif (a.name == 'ConstructQuery') or (a.name == 'SelectQuery') or (a.name == 'Filter') or (a.name == 'Project') or (a.name == 'Group') or (a.name == 'OrderBy') or (a.name == 'ToMultiSet') or (a.name == 'Graph'):
            return getTriplesRec(a.p)
        else:
            return []

class TriplePatternFragment:
    def __init__(self, t, ds):
        self.triple = t
        self.dataset = ds
        self.endpoints = []

    def addSource(self, e):
        self.endpoints.append(e)

    def getDataset(self):
        return self.dataset

    def contains(self, other):
        c = isinstance(other, TriplePatternFragment)
        #print 'other is TPF? '+str(c)
        c = c and (self.dataset == other.dataset)
        #print 'same dataset? '+str(c)
        c = c and self.weaker(self.triple.predicate, other.triple.predicate)
        c = c and self.weaker(self.triple.subject, other.triple.subject)
        c = c and self.weaker(self.triple.theobject, other.triple.theobject)
        return c

    def containedBy(self, other):
        c = isinstance(other, TriplePatternFragment)
        c = c and (self.dataset == other.dataset)
        c = c and self.weaker(other.triple.predicate, self.triple.predicate)
        c = c and self.weaker(other.triple.subject, self.triple.subject)
        c = c and self.weaker(other.triple.theobject, self.triple.theobject)
        return c

    def canAnswer(self, triple):                                               
        c = isinstance(triple, Triple)
        c = c and self.compatible(triple.predicate, self.triple.predicate)
        c = c and self.compatible(triple.subject, self.triple.subject)
        c = c and self.compatible(triple.theobject, self.triple.theobject)
        return c

    def containsTriple(self, triple):
        c = isinstance(triple, Triple)
        c = c and self.weaker(self.triple.predicate, triple.predicate)
        c = c and self.weaker(self.triple.subject, triple.subject)
        c = c and self.weaker(self.triple.theobject, triple.theobject)
        return c

    def exactMatch(self, triple): 
        m = isinstance(triple, Triple)
        m = m and self.exactM(triple.predicate, self.triple.predicate)           
        m = m and self.exactM(triple.subject, self.triple.subject)               
        m = m and self.exactM(triple.theobject, self.triple.theobject)
        return m

    def weaker(self, t1, t2):
        #print 't1 constant: '+str(t1.constant)+' t1 name: '+str(t1.name)
        #print 't2 constant: '+str(t2.constant)+' t2 name: '+str(t2.name)
        b = (not t1.constant or (t2.constant and (t1.name == t2.name)))
        #print 't1 weaker than t2: '+str(b)
        return b

    def compatible(self, t1, t2):
        b = not (t1.constant and t2.constant) or (t1.name == t2.name)
        return b

    def exactM(self, t1, t2):                                                     
        b = (not t1.constant and not t2.constant) or (t1.constant and t2.constant and (t1.name == t2.name))
        return b

    def getAllSources(self):
        return self.endpoints

    def __eq__(self, other):
        e = not (other == None) and isinstance(other, TriplePatternFragment)
        e = e and self.dataset == other.dataset
        e = e and self.exactM(self.triple.predicate, other.triple.predicate)
        e = e and self.exactM(self.triple.subject, other.triple.subject)
        e = e and self.exactM(self.triple.theobject, other.triple.theobject)
        return e

    def __hash__(self):
        predicate = None
        subject = None
        object = None
        if (self.triple.predicate.constant):
            predicate = self.triple.predicate
        if (self.triple.subject.constant):
            subject = self.triple.subject
        if (self.triple.theobject.constant):
            object = self.triple.theobject
        return (hash((self.dataset, predicate, subject, object)))

    def __repr__(self):
        return '<'+str(self.triple)+', '+str(self.dataset)+', '+str(self.endpoints)+'>'

class LilacDecomposer:

    def __init__(self, tl, el, props, ps):
        self.tripleList = []
        for t in tl:
            ns = Argument(getUri(t.subject, ps), t.subject.constant)
            np = Argument(getUri(t.predicate, ps), t.predicate.constant)
            no = Argument(getUri(t.theobject, ps), t.theobject.constant)
            self.tripleList.append(Triple(ns, np, no))
        self.endpointsList = el
        self.selectedSources = {}
        self.fns = []
        self.loadIndex(props['PredicateIndex'])
        self.loadFragmentDefinitions(props['FragmentsDefinitionFolder'], props['FragmentsSources'])
        self.loadEndpoints(props['EndpointsFile'])
        self.random = (props['Random'] == 'true')
        #self.loadIndex(props['PredicateIndex'])
        self.options = {}
        #print 'end of the constructor'
        #print 'fragments: '+str(self.fragments)
        #print 'PredicateIndex: '+str(self.predicateIndex)

    def getSelectedSources(self):
        return self.selectedSources

    def getOptions(self):
        return self.options

    def getEndpoints(self, selectedFragments):
        fs = set()
        #print 'self.endpointsList: '+str(self.endpointsList)
        for fragmentList in selectedFragments:
            f = set()
            for fragment in fragmentList:
                l = fragment.getAllSources()
                #print 'l: '+str(l)
                #print '[s[0] for s in self.endpointsList]: '+str([s[0] for s in self.endpointsList])
                f = [s for s in l if '<'+s+'>' in { f for (f, s) in self.endpointsList}]
            #print 'f: '+str(f)
            fs.add(frozenset(f))
        return fs

    def sourceSelectionPerTriple(self):
        #print 1
        candidates = {}
        for t in self.tripleList:
            #print 'triple: '+str(t)
            selectedFragments = set()
            # predicate index
            frags = None
            if t.predicate.constant:
                frags = self.predicateIndex[t.predicate.name]
            else:
                frags = self.fns

            for fn in frags:
                f = self.fragments[fn]
                #print 'considering fragment: '+str(fn)
                #print 'f: '+str(f)
                if f.canAnswer(t):
                    #print 'it can answer'
                    redundantFragments = set()
                    toAdd = True
                    includeWith = []
                    for l in selectedFragments:
                        lAux = list(l)
                        f2 = lAux[0]
                        #print 'already included fragment: '+str(f2.triple)
                        #print 'f.containsTriple(t): '+str(f.containsTriple(t))
                        #print 'f2.containsTriple(t): '+str(f2.containsTriple(t))
                        #print 'f.contains(f2): '+str(f.contains(f2))
                        #print 'f.containedBy(f2): '+str(f.containedBy(f2))
                        if (f.containsTriple(t) and f2.containsTriple(t) and (f.contains(f2) or f.containedBy(f2))):
                            includeWith.append(l)
                            toAdd = False
                        elif f.containedBy(f2):
                            toAdd = False
                            break
                        elif f.contains(f2):
                            redundantFragments.add(l)
                    #print 'redundantFragments: '+str(redundantFragments)
                    #print 'toAdd: '+str(toAdd)
                    for l in redundantFragments:
                        selectedFragments.remove(l)
                    for fl in includeWith:
                        if not (f in fl):
                            selectedFragments.remove(fl)
                            s = set(fl)
                            s.add(f)
                            selectedFragments.add(frozenset(s))
                    if toAdd:
                        l = frozenset({f})
                        selectedFragments.add(l)

            endpoints = self.getEndpoints(selectedFragments)
            candidates[t] = endpoints
        return candidates 

    def performSourceSelection(self):
        candidateSources = self.sourceSelectionPerTriple()
        #print 'candidate sources: '+str(candidateSources)
        for t in candidateSources:
            sources = candidateSources[t]
            if len(sources) == 1:
                for x in sources:
                    equivalentSources = x
                    break
                for es in equivalentSources:
                    if es in self.options:
                        optionalStatements = self.options[es]
                    else:
                        optionalStatements = set()
                    optionalStatements.add(t)
                    self.options[es] = optionalStatements

        bgp2 = []
        for t in self.tripleList:
            if len(candidateSources[t]) > 1:
                fragments = candidateSources[t]
                for fragment in fragments:
                    break
                intersection = { f for f in fragment }
                for f in fragments:
                    intersection.intersection_update(f)
                if len(intersection) > 0:
                    fragments = { frozenset(intersection) }
                    candidateSources[t] = fragments
        for t in self.tripleList:
            if len(candidateSources[t]) == 1:
                bgp2.append(t)
        bgpCandidateSources = filter(candidateSources, bgp2)
        elements = set()
        collections = {}
        obtainInstance(bgpCandidateSources, elements, collections)
        collections = OrderedDict(sorted(collections.items()))
        #print 'elements: '+str(elements)
        #print 'collections: '+str(collections)
        selectedSubsets = getMinimalSetCovering(elements, collections, self.random)
        #print 'selectedSubsets: '+str(selectedSubsets)
        #print 'bgpCandidateSources: '+str(bgpCandidateSources)
        bgpCandidateSources = select(bgpCandidateSources, selectedSubsets)
        #print 'bgpCandidateSources: '+str(bgpCandidateSources)
        update(candidateSources, bgpCandidateSources)
        self.selectedSources = candidateSources

    def loadIndex(self, fileName):
        #print "inside loadIndex"
        self.predicateIndex = {}
        predicates = set()
        for t in self.tripleList:
            if t.predicate.constant:
                predicates.add(t.predicate.name)
            else:
                predicates = None
                break
        #print 'predicates: '+str(predicates)
        with open (fileName, 'r') as f:
            
            for line in f:
                #print line
                line = line.strip()
                ws = line.split()
                if (len(ws) == 0):
                    continue
     
                predicate = ws[0]
                fs = set()
                if (predicates != None) and not(('<'+predicate+'>') in predicates):
                    continue
                #print 'predicate! '+str(predicate)
                i = 1
                while i < len(ws):
                    f = ws[i]
                    #print 'f: '+str(f)
                    fs.add(f)
                    i = i + 1
                #print 'fs '+str(fs)
                self.predicateIndex['<'+predicate+'>'] = fs
     
        #print 'predicateIndex: '+str(self.predicateIndex)
    def loadEndpoints(self, fileName):
        with open (fileName, 'r') as f:
            for line in f:
                line = line.strip()
                ws = line.split()
                if (len(ws) > 0):
                    fragment = ws[0]
                    i = 1
                    if not(fragment in self.fragments):
                        continue
                    f = self.fragments[fragment]
                    while (i < len(ws)):
                        endpoint = ws[i]
                        f.addSource(endpoint)
                        i = i + 1

    def loadFragmentDefinitions(self, folder, fileName):
        datasets = {}
        self.fragments = {}
        with open (fileName, 'r') as f:
            for line in f:
                line = line.strip()
                ws = line.split()
                if (len(ws) > 0):
                    fragment = ws[0]
                    ds = ws[1]
                    datasets[fragment] = ds
                    self.fns.append(fragment)
        fs = set()
        for frags in self.predicateIndex.values():
            for f in frags:
                fs.add(f)
        #print 'fs: '+str(fs)
        for f in fs:
            path = folder+'/'+f
            f = open(path)
            viewStr = f.read()
            f.close()
            view = prepareQuery(viewStr)
            t = getTriple(view)
            i = path.rfind('/') + 1
            j = path.rfind('.')
            j = len(path) if j < 0 else j
            name = path[i:j]
            ds = datasets[name]
            self.fragments[name] = TriplePatternFragment(t, ds)
        #print self.fragments
def getTriple(query):
    ts = getTriples(query)
    t = ts[0]
    (s, p, o) = t
    s = Argument(s.n3(), not isinstance(s, Variable))
    p = Argument(p.n3(), not isinstance(p, Variable))
    o = Argument(o.n3(), not isinstance(o, Variable))
    t = Triple(s,p,o)
    return t

#def main(argv):

#if __name__ == '__main__':
#    main(sys.argv)
