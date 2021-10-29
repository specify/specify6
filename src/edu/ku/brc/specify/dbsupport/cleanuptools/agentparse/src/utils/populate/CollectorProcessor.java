/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class CollectorProcessor extends AgentProcessor {
	protected final List<Pair<String, Object>> newCollectorDefaults;

	static public List<String> lsuBaddies = new ArrayList<String>();
	
	public CollectorProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newCollectorDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
		this.newCollectorDefaults = newCollectorDefaults;
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String collEventID = rec.getKey();
		//String valueThatGotParsed = rec.getValue();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		int collectorOrder = 1;
		List<Integer> ids = new ArrayList<Integer>(records.size());
		for (int rIdx = 0; rIdx < records.size(); rIdx++) {
			RawRecord r = records.get(rIdx);
			Integer id = getId(r);
			if (id != null && ids.indexOf(id) == -1) {
				boolean addedIt = addCollectorRecord(collEventID, id, collectorOrder);
				if (addedIt) {
					collectorOrder++;
				}
				ids.add(id);
			}
		}
	}

	protected boolean addCollectorRecord(String collEventID, Integer agentID, int order) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newCollectorDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", CollectingEventID, AgentID, OrderNumber";
		vals += ", " + collEventID + "," + agentID + ", " + order;
		String sql = "insert into collector(" + flds + ") values(" + vals + ")";
		try {
			insStmt.executeUpdate(sql);
			return true;
		} catch (Exception ex) {
			System.out.println("Exception occurred for: " + sql);
			lsuBaddies.add(collEventID + ", " + agentID + ", " + order);
			return false;
		}
	}

}
