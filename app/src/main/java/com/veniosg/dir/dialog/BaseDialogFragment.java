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

package com.veniosg.dir.dialog;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;

import static android.R.attr.textColorPrimary;
import static com.veniosg.dir.view.Themer.getThemedColor;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

abstract class BaseDialogFragment extends DialogFragment {

    public BaseDialogFragment() {
        super();
    }

    protected Drawable tintIcon(Drawable icon) {
        return tintDrawable(icon, getIconTintColor());
    }

    protected Drawable tintIcon(int resId) {
        return tintDrawable(resId, getIconTintColor());
    }

    private Drawable tintDrawable(int resId, int tint) {
        return tintDrawable(getResources().getDrawable(resId), tint);
    }

    private Drawable tintDrawable(Drawable drawable, int tint) {
        Drawable d = drawable
                .getConstantState()
                .newDrawable(getActivity().getResources())
                .mutate();
        d.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        return d;
    }

    /**
     * Overrides tint set with tintDrawable().
     */
    private Drawable lightenDrawable(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        Drawable newDrawable = drawable.getConstantState().newDrawable().mutate();
        newDrawable.setColorFilter(new LightingColorFilter(
                Color.rgb(255, 255, 255),
                Color.argb(200, 255, 255, 255)));

        return newDrawable;
    }

    private int getIconTintColor() {
        // Resolve AlertDialog theme
        TypedValue outValue = new TypedValue();
        getActivity().getTheme().resolveAttribute(android.R.attr.alertDialogTheme, outValue, true);
        int theme = outValue.resourceId;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);

        // Try to get title color
        int colorId = getThemedResourceId(contextThemeWrapper.getTheme(), textColorPrimary);
        int tintColor;
        if (colorId == -1) {
            tintColor = getThemedColor(contextThemeWrapper.getTheme(), textColorPrimary);
        } else {
            tintColor = contextThemeWrapper.getResources().getColor(colorId);
        }

        return tintColor;
    }
}
