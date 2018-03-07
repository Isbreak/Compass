package com.sunny.www.compass.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sunny.www.compass.utils.DisplayUtil;

/**
 * Created by 67045 on 2018/2/26.
 * 自定义View实现指南针转盘
 */
public class CompassView extends View {

    private Paint mPaint;
    private float directionAngle = 0;
    private float oldDirectionAngle = 0;
    private Rect mTextRect;

    private int textDirSize = DisplayUtil.px2dp(getContext(), 102f);
    private int textMidAngelSize = DisplayUtil.px2dp(getContext(), 460f);
    private int textRudAngelSize = DisplayUtil.px2dp(getContext(), 66f);
    private int inOvalSize = DisplayUtil.px2dp(getContext(), 1020f);
    private int outOvalSize = inOvalSize + DisplayUtil.px2dp(getContext(), 260f);
    private int inOvalStrokeWidth = DisplayUtil.px2dp(getContext(), 14f);
    private int outOvalStrokeWidth = DisplayUtil.px2dp(getContext(), 12f);
    private int scaleLength = DisplayUtil.px2dp(getContext(), 102f);
    private int normalScaleWidth = DisplayUtil.px2dp(getContext(), 7f);
    private int specialScaleWidth = DisplayUtil.px2dp(getContext(), 10f);
    private int trigonSize = DisplayUtil.px2dp(getContext(), 168f);
    private int spaceSize = DisplayUtil.px2dp(getContext(), 50f);

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.BLACK);
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);

        // 绘制最外部两个圆弧
        Paint outsideArcPaint = new Paint(mPaint);
        outsideArcPaint.setStyle(Paint.Style.STROKE);
        outsideArcPaint.setColor(Color.argb(255, 50, 50, 50));
        outsideArcPaint.setStrokeWidth(outOvalStrokeWidth);
        RectF outsideOval = new RectF(-outOvalSize, -outOvalSize, outOvalSize, outOvalSize);
        canvas.drawArc(outsideOval, -83, 140, false, outsideArcPaint);
        canvas.drawArc(outsideOval, 123, 140, false, outsideArcPaint);

        // 画灰色三角形
        Paint grayTrigonPaint = new Paint(mPaint);
        grayTrigonPaint.setColor(Color.argb(255, 50, 50, 50));
        Path grayTriangle = new Path();
        grayTriangle.moveTo(-trigonSize / 2, -outOvalSize);
        grayTriangle.lineTo(trigonSize / 2, -outOvalSize);
        grayTriangle.lineTo(0, -outOvalSize - (int) (Math.sqrt(trigonSize * trigonSize - trigonSize / 2 * trigonSize / 2) + 0.5));
        grayTriangle.close();
        canvas.drawPath(grayTriangle, grayTrigonPaint);

        // 画中间角度值
        String angel = String.valueOf((int) directionAngle);

        Paint midAngelPaint = new Paint(mPaint);
        String sign = "°";

        midAngelPaint.setColor(Color.argb(255, 252, 252, 252));
        midAngelPaint.setTextSize(textMidAngelSize);
        midAngelPaint.getTextBounds(angel, 0, sign.length(), mTextRect);
        int signWidth = mTextRect.width();

        midAngelPaint.getTextBounds(angel, 0, angel.length(), mTextRect);
        int width = mTextRect.width();
        int height = mTextRect.height();
        canvas.drawText(angel + "°", -width / 2 - signWidth / 4, height / 2, midAngelPaint);

        // 画圆圈
        // 红色画笔
        Paint redCirclePaint = new Paint(mPaint);
        redCirclePaint.setColor(Color.argb(255, 253, 57, 0));
        redCirclePaint.setStyle(Paint.Style.STROKE);
        redCirclePaint.setStrokeWidth(inOvalStrokeWidth);

        // 灰色画笔
        Paint grayCirclePaint = new Paint(mPaint);
        grayCirclePaint.setColor(Color.argb(255, 155, 155, 155));
        grayCirclePaint.setStyle(Paint.Style.STROKE);
        grayCirclePaint.setStrokeWidth(inOvalStrokeWidth);

        RectF insideOval = new RectF(-inOvalSize, -inOvalSize, inOvalSize, inOvalSize);
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
        redTriangle.moveTo(-trigonSize / 2, -inOvalSize);
        redTriangle.lineTo(trigonSize / 2, -inOvalSize);
        redTriangle.lineTo(0, -inOvalSize - (int) (Math.sqrt(trigonSize * trigonSize - trigonSize / 2 * trigonSize / 2) + 0.5));
        redTriangle.close();
        canvas.drawPath(redTriangle, redTrigonPaint);

        // 普通刻度
        Paint normalScalePaint = new Paint(mPaint);
        normalScalePaint.setStyle(Paint.Style.FILL);
        normalScalePaint.setStrokeWidth(normalScaleWidth);
        normalScalePaint.setColor(Color.argb(255, 107, 107, 107));
        // 特殊刻度
        Paint specialScalePaint = new Paint(mPaint);
        specialScalePaint.setStyle(Paint.Style.FILL);
        specialScalePaint.setStrokeWidth(specialScaleWidth);
        specialScalePaint.setColor(Color.argb(255, 155, 155, 155));

        Paint dirTextPaint = new Paint(mPaint);
        dirTextPaint.setTextSize(textDirSize);

        for (int i = 0; i < 360; i++) {
            if (i % 90 == 0) {
                canvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, specialScalePaint);
                dirTextPaint.setColor(Color.argb(255, 252, 252, 252));
                String text = "北";
                dirTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
                int textWidth = mTextRect.width();
                int textHeight = mTextRect.height();

                if (i == 0) {
                    String direction = "北";
                    dirTextPaint.setColor(Color.argb(255, 253, 57, 0));
                    canvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                } else {
                    dirTextPaint.setColor(Color.WHITE);
                    if ((i == 90)) {
                        String direction = "东";
                        canvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    } else if (i == 180) {
                        String direction = "南";
                        canvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    } else if (i == 270) {
                        String direction = "西";
                        canvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    }
                }
            } else if (i % 30 == 0) {
                canvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, specialScalePaint);
                Paint roundAngelTextPaint = new Paint(mPaint);
                String angelValue = String.valueOf(i);
                roundAngelTextPaint.setColor(Color.argb(255, 107, 107, 107));
                roundAngelTextPaint.setTextSize(textRudAngelSize);
                roundAngelTextPaint.getTextBounds(angelValue, 0, angelValue.length(), mTextRect);
                int textWidth = mTextRect.width();
                int textHeight = mTextRect.height();
                canvas.drawText(angelValue, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), roundAngelTextPaint);

            } else if (i % 2 == 0) {
                canvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, normalScalePaint);
            }
            canvas.rotate(1, 0f, 0f);
        }
    }
}
