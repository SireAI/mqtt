package com.jd.jrapp.bm.message.adapter;

import android.arch.core.executor.ArchTaskExecutor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.recyclerview.extensions.AsyncDifferConfig;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.ListUpdateCallback;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/19
 * Author:wangkai
 * Description:
 * =====================================================
 */
public class AsyncPagedListDiffer<T> {
    private final ListUpdateCallback mUpdateCallback;
    private final AsyncDifferConfig<T> mConfig;
    private final RecyclerView.Adapter adapter;


    @SuppressWarnings("RestrictedApi")
    Executor mMainThreadExecutor = ArchTaskExecutor.getMainThreadExecutor();


    private List<T> mPagedList;


    public AsyncPagedListDiffer(@NonNull RecyclerView.Adapter adapter,
                                @NonNull DiffUtil.ItemCallback<T> diffCallback) {
        mUpdateCallback = new AdapterListUpdateCallback(adapter);
        mConfig = new AsyncDifferConfig.Builder<T>(diffCallback).build();
        this.adapter = adapter;
    }



    @Nullable
    public T getItem(int index) {
        if (mPagedList == null) {
            throw new IndexOutOfBoundsException(
                    "Item count is zero, getItem() call is invalid");
        }
        return mPagedList.get(index);
    }


    public int getItemCount() {
        if (mPagedList != null) {
            return mPagedList.size();
        }
        return 0;
    }


    public void submitList(final List<T> pagedList) {
        if (pagedList == null || pagedList == mPagedList) {
            return;
        }

        if (mPagedList == null) {
            mPagedList = pagedList;
            return;
        }


        mConfig.getBackgroundThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final DiffUtil.DiffResult result;
                result = PagedStorageDiffHelper.computeDiff(
                        mPagedList,
                        pagedList,
                        mConfig.getDiffCallback());
                mMainThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        //更换数据源后没有紧跟刷新可能会出现底层异常，需要捕获
                        mPagedList = pagedList;
                        result.dispatchUpdatesTo(mUpdateCallback);
                    }
                });
            }
        });
    }


    @Nullable
    public List<T> getCurrentList() {
        return mPagedList;
    }
}
