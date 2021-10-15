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
public class UFlaIvpAuthorProcessor extends AgentProcessor {
	protected final List<Pair<String, Object>> newAuthorDefaults;
		
	/**
	 * @param connection
	 */
	public UFlaIvpAuthorProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newAuthorDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
		this.newAuthorDefaults = newAuthorDefaults;
	}
		
	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String referenceWorkID = rec.getKey();
		//String valueThatGotParsed = rec.getValue();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		int order = 0;
		for (RawRecord r : parseInfo.getRecords()) {
			Integer id = getId(r);
			addAuthorRecord(referenceWorkID, id, order++);
		}
	}
	
	/**
	 * @param ReferenceWorkID
	 * @param agentID
	 * @param order
	 */
	protected void addAuthorRecord(String referenceWorkID, Integer agentID, int order) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newAuthorDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", ReferenceWorkID, AgentID, OrderNumber";
		vals += ", " + referenceWorkID + "," + agentID + ", " + order;
		String sql = "insert into author(" + flds + ") values(" + vals + ")";
		try {
			insStmt.executeUpdate(sql);
		} catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex) {
			ex.printStackTrace();
		}
	}
		
}
