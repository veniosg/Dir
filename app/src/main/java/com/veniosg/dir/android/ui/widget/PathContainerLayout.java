/*
 * Copyright (C) 2014-2016 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class PathContainerLayout extends ChildrenChangedListeningLinearLayout {
    private int mMaxChildWidth = 0;

    public PathContainerLayout(Context context) {
        super(context);
    }

    public PathContainerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathContainerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    void setMeasuredWidth(int width) {
        setMeasuredDimension(width, getMeasuredHeight());
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
     *
     * @param maxChildWidthPx Negative or zero to cancel, positive to set the max width.
     */
    @SuppressWarnings("WeakerAccess")
    public void setMaxChildWidth(int maxChildWidthPx) {
        this.mMaxChildWidth = maxChildWidthPx;
    }

    @SuppressWarnings("unused")
    public int getMaxChildWidth() {
        return mMaxChildWidth;
    }
}
