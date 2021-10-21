/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.tools.webportal;

import com.csvreader.CsvWriter;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.weblink.WebLinkButton;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.af.ui.weblink.WebLinkDef;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpExportSchemaMapping;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tasks.subpane.wb.CSVExport;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Created Date: Feb 2, 2012
 * 
 * Builds lucene indexes, and sets up configuration files for solr and the web portal application.
 * (Currently accessed via Export tool.)
 *
 */
public class BuildSearchIndex2
{

	public final static String PROCESS_WEBLINKS_PREF_NAME = "webportal.PROCESS_WEBLINKS";
	public final static boolean PROCESS_WEBLINKS_PREF_DEFAULT = false;

    private final String collectionName;
    private final String attachmentURL;
    private final String outputFileName;
    private final Path tempDir;
    private Connection dbConn    = null;
    private Connection dbConn2   = null;
    private Connection dbConn3   = null;
    
    //-------------------------------
    // Lucene Indexing
    //-------------------------------
    
    protected int         INDEX_DIR     = 0;
    protected int         INDEX_DIR_COL = 1;
    protected int         INDEX_DIR_GEO = 2;
    protected int         INDEX_DIR_LOC = 3;
    protected int         INDEX_DIR_TXN = 4;

    //protected String[]      fileNames  = new String[] {"index-specify-kuherps"};
    protected String[]      fileNames;
    protected File[]        files;
    protected Analyzer[]    analyzers;
    protected IndexReader[] readers;

    //protected Searcher     searcher;
    protected String       dbName;
    
    protected final SpExportSchemaMapping mapping;
    protected final String writeToDir;
    
    protected String 		fieldsXml; //the "fields" block for the solr schema.xml file.
    
    protected final String[][] systemFlds = {
    		//name, type, indexed, stored, required
    		{"spid", "string", "true", "true", "true"},
    		//{"cs", "string", "true", "false", "true"},
    		{"contents", "text_general", "true", "false", "true"},
    		{"img", "string", "true", "true", "false"},
    		{"geoc", "string", "true", "true", "false"}
    };

