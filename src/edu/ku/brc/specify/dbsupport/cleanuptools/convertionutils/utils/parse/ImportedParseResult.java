/**
 * 
 */
package utils.parse;

import java.util.List;

/**
 * @author tnoble
 *
 */
public class ImportedParseResult extends ParseResult {

	protected final List<Record> records;
	
	/**
	 * @param typeName
	 * @param attributes
	 */
	public ImportedParseResult(String typeName,
			List<ParseResultAttribute> attributes, List<Record> records) {
		super(typeName, attributes);
		this.records = records;
	}
	
	/* (non-Javadoc)
	 * @see utils.parse.ParseResult#getRecords()
	 */
	@Override
	public List<Record> getRecords() {
		return records;
	}
	
}
