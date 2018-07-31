package com.example.im.imdemo;

import com.jd.jrapp.bm.message.db.IMMessage;
import com.jd.jrapp.bm.message.utils.TimeOffSetUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.util.Arrays;

public class test {
    public static void main(String[]args){
//        UploadInfor uploadInfor = JSONUtils.jsonString2Bean("{\"fileName\":\"test.png\",\"filePath\":\"/storage/emulated/0/Download/test.png\",\"fileSize\":\"-1\",\"FILE_TYPE\":\"\",\"uploadState\":{\"contentLength\":0,\"fileName\":null,\"fromClientId\":\"ANDROID;version=1.0;uuid=imei000000000000000\",\"lengthPerRead\":0,\"progress\":0,\"readStartPonint\":0,\"savePath\":\"/storage/emulated/0/Download/test.png\",\"state\":1,\"tempBrokenPosition\":-1,\"toClientId\":\"ANDROID;version=1.0;uuid=id2dc912db033142beae65916699e00043\",\"uploadId\":\"d3a96e6c47b5447595120b7f7702ca45\",\"uploaded\":false}}", UploadInfor.class);
//        System.out.println("======="+uploadInfor);
//        String filePath = "/storage/emulated/0/Download/test.png";
//        File file = new File(filePath);
//        System.out.println("====="+file.getName());
//        IMMessage imMessage = new IMMessage();
//        imMessage.setContentType("2");
//        System.out.println("+++++"+ TimeOffSetUtils.tommorrow().getTime());
        String[] strings = {"1","2","3","4","5"};
        int[] ints = {1,2,3,4,5,6,7};
//        TestData[] testDatas = {new TestData(),new TestData(),new TestData()};
//        String s = Arrays.deepToString((Object[]) ints);
        System.out.println("======"+ints.getClass().getCanonicalName());

    }
}
