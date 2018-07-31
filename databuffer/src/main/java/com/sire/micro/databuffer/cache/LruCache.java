package com.sire.micro.databuffer.cache;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static com.sire.micro.databuffer.cache.Topic.INFINITE;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/24
 * Author:wangkai
 * Description:内存大小以及时间两个维度对缓存进行实时调整
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class LruCache<V> {
    private final LinkedHashMap<String, ValueInfor<V>> map;
    private final Topic topic;
    private final CacheDelegate cacheDelegate;
    /**
     * Size of this cache in units. Not necessarily the number of elements.
     */
    private int size;
    private int maxSize;
    private int putCount;
    private int createCount;
    private int evictionCount;
    private int hitCount;
    private int missCount;

    public LruCache(Topic topic, CacheDelegate cacheDelegate) {
        this.maxSize = topic.getMaxSize();
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }
        this.topic = topic;
        this.cacheDelegate = cacheDelegate;
        this.map = new LinkedHashMap<>(0, 0.75f, true);
    }

    /**
     * Sets the size of the cache.
     *
     * @param maxSize The new maximum size.
     */
    public void resize(int maxSize) {
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize <= 0");
        }

        synchronized (this) {
            this.maxSize = maxSize;
        }
        trimToSize(maxSize);
    }

    /**
     * Returns the value for {@code key} if it exists in the cache or can be
     * created by {@code #create}. If a value was returned, it is moved to the
     * head of the queue. This returns null if a value is not cached and cannot
     * be created.
     */
    public final V get(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        ValueInfor<V> mapValue;
        synchronized (this) {
            mapValue = map.get(key);
            if (mapValue != null) {
                if (topic.getExpiredTime() > INFINITE) {
                    long cacheTime = mapValue.getCacheTime();
                    long passedTime = new Date().getTime() - cacheTime;
                    if (passedTime >= topic.getExpiredTime()) {
                        missCount++;
                        remove(key);
                        return null;
                    }
                }
                hitCount++;
                return mapValue.getValue();
            }
            missCount++;
        }

        /*
         * Attempt to create a value. This may take a long time, and the map
         * may be different when create() returns. If a conflicting value was
         * added to the map while create() was working, we leave that value in
         * the map and release the created value.
         */

        ValueInfor<V> createdValue = create(key);
        if (createdValue == null) {
            return null;
        }

        synchronized (this) {
            createCount++;
            mapValue = map.put(key, createdValue);

            if (mapValue != null) {
                // There was a conflict so undo that last put
                map.put(key, mapValue);
            } else {
                size += safeSizeOf(key, createdValue);
            }
        }

        if (mapValue != null) {
            entryRemoved(false, key, createdValue, mapValue);
            return mapValue.getValue();
        } else {
            trimToSize(maxSize);
            return createdValue.getValue();
        }
    }

    @NonNull
    private ValueInfor<V> getValueInfor(V value) {
        ValueInfor<V> valueInfor = new ValueInfor<>();
        valueInfor.setValue(value);
        valueInfor.setCacheTime(new Date().getTime());
        return valueInfor;
    }

    public final V put(String key, V value) {
        return put(key, value, true);
    }

    /**
     * Caches {@code value} for {@code key}. The value is moved to the head of
     * the queue.
     *
     * @return the previous value mapped by {@code key}.
     */
    final V put(String key, V value, boolean notify) {
        if (key == null || value == null) {
            throw new NullPointerException("key == null || value == null");
        }

        ValueInfor<V> previous;
        ValueInfor<V> valueInfor = getValueInfor(value);
        synchronized (this) {
            putCount++;
            size += safeSizeOf(key, valueInfor);
            if (cacheDelegate != null && notify) {
                cacheDelegate.onCache(topic.getTopic(), key, valueInfor);
            }
            previous = map.put(key, valueInfor);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, valueInfor);
        }

        trimToSize(maxSize);
        return previous == null ? null : previous.getValue();
    }

    /**
     * Remove the eldest entries until the total of remaining entries is at or
     * below the requested size.
     *
     * @param maxSize the maximum size of the cache before returning. May be -1
     *                to evict even 0-sized elements.
     */
    public void trimToSize(int maxSize) {
        while (true) {
            String key;
            ValueInfor<V> value;
            synchronized (this) {
                if (size < 0 || (map.isEmpty() && size != 0)) {
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");
                }
                Iterator<Map.Entry<String, ValueInfor<V>>> iterator = map.entrySet().iterator();
                if (!iterator.hasNext()) {
                    break;
                }
                if (size <= maxSize || map.isEmpty()) {
                    if (topic.getExpiredTime() > INFINITE) {
                        Map.Entry<String, ValueInfor<V>> toEvict = iterator.next();
                        key = toEvict.getKey();
                        value = toEvict.getValue();
                        long cacheTime = value.getCacheTime();
                        long passedTime = new Date().getTime() - cacheTime;
                        if (passedTime >= topic.getExpiredTime()) {
                            System.out.println("trimToSize======cachetime:" + cacheTime);
                            evict(key, value);
                        } else {
                            //ordered by time
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    Map.Entry<String, ValueInfor<V>> toEvict = iterator.next();
                    key = toEvict.getKey();
                    value = toEvict.getValue();
                    evict(key, value);
                }
            }

            entryRemoved(true, key, value, null);
        }
    }

    private void evict(String key, ValueInfor<V> value) {
        map.remove(key);
        size -= safeSizeOf(key, value);
        evictionCount++;
    }

    /**
     * Removes the entry for {@code key} if it exists.
     *
     * @return the previous value mapped by {@code key}.
     */
    public final V remove(String key) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }

        ValueInfor<V> previous;
        synchronized (this) {
            previous = map.remove(key);
            if (previous != null) {
                size -= safeSizeOf(key, previous);
            }
        }

        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous == null ? null : previous.getValue();
    }


    protected void entryRemoved(boolean evicted, String key, ValueInfor<V> oldValue, ValueInfor<V> newValue) {
        if (cacheDelegate == null) {
            return;
        }
        if (evicted) {
            //超过限制，删除操作
            cacheDelegate.onTrimSize(topic.getTopic(), key, oldValue);
        } else {
            if (newValue != null) {
                //替换值操作
            } else {
                //主动删除操作
                cacheDelegate.onManualDelete(topic.getTopic(), key, oldValue);
            }
        }
    }

    /**
     * Called after a cache miss to compute a value for the corresponding key.
     * Returns the computed value or null if no value can be computed. The
     * default implementation returns null.
     * <p>
     * <p>The method is called without synchronization: other threads may
     * access the cache while this method is executing.
     * <p>
     * <p>If a value for {@code key} exists in the cache when this method
     * returns, the created value will be released with {@link #entryRemoved}
     * and discarded. This can occur when multiple threads request the same key
     * at the same time (causing multiple values to be created), or when one
     * thread calls {@link #put} while another is creating a value for the same
     * key.
     */
    protected ValueInfor<V> create(String key) {
        return null;
    }

    private int safeSizeOf(String key, ValueInfor<V> value) {
        int result = sizeOf(key, value);
        if (result < 0) {
            throw new IllegalStateException("Negative size: " + key + "=" + value);
        }
        return result;
    }

    /**
     * Returns the size of the entry for {@code key} and {@code value} in
     * user-defined units.  The default implementation returns 1 so that size
     * is the number of entries and max size is the maximum number of entries.
     * <p>
     * <p>An entry's size must not change while it is in the cache.
     */
    protected int sizeOf(String key, ValueInfor<V> value) {
        return 1;
    }

    /**
     * Clear the cache, calling {@link #entryRemoved} on each removed entry.
     */
    public final void evictAll() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the number
     * of entries in the cache. For all other caches, this returns the sum of
     * the sizes of the entries in this cache.
     */
    public synchronized final int size() {
        return size;
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns the maximum
     * number of entries in the cache. For all other caches, this returns the
     * maximum sum of the sizes of the entries in this cache.
     */
    public synchronized final int maxSize() {
        return maxSize;
    }

    /**
     * Returns the number of times {@link #get} returned a value that was
     * already present in the cache.
     */
    public synchronized final int hitCount() {
        return hitCount;
    }

    /**
     * Returns the number of times {@link #get} returned null or required a new
     * value to be created.
     */
    public synchronized final int missCount() {
        return missCount;
    }


    public synchronized final int createCount() {
        return createCount;
    }

    /**
     * Returns the number of times {@link #put} was called.
     */
    public synchronized final int putCount() {
        return putCount;
    }

    /**
     * Returns the number of values that have been evicted.
     */
    public synchronized final int evictionCount() {
        return evictionCount;
    }

    /**
     * Returns a copy of the current contents of the cache, ordered from least
     * recently accessed to most recently accessed.
     */
    public synchronized final Map<String, ValueInfor<V>> snapshot() {
        return new LinkedHashMap<>(map);
    }

    public long getExpiredTime() {
        return topic.getExpiredTime();
    }

    public void setExpiredTime(long expiredTime) {
        this.topic.setExpiredTime(expiredTime);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public Topic getTopic() {
        return topic;
    }

    @Override
    public synchronized final String toString() {
        int accesses = hitCount + missCount;
        int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
        return String.format(Locale.US, "LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]",
                maxSize, hitCount, missCount, hitPercent);
    }
}
