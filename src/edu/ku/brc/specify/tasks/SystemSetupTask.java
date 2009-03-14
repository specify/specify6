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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.SecurityMgr;
import edu.ku.brc.af.auth.SecurityOption;
import edu.ku.brc.af.auth.SecurityOptionIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.weblink.WebLinkConfigDlg;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.config.ResourceImportExportDlg;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
import edu.ku.brc.specify.tools.schemalocale.PickListEditorDlg;
import edu.ku.brc.specify.tools.schemalocale.SchemaToolsDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;

/**
 *
 * This is used for launching editors for Database Objects that are at the "core" of the data model.
 * 
 * @code_status Beta
 *
 * @author rods
 *
 */
public class SystemSetupTask extends BaseTask implements FormPaneAdjusterIFace, BusinessRulesOkDeleteIFace
{
    // Static Data Members
    private static final Logger log  = Logger.getLogger(SystemSetupTask.class);
    private static final String RESIMPORTEXPORT_SECURITY = "RESIMPORTEXPORT";
    private static final String SCHEMACONFIG_SECURITY    = "SCHEMACONFIG";
    private static final String WBSCHEMACONFIG_SECURITY  = "WBSCHEMACONFIG";
    
    public static final String     SYSTEMSETUPTASK        = "SystemSetup";
    public static final DataFlavor SYSTEMSETUPTASK_FLAVOR = new DataFlavor(SystemSetupTask.class, SYSTEMSETUPTASK);

    // Data Members
    protected NavBox           globalNavBox     = null;
    protected NavBox           navBox           = null;
    protected PickListBusRules pickListBusRules = new PickListBusRules();
    protected FormPane         formPane         = null;

    /**
     * Default Constructor
     *
     */
    public SystemSetupTask()
    {
        super(SYSTEMSETUPTASK, getResourceString(SYSTEMSETUPTASK));
        
        CommandDispatcher.register(SYSTEMSETUPTASK, this);
        CommandDispatcher.register(DataEntryTask.DATA_ENTRY, this);
        
        isShowDefault = true;
    }

    /**
     * Returns a title for the PickList
     * @param pickList the pickList to construct a title for
     * @return Returns a title for the pickList
     */
    protected String getTitle(final PickList pickList)
    {
        return pickList.getName();
    }


