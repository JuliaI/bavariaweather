package de.juliai.bavariaweather;

/**
 * 
 * @author JuliaI
 * 
 */
public class StringUtils {

	/**
	 * @param string
	 * @return
	 */
	public static final boolean isNotEmpty(final String string) {
		return !isEmpty(string);
	}

	/**
	 * @param string
	 * @return
	 */
	public static final boolean isEmpty(final String string) {
		return string == null || string.length() <= 0;
	}

	/**
	 * @param inStr
	 * @return
	 */
	public static final String clean(final String inStr) {
		return inStr.replace("\t", "").replaceAll("\\s+", " ").trim();
	}
}
