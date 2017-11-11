package com.veniosg.dir.test.actor.assertion;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import com.veniosg.dir.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.veniosg.dir.test.matcher.FileHolderHasName.hasName;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class CannotSeeAssertions {
    public void fileInList(String filename) {
        onView(allOf(
                withId(android.R.id.list),
                isDescendantOfA(withId(R.id.zoomview))
        )).check(matches(not(withAdaptedData(hasName(filename)))));
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
