package com.sunny.www.compass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by 67045 on 2018/2/23.
 */

public class MyView extends View {

    public MyView(Context context) {
        super(context);
    }

    public MyView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        canvas.drawColor(Color.WHITE);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(15);

        canvas.drawCircle(200, 200, 100, paint);

        canvas.drawRect(100, 400, 400, 600, paint);

        canvas.drawRoundRect(100, 700, 400, 900, 20, 20, paint);

        canvas.drawOval(100, 1000, 400, 1200, paint);

        canvas.drawArc(100, 1300, 400, 1500, 0, 120, false, paint);

        paint.setTextSize(100);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText("Hello World", 100, 1700, paint);
    }
}
