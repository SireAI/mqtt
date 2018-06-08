package com.jd.im.client;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description: 序列化和反序列化任务
 * =====================================================
 */
 class Task<T> {
    public static int SERIALIZE_REQUEST = -5;
    public static int DESERIALIZE_RESPONSE = -6;

    private int type;
    private T object;


    public static <T> Task create(int type, T object){
        return new Task<>(type,object);
    }

    private Task(int type, T object) {
        this.type = type;
        this.object = object;
    }

    public int getType() {
        return type;
    }



    public T getObject() {
        return object;
    }


}
