package com.veniosg.dir.view;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.io.File;

public interface PathController {
    /**
     * The available Modes of this PathBar. </br> See {@link PathBar#switchToManualInput() switchToManualInput()} and {@link PathBar#switchToStandardInput() switchToStandardInput()}.
     */
    public enum Mode {
        /**
         * The button path selection mode.
         */
        STANDARD_INPUT,
        /**
         * The text path input mode.
         */
        MANUAL_INPUT
    }

    Mode getMode();
    void switchToStandardInput();
    void switchToManualInput();

    /**
     * @see {@link com.veniosg.dir.view.PathBar#cd(File) cd(File)}
     * @param path
     *            The path of the Directory to {@code cd} to.
     * @return Whether the path entered exists and can be navigated to.
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
     * @see #setInitialDirectory(File)
     * @return The initial directory.
     */
    File getInitialDirectory();

    /**
     * Sets the directory the parent activity showed first so that back behavior is fixed.
     *
     * @param initDir The directory.
     */
    void setInitialDirectory(File file);

    /**
     * See {@link #setInitialDirectory(File)}.
     */
    void setInitialDirectory(String path);

    /**
     * Activities containing this bar, will have to call this method when the back button is pressed to provide correct backstack redirection and mode switching.
     *
     * @return Whether this view consumed the event.
     */
    boolean onBackPressed();

    void setOnDirectoryChangedListener(OnDirectoryChangedListener listener);

    /**
     * Interface notifying users of this class when the user has chosen to navigate elsewhere.
     */
    public interface OnDirectoryChangedListener {
        public void directoryChanged(File newCurrentDir);
    }
}
