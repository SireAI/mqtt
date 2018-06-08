package com.jd.im.client;

import android.util.SparseArray;

import com.jd.im.mqtt.MQTTException;
import com.jd.im.utils.Log;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/27
 * Author:wangkai
 * Description:用来管理各种客户端操作的回应识别
 * =====================================================
 */
class TokenManager {
    private static String TAG = "TokenManager";
    private SparseArray<MessageCallBack> tokens = new SparseArray<>();
    private DefaultCallBack defaultCallBack = new DefaultCallBack();

    public MessageCallBack get(int key) {
        defaultCallBack.setMessageIdentifier(key);
        return tokens.get(key, defaultCallBack);
    }

    public void remove(int key) {
        tokens.remove(key);
    }

    public void put(int key, MessageCallBack value) {
        tokens.put(key, value);
    }

    public void release() {
        if (tokens != null) {
            tokens.clear();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    public void notifyAllFailed(MQTTException mqttException) {
        for (int i = 0; i < tokens.size(); i++) {
            int id = tokens.keyAt(i);
            tokens.get(id).onFailed(mqttException);
        }
        release();
    }

    private class DefaultCallBack implements MessageCallBack {
        private int messageIdentifier = 0;

        public void setMessageIdentifier(int messageIdentifier) {
            this.messageIdentifier = messageIdentifier;
        }

        @Override
        public void onSuccess() {
            Log.d(TAG, "协议id" + messageIdentifier + "消息操作成功");
        }

        @Override
        public void onFailed(MQTTException exception) {
            Log.d(TAG, "协议id" + messageIdentifier + "消息操作失败:" + exception.getMessage());
        }
    }
}
