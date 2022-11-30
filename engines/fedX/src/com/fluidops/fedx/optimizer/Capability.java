package com.fluidops.fedx.optimizer;

import java.util.*;

class Capability {
    
    long total;
    double averageSubSel;
    double averageObjSel;
    Vector<Long> MIPsVector;
    
    public Capability(long t, double ss, double os, Vector<Long> mv) {
        
        this.total = t;
        this.averageSubSel = ss;
        this.averageObjSel = os;
        this.MIPsVector = mv;
    }
    
    public String toString() {
        
        return "("+total+", "+averageSubSel+", "+averageObjSel+", "+MIPsVector+")";
    }
    
    public Vector<Long> getMIPVector() {
        
        return this.MIPsVector;
    }
    
    public long getTotal() {
        
        return this.total;
    }
    
    public double getAverageSubSel() {
        
        return this.averageSubSel;
    }
    
    public double getAverageObjSel() {
        
        return this.averageObjSel;
    }
}
