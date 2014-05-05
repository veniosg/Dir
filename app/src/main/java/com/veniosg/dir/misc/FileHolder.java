package com.veniosg.dir.misc;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateUtils;
import android.text.format.Formatter;

import com.veniosg.dir.FileManagerApplication;
import com.veniosg.dir.R;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.Utils;

import java.io.File;

public class FileHolder implements Parcelable, Comparable<FileHolder> {
	private File mFile;
	private Drawable mIcon;
	private String mMimeType = "";
	private Context mContext;
	private String mExtension;
	
	public FileHolder(File f, Context c){
		mFile = f;
		mExtension = parseExtension();
        MimeTypes mimeTypes = ((FileManagerApplication) c.getApplicationContext()).getMimeTypes();
		mMimeType = mimeTypes.getMimeType(f.getName());
        mIcon = Utils.getIconForFile(mimeTypes, mMimeType, mFile, c);
		mContext = c;
	}

    public FileHolder(File f, Drawable i, Context c){
		mFile = f;
		mIcon = i;
		mExtension = parseExtension();
        MimeTypes mimeTypes = ((FileManagerApplication) c.getApplicationContext()).getMimeTypes();
        mMimeType = mimeTypes.getMimeType(f.getName());
        mIcon = Utils.getIconForFile(mimeTypes, mMimeType, mFile, c);
		mContext = c;
	}

	public FileHolder(File f, String m, Context c) {
		mFile = f;
		mExtension = parseExtension();
		mMimeType = m;
		mContext = c;
        MimeTypes mimeTypes = ((FileManagerApplication) c.getApplicationContext()).getMimeTypes();
        mIcon = Utils.getIconForFile(mimeTypes, mMimeType, mFile, c);
	}
	
	/**
	 * Fastest constructor as it takes everything ready.
	 */
	public FileHolder(File f, String m, Drawable i, Context c){
		mFile = f;
		mIcon = i;
		mExtension = parseExtension();
		mMimeType = m;
		mContext = c;
	}
	
	private FileHolder(Parcel in){
		mFile = new File(in.readString());
		mMimeType = in.readString();
		mExtension = in.readString();
	}

	public File getFile(){
		return mFile;
	}
	
	/**
	 * Gets the icon representation of this file.
	 * @return The icon.
	 */
	public Drawable getIcon(){
		return mIcon;
	}

	public void setIcon(Drawable icon) {
		mIcon = icon;
	}
	
	/**
	 * Shorthand for getFile().getName().
	 * @return This file's name. 
	 */
	public String getName(){
		return mFile.getName();
	}
	
	/**
	 * Get the contained file's extension.
	 */
	public String getExtension() {
		return mExtension;
	}
	
	/**
	 * @return The held item's mime type.
	 */
	public String getMimeType() {
		return mMimeType;
	}
	
	public CharSequence getFormattedModificationDate(){
        // Catch weird exception reported on Play store.
        if (mFile == null) {
            return "";
        } else {
            return mContext.getString(R.string.modified) + " " +
                    DateUtils.getRelativeDateTimeString(mContext, mFile.lastModified(),
                            DateUtils.MINUTE_IN_MILLIS, DateUtils.YEAR_IN_MILLIS * 10, 0);
        }
	}
	
	/**
	 * @param recursive Whether to return size of the whole tree below this file (Directories only).
	 */
	public String getFormattedSize(Context c, boolean recursive){
		return Formatter.formatFileSize(c, getSizeInBytes(recursive));
	}
	
	private long getSizeInBytes(boolean recursive){
		if (recursive && mFile.isDirectory())
			return FileUtils.folderSize(mFile);
		else
			return mFile.length();
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(mFile.getAbsolutePath());
		dest.writeString(mMimeType);
		dest.writeString(mExtension);
	}
	
    public static final Parcelable.Creator<FileHolder> CREATOR = new Parcelable.Creator<FileHolder>() {
        public FileHolder createFromParcel(Parcel in) {
            return new FileHolder(in);
        }
 
        public FileHolder[] newArray(int size) {
            return new FileHolder[size];
        }
    };

	@Override
	public int compareTo(FileHolder another) {
		return mFile.compareTo(another.getFile());
	}
	
	/**
	 * Parse the extension from the filename of the mFile member.
	 */  
	private String parseExtension() {
	    String ext = "";
	    String name = mFile.getName();
	    
	    int i = name.lastIndexOf('.');

	    if (i > 0 &&  i < name.length() - 1) {
	        ext = name.substring(i+1).toLowerCase();
	    }
	    return ext;
	}
}