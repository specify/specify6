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
package edu.ku.brc.specify.toycode.mexconabio;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;
import edu.ku.brc.util.Triple;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 22, 2010
 *
 */
public class CreateReportForXRef extends AnalysisBase
{
    protected HashMap<String, Pair<Integer, Integer>> instAverageScoresHashpMap = new HashMap<String, Pair<Integer,Integer>>();
    
    protected Stack<Object[]> recycler = new Stack<Object[]>();
    
    /**
     * 
     */
    public CreateReportForXRef()
    {
        super();
    }
    
    /**
     * @return
     */
    protected Object[] getRow()
    {
        Object[] row;
        if (recycler.size() > 0)
        {
            row = recycler.pop();
        } else
        {
            row = new Object[NUM_FIELDS];
        }
        System.arraycopy(objNulls, 0, row, 0, NUM_FIELDS);
        return row;
    }
    
    /**
     * @param collection
     */
    protected void recycle(final Collection<Object[]> collection)
    {
        recycler.addAll(collection);
        collection.clear();
    }
    
    /**
     * @param refRow
     * @param cmpRow
     */
    protected void compareRowsForReports(final String trClass, final Object[] refRow, final Object[] cmpRow)
    {
        clearRowAttrs(); // Clears Color Codes
        
        // rescore to set Color Codes
        score(refRow, cmpRow); // Sets Color Codes
        
        writeRow(trClass, cmpRow, tdColorCodes);
    }
    
