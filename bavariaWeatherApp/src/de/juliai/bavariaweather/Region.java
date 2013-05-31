package de.juliai.bavariaweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.webkit.WebView;

/**
 * 
 * @author JuliaI
 * 
 */
public class Region {

	private String url;

	private String preferencesKey;

	private String weatherData;

	private WebView view;

	/**
	 * constructor
	 * 
	 * @param url
	 * @param preferencesKey
	 * @param sharedPrefs
	 * @param thisActivity
	 */
	public Region(final String url, final String preferencesKey,
			final SharedPreferences sharedPrefs, final Activity thisActivity) {
		this.url = url;
		this.preferencesKey = preferencesKey;

		this.weatherData = sharedPrefs.getString(preferencesKey, "");

		final WebView regionsView = new WebView(thisActivity);
		regionsView.loadData(weatherData, "text/html", "UTF8");
		this.view = regionsView;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPreferencesKey() {
		return preferencesKey;
	}

	public void setPreferencesKey(String preferencesKey) {
		this.preferencesKey = preferencesKey;
	}

	public String getWeatherData() {
		return weatherData;
	}

	public void setWeatherData(String weatherData) {
		this.weatherData = weatherData;

		if (weatherData == null) {
			weatherData = "";
		}
		this.view.loadData(weatherData, "text/html", "UTF8");
	}

	public WebView getView() {
		return view;
	}

	public void setView(WebView view) {
		this.view = view;
	}
}
