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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormHelper;
/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class RecordSetTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(RecordSetTask.class);
            
    // Static Data Members
    public static final String RECORD_SET = "Record_Set";
    public static final DataFlavor RECORDSET_FLAVOR = new DataFlavor(RecordSetTask.class, "RECORD_SET");

    // Data Members
    protected DroppableNavBox navBox = null;

    /**
     * Default Constructor
     *
     */
    public RecordSetTask()
    {
        super(RECORD_SET, getResourceString(RECORD_SET));
        
        CommandDispatcher.register(RECORD_SET, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // TODO Search for the the users or group's RecordSets!
            DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
            List                     recordSets = session.getDataList(RecordSet.class);

            navBox = new DroppableNavBox(title);

            for (Iterator iter=recordSets.iterator();iter.hasNext();)
            {
                RecordSetIFace recordSet = (RecordSetIFace)iter.next();
                recordSet.getItems(); // loads all lazy object 
                                      // TODO Probably don't want to do this defer it to later when they are used.
                
                session.evict(recordSet);
                
                NavBoxItemIFace nbi = addNavBoxItem(navBox, recordSet.getName(), name, RECORD_SET, "Delete", recordSet);
                DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
                if (tblInfo != null)
                {
                    ImageIcon rsIcon = tblInfo.getIcon(IconManager.IconSize.Std16);
                    if (rsIcon != null)
                    {
                        nbi.setIcon(rsIcon);
                    }
                }
                addDraggableDataFlavors(nbi);
            }
            navBoxes.addElement(navBox);
            session.close();

        }
    }

    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(final NavBoxItemIFace nbi)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        roc.addActionListener(new RecordSetSelectedAction((NavBoxButton)nbi, (RecordSetIFace)roc.getData()));
    }

    /**
     * Save a record set
     * @param recordSet the rs to be saved
     */
    public void saveRecordSet(final RecordSet recordSet)
    {
        NavBoxItemIFace nbi = addNavBoxItem(navBox, recordSet.getName(), name, "Record_Set", "Delete", recordSet);
        
        DBTableIdMgr.TableInfo tblInfo = DBTableIdMgr.lookupInfoById(recordSet.getDbTableId());
        if (tblInfo != null)
        {
            ImageIcon rsIcon = tblInfo.getIcon(IconManager.IconSize.Std16);
            if (rsIcon != null)
            {
                nbi.setIcon(rsIcon);
            }
        }
        
        addDraggableDataFlavors(nbi);
        
        recordSet.setTimestampCreated(Calendar.getInstance().getTime());
        recordSet.setOwner(SpecifyUser.getCurrentUser());
        persistRecordSet(recordSet);
        

        NavBoxMgr.getInstance().addBox(navBox);

        // XXX this is pathetic and needs to be generized
        navBox.invalidate();
        navBox.setSize(navBox.getPreferredSize());
        navBox.doLayout();
        navBox.repaint();
        NavBoxMgr.getInstance().invalidate();
        NavBoxMgr.getInstance().doLayout();
        NavBoxMgr.getInstance().repaint();
        UICacheManager.forceTopFrameRepaint();

        CommandDispatcher.dispatch(new CommandAction("Labels", "NewRecordSet", nbi));
    }
    
    /**
     * Save it out to persistent storage.
     * @param recordSet the RecordSet
     */
    protected void persistRecordSet(final RecordSetIFace recordSet)
    {
        // TODO Add StaleObject Code from FormView
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.saveOrUpdate(recordSet);
            session.commit();
            
            FormHelper.updateLastEdittedInfo(recordSet);

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();


    }

    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteRecordSet(final RecordSetIFace recordSet)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(recordSet);
        try
        {
            session.beginTransaction();
            session.delete(recordSet);
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();
    }

    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace.
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteRecordSetFromUI(final NavBoxItemIFace boxItem, final RecordSetIFace recordSet)
    {
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByTitle(navBox, recordSet.getName()).getUIComponent();
        if (comp != null)
        {
            navBox.remove(comp);

            // XXX this is pathetic and needs to be generized
            navBox.invalidate();
            navBox.setSize(navBox.getPreferredSize());
            navBox.doLayout();
            navBox.repaint();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UICacheManager.forceTopFrameRepaint();
        }
    }

    /**
     * Returns a list of RecordSets for a given Table Id (never returns null)
     * @param tableId the matching tableId or -1 if all the recordsets should be returned
     * @return a list of recordsets (never returns null)
     */
    public List<RecordSet> getRecordSets(final int tableId)
    {
        List<RecordSet> list = new ArrayList<RecordSet>();
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            RecordSet rs = (RecordSet)nbi.getData();
            if (tableId == -1 || tableId == rs.getDbTableId())
            {
                list.add(rs);
            }
        }
        return list;
    }

    /**
     * Returns all the recordsets (never returns null)
     * @return all the recordsets (never returns null)
     */
    public List<RecordSet> getRecordSets()
    {
        return getRecordSets(-1);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        return new SimpleDescPane(name, this, "This is the Data Entry Pane");
    }

     /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();

        //ToolBarDropDownBtn btn = createToolbarButton(RECORD_SET,   "dataentry.gif",    "dataentry_hint");
        //list.add(new ToolBarItemDesc(btn));

        return list;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                // If there is only one item in the RecordSet then the User will most likely want it named the same
                // as the "identity" of the data object. So this goes and gets the Identity name and
                // pre-sets the name in the dialog.
                String intialName = "";
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                if (recordSet.getItems().size() == 1)
                {
                    RecordSetItemIFace item = recordSet.getItems().iterator().next();
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    String                   sqlStr  = DBTableIdMgr.getQueryForTable(recordSet.getDbTableId(), item.getRecordId());
                    if (StringUtils.isNotEmpty(sqlStr))
                    {
                        Object dataObj = session.getData(sqlStr);
                        if (dataObj != null)
                        {
                            intialName = ((FormDataObjIFace)dataObj).getIdentityTitle();
                        }
                    }
                    session.close();
                }
                String rsName  = JOptionPane.showInputDialog(UICacheManager.get(UICacheManager.FRAME), 
                                                             getResourceString("AskForRSName"), intialName);
                if (isNotEmpty(rsName))
                {
                    RecordSet rs = (RecordSet)data;
                    rs.setName(rsName);
                    rs.setLastEditedBy(FormHelper.getCurrentUserEditStr());
                    saveRecordSet(rs);
                }
            }
        } else if (cmdAction.isAction("Delete") && cmdAction.getData() instanceof RecordSet)
        {
            RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
            deleteRecordSet(recordSet);
            deleteRecordSetFromUI(null, recordSet);

        } else if (cmdAction.isAction("Dropped"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            
            if (srcObj != null && dstObj != null && srcObj instanceof RecordSet && dstObj instanceof RecordSet)
            {
                RecordSetIFace srcRecordSet = (RecordSetIFace)srcObj;
                RecordSetIFace dstRecordSet = (RecordSetIFace)dstObj;
                if (srcRecordSet.getDbTableId().intValue() == dstRecordSet.getDbTableId().intValue())
                {
                    int oldSize = dstRecordSet.getItems().size();
                    Vector<RecordSetItemIFace> dstList  = new Vector<RecordSetItemIFace>(dstRecordSet.getItems());
                    log.debug("Source:");
                    for (RecordSetItemIFace rsi : srcRecordSet.getItems())
                    {
                        log.debug(" "+rsi.getRecordId());
                    }                
                    log.debug("\nDest:");
                    for (RecordSetItemIFace rsi : dstRecordSet.getItems())
                    {
                        log.debug(" "+rsi.getRecordId());
                    }    
                    log.debug("");
                    for (RecordSetItemIFace rsi : srcRecordSet.getItems())
                    {
                        if (Collections.binarySearch(dstList, rsi) < 0)
                        {
                            RecordSetItem newrsi = new RecordSetItem(rsi.getRecordId());
                            dstRecordSet.getItems().add(newrsi);
                            newrsi.setRecordSet(dstRecordSet);
                        }
                    }
                    log.debug("");
                    log.debug("New Dest:");
                    for (RecordSetItemIFace rsi : dstRecordSet.getItems())
                    {
                        log.debug(" "+rsi.getRecordId());
                    }                
                    log.debug("");
                    
                    if (dstRecordSet.getItems().size() > oldSize)
                    {
                        persistRecordSet(dstRecordSet);
                    }
                } else
                {
                    DBTableIdMgr.TableInfo srcTI = DBTableIdMgr.lookupInfoById(srcRecordSet.getDbTableId());
                    DBTableIdMgr.TableInfo dstTI = DBTableIdMgr.lookupInfoById(dstRecordSet.getDbTableId());
                    JOptionPane.showMessageDialog(null, 
                        String.format(getResourceString("RECORDSET_MERGE_ERROR"), new Object[] {srcTI.getShortClassName(), dstTI.getShortClassName()}), 
                            getResourceString("Error"), 
                            JOptionPane.ERROR_MESSAGE);

                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(RecordSetTask.RECORD_SET))
        {
            processRecordSetCommands(cmdAction);
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            isInitialized = false;
            this.initialize();
        }
    }
    
    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

     /**
     *
     * @author rods
     *
     */
    class RecordSetSelectedAction implements ActionListener
    {
        private NavBoxButton ro;
        private RecordSetIFace rs;

        public RecordSetSelectedAction(final NavBoxButton ro, final RecordSetIFace rs)
        {
            this.ro = ro;
            this.rs = rs;
        }

        public void actionPerformed(ActionEvent e)
        {
            Object src = e.getSource();
            
            log.debug(src.hashCode()+"  "+ro.hashCode());
            
            if (e instanceof DataActionEvent)
            {
                DataActionEvent dataActionEv = (DataActionEvent)e;
                CommandDispatcher.dispatch(new CommandAction(RECORD_SET, src == ro ? "Clicked" : "Dropped", dataActionEv.getData(), rs, null));
            } else
            {
                throw new RuntimeException("How did we get here?");
                //CommandDispatcher.dispatch(new CommandAction(RECORD_SET, "Clicked", null, rs, null));
            }
        }

    }

    /**
     * @author rods
     *
     */
    class DroppableNavBox extends NavBox implements GhostActionable
    {
        // DnD
        protected List<DataFlavor> dropFlavors = new ArrayList<DataFlavor>(); 
        protected Object data;
        
        /**
         * Constructor.
         * @param name
         */
        public DroppableNavBox(final String name)
        {
            super(name);
            dropFlavors.add(RECORDSET_FLAVOR);
        }
        
        //-----------------------------------------------
        // GhostActionable Interface
        //-----------------------------------------------
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#doAction(edu.ku.brc.ui.dnd.GhostActionable)
         */
        public void doAction(GhostActionable src)
        {
            if (src != null)
            {  
                Object dataObj = src.getData();
                System.out.println(dataObj);
                
                CommandDispatcher.dispatch(new CommandAction(RECORD_SET, "Save", src.getData()));
            }
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#setData(java.lang.Object)
         */
        public void setData(final Object data)
        {
            this.data = data;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getData()
         */
        public Object getData()
        {
            return data;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getDataForClass(java.lang.Class)
         */
        public Object getDataForClass(Class classObj)
        {
            return UIHelper.getDataForClass(data, classObj);
        }
       
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#createMouseInputAdapter()
         */
        public void createMouseInputAdapter()
        {
        }
        
        /**
         * Returns the adaptor for tracking mouse drop gestures
         * @return Returns the adaptor for tracking mouse drop gestures
         */
        public GhostMouseInputAdapter getMouseInputAdapter()
        {
            return null;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getBufferedImage()
         */
        public BufferedImage getBufferedImage() 
        {
            return null;
        }
        
        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getDataFlavor()
         */
        public List<DataFlavor> getDropDataFlavors()
        {
            return dropFlavors;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.ui.dnd.GhostActionable#getDragDataFlavors()
         */
        public List<DataFlavor> getDragDataFlavors()
        {
            return null; // this is not draggable
        }
    }

}
