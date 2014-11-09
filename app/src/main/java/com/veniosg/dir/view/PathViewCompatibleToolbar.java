package com.veniosg.dir.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toolbar;

import com.veniosg.dir.R;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.veniosg.dir.view.Themer.getThemedDimension;

public class PathViewCompatibleToolbar extends Toolbar {
    public PathViewCompatibleToolbar(Context context) {
        super(context);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View pathView = findViewById(R.id.pathview);
        if (pathView != null) {
            pathView.measure(widthMeasureSpec, makeMeasureSpec(pathView.getMeasuredHeight(), EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        View pathView = findViewById(R.id.pathview);
        if (pathView != null) {
            pathView.layout(0, pathView.getTop(), r, pathView.getBottom());
        }
    }

    public boolean hasPathView() {
        return findViewById(R.id.pathview) != null;
    }
}
