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
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Taxon;
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

    
    private ProgressFrame progressFrame;
    private Connection    conn;
    private int           tooManyCnt   = 0;
    private int           notFoundCnt  = 0;
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
        progressFrame.setDesc("Cleaning up Synonyms...");
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
     * @param doCleanup true does clean and report, false does report only
     * @param frame progress frame
     */
    private void fixMisparentedSynonyms()
    {
        Connection newDBConn = DBConnection.getInstance().getConnection();
        final String TOKEN = "<!-- STATISTICS -->";
        try
        {
            TableWriter tblWriter  = new TableWriter(tmpReportName, "Orphan Synonyms for " + collectionName, true);
            tblWriter.println(TOKEN);
            tblWriter.startTable();
            tblWriter.logHdr(NBSP, "Orphan Synonym", "Current Genus", "Current Family", "Proposed Genus", "Proposed Family", "Catalog Numbers<BR>Determined to Synonym");
            
            DBTableInfo ti       = DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId());
            String      whereStr = QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, false);
            //String      cntStr   = String.format("SELECT COUNT(*) FROM taxon WHERE IsAccepted = 0 AND %s", whereStr);
            
            Discipline        discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
            PlaceholderHelper phHelper   = new PlaceholderHelper(discipline.getTaxonTreeDef());
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
    
            String statsStr = fixMisparentedSynonymsLevel(newDBConn, tblWriter, phHelper, 180, 220);
            tblWriter.endTable();
            tblWriter.flush();
            tblWriter.close();
            
            try
            {
                phHelper.getTaxonTreeDef().updateAllNodes(null, true, true);
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TreeTableViewer.class, ex);
            }
            
            PrintWriter    pw = new PrintWriter(reportName);
            File           tf  = new File(tmpReportName);
            BufferedReader br = new BufferedReader(new FileReader(tf));
            try
            {
                String line = br.readLine();

                while (line != null)
                {
                    if (line.startsWith(TOKEN))
                    {
                        pw.println(statsStr);
                        
                    } else
                    {
                        pw.println(line);
                    }
                    line = br.readLine();
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

    /**
     * @param newDBConn
     * @param tblWriter
     * @param phHelper
     * @param parentLevelRankID
     * @param childLevelRankID
     */
    public String fixMisparentedSynonymsLevel(final Connection        newDBConn, 
                                              final TableWriter       tblWriter,
                                              final PlaceholderHelper phHelper,
                                              final int               parentLevelRankID, 
                                              final int               childLevelRankID)
    {
        StringBuilder statsSB = new StringBuilder();
        
        DBTableInfo ti       = DBTableIdMgr.getInstance().getInfoById(Taxon.getClassTableId());
        String      whereStr = QueryAdjusterForDomain.getInstance().getSpecialColumns(ti, false);
        
        String parentName = BasicSQLUtils.querySingleObj(String.format("SELECT Name FROM taxontreedefitem WHERE %s AND RankID = %d", whereStr, parentLevelRankID));
        String childName  = BasicSQLUtils.querySingleObj(String.format("SELECT Name FROM taxontreedefitem WHERE %s AND RankID = %d", whereStr, childLevelRankID));
        
        int numFixed = BasicSQLUtils.update(newDBConn, "UPDATE taxon SET IsAccepted=1 WHERE IsAccepted = 0 AND AcceptedID IS NULL AND " + whereStr);
        log.debug("Number of IsAccepted Fixed: " + numFixed);
        
        log.debug(String.format("\nParent: %s (%d)    Child: %s (%d)", parentName, parentLevelRankID, childName, childLevelRankID));
        
        String postfix = " FROM taxon WHERE IsAccepted = 0 AND AcceptedID IS NOT NULL AND RankID = " + childLevelRankID + " AND " + whereStr;
        int totalCnt   = BasicSQLUtils.getCountAsInt("SELECT COUNT(TaxonID) " + postfix);
        if (totalCnt == 0)
        {
            return "";
        }
        
        setProgress(0, 100);
        
        UIFieldFormatterIFace catNumFmt = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
        
        log.debug("SELECT COUNT(TaxonID) " + postfix);
        int percent = totalCnt / 50;
        percent = Math.max(percent, 1);
        
        int cnt = 0;
        try
        {
            HashMap<Integer, Taxon> rankToPlaceHolderHash = phHelper.getPlaceHolderTreeHash();
            
            tooManyCnt  = 0;
            notFoundCnt = 0;
            
            int fndCnt        = 0;
            int phCnt         = 0;
            int err           = 0;
            int correct       = 0;
            int processCnt    = 0;
            int withCatNumCnt = 0;
            int updateErr     = 0;
            
            String searchStr = String.format("SELECT TaxonID, FullName, Name FROM taxon WHERE IsAccepted <> 0 AND BINARY Name = ? AND RankID = ? AND %s", whereStr);
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
                int     rankId          = rs.getInt(2);
                String  fullName        = rs.getString(3);
                int     oldParentId     = rs.getInt(4);
                boolean oldParentIdNull = rs.wasNull();
                
                //System.err.println("-------["+fullName+"]-------");
                
                String oldName;
                if (oldParentIdNull)
                {
                    oldName = NBSP;
                } else
                {
                    oldName = BasicSQLUtils.querySingleObj("SELECT FullName FROM taxon WHERE TaxonID = " + oldParentId);
                }
                
                ArrayList<String> names      = parseFullName(fullName);
                String            genus      = names.get(0);
                Integer           newGenusID = getTaxonNode(genus, parentLevelRankID);
                String            oldFamily  = !oldParentIdNull ? getFamilyName(oldParentId) : NBSP;
                String            catNums    = getCatNumsForTaxon(taxonID, catNumFmt);
                
                if (!catNums.equals(NBSP))
                {
                    withCatNumCnt++;
                }

                if (newGenusID != null)
                {
                    if (newGenusID != oldParentId) // Search for new Parent and found one
                    {
                        cnt++;
                        String newFamily = getFamilyName(newGenusID);
                        tblWriter.logWithSpaces(Integer.toString(cnt), fullName, oldName, oldFamily, genus, newFamily, catNums);
                        if (doCleanup)
                        {
                            if (update(newGenusID, taxonID))
                            {
                                updateErr++;
                            }
                        }
                        fndCnt++;
                    } else
                    {
                        correct++;
                    }
                } else 
                {
                    Taxon placeHolder = rankToPlaceHolderHash.get(parentLevelRankID);
                    if (placeHolder != null)
                    {
                        cnt++;
                        tblWriter.logWithSpaces(Integer.toString(cnt), fullName, oldName, oldFamily, placeHolder.getName(), NBSP, catNums);
                        phCnt++;
                        if (doCleanup)
                        {
                            if (update(placeHolder.getId(), taxonID))
                            {
                                updateErr++;
                            }
                        }
                    } else
                    {
                        cnt++;
                        tblWriter.logErrors(Integer.toString(cnt), fullName, String.format("Bad RankID %s",  rankId), oldFamily, NBSP, catNums);
                       err++;
                    }
                }
                processCnt++;
                if (processCnt % percent == 0)
                {
                    int p = (int)(((processCnt * 100) / (double)totalCnt) + 0.5);
                    setProgress(p, null);
                    //if (p > 5) break;
                }
            }
            rs.close();
            
            statsSB.append("<BR><TABLE class=\"o\" cellspacing=\"0\" cellpadding=\"1\">\n");
            String[] descs  = {"Total Records Processed", "Number of Records in Report", "Number Records with new Genus", 
                               "Number of Records parented to Place Holder", "Number of Synonyms used in Determinations", 
                               "Number of Synonyms correctly parented",  "Number of Records in Error", "Number of Update Errors"};
            int[]    values = {processCnt, cnt, fndCnt, phCnt, withCatNumCnt, correct, err, updateErr};
            for (int i=0;i<descs.length;i++)
            {
                statsSB.append(createRow(descs[i], values[i], processCnt));
            }
            statsSB.append("</TABLE><BR/><BR/><BR/>"); 
            
//            log.debug(String.format("cnt:                     %6d", cnt));
//            log.debug(String.format("fndCnt:                  %6d", fndCnt));
//            log.debug(String.format("phCnt:                   %6d", phCnt));
//            log.debug(String.format("err:                     %6d", err));
//            log.debug(String.format("correct:                 %6d", correct));
            
            pTaxNodeStmt.close();
            pCatNumStmt.close();
            pUpdateStmt.close();
            
            st.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        return statsSB.toString();
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
