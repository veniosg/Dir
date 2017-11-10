package com.veniosg.dir.test.acceptance;

import android.os.Environment;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.SaveAsActivity;
import com.veniosg.dir.test.actor.User;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static com.veniosg.dir.test.injector.ActorInjector.user;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SaveAsActivityTest {
    private static final String ORIGINAL_FILE_NAME = "orig.txt";
    private static final String DEST_FILE_NAME = "dest.txt";
    private static final String DEST_DIR_NAME = "saveasdir";

    @Rule
    public final IntentsTestRule<SaveAsActivity> mIntentTestRule =
            new IntentsTestRule<>(SaveAsActivity.class, false, false);
    private final User user = user(mIntentTestRule);

    private final File sdCardDir = Environment.getExternalStorageDirectory();
    private final File originalFile = new File(sdCardDir, ORIGINAL_FILE_NAME);
    private final File destDir = new File(sdCardDir, DEST_DIR_NAME);
    private final File destFile = new File(destDir, DEST_FILE_NAME);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        originalFile.createNewFile();
        destDir.mkdir();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @After
    public void tearDown() throws Exception {
        originalFile.delete();
        destFile.delete();
        destDir.delete();
    }

    @Test
    public void savesToPickedDestination() {
        user.launches().saveAs(originalFile);

        user.selects().fileInList(DEST_DIR_NAME);
        user.types().pickFileName(DEST_FILE_NAME);
        user.selects().pickFileButton();

        assertTrue(destFile.exists());
        // Ideally this would also check content equality
    }
}
