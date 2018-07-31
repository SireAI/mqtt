package com.sire.micro.databuffer.cache;

import android.support.annotation.RestrictTo;
import android.support.v4.util.SparseArrayCompat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.sire.micro.databuffer.cache.Topic.INFINITE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/23
 * Author:wangkai
 * Description:内存管理
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class MemoryCache {

    public static final int DEFAULT_TOPIC = -1;
    public static final int MAX_SIZE = 200;
    private final SparseArrayCompat<LruCache> topics;
    private final CacheDelegate cacheDelegate;

    public MemoryCache(CacheDelegate cacheDelegate) {
        topics = new SparseArrayCompat<>();
        this.cacheDelegate = cacheDelegate;
    }

    public final synchronized <V> V put(int topic, String key, V value) {
        if (value == null) {
            return null;
        }
        LruCache<V> lruCache = topics.get(topic);
        if (lruCache == null) {
            buildTopicCache(topic, MAX_SIZE, INFINITE);
            lruCache = topics.get(topic);
        }
        return lruCache.put(key, value);
    }

    public final synchronized <V> V delete(int topic, String key) {
        if (key == null || key.equals("")) {
            return null;
        }
        LruCache<V> lruCache = topics.get(topic);
        if (lruCache == null) {
            return null;
        }
        return lruCache.remove(key);
    }

    public final synchronized <V> V delete(String key) {
        return delete(DEFAULT_TOPIC, key);
    }

    public final <V> V put(String key, V value) {
        return put(DEFAULT_TOPIC, key, value);
    }

    public final synchronized <V> V get(int topic, String key) {
        LruCache<V> lruCache = topics.get(topic);
        if (lruCache == null) {
            return null;
        }
        V value = lruCache.get(key);
        return value;
    }

    public final <V> V get(String key) {
        return get(DEFAULT_TOPIC, key);
    }

    public SparseArrayCompat<LruCache> getTopics() {
        return topics;
    }

    public void buildTopicCache(int topic, int maxSize,  long expiredTime) {
        LruCache lruCache = topics.get(topic);
        if (lruCache == null) {
            lruCache = new LruCache(new Topic(topic,maxSize, expiredTime), cacheDelegate);
            topics.put(topic, lruCache);
            notifyCacheChanged(topic, maxSize, expiredTime);
        } else {
            if(lruCache.getMaxSize() != maxSize || lruCache.getExpiredTime() != expiredTime){
                lruCache.resize(maxSize);
                lruCache.setExpiredTime(expiredTime);
                notifyCacheChanged(topic, maxSize, expiredTime);
            }
        }
    }

     void buildTopicCache(Topic topic){
        LruCache lruCache = topics.get(topic.getTopic());
        if(lruCache == null){
            lruCache = new LruCache(topic, cacheDelegate);
            topics.put(topic.getTopic(), lruCache);
        }
    }

    private void notifyCacheChanged(int topic, int maxSize, @Topic.EXPIRED long expiredTime) {
        if (cacheDelegate != null) {
            cacheDelegate.onTopicCacheBuild(topic, maxSize, expiredTime);
        }
    }

    public CacheDelegate getCacheDelegate() {
        return cacheDelegate;
    }

    public void delete(int topic) {
        topics.delete(topic);
        if (cacheDelegate != null) {
            cacheDelegate.onDeleteTopic(topic);
        }
    }

    public <V> Collection<V> getTopicValues(int topic) {
        LruCache lruCache = topics.get(topic);
        if(lruCache == null){
            return new ArrayList<>();
        }
        return lruCache.snapshot().values();
    }
    public <V> Collection<String> getTopicKeys(int topic) {
        LruCache<V> lruCache = topics.get(topic);
        if(lruCache == null){
            return new ArrayList<>();
        }
        return lruCache.snapshot().keySet();
    }
}
