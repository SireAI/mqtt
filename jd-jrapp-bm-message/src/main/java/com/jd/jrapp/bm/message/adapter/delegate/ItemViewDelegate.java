package com.jd.jrapp.bm.message.adapter;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/13
 * Author:wangkai
 * Description:
 * =====================================================
 */
public interface ItemViewDelegate<T> {
    //类型
    int getType();
    //由数据和位置共同确定是否是所需的类型
    boolean isForViewType(T item, int position);
    //进行viewholder的数据与视图的设置
    void convert(ViewHolder holder, T t, int position);
}
