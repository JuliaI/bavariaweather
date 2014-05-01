package de.juliai.bavariaweather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import de.juliai.bavariaweather.WeatherData.WeatherText;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherDataSerializer {

	/**
	 * @param weatherData
	 * @return
	 */
	public static final String serialize(final WeatherData weatherData) {
		if (weatherData == null) {
			return "";
		}

		final JSONObject weatherDataJson = new JSONObject();

		try {
			weatherDataJson.put("crestDrawableName",
					weatherData.getCrestDrawableName());
			weatherDataJson.put("title", weatherData.getTitle());
			weatherDataJson.put("subtitle1", weatherData.getSubtitle1());
			weatherDataJson.put("subtitle2", weatherData.getSubtitle2());
			weatherDataJson.put("timestamp", weatherData.getTimestamp());

			final JSONArray weatherTextsArray = new JSONArray();
			if (weatherData.getWeatherTexts() != null) {
				for (final WeatherText text : weatherData.getWeatherTexts()) {
					final JSONObject weatherTextJson = new JSONObject();

					final String head = text.getHead();
					if (StringUtils.isNotEmpty(head)) {
						weatherTextJson.put("head", head);
					}

					weatherTextJson.put("text", text.getText());
					weatherTextsArray.put(weatherTextJson);
				}
			}
			weatherDataJson.put("weatherTexts", weatherTextsArray);

		} catch (JSONException e) {
			Log.e("WeatherDataSerializer",
					"JSONException happened during serializing! Message: "
							+ e.getMessage(), e);
		}

		return weatherDataJson.toString();
	}

	/**
	 * @param weatherDataString
	 * @return
	 */
	public static final WeatherData deserialize(final String weatherDataString) {
		if (StringUtils.isEmpty(weatherDataString)) {
			return null;
		}

		final WeatherData weatherData = new WeatherData();

		try {
			final JSONObject weatherDataJson = new JSONObject(weatherDataString);

			weatherData.setCrestDrawableName(weatherDataJson
					.getString("crestDrawableName"));
			weatherData.setTitle(weatherDataJson.getString("title"));
			weatherData.setSubtitle1(weatherDataJson.getString("subtitle1"));
			weatherData.setSubtitle2(weatherDataJson.getString("subtitle2"));
			weatherData.setTimestamp(weatherDataJson.getString("timestamp"));

			final JSONArray weatherTextsArray = weatherDataJson
					.getJSONArray("weatherTexts");
			for (int i = 0; i < weatherTextsArray.length(); i++) {
				final JSONObject weatherTextJson = weatherTextsArray
						.getJSONObject(i);
				final WeatherText text = new WeatherText();

				try {
					text.setHead(weatherTextJson.getString("head"));
				} catch (JSONException e) {
					// do nothing, that's fine
				}

				text.appendText(weatherTextJson.getString("text"));
				weatherData.addWeatherText(text);
			}

		} catch (JSONException e) {
			Log.e("WeatherDataSerializer",
					"JSONException happened during deserializing! Message: "
							+ e.getMessage(), e);
		}

		return weatherData;
	}
}
