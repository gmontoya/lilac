package com.fluidops.fedx.optimizer;

import java.util.*;

class EndpointSummary {
    
    HashMap<String, Capability> capabilities;
    
    public EndpointSummary() {

        this.capabilities = new HashMap<String, Capability>();
    }
    
    public void addCapability(String p, Capability c) {
        this.capabilities.put(p, c);
    }
    
    public String toString() {
        
        return capabilities.toString();
    }
    
    public Capability getCapability(String p) {
        
        return capabilities.get(p);
    }
}
