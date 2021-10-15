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
public class GeoRefferProcessor extends AgentProcessor {

	public GeoRefferProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String geocoordID = rec.getKey();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		RawRecord r = records.get(0);
		Integer id = getId(r);
		if (!updateGeoCoordDetail(geocoordID, id)) {
			throw new Exception("Failed updating geocoorddetail " + geocoordID + " with agentid " + id);
		}
		if (records.size() > 1) {
			System.out.println("More than one georeffer for " + geocoordID);
		}
	}

	/**
	 * @param geocoordID
	 * @param agentID
	 * @return
	 * @throws Exception
	 */
	protected boolean updateGeoCoordDetail(String geocoordID, Integer agentID) throws Exception {
		String sql = "update geocoorddetail set agentid = " + agentID + " where geocoorddetailid = " + geocoordID;
		try {
			insStmt.executeUpdate(sql);
			return true;
		} catch (Exception ex) {
			System.out.println("Exception occurred for: " + sql);
			return false;
		}
	}

}
