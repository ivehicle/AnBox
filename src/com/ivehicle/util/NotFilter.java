package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class NotFilter implements FilenameFilter {

	FilenameFilter filt = null;

	public NotFilter(FilenameFilter filter) {
		filt = filter;
	}

	public boolean accept(File dir, String fn) {
		return !filt.accept(dir, fn);
	}

}
