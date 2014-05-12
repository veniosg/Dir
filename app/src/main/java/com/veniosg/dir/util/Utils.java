package com.veniosg.dir.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcelable;
import android.widget.Toast;

import com.veniosg.dir.R;
import com.veniosg.dir.activity.FileManagerActivity;
import com.veniosg.dir.misc.FileHolder;
import com.veniosg.dir.misc.MimeTypes;
import com.veniosg.dir.provider.FileManagerProvider;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author George Venios
 */
public abstract class Utils {
    private Utils() {
    }

    /**
     *
     * @param root Folder to start search from.
     * @param filter The namefilter to use for matching.
     * @param mimeTypes Initialized MimeTypes instance.
     * @param context A context.
     * @param recursive Whether to recursively follow the paths.
     * @param maxLevel How many levels below the current we're allowed to recurse.
     * @return A list of fileholders found in root and matching filter.
     */
    public static List<FileHolder> searchIn(File root, FilenameFilter filter, MimeTypes mimeTypes, Context context, boolean recursive, int maxLevel) {
        ArrayList<FileHolder> result = new ArrayList<FileHolder>(10);

        for (File f : root.listFiles(filter)) {
            String mimeType = mimeTypes.getMimeType(f.getName());

            result.add(new FileHolder(f, mimeType,
                    getIconForFile(mimeTypes, mimeType, f, context)));
        }

        if (recursive && (maxLevel-- != 0)) {
            for (File f : root.listFiles()) {
                // Prevent trying to search invalid folders
                if (f.isDirectory() && f.canRead()) {
                    result.addAll(searchIn(f, filter, mimeTypes, context, true, maxLevel));
                }
            }
        }

        return result;
    }

    public static List<FileHolder> searchIn(File root, final String query, MimeTypes mimeTypes, Context context) {
        return searchIn(root, newFilter(query), mimeTypes, context, false, 0);
    }

