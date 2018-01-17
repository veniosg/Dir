package com.veniosg.dir.test.actor.assertion;

import android.support.annotation.IdRes;

import com.veniosg.dir.R;
import com.veniosg.dir.android.ui.widget.PathItemView;
import com.veniosg.dir.mvvm.model.FileHolder;

import java.io.File;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

public class SeesAssertions {
    public void fileInList(File file) {
        fileInList(file.getName());
    }

    public void fileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename))
        ).inAdapterView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).check(matches(isDisplayed()));
    }

    public void fileInPath(File file) {
        String pathFragmentName = file.getName();
        onView(allOf(
                withParent(withClassName(equalTo(PathItemView.class.getName()))),
                withText(pathFragmentName))
        ).check(matches(isDisplayed()));
    }

    public void activityTitle(String title) {
        onView(allOf(
                isDescendantOfA(withId(R.id.toolbar)),
                withText(title)
        )).check(matches(isDisplayed()));
    }

    public void searchResult(File result) {
        onView(allOf(
                withId(R.id.primary_info),
                withText(result.getName())
        )).check(matches(isDisplayed()));
    }

    public void searchHintFor(File testDirectory) {
        String hint = "Search in " + testDirectory.getName();
        onView(allOf(
                withId(R.id.searchQuery),
                withHint(hint)
        )).check(matches(isDisplayed()));
    }

    public void searchEmptyView() {
        onView(allOf(
                withId(R.id.empty_text),
                withText("No matching files found!")
        )).check(matches(isDisplayed()));
        viewWithId(R.id.empty_img);
    }

    private void viewWithId(@IdRes int id) {
        onView(withId(id)).check(matches(isDisplayed()));
    }
}
