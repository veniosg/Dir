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

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;

import com.veniosg.dir.R;
import com.veniosg.dir.fragment.PreferenceFragment;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.L;
import static com.veniosg.dir.view.Themer.Flavor.TRANSLUCENT_NAV;

public class Themer {
    public static enum Theme {
        DIR,
        GRAYSCALE,
        DARK,
        HOLO_DARK
    }
    public static enum Flavor {
        TRANSLUCENT_NAV,
        OPAQUE
    }

    public static final void applyTheme(Activity act) {
        boolean trans = false;
        if (act instanceof Themable) {
            trans = ((Themable) act).getThemeFlavor() == TRANSLUCENT_NAV
                        && SDK_INT < L;
        }

        switch (Theme.values()[PreferenceFragment.getThemeIndex(act)]) {
            case DIR:
                act.setTheme(trans ? R.style.Theme_Dir_TranslucentNav : R.style.Theme_Dir);
                break;
            case GRAYSCALE:
                act.setTheme(trans ? R.style.Theme_Dir_Grayscale_TranslucentNav : R.style.Theme_Dir_Grayscale);
                break;
            case DARK:
                act.setTheme(trans ? R.style.Theme_Dir_Dark_TranslucentNav : R.style.Theme_Dir_Dark);
                break;
            case HOLO_DARK:
                act.setTheme(trans ? R.style.Theme_Dir_HoloDark_TranslucentNav : R.style.Theme_Dir_HoloDark);
                break;
        }
    }

    public interface Themable {
        /**
         * Get the theme flavor this component should have.
         * @return One of the Flavor constants.
         */
        public Flavor getThemeFlavor();
    }

    public static final int getThemedResourceId(Context ctx, int attributeId) {
        TypedArray a = ctx.getTheme().obtainStyledAttributes(new int[] {attributeId});
        int attributeResourceId = a.getResourceId(0, 0);
        a.recycle();

        return attributeResourceId;
    }

    public static final Drawable colorDrawable(Context context, Drawable drawable, int color) {
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY));
        return drawable;
    }
}
