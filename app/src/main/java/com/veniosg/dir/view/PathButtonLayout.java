/*
 * Copyright (C) 2012 OpenIntents.org
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Utils;

import java.io.File;
import java.util.HashMap;

import static android.os.Environment.getExternalStorageDirectory;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;
import static com.veniosg.dir.util.Utils.dp;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

/**
 * This class handles the displaying of children in {@link Mode#STANDARD_INPUT}, including choosing
 * which children to display, how, and where. It automatically uses the
 * {@link PathBar#mCurrentDirectory} field. <b>Note: </b> Never use this with
 * a width of WRAP_CONTENT.
 */
@Deprecated
class PathButtonLayout extends LinearLayout implements OnLongClickListener {
	private PathBar mPathBar = null;
	/** <absolute path, R.drawable id of image to use> */
	private static HashMap<String, Integer> mPathDrawables = new HashMap<String, Integer>();

	public PathButtonLayout(Context context) {
		super(context);
		init();
	}

	public PathButtonLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	@SuppressLint("SdCardPath")
    private void init() {
		this.setOrientation(LinearLayout.HORIZONTAL);
		this.setOnLongClickListener(this);

		mPathDrawables.put(getExternalStorageDirectory().getAbsolutePath(), R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/sdcard", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/mnt/sdcard", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/mnt/sdcard-ext", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/mnt/sdcard0", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/mnt/sdcard1", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/mnt/sdcard2", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/storage/sdcard0", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/storage/sdcard1", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/storage/sdcard2", R.drawable.ic_navbar_sdcard);
		mPathDrawables.put("/", R.drawable.ic_navbar_home);
	}

	public void setNavigationBar(PathBar pathbar) {
		mPathBar = pathbar;
	}

	/**
	 * Call to properly refresh this {@link PathButtonLayout}'s contents based on the newDir parameter.
	 */
	public void refresh(File oldDir, File newDir) {
        // Remove only the non-matching buttons.
        int lastCommonDirectory;
        if(oldDir != null && getChildCount() > 0) {
            lastCommonDirectory = Utils.lastCommonDirectoryIndex(oldDir, newDir);
        } else {
            // First layout, init by hand.
            lastCommonDirectory = -1;
        }
        for (int i = getChildCount()-1; i > lastCommonDirectory; i--) {
            removeViewAt(i);
        }

		// Reload buttons.
		addPathButtons(lastCommonDirectory+1, newDir);

		// Redraw.
		invalidate();
	}

    /**
	 * Adds the proper buttons according to the fPath parameter.
     * @param firstDirToAdd The index of the first directory of fPath to add.
	 */
	private void addPathButtons(int firstDirToAdd, File fPath) {
		StringBuilder cPath = new StringBuilder();
		char cChar;
        int cDir = 0;
		String path = fPath.getAbsolutePath();

		for (int i = 0; i < path.length(); i++) {
			cChar = path.charAt(i);
			cPath.append(cChar);

			if ((cChar == '/' || i == path.length() - 1)) { // if folder name ended, or path string ended but not if we 're on root
                if (cDir++ >= firstDirToAdd) {
                    // Add a button
                    addView(PathButtonFactory.newButton(cPath.toString(), mPathBar));
                    if(firstDirToAdd != 0) // if not on first draw
                        getChildAt(getChildCount()-1).setAlpha(0); // So that it doesn't flash due to the animation's delay
                }
			}
		}
	}

	/**
	 * Add an icon to be shown instead of a the directory name.
	 * 
	 * @param path
	 *            The path on which to display the icon.
	 * @param drawableResourceId
	 *            The icon' resource id.
	 */
	public void addPathDrawable(String path, int drawableResourceId) {
		mPathDrawables.put(path, drawableResourceId);
	}

	public static HashMap<String, Integer> getPathDrawables() {
		return mPathDrawables;
	}

    @Override
    public boolean onLongClick(View v) {
        mPathBar.switchToManualInput();
        return true;
    }

	private static class PathButtonFactory {
		/**
		 * Creates a Button or ImageButton according to the path. e.g. {@code if(file.getAbsolutePath() == '/')}, it should return an ImageButton with the home drawable on it.
		 *
		 * @param file The directory this button will represent.
		 * @param navbar The {@link PathBar} which will contain the created buttons.
		 * @return An {@link ImageButton} or a {@link Button}.
		 */
		private static View newButton(final File file, final PathBar navbar) {
			View btn = null;

			if (mPathDrawables.containsKey(file.getAbsolutePath())) {
				btn = new ImageButton(navbar.getContext());
				((ImageButton) btn).setImageResource(mPathDrawables.get(file.getAbsolutePath()));
                ((ImageButton) btn).setScaleType(CENTER_INSIDE);
			} else {
                btn = new Button(navbar.getContext());

				((Button) btn).setText(file.getName());
				((Button) btn).setMaxLines(1);
				((Button) btn).setTextColor(navbar.getResources().getColor(
                        getThemedResourceId(navbar.getContext(), R.attr.textColorPathBar)));
			}

            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.rightMargin = (int) dp(-4, navbar.getContext());

            btn.setLayoutParams(params);
			btn.setTag(file);
			btn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					navbar.cd((File) v.getTag());
				}
			});
			btn.setOnLongClickListener(navbar.getPathButtonLayout());
            btn.setBackgroundDrawable(navbar.getItemBackgroundDrawable(navbar.getContext(),
                    file.getAbsolutePath()));

            // We have to set this after adding the background as it'll cancel the padding out.
            if(btn instanceof Button) {
                int sidePadding = (int) dp(8, navbar.getContext());
                btn.setPadding(sidePadding, btn.getPaddingTop(), sidePadding, btn.getPaddingBottom());
            }


            return btn;
		}

        /**
		 * @see {@link #newButton(File, PathBar)}
		 */
		private static View newButton(String path, PathBar navbar) {
			return newButton(new File(path), navbar);
		}
	}
}