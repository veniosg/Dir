package com.accessifiers.filebrowser.test.matcher;

import com.accessifiers.filebrowser.mvvm.model.FileHolder;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.core.IsEqual.equalTo;

public class FileHolderHasName<T> extends FeatureMatcher<T, String> {
    public FileHolderHasName(Matcher<? super String> hasFileHolderMatcher) {
        super(hasFileHolderMatcher, "with getName()", "getName()");
    }

    @Override
    protected String featureValueOf(T actual) {
        if (actual instanceof FileHolder) {
            return ((FileHolder) actual).getName();
        } else {
            return null;
        }
    }

    /**
     * Creates a matcher that matches any examined object whose <code>toString</code> method
     * returns a value equalTo the specified string.
     * <p/>
     * For example:
     * <pre>assertThat(true, hasToString("TRUE"))</pre>
     *
     * @param expectedName the expected toString result
     */
    @Factory
    public static <T> Matcher<T> hasName(String expectedName) {
        return new FileHolderHasName<T>(equalTo(expectedName));
    }
}
