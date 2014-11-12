package com.veniosg.dir;

import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
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

    public static final int ANIM_START_DELAY = 10;

    public static final Interpolator inInterpolator = new PathInterpolator(0, 0, 0.2F, 1);  // linear_out_slow_in
    public static final Interpolator outInterpolator = new PathInterpolator(0.4F, 0, 1, 1); // fast_out_linear_in
}
