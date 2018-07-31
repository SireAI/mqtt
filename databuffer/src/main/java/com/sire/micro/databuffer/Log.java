package com.sire.micro.databuffer;

import android.support.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Log {
    private static boolean loggable = false;

    public static void openLog() {
        loggable = true;
    }

    public static void  v(String tag, String msg) {
        if (!loggable) return;
         android.util.Log.v(tag, msg);
    }


    public static void v(String tag, String msg, Throwable tr) {
        if (!loggable) return;
         android.util.Log.v(tag, msg, tr);
    }


    public static void d(String tag, String msg) {
        if (!loggable) return;
         android.util.Log.d(tag, msg);
    }


    public static void d(String tag, String msg, Throwable tr) {
        if (!loggable) return;
         android.util.Log.d(tag, msg, tr);
    }


    public static void i(String tag, String msg) {
        if (!loggable) return ;
         android.util.Log.i(tag, msg);

    }


    public static void i(String tag, String msg, Throwable tr) {
        if (!loggable) return;
         android.util.Log.i(tag, msg, tr);
    }


    public static void w(String tag, String msg) {
        if(!loggable)return ;
         android.util.Log.w(tag, msg);
    }


    public static void w(String tag, String msg, Throwable tr) {
        if(!loggable)return ;
         android.util.Log.w(tag, msg, tr);
    }


    public static void w(String tag, Throwable tr) {
        if(!loggable)return ;
         android.util.Log.w(tag, tr);
    }


    public static void e(String tag, String msg) {
         android.util.Log.e(tag, msg);
    }


    public static void e(String tag, String msg, Throwable tr) {
         android.util.Log.e(tag, msg, tr);
    }
}
