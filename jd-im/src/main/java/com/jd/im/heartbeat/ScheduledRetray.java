package com.jd.im.heartbeat;

import android.content.Context;
import com.jd.im.utils.Log;

import com.jd.im.heartbeat.net.NetStatusUtil;

import java.util.Random;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/5
 * Author:wangkai
 * Description:重连机制闭环，心跳自适应中已经处理了断网重连以及一周
 * 期内的重连机制，以下主要是对服务器宕机重连处理
 * =====================================================
 */
public class ScheduledRetray implements Alarm.ICallBack {
    private static final String TAG = "ScheduledRetray";
    private  Context context;
    private ICallBack callBack;
    private static final int SCHEDULED_INTERVAL = 10 * 60 * 1000;

    public ScheduledRetray(Context context,ICallBack callBack) {
        this.callBack = callBack;
        this.context = context.getApplicationContext();
    }



    public void checkCondition(Context context) {
        if (NetStatusUtil.isConnected(context)) {
            Log.d(TAG, "网络连接正常，服务器可能宕机，开启调度连接模式...");
            scheduledConnect(context);
        }
    }

    public void scheduledConnect(Context context) {
        boolean success = Alarm.start(new Random().nextInt(1000), SCHEDULED_INTERVAL, context, this);
        if(!success){
            scheduledConnect(context);
        }
    }

    @Override
    public void onAlarm() {
        if (callBack != null && context!=null) {
            boolean terminate = callBack.onScheduleRetray();
            if(!terminate){
                scheduledConnect(context);
            }
        }
    }

    public interface ICallBack {
        boolean onScheduleRetray();
    }
}
