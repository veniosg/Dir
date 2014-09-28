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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Environment;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
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
import static android.util.TypedValue.COMPLEX_UNIT_DIP;
import static android.util.TypedValue.applyDimension;
import static com.veniosg.dir.AnimationConstants.ANIM_DURATION;
import static com.veniosg.dir.AnimationConstants.ANIM_START_DELAY;
import static com.veniosg.dir.AnimationConstants.inInterpolator;
import static com.veniosg.dir.AnimationConstants.outInterpolator;
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
public class PathBar extends ViewFlipper {
    private static final float BG_ITEM_SKEW_DELTA_DP = 3F;
    private final int BG_ITEM_SKEW_DELTA_PX;
    private static float NEW_ITEM_DISTANCE;

    /**
	 * The available Modes of this PathBar. </br> See {@link PathBar#switchToManualInput() switchToManualInput()} and {@link PathBar#switchToStandardInput() switchToStandardInput()}.
	 */
	public enum Mode {
		/**
		 * The button path selection mode.
		 */
		STANDARD_INPUT,
		/**
		 * The text path input mode.
		 */
		MANUAL_INPUT
	}

	private File mCurrentDirectory = null;
	private Mode mCurrentMode = Mode.STANDARD_INPUT;
	private File mInitialDirectory = null;

