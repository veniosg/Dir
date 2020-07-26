package com.accessifiers.filebrowser.test.actor.action;

import com.accessifiers.filebrowser.R;
import com.accessifiers.filebrowser.mvvm.model.FileHolder;

import java.io.File;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.accessifiers.filebrowser.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SelectsActions {
    public void fileInList(File file) {
        fileInList(file.getName());
    }

    public void fileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename)
        )).inAdapterView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).perform(click());
    }

    public void longFileInList(File file) {
        longFileInList(file.getName());
    }

    public void longFileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename)
        )).inAdapterView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).perform(longClick());
    }

    public void searchResult(File result) {
        onView(allOf(
                withId(R.id.primary_info),
                withText(result.getName())
        )).perform(click());
    }

    public void pickFileButton() {
        onView(allOf(
                withText("Pick file"),
                isDisplayed()
        )).perform(click());
    }

    public void ok() {
        onView(allOf(
                anyOf(
                        withText("OK"),
                        withText("Ok"),
                        withText("ok")),
                isDisplayed()
        )).perform(click());
    }

    public void yes() {
        onView(allOf(
                anyOf(
                        withText("YES"),
                        withText("Yes"),
                        withText("yes")),
                isDisplayed()
        )).perform(click());
    }

    public void backButton() {
        onView(withId(android.R.id.content)).perform(pressBack());
    }

    public void overflowAction() {
        clickOnAction(R.id.menu_more_options, R.string.more_options);
    }

    public void copyAction() {
        clickOnAction(R.id.menu_copy, R.string.menu_copy);
    }

    public void moveAction() {
        clickOnAction(R.id.menu_move, R.string.menu_move);
    }

    public void pasteAction() {
        clickOnAction(R.id.menu_paste, R.string.menu_paste);
    }

    public void createDirectoryAction() {
        clickOnAction(R.id.menu_create_folder, R.string.menu_create_folder);
    }

    public void compressAction() {
        clickOnAction(R.id.menu_compress, R.string.menu_compress);
    }

    public void extractAction() {
        clickOnAction(R.id.menu_extract, R.string.menu_extract);
    }

    public void deleteAction() {
        clickOnAction(R.id.menu_delete, R.string.menu_delete);
    }

    public void renameAction() {
        clickOnAction(R.id.menu_rename, R.string.menu_rename);
    }

    public void searchAction() {
        clickOnAction(R.id.menu_search, R.string.menu_search);
    }

    private void clickOnAction(int id, int label) {
        onView(allOf(
                anyOf(
                        withText(label),
                        withId(id)
                ),
                isDisplayed()
        )).perform(click());
    }
}
