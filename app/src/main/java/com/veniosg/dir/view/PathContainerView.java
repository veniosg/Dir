package com.veniosg.dir.view;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.veniosg.dir.R;

import java.io.File;

import static android.animation.ObjectAnimator.ofFloat;
import static android.animation.ObjectAnimator.ofInt;
import static android.graphics.Typeface.NORMAL;
import static android.graphics.Typeface.create;
import static android.text.TextUtils.TruncateAt.MIDDLE;
import static android.util.TypedValue.COMPLEX_UNIT_SP;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.inInterpolator;
import static com.veniosg.dir.util.Utils.dp;
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
            revealScroll();
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
        configureButtons();
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

            if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we 're on root
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

        configurePrimaryButton((Button) mPathContainer.getChildAt(count-1));
    }

    private void configurePrimaryButton(Button item) {
        int eightDp = (int) dp(8, getContext());
        item.setTextColor(getResources().getColor(
                getThemedResourceId(getContext(), R.attr.textColorPathBar)));
        item.setEllipsize(MIDDLE);
        item.setTextSize(COMPLEX_UNIT_SP, 24);  // Title style as per spec
        item.setPadding(eightDp, item.getPaddingTop(), eightDp * 2, item.getPaddingBottom());
        item.setOnClickListener(mPrimaryButtonListener);
    }

    private void configureSecondaryButton(Button item) {
        int eightDp = (int) dp(8, getContext());
        item.setTextColor(getResources().getColor(
                getThemedResourceId(getContext(), R.attr.textColorSecondaryPathBar)));
        item.setEllipsize(null);
        item.setTextSize(COMPLEX_UNIT_SP, 16);  // Title style as per spec
        item.setPadding(eightDp, item.getPaddingTop(), eightDp * 2, item.getPaddingBottom());
        item.setOnClickListener(mSecondaryButtonListener);
    }

    private void revealScroll() {
        ObjectAnimator scrollXAnim = ofInt(this, "scrollX", max(0, mRevealScrollPixels));
        scrollXAnim.setInterpolator(inInterpolator);
        scrollXAnim.setDuration(ANIM_DURATION / 2 );
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
