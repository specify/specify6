/**
 * 
 */
package utils.populate;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

/**
 * @author tnoble
 *
 */
public class ParseInfo {
	protected final List<RawRecord> records;
	
	public ParseInfo(Element parseElem) {
		records = new ArrayList<RawRecord>();
		for (Object recObj : parseElem.selectNodes("record")) {
			records.addAll(RawRecord.getRawRecords((Element)recObj));
		}
	}

	/**
	 * @return the records
	 */
	public List<RawRecord> getRecords() {
		return records;
	}
	
}
