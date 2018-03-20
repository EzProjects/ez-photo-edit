package com.xulaoyao.ezphotoedit.listener;

import android.graphics.Bitmap;

/**
 * LoadBitmapWorkerListener
 * Created by renwoxing on 2018/3/20.
 */
public interface LoadBitmapWorkerListener {
    void onPostExecute(Bitmap bgBitmap);
}
