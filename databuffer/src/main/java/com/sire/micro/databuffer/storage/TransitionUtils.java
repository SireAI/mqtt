package com.sire.micro.databuffer.storage;

import android.database.sqlite.SQLiteDatabase;

 class TransitionUtils {
    public static void transition(SQLiteDatabase database, Runnable tanstionTask) {
        if (database == null || tanstionTask == null) {
            return;
        }
        //开启事务
        database.beginTransaction();
        try {
            tanstionTask.run();
            //设置事务标志为成功，当结束事务时就会提交事务
            database.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //结束事务
            database.endTransaction();
        }
    }
}
