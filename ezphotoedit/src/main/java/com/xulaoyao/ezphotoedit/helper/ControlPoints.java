package com.xulaoyao.ezphotoedit.helper;

import android.graphics.PointF;

/**
 * ControlPoints
 * Created by renwoxing on 2018/2/9.
 */
public class ControlPoints {

    public PointF c1;
    public PointF c2;

    public ControlPoints set(PointF c1, PointF c2) {
        this.c1 = c1;
        this.c2 = c2;
        return this;
    }
}
