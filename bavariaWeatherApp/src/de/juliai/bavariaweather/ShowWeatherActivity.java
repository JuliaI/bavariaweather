package de.juliai.bavariaweather;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * 
 * @author JuliaI
 * 
 */
public class ShowWeatherActivity extends Activity {

	private static final String URL_BAVARIA = "http://www.br.de/wetter/action/bayernwetter/bayern.do";

	private static final String WEATHER_DATA = "weatherData";

	private SharedPreferences sharedPrefs;

	private Editor prefsEditor;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_weather);

		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefsEditor = sharedPrefs.edit();

		showWeatherInfo();
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
	 * retrieves weatherData from shared preferences and shows them in the
	 * webView
	 */
	private void showWeatherInfo() {
		String weatherData = sharedPrefs.getString(WEATHER_DATA, null);

		// Log.i(this.getClass().getName(), "got weather-data: " + weatherData);

		if (weatherData == null) {
			weatherData = "";
		}

		final WebView webView = (WebView) findViewById(R.id.webView);
		webView.loadDataWithBaseURL(null, weatherData, "text/html", "UTF8",
				null);
	}

	/**
	 * loads weather data from the internet. Then stores them in weatherData in
	 * the shared preferences
	 */
	private void reloadWeatherInfo() {
		// Log.i(this.getClass().getName(), "started reloading weather info");

		final URL urlBayern;
		try {
			urlBayern = new URL(URL_BAVARIA);
		} catch (MalformedURLException e) {
			Log.e(this.getClass().getName(),
					"MalformedURLException happened while creating the bavaria-url! Message: "
							+ e.getMessage());
			return;
		}

		final DownloadAsyncTask taskBayern = new DownloadAsyncTask();
		taskBayern.execute(urlBayern);
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private final class DownloadAsyncTask extends AsyncTask<URL, Void, String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String doInBackground(URL... urls) {
			try {
				final HttpURLConnection conn = (HttpURLConnection) urls[0]
						.openConnection();
				conn.connect();
				final InputStream stream = conn.getInputStream();
				final InputStreamReader is = new InputStreamReader(stream,
						Charset.forName("UTF8"));
				final StringBuilder sb = new StringBuilder();
				final BufferedReader br = new BufferedReader(is);
				String read = br.readLine();

				while (read != null) {
					sb.append(read);
					read = br.readLine();
				}

				return extractWeather(sb.toString());

			} catch (IOException e) {
				Log.e(this.getClass().getName(),
						"IOException happened during downloading the page! Message: "
								+ e.getMessage());
				Toast.makeText(
						ShowWeatherActivity.this,
						"Probleme beim Laden der Seite. Stelle sicher, dass eine Internetverbindung besteht.",
						Toast.LENGTH_LONG).show();
				return null;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void onPostExecute(String result) {

			if (result != null && result.length() > 0) {
				prefsEditor.putString(WEATHER_DATA, result);
				prefsEditor.commit();
			}

			showWeatherInfo();
		}

		/**
		 * @param source
		 * @return
		 */
		private String extractWeather(String source) {
			// Log.i(this.getClass().getName(),
			// "started extractWeather from source with length " +
			// source.length());

			if (source == null || source.length() <= 0) {
				return null;
			}

			// fix malformed html
			source = source.replaceAll("</</div>", "</p></div>");
			source = source.replaceAll("&uuml;", "ü");
			source = source.replaceAll("&auml;", "ä");

			try {
				final SAXParserFactory factory = SAXParserFactory.newInstance();
				final SAXParser saxParser = factory.newSAXParser();
				final WeatherHandler handler = new WeatherHandler();
				saxParser.parse(new ByteArrayInputStream(source.getBytes()),
						handler);

				final WeatherData weatherData = handler.getData();

				return buildHtml(weatherData);

			} catch (ParserConfigurationException e) {
				Log.e(this.getClass().getName(),
						"ParserConfigurationException happened during parsing! Message: "
								+ e.getMessage());
				return null;
			} catch (SAXException e) {
				Log.e(this.getClass().getName(),
						"SAXException happened during parsing! Message: "
								+ e.getMessage());
				Toast.makeText(
						ShowWeatherActivity.this,
						"Probleme beim Parsen. Versuch es in ein paar Minuten noch einmal.",
						Toast.LENGTH_LONG).show();
				return null;
			} catch (IOException e) {
				Log.e(this.getClass().getName(),
						"IOException happened during parsing! Message: "
								+ e.getMessage());
				return null;
			}
		}

		/**
		 * @param weatherData
		 * @return
		 */
		private String buildHtml(final WeatherData weatherData) {
			final StringBuilder sb = new StringBuilder();

			sb.append("<div class=\"weatherBlock\">");

			sb.append("<h1>");
			sb.append(weatherData.getTitle());
			sb.append("</h1>");

			sb.append("<p style=\"font-size: 0.8em\">");
			sb.append(weatherData.getSubtitle1());
			sb.append("<br/>");
			sb.append(weatherData.getTimestamp());
			sb.append("</p>");

			sb.append("<p>");
			sb.append(weatherData.getSubtitle2());
			sb.append("</p>");

			for (final WeatherText text : weatherData.getWeatherTexts()) {
				sb.append("<div class=\"weatherText\">");

				sb.append("<h3>");
				sb.append(text.getHead());
				sb.append("</h3>");

				sb.append("<p>");
				sb.append(text.getText());
				sb.append("</p>");

				sb.append("</div>");
			}

			sb.append("</div>");

			return sb.toString();
		}
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private final static class WeatherHandler extends DefaultHandler {

		private WeatherData data;

		private boolean foundH1BrWetter;

		private boolean foundPVorhersage;

		private boolean foundDivSchlagzeile;

		private boolean foundSchlagzeileTextBlockHead;

		private boolean foundDivTimeStamp;

		private boolean foundDivTimeStampP;

		private boolean foundSpanUhrzeit;

		private boolean foundDivWetterTextBlock;

		private boolean foundWetterTextBlockHead;

		private boolean foundWetterTextBlockText;

		private WeatherText currentText;

		public WeatherHandler() {
			this.data = new WeatherData();
		}

		public WeatherData getData() {
			return data;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			// get class attribute first, because it will be needed
			String classAttrValue = getClassAttribute(attributes);

			// check tags
			if (qName.equalsIgnoreCase("h1")) {
				if (classAttrValue != null
						&& classAttrValue.equals("br-wetter")) {
					foundH1BrWetter = true;
				}
			} else if (qName.equalsIgnoreCase("p")) {
				if (classAttrValue != null
						&& classAttrValue.equals("vorhersage")) {
					foundPVorhersage = true;
				}
				if (classAttrValue == null && foundDivTimeStamp) {
					foundDivTimeStampP = true;
					foundDivTimeStamp = false;
				}
			} else if (qName.equalsIgnoreCase("div")) {
				if (classAttrValue != null) {
					if (classAttrValue.equals("Schlagzeile")) {
						foundDivSchlagzeile = true;
					} else if (classAttrValue.equals("TimeStamp")) {
						foundDivTimeStamp = true;
					} else if (classAttrValue.equals("WetterTextBlock")) {
						foundDivWetterTextBlock = true;
					}
				}
			} else if (qName.equalsIgnoreCase("span")) {
				if (classAttrValue != null) {
					if (classAttrValue.equals("uhrzeit")) {
						foundSpanUhrzeit = true;
					} else if (classAttrValue.equals("TextBlockHead")) {
						if (foundDivWetterTextBlock) {
							foundWetterTextBlockHead = true;
						} else if (foundDivSchlagzeile) {
							foundSchlagzeileTextBlockHead = true;
							foundDivSchlagzeile = false;
						}
					} else if (foundDivWetterTextBlock
							&& classAttrValue.equals("TextBlockText")) {
						foundWetterTextBlockText = true;
						foundDivWetterTextBlock = false;
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			if (foundH1BrWetter) {
				data.setTitle(new String(ch, start, length));
				foundH1BrWetter = false;
			}

			if (foundPVorhersage) {
				data.setSubtitle1(new String(ch, start, length));
				foundPVorhersage = false;
			}

			if (foundSchlagzeileTextBlockHead) {
				data.setSubtitle2(new String(ch, start, length));
				foundSchlagzeileTextBlockHead = false;
			}

			if (foundDivTimeStampP) {
				data.setTimestamp(new String(ch, start, length));
				foundDivTimeStampP = false;
			}

			if (foundSpanUhrzeit) {
				final String wholeTimestamp = data.getTimestamp() + " "
						+ new String(ch, start, length);
				data.setTimestamp(wholeTimestamp);
				foundSpanUhrzeit = false;
			}

			if (foundWetterTextBlockHead) {
				currentText = new WeatherText();
				currentText.setHead(new String(ch, start, length));
				foundWetterTextBlockHead = false;
			}

			if (foundWetterTextBlockText) {
				currentText.setText(new String(ch, start, length));
				data.addWeatherText(currentText);
				currentText = null;
				foundWetterTextBlockText = false;
			}
		}

		/**
		 * @param attributes
		 * @return
		 */
		private String getClassAttribute(Attributes attributes) {
			String classAttrValue = null;

			if (attributes.getLength() > 0) {
				classAttrValue = attributes.getValue("class");
			}

			return classAttrValue;
		}
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private final static class WeatherData {

		private String title; // h1, class="br-wetter"

		private String subtitle1; // p, class="vorhersage"

		private String subtitle2; // div, class="Schlagzeile"

		private String timestamp; // div, class="TimeStamp"

		private List<WeatherText> weatherTexts; // div, class="WetterTextBlock"

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public String getSubtitle1() {
			return subtitle1;
		}

		public void setSubtitle1(String subtitle1) {
			this.subtitle1 = subtitle1;
		}

		public String getSubtitle2() {
			return subtitle2;
		}

		public void setSubtitle2(String subtitle2) {
			this.subtitle2 = subtitle2;
		}

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
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
	}

	/**
	 * 
	 * @author JuliaI
	 * 
	 */
	private final static class WeatherText {

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
			this.head = head;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
}