    public BuildSearchIndex2(SpExportSchemaMapping mapping, String outputFileName, String collectionName, String attachmentURL)
    {
        super();
        //XXX need to get schemamapping object, probably
        this.mapping = mapping;
        this.outputFileName = outputFileName;
        this.collectionName = collectionName;
        this.attachmentURL = attachmentURL;
        try {
            tempDir = Files.createTempDirectory("portal_export");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.writeToDir = tempDir.toString() + File.separator + "PortalFiles";

        
        String solrIdxDir = this.writeToDir + File.separator + "solr";
        fileNames  = new String[] {solrIdxDir};
        files      = new File[fileNames.length];
        analyzers  = new Analyzer[fileNames.length];
        readers    = new IndexReader[fileNames.length];
    }

    private Integer getBaseTableId() {
		String sql = "select contexttableid from spexportschemaitemmapping m inner join spqueryfield f on " +
				"f.spqueryfieldid = m.spqueryfieldid inner join spquery q on q.spqueryid = f.spqueryid " +
				"where m.spexportschemamappingid = " + mapping.getId() + " limit 1";
		return BasicSQLUtils.querySingleObj(sql);
	}

	private DBTableInfo getBaseTableInfo() {
		return DBTableIdMgr.getInstance().getInfoById(getBaseTableId());
	}

	private String getBaseTableName() {
    	return getBaseTableInfo().getName();
	}
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createDBConnection(final String server, 
                                   final String port, 
                                   final String dbName, 
                                   final String username, 
                                   final String pwd) throws SQLException
    {
        disConnect();
        
    	this.dbName = dbName;
        
        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            dbConn  = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            dbConn2 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            dbConn3 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
        
    public void connect() throws SQLException 
    {
    	disConnect();
    	dbConn = DBConnection.getInstance().createConnection();
    	dbConn2 = DBConnection.getInstance().createConnection();
    	dbConn3 = DBConnection.getInstance().createConnection();
    }
    
    protected void disConnect() throws SQLException
    {
    	disConnect(dbConn);
    	disConnect(dbConn2);
    	disConnect(dbConn3);    	
    }
    
    protected void disConnect(Connection con) throws SQLException
    {
    	if (con != null)
    	{
    		if (!con.isClosed())
    		{
    			con.close();
    		}
    		con = null;
    	}
    }
    
    /**
     * 
     */
    public void cleanup() throws SQLException
    {
    	disConnect();
    }
    
    
    /**
     * @return the dbConn
     */
    public Connection getDBConn()
    {
        return dbConn;
    }

    private String createQuery(String tblName)
    {
        /*String sql = "SELECT si.FieldName, qf.FieldName, qf.ColumnAlias, qf.TableList, qf.StringId, qf.FormatName, qf.ContextTableIdent, qf.`Position` FROM spexportschema AS s " +
                        "Inner Join spexportschemaitem AS si ON s.SpExportSchemaID = si.SpExportSchemaID " +
                        "Inner Join spexportschemaitemmapping AS smi ON si.SpExportSchemaItemID = smi.ExportSchemaItemID " +
                        "Inner Join spqueryfield AS qf ON smi.SpQueryFieldID = qf.SpQueryFieldID  WHERE " +
                        "ORDER BY  qf.ContextTableIdent ASC, qf.`Position` ASC";

        Statement stmt = null;
        try
        {
            
        } catch (Exception ex)
        {
            
        }*/
        //
		DBTableInfo baseTblInfo = getBaseTableInfo();
        String sqlStr = "SELECT co.GUID, t.* from " + tblName  + " t inner join " + baseTblInfo.getName() +
				" co on co." + baseTblInfo.getIdFieldName() + " = t." + tblName + "id";
        return sqlStr;
    }
    

    private String getAttachments(Connection conn, String baseTblName, String baseGUID, boolean includeRelatedAttachments)
    	throws SQLException
    {
    	if (!"collectionobject".equalsIgnoreCase(baseTblName) && !"preparation".equalsIgnoreCase(baseTblName))
    	{
    		throw new NotImplementedException("unsupported base table");
    	}
    	if (includeRelatedAttachments) 
    	{
    		throw new NotImplementedException("includeRelatedAttachments is not implemented");
    	}
    	return getAttachments(conn, baseTblName, baseGUID);
    }
    
    private String getAttachments(Connection conn, String baseTblName, String baseGUID) throws SQLException
    {
    	String attacherTbl = baseTblName + "attachment";
    	String baseTblID = baseTblName + "ID"; //XXX dude... use DBTableMgr stuff...
    	String sql = "select att.AttachmentID, att.AttachmentLocation, att.Title, aia.Height, aia.Width from attachment att "
    			+ "left join attachmentimageattribute aia on aia.AttachmentImageAttributeID " 
    			+ "= att.AttachmentImageAttributeID inner join " + attacherTbl + " oatt on oatt.AttachmentID = att.AttachmentID"
				+ " inner join " + baseTblName + " baset on baset." + baseTblID + " = oatt." + baseTblID
    			+  " where att.IsPublic "
				//+ "and att.MimeType like 'image/%' "
				+ "and baset.GUID = '" + baseGUID + "'";
    			//+  " where att.IsPublic and baset.GUID = '" + baseGUID + "'";
    	Statement stmt = null;
    	ResultSet rs = null;
    	String result = null;
    	try 
    	{
        	stmt = conn.createStatement();
    		rs = stmt.executeQuery(sql);
    		while (rs.next())
    		{
    			if (result == null)
    			{
    				result = "[";
    			} else
    			{
    				result += ", ";
    			}
    			String[] flds = {"AttachmentID,no", "AttachmentLocation,yes",
    					"Title,yes", "Height,no", "Width,no"};
    			String rec = "";
    			for (String fld : flds)	
    			{
    				String[] def = fld.split(",");
    				boolean quote = "yes".equals(def[1]); 
    				String json = jsonFldVal(rs.getObject(def[0]), quote, def[0]);
    				if (json.length() > 0) 
    				{
    					if (rec.length() > 0)
    					{
    						rec += ",";
    					}
    					rec += json;
    				}
    			}
    			if (rec.length() > 0)
    			{
    				result += "{" + rec + "}";
    			}
    		}
    		if (result != null && result.length() > 0) {
    			result += "]";
    		}
    	} finally
    	{
    		if (stmt != null) {
    			stmt.close();
    		}
    		if (rs != null) {
    			rs.close();
    		}
    	}
    	return result;
    }

    private String jsonFldVal(Object val, boolean quote, String name)
    {
    	String result = "";
    	if (val != null)
    	{
    		String q = quote ? "\"" : "";
    		result = name + ":" + q + escape4Json(String.valueOf(val)) + q;
    	}
    	return result;
    }
    
	private String escape4Json(String str) {
		//escapes quotes,\, /, \r, \n, \b, \f, \t
		String[] badJson = {"\\", "\"", "\r", "\n", "\b", "\f", "\t"};
		String result = str;
		for (String bad : badJson) {
			result = result.replace(bad, "\\" + bad);
		}
		return result;
	}

    /**
     * @param columnIdx
     * @return
     */
    private boolean includeColumn(int columnIdx)
    {
    	return true;
    }
    
    /**
     * @param len
     * @param used
     * @return an un-used abbreviation for str, hopefully of length <= len. 
     * 
     * If necessary an abbreviation of length (slightly) > len will be returned.
     * Result is not added to used. 
     * 
     */
    private String getAbbreviation(ExportMappingInfo mapping, int len, List<String> used)
    {
    	String[] bad = {"°", ",", "°","\\","{","}","[","]",";",":","\"","'","!","@","#","$","%","^","&","*","(",")","+","=","|","/","?","<",">","~","`","."};
    	String str = mapping.getSpFldName();
    	for (String b : bad) {
    		str = str.replace(b, "_");
    	}
    	str = str.replace(" ", "");
    	String result = str;
    	String abbr = str;
    	if (len > 0) {
			abbr = str.substring(0, 1).toLowerCase();
			int c = 1;
			int lastUsed = 1;
			while (c < str.length()) {
				String s = str.substring(c, c + 1);
				if (s.equals(s.toUpperCase())) {
					abbr += s.toLowerCase();
					//lastUsed = c;
				}
				c++;
			}
			if (abbr.length() < len) {
				c = lastUsed;
				while (abbr.length() < len) {
					if (c < str.length()) {
						abbr += str.substring(c, c + 1).toLowerCase();
					} else {
						abbr += "x";
					}
				}
			}
			result = abbr.length() > len ? abbr.substring(0, len) : abbr;
		}
    	if (used.indexOf(result) != -1) {
			int l;
			if (len <= 0 && mapping.getTblInfo() != null) {
    			result = mapping.getTblInfo().getAbbrev() + "_" + result;
			} else {
				if (len > 0) {
					l = result.length() + 1;
					while (l < abbr.length()) {
						result = abbr.substring(0, l);
						if (used.indexOf(result) == -1) {
							l = abbr.length();
						} else {
							l++;
						}
					}
				}
			}
    		int numb = 1;
			l = result.length();
			while (used.indexOf(result) != -1) {
					result = result.substring(0, l) + numb++;
			}
    	}
    	return result;
    }
    
    private Map<Integer, String> getShortNamesForFields(ExportMappingHelper map) {
    	Map<Integer, String> result = new HashMap<Integer, String>();
		ArrayList<String> used = new ArrayList<String>();
		for (String[] sys : systemFlds) {
			used.add(sys[0]);
		}
    	for (ExportMappingInfo mapping : map.getMappings()) {
    		//XXX need to watch out that the QueryField.position-columnIndex relationship maintained --- is not messed up by un-mapped conditions, etc
			/* NOT abbreviating no more
			String abbr = getAbbreviation(mapping, 2, used); */
    		String abbr = getAbbreviation(mapping, -1, used);
    		used.add(abbr);
    		result.put(mapping.getColIdx(), abbr);
    	}
    	return result;
    }
    
    private String getFldXmlForSolr(String name, String type, boolean indexed, boolean stored, boolean required)
    {
    	String idxStr = indexed ? "true" : "false";
    	String storedStr = stored ? "true" : "false";
    	String reqStr = required ? "true" : "false";
    	return "<field name=\"" + name + "\""
    		+ " type=\"" + type + "\""
    		+ " indexed=\"" + idxStr + "\""
    		+ " stored=\"" + storedStr + "\""
    		+ " required=\"" + reqStr + "\""
    		+ "/>";
    }
    
    
    private String getSolrFldType(ExportMappingInfo info)
    {
    	DBFieldInfo fld = info.getFldInfo();
    	if (fld != null)
    	{
            if (info.isRecordTyper()) {
            	return "string";
            }
    		if (fld.getTableInfo().getTableId() == CollectionObject.getClassTableId() && fld.getName().equalsIgnoreCase("catalognumber"))
            {
            	//XXX ouch. What happens if there are multiple collections in a single portal, some with numeric catnums and some string???
            	UIFieldFormatterIFace formatter = fld.getFormatter();
            	if (formatter.isNumeric())
            	{
            		return "pint";
            	} else
            	{
            		return "string";
            	}
            }
            String type = fld.getType();
            if (StringUtils.isNotEmpty(type))
            {
            	if (type.equals("java.lang.String")) {return "string";}
            	else if (type.equals("java.util.Calendar")) 
            	{	
            		String fldId = info.getFldId();
            		if (fldId.endsWith("NumericDay") || fldId.endsWith("NumericMonth") || fldId.endsWith("NumericYear"))
            		{
            			return "pint";
            		} else
            		{
            			return "string";//XXX need to ALWAYS format dates in YYYY-MM-DD (plus time?) format???
            		}
            	}
            	else if (type.equals("java.lang.Float")) {return "pfloat";}
            	else if (type.equals("text")) {return "string";}
            	else if (type.equals("java.sql.Timestamp")) {return "string";}//XXX format???
            	else if (type.equals("java.math.BigDecimal")) {return "pdouble";} //XXX maybe?
            	else if (type.equals("java.lang.Integer")) {return "pint";}
            	else if (type.equals("java.lang.Boolean")) {return /*"boolean"; there seems to be no method in lucene.Document to add boolean fields*/ "string";}
            	else if (type.equals("java.lang.Byte")) {return "pint";}
            	else if (type.equals("java.lang.Double")) {return "pdouble";}
            	else if (type.equals("java.lang.Short")) {return "pint";}
            	else if (type.equals("java.util.Date")) {return "string";} //XXX format???
            	else if (type.equals("java.lang.Long")) {return "plong";}
            }
        }
    	//else the fld is formatted or aggregated
    	return "string";
    }

    
    /*private String getSolrFldTypeForSchema(DBFieldInfo  fld)
    {
    	if (fld != null)
    	{
            if (fld.getTableInfo().getTableId() == CollectionObject.getClassTableId() && fld.getName().equalsIgnoreCase("catalognumber"))
            {
            	//XXX ouch. What happens if there are multiple collections in a single portal, some with numeric catnums and some string???
            	UIFieldFormatterIFace formatter = fld.getFormatter();
            	if (formatter.isNumeric())
            	{
            		return "
            	}
            }
    		String type = fld.getType();
            if (StringUtils.isNotEmpty(type))
            {
            	if (type.equals("java.lang.String")) {return "string";}
            	else if (type.equals("java.util.Calendar")) {return "string";}//XXX need to ALWAYS format dates in YYYY-MM-DD (plus time?) format???
            	else if (type.equals("java.lang.Float")) {return "string";}
            	else if (type.equals("text")) {return "string";}
            	else if (type.equals("java.sql.Timestamp")) {return "string";}//XXX format???
            	else if (type.equals("java.math.BigDecimal")) {return "string";} //XXX maybe?  
            	else if (type.equals("java.lang.Integer")) {return "string";}
            	else if (type.equals("java.lang.Boolean")) {return "string";}
            	else if (type.equals("java.lang.Byte")) {return "string";}
            	else if (type.equals("java.lang.Double")) {return "tdouble";}
            	else if (type.equals("java.lang.Short")) {return "string";}
            	else if (type.equals("java.util.Date")) {return "string";} //XXX format???
            	else if (type.equals("java.lang.Long")) {return "string";} 
            }
        }
    	//else the fld is formatted or aggregated
    	return "string";
    }*/


    public List<String> getDbFieldInfoTypes() 
    {
    	List<String> result = new ArrayList<String>();
    	for (DBTableInfo tbl : DBTableIdMgr.getInstance().getTables())
    	{
    		for (DBFieldInfo fld : tbl.getFields())
    		{
    			if (result.indexOf(fld.getType()) == -1)
    			{
    				result.add(fld.getType());
    			}
    		}
    	}
    	for (String r : result)
    	{
    		System.out.println(r);
    	}
    	return result;
    	
    }
    
    private List<String> getFldsXmlForSchema(ExportMappingHelper map, Map<Integer, String> shortNames) 
    {
    	List<String> result = new ArrayList<String>();
    	for (ExportMappingInfo info : map.getMappings())
    	{
    		String name = shortNames.get(info.getColIdx()); 
    		//String type = getSolrFldTypeForSchema(info.getFldInfo());  why is getSolrFldTypeForSchema different than getSolrFldType?????
    		String type = getSolrFldType(info);
    		result.add(getFldXmlForSolr(name, type, true, true, false));
    	}
    	for (int f = 0; f < systemFlds.length; f++) {
    		String[] fld = systemFlds[f];
    		result.add(f, getFldXmlForSolr(fld[0], fld[1], Boolean.valueOf(fld[2]), Boolean.valueOf(fld[3]), Boolean.valueOf(fld[4])));
    	}
    	//XXX do we need the xref thing or something like it???
    	//result.add(3, getFldXmlForSolr("xref", "string", false, true, true));
    	return result;
    }
    
    protected List<String> getModelInJson(ExportMappingHelper map, 
    		Map<Integer, String> shortNames) throws IllegalAccessException {
    	List<String> result = new ArrayList<String>();
    	result.add("[");
    	result.add("{\"colname\":\"spid\", \"solrname\":\"spid\", \"solrtype\":\"int\"},");
    	for (ExportMappingInfo m : map.getMappings()) {
    		WebPortalFieldDef def = new WebPortalFieldDef(m, shortNames.get(m.getColIdx()),
    				getSolrFldType(m));
    		result.add(def.toJson() + (result.size() > 0 ? "," : "")); 
    	}
    	result.add("{\"colname\":\"img\", \"solrname\":\"img\", \"solrtype\":\"string\", \"title\":\"image\"}");
    	result.add("]");
    	return result;
    }

    protected void writeSolrFldXmlToFile(List<String> solrFldXml) throws IOException
    {
//        System.out.println("\nFlds:");
//        for (String fld : solrFldXml)
//        {
//        	System.out.println(fld);
//        }
//        System.out.println();

        List<String> myCopy = new ArrayList<String>(solrFldXml);
        Collections.sort(myCopy);
        myCopy.add(0, "<!-- solr field definitions for " + mapping.getMappingName() + " web portal -->");
        myCopy.add(1, "<!-- Paste the contents of this file into the solr/conf/schema.xml file. -->");
        File f = new File(writeToDir + File.separator + "SolrFldSchema.xml");
        FileUtils.writeLines(f, "utf8", myCopy);
    }
    
    protected void writePortalJsonToFile(List<String> portalJson) throws IOException
    {
//        System.out.println("\nJson:");
//        for (String fld : portalJson)
//        {
//        	System.out.println(fld);
//        }
//        System.out.println();

        List<String> myCopy = new ArrayList<String>(portalJson);
        File f = new File(writeToDir + File.separator + "flds.json");
        FileUtils.writeLines(f, "utf8", myCopy);
    }
    
    protected void writePortalInstanceJsonToFile(ExportMappingHelper map, Map<Integer, String> shortNames) throws IOException
    {
    	String portalInstance = UUID.randomUUID().toString();
    	File f = new File(writeToDir + File.separator + "PortalInstanceSetting.json");
        JSONObject json = new JSONObject();
        json.accumulate("portalInstance", portalInstance);
        json.accumulate("collectionName", collectionName);
        json.accumulate("imageBaseUrl", StringUtils.replace(attachmentURL, "/web_asset_store.xml", ""));
		String imageInfoFlds = "";
        for (ExportMappingInfo info : map.getMappings()) {
			if (info.spFldName.equalsIgnoreCase("catalognumber")
			|| info.spFldName.equalsIgnoreCase("fieldnumber")
			|| info.spFldName.equalsIgnoreCase("stationfieldnumber")){
				imageInfoFlds += " " + shortNames.get(info.getColIdx());
			}
		}
        json.accumulate("imageInfoFlds", imageInfoFlds);
        FileUtils.writeStringToFile(f,  json.toString(2), "utf8");
    }

    protected String processValue(String value, ExportMappingInfo info) {
		//check for weblink
        DBFieldInfo fld = info.getFldInfo();
        if (fld != null) {
			AppPreferences rPrefs = AppPreferences.getRemote();
        	if (rPrefs.getBoolean(PROCESS_WEBLINKS_PREF_NAME, PROCESS_WEBLINKS_PREF_DEFAULT)) {
				String wl = fld.getWebLinkName();
				if (wl != null) {
					WebLinkDef webLinkDef = WebLinkMgr.getInstance().get(wl);
					if (webLinkDef != null) {
						if (webLinkDef.getArgs().size() == 1 && "this".equals(webLinkDef.getArgs().get(0).getName())) {
							Hashtable<String, String> valHash = new Hashtable<>();
							valHash.put("this", value);
							return WebLinkButton.buildUrl(webLinkDef, valHash);
						}
					}
				}
			}
        }
        return value;
	}

	private List<String> getCsvHeaderRow(Map<Integer, String> shortNames) {
        List<String> result = new ArrayList<>();
        result.add("spid");
		//result.add("cs");
		result.add("contents");
		result.add("img");
		result.add("geoc");
        List<Integer> keys = new ArrayList<>(shortNames.keySet());
        Collections.sort(keys);
        for (Integer i : keys) {
            result.add(shortNames.get(i));
        }
        return result;
    }

	//this was copied (almost verbatim) from DarwinCoreArchive.addValToLine
	//Follows the candidate csv standard (of several years ago)
	//Allows saving as utf8, which seems not to be possible with very outdated csvwriter lib
    protected String addValToLine(String line, String eoFld, String encloser, String escaper, String valArg, int skippedFields) {
		String val = valArg == null ? "" : valArg;
    	if (line.length() > 0) {
			line += eoFld;
		}
		for (int i = 0; i < skippedFields; i++) {
			line += eoFld;
		}
		//XXX probably will need to change the way spexportschemaitem records are stored
		//so that concepts can be matched by uri
		if (val.indexOf(escaper) >= 0) {
			val = val.replace(escaper, escaper + escaper);
		}
		if (val.indexOf(encloser) >= 0) {
    		val = val.replace(encloser, encloser + encloser);
		}

		boolean enclose = val.indexOf(eoFld) >= 0 || val.indexOf("\n") >= 0 || val.indexOf("\r") >= 0 || val.indexOf(encloser) >= 0;
		if (enclose) line += encloser;
		line += val;
		if (enclose) line += encloser;
		return line;
	}

	private void writeTblToCsv(List<List<String>> tbl) throws IOException {
		if (tbl.size() > 0) {
			File f = new File(writeToDir + File.separator + "PortalData.csv");
			List<String> lines = new ArrayList<>();
			for (List<String> row : tbl) {
				String line = "";
				for (String s : row) {
					line = addValToLine(line, ",","\"", "\\", s, 0);
				}
				lines.add(line);
			}
			if (lines.size() > 0) {
				try {
					FileUtils.writeLines(f, "UTF-8", lines, true);
				} catch (IOException e) {
					edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
					edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(BuildSearchIndex2.class, e);
					throw (e);
				}
			}
		}
	}

	public boolean exportWebPortal(QBDataSourceListenerIFace progressListener) {
        if (progressListener != null) {
            progressListener.loading();
        }
        long startTime = System.currentTimeMillis();
        long totalRecs = 0;
        List<String> solrFldXml = null;
        List<String> portalFldJson = null;
        try {
            System.out.println("Exporting to directory '" + INDEX_DIR + "'...");
            ExportMappingHelper map = new ExportMappingHelper(dbConn, mapping.getId());
            totalRecs = BasicSQLUtils.getCount(dbConn, "SELECT COUNT(*) FROM " + map.getCacheTblName()).longValue();
            if (progressListener != null) {
                progressListener.loaded();
                progressListener.rowCount(totalRecs);
            }
            long procRecs  = 0;
            Statement stmt  = null;
            Statement stmt2 = null;
            Statement stmt3 = null;
            try {
                System.out.println("Total Records: "+totalRecs);
                //stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                stmt = dbConn.createStatement();
                stmt.setFetchSize(Integer.MIN_VALUE);
                stmt2 = dbConn2.createStatement();
                stmt3 = dbConn3.createStatement();
                stmt3.setFetchSize(Integer.MIN_VALUE);
                String sql = createQuery(map.getCacheTblName());
                System.out.println(sql);

                ResultSet rs = stmt.executeQuery(sql); //may consume all memory for giant caches
                ResultSetMetaData md = rs.getMetaData();

                //StringBuilder indexStr = new StringBuilder();
                StringBuilder contents = new StringBuilder();
                StringBuilder sb       = new StringBuilder();
                String lat1 = null, lng1 = null, lat2 = null, lng2 = null, collCode = null;
				Map<Integer, String> shortNames = getShortNamesForFields(map);
				solrFldXml = getFldsXmlForSchema(map, shortNames);
                portalFldJson = getModelInJson(map, shortNames);
                List<List<String>> tbl = new ArrayList<>(); //another memory muncher
                tbl.add(getCsvHeaderRow(shortNames));
                String baseTableName = getBaseTableName();
                while (rs.next()) {
                    List<String> row = new ArrayList<>();
                    //indexStr.setLength(0);
                    contents.setLength(0);
                    sb.setLength(0);
                    lat1 = null; lng1 = null; lat2 = null; lng2 = null; collCode = null;
					boolean guidPresent = false;
                    for (int c = 1; c <= md.getColumnCount(); c++) {
                        String value = "";
                        try {
                            value = rs.getString(c);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        if (c == 1) {
							//it's the GUID
							guidPresent = value != null && !"".equals(value.trim());
							if (guidPresent) {
								row.add(value);
							}
						} else if (c == 2) {
                        	//its the (CO by default for now) Record ID
							if (!guidPresent) {
								row.add(value);
							}
                        } else {
                        	ExportMappingInfo info = map.getMappingByColIdx(c - 3);
                            row.add(value == null ? null : processValue(value, info)); //will this lead to stupid precision for floats?
                            if (value != null) {
								contents.append(StringUtils.isNotEmpty(value) ? value : " ");
                                contents.append('\t');
                                if ("latitude1".equalsIgnoreCase(map.getMappingByColIdx(c - 3).getSpFldName())) {
                                    lat1 = value;
                                } else if ("latitude2".equalsIgnoreCase(map.getMappingByColIdx(c - 3).getSpFldName())) {
                                    lat2 = value;
                                } else if ("longitude1".equalsIgnoreCase(map.getMappingByColIdx(c - 3).getSpFldName())) {
                                    lng1 = value;
                                } else if ("longitude2".equalsIgnoreCase(map.getMappingByColIdx(c - 3).getSpFldName())) {
                                    lng2 = value;
                                }
                                if ("collectionCode".equalsIgnoreCase(info.getConcept())) {
                                    collCode = value;
                                }
                            }                        }
                    }
                    //indexStr.append(contents);

                    //XXX what, exactly, are the reasons for the store/tokenize settings on these 2 in index()?
                    //Ditto for store setting for geoc and img below?
                    //row.add(indexStr.toString()); //"cs" what is this for ? it is the same as contents no?
                    //row.add(contents.toString()); //"contents"

                    String geoc = null;
                    if (lat1 != null && lng1 != null) {
                        geoc = lat1 + " " + lng1;
                        if (collCode != null) {
                            geoc += " " + collCode;
                        }
                    }
                    //row.add(geoc);

                    String attachments = getAttachments(dbConn2, baseTableName, rs.getString(1), false);
                    //row.add(attachments);
					row.add(1, geoc);
					row.add(1, attachments);
					row.add(1, contents.toString());
					//row.add(1, indexStr.toString());
                    tbl.add(row);
                    if (tbl.size() == 250000) {
						writeTblToCsv(tbl);
						tbl.clear();
					}
                   //System.out.println(procRecs+" "+rs.getString(1));
                    procRecs++;
                    if (procRecs % 1000 == 0) {
                        System.out.println(procRecs);
                        if (progressListener != null) {
                            progressListener.currentRow(procRecs-1);
                        }
                    }
                }
                rs.close();

                writePortalJsonToFile(portalFldJson);
                writeSolrFldXmlToFile(solrFldXml);
                writePortalInstanceJsonToFile(map, shortNames);
                writeTblToCsv(tbl);

            } catch (Exception ex) {
                UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), ex);
                return false;
            } finally {
                if (stmt != null) {
                    try {
                        if (stmt != null) stmt.close();
                        if (stmt2 != null) stmt2.close();
                        if (stmt3 != null) stmt3.close();
                    } catch (SQLException e) {
                        UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), e);
                        return false;
                    }
                }

            }
        } catch (Exception ex){
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), ex);
            return false;
        }
        buildZipFile();
        if (progressListener != null) {
            progressListener.done(totalRecs);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time: "+ (endTime - startTime) / 1000);
        return true;
    }
    /**
     * 
     */
    public boolean index(QBDataSourceListenerIFace progressListener)
    {
		return exportWebPortal(progressListener);
//        if (progressListener != null)
//        {
//        	progressListener.loading();
//        }
//
//       long startTime = System.currentTimeMillis();
//
//        IndexWriter[] writers = null;
//        long totalRecs = 0;
//        List<String> solrFldXml = null;
//        List<String> portalFldJson = null;
//
//        try
//        {
//            for (int i=0;i<analyzers.length;i++)
//            {
//                files[i]     = new File(fileNames[i]);
//                analyzers[i] = new StandardAnalyzer(CharArraySet.EMPTY_SET);
//                FileUtils.deleteDirectory(files[i]);
//            }
//
//            System.out.println("Indexing to directory '" + INDEX_DIR + "'...");
//
//            ExportMappingHelper map = new ExportMappingHelper(dbConn, mapping.getId());
//
//            Map<Integer, String> shortNames = getShortNamesForFields(map);
//
//            totalRecs = BasicSQLUtils.getCount(dbConn, "SELECT COUNT(*) FROM " + map.getCacheTblName());
//            if (progressListener != null)
//            {
//            	progressListener.loaded();
//            	progressListener.rowCount(totalRecs);
//            }
//            long procRecs  = 0;
//
//            Statement stmt  = null;
//            Statement stmt2 = null;
//            Statement stmt3 = null;
//            try
//            {
//                writers = new IndexWriter[analyzers.length];
//                for (int i=0;i<files.length;i++)
//                {
//                    writers[i] = new IndexWriter(FSDirectory.open(java.nio.file.Paths.get(files[i].getPath())), new IndexWriterConfig(analyzers[i]));
//                }
//
//                System.out.println("Total Records: "+totalRecs);
//
//                //stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
//                stmt = dbConn.createStatement();
//                stmt.setFetchSize(Integer.MIN_VALUE);
//
//                stmt2 = dbConn2.createStatement();
//
//                stmt3 = dbConn3.createStatement();
//                stmt3.setFetchSize(Integer.MIN_VALUE);
//
//                //pStmt = dbConn3.prepareStatement("SELECT Text1 FROM preparation WHERE CollectionObjectID = ? AND Text1 IS NOT NULL");
//
//                String sql = createQuery(map.getCacheTblName());
//                System.out.println(sql);
//
//                ResultSet     rs       = stmt.executeQuery(sql); //may consume all memory for giant caches
//                ResultSetMetaData md = rs.getMetaData();
//
//                StringBuilder indexStr = new StringBuilder();
//                StringBuilder contents = new StringBuilder();
//                StringBuilder sb       = new StringBuilder();
//                String lat1 = null, lng1 = null, lat2 = null, lng2 = null, collCode = null;
//                solrFldXml = getFldsXmlForSchema(map, shortNames);
//                portalFldJson = getModelInJson(map, shortNames);
//                while (rs.next())
//                {
//                    Document doc = new Document();
//                    indexStr.setLength(0);
//                    contents.setLength(0);
//                    sb.setLength(0);
//                    lat1 = null; lng1 = null; lat2 = null; lng2 = null; collCode = null;
//                    for (int c = 1; c <= md.getColumnCount(); c++)
//                    {
//                    	if (includeColumn(c))
//                    	{
//                    		String value = "";
//                    		try
//                    		{
//                    			value = rs.getString(c);
//                    		} catch (Exception ex)
//                    		{
//                    			ex.printStackTrace();
//                    		}
//                    		if (c == 1)
//                    		{
//                    			//doc.add(new Field("spid", value, Field.Store.YES, Field.Index.ANALYZED));
//                    			doc.add(new StringField("spid", value, Field.Store.YES));
//                    		} else {
//								if (value != null) {
//									ExportMappingInfo info = map
//											.getMappingByColIdx(c - 2);
//									value = processValue(value, info);
//									String fldType = getSolrFldType(info);
//									if (fldType.equals("string")) {
//										if (info.isFullTextSearch()) {
//											doc.add(new TextField(shortNames.get(c - 2), value,
//													Field.Store.YES));
//										} else {
//											doc.add(new StringField(shortNames.get(c - 2), value,
//													Field.Store.YES));
//										}
//									} else if (fldType.equals("boolean")) {
//										doc.add(new StringField(shortNames.get(c - 2), value,
//												Field.Store.YES));
//									} else {
//										if (fldType.endsWith("int")) {
//											doc.add(new IntPoint(shortNames.get(c - 2), rs.getInt(c)));
//										} else if (fldType.endsWith("double")) {
//											doc.add(new DoublePoint(shortNames.get(c - 2), rs.getDouble(c)));
//										} else if (fldType.endsWith("long")) {
//											doc.add(new LongPoint(shortNames.get(c - 2), rs.getLong(c)));
//										} else if (fldType.endsWith("float")) {
//											doc.add(new FloatPoint(shortNames.get(c - 2), rs.getFloat(c)));
//										}
//									}
//									contents.append(StringUtils.isNotEmpty(value) ? value : " ");
//									contents.append('\t');
//									if ("latitude1".equalsIgnoreCase(map.getMappingByColIdx(c - 2).getSpFldName())) {
//										lat1 = value;
//									} else if ("latitude2".equalsIgnoreCase(map.getMappingByColIdx(c - 2).getSpFldName())) {
//										lat2 = value;
//									} else if ("longitude1".equalsIgnoreCase(map.getMappingByColIdx(c - 2).getSpFldName())) {
//										lng1 = value;
//									} else if ("longitude2".equalsIgnoreCase(map.getMappingByColIdx(c - 2).getSpFldName())) {
//										lng2 = value;
//									}
//									if ("collectionCode".equalsIgnoreCase(info.getConcept())) {
//										collCode = value;
//									}
//								}
//							}
//						}
//					}
//                    indexStr.append(contents);
//
//                    //XXX what, exactly, are the reasons for the store/tokenize settings on these 2?
//                    //Ditto for store setting for geoc and img below?
//                    doc.add(new TextField("cs", indexStr.toString(), Field.Store.NO));
//                    doc.add(new StringField("contents", contents.toString(), Field.Store.YES));
//
//                    if (lat1 != null && lng1 != null)
//                    {
//                    	String geoc = lat1 + " " + lng1;
////                    	if (lat2 != null && lng2 != null)
////                    	{
////                    		geoc += " " + lat2 + " " + lng2;
////                    	}
//						if (collCode != null) {
//							geoc += " " + collCode;
//						}
//                        doc.add(new StringField("geoc", geoc, Field.Store.NO));
//                    }
//
//                    String attachments = getAttachments(dbConn2, "collectionobject", rs.getInt(1), false);
//                    if (attachments != null && attachments.length() > 0)
//                    {
//                    	doc.add(new StringField("img", attachments, Field.Store.YES));
//                    }
//                    writers[0].addDocument(doc);
//
//                    //System.out.println(procRecs+" "+rs.getString(1));
//                    procRecs++;
//                    if (procRecs % 1000 == 0)
//                    {
//                        System.out.println(procRecs);
//                        if (progressListener != null)
//                        {
//                        	progressListener.currentRow(procRecs-1);
//                        }
//                    }
//
//                    if (procRecs % 100000 == 0)
//                    {
//                        System.out.println("Optimizing...");
//                        //writers[0].optimize();
//                    }
//                }
//                rs.close();
//
//                writePortalJsonToFile(portalFldJson);
//                writeSolrFldXmlToFile(solrFldXml);
//                writePortalInstanceJsonToFile();
//
//            } catch (Exception ex)
//            {
//                UsageTracker.incrHandledUsageCount();
//                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), ex);
//                return false;
//            } finally
//            {
//
//                if (stmt != null)
//                {
//                    try
//                    {
//                        if (stmt != null) stmt.close();
//                        if (stmt2 != null) stmt2.close();
//                        if (stmt3 != null) stmt3.close();
//
//                    } catch (SQLException e)
//                    {
//                        UsageTracker.incrHandledUsageCount();
//                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), e);
//                        return false;
//                    }
//                }
//
//            }
//
//        } catch (Exception ex)
//        {
//        	UsageTracker.incrHandledUsageCount();
//        	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), ex);
//        	return false;
//
//        } finally
//        {
//            for (Analyzer a : analyzers)
//            {
//                a.close();
//            }
//            analyzers = null;
//
//            for (IndexWriter writer : writers)
//            {
//                try
//                {
//                    System.out.println("Optimizing...");
//                    //writer.optimize();
//                    writer.close();
//                    System.out.println("Done Optimizing.");
//
//                } catch (Exception ex)
//                {
//                	UsageTracker.incrHandledUsageCount();
//                	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(this.getClass(), ex);
//                	return false;
//
//                }
//                writer = null;
//            }
//
//        }
//
//        buildZipFile();
//
//        if (progressListener != null)
//        {
//        	progressListener.done(totalRecs);
//        }
//        long endTime = System.currentTimeMillis();
//        System.out.println("Time: "+ (endTime - startTime) / 1000);
//        return true;
//
    }

    private void  buildZipFile() {
        try {
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outputFileName));
            File src = new File(writeToDir);
            addToZip(out, src, "");
            out.close();
        } catch (IOException e) {
            throw  new RuntimeException(e);
        }
    }


    private void addToZip(ZipOutputStream out, File src, String base) throws IOException {
        if (src.isDirectory())
        {
            base = base + src.getName() + "/";
            out.putNextEntry(new ZipEntry(base));
            for (File f : src.listFiles() )
            {
                addToZip(out, f, base);
            }
        } else {
            out.putNextEntry(new ZipEntry(base + src.getName()));
            IOUtils.copy(new FileInputStream(src), out);
        }
    }

    
    /**
     * 
     */
    /*public void testSearch()
    {
        Statement stmt = null;
        
        String querystr = "(Pengelly) OR (Castilleja AND applegatei)";
        String term     = "contents";
        try
        {
            stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            
            analyzers = new Analyzer[fileNames.length];
            for (int i=0;i<analyzers.length;i++)
            {
                files[i]     = new File(fileNames[i]);
                analyzers[i] = new StandardAnalyzer(Version.LUCENE_30);
                readers[i]   = IndexReader.open(FSDirectory.open(files[i]), true);
            }
            
            HashMap<Integer, Integer> tblIdHash = new HashMap<Integer, Integer>();
            
            for (int inx=0;inx<analyzers.length;inx++)
            {
                long  startTime   = System.currentTimeMillis();
                Query query       = new QueryParser(Version.LUCENE_30, term, analyzers[inx]).parse(querystr);
                int   hitsPerPage = 10;
                searcher = new IndexSearcher(readers[inx]);
                
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                
                System.out.println("\n------------- "+fileNames[inx] + " - Found: " + hits.length + " hits.");
                
                for (int i=0;i<hits.length;++i) 
                {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    //System.out.println((i + 1) + ". " + d.get("id") + " -> "+ d.get("xref"));
                    
                    tblIdHacssh.clear();
                    
                    String pairStr = d.get("xref");
                    if (StringUtils.isNotEmpty(pairStr))
                    {
                        String [] pairs = StringUtils.split(d.get("xref"), ',');
                        for (String p : pairs)
                        {
                            String [] ids = StringUtils.split(p, '=');
                            tblIdHash.put(Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
                        }
                    }
                    
                    if (inx == 0)
                    {
                        String id = d.get("id");
                        ResultSet         rs   = stmt.executeQuery("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+id);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        while (rs.next())
                        {
                            for (int j=1;j<=rsmd.getColumnCount();j++)
                            {
                                System.out.print(rs.getObject(j) + "\t");
                            }
                            System.out.println();
                        }
                        rs.close();
                        
                        Integer agentId = tblIdHash.get(5);
                        if (agentId != null)
                        {
                            rs   = stmt.executeQuery("SELECT LastName, FirstName, MiddleInitial FROM agent WHERE AgentID = "+agentId);
                            rsmd = rs.getMetaData();
                            while (rs.next())
                            {
                                for (int j=1;j<=rsmd.getColumnCount();j++)
                                {
                                    if (rs.getObject(j) != null) System.out.print(rs.getObject(j) + "\t");
                                }
                                System.out.println();
                            }
                            rs.close();
                        }
                    } else
                    {
                        Integer colObjId = tblIdHash.get(1);
                        if (colObjId != null)
                        {
                            ResultSet         rs   = stmt.executeQuery("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+colObjId);
                            ResultSetMetaData rsmd = rs.getMetaData();
                            while (rs.next())
                            {
                                for (int j=1;j<=rsmd.getColumnCount();j++)
                                {
                                    System.out.print(rs.getObject(j) + "\t");
                                }
                                System.out.println();
                            }
                            rs.close();
                        }
                    }
                    
                }
                System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis() - startTime) / 1000.0));
                searcher.close();
            }
            
            
            for (int i=0;i<analyzers.length;i++)
            {
                readers[i].close();
                analyzers[i].close();
            }
            
        } catch (SQLException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
            
        } catch (ParseException e)
        {
            e.printStackTrace();
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }*/
    
