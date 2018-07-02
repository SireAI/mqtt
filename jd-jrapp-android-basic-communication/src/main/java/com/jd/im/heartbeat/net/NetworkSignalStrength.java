package com.jd.im.heartbeat.net;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.RestrictTo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import com.jd.im.utils.Log;

import java.lang.reflect.Method;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/30
 * Author:wangkai
 * Description:手机信号强度监控
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class NetworkSignalStrength extends PhoneStateListener {
    public static final String TAG = "NetworkSignalStrength";
    public static final String[] leves = {
            "none", "poor", "moderate", "good", "great"
    };
    private static int strength = 0;
    private static Context context = null;

    public NetworkSignalStrength(Context context) {
        this.context = context.getApplicationContext();
        TelephonyManager mgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //监听手机通信信号强弱
        mgr.listen(this, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    /**
     * 分五级
     *
     * @return
     */
    public int getSignalLevel() {
        boolean mobile = NetStatusUtil.isMobile(context);
        try {
            if (mobile) {
                return getMobileSignalStrength();
            } else {
                return getWifiSignalStrength();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private int getMobileSignalStrength() {
        return strength;
    }

    private int getWifiSignalStrength() {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        int levelNum = leves.length;
        if (info != null && info.getBSSID() != null) {
            int sig = WifiManager.calculateSignalLevel(info.getRssi(), levelNum);
            sig = (sig > levelNum-1 ? levelNum-1 : sig);
            sig = (sig < 0 ? 0 : sig);
            return sig;
        }
        return 0;
    }

    /**
     * 信号强度变化
     * SIM卡分两种：GSM网络制式卡和CDMA网络制式卡，
     * GSM的卡只能用在GSM手机上，而CDMA也只能用于CDMA手机
     * ，因为两种卡所接入的网络不同（G网和C网）
     *
     * @param signalStrength
     */
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        int level = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            level = signalStrength.getLevel();
        } else {
            try {
                // TODO: 2018/5/31 低版本需要测试
                Method method = signalStrength.getClass().getMethod("getLevel");
                method.setAccessible(true);
                level = (int) method.invoke(signalStrength);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(strength != level){
            Log.d(TAG,"wifi signal strength:"+leves[level]);
            strength = level;
        }
    }
}
