package com.xulaoyao.ezphotoshop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xulaoyao.ezphotoedit.EzPhotoEditSurfaceView;
import com.xulaoyao.ezphotoedit.listener.PhotoEditListener;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //private EzBitmapDrawBuffer ezBitmapData = new EzBitmapDrawBuffer();

    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EzPhotoEditSurfaceView pesv = (EzPhotoEditSurfaceView) findViewById(R.id.pesv_image);

        pesv.setPhotoEditListener(new PhotoEditListener() {
            @Override
            public void info(int code, String msg) {
                Log.d("---", "info: " + msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
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
                    //ezBitmapData.drawBitmap(bmp);//设置图片
                    pesv.load(bmp);//初始化
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bmp = null;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.tip).setVisibility(View.GONE);
                    }
                });
            }
        }).start();


        Button btn = (Button) findViewById(R.id.btn_cancel);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesv.undo();
            }
        });

        final Button btnEdit = (Button) findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesv.setEdit("1".equals(view.getTag().toString()));
                if ("1".equals(view.getTag().toString())) {
                    view.setTag("0");
                    btnEdit.setText("可视");
                } else {
                    view.setTag("1");
                    btnEdit.setText("编辑");
                }
            }
        });

        final Button btnChange = (Button) findViewById(R.id.btn_change);
        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findViewById(R.id.tip).setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //背景图
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPreferredConfig = Bitmap.Config.RGB_565;
                        try {
                            InputStream inputStream = getAssets().open("jj.jpeg");
                            bmp = BitmapFactory.decodeStream(inputStream);//图片资源
                            //ezBitmapData.drawBitmap(bmp);//设置图片
                            pesv.load(bmp);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //bmp.recycle();
                        bmp = null;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.tip).setVisibility(View.GONE);
                            }
                        });
                    }
                }).start();
            }
        });

        final Button btnReset = (Button) findViewById(R.id.btn_rest);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesv.clear();
            }
        });


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
