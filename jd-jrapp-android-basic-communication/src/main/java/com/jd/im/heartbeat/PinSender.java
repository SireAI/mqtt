package com.jd.im.heartbeat;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.RestrictTo;

import com.jd.im.utils.Log;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/31
 * Author:wangkai
 * Description:处理pin发送
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class PinSender implements PinDetecter.PinDetectCallBack, Alarm.ICallBack {

    public static final String TAG = "PinSender";
    private static final int PIN_ID = 1001;
    private volatile static int TIME_INTERVAL = 3000;
    private ICallBack callBack;
    private Context context;
    private Handler taskSender;
    private Runnable pinTaskWrapper;
    private PinDetecter pinDetecter;
    private volatile long latestExchangeTime;

    public PinSender(Context context, Handler taskSender, Runnable pinSendTask, ICallBack callBack) {
        this.taskSender = taskSender;
        this.callBack = callBack;
        this.context = context.getApplicationContext();
        this.pinTaskWrapper = pinSendTask;
        this.pinDetecter = new PinDetecter(context, PinDetecter.ACTIVE_MODE, this);
        TIME_INTERVAL = pinDetecter.getFixedHeartInterval();
    }

    /**
     * 刷新通信通道时间,排除发送和接受pin数据情况
     */
    public void refreshLatestExchangeTime() {
        synchronized (PinSender.class) {
            this.latestExchangeTime = SystemClock.elapsedRealtime() - 1000;
        }
    }

    /**
     * 重新计时发送pin
     */
    public void start(long delta) {
        Alarm.start(PIN_ID, TIME_INTERVAL-delta, context, this);
    }

    public PinDetecter getPinDetecter() {
        return pinDetecter;
    }

    public void stop() {
        Alarm.stop(PIN_ID, context);
        if (taskSender != null && pinTaskWrapper!=null) {
            taskSender.removeCallbacks(pinTaskWrapper);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        release();
        super.finalize();
    }

    /**
     * 释放资源
     */
    public void release() {
        if (taskSender != null) {
            stop();
            taskSender = null;
        }
        if (pinTaskWrapper != null) {
            pinTaskWrapper = null;
        }
        if (pinDetecter != null ) {
            pinDetecter.release();
            pinDetecter = null;
            context = null;
        }
        if (taskSender != null && pinTaskWrapper != null) {
            taskSender.removeCallbacks(pinTaskWrapper);
            pinTaskWrapper = null;
            taskSender = null;
        }
    }

    public void reset() {
        stop();
        start(0);
    }

    @Override
    public void onHeartIntervalChanged(boolean needResetState, int newHeartInterval) {
        if (TIME_INTERVAL != newHeartInterval) {
            TIME_INTERVAL = newHeartInterval;
            Log.d(TAG, "new heart interval : " + newHeartInterval);
            if (needResetState) {
                reset();
            }
        }
    }

    @Override
    public void onNetConnected() {
        if (callBack != null && taskSender!=null) {
            taskSender.post(new Runnable() {
                @Override
                public void run() {
                    callBack.onNetConnected();
                }
            });
        }
    }

    @Override
    public void onNetLoss() {
        if (callBack != null) {
            callBack.onNetLoss();
        }
    }

    @Override
    public void onAlarm() {
        if (taskSender != null && pinTaskWrapper != null) {
            long now = SystemClock.elapsedRealtime();
            synchronized (PinSender.class) {
                long dataInterval = pinDetecter.getCurrentHeartInterval() - (now - latestExchangeTime);
                if (pinDetecter.isActive() && dataInterval >= 1000) {
                    start(dataInterval);
                } else {
                    taskSender.postAtFrontOfQueue(pinTaskWrapper);
                    start(0);
                }
            }
        }
    }


    public interface ICallBack {
        /**
         * 联网
         */
        void onNetConnected();

        /**
         * 断网
         */
        void onNetLoss();
    }

}
