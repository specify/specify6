/**
 * 
 */
package utils.parse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author timo
 *
 */
public class DerivedParseResult extends ParseResult implements Comparable<DerivedParseResult>
{
	protected DerivationTree derivation;
	
	/**
	 * @param typeName
	 * @param attributes
	 */
	public DerivedParseResult(String typeName, List<ParseResultAttribute> attributes, DerivationTree derivation) {
		super(typeName, attributes);
		this.derivation = derivation;
	}

	/**
	 * @return the derivation
	 */
	public DerivationTree getDerivation() {
		return derivation;
	}

	/**
	 * @param derivation the derivation to set
	 */
	public void setDerivation(DerivationTree derivation) {
		this.derivation = derivation;
	}

	/* (non-Javadoc)
	 * @see utils.parse.ParseResult#getRecords()
	 */
	@Override
	public List<Record> getRecords() {
		if (derivation != null) {
			return derivation.getRecords();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareTo(DerivedParseResult o2) {
		if (equals(o2)) return 0;
		if (getDerivation().getRecords().size() < o2.getDerivation().getRecords().size()) return -1;
		return 1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		/* returns true if the records in obj are equal to this object's records.*/
		if (obj instanceof DerivedParseResult) {
			DerivedParseResult arg = (DerivedParseResult) obj;
			if (getDerivation().getRecords().size() == arg.getDerivation().getRecords().size()) {
				List<Integer> matched = new ArrayList<Integer>();
				for (Record r : getDerivation().getRecords()) {
					int m = matchRecord(r, arg.getDerivation().getRecords(), matched);
					if (m != -1) {
						matched.add(m);
					} else {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	

	/**
	 * @param r
	 * @param toMatch
	 * @param alreadyMatched
	 * @return
	 */
	protected int matchRecord(Record r, List<Record> toMatch, List<Integer> alreadyMatched) {
		int result = -1;
		for (int i = 0; i < toMatch.size(); i++) {
			if (alreadyMatched.indexOf(i) == -1) {
				if (r.equals(toMatch.get(i))) {
					return i;
				}
			}
		}
		return result;
	}
	
}
