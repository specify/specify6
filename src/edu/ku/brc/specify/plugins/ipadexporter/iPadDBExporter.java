package edu.ku.brc.specify.plugins.ipadexporter;

import static edu.ku.brc.ui.UIRegistry.askYesNoLocalized;
import static edu.ku.brc.ui.UIRegistry.getFormattedResStr;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.loadAndPushResourceBundle;
import static edu.ku.brc.ui.UIRegistry.popResourceBundle;
import static edu.ku.brc.ui.UIRegistry.setResourceLocale;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
import org.jfree.chart.JFreeChart;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.DatabaseLoginPanel;
import edu.ku.brc.dbsupport.DBConnection;
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
 * @code_status Alpha
 *
 * Jul 12, 2011
 *
 */
public class iPadDBExporter
{
    private static final Logger  log                  = Logger.getLogger(iPadDBExporter.class);
    
    private static final Locale[] locales             = {Locale.ENGLISH, Locale.GERMAN, };

    private static final String  PROGRESS             = "progress";
    private static final String  MSG                  = "msg";

    private static final boolean doAll                = true;
    private static final boolean doRebuildDB          = false;
    
    //private static final String  BLD_SPTBL_FILE  = "build_sptables.sql";
    private static final String  BLD_ISITE_FILE  = "build_isite.sql";
    private static final String  BLD_XREF_FILE   = "build_xreftables.sql";
    private static final String  STATS_XML       = "stats.xml";
    private static final String  CAT_FILE        = "catalog.xml";
    private static final String  ZIP_FILE        = "isite.zip";
    
    private static final String  GEOLOCTBLNAME   = "ios_geoloc";
    private static final String  COLOBJAGENTNAME = "ios_colobjagents";
    private static final String  COLOBJCNTNAME   = "ios_colobjcnts";
    private static final String  COLOBJGEONAME   = "ios_colobjgeo";
    
    
    private static final int     BAR_CHART       = 0; 
    private static final int     LINE_CHART      = 1; 
    private static final int     PIE_CHART       = 2;
    private static final int     PIE_CHART3D     = 3;
    
    // Database Members
    private DBConnection                             dbRootConn        = null;
    private Connection                               dbConn            = null;
    private Connection                               dbS3Conn          = null;
    private Pair<String, String>                     itUsrPwd          = null;
    private String                                   imageURL          = null;
    private boolean                                  isUsingDirectURL  = false;
    
    private boolean                                  isInError         = false;
    
    private ChangeListener                           changeListener    = null;
    private TableWriter                              tblWriter;
    private int                                      numSteps = 0;
    private File                                     cacheDir;
    
    private iPadRepositoryHelper                     helper             = new iPadRepositoryHelper();
    private String                                   dbName;
    private HashMap<String, Integer>                 idMapper           = new HashMap<String, Integer>();
    private HashMap<Integer, Pair<Integer, Integer>> taxMapper          = new HashMap<Integer, Pair<Integer, Integer>>();
    private HashMap<Pair<Integer, Integer>, Integer> revMapper          = new HashMap<Pair<Integer, Integer>, Integer>();
    private int[]                                    array              = null;
    private int                                      largestVal         = 0;
    private Vector<Pair<Integer, Integer>>           familyIdList       = null;


    private ProgressDialog                           progressDelegate;
    private HashMap<Integer, Integer>                locNumObjsHash     = new HashMap<Integer, Integer>();
    //private HashMap<Integer, Integer>                geoNumObjsHash     = new HashMap<Integer, Integer>();
    private HashMap<Integer, Integer>                geoStateMapper     = new HashMap<Integer, Integer>();
    
    private SwingWorker<Integer, Integer>            worker;
    private IdMapperIFace                            locToGeoMapper     = null;
    private IdMapperIFace                            colObjToAgent      = null;
    private IdMapperIFace                            colObjToCnt        = null;
    private IdMapperIFace                            colObjToGeo        = null;
    
    private boolean                                  doZipFile          = true; 
    private boolean                                  doUpload           = true; 
    private ArrayList<ChartFileInfo>                 fileNamesForExport = new ArrayList<ChartFileInfo>();
    
    private int countryIdCnt   = 0;
    private int coltrIdCnt = 0;
    private int cntAmtCnt  = 0;

    private IPadCloudIFace ipadCloud;
    private Map<String, String>                      auxInfoMap  = null;
    
    // For charting
    private ChartHelper                              chartHelper  = new ChartHelper();
    private Timer                                    timer        = null;
    private int                                      chartIndex   = 0;
    private int                                      width        = 0;
    private int                                      height       = 0;
    
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
        
        this.width  = width;
        this.height = height;
        
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
    
