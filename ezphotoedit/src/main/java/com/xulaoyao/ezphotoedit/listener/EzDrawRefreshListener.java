package com.xulaoyao.ezphotoedit.listener;

import com.xulaoyao.ezphotoedit.cache.EzBitmapDrawBuffer;

/**
 * EzDrawRefreshListener
 * Created by renwoxing on 2018/3/18.
 * path 组合后 通知 {@link EzBitmapDrawBuffer}  刷新的接口
 */
public interface EzDrawRefreshListener {
    void onRefresh();
}
