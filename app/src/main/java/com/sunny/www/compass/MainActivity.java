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
import android.view.View;
import android.view.Window;

/**
 * 指南针主界面
 */
public class MainActivity extends AppCompatActivity {

    private Paint paint;
    private SensorManager mSensorManager;
    private Sensor accelerometer; // 加速度传感器
    private Sensor magneticField; // 地磁场传感器
    private int rotateAngle = 0; // 转盘需要转动的角度
    private int directionAngle = 0; // 当前手机指向的方向角度
    String direction = "未知"; // 方向描述
    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private String mOrientaionText[] = new String[]{"正北", "东北", "正东", "东南", "正南", "西南", "正西", "西北"};

    // 防止传感器过于灵敏，导致指南针一直抖动
    private int tempRotateAngle = 0;
    private int tempDirectionAngle = 0;

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
        magneticField = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //注册监听
        mSensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(sensorEventListener, magneticField, SensorManager.SENSOR_DELAY_UI);
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
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
    };

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
        if (orientation > 360) {
            orientation -= 360;
        }
        // 计算当前方向
        direction = mOrientaionText[((int) (orientation + 22.5f) % 360) / 45];

        // 计算转盘需要旋转的角度
        tempRotateAngle = 180 - (int) orientation;
        if (Math.abs(tempRotateAngle - rotateAngle) > 2) {
            rotateAngle = tempRotateAngle;
            directionAngle = (int) orientation;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //注销所有传感器
        mSensorManager.unregisterListener(sensorEventListener);
    }

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
            paint.setStrokeWidth(3f);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
        }

        @Override
        public void draw(Canvas canvas) {
            super.draw(canvas);
            //设置屏幕颜色，也可以利用来清屏。
            canvas.drawColor(Color.BLACK);
            //将画布移动到屏幕中心
            canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
            //画圆圈
            canvas.drawCircle(0, 0, 330, paint);

            //普通刻度
            Paint linePaint = new Paint(paint);
            linePaint.setStrokeWidth(1f);
            linePaint.setStyle(Paint.Style.FILL);

            // 转盘内的东西南北四个方向描述
            Paint textPaint = new Paint(paint);
            textPaint.setStrokeWidth(1f);
            textPaint.setStyle(Paint.Style.FILL);
            textPaint.setTextSize(30);

            // 转盘内角度值
            Paint angelPaint = new Paint(paint);
            angelPaint.setTextSize(120);
            angelPaint.setStyle(Paint.Style.FILL);

            // 顶部方向描述
            Paint descPaint = new Paint(paint);
            descPaint.setTextSize(100);
            descPaint.setStyle(Paint.Style.FILL);

            // 三角形
            Paint trigonPaint = new Paint(paint);
            trigonPaint.setStyle(Paint.Style.FILL);
            trigonPaint.setColor(Color.GRAY);

            // 最外部两个圆弧
            Paint arcPaint = new Paint(paint);
            arcPaint.setStyle(Paint.Style.STROKE);
            arcPaint.setColor(Color.GRAY);

            float y = 300;   //向Y方向移动画笔的位置
//            float x = 300;   //向X方向移动画笔的位置
            int count = 360; //总刻度数
            canvas.save(); //各个状态最初，是下次第一个canvas.restore()返回点

            // 绘制灰色的三角形
            Path grayTriangle = new Path();
            grayTriangle.moveTo(-28f, y + 28 - 750);
            grayTriangle.lineTo(28f, y + 28 - 750);
            grayTriangle.lineTo(0, (float) (y + 30 - Math.sqrt(56 * 56 - 28 * 28) - 750));
            grayTriangle.close();
            canvas.drawPath(grayTriangle, trigonPaint);

            // 绘制最外部两个圆弧
            RectF oval = new RectF(-422f, -422f, 422f, 422f);
            canvas.drawArc(oval, -82, 140, false, arcPaint);
            canvas.drawArc(oval, 122, 140, false, arcPaint);

            // 绘制顶部方向描述
            canvas.drawText(direction, -100f, -550f, descPaint);
            // 绘制中间方向角度值
            canvas.drawText(String.valueOf(directionAngle) + "°", -90f, 40f, angelPaint);
            //旋转画纸
            canvas.rotate(rotateAngle, 0f, 0f);

            for (int i = 0; i < count; i++) {
                if (i % 90 == 0) {
                    // 画四个方向的大刻度
                    canvas.drawLine(0f, y - 18f, 0, y + 12f, paint);

                    if (i == 0) {
                        canvas.drawText("北", -14f, y - 34f, textPaint);
                        // 绘制红色三角形
                        Path redTriangle = new Path();
                        redTriangle.moveTo(-28f, y + 28);
                        redTriangle.lineTo(28f, y + 28);
                        redTriangle.lineTo(0, (float) (y + 30 + Math.sqrt(56 * 56 - 28 * 28)));
                        redTriangle.close();
                        trigonPaint.setColor(Color.RED);
                        canvas.drawPath(redTriangle, trigonPaint);
                    } else if (i == 90) {
                        canvas.drawText("东", -14f, y - 34f, textPaint);
                    } else if (i == 180) {
                        canvas.drawText("南", -14f, y - 34f, textPaint);
                    } else if (i == 270) {
                        canvas.drawText("西", -14f, y - 34f, textPaint);
                    }
                } else if (i % 2 == 0) {
                    canvas.drawLine(0f, y - 16f, 0f, y + 12f, linePaint);

                    if (i % 30 == 0) {
                        // 绘制角度值为30整倍数的值
                        canvas.drawText(String.valueOf(i), -6f, y - 34f, linePaint);
                    }
                }

                //旋转画纸
                //每一个循环就旋转一个刻度，可以想象一下就是笔不动，下面的纸旋转，那么下一次画的位置就发生改变了
                canvas.rotate(360 / count, 0f, 0f);
            }
            canvas.restore();

            //刷新页面
            postInvalidateDelayed(1 / 60);
        }
    }
}
