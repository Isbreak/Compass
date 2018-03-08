package com.sunny.www.compass;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.sunny.www.compass.view.CompassView;
import com.tencent.bugly.crashreport.CrashReport;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private SensorManager mSensorManager;
    private Sensor orientationField;

    String direction = "未知"; // 方向描述
    private String mDirectionText[] = new String[]{"北", "东北", "东", "东南", "南", "西南", "西", "西北"};

    private CompassView mCompassView;
    private TextView mDirection;
    private float oldDirAngel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mCompassView = (CompassView) findViewById(R.id.compass);
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
        orientationField = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

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
            if ((int) dirAngel % 30 == 0 && (int) dirAngel != (int) oldDirAngel) {
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
