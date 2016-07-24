package com.bupt.edison.qqreddot;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

/**
 * Created by edison on 16/6/30.
 * 一招退朝
 */
public class QQRedDotView extends View {
    Context context;
    WindowManager windowManager;
    int statusBarHeight;//状态栏高度,在Window中无法测量,需要从Activity中传入

    QQRedDotView qqRedDotViewInActivity; //Activity中的红点,window中的红点维持一个Activity中红点的引用
    QQRedDotView qqRedDotViewInWindow; //Window中的红点,Acitvity中的红点需要维持一个Window中红点的引用

    Paint dragDotPaint; //红点画笔
    Paint rubberPaint;//皮筋画笔
    Paint anchorDotPaint;//錨点画笔
    Paint messageCountPaint;//未读消息数的画笔

    public static final float bezierCircleConstant = 0.552284749831f; //用于贝塞尔曲线画圆时的常量值
    float mDistance; //贝塞尔曲线画圆时的控制线长度, = bezierCircleConstant*radius

    //用六个数据点,八个控制点画红点.n = 4;
    PointF upPointFLeft, upPointFRight, downPointFLeft, downPointRight, leftPointF, rightPointF; //数据点
    //八个控制点
    PointF upLeftPointF, upRightPointF, downLeftPointF, downRightPointF, leftUpPointF, leftDownPointF, rightUpPointF, rightDownPointF; //控制点
    Path redDotPath;//红点的贝塞尔曲线path
    Path rubberPath;//皮筋的贝塞尔曲线path
    PointF anchorPoint;//锚点的圆心点.固定不动的点,记录红点在Activty中初始化时的位置.

    boolean isInitFromLayout = true; //是否从布局文件中初始化,true表示从布局文件中初始化(Activity中的点).false表示从代码中初始化(Window中的点)
    int unreadCount = 0; //未读的消息数
    RectF dragDotRectF;//红点的范围矩阵,用于判断当前的touch事件是否击中了红点.

    //从xml中读取的红点的属性
    float dragDistance;//红点的可拖拽距离,超过这个距离,红点会消失;小于这个距离,红点会复位
    float dragDotRadius;//红点的半径
    float anchorDotRadius;//锚点的半径.在拖动过程中,它的数值会不断减小.
    int dotColor;//红点,皮筋和锚点的颜色
    int textColor;//未读消息的字体颜色
    int textSize;//未读消息的字体大小
    int dotStyle;//红点的style.0,实心点;1,可拖动;2,不可拖动
    int countStyle;//未读消息数的显示风格.0,准确显示;1,超过一定数值,就显示一个大概的数
    int msgThresholdValue;//消息数量的阈值,当超过这个数,并且显示方式设置为模糊,就开始模糊显示

    float dotRealWidth;//红点真实的宽度
    float dotRealHeight;//红点真实的高度
    PointF dragDotCenterPoint;//红点的中心点
    PointF dragDotLeftTopPoint;//红点的左上角的点
    float widgetCenterXInWindow, widgetCenterYInWindow;//控件的中心点在Window中的位置.
    float initAnchorRadius;//锚点初始的半径, 与未变化前的anchorDotRadius值相等


    /**
     * 用固定的属性值初始化控件
     *
     * @param context
     */
    public QQRedDotView(Context context) {
        super(context);
        initAttribute(context);
        initTools(context);
        isInitFromLayout = false;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public QQRedDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(context, attrs);
        initTools(context);
        isInitFromLayout = true;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public QQRedDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context, attrs);
        initTools(context);
        isInitFromLayout = true;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public QQRedDotView(Context context, WindowManager windowManager, int statusBarHeight,
                        float widgetCenterXInWindow, float widgetCenterYInWindow,
                        float dragDistance, float dragDotRadius, float anchorDotRadius,
                        int dotColor, int textColor, int textSize, int dotStyle, int unreadCount,
                        int countStyle, int msgThresholdValue) {
        super(context);
        this.context = context;
        this.windowManager = windowManager;
        this.statusBarHeight = statusBarHeight;
        this.widgetCenterXInWindow = widgetCenterXInWindow;
        this.widgetCenterYInWindow = widgetCenterYInWindow;
        this.dragDistance = dragDistance;
        this.dragDotRadius = dragDotRadius;
        this.anchorDotRadius = anchorDotRadius;
        this.dotColor = dotColor;
        this.textColor = textColor;
        this.textSize = textSize;
        this.dotStyle = dotStyle;
        this.countStyle = countStyle;
        this.unreadCount = unreadCount;
        this.msgThresholdValue = msgThresholdValue;
        initTools(context);
        isInitFromLayout = false;
    }

