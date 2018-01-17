/*
 * Copyright (C) 2018 George Venios
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

package com.veniosg.dir.android.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.veniosg.dir.mvvm.model.FileHolder;
import com.veniosg.dir.mvvm.model.storage.operation.CompressOperation;
import com.veniosg.dir.mvvm.model.storage.operation.ExtractOperation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.veniosg.dir.mvvm.model.storage.operation.FileOperationRunnerInjector.operationRunner;
import static com.veniosg.dir.mvvm.model.storage.operation.argument.CompressArguments.compressArgs;
import static com.veniosg.dir.mvvm.model.storage.operation.argument.ExtractArguments.extractArgs;
import static java.util.Collections.singletonList;

public class ZipService extends IntentService {
    private static final String ACTION_COMPRESS = "com.veniosg.dir.action.COMPRESS";
    private static final String ACTION_EXTRACT = "com.veniosg.dir.action.EXTRACT";
    private static final String EXTRA_FILES = "com.veniosg.dir.action.FILES";

    public ZipService() {
        super(ZipService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        List<FileHolder> files = intent.getParcelableArrayListExtra(EXTRA_FILES);
        File to = new File(intent.getData().getPath());

        if (ACTION_COMPRESS.equals(intent.getAction())) {
            operationRunner(this).run(new CompressOperation(this), compressArgs(to, files));
        } else if (ACTION_EXTRACT.equals(intent.getAction())) {
            operationRunner(this).run(new ExtractOperation(this), extractArgs(to, files));
        }
    }

    public static void extractTo(Context c, final FileHolder tbe, File extractTo) {
        extractTo(c, singletonList(tbe), extractTo);
    }

    public static void compressTo(Context c, final FileHolder tbc, File compressTo) {
        compressTo(c, singletonList(tbc), compressTo);
    }

    public static void extractTo(Context c, List<FileHolder> tbe, File extractTo) {
        Intent i = new Intent(ACTION_EXTRACT);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(extractTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbe instanceof ArrayList
                ? (ArrayList<FileHolder>) tbe
                : new ArrayList<>(tbe));
        c.startService(i);
    }

    public static void compressTo(Context c, List<FileHolder> tbc, File compressTo) {
        Intent i = new Intent(ACTION_COMPRESS);
        i.setClassName(c, ZipService.class.getName());
        i.setData(Uri.fromFile(compressTo));
        i.putParcelableArrayListExtra(EXTRA_FILES, tbc instanceof ArrayList
                ? (ArrayList<FileHolder>) tbc
                : new ArrayList<>(tbc));
        c.startService(i);
    }
}
