package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getNumRecords;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForInts;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.queryForRow;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleObj;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.setDBConnection;
import static edu.ku.brc.ui.UIRegistry.askYesNoLocalized;
import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.IdHashMapper;
import edu.ku.brc.specify.conversion.IdMapperIFace;
import edu.ku.brc.specify.conversion.IdMapperMgr;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.ui.ProgressDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Jul 12, 2011
 *
 */
public class iPadDBExporter implements VerifyCollectionListener
{
    public static final boolean IS_TESTING = false;
    private static final Logger  log                  = Logger.getLogger(iPadDBExporter.class);
    
    protected static final String PROGRESS            = "progress";
    private static final String   MSG                 = "msg";

    private static final boolean doAll                = true;
    private static final boolean doRebuildDB          = false;
    
    private static final String  BLD_ISITE_FILE  = "build_isite.sql";
    private static final String  STATS_XML       = "stats.xml";
    private static final String  CAT_FILE        = "catalog.xml";
    private static final String  ZIP_FILE        = "isite.zip";
    
    private static final String  GEOGEOCTYTBLNAME    = "ios_geogeo_cty";
    private static final String  GEOGEOCNTTBLNAME    = "ios_geogeo_cnt";
    
    private static final String  GEOLOCCNTTBLNAME = "ios_geoloc_cnt";
    private static final String  GEOLOCCTYTBLNAME = "ios_geoloc_cty";
    private static final String  COLOBJAGENTNAME  = "ios_colobjagents";
    private static final String  COLOBJCNTNAME    = "ios_colobjcnts";
    private static final String  TAXONTBLNAME     = "ios_taxon_pid";

    // Paleo
    private static final String  COLOBJLITHONAME  = "ios_colobjlitho";
    private static final String  COLOBJBIONAME    = "ios_colobjbio";
    private static final String  COLOBJCHRONNAME  = "ios_colobjchron";

    
    // Database Members
    private Connection                               dbConn            = null;
    private Connection                               dbUpdateConn      = null;
    private Connection                               dbS3Conn          = null;
    private String                                   imageURL          = null;
    private boolean                                  isUsingDirectURL  = false;
    
    private boolean                                  isInError         = false;
    
    private ChangeListener                           changeListener    = null;
    private TableWriter                              tblWriter;
    private int                                      numSteps = 0;
    private File                                     cacheDir;
    private VerifyCollectionDlg                      verifyDlg;
    
    private iPadRepositoryHelper                     helper             = new iPadRepositoryHelper();
    private String                                   dbName;
    private HashMap<String, Integer>                 idMapper           = new HashMap<String, Integer>();


    private ProgressDialog                           progressDelegate;
    private HashMap<Integer, Integer>                locNumObjsHash     = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer>                geoStateMapper     = new HashMap<Integer, Integer>();
    
    private SwingWorker<Integer, Integer>            worker;
    private IdMapperIFace                            locToGeoContinentMapper = null;
    private IdMapperIFace                            locToGeoCountryMapper   = null;
    
    private IdMapperIFace                            geoToGeoContinentMapper = null;
    private IdMapperIFace                            geoToGeoCountryMapper   = null;
    private IdMapperIFace                            taxonToPIDMapper        = null;
    
    private IdMapperIFace                            colObjToAgent           = null;
    private IdMapperIFace                            colObjToCnt             = null;
    
    private IdMapperIFace                            colObjToLitho           = null;
    private IdMapperIFace                            colObjToBio             = null;
    private IdMapperIFace                            colObjToChron           = null;
    
    private boolean                                  doZipFile               = true; 
    private boolean                                  doUpload                = true; 
    private ArrayList<ChartFileInfo>                 fileNamesForExport      = new ArrayList<ChartFileInfo>();
    private String                                   institutionImageName    = null;
    
    private int continentIdCnt = 0;
    private int countryIdCnt   = 0;
    private int coltrIdCnt     = 0;
    private int cntAmtCnt      = 0;

    private IPadCloudIFace ipadCloud;
    private Map<String, String>                      auxInfoMap  = null;
    
    /**
     * @param dbName
     * @param width
     * @param height
     */
    public iPadDBExporter(final IPadCloudIFace ipadCloud, final String dbName, final int width, final int height)
    {
        super();
        this.ipadCloud = ipadCloud;
        
        if (UIRegistry.getAppName() == null)
        {
            UIRegistry.setAppName("Specify");
        }
        
        this.dbName = dbName;
    }
    
    /**
     * 
     */
    private void createProgressUI()
    {
        
        progressDelegate = new ProgressDialog("", true, false);//UIRegistry.getGlassPane() == null);
        progressDelegate.setOverall(0, numSteps);
        
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                //progressDelegate.showAndFront();
                UIHelper.centerAndShow(progressDelegate);
                progressDelegate.setAlwaysOnTop(true);
                progressDelegate.toFront();
            }
        });
    }

    /**
     * @param desc
     */
    public void addProgress(final String desc)
    {
        if (progressDelegate != null)
        {
            if (StringUtils.isNotEmpty(desc))
            {
                progressDelegate.setDesc(desc);
            }
            progressDelegate.incOverall();
        }
    }
    
    protected ProgressDialog getProgressDelegate()
    {
        return progressDelegate;
    }
    
    protected SwingWorker<Integer, Integer> getWorker()
    {
        return worker;
    }
    
    protected File getConfigFile(final String fileName)
    {
        return XMLHelper.getConfigDir("ipad_exporter" + File.separator + fileName);
    }
    
