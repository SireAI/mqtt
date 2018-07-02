package com.jd.im.client;

import com.jd.im.mqtt.MQTTException;

public abstract class MessageResult implements MessageCallBack{
    @Override
    public void onSuccess(Object extraInfor) {
        onSuccess();
    }
    public abstract void onSuccess();
}
