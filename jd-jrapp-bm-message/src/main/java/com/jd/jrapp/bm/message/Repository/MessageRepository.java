package com.jd.jrapp.bm.message.repository;


import android.arch.lifecycle.LiveData;

import com.google.protobuf.nano.MessageNano;
import com.jd.im.client.MessageCallBack;
import com.jd.im.client.MqttClient;
import com.jd.jrapp.bm.message.Manager.UploadState;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.db.Message;
import com.jd.jrapp.bm.message.db.MessageDao;

import java.util.Date;
import java.util.List;


/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/24
 * Author:Sire
 * Description:  数据仓库
 * ==================================================
 */
public class MessageRepository {
    private final MessageDao messageDao;

    public MessageRepository(MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

    public void localSave(Message message) {
        messageDao.savePushMessage(message);
    }


    public void setMessageRead(Message message) {
        messageDao.updateMessage(message);
    }

    public void saveIMMessage(IMMessage data) {
        messageDao.saveIMMessage(data);
    }

    public Message findMessageByID(String messageId) {
        return messageDao.findMessageById(messageId);
    }

    public void updateIMMessage(IMMessage imMessage) {
        messageDao.updateIMMessage(imMessage);
    }

    public void updateContent(Message message) {
        messageDao.updateMessage(message);
    }




    public void setIMMessageRead(IMMessage item) {
        messageDao.updateIMMessage(item);
    }

    public Date getLatestIMMessageTime() {
        return messageDao.findLatestIMMessageTime();
    }

    public LiveData<List<IMMessage>> listenUploadMessage(String[] uploadMessages) {
       return messageDao.queryUploadMessage(uploadMessages);
    }


    public IMMessage queryIMMessageById(String messageId) {
        return messageDao.queryIMMessagesById(messageId);
    }
}
