package com.veniosg.dir.view;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.veniosg.dir.R;

import java.io.File;

import static android.text.TextUtils.TruncateAt.MIDDLE;
import static android.view.Gravity.CENTER_VERTICAL;
import static android.view.View.TEXT_ALIGNMENT_GRAVITY;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.LayoutParams;
import static com.veniosg.dir.util.FileUtils.getFileName;
import static com.veniosg.dir.util.Utils.dp;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

public class PathButtonFactory {
    private PathButtonFactory() {
    }

    /**
     * Creates a Button or ImageButton according to the path. e.g. {@code if(file.getAbsolutePath() == '/')}, it should return an ImageButton with the home drawable on it.
     *
     * @param file           The directory this button will represent.
     * @param pathController The {@link PathBar} which will contain the created buttons.
     * @return An {@link android.widget.ImageButton} or a {@link android.widget.Button}.
     */
    public static Button newButton(final File file, final PathController pathController) {
        Button btn = new Button(pathController.getContext(), null, R.attr.pathbarItemStyle);
        int eightDp = (int) dp(8, pathController.getContext());
        int iconMargin = pathController.getResources().getDimensionPixelOffset(R.dimen.item_icon_margin_left);
        int textMargin = pathController.getResources().getDimensionPixelOffset(R.dimen.item_text_margin_left);
        int caretSize = (int) dp(24, pathController.getContext());
        int marginLeft = iconMargin;
        int compoundPadding = textMargin - marginLeft - caretSize - eightDp;
        LayoutParams params = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);

        btn.setText(getFileName(file));
        btn.setMinimumWidth(0);
        btn.setMaxLines(1);
        btn.setGravity(CENTER_VERTICAL);
        btn.setTextAlignment(TEXT_ALIGNMENT_GRAVITY);
        btn.setTextColor(pathController.getResources().getColor(
                getThemedResourceId(pathController.getContext(), R.attr.textColorPathBar)));
        btn.setPadding(eightDp, btn.getPaddingTop(), eightDp * 2, btn.getPaddingBottom());
        btn.setLayoutParams(params);
        btn.setTag(file);
        btn.setEllipsize(MIDDLE);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathController.cd((File) v.getTag());
            }
        });
        if (!file.getAbsolutePath().equals("/")) {
            btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_item_caret, 0, 0, 0);
            btn.setCompoundDrawablePadding(compoundPadding);
            ((LayoutParams) btn.getLayoutParams()).setMarginStart(marginLeft);
        } else {
            // TODO fix me
            ((LayoutParams) btn.getLayoutParams()).setMarginStart(marginLeft + eightDp + caretSize);
        }

        return btn;
    }

    /**
     * @see {@link #newButton(File, PathBar)}
     */
    public static View newButton(String path, PathController pathController) {
        return newButton(new File(path), pathController);
    }
}
