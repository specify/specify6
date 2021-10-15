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
public class UFlaDonorProcessor extends UFlaDeterminerProcessor {
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fieldsUsed
	 */
	public UFlaDonorProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.UFlaDeterminerProcessor#updateRecord(java.lang.String, java.lang.Integer)
	 */
	@Override
	protected void updateRecord(String recordID, Integer agentID)
			throws Exception {
		String sql = "update conservevent set TreatedByAgentID = " + agentID + " where ConservEventID = " + recordID;
		insStmt.executeUpdate(sql);
	}

	
}
