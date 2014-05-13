package com.veniosg.dir.test;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewParent;

import com.robotium.solo.Solo;
import com.veniosg.dir.R;
import com.veniosg.dir.activity.FileManagerActivity;
import com.veniosg.dir.util.FileUtils;

import junit.framework.Assert;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Random;

public class FileManagerActivityTest extends ActivityInstrumentationTestCase2<FileManagerActivity>{
    private Solo solo;
    private String sdcardPath;

    public FileManagerActivityTest() {
        super(FileManagerActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath()+'/';

        // need to do this before creating activity
        TestUtils.cleanDirectory(new File(sdcardPath, "oi-filemanager-tests"));
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");

        setActivityIntent(getFileManagerIntent());

        solo = new Solo(getInstrumentation(), getActivity());
    }

    protected void tearDown() throws Exception {
        try {
            this.solo.finishOpenedActivities();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        super.tearDown();
        TestUtils.cleanDirectory(new File(sdcardPath + "oi-filemanager-tests"));
    }

    public void testActions() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/oi-move-target");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-1.txt", "");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-2.txt", "");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-3.txt", "");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-4.txt", "");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-file-5.txt", "");

        clickOnList("oi-filemanager-tests");

        // copy
        solo.clickLongOnText("oi-file-1.txt");
        clickOnCAB(R.id.menu_file_ops);
        solo.clickOnText(solo.getString(R.string.menu_copy));

        navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-1.txt", null);
        assertTrue(solo.searchText("oi-file-1.txt"));

        // move
        solo.clickLongOnText("oi-file-2.txt");
        clickOnCAB(R.id.menu_file_ops);
        solo.clickOnText(solo.getString(R.string.menu_move));
        navigateToTargetAndPasteAndCheck("oi-move-target", "oi-file-2.txt", null);
        assertFalse(solo.searchText("oi-file-2.txt"));

        // rename
        solo.clickLongOnText("oi-file-5.txt");
        clickOnCAB(R.id.menu_file_ops);
        solo.clickOnText(solo.getString(R.string.menu_rename));
        solo.enterText(0, "oi-renamed-file.txt");
        solo.clickOnText(solo.getString(android.R.string.ok)); // not sure what to do
        assertTrue(solo.searchText("oi-renamed-file.txt"));

        solo.goBack();
        solo.goBack();
    }

    /*public void testZipCompression() throws IOException {
        // create empty directory
        TestUtils.createDirectory(sdcardPath + "emptyFolder");
        // create empty subdirectory
        TestUtils.createDirectory(sdcardPath + "emptyFolder/emptySubFolder");
        solo.clickLongOnText("emptyFolder");
        clickOnCAB(R.id.menu_file_ops);
        solo.clickOnText(solo.getString(R.string.menu_compress));
        solo.enterText(0, "testzip");
        solo.clickOnText(solo.getString(android.R.string.ok));
        Utils.searchIn(new File(sdcardPath), null, );
        assertTrue()
    }*/

    public void testBookmarks() throws IOException {
        String fn = "oi-bookmark-" + new Random().nextInt(1000);
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/" + fn);
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/" + fn + "/oi-inside-book.txt", "");

        // create bookmark
        clickOnList("oi-filemanager-tests");
        solo.clickLongOnText(fn);
        clickOnCAB(R.id.menu_bookmark, solo.getString(R.string.menu_bookmark));

        // navigate to it
        solo.clickOnText(fn);
        assertTrue(solo.searchText("oi-inside-book.txt"));

        // remove it
        solo.goBack();
        solo.goBack(); // navigate away from the list. needed for the last assertion
        solo.scrollToSide(Solo.LEFT);
        solo.clickLongOnText(fn);
        clickOnCAB(R.id.menu_delete);

        // make sure that it is deleted
        assertFalse(solo.searchText(fn));
        solo.goBack();
    }

    public void testNavigation() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-test.txt", "");
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/oi-test-dir");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-test-dir/oi-fff.txt", "");

        solo.clickOnText("oi-filemanager-tests");
        assertTrue(solo.searchText("oi-test.txt"));
        solo.clickOnText("oi-test-dir");
        assertTrue(solo.searchText("oi-fff.txt"));

        solo.goBack();
        solo.goBack();
        solo.clickOnText("oi-filemanager-tests");
        assertTrue(solo.searchText("oi-test.txt"));

        solo.clickOnText("oi-test-dir");
        solo.goBack();
        assertTrue(solo.searchText("oi-test.txt"));

        solo.goBack();
    }

    public void testModification() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "/oi-filemanager-tests/oi-rem-test.txt", "");
        solo.clickOnText("oi-filemanager-tests");
        solo.clickLongOnText("oi-rem-test.txt");

        clickOnCAB(R.id.menu_file_ops);
        solo.clickOnText(solo.getString(R.string.menu_delete));
        solo.clickOnText(solo.getString(R.string.yes));

        solo.clickOnActionBarItem(R.id.menu_create_folder);
        solo.enterText(0, "oi-created-folder");
        solo.clickOnText(solo.getString(android.R.string.ok));

        assertTrue(solo.searchText("oi-created-folder"));
        solo.goBack();

        File createdFolder = new File(sdcardPath + "oi-filemanager-tests/oi-created-folder");
        assertTrue(createdFolder.exists());
        assertTrue(createdFolder.isDirectory());
        assertFalse(new File(sdcardPath + "oi-filemanager-tests/oi-rem-test.txt").exists());
    }

    public void testDetails() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-detail.txt", "abcdefg");

        solo.clickOnText("oi-filemanager-tests");

        solo.clickLongOnText("oi-detail.txt");
        clickOnCAB(R.id.menu_details, solo.getString(R.string.menu_details));
        assertTrue(solo.searchText(solo.getString(R.string.details_type_file)));
        assertTrue(solo.searchText(FileUtils.formatSize(getActivity(), 7)));

        solo.goBack();
        solo.goBack();
        solo.goBack();
    }

    public void testHiddenFiles() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/.oi-hidden.txt", "");
        solo.clickOnText("oi-filemanager-tests");

        boolean origState = solo.searchText(".oi-hidden.txt");

        solo.clickOnMenuItem(solo.getString(R.string.settings));

        solo.clickOnText(solo.getString(R.string.preference_displayhiddenfiles_title));
        solo.goBack();
        assertTrue(origState != solo.searchText(".oi-hidden.txt"));

        solo.goBack();
        solo.goBack();
    }

    public void testOrder() throws IOException, InterruptedException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-b.txt", "bbb");
        Thread.sleep(10); // make sure that next file is younger
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-a.txt", "aaaaaa");
        Thread.sleep(10);
        TestUtils.createFile(sdcardPath + "oi-filemanager-tests/oi-c.txt", "");
        solo.clickOnText("oi-filemanager-tests");

        String[] sortOrders = getActivity().getResources()
                .getStringArray(R.array.preference_sortby_names);

        setAscending(true);
        setSortOrder(sortOrders[0]);
        assertItemsInOrder("oi-a.txt", "oi-b.txt", "oi-c.txt");

        setSortOrder(sortOrders[1]);
        assertItemsInOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");

        setSortOrder(sortOrders[2]);
        assertItemsInOrder("oi-b.txt", "oi-a.txt", "oi-c.txt");

        setAscending(false);
        setSortOrder(sortOrders[0]);
        assertItemsInOrder("oi-c.txt", "oi-b.txt", "oi-a.txt");
    }

    public void testBrowseToOnPressEnter() throws IOException {
        String dirPath = "oi-filemanager-tests";
        String filename = "oi-test-is-in-right-directory";
        TestUtils.createDirectory(sdcardPath + dirPath);
        TestUtils.createFile(sdcardPath + dirPath + "/" + filename, "");

		/*
		 *  We start at the SD card.
		 */
        solo.clickLongOnText(Environment.getExternalStorageDirectory().getParentFile().getName());

        solo.clickOnEditText(0); // Let the editText have focus to be able to send the enter key.
        solo.enterText(0, "/"+dirPath);
        solo.sendKey(Solo.ENTER);

        assertTrue(solo.searchText(filename));

        solo.goBack();
        solo.goBack();
    }

    public void testIntentUrl() throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests");
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open");
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/oi-dir-to-open/oi-intent");

        Uri uri = Uri.parse("file://" + sdcardPath + "oi-filemanager-tests/oi-dir-to-open");
        getInstrumentation().startActivitySync(getFileManagerIntent()
                .setAction(Intent.ACTION_VIEW).setData(uri));

        assertTrue(solo.searchText("oi-intent"));
        solo.goBack();
        solo.goBack();
    }

    // Utils

    private void clickOnList(String s) {
        solo.clickOnText(s);
    }

    private void clickOnCAB(int resId) {
        clickOnCAB(resId, null);
    }

    private void clickOnCAB(int resId, String overflowItemText) {
        View btn = getActivity().findViewById(resId);
        if (btn!= null)
            solo.clickOnView(btn);
        else {
            clickMore();
            solo.clickOnText(overflowItemText);
        }
    }

    private void clickMore() {
        try {
            View view = solo.getView(R.id.menu_file_ops);
            ViewParent parentOfView = view.getParent();

            if (parentOfView.getClass().getName().contains("ActionMenuView")) {
                parentOfView = parentOfView.getParent();

                Class absAbv = parentOfView.getClass().getSuperclass();
                Field actionMenuPresenterField = absAbv.getDeclaredField("mActionMenuPresenter");
                actionMenuPresenterField.setAccessible(true);
                Object actionMenuPresenter = actionMenuPresenterField.get(parentOfView);
                Field actionMenuViewField = actionMenuPresenter.getClass().getSuperclass().getDeclaredField("mMenuView");
                actionMenuViewField.setAccessible(true);
                Object actionMenuView = actionMenuViewField.get(actionMenuPresenter);
                Field childrenField = actionMenuView.getClass().getSuperclass().getSuperclass().getDeclaredField("mChildren");
                childrenField.setAccessible(true);
                Object[] menuItemsAsViews = (Object[]) childrenField.get(actionMenuView);
                View moreView = null;
                for (int i = 0; i < menuItemsAsViews.length - 1; i++) {

                    View tempView = (View) menuItemsAsViews[i];
                    if (tempView != null) {
                        moreView = tempView;
                    } else {
                        break;
                    }
                }
                if (moreView != null)
                    solo.clickOnView(moreView);
                else
                    Assert.assertFalse("View is not found", false);
            }
        } catch (Exception e) {
            fail("NO OVERFLOW BUTTON");
        }
    }

    private void navigateToTargetAndPasteAndCheck(String dirname, String name1, String name2) throws IOException {
        TestUtils.createDirectory(sdcardPath + "oi-filemanager-tests/");
        solo.clickOnText(dirname);

        solo.clickOnActionBarItem(R.id.menu_paste);

        assertTrue(solo.searchText(name1));
        if(name2 != null)
            assertTrue(solo.searchText(name2));
        solo.goBack();
    }

    private void setSortOrder(String name) {
        solo.clickOnMenuItem(solo.getString(R.string.settings));
        solo.clickOnText(solo.getString(R.string.preference_sortby));
        solo.clickOnText(name);
        solo.goBack();
    }

    private void setAscending(boolean enabled) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("ascending", enabled);
        editor.commit();
    }

    private void assertItemsInOrder(String a, String b, String c) {
        int aPos = solo.getText(a).getTop();
        int bPos = solo.getText(b).getTop();
        int cPos = solo.getText(c).getTop();
        if(aPos > bPos)
            fail("aPos > bPos");
        if(bPos > cPos)
            fail("bpos > cPos");
    }

    public Intent getFileManagerIntent() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.MAIN");
        intent.setClassName("com.veniosg.dir", FileManagerActivity.class.getName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
