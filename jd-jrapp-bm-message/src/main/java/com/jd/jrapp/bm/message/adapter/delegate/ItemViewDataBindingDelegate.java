package com.jd.jrapp.bm.message.adapter.delegate;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/13
 * Author:wangkai
 * Description:
 * =====================================================
 */
public abstract class ItemViewDataBindingDelegate<T> implements ItemViewDelegate<T> {
    @Override
    public void convert(ViewHolder holder, T item, int position) {
        holder.getDataBinding().setVariable(getItemBRName(),item);
        holder.getDataBinding().executePendingBindings();
    }
    protected abstract int getItemBRName();
}
