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

    private Canvas mCanvas;
    private Paint mPaint;
    private float directionAngle = 0;
    private float oldDirectionAngle = 0;
    private Rect mTextRect;

    private int textDirSize = DisplayUtil.sp2px(getContext(), 11);
    private int textMidAngelSize = DisplayUtil.sp2px(getContext(), 51);
    private int textRudAngelSize = DisplayUtil.sp2px(getContext(), 7);
    private int inOvalSize = DisplayUtil.dp2px(getContext(), 113);
    private int outOvalSize = inOvalSize + DisplayUtil.dp2px(getContext(), 34);
    private int inOvalStrokeWidth = DisplayUtil.dp2px(getContext(), 2);
    private int outOvalStrokeWidth = DisplayUtil.dp2px(getContext(), 1);
    private int scaleLength = DisplayUtil.dp2px(getContext(), 11);
    private int normalScaleWidth = DisplayUtil.dp2px(getContext(), 1.1f);
    private int specialScaleWidth = DisplayUtil.dp2px(getContext(), 1.3f);
    private int trigonSize = DisplayUtil.dp2px(getContext(), 19);
    private int spaceSize = DisplayUtil.dp2px(getContext(), 6);


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
        this.mCanvas = canvas;

        mCanvas.drawColor(Color.BLACK);
        mCanvas.translate(mCanvas.getWidth() / 2, mCanvas.getHeight() / 2);

        // 绘制最外部两个圆弧
        drawOutSideArc();

        // 画灰色三角形
        drawGrayTrigon();

        // 画中间角度值
        drawMidAngel();

        // 画内部圆弧
        drawInSideArc();

        mCanvas.rotate(-directionAngle, 0f, 0f);

        // 画红色三角形
        drawRedTrigon();

        // 画表盘内的刻度线，刻度值及方向
        drawOthers();
    }

    // 绘制最外部两个圆弧
    private void drawOutSideArc() {
        Paint outsideArcPaint = new Paint(mPaint);
        outsideArcPaint.setStyle(Paint.Style.STROKE);
        outsideArcPaint.setColor(Color.argb(255, 50, 50, 50));
        outsideArcPaint.setStrokeWidth(outOvalStrokeWidth);
        RectF outsideOval = new RectF(-outOvalSize, -outOvalSize, outOvalSize, outOvalSize);
        mCanvas.drawArc(outsideOval, -83, 140, false, outsideArcPaint);
        mCanvas.drawArc(outsideOval, 123, 140, false, outsideArcPaint);
    }

    // 画灰色三角形
    private void drawGrayTrigon() {
        Paint grayTrigonPaint = new Paint(mPaint);
        grayTrigonPaint.setColor(Color.argb(255, 50, 50, 50));
        Path grayTriangle = new Path();
        grayTriangle.moveTo(-trigonSize / 2, -outOvalSize + outOvalStrokeWidth);
        grayTriangle.lineTo(trigonSize / 2, -outOvalSize + outOvalStrokeWidth);
        grayTriangle.lineTo(0, -outOvalSize - (int) (Math.sqrt(trigonSize * trigonSize - trigonSize / 2 * trigonSize / 2) + 0.5) + outOvalStrokeWidth);
        grayTriangle.close();
        mCanvas.drawPath(grayTriangle, grayTrigonPaint);
    }

    // 画中间角度值
    private void drawMidAngel() {
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
        mCanvas.drawText(angel + "°", -width / 2 - signWidth / 4, height / 2, midAngelPaint);
    }


    // 画内部圆弧
    private void drawInSideArc() {
        // 红色画笔
        Paint redArcPaint = new Paint(mPaint);
        redArcPaint.setColor(Color.argb(255, 253, 57, 0));
        redArcPaint.setStyle(Paint.Style.STROKE);
        redArcPaint.setStrokeWidth(inOvalStrokeWidth);

        // 灰色画笔
        Paint grayArcPaint = new Paint(mPaint);
        grayArcPaint.setColor(Color.argb(255, 155, 155, 155));
        grayArcPaint.setStyle(Paint.Style.STROKE);
        grayArcPaint.setStrokeWidth(inOvalStrokeWidth);

        RectF insideOval = new RectF(-inOvalSize, -inOvalSize, inOvalSize, inOvalSize);

        if (directionAngle < 180) {
            if (directionAngle > 7) {
                mCanvas.drawArc(insideOval, -90 - directionAngle + 6, directionAngle - 1 - 6, false, redArcPaint);
            }

            if (directionAngle <= 4) {
                mCanvas.drawArc(insideOval, -90 + 5 - directionAngle, 349, false, grayArcPaint);
            } else {
                mCanvas.drawArc(insideOval, -90 + 1, 360 - directionAngle - 7, false, grayArcPaint);
            }

        } else {
            if (directionAngle < 353) {
                mCanvas.drawArc(insideOval, -90 + 1, 360 - directionAngle - 7, false, redArcPaint);
            }

            if (directionAngle >= 354) {
                mCanvas.drawArc(insideOval, -90 + 5 + 360 - directionAngle, 349, false, grayArcPaint);
            } else {
                mCanvas.drawArc(insideOval, -90 + 360 - directionAngle + 6, directionAngle - 1 - 6, false, grayArcPaint);
            }

        }

    }

    // 画红色三角形
    private void drawRedTrigon() {
        Paint redTrigonPaint = new Paint(mPaint);
        redTrigonPaint.setColor(Color.argb(255, 253, 57, 0));
        Path redTriangle = new Path();
        redTriangle.moveTo(-trigonSize / 2, -inOvalSize + inOvalStrokeWidth);
        redTriangle.lineTo(trigonSize / 2, -inOvalSize + inOvalStrokeWidth);
        redTriangle.lineTo(0, -inOvalSize - (int) (Math.sqrt(trigonSize * trigonSize - trigonSize / 2 * trigonSize / 2) + 0.5) + inOvalStrokeWidth);
        redTriangle.close();
        mCanvas.drawPath(redTriangle, redTrigonPaint);
    }

    // 画表盘内的刻度线，刻度值及方向
    private void drawOthers() {
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

        // 表盘圈内的东西南北
        Paint dirTextPaint = new Paint(mPaint);
        dirTextPaint.setTextSize(textDirSize);

        for (int i = 0; i < 360; i++) {
            if (i % 90 == 0) {
                mCanvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, specialScalePaint);
                dirTextPaint.setColor(Color.argb(255, 252, 252, 252));
                String text = "北";
                dirTextPaint.getTextBounds(text, 0, text.length(), mTextRect);
                int textWidth = mTextRect.width();
                int textHeight = mTextRect.height();

                if (i == 0) {
                    String direction = "北";
                    dirTextPaint.setColor(Color.argb(255, 253, 57, 0));
                    mCanvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                } else {
                    dirTextPaint.setColor(Color.WHITE);
                    if ((i == 90)) {
                        String direction = "东";
                        mCanvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    } else if (i == 180) {
                        String direction = "南";
                        mCanvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    } else if (i == 270) {
                        String direction = "西";
                        mCanvas.drawText(direction, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), dirTextPaint);
                    }
                }
            } else if (i % 30 == 0) {
                mCanvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, specialScalePaint);
                Paint roundAngelTextPaint = new Paint(mPaint);
                String angelValue = String.valueOf(i);
                roundAngelTextPaint.setColor(Color.argb(255, 107, 107, 107));
                roundAngelTextPaint.setTextSize(textRudAngelSize);
                roundAngelTextPaint.getTextBounds(angelValue, 0, angelValue.length(), mTextRect);
                int textWidth = mTextRect.width();
                int textHeight = mTextRect.height();
                mCanvas.drawText(angelValue, -textWidth / 2, textHeight - inOvalSize + scaleLength + (int) (spaceSize * 1.6 + 0.5), roundAngelTextPaint);

            } else if (i % 2 == 0) {
                mCanvas.drawLine(0, -inOvalSize + spaceSize, 0, -inOvalSize + scaleLength + spaceSize, normalScalePaint);
            }
            mCanvas.rotate(1, 0f, 0f);
        }
    }

}
