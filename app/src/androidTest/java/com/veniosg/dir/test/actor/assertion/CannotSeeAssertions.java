package com.veniosg.dir.test.actor.assertion;

import android.support.test.espresso.assertion.ViewAssertions;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.veniosg.dir.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.io.File;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class CannotSeeAssertions {
    public void fileInList(File file) {
        String filename = file.getName();
        onView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).check(matches(not(withAdaptedData(hasName(filename)))));
    }

    public void visibleSearchResult(File result) {
        onView(allOf(
                withId(R.id.primary_info),
                withText(result.getName())
        )).check(ViewAssertions.doesNotExist());
    }

    public void searchEmptyView() {
        onView(withId(R.id.empty_text))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.empty_img))
                .check(matches(not(isDisplayed())));
    }

    private static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }

                @SuppressWarnings("rawtypes")
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }

                return false;
            }
        };
    }
}
