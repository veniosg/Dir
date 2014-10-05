package com.veniosg.dir.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.veniosg.dir.fragment.PreferenceFragment;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.Logger;
import com.veniosg.dir.util.Utils;

import java.util.List;

import static android.content.Intent.ACTION_VIEW;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;

public class ThumbnailRequestHelper {
    public static void loadIconWithForInto(Context context, FileHolder holder, ImageView imageView) {
        Uri uri;
        String mime = holder.getMimeType();

        if (holder.getFile().isDirectory()) {
            return;
        }

        if (Utils.isAPK(mime)) {
            uri = getApkIconUri(holder, context);
        } else if (Utils.isImage(mime)) {
            uri = Uri.fromFile(holder.getFile());
        } else {
            uri = getAssociatedAppIconUri(holder, context);
        }

        Picasso.with(context)
                .load(uri)
                .placeholder(holder.getBestIcon())
                .fit()
                .centerCrop()
                .into(imageView);
    }

    private static Uri getAssociatedAppIconUri(FileHolder holder, Context context) {
        // Let's probe the intent exactly in the same way as the VIEW action
        // is performed in FileUtils.openFile(..)
        PackageManager pm = context.getPackageManager();
        Uri data = FileUtils.getUri(holder.getFile());
        Intent intent = new Intent(ACTION_VIEW);

        intent.setDataAndType(data, holder.getMimeType());

        final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (lri != null && lri.size() > 0) {
            // Actually first element should be "best match",
            // but it seems that more recently installed applications
            // could be even better match.
            boolean useBestMatch = PreferenceFragment.getUseBestMatch(context);
            int index = (useBestMatch ? 0 : lri.size() - 1);
            final ResolveInfo ri = lri.get(index);

            return Uri.parse("android.resource://" + getPackageNameFromInfo(ri) + "/" + ri.getIconResource());
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

    private static final Uri getApkIconUri(FileHolder holder, Context context) {
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

            return Uri.parse("android.resource://" + aInfo.packageName + "/" + aInfo.icon);
        }

        return null;
    }
}
