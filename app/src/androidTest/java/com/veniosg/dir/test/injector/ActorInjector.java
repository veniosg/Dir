package com.veniosg.dir.test.injector;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.veniosg.dir.test.actor.Android;
import com.veniosg.dir.test.actor.User;

public class ActorInjector {
    private ActorInjector() {}

    public static User user() {
        return new User();
    }

    public static Android android(ActivityTestRule<? extends Activity> activityRule) {
        return new Android(activityRule);
    }
}
