package com.bupt.edison.qqreddotdemo;

import android.graphics.RectF;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    QQRedDotView qqRedDotView;
    RectF touchRectF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        qqRedDotView = (QQRedDotView)findViewById(R.id.qqdot);
    }

    @Override
    protected void onStart() {
        super.onStart();
        touchRectF = qqRedDotView.getDotRectF();
    }

    /*
    float downX,downY,moveX,moveY,upX,upY;
    boolean isdragable = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                if(touchRectF.contains(downX,downY)){
                    isdragable = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(isdragable){
                    moveX = event.getX();
                    moveY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if(isdragable){
                    isdragable = false;
                    upX = event.getX();
                    upY = event.getY();
                    //更新范围矩阵
                }
                break;
            default:
                break;
        }
        return true;
    }
    */

    //利用属性动画,做出拖动红点的效果
}
