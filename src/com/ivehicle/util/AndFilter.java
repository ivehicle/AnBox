/*
 * AnBox, and an Android Blackbox application for the have-not-so-much-money's
 * Copyright (C) 2010 Yoonsoo Kim, Heekuk Lee, Heejin Sohn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
