package com.jd.im.heartbeat.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.RestrictTo;
import android.text.TextUtils;

import com.jd.im.utils.Log;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description:网络状态监听，断连网，网络切换
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public abstract class NetState extends BroadcastReceiver {
    public static final int NOT_SET = -2;
    public static String TAG = "NetState";
    private ConnectivityManager mgr;
    private int currentNetType = NOT_SET;
    private boolean connected = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        if(mgr == null){
            mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
        // 如果相等的话就说明网络状态发生了变化
        checkNetState(context);
    }


    private void checkNetState(Context context) {
        int netWorkType = getNetWorkType();
         NetworkInfo activeNetworkInfo=null;
        try {
            activeNetworkInfo = mgr.getActiveNetworkInfo();
        }catch (Exception e){
        }
        //关闭情况
        if(activeNetworkInfo == null){
            updateNetParam(NOT_SET,false);
            onNetworkChange(context,false);
            return;
        }
        final boolean connectState = activeNetworkInfo.isConnected();
        if(netWorkType == currentNetType){
            //网络类型没有变，可能是断链了，也可能是统一网络类型不同网络的变化
            if(connected!=connectState){
                updateNetParam(netWorkType, connectState);
                onNetworkChange(context,connectState);
                return;
            }
        }else {
            updateNetParam(netWorkType, connectState);
            onNetworkChange(context, connectState);
        }
    }

    private void updateNetParam(int netWorkType, boolean connectState) {
        connected = connectState;
        currentNetType = netWorkType;
    }



    public abstract void onNetworkChange(Context context, boolean connected);

    /**
     * 没有连接网络
     */
    private static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    private static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    private static final int NETWORK_WIFI = 1;
    public  int getNetWorkType() {
        NetworkInfo activeNetworkInfo = mgr
                .getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_WIFI)) {
                return NETWORK_WIFI;
            } else if (activeNetworkInfo.getType() == (ConnectivityManager.TYPE_MOBILE)) {
                return NETWORK_MOBILE;
            }
        } else {
            return NETWORK_NONE;
        }
        return NETWORK_NONE;
    }
}

