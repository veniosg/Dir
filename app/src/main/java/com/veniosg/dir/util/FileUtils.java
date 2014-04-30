/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package com.veniosg.dir.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Video;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import com.veniosg.dir.R;
import com.veniosg.dir.misc.FileHolder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

/**
 * @version 2009-07-03
 * 
 * @author Peli
 *
 */
public class FileUtils {
	private static final int X_OK = 1;
	public static final String NOMEDIA_FILE_NAME = ".nomedia";
	
	private static boolean libLoadSuccess;
	
	static {
		try {
			System.loadLibrary("access");
			libLoadSuccess = true;
		} catch(UnsatisfiedLinkError e) {
			libLoadSuccess = false;
            Logger.log(Log.DEBUG, "libaccess.so failed to load.");
		}
	}

	/**
	 * Whether the filename is a video file.
	 * 
	 * @param filename
	 * @return
	 *//*
	public static boolean isVideo(String filename) {
		String mimeType = getMimeType(filename);
		if (mimeType != null && mimeType.startsWith("video/")) {
			return true;
		} else {
			return false;
		}
	}*/

	/**
	 * Whether the URI is a local one.
	 * 
	 * @param uri The URI to check.
	 * @return If the URI is local.
	 */
	public static boolean isLocal(String uri) {
        return uri != null && !uri.startsWith("http://");
    }

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 * 
	 * @param path The file path or name
	 * @return Extension including the dot("."); "" if there is no extension;
	 *         null if uri was null.
	 */
	public static String getExtension(String path) {
		if (path == null) {
			return null;
		}

		int dot = path.lastIndexOf(".");
		if (dot >= 0) {
			return path.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * Check if uri is a media uri.
	 * 
	 * @param uri The URI to check.
	 * @return Whether it's an Android media URI.
	 */
	public static boolean isMediaUri(String uri) {
        return uri.startsWith(Audio.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Audio.Media.EXTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.INTERNAL_CONTENT_URI.toString())
                || uri.startsWith(Video.Media.EXTERNAL_CONTENT_URI.toString());
	}
	
	/**
	 * Convert File into Uri.
	 * @param file The file to convert.
	 * @return The Uri representing the file.
	 */
	public static Uri getUri(File file) {
		if (file != null) {
			return Uri.fromFile(file);
		}
		return null;
	}
	
	/**
	 * Convert Uri into File.
	 * @param uri Uri to convert.
	 * @return The file pointed to by the uri.
	 */
	public static File getFile(Uri uri) {
		if (uri != null) {
			String filepath = uri.getPath();
			if (filepath != null) {
				return new File(filepath);
			}
		}
		return null;
	}
	
	/**
	 * Returns the path only (without file name).
	 * @param file The file whose path to get.
	 * @return The first directory up from file. If file.isdirectory returns the file.
	 */
	public static File getPathWithoutFilename(File file) {
		 if (file != null) {
			 if (file.isDirectory()) {
				 // no file to be split off. Return everything
				 return file;
			 } else {
				 String filename = file.getName();
				 String filepath = file.getAbsolutePath();
	  
				 // Construct path without file name.
				 String pathwithoutname = filepath.substring(0, filepath.length() - filename.length());
				 if (pathwithoutname.endsWith("/")) {
					 pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length() - 1);
				 }
				 return new File(pathwithoutname);
			 }
		 }
		 return null;
	}

	public static String formatSize(Context context, long sizeInBytes) {
		return Formatter.formatFileSize(context, sizeInBytes);
	}

	public static long folderSize(File directory) {
		long length = 0;
		File[] files = directory.listFiles();
		if(files != null)
			for (File file : files)
				if (file.isFile())
					length += file.length();
				else
					length += folderSize(file);
		return length;
	}
	
	public static String formatDate(Context context, long dateTime) {
		return DateFormat.getDateFormat(context).format(new Date(dateTime));
	}

    /**
     * @param f File which needs to be checked.
     * @return True if the file is a zip archive.
     */
    public static boolean checkIfZipArchive(File f){
        // Hacky but fast enough
        return f.isFile() && FileUtils.getExtension(f.getAbsolutePath()).equals(".zip");
    }

    /**
     * Recursively count all files in the <code>file</code>'s subtree.
     * @param file The root of the tree to count.
     */
    public static int getFileCount(File file){
        int fileCount = 0;
        if (!file.isDirectory()){
            fileCount++;
        } else {
            if(file.list() != null) {
                for (File f : file.listFiles()) {
                    fileCount += getFileCount(f);
                }
            }
        }

        return fileCount;
    }

    public static int getFileCount(List<FileHolder> list) {
        int fileCount = 0;
        for (FileHolder fh : list) {
            fileCount += FileUtils.getFileCount(fh.getFile());
        }

        return fileCount;
    }
	
	/**
	 * Native helper method, returns whether the current process has execute privilages.
	 * @param mContextFile File
	 * @return returns TRUE if the current process has execute privilages.
	 */
	public static boolean canExecute(File mContextFile) {
		try {
			// File.canExecute() was introduced in API 9.  If it doesn't exist, then
			// this will throw an exception and the NDK version will be used.
			Method m = File.class.getMethod("canExecute", new Class[] {} );
			return (Boolean) m.invoke(mContextFile);
		} catch (Exception e) {
            return libLoadSuccess && access(mContextFile.getPath(), X_OK);
		}
	}
	
	// Native interface to unistd.h's access(*char, int) method.
	public static native boolean access(String path, int mode);
	
	/**
	 * @param path The path that the file is supposed to be in.
	 * @param fileName Desired file name. This name will be modified to create a unique file if necessary.
	 * @return A file name that is guaranteed to not exist yet. MAY RETURN NULL!
	 */
	public static File createUniqueCopyName(Context context, File path, String fileName) {
		// Does that file exist?
		File file = new File(path, fileName);
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		// Split file's name and extension to fix internationalization issue #307
		int fromIndex = fileName.lastIndexOf('.');
		String extension = "";
		if (fromIndex > 0) {
			extension = fileName.substring(fromIndex);
			fileName = fileName.substring(0, fromIndex);
		}
		
		// Try a simple "copy of".
		file = new File(path, context.getString(R.string.copied_file_name, fileName).concat(extension));
		
		if (!file.exists()) {
			// Nope - we can take that.
			return file;
		}
		
		int copyIndex = 2;
		
		// Well, we gotta find a unique name at some point.
		while (copyIndex < 500) {
			file = new File(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName).concat(extension));
			
			if (!file.exists()) {
				// Nope - we can take that.
				return file;
			}

			copyIndex++;
		}
	
		// I GIVE UP.
		return null;
	}	
	
