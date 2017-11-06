package com.veniosg.dir.test.actor.assertion;

import android.app.Activity;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import com.veniosg.dir.IntentConstants;

import org.hamcrest.Matchers;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasPackage;
import static com.veniosg.dir.IntentConstants.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.AllOf.allOf;

public class LaunchedAssertions {
}
