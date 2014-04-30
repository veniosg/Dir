package com.veniosg.dir.misc;

import java.util.List;

public class DirectoryContents {
	public List<FileHolder> listDir;
    public List<FileHolder> listFile;
    public List<FileHolder> listSdCard;
    
    // If true, there's a ".nomedia" file in this directory.
    public boolean noMedia;
}
