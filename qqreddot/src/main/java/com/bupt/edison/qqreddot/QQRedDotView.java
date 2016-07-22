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
import android.util.Log;
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
    int titleBarHeight;//标题栏高度

    QQRedDotView mQQRedDotView; //window中的红点维持一个Activity中红点的引用

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
    int unreadCount; //未读的消息数
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

    float dotRealWidth;//红点真实的宽度
    float dotRealHeight;//红点真实的高度
    PointF dragDotCenterPoint;//红点的中心点
    PointF dragDotLeftTopPoint;//红点的左上角的点
    //一些控件的控制变量
    float initX, initY;//红点初始时的位置(左上角的坐标)
    float initCenterX, initCenterY;//红点初始时的中心位置(圆心的坐标)
    float initAnchorRadius;//锚点初始的半径, 与未变化前的anchorDotRadius值相等


    /**
     * 用固定的属性值初始化控件
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
        initAttribute(context,attrs);
        initTools(context);
        isInitFromLayout = true;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public QQRedDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context,attrs);
        initTools(context);
        isInitFromLayout = true;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public QQRedDotView(Context context,WindowManager windowManager,int statusBarHeight,int titleBarHeight,
                        float dragDistance,float dragDotRadius,float anchorDotRadius,
                        int dotColor,int textColor,int textSize,int dotStyle,int unreadCount,int countStyle){
        super(context);
        this.context = context;
        this.windowManager = windowManager;
        this.statusBarHeight = statusBarHeight;
        this.titleBarHeight = titleBarHeight;
        this.dragDistance = dragDistance;
        this.dragDotRadius = dragDotRadius;
        this.anchorDotRadius = anchorDotRadius;
        this.dotColor = dotColor;
        this.textColor = textColor;
        this.textSize = textSize;
        this.dotStyle = dotStyle;
        this.countStyle = countStyle;
        this.unreadCount = unreadCount;
        initTools(context);
        isInitFromLayout = false;
    }

    //用固定的属性值初始化各个属性
    private void initAttribute(Context context){
        dragDotRadius = Utils.dp2px(context,20);
        anchorDotRadius = Utils.dp2px(context,16);
        dotColor = Color.RED;
        textColor = Color.WHITE;
        textSize = Utils.sp2px(context,24);
        dotStyle = 1;
        countStyle = 1;
        dragDistance = Utils.dp2px(context,150);
    }

    //从layout文件中读取配置,初始化控件
    private void initAttribute(Context context, AttributeSet attrs){
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.QQRedDotView);
        //红点的半径
        dragDotRadius = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_dragDotRadius,20);
        //锚点的半径
        anchorDotRadius = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_anchorDotRadius,16);
        //红点的颜色
        dotColor = typedArray.getColor(R.styleable.QQRedDotView_dotColor,Color.RED);
        //消息字体的颜色
        textColor = typedArray.getColor(R.styleable.QQRedDotView_textColor,Color.WHITE);
        //消息字体的大小
        textSize = typedArray.getDimensionPixelSize(R.styleable.QQRedDotView_textSize,24);
        //红点的风格.0,实心点;1,可拖动;2,不可拖动
        dotStyle = typedArray.getInt(R.styleable.QQRedDotView_dotStyle,1);
        //红点可拖动的距离.也是消失距离和复位距离;
        dragDistance = typedArray.getDimensionPixelOffset(R.styleable.QQRedDotView_dragDistance,150);
        //未读消息数的显示风格
        countStyle = typedArray.getInt(R.styleable.QQRedDotView_countStyle,1);
        typedArray.recycle();
    }

    private void initTools(Context context) {
        this.context = context;

        dragDotPaint = new Paint();
        dragDotPaint.setAntiAlias(true);
        dragDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dragDotPaint.setStrokeWidth(1);
        dragDotPaint.setColor(dotColor);

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
        //贝塞尔曲线
        redDotPath = new Path();
        rubberPath = new Path();
        //锚点的圆心点
        anchorPoint = new PointF();

        //红点的范围矩阵
        dragDotRectF = new RectF();
        //红点的中心点
        dragDotCenterPoint = new PointF();
        //红点的左上角的点
        dragDotLeftTopPoint = new PointF();
        //未读消息数量
        unreadCount = 999; //初始化时,未读消息数置为0.这里设置为119,是为了测试


        //一些临时变量
        initX = 0;
        initY = 0;
        initCenterX = 0;
        initCenterY = 0;

        initAnchorRadius = anchorDotRadius;


        //状态栏和标题栏的高度,要从Activity传进来
        titleBarHeight = 0;
        titleBarHeight = 0;
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("edison onMeasure", "pivotX: " + getPivotX() + " pivotY: " + getPivotY());
        Log.d("edison onMeasure", "X: " + getX() + " Y: " + getY());
        Log.d("edison onMeasure", "width: " + getWidth() + " " + "height: " + getHeight());
        Log.d("edison onMeasure", "left: " + getLeft() + " " + "top: " + getTop() + " right: " + getRight() + " bottom: " + getBottom());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float width,height;

        //红点的高度固定,但是宽度是根据未读消息数而变化的
        dotRealHeight = dragDotRadius*2;
        if(unreadCount>=0 && unreadCount<10){ //消息数为个位数
            dotRealWidth = dragDotRadius*2;
        }else if(unreadCount>=10 && unreadCount<=99) { // 消息数为两位数
            dotRealWidth = 12*dragDotRadius/5;
        }else{ //消息数为三位数及以上
            dotRealWidth = 3*dragDotRadius;
        }

        if(widthMode == MeasureSpec.EXACTLY){
            width=widthSize<dotRealWidth?dotRealWidth:widthSize; //必须保证可以完整容纳红点
        }else{
            width = dotRealWidth;
        }

        if(heightMode == MeasureSpec.EXACTLY){
            height=heightSize<dragDotRadius*2?dragDotRadius*2:heightSize; //必须保证可以完整容纳红点
        }else{
            height = dragDotRadius*2;
        }

        setMeasuredDimension((int)width+2,(int)height+2); //宽高各加两个像素,防止红点的边缘被切掉
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("edison onLayout", "pivotX: " + getPivotX() + " pivotY: " + getPivotY());
        Log.d("edison onLayout", "X: " + getX() + " Y: " + getY());
        Log.d("edison onLayout", "width: " + getWidth() + " " + "height: " + getHeight());
        Log.d("edison onLayout", "left: " + getLeft() + " " + "top: " + getTop() + " right: " + getRight() + " bottom: " + getBottom());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("edison onSizeChanged", "pivotX: " + getPivotX() + " pivotY: " + getPivotY());
        Log.d("edison onSizeChanged", "X: " + getX() + " Y: " + getY());
        Log.d("edison onSizeChanged", "width: " + getWidth() + " " + "height: " + getHeight());
        Log.d("edison onSizeChanged", "left: " + getLeft() + " " + "top: " + getTop() + " right: " + getRight() + " bottom: " + getBottom());

        //计算红点和锚点的中心点位置,计算红点的数据点和控制点
        computePosition();

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("edison", "onDraw");

        if(0 == dotStyle){ //实心红点,没有未读消息数
            canvas.drawCircle(getWidth()/2.0f,getHeight()/2.0f,dragDotRadius,dragDotPaint);
        }else { //显示未读消息数
            if (unreadCount > 0 && !isDimiss) {
                if (isdragable && isInPullScale && isFirstOutPullScale) {
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
        if (unreadCount > 0 && unreadCount <= 99) {
            count = String.valueOf(unreadCount);
        } else if (unreadCount > 99) {
            if(0 == countStyle){ //准确显示数目
                count = String.valueOf(unreadCount);
            }else { //模糊显示
                count = "99+";
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

    float downX, downY, moveX, moveY, upX, upY, upRawX, upRawY;
    boolean isdragable = false;//是否可拖拽
    boolean isInPullScale = true; //是否在拉力范围内
    boolean isDimiss = false;//是否应该dimiss小红点
    boolean isFirstOutPullScale = true;//是否是第一次脱离拉力范围.false表示已经至少脱离过拉力范围一次;true表示尚未脱离过拉力范围
    QQRedDotView qqRedDotView;//Window中的红点

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isInitFromLayout) {
                    Log.d("edison event again", event + "");
                    downX = event.getRawX();
                    downY = event.getRawY()-getStatusBarHeight();
                    Log.d("edison action down", "downX: " + downX + " downY: " + downY);
                    Log.d("edison dot rectF", dragDotRectF +"");
                    if (dragDotRectF.contains(downX, downY)) {
                        isdragable = true;
                    }
                } else {
                    downX = event.getX();
                    downY = event.getY();
                    if (dragDotRectF.contains(downX, downY)) {
                        isdragable = true;

                        Log.d("edison event", event + "");
                        qqRedDotView = new QQRedDotView(context);
                        qqRedDotView.setQQRedDotView(this);
                        qqRedDotView.setStatusBarHeight(Utils.getStatusBarHeight(this));
                        qqRedDotView.setWindowManager(windowManager);
                        qqRedDotView.setInitX(event.getRawX());
                        qqRedDotView.setInitY(event.getRawY());
                        if (null != onComputeTitleBarHeightListner) {
                            qqRedDotView.setTitleBarHeight(onComputeTitleBarHeightListner.onComputeTitleBarHeight());
                        }
                        addQQRedDotViewToWindow(qqRedDotView, event);
                        this.setVisibility(GONE);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("edison event", "move" + event);
                if ( !isInitFromLayout) {

                    isdragable = true;

                    moveX = event.getRawX();
                    moveY = event.getRawY()-getStatusBarHeight();
                    if (MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y) <= dragDistance) {
                        isInPullScale = true;
                        updateAnchorDotRadius(moveX, moveY);
                    } else {
                        isFirstOutPullScale = false;
                        isInPullScale = false;
                    }
                    computePosition(centerX2StartX(moveX), centerY2StartY(moveY));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isInitFromLayout) {
                    if (isdragable && isInPullScale) {
                        upX = event.getRawX();
                        upY = event.getRawY();
                        if (isFirstOutPullScale) {
                            animatorBackToAnchorPoint(upX, upY);
                        } else {
                            simpleBackToAnchorPoint(upX, upY);
                        }
                    } else if (isdragable && !isInPullScale) {
                        upX = event.getRawX();
                        upY = event.getRawY();
                        upRawX = event.getRawX();
                        upRawY = event.getRawY();

                        //消失
                        isDimiss = true;
                        invalidate();
                        animationDismiss();
                    }
                }
                break;
            default:
                break;
        }
        if (qqRedDotView != null) {
            qqRedDotView.dispatchTouchEvent(event);
        }
        return true;
    }

    //拖拽时,锚点的半径会逐渐变小
    private void updateAnchorDotRadius(float moveX, float moveY) {
        float distance = MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y);
        anchorDotRadius = (int) (initAnchorRadius - (distance / dragDistance) * (initAnchorRadius - 1));
    }

    /**
     * 计算红点数据点和控制点
     * 红点位于控件的中心点位置,红点的中心点位置与控件的中心点位置重合.
     */
    private void computePosition() {
        if(isInitFromLayout) {
            computePosition(getWidth()/2.0f, getHeight()/2.0f);
        }else{
            computePosition(getInitX(),getInitY());
        }
    }

    public float getInitX() {
        return initX;
    }

    public void setInitX(float initX) {
        this.initX = initX;
    }

    public float getInitY() {
        return initY;
    }

    public void setInitY(float initY) {
        this.initY = initY;
    }

    /**
     * 根据中心点的位置,计算红点的数据点和控制点
     * @param centerX
     * @param centerY
     */
    private void computePosition(float centerX, float centerY) {
        dragDotCenterPoint.set(centerX,centerY);//保存中心点位置
        dragDotLeftTopPoint = center2LeftTop(dragDotCenterPoint);//保存左上角的位置
        anchorPoint.set(centerX, centerY);//保存锚点的位置

        //以后不需要在这里初始化它们了,删掉
        initCenterX = centerX;
        initCenterY = centerY;
        initX = centerX2StartX(centerX);
        initY = centerY2StartY(centerY);

        if (unreadCount > 0 && unreadCount <= 9) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x+dragDotRadius*2,dragDotLeftTopPoint.y+dragDotRadius*2);
        } else if (unreadCount > 9 && unreadCount <= 99) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x+dragDotRadius*12/5,dragDotLeftTopPoint.y+dragDotRadius*2);
            computeRedDotBezierPoint(12 * dragDotRadius / 5, 2 * dragDotRadius);
        } else if (unreadCount > 99) {
            dragDotRectF.set(dragDotLeftTopPoint.x, dragDotLeftTopPoint.y, dragDotLeftTopPoint.x+dragDotRadius*3,dragDotLeftTopPoint.y+dragDotRadius*2);
            computeRedDotBezierPoint(3 * dragDotRadius, 2 * dragDotRadius);
        }
    }

    /**
     * 计算红点的数据点和控制点
     *
     * @param width 红点的实际宽度
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

    //红点中心点坐标转换为左上角坐标
    private float centerX2StartX(float centerX) {
        float startX;
        if(unreadCount>=0 && unreadCount<10){
            startX = centerX - dragDotRadius;
        }else if(unreadCount>=10 && unreadCount<100){
            startX = centerX - 6*dragDotRadius/5;
        }else{
            startX = centerX - 3*dragDotRadius/2;
        }
        return startX;
    }

    //红点中心点坐标转换为左上角坐标
    private float centerY2StartY(float centerY) {
        return centerY - dragDotRadius;
    }

    /**
     * 根据中间点的坐标,计算出左上角点的坐标
     * @param centerPointF
     * @return
     */
    private PointF center2LeftTop(PointF centerPointF){
        PointF leftTopPointF = new PointF();
        if(unreadCount>=0 && unreadCount<10){
            leftTopPointF.set(centerPointF.x-dragDotRadius,centerPointF.y-dragDotRadius);
        }else if(unreadCount>=10 && unreadCount<100){
            leftTopPointF.set(centerPointF.x-6*dragDotRadius/5,centerPointF.y-dragDotRadius);
        }else{
            //TODO 这里需要根据countStyle的值定制
            leftTopPointF.set(centerPointF.x-3*dragDotRadius/2,centerPointF.y-dragDotRadius);
        }
        return leftTopPointF;
    }

    //小红点的消失动画
    private void animationDismiss() {
        final ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.dismiss_anim);
        final AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        long duration = 500;
        int width = imageView.getDrawable().getIntrinsicWidth();
        int height = imageView.getDrawable().getIntrinsicHeight();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.x = (int) (upRawX - width / 2);
        layoutParams.y = (int) (upRawY - height / 2) - getStatusBarHeight();
        Log.d("edison LayoutParams", "x: " + upRawX);
        Log.d("edison LayoutParams", "y: " + upRawY);
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        windowManager.addView(imageView, layoutParams);
        animationDrawable.start();
        imageView.postDelayed(new Runnable() {
            @Override
            public void run() {
                animationDrawable.stop();
                imageView.clearAnimation();
                windowManager.removeView(imageView);
                removeQQRedDotViewToWindow();
            }
        }, duration);
        Log.d("edison", "dismiss reddot");
    }

    //简单的复位动画,没有回弹效果.从消失区域回到复原区域时调用
    private void simpleBackToAnchorPoint(final float upX, final float upY) {
        ValueAnimator animatorX = ValueAnimator.ofFloat(upX, anchorPoint.x);
        animatorX.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                float currentX = (float) animation.getAnimatedValue();
                float currentY = (anchorPoint.y - upY) * fraction + upY;
                moveX = currentX;
                moveY = currentY;
                computePosition(centerX2StartX(currentX), centerY2StartY(currentY));
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
                moveX = currentX;
                moveY = currentY;
                computePosition(centerX2StartX(currentX), centerY2StartY(currentY));
                invalidate();
            }
        });
        animatorX.addListener(animatorListener);
        //这个回弹效果不够机智,将来自顶一个Interpolator优化一下
        animatorX.setInterpolator(new OvershootInterpolator(4.0f));
