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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String name = "22.png";
        copy(name, getFilesDir().getAbsolutePath(), name);
        final String path = getFilesDir().getAbsolutePath() + File.separator + name;

        String name2 = "ico_feed_rate.png";
        copy(name2, getFilesDir().getAbsolutePath(), name2);
        final String path2 = getFilesDir().getAbsolutePath() + File.separator + name2;

        String name3 = "ic_expand.png";
        copy(name3, getFilesDir().getAbsolutePath(), name3);
        final String path3 = getFilesDir().getAbsolutePath() + File.separator + name3;


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

        try {
            Log.d("=-=", "onCreate: " + path);
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
                Log.d("=-=", "onCreate: " + path3);
                pesv.load(path3);
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