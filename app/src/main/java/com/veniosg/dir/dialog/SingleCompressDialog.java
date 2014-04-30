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

import com.veniosg.dir.IntentConstants;
import com.veniosg.dir.R;
import com.veniosg.dir.dialog.OverwriteFileDialog.Overwritable;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.service.ZipService;

import java.io.File;

public class SingleCompressDialog extends DialogFragment implements Overwritable {
	private FileHolder mFileHolder;
    private File tbcreated;
    private String zipname;

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
		v.setHint(R.string.compressed_file_name);
		
		v.setOnEditorActionListener(new TextView.OnEditorActionListener(){
			public boolean onEditorAction(TextView text, int actionId, KeyEvent event) {
				   if (actionId == EditorInfo.IME_ACTION_GO)
					   compress(v.getText().toString());
				   dismiss();
				   return true;
				}
		});
		
		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.menu_compress)
				.setView(view)
				.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						compress(v.getText().toString());
					}
				})
                .setNegativeButton(android.R.string.cancel, null)
                .create();
	}
	
	private void compress(final String zipname){
		tbcreated = new File(mFileHolder.getFile().getParent() + File.separator + zipname + ".zip");
		if (tbcreated.exists()) {
			this.zipname = zipname;
			OverwriteFileDialog dialog = new OverwriteFileDialog();
			dialog.setTargetFragment(this, 0);
			dialog.show(getFragmentManager(), "OverwriteFileDialog");
		} else {
            ZipService.compressTo(getActivity(), mFileHolder, tbcreated);
		}
	}
	
	@Override
	public void overwrite() {
        if (tbcreated != null && zipname != null) {
            tbcreated.delete();
            compress(zipname);
        }
	}
}