package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterExcludingExtension implements FilenameFilter {

	private FilenameFilterByExtension filt = null;
	
	public FilenameFilterExcludingExtension(String ext) {
		filt = new FilenameFilterByExtension(ext);
	}	

	public boolean accept(File dir, String fn) {
		return !(filt.accept(dir, fn));
	}

}
