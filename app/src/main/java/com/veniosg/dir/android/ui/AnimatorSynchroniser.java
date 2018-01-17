/*
 * Copyright (C) 2014 George Venios
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

package com.veniosg.dir.android.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;

import java.util.ArrayList;
import java.util.List;

public class AnimatorSynchroniser {
    private static final int DEFAULT_NUM_COMPONENTS_TO_SYNCHRONISE = 2;

    private List<Animator> mAnimators = new ArrayList<>(DEFAULT_NUM_COMPONENTS_TO_SYNCHRONISE);

    /**
     * Add an animation and fire it as soon as the number of waiting animators reaches the max.
     */
    public synchronized void addWaitingAnimation(Animator anim) {
        mAnimators.add(anim);

        if (mAnimators.size() >= DEFAULT_NUM_COMPONENTS_TO_SYNCHRONISE) {
            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(mAnimators);
            animSet.start();

            mAnimators.clear();
        }
    }
}
