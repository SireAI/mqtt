package com.sire.micro.databuffer.cache;

import android.content.Context;
import android.os.Process;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.util.SparseArrayCompat;

import com.sire.micro.databuffer.CacheEntry;
import com.sire.micro.databuffer.command.core.IReceiver;
import com.sire.micro.databuffer.command.Result;
import com.sire.micro.databuffer.storage.DBStore;
import com.sire.micro.databuffer.storage.IStore;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;




/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/23
 * Author:wangkai
 * Description:硬存管理
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class DiskCache implements IReceiver {

    public static final String SERIALIZABLE = "Serializable";
    public static final String COLLECTION = "Collection:";
    private static final int UNPREPARE = 0;
    private static final int LOADING = 1;
    private static final int LOADED = 2;
    private static final int EXCUTING = 3;
    final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            0,
            1,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new DiskLruCacheThreadFactory());
    private final IStore store;
    private final CacheAdapter cacheAdapter;
    private @State
    int state = UNPREPARE;


    public DiskCache(final Context context, CacheAdapter cacheAdapter) {
        Factory factory = new Factory() {
            @Override
            public IStore create() {
                return new DBStore(context);
            }
        };
        this.cacheAdapter = cacheAdapter;
        this.store = factory.create();
    }

    public void preLoad() {
        //异步加载
        this.state = LOADING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                List[] datas = store.queryAll();
                if (cacheAdapter != null) {
                    cacheAdapter.onLoadSuccess(datas[0],datas[1]);
                    checkWeatherIdelState();
                }
            }
        });
    }

    private void insertEntry(CacheEntry cacheEntry) {
        if (cacheEntry == null) {
            return;
        }
        this.store.insert(cacheEntry);
    }

    private void deleteEntry(int topic, String key) {
        this.store.delete(topic, key);
    }


    public boolean isLoaded() {
        return state == LOADED;
    }

    public boolean isPrepared() {
        return state >= LOADED;
    }



    private void checkWeatherIdelState() {
        if (executorService != null) {
            if(executorService.getTaskCount() <= 0){
                DiskCache.this.state = LOADED;
            }else {
                DiskCache.this.state = EXCUTING;
            }
        }
    }

    @Override
    public void insertData(final int topic, final String key, final Object value, final long cacheTime) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                CacheEntry cacheEntry = generateCacheEntry(topic, key, value,cacheTime);
                DiskCache.this.insertEntry(cacheEntry);
                checkWeatherIdelState();
            }
        });
    }

    @Override
    public void deleteData(final int topic, final String key) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DiskCache.this.deleteEntry(topic, key);
                checkWeatherIdelState();
            }
        });
    }

    @Override
    public void batchInsertData(final SparseArrayCompat<LruCache> topics, final Result result) {
        if (topics == null || topics.size() == 0) {
            return;
        }
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < topics.size(); i++) {
                        int topic = topics.keyAt(i);
                        LruCache lruCache = topics.get(topic);
                        if (lruCache == null) {
                            continue;
                        }
                        Map<String, ValueInfor> snapshot = lruCache.snapshot();
                        for (String key : snapshot.keySet()) {
                            ValueInfor valueInfor = snapshot.get(key);
                            CacheEntry cacheEntry = generateCacheEntry(topic, key,valueInfor.getValue(),valueInfor.getCacheTime());
                            DiskCache.this.insertEntry(cacheEntry);
                        }
                    }
                    checkWeatherIdelState();
                    DiskCache.this.notify(result, true);
                } catch (Exception e) {
                    e.printStackTrace();
                    DiskCache.this.notify(result, false);
                }
            }
        });
    }

    @Override
    public void insertTopicCache(final int topic, final int maxSize, final long expiredTime) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DiskCache.this.insertTopic(topic,maxSize,expiredTime);
                checkWeatherIdelState();
            }
        });
    }

    @Override
    public void deleteTopic(final int topic) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                store.delete(topic);
                checkWeatherIdelState();
            }
        });
    }

    private void insertTopic(int topic, int maxSize, long expiredTime) {
        this.store.insert(topic,maxSize,expiredTime);
    }

    private void notify(Result result, boolean success) {
        if (result != null) {
            result.onResult(success);
        }
    }

    @Nullable
    private CacheEntry generateCacheEntry(int topic, String key, Object value, long cacheTime) {
        CacheEntry cacheEntry = null;
        if (value != null) {
            if (value instanceof Serializable) {
                byte[] bytes = Utils.Object2Bytes((Serializable) value);
                if (bytes != null) {
                    cacheEntry = new CacheEntry(topic, key, SERIALIZABLE, bytes, cacheTime);
                }
            } else if (value instanceof Collection) {
                value = ((Collection) value).toArray();
                byte[] bytes = Utils.Object2Bytes((Serializable) value);
                if (bytes != null) {
                    cacheEntry = new CacheEntry(topic,  key, COLLECTION, bytes, cacheTime);
                }
            } else {
                throw new RuntimeException(value.getClass().getName() + " is not support!");
            }
        }
        return cacheEntry;
    }

    public void onDestroy() {
        if (store != null) {
            store.close();
        }
    }


    @IntDef({UNPREPARE, LOADING, LOADED, EXCUTING})
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {
    }

    public interface Factory {
        IStore create();
    }

    private static final class DiskLruCacheThreadFactory implements ThreadFactory {
        @Override
        public synchronized Thread newThread(Runnable runnable) {
            Thread result = new Thread(runnable, "data-buffer-cache-thread");
            result.setPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if(result.isDaemon()){
                result.setDaemon(false);
            }
            return result;
        }
    }
}
