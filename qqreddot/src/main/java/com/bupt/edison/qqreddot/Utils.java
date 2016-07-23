package com.bupt.edison.qqreddot;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;

/**
 * Created by edison on 16/7/3.
 */
public class Utils {
    /**
     * dip转化为px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dp2px(Context context, int dipValue){
        if (context == null) {
            return dipValue;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, context.getResources().getDisplayMetrics());

    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        if (context == null) {
            return (int)spValue;
        }
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 获取标题栏高度
     * @param window
     * @return
     */
    public static int getTitleBarHeight(Window window){
        int contentTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        return contentTop - getStatusBarHeight(window);
    }

    /**
     * 获取状态栏的高度
     * @param view
     * @return
     */
    public static int getStatusBarHeight(View view) {
        Rect rect = new Rect();
        view.getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    /**
     * 获取状态栏的高度
     * @param window
     * @return
     */
    public static int getStatusBarHeight(Window window){
        Rect frame = new Rect();
        window.getDecorView().getWindowVisibleDisplayFrame(frame);
        return frame.top;
    }

    /**
     * 计算字符串的绘制宽度
     *
     * @param paint
     * @param str
     * @return
     */
    public static int computeStringWidth(Paint paint, String str) {
        int iRet = 0;
        if (!TextUtils.isEmpty(str)) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) Math.ceil(widths[j]);
            }
        }
//        Log.d("edison","text width: "+iRet);
        return iRet;
    }

    /**
     * 计算字符串的绘制高度
     *
     * @param paint
     * @param string
     * @return 只有这种方法可以计算字符串的高度
     */
    public static int computeStringHeight(Paint paint, String string) {
        Rect rect = new Rect();

        //返回包围整个字符串的最小的一个Rect区域
        paint.getTextBounds(string, 0, 1, rect);
//        Log.d("edison","text height: "+rect.height());
        return rect.height();
    }
}
