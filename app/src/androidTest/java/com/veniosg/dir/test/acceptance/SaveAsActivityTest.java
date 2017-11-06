package com.veniosg.dir.test.acceptance;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.SaveAsActivity;
import com.veniosg.dir.test.actor.Android;
import com.veniosg.dir.test.actor.User;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.veniosg.dir.test.injector.ActorInjector.android;
import static com.veniosg.dir.test.injector.ActorInjector.user;
import static junit.framework.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SaveAsActivityTest {
    @Rule
    public final IntentsTestRule<SaveAsActivity> mIntentTestRule =
            new IntentsTestRule<>(SaveAsActivity.class, false, false);
    private final User user = user();
    private final Android android = android(mIntentTestRule);

    @Test
    public void forwardsToPickActivity() {
        android.launches().openableViewWithContentSchemeAndAnyType();

        android.launched().dirPickFileForResult();
    }

    @Test
    public void handlesResult() {
        assertFalse(true);
    }
}
