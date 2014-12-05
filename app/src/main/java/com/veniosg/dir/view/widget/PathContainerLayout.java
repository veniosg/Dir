package com.veniosg.dir.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.veniosg.dir.view.widget.ChildrenChangedListeningLinearLayout;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.getMode;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class PathContainerLayout extends ChildrenChangedListeningLinearLayout {
    private int mMaxChildWidth = -1;

    public PathContainerLayout(Context context) {
        super(context);
    }

    public PathContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PathContainerLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        int childWidthSpec = makeMeasureSpec(0, UNSPECIFIED);
        if (mMaxChildWidth > 0) {
            childWidthSpec = makeMeasureSpec(mMaxChildWidth, AT_MOST);
        }

        child.measure(childWidthSpec, parentHeightMeasureSpec);
    }

    /**
     * Set the maximum width that this layout's children are allowed to have.
     * @param maxChildWidthPx Negative or zero to cancel, positive to set the max width.
     */
    public void setMaxChildWidth(int maxChildWidthPx) {
        this.mMaxChildWidth = maxChildWidthPx;
    }

    public int getMaxChildWidth() {
        return mMaxChildWidth;
    }
}
