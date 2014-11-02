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

	/*
	 * TODO: 
	 * - wappen 
	 * - create settings page from do-data 
	 * - add order forregions
	 */
	private int numberOfRegions;

	private SharedPreferences sharedPrefs;

	private RegionsPageAdapter regionsPageAdapter;

	private Menu menu;

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
		this.menu = menu;
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
			showAbout();
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
		final String CURRENT_VERSION = "1.6";

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
		 * if you update something here, also update weather_settings.xml and
		 * strings.xml !
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
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Schwaben",
				"@drawable/wappenschwaben");
		regionDos.add(swabia);

		final RegionDo upperBavaria = new RegionDo(
				"weatherData_upperBavaria",
				"weatherSetting_sel_upperBavaria",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberbayern",
				"@drawable/wappenoberbayern");
		regionDos.add(upperBavaria);

		final RegionDo lowerBavaria = new RegionDo(
				"weatherData_lowerBavaria",
				"weatherSetting_sel_lowerBavaria",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Niederbayern",
				"@drawable/wappenniederbayern");
		regionDos.add(lowerBavaria);

		final RegionDo upperPalatinate = new RegionDo(
				"weatherData_upperPalatinate",
				"weatherSetting_sel_upperPalatinate",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberpfalz",
				"@drawable/wappenoberpfalz");
		regionDos.add(upperPalatinate);

		final RegionDo middleFranconia = new RegionDo(
				"weatherData_middleFranconia",
				"weatherSetting_sel_middleFranconia",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Mittelfranken",
				"@drawable/wappenmittelfranken");
		regionDos.add(middleFranconia);

		final RegionDo upperFranconia = new RegionDo(
				"weatherData_upperFranconia",
				"weatherSetting_sel_upperFranconia",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Oberfranken",
				"@drawable/wappenoberfranken");
		regionDos.add(upperFranconia);

		final RegionDo lowerFranconia = new RegionDo(
				"weatherData_lowerFranconia",
				"weatherSetting_sel_lowerFranconia",
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?id=0&regio=Unterfranken",
				"@drawable/wappenunterfranken");
		regionDos.add(lowerFranconia);

		final RegionDo mountainWeather = new RegionDo(
				"weatherData_mountainWeather",
				"weatherSetting_sel_mountainWeather",
				"http://www.br.de/wetter/action/reisewetter/bergwetter.do",
				"@drawable/wappenbergwetter");
		regionDos.add(mountainWeather);

		final RegionDo winterWeather = new RegionDo(
				"weatherData_winterWeather",
				"weatherSetting_sel_winterWeather",
				"http://www.br.de/wetter/action/reisewetter/wintersport.do",
				"@drawable/wappenwinterwetter");
		regionDos.add(winterWeather);

		numberOfRegions = regionDos.size();

		regions = new ArrayList<Region>(numberOfRegions);
		regionFragments = new ArrayList<RegionFragment>(numberOfRegions);
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
	 * 
	 */
	private void reloadWeatherInfo() {
		final MenuItem item = menu.findItem(R.id.itemRefresh);
		reloadWeatherInfo(item);
	}

	/**
	 * loads the weather-data from the internet and stores it in the shared
	 * preferences.
	 */
	private void reloadWeatherInfo(final MenuItem item) {
		// start refresh-icon-spinner
		item.setActionView(R.layout.action_progressbar);
		item.expandActionView();

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
						// stop refresh-icon-spinner
						item.collapseActionView();
						item.setActionView(null);
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
		regionFragments = new ArrayList<RegionFragment>(numberOfRegions);

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

		reloadWeatherInfo();
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

		for (final Region region : regions) {
			region.changeWeatherData("");
		}

		Toast.makeText(this, "Der Cache wurde gelöscht", Toast.LENGTH_LONG)
				.show();
	}

	/**
	 * shows the about-activity
	 */
	private void showAbout() {
		final Intent intent = new Intent();
		intent.setClass(ShowWeatherActivity.this, AboutActivity.class);
		startActivity(intent);
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
