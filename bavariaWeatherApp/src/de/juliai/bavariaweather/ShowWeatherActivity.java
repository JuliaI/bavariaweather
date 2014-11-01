package de.juliai.bavariaweather;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.juliai.bavariaweather.AsyncTaskCounter.AsyncTaskCounterCallback;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends FragmentActivity {

	private static final String VERSION_KEY = "bavariaWeatherVersion";

	private static final String CURRENT_VERSION = "1.5";

	private SharedPreferences sharedPrefs;

	private List<Region> regions;

	private List<RegionFragment> regionFragments;

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
		final RegionsPageAdapter regionsPageAdapter = new RegionsPageAdapter(
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
			Toast.makeText(this, "clicked menu settings item!", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.dropdown_delete_cache:
			Toast.makeText(this, "clicked menu delete-cache item!", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.dropdown_about:
			Toast.makeText(this, "clicked menu about item!", Toast.LENGTH_SHORT).show();
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
		regionFragments = new LinkedList<RegionFragment>();

		// check version
		boolean deleteData;
		final String version = sharedPrefs.getString(VERSION_KEY, "");
		if (CURRENT_VERSION.equals(version)) {
			deleteData = false;

		} else {
			deleteData = true;
			final Editor prefsEditor = sharedPrefs.edit();
			prefsEditor.putString(VERSION_KEY, CURRENT_VERSION);
			prefsEditor.commit();
		}

		// Bavaria
		{
			final String preferencesKeyBavaria = "weatherDataBavaria";

			String weatherDataBavaria = "";
			if (!deleteData) {
				weatherDataBavaria = sharedPrefs.getString(
						preferencesKeyBavaria, "");
			}

			final RegionFragment fragmentBavaria = RegionFragment
					.newInstance(weatherDataBavaria);

			regionFragments.add(fragmentBavaria);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do",
					preferencesKeyBavaria, "@drawable/wappenbayern",
					fragmentBavaria));
		}

		// Swabia
		{
			final String preferencesKeySwabia = "weatherDataSwabia";

			String weatherDataSwabia = "";
			if (!deleteData) {
				weatherDataSwabia = sharedPrefs.getString(preferencesKeySwabia,
						"");
			}

			final RegionFragment fragmentSwabia = RegionFragment
					.newInstance(weatherDataSwabia);

			regionFragments.add(fragmentSwabia);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
					preferencesKeySwabia, "@drawable/wappenschwaben",
					fragmentSwabia));
		}

		// Upper Bavaria
		{
			final String preferencesKeyUpperBavaria = "weatherDataUpperBavaria";

			String weatherDataUpperBavaria = "";
			if (!deleteData) {
				weatherDataUpperBavaria = sharedPrefs.getString(
						preferencesKeyUpperBavaria, "");
			}

			final RegionFragment fragmentUpperBavaria = RegionFragment
					.newInstance(weatherDataUpperBavaria);

			regionFragments.add(fragmentUpperBavaria);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
					preferencesKeyUpperBavaria, "@drawable/wappenoberbayern",
					fragmentUpperBavaria));
		}
	}

	/**
	 * loads the weather-data from the internet and stores it in the shared
	 * preferences.
	 */
	private void reloadWeatherInfo(final MenuItem item) {
		// start refresh-icon-spinner
		item.setActionView(R.layout.action_progressbar);
		item.expandActionView();

		final AsyncTaskCounter counter = new AsyncTaskCounter(regions.size(),
				new AsyncTaskCounterCallback() {
					@Override
					public void onFinishedAllTasks() {
						// stop refresh-icon-spinner
						item.collapseActionView();
						item.setActionView(null);
					}
				});

		// refresh weather data for all regions
		for (final Region region : regions) {
			final DownloadWeatherDataAsyncTask task = new DownloadWeatherDataAsyncTask(
					this, sharedPrefs, region, counter);
			task.execute(new URL[] {});
		}
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private class RegionsPageAdapter extends FragmentPagerAdapter {

		private List<RegionFragment> fragments;

		public RegionsPageAdapter(FragmentManager fm,
				List<RegionFragment> fragments) {
			super(fm);
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return this.fragments.get(position);
		}

		@Override
		public int getCount() {
			return this.fragments.size();
		}
	}
}
