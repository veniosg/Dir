package com.accessifiers.filebrowser.mvvm.model.search;

import android.os.SystemClock;
import android.support.test.espresso.IdlingResource;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;

public class BooleanIdlingResource implements IdlingResource {
    private static final String TAG = "BooleanIdlingResource";
    private final String resourceName;
    private final AtomicBoolean isIdle;
    private final boolean debugFlag;
    private volatile ResourceCallback resourceCallback;
    private volatile long becameBusyAt;
    private volatile long becameIdleAt;

    public BooleanIdlingResource(String resourceName) {
        this(resourceName, false);
    }

    @SuppressWarnings("WeakerAccess")
    public BooleanIdlingResource(String resourceName, boolean debugFlag) {
        this.isIdle = new AtomicBoolean(true);
        this.becameBusyAt = 0L;
        this.becameIdleAt = 0L;
        if (resourceName == null || resourceName.isEmpty()) {
            throw new IllegalArgumentException("resourceName cannot be empty or null!");
        } else {
            this.resourceName = resourceName;
            this.debugFlag = debugFlag;
        }
    }

    public String getName() {
        return this.resourceName;
    }

    public boolean isIdleNow() {
        return this.isIdle.get();
    }

    public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
        this.resourceCallback = resourceCallback;
    }

    public void setBusy() {
        boolean wasIdle = this.isIdle.getAndSet(false);
        if (wasIdle) {
            this.becameBusyAt = SystemClock.uptimeMillis();
        }

        if (this.debugFlag) {
            Log.i("BooleanIdlingResource", (new StringBuilder(51 + String.valueOf(this.resourceName).length()))
                    .append("Resource: ")
                    .append(this.resourceName)
                    .append(" isIdle flag set to: ")
                    .append(wasIdle).toString());
        }

    }

    public void setIdle() {
        boolean wasIdle = this.isIdle.get();
        this.isIdle.set(true);

        if (!wasIdle) {
            if (null != this.resourceCallback) {
                this.resourceCallback.onTransitionToIdle();
            }

            this.becameIdleAt = SystemClock.uptimeMillis();
        }

        if (this.debugFlag) {
            if (isIdleNow()) {
                long busyFor = this.becameIdleAt - this.becameBusyAt;
                Log.i("BooleanIdlingResource", (new StringBuilder(52 + String.valueOf(resourceName).length()))
                        .append("Resource: ")
                        .append(resourceName)
                        .append(" went idle! (Time spent not idle: ")
                        .append(busyFor)
                        .append(")")
                        .toString());
            } else {
                Log.i("BooleanIdlingResource", (new StringBuilder(36 + String.valueOf(resourceName).length()))
                        .append("Resource: ")
                        .append(resourceName)
                        .append(" isIdle flag set to: false")
                        .toString());
            }
        }
    }

    public void dumpStateToLogs() {
        StringBuilder message = (new StringBuilder("Resource: "))
                .append(this.resourceName)
                .append(" is idle?: ")
                .append(this.isIdle.get());
        if (0L == this.becameBusyAt) {
            Log.i("BooleanIdlingResource", message.append(" and has never been busy!").toString());
        } else {
            message.append(" and was last busy at: ").append(this.becameBusyAt);
            if (0L == this.becameIdleAt) {
                Log.w("BooleanIdlingResource", message.append(" AND NEVER WENT IDLE!").toString());
            } else {
                message.append(" and last went idle at: ").append(this.becameIdleAt);
                Log.i("BooleanIdlingResource", message.toString());
            }
        }

    }
}
