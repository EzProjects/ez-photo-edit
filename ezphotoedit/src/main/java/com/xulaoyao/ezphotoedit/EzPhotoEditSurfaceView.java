package com.xulaoyao.ezphotoedit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

import com.xulaoyao.ezphotoedit.draw.EzBitmapCache;
import com.xulaoyao.ezphotoedit.listener.EzDrawListener;
import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.listener.IEzBitMapCache;
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

    private IEzBitMapCache mIEzBitmapData;
    private EzBitmapCache mEzBitmapCache;
    //private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

    private Path mCurrentPath = new Path();
    private List<EzPathInfo> mPathInfoList = new ArrayList<>();

    private int firstX, firstY;
    private static final int CLICK = 0;// 点击
    private static final int DRAG = 1;// 拖动
    private static final int ZOOM = 2;// 放大
    private int mStatus = 0;//状态
    private int mClick = 0;//状态

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

    /**
     * 加入触摸事件
     */
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        int action = event.getAction();
//        if (action == MotionEvent.ACTION_CANCEL) {
//            return false;
//        }
//        if (event.getPointerCount() < 2) {
//            float touchX = event.getRawX();
//            float touchY = event.getRawY();
//
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    setCurrentPathInfo(touchX, touchY);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    mCurrentPathInfo.pathMove(touchX, touchY);
//                    Log.d("--", "onTouchEvent x: " + touchX + " y:" + touchY + " size:" + mPathInfoList.size());
//                    break;
//                case MotionEvent.ACTION_UP:
//                    mCurrentPathInfo.pathMove(touchX, touchY);
//                    mPathInfoList.add(mCurrentPathInfo);
//                    mCurrentPathInfo = null;
//                    break;
//
//                default:
//                    break;
//            }
//        } else {
//
//        }
//        return super.onTouchEvent(event);
//    }
    @Override
    public void computeScroll() {
        //先判断mScroller滚动是否完成
        if (mScroller.computeScrollOffset() && mStatus == 1 && mClick == 1) {
            //这里调用View的scrollTo()完成实际的滚动
            PointF currentPoint = new PointF();
            currentPoint.set(mScroller.getCurrX(), mScroller.getCurrY());
            int offsetX = (int) (currentPoint.x - mStartPoint.x);
            int offsetY = (int) (currentPoint.y - mStartPoint.y);
            mStartPoint = currentPoint;
            bx += offsetX;
            by += offsetY;
            postInvalidate(); //相当于递归computeScroll();目的是触发computeScroll
        } else if (mStatus == 1 && mClick == 1) {
            mEzDrawThread.setCanPaint(false);
            Log.d("scroll", "stop"); // 暂停绘制
        }
        super.computeScroll();
    }


    // SurfaceHolder.Callback
    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mScreenWidth = this.getWidth();
        mScreenHeight = this.getHeight();
        if (mIEzBitmapData != null) setBitmapDataInit();
        startDrawThreadRun();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mEzDrawThread.setThreadRun(false);
        mEzDrawThread.interrupt();  //中断线程
        mEzDrawThread = null;
    }


    //公有方法
    public void setBitmapData(IEzBitMapCache data) {
        this.mIEzBitmapData = data;
        //重新校准位置和放大倍数
//        if (mScreenHeight > 0 && mScreenWidth > 0) setBitmapDataInit();
    }

    public void setBitmapCache(EzBitmapCache cache) {
        this.mEzBitmapCache = cache;
        Log.d("---", "setBitmapCache: cache 图片加载完成后 ");
        //this.mIEzBitmapData = cache;
        //重新校准位置和放大倍数
        if (mScreenHeight > 0 && mScreenWidth > 0) setBitmapDataInit();
    }


    //私有方法

    private void init() {
        this.setOnTouchListener(this);
        getHolder().addCallback(this);
        mSurfaceHolder = getHolder();
        mScroller = new Scroller(mContext);   // 滑动
        mPaint = new Paint();
    }

    /**
     * 开启绘制图画线程
     */
    private void startDrawThreadRun() {
        mEzDrawThread = new EzDrawThread(mSurfaceHolder);
        mEzDrawThread.setEzDrawingListener(new EzDrawListener() {
            public void onDraw(Canvas c) {
                if (c != null && mIEzBitmapData != null && mIEzBitmapData.getBgAndPathBitmap() != null) {
                    //Log.d("---ss--", " thread onDraw: ----  ");
                    c.drawColor(Color.GRAY);
                    c.scale(mScale, mScale);
                    c.drawBitmap(mIEzBitmapData.getBgAndPathBitmap(), bx / mScale, by / mScale, mPaint);
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
        float scaleWidth = mScreenWidth / mIEzBitmapData.getBgAndPathBitmap().getWidth();
        float scaleHeight = mScreenHeight / mIEzBitmapData.getBgAndPathBitmap().getHeight();
        mScale = scaleWidth > scaleHeight ? scaleHeight : scaleWidth;
        if (changeFirst) mScaleFirst = mScale;
    }

    /**
     * 刷新合成 bitmap 的数据
     */
    private void setBitmapDataInit() {
        mIEzBitmapData.setDrawRefreshListener(new EzDrawRefreshListener() {
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
        mPicWidth = mIEzBitmapData.getBgAndPathBitmap().getWidth() * mScale;
        mPicHeight = mIEzBitmapData.getBgAndPathBitmap().getHeight() * mScale;
        //初始坐标
        bx = (mScreenWidth - mPicWidth) / 2;
        by = (mScreenHeight - mPicHeight) / 2;
    }


    /**
     * 手势(放大)事件
     */
    private void zoom(MotionEvent event) {
        synchronized (EzPhotoEditSurfaceView.class) {
            float newDist = spacing(event);
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
            float fx = (event.getX(1) - event.getX(0)) / 2 + event.getX(0);//中点坐标
            float fy = (event.getY(1) - event.getY(0)) / 2 + event.getY(0);
            float XIn = fx - bx;//获得中点在图中的坐标
            float YIn = fy - by;
            XIn *= scale1;//坐标根据图片缩放而变化
            YIn *= scale1;
            bx = fx - XIn;//左上角的坐标等于中点坐标加图中偏移的坐标
            by = fy - YIn;
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
     *
     * @param event
     * @return
     */
    //获取距离运算
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // 得到当前画笔的类型，并进行实例
    public void setCurrentPathInfo(MotionEvent event) {
        float XIn = event.getX() - bx;//获得中点在图中的坐标
        float YIn = event.getY() - by;
        XIn /= mScale;//坐标根据图片缩放而变化
        YIn /= mScale;

        log(XIn, YIn);
        mCurrentPath = null;
        mCurrentPath = new Path();
        mCurrentPath.moveTo(XIn, YIn);
    }
    private void setPathMove(MotionEvent event) {
        float XIn = event.getX() - bx;//获得中点在图中的坐标
        float YIn = event.getY() - by;
        XIn /= mScale;//坐标根据图片缩放而变化
        YIn /= mScale;
        log(XIn, YIn);
        mCurrentPath.lineTo(XIn, YIn);
    }
    private void setPathInfo() {
        mPathInfoList.add(new EzPathInfo(mCurrentPath));
        //Log.d("---55---", "setPathInfo: size:" + mPathInfoList.size());
        //mIEzBitmapData.setPathInfoList(mPathInfoList);
        //mEzBitmapData.refreshData();
        mEzBitmapCache.drawPath(mPathInfoList);
    }



    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (event.getPointerCount() < 2) {
            float touchX = event.getRawX();
            float touchY = event.getRawY();
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    setCurrentPathInfo(event);
                    break;
                case MotionEvent.ACTION_MOVE:
                    setPathMove(event);
                    //Log.d("--", "onTouchEvent x: " + touchX + " y:" + touchY + " size:" + mPathInfoList.size());
                    break;
                case MotionEvent.ACTION_UP:
                    setPathMove(event);
                    setPathInfo();
                    mEzDrawThread.setCanPaint(true);
                    invalidate(); //触发computeScroll
                    break;

                default:
                    break;
            }
            return true;
        }
        if (mIEzBitmapData != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    firstX = (int) event.getX();
                    firstY = (int) event.getY();
                    mStartPoint.set(event.getX(), event.getY());
                    mStatus = DRAG;
                    mEzDrawThread.setCanPaint(true);
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    float distance = spacing(event); //初始距离
                    if (distance > 5f) {
                        mStatus = ZOOM;
                        mStartDistance = distance;
                    }
                    mEzDrawThread.setCanPaint(true);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(firstX - event.getX()) < 3 || Math.abs(firstY - event.getY()) < 3) {
                        mClick = CLICK; // 防止手滑的误差
                    } else {
                        if (mStatus == DRAG) {
                            drag(event);
                            mClick = DRAG;

                            if (mVelocityTracker == null) {
                                mVelocityTracker = VelocityTracker.obtain();
                            }
                            mVelocityTracker.addMovement(event);

                        } else {
                            if (event.getPointerCount() == 1) return true;
                            zoom(event);
                            mClick = DRAG;
                        }
                    }
                    break;
                case MotionEvent.ACTION_UP:

                    if (mClick == CLICK) { //点击图案
                        //clickMap(event);
                        mEzDrawThread.setCanPaint(false);
                    } else {
                        //获得VelocityTracker对象，并且添加滑动对象
                        int dx = 0;
                        int dy = 0;
                        if (mVelocityTracker != null) {
                            mVelocityTracker.computeCurrentVelocity(100);
                            dx = (int) (mVelocityTracker.getXVelocity() * VELOCITY_MULTI);
                            dy = (int) (mVelocityTracker.getYVelocity() * VELOCITY_MULTI);
                        }
                        mScroller.startScroll((int) mStartPoint.x, (int) mStartPoint.y, dx, dy, VELOCITY_DURATION);
                        invalidate(); //触发computeScroll
                        //回收VelocityTracker对象
                        if (mVelocityTracker != null) {
                            mVelocityTracker.clear();
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    break;
                default:
                    break;
            }
        }
        return true;
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
