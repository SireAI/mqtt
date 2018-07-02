package com.jd.im.service;

import android.os.RemoteException;
import com.jd.im.utils.Log;

import com.jd.im.IMQTTMessage;
import com.jd.im.socket.SocketWorker;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:发送消息任务
 * =====================================================
 */
public class MqttSenderAction implements Runnable {
    private static String TAG = "MqttSenderAction";
    private final SocketWorker socketWorker;
    private final IMQTTMessage message;
    private PersistentDataAction persistentDataAction;

    public MqttSenderAction(SocketWorker socketWorker, IMQTTMessage message) {
        this.socketWorker = socketWorker;
        this.message = message;
    }

    public void setPersistentDataAction(PersistentDataAction persistentDataAction) {
        this.persistentDataAction = persistentDataAction;
    }

    @Override
    public void run() {
        if(persistentDataAction!=null){
            persistentDataAction.run();
        }
        try {
            if (socketWorker != null && socketWorker.isAlive() && !socketWorker.isInterrupted() && message != null) {
                socketWorker.sendData(message.get());
            } else {
                Log.w(TAG, "socketWorker 已经中断，无法发送消息");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public IMQTTMessage getMessage() {
        return message;
    }
}