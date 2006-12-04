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

import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;

import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.CollectionObjDef;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.TableViewObj;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.persist.View;

/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 *
 * @code_status Unknown (auto-generated)
 * 
 * @author rods
 *
 */
public class InfoRequestTask extends BaseTask
{
    // Static Data Members
    private static final Logger log  = Logger.getLogger(InfoRequestTask.class);
    
    public static final String     INFOREQUEST        = "InfoRequest";
    public static final DataFlavor INFOREQUEST_FLAVOR = new DataFlavor(RecordSetTask.class, INFOREQUEST);
    public static final String     INFO_REQ_MESSAGE = "Specify Info Request";
    public static final String     CREATE_MAILMSG   = "CreateMailMsg";
    

    protected static DateWrapper scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");


    // Data Members
    protected NavBox navBox = null;
    
    /**
     * Default Constructor
     *
     */
    public InfoRequestTask()
    {
        super(INFOREQUEST, getResourceString(INFOREQUEST));
        CommandDispatcher.register(INFOREQUEST, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
        CommandDispatcher.register(DB_CMD_TYPE, this);
    }
    
    /**
     * Returns a title for the InfoRequest
     * @param infoRequest the infor request to construct a title for
     * @return Returns a title for the InfoRequest
     */
    protected String getTitle(final InfoRequest infoRequest)
    {
        //System.out.println(scrDateFormat.toPattern());
        return infoRequest.getFirstName() + " " + infoRequest.getLastName() + " - " + scrDateFormat.format(infoRequest.getRequestDate());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            navBox = new NavBox(title);
            /*
            List infoRequests = session.getDataList(InfoRequest.class);
            for (Iterator iter=infoRequests.iterator();iter.hasNext();)
            {
                addInfoRequest((InfoRequest)iter.next());
                
            } */         
            navBoxes.addElement(navBox);
            session.close();
        }
    }
    
