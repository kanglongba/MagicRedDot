package com.bupt.edison.qqreddot;

import android.graphics.PointF;

/**
 * Created by edison on 16/7/3.
 */
public class MathUtils {

    /**
     * 返回两点之间的距离
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static float getDistanceBetweenPoints(float x1,float y1,float x2,float y2){
        return (float)Math.sqrt(Math.pow(x2-x1,2)+Math.pow(y2-y1,2));
    }

    /**
     * 返回两点的中点
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     * 把中点当成贝塞尔曲线的控制点
     */
    public static PointF getMiddlePoint(float x1,float y1,float x2,float y2){
        PointF pointF = new PointF((x1+x2)/2.0f,(y1+y2)/2.0f);
        return pointF;
    }

    /**
     * 获取两点之间的斜率
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @return
     */
    public static double getGradient(float x1,float y1,float x2,float y2){
        float dy = Math.abs(y2-y1);
        float dx = Math.abs(x2-x1);
        double arcT = Math.atan(dy/dx);
        return arcT;
    }

    /**
     * 计算拖拽时,贝塞尔曲线的四个数据点
     * http://isux.tencent.com/wp-content/uploads/2014/10/201410171340142.png
     * center2是拖拽点,center1是固定点.一般radius2要大于radius1.
     *
     * @param centerX1
     * @param centerY1
     * @param radius1
     * @param centerX2
     * @param centerY2
     * @param radius2
     * @return
     */
    public static PointF[] getTangentPoint(float centerX1,float centerY1,float radius1,float centerX2,float centerY2,float radius2){
        PointF[] pointFs = new PointF[4];
        PointF p1 = new PointF();
        PointF p2 = new PointF();
        PointF p3 = new PointF();
        PointF p4 = new PointF();

        double arcGradient = getGradient(centerX1,centerY1,centerX2,centerY2); //因为象限的不同,斜率是会变的.所以要分象限计算
        double arcP2C2C1 = Math.acos((radius2-radius1)/getDistanceBetweenPoints(centerX1,centerY1,centerX2,centerY2));

        double arcP2C2X;
        if(centerX1>=centerX2&&centerY1>=centerY2){//三象限
            arcP2C2X = Math.PI - arcP2C2C1 - arcGradient;
            p2.set((float)(centerX2-radius2*Math.cos(arcP2C2X)),(float)(centerY2+radius2*Math.sin(arcP2C2X)));
        }else if(centerX1<=centerX2&&centerY1<=centerY2){//一象限
            arcP2C2X = Math.PI - arcP2C2C1 - arcGradient;
            p2.set((float)(centerX2+radius2*Math.cos(arcP2C2X)),(float)(centerY2-radius2*Math.sin(arcP2C2X)));
        }else if(centerX1<centerX2&&centerY1>centerY2){ //四象限
            arcP2C2X = arcP2C2C1-arcGradient;
            p2.set((float)(centerX2-radius2*Math.cos(arcP2C2X)),(float)(centerY2-radius2*Math.sin(arcP2C2X)));
        }else{ //二象限
            arcP2C2X = arcP2C2C1-arcGradient;
            p2.set((float)(centerX2+radius2*Math.cos(arcP2C2X)),(float)(centerY2+radius2*Math.sin(arcP2C2X)));
        }

        double arcP1C1C2 = Math.PI - arcP2C2C1;
        double arcP1C1X;
        if(centerX1>=centerX2&&centerY1>=centerY2){//三象限
            arcP1C1X = arcP1C1C2 - arcGradient;
            p1.set((float)(centerX1-radius1*Math.cos(arcP1C1X)),(float)(centerY1+radius1*Math.sin(arcP1C1X)));
        }else if(centerX1<=centerX2&&centerY1<=centerY2){ //一象限
            arcP1C1X = arcP1C1C2 - arcGradient;
            p1.set((float)(centerX1+radius1*Math.cos(arcP1C1X)),(float)(centerY1-radius1*Math.sin(arcP1C1X)));
        }else if(centerX1<centerX2&&centerY1>centerY2){ //四象限
            arcP1C1X = arcP2C2C1-arcGradient;
            p1.set((float)(centerX1-radius1*Math.cos(arcP1C1X)),(float)(centerY1-radius1*Math.sin(arcP1C1X)));
        }else{ //二象限
            arcP1C1X = arcP2C2C1-arcGradient;
            p1.set((float)(centerX1+radius1*Math.cos(arcP1C1X)),(float)(centerY1+radius1*Math.sin(arcP1C1X)));
        }

        //p3,p4与p1,p2是对称的
        double arcC1C2Y = Math.PI/2-arcGradient;
        double arcP4C2Y;
        if(centerX1>=centerX2&&centerY1>=centerY2){ //三象限
            arcP4C2Y = Math.PI/2 - arcP2C2C1 + arcGradient;
            p4.set((float)(centerX2+radius2*Math.sin(arcP4C2Y)),(float)(centerY2-radius2*Math.cos(arcP4C2Y)));
        }else if(centerX1<=centerX2&&centerY1<=centerY2){ //一象限
            arcP4C2Y = Math.PI/2 - arcP2C2C1 + arcGradient;
            p4.set((float)(centerX2-radius2*Math.sin(arcP4C2Y)),(float)(centerY2+radius2*Math.cos(arcP4C2Y)));
        }else if(centerX1<centerX2&&centerY1>centerY2){ //四象限
            arcP4C2Y = arcP2C2C1 - arcC1C2Y;
            p4.set((float)(centerX2+radius2*Math.sin(arcP4C2Y)),(float)(centerY2+radius2*Math.cos(arcP4C2Y)));
        }else{//二象限
            arcP4C2Y = arcP2C2C1 - arcC1C2Y;
            p4.set((float)(centerX2-radius2*Math.sin(arcP4C2Y)),(float)(centerY2-radius2*Math.cos(arcP4C2Y)));
        }

        double arcP3C1X;
        if(centerX1>=centerX2&&centerY1>=centerY2){//三象限
            arcP3C1X = Math.PI - arcP1C1C2 - arcGradient;
            p3.set((float)(centerX1+radius1*Math.cos(arcP3C1X)),(float)(centerY1-radius1*Math.sin(arcP3C1X)));
        }else if(centerX1<=centerX2&&centerY1<=centerY2){//一象限
            arcP3C1X = Math.PI - arcP1C1C2 - arcGradient;
            p3.set((float)(centerX1-radius1*Math.cos(arcP3C1X)),(float)(centerY1+radius1*Math.sin(arcP3C1X)));
        }else if(centerX1<centerX2&&centerY1>centerY2){ //四象限{
            arcP3C1X = arcP1C1C2 - arcGradient;
            p3.set((float)(centerX1+radius1*Math.cos(arcP3C1X)),(float)(centerY1+radius1*Math.sin(arcP3C1X)));
        }else{//二象限
            arcP3C1X = arcP1C1C2 - arcGradient;
            p3.set((float)(centerX1-radius1*Math.cos(arcP3C1X)),(float)(centerY1-radius1*Math.sin(arcP3C1X)));
        }

//        Log.d("edison","斜率: "+arcGradient);
//        Log.d("edison","p1 arc: "+arcP1C1X);
//        Log.d("edison","p2 arc: "+arcP2C2X);
//        Log.d("edison","p3 arc: "+arcP3C1X);
//        Log.d("edison","p4 arc: "+arcP4C2Y);

        pointFs[0] = p1;
        pointFs[1] = p2;
        pointFs[2] = p4;
        pointFs[3] = p3;

        return pointFs;
    }
}
