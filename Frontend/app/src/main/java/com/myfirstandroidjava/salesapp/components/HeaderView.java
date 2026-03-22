package com.myfirstandroidjava.salesapp.components;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.myfirstandroidjava.salesapp.R;

public class HeaderView extends LinearLayout {
    private TextView tvHeaderTitle;
    private ImageButton btnBack;

    public HeaderView(Context context) {
        super(context);
        init(context, null);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.layout_header, this, true);

        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        btnBack = findViewById(R.id.btnBack);

        // Handle back button click (go back to previous activity)
        btnBack.setOnClickListener(v -> {
            if (context instanceof Activity) {
                ((Activity) context).onBackPressed();
            }
        });

        // Handle custom attribute for title
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HeaderView);
            String title = typedArray.getString(R.styleable.HeaderView_headerTitle);
            if (title != null) {
                tvHeaderTitle.setText(title);
            }
            typedArray.recycle();
        }
    }

    public void setHeaderTitle(String title) {
        tvHeaderTitle.setText(title);
    }

    public void setOnBackClickListener(OnClickListener listener) {
        btnBack.setOnClickListener(listener);
    }
}