    /**
     * @param sysNavBox
     * @param tableId
     */
    protected void createSysNavBtn(final NavBox sysNavBox, final int tableId, final boolean useJoinAndSpecCols)
    {
        final DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        sysNavBox.add(NavBox.createBtnWithTT(ti.getTitle(), ti.getShortClassName(), "", IconManager.STD_ICON_SIZE, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                startEditor(ti.getClassObj(), SYSTEMSETUPTASK, ti.getShortClassName(), useJoinAndSpecCols);
            }
        }));
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false

            // Temporary
            NavBox sysNavBox = new NavBox(getResourceString("CORE_DATA_OBJECTS"));
            createSysNavBtn(sysNavBox, Institution.getClassTableId(), false);
            createSysNavBtn(sysNavBox, Division.getClassTableId(), false);
            createSysNavBtn(sysNavBox, Discipline.getClassTableId(), false);
            createSysNavBtn(sysNavBox, edu.ku.brc.specify.datamodel.Collection.getClassTableId(), false);
            /*sysNavBox.add(NavBox.createBtnWithTT(getResourceString("WEBLINKS_EDITOR"), "WebLink", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    editWebLinks();
                }
            })); */
            navBoxes.add(sysNavBox);
            
            NavBox collNavBox = new NavBox(getResourceString("COLL_DATA_OBJECTS"));
            createSysNavBtn(collNavBox, PrepType.getClassTableId(), true);
            collNavBox.add(NavBox.createBtnWithTT(getResourceString("PICKLIST_EDITOR"), "PickList", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    PickListEditorDlg dlg = new PickListEditorDlg(null, true);
                    dlg.createUI();
                    dlg.setSize(400,500);
                    dlg.setVisible(true);
                }
            })); 
            navBoxes.add(collNavBox);
            
        }
        isShowDefault = true;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        if (!isAnyOtherUsersOn() && isTabsClosed())
        {
            ContextMgr.requestContext(this);
    
            if (starterPane == null)
            {
                if (formPane == null)
                {
                    super.requestContext();
                    
                } else
                {
                    SubPaneMgr.getInstance().showPane(formPane);
                }
                
            } else  
            {
                SubPaneMgr.getInstance().addPane(starterPane);
            }
            TaskMgr.disableAllEnabledTasks();
        }
    }
    
    /**
     * Start Web Links Editor.
     */
    protected void editWebLinks()
    {
        UsageTracker.incrUsageCount("SS.EDTWEBLNK");
        WebLinkConfigDlg dlg = WebLinkMgr.getInstance().editWebLinks(null, false);
        if (dlg.getBtnPressed() == CustomDialog.OK_BTN)
        {
            if (dlg.hasChanged())
            {
                WebLinkMgr.getInstance().write(); // saves
            }
        }
    }
    
    /**
     * Adds a new PickList to the NavBox Container.
     * @param pickList the new pickList
     */
    protected void addPickList(final PickList pickList, final boolean isNew)
    {
        final String nameStr  = pickList.getName();
        
        RolloverCommand roc;
        if (pickList.getIsSystem())
        {
            roc = (RolloverCommand)NavBox.createBtnWithTT(nameStr, "PickList", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name", nameStr, name, "PickList");
                }
            });
        } else
        {
            roc = (RolloverCommand)makeDnDNavBtn(navBox, nameStr, "PickList", null, 
                new CommandAction(SYSTEMSETUPTASK, DELETE_CMD_ACT, pickList.getPickListId()), 
                true, true);// true means make it draggable
        
            roc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name", nameStr, name, "PickList");
                }
            });
            roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        }
        roc.setData(pickList.getPickListId());
        addPopMenu(roc, pickList);
        if (isNew)
        {
            navBox.insertSorted((NavBoxItemIFace)roc);
        } else
        {
            navBox.add((NavBoxItemIFace)roc);
        }
    }
    
    /**
     * Adds the Context PopupMenu for the RecordSet.
     * @param roc the RolloverCommand btn to add the pop to
     */
    public void addPopMenu(final RolloverCommand roc, final PickList pickList)
    {
        if (roc.getLabelText() != null)
        {
            final JPopupMenu popupMenu = new JPopupMenu();
            
            JMenuItem delMenuItem = new JMenuItem(UIRegistry.getResourceString("Delete"));
            if (!pickList.getIsSystem())
            {
                delMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionEvent) {
                        CommandDispatcher.dispatch(new CommandAction(SYSTEMSETUPTASK, DELETE_CMD_ACT, roc));
                    }
                  });
            } else
            {
                delMenuItem.setEnabled(false);
            }
            popupMenu.add(delMenuItem);
            
            JMenuItem viewMenuItem = new JMenuItem(UIRegistry.getResourceString("EDIT"));
            viewMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name",  roc.getName(), roc.getName(), "PickList");
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
     * Searches for a SubPaneIFace that has the same class of data as the argument and then "shows" that Pane and returns true. 
     * If it can't be found then it shows false.
     * @param clazz the class of data to be searched for
     * @return true if found, false if not
     */
    protected boolean checkForPaneWithData(final Class<?> clazz)
    {
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            Object uiComp = pane.getUIComponent();
            if (uiComp instanceof FormPane)
            {
                Object dataObj = ((FormPane)uiComp).getData();
                if (dataObj instanceof Collection<?>)
                {
                    Collection<?> collection = (Collection<?>)dataObj;
                    if (collection.size() > 0)
                    {
                        dataObj = collection.iterator().next();
                    }
                }
                if (dataObj != null && dataObj.getClass() == clazz)
                {
                    SubPaneMgr.getInstance().showPane(pane);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Searches for a SubPaneIFace that has the same class of data as the argument and then "shows" that Pane and returns true. 
     * If it can't be found then it shows false.
     * @param tabName the name of the tab to be searched for
     * @return true if found, false if not
     */
    protected boolean checkForPaneWithName(final String tabName)
    {
        for (SubPaneIFace pane : SubPaneMgr.getInstance().getSubPanes())
        {
            Object uiComp = pane.getUIComponent();
            if (uiComp instanceof FormPane)
            {
                if (pane.getPaneName().equals(tabName))
                {
                    SubPaneMgr.getInstance().showPane(pane);
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * @param clazz
     * @param iconNameArg
     * @param viewName
     */
    protected void startEditor(final Class<?> clazz, 
                               final String iconNameArg, 
                               final String viewName, 
                               final boolean useJoinAndSpecCols)
    {
        if (formPane != null)
        {
            if (!formPane.aboutToShutdown())
            {
                return;        
            }
            SubPaneMgr.getInstance().removePane(formPane);
        }
        formPane = null;
        
        TaskMgr.disableAllEnabledTasks();
        
        UsageTracker.incrUsageCount("SS.EDT."+viewName);
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(clazz.getName());
        String      tiTitle   = tableInfo.getTitle();
        
        if (!checkForPaneWithName(tiTitle))
        {
            List<?> dataItems = null;
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                StringBuffer sb = new StringBuffer();
                sb.append("FROM ");
                sb.append(clazz.getName());
                sb.append(" as ");
                sb.append(tableInfo.getAbbrev());

                if (useJoinAndSpecCols)
                {
                    String joins = QueryAdjusterForDomain.getInstance().getJoinClause(tableInfo, true, null, true);
                    if (StringUtils.isNotEmpty(joins))
                    {
                        sb.append(" ");
                        sb.append(joins);
                    }
                    
                    String specialCols = QueryAdjusterForDomain.getInstance().getSpecialColumns(tableInfo, true);
                    if (StringUtils.isNotEmpty(specialCols))
                    {
                        sb.append(" WHERE ");
                        sb.append(specialCols);
                    }
                }
                
                log.debug(sb.toString());
                dataItems = session.getDataList(sb.toString());
                
                if (dataItems.get(0) instanceof Object[])
                {
                    Vector<Object>dataList = new Vector<Object>();
                    for (Object row : dataItems)
                    {
                        Object[] cols = (Object[])row;
                        dataList.add(cols[0]);
                    }
                    dataItems = dataList;
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
                log.error(ex);
                
            } finally
            {
                session.close();
            }
            
            if (dataItems != null)
            {
                ViewIFace view = AppContextMgr.getInstance().getView("SystemSetup", viewName);
                
                formPane = createFormPanel(tiTitle, 
                                            view.getViewSetName(), 
                                            view.getName(), 
                                            "edit", 
                                            dataItems,  
                                            MultiView.RESULTSET_CONTROLLER,
                                            IconManager.getIcon(clazz.getSimpleName(), IconManager.IconSize.Std16));
                starterPane = null;
                TaskMgr.disableAllEnabledTasks();
            }

        } 
    }
    
    /**
     * @param clazz
     * @param fieldName
     * @param value
     * @param iconNameArg
     * @param viewName
     */
    protected void startEditor(final Class<?> clazz, 
                               final String   fieldName, 
                               final String   value, 
                               final String   iconNameArg, 
                               final String   viewName)
    {
        String plTitle = value == null ? getResourceString("PL_NEWPICKLIST") : value;
        
        if (!checkForPaneWithName(plTitle))
        {
            Object dataObj = null;
            if (value != null)
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    dataObj = session.getData(clazz, fieldName, value, DataProviderSessionIFace.CompareType.Equals);
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
                    log.error(ex);
                    // XXX error dialog
                } finally
                {
                    session.close();
                }
                
            } else
            {
                PickList pl = new PickList();
                pl.initialize();
                dataObj = pl;
            }
            
            ViewIFace view = AppContextMgr.getInstance().getView("SystemSetup", viewName);
            
            formPane = createFormPanel(plTitle,
                                        view.getViewSetName(), 
                                        view.getName(), 
                                        "edit", 
                                        dataObj, 
                                        MultiView.NO_OPTIONS,
                                        IconManager.getIcon(iconNameArg, IconManager.IconSize.Std16),
                                        this);
            starterPane = null;
        } 
    }
    
    /**
     * @param clazz
     * @param iconNameArg
     * @param viewName
     */
    protected void startEditorDlg(final Class<?> clazz, 
                                  final String viewName)
    {
        UsageTracker.incrUsageCount("SS.EDT."+viewName);
        
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(clazz.getName());
        String      tiTitle   = tableInfo.getTitle();
        
        if (!checkForPaneWithName(tiTitle))
        {
            List<?> dataItems = null;
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                StringBuffer sb = new StringBuffer();
                sb.append("FROM ");
                sb.append(clazz.getName());
                sb.append(" as ");
                sb.append(tableInfo.getAbbrev());

                log.debug(sb.toString());
                dataItems = session.getDataList(sb.toString());
                
                if (dataItems.get(0) instanceof Object[])
                {
                    Vector<Object>dataList = new Vector<Object>();
                    for (Object row : dataItems)
                    {
                        Object[] cols = (Object[])row;
                        dataList.add(cols[0]);
                    }
                    dataItems = dataList;
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
                log.error(ex);
                
            } finally
            {
                session.close();
            }
            
            if (dataItems != null)
            {
                if (false)
                {
                    //DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(clazz.getSimpleName());
                    ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                            "SystemSetup",
                            viewName,
                            null, // displayName
                            tableInfo.getTitle(),
                            "CLOSE",
                            clazz.getName(),
                            tableInfo.getPrimaryKeyName(),
                            true,
                            false,
                            null,
                            null,
                            MultiView.HIDE_SAVE_BTN | 
                            MultiView.DONT_ADD_ALL_ALTVIEWS | 
                            MultiView.IS_EDITTING |
                            MultiView.RESULTSET_CONTROLLER,
                            CustomDialog.OK_BTN);
                    dlg.setWhichBtns(CustomDialog.OK_BTN);
                    dlg.setData(dataItems);
                    UIHelper.centerAndShow(dlg);
                } else
                {
                    FormPane     pane = new FormPane(tableInfo.getTitle(), this, "SystemSetup", viewName, "edit", null, MultiView.RESULTSET_CONTROLLER, null); // not new data object
                    CustomDialog dlg  = new CustomDialog((Frame)UIRegistry.getTopWindow(), tableInfo.getTitle(), true, pane);
                    dlg.setWhichBtns(CustomDialog.OK_BTN);
                    dlg.setOkLabel(getResourceString("CLOSE"));
                    pane.getMultiView().setData(dataItems);
                    UIHelper.centerAndShow(dlg);
                }
            }
        } 
    }

    /**
     * Adds the appropriate flavors to make it draggable
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(NavBoxButton roc)
    {
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(DroppableTaskPane.DROPPABLE_PANE_FLAVOR);
    }

    /**
     * Save a PickList.
     * @param pickList the pickList to be saved
      */
    public void savePickList(final PickList pickList)
    {

    }

    /**
     * Delete a PickList.
     * @param pickList the pickList to be deleted
     */
    protected PickList deletePickList(final PickList pickList, final DataProviderSessionIFace sessionArg)
    {
        DataProviderSessionIFace session = sessionArg == null ? DataProviderFactory.getInstance().createSession() : sessionArg;
        try
        {
            session.beginTransaction();
            session.delete(pickList);
            session.commit();
        
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
            log.warn(ex);
            
        } finally
        {
            if (sessionArg == null)
            {
                session.close();
            }
        }
        
        return pickList;
    }

    /**
     * Return a NavBoxItem by name
     * @param boxName the name of the NavBoxItem
     * @return Return a NavBoxItem by name
     */
    protected NavBoxItemIFace getBoxByName(final String boxName)
    {
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            if (((NavBoxButton)nbi).getLabelText().equals(boxName))
            {
                return nbi;
            }
        }
        return null;
    }

    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace.
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deletePickListFromUI(final NavBoxItemIFace boxItem, final PickList pickList)
    {

        NavBoxItemIFace nb = boxItem != null ? boxItem : getBoxByName(getTitle(pickList));
        if (nb != null)
        {
            navBox.remove(nb);

            // XXX this is pathetic and needs to be generized
            navBox.invalidate();
            navBox.setSize(navBox.getPreferredSize());
            navBox.doLayout();
            navBox.repaint();
            NavBoxMgr.getInstance().invalidate();
            NavBoxMgr.getInstance().doLayout();
            NavBoxMgr.getInstance().repaint();
            UIRegistry.forceTopFrameRepaint();
        }
    }
    
    /**
     * 
     */
    protected void doSchemaConfig(final Byte schemaType, final DBTableIdMgr tableMgr)
    {
        if (SubPaneMgr.getInstance().aboutToShutdown())
        {
            boolean ok = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).displayAgentsLoggedInDlg("SystemSetupTask.SCHEMA_CFG");
            if (!ok)
            {
                return;
            }
            
            UsageTracker.incrUsageCount("SS.SCHEMACFG");
            
            UIRegistry.getStatusBar().setIndeterminate(SYSTEMSETUPTASK, true);
            UIRegistry.getStatusBar().setText(getResourceString(getI18NKey("LOADING_LOCALES"))); //$NON-NLS-1$
            UIRegistry.getStatusBar().repaint();
            
            SwingWorker workerThread = new SwingWorker()
            {
                @Override
                public Object construct()
                {
                    Locale.getAvailableLocales(); // load all the locales
                    return null;
                }
                
                @Override
                public void finished()
                {
                    UIRegistry.getStatusBar().setText(""); //$NON-NLS-1$
                    UIRegistry.getStatusBar().setProgressDone(SYSTEMSETUPTASK);
                    
                    SchemaToolsDlg dlg = new SchemaToolsDlg((Frame)UIRegistry.getTopWindow(), schemaType, tableMgr);
                    dlg.setVisible(true);
                }
            };
            
            // start the background task
            workerThread.start();
        }
    }
    
    
    /**
     * Launches dialog for Importing and Exporting Forms and Resources.
     */
    protected void doResourceImportExport()
    {
        if (SubPaneMgr.getInstance().aboutToShutdown())
        {
            Object[] options = { getResourceString("CONTINUE"),  //$NON-NLS-1$
                                 getResourceString("CANCEL")  //$NON-NLS-1$
                  };
            int userChoice = JOptionPane.showOptionDialog(UIRegistry.getTopWindow(), 
                                                             getLocalizedMessage(getI18NKey("REI_MSG")),  //$NON-NLS-1$
                                                             getResourceString(getI18NKey("REI_TITLE")),  //$NON-NLS-1$
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (userChoice == JOptionPane.YES_OPTION)
            {
                ResourceImportExportDlg dlg = new ResourceImportExportDlg();
                dlg.setVisible(true);
                if (dlg.hasChanged())
                {
                    //CommandDispatcher.dispatch(new CommandAction(APP_CMD_TYPE, APP_REQ_RESTART));
                    CommandDispatcher.dispatch(new CommandAction(APP_CMD_TYPE, APP_REQ_EXIT));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.SubPaneMgrListener#subPaneRemoved(edu.ku.brc.af.ui.SubPaneIFace)
     */
    @Override
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        if (subPane instanceof SimpleDescPane || subPanes.size() == 0)
        {
            TaskMgr.reenableAllDisabledTasks();
        }
    }
    
    /**
     * @param key
     * @return
     */
    private String getI18NKey(final String key)
    {
        return "SystemSetupTask." + key;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        final String COLSETUP_MENU   = "Specify.COLSETUP_MENU";
        final String SYSTEM_MENU     = "Specify.SYSTEM_MENU";
        final String FULL_SYSTEM_MENU = SYSTEM_MENU + "/" + COLSETUP_MENU;

        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        menuItems = new Vector<MenuItemDesc>();
        
        MenuItemDesc mid;
        /*mid = createDataObjEditMenu(edu.ku.brc.specify.datamodel.Collection.class, "", FULL_SYSTEM_MENU);
        mid.setSepPosition(MenuItemDesc.Position.After);
        menuItems.add(mid);
        menuItems.add(createDataObjEditMenu(Discipline.class,  "", FULL_SYSTEM_MENU));
        menuItems.add(createDataObjEditMenu(Division.class,    "", FULL_SYSTEM_MENU));
        menuItems.add(createDataObjEditMenu(Institution.class, "", FULL_SYSTEM_MENU));
        */
        
        String    titleArg; 
        String    mneu; 
        JMenuItem mi;
        String    menuDesc = getResourceString(COLSETUP_MENU);
        
        if (!UIHelper.isSecurityOn() || 
            (secMgr.getPermission(WBSCHEMACONFIG_SECURITY) != null && 
             secMgr.getPermission(WBSCHEMACONFIG_SECURITY).canAdd()))
        {
            titleArg = getI18NKey("WBSCHEMA_CONFIG"); //$NON-NLS-1$
            mneu     = getI18NKey("WBSCHEMA_CONFIG_MNU");  //$NON-NLS-1$
            mi       = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null); 
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            DBTableIdMgr schema = new DBTableIdMgr(false);
                            schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml"))); //$NON-NLS-1$
                            doSchemaConfig(SpLocaleContainer.WORKBENCH_SCHEMA, schema);
                        }
                    });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.After, menuDesc);
            menuItems.add(mid);
        }
        
        if (!UIHelper.isSecurityOn() || 
            (secMgr.getPermission(RESIMPORTEXPORT_SECURITY) != null && 
             !secMgr.getPermission(RESIMPORTEXPORT_SECURITY).hasNoPerm()))
        {
            titleArg = getI18NKey("RIE_MENU"); //$NON-NLS-1$
            mneu     = getI18NKey("RIE_MNU");  //$NON-NLS-1$
            mi       = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null); 
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            doResourceImportExport();
                        }
                    });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.After, menuDesc);
            menuItems.add(mid);
        }
        
        if (!UIHelper.isSecurityOn() || 
            (secMgr.getPermission(SCHEMACONFIG_SECURITY) != null && 
             secMgr.getPermission(SCHEMACONFIG_SECURITY).canAdd()))
        {
            titleArg = getI18NKey("SCHEMA_CONFIG"); //$NON-NLS-1$
            mneu = getI18NKey("SCHEMA_CONFIG_MNU");  //$NON-NLS-1$
            mi = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null);
            mi.addActionListener(new ActionListener()
                    {
                        public void actionPerformed(ActionEvent ae)
                        {
                            doSchemaConfig(SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
                        }
                    });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.After, menuDesc);
            menuItems.add(mid);
        }
        
        if (!UIHelper.isSecurityOn() || 
             (getPermissions() != null && getPermissions().canAdd()))
        {
            titleArg = getI18NKey("COLL_CONFIG"); 
            mneu     = getI18NKey("COLL_CONFIG_MNEU"); 
            mi = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null);
            mi.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent ae)
                {
                    SystemSetupTask.this.requestContext();
                }
            }); 
            mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.Top, FULL_SYSTEM_MENU);
            menuItems.add(mid);
        }
        
        return menuItems;
    }
    
    /**
     * @param cls
     * @param titleKey
     * @param FULL_SYSTEM_MENU
     * @return
     */
    /*private MenuItemDesc createDataObjEditMenu(final Class<?> cls,
                                               final String titleKey, 
                                               final String FULL_SYSTEM_MENU)
    {
        String      miTitle = cls.getSimpleName();//getI18NKey(titleKey); 
        JMenuItem   mi      = UIHelper.createLocalizedMenuItem(miTitle, null, miTitle, true, null);
        mi.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                startEditorDlg(cls, cls.getSimpleName());
            }
        });
        
        MenuItemDesc mid = new MenuItemDesc(mi, FULL_SYSTEM_MENU);
        mid.setPosition(MenuItemDesc.Position.Top, FULL_SYSTEM_MENU); 
        return mid;
    }*/
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    

    /**
     * A PickList was saved, if it is new then add a new NavBoxItem
     * @param pickList the saved PickList
     */
    private void pickListSaved(final PickList pickList)
    {
        boolean fnd = false;
        boolean resort = false;
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            if (nbi.getData() != null && ((Integer)nbi.getData()).intValue() == pickList.getPickListId().intValue())
            {
                fnd = true;
                String oldName = ((RolloverCommand)nbi).getLabelText();
                if (!oldName.equals(pickList.getName()))
                {
                    ((RolloverCommand)nbi).setLabelText(pickList.getName());
                    resort = true;
                }
                break;
            }
        }
        
        if (!fnd)
        {
            addPickList(pickList, true);
            
        } else if (resort)
        {
            navBox.sort();
        }
        
        SubPaneIFace subPane = SubPaneMgr.getInstance().getCurrentSubPane();
        if (subPane != null && subPane.getPaneName().startsWith(getResourceString("PL_NEWPICKLIST")))
        {
            SubPaneMgr.getInstance().renamePane(subPane, pickList.getName());
        }
    }

    //-------------------------------------------------------
    // SecurityOption Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getAdditionalSecurityOptions()
     */
    @Override
    public List<SecurityOptionIFace> getAdditionalSecurityOptions()
    {
        List<SecurityOptionIFace> list = new ArrayList<SecurityOptionIFace>();
        
        list.add(new SecurityOption(RESIMPORTEXPORT_SECURITY, 
                getResourceString("RIE_TITLE"), 
                securityPrefix,
                new BasicPermisionPanel("RIE_TITLE", 
                                        "RIE_SEC_IMPORT", 
                                        "RIE_SEC_EXPORT")));

        list.add(new SecurityOption(SCHEMACONFIG_SECURITY, 
                getResourceString(getI18NKey("SCHEMA_CONFIG")), 
                securityPrefix,
                new BasicPermisionPanel(getI18NKey("SCHEMA_CONFIG"), 
                                        "Enable")));

        list.add(new SecurityOption(WBSCHEMACONFIG_SECURITY, 
                getResourceString(getI18NKey("WBSCHEMA_CONFIG")), 
                securityPrefix,
                new BasicPermisionPanel(getI18NKey("WBSCHEMA_CONFIG"), 
                                        "Enable")));

        return list;
    }
    
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type LABELS.
     * @param cmdAction the command to be processed
     */
    protected void processSysSetupCommands(final CommandAction cmdAction)
    {
        Object data = cmdAction.getData() instanceof DroppableFormObject ? ((DroppableFormObject)cmdAction.getData()).getData() : cmdAction.getData();
        
        if (data instanceof NavBoxButton)
        {
            data = ((NavBoxButton)data).getData();
        }
        
        if (cmdAction.isAction(DELETE_CMD_ACT))
        {
            if (data instanceof Integer)
            {
                final Integer id = (Integer) data;
                UIRegistry.getStatusBar().setIndeterminate(SYSTEMSETUPTASK, true);
                final SwingWorker worker = new SwingWorker()
                {
                    public Object construct()
                    {
                        DataProviderSessionIFace session = null;
                        try
                        {
                            session = DataProviderFactory.getInstance().createSession();
                            PickList pickList = session.getData(PickList.class, "pickListId", id, DataProviderSessionIFace.CompareType.Equals);
                            if (pickList != null)
                            {
                                pickListBusRules.okToDelete(pickList, session, SystemSetupTask.this);
                            }
                        } catch (Exception ex)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
                            log.error(ex); // XXX need error dialog
                            
                        } finally
                        {
                            if (session != null)
                            {
                                session.close();
                            }
                        }
                        return null;
                    }

                    //Runs on the event-dispatching thread.
                    public void finished()
                    {
                        UIRegistry.getStatusBar().setProgressDone(SYSTEMSETUPTASK);
                        
                    }
                };
                worker.start();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(SYSTEMSETUPTASK))
        {
            processSysSetupCommands(cmdAction);
            
        } else if (cmdAction.isType(DataEntryTask.DATA_ENTRY))
        {
            Object data = cmdAction.getData() instanceof DroppableFormObject ? ((DroppableFormObject)cmdAction.getData()).getData() : cmdAction.getData();
            
            if (cmdAction.isAction("Save") && data instanceof PickList)
            {
                pickListSaved((PickList)data);
            }
        }
    }

    //-----------------------------------------------------------------------------------
    //-- FormPaneAdjuster Interface
    //-----------------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjuster#adjustForm(edu.ku.brc.ui.forms.FormViewObj)
     */
    public void adjustForm(FormViewObj fvo)
    {
        new PickListBusRules().adjustForm(fvo);
    }

    //-----------------------------------------------------------------------------------
    //-- BusinessRulesOkDeleteIFace Interface
    //-----------------------------------------------------------------------------------
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace#doDeleteDataObj(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    public void doDeleteDataObj(final Object dataObj, final DataProviderSessionIFace session, final boolean doDelete)
    {
        UIRegistry.getStatusBar().setProgressDone(SYSTEMSETUPTASK);
        
        if (dataObj instanceof PickList)
        {
            PickList pickList = (PickList)dataObj;
            
            if (doDelete)
            {
                deletePickList(pickList, session);
                if (pickList != null)
                {
                    if (!pickList.getIsSystem())
                    {
                        deletePickListFromUI(null, pickList);
                        SubPaneIFace sp = SubPaneMgr.getInstance().getSubPaneByName(pickList.getName());
                        
                        if (sp != null)
                        {
                            SubPaneMgr.getInstance().removePane(sp);
                        }
                    } else
                    {
                        UIRegistry.getStatusBar().setErrorMessage(getResourceString("PL_NO_DEL_SYSPL"));
                    }
                }
            } else
            {
                UIRegistry.getStatusBar().setErrorMessage(getResourceString("PL_NO_DEL_PL_INUSE"));
            }
        }
    }
    
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {false, false, false, false},
                                {false, false, false, false},
                                {false, false, false, false}};
    }
}
