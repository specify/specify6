/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class USGSUtils extends UtilitaryBase {

	/**
	 * @param con
	 */
	public USGSUtils(Connection con) {
		super(con);
	}

	protected String removeParenthesiezures(String str) throws Exception {
		String result = "";
		byte[] strb = str.getBytes();
		int siezed = 0;
		for (int b = 0; b < strb.length; b++) {
			byte[] currb = new byte[1];
			currb[0] = strb[b];
			String current = new String(currb);
			if (")".equals(current)) {
				if (siezed <= 0) {
					throw new Exception(str + ": " + " close paren with match open paren.");
				} else {
					siezed--;
				}
			} else if ("(".equals(current)) {
				siezed++; 
			}
			if (siezed == 0) {
				result += current;
			}
		}
		return result;
	}
	/**
	 * @param resumes
	 * @return fauna split by commas
	 */
	protected List<Pair<String, String[]>> parseResumeOfFauna1(List<String> resumes) throws Exception {
		List<Pair<String, String[]>> result = new ArrayList<Pair<String, String[]>>();
		for (String resume : resumes) {
			String unsiezedResume = removeParenthesiezures(resume);
			result.add(new Pair<String, String[]>(resume, unsiezedResume.split(",")));
		}
		return result;
	}
	
	/**
	 * @param fauna
	 * @return list of fldname - value pairs
	 * Assumes fauna consists of Gen Sp Author for now.
	 */
	protected List<Pair<String, String>> parseFauna(String fauna) {
		List<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
		String[] pieces = fauna.split(" ");
		if (pieces.length > 0) {
			result.add(new Pair<String, String>("genus", pieces[0]));
			if (pieces.length > 1) {
				if (pieces[1].toLowerCase().equals(pieces[1])) {
					result.add(new Pair<String, String>("species", pieces[1]));
				}
				if (pieces.length > 2) {
				}
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
