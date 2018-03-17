package com.sunny.www.compass;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.sunny.www.compass.view.CompassView;
import com.trycatch.mysnackbar.Prompt;
import com.trycatch.mysnackbar.TSnackbar;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private long lastTime = 0;
    private int times = 0;

    String direction = "未知"; // 方向描述
    private String mDirectionText[] = new String[]{"北", "东北", "东", "东南", "南", "西南", "西", "西北"};

    private CompassView mCompassView;
    private TextView mDirection;
    private float oldDirAngel = 0;
    private boolean isVibrate = true;
    private boolean isFirstOpen = true;
    private String IS_VIBRATE = "IS_VIBRATE";
    private String IS_FIRST_OPEN = "IS_FIRST_OPEN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        final SharedPreferences sp = getSharedPreferences("Config", Context.MODE_PRIVATE);
        isFirstOpen = sp.getBoolean(IS_FIRST_OPEN, true);
        if (isFirstOpen) {
            final ViewGroup viewGroup = (ViewGroup) findViewById(android.R.id.content).getRootView();//注意getRootView()最为重要，直接关系到TSnackBar的位置


            TSnackbar snackBar = TSnackbar.make(viewGroup, "点击三次表盘，可关闭(打开)震动哦", TSnackbar.LENGTH_INDEFINITE, TSnackbar.APPEAR_FROM_TOP_TO_DOWN);
            snackBar.setAction("确认", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isFirstOpen = false;
                    sp.edit().putBoolean(IS_FIRST_OPEN, false).apply();
                }
            });
            snackBar.setPromptThemBackground(Prompt.SUCCESS);
            snackBar.show();
        }

        isVibrate = sp.getBoolean(IS_VIBRATE, true);

        mCompassView = (CompassView) findViewById(R.id.compass);
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
                        SharedPreferences sp = getSharedPreferences("Config", Context.MODE_PRIVATE);
                        sp.edit().putBoolean(IS_VIBRATE, isVibrate).apply();
                        if (isVibrate) {
                            Toast.makeText(MainActivity.this, "打开震动", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "关闭震动", Toast.LENGTH_SHORT).show();
                        }
                        times = 0;
                    }
                }
            }
        });

        mDirection = (TextView) findViewById(R.id.tv_dir);
    }


    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @SuppressWarnings("deprecation")
    private void init() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor orientationField = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        //注册监听
        mSensorManager.registerListener(sensorEventListener, orientationField, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float dirAngel = event.values[0];

            mCompassView.setDirectionAngle(dirAngel);
            direction = mDirectionText[((int) (dirAngel + 22.5f) % 360) / 45];
            mDirection.setText(direction);
            if (isVibrate && (int) dirAngel % 30 == 0 && (int) dirAngel != (int) oldDirAngel) {
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(20);
            }
            oldDirAngel = dirAngel;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        //注销所有传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }
}
