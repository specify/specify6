/**
 * 
 */
package edu.ku.brc.specify.plugins.morphbank;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Collection;

import javax.swing.JProgressBar;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.datamodel.*;
import edu.ku.brc.specify.tasks.subpane.qb.QBDataSourceListenerIFace;
import edu.ku.brc.specify.tools.gbifregistration.GbifSandbox;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class DarwinCoreArchive 
{
    private static final Logger log  = Logger.getLogger(DarwinCoreArchive.class);

    private static final String BLOCK_SIZE_PREF = "DarwinCoreArchive.BlockSize";
	protected List<DarwinCoreArchiveFile> files;
	protected DwcMapper mapper;
	protected final boolean useCache;
	protected Statement stmt = null;

	public int getBlockSize() {
		return blockSize;
	}

	protected int blockSize = AppPreferences.getLocalPrefs().getInt(BLOCK_SIZE_PREF, 50000);
	DataProviderSessionIFace globalSession = null;

	/**
	 * @param file
	 * @param mapperID
	 * @param useCache
	 * @throws Exception
	 */
	public DarwinCoreArchive(File file, int mapperID, boolean useCache) throws Exception {
		this(XMLHelper.readFileToDOM4J(file), mapperID, null, useCache);
	}

	/**
	 *
	 * @param el
	 * @param mapperID
	 * @param useCache
	 * @throws Exception
	 */
	public DarwinCoreArchive(Element el, Integer mapperID, SpExportSchemaMapping mapping, boolean useCache) throws Exception {
		mapper = new DwcMapper(mapperID, mapping, false);
		files = new ArrayList<>();
		for (Object core : el.selectNodes("/archive")) {
			for (Object c : ((Element) core).selectNodes("*")) {
				files.add(new DarwinCoreArchiveFile("core".equals(((Element) c).getName()), (Element) c));
			}
		}
		this.useCache = useCache;
	}

	/**
	 * @param exportSchemaName
	 */
	public boolean buildExportSchemasFromSymbiotaDwcDef(String exportSchemaName) throws Exception {
		boolean result = true;
		
		TreeSet<Pair<String, Set<DarwinCoreArchiveField>>> schemas = new TreeSet<Pair<String, Set<DarwinCoreArchiveField>>>(new Comparator<Pair<String, Set<DarwinCoreArchiveField>>>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Pair<String, Set<DarwinCoreArchiveField>> o1,
					Pair<String, Set<DarwinCoreArchiveField>> o2) {
				return o1.getFirst().compareTo(o2.getFirst());
			}
			
		});
		for (DarwinCoreArchiveFile f : files) {
			for (DarwinCoreArchiveField m : f.getMappings()) {
				if (!m.isId()) {
					Pair<String, Set<DarwinCoreArchiveField>> mp = new Pair<String, Set<DarwinCoreArchiveField>>(m.getTermQualifier(), new TreeSet<DarwinCoreArchiveField>());
					if (!schemas.contains(mp)) {
						schemas.add(mp);
					}
					if ("genus".equals(m.getTermName())) {
						System.out.println("before genus: " + schemas.floor(mp).getSecond().size() + " " + schemas.floor(mp).getSecond().contains(m));
					}
					schemas.floor(mp).getSecond().add(m);
					if ("genus".equals(m.getTermName())) {
						System.out.println("after genus: " + schemas.floor(mp).getSecond().size() + " " + schemas.floor(mp).getSecond().contains(m));
					}
					
				}
			}
		}
		
		Iterator<Pair<String, Set<DarwinCoreArchiveField>>> schemer = schemas.iterator();
		int schemaNum = 1;
		while (schemer.hasNext()) {
			try {
				Pair<String, Set<DarwinCoreArchiveField>> s = schemer.next();
				String fullName = exportSchemaName;
				if (schemas.size() > 1) {
					fullName += String.valueOf(schemaNum++);
				}
				buildExportSchema(fullName, s.getFirst(), s.getSecond());
			} catch (Exception e) {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(DarwinCoreArchive.class, e);
                result = false;
			}
		}
		return result;
	}


	/**
	 * @param schemaName
	 * @param schemaNum
	 * @param uri
	 * @param concepts
	 * @throws Exception
	 */
	protected void buildExportSchema(String schemaName, String uri, Set<DarwinCoreArchiveField> concepts) throws Exception {
		Statement stmt = DBConnection.getInstance().getConnection().createStatement();
		try {
			String sql = "insert into spexportschema (TimestampCreated, Version, Description, SchemaName, DisciplineID, CreatedByAgentID) values("
					+ "now(), 0, '" + uri.replace("'", "''") + "', '" + schemaName.replace("'", "''") + "', "
					+ AppContextMgr.getInstance().getClassObject(Discipline.class).getId() +", " + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId()
					+ ")";
			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet key = stmt.getGeneratedKeys();
			key.next();
			Integer schemaId = key.getInt(1);
			try {
				buildExportSchemaItems(schemaId, concepts);
			} catch (SQLException e) {
				stmt.executeUpdate("DELETE FROM spexportschemaitem WHERE SpExportSchemaID=" + schemaId);
				stmt.executeUpdate("DELETE FROM spexportschema WHERE SpExportSchemaID=" + schemaId);
				throw e;
			}
		} finally {
			stmt.close();
			stmt = null;
		}
	}

	/**
	 * @param schemaId
	 * @param concepts
	 * @throws SQLException
	 */
	protected void buildExportSchemaItems(Integer schemaId, Set<DarwinCoreArchiveField> concepts) throws SQLException {
		Statement stmt = DBConnection.getInstance().getConnection().createStatement();
		try {
			Iterator<DarwinCoreArchiveField> iter = concepts.iterator();
			while (iter.hasNext()) {
				DarwinCoreArchiveField concept = iter.next();
				String sql = "insert into spexportschemaitem (TimestampCreated, Version, SpExportSchemaID, FieldName, CreatedByAgentID) values("
					+ "now(), 0, " + schemaId + ", '" + concept.getTermName().replace("'", "''") + "', "
					+ AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId()
					+ ")";
				stmt.executeUpdate(sql);
			}
		} finally {
			stmt.close();
			stmt = null;
		}
		
	}
	/**
	 * @return
	 */
	protected DarwinCoreArchiveFile getCoreFile()
	{
		for (DarwinCoreArchiveFile f : files)
		{
			if (f.isCore())
			{
				return f;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	protected List<DarwinCoreArchiveFile> getExtensionFiles()
	{
		List<DarwinCoreArchiveFile> result = new ArrayList<DarwinCoreArchiveFile>();
		for (DarwinCoreArchiveFile f : files)
		{
			if (!f.isCore())
			{
				result.add(f);
			}
		}
		return result;		
	}
	
	/**
	 * @return
	 */
	private Class<? extends DataModelObjBase> getBaseClass() {
		return CollectionObject.class;
	}

	protected DataProviderSessionIFace getGlobalSession() {
		if (globalSession == null) {
			globalSession = DataProviderFactory.getInstance().createSession();
			mapper.setGlobalSession(globalSession);
		}
		return globalSession;
	}

	public void setGlobalSession(DataProviderSessionIFace globalSession) {
		if (this.globalSession !=  null) {
			this.globalSession.close();
		}
		this.globalSession = globalSession;
	}

	public DataProviderSessionIFace getCurrentGlobalSession() {
		return this.globalSession;
	}
	/**
	 * @param id
	 * @return
	 */
	private DataModelObjBase getBaseRecord(Integer id) {
        //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		DataProviderSessionIFace session = getGlobalSession();
		DataModelObjBase result = null;
		try
		{
			result = session.get(getBaseClass(), id);
			result.forceLoad();
		} finally 
		{
			//session.close();
		}
		return result;		
	}
	/**
	 * @param collectionObjectID
	 * @return
	 * @throws Exception
	 */
	protected List<Pair<String, List<String>>> getExportText(int collectionObjectID) throws Exception {
		List<Pair<String, List<String>>> result = new ArrayList<Pair<String, List<String>>>();
		DarwinCoreSpecimen spec = new DarwinCoreSpecimen(mapper);
		if (useCache) {
			spec.setCollectionObjectId(collectionObjectID);
		} else {
			CollectionObject co = (CollectionObject) getBaseRecord(collectionObjectID);
			if (co != null) {
				spec.setCollectionObject(co);
			}
		}
			/*for (Pair<String, Object> fld : spec.getFieldValues())
			{
				System.out.println(fld.getFirst() + " = " + fld.getSecond());
			}*/

		DarwinCoreArchiveFile core = getCoreFile();
		if (core != null) {
			String id = spec.getCollectionObjectGUID();
			if (id != null) {
				List<String> line = getFileLines(core, spec, id);
				result.add(new Pair<String, List<String>>(core.getFiles().get(0), line));
			} else {
				log.warn("skipping record without ID");
			}
		} else {
			throw new Exception("DarwinCoreArchive missing core file.");
		}

		for (DarwinCoreArchiveFile ext : getExtensionFiles()) {
			String id = spec.getCollectionObjectGUID();
			if (id != null) {
				List<String> lines = getFileLines(ext, spec, id);
				result.add(new Pair<String, List<String>>(ext.getFiles().get(0), lines));
			} else {
				log.warn("skipping record without ID");
			}
		}

		return result;
	}

	
	/**
	 * @param spec
	 * @return
	 */
	protected boolean specContainsGUID(DarwinCoreSpecimen spec) {
		return spec.isMappedByName("occurrenceID");
	}
	
	/**
	 * @param spec
	 * @return
	 */
	protected String getGUIDFromSpec(DarwinCoreSpecimen spec) {
		Object result = spec.getByName("occurrenceID");
		return result == null ? null : result.toString();
	}
	
	/**
	 * @param spec
	 * @return
	 */
	protected String getCOGUID(DarwinCoreSpecimen spec) {
		if (!useCache) {
			return spec.getCollectionObjectGUID();
		} else {
			if (!specContainsGUID(spec)) {
				return getGUID(spec.getCollectionObjectId());
			} else {
				return getGUIDFromSpec(spec);
			}
		}
	}


	protected DarwinCoreSpecimen setupSpecForExportText(int collectionObjectID) throws Exception {
		DarwinCoreSpecimen spec = new DarwinCoreSpecimen(mapper);
		if (useCache) {
			spec.setCollectionObjectId(collectionObjectID);
		} else {
			CollectionObject co = (CollectionObject) getBaseRecord(collectionObjectID);
			if (co != null) {
				spec.setCollectionObject(co);
			}
		}
		return spec;
	}
	/**
	 *
	 * @param collectionObjectID
	 * @param archiveData
	 * @throws Exception
	 */

	protected void getExportText(int collectionObjectID, List<Pair<String, List<String>>> archiveData) throws Exception {
		//first the core
		mapper.setGetAllManies(false);
		DarwinCoreSpecimen spec = setupSpecForExportText(collectionObjectID);
		String id = getCOGUID(spec);
		if (id == null) {
			log.warn("skipping record without ID");
			return;
		}
		for (Pair<String, List<String>> data : archiveData) {
			DarwinCoreArchiveFile file = getFileByName(data.getFirst());
			if (file.isCore()) {
				data.getSecond().addAll(getFileLines(getFileByName(data.getFirst()), spec, id));
			}
		}
		//now extensions
		for (Pair<String, List<String>> data : archiveData) {
			DarwinCoreArchiveFile file = getFileByName(data.getFirst());
			if (!file.isCore()) {
				if (!mapper.getGetAllManies()) {
					mapper.setGetAllManies(true);
					spec = setupSpecForExportText(collectionObjectID);
				}
				data.getSecond().addAll(getFileLines(getFileByName(data.getFirst()), spec, id));
			}
		}
	}

	/**
	 * @param name
	 * @return
	 */
	protected DarwinCoreArchiveFile getFileByName(String name)
	{
		for (DarwinCoreArchiveFile f : getFiles())
		{
			//XXX what exacatly is the point of multiple filenames in the DarwinCoreArchiveFile class?
			for (String fileName : f.getFiles())
			{
				if (name.equals(fileName))
				{
						return f;
				}
			}
		}
		return null;
	}
	
	/**
	 * @return
	 * @throws Exception
	 */
	protected List<Pair<String, List<String>>> buildExportDataStruct() throws Exception {
		List<Pair<String, List<String>>> result = new ArrayList<Pair<String, List<String>>>();
		DarwinCoreArchiveFile core = getCoreFile();
		if (core != null) {
			result.add(new Pair<>(core.getFiles().get(0), new ArrayList<>()));
		} else {
			throw new Exception("DarwinCoreArchive missing core file.");
		}

		for (DarwinCoreArchiveFile ext : getExtensionFiles()) {
			result.add(new Pair<>(ext.getFiles().get(0), new ArrayList<>()));
		}
		return result;
	}

	/**
	 *
	 * @param tableId
	 * @param recordIds
	 * @param prog
	 * @return
	 * @throws Exception
	 */
	public List<Pair<String, List<String>>> getExportText(Integer tableId, List<Integer> recordIds,
														  QBDataSourceListenerIFace prog) throws Exception {
		return getExportText(tableId, recordIds, 0, recordIds.size(), prog);
	}

		/**
         *
         * @param tableId
         * @param recordIds
         * @param prog
         * @return
         * @throws Exception
         */
	public List<Pair<String, List<String>>> getExportText(Integer tableId, List<Integer> recordIds, int startIdx, int endIdx,
														  QBDataSourceListenerIFace prog) throws Exception {
		if (tableId != CollectionObject.getClassTableId()) {
			throw new Exception("Unsupported Table " + tableId);
		}
		stmt = DBConnection.getInstance().getConnection().createStatement();
		List<Pair<String, List<String>>> result = buildExportDataStruct();
		try {
			for (Pair<String, List<String>> f : result) {
				f.getSecond().add(getFileByName(f.getFirst()).getHeader());
			}
			for (int i = startIdx; i < endIdx; i++) {
				getExportText(recordIds.get(i), result);
				if (prog != null) {
					prog.currentRow(i);
					prog.anotherRow();
				} else {
					System.out.println("getExportText() row = " + i);
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
		return result;
	}

	public DwcMapper getMapper() {
		return mapper;
	}

	/**
	 *
	 * @param records
	 * @param prog
	 * @return
	 * @throws Exception
	 */
	public List<Pair<String, List<String>>> getExportText(RecordSet records, JProgressBar prog) throws Exception {
		if (records.getDbTableId() != CollectionObject.getClassTableId()) {
			throw new Exception("Unsupported Table " + records.getTableId());
		}
		
		stmt = DBConnection.getInstance().getConnection().createStatement();
		List<Pair<String, List<String>>> result = buildExportDataStruct();
		try {
			for (Pair<String, List<String>> f : result) {
				f.getSecond().add(getFileByName(f.getFirst()).getHeader());
			}
			int n = 0;
			for (RecordSetItem rec : records.getRecordSetItems()) {
				getExportText(rec.getRecordId(), result);
				if (prog != null) {
					prog.setValue(++n);
				} else {
					System.out.println("getExportText() row = " + (++n));
				}
			}
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
		return result;
	}
	
	/**
	 * @param coID
	 * @return
	 */
	protected String getGUID(Integer coID) {
		String sql = "select GUID from collectionobject where CollectionObjectID = " + coID;
		if (stmt == null) {
			return BasicSQLUtils.querySingleObj(sql);
		} else {
			try {
				ResultSet rs = stmt.executeQuery(sql);
				if (rs.next()) {
					return rs.getString(1);
				}		
			} catch (SQLException ex) {
				log.error(ex.getMessage());
			}
		}
		return null;
	}
	/**
	 * @param line
	 * @param eoFld
	 * @param encloser
	 * @param val
	 * @return
	 */
	protected String addValToLine(String line, String eoFld, String encloser, String escaper, String val, int skippedFields)
	{
		if (line.length() > 0) 
		{
			line += eoFld;
		} 
		for (int i = 0; i < skippedFields; i++)
		{
			line += eoFld;
		}
		//XXX probably will need to change the way spexportschemaitem records are stored
		//so that concepts can be matched by uri
		if (val.indexOf(escaper) >= 0)
		{
			val = val.replace(escaper, escaper + escaper);
		}
		if (val.indexOf(encloser) >= 0)
		{
			if ("\"".equals(encloser)) {
				/*this is strange. Is it here to conform to a symbiota requirement?
				A modified copy of this method is being used in BuildSearchIndex2.java and
				there the replacement in the else clause needs to be used in all cases or
				csv import to solr fails.
				 */
				val = val.replace(encloser, encloser + encloser);
			} else {
				val = val.replace(encloser, escaper + encloser);
			}
		} 
		
		boolean enclose = val.indexOf(eoFld) >= 0 || val.indexOf("\n") >= 0 || val.indexOf("\r") >= 0;
		if (enclose) line += encloser;
		line += val;
		if (enclose) line += encloser;
		return line;
	}

	private List<DarwinCoreArchiveField> getUnmappedFields(DarwinCoreArchiveFile f) {
		List<DarwinCoreArchiveField> result = new ArrayList<>();
		for (DarwinCoreArchiveField fld : f.getMappings()) {
			if (!"coreid".equals(fld.getTerm()) && !"id".equals(fld.getTerm()) && !mapper.isMapped(fld.getTerm()) ) {
				result.add(fld);
			}
		}
		return result;
	}

	public Map<DarwinCoreArchiveFile, List<DarwinCoreArchiveField>> getUnmappedFields() {
		Map<DarwinCoreArchiveFile, List<DarwinCoreArchiveField>> result = new HashMap<>();
		for (DarwinCoreArchiveFile f : files) {
			List<DarwinCoreArchiveField> unmapped = getUnmappedFields(f);
			if (unmapped.size() > 0) {
				result.put(f, unmapped);
			}
		}
		return result;
	}

	/**
	 * @param archiveFile
	 * @param obj
	 * @return
	 */
	public List<String> getFileLines(DarwinCoreArchiveFile archiveFile, DarwinCoreSpecimen spec, String id)
	{
		//XXX Still need to handle multiple lines for extensions
		String eoFld = archiveFile.getFieldsTerminatedBy();
		String encloser = archiveFile.getFieldsEnclosedBy();
		String escaper = archiveFile.getEscaper();
		List<String> result = new ArrayList<String>();
		int skippedFields = 0;
		for (DarwinCoreArchiveField fld : archiveFile.getMappings()) {
			if (isMapped(fld, spec) || fld.isId()) {
				//String termName = fld.getTerm().substring(fld.getTerm().lastIndexOf("/")+1);
				String termName = fld.getTerm();
				Object valObj = fld.isId() ? id : spec.get(termName);
				if (result.size() == 0) {
					if (archiveFile.isCore()) {
						result.add(id.toString());
					} else {
						if (valObj != null) {
							if (Collection.class.isAssignableFrom(valObj.getClass())) {
								for (int i = 0; i < ((Collection<?>)valObj).size(); i++) {
									result.add(id.toString());
								}
							} else {
								result.add(id.toString());
							}
						}
					}
					continue;
				} 
				
				if (valObj != null && List.class.isAssignableFrom(valObj.getClass())) {
					List<?> vals = (List<?>)valObj;
					//for cores, result.size() is always 1;
					if (vals.size() > result.size() && !archiveFile.isCore()) {
						String parentId = result.get(0);
						while (result.size() < vals.size()) {
							result.add(parentId);
						}
					}
					for (int i = 0; i < result.size(); i++)
					{
						String currentLine = result.get(i);
						String val = vals.get(i) == null ? "" : vals.get(i).toString();
						result.set(i, addValToLine(currentLine, eoFld, encloser, escaper, val, skippedFields));
					}
				} else {
					String val = valObj == null ? "" : valObj.toString();
					//for extensions this only should happen for null values
					//for cores, result.size() is always 1;
					for (int i = 0; i < result.size(); i++)
					{
						String currentLine = result.get(i);
						result.set(i, addValToLine(currentLine, eoFld, encloser, escaper, val, skippedFields));

					}
				}
				skippedFields = 0;
			} else if (!"coreid".equals(fld.getTerm()) && !"id".equals(fld.getTerm()))
			{
				skippedFields++;
			}
		}
		for (int l = 0; l < result.size(); l++)
		{
			String currentLine = result.get(l);
			for (int f = 0; f < skippedFields; f++)
			{
				currentLine += ",";
			}
			result.set(l, currentLine);
		}
		return result;
	}
	
	protected boolean isMapped(DarwinCoreArchiveField fld, DarwinCoreSpecimen spec)
	{
		//XXX probably will need to change the way spexportschemaitem records are stored
		//so that concepts can be matched by uri 
		//String termName = fld.getTerm().substring(fld.getTerm().lastIndexOf("/")+1);
		
		String termName = fld.getTerm();
		return spec.isMapped(termName);
	}
	/**
	 * @param fld
	 * @return
	 */
	protected int getMappingIdx(DarwinCoreArchiveField fld)
	{
		for (int i = 0; i < mapper.getConceptCount(); i++)
		{
			MappingInfo mi = mapper.getConcept(i);
			//XXX probably will need to change the way spexportschemaitem records are stored
			//so that concepts can be matched by uri 
			if (fld.getTerm().endsWith(mi.getName()))
			{
				return i;
			}
		}
		return -1;
	}
	
	
	/**
	 * @return
	 */
	public List<DarwinCoreArchiveFile> getFiles()
	{
		return files;
	}
	
	/**
	 * @param args
	 */
	static public void  main(String[] args)
	{
		try
		{
			List<String> lines = FileUtils.readLines(new File("/home/timo/workspace/XSpTrnk/config/wbschema_localization.xml"), "UTF-8");
			//List<String> lines = FileUtils.readLines(new File("/home/timo/workspace/XSpTrnk/config/wbschema_localization.xml"));
			//List<String> lines = FileUtils.readLines(new File("/home/timo/wbschemaISO.xml"), "ISO-8859-1");
			
			List<String> outlines = new ArrayList<String>();
			
			for (String line : lines) {
				//System.out.println(new String(line.getBytes("ISO-8859-1"), "UTF-8"));
				System.out.println(new String(line.getBytes("UTF-8"), "ISO-8859-1"));
				outlines.add(new String(line.getBytes("UTF-8"), "ISO-8859-1"));
			}
			//FileUtils.writeLines(new File("/home/timo/wbschemaISO.xml"), "ISO-8859-1", lines);
			FileUtils.writeLines(new File("/home/timo/wbschemaISO.xml"), "ISO-8859-1", outlines);
			
			
			 // return new String(instance.getResourceStringInternal(key).getBytes("ISO-8859-1"), "UTF-8");
			
//			String connStr = "jdbc:mysql://localhost/creac?characterEncoding=UTF-8&autoReconnect=true"; 
//			DwcMapper.connection = DriverManager.getConnection(connStr, "Master", "Master");
//			CollectionObjectFieldMapper.connection = DwcMapper.connection;
//			
//			DarwinCoreArchive dwc = new DarwinCoreArchive(new File("/home/timo/meta.xml"), 3);
//			DarwinCoreArchive dwc = new DarwinCoreArchive(new File("/home/timo/AgentQuery.xml"));
//			for (DarwinCoreArchiveFile f : dwc.getFiles())
//			{
//				System.out.println(f.getRowType());
//				for (String fileName : f.getFiles())
//				{
//					System.out.println("    " + fileName);
//				}
//				System.out.println();
//				for (DarwinCoreArchiveField fld : f.getMappings())
//				{
//					System.out.println("    " + fld.getIndex() + ": " + fld.getTerm());
//				}
//				System.out.println();
//				System.out.println();
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
}
