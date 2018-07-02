package com.jd.jrapp.bm.message.Manager;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.jd.jrapp.bm.message.BR;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description: 文件上传状态
 * =====================================================
 */
public class UploadState extends BaseObservable {
    public static final int UPLOADING = 1;
    public static final int UPLOADED = 2;
    public static final int UPLOAD_FAILED = 3;
    private int progress;
    private int state;




    public UploadState(int progress, int state) {
        this.progress = progress;
        this.state = state;
    }

    public UploadState() {
    }

    @Bindable
    public int getProgress() {
        if (isUploaded()) {
            progress = 100;
        }
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        notifyPropertyChanged(BR.progress);
    }

    @Bindable
    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
        notifyPropertyChanged(BR.state);
    }

    public boolean isUploaded() {
        return state == UPLOADED;
    }


}
