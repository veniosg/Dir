/*
 * Copyright (C) 2012 OpenIntents.org
 * Copyright (C) 2014-2015 George Venios
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

package com.veniosg.dir.android.misc;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.veniosg.dir.android.fragment.PreferenceFragment;
import com.veniosg.dir.android.util.FileUtils;
import com.veniosg.dir.android.util.Logger;
import com.veniosg.dir.android.util.Utils;
import com.veniosg.dir.mvvm.model.DirectoryHolder;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.veniosg.dir.android.util.Utils.getIconForFile;

public class DirectoryScanner extends Thread {
	/** List of contents is ready. */
	public static final int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500;	// List of contents is ready, obj = DirectoryHolder
	public static final int MESSAGE_SET_PROGRESS = 501;	// Set progress bar, arg1 = current value, arg2 = max value

	private File currentDirectory;

	private boolean running = false;
	private boolean cancelled;

	private String mSdCardPath;
	private Context mContext;
    private MimeTypes mMimeTypes;
	private Handler mHandler;
	@NonNull
	private String mFilterFiletype;
	@NonNull
	private String mFilterMimetype;

	private boolean mWriteableOnly;
	private boolean mDirectoriesOnly;

	// Update progress bar every n files
	static final private int PROGRESS_STEPS = 50;

	// Scan related variables.
	private int totalCount, progress;
	private long operationStartTime;
	private boolean noMedia, displayHidden;
	private File[] files;
	/** We keep all these three instead of one, so that sorting is done separately on each. */
	private List<FileHolder> listDir, listFile, listSdCard;

	public DirectoryScanner(File directory, Context context, Handler handler,
							MimeTypes mimeTypes,
							@NonNull String filterFiletype,
							@NonNull String filterMimetype,
							boolean writeableOnly,
							boolean directoriesOnly) {
		super("Directory Scanner");
		currentDirectory = directory;
		this.mContext = context;
		this.mHandler = handler;
		this.mMimeTypes = mimeTypes;
		this.mFilterFiletype = filterFiletype;
		this.mFilterMimetype = filterMimetype;
		this.mSdCardPath = Environment.getExternalStorageDirectory().getAbsolutePath();
		this.mWriteableOnly = writeableOnly;
		this.mDirectoriesOnly = directoriesOnly;
	}

	private void init(){
        Logger.logV(Logger.TAG_DIRSCANNER, "Scanning directory " + currentDirectory);

		if (cancelled) {
            Logger.logV(Logger.TAG_DIRSCANNER, "Scan aborted");
			return;
		}

		totalCount = 0;
		progress = 0;
		files = currentDirectory.listFiles();
		noMedia = false;
		displayHidden = PreferenceFragment.getDisplayHiddenFiles(mContext);

		operationStartTime = SystemClock.uptimeMillis();

		if (files == null) {
            Logger.logV(Logger.TAG_DIRSCANNER, "Returned null - inaccessible directory?");
		} else {
			totalCount = files.length;
		}
        Logger.logV(Logger.TAG_DIRSCANNER, "Total count=" + totalCount);

		/** Directory container */
 		listDir = new ArrayList<>(totalCount);
		/** File container */
 		listFile = new ArrayList<>(totalCount);
		/** External storage container*/
		listSdCard = new ArrayList<>(3);
	}

	public void run() {
		running = true;
		init();

		// Scan files
		if (files != null) {
			for (File currentFile : files){
				if (cancelled) {
                    Logger.logV(Logger.TAG_DIRSCANNER, "Scan aborted while checking files");
					return;
				}

				progress++;
				updateProgress(progress, totalCount);

				// It's the noMedia file. Raise the flag.
				if(currentFile.getName().equalsIgnoreCase(FileUtils.NOMEDIA_FILE_NAME))
					noMedia = true;

				//If the user doesn't want to display hidden files and the file is hidden, ignore this file.
				if (!displayHidden && currentFile.isHidden()){
					continue;
				}

				// It's a directory. Handle it.
				if (currentFile.isDirectory()) {
					// It's the sd card.
					if (currentFile.getAbsolutePath().equals(mSdCardPath)) {
						listSdCard.add(new FileHolder(currentFile,
                                mMimeTypes.getMimeType(currentFile.getName()),
                                Utils.getSdCardIcon(mContext)));
					}
					// It's a normal directory.
					else {
//                      if (!mWriteableOnly || currentFile.canWrite()) {
                            String mimetype = mMimeTypes.getMimeType(currentFile.getName());
                            listDir.add(new FileHolder(currentFile,
                                    mMimeTypes.getMimeType(currentFile.getName()),
                                    getIconForFile(mContext, mimetype, currentFile)));
//                      }
					}
				// It's a file. Handle it too :P
				} else {
					String fileName = currentFile.getName();

					// Get the file's mimetype.
					String mimetype = mMimeTypes.getMimeType(fileName);
					String filetype = FileUtils.getExtension(fileName);

					boolean fileTypeAllowed = mFilterFiletype.isEmpty()
							|| filetype.equalsIgnoreCase(mFilterFiletype);
					boolean mimeTypeAllowed = mFilterMimetype.isEmpty()
							|| mFilterMimetype.contentEquals("*/*")
							|| mimetype.contentEquals(mFilterMimetype);
					if (!mDirectoriesOnly && fileTypeAllowed && mimeTypeAllowed) {
                        listFile.add(new FileHolder(currentFile,
                                mimetype,
                                // Take advantage of the already parsed mimetype to set a specific icon.
                                getIconForFile(mContext, mimetype, currentFile)));
					}
				}
			}
		}

        Logger.logV(Logger.TAG_DIRSCANNER, "Sorting results...");

        int sortBy = PreferenceFragment.getSortBy(mContext);
		boolean ascending = PreferenceFragment.getAscending(mContext);

		// Sort lists
		if (!cancelled) {
			Collections.sort(listSdCard);
			Collections.sort(listDir, Comparators.getForDirectory(sortBy, ascending));
			Collections.sort(listFile, Comparators.getForFile(sortBy, ascending));
		}

		// Return lists
		if (!cancelled) {
            Logger.logV(Logger.TAG_DIRSCANNER, "Sending data back to main thread");

			DirectoryHolder contents = new DirectoryHolder();

			contents.listDir = listDir;
			contents.listFile = listFile;
			contents.listSdCard = listSdCard;
			contents.noMedia = noMedia;

			Message msg = mHandler.obtainMessage(MESSAGE_SHOW_DIRECTORY_CONTENTS);
			msg.obj = contents;
			msg.sendToTarget();
		}

		running = false;
	}

	private void updateProgress(int progress, int maxProgress) {
		// Only update the progress bar every n steps...
		if ((progress % PROGRESS_STEPS) == 0) {
			// Also don't update for the first second.
			long curTime = SystemClock.uptimeMillis();

			if (curTime - operationStartTime < 1000L) {
				return;
			}

			// Okay, send an update.
			Message msg = mHandler.obtainMessage(MESSAGE_SET_PROGRESS);
			msg.arg1 = progress;
			msg.arg2 = maxProgress;
			msg.sendToTarget();
		}
	}

	public void cancel(){
		cancelled = true;
	}

	public boolean getNoMedia() {
		return noMedia;
	}

	public boolean isRunning(){
		return running;
	}
}

