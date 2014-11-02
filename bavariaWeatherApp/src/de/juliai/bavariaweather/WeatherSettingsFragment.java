package de.juliai.bavariaweather;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherSettingsFragment extends PreferenceFragment {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.weather_settings);
	}
}
