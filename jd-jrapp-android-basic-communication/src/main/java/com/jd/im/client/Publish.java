package com.jd.im.client;


/**
 * =====================================================
 * All Right Reserved
 * Date:2018/5/29
 * Author:wangkai
 * Description:发布消息封装
 * =====================================================
 */
 class Publish {
    private String topic;
    private Object payload;
    private MessageCallBack messageCallBack;
    private int codingType = 3;
    private int messageType = 1;


    public static Publish create(String topic,Object payload,int codingType,int messageType,MessageCallBack messageCallBack){
        return new Publish(topic,payload,messageCallBack,codingType,messageType);
    }

    public Publish(String topic, Object payload, MessageCallBack messageCallBack, int codingType, int messageType) {
        this.topic = topic;
        this.payload = payload;
        this.messageCallBack = messageCallBack;
        this.codingType = codingType;
        this.messageType = messageType;
    }

    public String getTopic() {
        return topic;
    }

    public MessageCallBack getMessageCallBack() {
        return messageCallBack;
    }

    public Object getPayload() {
        return payload;
    }

    public int getCodingType() {
        return codingType;
    }

    public void setCodingType(int codingType) {
        this.codingType = codingType;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
