package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class Region {

	private String url;

	private String preferencesKey;

	private RegionFragment fragment;

	public Region(final String url, final String preferencesKey,
			final RegionFragment fragment) {
		this.url = url;
		this.preferencesKey = preferencesKey;
		this.fragment = fragment;
	}

	public void changeWeatherData(final String newWeatherData) {
		this.fragment.changeWeatherData(newWeatherData);
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

	public RegionFragment getFragment() {
		return fragment;
	}

	public void setFragment(RegionFragment fragment) {
		this.fragment = fragment;
	}
}
