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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

public class PreferencesActivity
        extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String[] extension = new String[]{".z1", ".z2", ".z3", ".z4", ".z5", ".z6", ".z7", ".z8", ".zblorb", ".zlb", ".t2", ".t3", ".gam"};

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        Preference apref = findPreference("setIFDir");
        if (apref != null) {
            apref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    DirChooser chd = new DirChooser();
                    chd.show(getFragmentManager(), "");
                    return false;
                }
            });
        }

        final Preference dpref = findPreference("defaultif");
        if (dpref != null) {
            dpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Paths.setIfDirectory(new File(Paths.cardDirectory().getPath() + "/Interactive Fiction")); //set Path as default

                    Toast.makeText(PreferencesActivity.this, "You have set the default directory.", Toast.LENGTH_SHORT).show();

                    /* pushes the default If Directory to SharedPreferneces */
                    SharedPreferences sharedPrefs = getSharedPreferences("ifPath", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("ifPath", Paths.ifDirectory().getAbsolutePath());
                    editor.commit();
                    dpref.setSummary(Paths.cardDirectory().getPath() + "/Interactive Fiction is set.");
                    return false;
                }
            });
        }
        /* Refreshes the summary of SetIF before the listView is shown. */
        getListView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                onSharedPreferenceChanged(null, "setIFDir");
                return true;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setSummaryAll(getPreferenceScreen());
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(null, "fontFolderPath");
        onSharedPreferenceChanged(null, "setIFDir"); //onSharedPreferenceChanged(null, "defaultif"); //otherwise default summary is not visible
        // however setDir is not persistent when only onClick set
    }

    @Override
    protected void onPause() {
        super.onPause();
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
        } else if (key.equals("setIFDir")) {
            Preference preference = findPreference(key);
            preference.setSummary(Paths.ifDirectory().getAbsolutePath());
        } else if (key.equals("defaultif")) {
            Preference preference = findPreference(key);
            preference.setSummary(Paths.cardDirectory().getPath() + "/Interactive Fiction");
        } else {
            setSummaryPref(pref);
        }
    }
}
