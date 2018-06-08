package com.jd.im.converter;

import android.text.TextUtils;

import java.io.IOException;

public class StringRequestConverter implements Converter<String,byte[]> {
    @Override
    public byte[] convert(String value) throws IOException {
        byte[] data = new byte[0];
        if(!TextUtils.isEmpty(value)){
            data = value.getBytes("utf-8");
        }
        return data;
    }
}
