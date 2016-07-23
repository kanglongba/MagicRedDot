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

    }

    @Override
    protected void onResume() {
        super.onResume();
        //这里还有个坑,状态栏和标题栏的高度计算结果都是0
        qqRedDotView.setStatusBarHeight(Utils.getStatusBarHeight(getWindow()));
        Log.d("edison titlebar",Utils.getTitleBarHeight(getWindow())+"");
        Log.d("edison statusbar",Utils.getStatusBarHeight(getWindow())+"");
        Log.d("edison statusbar",Utils.getStatusBarHeight(qqRedDotView)+"");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
