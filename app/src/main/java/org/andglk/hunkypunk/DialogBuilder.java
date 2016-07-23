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
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.widget.TextView;

public class DialogBuilder {
    public static AlertDialog showAboutDialog(Context context) throws NameNotFoundException {
        PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA
        );

        String versionInfo = pInfo.versionName;

        String aboutTitle = String.format("About %s", context.getString(R.string.hunky_punk));
        String versionString = String.format("Version: %s", versionInfo);

        String aboutText = versionString + "\n\nBy Dan Vernon\n\n";

        aboutText += "(based on the original Hunky Punk by Rafał Rzepecki)\n\n";

        aboutText += "Improvements include:\n";
        aboutText += "  * Tads support (Tads 2.5.14, 3.0.18) \n";
        aboutText += "  * Better Z-code support (Frotz 2.50) \n";
        aboutText += "  * Swype, voice, 3rd party keyboards\n";
        aboutText += "  * Fling scrollback\n";
        aboutText += "  * Font size preference\n";
        aboutText += "  * Blorb support\n";
        aboutText += "  * Stability\n\n";

        aboutText += "Help topics can be found here:\n";
        aboutText += "http://code.google.com/p/hunkypunk/wiki/Introduction\n\n";

        aboutText += "Please report issues & requests here:\n";
        aboutText += "http://code.google.com/p/hunkypunk/issues\n\n\n";

		/*JPDOB Team*/

        aboutText += "This version has been updated by the JPDOB Team, ";
        aboutText += "students from the University of Constance, Germany.\n\n";
        aboutText += "Improvements include:\n";
        aboutText += "  * Shortcut command buttons\n";
        aboutText += "  * Long Touch to Copy Words\n";
        aboutText += "  * Selectable IFs File Path\n";
        aboutText += "  * Swipe through games\n";
        aboutText += "  * Night Mode \n";
        aboutText += "  * Fonts support\n";
        aboutText += "  * TADS actualization\n";
        aboutText += "  * Improved preferences layout\n";
        aboutText += "  * Bug fixes\n\n";


        aboutText += "Issues and project wiki here:\n";
        aboutText += "https://github.com/BeniEnge/as_son_of_hunkypunk\n\n";

        final SpannableString s = new SpannableString(aboutText);
        Linkify.addLinks(s, Linkify.WEB_URLS);

        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(context.getString(android.R.string.ok), null)
                .setIcon(R.drawable.icon)
                .setMessage(s)
                .setCancelable(true)
                .setTitle(aboutTitle).create();

        d.show();

        TextView tv = ((TextView) d.findViewById(android.R.id.message));
        TextView tvDefault = new TextView(context);

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvDefault.getTextSize());

        return d;
    }


    public static AlertDialog showShortcutHelpDialog(Context context) {
        String aboutTitle = "Shortcuts Help";

        String aboutText = "\n";
        aboutText += " * It is important that your commands\n" + "    have the right syntax. \n\n";//1.2
        aboutText += " * You can use normal commands or\n" + "    commands with insertion points. \n\n";//1.3
        aboutText += " * The insertion point (<%>) \n" + "    is a placeholder which will be\n" + "    replaced by your copied word.\n\n";//1.4
        aboutText += " * Insertion points are only useful,\n" + "    if copy is activated.\n\n";//1.5
        aboutText += " * If you have used auto enter,\n" + "    the command would be confirmed\n" + "    with enter.\n\n";//1.6
        aboutText += " * If you have used auto enter and\n" + "    insertion point, the command will be\n" + "    confirmed with enter when all\n" + "    insertion points are replaced\n" + "    by a copied word.\n\n";//1.7
        aboutText += " * If you copy a word and there isn't a\n" + "    insertion point, the copied word will\n" + "    written to the end of the input line.\n\n\n";
        aboutText += "Command with/without auto enter: \n";//2.1
        aboutText += " * Type in the command line a valid\n" + "    command. \n\n";//2.2
        aboutText += " * It's not allowed to seperate the\n" + "    letters of one command with an space.\n\n";//2.3
        aboutText += " * Example: wait is a valid command,\n" + "    w ait is not allowed. \n\n\n";
        aboutText += "Command with insertion point:\n";//3.1
        aboutText += " * Type in the command line a valid\n" + "    command which action is on an object. \n\n";//3.2
        aboutText += " * Type space and a copy\n" + "    placeholder (<%>). \n\n";//3.3
        aboutText += " * Example: examine <%> \n\n";//3.4
        aboutText += " * Now if you copy a word the copy\n" + "    placeholder will be replaced\n" + "    by the word.\n\n";//3.5
        aboutText += " * It's also possible to use multiple\n" + "    insertion points: \n\n";//3.6
        aboutText += " * Example: ask <%> about <%> \n\n";//3.7
        aboutText += " * Now if you copy a word the first\n" + "    insertion point will be replaced,\n" + "    the second copied word will\n" + "    replace the second insertion point.";

        final SpannableString s = new SpannableString(aboutText);
        Linkify.addLinks(s, Linkify.WEB_URLS);

        AlertDialog d = new AlertDialog.Builder(context)
                .setPositiveButton(context.getString(android.R.string.ok), null)
                .setIcon(R.drawable.icon)
                .setMessage(s)
                .setCancelable(true)
                .setTitle(aboutTitle).create();

        d.show();

        TextView tv = ((TextView) d.findViewById(android.R.id.message));
        TextView tvDefault = new TextView(context);

        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tvDefault.getTextSize());

        return d;
    }
}