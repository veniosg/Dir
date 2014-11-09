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

package com.veniosg.dir.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.veniosg.dir.R;
import com.veniosg.dir.util.Logger;

import java.io.File;

import static android.animation.LayoutTransition.APPEARING;
import static android.animation.LayoutTransition.CHANGE_APPEARING;
import static android.animation.LayoutTransition.CHANGE_DISAPPEARING;
import static android.animation.LayoutTransition.DISAPPEARING;
import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.text.InputType.TYPE_TEXT_VARIATION_URI;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_DPAD_CENTER;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.inputmethod.EditorInfo.IME_ACTION_GO;
import static android.widget.ImageView.ScaleType.CENTER_INSIDE;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.LEFT_OF;
import static android.widget.RelativeLayout.RIGHT_OF;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.ANIM_START_DELAY;
import static com.veniosg.dir.AnimationConstants.inInterpolator;
import static com.veniosg.dir.AnimationConstants.outInterpolator;
import static com.veniosg.dir.util.FileUtils.isOk;
import static com.veniosg.dir.util.Utils.backWillExit;
import static com.veniosg.dir.util.Utils.dp;
import static com.veniosg.dir.view.PathController.Mode.MANUAL_INPUT;
import static com.veniosg.dir.view.PathController.Mode.STANDARD_INPUT;
import static com.veniosg.dir.view.Themer.getThemedResourceId;

/**
 * Provides a self contained way to represent the current path and provides a handy way of navigating. <br/><br/>
 *
 * <b>Note 1:</b> If you need to allow directory navigation outside of this class (e.g. when the user clicks on a folder from a {@link ListView}), use {@link #cd(File)} or {@link #cd(String)}. This is a requirement for the views of this class to
 * properly refresh themselves. <i>You will get notified through the usual {@link OnDirectoryChangedListener}. </i><br/><br/>
 *
 * <b>Note 2:</b> To switch between {@link Mode Modes} use the {@link #switchToManualInput()} and {@link #switchToStandardInput()} methods!
 *
 * @author George Venios
 */
public class PathBar extends ViewFlipper implements PathController {
    private static final float BG_ITEM_SKEW_DELTA_DP = 3F;
    private final int BG_ITEM_SKEW_DELTA_PX;
    private static float NEW_ITEM_DISTANCE;

	private File mCurrentDirectory = null;
	private Mode mCurrentMode = STANDARD_INPUT;
	private File mInitialDirectory = null;

	/** ImageButton used to switch to MANUAL_INPUT. */
	private ImageButton mSwitchToManualModeButton = null;
	/** Layout holding all path buttons. */
	private PathButtonLayout mPathButtons = null;
	/** Container of {@link #mPathButtons}. Allows horizontal scrolling. */
	private HorizontalScrollView mPathButtonsContainer = null;
	/** The EditText holding the path in MANUAL_INPUT. */
	private EditText mPathEditText = null;
	/** The ImageButton to confirm the manually entered path. */
	private ImageButton mGoButton = null;

	private OnDirectoryChangedListener mDirectoryChangedListener = new OnDirectoryChangedListener() {
		@Override
		public void directoryChanged(File newCurrentDir) {
		}
	};

	public PathBar(Context context) {
		super(context);
        NEW_ITEM_DISTANCE = getResources().getDisplayMetrics().widthPixels;
        BG_ITEM_SKEW_DELTA_PX = (int) dp((int) BG_ITEM_SKEW_DELTA_DP, context);
		init();
	}

