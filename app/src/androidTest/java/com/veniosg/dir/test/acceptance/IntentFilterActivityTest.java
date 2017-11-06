package com.veniosg.dir.test.acceptance;

import android.os.Environment;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.IntentFilterActivity;
import com.veniosg.dir.test.actor.Android;
import com.veniosg.dir.test.actor.User;

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
import static com.veniosg.dir.test.injector.ActorInjector.android;
import static com.veniosg.dir.test.injector.ActorInjector.user;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class IntentFilterActivityTest {
    private static final String SDCARD_MARKER_FILE_NAME = "sdcardmarkerfile";
    private static final String SDCARD_MARKER_DIR_NAME = "sdcardmarkerdirectory";

    @Rule
    public final ActivityTestRule<IntentFilterActivity> activityRule =
            new ActivityTestRule<>(IntentFilterActivity.class, false, false);
    private final User user = user();
    private final Android android = android(activityRule);

    private final File sdCardDir = Environment.getExternalStorageDirectory();
    private final File markerFile = new File(sdCardDir, SDCARD_MARKER_FILE_NAME);
    private final File textFile = new File(sdCardDir, "text.txt");
    private final File imageFile = new File(sdCardDir, "image.png");
    private final File markerDirectory = new File(sdCardDir, SDCARD_MARKER_DIR_NAME);


    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        markerFile.createNewFile();
        textFile.createNewFile();
        imageFile.createNewFile();
        markerDirectory.mkdir();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        markerFile.delete();
        textFile.delete();
        imageFile.delete();
        markerDirectory.delete();
    }

    @Test
    public void remembersPickFilePath() {
        assertTrue(false);
    }

    @Test
    public void supportsPickFileWithFileSchemeAndAnyType() {
        android.launches().pickFileWithFileSchemeAndAnyType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void supportsPickFileWithFileSchemeAndNoType() {
        android.launches().pickFileWithFileSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void supportsPickFileWithNoSchemeAndNoType() {
        android.launches().pickFileWithNoSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void supportsPickDirectoryWithFileSchemeAndNoType() {
        android.launches().pickDirectoryWithFileSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
    }

    @Test
    public void supportsPickDirectoryWithNoSchemeAndNoType() {
        android.launches().pickDirectoryWithNoSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
    }

    @Test
    public void supportsGetContentWithNoSchemeAndAnyType() {
        android.launches().getContentWithNoSchemeAndAnyType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void supportsGetContentWithFileSchemeAndNoType() {
        android.launches().getContentWithFileSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void supportsGetContentWithNoSchemeAndNoType() {
        android.launches().getContentWithNoSchemeAndNoType();

        user.sees().pathFragmentInPathView(sdCardDir.getName());
        user.sees().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void respectsOpenableContract() {
        android.launches().openableGetContentWithNoSchemeAndNoType();

        user.selects().fileInList(SDCARD_MARKER_FILE_NAME);
        user.selects().pickFileButton();

        assertThat(activityRule.getActivityResult(), hasResultCode(RESULT_OK));
        assertThat(activityRule.getActivityResult(), hasResultData(hasData(allOf(
                hasScheme("content"),
                hasHost("com.veniosg.dir.filemanager"),
                hasPath(markerFile.getAbsolutePath())
        ))));
    }

    @Test
    public void usesTypeFilterFromExtra() {
        android.launches().pickFileWithNoSchemeAndNoTypeAndExtraFilter("text/*");

        user.sees().fileInList(textFile.getName());
        user.cannotSee().fileInList(imageFile.getName());
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

        // FIXME cannotsee doesn't work
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
        user.cannotSee().fileInList(SDCARD_MARKER_FILE_NAME);
    }

    @Test
    public void showsOnlyDirectoriesIfActionIsPickDirectory() {
        android.launches().pickDirectoryWithNoSchemeAndNoType();

        // FIXME cannotsee doesn't work
        user.sees().fileInList(SDCARD_MARKER_DIR_NAME);
        user.cannotSee().fileInList(SDCARD_MARKER_FILE_NAME);
    }
}
