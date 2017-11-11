package com.veniosg.dir.test.actor.action;

import com.veniosg.dir.R;
import com.veniosg.dir.mvvm.model.FileHolder;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SelectsActions {
    public void fileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename)
        )).inAdapterView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).perform(click());
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

    public void pickFileButton() {
        onView(allOf(
                withText("Pick file"),
                isDisplayed()
        )).perform(click());
    }

    public void backButton() {
        onView(withId(android.R.id.content)).perform(pressBack());
    }

    public void operationsAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_file_ops),
                        withId(R.id.menu_file_ops)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void copyAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_copy),
                        withId(R.id.menu_copy)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void moveAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_move),
                        withId(R.id.menu_move)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void pasteAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_paste),
                        withId(R.id.menu_paste)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void createDirectoryAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_create_folder),
                        withId(R.id.menu_create_folder)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void compressAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_compress),
                        withId(R.id.menu_compress)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void extractAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_extract),
                        withId(R.id.menu_extract)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void deleteAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_delete),
                        withId(R.id.menu_delete)
                ),
                isDisplayed()
        )).perform(click());
    }

    public void renameAction() {
        onView(allOf(
                anyOf(
                        withText(R.string.menu_rename),
                        withId(R.id.menu_rename)
                ),
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
}