	public PathBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        NEW_ITEM_DISTANCE = getResources().getDisplayMetrics().widthPixels;
        BG_ITEM_SKEW_DELTA_PX = (int) dp((int) BG_ITEM_SKEW_DELTA_DP, context);
        init();
	}

	private void init() {
		mCurrentDirectory = Environment.getExternalStorageDirectory();
		mInitialDirectory = Environment.getExternalStorageDirectory();

		this.setInAnimation(getContext(), android.R.anim.fade_in);
		this.setOutAnimation(getContext(), android.R.anim.fade_out);

		// RelativeLayout1
		RelativeLayout standardModeLayout = new RelativeLayout(getContext());
		{ // I use a block here so that layoutParams can be used as a variable name further down.
			android.widget.ViewFlipper.LayoutParams layoutParams = new android.widget.ViewFlipper.LayoutParams(
					MATCH_PARENT, MATCH_PARENT);
			standardModeLayout.setLayoutParams(layoutParams);

			this.addView(standardModeLayout);
		}

		// ImageButton -- GONE. Kept this code in case we need to use an right-aligned button in the future.
		mSwitchToManualModeButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
                    WRAP_CONTENT, MATCH_PARENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);

			mSwitchToManualModeButton.setLayoutParams(layoutParams);
			mSwitchToManualModeButton.setId(10);
			mSwitchToManualModeButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);
			mSwitchToManualModeButton.setVisibility(GONE);

			standardModeLayout.addView(mSwitchToManualModeButton);
		}

		// ImageButton -- GONE. Kept this code in case we need to use a left-aligned button in the future.
		ImageButton cdToRootButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					WRAP_CONTENT, MATCH_PARENT);
			layoutParams.addRule(ALIGN_PARENT_LEFT);

			cdToRootButton.setLayoutParams(layoutParams);
			cdToRootButton.setId(11);
            cdToRootButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);
            cdToRootButton.setImageResource(R.drawable.ic_navbar_home);
			cdToRootButton.setScaleType(CENTER_INSIDE);
			cdToRootButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cd("/");
				}
			});
			cdToRootButton.setVisibility(GONE);

			standardModeLayout.addView(cdToRootButton);
		}

		// Horizontal ScrollView container
		mPathButtonsContainer = new HorizontalScrollView(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					WRAP_CONTENT, MATCH_PARENT);
			layoutParams.addRule(LEFT_OF, mSwitchToManualModeButton.getId());
			layoutParams.addRule(RIGHT_OF, cdToRootButton.getId());
			layoutParams.alignWithParent = true;

			mPathButtonsContainer.setLayoutParams(layoutParams);
			mPathButtonsContainer.setHorizontalScrollBarEnabled(false);
            mPathButtonsContainer.setFillViewport(true);

			standardModeLayout.addView(mPathButtonsContainer);
		}

		// PathButtonLayout
		mPathButtons = new PathButtonLayout(getContext());
		{
			android.widget.LinearLayout.LayoutParams layoutParams = new android.widget.LinearLayout.LayoutParams(
					WRAP_CONTENT, MATCH_PARENT);

			mPathButtons.setLayoutParams(layoutParams);
			mPathButtons.setNavigationBar(this);
            LayoutTransition transition = new LayoutTransition();
            // Next two values should be the same as in AnimatedFileListContainer
            transition.setDuration(ANIM_DURATION);
            transition.setStartDelay(APPEARING, ANIM_START_DELAY);
            transition.setStartDelay(DISAPPEARING, ANIM_START_DELAY);
            transition.setAnimator(APPEARING, createAppearingAnimator(transition));
            transition.setAnimator(DISAPPEARING, createDisappearingAnimator(transition));
            transition.setInterpolator(CHANGE_APPEARING, inInterpolator);
            transition.setInterpolator(CHANGE_DISAPPEARING, outInterpolator);
            mPathButtons.setLayoutTransition(transition);

			mPathButtonsContainer.addView(mPathButtons);
		}

		// RelativeLayout2
		RelativeLayout manualModeLayout = new RelativeLayout(getContext());
		{
			android.widget.ViewFlipper.LayoutParams layoutParams = new android.widget.ViewFlipper.LayoutParams(
					MATCH_PARENT, MATCH_PARENT);
			manualModeLayout.setLayoutParams(layoutParams);

			this.addView(manualModeLayout);
		}

		// ImageButton
		mGoButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
			layoutParams.addRule(ALIGN_PARENT_RIGHT);

			mGoButton.setLayoutParams(layoutParams);
			mGoButton.setId(20);
			mGoButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);
            mGoButton.setTranslationZ(dp(4, getContext()));
            mGoButton.setImageResource(R.drawable.ic_navbar_accept);
			mGoButton.setScaleType(CENTER_INSIDE);
			mGoButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					manualInputCd(mPathEditText.getText().toString());
				}
			});
            mGoButton.setMinimumWidth((int) dp(48, getContext()));

			manualModeLayout.addView(mGoButton);
		}

		// EditText
		mPathEditText = new EditText(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					WRAP_CONTENT, MATCH_PARENT);
			layoutParams.addRule(ALIGN_PARENT_LEFT);
			layoutParams.alignWithParent = true;
			layoutParams.addRule(LEFT_OF, mGoButton.getId());
            float iconPadding = getResources().getDimension(R.dimen.item_icon_margin_left);

			mPathEditText.setLayoutParams(layoutParams);
            mPathEditText.setPadding((int) iconPadding, 0,
                    mPathEditText.getPaddingRight(), 0);
			mPathEditText.setInputType(TYPE_TEXT_VARIATION_URI);
			mPathEditText.setImeOptions(IME_ACTION_GO);
			mPathEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							if ((actionId == IME_ACTION_GO)
                                    || ((event.getAction() == ACTION_DOWN) && ((event.getKeyCode() == KEYCODE_DPAD_CENTER) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)))) {
								if (manualInputCd(v.getText().toString()))
									// Since we have successfully navigated.
									return true;
							}

							return false;
						}
					});

			manualModeLayout.addView(mPathEditText);
		}
	}


    private Animator createAppearingAnimator(final LayoutTransition transition) {
        AnimatorSet anim = new AnimatorSet();
        anim.setDuration(transition.getDuration(APPEARING));
        anim.setInterpolator(inInterpolator);
        anim.playTogether(ObjectAnimator.ofFloat(null, "alpha", 0.3F, 1F),
                ObjectAnimator.ofFloat(null, "translationX", NEW_ITEM_DISTANCE, 0));
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                Animator animator = ObjectAnimator.ofInt(mPathButtonsContainer, "scrollX",
                            mPathButtonsContainer.getScrollX(),
                            mPathButtons.getWidth())
                        .setDuration((long) (transition.getDuration(APPEARING)
                                + (0.5F *transition.getDuration(APPEARING))));
                animator.setInterpolator(inInterpolator);
                animator.start();
            }

            @Override
            public void onAnimationEnd(Animator animation) {}

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });
        return anim;
    }

    private Animator createDisappearingAnimator(final LayoutTransition transition) {
        AnimatorSet anim = new AnimatorSet();
        anim.setDuration(transition.getDuration(DISAPPEARING));
        anim.setInterpolator(outInterpolator);
        anim.playTogether(ObjectAnimator.ofFloat(null, "translationX", 0, NEW_ITEM_DISTANCE),
                ObjectAnimator.ofFloat(null, "alpha", 1F, 0.3F));
        return anim;
    }

	public void setInitialDirectory(File initDir) {
		mInitialDirectory = initDir;
		cd(initDir);
	}

	public void setInitialDirectory(String initPath) {
		setInitialDirectory(new File(initPath));
	}

	public File getInitialDirectory() {
		return mInitialDirectory;
	}

	public File getCurrentDirectory() {
		return mCurrentDirectory;
	}

	/**
	 * Use instead of {@link #cd(String)} when in {@link Mode#MANUAL_INPUT}.
	 *
	 * @param path The path to cd() to.
	 * @return true if the cd succeeded.
	 */
	boolean manualInputCd(String path) {
		if (cd(path)) {
			// if cd() successful, hide the keyboard
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
			switchToStandardInput();
			return true;
		} else {
			Logger.log(Log.WARN, Logger.TAG_PATHBAR, "Input path does not exist or is not a folder!");
			return false;
		}
	}

    public boolean cd(File file) {
        return cd(file, false);
    }

	public boolean cd(File file, boolean forceNoAnim) {
		boolean res = false;

		if (isOk(file)) {
            File oldDir = new File(mCurrentDirectory.getAbsolutePath());

			// Set proper current directory.
			mCurrentDirectory = file;

			// Refresh button layout.
			mPathButtons.refresh(forceNoAnim ? null : oldDir, mCurrentDirectory);

			// Refresh manual input field.
			mPathEditText.setText(file.getAbsolutePath());

			res = true;
		} else
			res = false;

		mDirectoryChangedListener.directoryChanged(file);

		return res;
	}
	public boolean cd(String path) {
		return cd(new File(path));
	}

	public File[] ls() {
		return mCurrentDirectory.listFiles();
	}

	public void setOnDirectoryChangedListener(
			OnDirectoryChangedListener listener) {
		if (listener != null)
			mDirectoryChangedListener = listener;
		else
			mDirectoryChangedListener = new OnDirectoryChangedListener() {
				@Override
				public void directoryChanged(File newCurrentDir) {
				}
			};
	}

	/**
	 * Switches to {@link Mode#MANUAL_INPUT}.
	 */
	public void switchToManualInput() {
		setDisplayedChild(1);
		mCurrentMode = MANUAL_INPUT;
	}

	/**
	 * Switches to {@link Mode#STANDARD_INPUT}.
	 */
	public void switchToStandardInput() {
		setDisplayedChild(0);
		mCurrentMode = STANDARD_INPUT;
	}

	public boolean onBackPressed() {
		// Switch mode.
		if (mCurrentMode == MANUAL_INPUT) {
			switchToStandardInput();
		}
		// Go back.
		else if (mCurrentMode == STANDARD_INPUT) {
			if (!backWillExit(mInitialDirectory.getAbsolutePath(),
                    mCurrentDirectory.getAbsolutePath())) {
				cd(mCurrentDirectory.getParent());
				return true;
			} else
				return false;
		}

		return true;
	}

	/**
	 * Returns the current {@link PathBar.Mode}.
	 *
	 */
	public Mode getMode() {
		return mCurrentMode;
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(enabled)
			switchToStandardInput();
		else
			switchToManualInput();
		mPathEditText.setEnabled(enabled);
		mGoButton.setVisibility(enabled ? VISIBLE : GONE);

		super.setEnabled(enabled);
	}

	PathButtonLayout getPathButtonLayout() {
		return mPathButtons;
	}

    public Drawable getItemBackgroundDrawable(Context c, String absolutePath) {
        if ("/".equals(absolutePath)) {
            return c.getDrawable(R.drawable.bg_btn_pathbar_semiskewed);
        } else {
            return c.getDrawable(R.drawable.bg_btn_pathbar_skewed);
        }
    }
}