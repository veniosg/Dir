package com.veniosg.dir.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.LoadedFrom;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.Logger;
import com.veniosg.dir.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Shader.TileMode.CLAMP;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.KITKAT;
import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;

public class ThumbnailHelper {
    private static final int FADE_IN_DURATION = 400;

    private static ImageDecoder sDecoder = null;
    private static DisplayImageOptions.Builder sDefaultImageOptionsBuilder;

    public static void requestIcon(FileHolder holder, ImageView imageView) {
        Uri uri = Uri.fromFile(holder.getFile());
        DisplayImageOptions options = defaultOptionsBuilder()
                .extraForDownloader(holder)
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
        } else if (!holder.getMimeType().equals("*/*")) {
            final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
                    MATCH_DEFAULT_ONLY);
            if (lri != null && lri.size() > 0) {
                // Again, contrary to documentation, best match is actually the last item.
                icon = lri.get(lri.size()-1).loadIcon(pm);
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

    public static ImageDecoder imageDecoder(final Context context) {
        if (sDecoder == null) {
            sDecoder = new ImageDecoder() {
                private BaseImageDecoder internal = new BaseImageDecoder(false);

                @Override
                public Bitmap decode(ImageDecodingInfo imageDecodingInfo) throws IOException {
                    FileHolder holder = (FileHolder) imageDecodingInfo.getExtraForDownloader();
                    Bitmap bitmap = null;

                    if (!holder.getFile().isDirectory()) {
                        if (Utils.isImage(holder.getMimeType())) {
                            try {
                                Bitmap bmp = internal.decode(imageDecodingInfo);
                                // Make bmp round
                                bitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), ARGB_8888);
                                int radius = bmp.getWidth()/2;
                                BitmapShader shader = new BitmapShader(bmp, CLAMP, CLAMP);
                                Canvas canvas = new Canvas(bitmap);
                                Paint paint = new Paint();
                                paint.setAntiAlias(true);
                                paint.setShader(shader);
                                canvas.drawCircle(radius, bmp.getHeight() / 2, radius, paint);

                                bmp.recycle();
                            } catch (FileNotFoundException ex) {
                                Logger.log(ex);
                                return null;
                                // Fail silently.
                            }
                        } else if (Utils.isAPK(holder.getMimeType())) {
                            Drawable drawable = getApkIconDrawable(holder, context);
                            if (drawable != null) {
                                bitmap = ((BitmapDrawable) drawable).getBitmap();
                            }
                        } else {
                            Drawable drawable = getAssociatedAppIconDrawable(holder, context);
                            if (drawable != null) {
                                bitmap = ((BitmapDrawable) drawable).getBitmap();
                            }
                        }
                    }

                    return bitmap;
                }
            };
        }

        return sDecoder;
    }

    private static DisplayImageOptions.Builder defaultOptionsBuilder() {
        if (sDefaultImageOptionsBuilder == null) {
            sDefaultImageOptionsBuilder = new DisplayImageOptions.Builder()
                    .displayer(new FadeInBitmapDisplayer(FADE_IN_DURATION))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .delayBeforeLoading(75)
                    .imageScaleType(ImageScaleType.EXACTLY);
        }

        return sDefaultImageOptionsBuilder;
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
}
