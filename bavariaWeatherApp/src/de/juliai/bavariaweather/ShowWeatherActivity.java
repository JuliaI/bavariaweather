package de.juliai.bavariaweather;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends FragmentActivity {

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
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.itemRefresh:
			reloadWeatherInfo();
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

		// Bavaria
		{
			final String preferencesKeyBavaria = "weatherDataBavaria";

			String weatherDataBavaria = sharedPrefs.getString(
					preferencesKeyBavaria, "");
			// remove legacy data, can be removed after first start
			if (weatherDataBavaria.startsWith("<html")) {
				weatherDataBavaria = "";
			}

			final RegionFragment fragmentBavaria = RegionFragment
					.newInstance(weatherDataBavaria);

			regionFragments.add(fragmentBavaria);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do",
					preferencesKeyBavaria, R.drawable.wappenbayern,
					fragmentBavaria));
		}

		// Swabia
		{
			final String preferencesKeySwabia = "weatherDataSwabia";

			String weatherDataSwabia = sharedPrefs.getString(
					preferencesKeySwabia, "");
			if (weatherDataSwabia.startsWith("<html")) {
				weatherDataSwabia = "";
			}

			final RegionFragment fragmentSwabia = RegionFragment
					.newInstance(weatherDataSwabia);

			regionFragments.add(fragmentSwabia);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
					preferencesKeySwabia, R.drawable.wappenschwaben,
					fragmentSwabia));
		}

		// Upper Bavaria
		{
			final String preferencesKeyUpperBavaria = "weatherDataUpperBavaria";

			String weatherDataUpperBavaria = sharedPrefs.getString(
					preferencesKeyUpperBavaria, "");
			if (weatherDataUpperBavaria.startsWith("<html")) {
				weatherDataUpperBavaria = "";
			}

			final RegionFragment fragmentUpperBavaria = RegionFragment
					.newInstance(weatherDataUpperBavaria);

			regionFragments.add(fragmentUpperBavaria);
			regions.add(new Region(
					"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
					preferencesKeyUpperBavaria, R.drawable.wappenoberbayern,
					fragmentUpperBavaria));
		}
	}

	/**
	 * loads weather data from the internet. Then stores them in the shared
	 * preferences.
	 */
	private void reloadWeatherInfo() {
		for (final Region region : regions) {
			final DownloadWeatherDataAsyncTask task = new DownloadWeatherDataAsyncTask(
					this, sharedPrefs, region);
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
