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
    private final File sdCardDir = Environment.getExternalStorageDirectory();
    private final File testDirectory = new File(sdCardDir, "testDir");
    private final File compressedFile = new File(sdCardDir, "lala.zip");
    private final File testExtractedDirectory = new File(sdCardDir, "lala");
    private final File testChildDirectory = new File(testDirectory, "testChildDir");
    private final File testCopyDestination = new File(testDirectory, "testCopyDestination");
    private final File testChildFile = new File(testDirectory, "testChildFile");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        testDirectory.mkdir();
        testChildDirectory.mkdir();
        testCopyDestination.mkdir();
        testChildFile.createNewFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        if (testExtractedDirectory.exists())
            testExtractedDirectory.renameTo(new File(testDirectory, "extracted"));
        if (compressedFile.exists())
            compressedFile.renameTo(new File(testDirectory, "compressed"));
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
        user.sees().fileInPath(sdCardDir);
        user.sees().fileInList(testDirectory);
    }

    @Test
    public void copiesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCopiesFileInList(testChildFile);
        user.selects().fileInList(testCopyDestination);
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile);
    }

    @Test
    public void copiesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCopiesFileInList(testChildFile);
        user.selects().fileInList(testCopyDestination);
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile);
    }

    @Test
    public void movesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCutsFileInList(testChildFile);
        user.selects().fileInList(testCopyDestination);
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile);
        user.selects().backButton();
        user.cannotSee().fileInList(testChildFile);
    }

    @Test
    public void movesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCutsFileInList(testChildDirectory);
        user.selects().fileInList(testCopyDestination);
        user.selects().pasteAction();

        user.sees().fileInList(testChildDirectory);
        user.selects().backButton();
        user.cannotSee().fileInList(testChildDirectory);
    }

    @Test
    public void movesNonEmptyDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        // Move file in directory-to-move
        userCutsFileInList(testChildFile);
        user.selects().fileInList(testChildDirectory);
        user.selects().pasteAction();
        user.selects().backButton();

        // Move directory
        userCutsFileInList(testChildDirectory);
        user.selects().fileInList(testCopyDestination);
        user.selects().pasteAction();

        // Assert directory and contents in new place
        user.sees().fileInList(testChildDirectory);
        user.selects().fileInList(testChildDirectory);
        user.sees().fileInList(testChildFile);
    }

    @Test
    public void deletesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildFile);
        user.selects().operationsAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildFile);
    }

    @Test
    public void deletesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory);
        user.selects().operationsAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildDirectory);
    }

    @Test
    public void renamesFile() throws Exception {
        String nameSuffix = "2";
        String updatedName = testChildFile.getName() + nameSuffix;
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildFile);
        user.selects().operationsAction();
        user.selects().renameAction();
        user.types().inputFileName(nameSuffix);
        user.selects().ok();

        user.sees().fileInList(updatedName);
        user.cannotSee().fileInList(testChildFile);
    }

    @Test
    public void renamesDirectory() throws Exception {
        String nameSuffix = "2";
        String updatedName = testChildDirectory.getName() + nameSuffix;
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory);
        user.selects().operationsAction();
        user.selects().renameAction();
        user.types().inputFileName(nameSuffix);
        user.selects().ok();

        user.sees().fileInList(updatedName);
        user.cannotSee().fileInList(testChildDirectory);
    }

    @Test
    public void compressesAndExtracts() throws Exception {
        String zipName = "lala";
        user.launches().viewWithFileScheme(testDirectory.getParentFile());

        // compress
        user.selects().longFileInList(testDirectory);
        user.selects().operationsAction();
        user.selects().compressAction();
        user.types().inputFileName(zipName);
        user.selects().ok();

        // uncompress
        user.selects().longFileInList(zipName + ".zip");
        user.selects().operationsAction();
        user.selects().extractAction();

        // assert uncompressed contents
        user.selects().fileInList(zipName);
        user.selects().fileInList(testDirectory);
        user.sees().fileInList(testChildDirectory);
        user.sees().fileInList(testChildFile);
    }

    @Test
    public void createsDirectory() throws Exception {
        String newDirName = "newDirectory";
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().createDirectoryAction();
        user.types().inputFileName(newDirName);
        user.selects().ok();

        user.sees().fileInList(newDirName);
    }

    @Test
    public void launchesSearchForCurrentDirectory() throws Exception {
        user.launches().viewWithFileScheme(sdCardDir);

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

    // TODO test other features (pt2)

    private void userCopiesFileInList(File file) {
        user.selects().longFileInList(file);
        user.selects().operationsAction();
        user.selects().copyAction();
    }

    private void userCutsFileInList(File file) {
        user.selects().longFileInList(file);
        user.selects().operationsAction();
        user.selects().moveAction();
    }
}
