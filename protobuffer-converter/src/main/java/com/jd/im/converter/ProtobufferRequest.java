package com.jd.im.converter;

import com.jd.im.message.ClientPublishMessage;

import java.io.IOException;

public class ProtobufferRequest implements Converter<ClientPublishMessage.MessageRequst, byte[]> {
    @Override
    public byte[] convert(ClientPublishMessage.MessageRequst value) throws IOException {
        return value.toByteArray();
    }
}
