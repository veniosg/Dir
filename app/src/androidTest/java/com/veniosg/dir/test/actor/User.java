package com.accessifiers.filebrowser.test.actor;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.accessifiers.filebrowser.test.actor.action.LaunchesActions;
import com.accessifiers.filebrowser.test.actor.action.SelectsActions;
import com.accessifiers.filebrowser.test.actor.action.TypesActions;
import com.accessifiers.filebrowser.test.actor.assertion.CannotSeeAssertions;
import com.accessifiers.filebrowser.test.actor.assertion.SeesAssertions;

public class User {
    private final SeesAssertions sees = new SeesAssertions();
    private final CannotSeeAssertions cannotSeeAssertions = new CannotSeeAssertions();
    private final SelectsActions selects = new SelectsActions();
    private final TypesActions types = new TypesActions();
    private final LaunchesActions launches;

    public User(ActivityTestRule<? extends Activity> activityRule) {
        launches = new LaunchesActions(activityRule);
    }

    public SeesAssertions sees() {
        return sees;
    }

    public SelectsActions selects() {
        return selects;
    }

    public CannotSeeAssertions cannotSee() {
        return cannotSeeAssertions;
    }

    public TypesActions types() {
        return types;
    }

    public LaunchesActions launches() {
        return launches;
    }
}
