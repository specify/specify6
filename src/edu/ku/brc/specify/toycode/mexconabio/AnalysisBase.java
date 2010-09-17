/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.util.AttachmentUtils;

public abstract class AnalysisBase
{
    protected static final int COLNUM_INX     = 0;
    protected static final int CATNUM_INX     = 1;
    protected static final int GENUS_INX      = 2;
    protected static final int SPECIES_INX    = 3;
    protected static final int SUBSPECIES_INX = 4;
    protected static final int COLLECTOR_INX  = 5;
    protected static final int LOCALITY_INX   = 6;
    protected static final int LATITUDE_INX   = 7;
    protected static final int LONGITUDE_INX  = 8;
    protected static final int YEAR_INX       = 9;
    protected static final int MON_INX        = 10;
    protected static final int DAY_INX        = 11;
    protected static final int COUNTRY_INX    = 12;
    protected static final int STATE_INX      = 13;
    protected static final int SCORE_INX      = 14;
    
    protected static final int DO_COLNUM     = 1;
    protected static final int DO_CATNUM     = 2;
    protected static final int DO_GENUS      = 4;
    protected static final int DO_SPECIES    = 8;
    protected static final int DO_SUBSPECIES = 16;
    protected static final int DO_COLLECTOR  = 32;
    protected static final int DO_LOCALITY   = 64;
    protected static final int DO_LATITUDE   = 128;
    protected static final int DO_LONGITUDE  = 256;
    protected static final int DO_YEAR       = 512;
    protected static final int DO_MON        = 1024;
    protected static final int DO_COUNTRY    = 8096;
    protected static final int DO_STATE      = 16192;
    protected static final int DO_ALL        = (DO_STATE*2)-1;
    
    public static final  String CONN_STR = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
    
    protected String[] titles         = {"Cat Num", "Col Num", "Genus", "Species", "SubSpecies", "Collector", "Locality", "Latitude", "Longitude", "Year", "Mon", "Day", "Country", "State", "Score"};
    protected int[]    MAXSCORES      = {0,         0,         15,       20,        20,          10,          10,          0,          0,            10,     10,    10,    10,      10,      0};
    protected int      maxScore       = 0;
    protected int      thresholdScore = 50;
    
    protected static final String BGR = "bgr";
    protected static final String GR  = "gr";
    protected static final String BYW = "byw";
    protected static final String YW  = "yw";
    protected static final String DF  = "df";
    protected static final String RD  = "rd";
    protected static final String BL  = "bl";
    
    protected String[]          cCls;
    protected String[]          mCls;
    
    protected Vector<String>    toks       = new Vector<String>();
    protected char[]            seps       = {'`', '~', '|', '+'};
    
    protected ConversionLogger  convLogger = new ConversionLogger();
    protected SimpleDateFormat  sdf        = new SimpleDateFormat("yyyy-MM-dd");
    
    protected TableWriter       tblWriter  = null;
    
    // Database Connections
    protected Connection        dbGBIFConn = null;
    protected Connection        dbSrcConn  = null;
    protected Connection        dbDstConn  = null;
    protected Connection        dbLMConn   = null;

    
    /**
     * 
     */
    public AnalysisBase()
    {
        super();
        
        cCls = new String[titles.length];
        mCls = new String[titles.length];
        
        for (int i=0;i<titles.length;i++)
        {
            cCls[i] = null;
            mCls[i] = null;
        }
    }
    
    
    /**
     * @param baseDirName
     * @param dirName
     */
    public void startLogging(final String baseDirName, 
                             final String dirName,
                             final String fileName,
                             final String title)
    {
        convLogger.initialize(baseDirName, dirName);
        
        startNewDocument(fileName, title, true);
    }
    
    /**
     * @param baseDirName
     * @param dirName
     */
    public void startLogging(final String baseDirName, 
                             final String dirName,
                             final String fileName,
                             final String title,
                             final boolean includeTable)
    {
        convLogger.initialize(baseDirName, dirName);
        
        startNewDocument(fileName, title, includeTable);
    }
    
