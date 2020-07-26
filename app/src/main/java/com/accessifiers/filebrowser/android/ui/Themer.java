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

package com.accessifiers.filebrowser.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.StyleRes;

import com.accessifiers.filebrowser.R;
import com.accessifiers.filebrowser.android.fragment.PreferenceFragment;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public abstract class Themer {
    @Retention(SOURCE)
    @IntDef({DEFAULT, GRAYSCALE, DARK})
    public @interface Theme {}
    public static final int DEFAULT = 0;
    static final int GRAYSCALE = 1;
    static final int DARK = 2;

    public static void applyTheme(Activity act) {
        switch (PreferenceFragment.getThemeIndex(act)) {
            case DEFAULT:
                act.setTheme(R.style.Theme_FileBrowser);
                break;
            case GRAYSCALE:
                act.setTheme(R.style.Theme_FileBrowser_Grayscale);
                break;
            case DARK:
                act.setTheme(R.style.Theme_FileBrowser_Dark);
                break;
        }
    }

    @StyleRes
    public static int getTranslucentThemeId(Activity activity) {
        switch (PreferenceFragment.getThemeIndex(activity)) {
            case DEFAULT:
                return R.style.Theme_FileBrowser_Translucent;
            case GRAYSCALE:
                return R.style.Theme_FileBrowser_Grayscale_Translucent;
            case DARK:
                return R.style.Theme_FileBrowser_Dark_Translucent;
            default:
                return android.R.style.Theme_Translucent;
        }
    }

    public static void setStatusBarColour(Activity activity, boolean actionModeEnabled) {
        if (activity != null) {
            int statusColourAttr = actionModeEnabled ?
                    R.attr.colorActionModeStatusBar : android.R.attr.statusBarColor;
            activity.getWindow().setStatusBarColor(getThemedColor(activity, statusColourAttr));
        }
    }

    public static int getThemedResourceId(Context ctx, int attributeId) {
        return getThemedResourceId(ctx.getTheme(), attributeId);
    }

    public static int getThemedResourceId(Resources.Theme theme, int attributeId) {
        TypedArray a = theme.obtainStyledAttributes(new int[]{attributeId});
        int result = a.getResourceId(0, -1);
        a.recycle();
        return result;
    }

    public static int getThemedColor(Context ctx, int attributeId) {
        return getThemedColor(ctx.getTheme(), attributeId);
    }

    public static int getThemedColor(Resources.Theme theme, int attributeId) {
        TypedArray a = theme.obtainStyledAttributes(new int[] {attributeId});
        int result = a.getColor(0, -1);
        a.recycle();
        return result;
    }

    public static float getThemedDimension(Context ctx, int attributeId) {
        TypedArray a = ctx.getTheme().obtainStyledAttributes(new int[] {attributeId});
        float result = a.getDimension(0, 0);
        a.recycle();
        return result;
    }
}
