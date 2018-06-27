package com.jd.jrapp.bm.message.configure;

import android.databinding.BaseObservable;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description: 文件上传状态
 * =====================================================
 */
public class UploadState extends BaseObservable{
    public static final int UPLOADING = 1;
    public static final int UPLOADED = 2;
    public static final int UPLOAD_FAILED = 3;
    private long progress;
    private int state;

    public UploadState(long progress, int state) {
        this.progress = progress;
        this.state = state;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
        notifyChange();
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        notifyChange();
    }
}
