package com.jty.backtrack_demo;

import android.app.Application;

import com.jty.backtrack.core.Backtrack;
import com.jty.backtrack.core.Config;

/**
 * @author jty
 * @date 2021/11/4
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Config config = new Config.Builder()
                .build();
        Backtrack.init(config);
    }
}
