package com.jd.jrapp.bm.message.bean;

import android.os.Parcel;
import android.os.Parcelable;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/20
 * Author:wangkai
 * Description:
 * =====================================================
 */
public class Talker implements Parcelable {
    /**
     * 对话对象
     */
    private Talker peerTalker;
    private String userName;
    private String userId;
    private String userImg;
    private String clientId;

    public Talker getPeerTalker() {
        return peerTalker;
    }

    public void setPeerTalker(Talker peerTalker) {
        this.peerTalker = peerTalker;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.peerTalker, flags);
        dest.writeString(this.userName);
        dest.writeString(this.userId);
        dest.writeString(this.userImg);
        dest.writeString(this.clientId);
    }

    public Talker() {
    }

    protected Talker(Parcel in) {
        this.peerTalker = in.readParcelable(Talker.class.getClassLoader());
        this.userName = in.readString();
        this.userId = in.readString();
        this.userImg = in.readString();
        this.clientId = in.readString();
    }

    public static final Creator<Talker> CREATOR = new Creator<Talker>() {
        @Override
        public Talker createFromParcel(Parcel source) {
            return new Talker(source);
        }

        @Override
        public Talker[] newArray(int size) {
            return new Talker[size];
        }
    };
}
