package com.sunny.www.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by 67045 on 2018/2/26.
 */
public class CompassView extends View {

    private Paint mPaint;
    private int viewSize;
    private int directionAngle = 270;
    private int oldDirectionAngle = 0;
    private int radius;
    private Rect mTextRect;

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTextRect = new Rect();
    }

    public void setDirectionAngle(int directionAngle) {
        this.directionAngle = directionAngle;
        if (oldDirectionAngle != directionAngle) {
            postInvalidateDelayed(1 / 60);
            oldDirectionAngle = directionAngle;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        viewSize = Math.min(widthSize, heightSize);
        radius = viewSize * 3 / 5 / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        // 画中间角度值
        String textAngel = String.valueOf(directionAngle);
        Paint angelPaint = new Paint(mPaint);
        angelPaint.setColor(Color.argb(255, 252, 252, 252));
        angelPaint.setTextSize(140f);
        angelPaint.getTextBounds(textAngel, 0, textAngel.length(), mTextRect);
        int width = mTextRect.width();
        int height = mTextRect.height();
        canvas.drawText(textAngel + "°", -width / 2, height / 2, angelPaint);

        // 画灰色三角形
        Paint grayTrigonPaint = new Paint(mPaint);
        grayTrigonPaint.setColor(Color.argb(255, 50, 50, 50));
        Path grayTriangle = new Path();
        grayTriangle.moveTo(-28f, -radius - 90);
        grayTriangle.lineTo(28f, -radius - 90);
        grayTriangle.lineTo(0, (float) (-radius - Math.sqrt(56 * 56 - 28 * 28) - 90));
        grayTriangle.close();
        canvas.drawPath(grayTriangle, grayTrigonPaint);

        // 绘制最外部两个圆弧
        Paint arcPaint = new Paint(mPaint);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(Color.argb(255, 50, 50, 50));
        arcPaint.setStrokeWidth(4f);

        RectF oval = new RectF(-radius - 90, -radius - 90, radius + 90, radius + 90);
        canvas.drawArc(oval, -82, 140, false, arcPaint);
        canvas.drawArc(oval, 122, 140, false, arcPaint);


        // 画圆圈
        // 红色画笔
        Paint circlePaint1 = new Paint(mPaint);
        circlePaint1.setColor(Color.argb(255, 253, 57, 0));
        circlePaint1.setStyle(Paint.Style.STROKE);
        circlePaint1.setStrokeWidth(6f);

        // 灰色画笔
        Paint circlePaint2 = new Paint(mPaint);
        circlePaint2.setColor(Color.argb(255, 155, 155, 155));
        circlePaint2.setStyle(Paint.Style.STROKE);
        circlePaint2.setStrokeWidth(6f);
        RectF oval2 = new RectF(-radius, -radius, radius, radius);

        if (directionAngle < 180) {
            canvas.drawArc(oval2, -90 - directionAngle + 6, directionAngle - 1 - 6, false, circlePaint1);
            canvas.drawArc(oval2, -90 + 1, 360 - directionAngle - 7, false, circlePaint2);
        } else {
            canvas.drawArc(oval2, -90 + 1, 360 - directionAngle - 7, false, circlePaint1);
            canvas.drawArc(oval2, -90 + 360 - directionAngle + 6, directionAngle - 1 - 6, false, circlePaint2);
        }

        canvas.rotate(-directionAngle, 0f, 0f);

        // 画红色三角形
        Paint trigonPaint = new Paint(mPaint);
        trigonPaint.setColor(Color.argb(255, 253, 57, 0));
        Path redTriangle = new Path();
        redTriangle.moveTo(-28f, -radius + 6);
        redTriangle.lineTo(28f, -radius + 6);
        redTriangle.lineTo(0, (float) (-radius - Math.sqrt(56 * 56 - 28 * 28) + 6));
        redTriangle.close();
        canvas.drawPath(redTriangle, trigonPaint);

        // 画刻度
        Paint normalScalePaint = new Paint(mPaint);
        normalScalePaint.setStyle(Paint.Style.FILL);
        normalScalePaint.setStrokeWidth(3f);
        normalScalePaint.setColor(Color.argb(255, 107, 107, 107));

        Paint specialScalePaint = new Paint(mPaint);
        specialScalePaint.setStyle(Paint.Style.FILL);
        specialScalePaint.setStrokeWidth(4f);
        specialScalePaint.setColor(Color.argb(255, 155, 155, 155));

        Paint dirTextPaint = new Paint(mPaint);
        dirTextPaint.setTextSize(40f);

        for (int i = 0; i < 360; i++) {
            if (i % 90 == 0) {
                canvas.drawLine(0, -radius + 15, 0, -radius + 45, specialScalePaint);
                dirTextPaint.setColor(Color.argb(255, 252, 252, 252));

                if (i == 0) {
                    String direction = "N";
                    dirTextPaint.setColor(Color.argb(255, 253, 57, 0));
                    dirTextPaint.getTextBounds(direction, 0, direction.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                } else if (i == 90) {
                    String direction = "E";
                    dirTextPaint.setColor(Color.WHITE);
                    dirTextPaint.getTextBounds(direction, 0, direction.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                } else if (i == 180) {
                    String direction = "S";
                    dirTextPaint.setColor(Color.WHITE);
                    dirTextPaint.getTextBounds(direction, 0, direction.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                } else if (i == 270) {
                    String direction = "W";
                    dirTextPaint.setColor(Color.WHITE);
                    dirTextPaint.getTextBounds(direction, 0, direction.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                }
            } else if (i % 2 == 0) {
                canvas.drawLine(0, -radius + 15, 0, -radius + 45, normalScalePaint);
                if (i % 30 == 0) {
                    Paint normalDirTextPaint = new Paint(mPaint);
                    String text = String.valueOf(i);
                    normalDirTextPaint.setColor(Color.argb(255, 107, 107, 107));
                    normalDirTextPaint.setTextSize(22f);
                    normalDirTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(text, -textWidth / 2, textHeight / 2 - radius + 70, normalDirTextPaint);
                }
            }
            canvas.rotate(1, 0f, 0f);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
