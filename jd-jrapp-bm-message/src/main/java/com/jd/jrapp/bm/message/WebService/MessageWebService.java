package com.jd.jrapp.bm.message.WebService;


import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/**
 * ==================================================
 * All Right Reserved
 * Date:2018/01/22
 * Author:Sire
 * Description:
 * ==================================================
 */
public interface MessagePushWebService {

    @POST()
    LiveData<Response<JsonResponse<User>>> uploadFile(@Url String fileUrl , @Body RequestBody Body);
}
