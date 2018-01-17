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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import static android.R.attr.listDivider;
import static com.veniosg.dir.android.ui.Themer.getThemedResourceId;

public class DividerGridView extends GridView {
    private int mDividerSize;
    private Drawable mDivider;

    public DividerGridView(Context context) {
        super(context);
        init(context);
    }

    public DividerGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DividerGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mDividerSize = 1;
        //noinspection deprecation
        mDivider = context.getResources().getDrawable(getThemedResourceId(context, listDivider));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        double count = getChildCount();
        int bottom, top, right;
        int numColumns = getNumColumns();
        Rect bounds = new Rect();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            int effectivePaddingBottom = getPaddingBottom();    // Correct only when clipToPadding is enabled
            final int listBottom = getBottom() - getTop() - effectivePaddingBottom + getScrollY();
            top = child.getTop();
            bottom = child.getBottom();
            right = child.getRight();

            // Not the rightmost child
            if (i % numColumns != numColumns - 1) {
                bounds.top = top + child.getPaddingTop();
                bounds.bottom = bottom - child.getPaddingBottom();
                bounds.right = right;
                bounds.left = right - mDividerSize;
                drawDivider(canvas, bounds);
            }
            if (bottom < listBottom) {
                bounds.top = bottom;
                bounds.bottom = bottom + mDividerSize;
                bounds.left = child.getLeft() + child.getPaddingLeft();
                bounds.right = child.getRight() - child.getPaddingRight();
                drawDivider(canvas, bounds);
            }
        }

        super.dispatchDraw(canvas);
    }

    private void drawDivider(Canvas canvas, Rect bounds) {
        mDivider.setBounds(bounds);
        mDivider.draw(canvas);
    }
}
