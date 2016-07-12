package com.bupt.edison.qqreddotdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.bupt.edison.qqreddot.QQRedDotView;
import com.bupt.edison.qqreddot.Utils;

public class MainActivity extends AppCompatActivity {

    QQRedDotView qqRedDotView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);
        qqRedDotView = (QQRedDotView)findViewById(R.id.qqdot);
        qqRedDotView.setOnComputeTitleBarHeightListner(new QQRedDotView.OnComputeTitleBarHeightListner() {
            @Override
            public int onComputeTitleBarHeight() {
                qqRedDotView.setWindowManager(getWindowManager());
                Log.d("edison","titlebar height "+Utils.getTitleBarHeight(getWindow()));
                return Utils.getTitleBarHeight(getWindow());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
