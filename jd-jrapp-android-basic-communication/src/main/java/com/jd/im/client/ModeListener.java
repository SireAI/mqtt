package com.jd.im.client;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RestrictTo;

import com.jd.im.utils.Utils;
/**
 * =====================================================
 * All Right Reserved
 * Date:2018/6/21
 * Author:wangkai
 * Description:
 * =====================================================
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class ModeListener {
    private final ICallBack callBack;
    private final Handler handler;
    private final CheckMode checkMode;

    public ModeListener(Application app, ICallBack callBack) {
        handler = new Handler();
        checkMode = new CheckMode(app);
        this.callBack = callBack;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                notifyAppMode();
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                notifyAppMode();
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });

    }

    public void notifyAppMode() {
        if (callBack != null) {
            handler.removeCallbacks(checkMode);
            handler.postDelayed(checkMode,2000);
        }
    }


    public interface ICallBack {
        void notifyAppMode(int mode);
    }
    private class CheckMode implements Runnable{
        private final Context context;

        public CheckMode(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            if(callBack!=null){
                boolean appIsInBackground = Utils.isAppIsInBackground(context);
                int mode = appIsInBackground ? 2 : 1;
                callBack.notifyAppMode(mode);
            }
        }
    }
}