//    /**
//     * @param conn
//     * @param fileName
//     * @throws Exception
//     */
//    @SuppressWarnings("unchecked")
//    protected boolean createDBTables(final Connection conn, final String fileName) throws Exception
//    {
//        File outFile = getConfigFile(fileName);
//        if (outFile != null && outFile.exists())
//        {
//            StringBuilder sb      = new StringBuilder();
//            Statement     stmt    = conn.createStatement();
//            List<?>       list    = FileUtils.readLines(outFile);
//            
//            for (String line : (List<String>)list)
//            {
//                String tLine = line.trim();
//                sb.append(tLine);
//                
//                if (tLine.endsWith(";"))
//                {
//                    System.out.println(sb.toString());
//                    stmt.executeUpdate(sb.toString());
//                    sb.setLength(0);
//                }
//            }
//            stmt.close();
//            return true;
//        }
//        return false;
//    }
    
    /**
     * @param msg
     */
    private void logMsg(final String msg)
    {
        System.err.println(msg);
        addProgress(msg);
    }
    
    /**
     * @param cntObj
     * @return
     */
    private Integer getCount(final Object cntObj)
    {
        if (cntObj instanceof BigDecimal)
        {
            return ((BigDecimal)cntObj).intValue();
            
        }
        if (cntObj instanceof Integer)
        {
            return (Integer)cntObj;
        }
        
        if (cntObj instanceof Long)
        {
            return ((Long)cntObj).intValue();
        }
        return 0;
    }
    
    /**
     * @param collectionId
     * @param disciplineId
     * @param divisionId
     * @param taxTreeDefId
     * @param geoTreeDefId
     * @param lithoTreeDefId
     * @param gtpTreeDefId
     */
    public void createMappings(final int collectionId, 
                               final int disciplineId, 
                               final int divisionId, 
                               final int taxTreeDefId, 
                               final int geoTreeDefId,
                               final int lithoTreeDefId,
                               final int gtpTreeDefId)
    {
        String[] names = {"COLMEMID", "DSPLNID", "DIVID", "TAXTREEDEFID", "GEOTREEDEFID", "LITHOTREEDEFID", "GTPTREEDEFID"};
        int[]    ids   = {collectionId, disciplineId, divisionId, taxTreeDefId, geoTreeDefId, lithoTreeDefId, gtpTreeDefId};
        
        for (int i=0;i<names.length;i++)
        {
            idMapper.put(names[i], ids[i]);
        }
    }
    
    /**
     * @param sql
     * @return
     */
    protected String adjustSQL(final String sql)
    {
        String sqlStr = sql;
        for (String key : idMapper.keySet())
        {
            String idStr = Integer.toString(idMapper.get(key));
            sqlStr = StringUtils.replace(sqlStr, key, idStr);
        }
        return sqlStr;
    }
    
    /**
     * @return
     * @throws Exception
     */
    private boolean createStatsTable() throws Exception
    {
        loadAndPushResourceBundle("stats");

        UIRegistry.setDoShowAllResStrErors(false);
        logMsg("Creating Stats..."); // I18N
        
        File tmpFile = getConfigFile(STATS_XML);
        if (tmpFile != null && tmpFile.exists())
        {
            dbS3Conn.setAutoCommit(false);
            PreparedStatement s3Stmt = null;
            Statement         stmt0  = null;
            try
            {
                Element root = XMLHelper.readFileToDOM4J(tmpFile);
                if (root != null)
                {
                    List<?> items = root.selectNodes("stat"); //$NON-NLS-1$
                    //System.out.println(items.size());
                    if (progressDelegate != null)
                    {
                        progressDelegate.setProcess(0, items.size());
                    }
        
                    stmt0  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                    stmt0.setFetchSize(Integer.MIN_VALUE);
                    s3Stmt = dbS3Conn.prepareStatement("INSERT INTO stats (_id, Tbl, Grp, Name, Descr, NumObjs, RecID) VALUES (?,?,?,?,?,?,?)");
                    
//                    for (Iterator<?> capIter = items.iterator(); capIter.hasNext(); )
//                    {
//                        Element fieldNode = (Element)capIter.next();
//                        String  desc = fieldNode.attributeValue("desc"); //$NON-NLS-1$
//                        System.out.println(String.format("placeholder = NSLocalizedString(@\"%s\", @\"From Stats\");", UIRegistry.getResourceString(desc)));
//                    }
//                        
//                    for (Iterator<?> capIter = items.iterator(); capIter.hasNext(); )
//                    {
//                        Element fieldNode = (Element)capIter.next();
//                        String  desc = fieldNode.attributeValue("desc"); //$NON-NLS-1$
//                        desc = UIRegistry.getResourceString(desc);
//                        System.out.println(String.format("\n/* From Stats */", desc));
//                        System.out.println(String.format("\"%s\" = \"%s\";", desc, desc));
//                    }
                        
                    int recId    = 0;
                    int cnt      = 0;
                    for (Iterator<?> capIter = items.iterator(); capIter.hasNext(); )
                    {
                        Element fieldNode = (Element)capIter.next();
                        String  tbl  = fieldNode.attributeValue("tbl"); //$NON-NLS-1$
                        String  grp  = fieldNode.attributeValue("grp"); //$NON-NLS-1$
                        String  name = fieldNode.attributeValue("name"); //$NON-NLS-1$
                        String  desc = fieldNode.attributeValue("desc"); //$NON-NLS-1$
                        String  sql  = fieldNode.getTextTrim();
                        
                        int table = Integer.parseInt(tbl);
                        int group = StringUtils.isNotEmpty(grp) ? Integer.parseInt(grp) : 0;
                        
                        sql = adjustSQL(sql);
                        
                        ResultSet         rs  = stmt0.executeQuery(sql);
                        ResultSetMetaData rdm = rs.getMetaData();
                        
                        int transCnt = 0;
                        while (rs.next())
                        {
                            transCnt++;

                            s3Stmt.setInt(1,    recId++);
                            s3Stmt.setInt(2,    table);
                            s3Stmt.setInt(3,    group);
                            s3Stmt.setString(4, name);
                            s3Stmt.setString(5, desc);
                            
                            //System.out.println(String.format("%s - %d", name, recId));
                            
                            int colCnt = rdm.getColumnCount();
                            if (colCnt == 1)
                            {
                                if (rdm.getColumnType(1) == 12)
                                {
                                    s3Stmt.setString(5, rs.getString(1)); // Desc
                                    s3Stmt.setInt(6, -1);
                                } else
                                {
                                    s3Stmt.setInt(6, rs.getInt(1));
                                }
                                s3Stmt.setObject(7, null);
                                
                            } else if (colCnt == 2 || colCnt == 3)
                            {
                                s3Stmt.setString(5, "");
                                if (rdm.getColumnType(1) == 12)
                                {
                                    s3Stmt.setString(4, rs.getString(1));
                                    s3Stmt.setInt(6, rs.getInt(2));
                                } else
                                {
                                    s3Stmt.setString(6, rs.getString(1));
                                    s3Stmt.setInt(4, rs.getInt(2));
                                }
                                if (colCnt == 3 && rdm.getColumnType(3) != 12)
                                {
                                    s3Stmt.setInt(7, rs.getInt(3));
                                } else
                                {
                                    s3Stmt.setObject(7, null);
                                }
                                //System.err.println(String.format("1 - [%d], 2 - [%d] [%s][%s]", rdm.getColumnType(1), rdm.getColumnType(2), rs.getString(1), rs.getString(2)));
                            } else
                            {
                                System.err.println("Error on num columns");
                            }
                            
                            if (s3Stmt.executeUpdate() != 1)
                            {
                                //System.out.println("Error updating taxon: "+recId);
                            }
                        }
                        rs.close();
                        try
                        {
                            if (transCnt > 0) dbS3Conn.commit();
                        } catch (Exception ex2) { ex2.printStackTrace();}
                       

                        cnt++;
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                    }
                    stmt0.close();
                }
            } catch (Exception ex)
            {
                try
                {
                    dbS3Conn.rollback();
                } catch (Exception ex2) {}
                
                ex.printStackTrace();
                
            } finally
            {
                if (stmt0 != null)  stmt0.close();
                if (s3Stmt != null)  s3Stmt.close();
                
                try
                {
                    dbS3Conn.setAutoCommit(true);
                } catch (Exception ex2) { ex2.printStackTrace();}
            }
            return true;
        }
        return false;
    }
     
    /**
     * @param processWorker
     */
    private void addProgressListener(final SwingWorker<?, ?> processWorker)
    {
        processWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if (progressDelegate != null && evt.getPropertyName().equals(PROGRESS))
                        {
                            progressDelegate.setProcess((Integer)evt.getNewValue());
                        } else if (progressDelegate != null && evt.getPropertyName().equals(MSG)) 
                        {
                            progressDelegate.setDesc((String)evt.getNewValue());
                        }
                    }
                });
    }
    
    @SuppressWarnings("unused")
    private void doBuildTaxonMappings() throws SQLException
    {
        int cnt = 0;
        taxonToPIDMapper.reset();
        
        worker.firePropertyChange(MSG, "", "Build Taxonomic Information...");
        
        HashSet<Integer> familySet = new HashSet<Integer>();

        
        String sql = "SELECT DISTINCT RNK FROM (SELECT t.RankID RNK, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) CNT FROM taxon t " +
                     "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                     "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                     "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = true GROUP BY t.RankID) T1 WHERE CNT > 0 AND RNK > 139 ORDER BY RNK DESC";
        System.out.println(sql);
        
        Vector<Integer> ranks = queryForInts(sql); 
        System.out.println(ranks);
        
        String post = adjustSQL(" FROM (SELECT t.TaxonID TID, IF (co.CountAmt IS NULL, 1, co.CountAmt) CNT, t.ParentID PID FROM taxon t  " +
                                "INNER JOIN determination d ON t.TaxonID = d.TaxonID  " +
                                "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                                "WHERE co.CollectionID = COLMEMID AND d.IsCurrent = true AND t.RankID = %d AND TaxonTreeDefID = TAXTREEDEFID GROUP BY t.TaxonID) T1 WHERE CNT > 0");
        
        Statement stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        
        //HashSet<Integer> unrankedSet = new HashSet<Integer>(queryForInts("select TaxonID from taxon where FullName = 'unranked' AND RankID = 140"));
        HashSet<Integer> mySet = new HashSet<Integer>();
//        mySet.add(359606); // DEBUG
//        mySet.add(359604);
//        mySet.add(359603);
        

        for (Integer rankId : ranks)
        {
            String postSQL = String.format(post, rankId);
            sql = "SELECT COUNT(*) " + postSQL;
            int totCnt = getCountAsInt(dbConn, sql);
            System.out.println(totCnt+"  "+rankId+"\n"+sql);
            if (totCnt < 0)
            {
                continue;
            }
            
            if (progressDelegate != null)
            {
                progressDelegate.setProcess(0, totCnt);
                progressDelegate.setProcess(0);
            }
            int inc = Math.max(totCnt / 20, 1);
            
            sql = "SELECT TID, PID " + postSQL;
            System.out.println(sql);
            cnt = 0;
            ResultSet rs = stmt.executeQuery(sql); // Get the GeoID and LocID
            while (rs.next())
            {
                int taxonId  = rs.getInt(1);
                int parentID = rs.getInt(2);
                if (rankId == 140)
                {
                    String nm = querySingleObj("SELECT FullName FROM taxon where TaxonID = "+taxonId);
                    System.out.println(String.format("%d %s p: %d", taxonId, nm, parentID));
                }
                taxonToPIDMapper.put(taxonId, parentID); //XX
                if (rankId == 140)
                {
                    familySet.add(taxonId);
                }
                cnt++;
                if (cnt % inc == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                }
            }
            rs.close();
        }
        
        HashSet<Integer> parentSet = new HashSet<Integer>();
        sql = String.format("SELECT DISTINCT NewId FROM %s", TAXONTBLNAME);
        ResultSet rs = stmt.executeQuery(sql); // Get the GeoID and LocID
        while (rs.next())
        {
            int parentId = rs.getInt(1);
            parentSet.add(parentId);
        }
        rs.close();
        
        //ArrayList<HashSet<Integer>> listOfParents = new ArrayList<HashSet<Integer>>();
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "SELECT ParentID, RankID FROM taxon WHERE TaxonID=? AND RankID > 139";
            pStmt = dbConn.prepareStatement(upStr);
            
            HashSet<Integer> prevParents = new HashSet<Integer>(parentSet);
            while (prevParents.size() > 0)
            {
                ArrayList<Integer> pids = new ArrayList<Integer>(prevParents);
                prevParents.clear();
                for (Integer pid: pids)
                {
                    pStmt.setInt(1, pid);
                    rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        Integer newPID = rs.getInt(1);
                        prevParents.add(newPID);
                    }
                }
                System.out.println(prevParents.size());
                parentSet.addAll(prevParents);
            }
            pStmt.close();
            
            System.out.println("Families:");
            for (Integer pid : familySet)
            {
                String nm = BasicSQLUtils.querySingleObj("SELECT FullName FROM taxon WHERE TaxonID = "+pid);
                System.out.println(String.format("%s - %d", nm, pid));
            }
                
            System.out.println("Parents:");
            for (Integer pid : parentSet)
            {
                String nm = BasicSQLUtils.querySingleObj("SELECT FullName FROM taxon WHERE TaxonID = "+pid);
                System.out.println(String.format("%s - %d", nm, pid));
            }
                
            
            if (progressDelegate != null)
            {
                progressDelegate.setProcess(0, taxonToPIDMapper.size() + parentSet.size());
            }
            
            taxonToPIDMapper.setShowLogErrors(false);
            PreparedStatement s3Stmt = dbS3Conn.prepareStatement("INSERT INTO taxon (_id, FullName, RankID, ParentID, FamilyID, TotalCOCnt, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)");
            sql = "SELECT TaxonID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber FROM taxon WHERE RankID > 139 AND TaxonID = ?";
            pStmt = dbConn.prepareStatement(sql);
            
            try
            {
                dbS3Conn.setAutoCommit(false);
                int transCnt = 0;
                
                sql = String.format("SELECT OldId FROM %s", TAXONTBLNAME);
                ResultSet loopRS = stmt.executeQuery(sql); // Get the GeoID and LocID
                while (loopRS.next())
                {
                    int id = loopRS.getInt(1);
                    if (mySet.contains(id))
                    {
                    	System.out.println();
                    }
                    
                    pStmt.setInt(1, id);
                    rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        int highNodeNum = rs.getInt(5);
                        int nodeNum     = rs.getInt(6);
                        
                        Integer coTotal = null;
                        if (familySet.contains(id))
                        {
                            //sql = "select NodeNumber, HighestChildNodeNumber FROM taxon where TaxonID =" + id;
                            //Object[] row = queryForRow(sql);

                            sql = String.format("SELECT SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) CNT FROM taxon t " +
                                                "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                                                "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                                                "WHERE NodeNumber > %d AND HighestChildNodeNumber <= %d", nodeNum, highNodeNum);//(Integer)row[0], (Integer)row[1]);
                            coTotal = BasicSQLUtils.getCount(sql);
                            //if (coTotal != null) System.out.println(String.format("1ID: %d -> %d", id, coTotal));
                        }                    

                        s3Stmt.setInt(1,    id); // TaxonID
                        s3Stmt.setString(2, rs.getString(2));
                        s3Stmt.setInt(3,    rs.getInt(3));
                        s3Stmt.setInt(4,    rs.getInt(4));
                        s3Stmt.setObject(5, null);
                        s3Stmt.setInt(6,    coTotal != null ? coTotal : 1);
                        
                        s3Stmt.setInt(7, highNodeNum);
                        s3Stmt.setInt(8, nodeNum);
                        
                        if (s3Stmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating taxon: "+id);
                        }
                        transCnt++;
                    }
                    rs.close();
                    
                    cnt++;
                    if (cnt % 10 == 0) 
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                    }
                }
                
                
                for (Integer id : parentSet)
                {
                    if (taxonToPIDMapper.get(id) == null)
                    {
                        pStmt.setInt(1, id);
                        rs = pStmt.executeQuery();
                        if (rs.next())
                        {
                            int highNodeNum = rs.getInt(5);
                            int nodeNum     = rs.getInt(6);
                            int rnkId       = rs.getInt(3);

                            Integer coTotal = null;
                            if (rnkId == 140)
                            {
                                sql = String.format("SELECT SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) CNT FROM taxon t " +
                                                    "INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                                                    "INNER JOIN collectionobject co ON co.CollectionObjectID = d.CollectionObjectID " +
                                                    "WHERE NodeNumber > %d AND HighestChildNodeNumber <= %d", nodeNum, highNodeNum);//(Integer)row[0], (Integer)row[1]);
                                coTotal = BasicSQLUtils.getCount(sql);
                                familySet.add(id);
                            }
                            
                            s3Stmt.setInt(1,    id); // TaxonID
                            s3Stmt.setString(2, rs.getString(2));
                            s3Stmt.setInt(3,    rs.getInt(3));
                            s3Stmt.setInt(4,    rs.getInt(4));
                            s3Stmt.setObject(5, null);
                            if (rnkId == 140)
                            {
                                s3Stmt.setInt(6, coTotal != null ? coTotal : 1);
                            } else
                            {
                                s3Stmt.setObject(6, null);
                            }
                            
                            s3Stmt.setInt(7, highNodeNum);
                            s3Stmt.setInt(8, nodeNum);
                            
                            if (s3Stmt.executeUpdate() != 1)
                            {
                                System.out.println("Error updating taxon: "+id);
                            }
                            transCnt++;
                        }
                        rs.close();
                    }
                    cnt++;
                    if (cnt % 10 == 0) 
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                    }
                }
                pStmt.close();
                s3Stmt.close();
                
                sql    = "SELECT HighestChildNodeNumber, NodeNumber FROM taxon WHERE TaxonID = ?";
                pStmt  = dbConn.prepareStatement("SELECT NodeNumber, HighestChildNodeNumber FROM taxon WHERE TaxonID = ?");
                s3Stmt = dbS3Conn.prepareStatement("UPDATE taxon SET FamilyID=? WHERE NodeNum > ? AND HighNodeNum <= ?");
                
                for (Integer id : familySet)
                {
                    Object[] row = queryForRow(dbS3Conn, "SELECT _id, FullName FROM taxon where _id = "+id);
                    if (row != null)
                    {
                        System.out.println(row[0]+"  "+row[1]);
                    } else
                    {
                        System.out.println("Family is missing: "+id);
                    }
                    
                    pStmt.setInt(1, id);
                    rs = pStmt.executeQuery();
                    if (rs.next())
                    {
                        s3Stmt.setInt(1, id);
                        s3Stmt.setInt(2, rs.getInt(1));
                        s3Stmt.setInt(3, rs.getInt(2));
                        if (s3Stmt.executeUpdate() == 0)
                        {
                            System.out.println(String.format("SELECT _id, RankID,ParentID FROM taxon WHERE NodeNum > %d AND HighNodeNum <= %d", rs.getInt(1), rs.getInt(2)));
                            System.out.println(String.format("Error updating taxon: %d (%d, %d)", id, rs.getInt(1), rs.getInt(2)));
                        } else 
                        {
                            transCnt++;
                        }
                    }
                }
                
                try
                {
                    if (transCnt > 0) dbS3Conn.commit();
                } catch (Exception ex2) { ex2.printStackTrace();}
                
            } catch (Exception e) 
            {
                try
                {
                    dbS3Conn.rollback();
                } catch (Exception ex2) {}
                e.printStackTrace();
                
            } finally
            {
                if (stmt != null) stmt.close();
                if (s3Stmt != null) s3Stmt.close();
                try
                {
                    dbS3Conn.setAutoCommit(true);
                } catch (Exception ex2) { ex2.printStackTrace();}
            }
            
        } catch (Exception e) 
        {
        }
        taxonToPIDMapper.setShowLogErrors(true);

        System.out.println(taxonToPIDMapper.size()+"  PIDS: "+parentSet.size());
    }
   
    /**
     * @throws SQLException
     */
    private void doBuildAgents() throws SQLException
    {
        int cnt = 0;
        String sql;
        Integer count = null;

        worker.firePropertyChange(MSG, "", "Getting Agents Counts...");
        
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = "SELECT ID, if (CNT IS NULL, 0, CNT) FROM (SELECT a.AgentID AS ID, COUNT(a.AgentID) CNT FROM agent a INNER JOIN collector cr ON a.AgentID = cr.AgentID " +
                "INNER JOIN collectingevent ce ON cr.CollectingEventID = ce.CollectingEventID " +
                "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                "WHERE cr.OrderNumber = 1 " +
                "GROUP BY a.AgentID ORDER BY CNT DESC) T1";
        
        for (Object[] row : query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        worker.firePropertyChange(MSG, "", "Building Agents...");
        
        String prefix  = "SELECT AgentID, FirstName, Initials, LastName ";
        String postfix = "FROM agent ORDER BY LastName";
        
        boolean isSingleCollection = getCountAsInt(dbConn, "SELECT COUNT(*) FROM collection") == 1;
        
        int totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix;

        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            int transCnt = 0;
            cnt = 0;
            
            StringBuilder sb = new StringBuilder();
            
            s3Stmt = dbS3Conn.prepareStatement("INSERT INTO agent (_id, Name, NumObjs) VALUES (?,?,?)");
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                
                // don't check to see if Agent has items in the collection 
                // unless there is more than one collection
                if (!isSingleCollection)  
                {
                    Integer colObjId = colObjToAgent.reverseGet(id);
                    if (colObjId == null) continue;
                }
                
                String  first = rs.getString(2);
                String  mid   = rs.getString(3);
                String  last  = rs.getString(4);
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                
                String fullName = formatName(sb, first, mid, last, 64);
                    
                s3Stmt.setInt(1, id);
                s3Stmt.setString(2, fullName);
                s3Stmt.setInt(3, numObjs);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    //System.out.println("Error updating agent: "+id);
                }
                cnt++;
                if (cnt % 100 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Agent: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }

    private void doBuildGeographyContCountry() throws SQLException
    {
        int cnt = 0;
        
        worker.firePropertyChange(MSG, "", "Build Mapping From Locality to Geography...");
        
        Statement stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        cnt = 0;
        
        String[] prefixes = { 
                //"SELECT g5.GeographyID AS GID, g5.RankId, g1.GeographyID, g1.RankID ",
                "SELECT g4.GeographyID AS GID, g4.RankId, g1.GeographyID, g1.RankID ",
                "SELECT g3.GeographyID AS GID, g3.RankId, g1.GeographyID, g1.RankID ",
                "SELECT g2.GeographyID AS GID, g2.RankId, g1.GeographyID, g1.RankID ",
                "SELECT g1.ParentID, g1.RankId, g1.GeographyID ",
                };
        
        String[] sqls = { 
                //"FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID INNER JOIN geography g3 ON g2.ParentID = g3.GeographyID INNER JOIN geography g4 ON g3.ParentID = g4.GeographyID INNER JOIN geography g5 ON g4.ParentID = g5.GeographyID WHERE g1.RankID = %d AND g1.GeographyTreeDefID = GEOTREEDEFID",
                "FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID INNER JOIN geography g3 ON g2.ParentID = g3.GeographyID INNER JOIN geography g4 ON g3.ParentID = g4.GeographyID WHERE g1.RankID = %d AND g1.GeographyTreeDefID = GEOTREEDEFID",
                "FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID INNER JOIN geography g3 ON g2.ParentID = g3.GeographyID WHERE g1.RankID = %d AND g1.GeographyTreeDefID = GEOTREEDEFID",
                "FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID WHERE g1.RankID = %d AND g1.GeographyTreeDefID = GEOTREEDEFID",
                "FROM geography g1 WHERE g1.RankID = %d AND g1.GeographyTreeDefID = GEOTREEDEFID",
                };
        
        Integer[][] ranks = {
                {400},
                {400, 300},
                {400, 300, 200},
                {200, 100},
        };
        geoToGeoContinentMapper.setShowLogErrors(true);
        geoToGeoCountryMapper.setShowLogErrors(true);

        for (int i=0;i<prefixes.length;i++)
        {
            
            Integer[] ranksArray = ranks[i];
            for (Integer rnk : ranksArray)
            {
                String postSQL = String.format(sqls[i], rnk);
                int totCnt = getCountAsInt(dbConn, adjustSQL("SELECT COUNT(*) " + postSQL));
                if (totCnt < 1)
                {
                    continue;
                }
                //totLocs += totCnt;
                //System.out.println("totLocs: "+totLocs);
                
                if (progressDelegate != null)
                {
                    progressDelegate.setProcess(0, totCnt);
                    progressDelegate.setProcess(0);
                }
                int inc = Math.max(totCnt / 20, 1);
                
                String sqlStr = prefixes[i] + postSQL;
                
                System.out.println(adjustSQL(sqlStr));
                ResultSet rs = stmt.executeQuery(adjustSQL(sqlStr)); // Get the GeoID and LocID
                while (rs.next())
                {
                    int geoId   = rs.getInt(1);
                    int rankLvl = rs.getInt(2);
                    int fromId  = rs.getInt(3);
                    
                    if (rankLvl == 100)
                    {
                        geoToGeoContinentMapper.put(fromId, geoId);
                        
                    } else if (rankLvl == 200)
                    {
                        geoToGeoCountryMapper.put(fromId, geoId);
                    }
                    
                    cnt++;
                    if (cnt % inc == 0) 
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                        //log.debug("LocID -> GeoID: "+cnt+ " -> "+getCountAsInt("SELECT COUNT(*) FROM geoloc"));
                    }
                }
                rs.close();
                worker.firePropertyChange(PROGRESS, 0, totCnt);
            }
        }
    }
    
    /**
     * Count number of Specimens for each Collection Object.
     * @throws SQLException
     */
    private void doBuildPaleo() throws SQLException
    {
        worker.firePropertyChange(MSG, "", "Locating Specimen Paleo data...");
        
        String postSQL = " FROM collectionobject co LEFT JOIN paleocontext p ON co.PaleoContextID = p.PaleoContextID " +
                         "LEFT JOIN lithostrat l ON p.LithoStratID = l.LithoStratID " +
                         "LEFT JOIN lithostrat bl ON p.BioStratID = bl.LithoStratID " +
                         "LEFT JOIN geologictimeperiod g ON p.ChronosStratID = g.GeologicTimePeriodID WHERE CollectionID = COLMEMID";
        String sql = adjustSQL("SELECT COUNT(*)" + postSQL);
        
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null) progressDelegate.setProcess(0, 100);
                
        colObjToLitho = IdMapperMgr.getInstance().addHashMapper(COLOBJLITHONAME, null, true);
        colObjToLitho.reset();

        colObjToBio = IdMapperMgr.getInstance().addHashMapper(COLOBJBIONAME, null, true);
        colObjToBio.reset();
       
        colObjToChron = IdMapperMgr.getInstance().addHashMapper(COLOBJCHRONNAME, null, true);
        colObjToChron.reset();
        
        IdHashMapper.setEnableDelete(false);
        IdMapperMgr.getInstance().setDBs(dbConn, dbConn);
        
        dbS3Conn.setAutoCommit(false);

        //----------------------------------------------------------
        // Creating Mapping from ColObj top Geo
        //----------------------------------------------------------
        int fivePercent = Math.max(totCnt / 20, 1);
        
        int cnt = 0; 
        
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            String upSQL = "INSERT INTO paleo (_id, LithoID, ChronosID, BioStratID) VALUES (?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
    
            sql  = adjustSQL("SELECT co.CollectionObjectID, l.LithoStratID, p.ChronosStratID, bl.LithoStratID " + postSQL);
            stmt = dbConn.createStatement();
            ResultSet rs    = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                int coId       = rs.getInt(1);
                s3Stmt.setInt(1, coId);
                
                Integer lithId = rs.getInt(2);
                if (rs.wasNull())
                {
                    lithId = null;
                    s3Stmt.setObject(2, null);
                } else
                {
                    s3Stmt.setInt(2, lithId);
                }
                
                Integer chronosId = rs.getInt(3);
                if (rs.wasNull())
                {
                    chronosId = null;
                    s3Stmt.setObject(3, null);
                } else
                {
                    s3Stmt.setInt(3, chronosId);
                }
                
                Integer bioId = rs.getInt(4);
                if (rs.wasNull())
                {
                    bioId = null;
                    s3Stmt.setObject(4, null);
                } else
                {
                    s3Stmt.setInt(4, bioId);    
                }

                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+coId);
                }                
                cnt++;
                if (cnt % fivePercent == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, Math.max((100 * cnt) / totCnt, 1));
                }
            }
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
        
    /**
     * @throws SQLException
     */
    private void doBuildLitho() throws SQLException
    {
        int     cnt = 0;
        String  sql;
        Integer count = null;

        // Get Specimen Counts for Litho
        worker.firePropertyChange(MSG, "", "Getting Lithostratigraphy Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = adjustSQL("SELECT l.LithoStratID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM collectionobject co " +
                        "INNER JOIN paleocontext p ON co.PaleoContextID = p.PaleoContextID " +
                        "INNER JOIN lithostrat l ON p.LithoStratID = l.LithoStratID " +
                        "WHERE l.LithoStratTreeDefID = LITHOTREEDEFID GROUP BY l.LithoStratID");
        
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        // Get All Unsed Litho items
        worker.firePropertyChange(MSG, "", "Building Lithostrat...");
        
        String prefix  = "SELECT l.LithostratID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
        String fullPostFix = adjustSQL(" FROM lithostrat l " +
                                        "LEFT JOIN paleocontext p ON p.LithoStratID = l.LithoStratID " +
                                        "LEFT JOIN collectionobject co ON co.PaleoContextID = p.PaleoContextID " +
                                        "WHERE l.LithoStratTreeDefID = LITHOTREEDEFID AND co.CollectionObjectID IS NOT NULL ");

        
        sql = "SELECT COUNT(*) " + fullPostFix;
        log.debug(sql);
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + fullPostFix + " GROUP BY l.LithostratID";
        log.debug(sql);
        
        tblWriter.log("Lithostrat Issues");
        tblWriter.startTable();
        tblWriter.logHdr("Full Name", "Rank Id", "Issue");

        
        //boolean isSingleCollection = getCountAsInt(dbConn, "SELECT COUNT(*) FROM collection") == 1;

        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            dbS3Conn.setAutoCommit(false);
            int transCnt = 0;
            
            cnt = 0;
            String upSQL = "INSERT INTO litho (_id, FullName, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2)); // FullName
                s3Stmt.setInt(3,    rs.getInt(3));    // RankID
                s3Stmt.setInt(4,    rs.getInt(4));    // ParentID
                
                int highestNodeNum = rs.getInt(5);    
                int nodeNumber     = rs.getInt(6);
                
                s3Stmt.setInt(5,    0);          // Total Number of Specimens below
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(6,    numObjs);          // Number of Specimens this Lithostrat has
                
                s3Stmt.setInt(7,    highestNodeNum);
                s3Stmt.setInt(8,    nodeNumber);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Lithostrat: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        tblWriter.endTable();
        
        worker.firePropertyChange(MSG, "", "Updating Total Lithostrategraphy Counts...");
        
        treeCnt = 0;

        //sql = adjustSQL("SELECT LithoStratID FROM lithostrat WHERE LithoStratTreeDefID = LITHOTREEDEFID AND RankID = 0");
        //int rootId = getCountAsInt(dbConn, sql);
        
        sql = adjustSQL("SELECT COUNT(*) FROM lithostrat WHERE LithoStratTreeDefID = LITHOTREEDEFID");
        treeTotal = getCountAsInt(dbConn, sql);
        if (treeTotal == 0)
        {
            return;
        }
        treePercent = treeTotal / 20;
        if (progressDelegate != null) 
        {
            progressDelegate.setProcessPercent(true);
            progressDelegate.setProcess(0, 100);
        }

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "UPDATE litho SET TotalCOCnt=? WHERE _id=?";
            pStmt = dbS3Conn.prepareStatement(upStr);
            fillTotalCountForTree("Litho", pStmt);
            pStmt.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (pStmt != null) pStmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        if (progressDelegate != null) progressDelegate.setProcessPercent(false);

    }

    
    /**
     * @throws SQLException
     */
    private void doBuildChronostrat() throws SQLException
    {
        int     cnt = 0;
        String  sql;
        Integer count = null;
        
        // GeologicTimePeriodID geologictimeperiod

        // Get Specimen Counts for ChronosStrat
        worker.firePropertyChange(MSG, "", "Getting ChronosStrat Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = adjustSQL("SELECT g.GeologicTimePeriodID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM collectionobject co " +
                        "INNER JOIN paleocontext p ON co.PaleoContextID = p.PaleoContextID " +
                        "INNER JOIN geologictimeperiod g ON p.ChronosStratID = g.GeologicTimePeriodID " +
                        "WHERE g.GeologicTimePeriodTreeDefID = GTPTREEDEFID GROUP BY g.GeologicTimePeriodID");
        
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        // Get All Unsed Litho items
        worker.firePropertyChange(MSG, "", "Pruning ChronosStrat Tree...");
         worker.firePropertyChange(MSG, "", "Building ChronosStrat...");
        
        String prefix  = "SELECT g.GeologicTimePeriodID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
        String fullPostfix = adjustSQL(" FROM geologictimeperiod g LEFT JOIN paleocontext p ON p.ChronosStratID = g.GeologicTimePeriodID " +
                                       "LEFT JOIN collectionobject co ON co.PaleoContextID = p.PaleoContextID " +
                                       "WHERE g.GeologicTimePeriodTreeDefID = GTPTREEDEFID AND co.CollectionObjectID IS NOT NULL");
        
        sql = "SELECT COUNT(*) " + fullPostfix;
        log.debug(sql);
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + fullPostfix + " GROUP BY g.GeologicTimePeriodID";
        log.debug(sql);
        
        tblWriter.log("ChronosStrat Issues");
        tblWriter.startTable();
        tblWriter.logHdr("Full Name", "Rank Id", "Issue");
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            dbS3Conn.setAutoCommit(false);
            int transCnt = 0;
            
            cnt = 0;
            String upSQL = "INSERT INTO gtp (_id, FullName, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2)); // FullName
                s3Stmt.setInt(3,    rs.getInt(3));    // RankID
                s3Stmt.setInt(4,    rs.getInt(4));    // ParentID
                
                int highestNodeNum = rs.getInt(5);    
                int nodeNumber     = rs.getInt(6);
                
                s3Stmt.setInt(5,    0);          // Total Number of Specimens below
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(6,    numObjs);          // Number of Specimens this Chronos has
                
                s3Stmt.setInt(7,    highestNodeNum);
                s3Stmt.setInt(8,    nodeNumber);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating litho: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Chronos: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        tblWriter.endTable();
        
        worker.firePropertyChange(MSG, "", "Updating Total Chronostratigraphy Counts...");
        
        treeCnt = 0;

        //sql = adjustSQL("SELECT GeologicTimePeriodID FROM geologictimeperiod WHERE GeologicTimePeriodTreeDefID = GTPTREEDEFID AND RankID = 0");
        //int rootId = getCountAsInt(dbConn, sql);
        
        sql = adjustSQL("SELECT COUNT(*) FROM geologictimeperiod WHERE GeologicTimePeriodTreeDefID = GTPTREEDEFID");
        treeTotal = getCountAsInt(dbConn, sql);
        if (treeTotal == 0)
        {
            return;
        }
        treePercent = treeTotal / 20;
        if (progressDelegate != null) 
        {
            progressDelegate.setProcessPercent(true);
            progressDelegate.setProcess(0, 100);
        }

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "UPDATE gtp SET TotalCOCnt=? WHERE _id=?";
            pStmt = dbS3Conn.prepareStatement(upStr);
            fillTotalCountForTree("GTP", pStmt);
            pStmt.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (pStmt != null) pStmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        if (progressDelegate != null) progressDelegate.setProcessPercent(false);

    }
        
    /**
     * @throws SQLException
     */
    @SuppressWarnings("unused")
    private void doBuildGeography() throws SQLException
    {
        //doBuildColObjToGeoMapping();
        
        doBuildGeographyContCountry();
        
        int     cnt = 0;
        String  sql;
        Integer count = null;

        // Get Specimen Counts for Geography
        worker.firePropertyChange(MSG, "", "Getting Geography Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = adjustSQL("SELECT g.GeographyID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM geography g INNER JOIN locality l ON g.GeographyID = l.GeographyID " +
                        "INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                        "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                       "WHERE co.CollectionID = COLMEMID GROUP BY g.GeographyID ");
        log.debug(sql);
        
        for (Object[] row : query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }

        // Get All Unsed Geo items
        worker.firePropertyChange(MSG, "", "Building Geography...");
        
        String prefix  = "SELECT g.GeographyID, FullName, GeographyCode, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
        String postfix = adjustSQL("FROM geography g " +
                                   "LEFT JOIN locality l ON g.GeographyID = l.GeographyID " +
                                   "LEFT JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                                   "LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                                   "WHERE co.CollectionID = COLMEMID AND co.CollectionObjectID IS NOT NULL");
        
        sql = "SELECT COUNT(*) " + postfix;
        log.debug(sql);
        
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix + " GROUP BY g.GeographyID ORDER BY FullName";
        log.debug(sql);
        
        tblWriter.log("Geography Issues");
        tblWriter.startTable();
        tblWriter.logHdr("Full Name", "Rank Id", "Issue");
       
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            dbS3Conn.setAutoCommit(false);
            int transCnt = 0;
            
            cnt = 0;
            String upSQL = "INSERT INTO geo (_id, FullName, ISOCode, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum, ContinentId, CountryId, Latitude, Longitude) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2)); // FullName
                s3Stmt.setString(3, rs.getString(3)); // GeoCode / ISOCode
                s3Stmt.setInt(4,    rs.getInt(4));    // RankID
                s3Stmt.setInt(5,    rs.getInt(5));    // ParentID
                
                int highestNodeNum = rs.getInt(6);    
                int nodeNumber     = rs.getInt(7);
                
                s3Stmt.setInt(6,    0);          // Total Number of Specimens below
                
                Integer numObjs = numObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(7,    numObjs);          // Number of Specimens this Geo has
                
                s3Stmt.setInt(8,    highestNodeNum);
                s3Stmt.setInt(9,    nodeNumber);
                
                Integer continentId  = geoToGeoContinentMapper.get(id); // Get GeoID for the Continent that the Locality belongs to.
                Integer countryId    = geoToGeoCountryMapper.get(id);   // Get GeoID for the Country that the Locality belongs to.

                if (continentId != null)
                {
                    s3Stmt.setInt(10, continentId);        // GeoID (continentId)
                } else
                {
                    s3Stmt.setObject(10, null);
                    tblWriter.logWithSpaces(rs.getString(2), ((Integer)rs.getInt(4)).toString(), "Missing Continent");
                }
                
                if (countryId != null)
                {
                    s3Stmt.setInt(11, countryId);        // GeoID (CountryID)
                } else
                {
                    s3Stmt.setObject(11, null);
                    tblWriter.logWithSpaces(rs.getString(2), ((Integer)rs.getInt(4)).toString(), "Missing Country");
                }
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating geo: "+id);
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Geography: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        tblWriter.endTable();
        
        worker.firePropertyChange(MSG, "", "Updating Total Geography Counts...");
        
        treeCnt = 0;

//        sql = adjustSQL("SELECT GeographyID FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = 0");
//        int rootId = getCountAsInt(dbConn, sql);
        
        sql = adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID");
        treeTotal = getCountAsInt(dbConn, sql);
        treePercent = treeTotal / 20;
        if (progressDelegate != null) 
        {
            progressDelegate.setProcessPercent(true);
            progressDelegate.setProcess(0, 100);
        }

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt = null;
        try
        {
            String upStr = "UPDATE geo SET TotalCOCnt=? WHERE _id=?";
            pStmt = dbS3Conn.prepareStatement(upStr);
            fillTotalCountForTree("Geo", pStmt);
            pStmt.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (pStmt != null) pStmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        if (progressDelegate != null) progressDelegate.setProcessPercent(false);
    }
    
    int treePercent = 1;
    int treeCnt     = 0;
    int treeTotal   = 0;
    
    /**
     * @param parentId
     * @param pStmt
     * @return
     * @throws SQLException
     */
    private void fillTotalCountForTree(final String tableClassName, 
                                       final PreparedStatement pStmt) throws SQLException
    {
        HashMap<Integer, Integer> idToTotal = new HashMap<Integer, Integer>();
        String tableName = tableClassName.toLowerCase();
        String sql       = String.format("SELECT DISTINCT RankID FROM %s ORDER BY RankID DESC", tableName);
        Vector<Integer> rankIds = queryForInts(dbS3Conn, sql);
        int prevPercent = -1;
        for (int i=1;i<rankIds.size();i++)
        {
            log.debug("RankID: "+rankIds.get(i));
            idToTotal.clear();
            sql = String.format("SELECT _id,NodeNum,HighNodeNum FROM %s WHERE RankID = %d", tableName, rankIds.get(i));
            Vector<Object[]> items = query(dbS3Conn, sql);
            log.debug("RankID: "+rankIds.get(i)+ " Size: "+items.size());
            for (int j=0;j<items.size();j++)
            {
                Object[] values = items.get(j);
                int id  = (Integer)values[0];
                int nn  = (Integer)values[1];
                int hnn = (Integer)values[2];    
                 sql = String.format("SELECT SUM(NumObjs) FROM %s WHERE NodeNum > %d AND HighNodeNum <= %d", tableName, nn, hnn);
                 Integer kidTotal = BasicSQLUtils.getCount(dbS3Conn, sql);
                 if (kidTotal != null && kidTotal > 0)
                 {
                     idToTotal.put(id, kidTotal);
                 }
                 int percent = (int)((double)j * 100.0 /  (double)items.size());
                 if (percent != prevPercent)
                 {
                     worker.firePropertyChange(PROGRESS, 0, (int)((double)j * 100.0 /  (double)items.size()));
                     prevPercent = percent;
                 }
            }
            worker.firePropertyChange(PROGRESS, 0, 100);
            int j = 0;
            prevPercent = -1;
            for (Integer id : idToTotal.keySet())
            {
                pStmt.setInt(1, idToTotal.get(id));
                pStmt.setInt(2, id);
                if (pStmt.executeUpdate() != 1)
                {
                    log.error(String.format("Error updating ParentID %d KidCnt: %d  Tbl: %s", id, idToTotal.get(id), tableName));
                }
                 int percent = (int)((double)j * 100.0 /  (double)items.size());
                 if (percent != prevPercent)
                 {
                     worker.firePropertyChange(PROGRESS, 0, (int)((double)j * 100.0 /  (double)items.size()));
                     prevPercent = percent;
                 }
            }
        }
    }
//    private int fillTotalCountForTree(final String tablePrefix, 
//                                     final int parentId, 
//                                     final int nodeNum,
//                                     final int highNodeNum,
//                                     final PreparedStatement pStmt) throws SQLException
//    {
//        // select SUM(NumObjs) FROM taxon WHERE NodeNum > 3266 AND HighNodeNum <= 3270;
//        if (treeCnt % treePercent == 0)
//        {
//            worker.firePropertyChange(PROGRESS, 0, (int)((double)treeCnt * 100.0 /  (double)treeTotal));
//        }
//        treeCnt++;
//        
//        String  sql   = String.format("SELECT SUM(NumObjs) FROM %s WHERE NodeNum > %d AND HighNodeNum <= %d", tablePrefix.toLowerCase(), nodeNum, highNodeNum);
//        Integer total = getCount(dbS3Conn, sql);
//        if (total == null) total = 0;
//        
//        int kidTotal = 0;
//        sql = String.format("SELECT _id,NodeNum,HighNodeNum FROM %s WHERE ParentID = %d", tablePrefix.toLowerCase(), parentId);
//        for (Object[] values : query(dbS3Conn, sql))
//        {
//            int id  = (Integer)values[0];
//            int nn  = (Integer)values[1];
//            int hnn = (Integer)values[2];            
//            kidTotal += fillTotalCountForTree(tablePrefix, id, nn, hnn, pStmt);
//        }
//        
//        //if (kidTotal > 0) System.out.println(String.format("%d -> %d", parentId, kidTotal));
//        if (kidTotal > 0)
//        {
//            pStmt.setInt(1, kidTotal);
//            pStmt.setInt(2, parentId);
//            if (pStmt.executeUpdate() != 1)
//            {
//                log.error(String.format("Error updating ParentID %d KidCnt: %d  Tbl: %s", parentId, kidTotal, tablePrefix));
//            }
//        }
//        return total + kidTotal;
//    }
    
    /**
     * @param pStmt
     * @param value
     * @param index
     * @throws SQLException
     */
    private void setInt(final PreparedStatement pStmt, final Integer value, final int index) throws SQLException
    {
        if (value != null)
        {
            pStmt.setInt(index, value);     // Type Status
        } else
        {
            pStmt.setObject(index, null);
        }
    }
    
    /**
     * @param rs
     * @param s3StmtPrep
     * @param prevId
     * @param typeStatusHash
     * @return
     * @throws SQLException
     */
    private int writeColObj(final ResultSet rs, 
                            final PreparedStatement s3StmtPrep, 
                            final int prevId,
                            final HashMap<String, Integer> typeStatusHash,
                            final HashMap<Integer, Integer> taxaToFamily,
                            final PreparedStatement s3Stmt2) throws SQLException
    {
        int id = rs.getInt(1);
        if (id != prevId) 
        {
            String catNum        = rs.getString(2);
            if (StringUtils.isEmpty(catNum)) return prevId;
            
            catNum = catNum.replaceFirst("^0+(?!$)", "");
            String fieldNumber   = rs.getString(3); // FieldNumber
            String stationFldNum = rs.getString(4); // StationFieldNumber
            
            Object typeStatus    = rs.getObject(8); // Taxon TypeStatus Name
            
            String  collectorNum = StringUtils.isNotEmpty(fieldNumber) ? fieldNumber : stationFldNum;
            
            Integer locId        = rs.getInt(10);
            if (rs.wasNull()) locId = null;
            
            Integer geoId        = rs.getInt(11);
            if (rs.wasNull()) geoId = null;
            
            Integer rankId       = rs.getInt(12);
            if (rs.wasNull()) rankId = null;
            
            Integer continentId  = locToGeoContinentMapper.get(locId); // Get GeoID for the Continent that the Locality belongs to.
            Integer countryId    = locToGeoCountryMapper.get(locId); // Get GeoID for the Country that the Locality belongs to.
            Integer coltrId      = colObjToAgent.get(id);
            Integer taxaId       = rs.getInt(9);
            Integer cntAmt       = colObjToCnt.get(id);
            
            if (continentId == null)
            {
                continentIdCnt++;
            }
            if (countryId == null)
            {
                countryIdCnt++;
            }
            if (coltrId == null)
            {
                coltrIdCnt++;
            }
            if (cntAmt == null)
            {
                cntAmtCnt++;
            }
            
            if (cntAmt == null) cntAmt = 1;
            
            s3StmtPrep.setInt(1,     id);               // ColObjID
            s3StmtPrep.setString(2,  catNum);           // Catalog Number
            s3StmtPrep.setInt(3,     cntAmt);           // CountAmt
            s3StmtPrep.setString(4,  collectorNum);     // Collector Number
            s3StmtPrep.setString(5,  rs.getString(5));  // Collected Date
            s3StmtPrep.setBoolean(6, rs.getBoolean(6)); // IsMappable
            s3StmtPrep.setBoolean(7, rs.getBoolean(7)); // HasImage
            
            setInt(s3StmtPrep, taxaId, 9);  // TaxonID
            setInt(s3StmtPrep, locId, 10);  // LocID
            
            if (typeStatus != null)
            {
                setInt(s3StmtPrep, typeStatusHash.get(typeStatus), 8);
            } else
            {
                s3StmtPrep.setObject(8, null);
            }
            
            if (countryId != null)
            {
                s3StmtPrep.setInt(11, countryId);        // GeoID (CountryID)
            } else
            {
                s3StmtPrep.setObject(11, null);
                String localityName = querySingleObj("SELECT LocalityName FROM locality WHERE LocalityID = "+locId);
                
                //"No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName
                tblWriter.logErrors(catNum, collectorNum == null ? "&nbsp;" : collectorNum,
                                    localityName == null ? "&nbsp;" : localityName, locId != null ? Integer.toString(locId) : "null", "Missing Country");
                //log.debug("No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName);
            }
            
            if (continentId != null)
            {
                s3StmtPrep.setInt(12, continentId);        // GeoID (continentId)
            } else
            {
                s3StmtPrep.setObject(12, null);
                String localityName = querySingleObj("SELECT LocalityName FROM locality WHERE LocalityID = "+locId);
                
                //"No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName
                tblWriter.logErrors(catNum, collectorNum == null ? "&nbsp;" : collectorNum,
                                    localityName == null ? "&nbsp;" : localityName, locId != null ? Integer.toString(locId) : "null", "Missing Continent");
                //log.debug("No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName);
            }
            
            setInt(s3StmtPrep, coltrId, 13);// CollectorID
            
            Integer familyId = null;
            if (taxaId != null)
            {
                if (taxaToFamily != null)
                {
                    familyId = taxaToFamily.get(taxaId);
                } else
                {
                    s3Stmt2.setInt(1, taxaId);
                    ResultSet rs2 = s3Stmt2.executeQuery();
                    if (rs2 != null && rs2.next())
                    {
                        familyId = rs2.getInt(1);
                    }
                }
            }
            
            setInt(s3StmtPrep, familyId, 14); // FamilyID
            setInt(s3StmtPrep, geoId, 15);    // GeographyID
            
            if (geoId != null && rankId != null && rankId == 300)
            {
                s3StmtPrep.setInt(16, geoId);
            } else
            {
                setInt(s3StmtPrep, geoId != null ? geoStateMapper.get(geoId) : null, 16);    // StateID
            }
            
            try
            {
                if (s3StmtPrep.executeUpdate() != 1)
                {
                    System.out.println("Error updating ColObj: "+id);
                }
                    
            } catch (SQLException ex)
            {
                System.out.println(ex.getMessage());
                System.out.println("Error updating ColObj: "+id+" CatNum: "+catNum);
            }
        }
        return id;
    }
    
    /**
     * @throws SQLException
     */
    private void buildColObjToAgent() throws SQLException
    {
        worker.firePropertyChange(MSG, "", "Building Collection Objects to Collectors...");
        
        IdMapperMgr.getInstance().setDBs(dbConn, dbConn);
        colObjToAgent = IdMapperMgr.getInstance().addHashMapper(COLOBJAGENTNAME, null, false);
        colObjToAgent.reset();

        String prefix  = "SELECT co.CollectionObjectID, a.AgentID ";
        String postfix = "FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                         "INNER JOIN collector c ON ce.CollectingEventID = c.CollectingEventID " +
                         "INNER JOIN agent a ON c.AgentID = a.AgentID WHERE c.OrderNumber = 1 AND CollectionID = COLMEMID";

        String sql    = adjustSQL("SELECT COUNT(*) " + postfix);
        int    totCnt = getCountAsInt(sql);
        int    inc    = Math.max(totCnt / 20, 1);
        
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        
        int       cnt   = 0;
        Statement stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);

        String sqlStr = prefix + postfix;
        //System.out.println(adjustSQL(sqlStr));
        ResultSet rs = stmt.executeQuery(adjustSQL(sqlStr)); // Get the GeoID and LocID
        while (rs.next())
        {
            colObjToAgent.put(rs.getInt(1), rs.getInt(2));
            cnt++;
            if (cnt % inc == 0) 
            {
                worker.firePropertyChange(PROGRESS, 0, cnt);
                //log.debug("LocID -> GeoID: "+cnt+ " -> "+getCountAsInt("SELECT COUNT(*) FROM geoloc"));
            }
        }
        rs.close();
        stmt.close();
        
        worker.firePropertyChange(PROGRESS, 0, totCnt);
        progressDelegate.incOverall();
    }
    
    /**
     * @throws SQLException 
     * 
     */
    private void fixColObjAttachments() throws SQLException
    {
        String prefix  = "SELECT co.CollectionObjectID ";
        String postfix = "FROM preparation p INNER JOIN collectionobject co ON p.CollectionObjectID = co.CollectionObjectID " +
                         "WHERE p.Text1 LIKE 'http://nhm.ku.edu/fishes/collectionimages/%' " +
                         "ORDER BY co.CollectionObjectID ASC ";
        
        int totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        String sql = prefix + postfix;

        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
    
            int transCnt = 0;
            int  cnt     = 0;
            
            s3Stmt = dbS3Conn.prepareStatement("UPDATE colobj SET HasImage=? WHERE _id=?");
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer colObjId = rs.getInt(1);
                
                s3Stmt.setInt(1,    1);
                s3Stmt.setInt(2,    colObjId);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
                }
                
                cnt++;
                if (cnt % 100 == 0) 
                {
                    //worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Col Objs: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
    
            rs.close();
            
        } catch (Exception ex) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            
            
            ex.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    
    /**
     * Count number of Specimens for each Collection Object.
     * @throws SQLException
     */
