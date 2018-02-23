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

public class MainActivity2 extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Paint paint;
    private int begin = 0;
    private SensorManager mSensorManager;
    String direction = "未知";
    private Sensor accelerometer;
    private Sensor magnetic;
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    CompassSensorEventListener mSensorEventListener = new CompassSensorEventListener();
    private float mRotation = 0;
    private int a = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomView customView = new CustomView(this);
        setContentView(customView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // 初始化加速度传感器
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        // 初始化地磁场传感器
        magnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //注册监听
        mSensorManager.registerListener(mSensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mSensorEventListener, magnetic, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销所有传感器
        mSensorManager.unregisterListener(mSensorEventListener);
    }

    class CompassSensorEventListener implements SensorEventListener {

        @Override

        public void
        onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                accelerometerValues = event.values;
            }
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticFieldValues = event.values;
            }
            calculateOrientation();

        }


        @Override

        public void onAccuracyChanged(Sensor sensor, int accuracy) {


        }


    }

    private void calculateOrientation() {
        float[] values = new float[3];
        float[] R = new float[9];
        SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(R, values);
        values[0] = (float) Math.toDegrees(values[0]);
        float orientation = values[0];
        if (orientation < 0) {
            orientation = 360 + orientation;
        }
        orientation += mRotation;
        if (orientation > 360) {
            orientation -= 360;
        }
        direction = mOrientaionText[((int) (orientation + 22.5f) % 360) / 45];
        begin = 180 - (int) orientation;

        a = (int) orientation;

        // 40
        Log.d(TAG, orientation + "");
    }

    private String mOrientaionText[] = new String[]{"正北", "东北", "正东", "东南", "正南", "西南", "正西", "西北"};


    class CustomView extends View {
        public CustomView(Context context) {
            super(context);
            //new 一个画笔
            paint = new Paint();
            //设置画笔颜色
            paint.setColor(Color.WHITE);
            //设置结合处的样子,Miter:结合处为锐角, Round:结合处为圆弧:BEVEL:结合处为直线。
            paint.setStrokeJoin(Paint.Join.ROUND);
            //设置画笔笔刷类型 如影响画笔但始末端
            paint.setStrokeCap(Paint.Cap.ROUND);
            //设置画笔宽度
            paint.setStrokeWidth(3);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            //设置屏幕颜色，也可以利用来清屏。
            canvas.drawColor(Color.BLACK);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3f);
            canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2); //将画布移动到屏幕中心
            canvas.drawCircle(0, 0, 330, paint); //画圆圈

            Paint linePaint = new Paint(paint); //小刻度画笔对象
            linePaint.setStrokeWidth(1f);//设置画笔笔尖的粗细
            linePaint.setTextSize(20);
            linePaint.setStyle(Paint.Style.FILL);

            Paint textPaint = new Paint(paint);
            textPaint.setStrokeWidth(1f);//设置画笔笔尖的粗细
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(30);

            Paint angelPaint = new Paint(paint);
            angelPaint.setTextSize(120);
            angelPaint.setStyle(Paint.Style.FILL);

            Paint dirPaint = new Paint(paint);
            dirPaint.setTextSize(100);
            dirPaint.setStyle(Paint.Style.FILL);

            Paint trigonPaint = new Paint(paint);
            trigonPaint.setStyle(Paint.Style.FILL);
            trigonPaint.setColor(Color.GRAY);

            Paint arcPaint = new Paint(paint);
            arcPaint.setStyle(Paint.Style.STROKE);
            arcPaint.setColor(Color.GRAY);

            float y = 300;   //向Y方向移动画笔的位置
            float x = 300;   //向X方向移动画笔的位置
            int count = 360; //总刻度数
            canvas.save(); //各个状态最初，是下次第一个canvas.restore()返回点

            Path path2 = new Path();
            path2.moveTo(-28f, y + 28 - 750);// 此点为多边形的起点
            path2.lineTo(28f, y + 28 - 750);
            path2.lineTo(0, (float) (y + 30 - Math.sqrt(56 * 56 - 28 * 28) - 750));
            path2.close(); // 使这些点构成封闭的多边形
            canvas.drawPath(path2, trigonPaint);

            RectF oval = new RectF(-422f, -422f, 422f, 422f);
            canvas.drawArc(oval, -82, 140, false, arcPaint);
            canvas.drawArc(oval, 122, 140, false, arcPaint);

            canvas.drawText(direction, -100f, -550f, dirPaint);

            canvas.drawText(String.valueOf(a) + "°", -90f, 40f, angelPaint);

            canvas.rotate(begin, 0f, 0f); //旋转画纸

            for (int i = 0; i < count; i++) {
                if (i % 90 == 0) {
                    //画刻度
                    canvas.drawLine(0f, y - 18f, 0, y + 12f, paint);
                    //画刻度的数字
                    if (i == 0) {
                        canvas.drawText("北", -14f, y - 34f, textPaint);

                        Path path = new Path();
                        path.moveTo(-28f, y + 28);// 此点为多边形的起点
                        path.lineTo(28f, y + 28);
                        path.lineTo(0, (float) (y + 30 + Math.sqrt(56 * 56 - 28 * 28)));
                        path.close(); // 使这些点构成封闭的多边形
                        trigonPaint.setColor(Color.RED);
                        canvas.drawPath(path, trigonPaint);

                    } else if (i == 90) {
                        canvas.drawText("东", -14f, y - 34f, textPaint);
                    } else if (i == 180) {
                        canvas.drawText("南", -14f, y - 34f, textPaint);
                    } else if (i == 270) {
                        canvas.drawText("西", -14f, y - 34f, textPaint);
                    }
                } else if (i % 30 == 0) {
                    canvas.drawLine(0f, y - 16f, 0f, y + 12f, linePaint);
                    canvas.drawText(String.valueOf(i), -6f, y - 34f, linePaint);
                } else if (i % 2 == 0) {
                    canvas.drawLine(0f, y - 16f, 0f, y + 12f, linePaint);
                }
                //每一个循环就旋转一个刻度，可以想象一下就是笔不动，下面的纸旋转，那么下一次画的位置就发生改变了
                canvas.rotate(360 / count, 0f, 0f); //旋转画纸
            }
            canvas.restore();

            //刷新页面
            postInvalidateDelayed(1 / 60);
        }
    }
}
