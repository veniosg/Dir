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
import android.graphics.RadialGradient;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Logger;
import com.veniosg.dir.view.Themer;

/**
 * This class' implementation depends on the internal android layout file "alert_dialog_holo".
 * Thus this is highly susceptible to undefined behavior on major changes in the platform, or on
 * changed implementation of alert dialog layouts from an OEM. <br/><br/>
 * This class should force an exception on any detectable change of the expected layout structure
 * and therefore abort the operation before changing anything.
 * Despite that, it is most certainly not foolproof.
 * @author George Venios
 */
public class DarkTitleDialogFragment extends DialogFragment {

    @Override
    public void onStart() {
        // DialogFragment's onStart is the only place that calls the internal Dialog's .show() method
        super.onStart();

        try {
            // If the internal dialog is shown, style its title
            if (getShowsDialog()
                    && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // Init everything as a field to catch exceptions before any styling occurs.
                // Exceptions here indicate that the layout structure has changed.
                ImageView icon = (ImageView) getDialog().findViewById(android.R.id.icon);
                LinearLayout titleTemplate = (LinearLayout) icon.getParent();
                LinearLayout topPanel = (LinearLayout) titleTemplate.getParent();
                LinearLayout.LayoutParams ttlp = (LinearLayout.LayoutParams) titleTemplate.getLayoutParams();
                View topDivider = topPanel.getChildAt(0);
                View bottomDivider = topPanel.getChildAt(2);
                TextView titleView = (TextView) titleTemplate.getChildAt(1);

                icon.setImageDrawable(lightenDrawable(icon.getDrawable()));
                topDivider.setBackgroundColor(getResources().getColor(
                        Themer.getThemedResourceId(getActivity(), R.attr.colorAccent)));
                topDivider.setVisibility(View.GONE);
                bottomDivider.setBackgroundColor(getResources().getColor(
                        Themer.getThemedResourceId(getActivity(), R.attr.colorAccent)));
                bottomDivider.setVisibility(View.VISIBLE);
                titleView.setTextColor(getActivity().getResources().getColor(R.color.navbar_details));
                titleTemplate.setBackgroundResource(
                        Themer.getThemedResourceId(getActivity(), R.attr.colorLight));
                titleTemplate.setPadding(ttlp.leftMargin, ttlp.topMargin, ttlp.rightMargin, ttlp.bottomMargin);
                ttlp.setMargins(0, 0, 0, 0);
                titleTemplate.requestLayout();
            }
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

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
}
