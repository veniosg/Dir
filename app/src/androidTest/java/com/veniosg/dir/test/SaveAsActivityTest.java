package com.veniosg.dir.test;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.InstrumentationTestCase;

import com.robotium.solo.Solo;
import com.veniosg.dir.activity.SaveAsActivity;
import com.veniosg.dir.util.Logger;

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

    public void testIntentSaveAs() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-to-open.txt", "bbb");

        Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-to-open.txt");
        mActivity = getInstrumentation().startActivitySync(getFileManagerSaveAsIntent()
                .setAction(Intent.ACTION_VIEW).setData(uri));
        solo = new Solo(getInstrumentation(), mActivity);

        solo.clickLongOnText(Environment.getExternalStorageDirectory().getParentFile().getName());
        solo.enterText(0, "oi-target.txt");
        solo.sendKey(Solo.ENTER);
        assertTrue(new File(sdcardPath, "oi-filemanager-tests/oi-to-open.txtoi-target.txt").exists());
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
