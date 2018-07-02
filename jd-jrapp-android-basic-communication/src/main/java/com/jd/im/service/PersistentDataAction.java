package com.jd.im.service;

import com.jd.im.utils.Log;

import com.jd.im.IMQTTMessage;
import com.jd.im.socket.Event;
import com.jd.im.socket.TimingWheel;
import com.jd.im.storage.MessageStore;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/6
 * Author:wangkai
 * Description:存储数据
 * =====================================================
 */
public class PersistentDataAction implements Runnable {
    private static final String TAG = "PersistentDataAction";
    private  IMQTTMessage message;
    private MqttService mqttService;

    public PersistentDataAction(MqttService mqttService, IMQTTMessage message) {
        this.mqttService = mqttService;
        this.message = message;
    }

    @Override
    public void run() {
        final MessageStore databaseMessageStore = mqttService.getMessageStore();
        TimingWheel<Event<MqttSenderAction>> eventTimingWheel = mqttService.getEventTimingWheel();
        if (databaseMessageStore == null || eventTimingWheel == null || message == null) return;
        String id = databaseMessageStore.storeArrived(mqttService.getClientHandle(), message);
        Log.d(TAG,"存储id为："+id);
    }
}
