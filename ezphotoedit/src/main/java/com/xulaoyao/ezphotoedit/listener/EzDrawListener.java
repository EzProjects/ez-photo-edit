package com.xulaoyao.ezphotoedit.listener;

import android.graphics.Canvas;

/**
 * EzDrawListener
 * 绘画接口
 * 通过 bg  +  Canvas 组合成 bitmap 返回 给 surface view
 * Created by renwoxing on 2018/3/18.
 */
public interface EzDrawListener {
    void onDraw(Canvas canvas);
}
