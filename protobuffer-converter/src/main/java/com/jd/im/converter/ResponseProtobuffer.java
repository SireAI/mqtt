package com.jd.im.converter;



import com.jd.im.message.ClientPublishMessage;

import java.io.IOException;

public class ResponseProtobuffer implements Converter<byte[], ClientPublishMessage.MessageResponse> {
    @Override
    public ClientPublishMessage.MessageResponse convert(byte[] value) throws IOException {
        return ClientPublishMessage.MessageResponse.parseFrom(value);
    }
}
