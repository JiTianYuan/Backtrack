package com.jty.backtrack_demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.jty.backtrack.core.Backtrack;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            A();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try{
            int a = 0;
            for (int i = 0; i < 10; i++) {
                a++;
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void A() throws InterruptedException {
        Thread.sleep(1000);
        B();
    }

    private void B(){
        int a = 0;
        for (int i = 0; i < 1000; i++) {
            a++;
        }
        Activity activity = null;
        activity.onContentChanged();
    }


}