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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.veniosg.dir.R;

import java.io.File;

import static android.animation.ObjectAnimator.ofFloat;
import static android.graphics.Color.argb;
import static android.text.TextUtils.TruncateAt.MIDDLE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;
import static com.veniosg.dir.android.util.FileUtils.getFileName;

/**
 * A view that supports displaying a drawable to the left and text in the rest of the space
 * and can scale the text independently of the drawable/self measurements.
 */
public class PathItemView extends FrameLayout {
    private static final float SECONDARY_ITEM_ALPHA = 0.54f;

    private ImageView mCaretView;
    private TextView mTextView;

    static PathItemView newInstanceFor(String path, Context context) {
        PathItemView v = new PathItemView(context, null, -1, android.R.style.Widget_Material_Button_Borderless);
        File file = new File(path);
        v.setTag(file);
        v.mTextView.setText(getFileName(file));
        if (file.getAbsolutePath().equals("/")) {
            v.mCaretView.setVisibility(INVISIBLE);
        }
        v.setLayoutParams(new LayoutParams(WRAP_CONTENT, MATCH_PARENT));

        return v;
    }

    public PathItemView(Context context) {
        super(context);
        init();
    }

    public PathItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PathItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private PathItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        int iconMargin = getResources().getDimensionPixelOffset(R.dimen.item_icon_margin_left);
        int textMargin = getResources().getDimensionPixelOffset(R.dimen.item_text_margin_left);

        setMinimumWidth(0);
        setPadding(0, getPaddingTop(), getPaddingRight(), getPaddingBottom());

        mCaretView = new ImageView(getContext());
        mCaretView.setScaleType(CENTER_INSIDE);
        mCaretView.setImageResource(R.drawable.ic_item_caret);
        mCaretView.getDrawable().setTint(argb((int) (255 * SECONDARY_ITEM_ALPHA), 255, 255, 255));

        mTextView = new TextView(getContext(), null);
        mTextView.setGravity(Gravity.CENTER);
        mTextView.setMaxLines(1);
        mTextView.setSingleLine(true);
        mTextView.setAllCaps(false);
        mTextView.setEllipsize(MIDDLE);
        mTextView.setTextAppearance(getContext(), R.style.TextAppearance_Dir_Large);

        addView(mCaretView, new LayoutParams(WRAP_CONTENT, MATCH_PARENT));
        addView(mTextView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        ((LayoutParams) mCaretView.getLayoutParams()).setMarginStart(iconMargin);
        mTextView.setPaddingRelative(textMargin, 0, 0, 0);
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    void styleAsSecondary() {
        mTextView.setAlpha(SECONDARY_ITEM_ALPHA);
    }

    @SuppressWarnings("unused")
    void styleAsPrimary() {
        mTextView.setAlpha(1);
    }

    boolean isStyledAsSecondary() {
        return mTextView.getAlpha() == SECONDARY_ITEM_ALPHA;
    }

    Animator transformToSecondaryAnimator() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ofFloat(mTextView, "alpha", SECONDARY_ITEM_ALPHA)
        );
        return set;
    }

    Animator transformToPrimaryAnimator() {
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ofFloat(mTextView, "alpha", 1)
        );
        return set;
    }
}
