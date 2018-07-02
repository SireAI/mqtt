package com.jd.im.client;

import android.support.annotation.RestrictTo;
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
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class TokenManager {
    private static String TAG = "TokenManager";
    private SparseArray<MessageCallBack> tokens = new SparseArray<>();
    private DefaultCallBack defaultCallBack = new DefaultCallBack();

    public MessageCallBack get(int key) {
        defaultCallBack.setMessageIdentifier(key);
        return tokens.get(key, defaultCallBack);
    }

    public void setDefaultCallBack(OutputCallBack outputCallBack){
        if(defaultCallBack!=null){
            defaultCallBack.setOutputCallBack(outputCallBack);
        }
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
        private OutputCallBack outputCallBack;

        public void setMessageIdentifier(int messageIdentifier) {
            this.messageIdentifier = messageIdentifier;
        }


        @Override
        public void onSuccess(Object extraInfor) {
            Log.d(TAG, "协议id" + messageIdentifier + "消息操作成功");
            if(outputCallBack!=null){
                outputCallBack.onSuccess(extraInfor);
            }
        }

        @Override
        public void onFailed(MQTTException exception) {
            Log.d(TAG, "协议id" + messageIdentifier + "消息操作失败:" + exception.getMessage());
        }
        public void setOutputCallBack(OutputCallBack outputCallBack){
            this.outputCallBack = outputCallBack;
        }
    }
}
