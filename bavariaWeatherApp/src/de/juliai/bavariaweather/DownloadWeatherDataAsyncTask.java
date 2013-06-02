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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import android.util.Log;
import android.widget.Toast;

/**
 * 
 * @author JuliaI
 * 
 */
public class DownloadWeatherDataAsyncTask extends
		AsyncTask<URL, Void, AsyncTaskResult> {

	private static final String DIGITS = "column ([0-9]+)";

	private final Pattern pattern;

	private final Activity thisActivity;

	private final SharedPreferences sharedPrefs;

	private final Region region;

	/**
	 * constructor
	 * 
	 * @param activity
	 * @param sharedPrefs
	 * @param region
	 */
	public DownloadWeatherDataAsyncTask(final Activity activity,
			final SharedPreferences sharedPrefs, final Region region) {
		this.thisActivity = activity;
		this.sharedPrefs = sharedPrefs;
		this.region = region;
		this.pattern = Pattern.compile(DIGITS);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPreExecute() {
		region.setWeatherData("Wetterdaten werden geladen...");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected AsyncTaskResult doInBackground(URL... urls) {
		final AsyncTaskResult result = new AsyncTaskResult();

		// don't care about the given urls,
		// just use the url from the given region
		final URL url;
		try {
			url = new URL(region.getUrl());

		} catch (MalformedURLException e) {
			Log.e(this.getClass().getName(),
					"MalformedURLException happened while creating the url! Message: "
							+ e.getMessage());
			result.setErrorMessage("MalformedURLException");
			return result;
		}

		try {
			final HttpURLConnection conn = (HttpURLConnection) url
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

			return extractWeather(sb.toString(), result);

		} catch (IOException e) {
			Log.e(this.getClass().getName(),
					"IOException happened during downloading the page! Message: "
							+ e.getMessage());

			result.setErrorMessage("Probleme beim Laden der Seite. Stelle sicher, dass eine Internetverbindung besteht.");
			return result;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(AsyncTaskResult result) {
		// the html-data
		final String htmlData = result.getData();
		if (htmlData != null && htmlData.length() > 0) {

			final Editor prefsEditor = sharedPrefs.edit();
			prefsEditor.putString(region.getPreferencesKey(), htmlData);
			prefsEditor.commit();

			region.setWeatherData(htmlData);

		} else {
			// if no new data could be retrieved: show at least old data
			region.setWeatherData(sharedPrefs.getString(
					region.getPreferencesKey(), ""));
		}

		// show the error-message in toast
		final String errorMessage = result.getErrorMessage();
		if (errorMessage != null && errorMessage.length() > 0) {
			Toast.makeText(thisActivity,
					region.getPreferencesKey() + ": " + errorMessage,
					Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * @param source
	 * @param result
	 * @return
	 */
	private AsyncTaskResult extractWeather(String source,
			final AsyncTaskResult result) {
		if (source == null || source.length() <= 0) {
			result.setErrorMessage("Keine Daten erhalten. Versuch es in ein paar Minuten noch einmal.");
			return result;
		}

		// fix malformed html so that it can be parsed
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

			String html = buildHtml(weatherData);
			// fix html for WebView
			html = html.replaceAll("ä", "&auml;");
			html = html.replaceAll("ö", "&ouml;");
			html = html.replaceAll("ü", "&uuml;");
			html = html.replaceAll("ß", "&szlig;");

			result.setData(html);
			return result;

		} catch (ParserConfigurationException e) {
			Log.e(this.getClass().getName(),
					"ParserConfigurationException happened during parsing! Message: "
							+ e.getMessage());
			result.setErrorMessage("ParserConfigurationException");
			return result;

		} catch (SAXException e) {
			final String exceptionMsg = e.getMessage();

			Log.e(this.getClass().getName(),
					"SAXException happened during parsing! Message: "
							+ exceptionMsg);

			// try to show problematic position in source
			final Matcher matcher = pattern.matcher(exceptionMsg);
			if (matcher.find()) {
				final String group = matcher.group(1);
				if (group != null && group.length() > 0) {
					int column = Integer.parseInt(group);

					int start = column - 20;
					if (start < 0) {
						start = 0;
					}

					int end = column + 20;
					if (end > (source.length() - 1)) {
						end = source.length() - 1;
					}

					result.setErrorMessage("Probleme beim Parsen. \n("
							+ exceptionMsg + ") \n("
							+ source.substring(start, end) + ")");
					return result;
				}
			}

			result.setErrorMessage("Probleme beim Parsen. Versuch es in ein paar Minuten noch einmal. \n("
					+ exceptionMsg + ")");
			return result;

		} catch (IOException e) {
			Log.e(this.getClass().getName(),
					"IOException happened during parsing! Message: "
							+ e.getMessage());
			result.setErrorMessage("IOException");
			return result;
		}
	}

	/**
	 * @param weatherData
	 * @return
	 */
	private String buildHtml(final WeatherData weatherData) {
		final StringBuilder sb = new StringBuilder();

		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"content-type\" content=\"text/html;charset=UTF-8\" />");
		sb.append("</head>");
		sb.append("<body>");

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

			final String head = text.getHead();
			if (head != null && head.length() > 0) {
				sb.append("<h3>");
				sb.append(head);
				sb.append("</h3>");
			}

			sb.append("<p>");
			sb.append(text.getText());
			sb.append("</p>");

			sb.append("</div>");
		}

		sb.append("</div>");

		sb.append("</body>");
		sb.append("</html>");

		return sb.toString();
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
				if (currentText == null) {
					currentText = new WeatherText();
				}

				currentText.setHead(new String(ch, start, length));

				if (currentText.getText() != null) {
					data.addWeatherText(currentText);
					currentText = null;
				}

				foundWetterTextBlockHead = false;
			}

			if (foundWetterTextBlockText) {
				if (currentText == null) {
					currentText = new WeatherText();
				}

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
