/*
 * Copyright (C) 2014-2015 George Venios
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
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

import static com.veniosg.dir.AnimationConstants.ANIM_START_DELAY;

/**
 * Children must always follow the PAGE_INDEX_* indexing.
 */
public class WaitingViewFlipper extends ViewFlipper {
    public static final int PAGE_INDEX_CONTENT = 0;
    public static final int PAGE_INDEX_LOADING = 1;
    public static final int PAGE_INDEX_PERMISSION_DENIED = 2;

    private Handler mWaiterHandler = new Handler();
    private Runnable mWaiter;
    private int mWaitingChild = -1;

    public WaitingViewFlipper(Context context) {
        super(context);
    }

    public WaitingViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDisplayedChildDelayed(final int child) {
        if (mWaiter != null) {
            mWaiterHandler.removeCallbacks(mWaiter);
        }

        mWaiter = new Runnable() {
            @Override
            public void run() {
                WaitingViewFlipper.this.setDisplayedChild(child);
            }
        };
        mWaiterHandler.postDelayed(mWaiter, ANIM_START_DELAY);
        mWaitingChild = child;
    }

    public void setDisplayedChild(int whichChild) {
        if (mWaitingChild != -1) {
            mWaitingChild = -1;
            mWaiterHandler.removeCallbacks(mWaiter);
        }

        if (getDisplayedChild() != whichChild)
            super.setDisplayedChild(whichChild);
    }
}