    /**
     * @return
     */
    protected String getStyle()
    {
        StringBuilder extraStyle = new StringBuilder();
        extraStyle.append("TD.yw  { color: rgb(200, 200, 0); }\n");   // Dark Yellow
        extraStyle.append("TD.gr  { color: rgb(200, 0, 200); }\n");   // Green
        extraStyle.append("TD.bgr { color: rgb(0, 255, 0); }\n");     // Bright Green
        extraStyle.append("TD.byw { color: yellow; }\n");           // Yellow
        extraStyle.append("TD.df  { color: rgb(100, 255, 200); }\n"); // Magenta
        extraStyle.append("TD.rd  { color: rgb(255, 0, 0); }\n");     // Red
        extraStyle.append("TD.bl  { color: rgb(0, 0, 0); }\n");       // Black
        extraStyle.append("TR.od  { background-color: rgb(240, 240, 240); }\n");       // white
        extraStyle.append("TR.ev  { background-color: rgb(232, 242, 254); }\n"); // light blue
        
        extraStyle.append("TR.odh { font-weight: bold; background-color: rgb(240, 240, 240); }\n"); // white
        extraStyle.append("TR.evh { font-weight: bold; background-color: rgb(232, 242, 254); }\n"); // light blue
        //extraStyle.append("TR.hd  { border }\n"); // light blue
        
        extraStyle.append(" TABLE.o tr.hd { border-top: solid 2px rgb(0, 0, 0); border-left: solid 1px rgb(128, 128, 128); }\n");
//        /extraStyle.append(" TABLE.o    { border-bottom: solid 1px rgb(128, 128, 128); border-right: solid 1px rgb(128, 128, 128); }\n");
        return extraStyle.toString();
    }
    
    /**
     * @param fileName
     * @param title
     */
    public void startNewDocument(final String fileName,
                                 final String title,
                                 final boolean includeTable)
    {
        if (tblWriter != null)
        {
            tblWriter.close();
        }
        
        tblWriter = convLogger.getWriter(fileName, title, getStyle());

        if (includeTable)
        {
            tblWriter.startTable();
            //tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
            tblWriter.logHdr(titles);
        }
    }

    
    /**
     * 
     */
    public void endLogging()
    {
        endLogging(false);
    }
    
