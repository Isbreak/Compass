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
    private float directionAngle = 0;
    private float oldDirectionAngle = 0;
    private int radius;
    private Rect mTextRect;

    public CompassView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mTextRect = new Rect();
    }

    public void setDirectionAngle(float directionAngle) {
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
        Paint outsideArcPaint = new Paint(mPaint);
        outsideArcPaint.setStyle(Paint.Style.STROKE);
        outsideArcPaint.setColor(Color.argb(255, 50, 50, 50));
        outsideArcPaint.setStrokeWidth(4f);
        RectF outsideOval = new RectF(-radius - 90, -radius - 90, radius + 90, radius + 90);
        canvas.drawArc(outsideOval, -82, 140, false, outsideArcPaint);
        canvas.drawArc(outsideOval, 122, 140, false, outsideArcPaint);

        // 画中间角度值
        String angel = String.valueOf((int)directionAngle);
        Paint midAngelPaint = new Paint(mPaint);
        midAngelPaint.setColor(Color.argb(255, 252, 252, 252));
        midAngelPaint.setTextSize(140f);
        midAngelPaint.getTextBounds(angel, 0, angel.length(), mTextRect);
        int width = mTextRect.width();
        int height = mTextRect.height();
        canvas.drawText(angel + "°", -width / 2, height / 2, midAngelPaint);

        // 画圆圈
        // 红色画笔
        Paint redCirclePaint = new Paint(mPaint);
        redCirclePaint.setColor(Color.argb(255, 253, 57, 0));
        redCirclePaint.setStyle(Paint.Style.STROKE);
        redCirclePaint.setStrokeWidth(6f);

        // 灰色画笔
        Paint grayCirclePaint = new Paint(mPaint);
        grayCirclePaint.setColor(Color.argb(255, 155, 155, 155));
        grayCirclePaint.setStyle(Paint.Style.STROKE);
        grayCirclePaint.setStrokeWidth(6f);

        RectF insideOval = new RectF(-radius, -radius, radius, radius);
        if (directionAngle < 180) {
            canvas.drawArc(insideOval, -90 - directionAngle + 6, directionAngle - 1 - 6, false, redCirclePaint);
            canvas.drawArc(insideOval, -90 + 1, 360 - directionAngle - 7, false, grayCirclePaint);
        } else {
            canvas.drawArc(insideOval, -90 + 1, 360 - directionAngle - 7, false, redCirclePaint);
            canvas.drawArc(insideOval, -90 + 360 - directionAngle + 6, directionAngle - 1 - 6, false, grayCirclePaint);
        }

        canvas.rotate(-directionAngle, 0f, 0f);

        // 画红色三角形
        Paint redTrigonPaint = new Paint(mPaint);
        redTrigonPaint.setColor(Color.argb(255, 253, 57, 0));
        Path redTriangle = new Path();
        redTriangle.moveTo(-28f, -radius + 6);
        redTriangle.lineTo(28f, -radius + 6);
        redTriangle.lineTo(0, (float) (-radius - Math.sqrt(56 * 56 - 28 * 28) + 6));
        redTriangle.close();
        canvas.drawPath(redTriangle, redTrigonPaint);

        // 普通刻度
        Paint normalScalePaint = new Paint(mPaint);
        normalScalePaint.setStyle(Paint.Style.FILL);
        normalScalePaint.setStrokeWidth(3f);
        normalScalePaint.setColor(Color.argb(255, 107, 107, 107));
        // 特殊刻度
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
                String text = "N";
                dirTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
                int textWidth = mTextRect.width();
                int textHeight = mTextRect.height();

                if (i == 0) {
                    String direction = "N";
                    dirTextPaint.setColor(Color.argb(255, 253, 57, 0));
                    canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                } else {
                    dirTextPaint.setColor(Color.WHITE);
                    if ((i == 90)) {
                        String direction = "E";
                        canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                    } else if (i == 180) {
                        String direction = "S";
                        canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                    } else if (i == 270) {
                        String direction = "W";
                        canvas.drawText(direction, -textWidth / 2, textHeight / 2 - radius + 75, dirTextPaint);
                    }
                }
            } else if (i % 2 == 0) {
                canvas.drawLine(0, -radius + 15, 0, -radius + 45, normalScalePaint);
                if (i % 30 == 0) {
                    Paint roundAngelTextPaint = new Paint(mPaint);
                    String angelValue = String.valueOf(i);
                    roundAngelTextPaint.setColor(Color.argb(255, 107, 107, 107));
                    roundAngelTextPaint.setTextSize(22f);
                    roundAngelTextPaint.getTextBounds(angelValue, 0, angelValue.length(), mTextRect);
                    int textWidth = mTextRect.width();
                    int textHeight = mTextRect.height();
                    canvas.drawText(angelValue, -textWidth / 2, textHeight / 2 - radius + 70, roundAngelTextPaint);
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
