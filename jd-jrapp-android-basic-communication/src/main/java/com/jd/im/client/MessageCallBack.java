package com.jd.im.client;

import com.jd.im.mqtt.MQTTException;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/24
 * Author:wangkai
 * Description:客户端消息发送成功失败回调
 * =====================================================
 */
public interface MessageCallBack<T> {
    /**
     *
     * @param extraInfor 额外确认信息，服务端与客户端约定
     */
    void onSuccess(T extraInfor);
    void onFailed(MQTTException exception);
}
