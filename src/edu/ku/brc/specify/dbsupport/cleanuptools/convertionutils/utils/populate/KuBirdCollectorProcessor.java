/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class KuBirdCollectorProcessor extends CollectorProcessor {
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param newCollectorDefaults
	 * @param fieldsUsed
	 */
	
	protected final String methodFieldName;
	
	public KuBirdCollectorProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newCollectorDefaults,
			List<String> fieldsUsed,
			String methodFieldName) {
		super(connection, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		this.methodFieldName = methodFieldName;
	}

	/* (non-Javadoc)
	 * @see utils.populate.CollectorProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		//assuming 1 record.
		String agentType = records.get(0).getFldVal("RecordType");
		if (agentType == null || !"Method".equalsIgnoreCase(agentType)) {
			super.process(rec);
		} else {
			String collEventID = rec.getKey();
			String method = records.get(0).getFldVal("LastName");
			if (method != null) {
				updateMethod(collEventID, method.replaceAll("'", "''"));
			} else {
				System.out.println("Null method for CE: " + collEventID);
			}
		}
	}

	protected boolean updateMethod(String collEventID, String method) throws Exception {
		String sql = "update collectingevent set " + methodFieldName + "=trim(concat(ifnull(" + methodFieldName + ",''), ' ', '" 
				+ method + "')) where collectingeventid=" + collEventID;
		try {
			insStmt.executeUpdate(sql);
			return true;
		} catch (Exception ex) {
			System.out.println("Exception occurred for: " + sql);
			return false;
		}
	}

}
