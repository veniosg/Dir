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

package com.veniosg.dir.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.veniosg.dir.R;

import static com.veniosg.dir.view.Themer.getThemedColor;

class RippleEnabledDrawableView extends View {
    // A value of 2 causes the right edge to remain in the same physical position on the screen.
    // We want to follow the direction of the rest of the animations so always use values < 2.
    private static final float BACKGROUND_PROGRESS_FACTOR = 1.5f;

    private Drawable drawable;
    private ColorMatrix saturationMatrix = new ColorMatrix();
    private ColorMatrix blacknessMatrix = new ColorMatrix();

    private Paint backgroundPaint;
    private int initialBackgroundRight;
    private int initialBackgroundLeft;
    private int currentBackgroundLeft;
    private int currentBackgroundRight;
    private int backgroundRadius;
    private int backgroundCenterX;
    private int backgroundCenterY;

    private boolean multiColumnFilelist = false;
    private boolean withBackground = false;

    public RippleEnabledDrawableView(Context context) {
        super(context);

        setWillNotDraw(false);
        setClipToOutline(true);
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        saturationMatrix.setSaturation(1F);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(getThemedColor(getContext(), android.R.attr.colorBackground));
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAntiAlias(true);

        multiColumnFilelist = getResources().getInteger(R.integer.grid_columns) > 1;
    }

    public void setInitialBackgroundPosition(int left, int right) {
        withBackground = true;
        initialBackgroundLeft = left;
        initialBackgroundRight = right;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (drawable != null) {
            if (multiColumnFilelist) {
                canvas.drawCircle(backgroundCenterX, backgroundCenterY, backgroundRadius, backgroundPaint);
            } else {
                canvas.drawColor(backgroundPaint.getColor());
            }
            drawable.draw(canvas);
        }
    }

    public void setBackgroundProgress(float progress) {
        if (drawable != null) {
            progress *= BACKGROUND_PROGRESS_FACTOR;
            currentBackgroundLeft = (int) (initialBackgroundLeft - initialBackgroundLeft * progress);
            currentBackgroundRight = (int) (initialBackgroundRight + (drawable.getIntrinsicWidth() - initialBackgroundRight) * progress);

            // Only needed if drawing a circle background
            backgroundRadius = (currentBackgroundRight - currentBackgroundLeft) / 2;
            backgroundCenterX = currentBackgroundLeft + backgroundRadius;
            backgroundCenterY = getHeight() / 2;

            postInvalidateOnAnimation(currentBackgroundLeft, 0, currentBackgroundRight, getHeight());
        }
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;

        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                if (view instanceof RippleEnabledDrawableView) {
                    if (outline == null) {
                        outline = new Outline();
                    }

                    outline.setRect(0, 0,
                            // Extending outline to the right to avoid shadow glitch
                            ((RippleEnabledDrawableView) view).getDrawableWidth() * 2,
                            ((RippleEnabledDrawableView) view).getDrawableHeight());
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (drawable == null) {
            setMeasuredDimension(0, 0);
        } else {
            // We know the view will never need to be less than its drawable's intrinsic size in this context.
            setMeasuredDimension(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
    }

    protected int getDrawableWidth() {
        if (drawable != null) {
            return drawable.getIntrinsicWidth();
        } else {
            return 0;
        }
    }

    protected int getDrawableHeight() {
        if (drawable != null) {
            return drawable.getIntrinsicHeight();
        } else {
            return 0;
        }
    }

    public void setSaturation(float sat) {
        if (sat < 0)
            sat = 0;

        saturationMatrix.setSaturation(sat);
        if (drawable != null) {
            drawable.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        }
        postInvalidateOnAnimation(0, 0 , getWidth(), getHeight());
    }
}
