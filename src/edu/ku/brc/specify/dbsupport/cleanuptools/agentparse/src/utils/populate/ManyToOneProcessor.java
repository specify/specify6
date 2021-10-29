/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class ManyToOneProcessor extends AgentProcessor {
	protected final String tableName;
	protected final String foreignKeyName;
	
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 * @param tableName
	 * @param foreignKeyName
	 */
	public ManyToOneProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fldsUsed,
			String tableName, String foreignKeyName) {
		super(connection, newAgentDefaults, fldsUsed);
		this.tableName = tableName;
		this.foreignKeyName = foreignKeyName;
	}
	
	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String recId = rec.getKey();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		for (RawRecord r : parseInfo.getRecords()) {
			Integer id = getId(r);
			updateRecord(recId, id);
		}
	}

	/**
	 * @param recId
	 * @param agentID
	 * @throws Exception
	 */
	protected void updateRecord(String recId, Integer agentID) throws Exception {
		String sql = "update " + tableName + " set " + foreignKeyName + "=" + agentID 
				+ " where " + tableName + "ID=" + recId;
		insStmt.executeUpdate(sql);
	}

}
