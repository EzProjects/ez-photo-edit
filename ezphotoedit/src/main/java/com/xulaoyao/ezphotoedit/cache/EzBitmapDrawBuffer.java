package com.xulaoyao.ezphotoedit.cache;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.listener.IEzBitmapDrawCache;
import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.List;

/**
 * 组合图片 buffer 缓存
 * EzBitmapDrawBuffer
 * Created by renwoxing on 2018/3/18.
 */
public class EzBitmapDrawBuffer implements IEzBitmapDrawCache {

    private Bitmap mBitmap = null;
    private Bitmap mBgBitmap = null;
    private Canvas mPathCanvas = null;
    private EzDrawRefreshListener mEzDrawRefreshListener;


    private List<EzPathInfo> mEzDrawInfoList;


    public EzBitmapDrawBuffer() {
        getPaint();
    }

    public void drawBitmap(Bitmap bg) {
        if (bg != null) {
            mBgBitmap = bg;
            Log.d("--ss--", "drawBitmap: bg w:" + bg.getWidth() + " h:" + bg.getHeight());
            //根据底图申请缓冲区
            //mBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.RGB_565);//创建内存位图
            if (bg.getWidth() > 5000 || bg.getHeight() > 6000) {
                mBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.RGB_565);//创建内存位图
            } else {
                mBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.ARGB_8888);//创建内存位图
            }
            //创建空白绘图画布
            mPathCanvas = new Canvas(mBitmap);
            //底图进来后绘制到缓冲区
            //mPathCanvas.drawBitmap(bg, new Rect(0, 0, bg.getWidth(), bg.getHeight()), new Rect(0, 0, bg.getWidth(), bg.getHeight()), null);
            initBufferBitmap();
            //监听到手势 改变 图片异步更新 缩放
            //改变背景改变缩放
            if (mEzDrawRefreshListener != null) mEzDrawRefreshListener.onRefresh();
            //刷新监听器缩放
        }
    }


    public void drawPath(List<EzPathInfo> pathInfoList) {
        if (pathInfoList != null) {
            mEzDrawInfoList = pathInfoList;
        }
        if (mEzDrawInfoList != null) {
            for (EzPathInfo path : mEzDrawInfoList) {
                paint.setStrokeWidth(path.strokeWidth);
                if (mPathCanvas != null)
                    mPathCanvas.drawPath(path.path, paint);
            }
        }
        mPathCanvas.save();
        mPathCanvas.restore();
    }


    public void undoDrawPath(List<EzPathInfo> pathInfoList) {
        if (pathInfoList != null) {
            mEzDrawInfoList = pathInfoList;
        }
        if (mEzDrawInfoList != null) {
            //mBitmap.eraseColor(Color.TRANSPARENT); // // done: 2018/3/20 此方法大图时有延时 效果不理想
            initBufferBitmap();
            for (EzPathInfo path : mEzDrawInfoList) {
                paint.setStrokeWidth(path.strokeWidth);
                mPathCanvas.drawPath(path.path, paint);
            }
        }
        mPathCanvas.save();
        mPathCanvas.restore();
    }

    public void reset() {
        initBufferBitmap();
        mPathCanvas.save();
        mPathCanvas.restore();
    }

    /**
     * 设置背景图为null
     */
    public void clearBgBitmap() {
        if (mBgBitmap != null)
            this.mBgBitmap.recycle();
        this.mBgBitmap = null;
        initBufferBitmap();
    }

    /**
     * 新的图片
     * 背景图 + path 合成的图片
     *
     * @return
     */
    @Override
    public Bitmap getBgAndPathBitmap() {
        return mBitmap;
    }

    @Override
    public void setDrawRefreshListener(EzDrawRefreshListener listener) {
        this.mEzDrawRefreshListener = listener;
    }


    public void setPathInfoList(List<EzPathInfo> list) {
        this.mEzDrawInfoList = list;
    }


    public void setPathInfo(EzPathInfo info) {
        if (info != null && !this.mEzDrawInfoList.contains(info))
            this.mEzDrawInfoList.add(info);
    }


    public List<EzPathInfo> getPathInfoList() {
        return mEzDrawInfoList;
    }

    ;


    //获得画笔
    private Paint paint;//画笔属性

    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setFilterBitmap(true);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeWidth(10);
            paint.setColor(Color.RED);
        }
        return paint;
    }

    private void initBufferBitmap() {
        if (mPathCanvas != null) {
            if (mBgBitmap != null) {
                mPathCanvas.drawBitmap(mBgBitmap, new Rect(0, 0, mBgBitmap.getWidth(), mBgBitmap.getHeight()), new Rect(0, 0, mBgBitmap.getWidth(), mBgBitmap.getHeight()), null);
            } else {
                mBitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888);//创建内存位图
                //创建空白绘图画布
                mPathCanvas = new Canvas(mBitmap);
                mPathCanvas.drawColor(Color.GRAY);
            }
        }
    }

    public void destroy() {
        if (mBgBitmap != null) {
            mBgBitmap.recycle();
            mBgBitmap = null;
        }
        if (mPathCanvas != null) {
            mPathCanvas = null;
        }
        if (mEzDrawInfoList != null) {
            mEzDrawInfoList.clear();
            mEzDrawInfoList = null;
        }
        if (mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }
    }

}
