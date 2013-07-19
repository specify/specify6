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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;

import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericGUIDGeneratorFactory;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.GeologicTimePeriod;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.Journal;
import edu.ku.brc.specify.datamodel.LithoStrat;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.ui.ProgressFrame;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostGlassPane;


/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 9, 2009
 *
 */
public class SpecifyGUIDGeneratorFactory extends GenericGUIDGeneratorFactory
{
    protected static String PREF_NAME_PREFIX = "Prefs.GUID.";
    protected static int[]  TABLE_IDS = {Attachment.getClassTableId(), CollectionObject.getClassTableId(), CollectingEvent.getClassTableId(), 
                                         LithoStrat.getClassTableId(), Locality.getClassTableId(), Agent.getClassTableId(),  
                                         ReferenceWork.getClassTableId(), Journal.getClassTableId(), GeologicTimePeriod.getClassTableId(), 
                                         Collection.getClassTableId(), Institution.getClassTableId(), Determination.getClassTableId(), };
    // Not including Taxon.getClassTableId(), Geography.getClassTableId(), 
    
    protected String              I18NPre   = SpecifyGUIDGeneratorFactory.class.getSimpleName();

    protected StringBuilder       errMsg        = new StringBuilder();
    protected String              lsidAuthority = null;
    protected String              instCode      = null;
    protected String              colCode       = null;
    
    protected ProgressFrame       frame;
    protected PrintWriter         pw            = null;
    
