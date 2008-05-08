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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.DroppableNavBox;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.Trash;
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
public class RecordSetTask extends BaseTask implements PropertyChangeListener
{
    private static final Logger log = Logger.getLogger(RecordSetTask.class);
            
    // Static Data Members
    public static final String RECORD_SET     = "Record_Set";
    public static final String SAVE_RECORDSET = "Save";
    
    public static final DataFlavor RECORDSET_FLAVOR = new DataFlavor(RecordSetTask.class, "RECORD_SET");
    
    protected Vector<DataFlavor> draggableFlavors = new Vector<DataFlavor>();
    protected Vector<DataFlavor> droppableFlavors = new Vector<DataFlavor>();

    // Data Members
    protected DroppableNavBox navBox = null;

    /**
     * Default Constructor
     *
     */
    public RecordSetTask()
    {
        super(RECORD_SET, getResourceString(RECORD_SET));
        
        draggableFlavors.add(Trash.TRASH_FLAVOR);
        //draggableFlavors.add(RecordSetTask.RECORDSET_FLAVOR);
        
        CommandDispatcher.register(RECORD_SET, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
        
        // Register all Tables as being able to be saved in a RecordSet
        // Althought some system tables we may not want, they won't be searchable anyway.
        for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
        {
            ContextMgr.registerService(ti.getTitle(), ti.getTableId(), new CommandAction(RECORD_SET, SAVE_RECORDSET), this, RECORD_SET, getResourceString("CreateRecordSetTT"));    
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // TODO RELEASE Search for the the users or group's RecordSets!
            DataProviderSessionIFace session    = DataProviderFactory.getInstance().createSession();
            List<?> recordSets = session.getDataList("From RecordSet where type = 0");

            navBox = new DroppableNavBox(title, RECORDSET_FLAVOR, RECORD_SET, SAVE_RECORDSET);

            for (Iterator<?> iter=recordSets.iterator();iter.hasNext();)
            {
                RecordSetIFace recordSet = (RecordSetIFace)iter.next();
                
                recordSet.getItems(); // loads all lazy object 
                                      // TODO Probably don't want to do this defer it to later when they are used.
                session.evict(recordSet);
                
                addToNavBox(recordSet);
            }
            navBoxes.add(navBox);
            session.close();

        }
    }
    
    /**
     * Adds a RecordSet to the Left Pane NavBox
     * @param recordSet the recordset to be added
     * @return the nav box
     */
    protected NavBoxItemIFace addToNavBox(final RecordSetIFace recordSet)
    {
        //NavBoxItemIFace nbi = addNavBoxItem(navBox, recordSet.getName(), name, new CommandAction(RECORD_SET, DELETE_CMD_ACT, recordSet), recordSet);
        
        final RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(navBox, recordSet.getName(), "Record_Set", null, 
                                                                   new CommandAction(RECORD_SET, DELETE_CMD_ACT, recordSet), 
                                                                   true, true);// true means make it draggable
        roc.setData(recordSet);
        addPopMenu(roc);
        
        NavBoxItemIFace nbi = (NavBoxItemIFace)roc;
        
        DBTableInfo tblInfo = DBTableIdMgr.getInstance().getInfoById(recordSet.getDbTableId());
        if (tblInfo != null)
        {
            ImageIcon rsIcon = tblInfo.getIcon(IconManager.STD_ICON_SIZE);
            if (rsIcon != null)
            {
                nbi.setIcon(rsIcon);
            }
        }
        
        roc.addDragDataFlavor(new DataFlavorTableExt(RecordSetTask.class, "RECORD_SET", recordSet.getDbTableId()));
        
        addDraggableDataFlavors(nbi);
        addDroppableDataFlavors(nbi);
        
        return nbi;
    }
    
    /**
     * Adds the Context PopupMenu for the RecordSet.
     * @param roc the RolloverCommand btn to add the pop to
     */
    public void addPopMenu(final RolloverCommand roc)
    {
        if (roc.getLabelText() != null)
        {
            final JPopupMenu popupMenu = new JPopupMenu();
            
            JMenuItem renameMenuItem = new JMenuItem(UIRegistry.getResourceString("Rename"));
            renameMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    roc.startEditting(RecordSetTask.this);
                }
              });
            popupMenu.add(renameMenuItem);
            
