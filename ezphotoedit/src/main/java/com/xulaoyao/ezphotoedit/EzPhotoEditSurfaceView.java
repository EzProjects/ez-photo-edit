package com.xulaoyao.ezphotoedit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import com.xulaoyao.ezphotoedit.draw.EzBitmapDraw;
import com.xulaoyao.ezphotoedit.listener.EzDrawListener;
import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * EzPhotoEditSurfaceView
 * Created by renwoxing on 2018/3/18.
 */
public class EzPhotoEditSurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private static final float VELOCITY_MULTI = 1f;// 滑动速度加权，计算松手后移动距离
    private static final int VELOCITY_DURATION = 600;// 缓动持续时间
    private static final int GESTURE_DETECTOR_CLICK = 0;  // 点击
    private static final int GESTURE_DETECTOR_DRAG = 1;   // 拖动
    private static final int GESTURE_DETECTOR_ZOOM = 2;   // 放大
    private static final int GESTURE_DETECTOR_PATH = 3;   // 涂鸦

    //触摸移动开始绘制 阀值
    private static final float TOUCH_TOLERANCE = 4;

    private EzDrawThread mEzDrawThread;//绘制线程
    private SurfaceHolder mSurfaceHolder;

    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private Paint mPaint;
    private Context mContext;

    private float mStartDistance; //初始距离
    private float mPicWidth, mPicHeight; //图案宽度,图案高度,实时状态
    private float mScreenWidth, mScreenHeight;
    private PointF mStartPoint = new PointF(); //下手点
    private float mScale, mScaleFirst; //放大倍数
    private float bx, by; //图案初始坐标

    private EzBitmapDraw mEzBitmapCache;


    private EzPathInfo mEzPathInfo;
    private List<EzPathInfo> mPathInfoList = new ArrayList<>();

    private int firstX, firstY;


    private int mStatus = 0;//状态
    private int mClick = 0;//状态

    private boolean isEdit = false;   //编辑状态 还是 可视状态


    public EzPhotoEditSurfaceView(Context context) {
        this(context, null);
    }

    public EzPhotoEditSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EzPhotoEditSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEzBitmapCache != null)
            mEzBitmapCache.destroy();
    }

    @Override
    public void computeScroll() {
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset() && mStatus == GESTURE_DETECTOR_DRAG && mClick == GESTURE_DETECTOR_DRAG) {
            //这里调用View的scrollTo()完成实际的滚动
            PointF currentPoint = new PointF();
            currentPoint.set(mScroller.getCurrX(), mScroller.getCurrY());
            int offsetX = (int) (currentPoint.x - mStartPoint.x);
            int offsetY = (int) (currentPoint.y - mStartPoint.y);
            mStartPoint = currentPoint;
            bx += offsetX;
            by += offsetY;
            postInvalidate(); //相当于递归computeScroll();目的是触发computeScroll
        } else if (mStatus == GESTURE_DETECTOR_DRAG && mClick == GESTURE_DETECTOR_DRAG) {
            mEzDrawThread.setCanPaint(false);
        }
        super.computeScroll();
    }


    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mScreenWidth = this.getWidth();
        mScreenHeight = this.getHeight();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mPathInfoList != null) {
            mPathInfoList.clear();
            mPathInfoList = null;
        }
        if (mEzBitmapCache != null)
            mEzBitmapCache.destroy();

        if (mVelocityTracker != null) {
            mVelocityTracker.clear();
            mVelocityTracker.recycle();
        }

        mEzDrawThread.setThreadRun(false);
        mEzDrawThread.interrupt();  //中断线程
        mEzDrawThread = null;
    }


    //公有方法
    public void load(Bitmap bgBitmap) {
        if (isEdit) {
            return;
        }
        resetClear();
        this.mEzBitmapCache.drawBitmap(bgBitmap);
        //重新校准位置和放大倍数
        setScale(true);
        setPicInit();
        //绘制 防止不正常显示
        mEzDrawThread.setCanPaint(true);
    }


    /**
     * 撤销
     */
    public void undo() {
        if (!isEdit)
            return;
        if (mPathInfoList != null && mPathInfoList.size() > 0) {
            mPathInfoList.remove(mPathInfoList.size() - 1);
            Log.d("--s-0-", "undo:  size:" + mPathInfoList.size());
            //mEzBitmapCache.setPathInfoList(mPathInfoList);
            mEzDrawThread.setCanPaint(true);
            mEzBitmapCache.undoDrawPath(mPathInfoList);
            postInvalidate();
        }
    }

    /**
     * 清屏
     */
    public void clear() {
        if (isEdit) {
            resetClear();
            this.mEzBitmapCache.reset();
            //重新校准位置和放大倍数
            setScale(true);
            setPicInit();
            //绘制 防止不正常显示
            mEzDrawThread.setCanPaint(true);
        }
    }

    /**
     * 设置为 编辑状态或可视状态
     * 编辑状态下 双指移动 单指绘画
     * 可视状态 双指缩放 单指移动
     *
     * @param edit
     */
    public void setEdit(boolean edit) {
        isEdit = edit;
    }


    //私有方法

    private void init() {
        this.setOnTouchListener(this);
        getHolder().addCallback(this);
        mSurfaceHolder = getHolder();
        mScroller = new Scroller(mContext);   // 滑动
        mPaint = new Paint();
        mEzBitmapCache = new EzBitmapDraw();
        //注册刷新回调
        registerBitmapRefreshListener();
        startDrawThreadRun();
    }

    /**
     * 开启绘制图画线程
     */
    private void startDrawThreadRun() {
        mEzDrawThread = new EzDrawThread(mSurfaceHolder);
        mEzDrawThread.setEzDrawingListener(new EzDrawListener() {
            public void onDraw(Canvas c) {
                if (c != null && mEzBitmapCache != null && mEzBitmapCache.getBgAndPathBitmap() != null) {
                    //Log.d("---ss--", " thread onDraw: ----  ");
                    c.drawColor(Color.GRAY);
                    c.scale(mScale, mScale);
                    c.drawBitmap(mEzBitmapCache.getBgAndPathBitmap(), bx / mScale, by / mScale, mPaint);
                }
            }
        });
        mEzDrawThread.start();
    }


    /**
     * 缩放比例设置
     *
     * @param changeFirst
     */
    private void setScale(boolean changeFirst) {
        float scaleWidth = mScreenWidth / mEzBitmapCache.getBgAndPathBitmap().getWidth();
        float scaleHeight = mScreenHeight / mEzBitmapCache.getBgAndPathBitmap().getHeight();
        mScale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
        if (changeFirst) mScaleFirst = mScale;
    }

    /**
     * 刷新合成 bitmap 的数据
     */
    private void registerBitmapRefreshListener() {
        mEzBitmapCache.setDrawRefreshListener(new EzDrawRefreshListener() {
            @Override
            public void onRefresh() {
                setScale(true);
                setPicInit();
            }
        });
    }

    private void setPicInit() {
        if (bx != 0 && by != 0) return;
        //图片初始状态
        mPicWidth = mEzBitmapCache.getBgAndPathBitmap().getWidth() * mScale;
        mPicHeight = mEzBitmapCache.getBgAndPathBitmap().getHeight() * mScale;
        //初始坐标
        bx = (mScreenWidth - mPicWidth) / 2;
        by = (mScreenHeight - mPicHeight) / 2;
    }


    /**
     * 手势(放大)事件
     */
    private void zoom(MotionEvent event) {
        synchronized (EzPhotoEditSurfaceView.class) {
            float newDist = distanceBetweenFingers(event);
            float scale1 = newDist / mStartDistance;
            mStartDistance = newDist;
            float tmp = mScale * scale1;//缩放了比例值
            if (tmp < mScaleFirst * 10 && tmp > mScaleFirst * 0.6) {//放大的倍数范围
                mScale = tmp;
            } else {
                return;
            }
            mPicHeight *= scale1;//缩放了高宽
            mPicWidth *= scale1;
            float fx = 0f, fy = 0f;
            if (event.getPointerCount() > 1) {
                fx = (event.getX(1) - event.getX(0)) / 2 + event.getX(0);//中点坐标
                fy = (event.getY(1) - event.getY(0)) / 2 + event.getY(0);
            }
            float inBgBitmapX = fx - bx;//获得中点在图中的坐标
            float inBgBitmapY = fy - by;
            inBgBitmapX *= scale1;//坐标根据图片缩放而变化
            inBgBitmapY *= scale1;
            bx = fx - inBgBitmapX;//左上角的坐标等于中点坐标加图中偏移的坐标
            by = fy - inBgBitmapY;
        }
    }

    /**
     * 手势(拖动)事件
     */
    private void drag(MotionEvent event) {
        synchronized (EzPhotoEditSurfaceView.class) {
            PointF currentPoint = new PointF();
            currentPoint.set(event.getX(), event.getY());
            int offsetX = (int) (currentPoint.x - mStartPoint.x);
            int offsetY = (int) (currentPoint.y - mStartPoint.y);
            mStartPoint = currentPoint;
            bx += offsetX;
            by += offsetY;
        }
    }


    /**
     * 双指中心
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return
     */
    private float distanceBetweenFingers(MotionEvent event) {
        float x = 0f, y = 0f;
        if (event.getPointerCount() > 1) {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 获取屏幕座标在原图（原始大小）上的点座标
     *
     * @param event
     * @return
     */
    public PointF getMotionEventPointInBgBitmapPointF(MotionEvent event) {
        float inBgBitmapX = event.getX() - bx;//获得中点在图中的坐标
        float inBgBitmapY = event.getY() - by;
        inBgBitmapX /= mScale;//坐标根据图片缩放而变化
        inBgBitmapY /= mScale;
        return new PointF(inBgBitmapX, inBgBitmapY);
    }

    // 得到当前画笔的类型，并进行实例
    public void setCurrentPathInfo(MotionEvent event) {
        PointF pointF = getMotionEventPointInBgBitmapPointF(event);
        //log(pointF.x, pointF.y);
        if (mEzPathInfo != null)
            mEzPathInfo.clear();
        mEzPathInfo = null;
        mEzPathInfo = new EzPathInfo();
        mEzPathInfo.setScale(mScale);
        mPathInfoList.add(mEzPathInfo);
        mEzPathInfo.getPath().moveTo(pointF.x, pointF.y);
    }

    private void setPathMove(MotionEvent event, float velocity) {
        PointF pointF = getMotionEventPointInBgBitmapPointF(event);
        //log(pointF.x, pointF.y);
        if (mEzPathInfo != null) {
            mEzPathInfo.setVelocity(velocity);
            mEzPathInfo.addPoint(pointF);
            setPathInfo();
        }
    }

    private void setPathInfo() {
        mEzBitmapCache.drawPath(mPathInfoList);
    }


    @Override
    public boolean onTouch(View view, MotionEvent event) {
        //此处是批改状态
        if (mEzBitmapCache != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    firstX = (int) event.getX();
                    firstY = (int) event.getY();
                    mStartPoint.set(event.getX(), event.getY());
                    mStatus = GESTURE_DETECTOR_DRAG;   //绘制路径
                    if (isEdit) {
                        mStatus = GESTURE_DETECTOR_PATH;   //绘制路径
                        setCurrentPathInfo(event);
                    }
                    mEzDrawThread.setCanPaint(true);
                    Log.d("--", "11111 onTouch: ACTION_DOWN --- x:" + firstX + " y: " + firstY);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    //多指按下
                    float distance = distanceBetweenFingers(event); //初始距离
                    //只有两只之间的距离大于20像素的是时候算是多点的触摸
                    if (distance > 5f) {
                        mStatus = GESTURE_DETECTOR_ZOOM;
                        mStartDistance = distance;
                    }
                    mEzDrawThread.setCanPaint(true);
                    Log.d("--", "2222222222 onTouch: ACTION_POINTER_DOWN --- x:" + event.getX() + "y: " + event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(firstX - event.getX()) < 1 || Math.abs(firstY - event.getY()) < 1) {
                        mClick = GESTURE_DETECTOR_CLICK; // 防止手滑的误差
                    } else {
                        float distanceMove = distanceBetweenFingers(event);
                        if (event.getPointerCount() > 1 && Math.abs(distanceMove - mStartDistance) < 1f) {
                            mStatus = GESTURE_DETECTOR_DRAG;
                        }
                        if (mStatus == GESTURE_DETECTOR_DRAG) {
                            drag(event);
                            mClick = GESTURE_DETECTOR_DRAG;
                            if (mVelocityTracker == null) {
                                mVelocityTracker = VelocityTracker.obtain();
                            }
                            mVelocityTracker.addMovement(event);

                        } else {
                            if (event.getPointerCount() == 1) {
                                if (mStatus == GESTURE_DETECTOR_PATH && isEdit) {
                                    if (event.getPointerCount() == 1) {
                                        if (Math.abs(firstX - event.getX()) >= TOUCH_TOLERANCE || Math.abs(firstY - event.getY()) >= TOUCH_TOLERANCE) {
                                            setPathMove(event, 0);
                                            mEzDrawThread.setCanPaint(true);
                                        }
                                        return true;
                                    }
                                }
                                return false;
                            }
                            zoom(event);
                            mClick = GESTURE_DETECTOR_DRAG;
                        }

                    }

                    Log.d("--", "333333333333333 onTouch: ACTION_MOVE --- x:" + event.getX() + "y: " + event.getY() + " status:" + mStatus);
                    break;
                case MotionEvent.ACTION_UP:
                    if (mStatus == GESTURE_DETECTOR_PATH && isEdit) {
                        setPathMove(event, 0);
                        setPathInfo();
                        mEzDrawThread.setCanPaint(true);
                        return true;
                    }
                    if (mClick == GESTURE_DETECTOR_CLICK) { //点击图案
                        //clickMap(event);
                        mEzDrawThread.setCanPaint(false);
                    } else {
                        //滑动速度
                        //获得VelocityTracker对象，并且添加滑动对象
                        int dx = 0;
                        int dy = 0;
                        if (mVelocityTracker != null) {
                            mVelocityTracker.computeCurrentVelocity(100);
                            dx = (int) (mVelocityTracker.getXVelocity() * VELOCITY_MULTI);
                            dy = (int) (mVelocityTracker.getYVelocity() * VELOCITY_MULTI);
                        }
                        mScroller.startScroll((int) mStartPoint.x, (int) mStartPoint.y, dx, dy, VELOCITY_DURATION);
                        postInvalidate(); //触发computeScroll
                        //回收VelocityTracker对象
                        if (mVelocityTracker != null) {
                            mVelocityTracker.clear();
                        }
                    }
                    mStartDistance = 0;
                    Log.d("--", "444444444444 onTouch: ACTION_UP --- x:" + event.getX() + "y: " + event.getY());
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    //多指离开
                    mStartDistance = 0;
                    Log.d("--", "55555 onTouch: ACTION_POINTER_UP --- x:" + event.getX() + "y: " + event.getY());
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    private void resetClear() {
        mEzDrawThread.setCanPaint(false);
        //mEzDrawThread.setThreadRun(false);
        //mEzDrawThread.interrupt();
        // mEzDrawThread = null;
//        if (mEzBitmapCache != null)
//            mEzBitmapCache.destroy();
        if (mEzPathInfo != null)
            mEzPathInfo.clear();
//        if (mIEzBitmapData != null)
//            mIEzBitmapData = null;
        isEdit = false;
        mStatus = 0;
        mClick = 0;
        firstX = 0;
        firstY = 0;
        mScale = 1;
        mScaleFirst = 1;
        mStartPoint.set(0, 0);
        bx = 0;
        by = 0;
        mPicWidth = 0;
        mPicHeight = 0;
        if (mPathInfoList != null && mPathInfoList.size() > 0)
            mPathInfoList.clear();
    }

    private void log(float x, float y) {
        Log.d("-log-", "--- log: getScrollX:" + getScrollX());
        Log.d("-log-", "--- log: getScrollY:" + getScrollY());
        Log.d("-log-", "--- log: x:" + x);
        Log.d("-log-", "--- log: y:" + y);
        Log.d("-log-", "--- screen: width:" + mScreenWidth + " height:" + mScreenHeight);
        Log.d("-log-", "--- image: width:" + mPicWidth + " height:" + mPicHeight);
        Log.d("-log-", "--- 偏移: by:" + bx + " by:" + by);
    }

}
