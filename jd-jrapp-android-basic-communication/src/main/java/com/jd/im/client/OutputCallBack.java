package com.jd.im.client;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/29
 * Author:wangkai
 * Description:处理丢失或者没有操作回调的消息
 * =====================================================
 */
public interface OutputCallBack<T> {
    void onSuccess(T messageInfor);
}
