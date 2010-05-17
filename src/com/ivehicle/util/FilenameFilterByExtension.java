package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterByExtension implements FilenameFilter {

	private String ext_;
	
	public FilenameFilterByExtension(String ext) {
		ext_ = ext;
	}
	
	public boolean accept(File dir, String fn) {
		return (fn.indexOf("." + ext_) >= 0);
	}

}
