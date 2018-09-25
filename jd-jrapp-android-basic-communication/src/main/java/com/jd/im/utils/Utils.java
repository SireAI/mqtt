package com.jd.im.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RestrictTo;

import java.util.List;
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class Utils {
    /**
     * 判断应用是否在后台
     *
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            if(runningProcesses!=null){
                for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                    //前台程序
                    if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && processInfo.pkgList!=null) {
                        for (String activeProcess : processInfo.pkgList) {
                            if (activeProcess.equals(context.getPackageName())) {
                                isInBackground = false;
                            }
                        }
                    }
                }
            }

        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            if(taskInfo!=null){
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                if (componentInfo!= null && componentInfo.getPackageName().equals(context.getPackageName())) {
                    isInBackground = false;
                }
            }

        }
        return isInBackground;
    }
}
