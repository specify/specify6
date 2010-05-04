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
package edu.ku.brc.specify.conversion;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.DesertBlue;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.MySQLDMBSUserMgr;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.Specify;
import edu.ku.brc.specify.dbsupport.PostInsertEventListener;
import edu.ku.brc.specify.ui.AppBase;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.AttachmentUtils;
import edu.ku.brc.util.Pair;

public class GroupPermittedToViewFixer extends AppBase
{
    private static final Logger log = Logger.getLogger(ConvertVerifier.class);
    
    private Pair<String, String> itUsrPwd          = new Pair<String, String>(null, null);
    private String               hostName          = "localhost";
    private Pair<String, String> namePairToConvert = null;
    
    private boolean              dbgStatus         = false;
    private boolean              compareTo6DBs      = false;
    
    
    private Connection                                    oldDBConn;
    private Connection                                    newDBConn;

    private int                                           numErrors = 0;
    private static SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    private boolean                                       debug = false;
    private static ProgressFrame                          progressFrame;
    
    private ConversionLogger                              convLogger = new ConversionLogger();
    private TableWriter                                   tblWriter  = null;
    
    
    /**
     * 
     */
    public GroupPermittedToViewFixer()
    {
        super();
        
        PostInsertEventListener.setAuditOn(false);
        
        setUpSystemProperties();
        
        AppContextMgr.getInstance().setHasContext(true);
        
        // Load Local Prefs
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.setDirPath(UIRegistry.getAppDataDir());

        // Then set this
        IconManager.setApplicationClass(Specify.class);
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_datamodel.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_plugins.xml")); //$NON-NLS-1$
        IconManager.loadIcons(XMLHelper.getConfigDir("icons_disciplines.xml")); //$NON-NLS-1$
        
        appIcon = new JLabel("  "); //$NON-NLS-1$
        setAppIcon(null); //$NON-NLS-1$
    }

