package sire.com.micro.databuffer;

import android.support.v4.util.LruCache;
import android.support.v4.util.SparseArrayCompat;
import android.text.TextUtils;

import sire.com.micro.databuffer.command.InsertCommand;
import sire.com.micro.databuffer.command.Operation;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/23
 * Author:wangkai
 * Description:内存管理
 * =====================================================
 */
public class MemoryCache {

    public static final int DEFAULT_TOPICK = -1;
    public static final int MAX_SIZE = 100;
    private final SparseArrayCompat<LruCache> topics;

    public MemoryCache() {
        topics = new SparseArrayCompat<>();
        topics.put(DEFAULT_TOPICK, new LruCache<>(MAX_SIZE));
    }

    public final synchronized <V> V put(int topic, String key, V value) {
        if (value == null) {
            return null;
        }
        LruCache<String, V> lruCache = topics.get(topic);
        if (lruCache == null) {
            lruCache = new LruCache(MAX_SIZE);
            topics.put(topic, lruCache);
        }
        return lruCache.put(key, value);
    }

    public final synchronized <V> V  delete(int topic, String key){
        if(key == null || key.equals("")){
            return null;
        }
        LruCache<String, V> lruCache = topics.get(topic);
        if(lruCache == null){
            return null;
        }
       return lruCache.remove(key);
    }

    public final synchronized <V> V  delete(String key){
        return delete(key);
    }

    public final <V> V put(String key, V value) {
        return put(DEFAULT_TOPICK, key, value);
    }

    public final synchronized <V> V get(int topic, String key) {
        LruCache<String, V> lruCache = topics.get(topic);
        if (lruCache == null) {
            return null;
        }
        V value = lruCache.get(key);
        return value;
    }

    public final <V> V get(String key) {
        return get(DEFAULT_TOPICK, key);
    }

    public void synchronize(CacheAdapter cacheAdapter, DiskCache diskCache) {
        cacheAdapter.synchronizeDisk(topics, diskCache);
    }
}
