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
package edu.ku.brc.specify.config;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericLSIDGeneratorFactory;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
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
public class SpecifyLSIDGeneratorFactory extends GenericLSIDGeneratorFactory
{
    protected static String PREF_NAME_PREFIX = "Prefs.LSID.";
    protected static int[]  TABLE_IDS = {1, 4, 3, 10, 100, 2, 5, 69, 51};
    protected static boolean doAllRecords = false;
    
    protected String       I18NPre   = SpecifyLSIDGeneratorFactory.class.getSimpleName();

    protected StringBuilder errMsg   = new StringBuilder();
    
    protected String        lsidAuthority = null;
    protected String        instCode = null;
    protected String        colCode  = null;
    
    protected ArrayList<Class<?>> classes = null;
    protected ProgressFrame       frame;
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#isReady()
     */
    @Override
    public boolean isReady()
    {
         return true;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getErrorMsg()
     */
    @Override
    public String getErrorMsg()
    {
        return super.getErrorMsg();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#reset()
     */
    @Override
    public void reset()
    {
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.CollectionObjLSIDGenFactory#getLSID(java.lang.String)
     */
    @Override
    public String createLSID(final CATEGORY_TYPE category, final String id)
    {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericLSIDGeneratorFactory#getLSID(edu.ku.brc.af.core.GenericLSIDGeneratorFactory.CATEGORY_TYPE, java.lang.String, int)
     */
//    @Override
//    public String createLSID(final CATEGORY_TYPE category, final String id, final int version)
//    {
//        if (isReady() && category != null && StringUtils.isNotEmpty(id))
//        {
//            return String.format("urn:lsid:%s:%s-%s:%s:%s:%d", lsidAuthority, instCode, colCode, category.toString(), id, version);
//        }
//        return super.createLSID(category, id, version);
//    }
    
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
     * @see edu.ku.brc.af.core.GenericLSIDGeneratorFactory#buildLSIDs(java.beans.PropertyChangeListener)
     */
    @Override
    public void buildLSIDs(final PropertyChangeListener pcl)
    {
        if (frame != null) frame.setDesc("Updating LSIDs/GUIDs..."); // I18N

        reset();
        
        setProgressValue(true, 0, 100); // Sets Overall Progress to 0 -> 100
        
        int count = 1;
        double tot = CATEGORY_TYPE.values().length;
        for (CATEGORY_TYPE cat : CATEGORY_TYPE.values())
        {
            String pName = PREF_NAME_PREFIX + cat.toString();
            
            if (pcl != null)
            {
                pcl.propertyChange(new PropertyChangeEvent(this, "COUNT", 0, count));
            }
            
            if (classes != null || 
                (AppContextMgr.getInstance().hasContext() && AppPreferences.getRemote().getBoolean(pName, false)))
            {
                try
                {
                    buildLSIDs(DBConnection.getInstance().getConnection(), cat, false, null);
                    
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
            }
            setProgressValue(true, (int)(((double)count / tot)*100.0));
            count++;
        }
        classes = null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.GenericLSIDGeneratorFactory#setLSIDOnId(edu.ku.brc.af.ui.forms.FormDataObjIFace, boolean, edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace)
     */
    @Override
    public String setLSIDOnId(final FormDataObjIFace      data,
                              final boolean               doVersioning,
                              final UIFieldFormatterIFace formatter)
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(data.getTableId());
        if (tableInfo != null)
        {
            String primaryColumn = StringUtils.capitalize(tableInfo.getIdFieldName());
            
            String sql = String.format("SELECT Version FROM %s WHERE %s = %d ", tableInfo.getName(), primaryColumn, data.getId());
            Integer version = BasicSQLUtils.getCount(sql);
            if (version != null)
            {
                Statement stmt    = null;
                Statement updStmt = null;
                try
                {
                    updStmt = DBConnection.getInstance().getConnection().createStatement();
                        
                    UUID   uuid    = UUID.randomUUID();
                    String uuidStr = uuid.toString();
                            
                    sql = String.format("UPDATE %s SET Version=%d,GUID=%d WHERE %s = %d", 
                                        tableInfo.getName(), version+1, uuidStr, primaryColumn, data.getId());
                    int rv = updStmt.executeUpdate(sql);
                    return rv == 1 ? uuidStr : null;
                    
                } catch (SQLException ex)
                {
                    ex.printStackTrace();
                    
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
        return null;
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
     * @param doVersioning
     * @param formatter
     * @return
     */
    private int buildLSIDs(final Connection            connection,
                           final CATEGORY_TYPE         category, 
                           final boolean               doVersioning,
                           final UIFieldFormatterIFace formatter)
    {
        int count = 0;
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[category.ordinal()]);
        if (tableInfo != null)
        {
            if (classes != null && classes.indexOf(tableInfo.getClassObj()) == -1)
            {
                return count;
            }
            
            String scope = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, false);
            //System.out.println(tableInfo.getTitle()+" -> "+scope);
            String where = doAllRecords ? scope : " (GUID IS NULL OR LOWER(GUID) = 'null' OR GUID = '') AND " + scope;
            String sql   = String.format("SELECT COUNT(*) FROM %s WHERE %s", tableInfo.getName(), where);
            
            //System.out.println(sql);
            count = BasicSQLUtils.getCountAsInt(sql);
            if (count > 0)
            {
                int percentage = count / 10;
                if (percentage == 0) percentage = 1;
                
                setProgressValue(false, 0, count);
                
                sql = String.format("SELECT %s,Version FROM %s WHERE %s", tableInfo.getIdColumnName(), tableInfo.getName(), where);
                
                Statement         stmt    = null;
                PreparedStatement updStmt = null;

                try
                {
                    String updateStr = String.format("UPDATE %s SET GUID=?, VERSION=? WHERE %s AND %s=?", tableInfo.getName(), scope, tableInfo.getIdColumnName());
                    stmt    = connection.createStatement();
                    updStmt = connection.prepareStatement(updateStr);

                    int cnt = 0;
                    ResultSet rs = stmt.executeQuery(sql);
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
                            String msg = "Error updating table["+tableInfo.getName()+"] field["+tableInfo.getIdFieldName()+"] with GUID/LSID.";
                            System.err.println(msg);
                        }
                        cnt++;
                        if (frame != null && cnt % percentage == 0) setProgressValue(false, cnt);
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
        return count;
    }
    
    /**
     * @return the doAll
     */
    public static boolean isDoAllRecords()
    {
        return doAllRecords;
    }

    /**
     * @param doAll the doAll to set
     */
    public static void setDoAll(boolean doAll)
    {
        SpecifyLSIDGeneratorFactory.doAllRecords = doAll;
    }

    /**
     * @param pcl
     */
    public static void buildAllLSIDsAynch(final PropertyChangeListener pcl)
    {
        buildAllLSIDsAynch(pcl, null);
    }

    /**
     * @param pcl
     * @param classes
     */
    public static void buildAllLSIDsAynch(final PropertyChangeListener pcl, final ArrayList<Class<?>> classes)
    {
        final String COUNT = "COUNT";
        
        final GhostGlassPane glassPane = UIRegistry.writeGlassPaneMsg(getResourceString("SETTING_LSIDS"), UIRegistry.STD_FONT_SIZE);
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
                    
                    final LSIDWorker worker = new LSIDWorker()
                    {
                        protected Integer doInBackground() throws Exception
                        {
                            if (getInstance() instanceof SpecifyLSIDGeneratorFactory)
                            {
                                SpecifyLSIDGeneratorFactory lsidGen = (SpecifyLSIDGeneratorFactory)getInstance();
                                lsidGen.setClasses(classes);
                                lsidGen.buildLSIDs(this);
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
     * @param classes the classes to set
     */
    public void setClasses(ArrayList<Class<?>> classes)
    {
        this.classes = classes;
    }

    protected static class LSIDWorker extends javax.swing.SwingWorker<Integer, Integer> implements PropertyChangeListener
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
