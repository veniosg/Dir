package com.veniosg.dir.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A LinearLayout that exposes a listener for when children have been added and measured (or removed)
 * but not yet displayed. Useful for triggering complex animations where the TransitionAnimation framework
 * falls short due to its static nature. <br/>
 * When batch removing views, always call endLayoutTransition() passing the removed view.
 */
public class ChildrenChangedListeningLinearLayout extends LinearLayout implements ViewTreeObserver.OnPreDrawListener {
    private List<View> mAddedWaitingViews = new ArrayList<View>();
    private List<View> mRemovedWaitingViews = new ArrayList<View>();
    private OnChildrenChangedListener mListener = OnChildrenChangedListener.NO_OP;
    private boolean firstDraw = true;

    public ChildrenChangedListeningLinearLayout(Context context) {
        super(context);
        init();
    }

    public ChildrenChangedListeningLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ChildrenChangedListeningLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ChildrenChangedListeningLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        getViewTreeObserver().addOnPreDrawListener(this);
    }

    public void setOnChildrenChangedListener(OnChildrenChangedListener listener) {
        if (listener != null) {
            this.mListener = listener;
        } else {
            this.mListener = OnChildrenChangedListener.NO_OP;
        }
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);

        mAddedWaitingViews.add(child);
    }

    @Override
    public void removeViews(int start, int count) {
        for (int i = start; i < start + count; i++) {
            mRemovedWaitingViews.add(getChildAt(i));
        }

        super.removeViews(start, count);

        if (!mRemovedWaitingViews.isEmpty()) {
            mListener.childrenRemoved(mRemovedWaitingViews);
            mRemovedWaitingViews.clear();
        }
    }

    @Override
    public void removeView(final View view) {
        mListener.childrenRemoved(new ArrayList<View>(){{
            add(view);
        }});

        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        removeViews(0, getChildCount());
    }

    @Override
    public boolean onPreDraw() {
        if (!mAddedWaitingViews.isEmpty() && !firstDraw) {
            mListener.childrenAdded(mAddedWaitingViews);
        }
        firstDraw = false;
        mAddedWaitingViews.clear();
        return true;
    }

    public interface OnChildrenChangedListener {
        public void childrenAdded(List<View> newChildren);
        public void childrenRemoved(List<View> oldChildren);

        public static OnChildrenChangedListener NO_OP = new OnChildrenChangedListener() {
            @Override
            public void childrenAdded(final List<View> newChildren) {}

            @Override
            public void childrenRemoved(final List<View> oldChildren) {}
        };
    }
}
