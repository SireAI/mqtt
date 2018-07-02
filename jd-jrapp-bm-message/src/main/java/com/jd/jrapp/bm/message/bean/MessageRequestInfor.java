package com.jd.jrapp.bm.message.bean;

import java.util.Date;

public class MessageRequestInfor {
    private Date timeLine;
    private int pageSize;
    private String toUserId;
    private String fromUserId;

    public MessageRequestInfor( String userId, String fromUserId,Date timeLine, int pageSize) {
        this.timeLine = timeLine;
        this.pageSize = pageSize;
        this.toUserId = userId;
        this.fromUserId = fromUserId;
    }

    public MessageRequestInfor(Date timeLine, int pageSize) {
        this.timeLine = timeLine;
        this.pageSize = pageSize;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Date getTimeLine() {
        return timeLine;
    }

    public void setTimeLine(Date timeLine) {
        this.timeLine = timeLine;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }
}