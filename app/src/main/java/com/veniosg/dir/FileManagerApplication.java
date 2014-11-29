package com.veniosg.dir;

import android.animation.Animator;
import android.app.Application;
import android.graphics.Bitmap;
import android.view.ViewConfiguration;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import com.veniosg.dir.misc.MimeTypes;
import com.veniosg.dir.util.CopyHelper;
import com.veniosg.dir.view.AnimatorSynchroniser;
import com.veniosg.dir.view.Themer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import static com.veniosg.dir.misc.ThumbnailHelper.imageDecoder;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
    private MimeTypes mMimeTypes;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mCopyHelper = new CopyHelper();
        mMimeTypes = MimeTypes.newInstance(this);

        // Force-enable the action overflow
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        // UIL
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheSize(10240000) // 10MB
                .imageDecoder(imageDecoder(getApplicationContext()))
                .build();
        ImageLoader.getInstance().init(config);
    }

    public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}

    public MimeTypes getMimeTypes() {
        return mMimeTypes;
    }

    // Static for easier access
    private static AnimatorSynchroniser mAnimSync = new AnimatorSynchroniser();
    public static void enqueueAnimator(Animator animator) {
        mAnimSync.addWaitingAnimation(animator);
    }
}