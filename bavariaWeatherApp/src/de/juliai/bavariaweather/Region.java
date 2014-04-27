package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class Region {

	private String url;

	private String preferencesKey;

	private String crestDrawableName;

	private RegionFragment fragment;

	public Region(final String url, final String preferencesKey,
			final String crestDrawableName, final RegionFragment fragment) {
		this.url = url;
		this.preferencesKey = preferencesKey;
		this.crestDrawableName = crestDrawableName;
		this.fragment = fragment;
	}

	public void changeWeatherData(final String newSerializedWeatherData) {
		this.fragment.changeWeatherData(newSerializedWeatherData);
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

	public String getCrestDrawableName() {
		return crestDrawableName;
	}

	public void setCrestDrawableName(String crestDrawableName) {
		this.crestDrawableName = crestDrawableName;
	}

	public RegionFragment getFragment() {
		return fragment;
	}

	public void setFragment(RegionFragment fragment) {
		this.fragment = fragment;
	}
}
