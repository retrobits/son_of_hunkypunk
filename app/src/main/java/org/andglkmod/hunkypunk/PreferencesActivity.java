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

package org.andglkmod.hunkypunk;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import org.andglkmod.glk.Glk;


public class PreferencesActivity
        extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //getPreferenceManager().setSharedPreferencesName("hunkypunk");

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SwitchPreference enablelist = (SwitchPreference) findPreference("enablelist");
        if (enablelist != null)
            enablelist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    if ((boolean) o)
                        sharedPreferencesEditor.putBoolean("enablelist", true);
                    else
                        sharedPreferencesEditor.putBoolean("enablelist", false);
                    sharedPreferencesEditor.commit();
                    return true;
                }
            });


        SwitchPreference enablelongpress = (SwitchPreference) findPreference("enablelongpress");
        if (enablelongpress != null)
            enablelongpress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    SharedPreferences sharedPreferences = getSharedPreferences("shortcutPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                    if ((boolean) o)
                        sharedPreferencesEditor.putBoolean("enablelongpress", true);
                    else
                        sharedPreferencesEditor.putBoolean("enablelongpress", false);
                    sharedPreferencesEditor.commit();
                    return true;
                }
            });

        Preference manageShortcuts = findPreference("manageshortcuts");
        if (manageShortcuts != null)
            manageShortcuts.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getApplicationContext(), ShortcutPreferencesActivity.class));
                    return true;
                }
            });

        SwitchPreference modePref = (SwitchPreference) findPreference("day_night");
        if (modePref != null) {
            modePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference pref, Object isOnObject) {
                    boolean isModeOn = (Boolean) isOnObject;
                    SharedPreferences sharedPrefs = getSharedPreferences("Night", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    if (isModeOn) {
                        //Toast.makeText(PreferencesActivity.this, "Night Mode toggled on.",Toast.LENGTH_SHORT).show();
                        //Implementation
                        org.andglkmod.glk.TextBufferWindow.DefaultBackground = Color.DKGRAY;
                        org.andglkmod.glk.TextBufferWindow.DefaultTextColor = Color.WHITE;
                        /* Since styles are compiled in advance and not dynamically modifiable
                         * new *night* input style is used and swapped with the default one
						 */
                        org.andglkmod.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_NIGHT;
                        //store the switch-state
                        editor.putBoolean("NightOn", true);
                        editor.commit();
                    } else {
                        //Toast.makeText(PreferencesActivity.this, "Night Mode toggled off.",Toast.LENGTH_SHORT).show();
                        //Implementation
                        org.andglkmod.glk.TextBufferWindow.DefaultBackground = Color.WHITE;
                        org.andglkmod.glk.TextBufferWindow.DefaultTextColor = Color.BLACK;
                        org.andglkmod.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_INPUT;

                        editor.putBoolean("NightOn", false);
                        editor.commit();
                    }
                    return true;
                }

            });
        }

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        SharedPreferences sharedPrefs = getSharedPreferences("ifPath", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString("ifPath", Paths.ifDirectory(getApplicationContext()).getAbsolutePath());
        editor.commit();

        Preference apref = findPreference("setIFDir");
        if (apref != null) {
            apref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    //DirChooser chd = new DirChooser();
                    //chd.show(getFragmentManager(), "");
                    return false;
                }
            });
        }

        /*
        final Preference dpref = findPreference("defaultif");
        dpref.setSummary(Paths.defaultIfDirectory(this).getPath());
        if (dpref != null) {
            dpref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Paths.setIfDirectory(Paths.defaultIfDirectory(getApplicationContext())); //set Path as default

                    Toast.makeText(PreferencesActivity.this, "You have set the default directory.", Toast.LENGTH_SHORT).show();

                    // pushes the default If Directory to SharedPreferneces
                    SharedPreferences sharedPrefs = getSharedPreferences("ifPath", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString("ifPath", Paths.ifDirectory(getApplicationContext()).getAbsolutePath());
                    editor.commit();

                    return false;
                }
            });
        }
        */

        // Refreshes the summary of SetIF before the listView is shown.
        getListView().getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                onSharedPreferenceChanged(null, "setIFDir");
                return true;
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
            if(desc.equals("Droid Serif (default)"))
                pref.setSummary("Droid Serif");
        } else if (pref instanceof PreferenceScreen) {
            setSummaryAll((PreferenceScreen) pref);
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences
                                                  sharedPreferences, String key) {
        Preference pref = findPreference(key);

        if (key.compareTo("fontFileName") == 0) {

            //EditTextPreference prefFol = (EditTextPreference)pref;
            ListPreference prefFn = (ListPreference) pref;//findPreference("fontFileName");

            ArrayList<String> ff = new ArrayList<String>();
            ff.add("256 BYTES");
            ff.add("Adventure");
            ff.add("Coda Regular");
            ff.add("CODE Bold");
            ff.add("CODE Light");
            ff.add("Crimson Roman");
            ff.add("Daniel");
            ff.add("Data Control");
            ff.add("Droid Mono");
            ff.add("Droid Sans");
            ff.add("Droid Serif   (default)");
            ff.add("Keep Calm");
            ff.add("Marlboro");
            ff.add("MKOCR");
            ff.add("Old Game Fatty");
            ff.add("Pokemon Hollow");
            ff.add("Pokemon Solid");
            ff.add("Roboto Regular");
            ff.add("Roboto Thin");
            ff.add("Star Jedi");
            ff.add("TeX Regular");
            ff.add("Traveling Typewriter");
            ff.add("Ubuntu Regular");



			/*File ffol = new File(prefFol.getText());
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

				for(int i=0;i<fileList.length;i++) {
					ff.add(fileList[i].getName());
				}
			}*/
            String[] aff = (String[]) ff.toArray(new String[ff.size()]);

            String save = prefFn.getValue();
            //prefFn.setValue(""); //WHY set?? >> it gives StackOverflow
            prefFn.setEntries(aff);
            prefFn.setEntryValues(aff);
            if (ff.contains(save)) prefFn.setValue(save);

            setSummaryPref(prefFn);
        } else if (key.equals("setIFDir")) {
            Preference preference = findPreference(key);
            preference.setSummary(Paths.ifDirectory(getApplicationContext()).getAbsolutePath());
        } else if (key.equals("defaultif")) {
            Preference preference = findPreference(key);
            preference.setSummary(Paths.cardDirectory().getPath() + "/Interactive Fiction");
        } else {
            setSummaryPref(pref);
        }
    }
}
