package com.veniosg.dir.view;

import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.veniosg.dir.R;

import java.io.File;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;
import static android.widget.LinearLayout.LayoutParams;
import static com.veniosg.dir.util.Utils.dp;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

public class PathButtonFactory {
    private PathButtonFactory() {}

    /**
     * Creates a Button or ImageButton according to the path. e.g. {@code if(file.getAbsolutePath() == '/')}, it should return an ImageButton with the home drawable on it.
     *
     * @param file   The directory this button will represent.
     * @param pathController The {@link PathBar} which will contain the created buttons.
     * @return An {@link android.widget.ImageButton} or a {@link android.widget.Button}.
     */
    public static Button newButton(final File file, final PathController pathController) {
        Button btn = new Button(pathController.getContext(), null, R.attr.pathbarItemStyle);

        btn.setText(file.getName());
        btn.setMaxLines(1);
        btn.setTextColor(pathController.getResources().getColor(
                getThemedResourceId(pathController.getContext(), R.attr.textColorPathBar)));

        LayoutParams params = new LayoutParams(WRAP_CONTENT, MATCH_PARENT);
        params.rightMargin = (int) dp(-4, pathController.getContext());

        btn.setLayoutParams(params);
        btn.setTag(file);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pathController.cd((File) v.getTag());
            }
        });

        // We have to set this after adding the background as it'll cancel the padding out.
        int sidePadding = (int) dp(8, pathController.getContext());
        btn.setPadding(sidePadding, btn.getPaddingTop(), sidePadding, btn.getPaddingBottom());

        return btn;
    }

    /**
     * @see {@link #newButton(File, PathBar)}
     */
    public static View newButton(String path, PathController pathController) {
        return newButton(new File(path), pathController);
    }
}
