/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public class UnderivedFieldValue extends BaseFieldValue {
	
	protected final String value;
	
	/**
	 * @param table
	 * @param field
	 * @param recordType
	 */
	public UnderivedFieldValue(String table, String field, String recordType, String value) {
		super(table, field, recordType);
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see utils.parse.BaseFieldValue#getValue()
	 */
	@Override
	public String getValue() {
		return value;
	}

	
}
