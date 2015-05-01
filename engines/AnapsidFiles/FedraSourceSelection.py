from utils import *
import collections
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
        for t in candidateSources:
            fs = candidateSources[t]
            i = 0
            for f in fs:
                elements.add((t, i)) 
                for e in f:
                    if not (e in collections):
                        ts = set()
                    else:
                        ts = collections[e]
                    ts.add((t, i))
                    collections[e] = ts
                i = i + 1

def getMinimalSetCovering(elements, collections, r):
        elementsToCover = { x for x in elements }
        selected = set()
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
            selected.add(e)
            del collections[e]
        return selected

def select(candidateSources, selected):
        ss = {}
        for t in candidateSources:
            fs = candidateSources[t]
            es = set()
            for f in fs:
                nf = set(f)
                nf.intersection_update(selected)
                es.add(frozenset(nf))
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

class FedraSourceSelection:

    def __init__(self, tl, el, props, ps):
        self.tripleList = []
        for t in tl:
            ns = Argument(getUri(t.subject, ps), t.subject.constant)
            np = Argument(getUri(t.predicate, ps), t.predicate.constant)
            no = Argument(getUri(t.theobject, ps), t.theobject.constant)
            self.tripleList.append(Triple(ns, np, no))
        self.endpointsList = el
        self.selectedSources = {}
        self.loadFragmentDefinitions(props['FragmentsDefinitionFolder'], props['FragmentsSources'])
        self.loadEndpoints(props['EndpointsFile'])
        self.random = (props['Random'] == 'true')
        #print 'end of the constructor'
        #print self.fragments

    def getSelectedSources(self):
        return self.selectedSources

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
            for fn in self.fragments:
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
        obtainInstance(candidateSources, elements, collections)
        selectedSubsets = getMinimalSetCovering(elements, collections, self.random)
        bgpCandidateSources = select(bgpCandidateSources, selectedSubsets)
        update(candidateSources, bgpCandidateSources)

        self.selectedSources.clear()
        for t in candidateSources:
            fs = candidateSources[t]
            es = set()
            for f in fs:
                e = random.choice(list(f))
                es.add('<'+str(e)+'>')
            self.selectedSources[t] = es

    def loadEndpoints(self, fileName):
        with open (fileName, 'r') as f:
            for line in f:
                line = line.strip()
                ws = line.split()
                if (len(ws) > 0):
                    fragment = ws[0]
                    i = 1
                    f = self.fragments[fragment]
                    while i < len(ws):
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

        content = os.listdir(folder)
        self.viewsDefinition = {}
        for g in content:
            path = folder+'/'+g
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
