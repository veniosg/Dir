package com.veniosg.dir.test.actor.action;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.support.test.rule.ActivityTestRule;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.android.activity.FileManagerActivity;

import java.io.File;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static android.content.Intent.ACTION_GET_CONTENT;
import static android.content.Intent.ACTION_SEARCH;
import static android.content.Intent.ACTION_VIEW;
import static android.content.Intent.CATEGORY_OPENABLE;
import static com.veniosg.dir.IntentConstants.ACTION_PICK_DIRECTORY;
import static com.veniosg.dir.IntentConstants.ACTION_PICK_FILE;
import static com.veniosg.dir.IntentConstants.EXTRA_TITLE;

public class LaunchesActions {
    private final ActivityTestRule<? extends Activity> mActivityRule;

    public LaunchesActions(ActivityTestRule<? extends Activity> activityRule) {
        mActivityRule = activityRule;
    }

    public void dir() {
        Intent dirIntent = new Intent(Intent.ACTION_MAIN);
        dirIntent.setComponent(
                new ComponentName("com.veniosg.dir", FileManagerActivity.class.getName()));
        launch(dirIntent);
    }

    public void viewWithFileScheme(File file) {
        launch(new Intent(ACTION_VIEW, Uri.fromFile(file)));
    }

    public void pickFileIn(File directory) {
        launch(new Intent(ACTION_PICK_FILE, Uri.fromFile(directory)));
    }

    public void pickFileWithFileSchemeAndAnyType() {
        launch(buildIntent(
                ACTION_PICK_FILE,
                "file",
                "text/plain"   // for */* filter
        ));
    }

    public void pickFileWithFileSchemeAndType(String mimeType) {
        launch(buildIntent(
                ACTION_PICK_FILE,
                "file",
                mimeType
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
                ACTION_PICK_DIRECTORY,
                "file",
                null
        ));
    }

    public void pickDirectoryWithNoSchemeAndNoType() {
        launch(buildIntent(
                ACTION_PICK_DIRECTORY,
                null,
                null
        ));
    }

    public void getContentWithNoSchemeAndAnyType() {
        launch(buildIntent(
                ACTION_GET_CONTENT,
                null,
                "text/plain"    // for */* filter
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

    public void saveAs(File sourceData) {
        Uri sourceUri = new Uri.Builder()
                .scheme("content")
                .path(sourceData.getAbsolutePath())
                .build();
        Intent openableIntent = new Intent(ACTION_VIEW, sourceUri);
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

    public void searchIn(File searchIn) {
        Intent intent = buildIntent(ACTION_SEARCH, null, null);
        intent.setData(new Uri.Builder()
                .path(searchIn.getAbsolutePath())
                .build());
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
