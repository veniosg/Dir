package com.veniosg.dir.test.actor.action;

import android.support.test.espresso.action.ViewActions;

import com.veniosg.dir.android.view.widget.PathItemView;
import com.veniosg.dir.mvvm.model.FileHolder;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SelectsActions {
    public void fileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename)
        )).perform(click());
    }

    public void pickFileButton() {
        onView(allOf(
                withText("Pick file"),
                isDisplayed()
        )).perform(click());
    }
}
