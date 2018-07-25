package sire.com.micro.databuffer;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v4.util.SparseArrayCompat;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import sire.com.micro.databuffer.command.IReceiver;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/7/23
 * Author:wangkai
 * Description:硬存管理
 * =====================================================
 */
public class DiskCache implements IReceiver{

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
                List<CacheEntry> cacheEntries = store.queryAll();
                if (cacheAdapter != null) {
                    cacheAdapter.onLoadSuccess(cacheEntries);
                    DiskCache.this.state = LOADED;
                }
            }
        });
    }

    private void insertEntry(CacheEntry cacheEntry){
        if(cacheEntry == null){
            return;
        }
        this.store.insert(cacheEntry);
    }

    private void deleteEntry(int topic,String key){
        this.store.delete(topic,key);
    }



    public boolean isLoaded(){
        return state==LOADED;
    }

    public void ergodic(final SparseArrayCompat<LruCache> topics) {
        if(topics == null || topics.size() == 0){
            return;
        }
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < topics.size(); i++) {
                    int topic = topics.keyAt(i);
                    LruCache lruCache = topics.get(topic);
                    if(lruCache == null){
                        continue;
                    }
                    Map<String,?> snapshot = lruCache.snapshot();
                    for (String key:snapshot.keySet()){
                        Object value = snapshot.get(key);
                        CacheEntry cacheEntry = generateCacheEntry(topic, key,value);
                        DiskCache.this.insertEntry(cacheEntry);
                    }
                }
                checkWeatherIdelState();
            }
        });
    }


    @Override
    public void insertData(final int topic, final String key, final Object value) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                CacheEntry cacheEntry = generateCacheEntry(topic, key,value);
                DiskCache.this.insertEntry(cacheEntry);
                checkWeatherIdelState();
            }
        });
    }

    private void checkWeatherIdelState() {
        if(executorService!=null && executorService.getTaskCount()<=0){
            DiskCache.this.state = LOADED;
        }
    }

    @Override
    public void deleteData(final int topic, final String key) {
        this.state = EXCUTING;
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                DiskCache.this.deleteEntry(topic,key);
                checkWeatherIdelState();
            }
        });
    }

    @Nullable
    private CacheEntry generateCacheEntry(int topic, String key,Object value) {
        System.out.println("========"+Thread.currentThread().getId());
        CacheEntry cacheEntry=null;
        if(value instanceof String){
            cacheEntry = new CacheEntry(topic, key, String.class.getName(), ((String) value).getBytes(), new Date().getTime());
        }else if(cacheEntry instanceof Serializable){
            byte[] bytes = Utils.Object2Bytes((Serializable) value);
            if(bytes!=null){
                cacheEntry = new CacheEntry(topic, key, value.getClass().getName(), bytes, new Date().getTime());
            }
        }
        return cacheEntry;
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
            result.setPriority(Thread.MIN_PRIORITY);
            return result;
        }
    }
}
