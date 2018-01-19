/*
 * Copyright (C) 2014-2018 George Venios
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

package com.veniosg.dir.android.ui;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IntDef;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public interface PathController {
    @Retention(SOURCE)
    @IntDef({STANDARD_INPUT, MANUAL_INPUT})
    public @interface Mode {
    }

    /**
     * The button path selection mode.
     */
    public static final int STANDARD_INPUT = 0;
    /**
     * The text path input mode.
     */
    public static final int MANUAL_INPUT = 1;

    /**
     * See {@link com.veniosg.dir.android.ui.widget.PathView#switchToManualInput() switchToManualInput()}
     * and {@link com.veniosg.dir.android.ui.widget.PathView#switchToStandardInput() switchToStandardInput()}.
     */
    @Mode
    int getMode();

    void switchToStandardInput();

    void switchToManualInput();

    /**
     * @param path The path of the Directory to {@code cd} to.
     * @return Whether the path entered exists and can be navigated to.
     * @see com.veniosg.dir.android.ui.widget.PathView#cd(File)
     */
    boolean cd(String path);

    /**
     * {@code cd} to the passed file. If the file is legal input, sets it as the currently active Directory. Otherwise calls the listener to handle it, if any.
     *
     * @param file The file to {@code cd} to.
     * @return Whether the path entered exists and can be navigated to.
     */
    boolean cd(File file);

    boolean cd(File file, boolean forceNoAnim);

    /**
     * The same as running {@code File.listFiles()} on the currently active Directory.
     */
    File[] ls();

    /**
     * Get the currently active directory.
     *
     * @return A {@link File} representing the currently active directory.
     */
    File getCurrentDirectory();

    /**
     * @return The initial directory.
     * @see #setInitialDirectory(File)
     */
    File getInitialDirectory();

    File getParentDirectory();

    boolean isParentDirectoryNavigable();

    /**
     * Sets the directory the parent activity showed first so that back behavior is fixed.
     *
     * @param file The directory.
     */
    void setInitialDirectory(File file);

    /**
     * @see #setInitialDirectory(File)
     */
    void setInitialDirectory(String path);

    /**
     * Activities containing this bar, will have to call this method when the back button is pressed to provide correct backstack redirection and mode switching.
     *
     * @return Whether this view consumed the event.
     */
    boolean onBackPressed();

    void setOnDirectoryChangedListener(OnDirectoryChangedListener listener);

    void setEnabled(boolean enabled);

    Context getContext();

    Resources getResources();

    /**
     * Interface notifying users of this class when the user has chosen to navigate elsewhere.
     */
    interface OnDirectoryChangedListener {
        void directoryChanged(File newCurrentDir);
    }
}
