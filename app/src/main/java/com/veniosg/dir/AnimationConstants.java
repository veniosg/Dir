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

package com.veniosg.dir;

import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.PathInterpolator;

/**
 * @author George Venios
 */
public class AnimationConstants {
    /**
     * Only edit based on system's constants:

     <!-- The duration (in milliseconds) of a short animation. -->
     <integer name="config_shortAnimTime">200</integer>

     <!-- The duration (in milliseconds) of a medium-length animation. -->
     <integer name="config_mediumAnimTime">400</integer>

     <!-- The duration (in milliseconds) of a long animation. -->
     <integer name="config_longAnimTime">500</integer>
     */
    public static final int ANIM_DURATION = 400;

    public static final int ANIM_START_DELAY = 0;

    public static final Interpolator IN_INTERPOLATOR = new PathInterpolator(0.8F, 0, 0.2F, 1);  // slow_in_slow_out
    public static final Interpolator OUT_INTERPOLATOR = new PathInterpolator(0.8F, 0, 0.2F, 1);
}
