package com.xulaoyao.ezphotoedit.draw;

import android.graphics.Bitmap;

import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * EzBitmapData
 * Created by renwoxing on 2018/3/18.
 */
@Deprecated
public class EzBitmapData extends EzBitmapCache {

    private Bitmap mBgBitmap;
    private List<EzPathInfo> mEzDrawInfoList = new ArrayList<>();

    public EzBitmapData() {
    }

    public EzBitmapData(List<EzPathInfo> mEzDrawInfoList) {
        this.mEzDrawInfoList = mEzDrawInfoList;
        //drawBitmap(this);
        //drawPath(this);
    }

    public EzBitmapData(Bitmap mBgBitmap, List<EzPathInfo> mEzDrawInfoList) {
        this.mBgBitmap = mBgBitmap;
        this.mEzDrawInfoList = mEzDrawInfoList;
        //drawBitmap(this);
        //drawPath(this);
    }

    public void setBgBitmap(Bitmap bmp) {
        this.mBgBitmap = bmp;
    }


    /**
     * 获取 path list
     *
     * @return
     */
    @Override
    public List<EzPathInfo> getPathInfoList() {
        if (mEzDrawInfoList == null)
            return new ArrayList<>();
        else
            return mEzDrawInfoList;
    }

    @Override
    public void setPathInfoList(List<EzPathInfo> list) {
        this.mEzDrawInfoList = list;
    }

    @Override
    public void setPathInfo(EzPathInfo info) {
        if (info != null && !this.mEzDrawInfoList.contains(info))
            this.mEzDrawInfoList.add(info);
    }

//    @Override
//    public Bitmap getBgBitmap() {
//        return this.mBgBitmap;
//    }


    //公有方法
    public void refreshData() {
        //drawBitmap(this);
        //drawPath(this);
        if (mBgBitmap != null) {
            if (!mBgBitmap.isRecycled()) {
                mBgBitmap.recycle();
            }
            this.mBgBitmap = null;
        }
    }
}
