package com.jd.jrapp.bm.message.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.databinding.ObservableBoolean;
import android.support.annotation.NonNull;



import java.util.Date;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/24
 * Author:Sire
 * Description:
 * string uuid = 1;        // 消息编号 不能重复
 string fromUri = 2;            // 消息来源uri
 string fromEpid = 3;            // 消息来源epid
 string fromNickname = 4;           // 发送者
 string contentType = 5;           // 消息类型
 bytes contentBuffer = 6;            // 消息内容
 * ==================================================
 */
@Entity
public class Message {

    /**
     * 消息编号
     */
    @PrimaryKey
    @NonNull
    private String messageId;
    /**
     * 消息是否阅读
     */
    private boolean read = false;

    /**
     * 消息接收方用户id，用于消息区分加载
     */
    private String userId;
    /**
     * 消息类型,默认未定义
     */
    private int messageType = -1;

    /**
     * 消息创建时间
     */
    private Date messageCreateTime;


    /**
     * 消息数据
     */
    private String jsonData;

    /**
     * 消息数据
     */
    @Ignore
    private Object data;
    @Ignore
    private ObservableBoolean messageRead = new ObservableBoolean(false);

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public Date getMessageCreateTime() {
        return messageCreateTime;
    }

    public void setMessageCreateTime(Date messageCreateTime) {
        this.messageCreateTime = messageCreateTime;
    }

    public ObservableBoolean getMessageRead() {
        return messageRead;
    }

    public void setMessageRead(ObservableBoolean messageRead) {
        this.messageRead = messageRead;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
        messageRead.set(read);
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "read=" + read +
                ", messageId='" + messageId + '\'' +
                ", userId='" + userId + '\'' +
                ", messageType=" + messageType +
                ", messageCreateTime=" + messageCreateTime +
                ", jsonData='" + jsonData + '\'' +
                ", data=" + data +
                ", messageRead=" + messageRead +
                '}';
    }
}
