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
    public static NetworkInfo lastActiveNetworkInfo = null;
    public static WifiInfo lastWifiInfo = null;
    public static boolean lastConnected = true;
    public static String TAG = "NetState";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null || intent == null) {
            return;
        }
        ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = null;
        try {
            netInfo = mgr.getActiveNetworkInfo();
        } catch (Exception e) {
            Log.i(TAG, "getActiveNetworkInfo failed.");
        }

        checkConnInfo(context, netInfo);
    }

    private void checkConnInfo(final Context context, final NetworkInfo activeNetInfo) {

        if (activeNetInfo == null) {
            lastActiveNetworkInfo = null;
            lastWifiInfo = null;
            onNetworkChange(context);
        } else if (activeNetInfo.getDetailedState() != NetworkInfo.DetailedState.CONNECTED) {

            if (lastConnected) {
                lastActiveNetworkInfo = null;
                lastWifiInfo = null;
                onNetworkChange(context);
            }

            lastConnected = false;
        } else {
            if (isNetworkChange(context, activeNetInfo)) {
                onNetworkChange(context);
            }
            lastConnected = true;
        }

    }


    /**
     * 与上次网络信息相比，是否发生网络切换
     *
     * @param context
     * @param activeNetInfo
     * @return
     */
    private boolean isNetworkChange(final Context context, final NetworkInfo activeNetInfo) {
        boolean isWifi = (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI);
        if (isWifi) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wi = wifiManager.getConnectionInfo();
            if (wi != null && lastWifiInfo != null && !TextUtils.isEmpty(lastWifiInfo.getBSSID()) && lastWifiInfo.getBSSID().equals(wi.getBSSID())
                    && lastWifiInfo.getSSID().equals(wi.getSSID())
                    && lastWifiInfo.getNetworkId() == wi.getNetworkId()) {
                Log.w(TAG, "Same Wifi, do not NetworkChanged");
                return false;
            }
            lastWifiInfo = wi;
        } else if (lastActiveNetworkInfo != null
                && lastActiveNetworkInfo.getExtraInfo() != null && activeNetInfo.getExtraInfo() != null
                && lastActiveNetworkInfo.getExtraInfo().equals(activeNetInfo.getExtraInfo())
                && lastActiveNetworkInfo.getSubtype() == activeNetInfo.getSubtype()
                && lastActiveNetworkInfo.getType() == activeNetInfo.getType()) {
            return false;

        } else if (lastActiveNetworkInfo != null
                && lastActiveNetworkInfo.getExtraInfo() == null && activeNetInfo.getExtraInfo() == null
                && lastActiveNetworkInfo.getSubtype() == activeNetInfo.getSubtype()
                && lastActiveNetworkInfo.getType() == activeNetInfo.getType()) {
            Log.w(TAG, "Same Network, do not NetworkChanged");
            return false;
        }
        lastActiveNetworkInfo = activeNetInfo;
        return true;
    }

    public abstract void onNetworkChange(Context context);
}
