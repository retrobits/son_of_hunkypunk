/*
	Copyright © 2009-2010 Rafał Rzepecki <divided.mind@gmail.com>

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

import java.io.File;
import java.io.FilenameFilter;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class PreferencesActivity
        extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
        PreferenceCategory cat = (PreferenceCategory) findPreference("shortcuts");
        Preference pref;
        Set<String> keySet = sharedPreferences.getAll().keySet();
        ArrayList<Map.Entry<String, ?>> prefList = new ArrayList<Map.Entry<String, ?>>();
        for (Map.Entry<String, ?> map : sharedPreferences.getAll().entrySet()) {
            prefList.add(map);

        }
        for (String s : keySet) {
            if (!s.matches("#.*")) {
                pref = new Preference(getApplicationContext());
                pref.setKey(s);
                pref.setTitle(s);
                pref.setSummary(sharedPreferences.getString(s, ""));
                cat.addPreference(pref);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, final Preference preference) {
        if (preference.getKey().equals("#addshortcut")) {
            final View promptsView = LayoutInflater.from(this).inflate(R.layout.shortcut_preferences_prompt, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(promptsView);
            builder.setTitle("Type new shortcut");

            final EditText inputTitle = (EditText) promptsView.findViewById(R.id.editTitleDialogUserInput);
            final EditText inputCommand = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String title = inputTitle.getText().toString();
                    String command = inputCommand.getText().toString();

                    if (((CheckBox) promptsView.findViewById(R.id.autoenter)).isChecked())
                        command += "$";

                    Preference pref = new Preference(getApplicationContext());
                    pref.setTitle(title);
                    pref.setKey(title);
                    pref.setSummary(command);

                    PreferenceCategory cat = (PreferenceCategory) findPreference("shortcuts");
                    cat.addPreference(pref);
                    addShortcutToSharedPreferences(title, command);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setNeutralButton("Help", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        } else if (preference.getKey().matches("#restoredefaultshortcuts")) {
            restoreDefaultShortcuts();
        } else if (!preference.getKey().matches("#.*") && preferenceScreen.getKey().equals("manageshortcuts")) {
            final View promptsView = LayoutInflater.from(this).inflate(R.layout.shortcut_preferences_prompt, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(promptsView);
            builder.setTitle("Edit shortcut");

            final SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);

            final String title = preference.getTitle().toString();
            final String command = sharedPreferences.getString(title, "");

            final EditText inputTitle = (EditText) promptsView.findViewById(R.id.editTitleDialogUserInput);
            final EditText inputCommand = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

            CheckBox checkBox = (CheckBox) promptsView.findViewById(R.id.autoenter);
            if (command.endsWith("$"))
                checkBox.setChecked(true);

            inputTitle.setText(title);
            inputTitle.setSelection(0, inputTitle.getText().length());
            if (command.endsWith("$"))
                inputCommand.setText(command.substring(0, command.length() - 1));
            else
                inputCommand.setText(command);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String nTitle = inputTitle.getText().toString();
                    String ncommand = sharedPreferences.getString(nTitle, "");
                    if (((CheckBox) promptsView.findViewById(R.id.autoenter)).isChecked()) {
                        if (!ncommand.endsWith("$"))
                            ncommand += "$";
                        preference.setSummary(ncommand.substring(0, ncommand.length() - 1));
                    } else if (ncommand.endsWith("$")) {
                        ncommand = ncommand.substring(0, ncommand.length() - 1);
                        preference.setSummary(ncommand);
                    }
                    preference.setTitle(nTitle);
                    preference.setKey(nTitle);

                    editShortcutPreference(title, nTitle, ncommand);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.setNeutralButton("Help", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alert = builder.create();
            alert.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public void restoreDefaultShortcuts() {
        SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
        PreferenceCategory cat = (PreferenceCategory) findPreference("shortcuts");
        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (String s : sharedPreferences.getAll().keySet())
            if (!s.matches("#.*"))
                editor.remove(s);

        editor.commit();
        cat.removeAll();

        Preference pref;
        String[] defaults = new String[]{"look", "examine", "take", "inventory", "ask", "drop", "tell", "again", "open", "close", "give", "show"};
        for (int i = 0; i < defaults.length; i++) {
            pref = new Preference(getApplicationContext());
            pref.setKey(defaults[i]);
            pref.setTitle(defaults[i]);
            pref.setSummary(defaults[i]);
            cat.addPreference(pref);
            addShortcutToSharedPreferences(defaults[i], defaults[i]);
        }
    }

    public void addShortcutToSharedPreferences(String title, String command) {
        SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(title, command);
        editor.commit();
    }

    public void editShortcutPreference(String oldTitle, String newTitle, String command) {
        SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(oldTitle);
        editor.putString(newTitle, command);

        editor.commit();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setSummaryAll(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(null, "fontFolderPath");
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setSummaryAll(PreferenceScreen pScreen) {
        for (int i = 0; i < pScreen.getPreferenceCount(); i++) {
            Preference pref = pScreen.getPreference(i);
            setSummaryPref(pref);
        }
    }

    public void setSummaryPref(Preference pref) {
        if (pref == null) return;

        String key = pref.getKey();
        if (key == null) key = "";

        if (pref instanceof EditTextPreference) {
            EditTextPreference etPref = (EditTextPreference) pref;
            String desc = etPref.getText();
            pref.setSummary(desc);
        } else if (pref instanceof PreferenceCategory) {
            PreferenceCategory prefCat = (PreferenceCategory) pref;
            int count = prefCat.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                setSummaryPref(prefCat.getPreference(i));
            }
        } else if (pref instanceof ListPreference) {
            ListPreference lPref = (ListPreference) pref;
            String desc = lPref.getValue();
            pref.setSummary(desc);
        } else if (pref instanceof PreferenceScreen) {
            setSummaryAll((PreferenceScreen) pref);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences
                                                  sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (key.compareTo("fontFolderPath") == 0) {

            EditTextPreference prefFol = (EditTextPreference) pref;
            ListPreference prefFn = (ListPreference) findPreference("fontFileName");

            ArrayList<String> ff = new ArrayList<String>();
            ff.add("Droid Sans");
            ff.add("Droid Serif");
            ff.add("Droid Mono");

            File ffol = new File(prefFol.getText());
            if (ffol.exists()) {
                final File[] fileList = new File(prefFol.getText()).listFiles(
                        new FilenameFilter() {
                            public boolean accept(File dir, String name) {
                                if (name.startsWith(".")) {
                                    return false;
                                }
                                final String lcName = name.toLowerCase();
                                return lcName.endsWith(".ttf") || lcName.endsWith(".otf");
                            }
                        }
                );

                for (int i = 0; i < fileList.length; i++) {
                    ff.add(fileList[i].getName());
                }
            }
            String[] aff = (String[]) ff.toArray(new String[ff.size()]);

            String save = prefFn.getValue();
            prefFn.setValue("");
            prefFn.setEntries(aff);
            prefFn.setEntryValues(aff);
            if (ff.contains(save)) prefFn.setValue(save);

            setSummaryPref(prefFn);
        } else {
            setSummaryPref(pref);
        }
    }
}
