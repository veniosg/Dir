package com.veniosg.dir.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Utils;

import java.io.File;

import static android.animation.LayoutTransition.APPEARING;
import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.animation.LayoutTransition.CHANGING;
import static android.animation.LayoutTransition.DISAPPEARING;
import static android.animation.ObjectAnimator.ofFloat;
import static android.animation.ObjectAnimator.ofInt;
import static android.graphics.Typeface.create;
import static android.text.TextUtils.TruncateAt.MIDDLE;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.ANIM_START_DELAY;
import static com.veniosg.dir.AnimationConstants.IN_INTERPOLATOR;
import static com.veniosg.dir.AnimationConstants.OUT_INTERPOLATOR;
import static com.veniosg.dir.util.Utils.dp;
import static com.veniosg.dir.util.Utils.getLastChild;
import static com.veniosg.dir.util.Utils.lastCommonDirectoryIndex;
import static com.veniosg.dir.util.Utils.measureExactly;
import static com.veniosg.dir.view.PathButtonFactory.newButton;
import static com.veniosg.dir.view.Themer.getThemedResourceId;
import static java.lang.Math.max;

public class PathContainerView extends HorizontalScrollView {
    /**
     * Additional padding to the end of mPathContainer
     * so that the last item is left aligned to the grid.
     */
    private int mPathContainerRightPadding;
    private LinearLayout mPathContainer;
    private RightEdgeRangeListener mRightEdgeRangeListener = noOpRangeListener();
    private int mRightEdgeRange;
    private int mRevealScrollPixels;
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
    private LayoutTransition.TransitionListener mTransitionListener = new LayoutTransition.TransitionListener() {
        @Override
        public void startTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
            if (transitionType == APPEARING) {
                Animator animator2 = ObjectAnimator.ofInt(PathContainerView.this, "scrollX", mPathContainer.getWidth());
                animator2.setDuration(ANIM_DURATION);
                animator2.setInterpolator(IN_INTERPOLATOR);
                animator2.setStartDelay(ANIM_START_DELAY);
                animator2.start();
            } else if (transitionType == DISAPPEARING) {
                View futureLastChild = mPathContainer.getChildAt(mPathContainer.getChildCount()-2);
                mPathContainer.setTranslationX(-futureLastChild.getMeasuredWidth());
                Animator animator = ObjectAnimator.ofFloat(mPathContainer, "translationX", 0);
                animator.setDuration(ANIM_DURATION);
                animator.setStartDelay(ANIM_START_DELAY);
                animator.setInterpolator(OUT_INTERPOLATOR);
                animator.start();
            }
        }

        @Override
        public void endTransition(LayoutTransition transition, ViewGroup container, View view, int transitionType) {
//            configureButtons();
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
            mPathContainer = (LinearLayout) getChildAt(0);
        } catch (ClassCastException ex) {
            throw new RuntimeException("First and only child of PathContainerView must be a LinearLayout");
        }

        mPathContainer.addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                mPathContainer.removeOnLayoutChangeListener(this);

