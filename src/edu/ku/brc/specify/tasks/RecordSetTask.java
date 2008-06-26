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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
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
import edu.ku.brc.dbsupport.DBConnection;
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
        
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // Register all Tables as being able to be saved in a RecordSet
            // Although some system tables we may not want, they won't be searchable anyway.
            for (DBTableInfo ti : DBTableIdMgr.getInstance().getTables())
            {
                ContextMgr.registerService(ti.getTitle(), ti.getTableId(), new CommandAction(RECORD_SET, SAVE_RECORDSET), this, RECORD_SET, getResourceString("CreateRecordSetTT"));    
            }
            
            navBox = new DroppableNavBox(title, RECORDSET_FLAVOR, RECORD_SET, SAVE_RECORDSET);

            // TODO RELEASE Search for the the users or group's RecordSets!
            //List<?> recordSets = session.getDataList("FROM RecordSet where type = 0");
            SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            
            //SQLExecutionProcessor sqlProc = new SQ
            
            String sqlStr = "SELECT rs.RecordSetID, Type, rs.Name, TableID, rs.Remarks FROM recordset as rs INNER JOIN specifyuser ON rs.SpecifyUserID = specifyuser.SpecifyUserID" +
                            " WHERE type = 0 AND specifyuser.specifyUserID = " + spUser.getId();
            
            Connection connection = null;
            Statement stmt        = null;
            ResultSet rs          = null;
            try
            {
                connection = DBConnection.getInstance().createConnection();
                stmt       = connection.createStatement();
                rs         = stmt.executeQuery(sqlStr);
                
                while (rs.next())
                {
                    int            dbTableId = rs.getInt(4);
                    DBTableInfo    tableInfo = DBTableIdMgr.getInstance().getInfoById(dbTableId);
                    RecordSetProxy rsProxy   = new RecordSetProxy(rs.getInt(1),
                                                                  rs.getByte(2),
                                                                  rs.getString(3),
                                                                  dbTableId,
                                                                  rs.getString(5),
                                                                  tableInfo.getClassObj());
                    addToNavBox(rsProxy);
                }
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                
            } finally
            {
                try
                {
                    if (rs != null)
                    {
                        rs.close();
                    }
                    if (stmt != null)
                    {
                        stmt.close();
                    }
                    if (connection != null)
                    {
                        connection.close();
                    }
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }


            /*for (Object row : rows)
            {
                Object[] data = (Object[])row;
                
                Integer     dbTableId = (Integer)data[1];
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(dbTableId);
                RecordSetProxy rsProxy = new RecordSetProxy((Integer)data[0],
                                                            (Byte)data[1],
                                                            (String)data[1],
                                                            dbTableId,
                                                            tableInfo.getClassObj(),
                                                            null);
                addToNavBox(rsProxy);*/
                
                /*RecordSetIFace recordSet = (RecordSetIFace)iter.next();
                
                recordSet.getItems(); // loads all lazy object 
                                      // TODO Probably don't want to do this defer it to later when they are used.
                session.evict(recordSet);
                
                addToNavBox(recordSet);
                (*/
            //}
            navBoxes.add(navBox);
            //session.close();

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
        roc.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, "RECORD_SET", recordSet.getDbTableId()));
        
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
         
        recordSet.setTimestampCreated(new Timestamp(System.currentTimeMillis()));
        recordSet.setOwner(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        
        if (persistRecordSet(recordSet))
        {
            RecordSetProxy rsProxy   = new RecordSetProxy(recordSet.getId(),
                                                          recordSet.getType(),
                                                          recordSet.getName(),
                                                          recordSet.getDbTableId(),
                                                          recordSet.getRemarks(),
                                                          recordSet.getDataClass());

            NavBoxItemIFace nbi = addToNavBox(rsProxy);
            
            NavBoxMgr.getInstance().addBox(navBox);

            // XXX this is pathetic and needs to be generisized
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

    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteRecordSet(final RecordSetIFace recordSet)
    {
        // Deleting this manually because the RecordSet may not be loaded (with Hibernate)
        // and the items are loaded EAGER, and there is not reason to take all the time (and memory)
        // to load them all just to delete them.
        // So doing this manually with JDBC is the faster way.
        Connection connection      = null;
        Statement  updateStatement = null;
        try
        {
            connection = DBConnection.getInstance().createConnection();
            String deleteRS  = "DELETE FROM recordset WHERE RecordSetID = " + recordSet.getRecordSetId();
            String deleteRSI = "DELETE FROM recordsetitem WHERE RecordSetID = " + recordSet.getRecordSetId();
            updateStatement = connection.createStatement();
            updateStatement.executeUpdate(deleteRSI);
            updateStatement.executeUpdate(deleteRS);
            updateStatement.clearBatch();
            updateStatement.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (updateStatement != null)
                {
                    updateStatement.close();
                }
                if (connection != null)
                {
                    connection.close();
                }
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
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
        return starterPane = new SimpleDescPane(name, this, getResourceString("RecordSetTask.DROP_BUNDLE_HERE"));
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
     * @param roc the rollover navbtn
     * @param rs the recordset
     * @param oldName the old name
     * @param newName the new name
     */
    protected void renameRecordSet(final RolloverCommand roc, final RecordSetIFace rs, final String oldName, final String newName)
    {
        String sqlStr = "select count(rs.name) From RecordSet as rs Inner Join rs.specifyUser as user where rs.name = '"+newName+"' AND user.specifyUserId = "+AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId();
        
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
            String msg = String.format(UIRegistry.getResourceString("RecordSetTask.RENAMING_ERROR"), newName);
            UIRegistry.getStatusBar().setErrorMessage(msg);
            rs.setName(oldName);
            roc.setLabelText(oldName);
            roc.repaint();
        }
    }
    
    /**
     * Merge two RecordSets removes duplicates and saves the destination RecordSet to the database
     * @param srcObj the source data (better be a RecordSet)
     * @param dstObj the destination data (better be a RecordSet)
     */
    protected void mergeRecordSets(final Object srcObj, final Object dstObj)
    {
        String MERGE_ERR = "RecordSetTask.MERGE_ERROR";
        
        if (srcObj != dstObj && 
            srcObj != null && 
            dstObj != null && 
            srcObj instanceof RecordSetIFace && 
            dstObj instanceof RecordSetIFace)
        {
            RecordSetIFace srcRecordSet = srcObj instanceof RecordSetProxy ? ((RecordSetProxy)srcObj).getRecordSet() : (RecordSetIFace)srcObj;
            RecordSetIFace dstRecordSet = dstObj instanceof RecordSetProxy ? ((RecordSetProxy)dstObj).getRecordSet() : (RecordSetIFace)dstObj;
            
            if (srcRecordSet != null && dstRecordSet != null)
            {
                // It' just easier to build this up front
                DBTableInfo srcTI = DBTableIdMgr.getInstance().getInfoById(srcRecordSet.getDbTableId());
                DBTableInfo dstTI = DBTableIdMgr.getInstance().getInfoById(dstRecordSet.getDbTableId());
                String mergeErrStr = String.format(getResourceString(MERGE_ERR), new Object[] {srcTI.getShortClassName(), dstTI.getShortClassName()});

                if (srcRecordSet.getDbTableId().intValue() == dstRecordSet.getDbTableId().intValue())
                {
                    int oldSize = dstRecordSet.getNumItems();
                    Vector<RecordSetItemIFace> dstList  = new Vector<RecordSetItemIFace>(dstRecordSet.getItems());
                    boolean debug = false;
                    if (debug)
                    {
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
                    }
                    for (RecordSetItemIFace rsi : srcRecordSet.getItems())
                    {
                        if (Collections.binarySearch(dstList, rsi) < 0)
                        {
                            RecordSetItem newrsi = new RecordSetItem(rsi.getRecordId());
                            dstRecordSet.addItem(newrsi);
                            newrsi.setRecordSet(dstRecordSet);
                        }
                    }
                    
                    if (debug)
                    {
                        log.debug("");
                        log.debug("New Dest:");
                        for (RecordSetItemIFace rsi : dstRecordSet.getItems())
                        {
                            log.debug(" "+rsi.getRecordId());
                        }                
                        log.debug("");
                    }
                    
                    if (dstRecordSet.getNumItems() > oldSize)
                    {
                        boolean success = persistRecordSet(dstRecordSet);
                        if (success)
                        {
                            String msg = String.format(getResourceString("RecordSetTask.MERGE_SUCCESS"), new Object[] {srcTI.getShortClassName(), dstTI.getShortClassName()});
                            UIRegistry.displayStatusBarText(msg);
                        } else
                        {
                            JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), mergeErrStr, getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else
                {
                    JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), mergeErrStr, getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                }
            } else
            {
                log.error("The src or the dst RecordSet were null src["+srcRecordSet+"]  dst["+dstRecordSet+"]");
            }
        }  
    }
    
    //-------------------------------------------------------
    // static methods for loading and saving a RecordSet
    //-------------------------------------------------------

    public static RecordSet copyRecordSet(final RecordSetIFace recordSetArg)
    {
        RecordSetIFace recordSet = recordSetArg instanceof RecordSetProxy ? ((RecordSetProxy)recordSetArg).getRecordSet() : recordSetArg;
        if (recordSet instanceof RecordSet)
        {
            try
            {
                RecordSet rs = (RecordSet)((RecordSet)recordSet).clone(); // shallow clone
                rs.addAll(recordSet.getOrderedItems());
                rs.setType(RecordSet.HIDDEN);
                return rs;
                
            } catch (CloneNotSupportedException ex)
            {
                throw new RuntimeException(ex);
            }
        } else
        {
            throw new RuntimeException("copyRecordSet doesn't support class of type ["+recordSet.getClass().getSimpleName()+"]");
        }
    }
    
    /**
     * Loads (or makes sure a RecordSet if loaded. This is a necessary because the RS in question
     * maybe a RecordSetProxy and this returns the actual RecordSet from the database.
     * @param recordSet the RecordSetIFace which could be proxy.
     * @return the actual RecordSet from the database.
     */
    public static RecordSetIFace loadRecordSet(final RecordSetIFace recordSet)
    {
        if (recordSet.getRecordSetId() == null)
        {
            throw new RuntimeException("Try to load a RecordSet that has a null id");
        }
        
        if (recordSet instanceof RecordSetProxy)
        {
            return ((RecordSetProxy)recordSet).getRecordSet();
        }
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            RecordSet rs = session.get(RecordSet.class, recordSet.getRecordSetId());
            return rs;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        return null;
    }
    
    /**
     * Save the RecordSet to the Database.
     * @param recordSet the RecordSetIFace
     */
    public static boolean persistRecordSet(final RecordSetIFace recordSet)
    {
        if (recordSet instanceof RecordSet)
        {
            // TODO Add StaleObject Code from FormView
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                Object mergedRS = recordSet.getRecordSetId() != null ? session.merge(recordSet) : recordSet;
                
                session.beginTransaction();
                FormHelper.updateLastEdittedInfo(mergedRS);
                session.saveOrUpdate(mergedRS);
                session.commit();
                session.flush();
                
                return true;
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                log.error(ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            return false;
        }
        throw new RuntimeException("Trying to save object of class["+recordSet.getClass()+"] and it must be RecordSet.");
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
            if (data instanceof RecordSetIFace)
            {
                // If there is only one item in the RecordSet then the User will most likely want it named the same
                // as the "identity" of the data object. So this goes and gets the Identity name and
                // pre-sets the name in the dialog.
                String intialName = "";
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                if (recordSet.getNumItems() == 1)
                {
                    RecordSetItemIFace item = recordSet.getOrderedItems().iterator().next();
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
                                                             getResourceString("RecordSetTask.ASKFORNAME")+":", intialName);
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
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                recordSet = (RecordSetIFace)cmdAction.getData();
                
            } else if (cmdAction.getData() instanceof RolloverCommand)
            {
                RolloverCommand roc = (RolloverCommand)cmdAction.getData();
                if (roc.getData() instanceof RecordSetIFace)
                {
                    recordSet = (RecordSetIFace)roc.getData();
                }
            }
            
            if (recordSet != null)
            {
                int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                        String.format(UIRegistry.getResourceString("RecordSetTask.CONFIRM_DELETE"), recordSet.getName()),
                        UIRegistry.getResourceString("RecordSetTask.CONFIRM_DELETE_TITLE"), 
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
            
            mergeRecordSets(srcObj, dstObj);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
     */
    @Override
    protected void doProcessAppCommands(CommandAction cmdAction)
    {
        super.doProcessAppCommands(cmdAction);
        
        if (cmdAction.isAction(APP_RESTART_ACT))
        {
            isInitialized = false;
            this.initialize();
        } 
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(RECORD_SET))
        {
            processRecordSetCommands(cmdAction);
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
