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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrTokenizer;

import org.apache.log4j.Logger;


import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericLSIDGeneratorFactory;
import edu.ku.brc.af.core.RecordSetFactory;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.BackupServiceFactory;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterMgr;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.CustomQueryFactory;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.SchemaUpdateService;
import edu.ku.brc.exceptions.ExceptionTracker;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * May 4, 2010
 *
 */
public class LocalityCleanup
{
    protected static final Logger  log = Logger.getLogger(LocalityCleanup.class);
    
    private Vector<LocalityInfo> items = new Vector<LocalityInfo>();
    private String[] header = {"Should Fix", "Locality Name", "Count"};
    
    
    /**
     * 
     */
    public LocalityCleanup()
    {
        super();
    }

    
    public void doCleanup()
    {
        String sql = "SELECT LocalityName, cnt FROM (SELECT LocalityName, COUNT(LocalityName) as cnt FROM locality GROUP BY LocalityName) T1 WHERE cnt > 1 ORDER BY cnt desc ";
        for (Object[] cols : BasicSQLUtils.query(sql))
        {
            items.add(new LocalityInfo((String)cols[0], (Integer)cols[1], true));
        }
        
        DefaultTableModel model = new DefaultTableModel()
        {

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#getColumnCount()
             */
            @Override
            public int getColumnCount()
            {
                return header != null ? header.length : 0;
            }

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#getColumnName(int)
             */
            @Override
            public String getColumnName(int column)
            {
                return header != null ? header[column] : "";
            }

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#getRowCount()
             */
            @Override
            public int getRowCount()
            {
                return items != null ? items.size() : 0;
            }

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#getValueAt(int, int)
             */
            @Override
            public Object getValueAt(int row, int column)
            {
                LocalityInfo locInfo = items != null ? items.get(row) : null;
                if (locInfo != null)
                {
                    switch (column)
                    {
                        case 0 : return locInfo.isIncluded();
                        case 1 : return locInfo.getLocalityName();
                        case 2 : return locInfo.getCnt();
                    }
                }
                return null;
            }

            /* (non-Javadoc)
             * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
             */
            @Override
            public Class<?> getColumnClass(int columnIndex)
            {
                switch (columnIndex)
                {
                    case 0 : return Boolean.class;
                    case 1 : return String.class;
                    case 2 : return Integer.class;
                }
                return String.class;
            }

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#isCellEditable(int, int)
             */
            @Override
            public boolean isCellEditable(int row, int column)
            {
                return column == 0;
            }

            /* (non-Javadoc)
             * @see javax.swing.table.DefaultTableModel#setValueAt(java.lang.Object, int, int)
             */
            @Override
            public void setValueAt(Object aValue, int row, int column)
            {
                if (column == 0)
                {
                    LocalityInfo locInfo = items != null ? items.get(row) : null;
                    if (locInfo != null)
                    {
                        locInfo.setIncluded((Boolean)aValue);
                    }
                }
            }
            
        };
        
        JTable table = new JTable(model);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "f:p:g"));
        pb.add(UIHelper.createScrollPane(table), cc.xy(1,1));
        pb.setDefaultDialogBorder();
        
        CustomDialog dlg = new CustomDialog(null, "Locality Duplicates", true, pb.getPanel());
        dlg.setVisible(true);
        if (!dlg.isCancelled())
        {
            doFixDuplicates();
        }
        
    }
    
    private void doFixDuplicates()
    {
      /*  
        for (LocalityInfo locInfo : items)
        {
            if (locInfo.isIncluded())
            {
                String sql = "SELECT LocalityID";
            }
        }
        */
    }
    
    class LocalityInfo
    {
        String  localityName;
        int     cnt;
        boolean isIncluded;
        /**
         * @param localityName
         * @param cnt
         * @param isIncluded
         */
        public LocalityInfo(String localityName, int cnt, boolean isIncluded)
        {
            super();
            this.localityName = localityName;
            this.cnt = cnt;
            this.isIncluded = isIncluded;
        }
        /**
         * @return the localityName
         */
        public String getLocalityName()
        {
            return localityName;
        }
        /**
         * @return the cnt
         */
        public int getCnt()
        {
            return cnt;
        }
        /**
         * @return the isIncluded
         */
        public boolean isIncluded()
        {
            return isIncluded;
        }
        /**
         * @param isIncluded the isIncluded to set
         */
        public void setIncluded(boolean isIncluded)
        {
            this.isIncluded = isIncluded;
        }
    }
    
    
    public static void setUpSystemProperties()
    {
        // Name factories
        System.setProperty(ViewFactory.factoryName,                     "edu.ku.brc.specify.config.SpecifyViewFactory");        // Needed by ViewFactory //$NON-NLS-1$
        System.setProperty(AppContextMgr.factoryName,                   "edu.ku.brc.specify.config.SpecifyAppContextMgr");      // Needed by AppContextMgr //$NON-NLS-1$
        System.setProperty(AppPreferences.factoryName,                  "edu.ku.brc.specify.config.AppPrefsDBIOIImpl");         // Needed by AppReferences //$NON-NLS-1$
        System.setProperty("edu.ku.brc.ui.ViewBasedDialogFactoryIFace", "edu.ku.brc.specify.ui.DBObjDialogFactory");            // Needed By UIRegistry //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.forms.DraggableRecordIdentifierFactory", "edu.ku.brc.specify.ui.SpecifyDraggableRecordIdentiferFactory"); // Needed By the Form System //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.dbsupport.AuditInterceptor",     "edu.ku.brc.specify.dbsupport.AuditInterceptor");       // Needed By the Form System for updating Lucene and logging transactions //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(DataProviderFactory.factoryName,             "edu.ku.brc.specify.dbsupport.HibernateDataProvider");  // Needed By the Form System and any Data Get/Set //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty("edu.ku.brc.ui.db.PickListDBAdapterFactory", "edu.ku.brc.specify.ui.db.PickListDBAdapterFactory");   // Needed By the Auto Cosmplete UI //$NON-NLS-1$ //$NON-NLS-2$
        System.setProperty(CustomQueryFactory.factoryName,              "edu.ku.brc.specify.dbsupport.SpecifyCustomQueryFactory"); //$NON-NLS-1$
        System.setProperty(UIFieldFormatterMgr.factoryName,             "edu.ku.brc.specify.ui.SpecifyUIFieldFormatterMgr");           // Needed for CatalogNumberign //$NON-NLS-1$
        System.setProperty(QueryAdjusterForDomain.factoryName,          "edu.ku.brc.specify.dbsupport.SpecifyQueryAdjusterForDomain"); // Needed for ExpressSearch //$NON-NLS-1$
        System.setProperty(SchemaI18NService.factoryName,               "edu.ku.brc.specify.config.SpecifySchemaI18NService");         // Needed for Localization and Schema //$NON-NLS-1$
        System.setProperty(WebLinkMgr.factoryName,                      "edu.ku.brc.specify.config.SpecifyWebLinkMgr");                // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(DataObjFieldFormatMgr.factoryName,           "edu.ku.brc.specify.config.SpecifyDataObjFieldFormatMgr");     // Needed for WebLnkButton //$NON-NLS-1$
        System.setProperty(RecordSetFactory.factoryName,                "edu.ku.brc.specify.config.SpecifyRecordSetFactory");          // Needed for Searching //$NON-NLS-1$
        System.setProperty(DBTableIdMgr.factoryName,                    "edu.ku.brc.specify.config.SpecifyDBTableIdMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(SecurityMgr.factoryName,                     "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        //System.setProperty(UserAndMasterPasswordMgr.factoryName,               "edu.ku.brc.af.auth.specify.SpecifySecurityMgr");              // Needed for Tree Field Names //$NON-NLS-1$
        System.setProperty(BackupServiceFactory.factoryName,            "edu.ku.brc.af.core.db.MySQLBackupService");                   // Needed for Backup and Restore //$NON-NLS-1$
        System.setProperty(ExceptionTracker.factoryName,                "edu.ku.brc.specify.config.SpecifyExceptionTracker");                   // Needed for Backup and Restore //$NON-NLS-1$
        
        System.setProperty(DBMSUserMgr.factoryName,                     "edu.ku.brc.dbsupport.MySQLDMBSUserMgr");
        System.setProperty(SchemaUpdateService.factoryName,             "edu.ku.brc.specify.dbsupport.SpecifySchemaUpdateService");   // needed for updating the schema
        System.setProperty(GenericLSIDGeneratorFactory.factoryName,     "edu.ku.brc.specify.config.SpecifyLSIDGeneratorFactory");
    }
    
    
    public static void fixOld()
    {
        String                    connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "kevin";
        
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        try
        {
            String sql = "SELECT LocalityName, cnt FROM (SELECT LocalityName, COUNT(LocalityName) as cnt FROM locality GROUP BY LocalityName) T1 WHERE cnt > 1 ORDER BY cnt desc ";
            
            Statement         stmt  = conn.createStatement();
            Statement         stmt2  = conn.createStatement();
            PreparedStatement pStmt = conn.prepareStatement("UPDATE collectingevent SET LocalityID=? WHERE CollectingEventID = ?");
            
            int fixedCnt = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                String locName = rs.getString(1);
                int    cnt     = rs.getInt(2);
                
                sql = String.format("SELECT LocalityID FROM locality WHERE LocalityName = '%s' ORDER BY LocalityID ASC", locName);
                System.out.println("------------------------------------" + locName + "-----------------------------------");
                
                int c = 0;
                Integer firstID = null;
                
                ResultSet rs2 = stmt2.executeQuery(sql);
                while (rs2.next())
                {
                    int id = rs2.getInt(1);
                    if (c == 0) 
                    {
                        firstID = id;
                        c = 1;
                        continue;
                    }
                    
                    System.out.println("Fixing LocalityID: "+id);
                    sql = String.format("SELECT CollectingEventId FROM collectingevent WHERE LocalityID = %d", id);
                    Vector<Integer> ids = BasicSQLUtils.queryForInts(conn, sql);
                    for (Integer ceId : ids)
                    {
                        pStmt.setInt(1, firstID);
                        pStmt.setInt(2, ceId);
                        if (pStmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating CE Id: "+ceId);
                        } else
                        {
                            System.out.println("Fixed CollectingEventID: "+ceId+"  with LocalityID: "+firstID);
                            fixedCnt++;
                        }
                    }
                    c++;
                }
                rs2.close();
                
                if (c != cnt)
                {
                    System.out.println("Error updating all Localities for "+locName);
                }
            }
            rs.close();
            
            stmt.close();
            stmt2.close();
            pStmt.close();
            
            System.out.println("Fixed CE Ids: "+fixedCnt);
            
        } catch (SQLException ex)
        {
           ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public static void fixGeonames()
    {
        String connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "testfish";
        
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName+"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        File file = new File("/Users/rods/Downloads/allCountries.txt");
        try
        {
            int cnt = 0;
            int updateCnt = 0;
            InputStream fileIS = new FileInputStream(file);

            PreparedStatement ps = conn.prepareStatement("UPDATE geoname SET name=? WHERE geonameId = ?");
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(fileIS));
            while (bufReader.ready())
            {
                String s = bufReader.readLine();
                //System.out.println(s);
                String str = new String(s.getBytes(), "UTF8");
                //System.out.println(str);
                
                String[] toks = new StrTokenizer(str, "\t").getTokenArray(); //StringUtils.tokenizeToStringArray(str, "\t");
                Integer key = Integer.parseInt(toks[0]);
                
                if (BasicSQLUtils.getCountAsInt(conn, "SELECT COUNT(*) FROM geoname WHERE geonameId = "+key) == 1)
                {
                    ps.setString(1, toks[1]);
                    ps.setInt(2, key);
                    
                    System.out.println(toks[1]+" "+key);
                    if (ps.executeUpdate() != 1)
                    {
                        System.err.println("Error updating "+key);
                    }
                    updateCnt++;
                }
                cnt++;
                if (cnt % 1000 == 0)
                {
                    System.out.println(cnt);
                }
            }
            bufReader.close();
            ps.close();
            conn.close();
            
            System.out.println(cnt+"  "+updateCnt);
            
        } catch (Exception e1)
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    
    public static void testI18N()
    {
        String connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "kevin";
        
        //DBConnection dbc = new DBConnection("root", "root", connectStr+dbName+"?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName+"?characterSetResults=ISO8859_1&characterEncoding=ISO8859_1", "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        String[] types = {"ASCII", "ISO8859_1", "Cp1252", "ISO8859_2", "ISO8859_3", "ISO8859_4", 
                          "UTF8", "ISO8859_5", "ISO8859_7", "ISO8859_9", "latin1"};
        try
        {
            PreparedStatement ps = conn.prepareStatement("show variables like '%character%'");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) 
            {
                System.out.println(rs.getString(1) +"-->"+rs.getString(2));
            }
            rs.close();
            ps.close();
            //System.out.println("-----------------------------");
            //System.out.println("ARGS[0]:"+args[0]);
            System.out.println("-----------------------------");
            System.out.println("JVM DEFAULT CHARSET:"+java.nio.charset.Charset.defaultCharset());
            System.out.println("-----------------------------");
            System.out.println("JVM file.encoding:"+System.getProperty("file.encoding"));
            System.out.println("-----------------------------");



            byte[] line = new byte[100];
            Charset utf8charset     = Charset.forName("UTF-8");
            Charset iso88591charset = Charset.forName("ISO-8859-1");

            //String sql = "SELECT name from geoname WHERE geonameId = 45060";//66575";
            
            String sql = "SELECT LocalityName from locality WHERE LocalityID = 1401 OR LocalityID = 2123";//66575";
            
            Statement stmt = conn.createStatement();
            rs   = stmt.executeQuery(sql);
            while (rs.next())
            {
                String name = rs.getString(1);
                System.out.println("String: "+name);
                
                //InputStream is = rs.getAsciiStream(1);
                /*Reader reader = rs.getCharacterStream(1);
                try
                {
                    //int len = is.read(line);
                    char[] cbuf = new char[100];
                    int len = reader.read(cbuf);
                    System.out.println("String IO: "+(new String(cbuf, 0, len)));
                    
                } catch (IOException e1)
                {
                    e1.printStackTrace();
                }*/

                byte[] nameBytes = rs.getBytes(1);
                System.out.println("String Bytes: "+(new String(nameBytes)));
                
                
                /*ByteBuffer inputBuffer = ByteBuffer.wrap(nameBytes);
                CharBuffer data = iso88591charset.decode(inputBuffer);
                
                char[] chars = data.array();
                byte[] word = new byte[chars.length];
                for (int i=0;i<chars.length;i++)
                {
                    word[i] = (byte)chars[i];
                }
                
                System.out.println(data.toString()+" / "+(new String(word)));
                
                ByteBuffer outputBuffer = utf8charset.encode(data);
                byte[] outputData = outputBuffer.array();
                System.out.println(new String(outputData));*/
                
                
                for (int i=0;i<types.length;i++)
                {
                    try
                    {
                        name = new String(nameBytes, types[i]);
                    } catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                    System.out.println(types[i]+":"+name);
                }
            }
            rs.close();
            
            stmt.close();
            
            conn.close();
            
        } catch (SQLException ex)
        {
           ex.printStackTrace();
        }
    }
    
    /**
     * 
     */
    public static void fixLocality()
    {
        String connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "kevin";
        
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        try
        {
            Statement         stmt     = conn.createStatement();
            PreparedStatement pStmt    = conn.prepareStatement("UPDATE collectingevent SET LocalityID=? WHERE CollectingEventID = ?");
            PreparedStatement delStmt  = conn.prepareStatement("DELETE FROM locality WHERE LocalityID=?");
            PreparedStatement delStmt2 = conn.prepareStatement("DELETE FROM localitydetail WHERE LocalityDetailID=?");
            PreparedStatement delStmt3 = conn.prepareStatement("DELETE FROM geocoorddetail WHERE GeocoordDetailID=?");
            
            int fixedCnt = 0;
            String sql = "SELECT LocalityName FROM (SELECT LocalityName, COUNT(LocalityName) as cnt FROM locality GROUP BY LocalityName) T1 WHERE cnt > 1 ORDER BY cnt desc";
            for (Object[] cols : BasicSQLUtils.query(sql))
            {
                String locName = cols[0].toString();
                
                sql = String.format("SELECT LocalityID FROM locality WHERE LocalityName = '%s' ORDER BY LocalityID ASC", locName);
                System.out.println("------------------------------------" + locName + "-----------------------------------");
                
                Integer firstID = null;
                int c = 0;
                ResultSet rs2 = stmt.executeQuery(sql);
                while (rs2.next())
                {
                    int id = rs2.getInt(1);
                    if (c == 0) 
                    {
                        firstID = id;
                        c = 1;
                        continue;
                    }
                    
                    System.out.println("Fixing LocalityID: "+id);
                    sql = String.format("SELECT CollectingEventId FROM collectingevent WHERE LocalityID = %d", id);
                    Vector<Integer> ids = BasicSQLUtils.queryForInts(conn, sql);
                    for (Integer ceId : ids)
                    {
                        pStmt.setInt(1, firstID);
                        pStmt.setInt(2, ceId);
                        if (pStmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating CE Id: "+ceId);
                        } else
                        {
                            System.out.println("Fixed CollectingEventID: "+ceId+"  with LocalityID: "+firstID);
                            fixedCnt++;
                        }
                    }
                    c++;
                    
                    System.out.println("Fixing LocalityID: "+id);
                    sql = String.format("SELECT LocalityDetailID FROM localitydetail WHERE LocalityID = %d", id);
                    ids = BasicSQLUtils.queryForInts(conn, sql);
                    for (Integer ldId : ids)
                    {
                        delStmt2.setInt(1, ldId);
                        if (delStmt2.executeUpdate() != 1)
                        {
                            System.out.println("Error deleting LocalityDetailID: "+id);
                        } else
                        {
                            System.out.println("Deleted LocalityDetailID: "+id);
                        }
                    }

                    System.out.println("Fixing GeocoordDetail for: "+id);
                    sql = String.format("SELECT GeocoordDetailID FROM geocoorddetail WHERE LocalityID = %d", id);
                    ids = BasicSQLUtils.queryForInts(conn, sql);
                    for (Integer ldId : ids)
                    {
                        delStmt3.setInt(1, ldId);
                        if (delStmt3.executeUpdate() != 1)
                        {
                            System.out.println("Error deleting GeocoordDetailID: "+id);
                        } else
                        {
                            System.out.println("Deleted GeocoordDetailID: "+id);
                        }
                    }

                    sql = "SELECT COUNT(*) FROM collectingevent WHERE LocalityID = " +id;
                    System.out.println(sql);
                    int ceCnt = BasicSQLUtils.getCountAsInt(sql);
                    
                    if (ceCnt == 0)
                    {
                        delStmt.setInt(1, id);
                        if (delStmt.executeUpdate() != 1)
                        {
                            System.out.println("Error deleting LocalityID: "+id);
                        } else
                        {
                            System.out.println("Deleted LocalityID: "+id);
                        }
                    } else
                    {
                        System.out.println("Can't Delete LocalityID: "+id);
                    }
                }
                rs2.close();
            }
            
            stmt.close();
            pStmt.close();
            
            System.out.println("Fixed CE Ids: "+fixedCnt);
            
        } catch (SQLException ex)
        {
           ex.printStackTrace();
        }
    }

    
    /**
     * 
     */
    public static void fixTaxa()
    {
        String connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "kevin";
        
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        try
        {
            // Fix Catalog Numbers
            String sql = "SELECT COUNT(*) FROM collectionobject WHERE CatalogNumber LIKE 'NHRS-COLE %'";
            System.out.println("CatNum to be fixed: "+BasicSQLUtils.getCountAsInt(sql));
            
            PreparedStatement pTxStmt = conn.prepareStatement("UPDATE collectionobject SET CatalogNumber=? WHERE CollectionObjectID = ?");
            sql = "SELECT CatalogNumber, CollectionObjectID FROM collectionobject WHERE CatalogNumber LIKE 'NHRS-COLE %'";
            for (Object[] cols : BasicSQLUtils.query(sql))
            {
                String catNum = cols[0].toString();
                catNum = StringUtils.replace(catNum, "COLE ", "COLE");
                
                pTxStmt.setString(1, catNum);
                pTxStmt.setInt(2, (Integer)cols[1]);
                
                if (pTxStmt.executeUpdate() != 1)
                {
                    System.out.println("Error deleting ColObjID: "+cols[1]);
                } else
                {
                    System.out.println("Fixed ColObjID: "+cols[1]);
                }
            }
            pTxStmt.close();
            
            sql = "SELECT COUNT(*) FROM collectionobject WHERE CatalogNumber LIKE 'NHRS-COLE %'";
            System.out.println("CatNum not fixed: "+BasicSQLUtils.getCountAsInt(sql));

            
            // Fix Taxon - Start by finding all the duplicate Taxon Records
            sql = "SELECT Name FROM (SELECT Name, COUNT(Name) as cnt, TaxonID FROM taxon GROUP BY Name) T1 WHERE cnt > 1 AND TaxonID > 15156 ORDER BY cnt desc";
            
            Statement         stmt    = conn.createStatement();
            PreparedStatement pStmt   = conn.prepareStatement("UPDATE determination SET TaxonID=? WHERE DeterminationID = ?");
            PreparedStatement pStmt2  = conn.prepareStatement("UPDATE determination SET PreferredTaxonID=? WHERE DeterminationID = ?");
            PreparedStatement pStmt3  = conn.prepareStatement("UPDATE taxon SET AcceptedID=? WHERE TaxonID = ?");
            PreparedStatement delStmt = conn.prepareStatement("DELETE FROM taxon WHERE TaxonID=?");
            
            int fixedCnt = 0;
            for (Object[] cols : BasicSQLUtils.query(sql))
            {
                String name = cols[0].toString();
                
                sql = String.format("SELECT COUNT(*) FROM taxon WHERE Name = '%s' ORDER BY TaxonID ASC", name);
                System.out.println("------------------------------------" + name + " - " +BasicSQLUtils.getCountAsInt(sql)+ "-----------------------------------");
                
                // Find all duplicate Taxon Objects
                sql = String.format("SELECT TaxonID FROM taxon WHERE Name = '%s' ORDER BY TaxonID ASC", name);
                
                int c = 0;
                Integer firstID = null;
                
                ResultSet rs2 = stmt.executeQuery(sql);
                while (rs2.next())
                {
                    int id = rs2.getInt(1);
                    if (c == 0) // Skip the first one which will the original
                    {
                        firstID = id;
                        c = 1;
                        continue;
                    }
                    
                    // Find all the determinations
                    sql = String.format("SELECT DeterminationId FROM determination WHERE TaxonID = %d", id);
                    System.out.println(sql);
                    
                    Vector<Integer> ids = BasicSQLUtils.queryForInts(conn, sql);
                    System.out.println("Fixing "+ids.size()+" determinations with TaxonID: "+id+" Setting to orig TaxonID: "+firstID);
                    for (Integer detId : ids)
                    {
                        pStmt.setInt(1, firstID);
                        pStmt.setInt(2, detId);
                        if (pStmt.executeUpdate() != 1)
                        {
                            System.out.println("Error updating DetId: "+detId);
                        } else
                        {
                            System.out.print(detId+", ");
                            fixedCnt++;
                        }
                    }
                    System.out.println();
                    
                    // Find all the determinations
                    sql = String.format("SELECT DeterminationId FROM determination WHERE PreferredTaxonID = %d", id, id);
                    System.out.println(sql);
                    
                    ids = BasicSQLUtils.queryForInts(conn, sql);
                    System.out.println("Fixing "+ids.size()+" determinations with PreferredTaxonID: "+id+" Setting to orig TaxonID: "+firstID);
                    for (Integer detId : ids)
                    {
                        pStmt2.setInt(1, firstID);
                        pStmt2.setInt(2, detId);
                        if (pStmt2.executeUpdate() != 1)
                        {
                            System.out.println("Error updating DetId: "+detId);
                        } else
                        {
                            System.out.print(detId+", ");
                            fixedCnt++;
                        }
                    }
                    System.out.println();
                    
                    sql = String.format("SELECT TaxonID FROM taxon WHERE AcceptedID = %d", id);
                    System.out.println(sql);
                    
                    ids = BasicSQLUtils.queryForInts(conn, sql);
                    System.out.println("Fixing "+ids.size()+" taxon with AcceptedID: "+id+" Setting to orig TaxonID: "+firstID);
                    for (Integer taxId : ids)
                    {
                        pStmt3.setInt(1, firstID);
                        pStmt3.setInt(2, taxId);
                        if (pStmt3.executeUpdate() != 1)
                        {
                            System.out.println("Error updating TaxId: "+taxId);
                        } else
                        {
                            System.out.print(taxId+", ");
                            fixedCnt++;
                        }
                    }
                    System.out.println();
                    
                    sql = "SELECT COUNT(*) FROM taxon WHERE ParentID = " +id;
                    System.out.println(sql);
                    
                    if (BasicSQLUtils.getCountAsInt(sql) == 0)
                    {
                        delStmt.setInt(1, id);
                        if (delStmt.executeUpdate() != 1)
                        {
                            System.out.println("Error deleting TaxonID: "+id);
                        } else
                        {
                            System.out.println("Deleted TaxonID: "+id);
                        }
                    } else
                    {
                        System.out.println("Unable to delete TaxonID: "+id+" it is a parent.");
                    }
                    c++;
                }
                rs2.close();
                
                int detCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) FROM determination WHERE TaxonID = " +firstID);
                if (detCnt > 0)
                {
                    System.out.println(detCnt+" Determinations still using TaxonID: "+firstID);
                }
            }
            
            stmt.close();
            pStmt.close();
            
            System.out.println("Fixed Det Ids: "+fixedCnt);
            
        } catch (SQLException ex)
        {
           ex.printStackTrace();
        }
    }

    
    /**
     * 
     */
    public static void fixGCRCatNums()
    {
        String connectStr = "jdbc:mysql://localhost/";
        
        String dbName = "gcrfish_6";
        
        DBConnection dbc = new DBConnection("root", "root", connectStr+dbName, "com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQLDialect", dbName);
        Connection conn = dbc.createConnection();
        BasicSQLUtils.setDBConnection(conn);
        
        try
        {
            // Fix Catalog Numbers
            String sql = "SELECT COUNT(*) FROM collectionobject WHERE CatalogNumber LIKE '%.%'";
            System.out.println("CatNum to be fixed: "+BasicSQLUtils.getCountAsInt(sql));
            
            int fixedCnt = 0;
            PreparedStatement pTxStmt = conn.prepareStatement("UPDATE collectionobject SET CatalogNumber=?,AltCatalogNumber=? WHERE CollectionObjectID = ?");
            sql = "SELECT CatalogNumber, CollectionObjectID FROM collectionobject WHERE CatalogNumber LIKE '%.%'";
            for (Object[] cols : BasicSQLUtils.query(sql))
            {
                String oldCatNum    = cols[0].toString();
                String newCatNum = "0" + StringUtils.replace(oldCatNum, ".", "");
                
                pTxStmt.setString(1, newCatNum);
                pTxStmt.setString(2, oldCatNum);
                pTxStmt.setInt(3, (Integer)cols[1]);
                
                if (pTxStmt.executeUpdate() != 1)
                {
                    System.out.println("Error updating ColObjID: "+cols[1]);
                } else
                {
                    System.out.println("Fixed ColObjID: "+cols[1]);
                    fixedCnt++;
                }
            }
            pTxStmt.close();
            
            System.out.println("Fixed ColObj CatNum: "+fixedCnt);
            conn.close();
            
        } catch (SQLException ex)
        {
           ex.printStackTrace();
        }
    }

    
    /**
     * @param args
     */
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        //LocalityCleanup.fixTaxa();
        LocalityCleanup.fixGCRCatNums();
    }
}
