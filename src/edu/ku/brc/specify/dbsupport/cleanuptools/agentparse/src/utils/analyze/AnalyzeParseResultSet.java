package utils.analyze;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import utils.parse.ParseResult;
import utils.parse.Parsing;

public class AnalyzeParseResultSet {
	
	public static final String TOTAL_ROWS = "TotalRows";
	public static final String MAX_PARSES = "MaxParses";
	public static final String MAX_RECORDS = "MaxRecords";
	public static final String FIELDS = "Fields";

	/**
	 * @return
	 */
	private Map<String, Object> buildEmptyResult() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(TOTAL_ROWS, 0);
		result.put(MAX_PARSES, 0);
		result.put(MAX_RECORDS, 0);
		result.put(FIELDS, new ArrayList<String>());
		return result;
	}
	
	/**
	 * @param parsings
	 * @return
	 */
	public  Map<String, Object> analyze(List<Parsing> parsings) {
		Map<String, Object> result = buildEmptyResult();
		result.put(TOTAL_ROWS, parsings.size());
		for (Parsing p : parsings) {
			analyze(p.getParses(), result);
			if (p.getParses().size() > (Integer)result.get(MAX_PARSES)) {
				result.put(MAX_PARSES, p.getParses().size());
			}
		}
		return result;
	}
	
	/**
	 * @param prs
	 * @param result
	 */
	@SuppressWarnings("unchecked")
	protected void analyze(List<ParseResult> prs, Map<String, Object> result) {
		AnalyzeParseResult a = new AnalyzeParseResult();
		for (ParseResult pr : prs) {
			int r = a.getRecords(pr, null);
			List<String> flds = a.getFieldNames(pr, null);
			if ((Integer)result.get(MAX_RECORDS) < r) {
				result.put(MAX_RECORDS, r);
			}
			List<String> currentFlds = (List<String>)result.get(FIELDS);
			for (String fld : flds) {
				if (currentFlds.indexOf(fld) == -1) {
					currentFlds.add(fld);
				}
			}
		}
	}
}