//    private void doBuildColObjToGeoMapping() throws SQLException
//    {
//        worker.firePropertyChange(MSG, "", "Locating Specimen Geographies...");
//        
//        String postSQL = " FROM geography g INNER JOIN locality l ON g.GeographyID = l.GeographyID " +
//                         "INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
//                         "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE CollectionID = COLMEMID";
//        String sql = adjustSQL("SELECT COUNT(*)" + postSQL);
//        
//        int totCnt = getCountAsInt(dbConn, sql);
//        if (progressDelegate != null) progressDelegate.setProcess(0, 100);
//
//        //----------------------------------------------------------
//        // Creating Mapping from ColObj top Geo
//        //----------------------------------------------------------
//        int fivePercent = Math.max(totCnt / 20, 1);
//        
//        int cnt = 0;
//        colObjToGeo = IdMapperMgr.getInstance().addHashMapper(COLOBJGEONAME, null, false);
//        colObjToGeo.reset();
//
//        sql = adjustSQL("SELECT co.CollectionObjectID, g.GeographyID " + postSQL);
//        Statement stmt2 = dbConn.createStatement();
//        ResultSet rs    = stmt2.executeQuery(adjustSQL(sql));
//        while (rs.next())
//        {
//            colObjToGeo.put(rs.getInt(1), rs.getInt(2));
//            cnt++;
//            if (cnt % fivePercent == 0) 
//            {
//                worker.firePropertyChange(PROGRESS, 0, Math.max((100 * cnt) / totCnt, 1));
//            }
//        }
//        rs.close();
//        stmt2.close();
//    }
    
    /**
     * Count number of Specimens for each Collection Object.
     * @throws SQLException
     */
    private void doBuildColObjCounts() throws SQLException
    {
        worker.firePropertyChange(MSG, "", "Calculating Specimen Counts...");
        
        String sql = "SELECT COUNT(*) FROM collectionobject WHERE CollectionID = COLMEMID";
        
        int totCnt = getCountAsInt(dbConn, adjustSQL(sql));
        if (progressDelegate != null) progressDelegate.setProcess(0, totCnt);

        //----------------------------------------------------------
        // Count number of Specimens for each Collection Object
        //----------------------------------------------------------
        int fivePercent = Math.max(totCnt / 20, 1);
        
        int cnt = 0;
        colObjToCnt = IdMapperMgr.getInstance().addHashMapper(COLOBJCNTNAME, null, false);
        colObjToCnt.reset();

        sql = "SELECT co.CollectionObjectID, co.CountAmt, COUNT(IF (p.CountAmt IS NULL, 0, p.CountAmt)) " +
              "FROM collectionobject co LEFT JOIN preparation p ON co.CollectionObjectID = p.CollectionObjectID " +
              "WHERE co.CollectionID = COLMEMID GROUP BY co.CollectionObjectID";
        Statement stmt2 = dbConn.createStatement();
        ResultSet rs    = stmt2.executeQuery(adjustSQL(sql));
        while (rs.next())
        {
            int coCntAmt = rs.getInt(2);
            int prCntAmt = rs.getInt(3);
            int cntAmt   = coCntAmt < 1 && prCntAmt < 1 ? 1 : Math.max(coCntAmt, prCntAmt);
            colObjToCnt.put(rs.getInt(1), cntAmt);
            cnt++;
            if (cnt % fivePercent == 0) 
            {
                worker.firePropertyChange(PROGRESS, 0, cnt);
            }
        }
        rs.close();
        stmt2.close();
    }
    
    /**
     * @throws SQLException
     */
    private void doBuildColObjs() throws SQLException
    {
        doBuildColObjCounts();
        
        int cnt = 0;
        String sql;
        
        worker.firePropertyChange(MSG, "", "Building Collection Objects...");
        
        cnt = 0;
        sql = adjustSQL("SELECT TypeStatusName, COUNT(TypeStatusName) CNT FROM determination WHERE CollectionMemberID = COLMEMID AND TypeStatusName IS NOT NULL GROUP BY TypeStatusName ORDER BY CNT DESC");
        HashMap<String, Integer> typeStatusHash = new HashMap<String, Integer>();
        for (Object[] row : query(sql))
        {
            typeStatusHash.put(row[0].toString(), getCount(row[1]));
        }
        
        /*
        `_id` INTEGER PRIMARY KEY, 
        `CatalogNumber` VARCHAR(16),
        `CollectorNumber` VARCHAR(16),
        `CollectedDate` DATETIME,
        `IsMappable` BOOLEAN,
        `HasImage` BOOLEAN,
        `TaxonID` INTEGER,
        `LocID` INTEGER,
        */
        //UIRegistry.showError("Stop");
        
        String prefix  = "SELECT co.CollectionObjectID, co.CatalogNumber, co.FieldNumber, ce.StationFieldNumber, ce.StartDate, " +
                         "if (l.Latitude1 IS NOT NULL AND l.Longitude1 IS NOT NULL, TRUE, FALSE) AS IsMappable, " +
                         "if (ca.AttachmentID IS NOT NULL, TRUE, FALSE) AS HasImage, d.TypeStatusName, d.TaxonID, ce.LocalityID, l.GeographyID, g.RankID  ";
        
        String postfix = "FROM collectionobject co " +
                         "LEFT JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                         "LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                         "LEFT OUTER JOIN collectionobjectattachment ca ON co.CollectionObjectID = ca.CollectionObjectID " +
                         "LEFT JOIN locality l ON ce.LocalityID = l.LocalityID " +
                         "LEFT JOIN geography g ON l.GeographyID = g.GeographyID " +
                         "WHERE d.IsCurrent = TRUE AND co.CollectionID = COLMEMID " +
                         "ORDER BY co.CatalogNumber ASC";
        
        sql = "SELECT COUNT(*) " + adjustSQL(postfix);
        System.out.println(sql);
        
        /*(HashSet<Integer> keepers = new HashSet<Integer>();
        String sql1 = "SELECT co1.CollectionObjectID, co2.CollectionObjectID FROM collectionobject AS co1 " +
                      "Inner Join collectionobject AS co2 ON co1.FieldNumber = co2.FieldNumber  WHERE co1.YesNo1 =  1 AND co2.YesNo1 =  0";
        for (Object[] row : query(dbConn, sql1))
        {
            keepers.add((Integer)row[0]);
            keepers.add((Integer)row[1]);
        }*/
        
        int totCnt = getCountAsInt(dbConn, sql);
        if (progressDelegate != null) progressDelegate.setProcess(0, totCnt);
        sql = prefix + postfix;
        System.out.println(sql);
        
        cnt = 0;
        int fivePercent = Math.max(totCnt / 20, 1);
        boolean isSmallCollection = totCnt < 100000;
        HashMap<Integer, Integer> taxaToFamily = null;
        if (isSmallCollection)
        {
            taxaToFamily   = new HashMap<Integer, Integer>();
            Statement stmt = dbS3Conn.createStatement();

            ResultSet rs   = stmt.executeQuery("SELECT _id, FamilyID FROM taxon WHERE FamilyID IS NOT NULL ORDER BY _id");
            while (rs.next())
            {
                taxaToFamily.put(rs.getInt(1), rs.getInt(2));
                cnt++;
                if (cnt % fivePercent == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    //log.debug("1Collection Object: "+cnt);
                }
            }
            rs.close();
            stmt.close();
        }
        
        locToGeoContinentMapper.setShowLogErrors(false);
        locToGeoCountryMapper.setShowLogErrors(false);
        colObjToAgent.setShowLogErrors(false);
        
        tblWriter.log("Collection Object's with missing geography");
        tblWriter.startTable();
        tblWriter.logHdr("Cat Num", "Collector Num", "Locality", "Col Event ID", "Issue");
        
        int transCnt = 0;
        int prevId   = -1;
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        Statement         stmt       = null;
        PreparedStatement s3StmtPrep = null;
        try
        {
            s3Stmt     = dbS3Conn.prepareStatement("SELECT FamilyID FROM taxon WHERE _id = ?");
            //                                                           1           2         3            4                5            6          7          8          9      10      11           12         13            14      15      16
            s3StmtPrep = dbS3Conn.prepareStatement("INSERT INTO colobj (_id, CatalogNumber, CountAmt, CollectorNumber, CollectedDate, IsMappable, HasImage, TypeStatus, TaxonID, LocID, CountryID, ContinentID, CollectorID, FamilyID, GeoID, StateID) " +
                                                   "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            cnt = 0;
            System.out.println(adjustSQL(sql));
        
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                int id = rs.getInt(1);
                if (id == 30512)
                {
                    System.out.println("");
                }
                //if (!keepers.contains(id)) continue;
                
                transCnt++;
   
                prevId = writeColObj(rs, s3StmtPrep, prevId, typeStatusHash, taxaToFamily, s3Stmt);
                cnt++;
                if (cnt % fivePercent == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    //log.debug("2Collection Object: "+cnt);
                }
            }
            
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) {ex2.printStackTrace();}
            
            System.out.println(String.format("continentIdCnt: %d  countryIdCnt: %d  coltrIdCnt: %d  cntAmtCnt: %d  ", continentIdCnt, countryIdCnt, coltrIdCnt, cntAmtCnt));
            countryIdCnt = coltrIdCnt = cntAmtCnt = 0;
    
            rs.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
            return;
        }
        
        tblWriter.endTable();
        
        ///////////////////////////////////////////////////////////////////
        // Now do all the ColObjs that do not have a Current Determination 
        ///////////////////////////////////////////////////////////////////
        
         postfix = "FROM (SELECT co.CollectionObjectID, SUM(if (d.IsCurrent = TRUE, 1, 0)) S, COUNT(co.CollectionObjectID) C " +
                   "FROM collectionobject co " +
                   "INNER JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                   "WHERE co.CollectionID = COLMEMID " + 
                   "GROUP BY co.CollectionObjectID) T1 WHERE s = 0";
         
        totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        
        sql = "SELECT * " + postfix;

        locToGeoContinentMapper.setShowLogErrors(false);
        locToGeoCountryMapper.setShowLogErrors(false);
        
        String postfix2 = "FROM collectionobject co " +
                          "LEFT JOIN determination d ON co.CollectionObjectID = d.CollectionObjectID " +
                          "LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " +
                          "LEFT OUTER JOIN collectionobjectattachment ca ON co.CollectionObjectID = ca.CollectionObjectID " +
                          "LEFT JOIN locality l ON ce.LocalityID = l.LocalityID " +
                          "LEFT JOIN geography g ON l.GeographyID = g.GeographyID " +
                          "WHERE co.CollectionObjectID = %d AND co.CollectionID = COLMEMID " +
                          "ORDER BY co.CatalogNumber ASC";
        
        ArrayList<Integer> noDetColObjIds = new ArrayList<Integer>();
        
        transCnt = 0;
        cnt      = 0;
        
        Statement stmt2 = null;
        try
        {
            stmt2 = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt2.setFetchSize(Integer.MIN_VALUE);
            
           prevId = -1;
            //System.out.println(adjustSQL(sql));
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next()) // loop through all the Col Obj Records with just a FALSE determination
            {
                //int id = rs.getInt(1);
                //if (!keepers.contains(id)) continue;
    
                int colObjID = rs.getInt(1);
                sql = prefix + String.format(adjustSQL(postfix2), colObjID);
                noDetColObjIds.add(colObjID);
                
                ResultSet rs2 = stmt2.executeQuery(sql);
                if (rs2.next())
                {
                    transCnt++;
                    prevId = writeColObj(rs2, s3StmtPrep, prevId, typeStatusHash, taxaToFamily, s3Stmt);
                    cnt++;
                    if (cnt % 1000 == 0)
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                        log.debug("Collection Object: " + cnt);
                    }
                }
                
                try
                {
                    if (transCnt > 0) dbS3Conn.commit();
                } catch (Exception ex2) { ex2.printStackTrace();}

    
                rs2.close();
            }
            rs.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            stmt.close();
            if (stmt2 != null) stmt2.close();
            if (s3Stmt != null) s3Stmt.close();
            if (s3StmtPrep != null) s3StmtPrep.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
        
        for (Integer colObjID : noDetColObjIds)
        {
            String cn = querySingleObj("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+colObjID);
            log.debug("Cat Num missing Current Determination: "+cn);
        }

        System.out.println(String.format("geoIdCnt: %d  coltrIdCnt: %d  cntAmtCnt: %d", countryIdCnt, coltrIdCnt, cntAmtCnt));
        
        //updateFamilyCounts();
        
        if (DBConnection.getInstance().getDatabaseName().equals("kui_fish_dbo_6"))
        {
            fixColObjAttachments();
        }
   }
    
