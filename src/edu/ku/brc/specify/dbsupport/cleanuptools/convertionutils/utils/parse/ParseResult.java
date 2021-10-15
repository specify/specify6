/**
 * 
 */
package utils.parse;

import java.util.List;

/**
 * @author tnoble
 *
 */
public abstract class ParseResult {
	protected String typeName;
	protected List<ParseResultAttribute> attributes;
	/**
	 * @param typeName
	 * @param attributes
	 */
	public ParseResult(String typeName, List<ParseResultAttribute> attributes) {
		super();
		this.typeName = typeName;
		this.attributes = attributes;
	}
	/**
	 * @return the typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	/**
	 * @return the attributes
	 */
	public List<ParseResultAttribute> getAttributes() {
		return attributes;
	}
	/**
	 * @param attributes the attributes to set
	 */
	public void setAttributes(List<ParseResultAttribute> attributes) {
		this.attributes = attributes;
	}

	public abstract List<Record> getRecords();
}
