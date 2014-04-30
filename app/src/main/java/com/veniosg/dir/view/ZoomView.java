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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.veniosg.dir.util.Logger;

/**
 * @author George Venios
 */
public class ZoomView extends FrameLayout {
    private static final int INDEX_CONTENT = 0;

    private Picture mScreenshot;

    public ZoomView(Context context) {
        super(context);
    }

    public ZoomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void saveScreenshot() {
        View oldView = getChildAt(INDEX_CONTENT);
        mScreenshot = new Picture();
        Canvas tmpCanvas = mScreenshot.beginRecording(oldView.getWidth(), oldView.getHeight());
        oldView.draw(tmpCanvas);
        mScreenshot.endRecording();
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Recorded picture");
    }

    public void doZoomIn(int fromWidth, int fromHeight, int fromLeftInParent, int fromTopInParent) {
        if(mScreenshot == null) {
            return;
        }

        final View newContent = getChildAt(INDEX_CONTENT);
        final PictureView oldContent = new PictureView(getContext());
        ColorDrawable newContentBackground = new ColorDrawable(Color.BLACK);
        newContentBackground.setAlpha(0);
        AnimatorSet set = new AnimatorSet();
        int toWidth = newContent.getWidth();
        int toHeight = newContent.getHeight();

        // Add and show last list state
        oldContent.setPicture(mScreenshot);
        addView(oldContent);    // Since this is the last child, it'll be overlaid on top of the others
        newContent.setAlpha(0);
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Added new view. New count: " + getChildCount());
        newContent.setBackgroundDrawable(newContentBackground);

        // Init animation
        int animDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        float scaleX;
        float scaleY;

        if (fromWidth > 0 && fromHeight > 0) {
            scaleX = (float) fromWidth / (float) toWidth;
            scaleY = (float) fromHeight / (float) toHeight;

            oldContent.setPivotX(fromLeftInParent + fromWidth / 2);
            oldContent.setPivotY(fromTopInParent + fromHeight / 2);

            if (newContent.getHeight() > newContent.getWidth()) {
                scaleX = scaleY;
            } else {
                scaleY = scaleX;
            }

            // Make scaling a bit less prominent
            if (scaleX < 0.3F) {
                scaleX = scaleY = 0.3F;
            }
        } else {
            scaleX = scaleY = 0.5F;
        }

        ObjectAnimator anim = ObjectAnimator.ofFloat(oldContent, "alpha", 1F, 0F);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(oldContent, "scaleX", 1F, 1F/scaleX);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(oldContent, "scaleY", 1F, 1F/scaleY);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(newContent, "alpha", 0F, 1F);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(newContent, "scaleX", 0.6F, 1F);
        ObjectAnimator anim6 = ObjectAnimator.ofFloat(newContent, "scaleY", 0.6F, 1F);
        ObjectAnimator anim7 = ObjectAnimator.ofInt(newContentBackground, "alpha", 40, 0);

        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(animDuration);
        anim4.setStartDelay((long) (animDuration * 0.5F));

        set.playTogether(anim, anim2, anim3, anim4, anim5, anim6, anim7);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Started animation");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);
                clearZoom();

                try {
                    removeView(oldContent);
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Finished animation");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeListener(this);
                clearZoom();

                try {
                    removeView(oldContent);
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

    public void doZoomOut() {
        if(mScreenshot == null) {
            return;
        }

        final View newContent = getChildAt(INDEX_CONTENT);
        final PictureView oldContent = new PictureView(getContext());
        AnimatorSet set = new AnimatorSet();

        // Add and show last list state
        oldContent.setPicture(mScreenshot);
        addView(oldContent);    // Since this is the last child, it'll be overlaid on top of the others
        newContent.setAlpha(0);
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Added new view. New count: " + getChildCount());

        // Init animation
        int animDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
        float scaleX = 0.3F;
        float scaleY = 0.3F;

        ObjectAnimator anim = ObjectAnimator.ofFloat(oldContent, "alpha", 1F, 0F).setDuration((long) (animDuration * 0.5));
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(oldContent, "scaleX", 1F, 0.6F);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(oldContent, "scaleY", 1F, 0.6F);
        ObjectAnimator anim4 = ObjectAnimator.ofFloat(newContent, "alpha", 0F, 1F);
        ObjectAnimator anim5 = ObjectAnimator.ofFloat(newContent, "scaleX", 1/scaleX, 1F);
        ObjectAnimator anim6 = ObjectAnimator.ofFloat(newContent, "scaleY", 1 / scaleY, 1F);

        set.setInterpolator(new AccelerateInterpolator());

        anim2.setDuration(animDuration);
        anim3.setDuration(animDuration);
        anim4.setDuration(animDuration);
        anim5.setDuration(animDuration);
        anim6.setDuration(animDuration);

        set.playTogether(anim, anim2, anim3, anim4, anim5, anim6);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Started animation");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeListener(this);
                clearZoom();

                try {
                    removeView(oldContent);
                } catch (Throwable t) {
                    Logger.log(t);
                }
                Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Finished animation");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeListener(this);
                clearZoom();

                try {
                    removeView(oldContent);
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

    public void clearZoom() {
        mScreenshot = null;
        Logger.log(Log.DEBUG, Logger.TAG_ANIMATION, "Cleared picture");
        getChildAt(INDEX_CONTENT).setBackgroundDrawable(null);
    }

    private class PictureView extends View {
        private Picture picture;

        public PictureView(Context context) {
            super(context);

            setWillNotDraw(false);
            setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        @Override
        public void draw(Canvas canvas) {
            picture.draw(canvas);
        }

        public void setPicture(Picture picture) {
            this.picture = picture;
        }

        public Picture getPicture() {
            return picture;
        }
    }
}
