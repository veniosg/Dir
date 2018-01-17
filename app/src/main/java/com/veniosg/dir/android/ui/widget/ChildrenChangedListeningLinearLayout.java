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

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A LinearLayout that exposes an item addition/removal listener that gets called right before the change is displayed.
 * Useful for triggering animations in cases where the TransitionAnimation framework falls short due to its static nature.
 * <br/>
 * When batch removing views, always call endLayoutTransition() passing the removed view.
 */
class ChildrenChangedListeningLinearLayout extends LinearLayout implements ViewTreeObserver.OnPreDrawListener {
    private List<View> mAddedWaitingViews = new ArrayList<View>();
    private List<View> mRemovedWaitingViews = new ArrayList<View>();
    private OnChildrenChangedListener mListener = OnChildrenChangedListener.NO_OP;
    private boolean firstDraw = true;

    public ChildrenChangedListeningLinearLayout(Context context) {
        this(context, null);
    }

    public ChildrenChangedListeningLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChildrenChangedListeningLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnPreDrawListener(this);
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
    }

    @Override
    public void removeView(final View view) {
        mRemovedWaitingViews.add(view);

        super.removeView(view);
    }

    @Override
    public void removeAllViews() {
        removeViews(0, getChildCount());
    }

    @Override
    public boolean onPreDraw() {
        if (!mAddedWaitingViews.isEmpty()) {
            if (!firstDraw) {
                mListener.childrenAdded(mAddedWaitingViews);
            }
        }
        if (!mRemovedWaitingViews.isEmpty()) {
            mListener.childrenRemoved(mRemovedWaitingViews);
        }

        firstDraw = false;
        mAddedWaitingViews.clear();
        mRemovedWaitingViews.clear();
        return true;
    }

    void setOnChildrenChangedListener(OnChildrenChangedListener listener) {
        if (listener != null) {
            this.mListener = listener;
        } else {
            this.mListener = OnChildrenChangedListener.NO_OP;
        }
    }

    int getLastAddedChildrenCount() {
        return mAddedWaitingViews.size();
    }

    interface OnChildrenChangedListener {
        void childrenAdded(List<View> newChildren);
        void childrenRemoved(List<View> oldChildren);

        OnChildrenChangedListener NO_OP = new OnChildrenChangedListener() {
            @Override
            public void childrenAdded(final List<View> newChildren) {}

            @Override
            public void childrenRemoved(final List<View> oldChildren) {}
        };
    }
}
