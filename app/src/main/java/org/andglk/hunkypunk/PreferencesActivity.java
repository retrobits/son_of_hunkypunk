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
import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import android.preference.SwitchPreference;
import android.widget.Toast;
import org.andglk.glk.Glk;
import org.andglk.glk.TextBufferWindow;


public class PreferencesActivity
	extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//getPreferenceManager().setSharedPreferencesName("hunkypunk");

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);


		SwitchPreference modePref = (SwitchPreference) findPreference("day_night");
		if (modePref != null) {
			modePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
				@Override
				public boolean onPreferenceChange(Preference pref, Object isOnObject) {
					boolean isModeOn = (Boolean) isOnObject;
					SharedPreferences sharedPrefs = getSharedPreferences("Night", Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPrefs.edit();

					if (isModeOn) {
						Toast.makeText(PreferencesActivity.this, "Night Mode toggled on.",Toast.LENGTH_SHORT).show();
						//Implementation
						org.andglk.glk.TextBufferWindow.DefaultBackground = Color.DKGRAY;
						org.andglk.glk.TextBufferWindow.DefaultTextColor = Color.WHITE;
						/* Since styles are compiled in advance and not dynamically modifiable
						 * new *night* input style is used and swapped with the default one
						 */
						org.andglk.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_NIGHT;
						//needed for the TextWatcher and different from sp's NightOn
						// since it is used only when in the same View instance to change color
						// of momentarily inputted text (on the go)
						//NOT RESTORED since only needed at runtime
						//store the switch-state
						editor.putBoolean("NightOn", true);
						editor.commit();
					} else {
						Toast.makeText(PreferencesActivity.this, "Night Mode toggled off.",Toast.LENGTH_SHORT).show();
						//Implementation
						org.andglk.glk.TextBufferWindow.DefaultBackground = Color.WHITE;
						org.andglk.glk.TextBufferWindow.DefaultTextColor = Color.BLACK;
						org.andglk.glk.TextBufferWindow.DefaultInputStyle = Glk.STYLE_INPUT;

						editor.putBoolean("NightOn", false);
						editor.commit();
					}
					return true;
				}

			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		setSummaryAll(getPreferenceScreen());
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		onSharedPreferenceChanged(null,"fontFolderPath");
	}

	@Override protected void onPause() {
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
		}
		else if (pref instanceof PreferenceCategory) {
			PreferenceCategory prefCat = (PreferenceCategory)pref;
			int count = prefCat.getPreferenceCount();
			for (int i=0; i < count; i++) {
				setSummaryPref(prefCat.getPreference(i));
			}
		}
		else if (pref instanceof ListPreference) {
			ListPreference lPref = (ListPreference) pref;     
			String desc = lPref.getValue();
			pref.setSummary(desc); 
		} 
		else if (pref instanceof PreferenceScreen) {
			setSummaryAll((PreferenceScreen) pref); 
		} 
	}

	public void	onSharedPreferenceChanged(SharedPreferences
										  sharedPreferences, String key) { 		
		Preference pref = findPreference(key); 
		
		if (key.compareTo("fontFolderPath")==0) {

			EditTextPreference prefFol = (EditTextPreference)pref;
			ListPreference prefFn = (ListPreference)findPreference("fontFileName");
			
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
			
				for(int i=0;i<fileList.length;i++) {
					ff.add(fileList[i].getName());
				}
			}
			String[] aff = (String[])ff.toArray(new String[ff.size()]);

			String save = prefFn.getValue();
			prefFn.setValue("");
			prefFn.setEntries(aff);
			prefFn.setEntryValues(aff);
			if (ff.contains(save)) prefFn.setValue(save);

			setSummaryPref(prefFn);
		}
		else {
			setSummaryPref(pref);
		}
	}
}
