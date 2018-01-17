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

package com.veniosg.dir.test.acceptance;

import android.os.Environment;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.FileManagerActivity;
import com.veniosg.dir.test.actor.Android;
import com.veniosg.dir.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.veniosg.dir.test.TestUtils.cleanDirectory;
import static com.veniosg.dir.test.injector.ActorInjector.android;
import static com.veniosg.dir.test.injector.ActorInjector.user;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FileManagerActivityTest {
    @Rule
    public IntentsTestRule<FileManagerActivity> intentsRule = new IntentsTestRule<>(
            FileManagerActivity.class, false, false);

    private final User user = user(intentsRule);
    private final Android android = android(intentsRule);
    private final File storageDir = Environment.getExternalStorageDirectory();
    private final File testDirectory = new File(storageDir, "testDir");
    private final File testChildFile = new File(testDirectory, "testChildFile");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        testDirectory.mkdir();
        testChildFile.createNewFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        cleanDirectory(testDirectory);
        testDirectory.delete();
    }

    @Test
    public void showsLaunchedDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.sees().fileInPath(testDirectory);
        user.sees().fileInList(testChildFile);
    }

    @Test
    public void navigatesFwdAndBwd() throws Exception {
        user.launches().dir();

        user.selects().fileInList(testDirectory);
        user.sees().fileInPath(testDirectory);
        user.sees().fileInList(testChildFile);

        user.selects().backButton();
        user.sees().fileInPath(storageDir);
        user.sees().fileInList(testDirectory);
    }

    @Test
    public void launchesSearchForCurrentDirectory() throws Exception {
        user.launches().viewWithFileScheme(storageDir);

        user.selects().fileInList(testDirectory);
        user.selects().searchAction();

        android.launched().searchIntentFor(testDirectory);
    }

    @Test
    @Ignore
    public void filtersBasedOnMimetypeExtra() {

    }

    @Test
    @Ignore
    public void filtersBasedOnFiletypeExtra() {

    }
}