    /**
     * @param databaseNameSource
     * @param databaseNameDest
     * @throws Exception
     */
    public void fixDB(final String databaseNameSource, 
                      final String databaseNameDest) throws Exception
    {
        convLogger.initialize("verify", databaseNameDest);
        
        String title = "From "+databaseNameSource+" to "+databaseNameDest;
        System.out.println("************************************************************");
        System.out.println(title);
        System.out.println("************************************************************");
        
        HibernateUtil.shutdown();    
        
        Properties initPrefs = BuildSampleDatabase.getInitializePrefs(databaseNameDest);
        
        String driverNameSource   = "";
        String databaseHostSource = "";
        DatabaseDriverInfo driverInfoSource = null;
        
        String driverNameDest   = "";
        String databaseHostDest = "";
        DatabaseDriverInfo driverInfoDest = null;
        
        log.debug("Running an non-custom MySQL convert, using old default login creds");
        driverNameSource    = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostSource  = initPrefs.getProperty("initializer.host",     "localhost"); 
        
        driverNameDest      = initPrefs.getProperty("initializer.driver",   "MySQL");
        databaseHostDest    = initPrefs.getProperty("initializer.host",     "localhost");  
    
        log.debug("Custom Convert Source Properties ----------------------");
        log.debug("databaseNameSource: " + databaseNameSource);        
        log.debug("driverNameSource: " + driverNameSource);
        log.debug("databaseHostSource: " + databaseHostSource);
        
        log.debug("Custom Convert Destination Properties ----------------------");
        log.debug("databaseNameDest: " + databaseNameDest);
        log.debug("driverNameDest: " + driverNameDest);
        log.debug("databaseHostDest: " + databaseHostDest);

        driverInfoSource = DatabaseDriverInfo.getDriver(driverNameSource);
        driverInfoDest = DatabaseDriverInfo.getDriver(driverNameDest);
        
        if (driverInfoSource == null)
        {
            throw new RuntimeException("Couldn't find Source DB driver by name ["+driverInfoSource+"] in driver list.");
        }
        if (driverInfoDest == null)
        {
            throw new RuntimeException("Couldn't find Destination driver by name ["+driverInfoDest+"] in driver list.");
        }
        
        if (driverNameDest.equals("MySQL"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.MySQL;
        else if (driverNameDest.equals("SQLServer"))BasicSQLUtils.myDestinationServerType = BasicSQLUtils.SERVERTYPE.MS_SQLServer;
        
        if (driverNameSource.equals("MySQL"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.MySQL;
        else if (driverNameSource.equals("SQLServer"))BasicSQLUtils.mySourceServerType = BasicSQLUtils.SERVERTYPE.MS_SQLServer;
        
        else 
        {
            log.error("Error setting ServerType for destination database for conversion.  Could affect the"
                    + " way that SQL string are generated and executed on differetn DB egnines");
        }
        String destConnectionString = driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, "", itUsrPwd.first, itUsrPwd.second, driverNameDest);
        log.debug("attempting login to destination: " + destConnectionString);
        // This will log us in and return true/false
        // This will connect without specifying a DB, which allows us to create the DB
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                destConnectionString,
                itUsrPwd.first, 
                itUsrPwd.second))
        {
            log.error("Failed connection string: "  +driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest) );
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        convLogger.setIndexTitle(databaseNameDest + " Verify "+(new SimpleDateFormat("yyy-MM-dd hh:mm:ss")).format(Calendar.getInstance().getTime()));
        
        log.debug("DESTINATION driver class: " + driverInfoDest.getDriverClassName());
        log.debug("DESTINATION dialect class: " + driverInfoDest.getDialectClassName());               
        log.debug("DESTINATION Connection String: " + driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest)); 
        
        // This will log us in and return true/false
        if (!UIHelper.tryLogin(driverInfoDest.getDriverClassName(), 
                driverInfoDest.getDialectClassName(), 
                databaseNameDest, 
                driverInfoDest.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostDest, databaseNameDest, itUsrPwd.first, itUsrPwd.second, driverNameDest),                 
                itUsrPwd.first, 
                itUsrPwd.second))
        {
            throw new RuntimeException("Couldn't login into ["+databaseNameDest+"] "+DBConnection.getInstance().getErrorMsg());
        }
        
