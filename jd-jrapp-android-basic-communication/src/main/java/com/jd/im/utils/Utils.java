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
        ActivityManager  am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
        if(runningProcesses!=null){
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.processName.equals(context.getPackageName())) {
                    isInBackground = processInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
                    break ;
                }
            }
        }
        return isInBackground;
    }
}
