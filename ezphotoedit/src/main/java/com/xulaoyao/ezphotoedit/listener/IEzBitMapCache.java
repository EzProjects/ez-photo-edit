package com.xulaoyao.ezphotoedit.listener;

import android.graphics.Bitmap;

import com.xulaoyao.ezphotoedit.model.EzPathInfo;

import java.util.List;

/**
 * IEzBitMapCache
 * Created by renwoxing on 2018/3/18.
 */
public interface IEzBitMapCache {

    List<EzPathInfo> getPathInfoList();

    Bitmap getBgAndPathBitmap();

    void setDrawRefreshListener(EzDrawRefreshListener listener);



}
