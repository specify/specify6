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
package edu.ku.brc.specify.tools.webportal;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 2, 2012
 *
 */
public class BuildSearchIndex
{
    private final static int FAMILY_RANKID  = 140;
    private final static int COUNTRY_RANKID = 200;
    
    private Connection dbConn    = null;
    private Connection dbConn2   = null;
    private Connection dbConn3   = null;
    
    //-------------------------------
    // Lucene Indexing
    //-------------------------------
    
    protected int         INDEX_DIR     = 0;
    protected int         INDEX_DIR_COL = 1;
    protected int         INDEX_DIR_GEO = 2;
    protected int         INDEX_DIR_LOC = 3;
    protected int         INDEX_DIR_TXN = 4;

    protected String[]      fileNames  = new String[] {"index-specify"};
    protected File[]        files      = new File[fileNames.length];
    protected Analyzer[]    analyzers  = new Analyzer[fileNames.length];
    protected IndexReader[] readers    = new IndexReader[fileNames.length];

    protected Searcher     searcher;
    protected String       dbName;
    
    
    /**
     * @param server
     * @param port
     * @param dbName
     * @param username
     * @param pwd
     */
    public BuildSearchIndex()
    {
        super();
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
        this.dbName = dbName;
        
        String connStr = "jdbc:mysql://%s:%s/%s?characterEncoding=UTF-8&autoReconnect=true";
        try
        {
            dbConn  = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            dbConn2 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            dbConn3 = DriverManager.getConnection(String.format(connStr, server, port, dbName), username, pwd);
            
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
        
    /**
     * 
     */
    public void cleanup()
    {
        try
        {
            if (dbConn != null) dbConn.close();
            if (dbConn2 != null) dbConn2.close();
            if (dbConn3 != null) dbConn3.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    
    /**
     * @return the dbConn
     */
    public Connection getDBConn()
    {
        return dbConn;
    }

    private String createQuery()
    {
        /*String sql = "SELECT si.FieldName, qf.FieldName, qf.ColumnAlias, qf.TableList, qf.StringId, qf.FormatName, qf.ContextTableIdent, qf.`Position` FROM spexportschema AS s " +
                        "Inner Join spexportschemaitem AS si ON s.SpExportSchemaID = si.SpExportSchemaID " +
                        "Inner Join spexportschemaitemmapping AS smi ON si.SpExportSchemaItemID = smi.ExportSchemaItemID " +
                        "Inner Join spqueryfield AS qf ON smi.SpQueryFieldID = qf.SpQueryFieldID  WHERE " +
                        "ORDER BY  qf.ContextTableIdent ASC, qf.`Position` ASC";

        Statement stmt = null;
        try
        {
            
        } catch (Exception ex)
        {
            
        }*/
        //                                             
        String sqlStr = "SELECT co.CatalogNumber, co.CountAmt, ce.StartDate, ce.StationFieldNumber, " +
        		        "det.TypeStatusName, tx.FullName, tx.Name, tx.RankID, " +
                        "loc.Latitude1, loc.Longitude1, loc.LocalityName, loc.MaxElevation, loc.MinElevation, geo.FullName, geo.Name, geo.RankID, " +
                        "ag.LastName, ag.FirstName, ag.MiddleInitial, co.Text1, " + // 18
        		        "co.collectionObjectId, det.DeterminationID, tx.TaxonID, ce.CollectingEventID, loc.LocalityID, geo.GeographyID, ag.AgentID, " +
        		        "tx.ParentID, geo.ParentID " +
        		        "FROM collectionobject AS co " +
        		        "LEFT JOIN determination AS det ON co.CollectionObjectID = det.CollectionObjectID " +
                        "LEFT JOIN taxon AS tx ON det.TaxonID = tx.TaxonID " +
                        "LEFT JOIN collectingevent AS ce ON co.CollectingEventID = ce.CollectingEventID " +
                        "LEFT JOIN collector AS col ON ce.CollectingEventID = col.CollectingEventID " +
                        "LEFT JOIN agent AS ag ON col.AgentID = ag.AgentID " +
                        "LEFT JOIN locality AS loc ON ce.LocalityID = loc.LocalityID " +
                        "LEFT JOIN geography AS geo ON loc.GeographyID = geo.GeographyID " +
        		        "WHERE (det.isCurrent <> 0 or det.DeterminationID is null) AND co.collectionMemberId = 4 AND (col.OrderNumber = 1 OR col.CollectorID IS NULL)" +
        		        "ORDER BY co.collectionObjectId";
        return sqlStr;
    }
    
    /**
     * @param rs
     * @param sb
     * @param indexes
     * @return
     * @throws SQLException
     */
    private String buildStr(final ResultSet rs, final StringBuilder sb, int...indexes) throws SQLException
    {
        sb.setLength(0);
        for (int i=0;i<indexes.length;i++)
        {
            String val = rs.getString(indexes[i]);
            if (StringUtils.isNotEmpty(val))
            {
                sb.append(val);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    
    /**
     * 
     */
    public void index()
    {
        //    0            1           2              3                4               5      6     7
        // CatalogNumber, CountAmt, StartDate, StationFieldNumber TypeStatusName, FullName, Name, RankID,
        //    8          9            10            11            12          13       14     15       16        17         18           19
        // Latitude1, Longitude1, LocalityName, MaxElevation, MinElevation, FullName, Name, RankID, LastName, FirstName, MiddleInitial, Text1
        //         20              21            22              23           24           25         26          27          28
        //collectionObjectId, DeterminationID, TaxonID, CollectingEventID, LocalityID, GeographyID, AgentID, tx.ParentID, geo.ParentID
        
        //      0            1              2                3               4           5           6          7               8         9          10        11
        // CatalogNumber, StartDate, StationFieldNumber TypeStatusName, tx.FullName, Latitude1, Longitude1, LocalityName, geo.FullName, LastName, FirstName, MiddleInitial
        //                  0  1   2   3  4  5  6  7  8  9  0  1  2  3  4  5  6  7  8  9   20  1  2   3  4  5  6  7  8
        int[] colToTblId = {1, 1, 10, 10, 4, 4, 4, 4, 2, 2, 2, 2, 2, 3, 3, 3, 5, 5, 5, 1,   1, 9, 4, 10, 2, 3, 5, 4, 3};
        int[] includeCol = {1, 0,  1,  1, 1, 1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 0,   0, 0, 0,  0, 0, 0, 0, 0, 0};
        
        // Index for ResultSet (which is one more than the array index)
        int  idIndex  = 20;
        int  taxIndex = 23;
        int  geoIndex = 26;
        int  ceIndex  = 24;
        int  geoNameIndex = 15;
        int  taxNameIndex = 7;
        int  collDateIndex = 3;
        
        int  taxParentIndex = 28;
        int  geoParentIndex = 29;
        
        Calendar cal = Calendar.getInstance();
        
        long startTime = System.currentTimeMillis();
        
        IndexWriter[] writers = null;
        try
        {
            for (int i=0;i<analyzers.length;i++)
            {
                files[i]     = new File(fileNames[i]);
                analyzers[i] = new StandardAnalyzer(Version.LUCENE_30);
                FileUtils.deleteDirectory(files[i]);
            }

            System.out.println("Indexing to directory '" + INDEX_DIR + "'...");
            
            long totalRecs = BasicSQLUtils.getCount(dbConn, "SELECT COUNT(*) FROM collectionobject");
            long procRecs  = 0;
            
            Statement stmt  = null;
            Statement stmt2 = null;
            Statement stmt3 = null;
            //PreparedStatement pStmt = null;
            try
            {
                writers = new IndexWriter[analyzers.length];
                for (int i=0;i<files.length;i++)
                {
                    writers[i] = new IndexWriter(FSDirectory.open(files[i]), analyzers[i], true, IndexWriter.MaxFieldLength.LIMITED);
                }
                
                System.out.println("Total Records: "+totalRecs);
                
                stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
                stmt.setFetchSize(Integer.MIN_VALUE);
                
                stmt2 = dbConn2.createStatement();
                
                stmt3 = dbConn3.createStatement();
                stmt3.setFetchSize(Integer.MIN_VALUE);
                
                //pStmt = dbConn3.prepareStatement("SELECT Text1 FROM preparation WHERE CollectionObjectID = ? AND Text1 IS NOT NULL");
                
                String sql = createQuery();
                System.out.println(sql);
                
                ResultSet     rs       = stmt.executeQuery(sql);
                ResultSetMetaData md = rs.getMetaData();
                
                StringBuilder indexStr = new StringBuilder();
                StringBuilder contents = new StringBuilder();
                StringBuilder sb       = new StringBuilder();
                while (rs.next())
                {
                    String   id  = rs.getString(idIndex+1);
                    Document doc = new Document();
                    
                    doc.add(new Field("id", id.toString(), Field.Store.YES, Field.Index.ANALYZED));
                    
                    indexStr.setLength(0);
                    contents.setLength(0);
                    sb.setLength(0);
                    
                    int cnt = 0;
                    for (int i=0;i<idIndex;i++)
                    {
                        if (includeCol[i] == 1)
                        {
                            String val = rs.getString(i+1);
                            if (i == 0)
                            {
                                val = val.replaceFirst("^0+(?!$)", "");
                            }
                            
                            //System.out.println(i+" "+cnt+"  "+md.getColumnName(i+1)+" ["+(StringUtils.isNotEmpty(val) ? val : " ")+"] ");
                            contents.append(StringUtils.isNotEmpty(val) ? val : " ");
                            contents.append('\t');
                            cnt++;
                        }
                    }
                    
                    indexStr.append(contents);

                    Date collDate = rs.getDate(collDateIndex);
                    if (collDate != null)
                    {
                        cal.setTime(collDate);
                        String yearStr = Integer.toString(cal.get(Calendar.YEAR));
                        indexStr.append(yearStr);
                        indexStr.append('\t');
                        doc.add(new Field("yr", yearStr, Field.Store.YES, Field.Index.ANALYZED));
                    }
                    
                    sb.setLength(0);
                    for (int i=idIndex;i<colToTblId.length;i++)
                    {
                        //if (i>idIndex) sb.append(',');
                        //sb.append(String.format("%d=%d", colToTblId[i], rs.getInt(i+1)));
                        doc.add(new Field(Integer.toString(colToTblId[i]), Integer.toString(rs.getInt(i+1)), Field.Store.YES, Field.Index.NOT_ANALYZED)); 
                    }
                    doc.add(new Field("xref", sb.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED)); 
                    //writers[0].addDocument(doc);
                    
                    
                    ///////////////////////////////////////////////
                    // Catalog Number
                    ///////////////////////////////////////////////
                    String catNum = rs.getString(1);
                    if (StringUtils.isNotEmpty(catNum))
                    {
                        doc.add(new Field("cn", catNum, Field.Store.YES, Field.Index.ANALYZED));
                    }
                    
                    ///////////////////////////////////////////////
                    // Image Name in Text1
                    ///////////////////////////////////////////////
                    boolean hasName = false;
                    /*try
                    {
                        int idd = Integer.parseInt(id);
                        //pStmt.setInt(1, idd);
                        //ResultSet rsp = pStmt.executeQuery();
                        ResultSet rsp = stmt3.executeQuery(String.format("SELECT Text1 FROM preparation WHERE CollectionObjectID = %d AND Text1 IS NOT NULL", idd));
                        if (rsp.next())
                        {
                            String imgName = rsp.getString(1);
                            if (StringUtils.isNotEmpty(imgName))
                            {
                                String nm = FilenameUtils.getName(imgName);
                                doc.add(new Field("im", nm, Field.Store.NO, Field.Index.ANALYZED));
                                contents.append(nm);
                                hasName = true;
                            }
                        }
                        rsp.close();
                    } catch (SQLException e) {e.printStackTrace();}
                    */
                    if (!hasName)
                    {
                        contents.append(" ");
                    }
                    contents.append('\t');
                    
                    ///////////////////////////////////////////////
                    // Collector  (Agent)
                    ///////////////////////////////////////////////
                    String dataStr = buildStr(rs, sb, 17, 18, 19);
                    if (StringUtils.isNotEmpty(dataStr))
                    {
                        doc.add(new Field("ag", dataStr, Field.Store.NO, Field.Index.ANALYZED));
                    }
                    
                    //sb.setLength(0);
                    //sb.append(String.format("%d=%d", 1, rs.getInt(17))); // Collection Object
                    //doc.add(new Field("xref", sb.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED)); 
                    
                    ///////////////////////////////////////////////
                    // Locality 
                    ///////////////////////////////////////////////
                    dataStr = buildStr(rs, sb, 9, 10, 11, 12, 13, 14);
                    if (StringUtils.isNotEmpty(dataStr))
                    {
                        doc.add(new Field("lc", dataStr, Field.Store.NO, Field.Index.ANALYZED));
                    }
                    //writers[2].addDocument(doc);
                    
                    //sb.setLength(0);
                    //sb.append(String.format("%d=%d", 1, rs.getInt(17))); // Collection Object
                    //doc.add(new Field("xref", sb.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED)); 
                    
                    ///////////////////////////////////////////////
                    // Taxon
                    ///////////////////////////////////////////////
                    dataStr = buildStr(rs, sb, 5, 6);
                    if (StringUtils.isNotEmpty(dataStr))
                    {
                        doc.add(new Field("tx", dataStr, Field.Store.NO, Field.Index.ANALYZED));
                    }
                    //writers[3].addDocument(doc);
                    
                    int     taxId  = rs.getInt(taxIndex);
                    boolean taxOK  = !rs.wasNull();
                    int     taxPId = rs.getInt(taxParentIndex);
                    taxOK = taxOK && !rs.wasNull();
                    
                    int     geoId = rs.getInt(geoIndex);
                    boolean geoOK = !rs.wasNull();
                    int     geoPId = rs.getInt(geoParentIndex);
                    geoOK = geoOK && !rs.wasNull();
                    
                    int     ceId = rs.getInt(ceIndex);
                    boolean ceOK = !rs.wasNull();
                    
                    if (taxOK)
                    {
                        addHigherTaxa(stmt2, doc, indexStr, taxId, taxPId, rs.getInt(taxNameIndex+1), rs.getString(taxNameIndex));
                        addAuthor(stmt2,     doc, indexStr, taxId);
                    }
                    
                    if (geoOK)
                    {
                        addCountry(stmt2, doc, indexStr, geoId, geoPId, rs.getInt(geoNameIndex+1), rs.getString(geoNameIndex));
                    }
                    
                    if (ceOK)
                    {
                        addHost(stmt2, doc, indexStr, ceId);
                    }
                    
                    //sb.setLength(0);
                    //sb.append(String.format("%d=%d", 1, rs.getInt(17))); // Collection Object
                    //doc.add(new Field("xref", sb.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED)); 
                    
                    doc.add(new Field("cs", indexStr.toString(), Field.Store.NO, Field.Index.ANALYZED));
                    doc.add(new Field("contents", contents.toString(), Field.Store.YES, Field.Index.NOT_ANALYZED));
                    writers[0].addDocument(doc);
                    
                    //System.out.println(procRecs+" "+rs.getString(1));
                    procRecs++;
                    if (procRecs % 1000 == 0)
                    {
                        System.out.println(procRecs);
                    }
                    
                    if (procRecs % 100000 == 0)
                    {
                        System.out.println("Optimizing...");
                        writers[0].optimize();
                    }
                }
                rs.close();
                
            } catch (SQLException sqlex) 
            {
                sqlex.printStackTrace();
                
            } catch (IOException e) 
            {
                e.printStackTrace();
                System.out.println("IOException adding Lucene Document: " + e.getMessage());
                
            } finally
            {
                
                if (stmt != null)
                {
                    try
                    {
                        if (stmt != null) stmt.close();
                        if (stmt2 != null) stmt2.close();
                        if (stmt3 != null) stmt3.close();
                        
                    } catch (SQLException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
            
        } catch (IOException e)
        {
            e.printStackTrace();
            
            System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
            
        } finally
        {
            for (Analyzer a : analyzers)
            {
                a.close();
            }
            analyzers = null;
            
            for (IndexWriter writer : writers)
            {
                try
                {
                    System.out.println("Optimizing...");
                    writer.optimize();
                    writer.close();
                    System.out.println("Done Optimizing.");
                    
                } catch (CorruptIndexException e)
                {
                    e.printStackTrace();
                    
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
                writer = null;
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Time: "+ (endTime - startTime) / 1000);
        }
    }
    
    
    /**
     * @param sb
     * @param val
     * @param doSep
     * @param sep
     */
    private void appendStr(final StringBuilder sb, final String val, final boolean doSep, final String sep)
    {
        if (StringUtils.isNotEmpty(val))
        {
            if (doSep) sb.append(sep);
            sb.append(val);
        }
    }
    
    /**
     * @param stmt
     * @param doc
     * @param indexStr
     * @param taxonId
     * @param rankId
     * @param taxName
     */
    private void addHigherTaxa(final Statement stmt, 
                               final Document doc, 
                               final StringBuilder indexStr, 
                               final int taxonId, 
                               final int taxonParentId, 
                               final int rankId,
                               final String taxName)
    {
        String name = null;
        if (rankId == FAMILY_RANKID)
        {
            if (StringUtils.isNotEmpty(taxName))
            {
                name = taxName; 
            }
        } else if (rankId > FAMILY_RANKID)
        {
            int       taxPId = taxonParentId;
            ResultSet rs    = null;
            int       rId   = rankId;
            try
            {
                boolean isParentNull = false;
                while (rId > FAMILY_RANKID && !isParentNull)
                {
                    rs = stmt.executeQuery("SELECT taxonID, RankID, Name, ParentID FROM taxon WHERE TaxonID = "+ taxPId);
                    if (rs.next() && !isParentNull)
                    {
                        rId     = rs.getInt(2);
                        name    = rs.getString(3);
                        taxPId  = rs.getInt(4);
                        if (taxPId == taxonParentId)
                        {
                            return; // points to self
                        }
                        isParentNull = rs.wasNull();
                    } else
                    {
                        isParentNull = true;
                    }
                    rs.close();
                }
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                try
                {
                    if (rs != null) rs.close();
                } catch (SQLException ex) {}
            }
        }         
        
        if (StringUtils.isNotEmpty(name))
        {
            indexStr.append('\t');
            indexStr.append(name);
            doc.add(new Field("ht", name, Field.Store.YES, Field.Index.ANALYZED));
        }
    }
    
    /**
     * @param stmt
     * @param doc
     * @param indexStr
     * @param taxId
     */
    private void addAuthor(final Statement stmt, 
                           final Document doc, 
                           final StringBuilder indexStr, 
                           final int taxId)
    {
        StringBuilder sb = null;
        
        String sql = "SELECT t.TaxonID, t.Author, r.ReferenceWorkType, r.Title, r.WorkDate, j.JournalName, ag.LastName, ag.FirstName, ag.MiddleInitial, a.OrderNumber FROM taxon AS t " +
                     "Left Join taxoncitation AS tc ON t.TaxonID = tc.TaxonID " +
                     "Left Join referencework AS r ON tc.ReferenceWorkID = r.ReferenceWorkID " +    
                     "Left Join journal AS j ON r.JournalID = j.JournalID Left Join author AS a ON r.ReferenceWorkID = a.ReferenceWorkID " +
                     "Inner Join agent AS ag ON a.AgentID = ag.AgentID WHERE t.TaxonID = " + taxId;
        
        ResultSet rs = null;
        try
        {
            boolean       first   = true;
            StringBuilder authors = null;
            
            rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                if (first)
                {
                    sb = new StringBuilder();
                    String auth    = rs.getString(2);
                    String title   = rs.getString(4);
                    String wrkDate = rs.getString(5);
                    String jName   = rs.getString(6);
                    
                    if (StringUtils.isNotEmpty(wrkDate))
                    {
                        doc.add(new Field("yb", wrkDate, Field.Store.YES, Field.Index.ANALYZED));
                    }
                    
                    appendStr(sb, auth, false, null);
                    appendStr(sb, title, sb.length() > 0, "; ");
                    appendStr(sb, jName, sb.length() > 0, "; ");
                    appendStr(sb, wrkDate, sb.length() > 0, "; ");
                    first = false;
                }
                
                String lName = rs.getString(7);
                String fName = rs.getString(8);
                String mName = rs.getString(9);

                if (authors == null)
                {
                    authors = new StringBuilder();
                }
                appendStr(authors, lName, authors.length() > 0, "; ");
                appendStr(authors, fName, authors.length() > 0, ", ");
                appendStr(authors, mName, authors.length() > 0, ", ");
            }
            
            if (authors != null && authors.length() > 0)
            {
                 if (sb == null)
                 {
                     sb = new StringBuilder();
                 }
                sb.append("(");
                sb.append(authors);
                sb.append(")");
            }
            
            if (sb != null)
            {
                doc.add(new Field("au", sb.toString(), Field.Store.YES, Field.Index.ANALYZED));
            }
            
        } catch (SQLException ex)
        {
            
        } finally
        {
            try
            {
                if (rs != null) rs.close();
            } catch (SQLException ex) {}
        }
        
        if (sb != null && sb.length() > 0)
        {
            indexStr.append('\t');
            indexStr.append(sb.toString());
        }
    }
    
    /**
     * @param stmt
     * @param doc
     * @param indexStr
     * @param geographyId
     * @param rankId
     * @param geoName
     */
    private void addCountry(final Statement stmt, 
                            final Document doc, 
                            final StringBuilder indexStr, 
                            final int geographyId, 
                            final int geographyParentId, 
                            final int rankId,
                            final String geoName)
    {
        String country = null;
        if (rankId == COUNTRY_RANKID)
        {
            if (StringUtils.isNotEmpty(geoName))
            {
                country = geoName; 
            }
        } else if (rankId > COUNTRY_RANKID)
        {
            int       geoPId = geographyParentId;
            ResultSet rs    = null;
            int       rId   = rankId;
            try
            {
                int prevRankId = rankId;
                boolean isParentNull = false;
                while (rId > COUNTRY_RANKID)
                {
                    rs = stmt.executeQuery("SELECT GeographyID, RankID, Name, ParentID FROM geography WHERE GeographyID = "+ geoPId);
                    if (rs.next() && !isParentNull)
                    {
                        rId     = rs.getInt(2);
                        country = rs.getString(3);
                        geoPId  = rs.getInt(4);
                        isParentNull = rs.wasNull();
                        
                        if (rId >= prevRankId)
                        {
                            break;
                        }
                        prevRankId = rId;
                        
                    } else
                    {
                        isParentNull = true;
                    }
                    rs.close();
                }
                
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            } finally
            {
                try
                {
                    if (rs != null) rs.close();
                } catch (SQLException ex) {}
            }
        }         
        
        if (StringUtils.isNotEmpty(country))
        {
            indexStr.append('\t');
            indexStr.append(country);
            doc.add(new Field("cy", country, Field.Store.YES, Field.Index.ANALYZED));
        }
    }
    
    /**
     * @param stmt
     * @param doc
     * @param indexStr
     * @param ceId
     */
    private void addHost(final Statement stmt, 
                         final Document doc, 
                         final StringBuilder indexStr, 
                         final int ceId)
    {
        String sql = "SELECT t.FullName FROM collectingevent AS ce " +
                     "Inner Join collectingeventattribute AS a ON ce.CollectingEventAttributeID = a.CollectingEventAttributeID " +
                     "Inner Join taxon AS t ON a.HostTaxonID = t.TaxonID WHERE ce.CollectingEventID = " + ceId;
        
        ResultSet rs = null;
        String name = null;
        try
        {
            rs = stmt.executeQuery(sql);
            if (rs.next())
            {
                name = rs.getString(1);
                if (StringUtils.isNotEmpty(name))
                {
                    indexStr.append('\t');
                    indexStr.append(name);
                    doc.add(new Field("hs", name, Field.Store.YES, Field.Index.ANALYZED));
                }
            }
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (rs != null) rs.close();
            } catch (SQLException ex) {}
        }
    }
    
    /**
     * 
     */
    /*public void testSearch()
    {
        Statement stmt = null;
        
        String querystr = "(Pengelly) OR (Castilleja AND applegatei)";
        String term     = "contents";
        try
        {
            stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            
            analyzers = new Analyzer[fileNames.length];
            for (int i=0;i<analyzers.length;i++)
            {
                files[i]     = new File(fileNames[i]);
                analyzers[i] = new StandardAnalyzer(Version.LUCENE_30);
                readers[i]   = IndexReader.open(FSDirectory.open(files[i]), true);
            }
            
            HashMap<Integer, Integer> tblIdHash = new HashMap<Integer, Integer>();
            
            for (int inx=0;inx<analyzers.length;inx++)
            {
                long  startTime   = System.currentTimeMillis();
                Query query       = new QueryParser(Version.LUCENE_30, term, analyzers[inx]).parse(querystr);
                int   hitsPerPage = 10;
                searcher = new IndexSearcher(readers[inx]);
                
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                
                System.out.println("\n------------- "+fileNames[inx] + " - Found: " + hits.length + " hits.");
                
                for (int i=0;i<hits.length;++i) 
                {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    //System.out.println((i + 1) + ". " + d.get("id") + " -> "+ d.get("xref"));
                    
                    tblIdHacssh.clear();
                    
                    String pairStr = d.get("xref");
                    if (StringUtils.isNotEmpty(pairStr))
                    {
                        String [] pairs = StringUtils.split(d.get("xref"), ',');
                        for (String p : pairs)
                        {
                            String [] ids = StringUtils.split(p, '=');
                            tblIdHash.put(Integer.parseInt(ids[0]), Integer.parseInt(ids[1]));
                        }
                    }
                    
                    if (inx == 0)
                    {
                        String id = d.get("id");
                        ResultSet         rs   = stmt.executeQuery("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+id);
                        ResultSetMetaData rsmd = rs.getMetaData();
                        while (rs.next())
                        {
                            for (int j=1;j<=rsmd.getColumnCount();j++)
                            {
                                System.out.print(rs.getObject(j) + "\t");
                            }
                            System.out.println();
                        }
                        rs.close();
                        
                        Integer agentId = tblIdHash.get(5);
                        if (agentId != null)
                        {
                            rs   = stmt.executeQuery("SELECT LastName, FirstName, MiddleInitial FROM agent WHERE AgentID = "+agentId);
                            rsmd = rs.getMetaData();
                            while (rs.next())
                            {
                                for (int j=1;j<=rsmd.getColumnCount();j++)
                                {
                                    if (rs.getObject(j) != null) System.out.print(rs.getObject(j) + "\t");
                                }
                                System.out.println();
                            }
                            rs.close();
                        }
                    } else
                    {
                        Integer colObjId = tblIdHash.get(1);
                        if (colObjId != null)
                        {
                            ResultSet         rs   = stmt.executeQuery("SELECT CatalogNumber FROM collectionobject WHERE CollectionObjectID = "+colObjId);
                            ResultSetMetaData rsmd = rs.getMetaData();
                            while (rs.next())
                            {
                                for (int j=1;j<=rsmd.getColumnCount();j++)
                                {
                                    System.out.print(rs.getObject(j) + "\t");
                                }
                                System.out.println();
                            }
                            rs.close();
                        }
                    }
                    
                }
                System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis() - startTime) / 1000.0));
                searcher.close();
            }
            
            
            for (int i=0;i<analyzers.length;i++)
            {
                readers[i].close();
                analyzers[i].close();
            }
            
        } catch (SQLException e)
        {
            e.printStackTrace();
            
        } catch (IOException e)
        {
            e.printStackTrace();
            
        } catch (ParseException e)
        {
            e.printStackTrace();
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }*/
    
    public void testSearch()
    {
        Statement stmt = null;
        
        String querystr = "23033";//(Pengelly) OR (Castilleja AND applegatei)";
        String term     = "1";//"contents"
        try
        {
            //stmt = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
            
            analyzers = new Analyzer[fileNames.length];
            for (int i=0;i<analyzers.length;i++)
            {
                files[i]     = new File(fileNames[i]);
                analyzers[i] = new StandardAnalyzer(Version.LUCENE_30);
                readers[i]   = IndexReader.open(FSDirectory.open(files[i]), true);
            }
            
            HashMap<Integer, Integer> tblIdHash = new HashMap<Integer, Integer>();
            
            for (int inx=0;inx<analyzers.length;inx++)
            {
                long  startTime   = System.currentTimeMillis();
                QueryParser queryParser = new QueryParser(Version.LUCENE_30, term, analyzers[inx]);
                Query query = queryParser.parse(querystr);
                
                int   hitsPerPage = 10;
                searcher = new IndexSearcher(readers[inx]);
                
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
                searcher.search(query, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                
                System.out.println("\n------------- "+fileNames[inx] + " - Found: " + hits.length + " hits.");
                
                for (int i=0;i<hits.length;++i) 
                {
                    int docId = hits[i].doc;
                    Document d = searcher.doc(docId);
                    System.out.println((i + 1) + ". " + d.get("1"));
                    
                    //tblIdHacssh.clear();
                }
                    
                System.out.println(String.format("Time: %8.2f", (System.currentTimeMillis() - startTime) / 1000.0));
                searcher.close();
            }
            
            
            for (int i=0;i<analyzers.length;i++)
            {
                readers[i].close();
                analyzers[i].close();
            }
            
        } catch (IOException e)
        {
            e.printStackTrace();
            
        } catch (ParseException e)
        {
            e.printStackTrace();
        } finally
        {
            if (stmt != null)
            {
                try
                {
                    stmt.close();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        BuildSearchIndex bsi = new BuildSearchIndex();
        //bsi.createDBConnection("localhost", "3306", "so_oregon_6", "root", "root");
        //bsi.createDBConnection("localhost", "3306", "kui_fish_dbo_6", "root", "root");
        bsi.createDBConnection("localhost", "3306", "entodb", "root", "root");
        bsi.index();
        bsi.testSearch();
    }

}
