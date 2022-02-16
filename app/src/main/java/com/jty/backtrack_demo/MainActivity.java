package com.jty.backtrack_demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.jty.backtrack.core.Backtrack;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    A();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        try {
            A();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //启动耗时检测结束
        Backtrack.getInstance().recordStartUpEnd();
    }

    private void A() throws InterruptedException {
        Thread.sleep(1000);
        B();
        C();
    }

    private void B() throws InterruptedException {
        Thread.sleep(100);
    }

    private void C() throws InterruptedException {
        Thread.sleep(300);
    }


}