package com.jd.im.socket;

import android.support.annotation.RestrictTo;

import com.jd.im.mqtt.MqttMessageSpliter;

import java.io.IOException;
import java.net.Socket;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/26
 * Author:wangkai
 * Description: socket通信
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
 class WebSocket extends Socket {

    private MqttMessageSpliter mqttMessageSpliter;

    /**
     * 阻塞方法，直到有响应，或者错误
     * 自动扩展buffer，在返回数据前读完所有数据
     *
     * @return 大于0表示正常，0表示对端关闭或者空字节数据，-1表示对端关闭链接或者流
     * @throws IOException
     */
    public  byte[] listenServer() throws IOException {
        if(mqttMessageSpliter == null){
            mqttMessageSpliter = new MqttMessageSpliter(getInputStream());
        }
        return mqttMessageSpliter.readSingleMessageBytes();
    }

}
