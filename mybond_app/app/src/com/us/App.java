package com.us;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import java.util.HashSet;
import java.util.Set;


/**
 * 应用程序
 */

public class App extends Application {
    private static App INSTANCE;
    private Set<Activity> mActivities;
    private static Context mContext;

    public static synchronized App getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        INSTANCE = this;

    }

    public static Context getmContext(){
        return mContext;
    }

    public void addActivity(Activity activity) {
        if (mActivities == null) {
            mActivities = new HashSet<>();
        }
        mActivities.add(activity);
    }

    public void removeActivity(Activity activity) {
        if (mActivities != null) {
            mActivities.remove(activity);
        }
    }

    public void exitApp() {
        if (mActivities != null) {
            synchronized (mActivities) {
                for (Activity activity :
                        mActivities) {
                    activity.finish();
                }
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}
