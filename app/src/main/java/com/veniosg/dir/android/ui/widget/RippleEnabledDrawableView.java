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

package com.veniosg.dir.android.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import com.veniosg.dir.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.veniosg.dir.android.ui.Themer.getThemedColor;

class RippleEnabledDrawableView extends View {
    // A value of 2 causes the right edge to remain in the same physical position on the screen.
    // We want to follow the direction of the rest of the animations so always use values < 2.
    private static final float BACKGROUND_PROGRESS_FACTOR = 1.5f;

    private Drawable mDrawable;
    private Paint mBackgroundPaint;
    private int mInitialBackgroundRight;
    private int mInitialBackgroundLeft;
    private int mBackgroundRadius;
    private int mBackgroundCenterX;
    private int mBackgroundCenterY;

    private final boolean mDrawDynamicProtection = getResources().getInteger(R.integer.grid_columns) > 1;

    public RippleEnabledDrawableView(Context context) {
        super(context);

        setWillNotDraw(false);
        setClipToOutline(true);
        setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(getThemedColor(getContext(), android.R.attr.colorBackground));
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mDrawable == null) {
            setMeasuredDimension(0, 0);
        } else {
            // We know the view will never need to be less than its drawable's intrinsic size in this context.
            setMeasuredDimension(mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawable != null) {
            if (mDrawDynamicProtection) {
                canvas.drawCircle(mBackgroundCenterX, mBackgroundCenterY, mBackgroundRadius, mBackgroundPaint);
            } else {
                canvas.drawColor(mBackgroundPaint.getColor());
            }
            mDrawable.draw(canvas);
        }
    }

    void setInitialBackgroundPosition(int left, int right) {
        mInitialBackgroundLeft = left;
        mInitialBackgroundRight = right;
    }

    @SuppressWarnings("unused")
    public void setBackgroundProgress(float progress) {
        if (mDrawable != null) {
            progress *= BACKGROUND_PROGRESS_FACTOR;
            int currentBackgroundLeft = (int) (mInitialBackgroundLeft - mInitialBackgroundLeft * progress);
            int currentBackgroundRight = (int) (mInitialBackgroundRight + (mDrawable.getIntrinsicWidth() - mInitialBackgroundRight) * progress);

            // Only needed if drawing a circle background
            mBackgroundRadius = (currentBackgroundRight - currentBackgroundLeft) / 2;
            mBackgroundCenterX = currentBackgroundLeft + mBackgroundRadius;
            mBackgroundCenterY = getHeight() / 2;

            postInvalidateOnAnimation(currentBackgroundLeft, 0, currentBackgroundRight, getHeight());
        }
    }

    public void setDrawable(Drawable drawable) {
        if (mDrawable != null && mDrawable instanceof BitmapDrawable) {
            ((BitmapDrawable) mDrawable).getBitmap().recycle();
        }

        this.mDrawable = drawable;

        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (view instanceof RippleEnabledDrawableView) {
                    if (outline == null) {
                        outline = new Outline();
                    }

                    outline.setRect(0, 0,
                            // Extending outline to the right to avoid shadow glitch
                            (int) (((RippleEnabledDrawableView) view).getDrawableWidth() * 1.05f),
                            ((RippleEnabledDrawableView) view).getDrawableHeight());
                }
            }
        });
    }

    @SuppressWarnings("WeakerAccess")
    int getDrawableWidth() {
        if (mDrawable != null) {
            return mDrawable.getIntrinsicWidth();
        } else {
            return 0;
        }
    }

    int getDrawableHeight() {
        if (mDrawable != null) {
            return mDrawable.getIntrinsicHeight();
        } else {
            return 0;
        }
    }
}
