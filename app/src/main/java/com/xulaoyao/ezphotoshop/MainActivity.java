package com.xulaoyao.ezphotoshop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.xulaoyao.ezphotoedit.EzPhotoEditSurfaceView;
import com.xulaoyao.ezphotoedit.draw.EzBitmapCache;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private EzBitmapCache ezBitmapData = new EzBitmapCache();

    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EzPhotoEditSurfaceView pesv = (EzPhotoEditSurfaceView) findViewById(R.id.pesv_image);

        //延迟展区区域数据加载
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                }
                //背景图
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inPreferredConfig = Bitmap.Config.RGB_565;
                try {
                    InputStream inputStream = getAssets().open("111.jpg");
                    //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.zxc, opt);//图片资源
                    bmp = BitmapFactory.decodeStream(inputStream);//图片资源
                    ezBitmapData.drawBitmap(bmp);//设置图片
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.zxc, opt);//图片资源
                //adapter.setBmp(bmp);//设置图片
                bmp = null;
                //getUnitList();//设置数组
                //adapter.setList(unitList);//设置数组
                //ezBitmapData.refreshData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pesv.setBitmapCache(ezBitmapData);//初始化
                        findViewById(R.id.tip).setVisibility(View.GONE);
                    }
                });
            }
        }).start();

        pesv.setBitmapData(ezBitmapData);//初始化



    }


//    EzDrawInfo mCurrentPathInfo = new EzDrawPath();
//    List<EzDrawInfo> mPathInfoList = new ArrayList<>();

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        //mPhotoEditSurfaceView.onTouchEvent(event);
//
//        int action = event.getAction();
//        if (action == MotionEvent.ACTION_CANCEL) {
//            return false;
//        }
//        if (event.getPointerCount() < 2) {
//            float touchX = event.getRawX();
//            float touchY = event.getRawY();
//
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    setCurrentPathInfo(touchX, touchY);
//                    break;
//                case MotionEvent.ACTION_MOVE:
//                    mCurrentPathInfo.pathMove(touchX, touchY);
//                    Log.d("--", "onTouchEvent x: " + touchX + " y:" + touchY + " size:" + mPathInfoList.size());
//                    break;
//                case MotionEvent.ACTION_UP:
//                    mCurrentPathInfo.pathMove(touchX, touchY);
//                    mPathInfoList.add(mCurrentPathInfo);
//                    mCurrentPathInfo = null;
//                    ezBitmapData.setPathInfo(mCurrentPathInfo);
//                    ezBitmapData.refreshData();
//                    break;
//                default:
//                    break;
//            }
//        } else {
//
//        }
//        return false;
//    }
//
//
//    public void setCurrentPathInfo(float x, float y) {
//        mCurrentPathInfo = new EzDrawPath(x, y, 5, Color.RED);
//    }


}
