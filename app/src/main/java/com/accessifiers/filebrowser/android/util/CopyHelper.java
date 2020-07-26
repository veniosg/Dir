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

package com.accessifiers.filebrowser.android.util;

import android.content.Context;
import android.support.annotation.IntDef;

import com.accessifiers.filebrowser.android.service.CopyService;
import com.accessifiers.filebrowser.mvvm.model.FileHolder;

import java.io.File;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * This class helps simplify copying and moving of files and folders by providing
 * a simple interface and handling the actual operation transparently.
 */
public class CopyHelper {
	@Retention(SOURCE)
	@IntDef({COPY, CUT})
	private @interface Operation {}
	public static final int COPY = 0;
	@SuppressWarnings("WeakerAccess")
    public static final int CUT = 1;

	private List<FileHolder> mClipboard;
	@Operation
	private int mOperation;

    public int getItemCount() {
        if (canPaste()){
            return mClipboard.size();
        } else {
            return 0;
        }
    }

    public void copy(List<FileHolder> tbc){
		mOperation = COPY;
		mClipboard = tbc;
	}
	
	public void copy(FileHolder tbc){
		ArrayList<FileHolder> tbcl = new ArrayList<>();
		tbcl.add(tbc);
		copy(tbcl);
	}
	
	public void cut(List<FileHolder> tbc){
		mOperation = CUT;
		mClipboard = tbc;
	}
	
	public void cut(FileHolder tbc){
		ArrayList<FileHolder> tbcl = new ArrayList<>();
		tbcl.add(tbc);
		cut(tbcl);
	}

    public void clear() {
        mClipboard.clear();
    }

    /**
	 * Call this to check whether there are file references on the clipboard. 
	 */
	public boolean canPaste(){
		return mClipboard != null && !mClipboard.isEmpty();
	}

    @Operation
	public int getOperationType() {
		return mOperation;
	}

	/**
	 * Paste the copied/cut items.
	 * @param copyTo Path to paste to.
	 */
	public void paste(Context c, File copyTo){
		// Quick check just to make sure. Normally this should never be the case as the path we get is not user-generated.
		if(!copyTo.isDirectory())
			return;
		
		switch (mOperation) {
            case COPY:
                CopyService.copyTo(c, mClipboard, copyTo);
                mClipboard.clear();
                break;
            case CUT:
                CopyService.moveTo(c, mClipboard, copyTo);
                mClipboard.clear();
                break;
            default:
                break;
		}
	}
}