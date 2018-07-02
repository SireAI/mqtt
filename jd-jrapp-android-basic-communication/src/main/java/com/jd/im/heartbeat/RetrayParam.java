package com.jd.im.heartbeat;

import android.support.annotation.RestrictTo;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/31
 * Author:wangkai
 * Description:连接重连参数
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class RetrayParam {
    private int defaultRepeatCount;
    private int defaultReconnectTime ;
    private int repeatCount;
    private int reconnectTime;

    public RetrayParam(int defaultRepeatCount, int defaultReconnectTime) {
        this.defaultRepeatCount = defaultRepeatCount;
        this.defaultReconnectTime = defaultReconnectTime;
        this.repeatCount = this.defaultRepeatCount;
        this.reconnectTime = 0;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public int getReconnectTime() {
        return reconnectTime;
    }

    public void setReconnectTime(int reconnectTime) {
        this.reconnectTime = reconnectTime;
    }

    public void reset() {
        this.repeatCount = defaultRepeatCount;
        this.reconnectTime = 0;
    }

    public int repeatCountStepDown() {
        return --repeatCount;
    }

    public int reconnectTimeStepUp() {
        return reconnectTime += defaultReconnectTime;
    }

    public int repeatCountSteped(){
        return defaultRepeatCount-repeatCount;
    }

    public int getDefaultRepeatCount() {
        return defaultRepeatCount;
    }

    public boolean hasRetried(){
        return defaultRepeatCount != repeatCount;
    }
}