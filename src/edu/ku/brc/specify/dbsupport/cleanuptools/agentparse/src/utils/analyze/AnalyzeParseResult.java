/**
 * 
 */
package utils.analyze;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.parse.BaseFieldValue;
import utils.parse.ParseResult;
import utils.parse.Record;

/**
 * @author tnoble
 *
 */
public class AnalyzeParseResult {
	public static final String REC_TYPE_HDR = "$$RecType$$";
	
	public int getRecords(ParseResult pr, String tblName) {
		//Assuming for now tblName is always the same and irrelevant
		return pr.getRecords().size();
	}

	public List<String> getFieldNames(ParseResult pr, String tblName) {
		//Assuming for now tblName is always the same and irrelevant
		List<String> result = new ArrayList<String>();
		for (Record r : pr.getRecords()) {
			String recType = r.getRecordType();
			if (StringUtils.isNotBlank(recType)) {
				if (result.indexOf(REC_TYPE_HDR) == -1) {
					result.add(REC_TYPE_HDR);
				}
			}
			for (BaseFieldValue fv : r.getFields()) {
				if (result.indexOf(fv.getField()) == -1) {
					result.add(fv.getField());
				}
			}
		}
		return result;
	}
}
