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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.ConversionLogger;
import edu.ku.brc.specify.conversion.TableWriter;
import edu.ku.brc.specify.toycode.mexconabio.FieldDef.DataType;
import edu.ku.brc.util.AttachmentUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 8, 2010
 *
 */
public class MexConvToSQL
{
    private static final String BGR = "bgr";
    private static final String GR  = "gr";
    private static final String BYW = "byw";
    private static final String YW  = "yw";
    private static final String DF  = "df";
    
    protected Vector<String> toks = new Vector<String>();
    protected char[] seps = {'`', '~', '|', '+'};
    
    private PreparedStatement pStmt      = null;
    private ConversionLogger  convLogger = new ConversionLogger();
    private PreparedStatement mpStmt     = null;//oldConn.prepareStatement("INSERT INTO "+mapTableName+" VALUES (?,?)");
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * 
     */
    public MexConvToSQL()
    {
        super();
    }

    
    /**
     * @param line
     * @return
     */
    private Vector<String> split(final String line, final char c)
    {
        toks.clear();
        
        String[] tokens = StringUtils.splitPreserveAllTokens(line, c);
        for (String s : tokens)
        {
            toks.add(s);
        }
        return toks;
    }
    
    /**
     * @param fmpInfo
     * @param srcFileName
     * @throws UnsupportedEncodingException
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void processFieldSizes(final FMPCreateTable fmpInfo, final String srcFileName) throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        Vector<FieldDef>  fieldDefs = fmpInfo.getFields();
        BufferedReader    in        = new BufferedReader(new InputStreamReader(new FileInputStream(new File(srcFileName)), "UTF8"));
        String            str       = in.readLine(); // header row
        
        int recNumber =  41477000;
        str = in.readLine();
        int rowCnt = 0;
        while (str != null)
        {
            
            String line = str;
            
            char   sep    = '`';
            String sepStr = "";
            for (char c : seps)
            {
                if (line.indexOf(c) == -1)
                {
                    sepStr += c;
                    sep = c;
                    break;
                }
            }
            
            /*if (rowCnt >= recNumber) 
            {
                System.out.println("Num Commas: "+StringUtils.countMatches(str, ","));
                System.out.println(line);
            }*/
                    
            str = StringUtils.replace(str.substring(1, str.length()-1), "\",\"", sepStr);
            /*if (rowCnt >= recNumber) 
            {
                System.out.println("Num pipe: "+StringUtils.countMatches(str, "|"));
                System.out.println(str);
            }*/
            
            if (rowCnt >= recNumber)
            {
                Vector<String> fields = split(str, '|');
                Vector<String> fields2 = new Vector<String>(split(str, sep));
                for (int x=0;x<fields.size();x++)
                {
                    if ((StringUtils.isNotEmpty(fields.get(x)) && StringUtils.isNotEmpty(fields2.get(x))) || (x >39 && x < 51))
                    {
                        System.out.println(x+"  ["+ fields.get(x)+"]["+fields2.get(x)+"]");
                    }
                }
                System.out.println(line);
                System.out.println(str);
            }
            
            Vector<String> fields = split(str, sep);
            
            if (fields.size() != fieldDefs.size()-1)
            {
                System.out.println(rowCnt+"  -  "+fields.size()+" != "+(fieldDefs.size()-1));
                int i = 0;
                for (String value : fields)
                {
                    System.out.println(i+" ["+value+"]");
                    i++;
                }
                System.out.println(line);
            }
            int inx = 1;
            for (String value : fields)
            {
                FieldDef fd  = fieldDefs.get(inx);
                int      len = value.length() + 1;
                fd.setMaxSize(len);
                
                if (fd.getType() == DataType.eNumber)
                {
                    if (value.contains("."))
                    {
                        fd.setDouble(true);
                    }
                }
                inx++;
            }
            
