package com.sire.micro.databuffer;

import android.support.annotation.RestrictTo;

import java.util.Arrays;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class CacheEntry {
    private int topic;
    private String key;
    private String valueType;
    private byte[] value;
    private long cacheTime;

    public CacheEntry(int topic,String key, String valueType, byte[] value, long cacheTime) {
        this.topic = topic;
        this.key = key;
        this.valueType = valueType;
        this.value = value;
        this.cacheTime = cacheTime;
    }

    public int getTopic() {
        return topic;
    }

    public void setTopic(int topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValueType() {
        return valueType;
    }

    public void setValueType(String valueType) {
        this.valueType = valueType;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
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
        return "CacheEntry{" +
                " topic=" + topic +
                ", key='" + key + '\'' +
                ", valueType='" + valueType + '\'' +
                ", value=" + Arrays.toString(value) +
                ", cacheTime=" + cacheTime +
                '}';
    }
}