        String srcConStr = driverInfoSource.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, databaseHostSource, databaseNameSource, itUsrPwd.first, itUsrPwd.second, driverNameSource);
        DBConnection oldDB = DBConnection.createInstance(driverInfoSource.getDriverClassName(), null, databaseNameSource, srcConStr, itUsrPwd.first, itUsrPwd.second);
        oldDBConn = oldDB.getConnection();
        if (oldDBConn == null)
        {
            throw new RuntimeException(oldDB.getErrorMsg());
        }
        newDBConn = DBConnection.getInstance().createConnection();
        
        long startTime = System.currentTimeMillis();

        
        progressFrame = new ProgressFrame("Checking Catalog Objects....");
        progressFrame.adjustProgressFrame();
        
        progressFrame.setOverall(0);
        progressFrame.setDesc("");

        UIHelper.centerAndShow(progressFrame);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                UIHelper.centerAndShow(progressFrame);
            }
        });
        
        tblWriter = convLogger.getWriter("GPTVReport.html", "Group Permitted To View");
        
        IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
        
        String[] tblNamesOld  = {"CollectionObject", "CollectingEvent", "Locality", "TaxonName"};
        String[] tblNamesNew  = {"CollectionObject", "CollectingEvent", "Locality", "Taxon"};
        for (int i=0;i<tblNamesOld.length;i++)
        {
            progressFrame.setDesc(tblNamesOld[i]);
            
            String oldTblName = tblNamesOld[i];
            String newTblName = tblNamesNew[i];
            String oldTblNameLower = oldTblName.toLowerCase();
            String newTblNameLower = newTblName.toLowerCase();
            String oldKeyName = oldTblName + "ID";
            String newKeyName = newTblName + "ID";
            
            int colCnt = BasicSQLUtils.getCount(oldDBConn, String.format("SELECT count(*) FROM `INFORMATION_SCHEMA`.`COLUMNS` " +
            		                            "WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s' AND COLUMN_NAME = 'GroupPermittedToView'", databaseNameSource, oldTblName));
            if (colCnt == 1)
            {
                String sql = String.format("SELECT COUNT(*) FROM %s.%s WHERE GroupPermittedToView is NOT NULL", databaseNameSource, oldTblName);
                int cnt = BasicSQLUtils.getCount(oldDBConn, sql);
                if (cnt > 0)
                {
                    tblWriter.log(String.format("%s %s  Count: %d", databaseNameSource, oldTblNameLower, cnt));
                    
                    int totalCnt = cnt;
                    int cnvCnt   = 0;
                    
                    progressFrame.setProcess(0, cnt);
                    
                    IdMapperMgr.getInstance().addTableMapper(oldTblNameLower, oldKeyName, false);
                    IdTableMapper mapper = (IdTableMapper)IdMapperMgr.getInstance().get(oldTblNameLower, oldKeyName);
                    
                    PreparedStatement pStmt = null;
                    try
                    {
                        pStmt = newDBConn.prepareStatement(String.format("UPDATE %s SET Visibility=? WHERE %s = ?", newTblNameLower, newKeyName));
                        
                        cnt = 0;
                        sql = String.format("SELECT %s,GroupPermittedToView FROM %s.%s WHERE GroupPermittedToView is NOT NULL", oldKeyName, databaseNameSource, oldTblName);
                        //System.out.println(sql);
                        for (Object[] col : BasicSQLUtils.query(sql))
                        {
                            Integer oldId = (Integer)col[0];
                            Integer newId = mapper.get(oldId);
                            if (newId != null)
                            {
                                Integer gptv = (Integer)col[1];
                                try
                                {
                                    pStmt.setInt(1, gptv);
                                    pStmt.setInt(2, newId);
                                    pStmt.execute();
                                    cnvCnt++;
                                    
                                } catch (SQLException ex)
                                {
                                    ex.printStackTrace();
                                }
                            } else
                            {
                                tblWriter.logError("No mapping for Old Id " + oldId);
                            }
                            cnt++;
                            if (cnt % 10 == 0)
                            {
                                progressFrame.setProcess(cnt);
                            }
                        }
                        progressFrame.setProcess(totalCnt);
                        tblWriter.log(String.format("%s %s  Converted: %d", databaseNameSource, oldTblNameLower, cnvCnt));
                        
                    } catch (SQLException ex)
                    {
                        ex.printStackTrace();
                    } finally
                    {
                        if (pStmt != null)
                        {
                            pStmt.close();
                        }
                    }
                }
            }
        }

        newDBConn.close();
        oldDBConn.close();
        
        
        File indexFile = convLogger.closeAll();
        
        log.info("Done.");
        
        progressFrame.setVisible(false);
        
        AttachmentUtils.openURI(indexFile.toURI());
        
        System.exit(0);
    }
    
    
    
    /**
     * Loads the dialog
     * @param hashNames every other one is the new name
     * @return the list of selected DBs
     */
    private boolean selectedDBsToConvert()
    {
        final JTextField     itUserNameTF = UIHelper.createTextField("root", 15);
        final JPasswordField itPasswordTF = UIHelper.createPasswordField("", 15);
        
        final JTextField     hostNameTF = UIHelper.createTextField("localhost", 15);

        CellConstraints cc = new CellConstraints();
        PanelBuilder pb = new PanelBuilder(new FormLayout("p,2px,p,f:p:g", "p,2px,p,2px,p,8px,p"));
        
        int y = 1;
        pb.addSeparator("IT User", cc.xyw(1, y, 4)); y += 2;
        pb.add(UIHelper.createLabel("Username:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itUserNameTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Password:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(itPasswordTF, cc.xy(3, y)); y += 2;

        pb.add(UIHelper.createLabel("Host Name:", SwingConstants.RIGHT), cc.xy(1, y));
        pb.add(hostNameTF, cc.xy(3, y)); y += 2;
        
        PanelBuilder panel = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "f:p:g"));
        panel.add(new JLabel(IconManager.getIcon("SpecifyLargeIcon")), cc.xy(1, 1));
        panel.add(pb.getPanel(), cc.xy(3, 1));

        CustomDialog dlg = new CustomDialog(null, "Database Info", true, panel.getPanel());
        ((JPanel)dlg.getContentPanel()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        UIHelper.centerAndShow(dlg);
        
        dlg.dispose();
        if (dlg.isCancelled())
        {           
           return false;
        }
        
        hostName        = hostNameTF.getText();
        itUsrPwd.first  = itUserNameTF.getText();
        itUsrPwd.second = ((JTextField)itPasswordTF).getText();
        
        return true;
    }
    
    /**
     * @return
     * @throws SQLException
     */
    private Pair<String, String> chooseTable() throws SQLException
    {
        MySQLDMBSUserMgr mgr = new MySQLDMBSUserMgr();
        
        final Vector<DBNamePair> availOldPairs = new Vector<DBNamePair>();
        final Vector<DBNamePair> availNewPairs = new Vector<DBNamePair>();
        
        try
        {
            if (mgr.connectToDBMS(itUsrPwd.first, itUsrPwd.second, hostName))
            {
                BasicSQLUtils.setSkipTrackExceptions(true);
                
                Connection conn = mgr.getConnection();
                Vector<Object[]> dbNames = BasicSQLUtils.query(conn, "show databases");
                for (Object[] row : dbNames)
                {
                    System.err.println("Setting ["+row[0].toString()+"] ");
                    conn.setCatalog(row[0].toString());
                    
                    boolean isSp5 = false;
                    boolean isSp6 = false;
                    
                    Vector<Object[]> tables = BasicSQLUtils.query(conn, "show tables");
                    for (Object[] tblRow : tables)
                    {
                        if (row[0].toString().equals("debugdb"))
                        {
                            System.err.println(tblRow[0].toString());
                        }
                        if (tblRow[0].toString().equals("usysversion"))
                        {
                            isSp5 = true;
                            break;
                            
                        } else if (tblRow[0].toString().equals("gift"))
                        {
                            isSp6 = true;
                            break;
                        }
                    }
                    
                    if (isSp5 || isSp6)
                    {
                        String collName = null;
                        Vector<Object[]> tableDesc = BasicSQLUtils.query(conn, "SELECT CollectionName FROM collection");
                        if (tableDesc.size() > 0)
                        {
                            collName =  tableDesc.get(0)[0].toString();
                        }
                        
                        if (collName == null)
                        {
                            continue;
                        }
        
                        if (isSp5)
                        {
                            availOldPairs.add(new DBNamePair(collName, row[0].toString()));
                        } else
                        {
                            availNewPairs.add(new DBNamePair(collName, row[0].toString()));
                        }
                    }
                    
                    System.err.println("isSp5 ["+isSp5+"] isSp6 ["+isSp6+"] ");
                }
                
                Comparator<Pair<String, String>> comparator =  new Comparator<Pair<String, String>>() {
                    @Override
                    public int compare(Pair<String, String> o1, Pair<String, String> o2)
                    {
                        return o1.second.compareTo(o2.second);
                    }
                };
                Collections.sort(availOldPairs, comparator);
                Collections.sort(availNewPairs, comparator);
                
                mgr.close();
                BasicSQLUtils.setSkipTrackExceptions(false);
                
                final JList     oldlist = new JList(availOldPairs);
                final JList     newList = new JList(availNewPairs);
                CellConstraints cc   = new CellConstraints();
                PanelBuilder    pb   = new PanelBuilder(new FormLayout("f:p:g,10px,f:p:g", "p,2px,f:p:g,4px,p"));
                pb.addSeparator("Specify 5 Databases",     cc.xy(1,1));
                pb.add(UIHelper.createScrollPane(oldlist), cc.xy(1,3));
                
                pb.addSeparator("Specify 6 Databases",     cc.xy(3,1));
                pb.add(UIHelper.createScrollPane(newList), cc.xy(3,3));
                
                
                ListSelectionListener oldDBListener = new ListSelectionListener()
                {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            DBNamePair pair = (DBNamePair)oldlist.getSelectedValue();
                            if (pair != null)
                            {
                                int index = 0;
                                for (DBNamePair p : availNewPairs)
                                {
                                    if (p.second.startsWith(pair.second))
                                    {
                                        final int inx = index;
                                        SwingUtilities.invokeLater(new Runnable(){
                                            @Override
                                            public void run()
                                            {
                                                newList.setSelectedIndex(inx);
                                                newList.ensureIndexIsVisible(inx);
                                            }
                                        });
                                    }
                                    index++;
                                }
                            }
                        }
                    }
                };
                
                oldlist.getSelectionModel().addListSelectionListener(oldDBListener);
    
                pb.setDefaultDialogBorder();
                
                final CustomDialog dlg = new CustomDialog(null, "Select a DB to Verify", true, pb.getPanel());
                
                ListSelectionListener lsl = new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            dlg.getOkBtn().setEnabled(oldlist.getSelectedIndex() > -1);
                        }
                    }
                };
                oldlist.addListSelectionListener(lsl);
                newList.addListSelectionListener(lsl);
                
                oldlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                newList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                
                MouseAdapter listMA = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e)
                    {
                        if (e.getClickCount() == 2)
                        {
                            dlg.getOkBtn().setEnabled(oldlist.getSelectedIndex() > -1 && newList.getSelectedIndex() > -1);
                            dlg.getOkBtn().doClick();
                        }
                    }
                };
                oldlist.addMouseListener(listMA);
                newList.addMouseListener(listMA);
                
                dlg.createUI();
                dlg.pack();
                //dlg.setSize(300, 800);
                dlg.pack();
                dlg.setVisible(true);
                if (dlg.isCancelled())
                {
                    return null;
                }
                
                DBNamePair oldPair = (DBNamePair)oldlist.getSelectedValue();
                namePairToConvert = (DBNamePair)newList.getSelectedValue();
                namePairToConvert.first = oldPair.second;
                return namePairToConvert;
            }
        } catch (Exception ex)
        {
            
        }
        return null;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        
        UIRegistry.setAppName("Specify");

        // Create Specify Application
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                try
                {
                    if (!System.getProperty("os.name").equals("Mac OS X"))
                    {
                        UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                        PlasticLookAndFeel.setPlasticTheme(new DesertBlue());
                    }
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ConvertVerifier.class, e);
                    log.error("Can't change L&F: ", e);
                }
                
                final GroupPermittedToViewFixer cv = new GroupPermittedToViewFixer();
                
                if (cv.selectedDBsToConvert())
                {
                    try
                    {
                        final Pair<String, String> pair = cv.chooseTable();
                        if (pair != null)
                        {
                            SwingWorker workerThread = new SwingWorker()
                            {
                                @Override
                                public Object construct()
                                {
                                    try
                                    {
                                        cv.fixDB( pair.first, pair.second);
                                        
                                    } catch (Exception ex)
                                    {
                                        ex.printStackTrace();
                                    }
                                    return null;
                                }
                                
                                @Override
                                public void finished()
                                {
                                }
                            };
                            
                            // start the background task
                            workerThread.start();
                            
                        } else
                        {
                            JOptionPane.showMessageDialog(null, "The GroupPermittedToViewFixer was unable to login", "Not Logged In", JOptionPane.ERROR_MESSAGE);
                            System.exit(0);
                        }
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        System.exit(0);
                    }
                }
               
            }
        });

    }
    
    //----------------------------------------
    class DBNamePair extends Pair<String, String>
    {
        
        /**
         * 
         */
        public DBNamePair()
        {
            super();
        }

        /**
         * @param first
         * @param second
         */
        public DBNamePair(String first, String second)
        {
            super(first, second);
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.util.Pair#toString()
         */
        @Override
        public String toString()
        {
            return second + "   ("+ first + ")";
        }
        
    }


}
