package com.jd.jrapp.bm.message.adapter.delegate;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewHolder extends RecyclerView.ViewHolder {
    private int type;
    private ViewDataBinding dataBinding;

    public ViewHolder(View itemView, int type) {
        super(itemView);
        this.type = type;
    }

    public ViewHolder(ViewDataBinding dataBinding, int type) {
        this(dataBinding.getRoot(), type);
        this.dataBinding = dataBinding;
    }

    /**
     * 创建viewHolder的一种方式
     *
     * @param parent
     * @param layoutId
     * @return
     */
    public static ViewHolder createDataBindingViewHolder(
            ViewGroup parent, int layoutId) {
        ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false);
        ViewHolder holder = new ViewHolder(dataBinding, layoutId);
        return holder;
    }

    public ViewDataBinding getDataBinding() {
        return dataBinding;
    }

    public int getType() {
        return type;
    }

}
