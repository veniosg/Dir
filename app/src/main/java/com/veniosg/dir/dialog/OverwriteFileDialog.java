package com.veniosg.dir.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.veniosg.dir.R;

class OverwriteFileDialog extends DialogFragment {
	
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