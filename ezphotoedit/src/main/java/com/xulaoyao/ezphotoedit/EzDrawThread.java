package com.xulaoyao.ezphotoedit;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.xulaoyao.ezphotoedit.listener.EzDrawListener;

/**
 * surface view 绘制线程
 * EzDrawThread
 * Created by renwoxing on 2018/3/18.
 */
public class EzDrawThread extends Thread {

    private boolean _isRunning = true;
    private boolean isPaint = true; // 是否直接暂停

    private EzDrawListener mEzDrawListener;

    private final SurfaceHolder mSurfaceHolder;
    private Canvas mCanvas = null;

    public EzDrawThread(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;
    }

    public void setEzDrawingListener(EzDrawListener listener) {
        this.mEzDrawListener = listener;
    }


    @Override
    public void run() {
        while (_isRunning) {
            drawBitMap();//绘制
        }
    }

    /**
     * 绘制图画
     */
    private void drawBitMap() {
        if (isPaint && mSurfaceHolder != null) {
            mCanvas = mSurfaceHolder.lockCanvas(); // 注意lock的时间消耗
            try {
                synchronized (mSurfaceHolder) {
                    // 调用外部接口
                    this.mEzDrawListener.onDraw(mCanvas);
                }
            } finally {
                if (mCanvas != null) {
                    //更新UI 线程
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }
    }

    public void setThreadRun(boolean running) { // 设置是否暂停
        this._isRunning = running;
    }

    public boolean isRunning() {
        return _isRunning;
    }

    public void setCanPaint(boolean canPaint) { // 设置是否绘制
        this.isPaint = canPaint;
    }


}
