package com.veniosg.dir.test.acceptance;

import android.os.Environment;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.FileManagerActivity;
import com.veniosg.dir.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.veniosg.dir.test.TestUtils.cleanDirectory;
import static com.veniosg.dir.test.injector.ActorInjector.user;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FileManagerActivityTest {
    private static final String DIR_NAME = "somedirectory";
    private static final String FILE_IN_DIR_NAME = "afileinsomedirectory";

    @Rule
    public ActivityTestRule<FileManagerActivity> activityRule = new ActivityTestRule<>(
            FileManagerActivity.class, false, false);

    private final User user = user(activityRule);
    private final File sdCardDir = Environment.getExternalStorageDirectory();
    private final File testDirectory = new File(sdCardDir, "testDir");
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
        cleanDirectory(testDirectory);
        testDirectory.delete();
    }

    @Test
    public void showsFolder() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.sees().pathFragmentInPathView(DIR_NAME);
        user.sees().fileInList(FILE_IN_DIR_NAME);
    }

    @Test
    public void selectingADirectoryNavigates() throws Exception {
        user.launches().dir();

        user.selects().fileInList(testDirectory.getName());
        user.sees().pathFragmentInPathView(testDirectory.getName());
        user.sees().fileInList(testChildFile.getName());

        user.selects().backButton();
        user.sees().pathFragmentInPathView(testDirectory.getParent());
        user.sees().fileInList(testDirectory.getName());
    }

    @Test
    public void copiesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCopiesFileInList(testChildFile.getName());
        user.selects().fileInList(testCopyDestination.getName());
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile.getName());
    }

    @Test
    public void copiesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCopiesFileInList(testChildDirectory.getName());
        user.selects().fileInList(testCopyDestination.getName());
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile.getName());
    }

    @Test
    public void movesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCutsFileInList(testChildFile.getName());
        user.selects().fileInList(testCopyDestination.getName());
        user.selects().pasteAction();

        user.sees().fileInList(testChildFile.getName());
        user.selects().backButton();
        user.cannotSee().fileInList(testChildFile.getName());
    }

    @Test
    public void movesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        userCutsFileInList(testChildDirectory.getName());
        user.selects().fileInList(testCopyDestination.getName());
        user.selects().pasteAction();

        user.sees().fileInList(testChildDirectory.getName());
        user.selects().backButton();
        user.cannotSee().fileInList(testChildDirectory.getName());
    }

    @Test
    public void movesNonEmptyDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        // Move file in directory-to-move
        userCutsFileInList(testChildFile.getName());
        user.selects().fileInList(testChildDirectory.getName());
        user.selects().pasteAction();
        user.selects().backButton();

        // Move directory
        userCutsFileInList(testChildDirectory.getName());
        user.selects().fileInList(testCopyDestination.getName());
        user.selects().pasteAction();

        // Assert directory and contents in new place
        user.sees().fileInList(testChildDirectory.getName());
        user.selects().fileInList(testChildDirectory.getName());
        user.sees().fileInList(testChildFile.getName());
    }

    @Test
    public void deletesFile() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildFile.getName());
        user.selects().operationsAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildFile.getName());
    }

    @Test
    public void deletesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory.getName());
        user.selects().operationsAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildDirectory.getName());
    }

    @Test
    public void renamesFile() throws Exception {
        String newName = "newName";
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildFile.getName());
        user.selects().operationsAction();
        user.selects().renameAction();
        user.types().inputFileName(newName);
        user.selects().ok();

        user.sees().fileInList(newName);
        user.cannotSee().fileInList(testChildFile.getName());
    }

    @Test
    public void renamesDirectory() throws Exception {
        String newName = "newName";
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory.getName());
        user.selects().operationsAction();
        user.selects().renameAction();
        user.types().inputFileName(newName);
        user.selects().ok();

        user.sees().fileInList(newName);
        user.cannotSee().fileInList(testChildDirectory.getName());
    }

    @Test
    public void compressesAndExtracts() throws Exception {
        String zipName = "lala";
        user.launches().viewWithFileScheme(testDirectory.getParentFile());

        // compress
        user.selects().longFileInList(testDirectory.getName());
        user.selects().operationsAction();
        user.selects().compressAction();
        user.types().inputFileName(zipName);
        user.selects().ok();

        // delete original
        user.selects().longFileInList(testDirectory.getName());
        user.selects().operationsAction();
        user.selects().deleteAction();

        // uncompress
        user.selects().longFileInList(zipName);
        user.selects().operationsAction();
        user.selects().extractAction();

        // assert uncompressed contents
        user.selects().fileInList(testDirectory.getName());
        user.sees().fileInList(testChildDirectory.getName());
        user.sees().fileInList(testChildFile.getName());
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

    // TODO test other features (pt2)

    private void userCopiesFileInList(String name) {
        user.selects().longFileInList(name);
        user.selects().operationsAction();
        user.selects().copyAction();
    }

    private void userCutsFileInList(String name) {
        user.selects().longFileInList(name);
        user.selects().operationsAction();
        user.selects().moveAction();
    }
}
