package de.juliai.bavariaweather;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author JuliaI
 * 
 */
public class WeatherData {

	private String crestDrawableName;

	private String title; // h1, class="br-wetter"

	private String subtitle1; // p, class="vorhersage"

	private String subtitle2; // div, class="Schlagzeile"

	private String timestamp; // div, class="TimeStamp"

	private List<WeatherText> weatherTexts; // div, class="WetterTextBlock"

	public String getCrestDrawableName() {
		return crestDrawableName;
	}

	public void setCrestDrawableName(String crestDrawableName) {
		this.crestDrawableName = crestDrawableName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = StringUtils.clean(title);
	}

	public String getSubtitle1() {
		return subtitle1;
	}

	public void setSubtitle1(String subtitle1) {
		this.subtitle1 = StringUtils.clean(subtitle1);
	}

	public String getSubtitle2() {
		return subtitle2;
	}

	public void setSubtitle2(String subtitle2) {
		this.subtitle2 = StringUtils.clean(subtitle2);
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = StringUtils.clean(timestamp);
	}

	public List<WeatherText> getWeatherTexts() {
		return weatherTexts;
	}

	public void addWeatherText(WeatherText text) {
		if (weatherTexts == null) {
			weatherTexts = new LinkedList<WeatherText>();
		}
		weatherTexts.add(new WeatherText(text));
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	public final static class WeatherText {

		private String head;

		private String text;

		WeatherText() {
		}

		WeatherText(WeatherText other) {
			this.head = other.head;
			this.text = other.text;
		}

		public String getHead() {
			return head;
		}

		public void setHead(String head) {
			this.head = StringUtils.clean(head);
		}

		public String getText() {
			return text;
		}

		public void appendText(String text) {
			if(StringUtils.isEmpty(this.text)) {
				this.text = StringUtils.clean(text);
			} else {
				this.text = this.text + StringUtils.clean(text);
			}
		}
	}
}
