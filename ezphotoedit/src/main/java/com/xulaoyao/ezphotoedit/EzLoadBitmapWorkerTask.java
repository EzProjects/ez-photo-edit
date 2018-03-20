package com.xulaoyao.ezphotoedit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.xulaoyao.ezphotoedit.listener.LoadBitmapWorkerListener;

/**
 * EzLoadBitmapWorkerTask
 * Created by renwoxing on 2018/3/20.
 */
public class EzLoadBitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

    private LoadBitmapWorkerListener mLoadBitmapWorkerListener;

    public EzLoadBitmapWorkerTask(LoadBitmapWorkerListener listener) {
        this.mLoadBitmapWorkerListener = listener;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        String path = params[0];
        Bitmap bgBitmap = null;
        try {
            bgBitmap = BitmapFactory.decodeFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bgBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (mLoadBitmapWorkerListener != null)
            mLoadBitmapWorkerListener.onPostExecute(bitmap);
    }
}