    /**
     * Add an InfoRequest Item to the box.
     * @param infoRequest the infoRequest to be added
     */
    protected void addInfoRequest(final InfoRequest infoRequest)
    {
        // These value should not be hard coded here
        int tableId = DBTableIdMgr.lookupIdByShortName("inforequest");
        DroppableFormObject dfo = new DroppableFormObject("view valid", tableId, infoRequest);
        NavBoxItemIFace     nbi = addNavBoxItem(navBox, getTitle(infoRequest), INFOREQUEST, "Delete", dfo);
        NavBoxButton     roc = (NavBoxButton)nbi;
        roc.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                createFormPanel((NavBoxButton)ae.getSource());
            }
        });
        addDraggableDataFlavors(roc);
    }
    
    /**
     * Adds the appropriate flavors to make it draggable.
     * @param nbi the item to be made draggable
     */
    protected void addDraggableDataFlavors(NavBoxButton roc)
    {
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(DroppableTaskPane.DROPPABLE_PANE_FLAVOR);
    }
    
    /**
     * Save a info request.
     * @param infoRequest the ir to be saved
     * @param recordSet the recordSet to be saved with it
     */
    public void saveInfoRequest(final InfoRequest infoRequest, final RecordSetIFace recordSet)
    {
        addInfoRequest(infoRequest);

        infoRequest.setTimestampCreated(Calendar.getInstance().getTime());
        infoRequest.setTimestampModified(Calendar.getInstance().getTime());
        
        // save to database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.saveOrUpdate(infoRequest);
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();
        
        NavBoxMgr.getInstance().addBox(navBox);
        
        // XXX This needs to be made generic
        navBox.invalidate();
        navBox.doLayout();
        navBox.repaint();
        
        // ?? 
        //CommandDispatcher.dispatch(new CommandAction("Labels", "NewRecordSet", nbi));
        
    }
    
    /**
     * Delete a record set
     * @param rs the recordSet to be deleted
     */
    protected void deleteInfoRequest(final InfoRequest infoRequest)
    {
        // delete from database
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.beginTransaction();
            session.delete(infoRequest);
            session.commit();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            log.error(ex);
        }
        session.close();

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
    public static void createInfoRequest(final RecordSetIFace recordSet)
    {
        DBTableIdMgr.TableInfo tableInfo = DBTableIdMgr.lookupByShortClassName(InfoRequest.class.getSimpleName());
        
        SpecifyAppContextMgr appContextMgr = (SpecifyAppContextMgr)AppContextMgr.getInstance();
        
        View view = appContextMgr.getView(tableInfo.getDefaultFormName(), CollectionObjDef.getCurrentCollectionObjDef());

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        infoRequest.setRecordSet(recordSet);
        
        //TaskMgr.getInstance().getTask(INFOREQUEST)
        
        FormPane formPane = new FormPane(DataProviderFactory.getInstance().createSession(), 
                                         view.getName(), TaskMgr.getTask(INFOREQUEST), view, "edit", infoRequest, 
                                         MultiView.IS_NEW_OBJECT );
        formPane.setIcon(IconManager.getIcon(INFOREQUEST, IconManager.IconSize.Std16));
        
        SubPaneMgr.getInstance().addPane(formPane);
        //formPane.setIcon(iconForFormClass.get(createFullName(view.getViewSetName(), view.getName())));

    }
    
    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace. 
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSet the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteInfoRequestFromUI(final NavBoxItemIFace boxItem, final InfoRequest infoRequest)
    {
        
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByName(getTitle(infoRequest)).getUIComponent(); 
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
     * (WORK IN PROGRESS)
     * @param commentId (WORK IN PROGRESS)
     * @param content (WORK IN PROGRESS)
     * @return (WORK IN PROGRESS)
     */
    public static String getCommentValueAsStr(final String commentId, final String content)
    {
        int inx = content.indexOf(commentId);
        if (inx > -1)
        {
            inx += commentId.length() + 1;
            int eInx = content.substring(inx).indexOf(" -->") + inx;
            return content.substring(inx, eInx);
        }
        return null;
    }
    
     /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        recentFormPane = new FormPane(null, name, this, "");
        return recentFormPane;
    }
    
     /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        
        //ToolBarDropDownBtn btn = createToolbarButton(INFOREQUEST,   "information.gif",    "inforequest_hint");      
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
     * Creates an Excel SpreadSheet or CVS file and attaches it to an email and send it to an agent.
     * 
     * @param infoRequest the info request to be sent
     */
    public static void createAndSendEMail(final SubPaneIFace subPane)
    {
        MultiView   mv          =  subPane.getMultiView();
        Viewable    mvViewable  = mv.getCurrentView();
        FormViewObj formViewObj = (FormViewObj)mvViewable;

        Boolean sendEMail = null;
        if (formViewObj != null)
        {
            Component comp = formViewObj.getControlByName("sendEMail");
            if (comp instanceof JCheckBox)
            {
                sendEMail = ((JCheckBox)comp).isSelected();
            }
        }
        
        mv = formViewObj.getSubView("InfoRequestColObj");
        if (mv != null && sendEMail)
        {
            final Viewable viewable = mv.getCurrentView();
            if (viewable instanceof TableViewObj)
            {
                final Hashtable<String, String> values = new Hashtable<String, String>();
                values.put("to", "rods@ku.edu");
                values.put("from", "rods@ku.edu");
                values.put("subject", "Information Request");
                values.put("bodytext", "");
                final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UICacheManager.get(UICacheManager.TOPFRAME),
                                              "SystemSetup",
                                              "SendMail",
                                              null,
                                              "Mail",
                                              "Send",
                                              null, // className,
                                              null, // idFieldName,
                                              true, // isEdit,
                                              0);
                dlg.setData(values);
                dlg.setModal(true);
                
                dlg.setCloseListener(new PropertyChangeListener()
                {
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        String action = evt.getPropertyName();
                        if (action.equals("OK"))
                        {
                            dlg.getMultiView().getDataFromUI();
                            
                            System.out.println("["+values.get("bodytext")+"]");
                            
                            TableViewObj  tblViewObj = (TableViewObj)viewable;
                            File          excelFile  = TableModel2Excel.convertToExcel("Test", tblViewObj.getTable().getModel());
                            StringBuilder sb         = TableModel2Excel.convertToHTML("Test", tblViewObj.getTable().getModel());
                            EMailHelper.setDebugging(true);
                            EMailHelper.sendMsg("imap.ku.edu", "rods", "Vintage1601*", "rods@ku.edu", "rods@ku.edu", 
                                                "Info Request", sb.toString(), EMailHelper.HTML_TEXT, excelFile);
                        }
                        else if (action.equals("Cancel"))
                        {
                            log.warn("User clicked Cancel");
                        }
                    }
                });

                dlg.setVisible(true);
                

            }
        }
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(DB_CMD_TYPE))
        {
            if (cmdAction.getData() instanceof InfoRequest)
            {
                if (cmdAction.isAction(INSERT_CMD_ACT))
                {
                    //final CommandAction cm = cmdAction;
                    // Create Specify Application
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run()
                        {
                            //createAndSendEMail((InfoRequest)cm.getData());  
                            CommandDispatcher.dispatch(new CommandAction(INFOREQUEST, CREATE_MAILMSG, SubPaneMgr.getInstance().getCurrentSubPane()));
                        }
                    });
                }
            }
            
        } else if (cmdAction.isAction(CREATE_MAILMSG))
        {
            createAndSendEMail((SubPaneIFace)cmdAction.getData());
            
        } else if (cmdAction.isAction("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                InfoRequest infoRequest = new InfoRequest();
                infoRequest.setEmail("rods@ku.edu");
                infoRequest.setFirstName("Rod");
                infoRequest.setLastName("Spears");
                infoRequest.setInstitution("KU");
                infoRequest.setRequestDate(Calendar.getInstance());
                
                // Get Info Request Information
                
                saveInfoRequest(infoRequest, (RecordSetIFace)data);
            }
        } else if (cmdAction.isAction("Delete") && cmdAction.getData() instanceof RecordSet)
        {
            InfoRequest inforRequest = (InfoRequest)cmdAction.getData();
            deleteInfoRequest(inforRequest);
            deleteInfoRequestFromUI(null, inforRequest);

        } else if (cmdAction.isAction("Create") && cmdAction.getData() instanceof RecordSet)
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSet)
            {
                createInfoRequest((RecordSetIFace)data);
            }
            //InfoRequest inforRequest = (InfoRequest)cmdAction.getData();
            //createInfoRequest(inforRequest);

        }
    }
    
}
