package de.juliai.bavariaweather;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import de.juliai.bavariaweather.WeatherSettingsActivity.CheckboxPreferenceData;
import de.juliai.bavariaweather.WeatherSettingsActivity.IntentExtras;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherSettingsFragment extends PreferenceFragment {

	private static final String KEY_DATAS = "weather_data";

	private List<CheckboxPreferenceData> checkboxPreferenceDatas;

	/**
	 * factory method
	 * 
	 * @param intentExtras
	 * @return
	 */
	public static final WeatherSettingsFragment newInstance(
			final IntentExtras intentExtras) {
		final WeatherSettingsFragment fragment = new WeatherSettingsFragment();

		final Bundle args = new Bundle();
		args.putSerializable(KEY_DATAS, intentExtras);
		fragment.setArguments(args);

		return fragment;
	}

	/**
	 * empty public constructor as required
	 */
	public WeatherSettingsFragment() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.layout.weather_settings);

		final IntentExtras intentExtras = (IntentExtras) getArguments()
				.getSerializable(KEY_DATAS);
		this.checkboxPreferenceDatas = intentExtras.getDatas();

		final Activity activity = getActivity();
		final PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference("weatherSetting_selection");

		for (final CheckboxPreferenceData data : checkboxPreferenceDatas) {
			final CheckBoxPreference checkbox = new CheckBoxPreference(activity);

			checkbox.setKey(data.getKey());
			checkbox.setTitle(data.getTitle());
			checkbox.setDefaultValue(data.isDefaultValue());

			preferenceCategory.addPreference(checkbox);
		}
	}
}