	/** ImageButton used to switch to MANUAL_INPUT. */
	private ImageButton mSwitchToManualModeButton = null;
	/** Layout holding all path buttons. */
	private PathButtonLayout mPathButtons = null;
	/** Container of {@link #mPathButtons}. Allows horizontal scrolling. */
	private ShadowFadingEdgeHorizontalScrollView mPathButtonsContainer = null;
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
        BG_ITEM_SKEW_DELTA_PX = (int) applyDimension(COMPLEX_UNIT_DIP, BG_ITEM_SKEW_DELTA_DP,
                context.getResources().getDisplayMetrics());
		init();
	}

	public PathBar(Context context, AttributeSet attrs) {
		super(context, attrs);
        NEW_ITEM_DISTANCE = getResources().getDisplayMetrics().widthPixels;
        BG_ITEM_SKEW_DELTA_PX = (int) applyDimension(COMPLEX_UNIT_DIP, BG_ITEM_SKEW_DELTA_DP,
                context.getResources().getDisplayMetrics());
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
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			standardModeLayout.setLayoutParams(layoutParams);

			this.addView(standardModeLayout);
		}

		// ImageButton -- GONE. Kept this code in case we need to use an right-aligned button in the future.
		mSwitchToManualModeButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			mSwitchToManualModeButton.setLayoutParams(layoutParams);
			mSwitchToManualModeButton.setId(10);
			mSwitchToManualModeButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);
			mSwitchToManualModeButton.setVisibility(View.GONE);

			standardModeLayout.addView(mSwitchToManualModeButton);
		}

		// ImageButton -- GONE. Kept this code in case we need to use an left-aligned button in the future.
		ImageButton cdToRootButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

			cdToRootButton.setLayoutParams(layoutParams);
			cdToRootButton.setId(11);
            cdToRootButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);
            cdToRootButton.setImageResource(R.drawable.ic_navbar_home);
			cdToRootButton.setScaleType(ScaleType.CENTER_INSIDE);
			cdToRootButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					cd("/");
				}
			});
			cdToRootButton.setVisibility(View.GONE);

			standardModeLayout.addView(cdToRootButton);
		}

		// Horizontal ScrollView container
		mPathButtonsContainer = new ShadowFadingEdgeHorizontalScrollView(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.LEFT_OF,
					mSwitchToManualModeButton.getId());
			layoutParams.addRule(RelativeLayout.RIGHT_OF,
					cdToRootButton.getId());
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
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);

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
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			manualModeLayout.setLayoutParams(layoutParams);

			this.addView(manualModeLayout);
		}

		// ImageButton
		mGoButton = new ImageButton(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

			mGoButton.setLayoutParams(layoutParams);
			mGoButton.setId(20);
			mGoButton.setBackgroundResource(R.drawable.bg_btn_pathbar_straight);

            mGoButton.setImageResource(R.drawable.ic_navbar_accept);
			mGoButton.setScaleType(ScaleType.CENTER_INSIDE);
			mGoButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					manualInputCd(mPathEditText.getText().toString());
				}
			});
            mGoButton.setMinimumWidth((int) applyDimension(
                    COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()
            ));

			manualModeLayout.addView(mGoButton);
		}

		// EditText
		mPathEditText = new EditText(getContext());
		{
			android.widget.RelativeLayout.LayoutParams layoutParams = new android.widget.RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			layoutParams.alignWithParent = true;
			layoutParams.addRule(RelativeLayout.LEFT_OF, mGoButton.getId());

			mPathEditText.setLayoutParams(layoutParams);
			mPathEditText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
			mPathEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
			mPathEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
						@Override
						public boolean onEditorAction(TextView v, int actionId,
								KeyEvent event) {
							if (actionId == EditorInfo.IME_ACTION_GO
									|| (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER || event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
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

    private int getPathBarItemColor() {
        return getContext().getResources().getColor(getThemedResourceId(getContext(), R.attr.pathBarItemColor));
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

    /**
	 * Sets the directory the parent activity showed first so that back behavior is fixed.
	 *
	 * @param initDir The directory.
	 */
	public void setInitialDirectory(File initDir) {
		mInitialDirectory = initDir;
		cd(initDir);
	}

	/**
	 * See {@link #setInitialDirectory(File)}.
	 */
	public void setInitialDirectory(String initPath) {
		setInitialDirectory(new File(initPath));
	}

	/**
	 * @see #setInitialDirectory(File)
	 * @return The initial directory.
	 */
	public File getInitialDirectory() {
		return mInitialDirectory;
	}

	/**
	 * Get the currently active directory.
	 *
	 * @return A {@link File} representing the currently active directory.
	 */
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
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(getWindowToken(), 0);
			switchToStandardInput();
			return true;
		} else {
			Logger.log(Log.WARN, Logger.TAG_PATHBAR, "Input path does not exist or is not a folder!");
			return false;
		}
	}

	/**
	 * {@code cd} to the passed file. If the file is legal input, sets it as the currently active Directory. Otherwise calls the listener to handle it, if any.
	 *
	 * @param file The file to {@code cd} to.
	 * @return Whether the path entered exists and can be navigated to.
	 */
    public boolean cd(File file) {
        return cd(file, false);
    }

	public boolean cd(File file, boolean forceNoAnim) {
		boolean res = false;

		if (isFileOk(file)) {
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

	/**
	 * @see {@link com.veniosg.dir.view.PathBar#cd(File) cd(File)}
	 * @param path
	 *            The path of the Directory to {@code cd} to.
	 * @return Whether the path entered exists and can be navigated to.
	 */
	public boolean cd(String path) {
		return cd(new File(path));
	}

	/**
	 * The same as running {@code File.listFiles()} on the currently active Directory.
	 */
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
		mCurrentMode = Mode.MANUAL_INPUT;
	}

	/**
	 * Switches to {@link Mode#STANDARD_INPUT}.
	 */
	public void switchToStandardInput() {
		setDisplayedChild(0);
		mCurrentMode = Mode.STANDARD_INPUT;
	}

	/**
	 * Activities containing this bar, will have to call this method when the back button is pressed to provide correct backstack redirection and mode switching.
	 *
	 * @return Whether this view consumed the event.
	 */
	public boolean pressBack() {
		// Switch mode.
		if (mCurrentMode == Mode.MANUAL_INPUT) {
			switchToStandardInput();
		}
		// Go back.
		else if (mCurrentMode == Mode.STANDARD_INPUT) {
			if (!backWillExit(mCurrentDirectory.getAbsolutePath())) {
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

	/**
	 *
	 * @param dirPath The current directory's absolute path.
	 * @return Whether the back button should exit the app.
	 */
	private boolean backWillExit(String dirPath) {
		// Count tree depths
		String[] dir = dirPath.split("/");
		int dirTreeDepth = dir.length;

		String[] init = mInitialDirectory.getAbsolutePath().split("/");
		int initTreeDepth = init.length;

		// analyze and return
		if (dirTreeDepth > initTreeDepth) {
			return false;
		} else {
            return dirPath.equals(mInitialDirectory.getAbsolutePath()) || dirPath.equals("/");
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		if(enabled)
			switchToStandardInput();
		else
			switchToManualInput();
		mPathEditText.setEnabled(enabled);
		mGoButton.setVisibility(enabled ? View.VISIBLE : View.GONE);

		super.setEnabled(enabled);
	}

	/**
	 * Interface notifying users of this class when the user has chosen to navigate elsewhere.
	 */
	public interface OnDirectoryChangedListener {
		public void directoryChanged(File newCurrentDir);
	}

	public PathButtonLayout getPathButtonLayout() {
		return mPathButtons;
	}

	boolean isFileOk(File file) {
		// Check file state.
		boolean isFileOK = true;
		isFileOK &= file.exists();
		isFileOK &= file.isDirectory();
		// add more filters here..

		return isFileOK;
	}

    public Drawable getItemBackgroundDrawable(Context c, String absolutePath) {
        if ("/".equals(absolutePath)) {
            return c.getDrawable(R.drawable.bg_btn_pathbar_semiskewed);
        } else {
            return c.getDrawable(R.drawable.bg_btn_pathbar_skewed);
        }
    }

    public Drawable getSquareMaskDrawable(Context c) {
        return wrapForTouchFeedback(c.getResources().getDrawable(R.drawable.btn_pathbar_straight));
    }

    public Drawable getSemiStraightMaskDrawable(Context c) {
        return wrapForTouchFeedback(c.getResources().getDrawable(R.drawable.btn_pathbar_semiskewed));
    }

    public Drawable getSkewedMaskDrawable(Context c) {
        return wrapForTouchFeedback(c.getResources().getDrawable(R.drawable.bg_btn_pathbar_skewed));
    }

    private void configureMaskDrawablePaint(ShapeDrawable shapeDrawable) {
        shapeDrawable.getPaint().setAntiAlias(true);
        shapeDrawable.getPaint().setStyle(Paint.Style.FILL);
        shapeDrawable.getPaint().setColor(Color.WHITE);
    }

    private Drawable wrapForTouchFeedback(Drawable drawable) {
        Drawable touchDrawable = getTouchFeedbackDrawable();
        Drawable[] drawables = new Drawable[]{drawable, touchDrawable.mutate()};
        LayerDrawable touchableDrawable = new LayerDrawable(drawables);
        return new LayerDrawable(drawables);
    }

    public Drawable getTouchFeedbackDrawable() {
        return getResources().getDrawable(getThemedResourceId(getContext(),
                android.R.attr.listChoiceBackgroundIndicator));
    }
}