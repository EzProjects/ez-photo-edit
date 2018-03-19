package com.xulaoyao.ezphotoedit.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.listener.IEzBitmapDraw;
import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.List;

/**
 * EzBitmapDraw
 * Created by renwoxing on 2018/3/18.
 */
public class EzBitmapDraw implements IEzBitmapDraw {

    private Bitmap mBitmap = null;
    private Canvas mPathCanvas = null;
    private EzDrawRefreshListener mEzDrawRefreshListener;


    private List<EzPathInfo> mEzDrawInfoList;


    public EzBitmapDraw() {

    }

    public void drawBitmap(Bitmap bg) {
        if (bg != null) {
            //Log.d("--ss--", "drawBitmap: bg w:" + bg.getWidth() + " h:" + bg.getHeight());
            //根据底图申请缓冲区
            mBitmap = Bitmap.createBitmap(bg.getWidth(), bg.getHeight(), Bitmap.Config.RGB_565);//创建内存位图
            //创建空白绘图画布
            mPathCanvas = new Canvas(mBitmap);
            //底图进来后绘制到缓冲区
            mPathCanvas.drawBitmap(bg, new Rect(0, 0, bg.getWidth(), bg.getHeight()), new Rect(0, 0, bg.getWidth(), bg.getHeight()), null);

            //监听到手势 改变 图片异步更新 缩放
            //改变背景改变缩放
            if (mEzDrawRefreshListener != null) mEzDrawRefreshListener.onRefresh();
            //刷新监听器缩放
        }
    }



    //@Override
    public void drawPath(List<EzPathInfo> pathInfoList) {
        if (pathInfoList != null) {
            mEzDrawInfoList = pathInfoList;
        }
        if (mEzDrawInfoList != null) {
            for (EzPathInfo path : mEzDrawInfoList) {
                mPathCanvas.drawPath(path.getPath(), getPaint());
            }
        }
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

    //@Override
    public void setPathInfoList(List<EzPathInfo> list) {
        this.mEzDrawInfoList = list;
    }

    //@Override
    public void setPathInfo(EzPathInfo info) {
        if (info != null && !this.mEzDrawInfoList.contains(info))
            this.mEzDrawInfoList.add(info);
    }

    //public abstract Bitmap getBgBitmap();

    public List<EzPathInfo> getPathInfoList() {
        return mEzDrawInfoList;
    }

    ;


    //获得画笔
    private Paint paint;//画笔属性

    private Paint getPaint() {
        if (paint == null) {
            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStrokeWidth(5);
            paint.setStyle(Paint.Style.STROKE);
            //paint.setStyle(Paint.Style.FILL);
            //paint.setAlpha(30);
        }
        return paint;
    }


    public void destroy() {
        if (mPathCanvas != null)
            mPathCanvas = null;
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
