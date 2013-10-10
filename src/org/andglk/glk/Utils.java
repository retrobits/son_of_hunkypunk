/*
  Copyright © 2009 Rafał Rzepecki <divided.mind@gmail.com>

  This file is part of Hunky Punk.

  Hunky Punk is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Hunky Punk is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Hunky Punk.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.andglk.glk;

import android.text.Editable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

	public static void copyStream(InputStream is, OutputStream os) throws IOException {
		int count;
		byte[] buf = new byte[32768];
		while ((count = is.read(buf)) != -1)
			os.write(buf, 0, count);
	}

	public static String getFileExtension(File f) {
		String fullPath = f.getAbsolutePath();
		String ext = "";
		int dot = fullPath.lastIndexOf(".");
		if (dot > -1) ext = fullPath.substring(dot+1);
		return ext;
	}
	public static String getFileNameNoExtension(File f) {
		String fullPath = f.getAbsolutePath();
		int dot = fullPath.lastIndexOf(".");
		int slash = fullPath.lastIndexOf("/");
		if (dot == -1 && slash == -1)
			return fullPath;
		else if (slash > -1 && dot == -1)
			return fullPath.substring(slash+1);
		else if (slash == -1 && dot > -1)
			return fullPath.substring(0,dot);
		else
			return fullPath.substring(slash+1,dot);
	}

	public static void beautify(Editable e, int position) {
		int len = e.length();
			
		final int NOTHING = 0;
		final int SPACE = 1;
		final int DASH = 2;
		final int SPACEQUOTE = 3;
		final int NDASH = 4;
		final int DOT = 5;
		final int DOUBLEDOT = 6;
			
		int state = NOTHING;
			
		if (position < 0)
			position = 0;
		if (len == position)
			return;
			
		do {
			final char c = e.charAt(position); 
			switch (state) {
				
			case NOTHING:
				switch (c) {
				case ' ':
				case '\n':
					state = SPACE;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
					continue;
				case '-':
					state = DASH;
					continue;
				case '.':
					state = DOT;
					continue;
				default:
					continue;
				}
					
			case SPACE:
				switch (c) {
				case ' ':
				case '\n':
					continue;
				case '"':
					state = SPACEQUOTE;
					continue;
				case '-':
					state = DASH;
					continue;
				case '.':
					state = DOT;
					continue;
				default:
					state = NOTHING;
					continue;
				}

			case DASH:
				switch (c) {
				case ' ':
				case '\n':
					state = SPACE;
					continue;
				case '-':
					state = NDASH;
					continue;
				case '.':
					state = DOT;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
				default:
					state = NOTHING;
					continue;
				}
					
			case SPACEQUOTE:
				switch (c) {
				case ' ':
				case '\n':
					state = SPACE;
					continue;
				case '-':
					e.replace(position - 1, position, "“");
					state = DASH;
					continue;
				case '.':
					e.replace(position - 1, position, "“");
					state = DOT;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
				default:
					e.replace(position - 1, position, "“");
					state = NOTHING;
					continue;
				}
					
			case NDASH:
				switch (c) {
				case '-':
					e.replace(position - 2, position + 1, "—");
					position -= 2;
					len -= 2;
					state = NOTHING;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
				default:
					state = NOTHING;
					break;
				case '.':
					state = DOT;
					break;
				case ' ':
				case '\n':
					state = SPACE;
					break;
				}
				e.replace(position - 2, position, "–");
				position--;
				len--;
				continue;
				
			case DOT:
				switch (c) {
				case '.':
					state = DOUBLEDOT;
					continue;
				case ' ':
				case '\n':
					state = SPACE;
					continue;
				case '-':
					state = DASH;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
				default:
					state = NOTHING;
					continue;
				}

			case DOUBLEDOT:
				switch (c) {
				case ' ':
				case '\n':
					state = SPACE;
					continue;
				case '-':
					state = DASH;
					continue;
				case '"':
					e.replace(position, position + 1, "”");
					break;
				case '.':
					e.replace(position - 2, position + 1, "…");
					position -= 2;
					len -= 2;
					break;
				default:
				}
				state = NOTHING;
				continue;
			}
		} while (++position < len);
	}		
}
