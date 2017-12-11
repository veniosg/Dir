package com.veniosg.dir.test.matcher;

import android.support.test.InstrumentationRegistry;
import com.veniosg.dir.mvvm.model.FileHolder;
import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.core.IsEqual.equalTo;

public class FileHolderHasSizeInfo<T> extends FeatureMatcher<T, String> {
  private FileHolderHasSizeInfo(Matcher<? super String> hasFileHolderMatcher) {
    super(hasFileHolderMatcher, "with getSizeInfo()", "getSizeInfo()");
  }

  @Override
  protected String featureValueOf(T actual) {
    if (actual instanceof FileHolder) {
      return ((FileHolder) actual).getSizeInfo(InstrumentationRegistry.getContext(), false);
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
   * @param expectedSizeInfo the expected toString result
   */
  @Factory
  public static <T> Matcher<T> hasSizeInfo(String expectedSizeInfo) {
    return new FileHolderHasSizeInfo<>(equalTo(expectedSizeInfo));
  }
}
