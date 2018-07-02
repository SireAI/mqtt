package com.jd.jrapp.bm.message.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.jd.jrapp.bm.message.Manager.UploadState;

import java.util.Date;
import java.util.UUID;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/14
 * Author:wangkai
 * Description:数据库初始化
 * =====================================================
 */
@Database(entities = {IMMessage.class,Message.class
}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class MessageDb extends RoomDatabase {
    abstract public MessageDao MessageDao();

    private static MessageDb instance ;
    public static synchronized MessageDb getInstance( Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), MessageDb.class, "messageDb.db").addCallback(new Callback() {
                @Override
                public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                    fillDb();
                }
            }).build();
        }
        return instance;
    }

    private static void fillDb() {
                new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 300; i++) {
                    IMMessage imMessage = new IMMessage();
                    imMessage.setMessageId(UUID.randomUUID().toString());
                    imMessage.setMessageCreateTime(new Date());
                    int j = i % 2;
                    if(j==0){
                        imMessage.setFromAuthorName("sire");
                        imMessage.setFromAuthorId("1");
                        imMessage.setFromAuthorImg("http://www.lagou.com/image2/M00/01/5C/CgqLKVXj66GAaM2cAAFq682mH60142.png");
                        imMessage.setFromDeviceId("11");
                        imMessage.setContentType("text/plain");
                        imMessage.setContent(new String("我是sire"+i).getBytes());
                        imMessage.setShowTime(true);
                        imMessage.setToAuthorId("2");
                        imMessage.setToAuthorName("jhona");
                        imMessage.setToAuthorImg("http://screen.uimg.cn/cc/9b/cc9b7ec9af34a15bd6f94190e8519fc4.jpg");
                    }else {
                        imMessage.setToAuthorName("sire");
                        imMessage.setToAuthorId("1");
                        imMessage.setToAuthorImg("http://www.lagou.com/image2/M00/01/5C/CgqLKVXj66GAaM2cAAFq682mH60142.png");
                        imMessage.setFromDeviceId("11");
                        imMessage.setContent(new String("我是jhona"+i).getBytes());
                        imMessage.setShowTime(true);
                        imMessage.setContentType("text/plain");
                        imMessage.setFromAuthorId("2");
                        imMessage.setFromAuthorName("jhona");
                        imMessage.setFromAuthorImg("http://screen.uimg.cn/cc/9b/cc9b7ec9af34a15bd6f94190e8519fc4.jpg");
                    }
                    instance.MessageDao().saveIMMessage(imMessage);
                }
            }
        }).start();
    }
}