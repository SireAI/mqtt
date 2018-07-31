package com.sire.micro.databuffer.command.core;

import android.support.annotation.RestrictTo;
import android.support.v4.util.SparseArrayCompat;

import com.sire.micro.databuffer.cache.LruCache;
import com.sire.micro.databuffer.command.Result;


@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface IReceiver {
    void insertData(int topic, String key, Object value, long cacheTime);
    void deleteData(int topic, String key);
    void batchInsertData(SparseArrayCompat<LruCache> topics, Result result);
    void insertTopicCache(int topic, int maxSize, long expiredTime);

    void deleteTopic(int topic);
}
