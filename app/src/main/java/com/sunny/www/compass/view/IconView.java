package com.sunny.www.compass.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.sunny.www.compass.utils.DisplayUtil;

/**
 * Created by 67045 on 2018/2/26.
 */
public class IconView extends View {

    private Canvas mCanvas;
    private Rect mTextRect;

    /**
     * dp 转 px，尺寸适配
     */
    private int textMidAngleSize = DisplayUtil.sp2px(getContext(), 150);

    private int inOvalSize = DisplayUtil.dp2px(getContext(), 100);
    private int inOvalStrokeWidth = DisplayUtil.dp2px(getContext(), 4);

    private int triangleSize = DisplayUtil.dp2px(getContext(), 40);
    private int blackTriangleSize = DisplayUtil.dp2px(getContext(), 45);

    public IconView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mTextRect = new Rect();
    }

    public void setDirectionAngle(float directionAngle) {

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

        drawMidAngle();

        drawInSideArc();

        drawRedTriangle();
    }

    private void drawMidAngle() {
        String angle = "N";
        Paint midAnglePaint = new Paint();
//        Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/fontnumber_light.ttf");
//        midAnglePaint.setTypeface(typeface);
        midAnglePaint.setAntiAlias(true);
        midAnglePaint.setColor(Color.argb(255, 245, 245, 245));
        midAnglePaint.setTextSize(textMidAngleSize);
        midAnglePaint.getTextBounds(angle, 0, angle.length(), mTextRect);
        Paint.FontMetricsInt fm = midAnglePaint.getFontMetricsInt();
        float height = -fm.descent + (fm.descent - fm.ascent) / 2;
        float width = midAnglePaint.measureText(angle);
        mCanvas.drawText(angle, -width / 2, height, midAnglePaint);
    }

    private void drawInSideArc() {
        Paint redArcPaint = new Paint();
        redArcPaint.setAntiAlias(true);
        redArcPaint.setColor(Color.argb(255, 253, 57, 0));
        redArcPaint.setStyle(Paint.Style.STROKE);
        redArcPaint.setStrokeWidth(inOvalStrokeWidth);

        Paint grayArcPaint = new Paint();
        grayArcPaint.setAntiAlias(true);
        grayArcPaint.setColor(Color.argb(255, 155, 155, 155));
        grayArcPaint.setStyle(Paint.Style.STROKE);
        grayArcPaint.setStrokeWidth(inOvalStrokeWidth);

        RectF insideOval = new RectF(-inOvalSize, -inOvalSize, inOvalSize, inOvalSize);

        float angle = 180;
        mCanvas.drawArc(insideOval, angle, 270 - angle, false, redArcPaint);
        mCanvas.drawArc(insideOval, -90, 270, false, grayArcPaint);
    }

    private void drawRedTriangle() {
        mCanvas.rotate(-45, 0f, 0f);

        Paint blackTrianglePaint = new Paint();
        blackTrianglePaint.setAntiAlias(true);
        blackTrianglePaint.setColor(Color.argb(255, 0, 0, 0));
        Path blackTriangle = new Path();
        blackTriangle.moveTo(-blackTriangleSize / 2, -inOvalSize + inOvalStrokeWidth);
        blackTriangle.lineTo(blackTriangleSize / 2, -inOvalSize + inOvalStrokeWidth);
        blackTriangle.lineTo(0, -inOvalSize - (int) (Math.sqrt(blackTriangleSize * blackTriangleSize - blackTriangleSize / 2 * blackTriangleSize / 2) + 0.5) + inOvalStrokeWidth);
        blackTriangle.close();
        mCanvas.drawPath(blackTriangle, blackTrianglePaint);

        Paint redTrianglePaint = new Paint();
        redTrianglePaint.setAntiAlias(true);
        redTrianglePaint.setColor(Color.argb(255, 253, 57, 0));
        Path redTriangle = new Path();
        redTriangle.moveTo(-triangleSize / 2, -inOvalSize + inOvalStrokeWidth);
        redTriangle.lineTo(triangleSize / 2, -inOvalSize + inOvalStrokeWidth);
        redTriangle.lineTo(0, -inOvalSize - (int) (Math.sqrt(triangleSize * triangleSize - triangleSize / 2 * triangleSize / 2) + 0.5) + inOvalStrokeWidth);
        redTriangle.close();
        mCanvas.drawPath(redTriangle, redTrianglePaint);
    }
}
