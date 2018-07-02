package com.jd.jrapp.bm.message.Manager;

import android.content.Context;

import com.jd.im.socket.BlockingLooper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

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
    private ConcurrentMap<String,UploadState> uploadStates = new ConcurrentHashMap<>();

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
        uploadStates.remove(uploadTask.getTaskId());
    }

    public synchronized void addUploadTask(UploadTask uploadTask) {
        String taskId = uploadTask.getTaskId();
        if(!uploadStates.containsKey(taskId)){
            uploadStates.put(taskId,uploadTask.getUploadState());
            uploadTasksLooper.enqueueTask(uploadTask);
        }
    }
    public boolean isAlreadyInTask(String taskId){
        return uploadStates.containsKey(taskId);
    }

    public UploadState getUploadState(String taskId){
      return   uploadStates.get(taskId);
    }


}
