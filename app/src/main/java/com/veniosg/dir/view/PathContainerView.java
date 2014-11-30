package com.veniosg.dir.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.veniosg.dir.FileManagerApplication;
import com.veniosg.dir.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.animation.LayoutTransition.*;
import static android.animation.ObjectAnimator.ofFloat;
import static android.animation.ObjectAnimator.ofInt;
import static android.graphics.Typeface.create;
import static android.text.TextUtils.TruncateAt.MIDDLE;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.ANIM_START_DELAY;
import static com.veniosg.dir.AnimationConstants.IN_INTERPOLATOR;
import static com.veniosg.dir.util.Utils.getLastChild;
import static com.veniosg.dir.util.Utils.lastCommonDirectoryIndex;
import static com.veniosg.dir.util.Utils.measureExactly;
import static com.veniosg.dir.view.PathButtonFactory.SECONDARY_ITEM_FACTOR;
import static com.veniosg.dir.view.PathButtonFactory.newButton;
import static com.veniosg.dir.view.Themer.getThemedResourceId;
import static java.lang.Math.max;

public class PathContainerView extends HorizontalScrollView {
    private static final String LOG_TAG = PathContainerView.class.getName();

    /**
     * Additional padding to the end of mPathContainer
     * so that the last item is left aligned to the grid.
     */
    private int mPathContainerRightPadding;
    private ChildrenChangedListeningLinearLayout mPathContainer;
    private RightEdgeRangeListener mRightEdgeRangeListener = noOpRangeListener();
    private int mRightEdgeRange;
    private int mRevealScrollPixels;
    private int itemsAddedOnLastCd;
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
            smoothRevealButtons();
        }
    };
    private ChildrenChangedListeningLinearLayout.OnChildrenChangedListener mOnPathChildrenChangedListener = new ChildrenChangedListeningLinearLayout.OnChildrenChangedListener() {
        @Override
        public void childrenAdded(final List<View> newChildren) {
            Logger.logV(LOG_TAG, "Starting add animation");

            AnimatorSet animSet = new AnimatorSet();
            animSet.setDuration(ANIM_DURATION);
            animSet.setStartDelay(ANIM_START_DELAY);
            animSet.setInterpolator(IN_INTERPOLATOR);
            animSet.playTogether(
                    scrollToEndAnimator(),
                    transformToSecondaryAnimator(mPathContainer, 0, mPathContainer.getChildCount() - 1),
                    addedViewsAnimator(newChildren)
            );
            FileManagerApplication.enqueueAnimator(animSet);
        }

        @Override
        public void childrenRemoved(final List<View> oldChildren) {
            Logger.logV(LOG_TAG, "Starting remove animation");

            FileManagerApplication.enqueueAnimator(ofFloat(0, 0));
        }
    };

    public PathContainerView(Context context) {
        super(context);
        init();
    }

    public PathContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PathContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
    }

    @Override
    protected void onFinishInflate() {
        try {
            mPathContainer = (ChildrenChangedListeningLinearLayout) getChildAt(0);
        } catch (ClassCastException ex) {
            throw new RuntimeException("First and only child of PathContainerView must be a ChildrenChangedListeningLinearLayout");
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

    private LayoutTransition delayRemovalOfChild() {
        // Workaround to keep items around until our removal animations are finished.
        LayoutTransition transition = new LayoutTransition();
        transition.disableTransitionType(APPEARING);
        transition.disableTransitionType(CHANGE_DISAPPEARING);
        transition.disableTransitionType(CHANGE_APPEARING);
        transition.disableTransitionType(CHANGING);
        transition.setAnimator(DISAPPEARING, ofFloat(0, 0));
        transition.setDuration(ANIM_DURATION);
        transition.setStartDelay(ANIM_START_DELAY, DISAPPEARING);
        return transition;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View lastChild = mPathContainer.getChildAt(mPathContainer.getChildCount() - 1);
        int marginStart = ((LinearLayout.LayoutParams) lastChild.getLayoutParams()).getMarginStart();
        mPathContainerRightPadding = getMeasuredWidth()
                - lastChild.getMeasuredWidth()
                - marginStart;

        // On really long names that take up the whole screen width
        if (lastChild.getMeasuredWidth() >= getMeasuredWidth() - marginStart - mRightEdgeRange) {
            mPathContainerRightPadding -= getMeasuredHeight();
            setPaddingRelative(0, 0, getMeasuredHeight(), 0);
        } else {
            setPaddingRelative(0, 0, 0, 0);
        }

        mPathContainer.measure(measureExactly(mPathContainerRightPadding + mPathContainer.getMeasuredWidth()),
                measureExactly(getMeasuredHeight()));

        mRevealScrollPixels = mPathContainer.getMeasuredWidth() - getMeasuredWidth() * 2;
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        invokeRightEdgeRangeListener(l);
    }

    public void setEdgeListener(RightEdgeRangeListener listener) {
        if (listener != null) {
            mRightEdgeRangeListener = listener;
        } else {
            mRightEdgeRangeListener = noOpRangeListener();
        }
        mRightEdgeRange = mRightEdgeRangeListener.getRange();
    }

    /**
     * @param previousDir Pass null to refresh the whole view.
     * @param newDir The new current directory.
     */
    public void updateWithPaths(File previousDir, File newDir, final PathController controller) {
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
        if(previousDir != null && count > 0) {
            lastCommonDirectory = lastCommonDirectoryIndex(previousDir, newDir);
            mPathContainer.removeViews(lastCommonDirectory + 1, count - lastCommonDirectory - 1);
        } else {
            // First layout, init by hand.
            forceStyle = true;
            lastCommonDirectory = -1;
            mPathContainer.removeAllViews();
        }

        // Add children as necessary.
        itemsAddedOnLastCd = fillPathContainer(lastCommonDirectory + 1, newDir, getContext());

        // Static styling and click configuration.
        View child;
        for (int i = 0; i < mPathContainer.getChildCount()-1; i++) {
            child = mPathContainer.getChildAt(i);
            child.setOnClickListener(mSecondaryButtonListener);
        }
        if (forceStyle) {
            for (int i = 0; i < mPathContainer.getChildCount()-1; i++) {
                forceStyleAsSecondary(mPathContainer.getChildAt(i));
            }
        }
        child = getLastChild(mPathContainer);
        child.setOnClickListener(mPrimaryButtonListener);
    }

    /**
     * Adds new buttons according to the fPath parameter.
     * @param firstDirToAdd The index of the first directory of fPath to add.
     * @return How many new items where added.
     */
    private int fillPathContainer(int firstDirToAdd, File fPath, Context context) {
        StringBuilder cPath = new StringBuilder();
        String path = fPath.getAbsolutePath();
        char cChar;
        int cDir = 0;
        View item;
        int added = 0;

        for (int i = 0; i < path.length(); i++) {
            cChar = path.charAt(i);
            cPath.append(cChar);

            if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we're on root
                if (cDir++ >= firstDirToAdd) {
                    mPathContainer.addView(newButton(cPath.toString(), context));
                    added++;
                }
            }
        }

        return added;
    }

    private void forceStyleAsSecondary(View childToStyle) {
        childToStyle.setScaleX(SECONDARY_ITEM_FACTOR);
        childToStyle.setScaleY(SECONDARY_ITEM_FACTOR);
        childToStyle.setAlpha(SECONDARY_ITEM_FACTOR);
    }

    private boolean isStyledAsSecondary(View v) {
        return v.getAlpha() == SECONDARY_ITEM_FACTOR
                && v.getScaleX() == SECONDARY_ITEM_FACTOR
                && v.getScaleY() == SECONDARY_ITEM_FACTOR;
    }

    private Animator transformToSecondaryAnimator(ViewGroup container, int first, int count) {
        List<Animator> anims = new ArrayList<Animator>(count);
        AnimatorSet set;
        View v;

        for (int i = first; i < first + count; i++) {
            v = container.getChildAt(i);

            if (!isStyledAsSecondary(v)) {
                set = new AnimatorSet();
                set.playTogether(
                        ofFloat(v, "scaleX", SECONDARY_ITEM_FACTOR),
                        ofFloat(v, "scaleY", SECONDARY_ITEM_FACTOR),
                        ofFloat(v, "alpha", SECONDARY_ITEM_FACTOR)
                );
                anims.add(set);
            }
        }

        AnimatorSet result = new AnimatorSet();
        result.playTogether(anims);
        return result;
    }

    private Animator addedViewsAnimator(List<View> newChildren) {
        if (newChildren != null) {
            AnimatorSet anim = new AnimatorSet();
            List<Animator> animList = new ArrayList<Animator>(newChildren.size());
            View previousView;
            View viewToAnimate;

            for(int i = 0; i < newChildren.size(); i++) {
                viewToAnimate = newChildren.get(i);
                // Too tired to figure out a single formula for the index.
                if (newChildren.size() == 1 || i == 0) {
                    // Last child, excluding the ones to animate
                    previousView = mPathContainer.getChildAt(
                            mPathContainer.getChildCount() - newChildren.size() - 1);
                } else {
                    previousView = newChildren.get(i-1);
                }
                viewToAnimate.setTranslationX(
                        getWidth() - previousView.getWidth());
                animList.add(ofFloat(viewToAnimate, "translationX", 0));
            }
            anim.playTogether(animList);

            return anim;
        }

        return null;
    }

    private Animator scrollToEndAnimator() {
        return ofInt(this, "scrollX", mPathContainer.getWidth() - getWidth());
    }

    private int screenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private void smoothRevealButtons() {
        ObjectAnimator scrollXAnim = ofInt(this, "scrollX", max(0, mRevealScrollPixels));
        scrollXAnim.setInterpolator(IN_INTERPOLATOR);
        scrollXAnim.setDuration(ANIM_DURATION / 2);
        scrollXAnim.start();
    }

    private void invokeRightEdgeRangeListener(int l) {
        // Scroll pixels for the last item's right edge to reach the parent's right edge
        int scrollToEnd = mPathContainer.getWidth() - getWidth() - max(mPathContainerRightPadding, 0);
        int pixelsScrolledWithinRange = scrollToEnd - l + mRightEdgeRange;
        mRightEdgeRangeListener.rangeOffsetChanged(pixelsScrolledWithinRange);
    }

    private RightEdgeRangeListener noOpRangeListener() {
        return new RightEdgeRangeListener() {
            @Override
            public int getRange() {
                return 0;
            }

            @Override
            public void rangeOffsetChanged(int offsetInRange) {}
        };
    }

    /**
     * Listener for when the last child is within the supplied mRangeRight of the right edge of this view.
     */
    public interface RightEdgeRangeListener {
        /**
         * @return The range in which to get the callback.
         */
        public int getRange();

        /**
         * Called when the distance of the last child in regards to the right edge of this view
         * has changed.
         * @param offsetInRange The current number of pixels within the range. If <0 means that the
         *                      child is not yet within the specified range.
         */
        public void rangeOffsetChanged(int offsetInRange);
    }

    public interface PathControllerGetter {
        public PathController getPathController();
    }
}
