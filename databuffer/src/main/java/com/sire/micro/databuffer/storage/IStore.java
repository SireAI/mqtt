package com.sire.micro.databuffer.storage;

import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.CacheEntry;
import com.sire.micro.databuffer.cache.Topic;

import java.util.List;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface IStore {

    void insert(CacheEntry cacheEntry);

    void delete(int topic,String key);

    void delete(int topic);

    void update(CacheEntry cacheEntry);

    List<CacheEntry> query(int topic);

    List<CacheEntry> queryAllEnties();

    List<Topic> queryAllTopics();

    List[] queryAll();

    void close();

    void insert(int topic, int maxSize, long expiredTime);

}
