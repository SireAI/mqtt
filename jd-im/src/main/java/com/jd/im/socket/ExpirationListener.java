package com.jd.im.socket;

public interface ExpirationListener<E extends SlotElement> {
      
    /** 
     * Invoking when a expired event occurs. 
     *  
     * @param expiredObject 
     */  
    void expired(E expiredObject);  
      
}