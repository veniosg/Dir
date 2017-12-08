package com.veniosg.dir.test.actor.assertion;

import android.content.Intent;

import com.veniosg.dir.android.util.FileUtils;

import java.io.File;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.AllOf.allOf;

public class LaunchedAssertions {
    public void viewFileIntent(File file) {
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(FileUtils.getUri(file)),
                hasType(any(String.class))
        ));
    }

    public void searchIntentFor(File file) {
        intended(allOf(
                hasAction(Intent.ACTION_SEARCH),
                hasData(FileUtils.getUri(file)),
                hasType(any(String.class))
        ));
    }
}
