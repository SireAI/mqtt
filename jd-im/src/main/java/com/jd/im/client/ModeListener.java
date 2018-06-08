package com.jd.im.client;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ModeListener {
    private final ICallBack callBack;
    private int count = 0;

    public ModeListener(Application app, ICallBack callBack) {
        this.callBack = callBack;
        app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                count++;
                notifyAppMode();
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                count--;
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
            int mode = count > 0 ? 1 : 2;
            callBack.notifyAppMode(mode);
        }
    }

    public interface ICallBack {
        void notifyAppMode(int mode);
    }
}
