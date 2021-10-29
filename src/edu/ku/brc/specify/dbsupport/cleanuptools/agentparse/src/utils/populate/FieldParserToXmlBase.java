/**
 * 
 */
package utils.populate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.apache.commons.io.FileUtils;

import utils.format.ParseResultsFormatter;
import utils.parse.Parsing;

/**
 * @author tnoble
 *
 */
public abstract class FieldParserToXmlBase {
	protected final String server;
	protected final String db;
	protected final String user;
	protected final String pw;
	protected final String tbl;
	protected final String keyFld;
	protected final String fld;
	protected final String outputFile;

	
	/**
	 * @param server
	 * @param db
	 * @param user
	 * @param pw
	 * @param tbl
	 * @param keyFld
	 * @param fld
	 * @param outputFile
	 */
	public FieldParserToXmlBase(String server, String db, String user,
			String pw, String tbl, String keyFld, String fld, String outputFile) {
		super();
		this.server = server;
		this.db = db;
		this.user = user;
		this.pw = pw;
		this.tbl = tbl;
		this.keyFld = keyFld;
		this.fld = fld;
		this.outputFile = outputFile;
	}

	/**
	 * @throws Exception
	 */
	protected abstract List<Parsing> processRows(ResultSet rows) throws Exception;

	/**
	 * @param dbName
	 * @param server
	 * @return
	 */
	protected String getConnectionStr(String dbName, String server) {
		return "jdbc:mysql://" + server + "/" + dbName
				+ "?characterEncoding=UTF-8&autoReconnect=true";
	}

	/**
	 * @throws Exception
	 */
	public void parse() throws Exception {
		String dbConnectionStr = getConnectionStr(db, server);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr, user, pw);
		Statement stmt = con.createStatement();
		String delimbo = (!fld.contains("`") && !tbl.contains("`") && !keyFld.contains("`")) ? "`" : "";
		String sql = "select " + delimbo + fld + delimbo + ", " + delimbo + keyFld
				+ delimbo + " from " + delimbo + tbl + delimbo + " where " + delimbo + fld + delimbo
				+ " is not null order by 2";
		ResultSet rows = stmt.executeQuery(sql);
		List<Parsing> parsings = processRows(rows);
		ParseResultsFormatter f = new ParseResultsFormatter(parsings, false,
				null);
		writeToXml(f);
	}

	/**
	 * @param f
	 * @throws Exception
	 */
	protected void writeToXml(ParseResultsFormatter f) throws Exception {
		List<String> xml = f.toXml(tbl, fld, keyFld);
		FileUtils.writeLines(new File(outputFile), "utf-8", xml);
	}

}
