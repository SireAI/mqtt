package com.jd.jrapp.bm.message.configure;

import com.jd.im.socket.BlockingLooper;

/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description:文件上传管理器
 * =====================================================
 */
public class UploadManager implements BlockingLooper.Callback<UploadTask> {

    private static UploadManager instance;
    private BlockingLooper<UploadTask> uploadTasksLooper;
    private IFileUpload fileUpload;

    private UploadManager() {
        uploadTasksLooper = new BlockingLooper<>(this);
        fileUpload = new FileUploadImpl();
    }

    public static UploadManager getInstance() {
        if (instance == null) {
            instance = new UploadManager();
        }
        return instance;
    }

    @Override
    public boolean meetTheCondition() {
        return true;
    }

    @Override
    public void onHandleTask(UploadTask uploadTask) {
        fileUpload.upload(uploadTask);
    }

    public void addUploadTask(UploadTask uploadTask) {
        uploadTasksLooper.enqueueTask(uploadTask);
    }
}
