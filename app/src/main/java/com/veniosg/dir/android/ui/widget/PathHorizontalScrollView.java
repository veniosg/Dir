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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import com.veniosg.dir.android.FileManagerApplication;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.ui.PathController;
import com.veniosg.dir.android.ui.widget.ChildrenChangedListeningLinearLayout.OnChildrenChangedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.animation.Animator.AnimatorListener;
import static android.animation.LayoutTransition.*;
import static android.animation.ObjectAnimator.ofFloat;
import static android.animation.ObjectAnimator.ofInt;
import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.veniosg.dir.AnimationConstants.*;
import static com.veniosg.dir.android.util.Utils.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class PathHorizontalScrollView extends HorizontalScrollView {
    private static final String LOG_TAG = PathHorizontalScrollView.class.getName();

    /**
     * Additional padding to the end of mPathContainer
     * so that the last item is left aligned to the grid.
     */
    private int mPathContainerRightPadding;
    private PathContainerLayout mPathContainer;
    private RightEdgeRangeListener mRightEdgeRangeListener = noOpRangeListener();
    private boolean mBlockTouch = false;
    private int mRightEdgeRange;
    private int mRevealScrollPixels;
    private int mLatestScrollOffset;
    private int mLatestPaddingRight;
    private PathControllerGetter mControllerGetter;

    private final OnClickListener mSecondaryButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mControllerGetter == null
                    || mControllerGetter.getPathController() == null) {
                return;
            }

            mControllerGetter.getPathController().cd((File) v.getTag());
        }
    };
    private final OnClickListener mPrimaryButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            smoothRevealButtonsAnimator().start();
        }
    };
    private final AnimatorListener mBlockTouchAnimatorListener = new AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            mBlockTouch = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            mBlockTouch = false;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            mBlockTouch = false;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };
    private final OnChildrenChangedListener mOnPathChildrenChangedListener = new OnChildrenChangedListener() {
        @Override
        public void childrenAdded(final List<View> newChildren) {
            Logger.logV(LOG_TAG, "Starting add animation");

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(ANIM_DURATION);
            animSet.setStartDelay(ANIM_START_DELAY);
            animSet.setInterpolator(IN_INTERPOLATOR);
            animSet.playTogether(
                    scrollToEndAnimator(),
                    addedViewsAnimator(newChildren),
                    transformToSecondaryAsNeededAnimator()
            );
            animSet.addListener(mBlockTouchAnimatorListener);
            FileManagerApplication.enqueueAnimator(animSet);
        }

        @Override
        public void childrenRemoved(final List<View> oldChildren) {
            Logger.logV(LOG_TAG, "Starting remove animation");

            if (mPathContainer.getLastAddedChildrenCount() <= 0) {
                AnimatorSet animSet = new AnimatorSet();
                animSet.setDuration(ANIM_DURATION);
                animSet.setStartDelay(ANIM_START_DELAY);
                animSet.setInterpolator(OUT_INTERPOLATOR);
                animSet.playTogether(
                        scrollToEndAnimator(),
                        translateForChildrenRemovalAnimator(oldChildren),
                        removedViewsAnimator(oldChildren),
                        transformToPrimaryAnimator(oldChildren)
                );
                animSet.addListener(mBlockTouchAnimatorListener);

                FileManagerApplication.enqueueAnimator(animSet);
            }
        }
    };

    public PathHorizontalScrollView(Context context) {
        super(context);
    }

    public PathHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathHorizontalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        try {
            mPathContainer = (PathContainerLayout) getChildAt(0);
        } catch (ClassCastException ex) {
            throw new RuntimeException("First and only child of PathHorizontalScrollView must be a PathContainerLayout.");
        }

        mPathContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mPathContainer.removeOnLayoutChangeListener(this);

                scrollTo(mPathContainer.getWidth(), 0);
            }
        });
        mPathContainer.setOnChildrenChangedListener(mOnPathChildrenChangedListener);
        LayoutTransition transition = delayRemovalOfChild();
        mPathContainer.setLayoutTransition(transition);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        int maxChildWidthPx = getMeasuredWidth() - mRightEdgeRange;

        // Probe last child width and set up container padding
        View lastItem = getLastChild(mPathContainer);
        if (lastItem != null) {
            lastItem.measure(makeMeasureSpec(maxChildWidthPx, AT_MOST), 0);
            mPathContainerRightPadding = getMeasuredWidth() - lastItem.getMeasuredWidth();
        }
        mPathContainer.setMaxChildWidth(maxChildWidthPx);

        // Probe complete content length
        mPathContainer.measure(0, heightMeasureSpec);

        // Force dimension of container (as we know child views are measured now), accounting for required padding to position last child correctly.
        mPathContainer.setMeasuredWidth(mPathContainerRightPadding + mPathContainer.getMeasuredWidth());

        // Now that we know the container's width we know the right amount of scroll to reveal items
        mRevealScrollPixels = mPathContainer.getMeasuredWidth() - getMeasuredWidth() * 2;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldL, int oldT) {
        super.onScrollChanged(l, t, oldL, oldT);

        invokeRightEdgeRangeListener(l);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mBlockTouch || super.onTouchEvent(ev);
    }

    @SuppressWarnings("WeakerAccess")
    public void setEdgeListener(RightEdgeRangeListener listener) {
        if (listener != null) {
            mRightEdgeRangeListener = listener;
        } else {
            mRightEdgeRangeListener = noOpRangeListener();
        }
        mRightEdgeRange = mRightEdgeRangeListener.getRange();
    }

    private LayoutTransition delayRemovalOfChild() {
        // Workaround to keep items around until our removal animations are finished.
        LayoutTransition transition = new LayoutTransition();
        transition.disableTransitionType(APPEARING);
        transition.disableTransitionType(CHANGE_DISAPPEARING);
        transition.disableTransitionType(CHANGE_APPEARING);
        transition.disableTransitionType(CHANGING);
        transition.setAnimator(DISAPPEARING, ofFloat(0, 0));
        transition.setDuration((long) (ANIM_DURATION * 1.5f));  // Account for possible delays between this and other animations' start
        transition.setStartDelay(ANIM_START_DELAY, DISAPPEARING);
        return transition;
    }

    /**
     * @param previousDir Pass null to refresh the whole view.
     * @param newDir      The new current directory.
     */
    void updateBasedOnPaths(File previousDir, File newDir, final PathController controller) {
        if (mControllerGetter == null) {
            mControllerGetter = new PathControllerGetter() {
                @Override
                public PathController getPathController() {
                    return controller;
                }
            };
        }

        // Remove only the non-matching buttons.
        int count = mPathContainer.getChildCount();
        int lastCommonDirectory;
        boolean forceStyle = false;
        mLatestScrollOffset = mPathContainer.getWidth() - (getWidth() + getScrollX());
        mLatestPaddingRight = getPaddingRight();
        if (previousDir != null && count > 0) {
            lastCommonDirectory = lastCommonDirectoryIndex(previousDir, newDir);
            mPathContainer.removeViews(lastCommonDirectory + 1, count - lastCommonDirectory - 1);
        } else {
            // First layout, init by hand.
            forceStyle = true;
            lastCommonDirectory = -1;
            mPathContainer.removeAllViews();
        }

        // Add children as necessary.
        fillPathContainer(lastCommonDirectory + 1, newDir, getContext());

        // Static styling and click configuration.
        PathItemView child;
        for (int i = 0; i < mPathContainer.getChildCount() - 1; i++) {
            child = (PathItemView) mPathContainer.getChildAt(i);
            child.setOnClickListener(mSecondaryButtonListener);
            if (forceStyle) {
                child.styleAsSecondary();
            }
        }
        View lastChild = getLastChild(mPathContainer);
        if (lastChild != null) {
            lastChild.setOnClickListener(mPrimaryButtonListener);
        }
    }

    /**
     * Adds new buttons according to the fPath parameter.
     *
     * @param firstDirToAdd The index of the first directory of fPath to add.
     */
    private void fillPathContainer(int firstDirToAdd, File fPath, Context context) {
        StringBuilder cPath = new StringBuilder();
        String path = fPath.getAbsolutePath();
        char cChar;
        int cDir = 0;

        for (int i = 0; i < path.length(); i++) {
            cChar = path.charAt(i);
            cPath.append(cChar);

            if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we're on root
                if (cDir++ >= firstDirToAdd) {
                    mPathContainer.addView(PathItemView.newInstanceFor(cPath.toString(), context));
                }
            }
        }
    }

    private Animator transformToSecondaryAsNeededAnimator() {
        AnimatorSet result = new AnimatorSet();
        int count = mPathContainer.getChildCount() - 1;
        List<Animator> animators = new ArrayList<Animator>(count);
        PathItemView v;

        for (int i = 0; i < count; i++) {
            v = (PathItemView) mPathContainer.getChildAt(i);

            if (!v.isStyledAsSecondary()) {
                animators.add(v.transformToSecondaryAnimator());
            }
        }

        result.playTogether(animators);
        return result;
    }

    private Animator addedViewsAnimator(List<View> newChildren) {
        if (newChildren != null) {
            AnimatorSet anim = new AnimatorSet();
            List<Animator> animList = new ArrayList<Animator>(newChildren.size());
            View secondToLastView = null;
            View viewToAnimate = null;

            for (int i = newChildren.size() - 1; i >= 0; i--) {
                viewToAnimate = newChildren.get(i);
                if (secondToLastView == null) {
                    // Too tired to figure out a single formula for the index.
                    if (newChildren.size() == 1 || i == 0) {
                        // Last child, excluding the ones to animate
                        secondToLastView = mPathContainer.getChildAt(
                                mPathContainer.getChildCount() - newChildren.size() - 1);
                    } else {
                        secondToLastView = newChildren.get(i - 1);
                    }
                }
                viewToAnimate.setTranslationX(
                        getWidth() - secondToLastView.getWidth());
                animList.add(ofFloat(viewToAnimate, "translationX", 0));
            }
            anim.playTogether(animList);

            return anim;
        }

        return null;
    }

    private Animator scrollToEndAnimator() {
        return ofInt(this, "scrollX", mPathContainer.getWidth() - getWidth() + getPaddingRight());
    }

    private Animator removedViewsAnimator(List<View> oldChildren) {
        AnimatorSet animSet = new AnimatorSet();
        List<Animator> animators = new ArrayList<Animator>(oldChildren.size());
        int lastChildWidth = 0;
        View lastChild = getLastChild(mPathContainer);
        if (lastChild != null) {
            lastChildWidth = lastChild.getWidth();
        }

        for (View v : oldChildren) {
            animators.add(ofFloat(v, "translationX", getWidth() - lastChildWidth + mLatestScrollOffset + mLatestPaddingRight));
        }
        animSet.playTogether(animators);
        return animSet;
    }

    private Animator transformToPrimaryAnimator(List<View> oldChildren) {
        int nextNewPrimaryItemIndex = mPathContainer.getChildCount() - 1;
        PathItemView newPrimaryItem = (PathItemView) mPathContainer.getChildAt(nextNewPrimaryItemIndex);

        return newPrimaryItem.transformToPrimaryAnimator();
    }

    private Animator translateForChildrenRemovalAnimator(List<View> oldChildren) {
        int lastChildWidth = 0;
        View lastChild = getLastChild(mPathContainer);
        if (lastChild != null) {
            lastChildWidth = lastChild.getWidth();
        }
        mPathContainer.setTranslationX(min(0, -lastChildWidth + mLatestScrollOffset + mLatestPaddingRight));
        ObjectAnimator anim = ofFloat(mPathContainer, "translationX", 0);
        anim.addListener(pathContainerClipAnimatorListener());
        return anim;
    }

    private Animator smoothRevealButtonsAnimator() {
        ObjectAnimator scrollXAnim = ofInt(this, "scrollX", max(0, mRevealScrollPixels));
        scrollXAnim.setInterpolator(IN_INTERPOLATOR);
        scrollXAnim.setDuration(ANIM_DURATION / 2);
        return scrollXAnim;
    }

    private AnimatorListener pathContainerClipAnimatorListener() {
        return new AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setClipChildren(false);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setClipChildren(true);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        };
    }

    private void invokeRightEdgeRangeListener(int scrollX) {
        // Scroll pixels for the last item's right edge to reach the parent's right edge
        int scrollToEnd = mPathContainer.getWidth() - getWidth() - max(mPathContainerRightPadding, 0);
        int pixelsScrolledWithinRange = scrollToEnd - scrollX + mRightEdgeRange;
        mRightEdgeRangeListener.rangeOffsetChanged(pixelsScrolledWithinRange);
    }

    private RightEdgeRangeListener noOpRangeListener() {
        return new RightEdgeRangeListener() {
            @Override
            public int getRange() {
                return 0;
            }

            @Override
            public void rangeOffsetChanged(int offsetInRange) {
            }
        };
    }

    /**
     * Listener for when the last child is within the supplied mRangeRight of the right edge of this view.
     */
    interface RightEdgeRangeListener {
        /**
         * @return The range at which to get the callback.
         */
        int getRange();

        /**
         * Called when the distance of the last child to the right edge of this view has changed.
         *
         * @param offsetInRange The distance travelled by the right child from the start of the range. If <0 means that the
         *                      child is not yet within the specified range.
         */
        void rangeOffsetChanged(int offsetInRange);
    }

    interface PathControllerGetter {
        PathController getPathController();
    }
}
