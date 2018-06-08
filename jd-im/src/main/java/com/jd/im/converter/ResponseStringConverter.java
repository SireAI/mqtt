package com.jd.im.converter;

import java.io.IOException;

public class ResponseStringConverter implements Converter<byte[],String>{

    @Override
    public String convert(byte[] value) throws IOException {
        return new String(value,"utf-8");
    }
}
