/*
 * Copyright (C) 2018 George Venios
 *
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

package com.veniosg.dir.android.adapter.viewholder;

import android.view.View;
import android.view.ViewGroup;

import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;

public class BookmarkListViewHolder extends FileListViewHolder {
    public BookmarkListViewHolder(ViewGroup parent) {
        super(parent);
        secondaryInfo.setMaxLines(3);
        tertiaryInfo.setVisibility(GONE);
    }

    public void bind(String filePath, OnItemClickListener listener) {
        FileHolder fileHolder = new FileHolder(new File(filePath), itemView.getContext());
        super.bind(fileHolder, listener);
        secondaryInfo.setText(filePath);
    }
}
