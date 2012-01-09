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

package org.andglk.hunkypunk;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class AboutDialogBuilder {
	public static AlertDialog create( Context context ) throws NameNotFoundException {
		PackageInfo pInfo = context.getPackageManager().getPackageInfo(
			context.getPackageName(), 
			PackageManager.GET_META_DATA
		);

		String versionInfo = pInfo.versionName;

		String aboutTitle = String.format("About %s", context.getString(R.string.app_name));
		String versionString = String.format("Version: %s", versionInfo);
		String aboutText = "By Dan Vernon\n\n";
		aboutText += "(cloned from the original Hunky Punk by Rafał Rzepecki)\n\n";
		aboutText += "Improvements include:\n";
		aboutText += "  * Tads support (Tads 2.5.14, 3.0.18) \n";
		aboutText += "  * improved Z-code support (Frotz 2.50) \n";
		aboutText += "  * blorb support\n";
		aboutText += "  * improved stability & misc bug fixes\n\n";
		aboutText += "Please report issues and requests at\n";
		aboutText += "http://code.google.com/p/hunkypunk/issues";

		final TextView message = new TextView(context);
		final SpannableString s = new SpannableString(aboutText);

		message.setPadding(5, 5, 5, 5);
		message.setText(versionString + "\n\n" + s);
		Linkify.addLinks(message, Linkify.ALL);

		return new AlertDialog.Builder(context)
			.setTitle(aboutTitle)
			.setCancelable(true)
			.setIcon(R.drawable.icon)
			.setPositiveButton(context.getString(android.R.string.ok), null)
			.setView(message).create();
	}
}