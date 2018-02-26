package com.sunny.www.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private SensorManager mSensorManager;
    private Sensor orientationField;

    String direction = "未知"; // 方向描述
    private String mOrientaionText[] = new String[]{"正北", "东北", "正东", "东南", "正南", "西南", "正西", "西北"};

    private CompassView mCompassView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        mCompassView = (CompassView) findViewById(R.id.compass);
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
        mSensorManager.registerListener(sensorEventListener, orientationField, SensorManager.SENSOR_DELAY_GAME);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            mCompassView.setDirectionAngle((int) event.values[0]);
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
