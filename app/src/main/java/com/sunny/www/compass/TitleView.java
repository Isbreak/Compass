package com.sunny.www.compass;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by 67045 on 2018/2/24.
 */

public class TitleView extends FrameLayout {

    private Button mBack;
    private TextView mTitle;
    private OnClickListener listener;

    public TitleView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title, this);

        mBack = findViewById(R.id.back);
        mTitle = findViewById(R.id.title);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }

    public void setBackText(String text) {
        mBack.setText(text);
    }

    public void setBackOnClickListener(OnClickListener listener) {
        this.listener = listener;
        mBack.setOnClickListener(listener);
    }
}
