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
import java.sql.Connection;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.GenericGUIDGeneratorFactory;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
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
        if (frame != null) frame.setDesc(getResourceString("SpecifyGUIDGeneratorFactory.UPDATING_GUIDS"));

        if (pcl != null) {
        	setProgressValue(true, false, 0, CATEGORY_TYPE.values().length); // Sets Overall Progress to 0 -> 100
        }
        
        Connection conn = DBConnection.getInstance().getConnection();
                
        int count = 1;
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
            if (pcl != null) {
            	setProgressValue(true, false, count++);
            }
        }
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
    private void setProgressValue(final boolean isOverall,
                                  final boolean indeterminate,
                                  final int...values)
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
                            frame.getOverallProgress().setIndeterminate(indeterminate);
                            frame.setOverall(values[0], values[1]);
                        }
                    } else
                    {
                        if (values.length == 1)
                        {
                            frame.setProcess(values[0]);
                        } else
                        {
                            frame.getProcessProgress().setIndeterminate(indeterminate);
                            frame.setProcess(values[0], values[1]);
                        }
                    }
                }
            });
        }
    }
    
    
    private void buildGUIDs(final Connection    connection,
                            final CATEGORY_TYPE category)
    {
        setProgressValue(false, true, 0, 100);
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(TABLE_IDS[category.ordinal()]);
        
        if (tableInfo != null)
        {
            String sql = String.format("SELECT count(*) FROM %s ", tableInfo.getName());
            int totalCnt = BasicSQLUtils.getCountAsInt(connection, sql);
            int notEmptyCnt = BasicSQLUtils.getCountAsInt(connection, sql + "WHERE GUID > ''");

            if (notEmptyCnt > 0) {
                System.out.println("Not populating GUIDS in " + tableInfo.getName()
                        + " because " + notEmptyCnt + " of " + totalCnt +
                        		" records contain values.");
                return;
            }
            setProgressDesc(String.format("Setting GUIDS in %s...", tableInfo.getName()));
            String updateStr = String.format(
                        "UPDATE %s SET GUID = UUID(), version = version+1 " +
                		"WHERE GUID IS NULL OR GUID = ''", 
                        tableInfo.getName());
            System.out.println(updateStr);
            BasicSQLUtils.update(connection, updateStr);
//            BasicSQLUtils.update(connection, String.format(
//                    "ALTER TABLE %s MODIFY GUID VARCHAR(128) NOT NULL",
//                    tableInfo.getName()));
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
