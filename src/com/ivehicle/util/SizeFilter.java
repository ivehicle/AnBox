package com.ivehicle.util;

import java.io.File;
import java.io.FilenameFilter;

public class SizeFilter implements FilenameFilter {

	public final static int OP_EQUAL = 0;
	public final static int OP_GREATER_THAN = 1;
	public final static int OP_LESS_THAN = 2;
	public final static int OP_GREATER_THAN_OR_EQUAL = 3;
	public final static int OP_LESS_THAN_OR_EQUAL = 4;
	public final static int OP_NOT_EQUAL = 5;

	private int op = OP_EQUAL;
	private long sz = 0;

	public SizeFilter(int operation, long size) {
		this.op = operation;
		this.sz = size;
	}

	public boolean accept(File dir, String fn) {
		long fileSize = new File(dir, fn).length();
		switch (op) {
		case OP_EQUAL:
			return (fileSize == sz);

		case OP_GREATER_THAN:
			return (fileSize > sz);

		case OP_LESS_THAN:
			return (fileSize < sz);

		case OP_GREATER_THAN_OR_EQUAL:
			return (fileSize >= sz);

		case OP_LESS_THAN_OR_EQUAL:
			return (fileSize < sz);

		case OP_NOT_EQUAL:
			return (fileSize != sz);
		}

		return false;
	}

}
