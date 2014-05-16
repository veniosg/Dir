package com.veniosg.dir;

import android.app.Application;
import android.view.ViewConfiguration;

import com.veniosg.dir.misc.MimeTypes;
import com.veniosg.dir.misc.ThumbnailLoader;
import com.veniosg.dir.util.CopyHelper;
import com.veniosg.dir.view.Themer;

import java.lang.reflect.Field;

public class FileManagerApplication extends Application{
	private CopyHelper mCopyHelper;
    private MimeTypes mMimeTypes;
    private ThumbnailLoader mThumbnailLoader;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mCopyHelper = new CopyHelper();
        mMimeTypes = MimeTypes.newInstance(this);
        mThumbnailLoader = new ThumbnailLoader(this);

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
    }
	
	public CopyHelper getCopyHelper(){
		return mCopyHelper;
	}

    public MimeTypes getMimeTypes() {
        return mMimeTypes;
    }

    public ThumbnailLoader getThumbnailLoader() {
        return mThumbnailLoader;
    }
}