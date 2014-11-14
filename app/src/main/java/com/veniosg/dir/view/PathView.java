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
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.ViewFlipper;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Logger;

import java.io.File;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Environment.getExternalStorageDirectory;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.text.InputType.TYPE_TEXT_VARIATION_URI;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static com.veniosg.dir.util.FileUtils.isOk;
import static com.veniosg.dir.util.Utils.backWillExit;
import static com.veniosg.dir.util.Utils.lastCommonDirectoryIndex;
import static com.veniosg.dir.view.PathButtonFactory.newButton;
import static com.veniosg.dir.view.PathController.Mode.MANUAL_INPUT;
import static com.veniosg.dir.view.PathController.Mode.STANDARD_INPUT;
import static com.veniosg.dir.view.Themer.getThemedDimension;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

public class PathView extends ViewFlipper implements PathController {
    private Mode mCurrentMode = STANDARD_INPUT;
    private File mCurrentDirectory = getExternalStorageDirectory();
    private File mInitialDirectory = getExternalStorageDirectory();

    private ViewGroup mPathContainer;
    private View mButtonRight;
    private View mManualButtonLeft;
    private View mManualButtonRight;
    private EditText mManualText;

    private OnDirectoryChangedListener mDirectoryChangedListener = noOpOnDirectoryChangedListener();
    private final OnClickListener mSwitchToManualOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchToManualInput();
        }
    };
    private final OnClickListener mResetManualInputClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = mCurrentDirectory.getAbsolutePath();
            setManualInputPath(path);
        }
    };

    private final OnClickListener mApplyManualInputClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            manualInputCd(mManualText.getText().toString());
        }
    };

    public PathView(Context context) {
        super(context);
        init();
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setId(R.id.pathview);
        setLayoutParams(toolbarLayoutParams());
        LayoutInflater.from(getContext()).inflate(R.layout.widget_pathview, this);

        mPathContainer = (ViewGroup) findViewById(R.id.pathview_path_container);
        mButtonRight = findViewById(R.id.pathview_button_right);
        mManualButtonLeft = findViewById(R.id.pathview_manual_button_left);
        mManualButtonRight = findViewById(R.id.pathview_manual_button_right);
        mManualText = (EditText) findViewById(R.id.pathview_manual_text);

        mButtonRight.setOnClickListener(mSwitchToManualOnClickListener);
        mManualButtonLeft.setOnClickListener(mResetManualInputClickListener);
        mManualButtonRight.setOnClickListener(mApplyManualInputClickListener);
        mManualText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == IME_ACTION_GO)
                        || ((event.getAction() == ACTION_DOWN) && ((event.getKeyCode() == KEYCODE_DPAD_CENTER) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {
                    if (manualInputCd(v.getText().toString()))
                        // Since we have successfully navigated.
                        return true;
                }

                // We can't focus the text again so just hide the damned thing.
                hideKeyboard();
                return false;
            }
        });

        // XML doesn't always work
        mManualText.setInputType(TYPE_TEXT_VARIATION_URI | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
    }

    @Override
    public Mode getMode() {
        return mCurrentMode;
    }

    @Override
    public void switchToStandardInput() {
        setDisplayedChild(0);
        mCurrentMode = STANDARD_INPUT;
    }

    @Override
    public void switchToManualInput() {
        setDisplayedChild(1);
        mCurrentMode = MANUAL_INPUT;
    }

    @Override
    public boolean cd(String path) {
        return cd(new File(path));
    }

    @Override
    public boolean cd(File file) {
        return cd(file, false);
    }

    @Override
    public boolean cd(File file, boolean forceNoAnim) {
        boolean result = false;

        if (isOk(file)) {
            File oldDir = new File(mCurrentDirectory.getAbsolutePath());
            mCurrentDirectory = file;

            updateViews(forceNoAnim ? null : oldDir, mCurrentDirectory);

            result = true;
        } else {
            result = false;
        }

        mDirectoryChangedListener.directoryChanged(file);

        return result;
    }

    private void updateViews(File previousDir, File newDir) {
        setManualInputPath(newDir.getAbsolutePath());
        updatePathContainer(previousDir, newDir);
    }

    /**
     * @param p Pass null to refresh the whole view.
     * @param n The new current directory.
     */
    private void updatePathContainer(File p, File n) {
        // Remove only the non-matching buttons.
        int lastCommonDirectory;
        if(p != null && mPathContainer.getChildCount() > 0) {
            lastCommonDirectory = lastCommonDirectoryIndex(p, n);
        } else {
            // First layout, init by hand.
            lastCommonDirectory = -1;
        }
        for (int i = mPathContainer.getChildCount()-1; i > lastCommonDirectory; i--) {
            mPathContainer.removeViewAt(i);
        }

        // Reload buttons.
        fillPathContainer(lastCommonDirectory + 1, n);
    }

    /**
     * Adds new buttons according to the fPath parameter.
     * @param firstDirToAdd The index of the first directory of fPath to add.
     */
    private void fillPathContainer(int firstDirToAdd, File fPath) {
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
                    mPathContainer.addView(newButton(cPath.toString(), this));
                    // TODO uncomment
//                    if(firstDirToAdd != 0) // if not on first draw
//                        mPathContainer.getChildAt(mPathContainer.getChildCount() - 1).setAlpha(0); // So that it doesn't flash due to the animation's delay
                }
            }
        }
    }

    @Override
    public File[] ls() {
        return mCurrentDirectory.listFiles();
    }

    @Override
    public File getCurrentDirectory() {
        return mCurrentDirectory;
    }

    @Override
    public File getInitialDirectory() {
        return mInitialDirectory;
    }

    @Override
    public void setInitialDirectory(File initDir) {
        mInitialDirectory = initDir;
        cd(initDir);
    }

    @Override
    public void setInitialDirectory(String initPath) {
        setInitialDirectory(new File(initPath));
    }

    @Override
    public boolean onBackPressed() {
        // Switch mode.
        if (mCurrentMode == MANUAL_INPUT) {
            switchToStandardInput();
        }
        // Go back.
        else if (mCurrentMode == STANDARD_INPUT) {
            if (!backWillExit(mInitialDirectory.getAbsolutePath(),
                    mCurrentDirectory.getAbsolutePath())) {
                cd(mCurrentDirectory.getParent());
                return true;
            } else
                return false;
        }

        return true;
    }

    @Override
    public void setOnDirectoryChangedListener(OnDirectoryChangedListener listener) {
        if (listener != null) {
            mDirectoryChangedListener = listener;
        } else {
            mDirectoryChangedListener = noOpOnDirectoryChangedListener();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }

    private boolean manualInputCd(String path) {
        if (cd(path)) {
            hideKeyboard();
            switchToStandardInput();
            return true;
        } else {
            Logger.log(Log.WARN, Logger.TAG_PATHBAR, "Input path does not exist or is not a folder!");
            return false;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void setManualInputPath(String path) {
        mManualText.setText(path);
        mManualText.setSelection(path.length());
    }

    private Toolbar.LayoutParams toolbarLayoutParams() {
        int abHeight = (int) getThemedDimension(getContext(), android.R.attr.actionBarSize);
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(MATCH_PARENT, abHeight);
        params.topMargin = abHeight;

        return params;
    }

    private static OnDirectoryChangedListener noOpOnDirectoryChangedListener() {
        return new OnDirectoryChangedListener() {
            @Override
            public void directoryChanged(File newCurrentDir) {
            }
        };
    }
}