                scrollTo(mPathContainer.getWidth(), 0);
            }
        });
        LayoutTransition transition = new LayoutTransition();
        // Next two values should be the same as in AnimatedFileListContainer
        transition.setDuration(ANIM_DURATION);
        transition.setInterpolator(APPEARING, IN_INTERPOLATOR);
        transition.setInterpolator(DISAPPEARING, OUT_INTERPOLATOR);
        transition.setAnimator(APPEARING, createAppearingAnimator(transition));
        transition.setAnimator(DISAPPEARING, createDisappearingAnimator(transition));
        transition.setStartDelay(APPEARING, ANIM_START_DELAY);
        transition.setStartDelay(DISAPPEARING, ANIM_START_DELAY);
        transition.disableTransitionType(CHANGE_APPEARING);
        transition.disableTransitionType(CHANGE_DISAPPEARING);
        transition.disableTransitionType(CHANGING);
        transition.addTransitionListener(mTransitionListener);
        mPathContainer.setLayoutTransition(transition);
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
        if(previousDir != null && count > 0) {
            lastCommonDirectory = lastCommonDirectoryIndex(previousDir, newDir);
            mPathContainer.removeViews(lastCommonDirectory + 1, count - lastCommonDirectory - 1);
        } else {
            // First layout, init by hand.
            lastCommonDirectory = -1;
            mPathContainer.removeAllViews();
        }

        // Reload buttons.
        fillPathContainer(lastCommonDirectory + 1, newDir, getContext());
    }

    /**
     * Adds new buttons according to the fPath parameter.
     * @param firstDirToAdd The index of the first directory of fPath to add.
     */
    private void fillPathContainer(int firstDirToAdd, File fPath, Context context) {
        StringBuilder cPath = new StringBuilder();
        char cChar;
        int cDir = 0;
        String path = fPath.getAbsolutePath();
        View item;

        for (int i = 0; i < path.length(); i++) {
            cChar = path.charAt(i);
            cPath.append(cChar);

            if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we're on root
                if (cDir++ >= firstDirToAdd) {
                    item = newButton(cPath.toString(), context);
                    mPathContainer.addView(item);
                    // TODO uncomment
//                    if(firstDirToAdd != 0) // if not on first draw
//                        item.setAlpha(0); // So that it doesn't flash due to the animation's delay
                }
            }
        }
    }

    private void configureButtons() {
        int count = mPathContainer.getChildCount();
        for (int i = 0; i < count -1; i++) {
            configureSecondaryButton((Button) mPathContainer.getChildAt(i));
        }

        configurePrimaryButton((Button) mPathContainer.getChildAt(count - 1));
    }

    private void configurePrimaryButton(Button item) {
        int eightDp = (int) dp(8, getContext());
        item.setTextColor(getResources().getColor(
                getThemedResourceId(getContext(), R.attr.textColorPathBar)));
        item.setEllipsize(MIDDLE);
        item.setTextSize(COMPLEX_UNIT_SP, 24);  // Title style as per spec
        item.setPadding(eightDp, item.getPaddingTop(), eightDp * 2, item.getPaddingBottom());
        item.setOnClickListener(mPrimaryButtonListener);
        setCaretOn(item, 1f);
    }

    private void configureSecondaryButton(Button item) {
        int eightDp = (int) dp(8, getContext());
        item.setTextColor(getResources().getColor(
                getThemedResourceId(getContext(), R.attr.textColorSecondaryPathBar)));
        item.setEllipsize(null);
        item.setTextSize(COMPLEX_UNIT_SP, 16);
        item.setPadding(eightDp, item.getPaddingTop(), eightDp * 2, item.getPaddingBottom());
        item.setOnClickListener(mSecondaryButtonListener);

        if (!((File) item.getTag()).getAbsolutePath().equals("/")) {
            setCaretOn(item, 16f / 24f);
        }
    }

    private void setCaretOn(Button btn, float scale) {
        Drawable caret = btn.getContext().getDrawable(R.drawable.ic_item_caret);
        caret.setBounds(0, 0, (int) (caret.getIntrinsicWidth() * scale), (int) (caret.getIntrinsicHeight() * scale));
        caret.setTint(Themer.getThemedColor(btn.getContext(), R.attr.textColorSecondaryPathBar));
        btn.setCompoundDrawablesRelative(caret, null, null, null);
    }

    private Animator createAppearingAnimator(final LayoutTransition transition) {
        ObjectAnimator anim = ofFloat(null, "translationX", screenWidth(), 0);
        anim.setDuration(transition.getDuration(APPEARING));
        anim.setStartDelay(transition.getStartDelay(APPEARING));
        anim.setInterpolator(transition.getInterpolator(APPEARING));
        return anim;
    }

    private Animator createDisappearingAnimator(final LayoutTransition transition) {
        ObjectAnimator anim = ofFloat(null, "translationX", screenWidth());
        anim.setDuration(transition.getDuration(DISAPPEARING));
        anim.setStartDelay(transition.getStartDelay(DISAPPEARING));
        anim.setInterpolator(transition.getInterpolator(DISAPPEARING));
        return anim;
    }

    private void changeAppearingWorkaround(View view, int initValue) {
        view.setTranslationX(initValue);
        Animator animator = ObjectAnimator.ofFloat(view, "translationX", 0);
        animator.setDuration(ANIM_DURATION);
        animator.setStartDelay(ANIM_START_DELAY);
        animator.setInterpolator(IN_INTERPOLATOR);
        animator.start();
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