//        animatorX.setInterpolator(new BounceInterpolator());
        animatorX.setDuration(500);
        animatorX.start();
    }

    Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {

        }

        @Override
        public void onAnimationEnd(Animator animation) {
            resetStatus();
            getQQRedDotView().setVisibility(VISIBLE);
            getQQRedDotView().resetStatus();
            removeQQRedDotViewToWindow();
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
    public void resetStatus(){
        isdragable = false;
        isInPullScale = true;
        isFirstOutPullScale = true;
        isDimiss = false;
    }

    //添加view到WindowManager
    public void addQQRedDotViewToWindow(QQRedDotView qqRedDotView, MotionEvent event) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
//        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.x = (int)event.getRawX();
        layoutParams.y = (int)event.getY() - getTitleBarHeight();
        windowManager.addView(qqRedDotView, layoutParams);
    }

    public void removeQQRedDotViewToWindow() {
        windowManager.removeView(this);
    }

    public void setQQRedDotView(QQRedDotView qqRedDotView) {
        this.mQQRedDotView = qqRedDotView;
    }

    public QQRedDotView getQQRedDotView() {
        return this.mQQRedDotView;
    }

    public int getStatusBarHeight() {
        return statusBarHeight;
    }

    public void setStatusBarHeight(int statusBarHeight) {
        this.statusBarHeight = statusBarHeight;
    }

    public int getTitleBarHeight() {
        return titleBarHeight;
    }

    public void setTitleBarHeight(int titleBarHeight) {
        this.titleBarHeight = titleBarHeight;
    }

    public WindowManager getWindowManager() {
        return windowManager;
    }

    public void setWindowManager(WindowManager windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * 因为在View中无法获得标题栏的高度,所以写个回调由Activity或Fragment传递
     */
    public interface OnComputeTitleBarHeightListner {
        /**
         * 返回标题栏的高度
         *
         * @return
         */
        public int onComputeTitleBarHeight();
    }

    OnComputeTitleBarHeightListner onComputeTitleBarHeightListner;

    public void setOnComputeTitleBarHeightListner(OnComputeTitleBarHeightListner onComputeTitleBarHeightListner) {
        this.onComputeTitleBarHeightListner = onComputeTitleBarHeightListner;
    }

    /**
     * 更新未读消息的数量
     */
    public interface OnUpdateMessageCountListener {
        /**
         * 更新未读消息的数量
         *
         * @return
         */
        public int onUpdateMessageCount();
    }

    OnUpdateMessageCountListener onUpdateMessageCountListener;

    public void setOnUpdateMessageCountListener(OnUpdateMessageCountListener onUpdateMessageCountListener) {
        this.onUpdateMessageCountListener = onUpdateMessageCountListener;
    }

    /**
     * 开始拖动红点的监听
     */
    public interface OnDragStartListener{
        public void OnDragStart();
    }

    OnDragStartListener onDragStartListener;

    public void setOnDragStartListener(OnDragStartListener onDragStartListener){
        this.onDragStartListener = onDragStartListener;
    }

    /**
     * 红点消失的监听
     */
    public interface OnDotDismissListener{
        public void OnDotDismiss();
    }

    OnDotDismissListener onDotDismissListener;

    public void setOnDotDismissListener(OnDotDismissListener onDotDismissListener){
        this.onDotDismissListener = onDotDismissListener;
    }

    /**
     * 红点复位时的监听
     */
    public interface OnDotResetListener{
        public void OnDotReset();
    }

    OnDotResetListener onDotResetListener;

    public void setOnDotResetListener(OnDotResetListener onDotResetListener){
        this.onDotResetListener = onDotResetListener;
    }

    /**
     * 使用builder模式初始化控件
     * 从代码中初始化控件时,调用
     */
    public static class Builder{
        private Context context;
        private WindowManager windowManager;//在window中
        private int statusBarHeight;//状态栏高度,在Window中无法测量,需要从Activity中传入
        private int titleBarHeight;//标题栏高度

        private float dragDistance;//红点的可拖拽距离,超过这个距离,红点会消失;小于这个距离,红点会复位
        private float dragDotRadius;//红点的半径
        private float anchorDotRadius;//锚点的半径.在拖动过程中,它的数值会不断减小.
        private int dotColor;//红点,皮筋和锚点的颜色
        private int textColor;//未读消息的字体颜色
        private int textSize;//未读消息的字体大小
        private int dotStyle;//红点的style.0,实心点;1,可拖动;2,不可拖动
        private int countStyle;//未读消息数的显示风格.0,准确显示;1,超过一定数值,就显示一个大概的数

        private int unreadCount;//未读消息数

        public Builder(){
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

        public Builder setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Builder setTextSize(int textSize) {
            this.textSize = textSize;
            return this;
        }

        public Builder setTitleBarHeight(int titleBarHeight) {
            this.titleBarHeight = titleBarHeight;
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

        public QQRedDotView create(){
            return new QQRedDotView(context,windowManager,statusBarHeight,titleBarHeight,
                    dragDistance,dragDotRadius,anchorDotRadius,dotColor,textColor,textSize,
                    dotStyle,unreadCount,countStyle);
        }
    }
}
