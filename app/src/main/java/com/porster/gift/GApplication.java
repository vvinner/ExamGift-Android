package com.porster.gift;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.porster.gift.core.DataManager;
import com.porster.gift.utils.LogCat;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by Porster on 17/2/28.
 */

public class GApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        boolean debug=BuildConfig.DEBUG;
        LogCat.setDebug(debug);
        MobclickAgent.setDebugMode(debug);
        DataManager.getInstance().init(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                MobclickAgent.onResume(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                MobclickAgent.onPause(activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}
