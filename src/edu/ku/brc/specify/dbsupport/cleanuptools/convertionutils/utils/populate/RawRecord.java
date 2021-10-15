/**
 * 
 */
package utils.populate;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 * This is really nothing more than a properties structure, for now.
 */
public class RawRecord {
	protected final List<Pair<String, String>> raw;
	
	/**
	 * @param recordElement
	 */
	private RawRecord(Element recordElement) {
		super();
		raw = new ArrayList<Pair<String, String>>();
		for (Object fldObj : recordElement.selectNodes("field")) {
			Element fldElem = (Element)fldObj;
			raw.add(new Pair<String, String>(
					fldElem.attributeValue("name"),
					((Node)fldElem.selectSingleNode("value")).getText())); 
		}
	}

	/**
	 * @param raw
	 */
	public RawRecord(List<Pair<String, String>> raw) {
		super();
		this.raw = raw;
	}
	
	/**
	 * @param idx
	 */
	protected void fixupFldNames(int idx) {
		for (int f = raw.size() -1; f >= 0; f--) {
			Pair<String, String> fld = raw.get(f);
			//XXX cheap but good enough
			String fldName = fld.getFirst();
			if (fldName.endsWith("_1")) {
				if (idx == 1) {
					fld.setFirst(fldName.replaceAll("_1", ""));
				} else {
					raw.remove(f);
				}
			}
			if (fldName.endsWith("_2")) {
				if (idx == 2) {
					fld.setFirst(fldName.replaceAll("_2", ""));
				} else {
					raw.remove(f);
				}
			}
		}
	}
	
	/**
	 * @param recordElement
	 * @return
	 */
	public static List<RawRecord> getRawRecords(Element recordElement) {
		List<RawRecord> result = new ArrayList<RawRecord>();
		RawRecord r1 = new RawRecord(recordElement);
		boolean multipleRecs = false;
		for (int f = 0; f < r1.getFldCount(); f++) {
			//still kind of cheating but, currently, endsWith("_2") is good enough
			if (r1.getFld(f).getFirst().endsWith("_2")) {
				multipleRecs = true;
				break;
			}
		}
		result.add(r1);
		if (multipleRecs) {
			result.add(new RawRecord(recordElement));
		}		
		int recNum = 1;
		for (RawRecord r : result) {
			r.fixupFldNames(recNum++);
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public int getFldCount() {
		return raw.size();
	}
	
	/**
	 * @param i
	 * @return
	 */
	public Pair<String, String> getFld(int i) {
		return raw.get(i);
	}
	
	/**
	 * @param i
	 * @return
	 */
	public String getFldVal(int i) {
		return raw.get(i).getSecond();
	}
	
	/**
	 * @param fldName
	 * @return
	 */
	public Pair<String, String> getFld(String fldName) {
		for (Pair<String, String> fld : raw) {
			if (fldName.equals(fld.getFirst())) {
				return fld;
			}
		}
		return null;
	}
	
	/**
	 * @param fldName
	 * @return
	 */
	public String getFldVal(String fldName) {
		for (Pair<String, String> fld : raw) {
			if (fldName.equals(fld.getFirst())) {
				return fld.getSecond();
			}
		}
		return null;
	}
	
}
