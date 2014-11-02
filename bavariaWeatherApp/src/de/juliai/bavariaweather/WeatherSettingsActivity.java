package de.juliai.bavariaweather;

import android.app.Activity;
import android.os.Bundle;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherSettingsActivity extends Activity {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new WeatherSettingsFragment())
				.commit();
	}
}
