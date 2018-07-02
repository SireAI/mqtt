package com.jd.jrapp.bm.message.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/20
 * Author:wangkai
 * Description:
 * =====================================================
 */
public class CommonUtils {
    public static String getPhoneSign(Context context) {
        StringBuilder deviceId = new StringBuilder();
        try {
            //IMEI（imei）
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return generateId(context, deviceId);
            }
            String imei = tm.getDeviceId();
            if(!TextUtils.isEmpty(imei)){
                deviceId.append("imei");
                deviceId.append(imei);
                return deviceId.toString();
            }
            //序列号（sn）
            String sn = tm.getSimSerialNumber();
            if(!TextUtils.isEmpty(sn)){
                deviceId.append("sn");
                deviceId.append(sn);
                return deviceId.toString();
            }
            //如果上面都没有， 则生成一个id：随机码
            return generateId(context,deviceId);
        } catch (Exception e) {
            e.printStackTrace();
            deviceId.append("id").append(getUUID(context));
        }
        return deviceId.toString();
    }

    @Nullable
    private static String generateId(Context context, StringBuilder deviceId) {
        String uuid = getUUID(context);
        if(!TextUtils.isEmpty(uuid)){
            deviceId.append("id");
            deviceId.append(uuid);
            return deviceId.toString();
        }
        return uuid;
    }

    private static String getUUID(Context context){
        SharedPreferences mShare = context.getSharedPreferences("uuid",MODE_PRIVATE);
        String uuid = null;
        if(mShare != null){
            uuid = mShare.getString("uuid", "");
        }
        if(TextUtils.isEmpty(uuid)){
            uuid = UUID.randomUUID().toString().replace("-","");
            mShare.edit().putString("uuid",uuid).commit();
        }
        return uuid;
    }

    public static String getVersionName(Context context)
    {
        // 获取packagemanager的实例
        PackageManager packageManager = context.getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        String version = "";
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(),0);
             version = packInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return version;
    }

    /**关闭键盘**/
    public static void hideSoftInput(View paramEditText) {
        ((InputMethodManager) paramEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(paramEditText.getWindowToken(), 0);
    }
    /**
     * double click,change the number to define click time
     */
    private static long[] mHits = new long[2];
    public static boolean isFastClick(long passTime) {
        System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);
        mHits[mHits.length - 1] = SystemClock.uptimeMillis();
        if (mHits[0] >= (SystemClock.uptimeMillis() - (passTime <= 500 ? 500 : passTime))) {
            return true;
        }
        return false;
    }
}
