package com.xulaoyao.ezphotoshop;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xulaoyao.ezphotoedit.EzPhotoEditSurfaceView;
import com.xulaoyao.ezphotoedit.listener.PhotoEditListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    //private EzBitmapDrawBuffer ezBitmapData = new EzBitmapDrawBuffer();

    private Bitmap bmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String name = "111.jpg";
        copy(name, getFilesDir().getAbsolutePath(), name);
        final String path = getFilesDir().getAbsolutePath() + File.separator + name;

        String name2 = "jj.jpeg";
        copy(name2, getFilesDir().getAbsolutePath(), name2);
        final String path2 = getFilesDir().getAbsolutePath() + File.separator + name2;


        final EzPhotoEditSurfaceView pesv = (EzPhotoEditSurfaceView) findViewById(R.id.pesv_image);

        pesv.setPhotoEditListener(new PhotoEditListener() {
            @Override
            public void info(int code, String msg) {
                Log.d("---", "info: " + msg);
                if (code == 200) {
                    findViewById(R.id.tip).setVisibility(View.GONE);
                }
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void next() {
                findViewById(R.id.tip).setVisibility(View.VISIBLE);
                Log.d("=-", "next: -----------------=---------=------------------=------------=-------》");
                //Toast.makeText(MainActivity.this, "next", Toast.LENGTH_SHORT).show();
                pesv.load(path2);
            }

            @Override
            public void previous() {
                findViewById(R.id.tip).setVisibility(View.VISIBLE);
                Log.d("=-", "previous: 《-----------------=---------=------------------=------------=-------");
                //Toast.makeText(MainActivity.this, "previous", Toast.LENGTH_SHORT).show();
                pesv.load(path);
            }
        });
        //延迟展区区域数据加载
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(200);
//                } catch (InterruptedException e) {
//                }
//                //背景图
//                BitmapFactory.Options opt = new BitmapFactory.Options();
//                opt.inPreferredConfig = Bitmap.Config.RGB_565;
//                try {
//                    InputStream inputStream = getAssets().open("111.jpg");
//                    //bmp = BitmapFactory.decodeResource(getResources(), R.drawable.zxc, opt);//图片资源
//                    bmp = BitmapFactory.decodeStream(inputStream);//图片资源
//                    //ezBitmapData.drawBitmap(bmp);//设置图片
//                    pesv.load(bmp);//初始化
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                bmp = null;
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        findViewById(R.id.tip).setVisibility(View.GONE);
//                    }
//                });
//            }
//        }).start();

        try {

            Log.d("=-=", "onCreate: " + path);
            //InputStream inputStream = getAssets().open("jj.jpeg");
            pesv.load(path);
        } catch (Exception e) {
            e.printStackTrace();
        }


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
                Log.d("=-=", "onCreate: " + path2);
                pesv.load(path2);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //背景图
//                        BitmapFactory.Options opt = new BitmapFactory.Options();
//                        opt.inPreferredConfig = Bitmap.Config.RGB_565;
//                        try {
//                            InputStream inputStream = getAssets().open("jj.jpeg");
//                            bmp = BitmapFactory.decodeStream(inputStream);//图片资源
//                            //ezBitmapData.drawBitmap(bmp);//设置图片
//                            pesv.load(bmp);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        //bmp.recycle();
//                        bmp = null;
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                findViewById(R.id.tip).setVisibility(View.GONE);
//                            }
//                        });
//                    }
//                }).start();
            }
        });

        final Button btnReset = (Button) findViewById(R.id.btn_rest);
        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pesv.clear();
            }
        });


        Button btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = pesv.getHandwritingBitmap();
                if (bitmap != null) {
                    saveBitmapToFile(bitmap);
                } else {
                    Log.d("==-=-", "onClick: --- get bitmap is null!");
                }
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


    public void copy(String ASSETS_NAME, String savePath, String saveName) {
        String filename = savePath + File.separator + saveName;
        File dir = new File(savePath);
        // 如果目录不中存在，创建这个目录
        if (!dir.exists()) dir.mkdir();
        try {
            if (!(new File(filename)).exists()) {
                InputStream is = getAssets().open(ASSETS_NAME);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveBitmapToFile(Bitmap btImage) {
        File file = new File(getCacheDir(), System.currentTimeMillis() + ".jpg");
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            btImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d("save", "saveBitmapToFile:保存的在sd +" + getCacheDir() + " 目录下");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (btImage != null) {
                btImage.recycle();
                btImage = null;
            }
        }
        Toast.makeText(MainActivity.this, "保存已经至" + getCacheDir() + "下", Toast.LENGTH_SHORT).show();
    }
}