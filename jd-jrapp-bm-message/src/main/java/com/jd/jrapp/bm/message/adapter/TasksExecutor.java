package com.jd.jrapp.bm.message.adapter;

import android.arch.core.executor.TaskExecutor;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutor {
    private final Object mLock = new Object();
    private ExecutorService mDiskIO = Executors.newFixedThreadPool(2);

    private volatile Handler mMainHandler;


    public void executeOnDiskIO(Runnable runnable) {
        mDiskIO.execute(runnable);
    }

    public void postToMainThread(Runnable runnable) {
        if (mMainHandler == null) {
            synchronized (mLock) {
                if (mMainHandler == null) {
                    mMainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        //noinspection ConstantConditions
        mMainHandler.post(runnable);
    }

    public boolean isMainThread() {
        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }
}