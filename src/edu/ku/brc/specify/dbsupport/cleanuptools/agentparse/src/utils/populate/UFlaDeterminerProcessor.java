/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class UFlaDeterminerProcessor extends AgentProcessor {
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fieldsUsed
	 */
	public UFlaDeterminerProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		String recordID = rec.getKey();
		//String valueThatGotParsed = rec.getValue();
		ParseInfo parseInfo = chooseParseInfo(rec.getKey(), rec.getParses());
		//There is only one case of multiple recs for identifier
		//And using the second one, which will occur here, is the correct thing to do
		//More generally, a group agent would need to be created...
		Integer id = getId(rec.getValue(), parseInfo.getRecords());
		updateRecord(recordID, id);
	}

	/**
	 * @param grpName
	 * @return
	 * @throws Exception
	 */
	protected String getGroupWhere(String grpName) throws Exception {
		//XXX doesn't take division into account
		return "LastName = " + getSqlTextForValue(grpName) + " AND AgentType = " 
				+ getCodeForAgentType("group");
	}
	
	/**
	 * @param sourceText
	 * @param records
	 * @return
	 * @throws Exception
	 */
	protected Integer getId(String sourceText, List<RawRecord> records) throws Exception {
		Integer result = null;
		if (records.size() == 1) {
			result = getId(records.get(0));
		} else {
			String where = getGroupWhere(sourceText);
			// XXX divisionID or whatever scoping might apply???
			String sql = "select AgentID from agent where " + where;
			ResultSet rs = selStmt.executeQuery(sql);
			while (rs.next()) {
				if (result != null) {
					// throw new ProcessorException("more than one match for " + where, false);
					System.out.println("more than one match for " + where);
				} else {
					result = rs.getInt(1);
				}
			}
			if (result == null) {
				result = createGroupRecord(sourceText, records);
			}
			rs.close();
		}
		return result;
	}

	/**
	 * @param sourceText
	 * @param records
	 * @return
	 * @throws Exception
	 */
	protected Integer createGroupRecord(String sourceText, List<RawRecord> records) throws Exception {
		List<Pair<String, String>> raw = new ArrayList<Pair<String, String>>();
		//raw.add(new Pair<String,String>("AgentType", getCodeForAgentType("group")));
		raw.add(new Pair<String,String>("AgentType", "group"));
		raw.add(new Pair<String,String>("LastName", sourceText));
		Integer result = createRecord(new RawRecord(raw));
		int ord = 0;
		for (RawRecord rec : records) {
			Integer memberId = getId(rec);
			if (memberId != null) {
				String sql = "insert into groupperson(TimestampCreated, Version, CreatedByAgentID, DivisionID, GroupID, MemberID, OrderNumber) "
						+ "values(now(), 0, 1, (select DivisionID from agent a where a.AgentID = " + result + "), "
						+ result + ", " + memberId + ", " + ord++ + ")";
				insStmt.executeUpdate(sql);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#chooseParseInfo(java.lang.String, java.util.List)
	 */
	@Override
	protected ParseInfo chooseParseInfo(String key, List<ParseInfo> parses)
			throws Exception {
		try {
			return super.chooseParseInfo(key, parses);
		} catch (ProcessorException pex) {
			if (!pex.isFatal() && parses.size() > 1) {
				System.out.println("Using first parse for key: " + key);
				return parses.get(0); //we know this is OK for determiners...
			} else {
				throw pex;
			}
		}
	}

	/**
	 * @param determinationID
	 * @param AgentID
	 */
	protected void updateRecord(String recordID, Integer agentID) throws Exception {
		String sql = "update determination set DeterminerID = " + agentID + " where DeterminationID = " + recordID;
		insStmt.executeUpdate(sql);
	}
}