    /**
     * @param instCode
     * @param score
     */
    protected void regScore(final String instCode, final int score)
    {
        Pair<Integer, Integer> entry = instAverageScoresHashpMap.get(instCode);
        if (entry == null)
        {
            entry = new Pair<Integer, Integer>(0, 0);
            instAverageScoresHashpMap.put(instCode, entry);
        }
        entry.first++;
        entry.second += score;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(int type, int options)
    {
        String dataDirName = "compare";
        
        Object[] refRow = new Object[NUM_FIELDS];
        Object[] cmpRow = new Object[NUM_FIELDS];
        
        String sql = "SELECT sm.SNIBID, a.IdSNIB, r.id FROM gbifsnib AS gs " +
                        "Inner Join snibmex AS sm ON gs.SNIBID = sm.SNIBID " +
                        "Inner Join raw AS r ON gs.GBIFID = r.id " +
                        "Inner Join angiospermas AS a ON sm.GBIFID = a.IdSNIB " +
                        "WHERE r.genus =  a.Genus AND r.`year` =  CONVERT(a.`Year`, CHAR(8)) AND r.`month` =  CONVERT(a.`Month`, CHAR(8)) AND a.latitude IS NOT NULL " +
                        "ORDER BY sm.SNIBID, a.IdSNIB, r.id";
        
        String sqlCnt = "SELECT COUNT(*) FROM gbifsnib AS gs " +
                        "Inner Join snibmex AS sm ON gs.SNIBID = sm.SNIBID " +
                        "Inner Join raw AS r ON gs.GBIFID = r.id " +
                        "Inner Join angiospermas AS a ON sm.GBIFID = a.IdSNIB " +
                        "WHERE r.genus =  a.Genus AND r.`year` =  CONVERT(a.`Year`, CHAR(8)) AND r.`month` =  CONVERT(a.`Month`, CHAR(8)) AND a.latitude IS NOT NULL " +
                        "ORDER BY sm.SNIBID, a.IdSNIB, r.id";

        
        String snibFromClause1 = "FROM angiospermas WHERE IdSNIB = ?";
        String gbifFromClause1 = "FROM raw WHERE id = ?";
        String michFromClause1 = "FROM conabio WHERE ID = ?";
        
        Comparator<Object[]> comparator = new Comparator<Object[]>()
        {
            @Override
            public int compare(Object[] o1, Object[] o2)
            {
                Integer i1 = (Integer)o1[SCORE_INX];
                Integer i2 = (Integer)o2[SCORE_INX];
                return i2.compareTo(i1);
            }
        };
        
        //System.out.println(String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore));
        long totalRecs     = BasicSQLUtils.getCount(dbLMConn, sqlCnt);
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;
        
        Statement         stmt     = null;
        PreparedStatement refStmt  = null;
        PreparedStatement snibStmt = null;
        PreparedStatement gbifStmt = null;
        try
        {
            stmt  = dbLMConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            refStmt  = dbSrcConn.prepareStatement(michSQL + michFromClause1);
            snibStmt = dbDstConn.prepareStatement(snibSQL + snibFromClause1);
            gbifStmt = dbGBIFConn.prepareStatement(gbifSQL + gbifFromClause1);
            
            HashSet<Integer> snibIdsHash = new HashSet<Integer>();
            HashSet<Integer> gbifIdsHash = new HashSet<Integer>();
            
            Integer prevRefId = null;
            
            
            int    reportRecsSize = 500;
            String title          = String.format("%s %d - %d",    dataDirName, 0, reportRecsSize);
            String fileName       = String.format("%s_%d_%d.html", dataDirName, 0, reportRecsSize);
            
            startLogging("analysis", dataDirName, fileName, title, true, 100);
            
            ResultSet rs = stmt.executeQuery(sql);
            
            Vector<Object[]> gbifRows = new Vector<Object[]>();
            Vector<Object[]> snibRows = new Vector<Object[]>();
            
            while (rs.next())
            {
                int refId  = rs.getInt(1);    // Inigo's database
                int snibId = rs.getInt(2);    // Snib's database
                int gbifId = rs.getInt(3);     // GBIF's database
                
                if (prevRefId == null || prevRefId != refId)
                {
                    for (Integer sId : snibIdsHash)
                    {
                        snibStmt.setInt(1, sId);
                        ResultSet snibRS = snibStmt.executeQuery();
                        if (snibRS.next())
                        {
                            cmpRow = getRow();
                            fillSNIBRow(cmpRow, snibRS);
                            int score = (int)Math.round((score(refRow, cmpRow) / (double)maxScore) * 100.0); // Sets Color Codes
                            cmpRow[SCORE_INX] = score;
                            regScore((String)cmpRow[INST_INX], score);
                            snibRows.add(cmpRow);
                        }
                        snibRS.close();
                    }
                    
                    tblWriter.println(String.format("<TR><TD colspan=\"%d\">&nbsp;</TD></TR>", NUM_FIELDS));
                    Collections.sort(snibRows, comparator);
                    for (Object[] srow : snibRows)
                    {
                        compareRowsForReports("ev", refRow, srow);
                    }
                    recycle(snibRows);
                    
                    snibIdsHash.clear();
                    
                    for (Integer gId : gbifIdsHash)
                    {
                        gbifStmt.setInt(1, gId);
                        ResultSet gbifRS = gbifStmt.executeQuery();
                        if (gbifRS.next())
                        {
                            cmpRow = getRow();
                            fillGBIFRow(cmpRow, gbifRS);
                            int score = (int)Math.round((score(refRow, cmpRow) / (double)maxScore) * 100.0); // Sets Color Codes
                            cmpRow[SCORE_INX] = score;
                            regScore((String)cmpRow[INST_INX], score);
                            gbifRows.add(cmpRow);
                        }
                        gbifRS.close();
                    }
                    
                    tblWriter.println(String.format("<TR><TD colspan=\"%d\">&nbsp;</TD></TR>", NUM_FIELDS));
                    Collections.sort(gbifRows, comparator);
                    for (Object[] grow : gbifRows)
                    {
                        compareRowsForReports("od", refRow, grow);
                    }
                    recycle(gbifRows);
                    gbifIdsHash.clear();
                    
                    if (prevRefId != null)
                    {
                        tblWriter.endTable();
                        tblWriter.println("<BR><BR>");
                        tblWriter.startTable(100);
                        tblWriter.logHdr(titles);
                    }
                    
                    prevRefId = refId;
                    
                    refStmt.setInt(1, refId);
                    ResultSet refRS = refStmt.executeQuery();
                    if (!refRS.next()) 
                    {
                        refRS.close();
                        continue;
                    }
                    fillMichRow(refRow, refRS);
                    refRS.close();
                    
                    writeRow("", refRow, null);
                }
                
                snibIdsHash.add(snibId);
                gbifIdsHash.add(gbifId);
                
                procRecs++;
                if (procRecs % reportRecsSize == 0)
                {
                    System.out.println(procRecs);
                    
                    title    = String.format("%s %d - %d",    dataDirName, procRecs, (procRecs+reportRecsSize));
                    fileName = String.format("%s_%d_%d.html", dataDirName, procRecs, (procRecs+reportRecsSize));
                            
                    startNewDocument(fileName, title, true, 100);
                    
                    long endTime     = System.currentTimeMillis();
                    long elapsedTime = endTime - startTime;
                    
                    double timePerRecord      = (elapsedTime / procRecs); 
                    
                    double hrsLeft = ((totalRecs - procRecs) * timePerRecord) / HRS;
                    
                    int seconds = (int)(elapsedTime / 60000.0);
                    if (secsThreshold != seconds)
                    {
                        secsThreshold = seconds;
                        
                        String msg = String.format("Elapsed %8.2f hr.mn   Percent: %6.3f  Hours Left: %8.2f ", 
                                ((double)(elapsedTime)) / HRS, 
                                100.0 * ((double)procRecs / (double)totalRecs),
                                hrsLeft);
                        System.out.println(msg);
                    }
                }
            }
            rs.close();
            
            for (Integer sId : snibIdsHash)
            {
                snibStmt.setInt(1, sId);
                ResultSet snibRS = snibStmt.executeQuery();
                if (snibRS.next())
                {
                    cmpRow = getRow();
                    fillSNIBRow(cmpRow, snibRS);
                    snibRows.add(cmpRow);
                }
                snibRS.close();
            }
            recycle(snibRows);
            
            tblWriter.println(String.format("<TR><TD colspan=\"%d\">&nbsp;</TD></TR>", NUM_FIELDS));
            Collections.sort(snibRows, comparator);
            for (Object[] srow : snibRows)
            {
                compareRowsForReports("ev", refRow, srow);
            }
            
            tblWriter.println(String.format("<TR><TD colspan=\"%d\">&nbsp;</TD></TR>", NUM_FIELDS));
            Collections.sort(gbifRows, comparator);
            for (Object[] grow : gbifRows)
            {
                compareRowsForReports("od", refRow, grow);
            }
            recycle(gbifRows);
            
            tblWriter.endTable();
            
            Vector<Triple<String, Integer, Double>> instScores = new Vector<Triple<String, Integer, Double>>();
            for (String ic : instAverageScoresHashpMap.keySet())
            {
                Pair<Integer, Integer> p = instAverageScoresHashpMap.get(ic);
                if (p != null)
                {
                    double averageScore = ((double)p.second) / ((double)p.first);
                    Triple<String, Integer, Double> ip = new Triple<String, Integer, Double>(ic, p.first, averageScore);
                    instScores.add(ip);
                }
            }
            Collections.sort(instScores, new Comparator<Triple<String, Integer, Double>>()
            {
                @Override
                public int compare(Triple<String, Integer, Double> o1, Triple<String, Integer, Double> o2)
                {
                    return o2.third.compareTo(o1.third);
                }
            });
            
            startNewDocument("InstScores.html", "Average Institution Scores", false, 100);
            tblWriter.startTable(100);
            tblWriter.logHdr("Institution", "Average Matching Score", "Number Matches");
            for (Triple<String, Integer, Double> p : instScores)
            {
                String scoreStr = String.format("%5.2f", p.third);
                tblWriter.logWithSpaces(p.first, scoreStr, Integer.toString(p.second));
            }
            tblWriter.endTable();
            tblWriter.setHasLines();
            
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally 
        {
            try
            {
                if (refStmt != null)
                {
                    refStmt.close();
                }
                if (snibStmt != null)
                {
                    snibStmt.close();
                }
                if (gbifStmt != null)
                {
                    gbifStmt.close();
                }
            } catch (Exception ex)
            {
                
            }
        }
        convLogger.closeAll();
        System.out.println("Done.");
    }

    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        CreateReportForXRef xrefReport = new CreateReportForXRef();
        xrefReport.createDBConnection("localhost", "3306", "plants", "root", "root");
        xrefReport.createSrcDBConnection("localhost",  "3306", "plants", "root", "root");
        xrefReport.createDestDBConnection("localhost", "3306", "plants", "root", "root");
        xrefReport.createLMDBConnection("localhost", "3306", "plants", "root", "root");
        xrefReport.process(0,0);
        xrefReport.cleanup();
    }
}
