package com.accessifiers.filebrowser.test.actor;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.accessifiers.filebrowser.test.actor.action.LaunchesActions;
import com.accessifiers.filebrowser.test.actor.assertion.LaunchedAssertions;

public class Android {
    private final LaunchesActions launches;
    private final LaunchedAssertions launched;

    public Android(ActivityTestRule<? extends Activity> activityRule) {
        launches = new LaunchesActions(activityRule);
        launched = new LaunchedAssertions();
    }

    public LaunchesActions launches() {
        return launches;
    }

    public LaunchedAssertions launched() {
        return launched;
    }
}