    //用固定的属性值初始化各个属性
    private void initAttribute(Context context) {
        dragDotRadius = Utils.dp2px(context, 20);
        anchorDotRadius = Utils.dp2px(context, 16);
        dotColor = Color.RED;
        textColor = Color.WHITE;
        textSize = Utils.sp2px(context, 24);
        dotStyle = 1;
        countStyle = 1;
        msgThresholdValue = 99;
        dragDistance = Utils.dp2px(context, 150);
    }

    //从layout文件中读取配置,初始化控件
    private void initAttribute(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QQRedDotView);
        //红点的半径
        dragDotRadius = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_dragDotRadius, 20);
        //锚点的半径
        anchorDotRadius = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_anchorDotRadius, 16);
        //红点的颜色
        dotColor = typedArray.getColor(R.styleable.QQRedDotView_dotColor, Color.RED);
        //消息字体的颜色
        textColor = typedArray.getColor(R.styleable.QQRedDotView_textColor, Color.WHITE);
        //消息字体的大小
        textSize = typedArray.getDimensionPixelSize(R.styleable.QQRedDotView_textSize, 24);
        //红点的风格.0,实心点;1,可拖动;2,不可拖动
        dotStyle = typedArray.getInt(R.styleable.QQRedDotView_dotStyle, 1);
        //红点可拖动的距离.也是消失距离和复位距离;
        dragDistance = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_dragDistance, 150);
        //未读消息数的显示风格
        countStyle = typedArray.getInt(R.styleable.QQRedDotView_countStyle, 1);
        //未读消息数的阈值
        msgThresholdValue = typedArray.getInt(R.styleable.QQRedDotView_msgThresholdCount, 99);
        typedArray.recycle();
    }

    private void initTools(Context context) {
        this.context = context;

        dragDotPaint = new Paint();
        dragDotPaint.setAntiAlias(true);
        dragDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dragDotPaint.setStrokeWidth(1);
        dragDotPaint.setColor(dotColor);

        if (dotStyle == 1 || dotStyle == 2) {
            messageCountPaint = new Paint();
            messageCountPaint.setColor(textColor);
            messageCountPaint.setAntiAlias(true);
            messageCountPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            messageCountPaint.setStrokeWidth(1);
            messageCountPaint.setTextSize(textSize);

            mDistance = bezierCircleConstant * dragDotRadius;

            //六个数据点
            upPointFLeft = new PointF();
            downPointFLeft = new PointF();
            upPointFRight = new PointF();
            downPointRight = new PointF();
            leftPointF = new PointF();
            rightPointF = new PointF();
            //八个控制点
            upLeftPointF = new PointF();
            upRightPointF = new PointF();
            downLeftPointF = new PointF();
            downRightPointF = new PointF();
            leftUpPointF = new PointF();
            leftDownPointF = new PointF();
            rightUpPointF = new PointF();
            rightDownPointF = new PointF();
            //绘制红点的贝塞尔曲线
            redDotPath = new Path();

            //红点的范围矩阵
            dragDotRectF = new RectF();
            //红点的中心点
            dragDotCenterPoint = new PointF();
            //红点的左上角的点
            dragDotLeftTopPoint = new PointF();
            //锚点的圆心点
            anchorPoint = new PointF(); //dotStyle==2时,不需要锚点.但是为了书写方便,还是把它放在这里.
        }

        if (dotStyle == 1) {
            rubberPaint = new Paint();
            rubberPaint.setStrokeWidth(1);
            rubberPaint.setColor(dotColor);
            rubberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            rubberPaint.setAntiAlias(true);

            anchorDotPaint = new Paint();
            anchorDotPaint.setColor(dotColor);
            anchorDotPaint.setStrokeWidth(1);
            anchorDotPaint.setAntiAlias(true);
            anchorDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            //绘制皮筋的贝塞尔曲线
            rubberPath = new Path();
            //初始的锚点半径
            initAnchorRadius = anchorDotRadius;
        }

        //未读消息的阈值不能小于99
        msgThresholdValue = msgThresholdValue < 99 ? 99 : msgThresholdValue;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float width, height;

        //红点的高度固定,但是宽度是根据未读消息数而变化的
        dotRealHeight = dragDotRadius * 2;
        if (dotStyle == 0) { //实心点,没有未读消息数
            dotRealWidth = dragDotRadius * 2;
        } else { //非实心点,带消息数
            //TODO 应该根据countStyle,msgThresholdValue和textSize动态计算红点的宽度
            //但是那样有点麻烦,留给下一个版本吧
            if (unreadCount >= 0 && unreadCount < 10) { //消息数为个位数
                dotRealWidth = dragDotRadius * 2;
            } else if (unreadCount >= 10 && unreadCount <= msgThresholdValue) { // 消息数为两位数
                dotRealWidth = 12 * dragDotRadius / 5;
            } else { //消息数为三位数及以上
                dotRealWidth = 3 * dragDotRadius;
            }
        }

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize < dotRealWidth ? dotRealWidth : widthSize; //必须保证可以完整容纳红点
        } else {
            width = dotRealWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize < dragDotRadius * 2 ? dragDotRadius * 2 : heightSize; //必须保证可以完整容纳红点
        } else {
            height = dragDotRadius * 2;
        }

        setMeasuredDimension((int) width + 2, (int) height + 2); //宽高各加两个像素,防止红点的边缘被切掉
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (0 != dotStyle) {
            //计算红点和锚点的中心点位置,计算红点的数据点和控制点
            computePosition();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (0 == dotStyle) { //实心红点,没有未读消息数
            canvas.drawCircle(getWidth() / 2.0f, getHeight() / 2.0f, dragDotRadius, dragDotPaint);
        } else if (2 == dotStyle) { //带消息数,但不可拖动
            if (unreadCount > 0) {
                drawDot(canvas); //只画红点和消息
            }
        } else { //显示未读消息数
            if (unreadCount > 0 && !isDismiss) {
                if (isdragable && isInPullScale && isNotExceedPullScale) {
                    drawRubber(canvas);
                    drawAnchorDot(canvas);
                }
                drawDot(canvas); //画红点
            }
        }
    }

    //绘制红点
    private void drawDot(Canvas canvas) {
        if (unreadCount > 0 && unreadCount <= 9) {
            canvas.drawCircle(dragDotCenterPoint.x, dragDotCenterPoint.y, dragDotRadius, dragDotPaint);
        } else if (unreadCount > 9) { //用贝塞尔取现画拉伸的红点
            redDotPath.reset();
            redDotPath.moveTo(upPointFLeft.x, upPointFLeft.y);
            redDotPath.lineTo(upPointFRight.x, upPointFRight.y);
            redDotPath.cubicTo(upRightPointF.x, upRightPointF.y, rightUpPointF.x, rightUpPointF.y, rightPointF.x, rightPointF.y);
            redDotPath.cubicTo(rightDownPointF.x, rightDownPointF.y, downRightPointF.x, downRightPointF.y, downPointRight.x, downPointRight.y);
            redDotPath.lineTo(downPointFLeft.x, downPointFLeft.y);
            redDotPath.cubicTo(downLeftPointF.x, downLeftPointF.y, leftDownPointF.x, leftDownPointF.y, leftPointF.x, leftPointF.y);
            redDotPath.cubicTo(leftUpPointF.x, leftUpPointF.y, upLeftPointF.x, upLeftPointF.y, upPointFLeft.x, upPointFLeft.y);
            canvas.drawPath(redDotPath, dragDotPaint);
        }

        drawMsgCount(canvas);
    }

    //绘制红点中的消息数量文字
    private void drawMsgCount(Canvas canvas) {
        String count = "";
        if (unreadCount > 0 && unreadCount <= msgThresholdValue) {
            count = String.valueOf(unreadCount);
        } else if (unreadCount > msgThresholdValue) {
            if (0 == countStyle) { //准确显示数目
                count = String.valueOf(unreadCount);
            } else { //模糊显示
                count = String.valueOf(msgThresholdValue) + "+";
            }
        }
        if (!TextUtils.isEmpty(count)) {
            int countWidth = Utils.computeStringWidth(messageCountPaint, count);
            int countHeight = Utils.computeStringHeight(messageCountPaint, count);
            canvas.drawText(count, dragDotCenterPoint.x - countWidth / 2, dragDotCenterPoint.y + countHeight / 2, messageCountPaint);
        }
    }

    /**
     * 拖拽时,绘制一个锚点
     *
     * @param canvas
     */
    private void drawAnchorDot(Canvas canvas) {
        canvas.drawCircle(anchorPoint.x, anchorPoint.y, anchorDotRadius, anchorDotPaint);
    }

    /**
     * 拖拽时,绘制一条橡皮筋,连接红点与锚点
     *
     * @param canvas
     */
    private void drawRubber(Canvas canvas) {
        PointF[] pointFs = MathUtils.getTangentPoint(anchorPoint.x, anchorPoint.y, anchorDotRadius, moveX, moveY, dragDotRadius);
        PointF controlPointF = MathUtils.getMiddlePoint(anchorPoint.x, anchorPoint.y, moveX, moveY);
        //利用贝塞尔取现画出皮筋
        rubberPath.reset();
        rubberPath.moveTo(anchorPoint.x, anchorPoint.y);
        rubberPath.lineTo(pointFs[0].x, pointFs[0].y);
        rubberPath.quadTo(controlPointF.x, controlPointF.y, pointFs[1].x, pointFs[1].y);
        rubberPath.lineTo(moveX, moveY);
        rubberPath.lineTo(pointFs[2].x, pointFs[2].y);
        rubberPath.quadTo(controlPointF.x, controlPointF.y, pointFs[3].x, pointFs[3].y);
        rubberPath.lineTo(anchorPoint.x, anchorPoint.y);

        canvas.drawPath(rubberPath, rubberPaint);
    }

    float downX, downY, moveX, moveY, upX, upY;
    boolean isdragable = false;//是否可拖拽
    boolean isInPullScale = true; //是否在拉力范围内
    boolean isDismiss = false;//是否应该dismiss小红点
    boolean isNotExceedPullScale = true;//是否未曾脱离拉力范围.false表示已经至少脱离过拉力范围一次;true表示尚未脱离过拉力范围

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (1 != dotStyle) { //只有dotStyle=1时,才可以拖动
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isInitFromLayout) { //从代码中实例化View
                    downX = event.getRawX();
                    downY = event.getRawY() - getStatusBarHeight(); //校正坐标

                    //在这里,dragDotRectF还没有被赋值
                    isdragable = false;
                } else { //从xml中实例化View
                    downX = event.getX();
                    downY = event.getY();
                    if (dragDotRectF.contains(downX, downY)) { //击中了红点,允许拖动
                        isdragable = true;
                        //在这里会遇到一个坑,getLocationInWindow返回的坐标与getLocationOnScreen返回的坐标相同
                        //这会导致计算错误,引起后续一系列问题.Android坑真多,被这个问题困了很久.参考下面两个答案
                        //http://stackoverflow.com/questions/17672891/getlocationonscreen-vs-getlocationinwindow
                        //http://stackoverflow.com/questions/2638342/incorrect-coordinates-from-getlocationonscreen-getlocationinwindow
                        int[] locationInScreen = new int[2]; //控件在当前屏幕中的坐标
                        getLocationOnScreen(locationInScreen);
                        setStatusBarHeight(Utils.getStatusBarHeight(this));
                        qqRedDotViewInWindow = new QQRedDotView.Builder().setAnchorDotRadius(this.anchorDotRadius)
                                .setDotColor(this.dotColor).setDotStyle(this.dotStyle).setDragDotRadius(this.dragDotRadius)
                                .setWindowManager(this.windowManager).setContext(this.context).setCountStyle(this.countStyle)
                                .setDragDistance(this.dragDistance).setUnreadCount(this.unreadCount).setTextSize(this.textSize)
                                .setTextColor(this.textColor).setStatusBarHeight(this.statusBarHeight).setMsgThresholdValue(this.msgThresholdValue)
                                .setWidgetCenterXInWindow(locationInScreen[0] + getWidth() / 2f)
                                .setwidgetCenterYInWindow(locationInScreen[1] + getHeight() / 2f - getStatusBarHeight())
                                .create();
                        qqRedDotViewInWindow.setQQRedDotViewInActivity(this);

                        addQQRedDotViewToWindow(qqRedDotViewInWindow); //添加到window中
                        this.setVisibility(GONE);//隐藏Activity中的红点

                        //红点开始拖动时的监听
                        if (null != onDragStartListener) {
                            onDragStartListener.OnDragStart();
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isInitFromLayout) {
                    //拖动的时候,再允许红点拉伸
                    isdragable = true;

                    moveX = event.getRawX();
                    moveY = event.getRawY() - getStatusBarHeight();//把坐标校正成Window中坐标
                    if (MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y) <= dragDistance) {
                        isInPullScale = true;
                        updateAnchorDotRadius(moveX, moveY);
                    } else {
                        isNotExceedPullScale = false;
                        isInPullScale = false;
                    }
                    computePosition(moveX, moveY);
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isInitFromLayout) {
                    if (isdragable && isInPullScale) { //在拉力范围内,要使红点复位
                        upX = event.getRawX();
                        upY = event.getRawY() - getStatusBarHeight();//校正坐标
                        if (isNotExceedPullScale) { //未曾脱离过拉力范围(橡皮筋还没有断),复位时,要有一个橡皮筋效果
                            animatorBackToAnchorPoint(upX, upY);
                        } else { //曾经脱离过拉力范围(橡皮筋已断),复位时,直接回到原始点
                            simpleBackToAnchorPoint(upX, upY);
                        }
                    } else if (isdragable && !isInPullScale) { //超过拉力范围,播放消失动画
                        upX = event.getRawX();
                        upY = event.getRawY() - getStatusBarHeight();//校正坐标

                        //消失
                        isDismiss = true;
                        invalidate();
                        animationDismiss(upX, upY);

                        //红点消失时的监听
                        if (null != getQQRedDotViewInActivity().getOnDotDismissListener()) {
                            getQQRedDotViewInActivity().getOnDotDismissListener().OnDotDismiss();
                        }
                    }
                }
                break;
            default:
                break;
        }
        //虽然qqRedDotViewInActivity会被隐藏,但是所有的Touch事件仍会被传递给
        //qqRedDotViewInActivity,所以需要在qqRedDotViewInActivity的onTouchEvent()函数中
        //把Touch事件传给qqRedDotViewInWindow
        if (qqRedDotViewInWindow != null) {
            qqRedDotViewInWindow.dispatchTouchEvent(event);
        }
        return true;
    }

    //拖拽过程中更新锚点半径.拖拽时,锚点的半径会逐渐变小.
    private void updateAnchorDotRadius(float moveX, float moveY) {
        float distance = MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y);
        anchorDotRadius = initAnchorRadius - (distance / dragDistance) * (initAnchorRadius - 5);
    }

    /**
     * 获取控件的中心点在window中的坐标
     *
     * @return
     */
    public float getWidgetCenterXInWindow() {
        return widgetCenterXInWindow;
    }

    /**
     * 获取控件的中心点在window中的坐标
     *
     * @return
     */
    public float getWidgetCenterYInWindow() {
        return widgetCenterYInWindow;
    }

    /**
     * 计算红点数据点和控制点
     * 红点位于控件的中心点位置,红点的中心点位置与控件的中心点位置重合.
     */
    private void computePosition() {
        if (isInitFromLayout) {
            anchorPoint.set(getWidth() / 2.0f, getHeight() / 2.0f);//保存锚点的位置
            computePosition(getWidth() / 2.0f, getHeight() / 2.0f);
        } else {
            anchorPoint.set(getWidgetCenterXInWindow(), getWidgetCenterYInWindow());//保存锚点的位置,因为锚点的位置是固定不变的,所以不能随着的touch事件更新,故放在这里初始化
            computePosition(getWidgetCenterXInWindow(), getWidgetCenterYInWindow());
        }
    }

    /**
     * 根据中心点的位置,计算红点的数据点和控制点
     *
     * @param centerX
     * @param centerY
     */
    private void computePosition(float centerX, float centerY) {
        dragDotCenterPoint.set(centerX, centerY);//保存中心点位置
        dragDotLeftTopPoint = center2LeftTop(dragDotCenterPoint);//保存左上角的位置
        //TODO 根据countStyle和msgThresholdValue
        if (unreadCount > 0 && unreadCount <= 9) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x + dragDotRadius * 2, dragDotLeftTopPoint.y + dragDotRadius * 2);
        } else if (unreadCount > 9 && unreadCount <= msgThresholdValue) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x + dragDotRadius * 12 / 5, dragDotLeftTopPoint.y + dragDotRadius * 2);
            computeRedDotBezierPoint(12 * dragDotRadius / 5, 2 * dragDotRadius);
        } else if (unreadCount > msgThresholdValue) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x + dragDotRadius * 3, dragDotLeftTopPoint.y + dragDotRadius * 2);
            computeRedDotBezierPoint(3 * dragDotRadius, 2 * dragDotRadius);
        }
    }

    /**
     * 计算红点的数据点和控制点
     *
     * @param width  红点的实际宽度
     * @param height 红点的实际高度
     */
    private void computeRedDotBezierPoint(float width, float height) {
        //数据点
        upPointFLeft.set(dragDotLeftTopPoint.x + dragDotRadius, dragDotLeftTopPoint.y);
        leftPointF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y + dragDotRadius);
        downPointFLeft.set(dragDotLeftTopPoint.x + dragDotRadius, dragDotLeftTopPoint.y + height);

        upPointFRight.set(dragDotLeftTopPoint.x + width - dragDotRadius, dragDotLeftTopPoint.y);
        rightPointF.set(dragDotLeftTopPoint.x + width, dragDotLeftTopPoint.y + dragDotRadius);
        downPointRight.set(dragDotLeftTopPoint.x + width - dragDotRadius, dragDotLeftTopPoint.y + height);

        //控制点
        upLeftPointF.set(dragDotLeftTopPoint.x + dragDotRadius - mDistance, dragDotLeftTopPoint.y);
        upRightPointF.set(dragDotLeftTopPoint.x + width - dragDotRadius + mDistance, dragDotLeftTopPoint.y);
        downLeftPointF.set(dragDotLeftTopPoint.x + dragDotRadius - mDistance, dragDotLeftTopPoint.y + height);
        downRightPointF.set(dragDotLeftTopPoint.x + width - dragDotRadius + mDistance, dragDotLeftTopPoint.y + height);
        leftUpPointF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y + dragDotRadius - mDistance);
        leftDownPointF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y + dragDotRadius + mDistance);
        rightUpPointF.set(dragDotLeftTopPoint.x + width, dragDotLeftTopPoint.y + dragDotRadius - mDistance);
        rightDownPointF.set(dragDotLeftTopPoint.x + width, dragDotLeftTopPoint.y + dragDotRadius + mDistance);
    }

    /**
     * 根据中间点的坐标,计算出红点实际左上角点的坐标
     * 注意,不是控件左上角.控件是大于等于红点的
     *
     * @param centerPointF
     * @return
     */
    private PointF center2LeftTop(PointF centerPointF) {
        PointF leftTopPointF = new PointF();
        if (unreadCount >= 0 && unreadCount < 10) {
            leftTopPointF.set(centerPointF.x - dragDotRadius, centerPointF.y - dragDotRadius);
        } else if (unreadCount >= 10 && unreadCount <= msgThresholdValue) {
            leftTopPointF.set(centerPointF.x - 6 * dragDotRadius / 5, centerPointF.y - dragDotRadius);
        } else {
            //TODO 这里需要根据countStyle的值定制
            leftTopPointF.set(centerPointF.x - 3 * dragDotRadius / 2, centerPointF.y - dragDotRadius);
        }
        return leftTopPointF;
    }

    //红点中心点坐标转换为左上角坐标
    private float centerX2StartX(float centerX) {
        float startX;
        if (unreadCount >= 0 && unreadCount < 10) {
            startX = centerX - dragDotRadius;
        } else if (unreadCount >= 10 && unreadCount <= msgThresholdValue) {
            startX = centerX - 6 * dragDotRadius / 5;
        } else {
            startX = centerX - 3 * dragDotRadius / 2;
        }
        return startX;
    }

    //红点中心点坐标转换为左上角坐标
    private float centerY2StartY(float centerY) {
        return centerY - dragDotRadius;
    }

    //小红点的消失动画
    private void animationDismiss(float upX, float upY) {
        final ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.dismiss_anim);
        final AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        long duration = 500;
        int width = imageView.getDrawable().getIntrinsicWidth();
        int height = imageView.getDrawable().getIntrinsicHeight();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.x = (int) (upX - width / 2);
        layoutParams.y = (int) (upY - height / 2);
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowManager.addView(imageView, layoutParams);
        animationDrawable.start();
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //更新未读消息数为0
                getQQRedDotViewInActivity().setUnreadCount(0);

                animationDrawable.stop();
                imageView.clearAnimation();
                windowManager.removeView(imageView);
                removeQQRedDotViewToWindow();
            }
        }, duration);
    }

    //简单的复位动画,没有回弹效果.
    private void simpleBackToAnchorPoint(final float upX, final float upY) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(upX, anchorPoint.x);
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float currentX = (float) animation.getAnimatedValue();
                float currentY = (anchorPoint.y - upY) * fraction + upY;
                moveX = currentX; //这时皮筋已断,已经不会再绘制皮筋了,所以其实可以取消对moveX和moveY的赋值操作
                moveY = currentY;
                computePosition(currentX, currentY);
                invalidate();
            }
        });
        animatorX.addListener(animatorListener);
        //不需要回弹效果,直接使用线性插值器
        animatorX.setInterpolator(new LinearInterpolator());
        animatorX.setDuration(200);
        animatorX.start();
    }

    //回到初始位置,带有回弹效果
    private void animatorBackToAnchorPoint(final float upX, final float upY) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(upX, anchorPoint.x);
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float currentX = (float) animation.getAnimatedValue();
                float currentY = (anchorPoint.y - upY) * fraction + upY;
                moveX = currentX; //画皮筋时要用到moveX和moveY
                moveY = currentY;
                computePosition(currentX, currentY);
                invalidate();
            }
        });
        animatorX.addListener(animatorListener);
        //TODO 这个回弹效果不够机智,将来自顶一个Interpolator优化一下
        animatorX.setInterpolator(new OvershootInterpolator(4.0f));
        //animatorX.setInterpolator(new BounceInterpolator()); //这个回弹效果不太好用
        animatorX.setDuration(500);
        animatorX.start();
    }

    //红点复位动画的监听器
    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            resetStatus();
            getQQRedDotViewInActivity().setVisibility(VISIBLE);
            getQQRedDotViewInActivity().resetStatus();

            //红点复位时的监听
            if (null != getQQRedDotViewInActivity().getOnDotResetListener()) {
                getQQRedDotViewInActivity().getOnDotResetListener().OnDotReset();
            }

            removeQQRedDotViewToWindow();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    //重置状态值
    public void resetStatus() {
        isdragable = false;
        isInPullScale = true;
        isNotExceedPullScale = true;
        isDismiss = false;
    }

    //添加红点到WindowManager
    public void addQQRedDotViewToWindow(QQRedDotView qqRedDotView) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.format = PixelFormat.RGBA_8888;
