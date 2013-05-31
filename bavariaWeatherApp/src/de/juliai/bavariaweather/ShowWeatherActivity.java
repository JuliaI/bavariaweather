package de.juliai.bavariaweather;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterViewFlipper;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends Activity {

	private SharedPreferences sharedPrefs;

	private List<Region> regions;

	private RegionsAdapter regionsAdapter;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_weather);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		initRegions(sharedPrefs);

		final AdapterViewFlipper regionsAdapterViewFlipper = (AdapterViewFlipper) findViewById(R.id.regionsAdapterViewFlipper);
		this.regionsAdapter = new RegionsAdapter(sharedPrefs, regions);
		regionsAdapterViewFlipper.setAdapter(regionsAdapter);

		// TODO how to flip right and left?
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
	 * @param sharedPrefs
	 */
	private void initRegions(final SharedPreferences sharedPrefs) {
		regions = new LinkedList<Region>();
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do",
				"weatherDataBavaria", sharedPrefs, this));
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
				"weatherDataSwabia", sharedPrefs, this));
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
				"weatherDataUpperBavaria", sharedPrefs, this));
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
}
