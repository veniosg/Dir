package com.veniosg.dir.mvvm.model.storage.operation;

import android.support.annotation.NonNull;

import com.veniosg.dir.mvvm.model.storage.access.StorageAccessManager;

import java.io.File;


public class FakeStorageAccessManager implements StorageAccessManager {
    private enum Behavior {
        GRANT,
        DENY,
        ERROR
    }

    private Behavior behavior = Behavior.ERROR;
    private boolean writeAccess = false;
    private boolean safBased = false;
    private boolean listened = true;

    public static FakeStorageAccessManager aFakeStorageAccessManager() {
        return new FakeStorageAccessManager();
    }

    public FakeStorageAccessManager thatAlwaysGrantsAccess() {
        behavior = Behavior.GRANT;
        listened = false;
        return this;
    }

    public FakeStorageAccessManager thatAlwaysDeniesAccess() {
        behavior = Behavior.DENY;
        listened = false;
        return this;
    }

    public FakeStorageAccessManager thatAlwaysErrors() {
        behavior = Behavior.ERROR;
        listened = false;
        return this;
    }

    public FakeStorageAccessManager thatHasNoWriteAccess() {
        writeAccess = false;
        return this;
    }

    public FakeStorageAccessManager thatHasWriteAccess() {
        writeAccess = true;
        return this;
    }

    public FakeStorageAccessManager thatIsSafBased() {
        safBased = true;
        return this;
    }

    @Override
    public boolean hasWriteAccess(@NonNull File fileInStorage) {
        return writeAccess;
    }

    @Override
    public void requestWriteAccess(@NonNull File fileInStorage, @NonNull AccessPermissionListener listener) {
        if (!listened) {
            listened = true;
            switch (behavior) {
                case GRANT:
                    writeAccess = true;
                    listener.granted();
                    break;
                case DENY:
                    writeAccess = false;
                    listener.denied();
                    break;
                case ERROR:
                    writeAccess = false;
                    listener.error();
                    break;
            }
        }
    }

    @Override
    public boolean isSafBased() {
        return safBased;
    }
}
