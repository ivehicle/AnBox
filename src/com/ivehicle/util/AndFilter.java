package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class AndFilter implements FilenameFilter {

	FilenameFilter filt1 = null;
	FilenameFilter filt2 = null;

	public AndFilter(FilenameFilter lhs, FilenameFilter rhs) {
		filt1 = lhs;
		filt2 = rhs;
	}

	public boolean accept(File dir, String fn) {
		return (filt1.accept(dir, fn) && filt2.accept(dir, fn));
	}

}
