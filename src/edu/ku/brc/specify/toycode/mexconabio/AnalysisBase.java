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

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.util.AttachmentUtils;

public class AnalysisBase
{
    protected final double        HRS = 1000.0 * 60.0 * 60.0;
    protected final Calendar      cal = Calendar.getInstance();
    protected final StringBuilder sb  = new StringBuilder();
    
    public    static final int NUM_FIELDS = 16;
    
    public static final int COLNUM_INX     = 0;
    public static final int CATNUM_INX     = 1;
    public static final int GENUS_INX      = 2;
    public static final int SPECIES_INX    = 3;
    public static final int SUBSPECIES_INX = 4;
    public static final int COLLECTOR_INX  = 5;
    public static final int LOCALITY_INX   = 6;
    public static final int LATITUDE_INX   = 7;
    public static final int LONGITUDE_INX  = 8;
    public static final int YEAR_INX       = 9;
    public static final int MON_INX        = 10;
    public static final int DAY_INX        = 11;
    public static final int COUNTRY_INX    = 12;
    public static final int STATE_INX      = 13;
    public static final int INST_INX       = 14;
    public static final int SCORE_INX      = 15;
    
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
    
//                                                 1          2           3       4              5               6         7          8           9               10           11       12       13        14     15       16         17               18
    public static final String snibSQL = "SELECT IdSNIB, CatalogNumber, Genus, Species, Cataegoryinfraspecies, Latitude, Longitude, Country, LastNameFather, LastNameMother, FirstName, State, Locality, `Year`, `Month`, `Day`, CollectorNumber, InstitutionAcronym "; 
    
//                                                1         2           3        4         5           6         7          8          9               10            11      12    13    14        15               16
    public static final String gbifSQL = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num, institution_code ";
    
//                                                 1       2           3              4           5             6              7               8           9       10   11
    public static final String michSQL = "SELECT BarCD, CollNr, Collectoragent1, GenusName, SpeciesName, SubSpeciesName, LocalityName, Datecollstandrd, COUNTRY, STATE, ID ";

    
    public static final  String CONN_STR = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
    
    protected String[] titles         = {"Collector<BR>Number", "Catalog<BR>Number", "Genus", "Species", "SubSpecies", "Collector", "Locality", "Latitude", "Longitude", "Year", "Mon", "Day", "Country", "State", "Institution<BR>Code", "Matching<BR>Score"};
    protected int[]    MAXSCORES      = {0,         0,         15,       20,        20,          10,          10,          0,          0,            10,     10,    10,    10,      10,      0};
    protected int      maxScore       = 0;
    protected int      thresholdScore = 50;
    
    protected static String BGR = "bgr";
    protected static String GR  = "gr";
    protected static String BYW = "byw";
    protected static String YW  = "yw";
    protected static String DF  = "df";
    protected static String RD  = "rd";
    protected static String BL  = "bl";
    
    protected String[]          tdColorCodes;
    
    protected Vector<String>    toks       = new Vector<String>();
    protected char[]            seps       = {'`', '~', '|', '+'};
    
    protected ConversionLogger  convLogger = new ConversionLogger();
    protected SimpleDateFormat  sdf        = new SimpleDateFormat("yyyy-MM-dd");
    
    protected TableWriter       tblWriter  = null;
    
    protected String[]           strNulls  = new String[NUM_FIELDS];
    protected String[]           objNulls  = new String[NUM_FIELDS];
    
    protected HashMap<Integer, String> instHashMap = new HashMap<Integer, String>();
    
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
        
        tdColorCodes = new String[NUM_FIELDS];
        
