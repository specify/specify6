/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.specify.toycode.mexconabio;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Triple;

public class MakeGBIFProcessHash extends AnalysisBase
{
    private static final Logger log = Logger.getLogger(MakeGBIFProcessHash.class);
    
    private final int MAX_RECORDS_SEG = 300000;
    
    protected PrintWriter       pw         = null;
    protected Statement         stmt       = null;
    protected PreparedStatement insertStmt = null;
    protected PreparedStatement updateStmt = null;
    protected PreparedStatement checkStmt  = null;
    protected PreparedStatement insertIds  = null;
    
    protected long totalRecs;
    protected int writeCnt  = 0;
    protected int updateCnt = 0;
    
    protected Stack<DataEntry>           recycleStack = new Stack<DataEntry>();
    protected HashMap<String, DataEntry> groupHash    = new HashMap<String, DataEntry>();
    
    /**
     * 
     */
    public MakeGBIFProcessHash()
    {
        super();
    }
    
    /**
     * @param grpId
     * @param rawIds
     */
    private void writeIds(final int grpId, final ArrayList<Integer> rawIds)
    {
        for (Integer rawId : rawIds)
        {
            try
            {
                insertIds.setInt(1, grpId);
                insertIds.setInt(2, rawId);
                insertIds.executeUpdate();
                
            } catch (Exception ex) 
            { 
                ex.printStackTrace();
            }  
        }
    }
    
    /**
     * @throws SQLException
     */
    private void writeHash() throws SQLException
    {
        System.out.println("Writing Hash...");
        pw.println("Writing Hash...");
        
        long sTm = System.currentTimeMillis();
        int err = 0;
        int cnt = 0;
        for (DataEntry de : groupHash.values())
        {
            checkStmt.setString(1, de.collnum);
            checkStmt.setString(2, de.genus);
            checkStmt.setString(3, de.year);
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next())
            {
                int id = rs.getInt(1);
                try
                {
                    updateStmt.setInt(1, de.cnt);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();
                    updateCnt++;
                    
                    writeIds(id, de.ids);
                    
                } catch (Exception ex) 
                { 
                    ex.printStackTrace();
                    err++;
                }
                
            } else
            {
                insertStmt.setString(1, de.collnum);
                insertStmt.setString(2, de.genus);
                insertStmt.setString(3, de.year);
                insertStmt.setString(4, de.mon);
                insertStmt.setInt(5,    de.cnt);
                
                try
                {
                    insertStmt.executeUpdate();
                    writeCnt++;
                    
                    writeIds(BasicSQLUtils.getInsertedId(insertStmt), de.ids);
                    
                } catch (Exception ex) 
                { 
                    ex.printStackTrace();
                    err++;
                }
            }
            cnt++;
            rs.close();
            
            if (cnt % 10000 == 0)
            {
                System.out.println(cnt);
                pw.println(cnt);
            }
        }
        
        recycle(groupHash.values());
        groupHash.clear();
        
