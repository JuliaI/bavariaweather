package de.juliai.bavariaweather;

import java.io.Serializable;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherSettingsActivity extends Activity {

	public static final String KEY_INTENT_EXTRAS = "weatherSettings_selectionData";

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		update();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);

		update();
	}

	/**
	 * 
	 */
	private final void update() {
		final IntentExtras intentExtras = getIntentExtras();
		if (intentExtras == null) {
			finish();
			return;
		}

		final WeatherSettingsFragment fragment = WeatherSettingsFragment
				.newInstance(intentExtras);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, fragment).commit();
	}

	/**
	 * @return
	 */
	private final IntentExtras getIntentExtras() {
		final Serializable intentExtras = getIntent().getSerializableExtra(
				KEY_INTENT_EXTRAS);

		if (intentExtras != null && (intentExtras instanceof IntentExtras)) {
			return (IntentExtras) intentExtras;
		}

		return null;
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	public static final class IntentExtras implements Serializable {

		private static final long serialVersionUID = -5538920954232412117L;

		private final List<CheckboxPreferenceData> datas;

		/**
		 * @param datas
		 */
		public IntentExtras(final List<CheckboxPreferenceData> datas) {
			this.datas = datas;
		}

		/**
		 * @return
		 */
		public final List<CheckboxPreferenceData> getDatas() {
			return datas;
		}
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	public static final class CheckboxPreferenceData implements Serializable {

		private static final long serialVersionUID = 7445117545948743633L;

		private final String key;

		private final String title;

		private final boolean defaultValue;

		public CheckboxPreferenceData(final String key, final String title,
				final boolean defaultValue) {
			this.key = key;
			this.title = title;
			this.defaultValue = defaultValue;
		}

		public String getKey() {
			return key;
		}

		public String getTitle() {
			return title;
		}

		public boolean isDefaultValue() {
			return defaultValue;
		}
	}
}