    // For estimating time
    protected int                 totalCnt      = 0;
    protected int                 currCnt       = 0;
    protected long                startTime     = 0;
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericGUIDGeneratorFactory#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return super.getErrorMsg();
    }

    /**
     * @param tableId the table id
     * @return the Category for a table id or null
     */
    protected CATEGORY_TYPE getCategoryFromTableId(final int tableId)
    {
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            if (tableId == TABLE_IDS[cat.ordinal()])
            {
                return cat;
            }
        } 
        return null;
    }
    
    /**
     * @param tableId
     * @return
     */
    public boolean isPrefOn(final int tableId)
    {
        if (AppContextMgr.getInstance().hasContext())
        {
            CATEGORY_TYPE cat = getCategoryFromTableId(tableId);
            if (cat != null)
            {
                return AppPreferences.getRemote().getBoolean(PREF_NAME_PREFIX + cat.toString(), false);
            }
        } else
        {
            UIRegistry.showError("No Context set!");
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericGUIDGeneratorFactory#buildGUIDs(java.beans.PropertyChangeListener)
     */
    @Override
    public void buildGUIDs(final PropertyChangeListener pcl)
    {
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddhhmmss");
        File outFile = new File(UIRegistry.getAppDataDir() + File.separator + String.format("guids_%s.txt", sf.format(Calendar.getInstance().getTime())));
        try
        {
            pw = new PrintWriter(outFile);
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
        
        if (frame != null) frame.setDesc(getResourceString("SpecifyGUIDGeneratorFactory.UPDATING_GUIDS"));

        setProgressValue(true, 0, 100); // Sets Overall Progress to 0 -> 100
        
        Connection conn = DBConnection.getInstance().getConnection();
        
        totalCnt = 0;
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            totalCnt += getRecordCount(conn, cat);
        }
        System.out.println(String.format("Total Records: %d", totalCnt));
        
        startTime = System.currentTimeMillis();
        
        int count = 1;
        double tot = CATEGORY_TYPE.values().length;
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(this, "COUNT", 0, count));
            }
            
            try
            {
                buildGUIDs(conn, cat);
                
            } catch (Exception ex)
            {
                 ex.printStackTrace();
                 
            } finally
            {
                if (pcl != null)
                {
                    pcl.propertyChange(new PropertyChangeEvent(this, "COUNT", 0, count));
                }

            }
            setProgressValue(true, (int)(((double)count / tot)*100.0));
            count++;
        }
        
        pw.close();
    }
    
    /**
     * @param isOverall
     * @param values
     */
    private void setProgressDesc(final String msg)
    {
        if (frame != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    frame.setDesc(msg);
                }
            });
        }
    }
    
    /**
     * @param isOverall
     * @param values
     */
    private void setProgressValue(final boolean isOverall, final int...values)
    {
        if (frame != null)
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    if (isOverall)
                    {
                        if (values.length == 1)
                        {
                            frame.setOverall(values[0]);
                        } else
                        {
                            frame.getOverallProgress().setIndeterminate(false);
                            frame.setOverall(values[0], values[1]);
                        }
                    } else
                    {
                        if (values.length == 1)
                        {
                            frame.setProcess(values[0]);
                        } else
                        {
                            frame.getProcessProgress().setIndeterminate(false);
                            frame.setProcess(values[0], values[1]);
                        }
                    }
                }
            });
        }
    }
    
    /**
     * @param connection
     * @param category
     * @return
     */
    private int getRecordCount(final Connection    connection,
                               final CATEGORY_TYPE category)
    {
        int count = 0;
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[category.ordinal()]);
        if (tableInfo != null)
        {
            String origScope = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false);
            
            // Do all Records
            String where =  origScope != null ? "WHERE " + origScope : "";
            
            String sql   = String.format("SELECT COUNT(*) FROM %s %s", tableInfo.getName(), where);
            count = BasicSQLUtils.getCountAsInt(sql);
            System.out.println(sql);
            System.out.println(tableInfo.getName()+" -> "+count);
        }
        return count;
    }
        
    /**
     * @param connection
     * @param category
     * @param doVersioning
     * @param formatter
     * @return
     */
    private void buildGUIDs(final Connection    connection,
                            final CATEGORY_TYPE category)
    {
        int count = 0;
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[category.ordinal()]);
//        if (CollectionObject.getClassTableId() == TABLE_IDS[category.ordinal()])
//        {
//            System.out.println("");
//        }
        if (tableInfo != null)
        {
            String origScope = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false);
            boolean isScopeNotEmpty = origScope != null;
            
            String scope = (isScopeNotEmpty ? ("AND " + origScope) : "");
            
            System.out.println(tableInfo.getTitle()+" -> "+scope);
            String where = "GUID IS NOT NULL " + scope;
            String sql   = String.format("SELECT %s,GUID FROM %s WHERE %s", tableInfo.getIdColumnName(), tableInfo.getName(), where);
            System.out.println(sql);
            
            Statement         stmt    = null;
            PreparedStatement updStmt = null;
            try
            {
                stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    pw.write(String.format("%d\t%d\t%s\n", tableInfo.getTableId(), rs.getInt(1), rs.getString(2)));
                }
                rs.close();
                stmt.close();
                stmt = null;
                pw.flush();
                
                // Do all Records
                where = isScopeNotEmpty ? "WHERE " + origScope : "";
                
                sql   = String.format("SELECT COUNT(*) FROM %s %s", tableInfo.getName(), where);
                count = BasicSQLUtils.getCountAsInt(sql);
                System.out.println(sql);
                System.out.println(tableInfo.getName()+" -> "+count);
                
                // Set all GUIDs
                int percentage = count / 50;
                if (percentage == 0) percentage = 1;
                
                setProgressValue(false, 0, 100);
                
                sql = String.format("SELECT %s,Version FROM %s %s", tableInfo.getIdColumnName(), tableInfo.getName(), where);
                System.out.println(sql);

                String updateStr = String.format("UPDATE %s SET GUID=?, VERSION=? WHERE %s=?", tableInfo.getName(), tableInfo.getIdColumnName());
                System.out.println(updateStr);
                stmt    = connection.createStatement();
                updStmt = connection.prepareStatement(updateStr);

                int cnt = 0;
                rs = stmt.executeQuery(sql);
                while (rs.next())
                {
                    Integer id      = rs.getInt(1);
                    int     version = rs.getInt(2) + 1;
                    UUID    uuid    = UUID.randomUUID();
                    
                    updStmt.setString(1, uuid.toString());
                    updStmt.setInt(2, version);
                    updStmt.setInt(3, id);
                    
                    if (updStmt.executeUpdate() != 1)
                    {
                        String msg = "Error updating table["+tableInfo.getName()+"] field["+tableInfo.getIdFieldName()+"] with GUID.";
                        System.err.println(msg);
                    }
                    cnt++;
                    currCnt++;
                    
                    if (frame != null && cnt % percentage == 0)
                    {
                        int percnt = (int)(((double)cnt / count)*100.0);
                        setProgressValue(false, percnt);
                        
                        double elapsedTime   = System.currentTimeMillis() - startTime;
                        double timePerRecord = elapsedTime / (double)currCnt;
                        double remainingTime = (((totalCnt - currCnt) * timePerRecord) / 60000.0); // result in minutes
                        int    seconds       = (int)((remainingTime - ((int)remainingTime)) * 60.0);
                        setProgressDesc(String.format("Updating GUIDs - Minutes remaining: %d:%02d", (int)remainingTime, seconds));
                    }
                }
                rs.close();
                if (frame != null) frame.setProcess(count);
           
            } catch (SQLException e)
            {
                e.printStackTrace();
                
            } finally
            {
                try
                {
                    if (stmt != null) stmt.close();
                    if (updStmt != null) updStmt.close();
                } catch (SQLException e) {}
            }
        }
    }
    
    /**
     * @param pcl
     */
    public static void buildAllGUIDsAynch(final PropertyChangeListener pcl)
    {
        buildAllGUIDsAynch(pcl, null);
    }

    /**
     * @param pcl
     * @param classes
     */
    public static void buildAllGUIDsAynch(final PropertyChangeListener pcl, final ArrayList<Class<?>> classes)
    {
        final String COUNT = "COUNT";
        
        final GhostGlassPane glassPane = UIRegistry.writeGlassPaneMsg(getResourceString("SETTING_GUIDS"), UIRegistry.STD_FONT_SIZE);
        glassPane.setProgress(0);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
                if (SubPaneMgr.getInstance().aboutToShutdown())
                {
                    Taskable task = TaskMgr.getTask("Startup");
                    if (task != null)
                    {
                        SubPaneIFace splash = edu.ku.brc.specify.tasks.StartUpTask.createFullImageSplashPanel(task.getTitle(), task);
                        SubPaneMgr.getInstance().addPane(splash);
                        SubPaneMgr.getInstance().showPane(splash);
                    }
                    
                    final GUIDWorker worker = new GUIDWorker()
                    {
                        protected Integer doInBackground() throws Exception
                        {
                            if (getInstance() instanceof SpecifyGUIDGeneratorFactory)
                            {
                                SpecifyGUIDGeneratorFactory guidGen = (SpecifyGUIDGeneratorFactory)getInstance();
                                guidGen.buildGUIDs(this);
                            }
                            return null;
                        }
                        
                        @Override
                        public void propertyChange(PropertyChangeEvent evt)
                        {
                            if (evt.getPropertyName().equals("COUNT"))
                            {
                                firePropertyChange("COUNT", 0, evt.getNewValue());
                            }
                        }

                        @Override
                        protected void done()
                        {
                            glassPane.setProgress(100);
                            UIRegistry.clearGlassPaneMsg();
                            if (pcl != null)
                            {
                                pcl.propertyChange(new PropertyChangeEvent(this, "complete", "true", "true"));
                            }
                        }
                    };
                    
                    worker.addPropertyChangeListener(
                        new PropertyChangeListener() {
                            public  void propertyChange(final PropertyChangeEvent evt) {
                                if (COUNT.equals(evt.getPropertyName())) 
                                {
                                    glassPane.setProgress((int)(((Integer)evt.getNewValue() * 100.0) / (double)CATEGORY_TYPE.values().length));
                                }
                            }
                        });
                    worker.execute();
                }
            }
        });

    }
    
    /**
     * @param frame the frame to set
     */
    public void setFrame(final ProgressFrame frame)
    {
        this.frame = frame;
    }

    /**
     *
     */
    protected static class GUIDWorker extends javax.swing.SwingWorker<Integer, Integer> implements PropertyChangeListener
    {
        /* (non-Javadoc)
         * @see javax.swing.SwingWorker#doInBackground()
         */
        @Override
        protected Integer doInBackground() throws Exception
        {
            return null;
        }
        
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            
        }
        
        @Override
        protected void done()
        {
            super.done();
        }
    }
}
