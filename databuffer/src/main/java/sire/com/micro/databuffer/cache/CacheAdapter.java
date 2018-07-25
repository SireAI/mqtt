package sire.com.micro.databuffer;

import android.support.v4.util.LruCache;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

import java.util.List;

import sire.com.micro.databuffer.cache.DiskCache;
import sire.com.micro.databuffer.cache.MemoryCache;
import sire.com.micro.databuffer.cache.Utils;

public class CacheAdapter {
    private static final String TAG = "CacheAdapter";
    private final MemoryCache memoryCache;

    public CacheAdapter(MemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    public void onLoadSuccess(List<CacheEntry> cacheEntries) {
        if (cacheEntries != null && cacheEntries.size() > 0) {
            for (int i = 0; i < cacheEntries.size(); i++) {
                CacheEntry cacheEntry = cacheEntries.get(i);
                Object value = recoverValue(cacheEntry.getValueType(), cacheEntry.getValue());
                memoryCache.put(cacheEntry.getTopic(), cacheEntry.getKey(), value);
            }
        }
    }

    private Object recoverValue(String valueType, byte[] value) {
        Object objectValue = null;
        if (valueType.equals(String.class.getName())) {
            return new String(value);
        } else {
            objectValue = Utils.bytes2Object(value);
        }
        return objectValue;
    }

    public void synchronizeDisk(SparseArrayCompat<LruCache> topics, DiskCache diskCache) {
        if (diskCache.isLoaded()) {
            diskCache.ergodic(topics);
        }else {
            Log.w(TAG,"disk is not in LOADED state , synchronize operation failed");
        }
    }
}