//   private void doAuxInfo()
//   {
//       worker.firePropertyChange(MSG, "", "Writing Auxillary Information");
//       try
//       {
//           dbS3Conn.setAutoCommit(false);
//       } catch (Exception ex2) { ex2.printStackTrace();}
//       
//       PreparedStatement s3Stmt     = null;
//       try
//       {
//           s3Stmt  = dbS3Conn.prepareStatement("INSERT INTO stats (Tbl, Grp, Name, Descr) VALUES (?,?,?,?)");
//           for (String key : this.auxInfoMap.keySet())
//           {
//               s3Stmt.setInt(1,    0);
//               s3Stmt.setInt(2,    0);
//               s3Stmt.setString(3, key);
//               s3Stmt.setString(4, this.auxInfoMap.get(key));
//               if (s3Stmt.executeUpdate() != 1)
//               {
//                   //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
//               }
//           }
//       } catch (Exception e) 
//       {
//           try
//           {
//               dbS3Conn.rollback();
//           } catch (Exception ex2) {}
//           e.printStackTrace();
//           
//       } finally
//       {
//           
//           try
//           {
//               if (s3Stmt != null) s3Stmt.close();
//               dbS3Conn.setAutoCommit(true);
//           } catch (Exception ex2) { ex2.printStackTrace();}
//       }
//   }

   
    /*private int getFamilyTaxId(final int nodeNumber)
    {
        for (Integer famId : taxMapper.keySet())
        {
            Pair<Integer, Integer> p = taxMapper.get(famId);
            if (p != null)
            {
                //System.out.println(String.format("F: %d   NN: %d   HN: %d", famId, p.first, p.second));
                if (nodeNumber > p.first && nodeNumber <= p.second)
                {
                    //System.out.println("Found Taxon: "+famId);
                    return famId;
                }
            }
        }
        return -1;
    }*/
       
    /**
     * @param conn
     * @throws SQLException
     */
    public static void doTestFamiles(final Connection conn) throws SQLException
    {
        HashMap<Integer, Pair<Integer, Integer>> taxMapper = new HashMap<Integer, Pair<Integer, Integer>>();
        HashMap<Pair<Integer, Integer>, Integer> revMapper = new HashMap<Pair<Integer, Integer>, Integer>();
        int[] array = null;
        int largestVal = 0;
        
        TreeSet<Integer> familyIdsSet = new TreeSet<Integer>();
        
        Vector<Pair<Integer, Integer>>     list = null;
        Comparator<Pair<Integer, Integer>> cmpr = new Comparator<Pair<Integer, Integer>>()
        {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2)
            {
                return o1.first.compareTo(o2.first);
            } 
        };
        
        String postfix;
        
        QueryAdjusterForDomain qad = QueryAdjusterForDomain.getInstance();
        
        Statement         stmt   = null;
        try
        {
            stmt  = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            String prefix = "SELECT TaxonID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
            for (int i=0;i<2;i++)
            {
                postfix = String.format("FROM taxon WHERE %s AND HighestChildNodeNumber IS NOT NULL AND NodeNumber IS NOT NULL ORDER BY FullName", i == 0 ? "RankID = 140" : "RankID > 140");
                
                String sql = prefix + postfix;
                
                System.out.println(sql);
                
                if (i == 1)
                {
                    list = new Vector<Pair<Integer, Integer>>(taxMapper.values());
                    Collections.sort(list, cmpr);
                    
                    array = new int[taxMapper.size()];
                    int inx = 0;
                    for (Pair<Integer, Integer> p : list)
                    {
                        array[inx] = p.first;
                        largestVal = p.second;
                        System.out.println(String.format("%d -> %d %d", inx, p.first, p.second));
                        inx++;
                    }
                }
                
                long stTm = System.currentTimeMillis();
                ResultSet rs = stmt.executeQuery(qad.adjustSQL(sql));
                while (rs.next())
                {
                    Integer id = rs.getInt(1);
                    
                    int highestNodeNum = rs.getInt(5);
                    int nodeNumber     = rs.getInt(6);
                    
                    Integer familyId = null;
                    
                    if (i == 0) // zero mean processing Families
                    {
                        Pair<Integer, Integer> p = new Pair<Integer, Integer>(nodeNumber, highestNodeNum);
                        taxMapper.put(id, p);
                        revMapper.put(p, id);
                    } else
                    {
                        boolean isFast = false;
                        if (isFast)
                        {
                            if (id <= largestVal)
                            {
                                int inx = Arrays.binarySearch(array, nodeNumber);
                                //System.out.println(id + " => " + inx);
                                if (inx < 0)
                                {
                                    inx = Math.abs(inx) - 2;
                                }
                                if (inx > -1)
                                {
                                    Pair<Integer, Integer> p = list.get(inx);
                                    familyId = revMapper.get(p);
                                    familyIdsSet.add(familyId);
                                }
                                System.out.println(familyId + " => " + id);
                            }
                        } else
                        {
                            for (Integer famId : taxMapper.keySet())
                            {
                                Pair<Integer, Integer> p = taxMapper.get(famId);
                                if (p != null)
                                {
                                    //System.out.println(String.format("F: %d   NN: %d   HN: %d", famId, p.first, p.second));
                                    if (nodeNumber >= p.first && nodeNumber <= p.second)
                                    {
                                        familyId = famId;
                                        familyIdsSet.add(famId);
                                        System.out.println(familyId + " => " + id + " " + nodeNumber);
                                        break;
                                    }
                                }
                            }
                        }
                        
                        if (familyId == null)
                        {
                            System.out.println(String.format("NN: %d  TX: %d NOT Found.", nodeNumber, id));
                        }
                    }
                }
                rs.close();
                System.out.println(System.currentTimeMillis() - stTm);
            }
            
        } catch (Exception e) 
        {
            
        } finally
        {
            if (stmt != null) stmt.close();
        }

    }

    /**
     * @throws SQLException
     */
