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
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.veniosg.dir.R;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.ui.PathController;
import com.veniosg.dir.android.ui.Themer;

import java.io.File;

import static android.animation.ObjectAnimator.ofFloat;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.os.Environment.getExternalStorageDirectory;
import static android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
import static android.text.InputType.TYPE_TEXT_VARIATION_URI;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.LayoutInflater.from;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.IN_INTERPOLATOR;
import static com.veniosg.dir.android.util.FileUtils.isValidDirectory;
import static com.veniosg.dir.android.util.Utils.backWillExit;
import static com.veniosg.dir.android.ui.CheatSheet.setup;
import static com.veniosg.dir.android.ui.PathController.Mode.MANUAL_INPUT;
import static com.veniosg.dir.android.ui.PathController.Mode.STANDARD_INPUT;
import static com.veniosg.dir.android.ui.Themer.getThemedDimension;
import static com.veniosg.dir.android.ui.widget.PathHorizontalScrollView.RightEdgeRangeListener;

public class PathView extends FrameLayout implements PathController {
    private Mode mCurrentMode = STANDARD_INPUT;
    private File mCurrentDirectory = getExternalStorageDirectory();
    private File mInitialDirectory = getExternalStorageDirectory();

    private PathHorizontalScrollView mPathContainer;
    private View mButtonRight;
    private EditText mManualText;

    private boolean interceptTouch = false;
    private ActivityProvider mActivityProvider = noOpActivityProvider();

    private OnDirectoryChangedListener mDirectoryChangedListener = noOpOnDirectoryChangedListener();
    private final OnClickListener mSwitchToManualOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switchToManualInput();
        }
    };
    private final OnClickListener mCloseManualInputClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String path = mCurrentDirectory.getAbsolutePath();
            setManualInputPath(path);
            switchToStandardInput();
        }
    };
    private final OnClickListener mApplyManualInputClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            manualInputCd(mManualText.getText().toString());
        }
    };
    private final TextView.OnEditorActionListener mOnEditorActionListener = new TextView.OnEditorActionListener() {
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
    };
    private final ActionMode.Callback mEditorActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Themer.setStatusBarColour(mActivityProvider.getActivity(), true);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Themer.setStatusBarColour(mActivityProvider.getActivity(), false);
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
        setLayoutParams(toolbarLayoutParams());
        from(getContext()).inflate(R.layout.widget_pathview, this, true);

        mPathContainer = (PathHorizontalScrollView) findViewById(R.id.pathview_path_container);
        mButtonRight = findViewById(R.id.pathview_button_right);
        View buttonManualLeft = findViewById(R.id.pathview_manual_button_left);
        View buttonManualRight = findViewById(R.id.pathview_manual_button_right);
        mManualText = (EditText) findViewById(R.id.pathview_manual_text);

        mButtonRight.setOnClickListener(mSwitchToManualOnClickListener);
        buttonManualLeft.setOnClickListener(mCloseManualInputClickListener);
        buttonManualRight.setOnClickListener(mApplyManualInputClickListener);
        mManualText.setOnEditorActionListener(mOnEditorActionListener);
        mManualText.setCustomSelectionActionModeCallback(mEditorActionModeCallback);

        // XML doesn't always work
        mManualText.setInputType(TYPE_TEXT_VARIATION_URI | TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        scheduleSetRightEdgeRangeListener();

        // Setup CheatSheets
        setup(mButtonRight);
        setup(buttonManualRight);
        setup(buttonManualLeft);
    }

    @Override
    public Mode getMode() {
        return mCurrentMode;
    }

    @Override
    public void switchToStandardInput() {
        if (mCurrentMode != STANDARD_INPUT) {
            switchToStandardInputAnimator().start();

            mCurrentMode = STANDARD_INPUT;
        }
    }

    @Override
    public void switchToManualInput() {
        if (mCurrentMode != MANUAL_INPUT) {
            switchToManualAnimator().start();

            mCurrentMode = MANUAL_INPUT;
        }
    }

    @Override
    public boolean cd(@NonNull String path) {
        return cd(new File(path));
    }

    @Override
    public boolean cd(File file) {
        return cd(file, false);
    }

    @Override
    public boolean cd(File file, boolean forceNoAnim) {
        boolean result;

        if (isValidDirectory(file)) {
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
    public File getParentDirectory() {
        return getCurrentDirectory().getParentFile();
    }

    @Override
    public boolean isParentDirectoryNavigable() {
        return mCurrentDirectory.getParentFile() != null;
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
            boolean backWillExit = backWillExit(
                    mInitialDirectory.getAbsolutePath(),
                    mCurrentDirectory.getAbsolutePath());
            if (backWillExit || mCurrentDirectory.getParent() == null) {
                return false;
            } else {
                cd(mCurrentDirectory.getParent());
                return true;
            }
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

    public void setActivityProvider(ActivityProvider activityProvider) {
        if (activityProvider == null) {
            this.mActivityProvider = noOpActivityProvider();
        }
        this.mActivityProvider = activityProvider;
    }

    @Override
    public void setEnabled(boolean enabled) {
        interceptTouch = !enabled;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return interceptTouch;
    }

    private void updateViews(File previousDir, File newDir) {
        setManualInputPath(newDir.getAbsolutePath());
        mPathContainer.updateBasedOnPaths(previousDir, newDir, this);
    }

    private void scheduleSetRightEdgeRangeListener() {
        addOnLayoutChangeListener(new OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                removeOnLayoutChangeListener(this);

                mPathContainer.setEdgeListener(new RightEdgeRangeListener() {
                    @Override
                    public int getRange() {
                        return mButtonRight.getWidth();
                    }

                    @Override
                    public void rangeOffsetChanged(int offsetInRange) {
                        mButtonRight.setTranslationX(Math.max(0, offsetInRange));
                    }
                });
            }
        });
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
        if (imm != null) imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    private void setManualInputPath(String path) {
        mManualText.setText(path);
        mManualText.setSelection(path.length());
    }

    private Animator switchToStandardInputAnimator() {
        final View standard = getChildAt(0);
        final View manual = getChildAt(1);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(IN_INTERPOLATOR);
        set.setDuration(ANIM_DURATION);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                manual.setVisibility(VISIBLE);
                standard.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                manual.setVisibility(GONE);
                standard.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                manual.setVisibility(GONE);
                standard.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        set.playTogether(
                ofFloat(manual, "translationY", 0, getHeight()),
                ofFloat(manual, "alpha", 1f, 0f),
                ofFloat(standard, "scaleX", 0.5f, 1f),
                ofFloat(standard, "scaleY", 0.5f, 1f),
                ofFloat(standard, "alpha", 0f, 1f)
        );

        return set;
    }

    private Animator switchToManualAnimator() {
        final View standard = getChildAt(0);
        final View manual = getChildAt(1);

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(IN_INTERPOLATOR);
        set.setDuration(ANIM_DURATION);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                standard.setVisibility(VISIBLE);
                manual.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                standard.setVisibility(GONE);
                manual.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                standard.setVisibility(GONE);
                manual.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        set.playTogether(
                ofFloat(manual, "translationY", getHeight(), 0),
                ofFloat(manual, "alpha", 0f, 1f),
                ofFloat(standard, "scaleX", 1f, 0.5f),
                ofFloat(standard, "scaleY", 1f, 0.5f),
                ofFloat(standard, "alpha", 1f, 0f)
        );
        return set;
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

    private ActivityProvider noOpActivityProvider() {
        return new ActivityProvider() {
            @Override
            public Activity getActivity() {
                return null;
            }
        };
    }

    public interface ActivityProvider {
        Activity getActivity();
    }
}
