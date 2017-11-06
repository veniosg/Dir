package com.veniosg.dir.test.acceptance;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.veniosg.dir.android.activity.FileManagerActivity;

import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FileManagerActivityTest {
    @Rule
    public ActivityTestRule<FileManagerActivity> mActivityRule = new ActivityTestRule<>(
            FileManagerActivity.class);

    // TODO test intent interface

    // TODO test navigation + operations

    // TODO test other features
}
