/*
 * Copyright (C) 2012 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.veniosg.dir.android.ui.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.veniosg.dir.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class PickBar extends LinearLayout {
	private EditText mEditText;
	private Button mButton;
	private OnPickRequestedListener mListener;
	
	public PickBar(Context context) {
		super(context);
		init();
	}
	public PickBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	private void init() {
        mButton = new Button(getContext(), null, android.R.attr.buttonBarButtonStyle);
		{
            mButton.setText(R.string.pick_button_default);
			mButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mListener != null)
						mListener.pickRequested(mEditText.getText().toString());
				}
			});
		}

		// EditText
		mEditText = new EditText(getContext());
		{
            int sideMargin = getResources().getDimensionPixelSize(R.dimen.item_icon_margin_left);
			LayoutParams layoutParams = new LayoutParams(0, WRAP_CONTENT);
			// Take up as much space as possible.
			layoutParams.weight = 1;
            layoutParams.setMarginStart(sideMargin);

			mEditText.setLayoutParams(layoutParams);
			mEditText.setHint(R.string.filename_hint);
			mEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
			mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_GO
                                    || (event.getAction() == KeyEvent.ACTION_DOWN
                                            && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                                                || event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
								if (mListener != null)
									mListener.pickRequested(mEditText.getText().toString());
								return true;
							}

							return false;
						}
					});
		}
		
		addView(mEditText);
		addView(mButton);
	}

	public void setText(CharSequence name) {
		mEditText.setText(name);
	}
	
	public void setOnPickRequestedListener(OnPickRequestedListener listener) {
		mListener = listener;
	}
	
	public interface OnPickRequestedListener {
		void pickRequested(String filename);
	}

	public void setButtonText(CharSequence text) {
		mButton.setText( (text == null || text.toString().trim().length() == 0) ? getResources().getString(R.string.pick_button_default) : text);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle state = new Bundle();
		state.putParcelable("superState", super.onSaveInstanceState());
		state.putParcelable("editText", mEditText.onSaveInstanceState());
		state.putParcelable("button", mButton.onSaveInstanceState());
		
		return state;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Bundle savedState = (Bundle) state;
		
		super.onRestoreInstanceState(savedState.getParcelable("superState"));
		mEditText.onRestoreInstanceState(savedState.getParcelable("editText"));
		mButton.onRestoreInstanceState(savedState.getParcelable("button"));
	}
}