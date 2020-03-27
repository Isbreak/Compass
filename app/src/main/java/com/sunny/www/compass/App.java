package com.sunny.www.compass;

import android.app.Application;
import android.os.Process;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Created by 67045 on 2018/3/8.
 * 软件的Application
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        initComponent();
    }

    private void initComponent() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                // Bugly
                CrashReport.initCrashReport(getApplicationContext(), "8ebdc24182", false);
            }
        }).start();
    }
}
