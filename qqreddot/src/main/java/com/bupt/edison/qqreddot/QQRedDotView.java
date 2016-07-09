package com.bupt.edison.qqreddot;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
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
    Paint dotPaint; //红点画笔
    Paint rubberPaint;//皮筋画笔
    Paint anchorDotPaint;//錨点画笔
    Paint messageCountPaint;//未读消息数的画笔
    float initX, initY;//红点初始时的位置
    float initCenterX, initCenterY;//红点初始时的中心位置
    int dismissRedius;//消失的距离
    int dotRedius;//红点的半径
    int anchorRedius;//锚点的半径
    RectF dotRectF;//红点的范围矩阵

    int unreadCount; //未读的消息数

    Context context;
    public static final float constant = 0.552284749831f;
    float mDistance;

    //用六个数据点,八个控制点画圆.n = 4;
    PointF upPointFLeft, upPointFRight, downPointFLeft, downPointRight, leftPointF, rightPointF; //数据点
    //八个控制点
    PointF upLeftPointF, upRightPointF, downLeftPointF, downRightPointF, leftUpPointF, leftDownPointF, rightUpPointF, rightDownPointF; //控制点
    Path redDotPath;//红点的贝塞尔曲线path
    Path rubberPath;//皮筋的贝塞尔取现path
    PointF anchorPoint;//锚点
    float initAnchorRedius;

    public QQRedDotView(Context context) {
        super(context);
        init(context);
    }

    public QQRedDotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QQRedDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;

        dotPaint = new Paint();
        dotPaint.setAntiAlias(true);
        dotPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        dotPaint.setStrokeWidth(3);
        dotPaint.setColor(Color.RED);

        rubberPaint = new Paint();
        rubberPaint.setStrokeWidth(3);
        rubberPaint.setColor(Color.RED);
        rubberPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        rubberPaint.setAntiAlias(true);

        anchorDotPaint = new Paint();
        anchorDotPaint.setColor(Color.RED);
        anchorDotPaint.setStrokeWidth(3);
        anchorDotPaint.setAntiAlias(true);
        anchorDotPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        messageCountPaint = new Paint();
        messageCountPaint.setColor(Color.WHITE);
        messageCountPaint.setAntiAlias(true);
        messageCountPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        messageCountPaint.setStrokeWidth(1);
        messageCountPaint.setTextSize(Utils.sp2px(context, 12));

        initX = 0;
        initY = 0;
        initCenterX = 0;
        initCenterY = 0;

        dismissRedius = Utils.dp2px(context, 150);
        dotRedius = 10;
        anchorRedius = 8;
        initAnchorRedius = 8;

        unreadCount = 119;

        dotRectF = new RectF();

        mDistance = constant * Utils.dp2px(context, dotRedius);

        upPointFLeft = new PointF();
        downPointFLeft = new PointF();
        upPointFRight = new PointF();
        downPointRight = new PointF();
        leftPointF = new PointF();
        rightPointF = new PointF();

        upLeftPointF = new PointF();
        upRightPointF = new PointF();
        downLeftPointF = new PointF();
        downRightPointF = new PointF();
        leftUpPointF = new PointF();
        leftDownPointF = new PointF();
        rightUpPointF = new PointF();
        rightDownPointF = new PointF();

        redDotPath = new Path();
        rubberPath = new Path();

        anchorPoint = new PointF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("edison onMeasure", "pivotX: " + getPivotX() + " pivotY: " + getPivotY());
        Log.d("edison onMeasure", "X: " + getX() + " Y: " + getY());
        Log.d("edison onMeasure", "width: " + getWidth() + " " + "height: " + getHeight());
        Log.d("edison onMeasure", "left: " + getLeft() + " " + "top: " + getTop() + " right: " + getRight() + " bottom: " + getBottom());
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
        computePosition();
        anchorPoint.set(initCenterX, initCenterY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("edison", "onDraw");
        if (unreadCount > 0 && !isDimiss) {
            if (isdragable && isInPullScale && isFirstOutPullScale) {
                drawRubber(canvas);
                drawAnchorDot(canvas);
            }
            drawDot(canvas); //画红点
        }
    }

    private void drawDot(Canvas canvas) {
        if (unreadCount > 0 && unreadCount <= 9) {
            canvas.drawCircle(initCenterX, initCenterY, Utils.dp2px(context, dotRedius), dotPaint);
        } else if (unreadCount > 9) { //用贝塞尔取现画拉伸的红点
            redDotPath.reset();
            redDotPath.moveTo(upPointFLeft.x, upPointFLeft.y);
            redDotPath.lineTo(upPointFRight.x, upPointFRight.y);
            redDotPath.cubicTo(upRightPointF.x, upRightPointF.y, rightUpPointF.x, rightUpPointF.y, rightPointF.x, rightPointF.y);
            redDotPath.cubicTo(rightDownPointF.x, rightDownPointF.y, downRightPointF.x, downRightPointF.y, downPointRight.x, downPointRight.y);
            redDotPath.lineTo(downPointFLeft.x, downPointFLeft.y);
            redDotPath.cubicTo(downLeftPointF.x, downLeftPointF.y, leftDownPointF.x, leftDownPointF.y, leftPointF.x, leftPointF.y);
            redDotPath.cubicTo(leftUpPointF.x, leftUpPointF.y, upLeftPointF.x, upLeftPointF.y, upPointFLeft.x, upPointFLeft.y);

            canvas.drawPath(redDotPath, dotPaint);
        }

        drawMsgCount(canvas);
    }

    private void drawMsgCount(Canvas canvas) {
        String count = "";
        if (unreadCount > 0 && unreadCount <= 99) {
            count = String.valueOf(unreadCount);
        } else if (unreadCount > 99) {
            count = "+99";
        }
        if (!TextUtils.isEmpty(count)) {
            int countWidth = Utils.computeStringWidth(messageCountPaint, count);
            int countHeight = Utils.computeStringHeight(messageCountPaint, count);
            canvas.drawText(count, initCenterX - countWidth / 2, initCenterY + countHeight / 2, messageCountPaint);
        }

    }

    /**
     * 拖拽时,绘制一个锚点
     *
     * @param canvas
     */
    private void drawAnchorDot(Canvas canvas) {
        canvas.drawCircle(anchorPoint.x, anchorPoint.y, Utils.dp2px(context, anchorRedius), anchorDotPaint);
    }

    /**
     * 拖拽时,绘制一条橡皮筋,连接红点与锚点
     *
     * @param canvas
     */
    private void drawRubber(Canvas canvas) {
        PointF[] pointFs = MathUtils.getTangentPoint(anchorPoint.x, anchorPoint.y, Utils.dp2px(context, anchorRedius), moveX, moveY, Utils.dp2px(context, dotRedius));
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
    boolean isdragable = false;
    boolean isInPullScale = true; //是否在拉力范围内
    boolean isDimiss = false;
    boolean isFirstOutPullScale = true;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                Log.d("edison action down", "downX: " + downX + " downY: " + downY);
                if (dotRectF.contains(downX, downY)) {
                    isdragable = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isdragable) {
                    moveX = event.getX();
                    moveY = event.getY();
                    if (MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y) <= dismissRedius) {
                        isInPullScale = true;
                        updateAnchorDotRedius(moveX, moveY);
                    } else {
                        isFirstOutPullScale = false;
                        isInPullScale = false;
                    }
                    computePosition(centerX2StartX(moveX), centerY2StartY(moveY));
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isdragable && isInPullScale) {
                    upX = event.getX();
                    upY = event.getY();
                    if (isFirstOutPullScale) {
                        animatorBackToAnchorPoint(upX, upY);
                    } else {
                        simpleBackToAnchorPoint(upX, upY);
                    }
                } else if (isdragable && !isInPullScale) {
                    upX = event.getX();
                    upY = event.getY();
                    upRawX = event.getRawX();
                    upRawY = event.getRawY();

                    //消失
                    isDimiss = true;
                    invalidate();
                    animationDismiss();

                }
                break;
            default:
                break;
        }
        return true;
    }

    private void updateAnchorDotRedius(float moveX, float moveY) {
        float distance = MathUtils.getDistanceBetweenPoints(moveX, moveY, anchorPoint.x, anchorPoint.y);
        anchorRedius = (int) (initAnchorRedius - (distance / dismissRedius) * (initAnchorRedius - 1));
    }

    /**
     * 利用属性动画拖动view
     *
     * @param toX
     * @param toY
     * @param oldX
     * @param oldY
     */
    public void animatorMove(float toX, float toY, float oldX, float oldY) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this, "translationX", oldX, toX);
        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(this, "translationY", oldY, toY);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator, objectAnimator1);
        animatorSet.setDuration(1);
        animatorSet.start();
    }

    //正因为下面方法的种种局限,所以要实现全屏可拖动小红点,必须使用 WindowManager

    //1.属性动画,可以移动view,但是不能超过父布局的视图布局
    //2.scrollTo scrollBy,只能移动view的内容,不能移动View
    //3.实时绘制view,同样是只能移动view的内容,不能移动view.而且必须保证有一个足够大的画布
    //4.requestLayout(),实时layout控件,但是拖动的时候有抖动


    /**
     * 更新未读消息的数量
     */
    public interface OnUpdateMessageCountListner {
        /**
         * 更新未读消息的数量
         *
         * @return
         */
        public int onUpdateMessageCount();
    }

    OnUpdateMessageCountListner onUpdateMessageCountListner;

    public void setOnUpdateMessageCountListner(OnUpdateMessageCountListner onUpdateMessageCountListner) {
        this.onUpdateMessageCountListner = onUpdateMessageCountListner;
    }

    boolean isInitPosition = true; //是否正在初始化位置

    /**
     * 计算红点位置
     */
    private void computePosition() {
        computePosition(getWidth() / 2, getHeight() / 2);
    }

    private void computePosition(float x, float y) {
        initX = x; //如果从(0,0)开始,小红点会有一小部分被切掉,所以前进两个像素
        initY = y;
        if (unreadCount > 0 && unreadCount <= 9) {
            initCenterX = initX + Utils.dp2px(context, dotRedius); //红点长度为一个半径
            initCenterY = initY + Utils.dp2px(context, dotRedius);
            dotRectF.set(initX, initY, initX + Utils.dp2px(context, 2 * dotRedius), initY + Utils.dp2px(context, 2 * dotRedius));
        } else if (unreadCount > 9 && unreadCount <= 99) {
            initCenterX = initX + Utils.dp2px(context, dotRedius * 6 / 5); //红点的长度加2/5个半径
            initCenterY = initY + Utils.dp2px(context, dotRedius);
            dotRectF.set(initX, initY, initX + Utils.dp2px(context, 12 * dotRedius / 5), initY + Utils.dp2px(context, 2 * dotRedius));
            computeRedDotBezierPoint(Utils.dp2px(context, 12 * dotRedius / 5), Utils.dp2px(context, 2 * dotRedius));
        } else if (unreadCount > 99) {
            initCenterX = initX + Utils.dp2px(context, dotRedius * 3 / 2); //红点的长度加一个半径
            initCenterY = initY + Utils.dp2px(context, dotRedius);
            dotRectF.set(initX, initY, initX + Utils.dp2px(context, 3 * dotRedius), initY + Utils.dp2px(context, 2 * dotRedius));
            computeRedDotBezierPoint(Utils.dp2px(context, 3 * dotRedius), Utils.dp2px(context, 2 * dotRedius));
        }
    }

    /**
     * 计算红点的数据点和控制点
     *
     * @param width
     * @param height
     */
    private void computeRedDotBezierPoint(float width, float height) {
        //数据点
        upPointFLeft.set(initX + Utils.dp2px(context, dotRedius), initY);
        leftPointF.set(initX, initY + Utils.dp2px(context, dotRedius));
        downPointFLeft.set(initX + Utils.dp2px(context, dotRedius), initY + height);

        upPointFRight.set(initX + width - Utils.dp2px(context, dotRedius), initY);
        rightPointF.set(initX + width, initY + Utils.dp2px(context, dotRedius));
        downPointRight.set(initX + width - Utils.dp2px(context, dotRedius), initY + height);

        //控制点
        upLeftPointF.set(initX + Utils.dp2px(context, dotRedius) - mDistance, initY);
        upRightPointF.set(initX + width - Utils.dp2px(context, dotRedius) + mDistance, initY);
        downLeftPointF.set(initX + Utils.dp2px(context, dotRedius) - mDistance, initY + height);
        downRightPointF.set(initX + width - Utils.dp2px(context, dotRedius) + mDistance, initY + height);
        leftUpPointF.set(initX, initY + Utils.dp2px(context, dotRedius) - mDistance);
        leftDownPointF.set(initX, initY + Utils.dp2px(context, dotRedius) + mDistance);
        rightUpPointF.set(initX + width, initY + Utils.dp2px(context, dotRedius) - mDistance);
        rightDownPointF.set(initX + width, initY + Utils.dp2px(context, dotRedius) + mDistance);
    }

    //红点中心点坐标转换为左上角坐标
    private float centerX2StartX(float centerX) {
        return centerX - dotRectF.width() / 2;
    }

    //红点中心点坐标转换为左上角坐标
    private float centerY2StartY(float centerY) {
        return centerY - dotRectF.height() / 2;
    }

    //小红点的消失动画
    private void animationDismiss() {
        final ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.dismiss_anim);
        final AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getDrawable();
        long duration = 500;
        int width = imageView.getDrawable().getIntrinsicWidth();
        int height = imageView.getDrawable().getIntrinsicHeight();
        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.x = (int) (upRawX - width / 2);
        layoutParams.y = (int) (upRawY - height / 2);
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
            isdragable = false;
            isInPullScale = true;
            isFirstOutPullScale = true;
            isDimiss = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };
}
