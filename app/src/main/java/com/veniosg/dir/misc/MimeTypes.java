/* 
 * Copyright (C) 2008 OpenIntents.org
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

package com.veniosg.dir.misc;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.webkit.MimeTypeMap;

import com.veniosg.dir.R;
import com.veniosg.dir.util.FileUtils;
import com.veniosg.dir.util.Logger;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MimeTypes {

    private static final Integer[] sIconIds = new Integer[]{
            R.drawable.ic_item_file_tinted,
            R.drawable.ic_item_folder_tinted,
            R.drawable.ic_item_image_tinted,
            R.drawable.ic_item_audio_tinted,
            R.drawable.ic_item_video_tinted,
            R.drawable.ic_item_archive_tinted,
            R.drawable.ic_item_text_tinted,
            R.drawable.ic_item_text_web_tinted,
            R.drawable.ic_item_text_csv_tinted,
            R.drawable.ic_item_text_xml_tinted,
            R.drawable.ic_item_android_package_tinted,
            R.drawable.ic_item_sdcard_tinted
    };

	private Map<String, String> mMimeTypes = new HashMap<String,String>();
	private Map<String, Integer> mIconIndices = new HashMap<String,Integer>();     // Legacy layer
    private Context mContext;

    MimeTypes(Context context) {
        mContext = context.getApplicationContext();
    }

	/**
	 * DO NOT USE. See FileManagerApplication#getMimeTypes().
	 */
	public static MimeTypes newInstance(Context c){
		MimeTypes mimeTypes = null;
		MimeTypeParser mtp = null;
		try {
			mtp = new MimeTypeParser(c, c.getPackageName());
		} catch (NameNotFoundException e) {
			// Should never happen
		}

		XmlResourceParser in = c.getResources().getXml(R.xml.mimetypes);

		try {
			mimeTypes = mtp.fromXmlResource(c, in);
		} catch (XmlPullParserException e) {
            Logger.log(e);
		} catch (IOException e) {
            Logger.log(e);
		}
		
		return mimeTypes;
	}
	
	/* I think the type and extension names are switched (type contains .png, extension contains x/y),
	 * but maybe it's on purpose, so I won't change it.
	 */
	public void put(String type, String extension, int icon){
		put(type, extension);
		mIconIndices.put(extension, icon);
	}
	
	public void put(String type, String extension) {
		// Convert extensions to lower case letters for easier comparison
		extension = extension.toLowerCase();
		
		mMimeTypes.put(type, extension);
	}
	
	public String getMimeType(String filename) {
		String extension = FileUtils.getExtension(filename);
		
		// Let's check the official map first. Webkit has a nice extension-to-MIME map.
		// Be sure to remove the first character from the extension, which is the "." character.
		if (extension.length() > 0) {
			String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.substring(1));
		
			if (webkitMimeType != null) {
				// Found one. Let's take it!
				return webkitMimeType;
			}
		}
		
		// Convert extensions to lower case letters for easier comparison
		extension = extension.toLowerCase();
		
		String mimetype = mMimeTypes.get(extension);
		
		if(mimetype==null) mimetype = "*/*";
		
		return mimetype;
	}
	
	private int getIconIndex(String mimetype){
		Integer index = mIconIndices.get(mimetype);

        return index == null ? 0 : index;
	}

    public Drawable getIcon(Context c, String mimeType) {
        return c.getDrawable(sIconIds[getIconIndex(mimeType)]);
    }
}
