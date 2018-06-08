package com.jd.im.converter;

import com.jd.im.message.ClientPublishMessage;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description: 数据序列化protobuffer
 * =====================================================
 */
public class ProtobufferConverterFactory implements Converter.Factory{

    private static Converter.Factory factory;

    public static Converter.Factory create() {
        if (factory == null) {
            factory = new ProtobufferConverterFactory();
        }
        return factory;
    }

    @Override
    public Converter<byte[], ClientPublishMessage.MessageResponse> responseObjectConverter() {
        return new ResponseProtobuffer();
    }

    @Override
    public Converter<ClientPublishMessage.MessageRequst, byte[]> objectRequestConverter() {
        return new ProtobufferRequest();
    }
}
