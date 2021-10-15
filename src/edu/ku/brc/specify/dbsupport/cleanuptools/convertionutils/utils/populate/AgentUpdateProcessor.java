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
public class AgentUpdateProcessor extends AgentProcessor {

	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 */
	public AgentUpdateProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<String> fldsUsed) {
		super(connection, newAgentDefaults, fldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String agentID = rec.getKey();
		ParseInfo parseInfo = chooseParseInfo(agentID, rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		if (records.size() > 0) {
			RawRecord r = records.get(0);
			boolean isGroup = records.size() > 1;
			updateAgent(agentID, r, isGroup);
			if (isGroup) {
				for (int m = 0; m < records.size(); m++) {
					addGroupMember(agentID, records.get(m), m);
				}
			}
		}
	}
	
	/**
	 * @param agentID
	 * @param r
	 * @param isGroup
	 * @throws Exception
	 */
	protected void updateAgent(String agentID, RawRecord r, boolean isGroup) throws Exception {
		String sql = "";
		if (isGroup) {
			sql = "update agent set agenttype = " + getCodeForAgentType("group") + " where agentid = " + agentID; 
		} else {
			for (int f = 0; f < r.getFldCount(); f++) {
				Pair<String, String> fld = r.getFld(f);
				if ("".equals(sql)) {
						sql += " set ";
				} else {
					sql += ", ";
				}
				sql += fixupFldName(fld.getFirst()) + "=" + getSqlTextForValue(fld);
			}
			if (!"".equals(sql)) {
				sql = "update agent " + sql + " where agentid=" + agentID;
			}
		}
		if (!"".equals(sql)) {
			insStmt.executeUpdate(sql);
		}
	}
	
	/**
	 * @param name
	 * @return
	 */
	protected Object getDefaultByName(String name) {
		for (Pair<String, Object> d : newAgentDefaults) {
			if (d.getFirst().equalsIgnoreCase(name)) {
				return d.getSecond();
			}
		}
		return null;
	}
	
	/**
	 * @param groupID
	 * @param m
	 * @param order
	 * @throws Exception
	 */
	protected void addGroupMember(String groupID, RawRecord m, int order) throws Exception {
		Integer memberId = getId(m);
		String sql = "insert into groupperson(TimestampCreated, Version, CreatedByAgentID, DivisionID,OrderNumber,MemberID,GroupID) values("
				+ "now(), 0, " + getDefaultByName("CreatedByAgentID") + ", " + getDefaultByName("DivisionID") + ", "
				+ order + ", " + memberId + ", " + groupID + ")";
		insStmt.executeUpdate(sql);
	}
}
