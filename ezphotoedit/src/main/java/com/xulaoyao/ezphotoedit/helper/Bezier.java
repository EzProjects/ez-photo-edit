package com.xulaoyao.ezphotoedit.helper;

import android.graphics.PointF;

/**
 * 曲线 更圆滑
 * 更接近人的手写形状
 * Bezier
 * 三阶贝塞尔曲线是由两个控制点控制的曲线
 * Created by renwoxing on 2018/2/9.
 */
public class Bezier {

    /**
     * 起始点
     */
    public PointF startPoint;  //开始点
    /**
     * 控制点1
     */
    public PointF control1;    //控制点1
    /**
     * 控制点2
     */
    public PointF control2;
    /**
     * 结束点
     */
    public PointF endPoint;

    public Bezier set(PointF startPoint, PointF control1,
                      PointF control2, PointF endPoint) {
        this.startPoint = startPoint;
        this.control1 = control1;
        this.control2 = control2;
        this.endPoint = endPoint;
        return this;
    }

    public float length() {
        int steps = 10;
        float length = 0;
        double cx, cy, px = 0, py = 0, xDiff, yDiff;

        for (int i = 0; i <= steps; i++) {
            float t = (float) i / steps;
            cx = point(t, this.startPoint.x, this.control1.x,
                    this.control2.x, this.endPoint.x);
            cy = point(t, this.startPoint.y, this.control1.y,
                    this.control2.y, this.endPoint.y);
            if (i > 0) {
                xDiff = cx - px;
                yDiff = cy - py;
                length += Math.sqrt(xDiff * xDiff + yDiff * yDiff);
            }
            px = cx;
            py = cy;
        }
        return length;

    }

    public double point(float t, float start, float c1, float c2, float end) {
        return start * (1.0 - t) * (1.0 - t) * (1.0 - t)
                + 3.0 * c1 * (1.0 - t) * (1.0 - t) * t
                + 3.0 * c2 * (1.0 - t) * t * t
                + end * t * t * t;
    }
}
