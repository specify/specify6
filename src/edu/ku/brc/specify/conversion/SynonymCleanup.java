/* Copyright (C) 2012, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.conversion;

import static edu.ku.brc.ui.UIRegistry.getAppDataDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.ui.treetables.TreeTableViewer;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Apr 11, 2013
 *
 */
public class SynonymCleanup extends SwingWorker<Boolean, Boolean>
{
    private static final Logger log = Logger.getLogger(SynonymCleanup.class);
    private static final String NBSP = "&nbsp;";
    
    private String tmpReportName = "orphan_synonym_report_%s.tmp";
    private String reportName    = "orphan_synonym_report_%s.html";

    private int maxSynRankToReparent = 230; //Ranks 'below' this rank are just moved to a placeholder
    private boolean moveAllSynsOfLowerRanksToPlaceHolder = true; //if a synonym's preferred taxon has a lower rank than the synonym 
    										//(and thus the synonym's parent has the same or lower rank than the synonym)
    										//move it to a placeholder
    
    private ProgressFrame progressFrame;
    private Connection    conn;
    private int           tooManyCnt = 0;
    private int           notFoundCnt = 0;
    private boolean       doCleanup;
    private boolean       isSuccessful = false;
    private boolean       hasStarted   = false;
    private String        collectionName;
    
    // Prepare statements
    private PreparedStatement pTaxNodeStmt = null;
    private PreparedStatement pCatNumStmt  = null;
    private PreparedStatement pUpdateStmt  = null;

