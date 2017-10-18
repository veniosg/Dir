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

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;
import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.android.activity.IntentFilterActivity;
import com.veniosg.dir.android.util.Logger;

import java.io.File;
import java.io.IOException;

public class IntentFilterActivityTest extends ActivityInstrumentationTestCase2<IntentFilterActivity> {
    private Solo solo;
    private String sdcardPath;

    public IntentFilterActivityTest() {
        super(IntentFilterActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';

        // need to do this before creating activity
        TestUtils.cleanDirectory(new File(sdcardPath, "oi-filemanager-tests"));
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");

        setActivityIntent(getFileManagerIntentFilterIntent());

        solo = new Solo(getInstrumentation(), getActivity());
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

    public void testIntentPickFile() throws IOException {
        // startActivityForResult is, I think, impossible to test on Robotium
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-pick-file", "");

        Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests");
        getInstrumentation().startActivitySync(getFileManagerIntentFilterIntent()
                .setAction(IntentConstants.ACTION_PICK_FILE).setData(uri));

        solo.clickOnText("oi-pick-file");

        solo.goBack();
    }

    public void testIntentRememberPickFilePath() throws IOException {
        String[] actions = new String[]{
                IntentConstants.ACTION_PICK_FILE,
                IntentConstants.ACTION_PICK_DIRECTORY,
                Intent.ACTION_GET_CONTENT
        };

        for(int i = 0; i < 3; i++){
            TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
            if (i == 1){ //Pick directory
                TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-pick");
            } else {
                TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-to-pick.txt", "bbb");
            }
            // Directory because PICK_DIRECTORY doesn't show files
            TestUtils.createDirectory(sdcardPath + "oi-to-pick-test-folder-deleted");


            // Pick a file first
            Uri uri = Uri.parse("file://" + sdcardPath); // If there was already a remembered pick file path
            getInstrumentation().startActivitySync(getFileManagerIntentFilterIntent()
                    .setAction(actions[i]).setData(uri));

            solo.clickOnText("oi-filemanager-tests");
            if (i == 1) //Pick directory
                solo.clickOnText("oi-dir-to-pick");
            else
                solo.clickOnText("oi-file-to-pick.txt");

            // Check, if we are in the oi-filemanager-tests directory
            getInstrumentation().startActivitySync(getFileManagerIntentFilterIntent()
                    .setAction(actions[i]).setData(uri));

            solo.goBack();


            // Delete the oi-filemanager-tests directory
            TestUtils.deleteDirectory(sdcardPath + "oi-filemanager-tests");

            // Check, if the current directory is the default (sdcardPath)
            getInstrumentation().startActivitySync(getFileManagerIntentFilterIntent()
                    .setAction(actions[i]).setData(uri));

            assertTrue(solo.searchText("oi-to-pick-test-folder-deleted"));

            // Clean up
            new File(sdcardPath + "oi-to-pick-test-folder-deleted").delete();

            solo.goBack();
            solo.goBack();
        }
    }

    private Intent getFileManagerIntentFilterIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.veniosg.dir", IntentFilterActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
