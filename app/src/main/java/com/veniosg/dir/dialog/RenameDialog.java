package com.veniosg.dir.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.fragment.FileListFragment;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.util.MediaScannerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RenameDialog extends DarkTitleDialogFragment {
	private FileHolder mFileHolder;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolder = getArguments().getParcelable(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_text_input, null);
		final EditText v = (EditText) view.findViewById(R.id.foldername);
		v.setText(mFileHolder.getName());

		v.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView text, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_GO)
					renameTo(text.getText().toString());
				dismiss();
				return true;
			}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.menu_rename)
				.setIcon(mFileHolder.getIcon())
				.setView(view)
				.setNegativeButton(android.R.string.cancel, null)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								renameTo(v.getText().toString());

							}
						})
                .create();
	}

	private void renameTo(String to){
		boolean res = false;
		
		if(to.length() > 0){
			File from = mFileHolder.getFile();
			File dest = new File(mFileHolder.getFile().getParent(), to);
            List<String> paths = new ArrayList<String>();
            if(from.isDirectory()) {
                MediaScannerUtils.getPathsOfFolder(paths, from);
            }

			if(!dest.exists()){
				res = mFileHolder.getFile().renameTo(dest);

				// Inform media scanner
                if (res) {
                    if (dest.isFile()) {
                        MediaScannerUtils.informFileDeleted(getActivity().getApplicationContext(), from);
                        MediaScannerUtils.informFileAdded(getActivity().getApplicationContext(), dest);
                    } else {
                        MediaScannerUtils.informPathsDeleted(getActivity().getApplicationContext(), paths);
                        MediaScannerUtils.informFolderAdded(getActivity().getApplicationContext(), dest);
                    }
                }
			}
		}
		
		Toast.makeText(getActivity(), res ? R.string.rename_success : R.string.rename_failure, Toast.LENGTH_SHORT).show();
	}
}