package com.jd.jrapp.bm.message.WebService;


import com.jd.jrapp.bm.message.bean.UploadResult;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/22
 * Author:Sire
 * Description:
 * ==================================================
 */
public interface MessageWebService {
    /**
     * 上传文件
     * @param Body  数据体
     * @return
     */
    @POST("upload/na/")
    Call<UploadResult> uploadFile(@Body RequestBody Body);
}