        long elapsed = (System.currentTimeMillis() - sTm) / 1000;
        String msg = String.format("Done Writing Dups: %d  Cnt: %d Elapsed: %d", err, cnt, elapsed);
        System.out.println(msg);
        pw.println(msg);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.toycode.mexconabio.AnalysisBase#process(int, int)
     */
    @Override
    public void process(final int type, final int options)
    {
        final double HRS = 1000.0 * 60.0 * 60.0;
        final long PAGE_CNT = 1000000;
        
        totalRecs = BasicSQLUtils.getCount(dbGBIFConn, "SELECT COUNT(*) FROM raw");
        
        int minIndex = BasicSQLUtils.getCount(dbGBIFConn, "SELECT MIN(id) FROM raw");
        //int maxIndex = BasicSQLUtils.getCount(dbGBIFConn, "SELECT MAX(id) FROM raw");
        
        int segs = (int)(totalRecs / PAGE_CNT) + 1;
        
        try
        {
            pw = new PrintWriter("GroupHash.log");
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        
        long procRecs      = 0;
        long startTime     = System.currentTimeMillis();
        int  secsThreshold = 0;

        try
        {
            String idsInsert = "INSERT INTO group_hash_ids (GrpID, RawID) VALUES (?,?)";
            insertIds = dbDstConn.prepareStatement(idsInsert);
            
            String gbifsnibInsert = "INSERT INTO group_hash (collnum, genus, year, mon, cnt) VALUES (?,?,?,?,?)";
            insertStmt = dbDstConn.prepareStatement(gbifsnibInsert);
            
            String gbifsnibUpdate = "UPDATE group_hash SET cnt=? WHERE id = ?";
            updateStmt = dbDstConn.prepareStatement(gbifsnibUpdate);
            
            String gbifsnibCheck = "SELECT id FROM group_hash WHERE collnum=? AND genus=? AND year=?";
            checkStmt = dbDstConn.prepareStatement(gbifsnibCheck);
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }

        for (int pc=0;pc<segs;pc++)
        {
            try
            {
                String clause  = String.format(" FROM raw WHERE id > %d AND id < %d", (pc * PAGE_CNT)+minIndex,  ((pc+1) * PAGE_CNT)+minIndex+1);
                String gbifSQL = "SELECT  id, collector_num, genus, year, month " + clause;
                
                System.out.println(gbifSQL);
                pw.println(gbifSQL);
                
                stmt  = dbGBIFConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                
                String msg = "Starting Query... "+totalRecs;
                System.out.println(msg);
                pw.println(msg);
                
                ResultSet rs = stmt.executeQuery(gbifSQL);
                
                msg = String.format("Starting Processing... Total Records %d  Max Score: %d  Threshold: %d", totalRecs, maxScore, thresholdScore);
                System.out.println(msg);
                pw.println(msg);

                while (rs.next())
                {
                    procRecs++;
                    
                    String year = rs.getString(4);
                    
                    year = StringUtils.isNotEmpty(year) ? year.trim() : null;
                    
                    if (StringUtils.isNotEmpty(year) && !StringUtils.isNumeric(year))
                    {
                        continue;
                    }
                    
                    int     rawId   = rs.getInt(1);
                    String  collnum = rs.getString(2);
                    String  genus   = rs.getString(3);
                    String  mon     = rs.getString(5);
                    
                    collnum = StringUtils.isNotEmpty(collnum) ? collnum.trim() : null;
                    genus   = StringUtils.isNotEmpty(genus) ? genus.trim() : null;
                    mon     = StringUtils.isNotEmpty(mon) ? mon.trim() : null;
                    
                    int c = 0;
                    if (collnum == null) c++;
                    if (genus == null) c++;
                    if (year == null) c++;
                    
                    if (c == 2)
                    {
                        continue;
                    }
                    
                    collnum = collnum != null ? collnum : "";
                    genus   = genus   != null ? genus   : "";
                    year    = year    != null ? year    : "";
                    mon     = mon     != null ? mon     : "";
                    
                    if (collnum.length() > 64)
                    {
                        collnum = collnum.substring(0, 63);
                    }

                    if (genus.length() > 64)
                    {
                        genus= genus.substring(0, 63);
                    }

                    if (year.length() > 8)
                    {
                        year = year.substring(0, 8);
                    }

                    if (mon.length() > 8)
                    {
                        mon = year.substring(0, 8);
                    }

                    String    name = String.format("%s_%s_%s", collnum, genus, year);
                    DataEntry de   = groupHash.get(name);
                    if (de != null)
                    {
                        de.cnt++;
                    } else
                    {
                        de = getDataEntry(collnum, genus, year, mon);
                        groupHash.put(name, de);
                    }
                    de.ids.add(rawId);
                    
                    if (groupHash.size() > MAX_RECORDS_SEG)
                    {
                        writeHash();
                    }
                }
                rs.close();
                
                if (groupHash.size() > 0)
                {
                    writeHash();
                }
            
            
                System.out.println("Done with seg "+pc);
                pw.println("Done with seg "+pc);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } finally 
            {
                try
                {
                    if (stmt != null)
                    {
                        stmt.close();
                    }
                } catch (Exception ex) {}
            }
            
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
                pw.println(msg);
                pw.flush();
            }
        }
        
        try
        {
            if (insertStmt != null)
            {
                insertStmt.close();
            }
            if (updateStmt != null)
            {
                updateStmt.close();
            }
            if (checkStmt != null)
            {
                checkStmt.close();
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        }
        
        String msg = String.format("Done - Writes: %d  Updates: %d", writeCnt, updateCnt);
        System.out.println(msg);
        pw.println(msg);
        pw.flush();
        pw.close();
    }
    
    /**
     * @param collnumArg
     * @param genusArg
     * @param yearArg
     * @return
     */
    private DataEntry getDataEntry(String collnumArg, String genusArg, String yearArg, String monArg)
    {
        if (recycleStack.size() > 0)
        {
            DataEntry de = recycleStack.pop();
            de.collnum = collnumArg;
            de.genus   = genusArg;
            de.year    = yearArg;
            de.mon     = monArg;
            de.cnt     = 1;
            de.clear();
            return de;
        }
        return new DataEntry(collnumArg, genusArg, yearArg, monArg);
    }
    
    /**
     * @param dec
     */
    protected void recycle(final Collection<DataEntry> dec)
    {
        recycleStack.addAll(dec);
    }
    
    //------------------------------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------------------------------
    class DataEntry
    {
        String collnum;
        String genus;
        String year;
        String mon;
        int    cnt;
        
        ArrayList<Integer> ids = new ArrayList<Integer>();
        
        /**
         * @param collnum
         * @param genus
         * @param year
         * @param cnt
         */
        public DataEntry(String collnum, String genus, String year, String mon)
        {
            super();
            this.collnum = collnum;
            this.genus   = genus;
            this.year    = year;
            this.mon     = mon;
            this.cnt     = 1;
        }
        
        public void addId(final int id)
        {
            ids.add(id);
        }
        
        public void clear()
        {
            ids.clear();
        }
    }
    
    //------------------------------------------------------------------------------------------
    public static void main(String[] args)
    {
        MakeGBIFProcessHash awg = new MakeGBIFProcessHash();
        awg.createDBConnection("localhost",     "3306", "plants", "root", "root");
        awg.createDestDBConnection("localhost", "3306", "plants", "root", "root");
        awg.process(0,0);
        awg.cleanup();
    }
}