    /**
     * @param fileName
     * @return
     * @throws IOException
     */
    private File getResourceFile(final String fileName)
    {
        File outFile = null;
        try
        {
            byte[]      bytes = new byte[1024];
            InputStream is    = this.getClass().getResourceAsStream(fileName);
            
            outFile = File.createTempFile("sp-", "tmp");
            FileOutputStream oFile   = new FileOutputStream(outFile);
            while (is.available() > 0)
            {
                int len = is.read(bytes);
                oFile.write(bytes, 0, len);
            }
            oFile.close();
            
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return outFile;
    }
    
    /**
     * @param conn
     * @param fileName
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    protected boolean createDBTables(final Connection conn, final String fileName) throws Exception
    {
        File outFile = getResourceFile(fileName);
        if (outFile != null && outFile.exists())
        {
            StringBuilder sb      = new StringBuilder();
            Statement     stmt    = conn.createStatement();
            List<?>       list    = FileUtils.readLines(outFile);
            
            for (String line : (List<String>)list)
            {
                String tLine = line.trim();
                
                /*if (StringUtils.contains(tLine, PRIMARYKEY))
                {
                    tLine = StringUtils.remove(tLine, PRIMARYKEY);
                    
                } else if (tLine.startsWith(CONSTRAINT))
                {
                    //System.out.println(sb.toString());
                    if (sb.charAt(sb.length()-1) == ',')
                    {
                        sb.setLength(sb.length()-1); // chomp ','
                    }
                    continue;
                }*/
                
                sb.append(tLine);
                
                if (tLine.endsWith(";"))
                {
                    System.out.println(sb.toString());
                    stmt.executeUpdate(sb.toString());
                    sb.setLength(0);
                }
            }
            stmt.close();
            return true;
        }
        return false;
    }
    
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
    private String adjustSQL(final String sql)
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
        //loadAndPushResourceBundle(iPadDBExporterPlugin.RES_NAME);
        loadAndPushResourceBundle("stats");

        UIRegistry.setDoShowAllResStrErors(false);
        logMsg("Creating Stats...");
        
        File tmpFile = getResourceFile(STATS_XML);
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
    
    
    /**
     * @throws SQLException
     */
    private void doBuildTaxon() throws SQLException
    {
        int cnt;
        String postfix;
        int totCnt;
        
        worker.firePropertyChange(MSG, "", "Updating Total Taxonomy Counts...");
         
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt = null;
        Statement         stmt   = null;
        try
        {
            worker.firePropertyChange(MSG, "", "Building Taxonomy...");
            
            cnt = 0;
            
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            s3Stmt = dbS3Conn.prepareStatement("INSERT INTO taxon (_id, FullName, RankID, ParentID, FamilyID, TotalCOCnt, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?)");
            
            String prefix = "SELECT TaxonID, FullName, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
            for (int i=0;i<2;i++)
            {
                postfix = String.format("FROM taxon WHERE TaxonTreeDefID = TAXTREEDEFID AND %s AND HighestChildNodeNumber IS NOT NULL AND NodeNumber IS NOT NULL ORDER BY FullName", i == 0 ? "RankID = 140" : "RankID > 140");
                
                totCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + adjustSQL(postfix));
                if (progressDelegate != null)
                {
                    progressDelegate.setProcess(0, totCnt);
                }
                
                String sql = prefix + postfix;
                
                System.out.println(sql);
                
                if (i == 1)
                {
                    familyIdList = new Vector<Pair<Integer, Integer>>(taxMapper.values());
                    Collections.sort(familyIdList, new Comparator<Pair<Integer, Integer>>()
                            {
                        @Override
                        public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2)
                        {
                            return o1.first.compareTo(o2.first);
                        } 
                    });
                    
                    array = new int[taxMapper.size()];
                    int inx = 0;
                    for (Pair<Integer, Integer> p : familyIdList)
                    {
                        array[inx] = p.first;
                        largestVal = p.second;
                        System.out.println(String.format("array => %d -> %d %d", inx, p.first, p.second));
                        inx++;
                    }
                }
                
                dbS3Conn.setAutoCommit(false);
                int transCnt = 0;
                
                ResultSet rs = stmt.executeQuery(adjustSQL(sql));
                while (rs.next())
                {
                    transCnt++;
    
                    Integer id = rs.getInt(1);
                    
                    s3Stmt.setInt(1,    id); // TaxonID
                    s3Stmt.setString(2, rs.getString(2));
                    s3Stmt.setInt(3,    rs.getInt(3));
                    s3Stmt.setInt(4,    rs.getInt(4));
                    
                    int highestNodeNum = rs.getInt(5);
                    int nodeNumber     = rs.getInt(6);
                    
                    Integer familyId = null;
                    
                    if (i == 0) // zero mean processing Families
                    {
                        Pair<Integer, Integer> p = new Pair<Integer, Integer>(nodeNumber, highestNodeNum);
                        taxMapper.put(id, p);
                        revMapper.put(p, id);
                        s3Stmt.setObject(5, null);
                        
                    } else
                    {
                        if (id <= largestVal)
                        {
                            int inx = Arrays.binarySearch(array, nodeNumber);
                            if (inx < 0)
                            {
                                inx = Math.abs(inx) - 2;
                            }
                            if (inx > -1)
                            {
                                Pair<Integer, Integer> p = familyIdList.get(inx);
                                familyId = revMapper.get(p);
                            }
                            //System.out.println(familyId + " => " + id);
                        }
                        if (familyId != null)
                        {
                            s3Stmt.setInt(5, familyId);
                        } else
                        {
                            s3Stmt.setObject(5, null);
                        }
                    }
                    
                    s3Stmt.setInt(6, 0);
                    
                    s3Stmt.setInt(7, highestNodeNum);
                    s3Stmt.setInt(8, nodeNumber);
                    
                    if (s3Stmt.executeUpdate() != 1)
                    {
                        System.out.println("Error updating taxon: "+id);
                    }
                    cnt++;
                    if (cnt % 1000 == 0) 
                    {
                        worker.firePropertyChange(PROGRESS, 0, cnt);
                        //log.debug("Taxon: "+cnt);
                    }
                }
                rs.close();
                try
                {
                    if (transCnt > 0) dbS3Conn.commit();
                } catch (Exception ex2) { ex2.printStackTrace();}
            }
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
        
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }
        
        worker.firePropertyChange(MSG, "", "Building Agents...");
        
        String prefix  = "SELECT AgentID, FirstName, Initials, LastName ";
        String postfix = "FROM agent ORDER BY LastName";
        
        boolean isSingleCollection = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) FROM collection") == 1;
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
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
    
    /**
     * @throws SQLException
     */
    private void doBuildGeography() throws SQLException
    {
        doBuildColObjToGeoMapping();
        
        int cnt = 0;
        String sql;
        Integer count = null;

        // Get Specimen Counts for Geography
        worker.firePropertyChange(MSG, "", "Getting Geography Counts...");
        HashMap<Integer, Integer> numObjsHash = new HashMap<Integer, Integer>();
        sql = adjustSQL("SELECT g.GeographyID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM geography g INNER JOIN locality l ON g.GeographyID = l.GeographyID " +
                "INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
                "WHERE g.GeographyTreeDefID = GEOTREEDEFID GROUP BY g.GeographyID ");
        log.debug(sql);
        
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            count = getCount(row[1]);
            numObjsHash.put((Integer)row[0], count);
        }

        worker.firePropertyChange(MSG, "", "Pruning Geography Tree...");
        HashSet<Integer> unsedGeosSet = new HashSet<Integer>();
        sql = adjustSQL("SELECT g.GeographyID FROM geography g " +
              "LEFT JOIN locality l ON g.GeographyID = l.GeographyID " +
              "LEFT JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
              "LEFT JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID " +
              "WHERE g.GeographyTreeDefID = GEOTREEDEFID AND co.CollectionObjectID IS NULL");
        log.debug(sql);
        unsedGeosSet.addAll(BasicSQLUtils.queryForInts(sql));
        
        HashSet<Integer> geosToDelete = new HashSet<Integer>();
        sql = adjustSQL("SELECT RankID FROM geographytreedefitem WHERE GeographyTreeDefID = GEOTREEDEFID ORDER BY RankID DESC");
        log.debug(sql);
        for (Integer rankId : BasicSQLUtils.queryForInts(sql))
        {
            sql = String.format("SELECT gp.GeographyID FROM geography gc INNER JOIN geography gp ON gc.ParentID = gp.GeographyID WHERE gp.RankID = %d AND gp.GeographyTreeDefID = GEOTREEDEFID AND gc.GeographyID", rankId);
            sql = adjustSQL(sql);
            log.debug(sql);
            for (Integer geoId : BasicSQLUtils.queryForInts(sql))
            {
                if (unsedGeosSet.contains(geoId))
                {
                    unsedGeosSet.remove(geoId);
                    geosToDelete.add(geoId);
                }
            }
        }
        
        System.out.println("Geos To delete "+geosToDelete.size());
        for (Integer geoId : geosToDelete)
        {
            System.out.println(geoId);
        }
        
        worker.firePropertyChange(MSG, "", "Building Geography...");
        
        String prefix  = "SELECT GeographyID, FullName, GeographyCode, RankID, ParentID, HighestChildNodeNumber, NodeNumber ";
        String postfix = adjustSQL("FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID ORDER BY FullName");
        
        sql = "SELECT COUNT(*) " + postfix;
        log.debug(sql);
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, sql);
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        sql = prefix + postfix;
        log.debug(sql);
        
        boolean isSingleCollection = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) FROM collection") == 1;

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
            String upSQL = "INSERT INTO geo (_id, FullName, ISOCode, RankID, ParentID, TotalCOCnt, NumObjs, HighNodeNum, NodeNum) VALUES (?,?,?,?,?,?,?,?,?)";
            s3Stmt = dbS3Conn.prepareStatement(upSQL);
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                transCnt++;
    
                Integer id = rs.getInt(1);
                
                // don't check to see if Agent has items in the collection 
                // unless there is more than one collection
                if (!isSingleCollection)  
                {
                    Integer colObjId = colObjToGeo.reverseGet(id);
                    if (colObjId == null) continue;
                }
                
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
                
                if (s3Stmt.executeUpdate() != 1)
                {
                    //System.out.println("Error updating geo: "+id);
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
        
        worker.firePropertyChange(MSG, "", "Updating Total Geography Counts...");
        
        treeCnt = 0;

        sql = adjustSQL("SELECT GeographyID FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID AND RankID = 0");
        int rootId = BasicSQLUtils.getCountAsInt(dbConn, sql);
        
        sql = adjustSQL("SELECT COUNT(*) FROM geography WHERE GeographyTreeDefID = GEOTREEDEFID");
        treeTotal = BasicSQLUtils.getCountAsInt(dbConn, sql);
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
            fillTotalCountForTree("Geo", rootId, pStmt);
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
    private int fillTotalCountForTree(final String tablePrefix, 
                                     final int parentId, 
                                     final PreparedStatement pStmt) throws SQLException
    {
        if (treeCnt % treePercent == 0)
        {
            worker.firePropertyChange(PROGRESS, 0, (int)((double)treeCnt * 100.0 /  (double)treeTotal));
        }
        treeCnt++;
        
        String  sql   = String.format("SELECT NumObjs FROM %s WHERE _id=%d", tablePrefix.toLowerCase(), parentId);
        Integer total = BasicSQLUtils.getCount(dbS3Conn, sql);
        if (total == null) total = 0;
        
        int kidTotal = 0;
        sql = String.format("SELECT _id FROM %s WHERE ParentID = %d", tablePrefix.toLowerCase(), parentId);
        for (Integer id : BasicSQLUtils.queryForInts(dbS3Conn, sql))
        {
            kidTotal += fillTotalCountForTree(tablePrefix, id, pStmt);
        }
        
        //if (kidTotal > 0) System.out.println(String.format("%d -> %d", parentId, kidTotal));
        if (kidTotal > 0)
        {
            pStmt.setInt(1, kidTotal);
            pStmt.setInt(2, parentId);
            if (pStmt.executeUpdate() != 1)
            {
                log.error("Error updating ID "+parentId);
            }
        }
        return total + kidTotal;
    }
    
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
            
            Integer countryId    = locToGeoMapper.get(locId); // Get GeoID for the Country that the Locality belongs to.
            Integer coltrId      = colObjToAgent.get(id);
            Integer taxaId       = rs.getInt(9);
            Integer cntAmt       = colObjToCnt.get(id);
            
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
                String localityName = BasicSQLUtils.querySingleObj("SELECT LocalityName FROM locality WHERE LocalityID = "+locId);
                
                //"No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName
                tblWriter.logErrors(catNum, collectorNum == null ? "&nbsp;" : collectorNum,
                                    localityName == null ? "&nbsp;" : localityName, locId != null ? Integer.toString(locId) : "null");
                //log.debug("No Geo For Cat Num: " + catNum + "  ColObjID: " + id+" for LocID: " + locId+" LocName: "+localityName);
            }
            
            setInt(s3StmtPrep, coltrId, 12);// CollectorID
            
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
            
            setInt(s3StmtPrep, familyId, 13); // FamilyID
            setInt(s3StmtPrep, geoId, 14);    // GeographyID
            
            if (geoId != null && rankId != null && rankId == 300)
            {
                s3StmtPrep.setInt(15, geoId);
            } else
            {
                setInt(s3StmtPrep, geoId != null ? geoStateMapper.get(geoId) : null, 15);    // StateID
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
        int    totCnt = BasicSQLUtils.getCountAsInt(sql);
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
                //log.debug("LocID -> GeoID: "+cnt+ " -> "+BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geoloc"));
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
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
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
    private void doBuildColObjToGeoMapping() throws SQLException
    {
        worker.firePropertyChange(MSG, "", "Locating Specimen Geographies...");
        
        String postSQL = " FROM geography g INNER JOIN locality l ON g.GeographyID = l.GeographyID " +
                         "INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
                         "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID WHERE CollectionID = COLMEMID";
        String sql = adjustSQL("SELECT COUNT(*)" + postSQL);
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, sql);
        if (progressDelegate != null) progressDelegate.setProcess(0, 100);

        //----------------------------------------------------------
        // Creating Mapping from ColObj top Geo
        //----------------------------------------------------------
        int fivePercent = Math.max(totCnt / 20, 1);
        
        int cnt = 0;
        colObjToGeo = IdMapperMgr.getInstance().addHashMapper(COLOBJGEONAME, null, false);
        colObjToGeo.reset();

        sql = adjustSQL("SELECT co.CollectionObjectID, g.GeographyID " + postSQL);
        Statement stmt2 = dbConn.createStatement();
        ResultSet rs    = stmt2.executeQuery(adjustSQL(sql));
        while (rs.next())
        {
            colObjToGeo.put(rs.getInt(1), rs.getInt(2));
            cnt++;
            if (cnt % fivePercent == 0) 
            {
                worker.firePropertyChange(PROGRESS, 0, Math.max((100 * cnt) / totCnt, 1));
            }
        }
        rs.close();
        stmt2.close();
    }
    
    /**
     * Count number of Specimens for each Collection Object.
     * @throws SQLException
     */
    private void doBuildColObjCounts() throws SQLException
    {
        worker.firePropertyChange(MSG, "", "Calculating Specimen Counts...");
        
        String sql = "SELECT COUNT(*) FROM collectionobject WHERE CollectionID = COLMEMID";
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, adjustSQL(sql));
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
        for (Object[] row : BasicSQLUtils.query(sql))
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
        for (Object[] row : BasicSQLUtils.query(dbConn, sql1))
        {
            keepers.add((Integer)row[0]);
            keepers.add((Integer)row[1]);
        }*/
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, sql);
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
        
        locToGeoMapper.setShowLogErrors(false);
        colObjToAgent.setShowLogErrors(false);
        
        tblWriter.startTable();
        tblWriter.log("Collection Object's with missing geography");
        tblWriter.logHdr("Cat Num", "Collector Num", "Locality", "Col Event ID");
        
        int transCnt = 0;
        int prevId   = -1;
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        Statement         stmt       = null;
        PreparedStatement s3StmtPrep = null;
        try
        {
            s3Stmt     = dbS3Conn.prepareStatement("SELECT FamilyID FROM taxon WHERE _id = ?");
            //                                                           1           2         3            4                5            6          7          8          9      10      11          12         13        14      15
            s3StmtPrep = dbS3Conn.prepareStatement("INSERT INTO colobj (_id, CatalogNumber, CountAmt, CollectorNumber, CollectedDate, IsMappable, HasImage, TypeStatus, TaxonID, LocID, CountryID, CollectorID, FamilyID, GeoID, StateID) " +
                                                   "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        
            stmt  = dbConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            cnt = 0;
            System.out.println(adjustSQL(sql));
        
            ResultSet rs = stmt.executeQuery(adjustSQL(sql));
            while (rs.next())
            {
                //int id = rs.getInt(1);
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
            
            System.out.println(String.format("geoIdCnt: %d  coltrIdCnt: %d  cntAmtCnt: %d", countryIdCnt, coltrIdCnt, cntAmtCnt));
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
         
        totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
        }
        
        sql = "SELECT * " + postfix;

        locToGeoMapper.setShowLogErrors(false);
        
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
            String cn = BasicSQLUtils.querySingleObj("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+colObjID);
            log.debug("Cat Num missing Current Determination: "+cn);
        }

        System.out.println(String.format("geoIdCnt: %d  coltrIdCnt: %d  cntAmtCnt: %d", countryIdCnt, coltrIdCnt, cntAmtCnt));
        
        updateFamilyCounts();
        
        if (DBConnection.getInstance().getDatabaseName().equals("kui_fish_dbo_6"))
        {
            fixColObjAttachments();
        }
   }
    
   
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
    private void updateFamilyCounts() throws SQLException
    {
        int cnt = 0;
        
        worker.firePropertyChange(MSG, "", "Calculating Taxonomy Counts...");

        if (progressDelegate != null) progressDelegate.getProcessProgress().setIndeterminate(true);
        
        dbS3Conn.setAutoCommit(false);
        PreparedStatement s3Stmt     = null;
        Statement         stmt       = null;
        try
        {
            int missingFamilyCnt = 0;
            
            s3Stmt = dbS3Conn.prepareStatement("UPDATE taxon SET NumObjs=? WHERE _id = ?");

            HashMap<Integer, Integer> famCounts = new HashMap<Integer, Integer>();
            
            // First, we get the counts for all ColObj's and the Taxon they point at directly.
            stmt   = dbS3Conn.createStatement();
            String    sql = "SELECT t._id, t.RankID, NodeNum, CNT FROM (SELECT TaxonID, SUM(CountAmt) CNT FROM colobj GROUP BY TaxonID) T1 INNER JOIN taxon t ON TaxonID = t._id";
            ResultSet rs  = stmt.executeQuery(sql);
            while (rs.next()) // loop through all the Col Obj Records with just a FALSE determination
            {
                int id      = rs.getInt(1);
                int rankId  = rs.getInt(2);
                int nodeNum = rs.getInt(3);
                int amt     = rs.getInt(4);
                
                if (rankId < 140) continue;
                
                Integer familyId = null;
                if (rankId == 140)
                {
                    familyId = id;
                } else
                {
                    int inx = Arrays.binarySearch(array, nodeNum);
                    if (inx < 0)
                    {
                        inx = Math.abs(inx) - 2;
                    }
                    if (inx > -1)
                    {
                        Pair<Integer, Integer> p = familyIdList.get(inx);
                        familyId = revMapper.get(p);
                    }
                    
                    if (familyId == null)
                    {
                        missingFamilyCnt++;
                    }
                    
                    s3Stmt.setInt(1, amt);
                    s3Stmt.setInt(2, id);
                    int rv = s3Stmt.executeUpdate();
                    if (rv != 1)
                    {
                        System.err.println("Error updating FamilyID: "+id+"  CntAmt: "+amt + "  rv: "+rv);
                    }
                }

                if (familyId != null)
                {
                    Integer sum = famCounts.get(familyId);
                    if (sum == null)
                    {
                        sum = 1;
                    } else
                    {
                        sum += amt;
                    }
                    famCounts.put(familyId, sum);
                }
            }
            
            System.out.println("Missing Families: "+missingFamilyCnt);
            
            int totCnt = famCounts.size();
            int tenPercent = totCnt / 10;
            
            if (progressDelegate != null)
            {
                progressDelegate.getProcessProgress().setIndeterminate(false);
                //progressDelegate.setProcess(1, 100);
                progressDelegate.setProcess(1);
            }
            worker.firePropertyChange(MSG, "", "Updating Taxonomy Counts...");

            for (Integer txId : famCounts.keySet())
            {
                int amt = famCounts.get(txId);
                s3Stmt.setInt(1, amt);
                s3Stmt.setInt(2, txId);
                
                //System.out.println(String.format("%d - %d", txId, amt));
                
                int rv = s3Stmt.executeUpdate();
                if (rv != 1)
                {
                    System.err.println("Error updating FamilyID: "+txId+"  CntAmt: "+amt + "  rv: "+rv);
                }
                cnt++;
                if (cnt % tenPercent == 0)
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                }
            }
            rs.close();
         
            treeCnt     = 0;
            sql         = adjustSQL("SELECT COUNT(*) FROM taxon WHERE TaxonTreeDefID = TAXTREEDEFID");
            treeTotal   = BasicSQLUtils.getCountAsInt(dbConn, sql);
            treePercent = Math.max(treeTotal / 20, 1);
            if (progressDelegate != null) 
            {
                progressDelegate.setProcessPercent(true);
                progressDelegate.setProcess(0, 100);
            }
    
            s3Stmt.close();
            
            if (progressDelegate != null) progressDelegate.setProcess(1);
            
            String upStr = "UPDATE taxon SET TotalCOCnt=? WHERE _id=?";
            s3Stmt = dbS3Conn.prepareStatement(upStr);
            
            worker.firePropertyChange(MSG, "", "Cascading Taxonomy Updates...");
            sql = "SELECT _id FROM taxon WHERE RankID = 140";
            for (Integer familyID : BasicSQLUtils.queryForInts(dbS3Conn, sql))
            {
                fillTotalCountForTree("Taxon", familyID, s3Stmt);
            }
            if (progressDelegate != null) progressDelegate.setProcessPercent(false);
            
            worker.firePropertyChange(MSG, "", "Done Upadating Taxonomy.");
            
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
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
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
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
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
                int statsId = BasicSQLUtils.getCountAsInt(dbS3Conn, "SELECT _id FROM stats ORDER BY _id DESC");
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
                    s3Stmt.setString(5, isUsingDirectURLArg ? ImageSetupDlg.DIRECT : ImageSetupDlg.ATTMGR);
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
    private void doBuildLocalities() throws SQLException
    {
        int     cnt   = 0;
        String  sql;
        Integer count = null;

        worker.firePropertyChange(MSG, "", "Getting Locality Counts...");
        
        locNumObjsHash.clear();
        sql = "SELECT l.LocalityID, SUM(IF (co.CountAmt IS NULL, 1, co.CountAmt)) FROM locality l INNER JOIN collectingevent ce ON l.LocalityID = ce.LocalityID " +
              "INNER JOIN collectionobject co ON ce.CollectingEventID = co.CollectingEventID GROUP BY l.LocalityID";
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            count = getCount(row[1]);
            locNumObjsHash.put((Integer)row[0], count);
        }

        worker.firePropertyChange(MSG, "", "Building Localities...");
        
        String prefix  = "SELECT LocalityID, LocalityName, Latitude1, Longitude1, GeographyID ";
        String postfix = "FROM locality ORDER BY LocalityName";
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, "SELECT COUNT(*) " + adjustSQL(postfix));
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
        //int totLocs = Math.max(BasicSQLUtils.getCountAsInt(sql), 0);
        //String sql = adjustSQL("SELECT COUNT(*) FROM locality l INNER JOIN geography g ON l.GeographyID = g.GeographyID WHERE g.RankID < 200 AND l.DisciplineID = DSPLNID");
        //totLocs += Math.max(BasicSQLUtils.getCountAsInt(sql), 0);
        //System.out.println("totLocs: "+totLocs);
        
        String[] prefixes = { 
                "SELECT g.GeographyID AS GID, l.LocalityID AS LID ",
                "SELECT geo2.GeographyID, LID ",
                "SELECT geo3.GeographyID, LID "};
        
        String[] sqls = { 
                "FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = 200 AND l.DisciplineID = DSPLNID",
                "FROM (SELECT g.GeographyID AS GID, l.LocalityID AS LID, g.RankID AS RID, ParentID AS PID FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = 300 AND l.DisciplineID = DSPLNID) T1 INNER JOIN geography geo2 ON PID = geo2.GeographyID",
                "FROM (SELECT geo2.GeographyID AS GID2, LID, PID, geo2.ParentID AS PID2 FROM (SELECT g.GeographyID AS GID, l.LocalityID AS LID, g.RankID AS RID, ParentID AS PID FROM locality as l inner join geography as g on l.GeographyID = g.GeographyID WHERE g.RankID = 400 AND l.DisciplineID = DSPLNID) T1 INNER JOIN geography geo2 ON PID = geo2.GeographyID) T2 INNER JOIN geography geo3 ON PID2 = geo3.GeographyID"};
        
        locToGeoMapper.setShowLogErrors(true);
        
        for (int i=0;i<3;i++)
        {
            int totCnt = BasicSQLUtils.getCountAsInt(dbConn, adjustSQL("SELECT COUNT(*) " + sqls[i]));
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
            
            String sqlStr = prefixes[i] + sqls[i];
            
            System.out.println(adjustSQL(sqlStr));
            ResultSet rs = stmt.executeQuery(adjustSQL(sqlStr)); // Get the GeoID and LocID
            while (rs.next())
            {
                int locId = rs.getInt(2);
                int geoId = rs.getInt(1);
                locToGeoMapper.put(locId, geoId);
                cnt++;
                if (cnt % inc == 0) 
                {
                    worker.firePropertyChange(PROGRESS, 0, cnt);
                    //log.debug("LocID -> GeoID: "+cnt+ " -> "+BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geoloc"));
                }
            }
            rs.close();
            worker.firePropertyChange(PROGRESS, 0, totCnt);
        }
        //sql = adjustSQL("SELECT COUNT(*) FROM locality WHERE DisciplineID = DSPLNID");
        //int totAllLocs = Math.max(BasicSQLUtils.getCountAsInt(sql), 0);
        //System.out.println("totLocs: "+totLocs+"   totAllLocs: "+totAllLocs+ "   Diff: "+(totAllLocs - totLocs));
        
        int totCnt = BasicSQLUtils.getCountAsInt(dbConn, adjustSQL("SELECT COUNT(*) FROM geography WHERE RankID = 300"));
        if (progressDelegate != null)
        {
            progressDelegate.setProcess(0, totCnt);
            progressDelegate.setProcess(0);
        }
        int inc = totCnt / 20;
        
        cnt = 0;
        String sql = "SELECT GeographyID, HighestChildNodeNumber, NodeNumber FROM geography WHERE RankID = 300";
        ResultSet rs = stmt.executeQuery(sql); 
        while (rs.next())
        {
            int geoId    = rs.getInt(1);
            int highNode = rs.getInt(2);
            int nodeNum  = rs.getInt(3);
            
            sql = String.format("SELECT GeographyID FROM geography WHERE NodeNumber >= %d AND NodeNumber <= %d", nodeNum, highNode);
            for (Integer id : BasicSQLUtils.queryForInts(dbConn, sql))
            {
                geoStateMapper.put(id, geoId);    
            }
            
            cnt++;
            if (cnt % inc == 0) 
            {
                worker.firePropertyChange(PROGRESS, 0, cnt);
                //log.debug("LocID -> GeoID: "+cnt+ " -> "+BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM geoloc"));
            }
        }
        rs.close();
        
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
                if (cnt % 10 == 0) log.debug("Fixing Collector Ordering: " + cnt);
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

    /**
     * @param s3Conn
     * @throws SQLException
     */
    public void doBuildCharts()
    {
        if (timer != null)
        {
            timer.cancel();
            timer = null;
        }
        
        String[] fileNames = {"coll_by_year", "coll_by_decade", "growth_by_decade", 
                              "top_20_families", "taxon_types", "specimens_country", };
        
        List<ChartFileInfo> fileInfoList = null;
        
        boolean    doNextChart = true;
        boolean    isDone      = false; 
        String     sql         = null;
        String     fileName    = null;
        String     titleKey    = null;
        String     xTitleKey   = null;
        String     yTitleKey   = null;
        
        if (chartIndex < fileNames.length)
        {
            try
            {
                titleKey   = String.format("CHART%d_TITLE",  chartIndex+1);
                xTitleKey  = String.format("CHART%d_X_AXIS", chartIndex+1);
                yTitleKey  = String.format("CHART%d_Y_AXIS", chartIndex+1);
                fileName   = fileNames[chartIndex];
                
                switch (chartIndex)
                {
                    case 0: 
                        sql          = "SELECT Name, NumObjs FROM stats WHERE Grp = 5";
                        fileInfoList = createChartsForLocales(fileName, BAR_CHART, dbS3Conn, adjustSQL(sql), titleKey, xTitleKey, yTitleKey, locales); 
                        break;
                        
                    case 1: 
                        sql          = "SELECT YR, SUM(CNT) SCNT FROM (SELECT IF (co.CountAmt IS NULL, 1, co.CountAmt) AS CNT, " +
                                     "(CEILING(YEAR(if (ce.StartDate IS NULL, 0, ce.StartDate)) / 10) * 10) AS YR FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID WHERE co.CollectionID = COLMEMID AND ce.StartDate IS NOT NULL AND YEAR(ce.StartDate) <= YEAR(CURDATE()) ) T1 GROUP BY YR";
                        fileInfoList = createChartsForLocales(fileName, BAR_CHART, dbConn, adjustSQL(sql), titleKey, xTitleKey, yTitleKey, locales); 
                        break;
                        
                    case 2: 
                        sql          = "SELECT q1.YR, (@runtot := @runtot + q1.SCNT) AS rt, q1.SCNT FROM (SELECT SUM(CNT) SCNT, YR FROM (SELECT (@runtot:=0), if (co.CountAmt IS NULL, 1, co.CountAmt) AS CNT, (CEILING(YEAR(ce.StartDate) / 10) * 10) AS YR FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID WHERE co.CollectionID = COLMEMID AND ce.StartDate IS NOT NULL AND YEAR(ce.StartDate) <= YEAR(CURDATE()) ) T1 GROUP BY YR) as q1";
                        fileInfoList = createChartsForLocales(fileName, LINE_CHART, dbConn, adjustSQL(sql), titleKey, xTitleKey, yTitleKey, locales); 
                        break;
                        
                    case 3: 
                        sql          = "SELECT t.FullName, t.NumObjs FROM taxon t WHERE RankID = 140 ORDER BY t.NumObjs DESC LIMIT 0,20";
                        fileInfoList = createChartsForLocales(fileName, PIE_CHART, dbS3Conn, sql, titleKey, "", "", locales); 
                        break;
                        
                    case 4: 
                        sql          = "SELECT Name, NumObjs FROM stats WHERE Grp = 7 ORDER BY NumObjs DESC";
                        fileInfoList = createChartsForLocales(fileName, BAR_CHART, dbS3Conn, sql, titleKey, "", "", locales); 
                        break;
                        
                    case 5: 
                        sql          = "SELECT g.FullName, CNT FROM (SELECT COUNT(*) AS CNT, c.CountryID AS ID FROM colobj c WHERE c.CountryID IS NOT NULL " +
                                     "GROUP BY c.CountryID ORDER BY CNT DESC) T1 INNER JOIN geo g ON ID = g._id LIMIT 0,10";
                        fileInfoList = createChartsForLocales(fileName, BAR_CHART, dbS3Conn, sql, titleKey, "", "", locales); 
                        break;
                        
                    default:
                        isDone = true;
                }
            } catch (SQLException ex) // 
            {
                ex.printStackTrace();
                isInError = true;
            }
        } else
        {
            isDone = true;
        }
        
        log.debug(chartIndex+" "+isInError);
        
        if (!isDone && fileInfoList != null && !fileInfoList.isEmpty())
        {
            if (progressDelegate != null) progressDelegate.setProcess(chartIndex+1);
            
            fileNamesForExport.addAll(fileInfoList);
            
        } else if (isDone)
        {
            doNextChart = false;
            
            if (timer != null) timer.cancel();
            
            progressDelegate.incOverall();
            
            if (doZipFile && !isInError)
            {
                progressDelegate.setDesc("Creating catalog...");
                progressDelegate.incOverall();
                
                createXMLIndexFile();
                
                progressDelegate.setDesc("Compressing data...");
                doBuildZipFile(ZIP_FILE, fileNamesForExport);
                
                progressDelegate.setDesc("Uploading data...");
                if (!uploadFiles())
                {
                    isInError = true;
                }
                
                //log.debug("Removing: "+cacheDir.getAbsolutePath());
                /*try
                {
                    FileUtils.deleteDirectory(cacheDir);
                } catch (IOException e)
                {
                    e.printStackTrace();
                }*/
                
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
        
        if (doNextChart)
        {
            timer = new Timer();
            timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    chartIndex++;
                    doBuildCharts();
                }
            }, 3000, 4000);

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
                
                pw.println(String.format("<file md5=\"%s\" type=\"%s\" locale=\"%s\">\n  <title><![CDATA[%s]]></title>\n  <fname>%s</fname>\n</file>", md5, (isDB ? "db" : "img"), locale, title, fName + (isDB ? ".gz" : "")));
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
        Collection col = AppContextMgr.getInstance().getClassObject(Collection.class);
        UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "SUCCESS_UPL_TITLE", "SUCCESS_UPL", col.getCollectionName());
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
                        isDataSetSaved = ipadCloud.addNewDataSet(collectionName, dirName, inst.getUri(), 
                                                                 div.getName(), dsp.getName(), collectionName, 
                                                                 false, auxInfoMap.get("icon"), auxInfoMap.get("curator"));
                    }
                }
                if (stopTrying)
                {
                    break; // Stops the upload of files.
                }
            }
            
            if (!stopTrying && isDataSetSaved)
            {
                progressDelegate.setDesc("The Collection information was uploaded successfully.");
            } else
            {
                progressDelegate.setDesc("The Collection information upload was stopped.");
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
     * @param fileName
     * @param chartType
     * @param conn
     * @param sql
     * @param title
     * @param xTitle
     * @param yTitle
     * @return
     * @throws SQLException
     */
    public JFreeChart doChart(final int chartType,
                              final ArrayList<Object> list, 
                              final String title, 
                              final String xTitle, 
                              final String yTitle)
    {
        JFreeChart jfreeChart = null;
        
        if (!list.isEmpty())
        {
            switch (chartType)
            {
                case BAR_CHART:
                    jfreeChart = chartHelper.createBarChart(list, title, xTitle, yTitle, true, width, height);
                    break;
                    
                case LINE_CHART:
                    jfreeChart = chartHelper.createLineChart(list, title, xTitle, yTitle, true, width, height);
                    break;
                    
                case PIE_CHART:
                case PIE_CHART3D:
                    jfreeChart = chartHelper.createPieChart(list, title, width, height, chartType == PIE_CHART3D);
                    break;
            }
        }
        return jfreeChart;
    }
    
    /**
     * @param conn
     * @param sql
     * @return
     * @throws SQLException
     */
    public ArrayList<Object> getChartData(final Connection conn,
                                          final String sql) throws SQLException
    {
        ArrayList<Object> list = new ArrayList<Object>();
     
        Statement stmt = conn.createStatement();
        
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next())
        {
            String desc = rs.getString(1);
            if (desc == null)
            {
                desc = "N/A";
            }
            list.add(desc);
            list.add(rs.getInt(2));
        }
        rs.close();
        stmt.close();
        
        return list;
    }
    
    /**
     * @param chartType
     * @param conn
     * @param sql
     * @param title
     * @param xTitle
     * @param yTitle
     * @return
     * @throws SQLException
     */
    public JFreeChart doChart(final int chartType,
                              final Connection conn,
                              final String sql, 
                              final String title, 
                              final String xTitle, 
                              final String yTitle) throws SQLException
    {
        ArrayList<Object> list = getChartData(conn, sql);
        if (!list.isEmpty())
        {
            return doChart(chartType, list, title, xTitle, yTitle);
        }
        return null;
    }
    

    /**
     * @param chartType
     * @param conn
     * @param sql
     * @param title
     * @param xTitle
     * @param yTitle
     * @return
     * @throws SQLException
     */
    public List<ChartFileInfo> createChartsForLocales(final String baseFileName,
                                                             final int chartType,
                                                             final Connection conn,
                                                             final String sql, 
                                                             final String titleKey, 
                                                             final String xTitleKey, 
                                                             final String yTitleKey,
                                                             final Locale[] localeList) throws SQLException
    {
        ArrayList<ChartFileInfo> fileList = new ArrayList<ChartFileInfo>();
        ArrayList<Object> list = getChartData(conn, sql);
        if (!list.isEmpty())
        {
            Locale currLocale = Locale.getDefault();
            for (Locale locale : localeList)
            {
                Locale.setDefault(locale);
                setResourceLocale(locale);
                loadAndPushResourceBundle(iPadDBExporterPlugin.CHART_RES_NAME);
                
                String title  = getResourceString(titleKey);
                String xTitle = StringUtils.isNotEmpty(xTitleKey) ? getResourceString(xTitleKey) : "";
                String yTitle = StringUtils.isNotEmpty(yTitleKey) ? getResourceString(yTitleKey) : "";

                String     fileName   = String.format("%s_%s.png", baseFileName, locale.getLanguage());
                JFreeChart jfreeChart = doChart(chartType, list, title, xTitle, yTitle);
                if (jfreeChart != null)
                {
                    File outFile = new File(cacheDir + File.separator + fileName);
                    chartHelper.createImage(outFile, jfreeChart, width, height);
                    
                    fileList.add(new ChartFileInfo(fileName, title, locale.getLanguage()));
                }
                popResourceBundle();
            }
            Locale.setDefault(currLocale);
            setResourceLocale(currLocale);
        }
        return fileList;
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
        fileNamesForExport.clear();
        fileNamesForExport.add(new ChartFileInfo(dbName, "The Data", ""));

        loadAndPushResourceBundle(iPadDBExporterPlugin.RES_NAME);
        
        Institution inst  = AppContextMgr.getInstance().getClassObject(Institution.class);
        Collection  col   = AppContextMgr.getInstance().getClassObject(Collection.class);
        String      colNm = col.getCollectionName();
        
        if (ipadCloud.doesDataSetExist(colNm, inst.getUri()))
        {
            String msg = getFormattedResStr("ASK_OVERWRITE_DS", colNm);
            if (askYesNoLocalized("CONTINUE", "CANCEL", msg, "OVERWRITE_DS") == JOptionPane.NO_OPTION)
            {
                return false;
            }
        }
        
        AppPreferences global = AppPreferences.getGlobalPrefs();
        
        isUsingDirectURL  = true;
        ImageSetupDlg dlg = new ImageSetupDlg();
        imageURL    = global.get(dlg.getRemoteImageURLPrefName(), null);
        String type = global.get(dlg.getRemoteImageURLTypePrefName(), null);
        if (StringUtils.isNotEmpty(imageURL) && StringUtils.isNotEmpty(type))
        {
            isUsingDirectURL = type.equals(ImageSetupDlg.DIRECT);
        }
        
        // XXX Testing for testing
        //imageURL = "http://anza.nhm.ku.edu/getfileref.php?type=<type>&filename=<fname>&coll=<coll>&disp=<disp>&div=<div>&inst=<inst>&scale=<scale>";
        //imageURL = "http://specify6-prod.nhm.ku.edu/getfileref.php?type=<type>&filename=<fname>&coll=<coll>&disp=<disp>&div=<div>&inst=<inst>&scale=<scale>";
        
//        if (StringUtils.isEmpty(imageURL))
//        {
//            // Ask for image URL
//        }
        
        DBConnection dbc = DBConnection.getInstance();
        if (itUsrPwd == null)
        {
            UIRegistry.showLocalizedMsg(JOptionPane.INFORMATION_MESSAGE, "LOGIN", "LOGIN_REQ");
            itUsrPwd = DatabaseLoginPanel.getITUsernamePwd();
            if (itUsrPwd == null)
            {
                popResourceBundle();
                return false;
            }
        }
        
        dbRootConn = new DBConnection(itUsrPwd.first, itUsrPwd.second, dbc.getConnectionStr(), dbc.getDriver(), dbc.getDialect(), dbc.getDatabaseName());
        if (dbRootConn != null)
        {
            Class.forName("org.sqlite.JDBC");
            dbS3Conn = DriverManager.getConnection("jdbc:sqlite:" + cacheDir + File.separator + dbName);
        } else
        {
            itUsrPwd = null;
            popResourceBundle();
            return false;
        }
        
        iPadRepositoryHelper h = new iPadRepositoryHelper();
        auxInfoMap = h.getAuxilaryInfo();
        if (auxInfoMap == null)
        {
            popResourceBundle();
            return false;
        }
        
        createProgressUI();
        addProgress(UIRegistry.getResourceString("STARTING"));

        IdHashMapper.setEnableDelete(false);
        changeListener = cl;
        worker = new SwingWorker<Integer, Integer>()
        {
            @Override
            protected Integer doInBackground() throws Exception
            {
                boolean doStats      = false;
                boolean doTaxon      = false;
                
                boolean doGeography  = false;
                boolean doAgents     = false;
                boolean doLocalities = false;
                boolean doImages     = false;
                
                boolean doPreBldGeo  = false;
                boolean doBldColObjs = false;
                boolean doCollectors = false;
        
                boolean doCharts     = false;
                
                doZipFile = true;
                
                boolean[] bools = {doRebuildDB, doStats, doAgents, doTaxon, doBldColObjs, doGeography, 
                                   doLocalities, doImages, doPreBldGeo, doCollectors, doCharts, doZipFile, 
                                   doUpload};
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
                    BasicSQLUtils.setDBConnection(dbConn);
                    
                    IdMapperMgr.getInstance().setDBs(dbConn, dbConn);
                    locToGeoMapper = IdMapperMgr.getInstance().addHashMapper(GEOLOCTBLNAME, null, false);
                    locToGeoMapper.reset();

                    fixCollectorOrder(dbConn);
                    
                    if (doAll || doRebuildDB)
                    {
                        if (!createDBTables(dbS3Conn, BLD_ISITE_FILE))
                        {
                            isInError = true;
                            return null;
                        }
                        if (!createDBTables(dbRootConn.getConnection(), BLD_XREF_FILE))
                        {
                            isInError = true;
                            return null;
                        }
                        progressDelegate.incOverall();
                    }
                    
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
                        doBuildTaxon();
                        progressDelegate.incOverall();
                    }
                    
                    if (doAll || doGeography)
                    {
                        doBuildGeography();
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
            
                    /*if (doAll || doCollectors)
                    {
                        worker.firePropertyChange(MSG, "", "Updating Collectors...");
                        doBuildCollectors();
                        progressDelegate.incOverall();
                    }*/
                    
                    worker.firePropertyChange(MSG, "", "Exporting Charts...");
                    
                    if (doAll || doCharts)
                    {
                        progressDelegate.incOverall();
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                chartIndex = 0;
                                progressDelegate.setDesc("Exporting Charts...");
                                progressDelegate.setProcess(0, 6);
                                iPadDBExporter.this.doBuildCharts();
                            }
                        });
                    }
                    
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
                
                if (isInError)
                {
                    shutdown();
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
        
        return true;
    }
    
    /**
     * 
     */
    private void showFinalErrorDlg()
    {
        if (helper.isNetworkConnError())
        {
            UIRegistry.showError("There was a problem connecting to the internet.\nThis is required to create the SpecifyInsight data store.");
        } else
        {
            UIRegistry.showError("There was an unrecoverable error while creating the iPad data store.");
        }
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
        if (dbRootConn != null)
        {
            dbRootConn.close();
        }
        
        progressDelegate.setVisible(false);
        progressDelegate.dispose();
        progressDelegate = null;
        
        tblWriter.close();
        
        try
        {
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
        public ChartFileInfo(String fileName, String title, String locale)
        {
            super();
            System.out.println(locale);
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
