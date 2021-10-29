/**
 * 
 */
package utils.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tnoble
 *
 */
public class FieldValue extends BaseFieldValue
{
	protected final List<Token> attributes = new ArrayList<Token>();
	
	/**
	 * @param table
	 * @param field
	 */
	public FieldValue(String table, String field, String recordType) {
		super(table, field, recordType);
	}


	/**
	 * @return the attributes
	 */
	public List<Token> getAttributes() {
		return attributes;
	}
		
	/* (non-Javadoc)
	 * @see utils.parse.BaseFieldValue#getValue()
	 */
	public String getValue() {
		String result = "";
		for (Token t : getAttributes()) {
			if (!"".equals(result)) {
				//result += " - ";
				result += "";
			}
			result += t.getValue() /*+ "[" + t.getName() + "]"*/;
		}
		return result.trim(); //XXX trimRight would be better?
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof FieldValue) {
			FieldValue arg = (FieldValue) obj;
			if (getAttributes().size() == arg.getAttributes().size()) {
				List<Token> atts = cloneAndSort(getAttributes());
				List<Token> argAtts = cloneAndSort(arg.getAttributes());
				for (int t = 0; t < atts.size(); t ++) {
					if (!atts.get(t).equals(argAtts.get(t))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;		
	}
	
	/**
	 * @param atts
	 * @return
	 */
	protected List<Token> cloneAndSort(List<Token> atts) {
		if (atts == null) return null;
		
		List<Token> result = new ArrayList<Token>(atts.size());
		for (Token t : atts) {
			result.add(t);
		}
		Collections.sort(result, new Comparator<Token>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Token arg0, Token arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		
		});
		return result;
	}
}
