package com.veniosg.dir.mvvm.model.storage.operation;

import android.support.annotation.NonNull;

import java.io.File;

public class FakeArguments extends FileOperation.Arguments {
    protected FakeArguments(@NonNull File target) {
        super(target);
    }
}
