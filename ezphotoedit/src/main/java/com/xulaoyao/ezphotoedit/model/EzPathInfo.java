package com.xulaoyao.ezphotoedit.model;

import android.graphics.Path;
import android.graphics.PointF;

import com.xulaoyao.ezphotoedit.helper.Bezier;
import com.xulaoyao.ezphotoedit.helper.ControlPoints;

import java.util.ArrayList;
import java.util.List;

/**
 * EzPathInfo
 * Created by renwoxing on 2018/3/19.
 */
public class EzPathInfo {

    public EzPathInfo() {
        mPoints = new ArrayList<>();
        this.path = new Path();
    }

    public EzPathInfo(Path path) {
        this.path = path;
    }

    public String name;
    public String id;
    public Path path;
    //比例，通过此值来算笔迹大小
    public float scale;
    public float strokeWidth = 5;
    //速度
    public float velocity;

    public List<PointF> mPoints;

    // Cache
    private List<PointF> mPointsCache = new ArrayList<>();
    private ControlPoints mControlPointsCached = new ControlPoints();
    private Bezier mBezierCached = new Bezier();
    private int mMinWidth = 5;
    private int mMaxWidth = 15;
    private float mVelocityFilterWeight = 0.9f;  //笔触比例
    private float mLastVelocity;
    private float mLastWidth;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public float getScale() {
        return scale == 0f ? 1f : scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public float getVelocity() {
        return velocity;
    }

    public void setVelocity(float velocity) {
        this.velocity = velocity;
    }

    /**
     * 生成 bezier 三阶曲线
     *
     * @param newPoint
     */
    public void addPoint(PointF newPoint) {
        newPoint = getNewPoint(newPoint.x, newPoint.y);
        mPoints.add(newPoint);
        int pointsCount = mPoints.size();
        if (pointsCount > 3) {

            //控制点1
            ControlPoints tmp = calculateCurveControlPoints(mPoints.get(0), mPoints.get(1), mPoints.get(2));
            PointF c2 = tmp.c2;
            recyclePoint(tmp.c1);

            //控制点2
            tmp = calculateCurveControlPoints(mPoints.get(1), mPoints.get(2), mPoints.get(3));
            PointF c3 = tmp.c1;
            recyclePoint(tmp.c2);


            Bezier curve = mBezierCached.set(mPoints.get(1), c2, c3, mPoints.get(2));

            PointF startPoint = curve.startPoint;
            PointF endPoint = curve.endPoint;

            path.cubicTo(c2.x, c2.y, c3.x, c3.y, endPoint.x, endPoint.y);


            float velocity = getVelocity();
            velocity = Float.isNaN(velocity) ? 0.0f : velocity;

            velocity = mVelocityFilterWeight * velocity
                    + (1 - mVelocityFilterWeight) * mLastVelocity;

            float newWidth = strokeWidth(velocity);

            float widthDelta = mLastWidth - newWidth;

            float drawSteps = (float) Math.floor(curve.length());

            for (int i = 0; i < drawSteps; i++) {
                // Calculate the Bezier (x, y) coordinate for this step.
                float t = ((float) i) / drawSteps;
                float tt = t * t;
                float ttt = tt * t;
                // Set the incremental stroke width and draw.
                //strokeWidth = (mLastWidth + ttt * widthDelta);
            }

            // The new width is a function of the velocity. Higher velocities
            // correspond to thinner strokes.

            mLastVelocity = velocity;
            mLastWidth = newWidth;

            // Remove the first element from the list,
            // so that we always have no more than 4 mPoints in mPoints array.
            recyclePoint(mPoints.remove(0));
            recyclePoint(c2);
            recyclePoint(c3);

        } else if (pointsCount == 1) {
            // To reduce the initial lag make it work with 3 mPoints
            // by duplicating the first point  复制 第一个点
            PointF firstPoint = mPoints.get(0);
            mPoints.add(getNewPoint(firstPoint.x, firstPoint.y));
        }

    }

    /**
     * 三个点来生成控制点
     *
     * @param s1
     * @param s2
     * @param s3
     * @return
     */
    private ControlPoints calculateCurveControlPoints(PointF s1, PointF s2, PointF s3) {
        float dx1 = s1.x - s2.x;
        float dy1 = s1.y - s2.y;
        float dx2 = s2.x - s3.x;
        float dy2 = s2.y - s3.y;

        float m1X = (s1.x + s2.x) / 2.0f;
        float m1Y = (s1.y + s2.y) / 2.0f;
        float m2X = (s2.x + s3.x) / 2.0f;
        float m2Y = (s2.y + s3.y) / 2.0f;

        float l1 = (float) Math.sqrt(dx1 * dx1 + dy1 * dy1);
        float l2 = (float) Math.sqrt(dx2 * dx2 + dy2 * dy2);

        float dxm = (m1X - m2X);
        float dym = (m1Y - m2Y);
        float k = l2 / (l1 + l2);
        if (Float.isNaN(k)) k = 0.0f;
        float cmX = m2X + dxm * k;
        float cmY = m2Y + dym * k;

        float tx = s2.x - cmX;
        float ty = s2.y - cmY;

        return mControlPointsCached.set(new PointF(m1X + tx, m1Y + ty), new PointF(m2X + tx, m2Y + ty));
    }

    private PointF getNewPoint(float x, float y) {
        int mCacheSize = mPointsCache.size();
        PointF timedPoint;
        if (mCacheSize == 0) {
            // Cache is empty, create a new point
            timedPoint = new PointF();
        } else {
            // Get point from cache
            timedPoint = mPointsCache.remove(mCacheSize - 1);
        }
        timedPoint.set(x, y);
        return timedPoint;
    }

    private void recyclePoint(PointF point) {
        mPointsCache.add(point);
    }

    public void clear() {
        if (mPoints != null)
            mPoints.clear();
        if (mPointsCache != null)
            mPointsCache.clear();
        mPoints = null;
        mPointsCache = null;
    }

    private float strokeWidth(float velocity) {
        return Math.max(mMaxWidth / (velocity + 1), mMinWidth);
    }
}
