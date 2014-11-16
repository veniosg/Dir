package com.veniosg.dir.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.io.File;

import static com.veniosg.dir.util.Utils.lastCommonDirectoryIndex;
import static com.veniosg.dir.util.Utils.measureExactly;
import static com.veniosg.dir.view.PathButtonFactory.newButton;
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

    public PathContainerView(Context context) {
        super(context);
    }

    public PathContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PathContainerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        try {
            mPathContainer = (LinearLayout) getChildAt(0);
        } catch (ClassCastException ex) {
            throw new RuntimeException("First and only child of PathContainerView must be a LinearLayout");
        }
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
        if (lastChild.getMeasuredWidth() >= getMeasuredWidth() - marginStart) {
            mPathContainerRightPadding -= getMeasuredHeight();
            setPaddingRelative(0, 0, getMeasuredHeight(), 0);
        } else {
            setPaddingRelative(0, 0, 0, 0);
        }

        mPathContainer.measure(measureExactly(mPathContainerRightPadding + mPathContainer.getMeasuredWidth()),
                measureExactly(getMeasuredHeight()));
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        // Scroll pixels for the last item's right edge to reach the parent's right edge
        int scrollToEnd = mPathContainer.getWidth() - getWidth() - max(mPathContainerRightPadding, 0);
        int pixelsScrolledWithinRange = scrollToEnd - l + mRightEdgeRange;
        mRightEdgeRangeListener.rangeOffsetChanged(pixelsScrolledWithinRange);
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
        // Remove only the non-matching buttons.
        int count = mPathContainer.getChildCount();
        int lastCommonDirectory;
        if(previousDir != null && count > 0) {
            lastCommonDirectory = lastCommonDirectoryIndex(previousDir, newDir);
            mPathContainer.removeViews(lastCommonDirectory+1, count-lastCommonDirectory-1);
        } else {
            // First layout, init by hand.
            lastCommonDirectory = -1;
            mPathContainer.removeAllViews();
        }

        // Reload buttons.
        fillPathContainer(lastCommonDirectory + 1, newDir, controller);
    }

    /**
     * Adds new buttons according to the fPath parameter.
     * @param firstDirToAdd The index of the first directory of fPath to add.
     */
    private void fillPathContainer(int firstDirToAdd, File fPath, final PathController pathController) {
        StringBuilder cPath = new StringBuilder();
        char cChar;
        int cDir = 0;
        String path = fPath.getAbsolutePath();

        for (int i = 0; i < path.length(); i++) {
            cChar = path.charAt(i);
            cPath.append(cChar);

            if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we 're on root
                if (cDir++ >= firstDirToAdd) {
                    // Add a button
                    mPathContainer.addView(newButton(cPath.toString(), pathController));
                    // TODO uncomment
//                    if(firstDirToAdd != 0) // if not on first draw
//                        mPathContainer.getChildAt(mPathContainer.getChildCount() - 1).setAlpha(0); // So that it doesn't flash due to the animation's delay
                }
            }
        }
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
}
