# Android-QQRedDot
Android平台，一站式小红点解决方案，先看效果图：

![](https://github.com/kanglongba/QQRedDot/blob/master/screenshot/QQRedDotView.gif)

PS，原本只是想写一个仿新版QQ可拖拽小红点的控件，然后一路写下来，发现可以扩展成一站式小红点解决方案，于是就有了这个控件。



# Introduction

控件可配置的属性有：

* 红点的模式
    * 实心红点：不可拖动，不显示数字，最基本的红点形式，类似于一张小红点图片
    * 普通红点：显示数字，且大小可以随数字的大小而变化，但是不可拖动
    * QQ红点：显示数字，大小可随数字的大小而变化，可拖动，拖动过程中，会有橡皮筋效果，且可对拖拽过程设置监听
* 红点的半径
* 锚点（拖拽时固定在屏幕上的点）的半径
    * 锚点的半径不能大于红点的半径
* 红点的颜色
    * 红点只是一个名字，它可以是任意颜色
* 数字的颜色
* 数字的大小
* 数字的显示方式
    * 准确显示：如实显示数字
    * 模糊显示：当超过设置的阈值时，只显示一个范围，例如：99+，199+
* 阈值
    * 阈值不能小于99，否则会被强制设为99.而且，只有在模糊模式下，才有效果
* 红点可拖动的距离
    * 在这个范围内，会有一条皮筋连接红点与锚点
    * 超过这个范围后，皮筋会断裂


# Usage

* QQ红点
    1. 在XML文件中设置属性
    ```
<com.bupt.edison.qqreddot.QQRedDotView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:id="@+id/qqdot_6"
    app:dotStyle="qqDot"
    app:countStyle="accurate"
    app:dotColor="@android:color/holo_red_light"
    app:textColor="@android:color/white"
    app:dragDotRadius="20dp"
    app:anchorDotRadius="15dp"
    app:textSize="20sp"
    app:dragDistance="200dp" />
    ```
    2. 在代码中更新数字和设置监听器
    ```
    //更新数字
    qqdot6.setUnreadCount(666);
    //开始拖动的监听
    qqdot6.setOnDragStartListener(new QQRedDotView.OnDragStartListener() {
        @Override
        public void OnDragStart() {
            Toast.makeText(MainActivity.this, "开始拖拽", Toast.LENGTH_SHORT).show();
        }
    });
    //复位的监听
    qqdot6.setOnDotResetListener(new QQRedDotView.OnDotResetListener() {
        @Override
        public void OnDotReset() {
            Toast.makeText(MainActivity.this, "红点复位", Toast.LENGTH_SHORT).show();
        }
    });
    //消失的监听
    qqdot6.setOnDotDismissListener(new QQRedDotView.OnDotDismissListener() {
        @Override
        public void OnDotDismiss() {
            Toast.makeText(MainActivity.this, "红点消失", Toast.LENGTH_SHORT).show();
        }
    });
    ```
    3. 效果图
![](https://github.com/kanglongba/QQRedDot/blob/master/screenshot/QQRedDotView_qqdot.gif)

* 普通红点
    1. 在xml中设置属性
    ```
<com.bupt.edison.qqreddot.QQRedDotView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginLeft="30dp"
    android:id="@+id/qqdot_5"
    app:dotStyle="common"
    app:dragDotRadius="15dp"
    app:textSize="13dp"
    app:dotColor="@android:color/holo_red_light"
    app:textColor="@android:color/white"
    app:countStyle="blurred"
    app:msgThresholdCount="799"/>
    ```
    2. 在代码中更新数字
    ```
    qqdot5.setUnreadCount(msgCount);
    ```
    3.效果图
    ![](https://github.com/kanglongba/QQRedDot/blob/master/screenshot/QQRedDotView_updateMsgCount.gif)
    
* 实心红点
    1. 在xml中设置属性
    ```
<com.bupt.edison.qqreddot.QQRedDotView
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_marginLeft="30dp"
    app:dotStyle="solid"
    app:dragDotRadius="20dp"
    app:dotColor="@android:color/holo_red_light"/>
    ```
    2. 不需要在代码中特别设置什么
    3. 效果图
    ![](https://github.com/kanglongba/QQRedDot/blob/master/screenshot/QQRedDotView_solid.png)

    
# Reference

* [QQ手机版 5.0“一键下班”设计小结](https://isux.tencent.com/qq-mobile-off-duty.html)
* [贝塞尔曲线扫盲](http://www.html-js.com/article/1628)
* [Path之贝塞尔曲线](https://github.com/GcsSloop/AndroidNote/blob/master/CustomView/Advance/%5B6%5DPath_Bezier.md)
* [类似QQ的小红点](https://github.com/mabeijianxi/stickyDots)



