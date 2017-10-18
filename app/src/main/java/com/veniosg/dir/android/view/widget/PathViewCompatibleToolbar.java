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

package com.veniosg.dir.android.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toolbar;

import com.veniosg.dir.R;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;

public class PathViewCompatibleToolbar extends Toolbar {
    public PathViewCompatibleToolbar(Context context) {
        super(context);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PathViewCompatibleToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        View pathView = findViewById(R.id.pathview);
        if (pathView != null) {
            pathView.measure(widthMeasureSpec, makeMeasureSpec(pathView.getMeasuredHeight(), EXACTLY));
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        View pathView = findViewById(R.id.pathview);
        if (pathView != null) {
            pathView.layout(0, pathView.getTop(), r, pathView.getBottom());
        }
    }

    public boolean hasPathView() {
        return findViewById(R.id.pathview) != null;
    }
}
