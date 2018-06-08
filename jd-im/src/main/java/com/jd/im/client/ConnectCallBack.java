package com.jd.im.client;

import com.jd.im.mqtt.MQTTException;

public interface ConnectCallBack<T> {
    void onKickOff(T kickInfor);
    void onConnectSuccess();
    void onConnectLoss(MQTTException excetion);
}