            JMenuItem delMenuItem = new JMenuItem(UIRegistry.getResourceString("Delete"));
            delMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    CommandDispatcher.dispatch(new CommandAction(RECORD_SET, DELETE_CMD_ACT, roc));
                }
              });
            popupMenu.add(delMenuItem);
            
            JMenuItem viewMenuItem = new JMenuItem(UIRegistry.getResourceString("View"));
            viewMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    CommandDispatcher.dispatch(new CommandAction("Express_Search", "ViewRecordSet", roc));
                }
              });
            popupMenu.add(viewMenuItem);
            
            MouseListener mouseListener = new MouseAdapter() 
            {
                  private boolean showIfPopupTrigger(MouseEvent mouseEvent) {
                      if (roc.isEnabled() && 
                          mouseEvent.isPopupTrigger() && 
                          popupMenu.getComponentCount() > 0) 
                      {
                          popupMenu.show(mouseEvent.getComponent(),
                                  mouseEvent.getX(),
                                  mouseEvent.getY());
                          return true;
                      }
                      return false;
                  }
                  @Override
                  public void mousePressed(MouseEvent mouseEvent) 
                  {
                      if (roc.isEnabled())
                      {
                          showIfPopupTrigger(mouseEvent);
                      }
                  }
                  @Override
                  public void mouseReleased(MouseEvent mouseEvent) 
                  {
                      if (roc.isEnabled())
                      {
                          showIfPopupTrigger(mouseEvent);
                      }
                  }
            };
            roc.addMouseListener(mouseListener);
        }
    }


    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(final NavBoxItemIFace nbi)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        for (DataFlavor df : draggableFlavors)
        {
            roc.addDragDataFlavor(df);
        }
        
        roc.addActionListener(new RecordSetSelectedAction((NavBoxButton)nbi, (RecordSetIFace)roc.getData()));
    }

    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDroppableDataFlavors(final NavBoxItemIFace nbi)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        
        for (DataFlavor df : droppableFlavors)
        {
            roc.addDropDataFlavor(df);
        }
    }

    /**
     * Save a record set.
     * @param recordSet the rs to be saved
     */
    public void saveNewRecordSet(final RecordSet recordSet)
    {
        NavBoxItemIFace nbi = addToNavBox(recordSet);
        
        recordSet.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
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
        UIRegistry.forceTopFrameRepaint();

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
            session.flush();
            
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
        deleteDnDBtn(navBox, boxItem != null ? boxItem : getBoxByTitle(navBox, recordSet.getName()));
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
        return starterPane = new SimpleDescPane(name, this, getResourceString("RCT_DROP_BUNDLE_HERE"));
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
    
    /**
     * Checks to see if the recordset AND the NavBtn can be renamed. The scope is the 'user' space.
     * @param roc the roolover navbtn
     * @param rs the recordset
     * @param oldName the old name
     * @param newName the new name
     */
    protected void renameRecordSet(final RolloverCommand roc, final RecordSetIFace rs, final String oldName, final String newName)
    {
        String sqlStr = "select count(rs.name) From RecordSet as rs Inner Join rs.specifyUser as user where rs.name = '"+newName+"' AND user.specifyUserId = "+SpecifyUser.getCurrentUser().getSpecifyUserId();
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        int                      count   = -1;
        try
        {
            Object result = session.getData(sqlStr);
            count =  result != null ? (Integer)result : 0;
            
        } catch (Exception ex)
        {
            
        } finally 
        {
            session.close();
        }
         
        if (count == 0)
        {
            rs.setName(roc.getLabelText());
            persistRecordSet(rs);    
        } else
        {
            String msg = String.format(UIRegistry.getResourceString("RST_RENAMING_ERROR"), newName);
            UIRegistry.getStatusBar().setErrorMessage(msg);
            rs.setName(oldName);
            roc.setLabelText(oldName);
            roc.repaint();
        }
    }

    //-------------------------------------------------------
    // PropertyChangeListener Interface
    //-------------------------------------------------------
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() instanceof RolloverCommand)
        {
            RolloverCommand roc = (RolloverCommand)evt.getSource();
            renameRecordSet(roc, (RecordSetIFace)roc.getData(), (String)evt.getOldValue(), (String)evt.getNewValue());
        }
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
        if (cmdAction.isAction(SAVE_RECORDSET))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                // If there is only one item in the RecordSet then the User will most likely want it named the same
                // as the "identity" of the data object. So this goes and gets the Identity name and
                // pre-sets the name in the dialog.
                String intialName = "";
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                if (recordSet.getNumItems() == 1)
                {
                    RecordSetItemIFace item = recordSet.getItems().iterator().next();
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    String                   sqlStr  = DBTableIdMgr.getInstance().getQueryForTable(recordSet.getDbTableId(), item.getRecordId());
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
                String rsName  = JOptionPane.showInputDialog(UIRegistry.get(UIRegistry.FRAME), 
                                                             getResourceString("RST_ASKFORNAME"), intialName);
                if (isNotEmpty(rsName))
                {
                    RecordSet rs = (RecordSet)data;
                    rs.setName(rsName);
                    rs.setModifiedByAgent(Agent.getUserAgent());
                    saveNewRecordSet(rs);
                }
            }
        } else if (cmdAction.isAction(DELETE_CMD_ACT))
        {
            RecordSetIFace recordSet = null;
            if (cmdAction.getData() instanceof RecordSet)
            {
                recordSet = (RecordSetIFace)cmdAction.getData();
                
            } else if (cmdAction.getData() instanceof RolloverCommand)
            {
                RolloverCommand roc = (RolloverCommand)cmdAction.getData();
                if (roc.getData() instanceof RecordSet)
                {
                    recordSet = (RecordSetIFace)roc.getData();
                }
            }
            
            if (recordSet != null)
            {
                int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                        String.format(UIRegistry.getResourceString("RST_CONFIRM_DELETE"), recordSet.getName()),
                        UIRegistry.getResourceString("RST_CONFIRM_DELETE_TITLE"), 
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N
                
                if (option == JOptionPane.YES_OPTION)
                {
                    deleteRecordSet(recordSet);
                    deleteRecordSetFromUI(null, recordSet);
                }
            }

        } else if (cmdAction.isAction("Dropped"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            
            if (srcObj != dstObj && 
                srcObj != null && 
                dstObj != null && 
                srcObj instanceof RecordSet && 
                dstObj instanceof RecordSet)
            {
                RecordSetIFace srcRecordSet = (RecordSetIFace)srcObj;
                RecordSetIFace dstRecordSet = (RecordSetIFace)dstObj;
                if (srcRecordSet.getDbTableId().intValue() == dstRecordSet.getDbTableId().intValue())
                {
                    int oldSize = dstRecordSet.getNumItems();
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
                            dstRecordSet.addItem(newrsi);
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
                    
                    if (dstRecordSet.getNumItems() > oldSize)
                    {
                        persistRecordSet(dstRecordSet);
                    }
                } else
                {
                    DBTableInfo srcTI = DBTableIdMgr.getInstance().getInfoById(srcRecordSet.getDbTableId());
                    DBTableInfo dstTI = DBTableIdMgr.getInstance().getInfoById(dstRecordSet.getDbTableId());
                    JOptionPane.showMessageDialog(null, 
                        String.format(getResourceString("RST_MERGE_ERROR"), new Object[] {srcTI.getShortClassName(), dstTI.getShortClassName()}), 
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
        if (cmdAction.isType(RECORD_SET))
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
            
            //log.debug(src.hashCode()+"  "+ro.hashCode());
            
            if (e instanceof DataActionEvent)
            {
                DataActionEvent dataActionEv = (DataActionEvent)e;
                if (dataActionEv.getSourceObj() != null)
                {
                    Object data = dataActionEv.getSourceObj().getData();
                    if (data instanceof CommandAction)
                    {
                        CommandAction cmdAction = (CommandAction)data;
                        cmdAction.setData(rs);
                        CommandDispatcher.dispatch(cmdAction);
                    } else
                    {
                        log.debug(data);
                    }
                    
                }
                CommandDispatcher.dispatch(new CommandAction(RECORD_SET, src == ro ? "Clicked" : "Dropped", dataActionEv.getData(), rs, null));
            } else
            {
                throw new RuntimeException("How did we get here?");
            }
        }
    }
    
    /**
     * Adds a DataFlavor to the list of Draggable DataFlavors that each RecordSet supports.
     * @param df the new DataFlavor
     */
    public static void addDraggableDataFlavor(final DataFlavor df)
    {
        RecordSetTask rst = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
        if (df != null && rst != null)
        {
            rst.draggableFlavors.add(df);
        }
    }
    
    /**
     * Adds a DataFlavor to the list of Droppable DataFlavors that each RecordSet supports.
     * @param df the new DataFlavor
     */
    public static void addDroppableDataFlavor(final DataFlavor df)
    {
        RecordSetTask rst = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
        if (df != null && rst != null)
        {
            rst.droppableFlavors.add(df);
        }
    }

}
