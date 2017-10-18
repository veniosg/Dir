/*
 * Copyright (C) 2008-2012 OpenIntents.org
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

package com.veniosg.dir.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.robotium.solo.Solo;
import com.veniosg.dir.android.activity.SaveAsActivity;
import com.veniosg.dir.android.util.Logger;

import java.io.File;
import java.io.IOException;

public class SaveAsActivityTest extends InstrumentationTestCase {
    private Solo solo;
    private String sdcardPath;
    private Activity mActivity;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';

        // need to do this before creating activity
        TestUtils.cleanDirectory(new File(sdcardPath, "oi-filemanager-tests"));
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
    }

    protected void tearDown() throws Exception {
        try {
            this.solo.finishOpenedActivities();
        } catch (Throwable e) {
            Logger.log(e);
        }
        super.tearDown();
        TestUtils.cleanDirectory(new File(sdcardPath + "oi-filemanager-tests"));
    }

    @Suppress
    public void testIntentSaveAs() throws IOException {
        // TODO fix test
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");

        Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt");
        mActivity = getInstrumentation().startActivitySync(getFileManagerSaveAsIntent()
                .setAction(Intent.ACTION_VIEW).setData(uri));
        solo = new Solo(getInstrumentation(), mActivity);

        solo.clickLongOnText(Environment.getExternalStorageDirectory().getParentFile().getName());
        solo.enterText(0, "oi-target.txt");
        solo.sendKey(Solo.ENTER);
        assertTrue(new File(sdcardPath, "oi-filemanager-tests/oi-target.txt").exists());
        solo.goBack();
        solo.goBack();
    }

    private Intent getFileManagerSaveAsIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.veniosg.dir", SaveAsActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