//    public void testSearch()
//    {
//        Statement stmt = null;
//        
//        String querystr = "23033";//(Pengelly) OR (Castilleja AND applegatei)";
//        String term     = "1";//"contents"
//        try
//        {
//            //stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
//            
//            analyzers = new Analyzer[fileNames.length];
//            for (int i=0;i<analyzers.length;i++)
//            {
//                files[i]     = new File(fileNames[i]);
//                analyzers[i] = new StandardAnalyzer(Version.LUCENE_30);
//                readers[i]   = IndexReader.open(FSDirectory.open(files[i]), true);
//            }
//            
//            //HashMap<Integer, Integer> tblIdHash = new HashMap<Integer, Integer>();
//            
//            for (int inx=0;inx<analyzers.length;inx++)
//            {
//                long  startTime   = System.currentTimeMillis();
//                QueryParser queryParser = new QueryParser(Version.LUCENE_30, term, analyzers[inx]);
//                Query query = queryParser.parse(querystr);
//                
//                int   hitsPerPage = 10;
//                searcher = new IndexSearcher(readers[inx]);
//                
//                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
//                searcher.search(query, collector);
//                ScoreDoc[] hits = collector.topDocs().scoreDocs;
//                
//                System.out.println("\n------------- "+fileNames[inx] + " - Found: " + hits.length + " hits.");
//                
//                for (int i=0;i<hits.length;++i) 
//                {
//                    int docId = hits[i].doc;
//                    Document d = searcher.doc(docId);
//                    System.out.println((i + 1) + ". " + d.get("1"));
//                    
//                    //tblIdHacssh.clear();
//                }
//                    
//                System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis() - startTime) / 1000.0));
//                searcher.close();
//            }
//            
//            
//            for (int i=0;i<analyzers.length;i++)
//            {
//                readers[i].close();
//                analyzers[i].close();
//            }
//            
//        } catch (IOException e)
//        {
//            e.printStackTrace();
//            
//        } catch (ParseException e)
//        {
//            e.printStackTrace();
//        } finally
//        {
//            if (stmt != null)
//            {
//                try
//                {
//                    stmt.close();
//                } catch (SQLException e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        System.setProperty(SchemaI18NService.factoryName, "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(DBTableIdMgr.factoryName, "edu.ku.brc.specify.config.SpecifyDBTableIdMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        
        try {
        	//BuildSearchIndex2 bsi = new BuildSearchIndex2("asdfasdf");
        	//bsi.createDBConnection("localhost", "3306", "kuherps", "root", "root");
        	//BuildSearchIndex2 bsi = new BuildSearchIndex2("dwcterms10_28_11_2");
        	//bsi.createDBConnection("localhost", "3306", "kuento", "root", "root");
 
        	//BuildSearchIndex2 bsi = new BuildSearchIndex2(7, "index-specify-kuento");
        	//bsi.createDBConnection("localhost", "3306", "bigento", "root", "root");

        	//bsi.index(null);
        	//bsi.getDbFieldInfoTypes();
        	//bsi.testSearch();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }

}
