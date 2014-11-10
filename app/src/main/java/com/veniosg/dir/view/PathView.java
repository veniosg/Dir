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
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Toolbar;
import android.widget.ViewFlipper;

import com.veniosg.dir.R;

import java.io.File;

import static android.os.Environment.getExternalStorageDirectory;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static com.veniosg.dir.util.FileUtils.isOk;
import static com.veniosg.dir.util.Utils.backWillExit;
import static com.veniosg.dir.view.PathController.Mode.MANUAL_INPUT;
import static com.veniosg.dir.view.PathController.Mode.STANDARD_INPUT;
import static com.veniosg.dir.view.Themer.getThemedDimension;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

public class PathView extends ViewFlipper implements PathController {
    private Mode mCurrentMode = STANDARD_INPUT;
    private File mCurrentDirectory = getExternalStorageDirectory();
    private File mInitialDirectory = getExternalStorageDirectory();
    private OnDirectoryChangedListener mDirectoryChangedListener = noOpOnDirectoryChangedListener();

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
        LayoutInflater.from(getContext()).inflate(R.layout.widget_pathview_toolbar, this);
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
        boolean res = false;

        if (isOk(file)) {
            File oldDir = new File(mCurrentDirectory.getAbsolutePath());
            mCurrentDirectory = file;

            updateViews(forceNoAnim ? null : oldDir, mCurrentDirectory);

            res = true;
        } else
            res = false;

        mDirectoryChangedListener.directoryChanged(file);

        return res;
    }

    private void updateViews(File previousDir, File newDir) {
        if (previousDir == null) {
            // TODO no anim
        }

//            mPathButtons.refresh(forceNoAnim ? null : oldDir, mCurrentDirectory);
//            mPathEditText.setText(file.getAbsolutePath());
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
