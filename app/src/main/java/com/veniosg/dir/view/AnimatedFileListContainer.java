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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.veniosg.dir.R;
import com.veniosg.dir.AnimationConstants;
import com.veniosg.dir.util.Logger;

/**
 * @author George Venios
 */
public class AnimatedFileListContainer extends FrameLayout {
    private static final int INDEX_CONTENT = 0;

    private BitmapDrawable mScreenshot;
    private float mDimAmount = 0;
    private int mDimColor = getResources().getColor(
            Themer.getThemedResourceId(getContext(), R.attr.colorFadeCovered));

    public AnimatedFileListContainer(Context context) {
        super(context);
    }

    public AnimatedFileListContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimatedFileListContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Call this before changing the content! <br/>
     * @param direction 1 for forward, -1 for backward, 0 is undefined.
     */
    public void setupAnimations(int direction) {
        View oldView = getChildAt(INDEX_CONTENT);

        Bitmap cache = Bitmap.createBitmap(oldView.getWidth(), oldView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(cache);
        if (direction < 0)
            getBackground().draw(canvas);
        oldView.draw(canvas);

        mScreenshot = new BitmapDrawable(getResources(), cache);
        mScreenshot.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());

        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Recorded drawable");
    }

    public void animateFwd(int fromWidth, int fromHeight, int fromLeftInParent, int fromTopInParent) {
        if(mScreenshot == null) {
            return;
        }

        final View newContent = getChildAt(INDEX_CONTENT);
        final DrawableView oldContent = new DrawableView(getContext());
        AnimatorSet set = new AnimatorSet();
        int toWidth = newContent.getWidth();
        int toHeight = newContent.getHeight();

        // Add and show last list state
        oldContent.setDrawable(mScreenshot);
        addView(oldContent, 0);
        newContent.setAlpha(0);
        newContent.setBackgroundDrawable(getBackground().mutate());
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Added new view. New count: " + getChildCount());

        // Init animation
        int animDuration = AnimationConstants.ANIM_DURATION;

        ObjectAnimator anim = ObjectAnimator.ofFloat(oldContent, "scaleX", 1F, 0.9F);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(oldContent, "scaleY", 1F, 0.9F);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(oldContent, "saturation", 1F, -3F);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(newContent, "translationX", newContent.getWidth(), 0);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(newContent, "alpha", 0.3F, 1F);
        ObjectAnimator anim6 = ObjectAnimator.ofFloat(this, "backgroundDim", 0F, 0.5F);

        anim.setInterpolator(new AccelerateInterpolator());
        anim2.setInterpolator(new AccelerateInterpolator());
        anim3.setInterpolator(new AccelerateInterpolator());
        anim4.setInterpolator(new DecelerateInterpolator());
        anim5.setInterpolator(new DecelerateInterpolator());
        anim6.setInterpolator(new DecelerateInterpolator());
        set.setDuration(animDuration);
        set.setStartDelay(AnimationConstants.ANIM_START_DELAY);

        set.playTogether(
                anim,
                anim2,
                anim3,
                anim4,
                anim5,
                anim6
        );
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Started animation");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);

                try {
                    removeView(oldContent);
                    Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Removed view. New count: " + getChildCount());

                    clearAnimations();
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Finished animation");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeListener(this);

                try {
                    removeView(oldContent);
                    Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Removed view. New count: " + getChildCount());

                    clearAnimations();
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Cancelled animation");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Play animation
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Starting animation");
        set.start();
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean result = super.drawChild(canvas, child, drawingTime);

        // Draw dim above second-to-last child as it's getting dimmed by the last child.
        if (mDimAmount > 0 && getChildCount() > 1 && indexOfChild(child) == getChildCount()-2) {
            canvas.drawColor(computeDarkenColor(), PorterDuff.Mode.SRC_OVER);
        }

        return result;
    }

    public void animateBwd() {
        if(mScreenshot == null) {
            return;
        }

        final View newContent = getChildAt(INDEX_CONTENT);
        final DrawableView oldContent = new DrawableView(getContext());
        AnimatorSet set = new AnimatorSet();

        oldContent.setDrawable(mScreenshot);
        addView(oldContent);    // Since this is the last child, it'll be overlaid on top of the others
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Added new view. New count: " + getChildCount());

        // Init animation
        int animDuration = AnimationConstants.ANIM_DURATION;

        ObjectAnimator anim = ObjectAnimator.ofFloat(newContent, "scaleX", 0.9F, 1F);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(newContent, "scaleY", 0.9F, 1F);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(oldContent, "translationX", 0, newContent.getWidth());
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(oldContent, "alpha", 1F, 0.3F);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(this, "backgroundDim", 0.5F, 0F);

        anim.setInterpolator(new DecelerateInterpolator());
        anim2.setInterpolator(new DecelerateInterpolator());
        anim3.setInterpolator(new AccelerateInterpolator());
        anim4.setInterpolator(new AccelerateInterpolator());
        anim5.setInterpolator(new AccelerateInterpolator());
        set.setDuration(animDuration);

        set.playTogether(
                anim,
                anim2,
                anim3,
                anim4,
                anim5
        );
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Started animation");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);

                try {
                    removeView(oldContent);
                    Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Removed view. New count: " + getChildCount());

                    clearAnimations();
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Finished animation");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeListener(this);

                try {
                    removeView(oldContent);
                    Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Removed view. New count: " + getChildCount());

                    clearAnimations();
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Cancelled animation");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        // Play animation
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Starting animation");
        set.start();
    }

    public void clearAnimations() {
        mScreenshot = null;
        setBackgroundDim(0);
        getChildAt(INDEX_CONTENT).setBackgroundDrawable(null);
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Cleared animations");
    }

    public void setBackgroundDim(float dim) {
        mDimAmount = dim;
        invalidate();
    }

    public float getBackgroundDim() {
        return mDimAmount;
    }

    public int computeDarkenColor() {
        final int baseAlpha = (mDimColor & 0xff000000) >>> 24;
        int imag = (int) (baseAlpha * mDimAmount);
        int color = imag << 24 | (mDimColor & 0xffffff);
        return color;
    }

    private class DrawableView extends View {
        private Drawable drawable;
        private int overlayColor;
        private ColorMatrix saturationMatrix = new ColorMatrix();
        private ColorMatrix blacknessMatrix = new ColorMatrix();

        public DrawableView(Context context) {
            super(context);

            setWillNotDraw(false);
            setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            saturationMatrix.setSaturation(1F);
        }

        @Override
        public void draw(Canvas canvas) {
            drawable.draw(canvas);
        }

        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }

        public void setOverlayColor(int color) {
            overlayColor = color;
        }

        public void setSaturation(float sat) {
            if(sat < 0)
                sat = 0;

            saturationMatrix.setSaturation(sat);
            drawable.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
            invalidate();
        }
    }
}
