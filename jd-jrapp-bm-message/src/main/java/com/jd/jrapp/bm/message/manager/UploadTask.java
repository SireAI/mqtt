package com.jd.jrapp.bm.message.manager;

import com.jd.jrapp.bm.message.db.IMMessage;

public class UploadTask {
    private final String taskId;
    private IMMessage uploadMsgInfor;
    private UploadInfor uploadInfor;

    public UploadTask(String taskId) {
        this.taskId = taskId;
    }

    public UploadState getUploadState() {
        return uploadInfor.getUploadState();
    }

    public IMMessage getUploadMsgInfor() {
        return uploadMsgInfor;
    }

    public void setUploadMsgInfor(IMMessage uploadMsgInfor) {
        this.uploadMsgInfor = uploadMsgInfor;
    }

    public String getTaskId(){
        return taskId;
    }

    public UploadInfor getUploadInfor() {
        return uploadInfor;
    }

    public void setUploadInfor(UploadInfor uploadInfor) {
        this.uploadInfor = uploadInfor;
    }
}
