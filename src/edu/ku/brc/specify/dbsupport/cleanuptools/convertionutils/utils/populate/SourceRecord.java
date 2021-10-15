/**
 * 
 */
package utils.populate;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author tnoble
 *
 */
public class SourceRecord {
	protected final String key;
	protected final String value;
	protected final List<ParseInfo> parses;
	
	public SourceRecord(Element element) {
		key = XMLHelper.getAttr(element, "key", "null");
		Object val = element.selectSingleNode("value");
		value = ((Node)val).getText();
		parses = new ArrayList<ParseInfo>();
		for (Object parseObj : element.selectNodes("parse")) {
			parses.add(new ParseInfo((Element)parseObj));
		}
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the parses
	 */
	public List<ParseInfo> getParses() {
		return parses;
	}
	
}