        for (int i=0;i<NUM_FIELDS;i++)
        {
            strNulls[i] = null;
            objNulls[i] = null;
        }
        System.arraycopy(strNulls, 0, tdColorCodes, 0, NUM_FIELDS);
    }
    
    
    /**
     * @return the tdColorCodes
     */
    public String[] getTDColorCodes()
    {
        return tdColorCodes;
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
     * @param baseDirName
     * @param dirName
     */
    public void startLogging(final String baseDirName, 
                             final String dirName,
                             final String fileName,
                             final String title,
                             final boolean includeTable,
                             final Integer width)
    {
        convLogger.initialize(baseDirName, dirName);
        
        startNewDocument(fileName, title, includeTable, width);
    }
    
    /**
     * @return the maxScore
     */
    public int getMaxScore()
    {
        return maxScore;
    }

    /**
     * @return the rowScore
     */
    public static int getRowScore()
    {
        return rowScore;
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
        startNewDocument(fileName, title, includeTable, null);
    }

    /**
     * @param fileName
     * @param title
     */
    public void startNewDocument(final String fileName,
                                 final String title,
                                 final boolean includeTable,
                                 final Integer width)
    {
        if (tblWriter != null)
        {
            tblWriter.close();
        }
        
        tblWriter = convLogger.getWriter(fileName, title, getStyle());

        if (includeTable)
        {
            if (width == null)
            {
                tblWriter.startTable();
            } else
            {
                tblWriter.startTable(width);
            }
            //tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TretrieveInstitutionNamesH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
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
            e.printStackTrace();
        }
    }
    
    
    /**
     * 
     */
    protected void retrieveInstitutionNames()
    {
        Connection conn = null;
        try
        {
            conn = createConnection("lm2gbdb.nhm.ku.edu", "3399", "gbc20100726", "rods", "specify4us");
            
            System.out.println("Getting Institutions");
            for (Object[] row : BasicSQLUtils.query(conn, "SELECT data_provider_id, name FROM data_provider"))
            {
                instHashMap.put((Integer)row[0], (String)row[0]);
            }
            System.out.println("Done Getting Institutions");
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (conn != null)
            {
                try
                {
                    conn.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
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
    
    
    static int rowScore = 0;
    private Object cntObject(final Object obj)
    {
        if (obj != null)
        {
            rowScore++;
        }
        return obj;
    }
    
    /**
     * 
     */
    protected void clearRowAttrs(final Object[] row)
    {
        System.arraycopy(strNulls, 0, tdColorCodes, 0, NUM_FIELDS);
        
        if (row != null)
        {
            System.arraycopy(objNulls, 0, row, 0, NUM_FIELDS);
        }
    }
    
    
    /**
     * @param val
     * @return
     */
    protected String getIntToStr(final Object val)
    {
        if (val != null && val instanceof Integer)
        {
            return Integer.toString((Integer)val);
        }
        return null;
    }
    
    /**
     * 
     */
    public void clearRowAttrs()
    {
        clearRowAttrs(null);
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
     * @param rs
     * @throws SQLException 
     */
    public void fillMichRow(final Object[] refRow, final ResultSet rs) throws SQLException
    {
        String  catNum       = rs.getString(1);
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
        refRow[INST_INX]       = "MICH";
        
        for (int i=COLNUM_INX;i<SCORE_INX;i++)
        {
            if (refRow[i] != null)
            {
                refRow[i] = ((String)refRow[i]).trim();
            }
        }
    }
    
    /**
     * @param cmpRow
     * @param rs
     * @throws SQLException
     */
    public void fillSNIBRow(final Object[] cmpRow, final ResultSet rs) throws SQLException
    {
        //   1          2           3        4                5             6         7          8          9               10            11       12       13        14     15       16         17                18
        // IdSNIB, CatalogNumber, Genus, Species, Cataegoryinfraspecies, Latitude, Longitude, Country, LastNameFather, LastNameMother, FirstName, State, Locality, `Year`, `Month`, `Day`, CollectorNumber, InstitutionAcronym FROM angiospermas "; 
        
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
        cmpRow[STATE_INX]      = rs.getString(12);
        cmpRow[INST_INX]       = rs.getString(18);
        
        String fatherName      = rs.getString(9);
        String motherName      = rs.getString(10);
        String firstName       = rs.getString(11);
        
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
        
        for (int i=COLNUM_INX;i<SCORE_INX;i++)
        {
            if (cmpRow[i] != null)
            {
                cmpRow[i] = ((String)cmpRow[i]).trim();
            }
        }
    }
    
    /**
     * @param cmpRow
     * @param rs
     * @throws SQLException
     */
    public void fillGBIFRow(final Object[] cmpRow, final ResultSet rs) throws SQLException
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
        
        String instCode        = rs.getString(16);
        
        cmpRow[INST_INX] = (StringUtils.isNotEmpty(instCode) && instCode.length() > 8) ? instCode.substring(0, 8) : instCode;
        
        for (int i=COLNUM_INX;i<SCORE_INX;i++)
        {
            if (cmpRow[i] != null)
            {
                cmpRow[i] = ((String)cmpRow[i]).trim();
            }
        }
        
        if (cmpRow[YEAR_INX] != null && StringUtils.isNotEmpty((String)cmpRow[YEAR_INX]))
        {
            cmpRow[MON_INX] = null;
        }
        if (cmpRow[MON_INX] != null && StringUtils.isNotEmpty((String)cmpRow[MON_INX]))
        {
            cmpRow[YEAR_INX] = null;
        }
        if (cmpRow[DAY_INX] != null && StringUtils.isNotEmpty((String)cmpRow[DAY_INX]))
        {
            cmpRow[DAY_INX] = null;
        }
    }
    

    
    /**
     * @param cmpRow
     * @param gRS
     * @throws SQLException
     */
    public int fillRowWithScore(final Object[] cmpRow, final ResultSet gRS) throws SQLException
    {
        //                        1       2               3        4         5         6          7         8           9               10          11       12    13    14      15
        //String srcSQL = "SELECT id, catalogue_number, genus, species, subspecies, latitude, longitude, country, state_province, collector_name, locality, year, month, day, collector_num " +
        
        rowScore = 0;
        cmpRow[CATNUM_INX]     = cntObject(gRS.getString(2));
        cmpRow[COLNUM_INX]     = cntObject(gRS.getString(15));
        cmpRow[GENUS_INX]      = cntObject(gRS.getString(3));
        cmpRow[SPECIES_INX]    = cntObject(gRS.getString(4));
        cmpRow[SUBSPECIES_INX] = cntObject(gRS.getString(5));
        cmpRow[LOCALITY_INX]   = cntObject(gRS.getString(11));
        cmpRow[LATITUDE_INX]   = cntObject(gRS.getString(6));
        cmpRow[LONGITUDE_INX]  = cntObject(gRS.getString(7));
        cmpRow[YEAR_INX]       = cntObject(gRS.getString(12));
        cmpRow[MON_INX]        = cntObject(gRS.getString(13));
        cmpRow[DAY_INX]        = cntObject(gRS.getString(14));
        cmpRow[COUNTRY_INX]    = cntObject(gRS.getString(8));
        cmpRow[STATE_INX]      = cntObject(gRS.getString(9));
        cmpRow[COLLECTOR_INX] = cntObject(gRS.getString(10));
        
        for (int i=COLNUM_INX;i<SCORE_INX;i++)
        {
            if (cmpRow[i] != null)
            {
                cmpRow[i] = ((String)cmpRow[i]).trim();
            }
        }
        
        if (cmpRow[COLNUM_INX] != null)
        {
            rowScore += 15;
        }
        if (cmpRow[GENUS_INX] != null)
        {
            rowScore += 10;
        }
        if (cmpRow[SPECIES_INX] != null)
        {
            rowScore += 10;
        }
        if (cmpRow[COUNTRY_INX] != null)
        {
            rowScore += 8;
        }
        if (cmpRow[YEAR_INX] != null)
        {
            rowScore += 10;
        }
        if (cmpRow[MON_INX] != null)
        {
            rowScore += 5;
        }
        
        return rowScore;
    }
    
    /**
     * @param row
     * @param scores
     * @param codes
     */
    protected void colorCode(final Object[] row, final int[] scores, final String[] codes)
    {
        
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
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = BGR;
            tdColorCodes[DAY_INX]  = BGR;
            
        } else if (year == refYear && mon == refMon && year != 0 && mon != 0)
        {
            score = 20;
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = BGR;
            tdColorCodes[DAY_INX]  = RD;
            
        } else if (year == refYear)
        {
            score = 10;
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = RD;
            tdColorCodes[DAY_INX]  = RD;
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
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = BGR;
            tdColorCodes[DAY_INX]  = BGR;
            score = 30;
            
        } else if (year == refYear && mon == refMon && year != 0 && mon != 0)
        {
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = BGR;
            tdColorCodes[DAY_INX]  = RD;
            score = 20;
            
        } else if (year == refYear)
        {
            tdColorCodes[YEAR_INX] = BGR;
            tdColorCodes[MON_INX]  = RD;
            tdColorCodes[DAY_INX]  = RD;
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
    public int score(final Object[] refRow, final Object[] compareRow)
    {
        //Calendar cal = Calendar.getInstance();
        
        if (maxScore == 0)
        {
            calcMaxScore();
        }
           
        //String refCatNum       = (String)refRow[CATNUM_INX];
        String refCollectorNum = (String)refRow[COLNUM_INX];
        String refGenus        = (String)refRow[GENUS_INX];
        String refSpecies      = (String)refRow[SPECIES_INX];
        String refSubSpecies   = (String)refRow[SUBSPECIES_INX];
        String refCollector    = (String)refRow[COLLECTOR_INX];
        String refLocality     = (String)refRow[LOCALITY_INX];
        //String refLatitude     = (String)refRow[LATITUDE_INX];
        //String refLongitude    = (String)refRow[LONGITUDE_INX];
        String refYear         = (String)refRow[YEAR_INX];
        String refMon          = (String)refRow[MON_INX];
        String refDay          = (String)refRow[DAY_INX];
        String refCountry      = (String)refRow[COUNTRY_INX];
        String refState        = (String)refRow[STATE_INX];
        
        //String catNum       = (String)compareRow[CATNUM_INX];
        String collectorNum = (String)compareRow[COLNUM_INX];
        String genus        = (String)compareRow[GENUS_INX];
        String species      = (String)compareRow[SPECIES_INX];
        String subSpecies   = (String)compareRow[SUBSPECIES_INX];
        String collector    = (String)compareRow[COLLECTOR_INX];
        String locality     = (String)compareRow[LOCALITY_INX];
        //String latitude     = (String)compareRow[LATITUDE_INX];
        //String longitude    = (String)compareRow[LONGITUDE_INX];
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
        
        if (StringUtils.isNotEmpty(refCollector))
        {
            refCollector = StringUtils.replaceChars(refCollector, ',', ' ');
        }
        if (StringUtils.isNotEmpty(collector))
        {
            collector = StringUtils.replaceChars(collector, ',', ' ');
        }
        
        int score = 0;
        
        score += compareDate(year, mon, day, refYear, refMon, refDay);
        
        String refCollectorStr = refCollector != null ? refCollector.toLowerCase() : "";
        String collectorStr    = collector != null ? collector.toLowerCase() : "";
        String refLocStr       = refLocality != null ? refLocality.toLowerCase() : "";
        String locStr          = locality != null ? locality.toLowerCase() : "";
        
        double ratingLoc   = longStringCompare(refLocStr,  locStr, false, true);
        double ratingColtr = longStringCompare(refCollectorStr, collectorStr, false, true);
        
        if (ratingLoc > 50.0)
        {
            score += 10;
            tdColorCodes[LOCALITY_INX] = BGR;
            
        } else if (ratingLoc > 30.0)
        {
            score += 6;
            tdColorCodes[LOCALITY_INX] = GR;
            
        } else if (ratingLoc > 0.0)
        {
            score += 3;
            tdColorCodes[LOCALITY_INX] = YW;
        }
        
        if (ratingColtr > 50.0)
        {
            score += 10;
            tdColorCodes[COLLECTOR_INX] = BGR;
            
        } else if (ratingColtr > 30.0)
        {
            score += 6;
            tdColorCodes[COLLECTOR_INX] = GR;
            
        } else if (ratingColtr > 0.0)
        {
            score += 3;
            tdColorCodes[COLLECTOR_INX] = YW;
        }

        boolean genusMatches = false;
        if (refGenus != null && genus != null)
        {
            if (refGenus.equals(genus))
            {
                score += 15;
                genusMatches = true;
                tdColorCodes[GENUS_INX] = GR;
                
            } else if (StringUtils.getLevenshteinDistance(genus, refGenus) < 3)
            {
                score += 7;
                tdColorCodes[GENUS_INX] = YW;
            }
        }
        
        boolean speciesMatches = false;
        if (refSpecies != null && species != null) 
        {
            if (refSpecies.equals(species))
            {
                score += 20;
                speciesMatches = true;
                if (genusMatches)
                {
                    tdColorCodes[GENUS_INX]   = BGR;
                    tdColorCodes[SPECIES_INX] = BGR;
                } else
                {
                    tdColorCodes[SPECIES_INX] = GR;
                }
            } else if (StringUtils.getLevenshteinDistance(species, refSpecies) < 3)
            {
                score += 10;
                tdColorCodes[SPECIES_INX] = YW;
            }
        }
        
        if (refSubSpecies != null && subSpecies != null) 
        {
            if (refSubSpecies.equals(subSpecies))
            {
                score += 20;
                if (genusMatches && speciesMatches)
                {
                    tdColorCodes[GENUS_INX]      = BGR;
                    tdColorCodes[SPECIES_INX]    = BGR;
                    tdColorCodes[SUBSPECIES_INX] = BGR;
                    
                } else if (speciesMatches)
                {
                    tdColorCodes[SPECIES_INX]    = BGR;
                    tdColorCodes[SUBSPECIES_INX] = BGR;
                } else
                {
                    tdColorCodes[SUBSPECIES_INX] = GR;
                }
                
            } else if (StringUtils.getLevenshteinDistance(subSpecies, refSubSpecies) < 3)
            {
                score += 10;
                tdColorCodes[SUBSPECIES_INX] = YW;
            }
        }
        
        if (refCountry != null && country != null) 
        {
            refCountry = refCountry.toLowerCase();
            country    = country.toLowerCase();
            
            if (refCountry.equals(country))
            {
                score += 10;
                tdColorCodes[COUNTRY_INX] = BGR;
                
            } else if (StringUtils.getLevenshteinDistance(country, refCountry) < 3)
            {
                score += 5;
                tdColorCodes[COUNTRY_INX] = YW;
            }
        }
        
        if (refState != null && state != null) 
        {
            refState = refState.toLowerCase();
            state    = state.toLowerCase();

            if (refState.equals(state))
            {
                score += 10;
                tdColorCodes[STATE_INX] = BGR;
                
            } else if (StringUtils.getLevenshteinDistance(state, refState) < 3)
            {
                score += 5;
                tdColorCodes[STATE_INX] = YW;
            }
        }
        
        /*if (refGenus != null && species != null && refGenus.equals(species))     
        {
            cCls[3] = DF;
            cCls[2] = DF;                        
            score += 10;
        }
        
        if (refSpecies != null && genus != null && refSpecies.equals(genus))
        {
            cCls[2] = DF;
            cCls[3] = DF;
            score += 15;
        }*/
        
        if (collectorNum != null && refCollectorNum != null && collectorNum.equals(refCollectorNum))
        {
            score += 10;
            tdColorCodes[COLNUM_INX] = BGR;
        }
        
        return score;
    }
    
    /**
     * @param str
     * @param usePeriods
     * @return
     */
    protected String cleanString(final String str, 
                                 final boolean usePeriods,
                                 final boolean replaceWithSpaces)
    {
        String s = StringUtils.remove(str, ':');
        
        if (!usePeriods)
        {
            if (replaceWithSpaces)
            {
                s = StringUtils.replaceChars(s, '.', ' ');
            } else
            {
                s = StringUtils.remove(s, '.');
            }
        }
        
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
    protected double longStringCompare(final String str1, 
                                       final String str2, 
                                       final boolean usePeriods,
                                       final boolean replaceWithSpaces)
    {
        if (str1 == null || str2 == null) return 0.0;
        
        String longStr;
        String shortStr;
        
        if (str1.length() >= str2.length())
        {
            longStr  = str1;
            shortStr = str2;
        } else
        {
            longStr  = str2;
            shortStr = str1;
        }
        
        longStr  = cleanString(longStr, usePeriods, replaceWithSpaces);
        shortStr = cleanString(shortStr, usePeriods, replaceWithSpaces);
        
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
     * Sets color for JTable
     */
    public void setColorsForJTable()
    {
        BGR = "0, 100, 0";
        GR  = "200, 0, 200";
        BYW = "255, 255, 0";
        YW  = "184, 134, 11"; // Dark Yellow (Golden Rod)
        DF  = "100, 255, 200";
        RD  = "255, 0, 0";
        BL  = "128, 128, 128";
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
    public void process(final int type, final int options)
    {
        
    }

    
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
