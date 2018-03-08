package com.sunny.www.compass;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by 67045 on 2018/3/8.
 * 软件的Application
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "8ebdc24182", false);
    }
}
