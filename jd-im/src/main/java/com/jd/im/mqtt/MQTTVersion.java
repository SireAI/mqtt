package com.jd.im.mqtt;


import android.support.annotation.IntDef;

import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:mqtt协议设定
 * =====================================================
 */
public class MQTTVersion {
    public static final String VERSION_31 = "MQIsdp";
    public static final String VERSION_311 = "MQTT";
    public static final String VERSION_IM = "IM";
    private static Map<String,Integer> versions = new HashMap<>();

    static {
        versions.put("MQIsdp",0x03);
        versions.put("MQTT",0x04);
        versions.put("IM",0x02);
    }

    public static int getVersionCode( String versionName) {
        return versions.get(versionName);
    }

}
