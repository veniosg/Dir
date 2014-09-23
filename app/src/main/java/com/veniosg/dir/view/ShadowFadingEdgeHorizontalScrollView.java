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

package com.veniosg.dir.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.HorizontalScrollView;

/**
 * @author George Venios
 */
public class ShadowFadingEdgeHorizontalScrollView extends HorizontalScrollView {
    public ShadowFadingEdgeHorizontalScrollView(Context context) {
        super(context);
        init();
    }

    public ShadowFadingEdgeHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShadowFadingEdgeHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setHorizontalFadingEdgeEnabled(true);
        setFadingEdgeLength((int) TypedValue.
                applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
    }

    @Override
    public int getSolidColor() {
        return Color.BLACK;
    }
}
