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

public class PathContainerView extends HorizontalScrollView {
    private LinearLayout mPathContainer;

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
        } else {
            // First layout, init by hand.
            lastCommonDirectory = -1;
        }
        for (int i = count-1; i > lastCommonDirectory; i--) {
            mPathContainer.removeViewAt(i);
        }

        // Reload buttons.
        fillPathContainer(lastCommonDirectory + 1, newDir, controller);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View lastChild = mPathContainer.getChildAt(mPathContainer.getChildCount() - 1);
        int paddingWidth = getMeasuredWidth()
                - lastChild.getMeasuredWidth()
                - ((LinearLayout.LayoutParams) lastChild.getLayoutParams()).getMarginStart();

        // TODO if last child WANTS TO be wider than screen - left margin, apply padding to the
        // right equal to the width of the right button.

        mPathContainer.measure(measureExactly(paddingWidth + mPathContainer.getMeasuredWidth()),
                measureExactly(getMeasuredHeight()));
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
}
