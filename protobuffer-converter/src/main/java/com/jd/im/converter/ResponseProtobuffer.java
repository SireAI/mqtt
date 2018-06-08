package com.jd.im.converter;


import com.google.protobuf.nano.MessageNano;
import com.jd.im.utils.Log;

import java.io.IOException;

public class ResponseProtobuffer<T extends MessageNano> implements Converter<byte[], MessageNano> {
    private static final String TAG = "ResponseProtobuffer";
    private Class<T> clazz;

    public ResponseProtobuffer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T convert(byte[] value) throws IOException, IllegalAccessException, InstantiationException {
        if(clazz == null){
            Log.e(TAG,"反序列化对象未绑定消息类型");
            return null;
        }
        return MessageNano.mergeFrom(clazz.newInstance(), value);
    }
}
