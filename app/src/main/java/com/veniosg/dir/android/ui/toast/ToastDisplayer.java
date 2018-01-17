package com.veniosg.dir.android.ui.toast;

import android.content.Context;

import com.veniosg.dir.R;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class ToastDisplayer {
    private Context context;

    public ToastDisplayer(Context context) {
        this.context = context;
    }

    public void renameSuccess() {
        makeText(context, R.string.rename_success, LENGTH_SHORT).show();
    }

    public void renameFailure() {
        makeText(context, R.string.rename_failure, LENGTH_SHORT).show();
    }

    public void createDirectorySuccess() {
        makeText(context, R.string.create_dir_success, LENGTH_SHORT).show();
    }

    public void createDirectoryFailure() {
        makeText(context, R.string.create_dir_failure, LENGTH_SHORT).show();
    }

    public void deleteSuccess() {
        makeText(context, R.string.delete_success, LENGTH_SHORT).show();
    }

    public void deleteFailure() {
        makeText(context, R.string.delete_failure, LENGTH_SHORT).show();
    }

    public void grantAccessWrongDirectory() {
        makeText(context, R.string.select_sd_root, LENGTH_LONG).show();
    }
}
