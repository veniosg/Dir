/*
 * Copyright (C) 2014-2017 George Venios
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

package com.veniosg.dir.android;

import android.animation.Animator;
import android.app.Application;
import android.view.ViewConfiguration;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.veniosg.dir.android.misc.MimeTypes;
import com.veniosg.dir.android.util.CopyHelper;
import com.veniosg.dir.android.ui.AnimatorSynchroniser;

import java.lang.reflect.Field;

import static com.veniosg.dir.android.misc.ThumbnailHelper.imageDecoder;

public class FileManagerApplication extends Application {
    private static AnimatorSynchroniser sAnimSync = new AnimatorSynchroniser();

    private CopyHelper mCopyHelper;
    private MimeTypes mMimeTypes;

    @Override
    public void onCreate() {
        super.onCreate();

        mCopyHelper = new CopyHelper();
        mMimeTypes = MimeTypes.newInstance(this);

        forceActionOverflow();
        initImageLoader();
    }

    public CopyHelper getCopyHelper() {
        return mCopyHelper;
    }

    public MimeTypes getMimeTypes() {
        return mMimeTypes;
    }

    private void forceActionOverflow() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            // Ignore
        }
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheSize(10240000) // 10MB
                .imageDecoder(imageDecoder(getApplicationContext()))
                .build();
        ImageLoader.getInstance().init(config);
    }

    public static void enqueueAnimator(Animator animator) {
        sAnimSync.addWaitingAnimation(animator);
    }
}