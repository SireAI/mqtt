package com.sire.micro.databuffer;

import android.content.Context;
import android.support.annotation.IntDef;

import com.sire.micro.databuffer.cache.CacheAdapter;
import com.sire.micro.databuffer.cache.CacheDelegate;
import com.sire.micro.databuffer.cache.DiskCache;
import com.sire.micro.databuffer.cache.MemoryCache;
import com.sire.micro.databuffer.cache.ValueInfor;
import com.sire.micro.databuffer.command.BatchInsertCommand;
import com.sire.micro.databuffer.command.DataDeleteCommand;
import com.sire.micro.databuffer.command.DataInsertCommand;
import com.sire.micro.databuffer.command.Operation;
import com.sire.micro.databuffer.command.Result;
import com.sire.micro.databuffer.command.TopicDeleteCommand;
import com.sire.micro.databuffer.command.TopicInsertCommand;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;

import static com.sire.micro.databuffer.cache.MemoryCache.DEFAULT_TOPIC;


public class DataBuffer implements CacheDelegate {
    public static final int MANUAL = 0;
    public static final int AUTOMATIC = 1;
    private static final String TAG = "DataBuffer";
    private static DataBuffer instance;
    private final MemoryCache memoryCache;
    private final CacheAdapter cacheAdapter;
    private Operation operation;
    private DiskCache diskCache;
    private @Strategy
    int strategy = AUTOMATIC;
    private Result preloadResult;


    private DataBuffer() {
        memoryCache = new MemoryCache(this);
        cacheAdapter = new CacheAdapter(memoryCache);
    }

    public static DataBuffer get() {
        if (instance == null) {
            instance = new DataBuffer();
        }
        return instance;
    }

    /**
     * 初始化
     *
     * @param context  上下文
     * @param strategy 同步策略
     */
    public static void prepare(Context context, @Strategy int strategy) {
        DataBuffer.get().init(context, strategy, null);
    }

    /**
     * 初始化
     *
     * @param context       上下文
     * @param strategy      同步策略
     * @param preloadResult 初始化成功回调
     */
    public static void prepare(Context context, @Strategy int strategy, Result preloadResult) {
        DataBuffer.get().init(context, strategy, preloadResult);
    }

    private void init(Context context, @Strategy int strategy, Result preloadResult) {
        if (diskCache == null) {
            this.strategy = strategy;
            diskCache = new DiskCache(context.getApplicationContext(), cacheAdapter);
            cacheAdapter.setResult(preloadResult);
            operation = new Operation(strategy);
            diskCache.preLoad();
        }
    }


    /**
     * 同步数据到disk
     */
    public void synchronizeDisk(Result result) {
        if (diskCache.isLoaded()) {
            operation.runCommand(new BatchInsertCommand(diskCache, memoryCache.getTopics(), result));
        } else {
            Log.w(TAG, "disk is not in LOADED state , synchronize operation failed");
        }
    }

    /**
     * 删除操作
     *
     * @param topic
     * @param key
     * @param <V>
     * @return
     */
    public <V> V delete(int topic, String key) {
        if (!diskCache.isPrepared()) {
            Log.w(TAG, "data from store is not prepared");
            return null;
        }
        return memoryCache.delete(topic, key);
    }

    private void operateDelete(int topic, String key) {
        operation.runCommand(new DataDeleteCommand(diskCache, topic, key));
    }

    /**
     * 从默认主题中删除
     *
     * @param key
     * @param <V>
     * @return
     */
    public <V> V delete(String key) {
        return delete(DEFAULT_TOPIC, key);
    }

    public void deleteTopic(int topic) {
        memoryCache.delete(topic);
    }

    /**
     * @param topic 存储主题维度
     * @param key   主题维度下的索引
     * @param value 存储值
     * @param <V>   若是覆盖存储，会返回之前的值
     * @return
     */
    public <V> V put(int topic, String key, V value) {
        return memoryCache.put(topic, key, value);
    }


    /**
     * 存储在默认主题中
     *
     * @param key
     * @param value
     * @param <V>
     * @return
     */
    public <V> V put(String key, V value) {
        return put(DEFAULT_TOPIC, key, value);
    }

    /**
     * 以主题和关键索引获取值
     *
     * @param topic
     * @param key
     * @param <V>
     * @return
     */
    public <V> V get(int topic, String key) {
        return memoryCache.get(topic, key);
    }

    public <V> Collection<V> getTopicValues(int topic) {
        return memoryCache.getTopicValues(topic);
    }

    public  Collection<String> getTopicKeys(int topic) {
        return memoryCache.getTopicKeys(topic);
    }

    /**
     * 从默认主题中获取
     *
     * @param key
     * @param <V>
     * @return
     */
    public <V> V get(String key) {
        return get(DEFAULT_TOPIC, key);
    }

    public void onDestroy() {
        if (diskCache != null) {
            diskCache.onDestroy();
        }
    }

    /**
     * {@hide}
     */
    @Override
    public void onTrimSize(int topic, String key, ValueInfor oldValue) {
        automaticDelete(topic, key);
    }

    private synchronized void automaticDelete(int topic, String key) {
        operateDelete(topic, key);
    }

    /**
     * {@hide}
     */
    @Override
    public void onManualDelete(int topic, String key, ValueInfor oldValue) {
        automaticDelete(topic, key);
    }

    /**
     * {@hide}
     */
    @Override
    public void onCache(int topic, String key, ValueInfor valueInfor) {
        operation.runCommand(new DataInsertCommand(diskCache, topic, key, valueInfor.getValue(), valueInfor.getCacheTime()));
    }

    /**
     * {@hide}
     */
    @Override
    public void onTopicCacheBuild(int topic, int maxSize, long expiredTime) {
        operation.runCommand(new TopicInsertCommand(diskCache, topic, maxSize, expiredTime));
    }

    /**
     * {@hide}
     */
    @Override
    public void onDeleteTopic(int topic) {
        operation.runCommand(new TopicDeleteCommand(diskCache, topic));
    }

    public void buildTopicCache(int topic, int maxSize, long expiredTime) {
        memoryCache.buildTopicCache(topic, maxSize, expiredTime);
    }

    public void openLog() {
        Log.openLog();
    }

    @IntDef({MANUAL, AUTOMATIC})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Strategy {
    }
}
