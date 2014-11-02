package de.juliai.bavariaweather;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;
import de.juliai.bavariaweather.AsyncTaskCounter.AsyncTaskCounterCallback;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends FragmentActivity {

	private static final String KEY_VERSION = "bavariaWeatherVersion";

	private static final String CURRENT_VERSION = "1.5";

	private static final int CURRENT_NUMBER_OF_REGIONS = 3;

	private static final String KEY_DATA_BAVARIA = "weatherData_bavaria";

	private static final String KEY_DATA_SWABIA = "weatherData_swabia";

	private static final String KEY_DATA_UPPERBAVARIA = "weatherData_upperBavaria";

	private static final String KEY_SEL_BAVARIA = "weatherSetting_sel_bavaria";

	private static final String KEY_SEL_SWABIA = "weatherSetting_sel_swabia";

	private static final String KEY_SEL_UPPERBAVARIA = "weatherSetting_sel_upperBavaria";

	private SharedPreferences sharedPrefs;

	private RegionsPageAdapter regionsPageAdapter;

	private List<Region> regions;

	private List<RegionFragment> regionFragments;

	private List<String> selectionKeys;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_weather);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		initRegions();

		final ViewPager regionsViewPager = (ViewPager) findViewById(R.id.regionsViewPager);
		regionsPageAdapter = new RegionsPageAdapter(
				getSupportFragmentManager(), this.regionFragments);
		regionsViewPager.setAdapter(regionsPageAdapter);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_weather_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			reloadWeatherInfo(item);
			return true;
		case R.id.dropdown_settings:
			showWeatherSettings();
			return true;
		case R.id.dropdown_delete_cache:
			Toast.makeText(this, "clicked menu delete-cache item!",
					Toast.LENGTH_SHORT).show();
			return true;
		case R.id.dropdown_about:
			Toast.makeText(this, "clicked menu about item!", Toast.LENGTH_SHORT)
					.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * initializes the regions and regionFragments.
	 */
	private void initRegions() {
		regions = new LinkedList<Region>();
		regionFragments = new ArrayList<RegionFragment>(
				CURRENT_NUMBER_OF_REGIONS);
		selectionKeys = new LinkedList<String>();

		// check version
		boolean deleteData;
		final String version = sharedPrefs.getString(KEY_VERSION, "");
		if (CURRENT_VERSION.equals(version)) {
			deleteData = false;

		} else {
			deleteData = true;
			final Editor prefsEditor = sharedPrefs.edit();
			prefsEditor.putString(KEY_VERSION, CURRENT_VERSION);
			prefsEditor.commit();
		}

		// Bavaria
		{
			final boolean selectionBavaria = sharedPrefs.getBoolean(
					KEY_SEL_BAVARIA, true);
			selectionKeys.add(KEY_SEL_BAVARIA);

			String weatherDataBavaria = "";
			if (!deleteData) {
				weatherDataBavaria = sharedPrefs
						.getString(KEY_DATA_BAVARIA, "");
			}

			final RegionFragment fragmentBavaria = RegionFragment
					.newInstance(weatherDataBavaria);

			if (selectionBavaria) {
				regionFragments.add(fragmentBavaria);
			}

			regions.add(new Region(selectionBavaria, KEY_SEL_BAVARIA,
					"http://www.br.de/wetter/action/bayernwetter/bayern.do",
					KEY_DATA_BAVARIA, "@drawable/wappenbayern", fragmentBavaria));
		}

		// Swabia
		{
			final boolean selectionSwabia = sharedPrefs.getBoolean(
					KEY_SEL_SWABIA, false);
			selectionKeys.add(KEY_SEL_SWABIA);

			String weatherDataSwabia = "";
			if (!deleteData) {
				weatherDataSwabia = sharedPrefs.getString(KEY_DATA_SWABIA, "");
			}

			final RegionFragment fragmentSwabia = RegionFragment
					.newInstance(weatherDataSwabia);

			if (selectionSwabia) {
				regionFragments.add(fragmentSwabia);
			}

			regions.add(new Region(
					selectionSwabia,
					KEY_SEL_SWABIA,
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
					KEY_DATA_SWABIA, "@drawable/wappenschwaben", fragmentSwabia));
		}

		// Upper Bavaria
		{
			final boolean selectionUpperBavaria = sharedPrefs.getBoolean(
					KEY_SEL_UPPERBAVARIA, false);
			selectionKeys.add(KEY_SEL_UPPERBAVARIA);

			String weatherDataUpperBavaria = "";
			if (!deleteData) {
				weatherDataUpperBavaria = sharedPrefs.getString(
						KEY_DATA_UPPERBAVARIA, "");
			}

			final RegionFragment fragmentUpperBavaria = RegionFragment
					.newInstance(weatherDataUpperBavaria);

			if (selectionUpperBavaria) {
				regionFragments.add(fragmentUpperBavaria);
			}

			regions.add(new Region(
					selectionUpperBavaria,
					KEY_SEL_UPPERBAVARIA,
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
					KEY_DATA_UPPERBAVARIA, "@drawable/wappenoberbayern",
					fragmentUpperBavaria));
		}
	}

	/**
	 * loads the weather-data from the internet and stores it in the shared
	 * preferences.
	 */
	private void reloadWeatherInfo(final MenuItem item) {
		if (item != null) {
			// start refresh-icon-spinner
			item.setActionView(R.layout.action_progressbar);
			item.expandActionView();
		}

		int numberOfActiveRegions = 0;
		for (final Region region : regions) {
			if (region.isActive()) {
				numberOfActiveRegions++;
			}
		}

		final AsyncTaskCounter counter = new AsyncTaskCounter(
				numberOfActiveRegions, new AsyncTaskCounterCallback() {
					@Override
					public void onFinishedAllTasks() {
						if (item != null) {
							// stop refresh-icon-spinner
							item.collapseActionView();
							item.setActionView(null);
						}
					}
				});

		// refresh weather data for all active regions
		for (final Region region : regions) {
			if (region.isActive()) {
				final DownloadWeatherDataAsyncTask task = new DownloadWeatherDataAsyncTask(
						this, sharedPrefs, region, counter);
				task.execute(new URL[] {});
			}
		}
	}

	/**
	 * opens the settings
	 */
	private void showWeatherSettings() {
		final Intent intent = new Intent();
		intent.setClass(ShowWeatherActivity.this, WeatherSettingsActivity.class);
		startActivityForResult(intent, 0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// after user closed the settings:
		regionFragments = new ArrayList<RegionFragment>(
				CURRENT_NUMBER_OF_REGIONS);

		for (final String selectionKey : selectionKeys) {
			final boolean selection = sharedPrefs.getBoolean(selectionKey,
					false);

			for (final Region region : regions) {
				if (region.getPreferencesKeySelection().equals(selectionKey)) {
					region.setActive(selection);

					if (selection) {
						regionFragments.add(region.getFragment());
					}
				}
			}
		}

		regionsPageAdapter.updateData(regionFragments);
		regionsPageAdapter.notifyDataSetChanged();

		reloadWeatherInfo(null);
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private class RegionsPageAdapter extends FragmentStatePagerAdapter {

		private List<RegionFragment> fragments;

		public RegionsPageAdapter(FragmentManager fm,
				List<RegionFragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		/**
		 * @param newFragments
		 */
		public void updateData(List<RegionFragment> newFragments) {
			this.fragments = newFragments;
		}

		@Override
		public int getCount() {
			return this.fragments.size();
		}

		@Override
		public Fragment getItem(int position) {
			return this.fragments.get(position);
		}

		@Override
		public int getItemPosition(Object item) {
			return POSITION_NONE;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			try {
				super.destroyItem(container, position, object);
			} catch (IndexOutOfBoundsException e) {
				// seems to be already gone...
			}
		}
	}
}
