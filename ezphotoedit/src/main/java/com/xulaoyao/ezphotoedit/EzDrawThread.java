package com.xulaoyao.ezphotoedit;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.xulaoyao.ezphotoedit.listener.EzDrawListener;

/**
 * EzDrawThread
 * Created by renwoxing on 2018/3/18.
 */
public class EzDrawThread extends Thread {

    private boolean _isRunning = true;
    private boolean isPaint = true; // 是否直接暂停
    private int FRAME_INTERVAL = 10;// 默认帧时间10ms

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
            long startTime = System.currentTimeMillis();
            mCanvas = mSurfaceHolder.lockCanvas(); // 注意lock的时间消耗
            try {
                synchronized (mSurfaceHolder) {
                    // 调用外部接口
                    this.mEzDrawListener.onDraw(mCanvas);

                    // 调用外部接口
                    long endTime = System.currentTimeMillis();
                    /**
                     * 计算出绘画一次更新的毫秒数
                     * **/
                    int diffTime = (int) (endTime - startTime);

                    if (diffTime < FRAME_INTERVAL) {
                        try {
                            Thread.sleep(FRAME_INTERVAL - diffTime);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                }
            } finally {
                if (mCanvas != null) {
                    //更新UI 线程
                    mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }else {
            try {
                Thread.sleep(FRAME_INTERVAL);
            } catch (InterruptedException e) {
                //e.printStackTrace();
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
