package de.juliai.bavariaweather;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.AdapterViewFlipper;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends Activity {

	private AdapterViewFlipper regionsAdapterViewFlipper;

	private SharedPreferences sharedPrefs;

	private List<Region> regions;

	private RegionsAdapter regionsAdapter;

	private GestureDetector gestureDetector;

	private static final int SWIPE_MIN_DISTANCE = 120;

	private static final int SWIPE_MAX_OFF_PATH = 250;

	private static final int SWIPE_THRESHOLD_VELOCITY = 100;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init view-flipper
		regionsAdapterViewFlipper = new AdapterViewFlipper(this);
		regionsAdapterViewFlipper.setId(R.id.regionsAdapterViewFlipper);
		setContentView(regionsAdapterViewFlipper);

		// init storage
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		// init listener
		initListener();

		// init regions
		initRegions();

		// init adapter
		regionsAdapter = new RegionsAdapter(regions);
		regionsAdapterViewFlipper.setAdapter(regionsAdapter);

	}

	/**
	 * initializes listener that listens to fling-events
	 */
	private void initListener() {
		final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {

				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH) {
					return false;
				}

				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// right to left swipe
					regionsAdapterViewFlipper.showNext();
					return true;
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					// left to right swipe
					regionsAdapterViewFlipper.showPrevious();
					return true;
				}

				return false;
			}
		};

		gestureDetector = new GestureDetector(this, gestureListener);
	}

	/**
	 * initializes the regions.
	 * 
	 * sharedPrefs and gestureDetector must be already initialized before
	 * calling this method.
	 */
	private void initRegions() {
		regions = new LinkedList<Region>();
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do",
				"weatherDataBavaria", sharedPrefs, gestureDetector, this));
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Schwaben&id=0",
				"weatherDataSwabia", sharedPrefs, gestureDetector, this));
		regions.add(new Region(
				"http://www.br.de/wetter/action/bayernwetter/bayern.do?regio=Oberbayern&id=0",
				"weatherDataUpperBavaria", sharedPrefs, gestureDetector, this));
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
