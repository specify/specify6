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
package edu.ku.brc.af.core.db;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.SimpleGlassPane;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 2, 2008
 *
 */
public class MySQLBackupService extends BackupServiceFactory
{
    private PropertyChangeListener listener = null;
    private int                    numTables;
    
    /**
     * 
     */
    public MySQLBackupService()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#getNumberofTables()
     */
    public int getNumberofTables()
    {
        Connection dbConnection = DBConnection.getInstance().createConnection();
        Statement dbStatement = null;
        try
        {
            dbConnection = DBConnection.getInstance().createConnection();
            if (dbConnection != null)
            {
                dbStatement = dbConnection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
                ResultSet resultSet = dbStatement.executeQuery("show tables");
                
                ResultSetMetaData metaData = resultSet.getMetaData();
                numTables = 0;
                while (resultSet.next())
                {
                    for (int i=0;i<metaData.getColumnCount();i++)
                    {
                        //System.out.print(resultSet.getString(i+1)+"\t");
                        numTables++;
                    }
                    //System.out.println();
                }
                //System.out.println(rows);
                resultSet.close();
                return numTables;
            }
        } catch (SQLException ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (dbStatement != null)
                {
                    dbStatement.close();
                }
                if (dbConnection != null)
                {
                    dbConnection.close();
                }
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return -1;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doBackUp()
     */
    @Override
    public void doBackUp(final PropertyChangeListener listenerArg)
    {
        final String STATUSBAR_NAME = "BackUp";
        
        this.listener = listenerArg;
        
        getNumberofTables();
        
        final JPanel oldGlassPane = UIRegistry.getGlassPane();
        
        ((JFrame)UIRegistry.getTopWindow()).setGlassPane(oldGlassPane);
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                int count = 0;
                BufferedWriter backupOut = null;
                try
                {
                    Thread.sleep(1000);
                    
                    Process process = Runtime.getRuntime().exec("mysqldump -u Specify -p testfish");
                    
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
                    
                    // wait as long it takes till the other process has prompted.
                    Thread.sleep(100);
                    out.write("Specify");
                    out.write("\n");
                    out.flush();
                    out.close();
                    
                    backupOut = new BufferedWriter(new FileWriter("backup.sql"));
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null)
                    {
                        if (line != null)
                        {
                            if (line.startsWith("CREATE TABLE"))
                            {
                                count++;
                                System.out.println(count);
                                setProgress((int)(100.0 * count / numTables));
                            }
                            backupOut.write(line);
                        }
                    }
                    setProgress(100);
                    
                    in = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    line = null;
                    while ((line = in.readLine()) != null)
                    {
                        System.err.println(line);
                    }
                    System.out.println(process.exitValue());
                    
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                } finally
                {
                    if (backupOut != null)
                    {
                        try
                        {
                            backupOut.flush();
                            backupOut.close();
                            
                        } catch (IOException ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
                System.out.println("Done");
                
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.getStatusBar().setProgressDone(STATUSBAR_NAME);
                //UIRegistry.clearGlassPaneMsg();
                
                ((JFrame)UIRegistry.getTopWindow()).setGlassPane(oldGlassPane);
            }
        };
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setProgressRange(STATUSBAR_NAME, 0, 100);
        
        SimpleGlassPane glassPane = new SimpleGlassPane(getResourceString("BackupTask.BACKINGUP"), 24);
        
        ((JFrame)UIRegistry.getTopWindow()).setGlassPane(glassPane);
        glassPane.setVisible(true);
        
        backupWorker.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public  void propertyChange(final PropertyChangeEvent evt) {
                        if ("progress".equals(evt.getPropertyName())) 
                        {
                            statusBar.setValue(STATUSBAR_NAME, (Integer)evt.getNewValue());
                        }
                    }
                });
        backupWorker.execute();
    }
    
    /**
     * @return
     */
    public void backup()
    {

        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.db.BackupServiceFactory#doRestore(edu.ku.brc.af.core.Taskable)
     */
    @Override
    public void doRestore(final PropertyChangeListener listenerArg)
    {
        this.listener = listenerArg;
    }

    
    
}
