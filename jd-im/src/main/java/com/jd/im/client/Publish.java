package com.jd.im.client;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:发布消息封装
 * =====================================================
 */
 class Publish<T> {
    private String topic;
    private T payload;
    private MessageCallBack messageCallBack;

    public static <T>Publish create(String topic,T payload,MessageCallBack messageCallBack){
        return new Publish(topic,payload,messageCallBack);
    }
    private Publish(String topic, T payload, MessageCallBack messageCallBack){
        this.topic = topic;
        this.payload = payload;
        this.messageCallBack = messageCallBack;
    }

    public String getTopic() {
        return topic;
    }

    public MessageCallBack getMessageCallBack() {
        return messageCallBack;
    }

    public T getPayload() {
        return payload;
    }
}
