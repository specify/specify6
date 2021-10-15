/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.util.ArrayList;
import java.util.List;


/**
 * @author timo
 *
 */
public class Dater {
			
	public static enum MultipleMatchType  {NONE, MULTIPLE, FIRST, LAST};
	
	final List<DaterExpression> exps;
	final MultipleMatchType matchType;
	final DaterPreProcessor prep;
	
	/**
	 * @param regexps
	 */
	public Dater(List<DaterExpression> regexps) {
		this(regexps, MultipleMatchType.FIRST, new DaterPreProcessor());
	}

	/**
	 * @param regexps
	 */
	public Dater(List<DaterExpression> regexps,  MultipleMatchType matchType) {
		this(regexps, matchType, new DaterPreProcessor());
	}

	/**
	 * @param regexps
	 * @param matchType
	 */
	public Dater(List<DaterExpression> regexps, MultipleMatchType matchType, DaterPreProcessor prep) {
		super();
		this.exps = regexps;
		this.matchType = matchType;
		this.prep = prep;
	}

	/**
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public List<Undateable> getDate(String text) throws DaterException {
		return textToUndate(text);
	}
	
	/**
	 * @param text
	 * @return
	 * @throws Exception
	 */
	protected List<Undateable> textToUndate(String exp) throws DaterException {
		List<Undateable> result =  new ArrayList<Undateable>();
		List<DaterExpression> matches = new ArrayList<DaterExpression>();
		String text = prep.preprocess(exp);
		for (DaterExpression r : exps) {
			if (r.matches(text)) {
				matches.add(r);
			}
		}
		if (matches.size() == 0) {
			throw new DaterException("No matches for: " + text, text);
		} else if (matches.size() >= 1) {
			if (matches.size() > 1) {
				if (matchType == MultipleMatchType.FIRST) {
					for (int m = 1; m < matches.size(); m++) {
						matches.remove(m);
					}
				} else if (matchType == MultipleMatchType.FIRST) {
					for (int  m = matches.size()-1; m > 1; m--) {
						matches.remove(m);
					}
				} else if (matchType == MultipleMatchType.NONE) {
					throw new DaterException("Multiple matches for: " + text, text);
				}
			}
			for (DaterExpression e : matches) {
				result.add(e.getDate(text));
			}
		}
		return result;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
