package com.accessifiers.filebrowser.test.acceptance;

import android.content.Context;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.accessifiers.filebrowser.android.activity.FileManagerActivity;
import com.accessifiers.filebrowser.test.actor.Android;
import com.accessifiers.filebrowser.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.android;
import static com.accessifiers.filebrowser.test.injector.ActorInjector.user;

@RunWith(AndroidJUnit4.class)
@LargeTest
public abstract class FileOperationsTest {
    @Rule
    public IntentsTestRule<FileManagerActivity> intentsRule = new IntentsTestRule<>(
            FileManagerActivity.class, false, false);

    private final User user = user(intentsRule);
    private final Android android = android(intentsRule);
    private File testDirectory;
    private File compressedFile;
    private File testExtractedDirectory;
    private File testChildDirectory;
    private File testCopyDestination;
    private File testChildFile;

    @Before
    public void setUp() throws Exception {
        Context context = getTargetContext();

        File storageDir = getStorageRoot(context);
        testDirectory = new File(storageDir, "testDir");
        compressedFile = new File(storageDir, "lala.zip");
        testExtractedDirectory = new File(storageDir, "lala");
        testChildDirectory = new File(testDirectory, "testChildDir");
        testCopyDestination = new File(testDirectory, "testCopyDestination");
        testChildFile = new File(testDirectory, "testChildFile");

        setUpFiles(context, testDirectory, testChildDirectory, testCopyDestination, testChildFile);
    }

    @After
    public void tearDown() throws Exception {
        tearDownFiles(getTargetContext(), testExtractedDirectory, testDirectory, compressedFile);
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
        user.selects().overflowAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildFile);
    }

    @Test
    public void deletesDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory);
        user.selects().overflowAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildDirectory);
    }

    @Test
    public void deletesFileAndDirectory() throws Exception {
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildDirectory);
        user.selects().fileInList(testChildFile);
        user.selects().overflowAction();
        user.selects().deleteAction();
        user.selects().yes();

        user.cannotSee().fileInList(testChildDirectory);
        user.cannotSee().fileInList(testChildFile);
    }

    @Test
    public void renamesFile() throws Exception {
        String nameSuffix = "2";
        String updatedName = testChildFile.getName() + nameSuffix;
        user.launches().viewWithFileScheme(testDirectory);

        user.selects().longFileInList(testChildFile);
        user.selects().overflowAction();
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
        user.selects().overflowAction();
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
        user.selects().overflowAction();
        user.selects().compressAction();
        user.types().inputFileName(zipName);
        user.selects().ok();

        // extract
        user.selects().longFileInList(zipName + ".zip");
        user.selects().overflowAction();
        user.selects().extractAction();

        // verify extracted structure
        user.selects().fileInList(zipName);
        user.selects().fileInList(testDirectory);
        user.sees().fileInList(testChildDirectory);
        user.sees().fileInList(testChildFile);
    }

    @Test
    public void compressesAndOverwrites() throws Exception {
        String zipName = "lala";
        user.launches().viewWithFileScheme(testDirectory);

        // compress
        user.selects().longFileInList(testChildDirectory);
        user.selects().overflowAction();
        user.selects().compressAction();
        user.types().inputFileName(zipName);
        user.selects().ok();

        // compress again
        user.selects().longFileInList(testChildFile);
        user.selects().overflowAction();
        user.selects().compressAction();
        user.types().inputFileName(zipName);
        user.selects().ok();

        // accept overwrite dialog
        user.selects().yes();

        // extract
        user.selects().longFileInList(zipName + ".zip");
        user.selects().overflowAction();
        user.selects().extractAction();

        // verify extracted structure matches second operation
        user.selects().fileInList(zipName);
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

    private void userCopiesFileInList(File file) {
        user.selects().longFileInList(file);
        user.selects().overflowAction();
        user.selects().copyAction();
    }

    private void userCutsFileInList(File file) {
        user.selects().longFileInList(file);
        user.selects().overflowAction();
        user.selects().moveAction();
    }

    protected abstract File getStorageRoot(Context context);

    protected abstract void setUpFiles(Context context,
                                       File testDirectory, File testChildDirectory,
                                       File testCopyDestination, File testChildFile) throws IOException;

    protected abstract void tearDownFiles(Context context, File testExtractedDirectory, File testDirectory, File compressedFile);
}
