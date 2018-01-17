/*
 * Copyright (C) 2018 George Venios
 * Copyright (C) 2008 OpenIntents.org
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir;

/**
 * Provides actions, extras, and categories used by providers.
 * <p>These specifiers extend the standard Android specifiers.</p>
 */
public final class IntentConstants {
	public static final String ACTION_REFRESH_LIST = "org.openintents.action.REFRESH_LIST";
	public static final String ACTION_REFRESH_THEME = "org.openintents.action.REFRESH_THEME";
	public static final String ACTION_STORAGE_ACCESS_RESULT = "com.veniosg.extra.STORAGE_ACCESS_RESULT";

	/**
	 * Activity Action: Pick a file through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current file name or file name suggestion.
	 * Returns a new file name as file URI in data.
	 * 
	 * <p>Constant Value: "org.openintents.action.PICK_FILE"</p>
	 */
	public static final String ACTION_PICK_FILE = "org.openintents.action.PICK_FILE";

	/**
	 * Activity Action: Pick a directory through the file manager, or let user
	 * specify a custom file name.
	 * Data is the current directory name or directory name suggestion.
	 * Returns a new directory name as file URI in data.
	 * 
	 * <p>Constant Value: "org.openintents.action.PICK_DIRECTORY"</p>
	 */
	public static final String ACTION_PICK_DIRECTORY = "org.openintents.action.PICK_DIRECTORY";
	
	/**
	 * The title to display.
	 * 
	 * <p>This is shown in the title bar of the file manager.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.TITLE"</p>
	 */
	public static final String EXTRA_TITLE = "org.openintents.extra.TITLE";

	/**
	 * The text on the button to display.
	 * 
	 * <p>Depending on the use, it makes sense to set this to "Open" or "Save".</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.BUTTON_TEXT"</p>
	 */
	public static final String EXTRA_BUTTON_TEXT = "org.openintents.extra.BUTTON_TEXT";

	/**
	 * Flag indicating to show only writeable files and folders.
     *
	 * <p>Constant Value: "org.openintents.extra.WRITEABLE_ONLY"</p>
	 */
	public static final String EXTRA_WRITEABLE_ONLY = "org.openintents.extra.WRITEABLE_ONLY";

	/**
	 * The search query as sent to SearchService.
	 * 
     * <p>Constant Value: "org.openintents.extra.SEARCH_QUERY"</p>
	 */
	public static final String EXTRA_SEARCH_QUERY = "org.openintents.extra.SEARCH_QUERY";

	/**
     * <p>Constant Value: "org.openintents.extra.DIR_PATH"</p>
	 */
	public static final String EXTRA_DIR_PATH = "org.openintents.extra.DIR_PATH";

	/**
	 * Extension by which to filter.
	 *
     * <p>Constant Value: "org.openintents.extra.FILTER_FILETYPE"</p>
	 */
	public static final String EXTRA_FILTER_FILETYPE = "org.openintents.extra.FILTER_FILETYPE";

	/**
	 * Mimetype by which to filter.
	 *
     * <p>Constant Value: "org.openintents.extra.FILTER_MIMETYPE"</p>
	 */
	public static final String EXTRA_FILTER_MIMETYPE = "org.openintents.extra.FILTER_MIMETYPE";

	/**
	 * Only show directories.
	 * 
     * <p>Constant Value: "org.openintents.extra.DIRECTORIES_ONLY"</p>
	 */
	public static final String EXTRA_DIRECTORIES_ONLY = "org.openintents.extra.DIRECTORIES_ONLY";

	public static final String EXTRA_DIALOG_FILE_HOLDER = "org.openintents.extra.DIALOG_FILE";

	public static final String EXTRA_IS_GET_CONTENT_INITIATED = "org.openintents.extra.ENABLE_ACTIONS";

	public static final String EXTRA_FILENAME = "org.openintents.extra.FILENAME";

    public static final String EXTRA_FROM_OI_FILEMANAGER = "org.openintents.extra.FROM_OI_FILEMANAGER";
	public static final String EXTRA_STORAGE_ACCESS_GRANTED = "com.veniosg.extra.STORAGE_ACCESS_GRANTED";
}
