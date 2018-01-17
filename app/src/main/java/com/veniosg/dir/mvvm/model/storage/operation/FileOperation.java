package com.veniosg.dir.mvvm.model.storage.operation;

import android.support.annotation.NonNull;

import java.io.File;

import static java.util.UUID.randomUUID;

public abstract class FileOperation<A extends FileOperation.Arguments> {
    protected final int id = randomUUID().hashCode();

    /**
     * @return Whether the operation was successful.
     */
    public abstract boolean operate(A args);

    /**
     * Try the operation using SAF facilities
     * Triggered if {@link #operate(Arguments)} returns false and we have write permissions.
     *
     * @return Whether the operation was successful.
     */
    public abstract boolean operateSaf(A args);

    /**
     * Good place to show initial UI, or prepare any dialogs etc.
     * Called right before running the operation. Can be called multiple times.
     *
     * @param args Original arguments for the invocation that is getting started.
     */
    abstract void onStartOperation(A args);

    /**
     * Good place to show final result (success/failure) UI.
     * No other callbacks will happen after this.
     *
     * @param success Whether the invocation was successful.
     * @param args    Original arguments for the invocation that just finished.
     */
    abstract void onResult(boolean success, A args);

    /**
     * Called if the user has denied storage write permissions on the parent volume of {@link Arguments#target}.
     * No other callbacks will happen after this.
     */
    abstract void onAccessDenied();

    /**
     * Good place to hide any progress UI. You may still get calls to onResult()/onAccessDenied() after this.
     */
    abstract void onRequestingAccess();

    /**
     * @return Whether this type of operation can fail because of lack of storage write permissions.
     */
    public abstract boolean needsWriteAccess();

    public static abstract class Arguments {
        @NonNull
        private final File target;

        protected Arguments(@NonNull File target) {
            this.target = target;
        }

        @NonNull
        protected File getTarget() {
            return target;
        }
    }
}
