package com.veniosg.dir.test.actor.assertion;

import android.support.test.espresso.matcher.ViewMatchers;

import com.veniosg.dir.R;
import com.veniosg.dir.android.view.widget.PathItemView;
import com.veniosg.dir.mvvm.model.FileHolder;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class CannotSeeAssertions {
    public void fileInList(String filename) {
        onData(allOf(
                is(instanceOf(FileHolder.class)),
                hasName(filename))
        ).check(matches(not(isDisplayed())));
    }
}
