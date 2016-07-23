package com.bupt.edison.qqreddotdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.bupt.edison.qqreddot.QQRedDotView;
import com.bupt.edison.qqreddot.Utils;

public class MainActivity extends AppCompatActivity {

    QQRedDotView qqRedDotView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        setContentView(R.layout.activity_main);
        setQQRedDotView();
    }

    private void setQQRedDotView(){
        qqRedDotView = (QQRedDotView)findViewById(R.id.qqdot);
        qqRedDotView.setOnDragStartListener(new QQRedDotView.OnDragStartListener() {
            @Override
            public void OnDragStart() {
                Toast.makeText(MainActivity.this,"开始拖拽",Toast.LENGTH_SHORT).show();
            }
        });
        qqRedDotView.setOnDotResetListener(new QQRedDotView.OnDotResetListener() {
            @Override
            public void OnDotReset() {
                Toast.makeText(MainActivity.this,"红点复位",Toast.LENGTH_SHORT).show();
            }
        });
        qqRedDotView.setOnDotDismissListener(new QQRedDotView.OnDotDismissListener() {
            @Override
            public void OnDotDismiss() {
                Toast.makeText(MainActivity.this,"红点消失",Toast.LENGTH_SHORT).show();
            }
        });
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
