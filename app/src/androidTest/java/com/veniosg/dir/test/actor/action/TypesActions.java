package com.veniosg.dir.test.actor.action;

import android.support.test.espresso.action.ViewActions;
import android.widget.EditText;

import com.veniosg.dir.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

public class TypesActions {
    public void pickFileButton() {
        onView(allOf(
                withText("Pick file"),
                isDisplayed()
        )).perform(click());
    }

    public void fileName(String fileName) {
        onView(allOf(
                withParent(withId(R.id.pickBar)),
                withClassName(equalTo(EditText.class.getName())),
                isDisplayed()
        )).perform(ViewActions.typeText(fileName));
    }
}
