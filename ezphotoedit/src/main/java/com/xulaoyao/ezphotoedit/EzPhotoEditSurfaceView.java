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

import com.xulaoyao.ezphotoedit.cache.EzBitmapDrawBuffer;
import com.xulaoyao.ezphotoedit.listener.EzDrawListener;
import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.listener.LoadBitmapWorkerListener;
import com.xulaoyao.ezphotoedit.listener.PhotoEditListener;
import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 涂鸦 SurfaceView
 * 分为： 编辑状态或可视状态
 * 编辑状态下：单指绘画，双指移动
 * 可视状态下：单指移动，双指缩放 , 三指左右快速滑动 fling 翻页
 * <p>
 * 可撤销，清屏
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
    private static final float TOUCH_ZOOM_TOLERANCE = 1f;

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

    private EzBitmapDrawBuffer mEzBitmapCache;

    private EzPathInfo mEzPathInfo;
    private List<EzPathInfo> mPathInfoList = new ArrayList<>();

    private int firstX, firstY;


    private int mStatus = 0;//状态
    private int mClick = 0;//状态

    private boolean isEdit = false;   //编辑状态 还是 可视状态
    private boolean isMultiPointerToOneUp = true;   //多点触摸单指抬起后的标记
    private long lastMultiPointerTime;    // 记录多点触控的时间
    private boolean isFlingPage = false;  //翻页状态

    private PhotoEditListener mPhotoEditListener;

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
            if (mPhotoEditListener != null) {
                mPhotoEditListener.info(503, "编辑状态下不能载入图片！");
            }
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

    public void load(final String path) {
        if (null != path) {
            new EzLoadBitmapWorkerTask(new LoadBitmapWorkerListener() {
                @Override
                public void onPostExecute(Bitmap bgBitmap) {
                    if (bgBitmap != null) {
                        load(bgBitmap);
                        if (mPhotoEditListener != null) {
                            mPhotoEditListener.info(200, "加载完成");
                        }
                    } else {
                        if (mPhotoEditListener != null) {
                            mPhotoEditListener.info(400, "加载失败");
                        }
                    }
                    Log.d("- work -", "onPostExecute: ------ bitmap: 返回");
                }
            }).execute(path);
        }
    }


    /**
     * 撤销
     */
    public void undo() {
        if (!isEdit) {
            if (mPhotoEditListener != null) {
                mPhotoEditListener.info(502, "可视状态下不能使用撤销功能！");
            }
            return;
        }
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
            mPathInfoList.clear();
            mEzBitmapCache.reset();
            mEzDrawThread.setCanPaint(true);
        } else {
            if (mPhotoEditListener != null) {
                mPhotoEditListener.info(501, "可视状态下不能清除！");
            }
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

    public boolean isIsHandWriting() {
        return mEzPathInfo != null && mEzPathInfo.mPoints != null;
    }


    /**
     * 设置监听
     *
     * @param photoEditListener
     */
    public void setPhotoEditListener(PhotoEditListener photoEditListener) {
        this.mPhotoEditListener = photoEditListener;
    }

    //私有方法

    private void init() {
        this.setOnTouchListener(this);
        getHolder().addCallback(this);
        mSurfaceHolder = getHolder();
        mScroller = new Scroller(mContext);   // 滑动
        mPaint = new Paint();
        mEzBitmapCache = new EzBitmapDrawBuffer();
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
                    c.drawColor(Color.GRAY);
                    c.scale(mScale, mScale);
                    try {
                        if (mEzBitmapCache.getBgAndPathBitmap() != null)
                            c.drawBitmap(mEzBitmapCache.getBgAndPathBitmap(), bx / mScale, by / mScale, mPaint);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        mEzDrawThread.start();
        //暂时不运行，init 时bg bitmap
        // 还未加载完成 load 完成后 设置为 true
        mEzDrawThread.setCanPaint(false);
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
            if (tmp < mScaleFirst * 5 && tmp > mScaleFirst * 0.6) {//放大的倍数范围
                mScale = tmp;
            } else {
                return;
            }
            mPicHeight *= scale1;//缩放了高宽
            mPicWidth *= scale1;
            float fx = 0f, fy = 0f;
            if (event.getPointerCount() > 1) {
                //fx = (event.getX(1) - event.getX(0)) / 2 + event.getX(0);//中点坐标
                //fy = (event.getY(1) - event.getY(0)) / 2 + event.getY(0);
                fx = centerBetweenFingers(event).x;
                fy = centerBetweenFingers(event).y;
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
            //currentPoint.set(event.getX(), event.getY());
            currentPoint.set(event.getRawX(), event.getRawY());
            int offsetX = (int) (currentPoint.x - mStartPoint.x);
            int offsetY = (int) (currentPoint.y - mStartPoint.y);
            mStartPoint = currentPoint;
            bx += offsetX;
            by += offsetY;
        }
    }


    /**
     * 计算两个手指之间的距离。
     *
     * @param event
     * @return
     */
    private float distanceBetweenFingers(MotionEvent event) {
        float x = event.getX(), y = event.getY();
        if (event.getPointerCount() > 1) {
            x = event.getX(0) - event.getX(1);
            y = event.getY(0) - event.getY(1);
        }
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * 计算两个手指间的中间点
     **/
    private PointF centerBetweenFingers(MotionEvent event) {
        float midX = event.getRawX(), midY = event.getRawY();
        if (event.getPointerCount() > 1) {
            midX = (event.getX(1) + event.getX(0)) / 2;
            midY = (event.getY(1) + event.getY(0)) / 2;
        }
        return new PointF(midX, midY);
    }

    /**
     * 获取屏幕座标在原图（原始大小）上的点座标
     *
     * @param event
     * @return
     */
    public PointF getMotionEventPointInBgBitmapPointF(MotionEvent event) {
        float inBgBitmapX = event.getRawX() - bx;//获得中点在图中的坐标
        float inBgBitmapY = event.getRawY() - by;
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
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
        mVelocityTracker.addMovement(event);
        //此处是批改状态
        if (mEzBitmapCache != null) {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    firstX = (int) centerBetweenFingers(event).x;   //判断点击
                    firstY = (int) centerBetweenFingers(event).y;
                    mStatus = GESTURE_DETECTOR_DRAG;   //绘制路径
                    mClick = GESTURE_DETECTOR_CLICK;
                    if (event.getPointerCount() > 1)
                        isMultiPointerToOneUp = false;
                    if (isEdit) {
                        mStatus = GESTURE_DETECTOR_PATH;   //绘制路径
                        mClick = mStatus;
                        setCurrentPathInfo(event);
                    } else {
                        //非编辑状态下记录此值
                        mStartPoint.set(centerBetweenFingers(event).x, centerBetweenFingers(event).y);
                    }
                    mEzDrawThread.setCanPaint(true);
                    //Log.d("--", "11111 onTouch: ACTION_DOWN --- x:" + event.getRawX() + " y: " + event.getRawY());
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    isFlingPage = false;
                    mStatus = GESTURE_DETECTOR_ZOOM;   //绘制路径
                    mClick = mStatus;
                    isMultiPointerToOneUp = false;
                    //多指按下
                    mStartDistance = distanceBetweenFingers(event); //初始距离
                    mEzDrawThread.setCanPaint(true);
                    //Log.d("--", "2222222222 onTouch: ACTION_POINTER_DOWN --- x:" + event.getX() + "y: " + event.getY());
                    break;
                case MotionEvent.ACTION_MOVE:
                    //滑动速度
                    //获得VelocityTracker对象，并且添加滑动对象
                    //值为1时：代表每毫秒运动一个像素，px/ms
                    //值为1000时：代表每秒运动1000个像素，1000px/s
                    int xVelocity = 0;
                    int yVelocity = 0;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.computeCurrentVelocity(1000);
                        xVelocity = (int) (mVelocityTracker.getXVelocity() * VELOCITY_MULTI);
                        yVelocity = (int) (mVelocityTracker.getYVelocity() * VELOCITY_MULTI);
                    }
                    log(xVelocity, yVelocity);
                    if (!isEdit && event.getPointerCount() > 2) {
                        //翻页
                        if (xVelocity > 3000) {
                            if (mPhotoEditListener != null && !isFlingPage) {
                                mPhotoEditListener.next();
                                isFlingPage = true;
                            }
                            return true;
                        }
                        if (xVelocity < -300) {
                            if (mPhotoEditListener != null && !isFlingPage) {
                                isFlingPage = true;
                                mPhotoEditListener.previous();
                            }
                            return true;
                        }
                    }
                    if (!isEdit && isFlingPage) {
                        return true;
                    }
                    if (Math.abs(firstX - centerBetweenFingers(event).x) < 3 || Math.abs(firstY - centerBetweenFingers(event).y) < 3) {
                        mClick = GESTURE_DETECTOR_CLICK; // 防止手滑的误差
                    } else {
                        float lastDistance = distanceBetweenFingers(event); //现在手指间距离
                        //Log.d("---", " ---- --- &****** onTouch: Math.abs(lastDistance - mStartDistance) :" + Math.abs(lastDistance - mStartDistance));
                        //只有两只之间的距离大于20像素的是时候算是多点的触摸
                        if (event.getPointerCount() > 1) {
                            if (Math.abs(lastDistance - mStartDistance) < TOUCH_ZOOM_TOLERANCE &&
                                    mStatus == GESTURE_DETECTOR_ZOOM) {
                                mStatus = GESTURE_DETECTOR_DRAG;
                            }
                            if (Math.abs(lastDistance - mStartDistance) > TOUCH_ZOOM_TOLERANCE &&
                                    mStatus == GESTURE_DETECTOR_DRAG) {
                                mStatus = GESTURE_DETECTOR_ZOOM;
                            }
                        }
                        //抖动问题  加入时间来判断。
                        if (mStatus == GESTURE_DETECTOR_DRAG) {
                            drag(event);
                            mClick = GESTURE_DETECTOR_DRAG;
                        } else {
                            //多指 需要延时来判断误触
                            if (event.getPointerCount() == 1 && isMultiPointerToOneUp && System.currentTimeMillis() - lastMultiPointerTime > 200) {
                                if (mStatus == GESTURE_DETECTOR_PATH && isEdit) {
                                    if (event.getPointerCount() == 1) {
                                        if (Math.abs(firstX - centerBetweenFingers(event).x) >= TOUCH_TOLERANCE || Math.abs(firstY - centerBetweenFingers(event).y) >= TOUCH_TOLERANCE) {
                                            setPathMove(event, 0);
                                            mEzDrawThread.setCanPaint(true);
                                        }
                                        return true;
                                    }
                                }
                            }
                            if (mStatus == GESTURE_DETECTOR_ZOOM) {
                                zoom(event);
                                mClick = GESTURE_DETECTOR_DRAG;
                            }
                        }
                    }
                    //Log.d("--", "333333333333333 onTouch: ACTION_MOVE --- x:" + event.getRawX() + "y: " + event.getRawY() + " status:" + mStatus + " click:" + mClick);
                    break;
                case MotionEvent.ACTION_UP:
                    isFlingPage = false;
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
                        if (mStatus == GESTURE_DETECTOR_DRAG) {
                            //滑动速度
                            //获得VelocityTracker对象，并且添加滑动对象
                            int dx = 0;
                            int dy = 0;
                            if (mVelocityTracker != null) {
                                mVelocityTracker.computeCurrentVelocity(1000);
                                dx = (int) (mVelocityTracker.getXVelocity() * VELOCITY_MULTI);
                                dy = (int) (mVelocityTracker.getYVelocity() * VELOCITY_MULTI);
                            }
                            //Log.d("--mVelocityTracker ", " dx , dy -> ");
                            //log(dx, dy);
                            mScroller.abortAnimation();
                            mScroller.startScroll((int) mStartPoint.x, (int) mStartPoint.y, dx, dy, VELOCITY_DURATION);
                            postInvalidate(); //触发computeScroll
                        }
                    }
                    mStartDistance = 0;
                    //Log.d("--", "444444444444 onTouch: ACTION_UP --- x:" + event.getRawX() + "y: " + event.getRawY());
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    isMultiPointerToOneUp = true;  //单指模式
                    lastMultiPointerTime = System.currentTimeMillis();
                    if (isEdit) {
                        mStatus = GESTURE_DETECTOR_PATH;
                    } else {
                        mStatus = GESTURE_DETECTOR_DRAG;
                    }
                    //多指中一指收起
                    //mStartDistance = 0;
                    //Log.d("--", "55555 onTouch: ACTION_POINTER_UP --- x:" + event.getX() + "y: " + event.getY());
                    break;

                case MotionEvent.ACTION_CANCEL:
                    mVelocityTracker.recycle();
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
        Log.d("-log-", "--- 偏移: bx:" + bx + " by:" + by);
    }

}
