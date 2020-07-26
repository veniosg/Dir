package com.accessifiers.filebrowser.test.acceptance;

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.accessifiers.filebrowser.android.activity.IntentFilterActivity;
import com.accessifiers.filebrowser.test.actor.Android;
import com.accessifiers.filebrowser.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static android.support.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static android.support.test.espresso.contrib.ActivityResultMatchers.hasResultData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasHost;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasPath;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasScheme;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.android;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.user;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntentFilterActivityTest {
    private static final String SDCARD_MARKER_DIR_NAME = "sdcardmarkerdirectory";

    @Rule
    public final ActivityTestRule<IntentFilterActivity> activityRule =
            new ActivityTestRule<>(IntentFilterActivity.class, false, false);
    private final User user = user(activityRule);
    private final Android android = android(activityRule);

    private final File sdCardDir = Environment.getExternalStorageDirectory();
    private final File textFile = new File(sdCardDir, "text.txt");
    private final File imageFile = new File(sdCardDir, "image.png");
    private final File markerDirectory = new File(sdCardDir, SDCARD_MARKER_DIR_NAME);
    private final File markerDirectoryChild = new File(markerDirectory, "aFile");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        textFile.createNewFile();
        imageFile.createNewFile();
        markerDirectory.mkdir();
        markerDirectoryChild.createNewFile();

        Context targetContext = InstrumentationRegistry.getTargetContext();
        PreferenceManager.getDefaultSharedPreferences(targetContext).edit().clear().commit();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        textFile.delete();
        imageFile.delete();
        markerDirectoryChild.delete();
        markerDirectory.delete();
    }

    @Test
    public void remembersPickFilePath() {
        // Launch pick with a specific destination
        user.launches().pickFileIn(sdCardDir);

        // Move to a folder and pick a child
        user.selects().fileInList(markerDirectory);
        user.selects().fileInList(markerDirectoryChild);
        user.selects().pickFileButton();

        // Launch pick without specific destination
        user.launches().pickFileWithNoSchemeAndNoType();

        // Check that we are in the marker folder
        user.sees().fileInPath(markerDirectory);
        user.sees().fileInList(markerDirectoryChild);
    }

    @Test
    public void supportsPickFileWithFileSchemeAndAnyType() {
        android.launches().pickFileWithFileSchemeAndAnyType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void supportsPickFileWithFileSchemeAndNoType() {
        android.launches().pickFileWithFileSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void supportsPickFileWithNoSchemeAndNoType() {
        android.launches().pickFileWithNoSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void supportsPickDirectoryWithFileSchemeAndNoType() {
        android.launches().pickDirectoryWithFileSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
    }

    @Test
    public void supportsPickDirectoryWithNoSchemeAndNoType() {
        android.launches().pickDirectoryWithNoSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
    }

    @Test
    public void supportsGetContentWithNoSchemeAndAnyType() {
        android.launches().getContentWithNoSchemeAndAnyType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void supportsGetContentWithFileSchemeAndNoType() {
        android.launches().getContentWithFileSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void supportsGetContentWithNoSchemeAndNoType() {
        android.launches().getContentWithNoSchemeAndNoType();

        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(textFile);
    }

    @Test
    public void respectsOpenableContract() {
        android.launches().openableGetContentWithNoSchemeAndNoType();

        user.selects().fileInList(textFile);
        user.selects().pickFileButton();

        assertThat(activityRule.getActivityResult(), hasResultCode(RESULT_OK));
        assertThat(activityRule.getActivityResult(), hasResultData(hasData(allOf(
                hasScheme("content"),
                // A content provider that respects the contract is registered on this host
                hasHost("com.accessifiers.filebrowser.filemanager"),
                hasPath(textFile.getAbsolutePath())
        ))));
    }

    @Test
    public void usesTypeFilterFromData() {
        android.launches().pickFileWithFileSchemeAndType("text/plain");

        user.sees().fileInList(textFile);
        user.cannotSee().fileInList(imageFile);
    }

    @Test
    public void showsTitleSpecifiedInExtra() {
        String activityTitle = "Activity title";

        android.launches().pickFileWithTitleExtra(activityTitle);

        user.sees().activityTitle(activityTitle);
    }

    @Test
    public void showsDefaultTitleIfNotSpecifiedExtra() {
        String activityTitle = "File picker";

        android.launches().pickFileWithFileSchemeAndAnyType();

        user.sees().activityTitle(activityTitle);
    }

    @Test
    public void showsOnlyDirectoriesIfSpecifiedInExtra() {
        android.launches().pickFileWithDirOnlyExtra();

        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
        user.cannotSee().fileInList(textFile);
    }

    @Test
    public void showsOnlyDirectoriesIfActionIsPickDirectory() {
        android.launches().pickDirectoryWithNoSchemeAndNoType();

        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
        user.cannotSee().fileInList(textFile);
    }
}
