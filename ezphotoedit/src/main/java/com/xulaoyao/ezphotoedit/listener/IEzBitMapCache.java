package com.xulaoyao.ezphotoedit.listener;

import android.graphics.Bitmap;

import com.xulaoyao.ezphotoedit.model.EzDrawInfo;

import java.util.List;

/**
 * IEzBitMapCache
 * Created by renwoxing on 2018/3/18.
 */
public interface IEzBitMapCache {

    List<EzDrawInfo> getPathInfoList();

    void setPathInfoList(List<EzDrawInfo> list);

    void setPathInfo(EzDrawInfo info);

    Bitmap getBgAndPathBitmap();

    void setDrawRefreshListener(EzDrawRefreshListener listener);
}
