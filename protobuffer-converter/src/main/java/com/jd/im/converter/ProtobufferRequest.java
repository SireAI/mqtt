package com.jd.im.converter;


import com.google.protobuf.nano.MessageNano;
import com.jd.im.message.nano.ClientPublishMessage;

import java.io.IOException;

public class ProtobufferRequest implements Converter<MessageNano, byte[]> {
    @Override
    public byte[] convert(MessageNano value) throws IOException {

        return MessageNano.toByteArray(value);
    }
}
