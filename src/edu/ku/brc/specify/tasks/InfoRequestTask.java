/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Component;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.sql.Timestamp;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.ui.db.CommandActionForDB;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.TableViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.EMailHelper;
import edu.ku.brc.helpers.Encryption;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.InfoRequest;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;

/**
 * Takes care of offering up record sets, updating, deleteing and creating them.
 *
 * @code_status Alpha
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
    public static final String     INFO_REQ_MESSAGE   = "Specify Info Request";
    public static final String     CREATE_MAILMSG     = "CreateMailMsg";
    
    protected static final String infoReqIconName     = "inforequest";
    

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
        CommandDispatcher.register(DB_CMD_TYPE, this);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                navBox = new NavBox(title);
                
                List<?> infoRequests = session.getDataList(InfoRequest.class);
                for (Iterator<?> iter=infoRequests.iterator();iter.hasNext();)
                {
                    addInfoRequest((InfoRequest)iter.next());
                    
                }      
                navBoxes.add(navBox);
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InfoRequestTask.class, ex);
                log.error(ex);
                ex.printStackTrace();
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        }
    }
    
    /**
     * Add an InfoRequest Item to the box.
     * @param infoRequest the infoRequest to be added
     */
    protected void addInfoRequest(final InfoRequest infoRequest)
    {           
        UsageTracker.incrUsageCount("IA.IR.ADD");

        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, 
                                            infoRequest.getIdentityTitle(), 
                                            infoReqIconName, 
                                            null, //ToolTip
                                            new CommandActionForDB("Data_Entry", "Edit", InfoRequest.getClassTableId(), infoRequest.getId()),
                                            new CommandAction(INFOREQUEST, DELETE_CMD_ACT, infoRequest),
                                            true,
                                            true);
        setUpDraggable(nbi, new DataFlavor[]{Trash.TRASH_FLAVOR, InteractionsTask.LOAN_FLAVOR}, new NavBoxAction("Data_Entry", "Edit"));
        navBox.validate();
        navBox.repaint();
        NavBox.refresh(navBox);
        //setUpDraggable(nbi, new DataFlavor[]{InteractionsTask.LOAN_FLAVOR}, null);//new NavBoxAction(InteractionsTask.INTERACTIONS, InteractionsTask.NEW_LOAN));
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
     */
    public void saveInfoRequest(final InfoRequest infoRequest)
    {
        addInfoRequest(infoRequest);
        
        Timestamp now = new Timestamp(System.currentTimeMillis());
        infoRequest.setTimestampCreated(now);
        
        if (DataModelObjBase.save(true, infoRequest))
        {
            NavBoxMgr.getInstance().addBox(navBox);
            
            // XXX This needs to be made generic
            navBox.invalidate();
            navBox.doLayout();
            navBox.repaint();
        }
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
        /*DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(InfoRequest.class.getSimpleName());
        
        ViewIFace view = AppContextMgr.getInstance().getView(tableInfo.getDefaultFormName());

        InfoRequest infoRequest = new InfoRequest();
        infoRequest.initialize();
        
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();
            RecordSet rs = session.get(RecordSet.class, recordSet.getRecordSetId());
            infoRequest.setRecordSet(rs);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(InfoRequestTask.class, ex);
            ex.printStackTrace();
            log.error(ex);
            
        } finally
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        //TaskMgr.getInstance().getTask(INFOREQUEST)
        
        FormPane formPane = new FormPane(view.getName(), TaskMgr.getTask(INFOREQUEST), view, "edit", infoRequest, 
                                         MultiView.IS_NEW_OBJECT );
        formPane.setIcon(IconManager.getIcon(INFOREQUEST, IconManager.IconSize.Std16));
        
        ((InfoRequestTask)TaskMgr.getTask(INFOREQUEST)).addSubPaneToMgr(formPane);
        //formPane.setIcon(iconForFormClass.get(createFullName(view.getViewSetName(), view.getName())));
*/
    }
    
    /**
     * Delete the RecordSet from the UI, which really means remove the NavBoxItemIFace. 
     * This method first checks to see if the boxItem is not null and uses that, i
     * f it is null then it looks the box up by name ans used that
     * @param boxItem the box item to be deleted
     * @param recordSets the record set that is "owned" by some UI object that needs to be deleted (used for secodary lookup
     */
    protected void deleteInfoRequestFromUI(final NavBoxItemIFace boxItem, final InfoRequest infoRequest)
    {
        
        Component comp = boxItem != null ? boxItem.getUIComponent() : getBoxByName(infoRequest.getIdentityTitle()).getUIComponent(); 
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
            UIRegistry.forceTopFrameRepaint();
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
        recentFormPane = new FormPane(name, this, "");
        starterPane = recentFormPane;
        return recentFormPane;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * Returns whether all the email prefs needed for sending mail have been filled in.
     * @return whether all the email prefs needed for sending mail have been filled in.
     */
    public boolean isEMailPrefsOK(final Hashtable<String, String> emailPrefs)
    {
        AppPreferences appPrefs = AppPreferences.getRemote();
        boolean allOK = true;
        String[] emailPrefNames = { "smtp", "username", "password", "email", "port", "security"};
        for (String pName : emailPrefNames)
        {
            String value = appPrefs.get("settings.email."+pName, null);
            //log.info("["+pName+"]["+value+"]");
            if (StringUtils.isNotEmpty(value))
            {
                emailPrefs.put(pName, value);
            } else
            {
                allOK = false;
                break;
            }
        }
        return allOK;
    }
    
    /**
     * Creates an Excel SpreadSheet or CVS file and attaches it to an email and send it to an agent.
     */
    public void createAndSendEMail()
    {
        FormViewObj formViewObj = getCurrentFormViewObj();
        if (formViewObj != null) // Should never happen
        {
            InfoRequest infoRequest = (InfoRequest)formViewObj.getDataObj();
            Agent       toAgent     = infoRequest.getAgent();
            
            boolean   sendEMailTmp = true; // default to true
            Component comp      = formViewObj.getControlByName("sendEMail");
            if (comp instanceof JCheckBox)
            {
                sendEMailTmp = ((JCheckBox)comp).isSelected();
            }
            final boolean sendEMail = sendEMailTmp;
            
            MultiView mv = formViewObj.getSubView("InfoRequestColObj");
            if (mv != null && sendEMail)
            {
                final Viewable viewable = mv.getCurrentView();
                if (viewable instanceof TableViewObj)
                {
                    final Hashtable<String, String> emailPrefs = new Hashtable<String, String>();
                    if (!isEMailPrefsOK(emailPrefs))
                    {
                        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), 
                                getResourceString("NO_EMAIL_PREF_INFO"), 
                                getResourceString("NO_EMAIL_PREF_INFO_TITLE"), JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    
                    final File tempExcelFileName = TableModel2Excel.getTempExcelName();
                    
                    AppPreferences appPrefs = AppPreferences.getLocalPrefs();
                    final Hashtable<String, String> values = new Hashtable<String, String>();
                    values.put("to", toAgent.getEmail() != null ? toAgent.getEmail() : "");
                    values.put("from", appPrefs.get("settings.email.email", ""));
                    values.put("subject", String.format(getResourceString("INFO_REQUEST_SUBJECT"), new Object[] {infoRequest.getIdentityTitle()}));
                    values.put("bodytext", "");
                    values.put("attachedFileName", tempExcelFileName.getName());
                    
                    final ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)UIRegistry.getTopWindow(),
                                                  "SystemSetup",
                                                  "SendMail",
                                                  null,
                                                  getResourceString("SEND_MAIL_TITLE"),
                                                  getResourceString("SEND_BTN"),
                                                  null, // className,
                                                  null, // idFieldName,
                                                  true, // isEdit,
                                                  0);
                    dlg.setData(values);
                    dlg.setModal(true);
                    dlg.setVisible(true);
                    if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
                    {
                        dlg.getMultiView().getDataFromUI();
                        
                        //System.out.println("["+values.get("bodytext")+"]");
                        
                        TableViewObj  tblViewObj = (TableViewObj)viewable;
                        File          excelFile  = TableModel2Excel.convertToExcel(tempExcelFileName, 
                                                                                   getResourceString("CollectionObject"), 
                                                                                   tblViewObj.getTable().getModel());
                        StringBuilder sb         = TableModel2Excel.convertToHTML(getResourceString("CollectionObject"), 
                                                                                  tblViewObj.getTable().getModel());
                        
                        //EMailHelper.setDebugging(true);
                        String text = values.get("bodytext").replace("\n", "<br>") + "<BR><BR>" + sb.toString();
                        
                        // XXX need to move the invokdeLater into the UIRegistry
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                UIRegistry.displayLocalizedStatusBarText("SENDING_EMAIL");
                            }
                        });
                        
                        if (sendEMail)
                        {
                            final EMailHelper.ErrorType status = EMailHelper.sendMsg(emailPrefs.get("servername"), 
                                                                                       emailPrefs.get("username"), 
                                                                                       Encryption.decrypt(emailPrefs.get("password")), 
                                                                                       emailPrefs.get("email"), 
                                                                                       values.get("to"), 
                                                                                       values.get("subject"), 
                                                                                       text, 
                                                                                       EMailHelper.HTML_TEXT, 
                                                                                       emailPrefs.get("port"), 
                                                                                       emailPrefs.get("security"), 
                                                                                       excelFile);
                            if (status != EMailHelper.ErrorType.Cancel)
                            {
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run()
                                    {
                                        UIRegistry.displayLocalizedStatusBarText(status == EMailHelper.ErrorType.Error ? "EMAIL_SENT_ERROR" : "EMAIL_SENT_OK");
                                    }
                                });
                            }
                        }
                    }
                }
            }
        } else
        {
            log.error("Why doesn't the current SubPane have a main FormViewObj?");
        }
    }
    
    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * Processes all Commands of type INFOREQUEST.
     * @param cmdAction the command to be processed
     */
    protected void processInfoRequestCommands(final CommandAction cmdAction)
    {
        UsageTracker.incrUsageCount("IR."+cmdAction.getType());
        
        if (cmdAction.isAction(CREATE_MAILMSG))
        {
            createAndSendEMail();
            
        } else if (cmdAction.isAction("Save"))
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSetIFace)
            {
                InfoRequest infoRequest = new InfoRequest();
                saveInfoRequest(infoRequest);
            }
        } else if (cmdAction.isAction(DELETE_CMD_ACT) && cmdAction.getData() instanceof RecordSetIFace)
        {
            InfoRequest inforRequest = (InfoRequest)cmdAction.getData();
            if (DataModelObjBase.delete(true, inforRequest))
            {
                deleteInfoRequestFromUI(null, inforRequest);
            }

        } else if (cmdAction.isAction("Create") && cmdAction.getData() instanceof RecordSetIFace)
        {
            Object data = cmdAction.getData();
            if (data instanceof RecordSetIFace)
            {
                createInfoRequest((RecordSetIFace)data);
            }
        }
    }
    
    /**
     * Processes all Commands of type DB_CMD_TYPE.
     * @param cmdAction the command to be processed
     */
    protected void processDatabaseCommands(final CommandAction cmdAction)
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
                        //CommandDispatcher.dispatch(new CommandAction(INFOREQUEST, CREATE_MAILMSG, SubPaneMgr.getInstance().getCurrentSubPane()));
                    }
                });
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.CommandListener#doCommand(edu.ku.brc.specify.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        if (cmdAction.isType(DB_CMD_TYPE))
        {
            processDatabaseCommands(cmdAction);
            
        } else if (cmdAction.isType(INFOREQUEST))
        {
            processInfoRequestCommands(cmdAction);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermsArray()
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, true},
                                {false, false, false, false}};
    }
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isPermissionsSettable()
     */
    @Override
    public boolean isPermissionsSettable()
    {
        return false;
    }
}
