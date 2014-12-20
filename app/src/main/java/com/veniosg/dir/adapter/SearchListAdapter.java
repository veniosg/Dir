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

package com.veniosg.dir.adapter;

import android.view.View;
import android.view.ViewGroup;

import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.view.ViewHolder;

import java.util.List;

/**
 * Simple adapter for displaying search results.
 *
 * @author George Venios
 */
public class SearchListAdapter extends FileHolderListAdapter {

    public SearchListAdapter(List<FileHolder> files) {
        super(files);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        ((ViewHolder) view.getTag()).secondaryInfo.setText(
                ((FileHolder) getItem(position)).getFile().getAbsolutePath());

        return view;
    }
}