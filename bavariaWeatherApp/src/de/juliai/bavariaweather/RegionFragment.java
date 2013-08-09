package de.juliai.bavariaweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * 
 * @author JuliaI
 * 
 */
public class RegionFragment extends Fragment {

	private static final String ARG_DATA = "weather_data";

	private String weatherData;

	/**
	 * factory method
	 * 
	 * @param weatherData
	 * @return
	 */
	public static final RegionFragment newInstance(final String weatherData) {
		final RegionFragment regionFragment = new RegionFragment();

		Bundle args = new Bundle();
		args.putString(ARG_DATA, weatherData);
		regionFragment.setArguments(args);

		return regionFragment;
	}

	/**
	 * empty public constructor as required
	 */
	public RegionFragment() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.weatherData = getArguments().getString(ARG_DATA);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.region_page_layout,
				container, false);

		final WebView regionWebView = (WebView) view
				.findViewById(R.id.regionWebView);
		regionWebView.loadData(this.weatherData, "text/html", "UTF8");

		return view;
	}

	/**
	 * @param newWeatherData
	 */
	public void changeWeatherData(final String newWeatherData) {
		this.weatherData = newWeatherData;
		getArguments().putString(ARG_DATA, newWeatherData);

		final View view = this.getView();
		if (view != null) {
			final WebView regionWebView = (WebView) view
					.findViewById(R.id.regionWebView);
			regionWebView.loadData(this.weatherData, "text/html", "UTF8");
		}
	}
}
