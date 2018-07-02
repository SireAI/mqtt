package com.jd.im.storage;


import android.support.annotation.RestrictTo;

import com.jd.im.IMQTTMessage;

import java.util.Iterator;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/10
 * Author:wangkai
 * Description:  存储接口
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface MessageStore {

    /**
     * 存储
     *
     * @param clientHandle clientId+serverUrl
     * @param message
     * @return
     */
    String storeArrived(String clientHandle, IMQTTMessage message);


    /**
     * 删除
     *
     * @param clientHandle clientId+serverUrl
     * @param id
     * @return
     */
    boolean discardArrived(String clientHandle, String id);

    /**
     * 获取所有消息
     *
     * @param clientHandle clientId+serverUrl
     * @return
     */
    Iterator<IMQTTMessage> getAllArrivedMessages(String clientHandle);

    /**
     * 清楚所有消息
     *
     * @param clientHandle
     */
    void clearArrivedMessages(String clientHandle);

    /**
     * 关闭存储
     */
    void close();


}
