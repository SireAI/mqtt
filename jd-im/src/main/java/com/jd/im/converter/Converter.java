package com.jd.im.converter;

import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/24
 * Author:wangkai
 * Description:数据转换接口
 * =====================================================
 */
public interface Converter<F, T> {
    /**
     * 数据转换
     *
     * @param value
     * @return
     * @throws IOException
     */
    T convert(F value) throws IOException;

    /**
     * 对象工厂
     */
     interface Factory {

        /**
         * 字节转换为对象
         *
         * @return
         */
        public @Nullable
        Converter<byte[], ?> responseObjectConverter() ;

        /**
         * 对象转换为字节
         *
         * @return
         */
        public @Nullable
        Converter<?, byte[]> objectRequestConverter() ;

    }
}