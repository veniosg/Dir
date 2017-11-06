package com.veniosg.dir.test.actor.action;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;

import com.veniosg.dir.IntentConstants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_OPENABLE;
import static com.veniosg.dir.IntentConstants.ACTION_PICK_FILE;
import static com.veniosg.dir.IntentConstants.EXTRA_TITLE;

public class LaunchesActions {
    private final ActivityTestRule<? extends Activity> mActivityRule;

    public LaunchesActions(ActivityTestRule<? extends Activity> activityRule) {
        mActivityRule = activityRule;
    }

    public void pickFileWithFileSchemeAndAnyType() {
        launch(buildIntent(
                ACTION_PICK_FILE,
                "file",
                "fu/bar"   // for */* filter
        ));
    }

    public void pickFileWithFileSchemeAndNoType() {
        launch(buildIntent(
                ACTION_PICK_FILE,
                "file",
                null
        ));
    }

    public void pickFileWithNoSchemeAndNoType() {
        launch(buildIntent(
                ACTION_PICK_FILE,
                null,
                null
        ));
    }

    public void pickDirectoryWithFileSchemeAndNoType() {
        launch(buildIntent(
                IntentConstants.ACTION_PICK_DIRECTORY,
                "file",
                null
        ));
    }

    public void pickDirectoryWithNoSchemeAndNoType() {
        launch(buildIntent(
                IntentConstants.ACTION_PICK_DIRECTORY,
                null,
                null
        ));
    }

    public void getContentWithNoSchemeAndAnyType() {
        launch(buildIntent(
                ACTION_GET_CONTENT,
                null,
                "fu/bar"    // for */* filter
        ));
    }

    public void getContentWithFileSchemeAndNoType() {
        launch(buildIntent(
                ACTION_GET_CONTENT,
                "file",
                null
        ));
    }

    public void getContentWithNoSchemeAndNoType() {
        launch(buildIntent(
                ACTION_GET_CONTENT,
                null,
                null
        ));
    }

    public void openableViewWithContentSchemeAndAnyType() {
        Intent openableIntent = buildIntent(ACTION_VIEW, "content", "fu/bar");
        openableIntent.addCategory(CATEGORY_OPENABLE);

        launch(openableIntent);
    }

    public void openableGetContentWithNoSchemeAndNoType() {
        Intent openableIntent = buildIntent(ACTION_GET_CONTENT, null, null);
        openableIntent.addCategory(CATEGORY_OPENABLE);

        launch(openableIntent);
    }

    public void pickFileWithTitleExtra(String activityTitle) {
        Intent intent = buildIntent(ACTION_PICK_FILE, null, null);
        intent.putExtra(EXTRA_TITLE, activityTitle);

        launch(intent);
    }

    public void pickFileWithDirOnlyExtra() {
        Intent intent = buildIntent(ACTION_PICK_FILE, null, null);
        intent.putExtra(IntentConstants.EXTRA_DIRECTORIES_ONLY, true);

        launch(intent);
    }

    private Intent buildIntent(@Nonnull String action, @Nullable String scheme, @Nullable String type) {
        Intent intent = new Intent(action);
        if (scheme != null) intent.setData(new Uri.Builder().scheme(scheme).build());
        if (type != null) intent.setType(type);
        return intent;
    }

    private void launch(Intent intent) {
        mActivityRule.launchActivity(intent);
    }
}
