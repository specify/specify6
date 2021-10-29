/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public abstract class AgentProcessor extends RecordProcessor {
	protected final List<Pair<String, Object>> newAgentDefaults;
	protected final List<String> fieldsUsed;

	protected String[] fldLens = {"LastName,120"}; //quick,dirty,done dirt cheap

	protected Map<String, Integer> maxLens = new HashMap<String, Integer>();
	
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 */
	public AgentProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fldsUsed) {
		super(connection);
		this.newAgentDefaults = newAgentDefaults;
		this.fieldsUsed = new ArrayList<String>();
		for (String field : fldsUsed) {
			String fixedField = fixupFldName(field);
			if (this.fieldsUsed.indexOf(fixedField) == -1) {
				this.fieldsUsed.add(fixedField);
			}
		}
		for (String l : fldLens) {
			String[] spec = l.split(",");
			maxLens.put(spec[0].trim(), Integer.valueOf(spec[1]));
		}
	}

	/**
	 * @param fldName
	 * @return
	 */
	protected String fixupFldName(String fldName) {
		// Dirt cheap but good enough for now.
		if (fldName.endsWith("_1")) {
			return fldName.replaceAll("_1", "");
		}
		if (fldName.endsWith("_2")) {
			return fldName.replaceAll("_2", "");
		}
		if (fldName.equals("RecordType")) {
			return "AgentType";
		}
		return fldName;
	}

	/**
	 * @param fldName
	 * @return
	 */
	protected String deFixupFldName(String fldName) {
		// Dirt cheap but good enough for now.
		if (fldName.equals("AgentType")) {
			return "RecordType";
		}
		return fldName;
	}

	/**
	 * @param r
	 * @return
	 */
	protected Integer getId(RawRecord r) throws Exception {
		
		if ("null".equalsIgnoreCase(getAgentType(r))) {
			return null;
		} else {

			String where = getWhere(r);
			// XXX divisionID or whatever scoping might apply???
			String sql = "select AgentID from agent where " + where;
			ResultSet rs = selStmt.executeQuery(sql);
			Integer result = null;
			while (rs.next()) {
				if (result != null) {
					// throw new ProcessorException("more than one match for " +
					// where, false);
					System.out.println("more than one match for " + where);
				} else {
					result = rs.getInt(1);
				}
			}
			if (result == null) {
				result = createRecord(r);
			}
			rs.close();
			return result;
		}
	}

	/**
	 * @param val
	 * @return
	 * @throws Exception
	 */
	protected String getCodeForAgentType(String val) throws Exception {
		if ("organization".equalsIgnoreCase(val)) {
			return "0";
		} else if ("person".equalsIgnoreCase(val)) {
			return "1";
		} else if ("other".equalsIgnoreCase(val)) {
			return "2";
		} else if ("group".equalsIgnoreCase(val)) {
			return "3";
		} else {
			throw new Exception("Unknow agent type: " + val);
		}
	}
	
	/**
	 * @param fld
	 * @return
	 */
	protected String getSqlTextForValue(Pair<String, String> fld) throws Exception {
		if ("AgentType".equalsIgnoreCase(fixupFldName(fld.getFirst()))) {
			return getCodeForAgentType(fld.getSecond());
		} else {
			return getSqlTextForValue(fld.getSecond(), maxLens.get(fld.getFirst()));
		}
	}
	
	/**
	 * @param r
	 */
	protected String getAgentType(RawRecord r) {
		for (int f = 0; f < r.getFldCount(); f++) {
			Pair<String, String> fld = r.getFld(f);
			if ("agenttype".equalsIgnoreCase(fixupFldName(fld.getFirst()))) {
				return fld.getSecond();
			}
		}
		return "";
	}
	
	/**
	 * @param r
	 * @return
	 */
	protected String guessAgentType(RawRecord r) {
		for (int f = 0; f < r.getFldCount(); f++) {
			Pair<String, String> fld = r.getFld(f);
			if ("firstname".equalsIgnoreCase(fixupFldName(fld.getFirst()))) {
				return "person";
			}
		}
		return "other";
	}
	
	/**
	 * @param r
	 * @return
	 */
	protected String getWhere(RawRecord r) throws Exception {
		String where = "";
		boolean guessedType = false;
		for (int f = 0; f < r.getFldCount(); f++) {
			Pair<String, String> fld = r.getFld(f);
			if (!"".equals(where)) {
				where += " AND ";
			}
			where += fixupFldName(fld.getFirst()) + "=" + getSqlTextForValue(fld);
		}
		if ("".equals(getAgentType(r))) {
			if (!"".equals(where)) {
				where += " AND ";
			}
			where += " AgentType =" + getCodeForAgentType(guessAgentType(r));
			guessedType = true;
		}
		for (String fldUsed : fieldsUsed) {
			if (r.getFld(deFixupFldName(fldUsed)) == null) {
				if (!"AgentType".equalsIgnoreCase(fixupFldName(fldUsed))) {
					if (!"".equals(where)) {
						where += " AND ";
					}
					Object defVal = getDefSearchValForFld(fldUsed);
					if ("$$null".equals(defVal)) {
						where += fldUsed + " IS NULL";
					} else {
						where +=  fldUsed + "=" + getSqlTextForValue(defVal);
					}
				}
			}
		}
		if (guessedType) {
			System.out.println("Guessed agent type for: " + where);
		}
		return where;
	}

	/**
	 * @param r
	 * @return
	 */
	protected Integer createRecord(RawRecord r) throws Exception {
		Pair<String, String> fldsVals = getFldsValsForInsert(newAgentDefaults);
		String flds = fldsVals.getFirst();
		String vals = fldsVals.getSecond();
		boolean guessedType = false;
		for (int f = 0; f < r.getFldCount(); f++) {
			Pair<String, String> fld = r.getFld(f);
			if (!"".equals(flds)) {
				flds += ", ";
				vals += ", ";
			}
			flds += fixupFldName(fld.getFirst());
			vals += getSqlTextForValue(fld);
		}
		if ("".equals(getAgentType(r))) {
			if (!"".equals(flds)) {
				flds += ", ";
				vals += ", ";
			}
			flds += "AgentType";
			vals += getCodeForAgentType(guessAgentType(r));
			guessedType = true;
		}
		
		String sql = "insert into agent(" + flds + ") values(" + vals + ")";
		//System.out.println(sql);
		if (guessedType) {
			System.out.println("Guessed agent type for: " + sql);
		}
		insStmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
		ResultSet key = insStmt.getGeneratedKeys();
		try {
			if (!key.next()) {
				throw new ProcessorException("Insert failed: " + sql, true);
			}
			Integer result = key.getInt(1);
			return result;
		} finally {
			key.close();
		}
	}

	/**
	 * @param fldsVals
	 * @return
	 */
	protected Pair<String, String> getFldsValsForInsert(List<Pair<String, Object>> fldsVals) {
		String flds = "";
		String vals = "";
		for (Pair<String, Object> fld : fldsVals) {
			if (!"".equals(flds)) {
				flds += ",";
				vals += ",";
			}
			flds += fld.getFirst();
			vals += getSqlTextForValue(fld.getSecond());
		}
		return new Pair<String, String>(flds, vals);
	}

	/**
	 * @param fld
	 * @return
	 */
	protected Object getDefSearchValForFld(String fld) {
		return "$$null";
	}
		
}
