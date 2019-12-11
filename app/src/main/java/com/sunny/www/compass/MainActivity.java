package com.sunny.www.compass;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sunny.www.compass.view.IconView;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private final String SP_CONFIG = "sp_config";

    private final String IS_VIBRATE = "IS_VIBRATE";

    private final String IS_FIRST_OPEN = "IS_FIRST_OPEN";

    private final String[] mDirectionText = new String[]{"北", "东北", "东", "东南", "南", "西南", "西", "西北"};

    private ViewGroup mViewGroup;

    private TextView mDirection;

    private IconView mCompassView;

    private TSnackbar accuracyWarnSnackBar;

    private boolean isFirstOpen = true;

    private boolean isVibrate = true;

    private long lastTime = 0;

    private int times = 0;

    private SensorManager mSensorManager;

    String direction = "未知";

    private float lastDirAngel = 0;

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        initListener();
    }

    private void initData() {
        final SharedPreferences sp = getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
        isFirstOpen = sp.getBoolean(IS_FIRST_OPEN, true);
        isVibrate = sp.getBoolean(IS_VIBRATE, true);
    }

    private void initView() {
        mViewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();
        mDirection = findViewById(R.id.tv_dir);
        mCompassView = findViewById(R.id.compass);

        showSnackBar();
    }

    private Bitmap createBitmap(View view) {
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    private void saveBitmap(Bitmap bitmap) {
        FileOutputStream fos;
        try {
            File root = Environment.getExternalStorageDirectory();
            File file = new File(root, "test.png");
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.e("gaozy", e.toString());
        }
    }

    private void initListener() {
        mCompassView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((System.currentTimeMillis() - lastTime) > 2000) {
                    times = 1;
                    lastTime = System.currentTimeMillis();
                } else {
                    times++;
                    if (times >= 3) {
                        isVibrate = !isVibrate;
                        SharedPreferences sp = getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
                        sp.edit().putBoolean(IS_VIBRATE, isVibrate).apply();
                        if (isVibrate) {
                            Toast.makeText(MainActivity.this, "打开震动", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "关闭震动", Toast.LENGTH_SHORT).show();
                            Bitmap bitmap = createBitmap(mCompassView);
                            saveBitmap(bitmap);
                        }
                        times = 0;
                    }
                }
            }
        });
    }

    private void showSnackBar() {
        if (isFirstOpen) {
            TSnackbar snackBar = TSnackbar.make(mViewGroup, "点击三次表盘，可关闭(打开)震动哦", TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
            snackBar.setAction("确认", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFirstOpen = false;

                    SharedPreferences sp = getSharedPreferences(SP_CONFIG, Context.MODE_PRIVATE);
                    sp.edit().putBoolean(IS_FIRST_OPEN, false).apply();
                }
            });
            snackBar.setPromptThemBackground(Prompt.SUCCESS);
            snackBar.show();
        }
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //注册监听
        mSensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float dirAngel = event.values[0];

            mCompassView.setDirectionAngle(dirAngel);
            direction = mDirectionText[((int) (dirAngel + 22.5f) % 360) / 45];
            mDirection.setText(direction);
            if (isVibrate && (int) dirAngel % 30 == 0 && (int) dirAngel != (int) lastDirAngel) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(20);
            }
            lastDirAngel = dirAngel;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (accuracy != SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
                    && accuracy != SensorManager.SENSOR_STATUS_ACCURACY_HIGH) {
                if (accuracyWarnSnackBar == null) {
                    accuracyWarnSnackBar = TSnackbar.make(mViewGroup, "附近可能有电磁干扰，请参照蔡徐坤打篮球的姿势晃动手机校准指南针",
                            TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
                    accuracyWarnSnackBar.setPromptThemBackground(Prompt.WARNING);
                }
                accuracyWarnSnackBar.show();
            } else if (accuracyWarnSnackBar != null) {
                accuracyWarnSnackBar.dismiss();
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        //注销传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }
}