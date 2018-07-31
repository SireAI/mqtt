package com.sire.micro.databuffer.cache;

import android.support.annotation.RestrictTo;

import com.sire.micro.databuffer.CacheEntry;
import com.sire.micro.databuffer.Log;
import com.sire.micro.databuffer.command.Result;

import java.util.Arrays;
import java.util.Date;
import java.util.List;


import static com.sire.micro.databuffer.cache.DiskCache.COLLECTION;
import static com.sire.micro.databuffer.cache.DiskCache.SERIALIZABLE;
import static com.sire.micro.databuffer.cache.Topic.INFINITE;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class CacheAdapter {
    private static final String TAG = "CacheAdapter";
    private final MemoryCache memoryCache;
    private Result result;

    public CacheAdapter(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public void onLoadSuccess(List<Topic> topics, List<CacheEntry> cacheEntries) {
        if (topics != null && topics.size() > 0) {
            for (int i = 0; i < topics.size(); i++) {
                memoryCache.buildTopicCache(topics.get(i));
            }
        }
        if (cacheEntries != null && cacheEntries.size() > 0) {
            for (int i = 0; i < cacheEntries.size(); i++) {
                CacheEntry cacheEntry = cacheEntries.get(i);
                LruCache lruCache = memoryCache.getTopics().get(cacheEntry.getTopic());
                if(lruCache==null ){
                    Log.w(TAG,"some error happend,topic cache lost");
                    continue;
                }
                CacheDelegate cacheDelegate = memoryCache.getCacheDelegate();
                if (lruCache.getExpiredTime() > INFINITE) {
                    long cacheTime = cacheEntry.getCacheTime();
                    long passedTime = new Date().getTime() - cacheTime;
                    if (passedTime >= lruCache.getExpiredTime()) {
                        if (cacheDelegate != null) {
                            Log.d(TAG,"data "+cacheEntry.toString()+" expired");
                            cacheDelegate.onManualDelete(cacheEntry.getTopic(), cacheEntry.getKey(), null);
                        }
                    } else {
                        cacheData(cacheEntry,cacheDelegate);
                    }
                } else {
                    cacheData(cacheEntry,cacheDelegate);
                }
            }
        }
        if (result != null) {
            result.onResult(true);
        }
        Log.d(TAG, "data loading finshed");
    }

    private void cacheData(CacheEntry cacheEntry, CacheDelegate cacheDelegate) {
        Object value = recoverValue(cacheEntry.getValueType(), cacheEntry.getValue());
        LruCache lruCache = memoryCache.getTopics().get(cacheEntry.getTopic());
        if(value !=null){
            lruCache.put(cacheEntry.getKey(), value,false);
        }else {
            if(cacheDelegate!=null){
                cacheDelegate.onManualDelete(cacheEntry.getTopic(),cacheEntry.getKey(),null);
            }
        }
    }

    private Object recoverValue(String valueType, byte[] value) {
        Object objectValue = null;
        if (valueType.equals(SERIALIZABLE)) {
            objectValue = Utils.bytes2Object(value);
        } else if (valueType.equals(COLLECTION)) {
            objectValue = Arrays.asList(Utils.bytes2Object(value));
        }
        return objectValue;
    }

}
