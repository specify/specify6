/**
 * 
 */
package utils.parse;

/**
 * @author tnoble
 *
 */
public abstract class BaseFieldValue {
	protected final String table;
	protected final String field;
	protected final String recordType;
	/**
	 * @param table
	 * @param field
	 * @param recordType
	 */
	public BaseFieldValue(String table, String field, String recordType) {
		super();
		this.table = table;
		this.field = field;
		this.recordType = recordType;
	}

	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}

	/**
	 * @return the field
	 */
	public String getField() {
		return field;
	}

	
	/**
	 * @return the recordType
	 */
	public String getRecordType() {
		return recordType;
	}

	public abstract String getValue();

}
