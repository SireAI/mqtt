package com.jd.im.service;

import com.jd.im.utils.Log;


public class MqttReconnectAction {
    private static String TAG = "MqttReconnectAction";
   private Runnable connectTask;
   private int repeatCount;

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setConnectTask(Runnable connectTask) {
        this.connectTask = connectTask;
    }

    public void excute(){
        if(connectTask!=null){
            connectTask.run();
        }
        Log.d(TAG, "第" + repeatCount + "次尝试重新建立长连接流程...");

    }
}
