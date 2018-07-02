package com.jd.im.converter;


import com.google.protobuf.nano.MessageNano;
import com.jd.im.message.nano.Ack;

import java.util.HashMap;
import java.util.Map;

import static com.jd.im.mqtt.MQTTConnectionConstants.MESSAGE_ACK;
import static com.jd.im.mqtt.MQTTConstants.DISCONNECT;
import static com.jd.im.mqtt.MQTTConstants.PUBLISH;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description: 数据序列化protobuffer
 * =====================================================
 */
public class ProtobufferConverterFactory implements Converter.Factory {

    private static ProtobufferConverterFactory factory;

    private Map<Integer, Class> clazzType = new HashMap<>();

    public static ProtobufferConverterFactory create() {
        if (factory == null) {
            factory = new ProtobufferConverterFactory();
        }
        return factory;
    }

    /**
     * 消息类型与解析类型绑定
     *
     * @param messageType
     * @param clazz
     */
    public ProtobufferConverterFactory bind(int messageType, Class clazz) {
        clazzType.put(messageType, clazz);
        return this;
    }
    public ProtobufferConverterFactory bindPublish(Class clazz) {
        clazzType.put((int) PUBLISH, clazz);
        return this;
    }

    public ProtobufferConverterFactory bindDisconnect(Class clazz) {
        clazzType.put((int) DISCONNECT, clazz);
        return this;
    }
    public ProtobufferConverterFactory bindAck(Class clazz) {
        clazzType.put((int) MESSAGE_ACK, clazz);
        return this;
    }
    @Override
    public Class getClazzByType(int messageType) {
        return clazzType.get(messageType);
    }

    @Override
    public Converter<byte[], MessageNano> responseObjectConverter(Class clazz) {
        return new ResponseProtobuffer(clazz);
    }

    @Override
    public Converter<MessageNano, byte[]> objectRequestConverter() {
        return new ProtobufferRequest();
    }
}
