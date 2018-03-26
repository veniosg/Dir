package com.veniosg.dir.test.matcher;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.v7.widget.RecyclerView;

import com.veniosg.dir.android.adapter.viewholder.FileListViewHolder;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;

public class RecyclerViewHasItemWithTitle extends BoundedMatcher<RecyclerView.ViewHolder, FileListViewHolder> {
    private final String expectedTitle;

    public RecyclerViewHasItemWithTitle(String expectedTitle) {
        super(FileListViewHolder.class);
        this.expectedTitle = expectedTitle;
    }

    @Override
    protected boolean matchesSafely(FileListViewHolder item) {
        try {
            return item.primaryInfo.getText().toString().equals(expectedTitle);
        } catch (NullPointerException ex) {
            return false;
        }
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("view holder with expectedTitle: " + expectedTitle);
    }

    @Factory
    public static Matcher<RecyclerView.ViewHolder> hasItemWithTitle(String expectedTitle) {
        return new RecyclerViewHasItemWithTitle(expectedTitle);
    }
}
