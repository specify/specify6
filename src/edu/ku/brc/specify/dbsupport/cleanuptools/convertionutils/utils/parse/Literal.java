/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public class Literal {
	protected final String text;
	protected final String parsedText;
	protected final String format;
	protected final String min;
	protected final String max;
	protected final boolean caseSensitive;
	
	/**
	 * @param text
	 * @param parsedText
	 * @param format
	 * @param min
	 * @param max
	 */
	public Literal(String text, String parsedText, String format, String min, String max, boolean caseSensitive) {
		super();
		this.text = text;
		this.parsedText = parsedText;
		this.format = format;
		this.min = min;
		this.max = max;
		this.caseSensitive = caseSensitive;
	}
	
	/**
	 * @param text
	 * @param parsedText
	 */
	public Literal(String text, String parsedText) {
		super();
		this.text = text;
		this.parsedText = parsedText;
		this.format = null;
		this.min = null;
		this.max = null;
		this.caseSensitive = true;
	}
	/**
	 * @param text
	 */
	public Literal(String text) {
		super();
		this.text = text;
		this.parsedText = text;
		this.format = null;
		this.min = null;
		this.max = null;
		this.caseSensitive = true;
	}
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}
	/**
	 * @return the parsedText
	 */
	public String getParsedText() {
		return parsedText;
	}
	
	/**
	 * @return the format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @return the min
	 */
	public String getMin() {
		return min;
	}

	/**
	 * @return the max
	 */
	public String getMax() {
		return max;
	}

	/**
	 * @return the caseSensitive
	 */
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	/**
	 * @param toMatch
	 * @return
	 */
	public boolean match(String toMatch) {
		if (format == null) {
			return isCaseSensitive() ? text.equals(toMatch) : text.equalsIgnoreCase(toMatch);
		} else {
			//XXX use real format strings and expression matching or something
				return toMatch.matches(format);
				/*if (toMatch.length() != format.length()) {
					return false;
				}
				try {
					Integer dummy = Integer.valueOf(toMatch);
					if (min != null && dummy.compareTo(Integer.valueOf(min)) < 0) {
						return false;
					}
					if (max != null && dummy.compareTo(Integer.valueOf(max)) > 0) {
						return false;
					}
					return true;
				} catch (NumberFormatException nex) {
					return false;
				}*/
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getText() + (getText().equals(getParsedText()) ? "" : " | " + getParsedText()) + " case matters: " + isCaseSensitive();
	}
	
	
}
