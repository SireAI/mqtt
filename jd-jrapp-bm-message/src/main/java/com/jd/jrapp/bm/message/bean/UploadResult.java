package com.jd.jrapp.bm.message.bean;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/27
 * Author:wangkai
 * Description:图片上传成功解析bean
 * =====================================================
 */
public class UploadResult {
    private String resultCode ;
    private String resultMsg;
    private ImageInfor resultData;


    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public ImageInfor getResultData() {
        return resultData;
    }

    public void setResultData(ImageInfor resultData) {

        this.resultData = resultData;
    }

    @Override
    public String toString() {
        return "UploadResult{" +
                "resultCode='" + resultCode + '\'' +
                ", resultMsg='" + resultMsg + '\'' +
                ", resultData=" + resultData +
                '}';
    }
}
