package com.jd.im.converter;

import android.support.annotation.Nullable;

public class StringConverterFactory implements Converter.Factory {
    private static Converter.Factory factory;

    public static Converter.Factory create() {
        if (factory == null) {
            factory = new StringConverterFactory();
        }
        return factory;
    }


    @Override
    public Class getClazzByType(int messageType) {
        return null;
    }

    @Nullable
    @Override
    public Converter<byte[], ?> responseObjectConverter(Class clazz) {
        return new ResponseStringConverter();
    }

    @Nullable
    @Override
    public Converter<String, byte[]> objectRequestConverter() {
        return new StringRequestConverter();
    }
}
