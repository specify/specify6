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
public class PrepperProcessor extends AgentProcessor {
	
	private List<String> prepsWithManyPreppers = new ArrayList<String>();
	
	public PrepperProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String prepID = rec.getKey();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		RawRecord r = records.get(0);
		Integer id = getId(r);
		if (!updatePrepper(prepID, id)) {
			throw new Exception("Failed updating preparation " + prepID + " with agentid " + id);
		}
		if (records.size() > 1) {
			System.out.println("More than one prepper for " + prepID);
			prepsWithManyPreppers.add(prepID);
		}
	}

	/**
	 * @param geocoordID
	 * @param agentID
	 * @return
	 * @throws Exception
	 */
	protected boolean updatePrepper(String prepID, Integer agentID) throws Exception {
		String sql = "update preparation set preparedbyid = " + agentID + " where preparationid = " + prepID;
		try {
			insStmt.executeUpdate(sql);
			return true;
		} catch (Exception ex) {
			System.out.println("Exception occurred for: " + sql);
			return false;
		}
	}
	
	/**
	 * @return
	 */
	public List<String> getPrepsWithManyPreppers() {
		return this.prepsWithManyPreppers;
	}

}
