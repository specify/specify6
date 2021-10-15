/**
 * 
 */
package utils.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tnoble
 *
 */
public class Record 
{
	private final String table;
	private final List<BaseFieldValue> fields;
	/**
	 * @param table
	 */
	public Record(String table, List<BaseFieldValue> fields) {
		super();
		this.table = table;
		this.fields = fields;
	}
	/**
	 * @return the table
	 */
	public String getTable() {
		return table;
	}
	/**
	 * @return the fields
	 */
	public List<BaseFieldValue> getFields() {
		return fields;
	}
	
	/**
	 * @param fldName
	 * @return
	 */
	public BaseFieldValue getField(String fldName) {
		for (BaseFieldValue fv : fields) {
			if (fv.getField().equals(fldName)) {
				return fv;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public String getRecordType() {
		List<String> results = new ArrayList<String>();
		for (BaseFieldValue fv : fields) {
			String t = fv.getRecordType();
			if (results.indexOf(t) == -1) {
				results.add(t);
			}
		}
		if (results.size() == 0) {
			return null;
		} else {
			String result = results.get(0);
			for (int r = 1; r < results.size(); r++) {
				result += "/" + results.get(r);  //this is probably a bad sign.
			}
			return result;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof Record) {
			Record arg = (Record) arg0;
			if (getTable().equals(arg.getTable())) {
				if (getFields().size() == arg.getFields().size()) {
					for (BaseFieldValue fld : getFields()) {
						BaseFieldValue aFld = arg.getField(fld.getField());
						if (aFld != null) {
							if (!fld.equals(aFld)) {
								return false;
							}
						}
					}
					return true;
				}
			}
		}
		return false;
	}	
	
}