/**
 * The container class for all comparators.
 */
class Comparators{
	private static final int NAME = 1;
	private static final int SIZE = 2;
	private static final int LAST_MODIFIED = 3;
	private static final int EXTENSION = 4;

	public static Comparator<FileHolder> getForFile(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new SizeComparator(ascending);
		case EXTENSION: return new ExtensionComparator(ascending);
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
	public static Comparator<FileHolder> getForDirectory(int comparator, boolean ascending){
		switch(comparator){
		case NAME: return new NameComparator(ascending);
		case SIZE: return new NameComparator(ascending); //Not a bug! Getting directory's size is very slow
		case EXTENSION: return new NameComparator(ascending); // Sorting by name as folders don't have extensions.
		case LAST_MODIFIED: return new LastModifiedComparator(ascending);
		default: return null;
		}
	}
}

abstract class FileHolderComparator implements Comparator<FileHolder>{
	private boolean ascending = true;

	FileHolderComparator(boolean asc){
		ascending = asc;
	}

	FileHolderComparator(){
		this(true);
	}

	public int compare(FileHolder f1, FileHolder f2){
		return comp((ascending ? f1 : f2), (ascending ? f2 : f1));
	}

	protected abstract int comp(FileHolder f1, FileHolder f2);
}

class NameComparator extends FileHolderComparator{
	public NameComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase());
	}
}

class SizeComparator extends FileHolderComparator{
	public SizeComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return ((Long)f1.getFile().length()).compareTo(f2.getFile().length());
	}
}

class ExtensionComparator extends FileHolderComparator{
	public ExtensionComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return f1.getExtension().compareTo(f2.getExtension());
	}
}

class LastModifiedComparator extends FileHolderComparator{
	public LastModifiedComparator(boolean asc){
		super(asc);
	}

	@Override
	protected int comp(FileHolder f1, FileHolder f2) {
	    return ((Long)f1.getFile().lastModified()).compareTo(f2.getFile().lastModified());
	}
}
