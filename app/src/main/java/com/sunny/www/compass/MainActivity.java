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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Paint paint;
    private int begin = 0;
    private SensorManager sensorManager;
    private float gravity[] = new float[3];
    String direction = "未知";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomView customView = new CustomView(this);
        setContentView(customView);
        //获取传感器管理器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //获取重力传感器
        Sensor acceler = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, acceler, SensorManager.SENSOR_DELAY_GAME);
        //获取方向传感器
        @SuppressWarnings("deprecation")
        Sensor orient = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(listener, orient, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销所有传感器
        sensorManager.unregisterListener(listener);
    }

    private SensorEventListener listener = new SensorEventListener() {
        boolean Switch = true;

        @SuppressWarnings("deprecation")
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[SensorManager.DATA_X];
            float y = event.values[SensorManager.DATA_Y];
            float z = event.values[SensorManager.DATA_Z];
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    float alpha = 0.8f;
                    gravity[0] = alpha * gravity[0] + (1 - alpha) * x;
                    gravity[1] = alpha * gravity[1] + (1 - alpha) * y;
                    gravity[2] = alpha * gravity[2] + (1 - alpha) * z;

                    //重力加速度
                    x = gravity[0];
                    y = gravity[1];
                    z = gravity[2];

                    double xs = -0.50;
                    double xa = 0.50;
                    double ys = -0.50;
                    double ya = 0.50;
                    double zs = 9.8;
                    double za = 10.4;
                    if ((x >= xs && x <= xa) && (y >= ys && y <= ya) && (z >= zs && z <= za)) {
//                        accelerometer.setText("水平状态");
                        Switch = true;
                    } else {
//                        accelerometer.setText("倾斜状态,请先置水平");
                        Switch = false;
                    }

                    break;
                case Sensor.TYPE_ORIENTATION:
                    //这时是水平状态才去看方向的变化，因为水平状态才保证方向是正确的
                    if (Switch) {
                        double X = Math.floor(x);
                        int range = 22;
                        int deg = 180;
                        // 指向正北
                        if (X > 360 - range && X < 360 + range) { // 338 22
                            begin = (int) (X - 170);
                            direction = "北";
                        }
                        if (X > 330 - range && X < 350) { // 308 350
                            begin = (int) (X - 150);
                        }
                        // 指向正东
                        if (X > 90 - range && X < 90 + range) { // 68 112
                            begin = (int) (180 - X);
                            direction = "东";
                        }
                        // 指向正南
                        if (X > 180 - range && X < 180 + range) { // 158 202
                            begin = (int) (X - 180);
                            direction = "南";
                        }
                        if (X > 190 && X < 207) { // 190 207
                            begin = (int) (X - 180 - 20);
                        }
                        // 指向正西
                        if (X > 270 - range && X < 270 + range) { // 258 292
                            begin = (int) (180 - X);
                            direction = "西";
                        }
                        // 东偏北
                        if (X > 0 && X < 45) { // 0 45
                            begin = (int) (180 - X);
                        }
                        // 指向东北
                        if (X > 45 - range && X < 45 + range) { // 23 67
                            begin = (int) (180 - X);
                            direction = "东北";
                        }
                        // 指向东南
                        if (X > 135 - range && X < 135 + range) { // 113 157
                            begin = (int) (180 - X);
                            direction = "东南";
                        }
                        //东南
                        if (X > 150 && X < 180) { // 150 180
                            begin = (int) (X - 140);
                            direction = "东南";
                        }
                        // 指向西南
                        if (X > 225 - range && X < 225 + range) { // 203 247
                            begin = (int) (180 - X);
                            direction = "西南";
                        }
                        // 指向西北
                        if (X > 315 - range && X < 315 + range) { // 293 337
                            begin = (int) (180 - X);
                            direction = "西北";
                        }
                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
            Log.d(TAG, "onAccuracyChanged: " + i);
        }
    };

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

            int angel = 180 - begin;
            canvas.drawText(String.valueOf(angel) + "°", -90f, 40f, angelPaint);

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
