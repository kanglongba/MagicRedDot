package com.bupt.edison.qqreddotdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bupt.edison.qqreddot.QQRedDotView;

public class MainActivity extends AppCompatActivity {

    QQRedDotView qqRedDotView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qqRedDotView = (QQRedDotView)findViewById(R.id.qqdot);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
