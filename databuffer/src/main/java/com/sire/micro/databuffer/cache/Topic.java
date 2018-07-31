package com.sire.micro.databuffer.cache;

import android.support.annotation.IntDef;
import android.support.annotation.LongDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Topic {
    public static final long INFINITE = -1;
    private long expiredTime;
    private int maxSize;
    private int topic;

    public Topic(int topic, int maxSize,long expiredTime) {
        this.expiredTime = expiredTime;
        this.maxSize = maxSize;
        this.topic = topic;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getTopic() {
        return topic;
    }

    public void setTopic(int topic) {
        this.topic = topic;
    }

    @LongDef({INFINITE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EXPIRED {

    }
}
