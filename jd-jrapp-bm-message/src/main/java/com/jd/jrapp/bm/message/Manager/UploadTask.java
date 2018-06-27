package com.jd.jrapp.bm.message.configure;

import static com.jd.jrapp.bm.message.configure.UploadState.UPLOADING;

public class UploadTask {
    private UploadState uploadState;
    private UploadInfor uploadInfor;

    public UploadTask() {
        uploadState = new UploadState(0,UPLOADING);
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {
        this.uploadState = uploadState;
    }

    public UploadInfor getUploadInfor() {
        return uploadInfor;
    }

    public void setUploadInfor(UploadInfor uploadInfor) {
        this.uploadInfor = uploadInfor;
    }
}
