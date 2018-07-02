package com.jd.jrapp.bm.message.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.jd.jrapp.bm.message.adapter.delegate.ItemViewDelegate;
import com.jd.jrapp.bm.message.adapter.delegate.ItemViewDelegateManager;
import com.jd.jrapp.bm.message.adapter.delegate.ViewHolder;


import java.util.List;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/13
 * Author:wangkai
 * Description: 聊天消息多布局项扩展
 * =====================================================
 */
public class IMMessageAdapter<T extends Element> extends RecyclerView.Adapter<ViewHolder> {

    private final ItemViewDelegateManager<T> mItemViewDelegateManager;
    private final AsyncPagedListDiffer<T> mDiffer;

    protected IMMessageAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        mDiffer = new AsyncPagedListDiffer<>(this, diffCallback);
        mItemViewDelegateManager = new ItemViewDelegateManager();
    }

    public static IMMessageAdapter create() {
        return new IMMessageAdapter(new DiffCallBack());
    }



    private static final  class DiffCallBack extends DiffUtil.ItemCallback<Element> {
        @Override
        public boolean areItemsTheSame(Element oldItem, Element newItem) {
            return oldItem.diffId().equals(newItem.diffId());
        }

        @Override
        public boolean areContentsTheSame(Element oldItem, Element newItem) {
            return oldItem.diffContent().equals(newItem.diffContent());
        }


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder = ViewHolder.createDataBindingViewHolder(parent, viewType);
        return holder;
    }

    public void refreshData(List<T> pagedList) {
        mDiffer.submitList(pagedList);
    }

    @Nullable
    protected T getItem(int position) {
        return mDiffer.getItem(position);
    }

    @Override
    public int getItemCount() {
        return mDiffer.getItemCount();
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T item = getItem(position);
        if(item!=null){
            mItemViewDelegateManager.getItemViewDelegate(holder.getType()).convert(holder,item,position);
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (!useItemViewDelegateManager()) {
            throw new RuntimeException("there is no view delegate!");
        }
        int itemViewType = mItemViewDelegateManager.getItemViewType(getItem(position), position);
        return itemViewType;
    }



    protected boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    /**
     * 添加view代理
     *
     * @param itemViewDelegate
     * @return
     */
    public IMMessageAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate) {
        mItemViewDelegateManager.addDelegate(itemViewDelegate.getType(),itemViewDelegate);
        return this;
    }

}