//    private void updateFamilyCounts() throws SQLException
//    {
//        int cnt = 0;
//        
//        worker.firePropertyChange(MSG, "", "Calculating Taxonomy Counts...");
//
//        if (progressDelegate != null) progressDelegate.getProcessProgress().setIndeterminate(true);
//        
//        dbS3Conn.setAutoCommit(false);
//        PreparedStatement s3Stmt     = null;
//        Statement         stmt       = null;
//        try
//        {
//            int missingFamilyCnt = 0;
//            
//            s3Stmt = dbS3Conn.prepareStatement("UPDATE taxon SET NumObjs=? WHERE _id = ?");
//
//            HashMap<Integer, Integer> famCounts = new HashMap<Integer, Integer>();
//            
//            // First, we get the counts for all ColObj's and the Taxon they point at directly.
//            stmt   = dbS3Conn.createStatement();
//            String    sql = "SELECT t._id, t.RankID, NodeNum, CNT FROM (SELECT TaxonID, SUM(CountAmt) CNT FROM colobj GROUP BY TaxonID) T1 INNER JOIN taxon t ON TaxonID = t._id";
//            ResultSet rs  = stmt.executeQuery(sql);
//            while (rs.next()) // loop through all the Col Obj Records with just a FALSE determination
//            {
//                int id      = rs.getInt(1);
//                int rankId  = rs.getInt(2);
//                int nodeNum = rs.getInt(3);
//                int amt     = rs.getInt(4);
//                
//                if (rankId < 140) continue;
//                
//                Integer familyId = null;
//                if (rankId == 140)
//                {
//                    familyId = id;
//                } else
//                {
//                    int inx = Arrays.binarySearch(array, nodeNum);
//                    if (inx < 0)
//                    {
//                        inx = Math.abs(inx) - 2;
//                    }
//                    if (inx > -1)
//                    {
//                        Pair<Integer, Integer> p = familyIdList.get(inx);
//                        familyId = revMapper.get(p);
//                    }
//                    
//                    if (familyId == null)
//                    {
//                        missingFamilyCnt++;
//                    }
//                    
//                    s3Stmt.setInt(1, amt);
//                    s3Stmt.setInt(2, id);
//                    int rv = s3Stmt.executeUpdate();
//                    if (rv != 1)
//                    {
//                        System.err.println("Error updating FamilyID: "+id+"  CntAmt: "+amt + "  rv: "+rv);
//                    }
//                }
//
//                if (familyId != null)
//                {
//                    Integer sum = famCounts.get(familyId);
//                    if (sum == null)
//                    {
//                        sum = 1;
//                    } else
//                    {
//                        sum += amt;
//                    }
//                    famCounts.put(familyId, sum);
//                }
//            }
//            
//            System.out.println("Missing Families: "+missingFamilyCnt);
//            
//            int totCnt = famCounts.size();
//            int tenPercent = totCnt / 10;
//            
//            if (progressDelegate != null)
//            {
//                progressDelegate.getProcessProgress().setIndeterminate(false);
//                //progressDelegate.setProcess(1, 100);
//                progressDelegate.setProcess(1);
//            }
//            worker.firePropertyChange(MSG, "", "Updating Taxonomy Counts...");
//
//            for (Integer txId : famCounts.keySet())
//            {
//                int amt = famCounts.get(txId);
//                s3Stmt.setInt(1, amt);
//                s3Stmt.setInt(2, txId);
//                
//                //System.out.println(String.format("%d - %d", txId, amt));
//                
//                int rv = s3Stmt.executeUpdate();
//                if (rv != 1)
//                {
//                    System.err.println("Error updating FamilyID: "+txId+"  CntAmt: "+amt + "  rv: "+rv);
//                }
//                cnt++;
//                if (cnt % tenPercent == 0)
//                {
//                    worker.firePropertyChange(PROGRESS, 0, cnt);
//                }
//            }
//            rs.close();
//         
//            treeCnt     = 0;
//            sql         = adjustSQL("SELECT COUNT(*) FROM taxon WHERE TaxonTreeDefID = TAXTREEDEFID");
//            treeTotal   = getCountAsInt(dbConn, sql);
//            treePercent = Math.max(treeTotal / 20, 1);
//            if (progressDelegate != null) 
//            {
//                progressDelegate.setProcessPercent(true);
//                progressDelegate.setProcess(0, 100);
//            }
//    
//            s3Stmt.close();
//            
//            if (progressDelegate != null) progressDelegate.setProcess(1);
//            
//            String upStr = "UPDATE taxon SET TotalCOCnt=? WHERE _id=?";
//            s3Stmt = dbS3Conn.prepareStatement(upStr);
//            
//            worker.firePropertyChange(MSG, "", "Cascading Taxonomy Updates...");
//            sql = "SELECT _id,NodeNum,HighNodeNum FROM taxon WHERE RankID = 140";
//            for (Object[] values : query(dbS3Conn, sql))
//            {
//                int familyId    = (Integer)values[0];
//                int nodeNum     = (Integer)values[1];
//                int highNodeNum = (Integer)values[2];
//                fillTotalCountForTree("Taxon", s3Stmt);
//            }
//            if (progressDelegate != null) progressDelegate.setProcessPercent(false);
//            
//            worker.firePropertyChange(MSG, "", "Done Upadating Taxonomy.");
//            
//        } catch (Exception e) 
//        {
//            try
//            {
//                dbS3Conn.rollback();
//            } catch (Exception ex2) {}
//            e.printStackTrace();
//            
//        } finally
//        {
//            if (stmt != null) stmt.close();
//            if (s3Stmt != null) s3Stmt.close();
//            
//            try
//            {
//                dbS3Conn.setAutoCommit(true);
//            } catch (Exception ex2) { ex2.printStackTrace();}
//        }
//    }
    
    /**
     * @throws SQLException
     */
    private void doBuildImages() throws SQLException
    {
        /*
         `ImgID` INTEGER PRIMARY KEY, 
         `ImgName` VARCHAR(32),
         `TableID` BYTE
         */
        updateURLPathIntoStats(imageURL, isUsingDirectURL);
        
        boolean useOrigName = false;
        
        String  sql;
        //Integer count = null;

        worker.firePropertyChange(MSG, "", getResourceString("CRE_IMG_ATTCHS"));
        
        String prefix  = String.format("SELECT co.CollectionObjectID, a.AttachmentID, %s ", useOrigName ? "a.OrigFilename" : "a.AttachmentLocation");
        String postfix = "FROM collectionobject co " +
                         "INNER JOIN collectionobjectattachment ca ON co.CollectionObjectID = ca.CollectionObjectID " +
                         "INNER JOIN attachment a ON ca.AttachmentID = a.AttachmentID " +
                         "ORDER BY co.CollectionObjectID";
        
        int totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix;

        int transCnt = 0;
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        Statement         stmt       = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            int cnt = 0;
            s3Stmt  = dbS3Conn.prepareStatement("INSERT INTO img (_id, OwnerID, TableID, ImgName) VALUES (?,?,?,?)");
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer colObjId = rs.getInt(1);
                Integer attID    = rs.getInt(2);
                String  imgName  = rs.getString(3);
                
                if (useOrigName)
                {
                    imgName = FilenameUtils.getName(imgName);
                }
                
                s3Stmt.setInt(1,    attID);
                s3Stmt.setInt(2,    colObjId);
                s3Stmt.setInt(3,    1);
                s3Stmt.setString(4, imgName);
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
                }
                
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Img: "+cnt);
                }
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            rs.close();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    
    /**
     * @throws SQLException
     */
    /*private void doBuildImagesFromPreps() throws SQLException
    {
        
//         `ImgID` INTEGER PRIMARY KEY, 
//         `ImgName` VARCHAR(32),
//         `TableID` BYTE
         
        
        boolean useOrigName = true;
        
        String  sql;
        //Integer count = null;

        worker.firePropertyChange(MSG, "", "Creating Image Attachments...");
        
        String prefix  = "SELECT co.CollectionObjectID, p.Text1 ";
        String postfix = "FROM preparation p INNER JOIN collectionobject co ON p.CollectionObjectID = co.CollectionObjectID " +
                         "WHERE p.Text1 LIKE 'http://nhm.ku.edu/fishes/collectionimages/%' " +
                         "ORDER BY co.CollectionObjectID ASC ";
        
        int totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix;

        Statement stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);

        dbS3Conn.setAutoCommit(false);
        int transCnt = 0;
        
        int               cnt    = 0;
        PreparedStatement s3Stmt = dbS3Conn.prepareStatement("INSERT INTO img (_id, OwnerID, TableID, ImgName) VALUES (?,?,?,?)");
        ResultSet         rs = stmt.executeQuery(adjustSQL(sql));
        while (rs.next())
        {
            transCnt++;

            Integer colObjId = rs.getInt(1);
            String  imgName  = rs.getString(2);
            
            if (useOrigName)
            {
                int inx = imgName.lastIndexOf('/');
                if (inx > -1)
                {
                    imgName = FilenameUtils.getName(imgName.substring(inx+1, imgName.length()));
                } else
                {
                    continue;
                }
            }
            
            s3Stmt.setInt(1,    cnt);
            s3Stmt.setInt(2,    colObjId);
            s3Stmt.setInt(3,    1);           // ColObj
            s3Stmt.setString(4, imgName);
            
            if (s3Stmt.executeUpdate() != 1)
            {
                //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
            }
            
            cnt++;
            if (cnt % 1000 == 0) 
            {
                worker.firePropertyChange(PROGRESS, 0, cnt);
                log.debug("Col Objs: "+cnt);
            }
        }
        if (transCnt > 0) dbS3Conn.commit();
        dbS3Conn.setAutoCommit(true);

        rs.close();
        s3Stmt.close();
        stmt.close();
        
        updateURLPathIntoStats("http://www.nhm.ku.edu/fishes/collectionimages/");
    }*/
    
    /**
     * @param urlStr
     */
    private void updateURLPathIntoStats(final String urlStr, final boolean isUsingDirectURLArg)
    {
        if (StringUtils.isNotEmpty(urlStr) && urlStr.startsWith("http:"))
        {
            try
            {
                int statsId = getCountAsInt(dbS3Conn, "SELECT _id FROM stats ORDER BY _id DESC");
                if (statsId > -1)
                {
                    PreparedStatement s3Stmt = dbS3Conn.prepareStatement("INSERT INTO stats (_id, Tbl, Grp, Name, Descr) VALUES (?,?,?,?,?)");
                    s3Stmt.setInt(1, statsId+1);
                    s3Stmt.setInt(2, 1);
                    s3Stmt.setInt(3, 8);
                    s3Stmt.setString(4, "img_url");
                    s3Stmt.setString(5, urlStr);
                    if (s3Stmt.executeUpdate() != 1)
                    {
                        //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
                    }
                    s3Stmt.setInt(1, statsId+2);
                    s3Stmt.setInt(2, 1);
                    s3Stmt.setInt(3, 8);
                    s3Stmt.setString(4, "img_url_type");
                    s3Stmt.setString(5, isUsingDirectURLArg ? InstitutionConfigDlg.DIRECT : InstitutionConfigDlg.ATTMGR);
                    if (s3Stmt.executeUpdate() != 1)
                    {
                        //System.out.println("Error updating ColObjID: "+colObjId+"  AttID: "+attID);
                    }
                    s3Stmt.close();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    

    /**
     * @param conn
     * @param s3Conn
     * @throws SQLException
     */
    private void doBuildAuxiliary() throws SQLException
    {
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        try
        {
        	s3Stmt = dbS3Conn.prepareStatement("INSERT INTO stats (Tbl, Grp, Name, Descr) VALUES (?,?,?,?)");
        	
            for (String key : this.auxInfoMap.keySet())
            {
            	String value = this.auxInfoMap.get(key);
                s3Stmt.setInt(1, 23); // Table Id
                s3Stmt.setInt(2, 4);  // Group Id
	            s3Stmt.setString(3, key);
	            s3Stmt.setString(4, value);
            
	            try
	            {
	                if (s3Stmt.executeUpdate() != 1)
	                {
	                    //System.out.println("Error updating Locality: "+id);
	                }
	            } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex)
	            {
	                System.err.println(String.format("Key: %s  Val: %s", key, value));
	                System.err.println(ex.getMessage());
	            }
            }

            dbS3Conn.commit();
        
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    

    /**
     * @param conn
     * @param s3Conn
     * @throws SQLException
     */
    private void doBuildLocalities() throws SQLException
    {
        int     cnt   = 0;
        String  sql;
        Integer count = null;

        worker.firePropertyChange(MSG, "", "Getting Locality Counts...");
        
        locNumObjsHash.clear();
        sql = "SELECT l.LocalityID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
              "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID GROUP BY l.LocalityID";
        for (Object[] row : query(sql))
        {
            count = getCount(row[1]);
            locNumObjsHash.put((Integer)row[0], count);
        }

        worker.firePropertyChange(MSG, "", "Building Localities...");
        
        String prefix  = "SELECT LocalityID, LocalityName, Latitude1, Longitude1, GeographyID ";
        String postfix = "FROM locality ORDER BY LocalityName";
        
        int totCnt = getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix;

        int transCnt = 0;
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        Statement         stmt       = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
        
            cnt = 0;
            s3Stmt = dbS3Conn.prepareStatement("INSERT INTO locality (_id, LocalityName, Latitude, Longitude, NumObjs, GeoID) VALUES (?,?,?,?,?,?)");
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                s3Stmt.setInt(1,    id);
                s3Stmt.setString(2, rs.getString(2));
                s3Stmt.setDouble(3, rs.getDouble(3));
                s3Stmt.setDouble(4, rs.getDouble(4));
                
                Integer numObjs = locNumObjsHash.get(id);
                if (numObjs == null) numObjs = 0;
                s3Stmt.setInt(5, numObjs);
                
                int geoId = rs.getInt(5);
                if (rs.wasNull())
                {
                    s3Stmt.setObject(6, null);
                } else
                {
                    s3Stmt.setInt(6, geoId);
                }
                
                try
                {
                    if (s3Stmt.executeUpdate() != 1)
                    {
                        //System.out.println("Error updating Locality: "+id);
                    }
                } catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException ex)
                {
                    System.err.println(String.format("LocID: %d  GeoId: %d", id, rs.getInt(5)));
                    System.err.println(ex.getMessage());
                }
                cnt++;
                if (cnt % 1000 == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    log.debug("Locality: "+cnt);
                }
    
            }
            try
            {
                if (transCnt > 0) dbS3Conn.commit();
            } catch (Exception ex2) { ex2.printStackTrace();}
            
            rs.close();
        
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (s3Stmt != null) s3Stmt.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    
    /**
     * @param conn
     * @param s3Conn
     * @throws SQLException
     */
    private void doPrebuildGeography() throws SQLException
    {
        int cnt = 0;
        
        worker.firePropertyChange(MSG, "", "Build Mapping From Locality to Geography...");
        
        Statement stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        stmt.setFetchSize(Integer.MIN_VALUE);
        cnt = 0;
        
        //String sql = adjustSQL("SELECT COUNT(*) FROM locality WHERE GeographyID IS NULL AND DisciplineID = DSPLNID");
        //int totLocs = Math.max(getCountAsInt(sql), 0);
        //String sql = adjustSQL("SELECT COUNT(*) FROM locality l INNER JOIN geography g ON l.GeographyID = g.GeographyID WHERE g.RankID < 200 AND l.DisciplineID = DSPLNID");
        //totLocs += Math.max(getCountAsInt(sql), 0);
        //System.out.println("totLocs: "+totLocs);
        
        String[] prefixes = { 
                "SELECT geo4.GeographyID AS GID4, geo4.RankID AS RID4, LID, RID, geo4.ParentID AS PID4 ",
                "SELECT geo3.GeographyID, geo3.RankID, LID, RID ",
                "SELECT geo2.GeographyID, geo2.RankID, LID, PID, RID ",
                "SELECT g.GeographyID AS GID, g.RankId, l.LocalityID AS LID ",
                };
        
        String[] sqls = { 
                "FROM (SELECT geo3.GeographyID AS GID3, geo3.RankID AS RID3, LID, RID, geo3.ParentID AS PID3 FROM (SELECT geo2.GeographyID AS GID2, LID, PID, geo2.ParentID AS PID2, RID FROM (SELECT g.GeographyID AS GID, l.LocalityID AS LID, g.RankID AS RID, ParentID AS PID FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = %d AND l.DisciplineID = DSPLNID) T1 INNER JOIN geography geo2 ON PID = geo2.GeographyID) T2 INNER JOIN geography geo3 ON PID2 = geo3.GeographyID) T3 INNER JOIN geography geo4 ON PID3 = geo4.GeographyID",
                "FROM (SELECT geo2.GeographyID AS GID2, LID, PID, geo2.ParentID AS PID2, RID FROM (SELECT g.GeographyID AS GID, l.LocalityID AS LID, g.RankID AS RID, ParentID AS PID FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = %d AND l.DisciplineID = DSPLNID) T1 INNER JOIN geography geo2 ON PID = geo2.GeographyID) T2 INNER JOIN geography geo3 ON PID2 = geo3.GeographyID",
                "FROM (SELECT g.GeographyID AS GID, l.LocalityID AS LID, g.RankID AS RID, ParentID AS PID FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = %d AND l.DisciplineID = DSPLNID) T1 INNER JOIN geography geo2 ON PID = geo2.GeographyID",
                "FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = %d AND l.DisciplineID = DSPLNID",
                };
        
        Integer[][] ranks = {
                {400},
                {400, 300},
                {400, 300, 200},
                {200, 100},
        };
        locToGeoContinentMapper.setShowLogErrors(true);
        locToGeoCountryMapper.setShowLogErrors(true);

        for (int i=0;i<prefixes.length;i++)
        {
            
            Integer[] ranksArray = ranks[i];
            for (Integer rnk : ranksArray)
            {
                String postSQL = String.format(sqls[i], rnk);
                int totCnt = getCountAsInt(dbConn, adjustSQL("SELECT COUNT(*) " + postSQL));
                if (totCnt < 1)
                {
                    continue;
                }
                //totLocs += totCnt;
                //System.out.println("totLocs: "+totLocs);
                
                if (progressDelegate != null)
                {
                    progressDelegate.setProcess(0, totCnt);
                    progressDelegate.setProcess(0);
                }
                int inc = Math.max(totCnt / 20, 1);
                
                String sqlStr = prefixes[i] + postSQL;
                
                System.out.println(adjustSQL(sqlStr));
                ResultSet rs = stmt.executeQuery(adjustSQL(sqlStr)); // Get the GeoID and LocID
                while (rs.next())
                {
                    int geoId   = rs.getInt(1);
                    int rankLvl = rs.getInt(2);
                    int locId   = rs.getInt(3);
                    
                    if (rankLvl == 100)
                    {
                        locToGeoContinentMapper.put(locId, geoId);
                        
                    } else if (rankLvl == 200)
                    {
                        locToGeoCountryMapper.put(locId, geoId);
                    }
                    
                    cnt++;
                    if (cnt % inc == 0) 
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                        //log.debug("LocID -> GeoID: "+cnt+ " -> "+getCountAsInt("SELECT COUNT(*) FROM geoloc"));
                    }
                }
                rs.close();
                worker.firePropertyChange(PROGRESS, 0, totCnt);
            }
        }
        //sql = adjustSQL("SELECT COUNT(*) FROM locality WHERE DisciplineID = DSPLNID");
        //int totAllLocs = Math.max(getCountAsInt(sql), 0);
        //System.out.println("totLocs: "+totLocs+"   totAllLocs: "+totAllLocs+ "   Diff: "+(totAllLocs - totLocs));
        
        String[] pre  = {"SELECT g1.GeographyID, g3.GeographyID ", "SELECT g1.GeographyID, g2.GeographyID "};
        String[] post = {"FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID INNER JOIN geography g3 ON g2.ParentID = g3.GeographyID WHERE g3.RankID = 3000 AND g1.GeographyTreeDefID = GEOTREEDEFID",
                         "FROM geography g1 INNER JOIN geography g2 ON g1.ParentID = g2.GeographyID WHERE g2.RankID = 300 AND g1.GeographyTreeDefID = GEOTREEDEFID"};
        
        for (int i=0;i<pre.length;i++)
        {
            int totCnt = getCountAsInt(dbConn, adjustSQL("SELECT COUNT(*) " + adjustSQL(post[i])));
            if (progressDelegate != null)
            {
                progressDelegate.setProcess(0, totCnt);
                progressDelegate.setProcess(0);
            }
            int inc = totCnt / 20;
            
            cnt = 0;
            String sql = pre[i] + adjustSQL(post[i]);
            ResultSet rs = stmt.executeQuery(sql); 
            while (rs.next())
            {
                geoStateMapper.put(rs.getInt(1), rs.getInt(2));
                
                cnt++;
                if (cnt % inc == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    //log.debug("LocID -> GeoID: "+cnt+ " -> "+getCountAsInt("SELECT COUNT(*) FROM geoloc"));
                }
            }
            rs.close();
        }
        
        stmt.close();
    }
    
    /**
     * @param conn
     */
    private void fixCollectorOrder(final Connection conn) throws SQLException
    {
        String sql = "SELECT ID FROM (SELECT ce.CollectingEventID ID, COUNT(c.OrderNumber) CNT, MAX(c.OrderNumber) MX, MIN(c.OrderNumber) MN " +
                     "FROM collectingevent ce " +
                     "INNER JOIN collector c ON ce.CollectingEventID = c.CollectingEventID " +
                     "INNER JOIN agent a ON c.AgentID = a.AgentID GROUP BY ce.CollectingEventID) T1 WHERE MN <> 1 OR MX <> CNT ";

        dbS3Conn.setAutoCommit(false);
        PreparedStatement pStmt     = null;
        PreparedStatement pStmt2    = null;
        Statement         stmt      = null;
        try
        {
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);

            pStmt  = conn.prepareStatement("SELECT CollectorID FROM collector WHERE CollectingEventID = ? ORDER BY OrderNumber");
            pStmt2 = conn.prepareStatement("UPDATE collector SET OrderNumber = ? WHERE CollectorID = ?");
            ResultSet         rs     = stmt.executeQuery(sql);
            int               cnt    = 0;
            while (rs.next())
            {
                int order = 1;
                pStmt.setInt(1, rs.getInt(1));
                ResultSet rs2 = pStmt.executeQuery();
                while (rs2.next())
                {
                    pStmt2.setInt(1, order++);
                    pStmt2.setInt(2, rs2.getInt(1));
                    if (pStmt2.executeUpdate() != 1)
                    {
                        log.error("Error updating CollectorID "+rs2.getInt(1));
                    }
                }
                rs2.close();
                cnt++;
                if (cnt % 1000 == 0) log.debug("Fixing Collector Ordering: " + cnt);
            }
            rs.close();
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            if (stmt != null) stmt.close();
            if (pStmt != null) pStmt.close();
            if (pStmt2 != null) pStmt2.close();
            try
            {
                dbS3Conn.setAutoCommit(true);
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    
    protected void buildZipFile()
    {
        progressDelegate.incOverall();
        
        if (!isInError)
        {
            progressDelegate.setDesc("Creating catalog...");
            progressDelegate.incOverall();
            
            createXMLIndexFile();
            
            progressDelegate.setDesc("Compressing data...");
            doBuildZipFile(ZIP_FILE, fileNamesForExport);
            
            progressDelegate.setDesc("Uploading data...");
            if (!IS_TESTING)
            {
                if (!uploadFiles())
                {
                    isInError = true;
                }
            }
            
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    shutdown();
                    if (!isInError)
                    {
                        if (doZipFile)
                        {
                            showDownloadCode();
                        }
                    } else
                    {
                        showFinalErrorDlg();
                    }
                    
                    if (changeListener != null)
                    {
                        changeListener.stateChanged(new ChangeEvent("done"));
                    }
                }
            });
        }
    }
    
    /**
     * @return
     */
    protected boolean createXMLIndexFile()
    {
        progressDelegate.setProcess(0, fileNamesForExport.size());
        
        try
        {
            int cnt = 0;
            PrintWriter pw = new PrintWriter(new File(cacheDir + File.separator + CAT_FILE));
            pw.println("<catalog>");
            for (ChartFileInfo fileInfo : fileNamesForExport)
            {
                String  fName  = fileInfo.getFileName();
                String  title  = fileInfo.getTitle(); 
                String  locale = fileInfo.getLocale();
                File    file   = new File(cacheDir + File.separator + fName);
                boolean isDB   = fName.endsWith("db");
                String  md5    = MD5Checksum.getMD5Checksum(file);
                
                pw.println(String.format("<file md5=\"%s\" type=\"%s\" locale=\"%s\">\n  <title><![CDATA[%s]]></title>\n  <fname>%s</fname>\n</file>", 
                        md5, (isDB ? "db" : "img"), locale, title, fName));
                worker.firePropertyChange(PROGRESS, 0, cnt++);
            }
            pw.println("<file md5=\"\" type=\"xml\" locale=\"\">\n  <title>Catalog</title>\n  <fname>catalog.xml</fname>\n</file>");
            
            for (String key : auxInfoMap.keySet())
            {
                pw.println(String.format("<info type=\"%s\" locale=\"\"><![CDATA[%s]]></info>", key, auxInfoMap.get(key)));
            }
            pw.println("</catalog>");
            pw.close();
            
            fileNamesForExport.add(new ChartFileInfo(CAT_FILE, "The catalog XML file.", ""));
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * 
     */
    protected void showDownloadCode()
    {
        loadAndPushResourceBundle(iPadDBExporterPlugin.RESOURCE_NAME);
        Collection col = AppContextMgr.getInstance().getClassObject(Collection.class);
        UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SUCCESS_UPL_TITLE", "SUCCESS_UPL", col.getCollectionName());
        popResourceBundle();
    }
    
    /**
     * 
     */
    /*protected void showDownloadCodeOld()
    {
        final String downloadCode = (helper.getSha1Hash() != null ? helper.getSha1Hash() : "N/A");
        
        String explain = "<HTML>Your compressed SpecifyInsight Data Store has been uploaded<br>to the web so it can be downloaded to your iPad.<br><br>The download key to be entered into the SpecifyInsight App is:";
        JTextField textField = UIHelper.createTextField(downloadCode);
        ViewFactory.changeTextFieldUIForDisplay(textField, false);
        CellConstraints cc = new CellConstraints();
        PanelBuilder pb0 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        pb0.add(textField, cc.xy(2,1));
        
        PanelBuilder pb = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p,8px,p"));
        pb.add(new JLabel(explain), cc.xyw(1,1,3));
        pb.add(pb0.getPanel(), cc.xy(2,3));
        
        pb.setDefaultDialogBorder();
        
        final String text = "Your download code for SpecifyInsight is:\n" + (downloadCode != null ? downloadCode : "N/A");
        
        final Hashtable<String, String> emailPrefs = new Hashtable<String, String>();
        boolean isEmailOK = EMailHelper.isEMailPrefsOK(emailPrefs);
        int     dlgBtns   = CustomDialog.OKCANCEL;
        if (isEmailOK) dlgBtns = CustomDialog.OKCANCELAPPLY;
        
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "Download Code", true, dlgBtns, pb.getPanel())
        {
            @Override
            protected void applyButtonPressed()
            {
                String password = Encryption.decrypt(emailPrefs.get("password"));
                if (StringUtils.isEmpty(password))
                {
                    password = EMailHelper.askForPassword((Frame)UIRegistry.getTopWindow());
                }
        
                if (StringUtils.isNotEmpty(password))
                {
                    final EMailHelper.ErrorType status = EMailHelper.sendMsg(emailPrefs.get("smtp"),
                                                                             emailPrefs.get("username"), 
                                                                             password, 
                                                                             emailPrefs.get("email"), // From
                                                                             emailPrefs.get("email"), // To
                                                                             "SpecifyInsight Download Code", 
                                                                             text, 
                                                                             EMailHelper.PLAIN_TEXT,
                                                                             emailPrefs.get("port"), 
                                                                             emailPrefs.get("security"), 
                                                                             null);
                    if (status != EMailHelper.ErrorType.Cancel)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            public void run()
                            {
                                UIRegistry.displayLocalizedStatusBarText(status == EMailHelper.ErrorType.Error ? "EMAIL_SENT_ERROR" : "EMAIL_SENT_OK");
                            }
                        });
                    }
                }
            }
            @Override
            protected void cancelButtonPressed()
            {
                UIHelper.setTextToClipboard(downloadCode);
            }
        };
        dlg.setOkLabel("Close");
        dlg.setCancelLabel("Copy To Clipboard");
        if (isEmailOK)
        {
            dlg.setApplyLabel("Send Email");
        }
        UIHelper.centerAndShow(dlg);
    }*/
    
    /**
     * @return
     */
    protected synchronized boolean uploadFiles()
    {
        try
        {
            File tmp = File.createTempFile("Specify-", "6");
            
            fileNamesForExport.clear();
            fileNamesForExport.add(new ChartFileInfo(ZIP_FILE, "The Zip File", ""));
            
            boolean isDataSetSaved = false;
            boolean stopTrying     = false;
            for (ChartFileInfo fileInfo : fileNamesForExport)
            {
                File fileToExport = new File(cacheDir + File.separator + fileInfo.getFileName());
                boolean tryAgain = true;
                while (tryAgain)
                {
                    if (!helper.sendFile(fileToExport, fileInfo.getFileName(), tmp.getName()))
                    {
                        log.error("Unable upload  ["+fileToExport.getAbsolutePath()+"]");
                        String msg = "ERROR_ON_UPLOAD";
                        tryAgain = JOptionPane.OK_OPTION == UIRegistry.askYesNoLocalized("YES", "CANCEL", msg, "UPLOAD_ERROR");
                        if (!tryAgain)
                        {
                            stopTrying = true;
                        }
                    } else
                    {
                        tryAgain = false;
                        String dirName = helper.getSha1Hash();
                        Institution inst = AppContextMgr.getInstance().getClassObject(Institution.class);
                        Division    div  = AppContextMgr.getInstance().getClassObject(Division.class);
                        Discipline  dsp  = AppContextMgr.getInstance().getClassObject(Discipline.class);
                        Collection  col  = AppContextMgr.getInstance().getClassObject(Collection.class);
                        
                        String collectionName = col.getCollectionName();
                        isDataSetSaved = ipadCloud.addNewDataSet(collectionName, dirName, inst.getGuid(), 
                                                                 div.getName(), dsp.getName(), collectionName, 
                                                                 false, auxInfoMap.get("icon"), auxInfoMap.get("curator"),
                                                                 col.getGuid());
                    }
                }
                if (stopTrying)
                {
                    break; // Stops the upload of files.
                }
            }
            
            if (progressDelegate != null)
            {
                if (!stopTrying && isDataSetSaved)
                {
                    progressDelegate.setDesc("The Collection information was uploaded successfully.");
                } else
                {
                    progressDelegate.setDesc("The Collection information upload was stopped.");
                }
            }
            if (tmp.exists()) tmp.delete();
            
            return !stopTrying;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return false;
    }
    
    /**
     * @param zipFileName
     * @param fileNames
     */
    private void doBuildZipFile(final String       zipFileName, 
                                final List<ChartFileInfo> fileInfoList)
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[10240];

        try
        {
            // Create the ZIP file
            String          fullPath = cacheDir + File.separator + zipFileName;
            ZipOutputStream out      = new ZipOutputStream(new FileOutputStream(fullPath));
            
            // Compress the files
            for (ChartFileInfo fileInfo : fileInfoList)
            {
                String filePath = cacheDir + File.separator + fileInfo.getFileName();
                log.debug("Adding ["+filePath+" to zip file.");
                FileInputStream in = new FileInputStream(filePath);

                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(fileInfo.getFileName()));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    
    public void initialize()
    {
        cacheDir = UIRegistry.getAppDataSubDir("ipad_export", true);

        // Cleanup old files if there are any
        if (doAll || doRebuildDB)
        {
            // Cleanup old files if there are any
            try
            {
                for (File f : cacheDir.listFiles())
                {
                    f.delete();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    
        try
        {
            tblWriter = new TableWriter(cacheDir + "/ExportReport.html", "Export Report", true);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

    }
    
    /**
     * @param databaseName
     * @throws Exception
     */
    public boolean createSQLiteDatabase(final String databaseName, final ChangeListener cl) throws Exception
    {
        changeListener = cl;

        fileNamesForExport.clear();
        fileNamesForExport.add(new ChartFileInfo(dbName, "The Data", ""));

        loadAndPushResourceBundle(iPadDBExporterPlugin.RESOURCE_NAME);
        
        Institution inst  = AppContextMgr.getInstance().getClassObject(Institution.class);
        Collection  col   = AppContextMgr.getInstance().getClassObject(Collection.class);
        String      colNm = col.getCollectionName();
        
        if (!IS_TESTING) // ZZZ       
        {
            if (ipadCloud.doesDataSetExist(colNm, inst.getGuid()))
            {
                String msg = getFormattedResStr("ASK_OVERWRITE_DS", colNm);
                if (askYesNoLocalized("CONTINUE", "CANCEL", msg, "OVERWRITE_DS") == JOptionPane.NO_OPTION)
                {
                    return false;
                }
            }
        }
        
        AppPreferences prefs = AppPreferences.getRemote();
        
        isUsingDirectURL  = true;
        InstitutionConfigDlg dlg = new InstitutionConfigDlg(null, null);
        imageURL    = prefs.get(dlg.getRemoteImageURLPrefName(), null);
        String type = prefs.get(dlg.getRemoteImageURLTypePrefName(), null);
        if (StringUtils.isNotEmpty(imageURL) && StringUtils.isNotEmpty(type))
        {
            isUsingDirectURL = type.equals(InstitutionConfigDlg.DIRECT);
        }
        
        // Copy institution picture
        institutionImageName = prefs.get(dlg.getRemotePicturePrefName(), null);
        if (StringUtils.isNotEmpty(institutionImageName))
        {
            File   srcPicFile  = new File(UIRegistry.getAppDataDir() + File.separator + institutionImageName);
            if (srcPicFile.exists())
            {
                FileUtils.copyFileToDirectory(srcPicFile, cacheDir);
                fileNamesForExport.add(new ChartFileInfo(institutionImageName, "Institution Picture", "en"));
            }
        }
        
        dbUpdateConn = DBConnection.getInstance().createConnection();
        
        File sqliteFile = new File(cacheDir + File.separator + dbName);
        if (sqliteFile.exists())
        {
            sqliteFile.delete();
        }
        Class.forName("org.sqlite.JDBC");
        dbS3Conn = DriverManager.getConnection("jdbc:sqlite:" + cacheDir + File.separator + dbName);
               
        iPadRepositoryHelper h = new iPadRepositoryHelper();
        auxInfoMap = h.getAuxilaryInfo(institutionImageName);
        if (auxInfoMap == null)
        {
            popResourceBundle();
            return false;
        }
        
        checkAndFixTaxon();

        verifyDlg = new VerifyCollectionDlg(this, cacheDir, this);
        UIHelper.centerAndShow(verifyDlg, 650, 600);
        
        return true;
    }
    
    protected void processDatabase()
    {
        createProgressUI();
        
        addProgress(UIRegistry.getResourceString("STARTING"));

        IdHashMapper.setEnableDelete(false);
        worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                boolean doStats       = false;
                boolean doTaxon       = false;
                
                boolean doGeography   = false;
                boolean doAgents      = false;
                boolean doLocalities  = false;
                boolean doImages      = false;
                
                boolean doPreBldGeo   = false;
                boolean doBldColObjs  = false;
                boolean doCollectors  = false;
                boolean doBuildPaleo  = false;
                boolean doLithostrat  = false;
                boolean doChronostrat = false;
                boolean doBuildAuxiliary = false;
                
                doZipFile = true;
                
                boolean[] bools = {doRebuildDB, doStats, doAgents, doTaxon, doBldColObjs, doGeography, 
                                   doLocalities, doBuildPaleo, doLithostrat, doChronostrat, doImages, 
                                   doPreBldGeo, doCollectors, doBuildAuxiliary, doZipFile, doUpload};
                numSteps = 0;
                for (boolean b : bools)
                {
                    if (b || doAll)
                    {
                        numSteps++;
                    }
                }
                
                numSteps++; // for export to SQLite
                
                progressDelegate.setOverall(0, numSteps);
        
                try
                {
                    dbConn = DBConnection.getInstance() != null ? DBConnection.getInstance().getConnection() : null;
                    setDBConnection(dbConn);

                    IdHashMapper.setEnableRemoveRecords(true);
                    IdMapperMgr.getInstance().setDBs(dbUpdateConn, dbUpdateConn);
                    
                    locToGeoContinentMapper = IdMapperMgr.getInstance().addHashMapper(GEOLOCCNTTBLNAME, null, true);
                    locToGeoContinentMapper.reset();

                    locToGeoCountryMapper = IdMapperMgr.getInstance().addHashMapper(GEOLOCCTYTBLNAME, null, true);
                    locToGeoCountryMapper.reset();
                   
                    geoToGeoContinentMapper = IdMapperMgr.getInstance().addHashMapper(GEOGEOCNTTBLNAME, null, true);
                    geoToGeoContinentMapper.reset();
                    
                    geoToGeoCountryMapper = IdMapperMgr.getInstance().addHashMapper(GEOGEOCTYTBLNAME, null, true);
                    geoToGeoCountryMapper.reset();
                    
                    taxonToPIDMapper = IdMapperMgr.getInstance().addHashMapper(TAXONTBLNAME, null, true);
                    taxonToPIDMapper.reset();
                    
                    IdHashMapper.setEnableDelete(false);
                    IdMapperMgr.getInstance().setDBs(dbConn, dbConn);

                    isInError = false;
                    
                    fixCollectorOrder(dbConn);
                    
                    if (doAll || doRebuildDB)
                    {
                        if (!SchemaUpdateService.createDBTablesFromSQLFile(dbS3Conn, "ipad_exporter" + File.separator + BLD_ISITE_FILE))
                        {
                            isInError = true;
                            return null;
                        }
                        progressDelegate.incOverall();
                    }
                    
                    // XXX Debug
                    //doBuildTaxonMappings();
                    //if (doAll) return null;
                    
                    if (doAll || doStats)
                    {
                        if (!createStatsTable())
                        {
                            isInError = true;
                            return null;
                        }
                        progressDelegate.incOverall();
                    }
            
                    if (doAll || doTaxon)
                    {
                        //checkAndFixTaxon();
                        //doBuildTaxonMappings();
                        TaxonTreeBuilding treeBuilding = new TaxonTreeBuilding(iPadDBExporter.this, dbS3Conn, dbConn);
                        treeBuilding.process();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doGeography)
                    {
                        //doBuildGeography();
                        TreeBuilder treeBuilder = new TreeBuilder(iPadDBExporter.this, dbS3Conn, dbConn);
                        treeBuilder.process();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doAgents)
                    {
                        buildColObjToAgent();

                        doBuildAgents();
                        progressDelegate.incOverall();
                    }
            
                    if (doAll || doLocalities)
                    {
                        doBuildLocalities();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doBuildPaleo)
                    {
                        doBuildPaleo();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doLithostrat)
                    {
                        doBuildLitho();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doChronostrat)
                    {
                        doBuildChronostrat();
                        progressDelegate.incOverall();
                    }
                    
                    if ((doAll || doImages) && StringUtils.isNotEmpty(imageURL))
                    {
                        doBuildImages();
                    }
                    progressDelegate.incOverall(); // it's OK it is outside.
                    
                    if (doAll || doPreBldGeo)
                    {
                        doPrebuildGeography();
                        progressDelegate.incOverall();
                    }
            
                    if (doAll || doBldColObjs)
                    {
                        doBuildColObjs();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doBuildAuxiliary)
                    {
                    	doBuildAuxiliary();
                        progressDelegate.incOverall();
                    }
                    
//                    if (doAll)
//                    {
//                        doFinalCleanupOfTaxon();
//                    }
                    
                    worker.firePropertyChange(MSG, "", "Build Index Catalog...");
                    
                    if (doAll || doZipFile)
                    {
                        progressDelegate.incOverall();
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                //progressDelegate.setDesc("Exporting Charts...");
                                iPadDBExporter.this.buildZipFile();
                            }
                        });
                    }

            
                    /*if (doAll || doCollectors)
                    {
                        worker.firePropertyChange(MSG, "", "Updating Collectors...");
                        doBuildCollectors();
                        progressDelegate.incOverall();
                    }*/
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    isInError = true;
                }
                return 0;
            }

            @Override
            protected void done()
            {
                super.done();
                
                popResourceBundle();
                shutdown();
                
                if (isInError)
                {
                    showFinalErrorDlg();
                    if (changeListener != null)
                    {
                        changeListener.stateChanged(new ChangeEvent("done"));
                    }
                }
            }
        };

        addProgressListener(worker);
        worker.execute();
    }
    
    @SuppressWarnings("unused")
    private void doFinalCleanupOfTaxon()
    {
        Statement stmt = null;
        try
        {
            dbS3Conn.setAutoCommit(false);
            
            //String sql = "DELETE FROM taxon WHERE _id IN (SELECT t._id FROM taxon t LEFT JOIN colobj co ON co.TaxonID = t._id WHERE t.RankID > 180 AND co.TaxonID IS NULL)";
            //BasicSQLUtils.update(dbS3Conn, sql);
            
            int tot = 0;
            String sql = "SELECT t._id FROM taxon t LEFT JOIN colobj co ON co.TaxonID = t._id WHERE t._id IN (SELECT _id FROM taxon WHERE RankID = 180 AND (HighNodeNum-NodeNum) == 1)  AND co.TaxonID IS NULL";
            stmt = dbS3Conn.createStatement();
            ResultSet rs  = stmt.executeQuery(sql);
            while (rs.next())
            {
                int recId = rs.getInt(1);
                int cnt = BasicSQLUtils.getCountAsInt(dbS3Conn, "SELECT COUNT(*) FROM taxon WHERE ParentID = "+recId);
                if (cnt > 0)
                {
                    System.out.println("Cnt: "+cnt+"  ID: "+recId);
                    //BasicSQLUtils.update(dbS3Conn, "DELETE FROM taxon WHERE _id = " + recId);
                }
                tot++;
            }
            System.out.println("Tot: "+tot);
            rs.close();
            dbS3Conn.commit();
            
        } catch (Exception e) 
        {
            try
            {
                dbS3Conn.rollback();
            } catch (Exception ex2) {}
            e.printStackTrace();
            
        } finally
        {
            try
            {
                dbS3Conn.setAutoCommit(true);
                if (stmt != null) stmt.close();
            } catch (Exception ex2) { ex2.printStackTrace();}
        }

    }
    
    /**
     * 
     */
    private void showFinalErrorDlg()
    {
        if (helper.isNetworkConnError())
        {
            UIRegistry.showError("There was a problem connecting to the internet.\nThis is required to create the SpecifyInsight data store."); // I18N
        } else
        {
            UIRegistry.showError("There was an unrecoverable error while creating the iPad data store."); // I18N
        }
    }

    // VerifyCollectionListener
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.VerifyCollectionListener#cancelPressed()
     */
    @Override
    public void cancelPressed()
    {
        if (changeListener != null)
        {
            changeListener.stateChanged(new ChangeEvent("done"));
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.ipadexporter.VerifyCollectionListener#okPressed()
     */
    @Override
    public void okPressed()
    {
        processDatabase();
    }

    /**
     * @param sb
     * @param first
     * @param mid
     * @param last
     * @param len
     * @return
     */
    protected String formatName(final StringBuilder sb, 
                                final String first, 
                                final String mid, 
                                final String last, 
                                final int len)
    {
        sb.setLength(0);
        
        boolean hasLast  = StringUtils.isNotEmpty(last);
        boolean hasMid   = StringUtils.isNotEmpty(mid);
        boolean hasFirst = StringUtils.isNotEmpty(first);
        
        sb.setLength(0);
        if (hasLast)
        {
            sb.append(last);
        }
        
        if (hasFirst)
        {
            if (hasLast) sb.append(", ");
            sb.append(first);
        }
            
        if (hasMid)
        {
            if (hasLast || hasFirst) sb.append(", ");
            sb.append(mid);
        }
        
        int l = sb.length();
        if (l > 0)
        {
            return l > 64 ? sb.substring(0, 64) : sb.toString();
        } 
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#shutdown()
     */
    public void shutdown()
    {
        if (progressDelegate != null)
        {
            progressDelegate.setVisible(false);
            progressDelegate.dispose();
            progressDelegate = null;
        }
        
        tblWriter.close();
        
        try
        {
            if (dbUpdateConn != null)
            {
                dbUpdateConn.close();
            }
            
            AppContextMgr ac = AppContextMgr.getInstance();
            if (ac == null)
            {
                dbConn.close();
            }
            dbS3Conn.close();
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
    
    private void checkAndFixTaxon()
    {
    	if (dbConn == null)
    	{
    		dbConn = DBConnection.getInstance().getConnection();
    	}
    	Connection conn2 = DBConnection.getInstance().createConnection();
    	
        String  post = " FROM (SELECT FullName NM, COUNT(FullName) CNT FROM taxon WHERE TaxonTreeDefID = TAXTREEDEFID AND RankID = 140 GROUP BY FullName) T1 WHERE CNT > 1";
        String  sql  = adjustSQL("SELECT COUNT(*)" + post);
        int cnt = getNumRecords(sql);
        if (cnt == 0)
        {
        	return;
        }
        
    	Connection        conn   = DBConnection.getInstance().getConnection();
    	Statement         stmt   = null;
        PreparedStatement pStmt  = null;
        PreparedStatement pStmt2 = null;
        try
        {
        	stmt   = conn.createStatement();
        	pStmt  = dbConn.prepareStatement(adjustSQL("UPDATE taxon SET FullName=? WHERE TaxonID = ? AND TaxonTreeDefID = TAXTREEDEFID"));
        	pStmt2 = conn2.prepareStatement(adjustSQL("SELECT TaxonID FROM taxon WHERE FullName = ? AND TaxonTreeDefID = TAXTREEDEFID"));
        	
        	sql = adjustSQL("SELECT NM, CNT" + post);
            ResultSet rs  = stmt.executeQuery(sql);
        	while (rs.next())
        	{
        	    cnt = 0; 
        		String name = rs.getString(1);
        		pStmt2.setString(1, name);
        		ResultSet rs2 = pStmt2.executeQuery();
        		while (rs2.next())
        		{
        		    String nm = cnt > 1 ? String.format("%s_%d", name, cnt) : name;
        			pStmt.setString(1, nm);
        			pStmt.setInt(2, rs2.getInt(1));
        			
        			if (pStmt.executeUpdate() != 1)
        			{
        				
        			}
        			cnt++;
        		}
        		rs2.close();
        	}
    		rs.close();
    		
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally 
        {
            try
            {
            	conn2.close();
                if (stmt != null)  stmt.close();
                if (pStmt != null)  pStmt.close();
                if (pStmt2 != null)  pStmt2.close();
            } catch (Exception ex2) { ex2.printStackTrace();}
        }
    }
    
    class ChartFileInfo
    {
        String fileName;
        String title;
        String locale;
        
        /**
         * @param fileName
         * @param title
         * @param locale
         */
        public ChartFileInfo(final String fileName, final String title, final String locale)
        {
            super();
            this.fileName = fileName;
            this.title = title;
            this.locale = locale;
        }

        /**
         * @return the fileName
         */
        public String getFileName()
        {
            return fileName;
        }

        /**
         * @return the title
         */
        public String getTitle()
        {
            return title;
        }

        /**
         * @return the locale
         */
        public String getLocale()
        {
            return locale;
        }        
    }
}
