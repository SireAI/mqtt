package com.jd.jrapp.bm.message.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;



import java.util.Date;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/24
 * Author:Sire
 * Description:  原类型消息
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
     * 消息接收方用户id，用于消息区分存储加载删除等操作
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
     * 消息来源
     */
    private String fromUri;
    /**
     * 消息来源epid
     */
    private String fromEpid;

    /**
     *设备标识
     */
    private String fromDeviceId;

    /**
     * 发送者昵称
     */
    private String fromNickname;
    /**
     * 消息内容
     */
    private String content;

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getFromUri() {
        return fromUri;
    }

    public void setFromUri(String fromUri) {
        this.fromUri = fromUri;
    }

    public String getFromEpid() {
        return fromEpid;
    }

    public void setFromEpid(String fromEpid) {
        this.fromEpid = fromEpid;
    }

    public String getFromNickname() {
        return fromNickname;
    }

    public void setFromNickname(String fromNickname) {
        this.fromNickname = fromNickname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFromDeviceId() {
        return fromDeviceId;
    }

    public void setFromDeviceId(String fromDeviceId) {
        this.fromDeviceId = fromDeviceId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", read=" + read +
                ", userId='" + userId + '\'' +
                ", messageType=" + messageType +
                ", messageCreateTime=" + messageCreateTime +
                ", fromUri='" + fromUri + '\'' +
                ", fromEpid='" + fromEpid + '\'' +
                ", fromNickname='" + fromNickname + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
