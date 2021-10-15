/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author tnoble
 *
 */
public abstract class RecordProcessor {
	protected final Connection connection;

	protected Statement selStmt;
	protected Statement insStmt;

	/**
	 * @param connection
	 */
	public RecordProcessor(Connection connection) {
		super();
		this.connection = connection;
	}
	
	abstract public void process(SourceRecord rec) throws Exception;
	
	/**
	 * @throws Exception
	 */
	public void startProcessing() throws Exception {
		selStmt = connection.createStatement();
		insStmt = connection.createStatement();
	}
	/**
	 * @throws Exception
	 */
	public void endProcessing() throws Exception {
		selStmt.close();
		insStmt.close();
	}
	
	/**
	 * @param parses
	 * @return
	 * @throws Exception
	 */
	protected ParseInfo chooseParseInfo(String key, List<ParseInfo> parses) throws Exception {
		//XXX pretty much assuming only 1 parse is present by this point in the process
		if (parses.size() > 1) {
			throw new ProcessorException("Which parse is the right parse for record " + key + "?", false);
		} else if (parses.size() == 0) {
			throw new ProcessorException("Nothing parsed for record " + key + ".", false);
		}
		return parses.get(0);
	}

	/**
	 * @param val
	 * @return
	 */
	protected String getSqlTextForValue(Object val) {
		return getSqlTextForValue(val, null);
	}
	/**
	 * @param val
	 * @return
	 */
	protected String getSqlTextForValue(Object val, Integer maxLen) {
		if (val instanceof String) {
			if (!((String) val).startsWith("$$")) {
				String valString = val.toString();
				if (maxLen != null && valString.length() > maxLen) {
					valString = valString.substring(0, maxLen);
					System.out.println(val.toString() + " TRUNCATED!!!!!! " + valString);
				}
				return "'" + BasicSQLUtils.escapeStringLiterals(valString, "'") + "' collate utf8_bin";
			} else {
				return ((String) val).substring(2);
			}
		} else {
			return val.toString();
		}
	}

}
