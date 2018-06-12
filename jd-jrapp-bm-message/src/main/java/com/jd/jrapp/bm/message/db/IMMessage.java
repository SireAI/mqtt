package com.jd.jrapp.bm.message.db;

import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;



import java.util.Date;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/02/23
 * Author:Sire
 * Description:存储IM消息对话数据
 * ==================================================
 */
@Entity(primaryKeys = "messageId")
public class IMMessage {
    @NonNull
    private String messageId;
    /**
     * 是否阅读过
     */
    private boolean read = false;

    /**
     * 是否显示时间
     */
    private boolean showTime = true;

    /**
     * 发表的用户id
     */
    private String fromAuthorId;
    /**
     * 接受用户的id
     */
    private String toAuthorId;

    /**
     * 发表用户的名字
     */
    private String fromAuthorName;
    /**
     * 接受用户的名字
     */
    private String toAuthorName;
    /**
     * 发表头像
     */
    private String fromAuthorImg;
    /**
     * 接受用户头像
     */
    private String toAuthorImg;
    /**
     * 消息创建时间
     */
    private Date messageCreateTime;

    /**
     * 内容
     */
    private String content;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getFromAuthorId() {
        return fromAuthorId;
    }

    public void setFromAuthorId(String fromAuthorId) {
        this.fromAuthorId = fromAuthorId;
    }

    public String getToAuthorId() {
        return toAuthorId;
    }

    public void setToAuthorId(String toAuthorId) {
        this.toAuthorId = toAuthorId;
    }

    public String getFromAuthorName() {
        return fromAuthorName;
    }

    public void setFromAuthorName(String fromAuthorName) {
        this.fromAuthorName = fromAuthorName;
    }

    public String getToAuthorName() {
        return toAuthorName;
    }

    public void setToAuthorName(String toAuthorName) {
        this.toAuthorName = toAuthorName;
    }

    public String getFromAuthorImg() {
        return fromAuthorImg;
    }

    public void setFromAuthorImg(String fromAuthorImg) {
        this.fromAuthorImg = fromAuthorImg;
    }

    public String getToAuthorImg() {
        return toAuthorImg;
    }

    public void setToAuthorImg(String toAuthorImg) {
        this.toAuthorImg = toAuthorImg;
    }

    public Date getMessageCreateTime() {
        return messageCreateTime;
    }

    public void setMessageCreateTime(Date messageCreateTime) {
        this.messageCreateTime = messageCreateTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }

    /**
     * 获取对话者头像
     * @return
     */
    public String getFromAuthorImageUrl(String currentUserId){
        if(currentUserId.equals(fromAuthorId)){
            return toAuthorImg;
        }
        if(currentUserId.equals(toAuthorId)){
            return fromAuthorImg;
        }
        return null;
    }

    /**
     * 获取对话者名字
     * @return
     */
    public String getTalkAuthorName(String currentUserId){
        if(currentUserId.equals(fromAuthorId)){
            return toAuthorName;
        }
        if(currentUserId.equals(toAuthorId)){
            return fromAuthorName;
        }
        return null;
    }

    @Override
    public String toString() {
        return "IMMessage{" +
                "messageId='" + messageId + '\'' +
                ", read=" + read +
                ", showTime=" + showTime +
                ", fromAuthorId='" + fromAuthorId + '\'' +
                ", toAuthorId='" + toAuthorId + '\'' +
                ", fromAuthorName='" + fromAuthorName + '\'' +
                ", toAuthorName='" + toAuthorName + '\'' +
                ", fromAuthorImg='" + fromAuthorImg + '\'' +
                ", toAuthorImg='" + toAuthorImg + '\'' +
                ", messageCreateTime=" + messageCreateTime +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IMMessage imMessage = (IMMessage) o;

        if (messageId != null ? !messageId.equals(imMessage.messageId) : imMessage.messageId != null) {
            return false;
        }
        return (fromAuthorId != null ? fromAuthorId.equals(imMessage.fromAuthorId) : imMessage.fromAuthorId == null) && (toAuthorId != null ? toAuthorId.equals(imMessage.toAuthorId) : imMessage.toAuthorId == null);
    }

}