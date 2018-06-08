package com.jd.im.converter;

import java.io.IOException;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/28
 * Author:wangkai
 * Description:数据序列化反序列化处理
 * =====================================================
 */
public class ConverterProcessor {

    private Converter.Factory factory;


    public ConverterProcessor(Converter.Factory factory) {
        this.factory = factory;
    }

    public <T> byte[] serialize(T request) {
        if (factory != null) {
            Converter<T, byte[]> converter = (Converter<T, byte[]>) factory.objectRequestConverter();
            try {
                return converter.convert(request);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public <T> T deserialize(byte[] response) {
        if (factory != null) {
            Converter<byte[], T> converter = (Converter<byte[], T>) factory.responseObjectConverter();
            try {
                return converter.convert(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
