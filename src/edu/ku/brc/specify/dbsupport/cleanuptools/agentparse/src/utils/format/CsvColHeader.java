/**
 * 
 */
package utils.format;

/**
 * @author tnoble
 *
 */
public class CsvColHeader {
	public final static int KEY_VALUE = 0;
	public final static int INPUT = 1;
	public final static int EXCEPTION = 2;
	public final static int FIELD = 3;
	public final static int OTHER = 4;
	
	public final static String INPUT_HEADER_NAME = "Input";
	public final static String KEY_HEADER_NAME = "SrcKey";
	public final static String EXCEPTION_HEADER_NAME = "ParseException";
	public final static String NOTE_HEADER_NAME = "Notes";
	public final static String TYPE_HEADER_NAME = "RecordType";
	
	protected final String text;
	protected final String name;
	protected final Integer parseNum;
	protected final Integer recNum;
	protected final Boolean recTypeFld;
	protected final int headerType;
	
	/**
	 * @param name
	 * @param parseNum
	 * @param recNum
	 */
	public CsvColHeader(String text, String name, Integer parseNum, Integer recNum, Boolean recTypeFld, 
			int headerType) {
		super();
		this.text = text;
		this.name = name;
		this.parseNum = parseNum;
		this.recNum = recNum;
		this.recTypeFld = recTypeFld;
		this.headerType = headerType;
	}
	
	/**
	 * @param hdrText
	 */
	public CsvColHeader(String hdrText) {
		super();
		Integer parseNum = null;
		Integer recNum = null;
		String name = hdrText;
		Boolean recTypeFld = false;
		int headerType = OTHER;
		if (INPUT_HEADER_NAME.equalsIgnoreCase(hdrText)) {
			headerType = INPUT;
			name = INPUT_HEADER_NAME;
		} else if (KEY_HEADER_NAME.equalsIgnoreCase(hdrText)) {
			headerType = KEY_VALUE; 
			name = KEY_HEADER_NAME;
		} else if (EXCEPTION_HEADER_NAME.equalsIgnoreCase(hdrText)) {
			headerType = EXCEPTION; 
			name = EXCEPTION_HEADER_NAME;
		}	else if (NOTE_HEADER_NAME.equalsIgnoreCase(hdrText)) {
				headerType = EXCEPTION; 
				name = EXCEPTION_HEADER_NAME;
		} else {
			try {
				String number = hdrText.substring(0, hdrText.indexOf(" "));
				String[] nums = number.split("\\.");
				if (nums.length == 2) {
					parseNum = Integer.valueOf(nums[0]) - 1;
					recNum = Integer.valueOf(nums[1]) - 1;
					name = hdrText.substring(hdrText.indexOf(" ") + 1);
					if (TYPE_HEADER_NAME.equalsIgnoreCase(name)) {
						recTypeFld = true; 
						name = TYPE_HEADER_NAME;
					} else {
						headerType = FIELD;
					}
				}
			} catch (NumberFormatException nex) {
				headerType = OTHER;
			}
		}
		this.text = hdrText;
		this.name = name;
		this.parseNum = parseNum;
		this.recNum = recNum;
		this.recTypeFld = recTypeFld;
		this.headerType = headerType;
	}
	
	/**
	 * @return the text
	 */
	public String getText() {
		return text;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the parseNum
	 */
	public Integer getParseNum() {
		return parseNum;
	}
	/**
	 * @return the recNum
	 */
	public Integer getRecNum() {
		return recNum;
	}

	/**
	 * @return the headerType
	 */
	public int getHeaderType() {
		return headerType;
	}

	/**
	 * @return the recTypeFld
	 */
	public Boolean getRecTypeFld() {
		return recTypeFld;
	}

}

