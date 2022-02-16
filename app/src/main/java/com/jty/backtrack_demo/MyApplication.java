package com.jty.backtrack_demo;

import android.app.Application;
import android.content.Context;

import com.jty.backtrack.core.Backtrack;
import com.jty.backtrack.core.Config;

import java.io.File;

/**
 * @author jty
 * @date 2021/11/4
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        Config config = new Config.Builder()
                .debuggable(true)
                .jankFrameThreshold(1)
                //.outputDir(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Backtrace")
                .outputDir(base.getFilesDir().getAbsolutePath() + File.separator + "Backtrace")
                .recordStartUp(true)
                .build();
        Backtrack.init(base, config);
        super.attachBaseContext(base);
    }
}
