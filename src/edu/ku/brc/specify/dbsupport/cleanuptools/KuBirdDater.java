/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class KuBirdDater extends Dater {

	public List<UnDateComponent> getParts(List<Pair<String,Object>> rowVals) {
		List<UnDateComponent> result = new ArrayList<UnDateComponent>();
		for (Pair<String, Object> v : rowVals) {
			if ("Day".equalsIgnoreCase(v.getFirst())) {
				result.add(new UnDay("", v.getSecond() == null ? null : v.getSecond().toString()));
			} else if ("Month".equalsIgnoreCase(v.getFirst())) {
				result.add(new UnMonth("", v.getSecond() == null ? null : v.getSecond().toString()));
			} else if ("Year".equalsIgnoreCase(v.getFirst())) {
				result.add(new UnYear("", v.getSecond() == null ? null : v.getSecond().toString()));
			}
		}
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}


	public KuBirdDater(List<DaterExpression> regexps) {
		super(regexps);
	}

	public KuBirdDater(List<DaterExpression> regexps, MultipleMatchType matchType) {
		super(regexps, matchType);
	}

	public KuBirdDater(List<DaterExpression> regexps, MultipleMatchType matchType, DaterPreProcessor prep) {
		super(regexps, matchType, prep);
	}
}
