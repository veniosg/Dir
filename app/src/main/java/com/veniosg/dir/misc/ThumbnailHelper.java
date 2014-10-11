package com.veniosg.dir.misc;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.veniosg.dir.fragment.PreferenceFragment;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.Logger;
import com.veniosg.dir.util.Utils;

import java.util.List;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.Intent.ACTION_VIEW;
import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;

public class ThumbnailHelper {
    private static final int FADE_IN_DURATION = 400;

    public static void loadIconWithForInto(Context context, FileHolder holder, ImageView imageView) {
        if (holder.getFile().isDirectory()) {
            return;
        } else if (Utils.isImage(holder.getMimeType())) {
            Uri uri = Uri.fromFile(holder.getFile());
            loadIconWithForInto(context, uri, imageView);
        }
    }

    public static void loadIconWithForInto(Context context, Uri uri, ImageView imageView) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .displayer(new FadeInBitmapDisplayer(FADE_IN_DURATION))
                .cacheInMemory(true)
                .cacheOnDisk(false)
                .delayBeforeLoading(25)
                .imageScaleType(ImageScaleType.EXACTLY)
                .build();

        getInstance().displayImage(uri.toString(), imageView, options);
    }

    /**
     * Unfortunately getting the default is not straightforward..
     * See https://groups.google.com/forum/#!topic/android-developers/UkfP70MtjGA
     */
    private static Drawable getAssociatedAppIconDrawable(FileHolder holder, Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = FileUtils.getViewIntentFor(holder, context);
        Drawable icon = null;

        // Contrary to queryIntentActivities documentation, the first item IS NOT the same
        // as the one returned by resolveActivity.
        ResolveInfo resolveInfo = pm.resolveActivity(intent, MATCH_DEFAULT_ONLY);
        if (!FileUtils.isResolverActivity(resolveInfo)) {
            icon = resolveInfo.loadIcon(pm);
        } else {
            final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
                    MATCH_DEFAULT_ONLY);
            if (lri != null && lri.size() > 0) {
                icon = lri.get(0).loadIcon(pm);
            }
        }

        return icon;
    }

    private static final Drawable getApkIconDrawable(FileHolder holder, Context context) {
        PackageManager pm = context.getPackageManager();
        String path = holder.getFile().getPath();
        PackageInfo pInfo = pm.getPackageArchiveInfo(path,
                PackageManager.GET_ACTIVITIES);
        if (pInfo != null) {
            ApplicationInfo aInfo = pInfo.applicationInfo;

            // Bug in SDK versions >= 8. See here:
            // http://code.google.com/p/android/issues/detail?id=9151
            if (SDK_INT >= 8) {
                aInfo.sourceDir = path;
                aInfo.publicSourceDir = path;
            }

            return aInfo.loadIcon(pm);
        }

        return null;
    }

    private static String getPackageNameFromInfo(ResolveInfo ri) {
        if (ri.resolvePackageName == null) {
            if (ri.activityInfo != null) {
                return ri.activityInfo.packageName;
            } else if (ri.serviceInfo.packageName != null) {
                return ri.serviceInfo.packageName;
            } else if (SDK_INT >= KITKAT) {
                return ri.providerInfo.packageName;
            } else {
                return "";
            }
        } else {
            return ri.resolvePackageName;
        }
    }

    public static Drawable getBestPreviewForNonImage(FileHolder fHolder, Context context) {
        if (!Utils.isImage(fHolder.getMimeType())) {
            if (Utils.isAPK(fHolder.getMimeType())) {
                return getApkIconDrawable(fHolder, context);
            } else {
                return getAssociatedAppIconDrawable(fHolder, context);
            }
        } else {
            return null;
        }
    }
}
