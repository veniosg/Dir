package com.accessifiers.filebrowser.test.actor.assertion;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;

import com.accessifiers.filebrowser.android.activity.SearchActivity;
import com.accessifiers.filebrowser.android.util.FileUtils;
import com.accessifiers.filebrowser.mvvm.model.FileHolder;

import java.io.File;

import static android.content.Intent.ACTION_SEARCH;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasType;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.core.AllOf.allOf;

public class LaunchedAssertions {
    public void viewFileIntent(File file) {
        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(FileUtils.getUri(new FileHolder(file, null, null))),
                hasType(any(String.class))
        ));
    }

    public void searchIntentFor(File file) {
        intended(allOf(
                hasAction(ACTION_SEARCH),
                hasComponent(new ComponentName("com.accessifiers.filebrowser", SearchActivity.class.getName())),
                hasData(new Uri.Builder().path(file.getAbsolutePath()).build())
        ));
    }
}