    /**
     * @param doCleanup
     * @param progressFrame
     */
    public SynonymCleanup(final boolean doCleanup)
    {
        super();
        
        this.doCleanup     = doCleanup;
        this.conn          = DBConnection.getInstance().getConnection();
        
        this.collectionName = AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionName();
        
        String dirPath = getAppDataDir() + File.separator;
        String colNm   = StringUtils.replaceChars(this.collectionName, ' ', '_');
        tmpReportName  = dirPath + String.format(tmpReportName, colNm);
        reportName     = dirPath + String.format(reportName, colNm);
        
        String msg = String.format("Synonym Cleanup for %s", collectionName);
        UIRegistry.writeSimpleGlassPaneMsg(msg, 24);
        
        progressFrame = new ProgressFrame(msg);
        progressFrame.turnOffOverAll();
        progressFrame.setDesc(doCleanup ? "Cleaning up Synonyms..." : "Creating Synonym Report...");
        progressFrame.adjustProgressFrame();
        progressFrame.getProcessProgress().setIndeterminate(true);
        progressFrame.getCloseBtn().setVisible(false);
        UIHelper.centerAndShow(progressFrame);
        progressFrame.toFront();
        progressFrame.setAlwaysOnTop(true);
        
        UIRegistry.pushWindow(progressFrame);
    }

    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#doInBackground()
     */
    @Override
    protected Boolean doInBackground() throws Exception
    {
        fixMisparentedSynonyms();
        return null;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done()
    {
        super.done();
        
        UIRegistry.clearSimpleGlassPaneMsg();
        
        if (progressFrame != null)
        {
            progressFrame.setVisible(false);
            progressFrame.dispose();
            UIRegistry.popWindow(progressFrame);
        }
        
        if (isSuccessful)
        {
            try
            {
                AttachmentUtils.openFile(new File(reportName));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param pTaxNodeStmt
     * @param name
     * @return
     * @throws SQLException
     */
    private Integer getTaxonNode(final String name, 
                                 final int rankId) throws SQLException
    {
        Integer id = null;
        pTaxNodeStmt.setString(1, name);
        pTaxNodeStmt.setInt(2, rankId);
        ResultSet rs2 = pTaxNodeStmt.executeQuery();
        int cnt = 0;
        while (rs2.next())
        {
            id = rs2.getInt(1);
            cnt++;
        }
        rs2.close();
        
        if (cnt > 1)
        {
            tooManyCnt++;
            return null;
        }
        
        if (cnt == 0)
        {
            notFoundCnt++;
        }
        
        return id;
    }
    
    /**
     * @param fullName
     * @return
     */
    private ArrayList<String> parseFullName(final String fullName)
    {
        ArrayList<String>  names     = new ArrayList<String>();
        String[] toks = StringUtils.split(fullName, ' ');
        for (String t : toks)
        {
            if (!t.endsWith("."))
            {
                names.add(t);
            }
        }
        return names;
    }
    
    
    /**
     * @param rank
     * @return
     */
    private String getRankText(int rank)
    {
        TreeDefIface<?,?,?> treeDef = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(Taxon.class);
    	TreeDefItemIface<?,?,?> defItem = treeDef.getDefItemByRank(rank);
    	return defItem.getDisplayText();
    }
    
    /**
     * 
     */
    private void fixMisparentedSynonyms()
    {
        TreeDefIface<?,?,?> treeDef = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getTreeDefForClass(Taxon.class);
        String sql = "select distinct p.rankid, t.rankid from taxon t inner join " 
        		+ "taxon p on p.taxonid = t.parentid where t.rankid >= 180 and t.acceptedid is not null and t.TaxonTreeDefID = " + treeDef.getTreeDefId()
        		+ " order by t.rankid, p.rankid";
    	List<Object[]> synConfigs = BasicSQLUtils.query(sql);
    	fixMisparentedSynonyms(synConfigs);
    }
    
    /**
     * @param doCleanup true does clean and report, false does report only
     * @param frame progress frame
     */
    private void fixMisparentedSynonyms(List<Object[]> synConfigs)
    {
        Connection newDBConn = DBConnection.getInstance().getConnection();
        final String TOKEN = "<!-- STATISTICS -->";
        try
        {
            TableWriter tblWriter  = new TableWriter(tmpReportName, "Orphan Synonyms for " + collectionName, true);
            tblWriter.println(TOKEN);
            //tblWriter.startTable();
            //String parentRankText = "Parent";//getRankText(parentRank);
            //tblWriter.logHdr(NBSP, "Orphan Synonym", "Current " + parentRankText, "Current Family", "Proposed " + parentRankText, "Proposed Family", "Catalog Numbers<BR>Determined to Synonym");
            
            DBTableInfo ti       = DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId());
            String      whereStr = QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, false);
            String      cntStr   = String.format("SELECT COUNT(*) FROM taxon WHERE IsAccepted = 0 AND %s", whereStr);
            
            Discipline        discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
            PlaceholderHelper phHelper   = new PlaceholderHelper(doCleanup, discipline.getTaxonTreeDef());
            phHelper.setSynonymBranch(true);
            phHelper.buildPlaceHolderInfo();
            
            // Now eliminate duplicates with no Determination
            String sql = String.format("SELECT K,CNT FROM (SELECT K,COUNT(K) CNT FROM (SELECT CONCAT(AcceptedID, '_', ParentID, '_', TimestampCreated, '_', FullName) AS K FROM taxon WHERE %s AND IsAccepted = 0) T1 GROUP BY K) T1 WHERE CNT > 1", whereStr);
            Statement stmt = null;
            try
            {
                int dupCnt = 0;
                stmt = newDBConn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    Object[] tokens = StringUtils.split(rs.getString(1), '_');
                    sql = String.format("SELECT t.TaxonID FROM taxon t LEFT JOIN determination d ON d.TaxonID = t.TaxonID WHERE AcceptedID = %s AND ParentID = %s AND t.TimestampCreated = '%s' AND t.FullName = '%s'", tokens[0], tokens[1], tokens[2], tokens[3]);
                    Integer taxonId = BasicSQLUtils.getCount(sql);
                    if (taxonId != null)
                    {
                        log.debug(tokens[3]);
                        dupCnt++;
                    }
                }
                rs.close();
                log.debug("dupCnt: "+dupCnt);
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                if (stmt != null) stmt.close();
            }
    
            //String [] statsStr = new String[synConfigs.size()];
            //int s = 0;
            
            //String statsStr = "";
            tblWriter.startTable();
            String parentRankText = "Parent";//getRankText((Integer)config[0]);
            tblWriter.logHdr(NBSP, UIRegistry.getResourceString("SynonymCleanup.OrphanSynonym"), 
            		String.format(UIRegistry.getResourceString("SynonymCleanup.CurrentParent"), parentRankText), 
            		UIRegistry.getResourceString("SynonymCleanup.CurrentFamily"), 
            		String.format(UIRegistry.getResourceString("SynonymCleanup.ProposedParent"), parentRankText), 
            		UIRegistry.getResourceString("SynonymCleanup.ProposedFamily"), 
            		UIRegistry.getResourceString("SynonymCleanup.CatalogNumsDetermined"));
            int[] stats = {0, 0, 0, 0, 0, 0, 0, 0};

            boolean needKeys = false;
            int currRank = (Integer)synConfigs.get(0)[1];
            int totalSynCount = BasicSQLUtils.getCount(cntStr);
            boolean doPercent = totalSynCount < 5000;
            int progressInterval = totalSynCount  / (!doPercent ? 500 : (totalSynCount > 1200 ? 100 : 50));
            if (!doPercent)
            {
                progressFrame.getProcessProgress().setIndeterminate(true);
            	setProgress(0, totalSynCount);
            } else
            {
            	setProgress(0, 100);
            }
            
            progressInterval = Math.max(progressInterval, 1);
            progressFrame.setDesc(doCleanup ? "Cleaning up Synonyms..." : "Creating Synonym Report...");
            
            for (int c = 1; c < synConfigs.size(); c++)
            {
            	if (currRank == (Integer)synConfigs.get(c)[1])
            	{
            		needKeys = true;
            		break;
            	} 
            	currRank = (Integer)synConfigs.get(c)[1];
            }
            
            Set<Integer> processedKeys = needKeys ? new HashSet<Integer>() : null;
            for (int c = 0; c < synConfigs.size(); c++)
            {
                //tblWriter.startTable();
                //String parentRankText = getRankText((Integer)config[0]);
                //tblWriter.logHdr(NBSP, "Orphan Synonym", "Current " + parentRankText, "Current Family", "Proposed " + parentRankText, "Proposed Family", "Catalog Numbers<BR>Determined to Synonym");
            	//statsStr[s++] = fixMisparentedSynonymsLevel(newDBConn, tblWriter, phHelper, (Integer)config[0], (Integer)config[1]);
                Object[] config = synConfigs.get(c);
                Integer parentRank = (Integer)config[0];
                Integer childRank = (Integer)config[1];		
                boolean skipBads = needKeys && c < synConfigs.size() - 1 && childRank.equals(synConfigs.get(c+1)[1]);                
            	fixMisparentedSynonymsLevel(newDBConn, tblWriter, phHelper, parentRank, childRank, skipBads, stats, processedKeys, progressInterval, doPercent);
            	//tblWriter.endTable();
            }
            tblWriter.endTable();
            tblWriter.flush();
            tblWriter.close();
            progressFrame.processDone();
            
            if (doCleanup)
            {
                try
                {
                    phHelper.getTaxonTreeDef().updateAllNodes(null, true, true);
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeTableViewer.class, ex);
                }
            }
            StringBuilder statsSB = new StringBuilder();
            statsSB.append("<BR><TABLE class=\"o\" cellspacing=\"0\" cellpadding=\"1\">\n");
            String[] descs  = {UIRegistry.getResourceString("SynonymCleanup.TotalRecsProcessed"), 
            		UIRegistry.getResourceString("SynonymCleanup.NumberRecsInReport"),//"Number of Records in Report", 
            		UIRegistry.getResourceString("SynonymCleanup.NumberRecsWithNewGenus"),//"Number Records with new Genus", 
            		UIRegistry.getResourceString("SynonymCleanup.NumberRecsParentedToPlaceHolder"),//"Number of Records parented to Place Holder", 
            		UIRegistry.getResourceString("SynonymCleanup.NumberSynsDetermined"),//"Number of Synonyms used in Determinations", 
            		UIRegistry.getResourceString("SynonymCleanup.NumberSynsCorrectlyParented"),//"Number of Synonyms correctly parented",  
            		UIRegistry.getResourceString("SynonymCleanup.NumberRecordsInError"),//"Number of Records in Error", 
            		UIRegistry.getResourceString("SynonymCleanup.NumberOfUpdateErrors")};//"Number of Update Errors"};
            //int[]    values = {processCnt, cnt, fndCnt, phCnt, withCatNumCnt, correct, err, updateErr};
            for (int i=0;i<descs.length;i++)
            {
                statsSB.append(createRow(descs[i], stats[i], stats[0]));
            }
            statsSB.append("</TABLE><BR/><BR/><BR/>"); 

            
            PrintWriter    pw = new PrintWriter(reportName);
            File           tf  = new File(tmpReportName);
            BufferedReader br = new BufferedReader(new FileReader(tf));
            try
            {
                String line = br.readLine();
                //s = 0;
                doPercent = stats[0] < 5000;
                progressInterval = stats[0]  / (!doPercent ? 500 : (stats[0] > 1200 ? 100 : 50));
                if (!doPercent)
                {
                    progressFrame.getProcessProgress().setIndeterminate(true);
                	setProgress(0, stats[0]);
                } else
                {
                	setProgress(0, 100);
                }
                
                progressInterval = Math.max(progressInterval, 1);
                int processCnt = 0;
                while (line != null)
                {
                    if (line.startsWith(TOKEN))
                    {
                        //pw.println(statsStr[s++]);
                        //pw.println(statsStr);
                    	pw.println(statsSB.toString());
                    } else
                    {
                        pw.println(line);
                    }
                    line = br.readLine();
                    processCnt++;
                    if (processCnt % progressInterval == 0)
                    {
                    	int p = (int)(((processCnt * 100) / (double)stats[0]) + 0.5);
                        setProgress(doPercent ? p : processCnt, null);
                    }
                }
                isSuccessful = true;
                
            } finally
            {
                br.close();
                pw.flush();
                pw.close();
            }
            tf.delete();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param parentID
     * @return family name or null
     * @throws SQLException 
     */
    private String getFamilyName(final Integer parentID)
    {
        if (parentID != null)
        {
            PreparedStatement pLookupStmt = null;
            try
            {
                pLookupStmt = conn.prepareStatement("SELECT RankID, FullName, ParentID FROM taxon WHERE TaxonID = ?");
                pLookupStmt.setInt(1, parentID);
                ResultSet rs = pLookupStmt.executeQuery();
                if (rs.next())
                {
                    int     rankID       = rs.getInt(1);
                    String  fullName     = rs.getString(2);
                    int     newParentID  = rs.getInt(3);
                    boolean isParentNull = rs.wasNull();
                    
                    if (rankID == 140)
                    {
                        return fullName != null ? fullName : NBSP;
                    }
                    
                    if (rankID > 140 && !isParentNull)
                    {
                        fullName = getFamilyName(newParentID);
                        if (StringUtils.isNotEmpty(fullName))
                        {
                            return fullName;
                        }
                    }
                }
                rs.close();
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                try
                {
                    if (pLookupStmt != null) pLookupStmt.close();
                } catch (SQLException e) {}
            }
        }        
        return NBSP;
    }
    
    /**
     * @param taxonID
     * @param catNumFmt
     * @return
     * @throws SQLException 
     */
    private String getCatNumsForTaxon(final int taxonID, final UIFieldFormatterIFace catNumFmt) throws SQLException
    {
        StringBuilder sb = null;
        pCatNumStmt.setInt(1, taxonID);
        ResultSet rs = pCatNumStmt.executeQuery();
        while (rs.next())
        {
            String catNum = rs.getString(1);
            if (StringUtils.isNotEmpty(catNum))
            {
                if (sb == null)
                {
                    sb = new StringBuilder();
                } else
                {
                    sb.append(", ");
                }
                catNum = catNumFmt != null ? (String)catNumFmt.formatToUI(catNum) : catNum;
                sb.append(catNum);
            }
        }
        rs.close();
        
        return sb != null ? sb.toString() : NBSP;
    }
    
    /**
     * @param value
     * @param total
     */
    private void setProgress(final Integer value, final Integer total)
    {
        if (progressFrame != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (!hasStarted)
                    {
                        progressFrame.getProcessProgress().setIndeterminate(false);
                        hasStarted = true;
                    }
                    if (total != null)
                    {
                        progressFrame.setProcess(value, total);
                    } else
                    {
                        progressFrame.setProcess(value);
                    }
                }
            });
        } else
        {
            log.debug("Progress: "+value+"%");
        }
    }
    
    /**
     * @param newParentId
     * @param taxonID
     * @return
     * @throws SQLException
     */
    private boolean update(final int newParentId, final int taxonID) throws SQLException
    {
        pUpdateStmt.setInt(1, newParentId);
        pUpdateStmt.setInt(2, taxonID);
        return pUpdateStmt.executeUpdate() != 1;
    }

    private String getParentFullName(final List<String> names)
    {
    	String result = "";
    	for (int n = 0; n < names.size() - 1; n++ )
    	{
    		result += names.get(n) + " ";
    	}
    	return result.trim();
    }
    
    /**
     * @param phHelper
     * @param parentLevelRankID
     * @return
     */
    Taxon getPlaceHolder(PlaceholderHelper phHelper, int parentLevelRankID)
    {
    	Taxon placeHolder = phHelper.getPlaceHolderTreeHash().get(parentLevelRankID);
    	if (placeHolder == null && parentLevelRankID== 0)
    	{
    		placeHolder = phHelper.getHighestPlaceHolder();
    	}
    	return placeHolder;
    }
    
    /**
     * @param newDBConn
     * @param tblWriter
     * @param phHelper
     * @param parentLevelRankID
     * @param childLevelRankID
     */
    public void fixMisparentedSynonymsLevel(final Connection        newDBConn, 
                                              final TableWriter       tblWriter,
                                              final PlaceholderHelper phHelper,
                                              final int               parentLevelRankID, 
                                              final int               childLevelRankID,
                                              final boolean 		  skipBadParentRanks,
                                              final int[]             stats,
                                              final Set<Integer>      processedKeys,
                                              int                     progressInterval,
                                              boolean                 doPercent)
    {

    	DBTableInfo ti       = DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId());
        String      whereStr = QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, false);
        
        String parentName = BasicSQLUtils.querySingleObj(String.format("SELECT Name FROM taxontreedefitem WHERE %s AND RankID = %d", whereStr, parentLevelRankID));
        String childName  = BasicSQLUtils.querySingleObj(String.format("SELECT Name FROM taxontreedefitem WHERE %s AND RankID = %d", whereStr, childLevelRankID));
        
        int numFixed = BasicSQLUtils.update(newDBConn, "UPDATE taxon SET IsAccepted=1 WHERE IsAccepted = 0 AND AcceptedID IS NULL AND " + whereStr);
        log.debug("Number of IsAccepted Fixed: " + numFixed);
        
        log.debug(String.format("\nParent: %s (%d)    Child: %s (%d)", parentName, parentLevelRankID, childName, childLevelRankID));
        
        String postfix = " FROM taxon WHERE IsAccepted = 0 AND AcceptedID IS NOT NULL AND RankID = " + childLevelRankID + " AND " + whereStr;
        int totalCnt   = BasicSQLUtils.getCountAsInt("SELECT COUNT(TaxonID) " + postfix);
 
    	System.out.println("fixMisparentedSynonymsLevel: " + parentLevelRankID + " > " + childLevelRankID + " (" + totalCnt + ")");

        if (totalCnt == 0)
        {
            return;
        }
                
        UIFieldFormatterIFace catNumFmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
        
        log.debug("SELECT COUNT(TaxonID) " + postfix);
        
        int cnt = stats[1];
        try
        {
            
            tooManyCnt  = 0;
            notFoundCnt = 0;
            
 
            //processCnt, cnt, fndCnt, phCnt, withCatNumCnt, correct, err, updateErr};            
            int processCnt    = stats[0]; //
            int fndCnt        = stats[2]; //
            int phCnt         = stats[3]; //
            int withCatNumCnt = stats[4]; //
            int correct       = stats[5]; //
            int err           = stats[6]; //
            int updateErr     = stats[7]; //

            String searchStr = String.format("SELECT TaxonID, FullName, Name FROM taxon WHERE IsAccepted <> 0 AND BINARY FullName = ? AND RankID = ? AND %s", whereStr);
            pTaxNodeStmt = newDBConn.prepareStatement(searchStr);
            
            pCatNumStmt = newDBConn.prepareStatement("SELECT co.CatalogNumber FROM taxon t INNER JOIN determination d ON t.TaxonID = d.TaxonID " +
                    "INNER JOIN collectionobject co ON d.CollectionObjectID = co.CollectionObjectID " +
                    "WHERE t.TaxonID = ?");
            
            pUpdateStmt = newDBConn.prepareStatement("UPDATE taxon SET ParentID=? WHERE TaxonID = ?");

                    
            Statement st = newDBConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = st.executeQuery("SELECT TaxonID, RankID, FullName, ParentID " + postfix + " ORDER BY FullNAme ASC");
            while (rs.next())
            {
                int     taxonID         = rs.getInt(1);
                if (processedKeys != null && processedKeys.contains(taxonID))
                {
                	continue;
                }
                int     rankId          = rs.getInt(2);
                String  fullName        = rs.getString(3);
                int     oldParentId     = rs.getInt(4);
                boolean oldParentIdNull = rs.wasNull();
                
                
                //System.err.println("-------["+fullName+"]-------");
                
                String  oldParentName = "";
                Integer parentRankID = null;
				if (oldParentIdNull) 
				{
					oldParentName = NBSP;
				} else 
				{
					Object[] row = BasicSQLUtils
							.queryForRow("SELECT FullName,RankID FROM taxon WHERE TaxonID = "
									+ oldParentId);

					oldParentName = (String) row[0];
					parentRankID = (Integer) row[1];
				}
				
                boolean parentRankOK = parentRankID != null && parentRankID == parentLevelRankID;
                
                if (!parentRankOK && parentRankID != null && skipBadParentRanks)
                {
                	continue;
                }
                
				if (processedKeys != null) {
					processedKeys.add(taxonID);
				}

				boolean getParent = !((moveAllSynsOfLowerRanksToPlaceHolder && (parentRankID != null && parentRankID >= childLevelRankID)) 
						|| childLevelRankID > maxSynRankToReparent);
						
				ArrayList<String> names = parseFullName(fullName);
				String parent = parentLevelRankID == 180 ? names.get(0)
						: getParentFullName(names); // names.get(0);
				Integer newParentID = parentRankOK && getParent ? getTaxonNode(parent,
							parentLevelRankID) : null;
				String oldFamily = !oldParentIdNull && parentRankOK ? getFamilyName(oldParentId)
							: NBSP;
				String catNums = getCatNumsForTaxon(taxonID, catNumFmt);

				if (!parentRankOK) {
					oldParentName = NBSP;
				}

				if (!catNums.equals(NBSP)) {
					withCatNumCnt++;
				}
				
				if (newParentID != null) {
					if (newParentID != oldParentId) // Search for new Parent and
													// found one
					{
						cnt++;
						String newFamily = getFamilyName(newParentID);
						tblWriter.logWithSpaces(Integer.toString(cnt),
								fullName, oldParentName, oldFamily, parent,
								newFamily, catNums);
						if (doCleanup) {
							if (update(newParentID, taxonID)) {
								updateErr++;
							}
						}
						fndCnt++;
					} else {
						correct++;
					}
				} else {
					Taxon placeHolder = getPlaceHolder(phHelper,
							parentLevelRankID);
					if (placeHolder != null) {
						cnt++;
						tblWriter.logWithSpaces(Integer.toString(cnt),
								fullName, oldParentName, oldFamily,
								placeHolder.getName(), NBSP, catNums);
						phCnt++;
						if (doCleanup) {
							if (update(placeHolder.getId(), taxonID)) {
								updateErr++;
							}
						}
					} else {
						cnt++;
						tblWriter.logErrors(Integer.toString(cnt), fullName,
								String.format("Bad RankID %s", rankId),
								oldFamily, parent, NBSP, catNums);
						err++;
					}
				}
				processCnt++;
	
				if (processCnt % progressInterval == 0) {
					int p = (int) (((processCnt * 100) / (double) totalCnt) + 0.5);
					setProgress(doPercent ? p : processCnt, null);
				}
            }
            rs.close();
            

            stats[0] = processCnt;
            stats[1] = cnt;
            stats[2] = fndCnt;
            stats[3] = phCnt;
            stats[4] = withCatNumCnt;
            stats[5] = correct;
            stats[6] = err;
            stats[7] = updateErr;
            
            pTaxNodeStmt.close();
            pCatNumStmt.close();
            pUpdateStmt.close();
            
            st.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param desc
     * @param cnt
     * @param totalCnt
     * @return
     */
    private String createRow(final String desc, final int cnt, final int totalCnt)
    {
        String percent = NBSP;
        if (cnt < totalCnt)
        {
            percent = String.format("%d%c", (int)((double)(cnt) * 100.0 / (double)(totalCnt)), '%');
        }
        return String.format("<tr><td align=\"right\">%s:</td><td align=\"right\">%d</td><td align=\"right\">%s</td></tr>", desc, cnt, percent);
    }

}