            rowCnt++;
            if (rowCnt % 1000 == 0) System.out.println(rowCnt);
            str = in.readLine();
        }
        in.close();
    }
    
    /**
     * @param databaseName
     */
    protected void mapTable(final Connection conn,
                            final String databaseName, 
                            final String mapTableName)
    {
        String sql = String.format("SELECT COUNT(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` WHERE TABLE_SCHEMA = '%s' AND " +
                                   "TABLE_NAME = '%s'", databaseName, mapTableName);
        System.out.println(sql);
        
        int count = BasicSQLUtils.getCountAsInt(conn, sql);
        if (count > 0)
        {
            BasicSQLUtils.update(conn, "DROP TABLE "+mapTableName);
        }
        
        sql = "CREATE TABLE `"+mapTableName+"` ("+
                "`OldID` int(11) NOT NULL default '0', "+
                "`NewID` int(11) NOT NULL default '0', "+
                " PRIMARY KEY (`OldID`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1";
        
        BasicSQLUtils.update(conn, sql);
        
        sql = "ALTER TABLE "+mapTableName+" ADD INDEX INX_"+mapTableName+" (NewID)";
        BasicSQLUtils.update(conn, sql);
    }
    
    
    int      fndCnt  = 0;
    int      numRecs = 0;
    Calendar cal     = Calendar.getInstance();
    
    String[] cCls = new String[] {null, null, null, null, null, null, null, };
    String[] mCls = new String[] {null, null, null, null, null, null, null, };
    
    int[]      histo      = new int[101];
    Connection conn       = null;
    int        totalScore = 0;

    
    /**
     * 
     */
    public void process(final String databaseName)
    {
        boolean doingMapTable = false;
        
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        for (int i=0;i<histo.length;i++)
        {
            histo[i] = 0;
        }
        
        convLogger.initialize("mich_conabio", databaseName);
        
        StringBuilder extraStyle = new StringBuilder();
        extraStyle.append("TD.yw  { color: rgb(200,200,0); }\n");
        extraStyle.append("TD.gr  { color: rgb(200,0,200); }\n");
        extraStyle.append("TD.bgr { color: rgb(0,255,0); }\n");
        extraStyle.append("TD.byw { color: yellow; }\n");
        extraStyle.append("TD.df  { color: rgb(100,255,200); }\n");
        
        String mapTableName = "maptable";
        
        Statement  stmt = null;
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+databaseName+"?characterEncoding=UTF-8&autoReconnect=true", "root", "root");
            BasicSQLUtils.setDBConnection(conn);
            
            int matches = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM maptable");
            //tblWriter.log(String.format("Number of records that match - Genus, Species, CollectorNumber, CatalogNumber: "+matches));
            
            mpStmt   = conn.prepareStatement("INSERT INTO "+mapTableName+" VALUES (?,?)");

            String sqlMain = "SELECT E.IdEjemplar, E.NumeroDeCatalogo, NumeroDeColecta, Grupo.DescripcionGpo AS Colector, " +
                            "NombreLocalidad.NombreOriginal, CategoriaTaxonomica.NombreCategoriaTaxonomica, IF (CategoriaTaxonomica.IdNivel1<7,Nombre.Nombre, " +
                            "IF (CategoriaTaxonomica.IdNivel3 = 0, CONCAT(Nombre_1.Nombre,\" \", Nombre.Nombre),CONCAT(Nombre_2.Nombre, \" \",Nombre_1.Nombre,\" \", " +
                            "Nombre.Nombre))) AS Nombre, Nombre_2.Nombre AS N2, Nombre_1.Nombre AS N1, Nombre.Nombre AS N0, " +
                            "AnioColecta, MesColecta, DiaColecta FROM Grupo " +
                            "INNER JOIN (CategoriaTaxonomica INNER JOIN (NombreLocalidad " +
                            "INNER JOIN (((Nombre INNER JOIN Nombre AS Nombre_1 ON Nombre.IdAscendObligatorio = Nombre_1.IdNombre) " +
                            "INNER JOIN Nombre AS Nombre_2 ON Nombre_1.IdAscendObligatorio = Nombre_2.IdNombre) " +
                            "INNER JOIN Ejemplar E ON Nombre.IdNombre = E.IdNombre) ON NombreLocalidad.IdNombreLocalidad = E.IdNombreLocalidad) " +
                            "ON CategoriaTaxonomica.IdCategoriaTaxonomica = Nombre.IdCategoriaTaxonomica) ON Grupo.IdGrupo = E.IdColector " +
                            "ORDER BY E.NumeroDeCatalogo ";
            
            String sql = sqlMain;
            //if (!doingMapTable)
            //{
            //    sql = "SELECT * FROM (" + sqlMain + ") T1 LEFT JOIN maptable ON IdEjemplar = OldID WHERE NewID IS NULL";
            //}
            
            TableWriter tblWriter = convLogger.getWriter("MichConabio1.html", "Matches Cat No. / Collector No. / Genus / Species", extraStyle.toString());

            tblWriter.startTable();
            tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
            tblWriter.logHdr("Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Score");
            fndCnt  = 0;
            numRecs = 0;
            System.out.println(sql);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
            
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                process(0, rs, tblWriter, doingMapTable);
            }
            rs.close();
            tblWriter.endTable();
            tblWriter.log("<BR>");
            tblWriter.log(String.format("Number of records that match: %d  of %d records.", fndCnt, numRecs));
            tblWriter.log(String.format("Average Score: %5.2f", ((double)totalScore / (double)fndCnt)));
            tblWriter.log(String.format("Mode Score: %d", getModeScore()));
            totalScore = 0;
            
            sql = "SELECT * FROM (" + sqlMain + ") T1 LEFT JOIN maptable ON IdEjemplar = OldID WHERE NewID IS NULL";
            
            
            
            fndCnt  = 0;
            numRecs = 0;
            tblWriter = convLogger.getWriter("MichConabio2.html", "Matches Cat No. / Collector No. ", extraStyle.toString());
            tblWriter.startTable();
            tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
            tblWriter.logHdr("Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Score");
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                process(1, rs, tblWriter, doingMapTable);
            }
            rs.close();
            tblWriter.endTable();
            tblWriter.log("<BR>");
            tblWriter.log(String.format("Number of records that match: %d  of %d records.", fndCnt, numRecs));
            tblWriter.log(String.format("Average Score: %5.2f", ((double)totalScore / (double)fndCnt)));
            tblWriter.log(String.format("Mode Score: %d", getModeScore()));
            totalScore = 0;

            
            
            
            fndCnt  = 0;
            numRecs = 0;
            tblWriter = convLogger.getWriter("MichConabio3.html", "Matches Collector No. ", extraStyle.toString());
            tblWriter.startTable();
            tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
            tblWriter.logHdr("Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Score");
            
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                process(2, rs, tblWriter, doingMapTable);
            }
            rs.close();
            tblWriter.endTable();
            tblWriter.log("<BR>");
            tblWriter.log(String.format("Number of records that match: %d  of %d records.", fndCnt, numRecs));
            tblWriter.log(String.format("Average Score: %5.2f", ((double)totalScore / (double)fndCnt)));
            tblWriter.log(String.format("Mode Score: %d", getModeScore()));
            totalScore = 0;

            
            
            fndCnt  = 0;
            numRecs = 0;
            tblWriter = convLogger.getWriter("MichConabio4.html", "Matches Cat No. ", extraStyle.toString());
            tblWriter.startTable();
            tblWriter.log("<TR><TH COLSPAN=\"7\">Conabio</TH><TH COLSPAN=\"7\">Michigan</TH><TD>&nbsp;</TH></TR>");
            tblWriter.logHdr("Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Col Num", "Cat Num", "Genus", "Species", "Collector", "Locality", "Date Collected", 
                             "Score");
            
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                process(3, rs, tblWriter, doingMapTable);
            }
            rs.close();
            tblWriter.endTable();
            tblWriter.log("<BR>");
            tblWriter.log(String.format("Number of records that match: %d  of %d records.", fndCnt, numRecs));
            tblWriter.log(String.format("Average Score: %5.2f", ((double)totalScore / (double)fndCnt)));
            tblWriter.log(String.format("Mode Score: %d", getModeScore()));
            totalScore = 0;

            
            tblWriter.flush();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
                mpStmt.close();
                conn.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } 
        
        File indexFile = convLogger.closeAll(); 
        
        try
        {
            AttachmentUtils.openURI(indexFile.toURI());
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //int n = StringUtils.getLevenshteinDistance("guatemaliense", "guatemalense");
        //System.out.println(n);
        
//      tblWriter.println("<BR>");
//      for (int i=0;i<histo.length;i++)
//      {
//          if (histo[i] > 0)
//          {
//              tblWriter.print(String.format("%3d - ", i));
//              for (int x=0;x<histo[i]/2;x++)
//              {
//                  tblWriter.print('*');
//              }
//              tblWriter.println("<BR>");
//          }
//      }
//      tblWriter.flush();
    }
    
    /**
     * @return the mean and zeros the histogram
     */
    private int getModeScore()
    {
        int max = 0;
        int mean = 0;
        for (int i=0;i<histo.length;i++)
        {
            if (histo[i] > max)
            {
                max = histo[i];
                mean = i;
            }
            histo[i] = 0;
        }
        return mean;
    }
    
    /**
     * @param type
     * @param rs
     * @param tblWriter
     * @param doingMapTable
     * @throws SQLException
     */
    public void process(final int         type,
                        final ResultSet   rs,
                        final TableWriter tblWriter,
                        final boolean     doingMapTable) throws SQLException
    {
        Integer idEjemplar   = rs.getInt(1);
        String  catNum       = rs.getString(2).trim();
        String  collectorNum = rs.getString(3).trim();
        String  collector    = rs.getString(4);
        String  genus        = rs.getString(9);
        String  species      = rs.getString(10);
        String  locality     = rs.getString(5);
        
        int     year         = rs.getInt(11);
        int     mon          = rs.getInt(12);
        int     day          = rs.getInt(13);
        boolean hasDate      = year > 0 && mon > 0 && day > 0;
        String  dateStr      = hasDate ? String.format("%04d-%02d-%02d", year, mon, day) : "&nbsp;";
        
        String where = null;
        switch (type)
        {
            case 0: 
                where = String.format("FROM conabio WHERE GenusName = '%s' AND SpeciesName = '%s' AND CollNr = '%s' AND BarCD = '%s'", genus, species, collectorNum, catNum);
                break;
                
            case 1: 
                where = String.format("FROM conabio WHERE CollNr = '%s' AND BarCD = '%s'", collectorNum, catNum);
                break;
                
            case 2: 
                where = String.format("FROM conabio WHERE CollNr = '%s' AND BarCD <> '%s'", collectorNum, catNum);
                break;
                
            case 3: 
                where = String.format("FROM conabio WHERE CollNr <> '%s' AND BarCD = '%s'", collectorNum, catNum);
                break;
        }
        
        String sql2 = "SELECT COUNT(*) " + where;
        int    cnt  = BasicSQLUtils.getCountAsInt(conn, sql2);
        
        if (cnt == 1)
        {
            for (int i=0;i<cCls.length;i++)
            {
                cCls[i] = null;
                mCls[i] = null;
            }
            
            if (doingMapTable)
            {
                sql2 = "SELECT ID " + where;
                int id = BasicSQLUtils.getCountAsInt(conn, sql2);
                
                mpStmt.setInt(1, idEjemplar);
                mpStmt.setInt(2, id);
                mpStmt.executeUpdate();
            }
            
            sql2 = "SELECT BarCD, CollNr, GenusName, SpeciesName, Collectoragent1, LocalityName, Datecollstandrd " + where;
            Vector<Object[]> rows = BasicSQLUtils.query(conn, sql2);
            Object[]         cols = rows.get(0);
            Date             date = (Date)cols[6];
            
            boolean michHasDate = date != null;
            String  michDate = michHasDate ? sdf.format(date) : "&nbsp;";
            
            Integer michCatNumInt    = (Integer)cols[0];
            String michCatNum    = michCatNumInt.toString();
            String michColNum    = ((String)cols[1]).trim();
            String michGenus     = ((String)cols[2]).trim();
            String michSpecies   = (String)cols[3];
            String michCollector = (String)cols[4];
            String michLocality  = (String)cols[5];
            
            int michYear         = 0;
            int michMon          = 0;
            int michDay          = 0;
            if (date != null) 
            {
                cal.setTime(date);
                michYear = michHasDate ? cal.get(Calendar.YEAR)  : 0;
                michMon  = michHasDate ? cal.get(Calendar.MONTH)+1 : 0;
                michDay  = michHasDate ? cal.get(Calendar.DATE)  : 0;
            }
            
            int maxScore = 115;
            int score    = 0;
            
            if (hasDate && michHasDate)
            {
                score += year == michYear ? 10 : 0;
                score += mon  == michMon  ? 20 : 0;
                score += day  == michDay  ? 30 : 0;
                
                if (year == michYear && mon == michMon && day == michDay && year != 0 && mon != 0 && day != 0)
                {
                    cCls[6] = BGR;
                    mCls[6] = BGR;
                    
                } else if (year == michYear && mon == michMon && year != 0 && mon != 0)
                {
                    cCls[6] = GR;
                    mCls[6] = GR;
                    
                } else if (year == michYear)
                {
                    cCls[6] = YW;
                    mCls[6] = YW;
                }
            }
            
            double ratingLoc   = check(locality, michLocality, false);
            double ratingColtr = check(collector, michCollector, false);
            
            if (ratingLoc > 50.0)
            {
                cCls[5] = BGR;
                mCls[5] = BGR;
                score += 10;
                
            } else if (ratingLoc > 30.0)
            {
                cCls[5] = GR;
                mCls[5] = GR;
                score += 6;
                
            } else if (ratingLoc > 0.0)
            {
                cCls[5] = YW;
                mCls[5] = YW;
                score += 3;
            }
            
            if (ratingColtr > 50.0)
            {
                cCls[4] = BGR;
                mCls[4] = BGR;
                score += 10;
                
            } else if (ratingColtr > 30.0)
            {
                cCls[4] = GR;
                mCls[4] = GR;
                score += 6;
                
            } else if (ratingColtr > 0.0)
            {
                cCls[4] = YW;
                mCls[4] = YW;
                score += 3;
            }
            
            boolean genusMatches = false;
            if (michGenus != null && genus != null)
            {
                if (michGenus.equals(genus))
                {
                    score += 15;
                    cCls[2] = GR;
                    mCls[2] = GR;
                    genusMatches = true;
                    
                } else if (StringUtils.getLevenshteinDistance(genus, michGenus) < 3)
                {
                    score += 7;
                    cCls[2] = YW;
                    mCls[2] = YW;
                }
            }
            
            if (michSpecies != null && species != null) 
            {
                if (michSpecies.equals(species))
                {
                    score += 20;
                    if (genusMatches)
                    {
                        cCls[2] = BGR;
                        mCls[2] = BGR;
                        cCls[3] = BGR;
                        mCls[3] = BGR;
                    } else
                    {
                        cCls[3] = GR;
                        mCls[3] = GR;
                    }
                } else if (StringUtils.getLevenshteinDistance(species, michSpecies) < 3)
                {
                    score += 10;
                    cCls[3] = YW;
                    mCls[3] = YW;
                }
            }
            
            if (michGenus != null && species != null && michGenus.equals(species))     
            {
                cCls[3] = DF;
                mCls[2] = DF;                        
                score += 10;
            }
            
            if (michSpecies != null && genus != null && michSpecies.equals(genus))
            {
                cCls[2] = DF;
                mCls[3] = DF;
                score += 15;
            }
            
            if (catNum.equals(michCatNum))
            {
                cCls[1] = BGR;
                mCls[1] = BGR;
            }
            
            if (collectorNum.equals(michColNum))
            {
                cCls[0] = BGR;
                mCls[0] = BGR;
            }
            
            int finalScore = (int)((((double)score / maxScore) * 100.0)+0.5);
            histo[finalScore]++;
            totalScore += finalScore;
            
            String scoreStr = String.format("%d", finalScore);
            tblWriter.println("<TR>");
            
            tblWriter.logTDCls(cCls[0], collectorNum);
            tblWriter.logTDCls(cCls[1], catNum);
            tblWriter.logTDCls(cCls[2], genus);
            tblWriter.logTDCls(cCls[3], species);
            tblWriter.logTDCls(cCls[4], collector);
            tblWriter.logTDCls(cCls[5], locality);
            tblWriter.logTDCls(cCls[6], dateStr);
            
            tblWriter.logTDCls(cCls[0], michColNum);
            tblWriter.logTDCls(cCls[1], michCatNum != null ? michCatNum.toString() : "&nbsp;");
            tblWriter.logTDCls(mCls[2], michGenus);
            tblWriter.logTDCls(mCls[3], michSpecies);
            tblWriter.logTDCls(mCls[4], michCollector);
            tblWriter.logTDCls(mCls[5], michLocality);
            tblWriter.logTDCls(mCls[6], michDate);
            
            tblWriter.logTDCls(null, scoreStr);
            tblWriter.println("</TR>");
            
            fndCnt++;
            System.out.println("Fnd: "+fndCnt+"  Num Recs: "+numRecs+"  Dif: "+(numRecs-fndCnt));
        }
        numRecs++;
    }
    
    /**
     * @param str
     * @param usePeriods
     * @return
     */
    private String clean(final String str, final boolean usePeriods)
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
    private double check(final String str1, final String str2, final boolean usePeriods)
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
        
        longStr  = clean(longStr, usePeriods);
        shortStr = clean(shortStr, usePeriods);
        
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
     * 
     */
    public void convert(final String databaseName,
                        final String tableName, 
                        final String srcFileName,
                        final String xmlFileName)
    {
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec", };
        HashMap<String, Integer> monthHash = new HashMap<String, Integer>();
        for (String mn : months)
        {
            monthHash.put(mn, monthHash.size()+1);
        }
        
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/"+databaseName+"?characterEncoding=UTF-8&autoReconnect=true", "root", "root");
            stmt = conn.createStatement();
            
            FMPCreateTable fmpInfo = new FMPCreateTable(tableName, null, true);
            fmpInfo.process(xmlFileName);
            
            processFieldSizes(fmpInfo, srcFileName);
            
            PrintWriter pw = new PrintWriter(new File("fields.txt"));
            int i = 0;
            for (FieldDef fd : fmpInfo.getFields())
            {
                pw.println(i+" "+fd.getName()+"\t"+fd.getType()+"\t"+fd.isDouble());
                i++;
            }
            pw.close();
            
            BasicSQLUtils.update(conn, fmpInfo.dropTableStr());
            
            String sqlCreateTable = fmpInfo.createTableStr();
            
            BasicSQLUtils.update(conn, sqlCreateTable);
            
            System.out.println(sqlCreateTable);
            
            String prepSQL = fmpInfo.getPrepareStmtStr(true, true);
            System.out.println(prepSQL);
            pStmt = conn.prepareStatement(prepSQL);
            
            Vector<FieldDef>  fieldDefs = fmpInfo.getFields();
            
            int            rowCnt = 0;
            BufferedReader in     = new BufferedReader(new InputStreamReader(new FileInputStream(srcFileName)));
            String str = in.readLine();
            str = in.readLine();
            while (str != null)
            {
                String line = str;
                char   sep    = '`';
                String sepStr = "";
                for (char c : seps)
                {
                    if (line.indexOf(c) == -1)
                    {
                        sepStr += c;
                        sep = c;
                        break;
                    }
                }
                str = StringUtils.replace(str.substring(1, str.length()-1), "\",\"", sepStr);
                Vector<String> fields = split(str, sep);
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                
                int col = 1;
                int inx = 0;
                for (String fld : fields)
                {
                    String   value  = fld.trim();
                    FieldDef fd     = fieldDefs.get(inx);
                    
                    switch (fd.getType())
                    {
                        case eText :
                        case eMemo : {
                            if (value.length() > 0)
                            {
                                //value = FMPCreateTable.convertFromUTF8(value);
                                if (value.length() <= fd.getMaxSize())
                                {
                                    pStmt.setString(col, value);
                                } else
                                {
                                    System.err.println(String.format("The data for `%s` (%d) is too big %d", fd.getName(), fd.getMaxSize(), value.length()));
                                    pStmt.setString(col, null);
                                }
                            } else
                            {
                                pStmt.setString(col, null);
                            }
                        }
                        break;
                        
                        case eNumber : {
                            if (StringUtils.isNotEmpty(value))
                            {
                                String origValue = value;
                                String val = value.charAt(0) == 'Ñ' ? value.substring(1) : value;
                                val = val.charAt(0) == '-' ? val.substring(1) : val;
                                val = val.indexOf('.') > -1 ? StringUtils.replace(val, ".", "") : val;
                                val = val.indexOf('¡') > -1 ? StringUtils.replace(val, "¡", "") : val;
                                
                                if (StringUtils.isNumericSpace(val))
                                {
                                    if (fd.isDouble())
                                    {
                                        pStmt.setDouble(col, Double.parseDouble(val));    
                                    } else
                                    {
                                        pStmt.setInt(col, Integer.parseInt(val));
                                    }
                                } else
                                {
                                    System.err.println(col + " Bad Number val["+val+"] origValue["+origValue+"] "+ fieldDefs.get(col-1).getName());
                                    pStmt.setObject(col, null);
                                }
                            } else
                            {
                                pStmt.setDate(col, null);
                            }
                        } break;
                        
                        case eTime : {
                            Time time =  null;
                            try
                            {
                               time = Time.valueOf(value);
                            } catch (Exception ex){}
                            pStmt.setTime(col, time);
                        } break;
                        
                        case eDate : {
                            String origValue = value;
                            try
                            {
                                if (StringUtils.isNotEmpty(value) && !value.equals("?") && !value.equals("-"))
                                {
                                    int len = value.length();
                                    if (len == 8 && value.charAt(1) == '-' && value.charAt(3) == '-') // 0-9-1886
                                    {
                                        String  dayStr = value.substring(0, 1);
                                        String  monStr = value.substring(2, 3);
                                        if (StringUtils.isNumeric(dayStr) && StringUtils.isNumeric(monStr))
                                        {
                                            String year = value.substring(4);
                                            int day = Integer.parseInt(dayStr);
                                            int mon = Integer.parseInt(monStr);
                                            if (day == 0) day = 1;
                                            if (mon == 0) mon = 1;
                                            
                                           value = String.format("%02d-%02d-%s", day, mon, year);
                                        } else
                                        {
                                            System.err.println(col+" Bad Date#["+value+"]  ["+origValue+"]\n");
                                        }
                                            
                                    } else if (len == 8 && (value.charAt(3) == '-' || value.charAt(3) == ' ')) // Apr-1886
                                    {
                                        String  monStr = value.substring(0, 3);
                                        Integer month  = monthHash.get(monStr);
                                        String  year   = value.substring(4);
                                        if (month != null && StringUtils.isNumeric(year))
                                        {
                                           value = String.format("01-%02d-%s", month, year);
                                        } else
                                        {
                                            System.err.println(col+" Bad Date*["+value+"]  ["+origValue+"]\n");
                                        }
                                        
                                    } else if ((len == 11 && value.charAt(2) == '-' && value.charAt(6) == '-') || // 10-May-1898
                                               (len == 10 && value.charAt(1) == '-' && value.charAt(5) == '-'))   //  7-May-1898
                                    {
                                        boolean do11 = len == 11;
                                        String  dayStr = value.substring(0, do11 ? 2 : 1);
                                        String  monStr = value.substring(do11 ? 3 : 2, do11 ? 6 : 5);
                                        
                                        Integer month  = monthHash.get(monStr);
                                        String  year   = value.substring(do11 ? 7 : 6);
                                        if (month != null && StringUtils.isNumeric(dayStr) && StringUtils.isNumeric(year))
                                        {
                                            int day = Integer.parseInt(dayStr);
                                            if (day == 0) day = 1;
                                            value = String.format("%02d-%02d-%s", day, month, year);
                                            
                                        } else
                                        {
                                            System.err.println(col+" Bad Date^["+value+"]  ["+origValue+"]\n");
                                        }
                                    } else if (len == 4)
                                    {
                                        if (StringUtils.isNumeric(value))
                                        {
                                            value = "01-01-" + value;
                                            
                                        } else if (value.equalsIgnoreCase("s.d.") || 
                                                   value.equalsIgnoreCase("n.d.") || 
                                                   value.equalsIgnoreCase("s.n."))
                                        {
                                            value = null;
                                        }
                                    } else if (StringUtils.contains(value, "/"))
                                    {
                                        value = StringUtils.replace(value, "/", "-");
                                        
                                    } else if (StringUtils.contains(value, " "))
                                    {
                                        value = StringUtils.replace(value, " ", "-");
                                    }
                                    pStmt.setDate(col, StringUtils.isNotEmpty(value) ? new java.sql.Date(sdf.parse(value).getTime()) : null);
                                } else
                                {
                                    pStmt.setDate(col, null);
                                }
                            } catch (Exception ex)
                            {
                                System.err.println(col+" Bad Date["+value+"]  ["+origValue+"]\n"+str);
                                pStmt.setDate(col, null);
                            }
                        } break;
                        
                        default:
                        {
                            System.err.println("Col: "+col+"  Error - "+fd.getType());
                        }
                    }
                    inx++;
                    col++;
                }
                pStmt.execute();
                str = in.readLine();
                rowCnt++;
                if (rowCnt % 1000 == 0)
                {
                    System.out.println(rowCnt);
                }
            }
            in.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                stmt.close();
                conn.close();
                pStmt.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }    
    }
    
    
    
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        String path = "/Users/rods/Documents/";
        MexConvToSQL m = new MexConvToSQL();
        //m.convert("michigan", "conabio", path+"ConabioAll.mer", path+"OneRecordFMP.xml");
        //m.convert("michigan", "michagents", path+"MichAgents.mer", path+"MichAgents.xml");
        m.convert("michigan", "michtaxon", path+"MichTaxon.mer", path+"MichTaxon.xml");
        //m.process("michigan");
    }
    

}
