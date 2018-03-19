package com.xulaoyao.ezphotoedit.draw;

import android.graphics.Bitmap;

import com.xulaoyao.ezphotoedit.model.EzDrawInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * EzBitmapData
 * Created by renwoxing on 2018/3/18.
 */
public class EzBitmapData extends EzBitmapCache {

    private Bitmap mBgBitmap;
    private List<EzDrawInfo> mEzDrawInfoList = new ArrayList<>();

    public EzBitmapData() {
    }

    public EzBitmapData(List<EzDrawInfo> mEzDrawInfoList) {
        this.mEzDrawInfoList = mEzDrawInfoList;
        drawBitmap(this);
        drawPath(this);
    }

    public EzBitmapData(Bitmap mBgBitmap, List<EzDrawInfo> mEzDrawInfoList) {
        this.mBgBitmap = mBgBitmap;
        this.mEzDrawInfoList = mEzDrawInfoList;
        drawBitmap(this);
        drawPath(this);
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
    public List<EzDrawInfo> getPathInfoList() {
        if (mEzDrawInfoList == null)
            return new ArrayList<>();
        else
            return mEzDrawInfoList;
    }

    @Override
    public void setPathInfoList(List<EzDrawInfo> list) {
        this.mEzDrawInfoList = mEzDrawInfoList;
    }

    @Override
    public void setPathInfo(EzDrawInfo info) {
        if (info != null && !this.mEzDrawInfoList.contains(info))
            this.mEzDrawInfoList.add(info);
    }

    @Override
    public Bitmap getBgBitmap() {
        return this.mBgBitmap;
    }


    //公有方法
    public void refreshData() {
        drawBitmap(this);
        drawPath(this);
        if (!mBgBitmap.isRecycled()) {
            mBgBitmap.recycle();
        }
        this.mBgBitmap = null;
    }
}
