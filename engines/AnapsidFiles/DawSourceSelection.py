from __future__ import division
from rdflib import Graph
from utils import *
import sys 
import math

class DawSourceSelection:

    def __init__(self, ss, props, ps):
        self.selectedSources = ss
        self.threshold = float(props['Threshold'])
        self.loadEndpointSummaries(props['DawIndex'])
        self.prefixes = ps
        #print self.summaries

    def loadEndpointSummaries(self, indexFile):
        self.summaries = {}
        g = Graph()
        g.parse(indexFile, format="turtle")

        queryStr = "PREFIX sd: <http://www.w3.org/ns/sparql-service-description#> \n "
        queryStr += "SELECT DISTINCT ?u ?pred ?tt ?ass ?aos ?mips \n WHERE { ?s a sd:Service . ?s sd:endpointUrl ?u . ?s sd:capability ?c . \n"
        queryStr += "?c <http://www.w3.org/ns/sparql-service-description#predicate> ?pred . \n"
        queryStr += "?c <http://www.w3.org/ns/sparql-service-description#totalTriples> ?tt . \n"
        queryStr += "?c <http://www.w3.org/ns/sparql-service-description#avgSbjSel> ?ass . \n"
        queryStr += "?c <http://www.w3.org/ns/sparql-service-description#avgObjSel> ?aos . \n"
        queryStr += "?c <http://www.w3.org/ns/sparql-service-description#MIPs> ?mips . }"

        queryResult = g.query(queryStr)
        for qr in queryResult:
            #print qr
            #print type(qr)
            url = str(qr['u'].n3())
            pred = str(qr['pred'].n3())
            tt = long(qr['tt'])
            ass = float(qr['ass'])
            aos = float(qr['aos'])
            mips = str(qr['mips'])
            #print 'url: '+str(url)+'. type: '+str(type(str(url)))
            #print 'pred: '+str(pred)+'. type: '+str(type(pred))
            #print 'tt: '+str(tt)+'. type: '+str(type(tt))
            #print 'ass: '+str(ass)+'. type: '+str(type(ass))
            #print 'aos: '+str(aos)+'. type: '+str(type(aos))
            #print 'mips: '+str(mips)+'. type: '+str(type(mips))
            v = parseVector(mips)
            capability = Capability(tt, ass, aos, v)
            if not (url in self.summaries):
                endpointSummary = EndpointSummary()
            else:
                endpointSummary = self.summaries[url]
            endpointSummary.addCapability(pred, capability)
            self.summaries[url] = endpointSummary

    def getSelectedSources(self):

        return self.selectedSources

    def performSourceSelection(self):

        for tp in self.selectedSources:
            capableDatasources = self.selectedSources[tp]
            capableDatasources = sorted(list(capableDatasources))
            #print 'triple: '+str(tp)
            #print 'capable datasources: '+str(capableDatasources)
            bestSources = []
            if len(capableDatasources) > 0:
                bestSources = self.sourceWiseRankingAndSkipping(tp, capableDatasources)
            self.selectedSources[tp] = bestSources

    def getMaxSizeSource(self, capableDatasources, tp):

        endpoint = None
        maxSize = 0
        #print type(tp)
        #print type(tp.predicate)
        p = str(getUri(tp.predicate, self.prefixes))
        #print p

        for endpointUrl in capableDatasources:
            if not (endpointUrl in self.summaries): # Ignore endpoints that are not in the index
                #print 'endpoint '+str(endpointUrl)+' not in the index'
                continue
            endpointSummary = self.summaries[endpointUrl]
            c = endpointSummary.getCapability(p)
            if c == None:  # Ignore predicates that are not in the index
                #print 'predicate: '+str(p)+' not in the index'
                continue
            size = c.getTotal()
            if tp.subject.constant:
                size = size * c.getAverageSubSel()
            elif tp.theobject.constant:
                size = size * c.getAverageObjSel()
            if size > maxSize:
                endpoint = endpointUrl
                maxSize = size
        return endpoint

    def sourceWiseRankingAndSkipping(self, tp, capableDatasources):

        # Daw Source Selection requires bounded predicates
        if not tp.predicate.constant:
            #print 'predicate variable: '+str(tp.predicate)
            return capableDatasources

        predicate = str(getUri(tp.predicate, self.prefixes))
        selectedSource = self.getMaxSizeSource(capableDatasources, tp)
        if selectedSource == None: # the index does not have information about the capable sources
            #print 'no source was selected'
            return capableDatasources

        e = self.summaries[selectedSource]
        #print 'predicate: '+str(predicate)+'. type: '+str(type(predicate))+'. name: '+str(predicate.name)
        #print 'e: '+str(e)
        rankedSources = []
        c = e.getCapability(predicate)
        #print '105,c: '+str(c)
        unionMIPsSetSize = c.getTotal()
        unionMIPs = c.getMIPVector()
        rankedSources.append(selectedSource)
        capableDatasources.remove(selectedSource)

        while len(capableDatasources) > 0:
            selectedSource = None
            maxNewTriples = 0 
            # -sys.maxint - 1

            for cd in capableDatasources:
                e = self.summaries[cd]
                c = e.getCapability(predicate)
                MIPs = c.getMIPVector()
                #print '119,c: '+str(c)
                MIPsSetSize = c.getTotal()

                if tp.subject.constant:
                    MIPsSetSize = MIPsSetSize * c.getAverageSubSel()
                elif tp.theobject.constant:
                    MIPsSetSize = MIPsSetSize * c.getAverageObjSel()
                
                overlapSize = getOverlap(unionMIPs,MIPs, unionMIPsSetSize, MIPsSetSize)
                # how many of the triples accessibles through cd are new ? 
                newTriples = MIPsSetSize - overlapSize
                if newTriples > maxNewTriples:
                    selectedSource = cd
                    maxNewTriples = newTriples
               
            curThresholdVal = maxNewTriples / unionMIPsSetSize
            if curThresholdVal > self.threshold:
                rankedSources.append(selectedSource)
                e = self.summaries[selectedSource]
                c = e.getCapability(predicate)
                selectedMIPs = c.getMIPVector()
                #print '140,c: '+str(c)
                selectedMIPsSize = c.getTotal()
                unionMIPs = makeUnion(unionMIPs, selectedMIPs)
                r = getResemblance(unionMIPs, selectedMIPs)
                unionMIPsSetSize = math.ceil((unionMIPsSetSize + selectedMIPsSize) / (r + 1))
            else:
                break
            capableDatasources.remove(selectedSource)
        return rankedSources