//        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.x = 0;
        layoutParams.y = 0;
        windowManager.addView(qqRedDotView, layoutParams);
    }

    //从Window中把红点移除
    public void removeQQRedDotViewToWindow() {
        windowManager.removeView(this);
    }

    /**
     * 保存对Activity中红点的引用
     *
     * @param qqRedDotView
     */
    public void setQQRedDotViewInActivity(QQRedDotView qqRedDotView) {
        this.qqRedDotViewInActivity = qqRedDotView;
    }

    /**
     * 获取Activity中的红点对象的引用
     *
     * @return
     */
    public QQRedDotView getQQRedDotViewInActivity() {
        return this.qqRedDotViewInActivity;
    }

    /**
     * 获取状态栏的高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    /**
     * 设置状态栏的高度
     *
     * @param statusBarHeight
     */
    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    /**
     * 更新未读消息的数量
     * 参考:http://blog.csdn.net/yanbober/article/details/46128379
     */
    public void setUnreadCount(int unreadCount) {
        int lastCount = this.unreadCount;
        this.unreadCount = unreadCount;
        if (0 == lastCount) {
            requestLayout();
            invalidate();
        } else if (lastCount > 0 && lastCount < 10) {
            if (unreadCount < 10) {
                invalidate();
            } else {
                requestLayout();
                invalidate();
            }
        } else if (lastCount >= 10 && lastCount <= msgThresholdValue) {
            if (unreadCount < 10) {
                requestLayout();
                invalidate();
            } else if (unreadCount >= 10 && unreadCount <= msgThresholdValue) {
                invalidate();
            } else if (unreadCount > msgThresholdValue) {
                requestLayout();
                invalidate();
            }
        } else if (lastCount > msgThresholdValue) {
            if (unreadCount > msgThresholdValue) {
                invalidate();
            } else {
                requestLayout();
                invalidate();
            }
        }
    }

    /**
     * 返回当前未读消息数量
     * @return
     */
    public int getUnreadCount(){
        return unreadCount;
    }

    /**
     * 开始拖动红点的监听
     */
    public interface OnDragStartListener {
        void OnDragStart();
    }

    OnDragStartListener onDragStartListener;

    public void setOnDragStartListener(OnDragStartListener onDragStartListener) {
        this.onDragStartListener = onDragStartListener;
    }

    public OnDragStartListener getOnDragStartListener() {
        return onDragStartListener;
    }

    /**
     * 红点消失的监听
     */
    public interface OnDotDismissListener {
        void OnDotDismiss();
    }

    OnDotDismissListener onDotDismissListener;

    public void setOnDotDismissListener(OnDotDismissListener onDotDismissListener) {
        this.onDotDismissListener = onDotDismissListener;
    }

    public OnDotDismissListener getOnDotDismissListener() {
        return onDotDismissListener;
    }

    /**
     * 红点复位时的监听
     */
    public interface OnDotResetListener {
        void OnDotReset();
    }

    OnDotResetListener onDotResetListener;

    public void setOnDotResetListener(OnDotResetListener onDotResetListener) {
        this.onDotResetListener = onDotResetListener;
    }

    public OnDotResetListener getOnDotResetListener() {
        return onDotResetListener;
    }

    /**
     * 使用builder模式初始化控件
     * 从代码中初始化控件时调用
     */
    public static class Builder {
        private Context context;
        private WindowManager windowManager;//在window中
        private int statusBarHeight;//状态栏高度,在Window中无法测量,需要从Activity中传入
        private float widgetCenterXInWindow;//控件的中心点在Window中的位置.
        private float widgetCenterYInWindow;//控件的中心点在Window中的位置.

        private float dragDistance;//红点的可拖拽距离,超过这个距离,红点会消失;小于这个距离,红点会复位
        private float dragDotRadius;//红点的半径
        private float anchorDotRadius;//锚点的半径.在拖动过程中,它的数值会不断减小.
        private int dotColor;//红点,皮筋和锚点的颜色
        private int textColor;//未读消息的字体颜色
        private int textSize;//未读消息的字体大小
        private int dotStyle;//红点的style.0,实心点;1,可拖动;2,不可拖动
        private int countStyle;//未读消息数的显示风格.0,准确显示;1,超过一定数值,就显示一个大概的数
        private int msgThresholdValue;//未读消息数的阈值

        private int unreadCount;//未读消息数

        public Builder() {
        }

        public Builder setAnchorDotRadius(float anchorDotRadius) {
            this.anchorDotRadius = anchorDotRadius;
            return this;
        }

        public Builder setContext(Context context) {
            this.context = context;
            return this;
        }

        public Builder setDotColor(int dotColor) {
            this.dotColor = dotColor;
            return this;
        }

        public Builder setDotStyle(int dotStyle) {
            this.dotStyle = dotStyle;
            return this;
        }

        public Builder setDragDistance(float dragDistance) {
            this.dragDistance = dragDistance;
            return this;
        }

        public Builder setDragDotRadius(float dragDotRadius) {
            this.dragDotRadius = dragDotRadius;
            return this;
        }

        public Builder setStatusBarHeight(int statusBarHeight) {
            this.statusBarHeight = statusBarHeight;
            return this;
        }

        public Builder setWidgetCenterXInWindow(float widgetCenterXInWindow) {
            this.widgetCenterXInWindow = widgetCenterXInWindow;
            return this;
        }

        public Builder setwidgetCenterYInWindow(float widgetCenterYInWindow) {
            this.widgetCenterYInWindow = widgetCenterYInWindow;
            return this;
        }

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setMsgThresholdValue(int msgThresholdValue) {
            this.msgThresholdValue = msgThresholdValue;
            return this;
        }

        public Builder setWindowManager(WindowManager windowManager) {
            this.windowManager = windowManager;
            return this;
        }

        public Builder setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
            return this;
        }

        public Builder setCountStyle(int countStyle) {
            this.countStyle = countStyle;
            return this;
        }

        public QQRedDotView create() {
            return new QQRedDotView(context, windowManager, statusBarHeight,
                    widgetCenterXInWindow, widgetCenterYInWindow,
                    dragDistance, dragDotRadius, anchorDotRadius, dotColor, textColor, textSize,
                    dotStyle, unreadCount, countStyle, msgThresholdValue);
        }
    }
}
