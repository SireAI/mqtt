package com.jd.jrapp.bm.message.configure;

import com.jd.jrapp.bm.message.WebService.FileRequestBody;
import com.jd.jrapp.bm.message.WebService.MessageWebService;
import com.jd.jrapp.bm.message.WebService.RetrofitCallback;
import com.jd.jrapp.bm.message.bean.UploadResult;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class FileUploadImpl implements IFileUpload{

    private final MessageWebService messageWebService;

    public FileUploadImpl() {
        messageWebService = new Retrofit.Builder()
                .baseUrl("http://msinner.jr.jd.com/gw/generic/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build()
                .create(MessageWebService.class);
    }

    @Override
    public void upload(UploadTask uploadTask) {
        UploadInfor uploadInfor = uploadTask.getUploadInfor();
        final UploadState uploadState = uploadTask.getUploadState();
        //构建body
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("aucode", "27994d8b0b7cf04ca009cff0d3215fb2")
                .addFormDataPart("clientType", "android")
                .addFormDataPart("clientVersion", "hbz19870606")
                .addFormDataPart("pin", "hbz19870606")
                .addFormDataPart("version", "200")
                .addFormDataPart("a2", "AAFbL6bSAEBfymlAk4xtpgECAVH4yRdORU6hIVe6uf-peXHsvmga6EscyKBLLsaS_ElEnLbHy8hBH21QqeIR0uNr1ZV1EO_X")
                .addFormDataPart("osVersion", "11.3.1")
                .addFormDataPart("file", uploadInfor.getFileName(), RequestBody.create(MediaType.parse("multipart/form-data"), new File(uploadInfor.getFilePath())))
                .build();

        FileRequestBody fileRequestBody = new FileRequestBody(requestBody, new RetrofitCallback< String >() {
            @Override
            public void onSuccess(Call< String > call, Response< String > response) {
//                runOnUIThread(activity, response.body().toString());
                //进度更新结束
            }
            @Override
            public void onFailure(Call< String > call, Throwable t) {
//                runOnUIThread(activity, t.getMessage());
                //进度更新结束
            }
            @Override
            public void onLoading(long total, long progress) {
                System.out.println("progress:"+progress);
                uploadState.setProgress(progress);
                //此处进行进度更新
            }
        });

        Call<UploadResult> responseBodyCall = messageWebService.uploadFile(fileRequestBody);
        try {
            Response<UploadResult> response = responseBodyCall.execute();
            UploadResult body = response.body();
            System.out.println("上传结果："+body);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
