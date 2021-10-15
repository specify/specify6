package utils.populate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class UFlaIvpCollectorProcessor extends AgentProcessor {
	protected final List<Pair<String, Object>> newCollectorDefaults;
	protected final List<Pair<String, Object>> newAccessionDefaults;
	protected final List<Pair<String, Object>> newAccessionAgentDefaults;
	protected final boolean battleCustody;

	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param newCollectorDefaults
	 * @param fieldsUsed
	 */
	public UFlaIvpCollectorProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newCollectorDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed);
		this.newCollectorDefaults = newCollectorDefaults;
		this.newAccessionDefaults = null;
		this.newAccessionAgentDefaults = null;
		this.battleCustody = false;
	}

	
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 * @param newCollectorDefaults
	 * @param newAccessionDefaults
	 * @param newAccessionAgentDefaults
	 */
	public UFlaIvpCollectorProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fldsUsed,
			List<Pair<String, Object>> newCollectorDefaults,
			List<Pair<String, Object>> newAccessionDefaults,
			List<Pair<String, Object>> newAccessionAgentDefaults) {
		super(connection, newAgentDefaults, fldsUsed);
		this.newCollectorDefaults = newCollectorDefaults;
		this.newAccessionDefaults = newAccessionDefaults;
		this.newAccessionAgentDefaults = newAccessionAgentDefaults;
		this.battleCustody = true;
	}


	/**
	 * @param rec
	 * @return
	 */
	protected boolean hasCustodialChain(SourceRecord rec) {
		//should be good enough...
		return rec.getValue().contains("/");
	}
	
	/**
	 * @param r
	 * @return
	 */
	protected boolean isCustodian(RawRecord r, boolean defaultVal) {
		boolean result = defaultVal;
		Pair<String, String> cust = r.getFld("IsCustodian");
		if (cust != null) {
			return Boolean.valueOf(cust.getSecond());
		}
		return result;
	}
	
	/**
	 * @param r
	 * @return
	 */
	protected String getAccWhere(SourceRecord r) throws Exception {
		return "AccessionNumber = " + getSqlTextForValue(getAccessionNumber(r));
	}
	
	/**
	 * @param r
	 * @return
	 * @throws Exception
	 */
	protected String getAccessionNumber(SourceRecord r) throws Exception {
		//assuming embedded collectingevents
		
		/*String sql = "select catalognumber from collectionobject co where collectingeventid = "
				+ r.getKey();
		ResultSet rs = selStmt.executeQuery(sql);
		rs.next();
		String result = rs.getString(1);  
		rs.close();
		//probably need to trim leading zeros and stuff
		return "Custody Chain " + result;*/
		
		//XXX this works because no custody chains are longer than 60 chars, currently
		return r.getValue();
	}
	
	/**
	 * @param r
	 * @return
	 * @throws Exception
	 */
	protected Integer getAccId(SourceRecord s, RawRecord r) throws Exception {
		String where = getAccWhere(s);
		String sql = "select AccessionID from accession where " + where;
		ResultSet rs = selStmt.executeQuery(sql);
		Integer result = null;
		while (rs.next()) {
			if (result != null) {
				throw new ProcessorException("more than one match for " + where, false);	
			} else {
				//result = null;//rs.getInt(1); only return key for newly created accIds.
				//break;
				return null;
			}
		}
		rs.close();
		if (result == null) {
			result = createAccRecord(s);
		}
		return result;
	}
	 
	/**
	 * @param r
	 * @return
	 * @throws Exception
	 *
	 * created a new accession record for the custody chain represented by r's value.
	 * 
	 * NOTE! New accessions are not connected to collectionobjects by this class.
	 * After processing all records the following must be executed:
	 * 
	 *update collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid
     *inner join accession a on a.accessionnumber = ce.verbatimlocality set co.accessionid = a.accessionid;
     *
	 */
	protected Integer createAccRecord(SourceRecord r) throws Exception {
		
		Pair<String, String> fldsVals = getFldsValsForInsert(newAccessionDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", AccessionNumber";
			vals += ", " + getSqlTextForValue(getAccessionNumber(r));

		String sql = "insert into accession(" + flds + ") values(" + vals + ")";
		insStmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet key = insStmt.getGeneratedKeys();
		if (!key.next()) {
			key.close();
			throw new ProcessorException("Insert failed: " + sql, true);
		}
		Integer result = key.getInt(1);
		key.close();
		return result;
	}

	/**
	 * @param order
	 * @return
	 */
	protected String getCustodianRoleOrder(int order) {
		return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(order-1, order);
	}
	
	/**
	 * @param accId
	 * @param agentId
	 * @throws Exception
	 */
	protected void addAccAgentRecord(Integer accId, Integer agentId, int order) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newAccessionAgentDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", AccessionID, AgentID, Role";
		vals += ", " + accId + "," + agentId + ", " + "'Custodian " + getCustodianRoleOrder(order) + "'";
		String sql = "insert into accessionagent(" + flds + ") values(" + vals + ")";
		insStmt.executeUpdate(sql);
	}
	
	protected int getCustodyChainLen(SourceRecord rec) {
		int result = -1;
		if (hasCustodialChain(rec)) {
			result = 0;
			String rev = StringUtils.reverse(rec.getValue());
			int mark = rev.indexOf("/");
			while (mark != -1) {
				result++;
				rev = rev.substring(mark + 1);
				mark = rev.indexOf("/");
				if (mark == -1) {
					mark = rev.indexOf("&");
				} 
				if (mark == -1) {
					mark = rev.indexOf(" ,");
				}
			}
		}
		return result;
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
		int collectorOrder = 0;
		//int custodyOrder = isCustodian(records.get(records.size()-1), false) ? records.size() : records.size() - 1;
		int custodyChainLen = getCustodyChainLen(rec);
		int custodyOrder = custodyChainLen;
		List<Integer> ids = new ArrayList<Integer>(records.size());
		boolean checkCustody = hasCustodialChain(rec);
		Integer accId = null;
		for (int rIdx = 0; rIdx < records.size(); rIdx++) {
			RawRecord r = records.get(rIdx);
			Integer id = getId(r);
			if (ids.indexOf(id) == -1) {
				if (!checkCustody || !isCustodian(r, rIdx < custodyChainLen)) {
					addCollectorRecord(collEventID, id, collectorOrder++);
				} else {
					if (rIdx == 0) {
						accId = getAccId(rec, r);
					}
					if (accId != null) {
						addAccAgentRecord(accId, id, custodyOrder--);
					}
				}
				ids.add(id);
			}
		}
	}

	/**
	 * @param ReferenceWorkID
	 * @param agentID
	 * @param order
	 */
	protected void addCollectorRecord(String collEventID, Integer agentID, int order) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newCollectorDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		flds += ", CollectingEventID, AgentID, OrderNumber";
		vals += ", " + collEventID + "," + agentID + ", " + order;
		String sql = "insert into collector(" + flds + ") values(" + vals + ")";
		insStmt.executeUpdate(sql);
	}

}
