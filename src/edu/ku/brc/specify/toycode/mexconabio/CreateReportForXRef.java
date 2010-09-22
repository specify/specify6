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

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Stack;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

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
    protected Calendar      cal = Calendar.getInstance();
    protected StringBuilder sb  = new StringBuilder();
    
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
     * @param rs
     * @throws SQLException 
     */
    protected void fillRefRow(final Object[] refRow, final ResultSet rs) throws SQLException
    {
        String  catNum       = rs.getString(1).trim();
        String  collectorNum = rs.getString(2);
        String  collector    = rs.getString(3);
        String  genus        = rs.getString(4);
        String  species      = rs.getString(5);
        String  subspecies   = rs.getString(6);
        String  locality     = rs.getString(7);
        Date    collDate     = rs.getDate(8);
        String  country      = rs.getString(9);
        String  state        = rs.getString(10);
        
        int     year;
        int     mon;
        int     day;
        
        if (collDate != null)
        {
            cal.setTime(collDate);
            year         = cal.get(Calendar.YEAR);
            mon          = cal.get(Calendar.MONTH) + 1;
            day          = cal.get(Calendar.DAY_OF_MONTH);
        } else
        {
            year = 0;
            mon  = 0;
            day  = 0;
        }
        
        refRow[CATNUM_INX]     = catNum;
        refRow[COLNUM_INX]     = collectorNum;
        refRow[GENUS_INX]      = genus;
        refRow[SPECIES_INX]    = species;
        refRow[SUBSPECIES_INX] = subspecies;
        refRow[COLLECTOR_INX]  = collector;
        refRow[LOCALITY_INX]   = locality;
        refRow[LATITUDE_INX]   = null;
        refRow[LONGITUDE_INX]  = null;
        refRow[YEAR_INX]       = year > 0 ? Integer.toString(year) : null;
        refRow[MON_INX]        = year > 0 ? Integer.toString(mon) : null;
        refRow[DAY_INX]        = year > 0 ? Integer.toString(day) : null;
        refRow[COUNTRY_INX]    = country;
        refRow[STATE_INX]      = state;
    }
    
    /**
     * @param cmpRow
     * @param rs
     * @throws SQLException
     */
    private void fillSNIBRow(final Object[] cmpRow, final ResultSet rs) throws SQLException
    {
        //   1          2           3        4                5             6         7          8       9         10               11           12       13        14     15       16         17
        // IdSNIB, CatalogNumber, Genus, Species, Cataegoryinfraspecies, Latitude, Longitude, Country, State, LastNameFather, LastNameMother, FirstName, Locality, `Year`, `Month`, `Day`, CollectorNumber FROM angiospermas "; 
        
        cmpRow[CATNUM_INX]     = rs.getString(2);
        cmpRow[COLNUM_INX]     = rs.getString(17);
        cmpRow[GENUS_INX]      = rs.getString(3);
        cmpRow[SPECIES_INX]    = rs.getString(4);
        cmpRow[SUBSPECIES_INX] = rs.getString(5);
        cmpRow[LOCALITY_INX]   = rs.getString(13);
        cmpRow[LATITUDE_INX]   = rs.getString(6);
        cmpRow[LONGITUDE_INX]  = rs.getString(7);
        cmpRow[YEAR_INX]       = getIntToStr(rs.getObject(14));
        cmpRow[MON_INX]        = getIntToStr(rs.getObject(15));
        cmpRow[DAY_INX]        = getIntToStr(rs.getObject(16));
        cmpRow[COUNTRY_INX]    = rs.getString(16);
        cmpRow[STATE_INX]      = rs.getString(9);
        
        String fatherName      = rs.getString(10);
        String motherName      = rs.getString(11);
        String firstName       = rs.getString(12);
        
        boolean hasFather = StringUtils.isNotEmpty(fatherName);
        boolean hasMother = StringUtils.isNotEmpty(motherName);
        boolean hasFirst  = StringUtils.isNotEmpty(firstName);
        
        sb.setLength(0);
        if (hasFather)
        {
            sb.append(fatherName);
        }
        if (hasMother)
        {
            if (hasFather) sb.append(", ");
            sb.append(motherName);
        }
        if (hasFirst)
        {
            if (hasFather || hasMother) sb.append(", ");
            sb.append(firstName);
        }
        cmpRow[COLLECTOR_INX] = sb.toString();
    }
    
    /**
     * @param cmpRow
     * @param rs
     * @throws SQLException
     */
    protected void fillGBIFRow(final Object[] cmpRow, final ResultSet rs) throws SQLException
    {
        cmpRow[CATNUM_INX]     = rs.getString(2);
        cmpRow[COLNUM_INX]     = rs.getString(15);
        cmpRow[GENUS_INX]      = rs.getString(3);
        cmpRow[SPECIES_INX]    = rs.getString(4);
        cmpRow[SUBSPECIES_INX] = rs.getString(5);
        cmpRow[COLLECTOR_INX]  = rs.getString(10);
        cmpRow[LOCALITY_INX]   = rs.getString(11);
        cmpRow[LATITUDE_INX]   = rs.getString(6);
        cmpRow[LONGITUDE_INX]  = rs.getString(7);
        cmpRow[YEAR_INX]       = rs.getString(12);
        cmpRow[MON_INX]        = rs.getString(13);
        cmpRow[DAY_INX]        = rs.getString(14);
        cmpRow[COUNTRY_INX]    = rs.getString(8);
        cmpRow[STATE_INX]      = rs.getString(9);
    }
    
    
    /**
     * @param row
     * @param colorCodes
     */
    protected void writeRow(final String trClass, final Object[] row, final String[] colorCodes)
    {
        tblWriter.println(String.format("<TR class=\"%s\">", trClass));
        for (int i=0;i<NUM_FIELDS;i++)
        {
            tblWriter.logTDCls(colorCodes != null ? colorCodes[i] : null, 
                               row[i] != null ? row[i].toString() : "");
        }
        tblWriter.println("</TR>");
    }
    
    
    /**
     * @param refRow
     * @param cmpRow
     */
    protected void compareRowsForReports(final String trClass, final Object[] refRow, final Object[] cmpRow)
    {
        clearRowAttrs(); // Clears Color Codes
        
        int score = score(refRow, cmpRow); // Sets Color Codes
        
        //cmpRow[SCORE_INX] = score;
        
        writeRow(trClass, cmpRow, cCls);
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
                        "ORDER BY sm.SNIBID, a.IdSNIB, r.id " +
                        "LIMIT 0,500";
        
                                   //      1          2           3        4                5             6         7          8           9               10            11      12       13        14     15       16         17
        String snibSQL         = "SELECT IdSNIB, CatalogNumber, Genus, Species, Cataegoryinfraspecies, Latitude, Longitude, Country, LastNameFather, LastNameMother, FirstName, State, Locality, `Year`, `Month`, `Day`, CollectorNumber "; 
        String snibFromClause1 = "FROM angiospermas WHERE IdSNIB = ?";
        //String snibFromClause2 = "FROM angiospermas WHERE CollectorNumber IS NULL AND `Year` = ? AND `Month` = ? AND Genus = ?";
        
                                           //      1         2           3        4        5           6         7          8          9               10            11      12    13    14        15
        String gbifSQL         = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num ";
        String gbifFromClause1 = "FROM raw WHERE id = ?";
        //String gbifFromClause2 = "FROM raw WHERE collector_num IS NULL AND year = ? AND month = ? AND genus = ?";
        
        //                        1       2           3              4           5             6              7               8           9        10   11
        String sql121K = "SELECT BarCD, CollNr, Collectoragent1, GenusName, SpeciesName, SubSpeciesName, LocalityName, Datecollstandrd, COUNTRY, STATE, ID FROM conabio " +
                         "WHERE ID = ?";
        
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

        Statement         stmt     = null;
        PreparedStatement refStmt  = null;
        PreparedStatement snibStmt = null;
        PreparedStatement gbifStmt = null;
        try
        {
            stmt  = dbLMConn.createStatement(ResultSet.FETCH_FORWARD, ResultSet.CONCUR_READ_ONLY);
            stmt.setFetchSize(Integer.MIN_VALUE);
            
            refStmt  = dbSrcConn.prepareStatement(sql121K);
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
            
            int procRecs = 0;
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
                            cmpRow[SCORE_INX] = (int)Math.round((score(refRow, cmpRow) / (double)maxScore) * 100.0); // Sets Color Codes
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
                            cmpRow[SCORE_INX] =  (int)Math.round((score(refRow, cmpRow) / (double)maxScore) * 100.0); // Sets Color Codes
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
                    fillRefRow(refRow, refRS);
                    refRS.close();
                    
                    writeRow("", refRow, null);
                }
                
                snibIdsHash.add(snibId);
                gbifIdsHash.add(gbifId);
                
                procRecs++;
                if (procRecs % reportRecsSize == 0)
                {
                    System.out.println(procRecs);
                    
                    break;
                    
                    /*
                    title    = String.format("%s %d - %d",    dataDirName, procRecs, (procRecs+reportRecsSize));
                    fileName = String.format("%s_%d_%d.html", dataDirName, procRecs, (procRecs+reportRecsSize));
                            
                    startNewDocument(fileName, title, true, 100);
                    */
                    
                    /*long endTime     = System.currentTimeMillis();
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
                        */
                    
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
