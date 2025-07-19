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
import androidx.preference.Preference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import org.andglkmod.glk.Glk;


public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences_activity);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.preferences_container, new PreferencesFragment())
                    .commit();
        }
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class PreferencesFragment extends PreferenceFragmentCompat implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            SwitchPreferenceCompat enablelist = findPreference("enablelist");
            if (enablelist != null)
                enablelist.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shortcutPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
                        if ((boolean) o)
                            sharedPreferencesEditor.putBoolean("enablelist", true);
                        else
                            sharedPreferencesEditor.putBoolean("enablelist", false);
                        sharedPreferencesEditor.commit();
                        return true;
                    }
                });

            SwitchPreferenceCompat enablelongpress = findPreference("enablelongpress");
            if (enablelongpress != null)
                enablelongpress.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shortcutPrefs", Context.MODE_PRIVATE);
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
                        startActivity(new Intent(requireContext(), ShortcutPreferencesActivity.class));
                        return true;
                    }
                });

            SwitchPreferenceCompat modePref = findPreference("day_night");
            if (modePref != null) {
                modePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference pref, Object isOnObject) {
                        boolean isModeOn = (Boolean) isOnObject;
                        SharedPreferences sharedPrefs = requireActivity().getSharedPreferences("Night", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs.edit();

                        if (isModeOn) {
                            org.andglkmod.glk.TextBufferWindow.DefaultBackground = Color.DKGRAY;
                            org.andglkmod.glk.TextBufferWindow.DefaultTextColor = Color.WHITE;
                            org.andglkmod.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_NIGHT;
                            editor.putBoolean("NightOn", true);
                            editor.commit();
                        } else {
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

            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);




        }

        @Override
        public void onResume() {
            super.onResume();
            setSummaryAll(getPreferenceScreen());
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(null, "fontFolderPath");
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
                if(desc != null && desc.equals("Droid Serif (default)"))
                    pref.setSummary("Droid Serif");
            } else if (pref instanceof PreferenceScreen) {
                setSummaryAll((PreferenceScreen) pref);
            }
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            if ("fontFileName".equals(key)) {
                ListPreference prefFn = (ListPreference) pref;

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

                String[] aff = (String[]) ff.toArray(new String[ff.size()]);

                String save = prefFn.getValue();
                prefFn.setEntries(aff);
                prefFn.setEntryValues(aff);
                if (ff.contains(save)) prefFn.setValue(save);

                setSummaryPref(prefFn);


            } else {
                setSummaryPref(pref);
            }
        }
    }
}
