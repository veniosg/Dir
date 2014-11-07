package com.veniosg.dir.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import java.util.List;

public class MultiCompressDialog extends BaseDialogFragment implements Overwritable {
	private List<FileHolder> mFileHolders;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mFileHolders = getArguments().getParcelableArrayList(IntentConstants.EXTRA_DIALOG_FILE_HOLDER);
	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return super.onCreateView(inflater, container, savedInstanceState);
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

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.menu_compress)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        compress(v.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setIcon(tintIcon(R.drawable.ic_action_box));
        return dialog;
	}
	
	private void compress(final String zipname){
		this.zipname = zipname;
		tbcreated = new File(mFileHolders.get(0).getFile().getParent() + File.separator + zipname + ".zip");
		if (tbcreated.exists()) {
			this.zipname = zipname;
			OverwriteFileDialog dialog = new OverwriteFileDialog();
			dialog.setTargetFragment(this, 0);
			dialog.show(getFragmentManager(), "OverwriteFileDialog");
		} else {
            ZipService.compressTo(getActivity(), mFileHolders, tbcreated);
		}
	}

	private File tbcreated;
	private String zipname;
	
	@Override
	public void overwrite() {
		tbcreated.delete();
		compress(zipname);
	}
}