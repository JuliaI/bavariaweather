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

	private static final int NUMBER_OF_REGIONS = 3;

	private SharedPreferences sharedPrefs;

	private RegionsPageAdapter regionsPageAdapter;

	private List<Region> regions;

	private List<RegionFragment> regionFragments;

	private List<String> dataKeys;

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
			deleteCache();
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

		// check version
		final String KEY_VERSION = "bavariaWeatherVersion";
		final String CURRENT_VERSION = "1.5";

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

		// init regions
		/*
		 * if you update something here, also update 
		 * - int NUMBER_OF_REGIONS 
		 * - weather_settings.xml
		 */
		final List<RegionDo> regionDos = new LinkedList<RegionDo>();

		final RegionDo bavaria = new RegionDo("weatherData_bavaria",
				"weatherSetting_sel_bavaria",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do",
				"@drawable/wappenbayern");
		regionDos.add(bavaria);

		final RegionDo swabia = new RegionDo(
				"weatherData_swabia",
				"weatherSetting_sel_swabia",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
				"@drawable/wappenschwaben");
		regionDos.add(swabia);

		final RegionDo upperBavaria = new RegionDo(
				"weatherData_upperBavaria",
				"weatherSetting_sel_upperBavaria",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
				"@drawable/wappenoberbayern");
		regionDos.add(upperBavaria);

		regions = new LinkedList<Region>();
		regionFragments = new ArrayList<RegionFragment>(NUMBER_OF_REGIONS);
		dataKeys = new LinkedList<String>();
		selectionKeys = new LinkedList<String>();

		for (final RegionDo regionDo : regionDos) {
			dataKeys.add(regionDo.getDataKey());
			selectionKeys.add(regionDo.getSelectionKey());

			String weatherData = "";
			if (!deleteData) {
				weatherData = sharedPrefs.getString(regionDo.getDataKey(), "");
			}

			final RegionFragment fragment = RegionFragment
					.newInstance(weatherData);

			final boolean selection = sharedPrefs.getBoolean(
					regionDo.getSelectionKey(), true);

			if (selection) {
				regionFragments.add(fragment);
			}

			regions.add(new Region(selection, regionDo.getSelectionKey(),
					regionDo.getUrl(), regionDo.getDataKey(), regionDo
							.getCrestDrawableName(), fragment));
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
		regionFragments = new ArrayList<RegionFragment>(NUMBER_OF_REGIONS);

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
	 * deletes all the weather-data
	 */
	private void deleteCache() {
		final Editor prefsEditor = sharedPrefs.edit();
		for (final String dataKey : dataKeys) {
			prefsEditor.remove(dataKey);
		}
		prefsEditor.commit();
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private static class RegionDo {

		private final String dataKey;

		private final String selectionKey;

		private final String url;

		private final String crestDrawableName;

		public RegionDo(String dataKey, String selectionKey, String url,
				String crestDrawableName) {
			this.dataKey = dataKey;
			this.selectionKey = selectionKey;
			this.url = url;
			this.crestDrawableName = crestDrawableName;
		}

		public String getDataKey() {
			return dataKey;
		}

		public String getSelectionKey() {
			return selectionKey;
		}

		public String getUrl() {
			return url;
		}

		public String getCrestDrawableName() {
			return crestDrawableName;
		}
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