    public static FilenameFilter newFilter(final String query) {
        return new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return query != null && filename.toLowerCase().contains(query.toLowerCase());
            }
        };
    }

    public static String getLastPathSegment(String path) {
        String[] segments = path.split("/");

        if (segments.length > 0)
            return segments[segments.length-1];
        else
            return "";
    }

    /**
     * Launch the home activity.
     * @param act The currently displayed activity.
     */
    public static void showHome(Activity act) {
        Intent intent = new Intent(act, FileManagerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivity(intent);
    }

    /**
     * Creates a home screen shortcut.
     * @param fileholder The {@link File} to create the shortcut to.
     */
    public static void createShortcut(FileHolder fileholder, Context context) {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, fileholder.getName());
        try {
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON, ((BitmapDrawable) fileholder
                    .getBestIcon()).getBitmap());
        } catch (Exception ex) {
            Logger.log(ex);
            Parcelable icon = Intent.ShortcutIconResource.fromContext(
                    context.getApplicationContext(), R.drawable.ic_launcher);
            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        } finally {
            // Intent to load
            Intent itl = new Intent(Intent.ACTION_VIEW);
            if(fileholder.getFile().isDirectory())
                itl.setData(Uri.fromFile(fileholder.getFile()));
            else
                itl.setDataAndType(Uri.fromFile(fileholder.getFile()), fileholder.getMimeType());
            itl.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

            shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, itl);
            context.sendBroadcast(shortcutintent);
        }
    }

    /**
     * Creates an activity picker to send a file.
     * @param fHolder A {@link FileHolder} containing the {@link File} to send.
     * @param context {@link Context} in which to create the picker.
     */
    public static void sendFile(FileHolder fHolder, Context context) {
        String filename = fHolder.getName();

        Intent i = new Intent();
        i.setAction(Intent.ACTION_SEND);
        i.setType(fHolder.getMimeType());
        i.putExtra(Intent.EXTRA_SUBJECT, filename);
        i.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri(fHolder.getFile()));
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + FileManagerProvider.AUTHORITY + fHolder.getFile().getAbsolutePath()));

        i = Intent.createChooser(i, context.getString(R.string.send_chooser_title));

        try {
            context.startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, R.string.send_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Resizes specific a Bitmap with keeping ratio.
     */
    public static Bitmap resizeBitmap(Bitmap drawable, int desireWidth,
                                      int desireHeight) {
        int width = drawable.getWidth();
        int height = drawable.getHeight();

        if (0 < width && 0 < height && desireWidth < width
                || desireHeight < height) {
            // Calculate scale
            float scale;
            if (width < height) {
                scale = (float) desireHeight / (float) height;
                if (desireWidth < width * scale) {
                    scale = (float) desireWidth / (float) width;
                }
            } else {
                scale = (float) desireWidth / (float) width;
            }

            // Draw resized image
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap bitmap = Bitmap.createBitmap(drawable, 0, 0, width, height,
                    matrix, true);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawBitmap(bitmap, 0, 0, null);

            drawable = bitmap;
        }

        return drawable;
    }

    /**
     * Resizes specific a Drawable with keeping ratio.
     */
    public static Drawable resizeDrawable(Resources res, Drawable drawable, int desireWidth,
                                          int desireHeight) {
        Drawable dr = drawable;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();

        if (0 < width && 0 < height &&
                (desireWidth < width
                        || desireHeight < height)) {
            Bitmap b = ((BitmapDrawable) drawable).getBitmap();
            Bitmap resized = Bitmap.createScaledBitmap(b, desireWidth,
                    desireHeight, true);
            dr = new BitmapDrawable(res, resized);
        }

        return dr;
    }

    public static Drawable getIconForFile(MimeTypes mimeTypes, String mimeType, File file, Context context) {
        return context.getResources().getDrawable(getIconResourceForFile(mimeTypes, mimeType, file));
    }

    public static int getIconResourceForFile(MimeTypes mimeTypes, String mimeType, File file) {
        int iconRes = mimeTypes.getIcon(mimeType);
        if (iconRes == 0) {
            iconRes = file.isDirectory() ? R.drawable.ic_item_folder : R.drawable.ic_item_file;
        }

        return iconRes;
    }

    /**
     * Get the directory index where two files are intersected last.
     * Effectively the last common directory in two files' paths. <br/>
     * This method seems kind of silly, if anyone has a more elegant solution, please lmk.
     * @param file1
     * @param file2
     * @return
     */
    public static int lastCommonDirectoryIndex(File file1, File file2) {
        String[] parts1 = file1.getAbsolutePath().split("/");
        String[] parts2 = file2.getAbsolutePath().split("/");
        int index = 0;

        for (String part1 : parts1) {
            if (parts2.length <= index) {
                // Reached the end of the second input.
                // The first input was fully contained in it.
                if(index > 0)
                    // If the index has been incremented, it is now
                    // outside the second path's bounds.
                    index--;
                // If it was 0, it meant that the second input was just the root.
                break;
            } else if (!part1.equals(parts2[index])) {
                // Found a difference between the two paths.
                // Last common directory was the previous one.
                index--;
                break;
            } else if (!parts1[parts1.length-1].equals(part1)) {
                // The end of the first path has not been reached.
                index++;
            }
        }

        return index;
    }

    /**
     *
     * @param file1
     * @param file2
     * @return 1 if file1 is above file2, -1 otherwise. 0 on errors
     */
    public static int getNavigationDirection(File file1, File file2) {
        if (file1 == null || file2 == null
                || file1.getAbsolutePath().equals("") || file2.getAbsolutePath().equals("")
                || file1.getAbsolutePath().equals(file2.getAbsolutePath()))
            return 0;

        int result = -1;
        if (file2.getAbsolutePath().startsWith(file1.getAbsolutePath())) {
            result = 1;
        }
        return result;
    }

    /**
     *
     * @param mat The matrix to set this transformation on.
     * @param b The darkness value. 1 means totally black, 0 means original color.
     */
    public static void setDrawableDarkness(ColorMatrix mat, float b) {
        b = 1-b;
        mat.set(new float[]{
                b, 0, 0, 0, 0,
                0, b, 0, 0, 0,
                0, 0, b, 0, 0,
                0, 0, 0, 1, 0});
    }
}