	/**
	 * Attempts to open a file for viewing.
	 * 
	 * @param fileholder The holder of the file to open.
	 */
	public static void openFile(FileHolder fileholder, Context c) {
		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

		Uri data = FileUtils.getUri(fileholder.getFile());
		String type = fileholder.getMimeType();
		
        intent.setDataAndType(data, type);

		
        launchFileIntent(intent, c);
	}

    private static void launchFileIntent(Intent intent, Context c) {
        try {
            List<ResolveInfo> activities = c.getPackageManager().queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
            if (activities.size() == 0
                    || ((activities.size() == 1
                    && c.getApplicationInfo().packageName
                    .equals(activities.get(0).activityInfo.packageName)))) {
                Toast.makeText(c.getApplicationContext(), R.string.application_not_available, Toast.LENGTH_SHORT).show();
            } else {
                c.startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(c.getApplicationContext(), R.string.application_not_available, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e){
            Toast.makeText(c.getApplicationContext(), R.string.application_not_available, Toast.LENGTH_SHORT).show();
        }
    }

    public static String getNameWithoutExtension(File f) {
		return f.getName().substring(0, f.getName().length() - getExtension(getUri(f).toString()).length());
	}

    public static void deleteDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteDirectory(child);

        fileOrDirectory.delete();
    }
}
