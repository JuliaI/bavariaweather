package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class AsyncTaskResult {

	private WeatherData data;

	private String errorMessage;

	public WeatherData getData() {
		return data;
	}

	public void setData(WeatherData data) {
		this.data = data;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
