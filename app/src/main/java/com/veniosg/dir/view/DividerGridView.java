/*
 * Copyright (C) 2014 George Venios
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

package com.veniosg.dir.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;

import static android.R.attr.listDivider;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

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
        mDivider = context.getResources().getDrawable(getThemedResourceId(context, listDivider));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        double count = getChildCount();
        int bottom, top, right;
        int numColumns = getNumColumns();
        Rect bounds = new Rect();

        for (int i = 0; i < count; i++) {
            final int itemIndex = (getFirstVisiblePosition() + i);
            final View child = getChildAt(i);
            final boolean isLastItem = (i == (count - 1));
            // Pretent that we know this to be true. Was: if (mGroupFlags & CLIP_TO_PADDING_MASK) == CLIP_TO_PADDING_MASK)
            int effectivePaddingTop = getPaddingTop();
            int effectivePaddingBottom = getPaddingBottom();
            final int listBottom = getBottom() - getTop() - effectivePaddingBottom + getScrollY();
            top =  child.getTop();
            bottom = child.getBottom();
            right = child.getRight();

            // Not the rightmost child
            if (i % numColumns != numColumns-1) {
                bounds.top = top + child.getPaddingTop();
                bounds.bottom = bottom - child.getPaddingBottom();
                bounds.right = right;
                bounds.left = right - mDividerSize;
                drawDivider(canvas, bounds, i);
            }
            if (bottom < listBottom) {
                final int nextIndex = (itemIndex + 1);
                bounds.top = bottom;
                bounds.bottom = bottom + mDividerSize;
                bounds.left = child.getLeft() + child.getPaddingLeft();
                bounds.right = child.getRight() - child.getPaddingRight();
                drawDivider(canvas, bounds, i);
            }
        }

        super.dispatchDraw(canvas);
    }

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        mDivider.setBounds(bounds);
        mDivider.draw(canvas);
    }
}
