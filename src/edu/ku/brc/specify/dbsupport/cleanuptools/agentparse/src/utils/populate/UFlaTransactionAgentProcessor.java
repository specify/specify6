/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class UFlaTransactionAgentProcessor extends AgentProcessor {
	protected final List<Pair<String, Object>> newTransAuthDefaults;
	protected final String role;
	protected final String transTbl;
	protected final String transAuthTbl;
	public UFlaTransactionAgentProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newTransAgentDefaults, String role, String transTbl, String transAuthTbl,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
		this.newTransAuthDefaults = newTransAgentDefaults;		
		this.role = role;
		this.transTbl = transTbl;
		this.transAuthTbl = transAuthTbl;
	}
	
	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String transID = rec.getKey();
		//String valueThatGotParsed = rec.getValue();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		for (int rIdx = 0; rIdx < records.size(); rIdx++) {
			RawRecord r = records.get(rIdx);
			Integer id = getId(r);
			addTransAuthRecord(transID, id);
		}
	}

	/**
	 * @param transID
	 * @param agentID
	 * @throws Exception
	 */
	protected void addTransAuthRecord(String transID, Integer agentID) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newTransAuthDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", " + transTbl + "ID, AgentID, Role";
		vals += ", " + transID + "," + agentID + ", '" + role + "'";
		String sql = "insert into " + transAuthTbl + "(" + flds + ") values(" + vals + ")";
		insStmt.executeUpdate(sql);
	}

}
