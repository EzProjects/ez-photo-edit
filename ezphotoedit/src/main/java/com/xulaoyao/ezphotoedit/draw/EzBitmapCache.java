package com.xulaoyao.ezphotoedit.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;

import com.xulaoyao.ezphotoedit.listener.IEzBitMapCache;
import com.xulaoyao.ezphotoedit.listener.EzDrawRefreshListener;
import com.xulaoyao.ezphotoedit.model.EzDrawInfo;

import java.util.List;

/**
 * EzBitmapCache
 * Created by renwoxing on 2018/3/18.
 */
public abstract class EzBitmapCache implements IEzBitMapCache {

    private Bitmap mBitmap = null;
    private Canvas mPathCanvas = null;
    private EzDrawRefreshListener mEzDrawRefreshListener;


    private List<EzDrawInfo> mEzDrawInfoList;


    public EzBitmapCache() {

    }

    public void drawBitmap(EzBitmapCache child) {
        Bitmap bg = child.getBgBitmap();
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

    public void drawPath(EzBitmapCache child) {
        Log.d("=-=-=-", "drawPath: ");
        if (mEzDrawInfoList == null) {
            mEzDrawInfoList = child.getPathInfoList();
        }
        if (mEzDrawInfoList != null) {
            Log.d("--", "drawPath  size:" + mEzDrawInfoList.size());
            for (EzDrawInfo path : mEzDrawInfoList) {
                path.draw(mPathCanvas);
            }
        }
        mPathCanvas = null;
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

    @Override
    public void setPathInfoList(List<EzDrawInfo> list) {
        this.mEzDrawInfoList = list;
    }

    @Override
    public void setPathInfo(EzDrawInfo info) {
        if (info != null && !this.mEzDrawInfoList.contains(info))
            this.mEzDrawInfoList.add(info);
    }

    public abstract Bitmap getBgBitmap();

    public abstract List<EzDrawInfo> getPathInfoList();

}
