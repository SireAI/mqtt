package com.jd.im.service;

import android.os.RemoteException;

import com.jd.im.socket.Event;
import com.jd.im.socket.TimingWheel;
import com.jd.im.utils.Log;

import com.jd.im.IMQTTMessage;
import com.jd.im.mqtt.MQTTHelper;
import com.jd.im.mqtt.messages.MQTTMessage;
import com.jd.im.storage.MessageStore;
import com.jd.im.storage.Persistentable;

import java.util.Iterator;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/6
 * Author:wangkai
 * Description:数据恢复
 * =====================================================
 */
public class RestoreDataAction implements Runnable {
    private static final String TAG = "RestoreDataAction";
    private MqttService mqttService;

    public RestoreDataAction(MqttService mqttService) {
        this.mqttService = mqttService;
    }

    @Override
    public void run() {
        MessageStore databaseMessageStore = mqttService.getMessageStore();
        if(databaseMessageStore == null)return;
        Iterator<IMQTTMessage> allArrivedMessages = databaseMessageStore.getAllArrivedMessages(mqttService.getClientHandle());
        if(allArrivedMessages!=null){
            TimingWheel<Event<MqttSenderAction>> eventTimingWheel = mqttService.getEventTimingWheel();
            while (allArrivedMessages.hasNext()) {
                IMQTTMessage message = allArrivedMessages.next();
                if (message == null || eventTimingWheel == null) continue;
                try {
                    boolean exist = eventTimingWheel.hasEvent(message.getPackageIdentifier());
                    if(!exist){
                        mqttService.getMqttQos().getIdentifierHelper().addSentPackage((MQTTMessage) message);
                        if(message instanceof Persistentable){
                            ((Persistentable) message).setPersistent();
                        }
                        mqttService.sendMessage(message);
                        Log.d(TAG, "类型" + MQTTHelper.decodePackageName(message.getType()) + ",id" + message.getPackageIdentifier() + "已经恢复...");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
