package com.jd.jrapp.bm.message.manager;

public class UploadInfor {
    private String filePath;
    private String fileName;
    private String fileSize;
    private String fileType;
    private String uploadMessageId;
    private String fromClientId;
    private String toClientId;
    private String uploadedUrl;


    private UploadState uploadState;

    public UploadInfor(String filePath, String fileName, String fileSize, String fileType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileType = fileType;
    }

    public UploadInfor() {
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {

        this.fileType = fileType;
    }

    public UploadState getUploadState() {
        return uploadState;
    }

    public void setUploadState(UploadState uploadState) {

        this.uploadState = uploadState;
    }

    public String getUploadMessageId() {
        return uploadMessageId;
    }

    public void setUploadMessageId(String uploadMessageId) {
        this.uploadMessageId = uploadMessageId;
    }

    public String getFromClientId() {
        return fromClientId;
    }

    public void setFromClientId(String fromClientId) {
        this.fromClientId = fromClientId;
    }

    public String getToClientId() {
        return toClientId;
    }

    public void setToClientId(String toClientId) {
        this.toClientId = toClientId;
    }

    public String getUploadedUrl() {
        return uploadedUrl;
    }

    public void setUploadedUrl(String uploadedUrl) {
        this.uploadedUrl = uploadedUrl;
    }
}
