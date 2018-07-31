package com.sire.micro.databuffer.cache;

public class ValueInfor<V> {
    private V value;
    private long cacheTime;

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    @Override
    public String toString() {
        return "ValueInfor{" +
                "value=" + value +
                ", cacheTime=" + cacheTime +
                '}';
    }
}
