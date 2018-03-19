package com.xulaoyao.ezphotoedit.model;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * EzDrawPath
 * Created by renwoxing on 2018/3/19.
 */
public class EzDrawPath extends EzDrawInfo {

    Path path;
    int size;

    public EzDrawPath() {
        path = new Path();
        size = 1;
    }

    public EzDrawPath(float x, float y, int size, int color) {
        super(color);
        path = new Path();
        this.size = size;
        path.moveTo(x, y);
        path.lineTo(x, y);
    }


    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(color);
        paint.setStrokeWidth(size);
        paint.setStyle(Paint.Style.STROKE);
        //paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(path, paint);
    }

    @Override
    public void pathMove(float moveX, float moveY) {
        path.lineTo(moveX, moveY);
    }
}