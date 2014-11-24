package com.veniosg.dir.view;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.View;
import android.widget.Button;

import com.veniosg.dir.R;

import java.io.File;

import static android.graphics.Typeface.NORMAL;
import static android.graphics.Typeface.create;
import static android.view.Gravity.CENTER;
import static android.view.View.TEXT_ALIGNMENT_GRAVITY;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.LayoutParams;
import static com.veniosg.dir.util.FileUtils.getFileName;
import static com.veniosg.dir.util.Utils.dp;

public abstract class PathButtonFactory {
    private static final Typeface TYPEFACE = create("sans-serif-regular", NORMAL);

    private PathButtonFactory() {
    }

    /**
     * Creates a Button or ImageButton according to the path. e.g. {@code if(file.getAbsolutePath() == '/')}, it should return an ImageButton with the home drawable on it.
     *
     * @param file           The directory this button will represent.
     * @param pathController The {@link PathBar} which will contain the created buttons.
     * @return An {@link android.widget.ImageButton} or a {@link android.widget.Button}.
     */
    public static Button newButton(final File file, Context context) {
        Button btn = new Button(context, null, -1, android.R.style.Widget_Material_Button_Borderless);
        int eightDp = (int) dp(8, context);
        int iconMargin = context.getResources().getDimensionPixelOffset(R.dimen.item_icon_margin_left);
        int textMargin = context.getResources().getDimensionPixelOffset(R.dimen.item_text_margin_left);
        int caretSize = (int) dp(24, context);
        int marginLeft = iconMargin;
        int compoundPadding = textMargin - marginLeft - caretSize;
        LayoutParams params = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);

        btn.setText(getFileName(file));
        btn.setMinimumWidth(0);
        btn.setMaxLines(1);
        btn.setAllCaps(false);
        btn.setTypeface(TYPEFACE);
        btn.setPadding(eightDp, btn.getPaddingTop(), eightDp * 2, btn.getPaddingBottom());
        btn.setTag(file);
        if (file.getAbsolutePath().equals("/")) {
            params.setMarginStart(marginLeft*2);
            btn.setPaddingRelative(eightDp, btn.getPaddingTop(), eightDp, btn.getPaddingBottom());
        } else {
            btn.setCompoundDrawablePadding(compoundPadding);
            params.setMarginStart(marginLeft - eightDp);
        }
        btn.setLayoutParams(params);

        return btn;
    }

    /**
     * @see {@link #newButton(File, PathBar)}
     */
    public static View newButton(String path, Context context) {
        return newButton(new File(path), context);
    }
}