def makeUnion(v1, v2):
    maxSize = max(len(v1), len(v2))
    v3 = []
    resemblance = getResemblance(v1,v2)
    for i in range(maxSize):
        x1 = sys.maxint if len(v1)-1<i else v1[i]
        x2 = sys.maxint if len(v2)-1<i else v2[i]
        if x1 == -1:
            v3.append(x2)
        elif x2==-1:
            v3.append(x1)
        else:
            v3.append(min(x1, x2))
      
    return v3;

def getOverlap(v1, v2, sizeS1, sizeS2):

    resemblance = getResemblance(v1, v2)
    return (resemblance*(sizeS1 + sizeS2))/(resemblance+1)

def getResemblance(v1, v2):

    minSize = min(len(v1), len(v2))
    count = 0
    for i in range(minSize):
        if (v1[i] != -1 and (v1[i] == v2[i])):
            count+=1
    return count/float(minSize)

def parseVector(s):

    v = []
    s = s.strip()
    es = s.split(', ')
    for e in es:
        l = long(e.lstrip('[').rstrip(']'))
        v.append(l)
    return v

class Capability:

    def __init__(self, t, ss, os, mv):
        self.total = t
        self.averageSubSel = ss
        self.averageObjSel = os
        self.MIPsVector = mv

    def __str__(self):
        return "("+str(self.total)+", "+str(self.averageSubSel)+", "+str(self.averageObjSel)+", "+str(self.MIPsVector)+")"

    def __repr__(self):
        return "("+repr(self.total)+", "+repr(self.averageSubSel)+", "+repr(self.averageObjSel)+", "+repr(self.MIPsVector)+")"

    def getMIPVector(self):

        return self.MIPsVector

    def getTotal(self):

        return self.total

    def getAverageSubSel(self):

        return self.averageSubSel

    def getAverageObjSel(self):

        return self.averageObjSel

class EndpointSummary:

    def __init__(self):

        self.capabilities = {}

    def addCapability(self, p, c):

        self.capabilities[p] = c

    def __str__(self):

        return str(self.capabilities)

    def __repr__(self):

        return repr(self.capabilities)

    def getCapability(self, p):

        c = None
        if p in self.capabilities:
            c = self.capabilities[p]
        return c
