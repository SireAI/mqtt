package com.jd.jrapp.bm.message.model;


public class MessageModel {
    private static MessageModel instance = new MessageModel();

    private MessageModel() {
    }

    public static MessageModel getInstance() {
        return instance;
    }

    public void onReceiveMessage(){

    }
}
