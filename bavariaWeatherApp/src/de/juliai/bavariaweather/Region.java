package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class Region {

	private boolean active;

	private String preferencesKeySelection;

	private String url;

	private String preferencesKeyData;

	private String crestDrawableName;

	private RegionFragment fragment;

	public Region(final boolean active, final String preferencesKeySelection,
			final String url, final String preferencesKeyData,
			final String crestDrawableName, final RegionFragment fragment) {
		this.active = active;
		this.preferencesKeySelection = preferencesKeySelection;
		this.url = url;
		this.preferencesKeyData = preferencesKeyData;
		this.crestDrawableName = crestDrawableName;
		this.fragment = fragment;
	}

	public void changeWeatherData(final String newSerializedWeatherData) {
		this.fragment.changeWeatherData(newSerializedWeatherData);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getPreferencesKeySelection() {
		return preferencesKeySelection;
	}

	public void setPreferencesKeySelection(String preferencesKeySelection) {
		this.preferencesKeySelection = preferencesKeySelection;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPreferencesKeyData() {
		return preferencesKeyData;
	}

	public void setPreferencesKeyData(String preferencesKeyData) {
		this.preferencesKeyData = preferencesKeyData;
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
