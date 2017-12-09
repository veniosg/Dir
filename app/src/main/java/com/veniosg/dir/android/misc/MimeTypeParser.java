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

package com.veniosg.dir.android.misc;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class MimeTypeParser {
	private static final String TAG_MIMETYPES = "MimeTypes";
	private static final String TAG_TYPE = "type";
	
	private static final String ATTR_EXTENSION = "extension";
	private static final String ATTR_MIMETYPE = "mimetype";
	private static final String ATTR_ICON = "icon";
	
	private XmlPullParser mXpp;
	private MimeTypes mMimeTypes;
	private Resources resources;
	private String packagename;
    
	public MimeTypeParser(Context ctx, String packagename) throws NameNotFoundException{
		this.packagename = packagename;
		resources = ctx.getPackageManager().getResourcesForApplication(packagename);
	}

 	protected MimeTypes fromXmlResource(Context context, XmlResourceParser in)
	throws XmlPullParserException, IOException {
		mXpp = in;
		
		return parse(context);
	}

	MimeTypes parse(Context context)
			throws XmlPullParserException, IOException {
		
		mMimeTypes = new MimeTypes(context);
		
		int eventType = mXpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = mXpp.getName();

			if (eventType == XmlPullParser.START_TAG) {
				if (tag.equals(TAG_MIMETYPES)) {
					
				} else if (tag.equals(TAG_TYPE)) {
					addMimeTypeStart();
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (tag.equals(TAG_MIMETYPES)) {
					
				}
			}

			eventType = mXpp.next();
		}

		return mMimeTypes;
	}
	
	private void addMimeTypeStart() {
		String extension = mXpp.getAttributeValue(null, ATTR_EXTENSION);
		String mimetype = mXpp.getAttributeValue(null, ATTR_MIMETYPE);
		String icon = mXpp.getAttributeValue(null, ATTR_ICON);
		
		if(icon == null){
            mMimeTypes.put(extension, mimetype, 0);
		} else {
            mMimeTypes.put(extension, mimetype, Integer.valueOf(icon));
        }
	}
	
}
