package com.veniosg.dir.view;

import android.animation.Animator;
import android.animation.AnimatorSet;

import java.util.ArrayList;
import java.util.List;

public class AnimatorSynchroniser {
    private static final int DEFAULT_NUM_COMPONENTS_TO_SYNCHRONISE = 2;

    private int mMaxAnimators = DEFAULT_NUM_COMPONENTS_TO_SYNCHRONISE;
    private List<Animator> mAnimators = new ArrayList<Animator>(mMaxAnimators);

    public void setMaxAnimators(int num) {
        this.mMaxAnimators = num;
    }

    /**
     * Add an animation and fire it as soon as the number of waiting animators reaches the max.
     */
    public synchronized void addWaitingAnimation(Animator anim) {
        mAnimators.add(anim);

        if (mAnimators.size() >= mMaxAnimators) {
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(mAnimators);
            animSet.start();

            mAnimators.clear();
        }
    }
}
