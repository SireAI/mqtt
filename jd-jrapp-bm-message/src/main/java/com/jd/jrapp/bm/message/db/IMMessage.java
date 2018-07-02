package com.jd.jrapp.bm.message.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;


import com.jd.jrapp.bm.message.adapter.Element;
import com.jd.jrapp.bm.message.bean.content.IMultiContent;
import com.jd.jrapp.bm.message.constant.Constant;

import java.util.Date;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/02/23
 * Author:Sire
 * Description:存储IM消息对话数据
 * ==================================================
 */
@Entity(primaryKeys = {"messageId"},indices = {@Index(name = "from_to_id",value = {"fromAuthorId","toAuthorId"})})
public class IMMessage implements Element {
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
     *设备标识
     */
    private String fromDeviceId;

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
    @Ignore
    private String fromAuthorImg;
    /**
     * 接受用户头像
     */
    @Ignore
    private String toAuthorImg;
    /**
     * 内容对象
     */
    @Ignore
    private IMultiContent contentObj;
    /**
     * 消息创建时间
     */
    private Date messageCreateTime;

    private String failedInfor;

    private String contentType;


    /**
     * 消息发送状态
     */
    private int transportState = Constant.T_SENDING;


    /**
     * 内容
     */
    private byte[] content;

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

    public String getFailedInfor() {
        return failedInfor;
    }

    public void setFailedInfor(String failedInfor) {
        this.failedInfor = failedInfor;
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

    @Override
    public String diffId() {
        return getMessageId();
    }

    @Override
    public String diffContent() {
        return toString();
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
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

    public String getFromDeviceId() {
        return fromDeviceId;
    }

    public void setFromDeviceId(String fromDeviceId) {
        this.fromDeviceId = fromDeviceId;
    }

    @Override
    public String toString() {
        return "IMMessage{" +
                "messageId='" + messageId + '\'' +
                ", read=" + read +
                ", showTime=" + showTime +
                ", fromAuthorId='" + fromAuthorId + '\'' +
                ", fromDeviceId='" + fromDeviceId + '\'' +
                ", toAuthorId='" + toAuthorId + '\'' +
                ", fromAuthorName='" + fromAuthorName + '\'' +
                ", toAuthorName='" + toAuthorName + '\'' +
                ", fromAuthorImg='" + fromAuthorImg + '\'' +
                ", toAuthorImg='" + toAuthorImg + '\'' +
                ", messageCreateTime=" + messageCreateTime +
                ", failedInfor='" + failedInfor + '\'' +
                ", transportState=" + transportState +
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getTransportState() {
        return transportState;
    }

    public void setTransportState(int transportState) {

        this.transportState = transportState;
    }

    public IMultiContent getContentObj() {
        return contentObj;
    }

    public void setContentObj(IMultiContent contentObj) {
        this.contentObj = contentObj;
    }
}