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
public interface MessageCallBack {
    void onSuccess();
    void onFailed(MQTTException exception);
}