    /**
     * 
     */
    public void endLogging(final boolean byOrderAdded)
    {
        File indexFile = convLogger.closeAll(byOrderAdded); 
        
        try
        {
            AttachmentUtils.openURI(indexFile.toURI());
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    
    /**
     * @param rs
     * @param colInx
     * @return
     * @throws SQLException
     */
    protected String getStr(final ResultSet rs, final int colInx) throws SQLException
    {
        Object obj = rs.getObject(colInx);
        return obj != null ? ((String)obj).trim() : null;
    }
    
    /**
     * @param options
     * @param opt
     * @return
     */
    protected static boolean isOn(final int options, final int opt)
    {
        return (options & opt) == opt;
    }
    
    /**
     * Converts string to an int or sets it to 0
     * @param str
     * @return
     */
    protected int getInt(final String str)
    {
        if (StringUtils.isNotEmpty(str) && StringUtils.isNumeric(str))
        {
            return Integer.parseInt(str);
        }
        return 0;
    }
    
    
    /**
     * @param cmpRow
     * @param gRS
     * @throws SQLException
     */
    protected void fillGBIF(final Object[] cmpRow, final ResultSet gRS) throws SQLException
    {
        //                        1       2               3        4         5         6          7         8           9               10          11       12    13    14      15
        //String srcSQL = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num " +
        
        cmpRow[CATNUM_INX]     = gRS.getString(2);
        cmpRow[COLNUM_INX]     = gRS.getString(15);
        cmpRow[GENUS_INX]      = gRS.getString(3);
        cmpRow[SPECIES_INX]    = gRS.getString(4);
        cmpRow[SUBSPECIES_INX] = gRS.getString(5);
        cmpRow[LOCALITY_INX]   = gRS.getString(11);
        cmpRow[LATITUDE_INX]   = gRS.getString(6);
        cmpRow[LONGITUDE_INX]  = gRS.getString(7);
        cmpRow[YEAR_INX]       = gRS.getString(12);
        cmpRow[MON_INX]        = gRS.getString(13);
        cmpRow[DAY_INX]        = gRS.getString(14);
        cmpRow[COUNTRY_INX]    = gRS.getString(8);
        cmpRow[STATE_INX]      = gRS.getString(9);
        cmpRow[COLLECTOR_INX] = gRS.getString(10);
    }
    
    /**
     * @param year
     * @param mon
     * @param day
     * @param refYear
     * @param refMon
     * @param refDay
     * @return
     */
    protected int compareDate(final String yearStr,    final String monStr,    final String dayStr,
                              final String refYearStr, final String refMonStr, final String refDayStr)
    {
        int year = getInt(yearStr);
        int mon  = getInt(monStr);
        int day  = getInt(dayStr);
        
        int refYear = getInt(refYearStr);
        int refMon  = getInt(refMonStr);
        int refDay  = getInt(refDayStr);
        
        int score = 0;
        
        if (year == refYear && mon == refMon && day == refDay && year != 0 && mon != 0 && day != 0)
        {
            score = 30;
            
        } else if (year == refYear && mon == refMon && year != 0 && mon != 0)
        {
            score = 20;
            
        } else if (year == refYear)
        {
            score = 10;
        }
        return score;
    }

    /**
     * @param year
     * @param mon
     * @param day
     * @param refYear
     * @param refMon
     * @param refDay
     * @return
     */
    protected int compareDate(final int year,    final int mon,    final int day,
                              final int refYear, final int refMon, final int refDay)
    {
        int score = 0;
        
        if (year == refYear && mon == refMon && day == refDay && year != 0 && mon != 0 && day != 0)
        {
            mCls[YEAR_INX] = BGR;
            mCls[MON_INX]  = BGR;
            mCls[DAY_INX]  = BGR;
            score = 30;
            
        } else if (year == refYear && mon == refMon && year != 0 && mon != 0)
        {
            mCls[YEAR_INX] = BGR;
            mCls[MON_INX]  = BGR;
            mCls[DAY_INX]  = RD;
            score = 20;
            
        } else if (year == refYear)
        {
            mCls[YEAR_INX] = BGR;
            mCls[MON_INX]  = RD;
            mCls[DAY_INX]  = RD;
            score = 10;
        }
        return score;
    }
    
    /**
     * @param refRow
     * @param compareRow
     * @return
     */
    protected void calcMaxScore()
    {
        maxScore = 0;
        for (int sc : MAXSCORES)
        {
            maxScore += sc;
        }
        thresholdScore = maxScore / 2;
    }
    
    /**
     * @param refRow
     * @param compareRow
     * @return
     */
    protected int score(final Object[] refRow, final Object[] compareRow)
    {
        //Calendar cal = Calendar.getInstance();
        
        if (maxScore == 0)
        {
            calcMaxScore();
        }
           
        String refCatNum       = (String)refRow[CATNUM_INX];
        String refCollectorNum = (String)refRow[COLNUM_INX];
        String refGenus        = (String)refRow[GENUS_INX];
        String refSpecies      = (String)refRow[SPECIES_INX];
        String refSubSpecies   = (String)refRow[SUBSPECIES_INX];
        String refCollector    = (String)refRow[COLLECTOR_INX];
        String refLocality     = (String)refRow[LOCALITY_INX];
        String refLatitude     = (String)refRow[LATITUDE_INX];
        String refLongitude    = (String)refRow[LONGITUDE_INX];
        String refYear         = (String)refRow[YEAR_INX];
        String refMon          = (String)refRow[MON_INX];
        String refDay          = (String)refRow[DAY_INX];
        String refCountry      = (String)refRow[COUNTRY_INX];
        String refState        = (String)refRow[STATE_INX];
        
        String catNum       = (String)compareRow[CATNUM_INX];
        String collectorNum = (String)compareRow[COLNUM_INX];
        String genus        = (String)compareRow[GENUS_INX];
        String species      = (String)compareRow[SPECIES_INX];
        String subSpecies   = (String)compareRow[SUBSPECIES_INX];
        String collector    = (String)compareRow[COLLECTOR_INX];
        String locality     = (String)compareRow[LOCALITY_INX];
        String latitude     = (String)compareRow[LATITUDE_INX];
        String longitude    = (String)compareRow[LONGITUDE_INX];
        String year         = (String)compareRow[YEAR_INX];
        String mon          = (String)compareRow[MON_INX];
        String day          = (String)compareRow[DAY_INX];
        String country      = (String)compareRow[COUNTRY_INX];
        String state        = (String)compareRow[STATE_INX];
        
        if (country != null && country.toLowerCase().equals("mexico"))
        {
            country = "México";
        }
        
        /*
        String refCollector = null;
        switch (numFieldForName)
        {
            case 1 :
                refLastNameF  = getStr(gRS, inx++);
                refCollector  = refLastNameF;
                break;
                
            case 2 :
                refLastNameF  = getStr(gRS, inx++);
                refFirstName  = getStr(gRS, inx++);
                refCollector = String.format("%s %s", (StringUtils.isNotEmpty(refLastNameF) ? refLastNameF : ""), (StringUtils.isNotEmpty(refFirstName) ? refFirstName : "")).trim();
                break;
                
            case 3 :
                refLastNameF  = getStr(gRS, inx++);
                refLastNameM  = getStr(gRS, inx++);
                refFirstName  = getStr(gRS, inx++);
                refCollector = String.format("%s %s %s", (StringUtils.isNotEmpty(refLastNameF) ? refLastNameF : ""), (StringUtils.isNotEmpty(refLastNameM) ? refLastNameM : ""), (StringUtils.isNotEmpty(refFirstName) ? refFirstName : "")).trim();
                break;
        }
        */
        
        
        if (refCountry != null && refCountry.toLowerCase().equals("mexico"))
        {
            refCountry = "México";
        }
        
        int score = 0;
        
         score += compareDate(year, mon, day, refYear, refMon, refDay);
        
        double ratingLoc   = longStringCompare(refLocality,  locality, false);
        double ratingColtr = longStringCompare(refCollector, collector, false);
        
        if (ratingLoc > 50.0)
        {
            score += 10;
            
        } else if (ratingLoc > 30.0)
        {
            score += 6;
            
        } else if (ratingLoc > 0.0)
        {
            score += 3;
        }
        
        if (ratingColtr > 50.0)
        {
            score += 10;
            
        } else if (ratingColtr > 30.0)
        {
            score += 6;
            
        } else if (ratingColtr > 0.0)
        {
            score += 3;
        }

        
        if (refGenus != null && genus != null)
        {
            if (refGenus.equals(genus))
            {
                score += 15;
                
            } else if (StringUtils.getLevenshteinDistance(genus, refGenus) < 3)
            {
                score += 7;
            }
        }
        
        if (refSpecies != null && species != null) 
        {
            if (refSpecies.equals(species))
            {
                score += 20;

            } else if (StringUtils.getLevenshteinDistance(species, refSpecies) < 3)
            {
                score += 10;
            }
        }
        
        if (refSubSpecies != null && subSpecies != null) 
        {
            if (refSubSpecies.equals(subSpecies))
            {
                score += 20;
                
            } else if (StringUtils.getLevenshteinDistance(subSpecies, refSubSpecies) < 3)
            {
                score += 10;
            }
        }
        
        if (refCountry != null && country != null) 
        {
            if (refCountry.equals(country))
            {
                score += 10;
                
            } else if (StringUtils.getLevenshteinDistance(country, refCountry) < 3)
            {
                score += 5;
            }
        }
        
        /*if (refGenus != null && species != null && refGenus.equals(species))     
        {
            cCls[3] = DF;
            mCls[2] = DF;                        
            score += 10;
        }
        
        if (refSpecies != null && genus != null && refSpecies.equals(genus))
        {
            cCls[2] = DF;
            mCls[3] = DF;
            score += 15;
        }*/
        
        if (collectorNum != null && refCollectorNum != null && collectorNum.equals(refCollectorNum))
        {
            score += 10;
        }
        
        return score;
    }
    
    /**
     * @param str
     * @param usePeriods
     * @return
     */
    protected String cleanString(final String str, final boolean usePeriods)
    {
        String s = StringUtils.remove(str, ':');
        
        if (!usePeriods) s = StringUtils.remove(s, '.');
        
        s = StringUtils.remove(s, '-');
        s = StringUtils.remove(s, ',');
        return s;
    }
    
    /**
     * @param str1
     * @param str2
     * @param usePeriods
     * @return
     */
    protected double longStringCompare(final String str1, final String str2, final boolean usePeriods)
    {
        if (str1 == null || str2 == null) return 0.0;
        
        String longStr;
        String shortStr;
        
        if (str1.length() > str2.length())
        {
            longStr  = str1;
            shortStr = str2;
        } else
        {
            longStr  = str2;
            shortStr = str1;
        }
        
        longStr  = cleanString(longStr, usePeriods);
        shortStr = cleanString(shortStr, usePeriods);
        
        int score = 0;
        String[] longToks  = StringUtils.split(longStr, " ");
        String[] shortToks = StringUtils.split(shortStr, " ");
        for (String sStr : shortToks)
        {
            for (String lStr : longToks)
            {
                if (lStr.equals(sStr) || (sStr.length() > 2 && StringUtils.getLevenshteinDistance(lStr, sStr) < 3)) score += 1;
            }
        }
        
        double rating = (double)score / (double)shortToks.length * 100.0;
        //System.out.println(" L: "+shortToks.length+"  Sc: "+score+"  Rating: "+String.format("%5.2f", rating));
        
        return rating;
    }
    
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    protected Connection createConnection(final String server, 
                                          final String port, 
                                          final String dbName, 
                                          final String username, 
                                          final String pwd)
    {
        try
        {
            return DriverManager.getConnection(String.format(CONN_STR, server, port, dbName), username, pwd);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
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
                                   final String pwd)
    {
        dbGBIFConn = createConnection(server, port, dbName, username, pwd);
    }
        
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createSrcDBConnection(final String server, 
                                           final String port, 
                                           final String dbName, 
                                           final String username, 
                                           final String pwd)
    {
        dbSrcConn = createConnection(server, port, dbName, username, pwd);
    }
        
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createDestDBConnection(final String server, 
                                      final String port, 
                                      final String dbName, 
                                      final String username, 
                                      final String pwd)
    {
        dbDstConn = createConnection(server, port, dbName, username, pwd);
    }
        
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public void createLMDBConnection(final String server, 
                                      final String port, 
                                      final String dbName, 
                                      final String username, 
                                      final String pwd)
    {
        dbLMConn = createConnection(server, port, dbName, username, pwd);
    }
        
    
    /**
     * 
     */
    public abstract void process(final int type, final int options);

    
    /**
     * 
     */
    public void cleanup()
    {
        try
        {
            if (dbGBIFConn != null)
            {
                dbGBIFConn.close();
            }
            if (dbSrcConn != null)
            {
                dbSrcConn.close();
            }
            if (dbDstConn != null)
            {
                dbDstConn.close();
            }
            if (dbLMConn != null)
            {
                dbLMConn.close();
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @return the dbConn
     */
    public Connection getGBIFDBConn()
    {
        return dbGBIFConn;
    }

    /**
     * @return the srcDBConn
     */
    public Connection getSrcDBConn()
    {
        return dbSrcConn;
    }

    /**
     * @return the dbDstConn
     */
    public Connection getDstDBConn()
    {
        return dbDstConn;
    }

    /**
     * @return the dbDstConn
     */
    public Connection getLMDBConn()
    {
        return dbLMConn;
    }

}
