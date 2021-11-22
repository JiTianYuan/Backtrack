package com.jty.backtrack_demo;

import android.app.Application;
import android.os.Environment;

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
        Config config = new Config.Builder()
                .debuggable(true)
                //.outputDir(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Backtrace")
                .outputDir(getFilesDir().getAbsolutePath() + File.separator + "Backtrace")
                .build();
        Backtrack.init(this, config);
    }
}
