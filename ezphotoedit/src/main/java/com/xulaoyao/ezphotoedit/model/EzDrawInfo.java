package com.xulaoyao.ezphotoedit.model;

import android.graphics.Canvas;
import android.graphics.Color;

/**
 * EzDrawInfo
 * Created by renwoxing on 2018/3/18.
 */
@Deprecated
public abstract class EzDrawInfo {

    public int color;

    EzDrawInfo() {
        color = Color.RED;
    }

    EzDrawInfo(int color) {
        this.color = color;
    }

    //绘制
    public abstract void draw(Canvas canvas);

    //移动
    public abstract void pathMove(float moveX, float moveY);
}
