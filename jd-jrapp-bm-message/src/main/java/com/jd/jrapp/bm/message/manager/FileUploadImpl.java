package com.jd.jrapp.bm.message.manager;

import android.util.Log;

import com.jd.jrapp.bm.message.WebService.FileRequestBody;
import com.jd.jrapp.bm.message.WebService.MessageWebService;
import com.jd.jrapp.bm.message.WebService.UploadProgressListener;
import com.jd.jrapp.bm.message.adapter.TasksExecutor;
import com.jd.jrapp.bm.message.bean.UploadResult;
import com.jd.jrapp.bm.message.constant.Constant;
import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.model.MessageModel;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.jd.jrapp.bm.message.constant.Constant.IMAGE_UPLOAD_SUCCESS;
import static com.jd.jrapp.bm.message.constant.Constant.T_FAILED;

public class FileUploadImpl implements IFileUpload {

    private final MessageWebService messageWebService;
    private static final String TAG = "FileUploadImpl";
    public FileUploadImpl() {
        messageWebService = new Retrofit.Builder()
                .baseUrl("http://msinner.jr.jd.com/gw/generic/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(MessageWebService.class);
    }

    @Override
    public void upload(UploadTask uploadTask) {
        IMMessage uploadMsgInfor = uploadTask.getUploadMsgInfor();
        UploadInfor uploadInfor = uploadTask.getUploadInfor();
        final UploadState uploadState = uploadTask.getUploadState();
        if(! MessageModel.getInstance().checkNetOK(uploadMsgInfor)){
            //网络未连接时
            uploadMsgInfor.setTransportState(T_FAILED);
            MessageModel.getInstance().updateImageTransportState(UploadState.UPLOAD_FAILED,uploadInfor,uploadMsgInfor);
            return;
        }

        //构建body
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("aucode", "27994d8b0b7cf04ca009cff0d3215fb2")
                .addFormDataPart("clientType", "android")
                .addFormDataPart("clientVersion", "hbz19870606")
                .addFormDataPart("pin", "释小夜")
                .addFormDataPart("version", "200")
                .addFormDataPart("a2", "AAFbOY3nAEA2s9TN0XbNcEzHBI4usSUDpg98o5k0xHOP51lEgGNFu4eIPQEmVaTwQt1fHM7vNi0SwSfmB42iFVbdxezSreLb")
                .addFormDataPart("osVersion", "11.3.1")
                .addFormDataPart("file", uploadInfor.getFileName(), RequestBody.create(MediaType.parse("multipart/form-data"), new File(uploadInfor.getFilePath())))
                .build();
        FileRequestBody fileRequestBody = new FileRequestBody(requestBody, new UploadProgressListener<UploadResult>() {
            @Override
            public void onSuccess(Call<UploadResult> call, Response<UploadResult> response) {
//                runOnUIThread(activity, response.body().toString());
                //进度更新结束
                System.out.println("onSuccess=====");
            }

            @Override
            public void onFailure(Call<UploadResult> call, Throwable t) {
//                runOnUIThread(activity, t.getMessage());
                //进度更新结束
                System.out.println("onFailure=====");

            }

            @Override
            public void onLoading(long total, long progress) {
                final double persentProgress = new Double(progress) / total * 100;
                // TODO: 2018/6/28 间隔优化
                TasksExecutor.getInstance().postToMainThread(new Runnable() {
                    @Override
                    public void run() {
                        uploadState.setProgress((int) persentProgress);
                    }
                });
                //此处进行进度更新
            }
        });

        Call<UploadResult> responseBodyCall = messageWebService.uploadFile(fileRequestBody);
        try {
            Response<UploadResult> response = responseBodyCall.execute();
            UploadResult uploadResult = response.body();
            if(response.isSuccessful() && uploadResult !=null && IMAGE_UPLOAD_SUCCESS.equals(uploadResult.getResultCode()) ){
                //图片上传成功，保存状态
                String uploadedUrl = uploadResult.getResultData().getImageDomain()+uploadResult.getResultData().getImageId();
                uploadInfor.setUploadedUrl(uploadedUrl);
                MessageModel.getInstance().updateImageTransportState(UploadState.UPLOADED,uploadInfor,uploadMsgInfor);
            }else {
                failedCase(uploadMsgInfor, uploadInfor, uploadResult);
            }
        } catch (IOException e) {
            e.printStackTrace();
            failedCase(uploadMsgInfor, uploadInfor, null);
        }
    }

    private void failedCase(IMMessage uploadMsgInfor, UploadInfor uploadInfor, UploadResult uploadResult) {
        Log.e(TAG,uploadResult==null?"图片上传失败":uploadResult.getResultMsg());
        uploadMsgInfor.setFailedInfor(Constant.FILE_SEND_FAILED);
        uploadMsgInfor.setTransportState(T_FAILED);
        MessageModel.getInstance().updateImageTransportState(UploadState.UPLOAD_FAILED,uploadInfor,uploadMsgInfor);
    }
}
