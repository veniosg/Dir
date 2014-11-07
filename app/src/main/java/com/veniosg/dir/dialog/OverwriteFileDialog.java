package com.veniosg.dir.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.veniosg.dir.R;

@SuppressLint("ValidFragment")
class OverwriteFileDialog extends BaseDialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.file_exists)
				.setMessage(R.string.overwrite_question)
				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								((Overwritable) getTargetFragment()).overwrite();
							}
						})
                .setNegativeButton(R.string.no, null)
				.create();
	}
	
	public interface Overwritable {
		public void overwrite();
	}
}