/*
 * Copyright (C) 2018 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.android.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

import com.veniosg.dir.R;

import static android.content.Intent.ACTION_OPEN_DOCUMENT_TREE;
import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;
import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
import static com.veniosg.dir.IntentConstants.ACTION_STORAGE_ACCESS_RESULT;
import static com.veniosg.dir.IntentConstants.EXTRA_STORAGE_ACCESS_GRANTED;
import static com.veniosg.dir.android.ui.Themer.getTranslucentThemeId;
import static com.veniosg.dir.android.util.Utils.viewUri;

public class SafPromptActivity extends Activity {
    private static final int REQUEST_CODE_SAF = 1;

    private boolean granted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Android can't apply the translucent flag at runtime and trying to set or update the theme
        // (even when it has already been set as translucent by the manifest-defined theme) results
        // in a black background. So we just piggyback off the system respecting the manifest-defined
        // theme and then rewrite it with our own theme to match the rest of the app's style.
        getTheme().applyStyle(getTranslucentThemeId(this), true);

        new AlertDialog.Builder(this)
                .setTitle(R.string.write_access_required)
                .setMessage(R.string.saf_dialog_message)
                .setNeutralButton(R.string.help, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewUri(SafPromptActivity.this, "http://pxhouse.co/saf", null);
                    }
                })
                .setPositiveButton(R.string.grant_access, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent safPickIntent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                        startActivityForResult(safPickIntent, REQUEST_CODE_SAF);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SAF) {
            if (resultCode == RESULT_OK && data.getData() != null) {
                Uri grantedDocTreeUri = data.getData();
                getContentResolver().takePersistableUriPermission(grantedDocTreeUri,
                        FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION);
                granted = true;
            } else {
                granted = false;
            }
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        final LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        Intent intent = new Intent(ACTION_STORAGE_ACCESS_RESULT);
        intent.putExtra(EXTRA_STORAGE_ACCESS_GRANTED, granted);
        localBroadcastManager.sendBroadcast(intent);

        super.onDestroy();
    }
}
