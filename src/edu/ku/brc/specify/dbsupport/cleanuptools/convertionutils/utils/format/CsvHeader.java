/**
 * 
 */
package utils.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tnoble
 *
 */
public class CsvHeader {
	protected final List<CsvColHeader> hdr;
	
	/**
	 * @param hdrs
	 */
	public CsvHeader(String[] hdrs) {
		hdr = new ArrayList<CsvColHeader>();
		for (String h : hdrs) {
			hdr.add(new CsvColHeader(h));
		}
		Collections.sort(hdr, new Comparator<CsvColHeader>(){
			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(CsvColHeader arg0, CsvColHeader arg1) {
				if (arg0.getHeaderType() < arg1.getHeaderType()) {
					return -1;
				} else if (arg0.getHeaderType() > arg1.getHeaderType()) {
					return 1;
				} else if (arg0.getHeaderType() != CsvColHeader.FIELD) {
					return 0;
				} else {
					if (arg0.getParseNum() < arg1.getParseNum()) {
						return -1;
					} else if (arg0.getParseNum() > arg1.getParseNum()) {
						return 1;
					} else {
						if (arg0.getRecNum() < arg1.getRecNum()) {
							return -1;
						} else if (arg0.getRecNum() > arg1.getRecNum()) {
							return 1;
						} else {
							if (arg0.getRecTypeFld() && !arg1.getRecTypeFld()) {
								return -1;
							} else if (!arg0.getRecTypeFld() && arg1.getRecTypeFld()) {
								return 1;
							} else {
								return arg0.getName().compareTo(arg1.getName());
							}
						}
					}
				}
			}
			
		});
	}
	
	/**
	 * @return
	 */
	public int getParseCount() {
		int result = -1;
		boolean allNull = true;
		for (CsvColHeader h : hdr) {
			if (h.getParseNum() != null) {
				allNull = false;
				if (h.getParseNum() > result) {
					result = h.getParseNum();
				}
			}
		}
		if (allNull) {
			return 1;
		} else {
			return result + 1;
		}
	}
	
	/**
	 * @param parseNum
	 * @return
	 */
	public List<CsvColHeader> getParse(int parseNum) {
		List<CsvColHeader> result = new ArrayList<CsvColHeader>();
		for (CsvColHeader h : hdr) {
			if (h.getParseNum() != null && h.getParseNum() == parseNum) {
				result.add(h);
			}
		}
		return result;
	}
	
	/**
	 * @param parseNum
	 * @return
	 */
	public int getRecordCount(int parseNum) {
		int result = -1;
		for (CsvColHeader h : getParse(parseNum)) {
			if (h.getRecNum() > result) {
				result = h.getRecNum();
			}
		}
		return result + 1;
	}
	
	/**
	 * @param parseNum
	 * @param recNum
	 * @return
	 */
	public List<CsvColHeader> getRecord(int parseNum, int recNum) {
		List<CsvColHeader> result = new ArrayList<CsvColHeader>();
		for (CsvColHeader h : getParse(parseNum)) {
			if (h.getRecNum() == recNum) {
				result.add(h);
			}
		}
		return result;
	}
	
	/**
	 * @param parseNum
	 * @param recNum
	 * @return
	 */
	public CsvColHeader getRecordType(int parseNum, int recNum) {
		for (CsvColHeader h : getRecord(parseNum, recNum)) {
			if (h.getRecTypeFld()) {
				return h; 
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public CsvColHeader getInput() {
		return getHead(CsvColHeader.INPUT);
	}

	/**
	 * @param headType
	 * @return first header with type headType
	 */
	public CsvColHeader getHead(int headType) {
		for (CsvColHeader h : hdr) {
			if (h.getHeaderType() == headType) {
				return h;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public CsvColHeader getKey() {
		return getHead(CsvColHeader.KEY_VALUE);
	}
	
	/**
	 * @return
	 */
	public CsvColHeader getException() {
		return getHead(CsvColHeader.EXCEPTION);
	}
	
}
