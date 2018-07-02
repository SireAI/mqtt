package com.jd.im.converter;

import com.google.protobuf.nano.MessageNano;
import com.jd.im.message.nano.ImageMessage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/26
 * Author:wangkai
 * Description: IM 具体内容解析
 * =====================================================
 */
public class MsgContentResolve {
    public static final String TEXT_PLAIN = "text/plain";
    public static final String IMAGE = "image/*";
    private static Map<String, Class> RELATION = new HashMap<>();
    /**
     * 文件类型
     */
    public static final String[] FILE_TYPE = {IMAGE};
    static {
        RELATION.put(TEXT_PLAIN, String.class);
        RELATION.put(IMAGE, ImageMessage.Image.class);
    }

    public static boolean isFileType(String contentType) {
        return Arrays.asList(FILE_TYPE).contains(contentType);
    }

    public static Object resolve(String contentType, byte[] content) {
        try {
            Class clazz = RELATION.get(contentType);
            if (clazz == String.class) {
                return  new String(content);
            } else if (clazz == ImageMessage.Image.class) {
                MessageNano messageNano = MessageNano.mergeFrom((MessageNano) clazz.newInstance(), content);
                return messageNano;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isText(String contentType) {
        return TEXT_PLAIN.equals(contentType);
    }

    public static boolean isImage(String contentType) {
        return IMAGE.equals(contentType);
    }

}
