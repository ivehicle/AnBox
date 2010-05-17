package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class FilenameFilterByShock implements FilenameFilter {

	public boolean accept(File dir, String fn) {
		String[] namesSplit = fn.split("\\.");
		if (new File(dir, namesSplit[0] + ".shk").length() > 0) {
			return true;
		}
		else {
			return false;
		}
	}

}
