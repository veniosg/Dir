package com.accessifiers.filebrowser.test.injector;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.accessifiers.filebrowser.test.actor.Android;
import com.accessifiers.filebrowser.test.actor.User;

public class ActorInjector {
    private ActorInjector() {}

    public static User user(ActivityTestRule<? extends Activity> activityRule) {
        return new User(activityRule);
    }

    public static Android android(ActivityTestRule<? extends Activity> activityRule) {
        return new Android(activityRule);
    }
}
