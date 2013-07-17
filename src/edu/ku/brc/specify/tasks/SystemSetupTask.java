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
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.tasks.services.PickListUtils.getI18n;
import static edu.ku.brc.specify.tasks.services.PickListUtils.getI18nRS;
import static edu.ku.brc.ui.UIRegistry.askYesNoLocalized;
import static edu.ku.brc.ui.UIRegistry.displayErrorDlgLocalized;
import static edu.ku.brc.ui.UIRegistry.displayInfoMsgDlgLocalized;
import static edu.ku.brc.ui.UIRegistry.forceTopFrameRepaint;
import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getMostRecentWindow;
import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIRegistry.getStatusBar;
import static edu.ku.brc.ui.UIRegistry.getTopWindow;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
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
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.QueryAdjusterForDomain;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.DroppableFormObject;
import edu.ku.brc.af.tasks.subpane.DroppableTaskPane;
import edu.ku.brc.af.tasks.subpane.FormPane;
import edu.ku.brc.af.tasks.subpane.FormPane.FormPaneAdjusterIFace;
import edu.ku.brc.af.tasks.subpane.SimpleDescPane;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.CollapsableSepExtraCompFactory;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.af.ui.forms.formatters.QueryComboboxEditor;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.af.ui.forms.validation.TypeSearchForQueryFactory;
import edu.ku.brc.af.ui.weblink.WebLinkConfigDlg;
import edu.ku.brc.af.ui.weblink.WebLinkMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.SpecifyUserTypes.UserType;
import edu.ku.brc.specify.config.ResourceImportExportDlg;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.conversion.SynonymCleanup;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.Institution;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.busrules.CollectionObjectBusRules;
import edu.ku.brc.specify.datamodel.busrules.PickListBusRules;
import edu.ku.brc.specify.tasks.services.PickListUtils;
import edu.ku.brc.specify.tools.schemalocale.PickListEditorDlg;
import edu.ku.brc.specify.tools.schemalocale.SchemaToolsDlg;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase;
import edu.ku.brc.specify.utilapps.BuildSampleDatabase.UpdateType;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.util.Pair;

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
    private static final String CANCELLED                = "_Cancelled_";
    private static final String PICKLIST                 = "PickList";
    
    
    public static final String     SYSTEMSETUPTASK        = "SystemSetup";
    public static final DataFlavor SYSTEMSETUPTASK_FLAVOR = new DataFlavor(SystemSetupTask.class, SYSTEMSETUPTASK);

    // Data Members
	protected NavBox											globalNavBox				= null;
	protected NavBox											navBox						= null;
	protected PickListBusRules									pickListBusRules			= new PickListBusRules();
	protected FormPane											formPane					= null;
	protected Vector<Pair<BaseTreeTask<?, ?, ?>, JMenuItem>>	treeUpdateMenuItems			= new Vector<Pair<BaseTreeTask<?, ?, ?>, JMenuItem>>();
	protected NavBoxButton                                      lockDBBtn;                   
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

            NavBox sysNavBox = new NavBox(getResourceString("CORE_DATA_OBJECTS"));
            createSysNavBtn(sysNavBox, Institution.getClassTableId(), false);
            createSysNavBtn(sysNavBox, Division.getClassTableId(), false);
            createSysNavBtn(sysNavBox, Discipline.getClassTableId(), false);
            createSysNavBtn(sysNavBox, edu.ku.brc.specify.datamodel.Collection.getClassTableId(), false);
            /*sysNavBox.add(NavBox.createBtnWithTT(getResourceString("WEBLINKS_EDITOR"), "WebLink", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    editWebLinks();
                }E
            })); */
            navBoxes.add(sysNavBox);
            
            NavBox collNavBox = new NavBox(getResourceString("COLL_DATA_OBJECTS"));
            createSysNavBtn(collNavBox, PrepType.getClassTableId(), true);
            collNavBox.add(NavBox.createBtnWithTT(getResourceString("PICKLIST_EDITOR"), PICKLIST, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    PickListEditorDlg dlg = new PickListEditorDlg(null, false, false);
                    dlg.createUI();
                    dlg.setSize(400,500);
                    dlg.setVisible(true);
                }
            })); 
            
            String btnTitle = getResourceString(getI18n("PL_EXPORT"));
            collNavBox.add(NavBox.createBtnWithTT(btnTitle, PICKLIST, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    PickListUtils.exportPickList(null, null);
                }
            })); 

            btnTitle = getResourceString(getI18n("PL_IMPORT"));
            collNavBox.add(NavBox.createBtnWithTT(btnTitle, PICKLIST, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    Collection collection = PickListUtils.getCollectionFromAppContext();
                    if (PickListUtils.importPickLists(null, collection))
                    {
                        collection = PickListUtils.getCollectionFromAppContext();
                        AppContextMgr.getInstance().setClassObject(Collection.class, collection);
                    }
                }
            })); 
            
            
            btnTitle = getI18nRS("SYN_CLEANUP");
            collNavBox.add(NavBox.createBtnWithTT(btnTitle, "Taxon", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    synonymCleanup();
                }
            })); 


            SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
            String sql = String.format("SELECT COUNT(*) FROM specifyuser su INNER JOIN specifyuser_spprincipal sup ON su.SpecifyUserID = sup.SpecifyUserID " +
                                       "INNER JOIN spprincipal p ON sup.SpPrincipalID = p.SpPrincipalID WHERE p.Name = 'Administrator' AND su.Name = '%s'", spUser.getName());
            if (BasicSQLUtils.getCountAsInt(sql) > 0)
            {
                lockDBBtn = (NavBoxButton)NavBox.createBtnWithTT("Lock", SYSTEMSETUPTASK, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        loadUnlockDB(false);
                    }
                });
                collNavBox.add((NavBoxItemIFace)lockDBBtn); 
            }
            loadUnlockDB(true);

            navBoxes.add(collNavBox);
            
            /*if (AppPreferences.getLocalPrefs().getBoolean("debug.menu", false))
            {
                collNavBox.add(NavBox.createBtnWithTT(getResourceString("ANS_EDITOR"), "AutoNumberingScheme", "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        NumberingSchemeDlg dlg = new NumberingSchemeDlg(null);
                        dlg.createUI();
                        dlg.pack();
                        dlg.setVisible(true);
                    }
                })); 
                navBoxes.add(collNavBox);
            }*/
            
            if (AppPreferences.getLocalPrefs().getBoolean("SHOW_ADDOBJ_PREF", true))
            {
                collNavBox.add(NavBox.createBtnWithTT("Activate Subforms", SYSTEMSETUPTASK, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        showAddObjPrefs();
                    }
                })); 
                navBoxes.add(collNavBox);
            }
            
            collNavBox.add(NavBox.createBtnWithTT(getResourceString("SYSSTP_SHOW_USERS_LOGGED"), SYSTEMSETUPTASK, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    showUsersLoggedIn();
                }
            })); 
            navBoxes.add(collNavBox);
        }
        isShowDefault = true;
    }
    
    /**
     * 
     */
    private void synonymCleanup()
    {
        JTextPane   tp = new JTextPane();
        JScrollPane js = new JScrollPane();
        js.getViewport().add(tp);
        
        String text = "";
        try
        {
            String template = "synonym_cleanup_%s.html";
            String fileName = String.format(template,  Locale.getDefault().getLanguage());
            File file = XMLHelper.getConfigDir(fileName);
            if (!file.exists())
            {
                fileName = String.format(template,  "en");
                file = XMLHelper.getConfigDir(fileName);
            }
            text = FileUtils.readFileToString(file);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(js, BorderLayout.CENTER);
        
        tp.setContentType("text/html");
        tp.setText(text);
        tp.setCaretPosition(0);
        tp.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 18));
        
        CustomDialog dlg  = new CustomDialog((Frame)getTopWindow(), getI18nRS("SYN_CLEANUP"), true, CustomDialog.OKCANCELAPPLY, p);
        dlg.setOkLabel(getI18nRS("SYN_CLEANUP"));
        dlg.setCancelLabel(getI18nRS("Report"));
        dlg.setApplyLabel(getResourceString("CANCEL"));
        dlg.setCloseOnApplyClk(true);
        dlg.createUI();
        dlg.setSize(600, 450);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled() && dlg.getBtnPressed() != CustomDialog.APPLY_BTN)
        {
            boolean        doCleanup      = dlg.getBtnPressed() == CustomDialog.OK_BTN;
            SynonymCleanup synonymCleanup = new SynonymCleanup(doCleanup);
            synonymCleanup.execute(); // start the background task
        }
    }
    
    /**
     * 
     */
    private void showAddObjPrefs()
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        Integer    colId      = collection.getId();

        boolean isPrepTypeOK = false;
        String defVal = CollectionObjectBusRules.getDefValForPrepTypeHaveOnForm();
        if (StringUtils.isNotEmpty(defVal))
        {
            isPrepTypeOK = CollectionObjectBusRules.getPrepTypeIdFromDefVal(defVal) != null;
        }

        AppPreferences remote = AppPreferences.getRemote();
        String CO_CREATE_PREP = "CO_CREATE_PREP";
        
        String[] keys = {"CO_CREATE_COA", CO_CREATE_PREP, "CO_CREATE_DET", };
        Properties props = new Properties();
        int i = 0;
        for (String key : keys)
        {
            String fullKey = key+"_"+colId;
            if (i == 1 && !isPrepTypeOK)
            {
                remote.putBoolean(fullKey, false);
            }
            props.put(key, remote.getBoolean(fullKey, false));
            i++;
        }
        
        FormPane     pane = new FormPane("AddObjPrefs", this, "SystemSetup", "AddObjPrefs", "edit", props, MultiView.NO_OPTIONS | MultiView.DONT_USE_EMBEDDED_SEP, null); // not new data object
        CustomDialog dlg  = new CustomDialog((Frame)getTopWindow(), getResourceString("SYSSTP_AOBTN"), true, pane);
        pane.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        dlg.createUI();
        
        if (!isPrepTypeOK)
        {
            Component comp = pane.getMultiView().getCurrentViewAsFormViewObj().getControlByName(CO_CREATE_PREP);
            if (comp != null)
            {
                comp.setEnabled(false);
                UIRegistry.showLocalizedError("SYSSTP_AOPREP_ERR");
            }
        }
        
        pane.getMultiView().setData(props);
        UIHelper.centerAndShow(dlg);
        if (!dlg.isCancelled())
        {
            pane.getMultiView().getCurrentViewAsFormViewObj().getDataFromUI();
            for (String key : keys)
            {
                Object val = props.get(key);
                if (val != null)
                {
                    remote.putBoolean(key+"_"+colId, (Boolean)val);
                }
            }
        }
    }
    
    /**
     * 
     */
    private void showUsersLoggedIn()
    {
        String sql = " SELECT Name, IsLoggedIn, IsLoggedInReport, LoginCollectionName, LoginDisciplineName FROM specifyuser WHERE IsLoggedIn <> 0";
        Vector<Object[]> dataRows = BasicSQLUtils.query(sql);
        Object[][] data = new Object[dataRows.size()][5];
        for (int i=0;i<dataRows.size();i++)
        {
            data[i] = dataRows.get(i);
        }
        DefaultTableModel model = new DefaultTableModel(data, new Object[] {"User", "Is Logged In", "Is Logged In to Report", "Login Collection", "Login Discipline"});
        JTable table = new JTable(model);
        UIHelper.makeTableHeadersCentered(table, true);
        
        JScrollPane scrollPane = UIHelper.createScrollPane(table);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        CustomDialog infoDlg = new CustomDialog((Dialog)null, "Users Logged In", true, CustomDialog.OK_BTN, panel);
        
        infoDlg.setCancelLabel("Close");
        infoDlg.createUI();
        infoDlg.setSize(600,300);
        infoDlg.setVisible(true);
    }
    
    /**
     * @param isLocked
     */
    private void setLockBtntitle(final Boolean isLocked)
    {
        if (lockDBBtn != null)
        {
            String btnTitle = UIRegistry.getLocalizedMessage(isLocked == null || !isLocked ? "SYSSTP_BTN_BLOCK" : "SYSSTP_BTN_ALLOW");
            lockDBBtn.setLabelText(btnTitle);
        }
    }
    
    /**
     * 
     */
    private void  loadUnlockDB(final boolean doJustBtnLabel)
    {
        Vector<Object[]>rows = query(DBConnection.getInstance().getConnection(), "SELECT IsDBClosed, DbClosedBy FROM spversion ORDER BY TimestampCreated DESC");
        if (rows.size() > 0)
        {
            Object[] row        = (Object[])rows.get(rows.size()-1);
            Boolean  isDBClosed = (Boolean)row[0];
            String   dbClosedBy = (String)row[1];
            
            //log.debug("isDBClosed["+isDBClosed+"]  dbClosedBy["+dbClosedBy+"] ");
            
            if (!doJustBtnLabel)
            {
                String updateStr = "UPDATE spversion SET IsDBClosed=%d, DbClosedBy=%s";
                if (isDBClosed == null || !isDBClosed)
                {
                    isDBClosed = true;
                    SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
                    if (spUser == null)
                    {
                        return;
                    }
                    dbClosedBy = String.format("'%s'", spUser.getName());
                } else
                {
                    isDBClosed = false;
                    dbClosedBy = "NULL";
                }
                
                updateStr = String.format(updateStr, isDBClosed ? 1 : 0, dbClosedBy);
                //log.debug(updateStr);
                int rv = BasicSQLUtils.update(updateStr);
                if (rv == 1)
                {
                    UIRegistry.displayInfoMsgDlgLocalized(isDBClosed ? "SYSSTP_LCK_MSG" : "SYSSTP_UNLCK_MSG");
                    setLockBtntitle(isDBClosed);
                }
            } else
            {
                setLockBtntitle(isDBClosed); 
            }
        }
    }
    
    /* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#doProcessAppCommands(edu.ku.brc.ui.CommandAction)
	 */
	@Override
	protected void doProcessAppCommands(CommandAction cmdAction)
	{
		//super.doProcessAppCommands(cmdAction);  
		for (Pair<BaseTreeTask<?,?,?>, JMenuItem> mi : treeUpdateMenuItems)
		{
			mi.getSecond().setVisible(mi.getFirst().isTreeOnByDefault());
		}
	}

	/* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        if (!isAnyOtherUsersOn() && SubPaneMgr.getInstance().aboutToShutdown())
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
    protected void addPickList(final PickList pickList, @SuppressWarnings("unused") final boolean isNew)
    {
        final String nameStr  = pickList.getName();
        
        @SuppressWarnings("unused")
        RolloverCommand roc;
        if (pickList.getIsSystem())
        {
            roc = (RolloverCommand)NavBox.createBtnWithTT(nameStr, PICKLIST, "", IconManager.STD_ICON_SIZE, new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name", nameStr, name, PICKLIST);
                }
            });
        } /*else
        {
            roc = (RolloverCommand)makeDnDNavBtn(navBox, nameStr, PICKLIST, null, 
                new CommandAction(SYSTEMSETUPTASK, DELETE_CMD_ACT, pickList.getPickListId()), 
                true, true);// true means make it draggable
        
            roc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e)
                {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name", nameStr, name, PICKLIST);
                }
            });
            roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        }
        roc.setData(pickList.getPickListId());
        addPopMenu(roc, pickList);
        if (navBox != null)
        {
            if (isNew)
            {
                navBox.insertSorted((NavBoxItemIFace)roc);
            } else
            {
                navBox.add((NavBoxItemIFace)roc);
            }
        }*/
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
            
            JMenuItem delMenuItem = new JMenuItem(getResourceString("Delete"));
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
            
            JMenuItem viewMenuItem = new JMenuItem(getResourceString("EDIT"));
            viewMenuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent actionEvent) {
                    startEditor(edu.ku.brc.specify.datamodel.PickList.class, "name",  roc.getName(), roc.getName(), PICKLIST);
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
                if (dataObj instanceof java.util.Collection<?>)
                {
                    java.util.Collection<?> collection = (java.util.Collection<?>)dataObj;
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
                ViewIFace view = AppContextMgr.getInstance().getView("Common", viewName);
                if (view == null)
                {
                    view = AppContextMgr.getInstance().getView(null, viewName);
                }
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
                /*if (false)
                {
                    //DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByShortClassName(clazz.getSimpleName());
                    ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame)getTopWindow(),
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
                } else*/
                {
                    FormPane     pane = new FormPane(tableInfo.getTitle(), this, "SystemSetup", viewName, "edit", null, MultiView.RESULTSET_CONTROLLER, null); // not new data object
                    CustomDialog dlg  = new CustomDialog((Frame)getTopWindow(), tableInfo.getTitle(), true, pane);
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
            forceTopFrameRepaint();
        }
    }
    
    /**
     * 
     */
    protected void doSchemaConfig(final Byte schemaType, final DBTableIdMgr tableMgr)
    {
        SpecifyUser spUser = AppContextMgr.getInstance().getClassObject(SpecifyUser.class);
        boolean ok = ((SpecifyAppContextMgr)AppContextMgr.getInstance()).checkToOverrideLogins(spUser.getName());
        if (!ok)
        {
            TaskMgr.getTask("Startup").requestContext();
            return;
        }
        
        UsageTracker.incrUsageCount("SS.SCHEMACFG");
        
        getStatusBar().setIndeterminate(SYSTEMSETUPTASK, true);
        getStatusBar().setText(getResourceString(getI18NKey("LOADING_LOCALES"))); //$NON-NLS-1$
        getStatusBar().repaint();
        
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
                getStatusBar().setText(""); //$NON-NLS-1$
                getStatusBar().setProgressDone(SYSTEMSETUPTASK);
                
                JComponent videoBtn = CollapsableSepExtraCompFactory.getInstance().getComponent("Schema", "Config");
                SchemaToolsDlg dlg = new SchemaToolsDlg((Frame)getTopWindow(), schemaType, tableMgr);
                dlg.setExtraBtn(videoBtn);
                dlg.setVisible(true);
                if (!dlg.isCancelled())
                {
                    TaskMgr.getTask("Startup").requestContext();
                }
            }
        };
        
        // start the background task
        workerThread.start();
    }
    
    
    /**
     * Launches dialog for Importing and Exporting Forms and Resources.
     */
    public static boolean askBeforeStartingTool()
    {
        if (SubPaneMgr.getInstance().aboutToShutdown())
        {
            Object[] options = { getResourceString("CONTINUE"),  //$NON-NLS-1$
                                 getResourceString("CANCEL")  //$NON-NLS-1$
                  };
            return JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(getTopWindow(), 
                                                             getLocalizedMessage(getI18NKey("REI_MSG")),  //$NON-NLS-1$
                                                             getResourceString(getI18NKey("REI_TITLE")),  //$NON-NLS-1$
                                                             JOptionPane.YES_NO_OPTION,
                                                             JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        }
        return false;
    }
    
    /**
     * @return the User Name who will get the resources imported into
     */
    protected String pickUserName()
    {
        Division division = AppContextMgr.getInstance().getClassObject(Division.class);
        String currUserName = (AppContextMgr.getInstance().getClassObject(SpecifyUser.class)).getName();
        String postSQL      = String.format(" FROM specifyuser su INNER JOIN agent a ON su.SpecifyUserID = a.SpecifyUserID " +
                                            "INNER JOIN division d ON a.DivisionID = d.UserGroupScopeId " +
                                            "WHERE su.Name <> '%s' AND d.UserGroupScopeId = %d", currUserName, division.getId());
        
        int count = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) "+postSQL);
        if (count == 0)
        {
            return null;
        }
        
        int choice = askYesNoLocalized("SYSSTP_CHSE_ME", "SYSSTP_CHSE_DIF", getResourceString("SYSSTP_CHSE_USER"), "SYSSTP_CHSE_USER_TITLE");
        if (choice == JOptionPane.YES_OPTION)
        {
            return null; // null means choose the current user
        }
        
        Vector<Object> names = BasicSQLUtils.querySingleCol("SELECT su.Name " + postSQL); 
        if (names.size() == 1)
        {
            return names.get(0).toString();
        }
        
        ChooseFromListDlg<Object> dlg = new ChooseFromListDlg<Object>((Frame)getMostRecentWindow(), getResourceString("SYSSTP_CHSE_USER_TITLE"), names);
        UIHelper.centerAndShow(dlg);
        return !dlg.isCancelled() ? dlg.getSelectedObject().toString() : CANCELLED;
    }

    /**
     * Launches dialog for Importing and Exporting Forms and Resources.
     */
    protected void doResourceImportExport()
    {
        if (askBeforeStartingTool())
        {
            String userName = pickUserName();
            if (userName != null && userName.equals(CANCELLED))
            {
                return;
            }
            
            if (userName == null)
            {
                userName = AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getName();
            }
            
            ResourceImportExportDlg dlg = new ResourceImportExportDlg((SpecifyAppContextMgr)AppContextMgr.getInstance(), userName);
            dlg.setVisible(true);
            if (dlg.hasChanged())
            {
                String fullMsg = dlg.getCompleteMsg() +"\n" + getResourceString("Specify.ABT_EXIT");
                displayInfoMsgDlgLocalized(fullMsg);
                CommandDispatcher.dispatch(new CommandAction(APP_CMD_TYPE, APP_REQ_EXIT));
                
            } else
            {
                TaskMgr.getTask("Startup").requestContext();
            }
        }
    }

    
    /**
     * @param tree
     */
    protected void doTreeUpdate(final BaseTreeTask<?,?,?> tree)
    {
        try
        {
        	boolean success = tree.getCurrentTreeDef().updateAllNodes(null, true, false); //true forces a progress dialog. 
        															   //Currently can't get WriteGlassPane working in this context.(???)
        	if (success)
        	{
        		displayInfoMsgDlgLocalized("SystemSetupTask.TREE_UPDATE_SUCCESS", tree.getTitle());
        	}
        }
        catch (Exception ex)
        {
        	displayErrorDlgLocalized("SystemSetupTask.TREE_UPDATE_DISASTER", tree.getTitle());
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
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
            final boolean isGlobalShutdown = SubPaneMgr.getInstance().isGlobalShutdown(); 
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    if (SubPaneMgr.getInstance().getSubPanes().size() == 0 &&
                        !isGlobalShutdown)
                    {
                        TaskMgr.reenableAllDisabledTasks();
                        TaskMgr.getTask("Startup").requestContext();
                    }
                }
                
            });
            
        }
    }
    
    /**
     * @param key
     * @return
     */
    private static String getI18NKey(final String key)
    {
        return "SystemSetupTask." + key;
    }
    
    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    public List<MenuItemDesc> getMenuItems()
    {
        final String COLSETUP_MENU    = "Specify.COLSETUP_MENU";
        final String TREES_MENU       = "Specify.TREES_MENU";
        final String SYSTEM_MENU      = "Specify.SYSTEM_MENU";
        final String FULL_SYSTEM_MENU  = SYSTEM_MENU + "/" + COLSETUP_MENU;
        final String FULL_TREE_MENU   = SYSTEM_MENU + "/" + TREES_MENU;
        SecurityMgr secMgr = SecurityMgr.getInstance();
        
        menuItems = new Vector<MenuItemDesc>();
        
        MenuItemDesc mid;
        String       titleArg; 
        String       mneu; 
        JMenuItem    mi;
        
        String menuDesc = getResourceString(TREES_MENU);
        
        
        JMenu formsMenu = UIHelper.createLocalizedMenu("Specify.FORMS_MENU", "Specify.FORMS_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
        mid = new MenuItemDesc(formsMenu, SYSTEM_MENU);
        mid.setPosition(MenuItemDesc.Position.Top, menuDesc);
        mid.setSepPosition(MenuItemDesc.Position.After);
        menuItems.add(mid);
        
        JMenu treesMenu = UIHelper.createLocalizedMenu("Specify.TREES_MENU", "Specify.TREES_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
        mid = new MenuItemDesc(treesMenu, SYSTEM_MENU);
        mid.setPosition(MenuItemDesc.Position.Top, menuDesc);
        menuItems.add(mid);
        
        JMenu setupMenu = UIHelper.createLocalizedMenu("Specify.COLSETUP_MENU", "Specify.COLSETUP_MNEU"); //$NON-NLS-1$ //$NON-NLS-2$
        mid = new MenuItemDesc(setupMenu, SYSTEM_MENU);
        mid.setPosition(MenuItemDesc.Position.Top, menuDesc);
        menuItems.add(mid);
        
        if (!AppContextMgr.isSecurityOn() ||
            (getPermissions() != null && getPermissions().canAdd()))
        {
            titleArg = getI18NKey("COLL_CONFIG");
            mneu = getI18NKey("COLL_CONFIG_MNEU");
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
        
        if (!AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(UserType.Manager))
		{
			Vector<BaseTreeTask<?,?,?>> trees = new Vector<BaseTreeTask<?,?,?>>(TreeTaskMgr.getInstance().getTreeTasks());
			Collections.sort(trees, new Comparator<BaseTreeTask<?,?,?>>(){
				@Override
				public int compare(BaseTreeTask<?, ?, ?> arg0, BaseTreeTask<?, ?, ?> arg1)
				{
					return arg0.getTitle().compareTo(arg1.getTitle());
				}
			});
			
        	for (final BaseTreeTask<?, ?, ?> tree : trees)
			{
				titleArg = getResourceString(getI18NKey("Tree_MENU")) + " " + tree.getTitle(); //$NON-NLS-1$
				mneu     = getI18NKey("Trees_MNU"); //$NON-NLS-1$
				mi       = UIHelper.createMenuItemWithAction((JMenu) null, titleArg, mneu, titleArg, true, null);
				mi.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae)
					{
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run()
							{
								doTreeUpdate(tree);							
							}
						});
					}
				});
				mi.setVisible(tree.isTreeOnByDefault());
				treeUpdateMenuItems.add(new Pair<BaseTreeTask<?,?,?>, JMenuItem>(tree, mi));
				mid = new MenuItemDesc(mi, FULL_TREE_MENU);
				mid.setPosition(MenuItemDesc.Position.After, menuDesc);

				menuItems.add(mid);
			}
		}
		
        String securityName = buildTaskPermissionName(RESIMPORTEXPORT_SECURITY);
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             !secMgr.getPermission(securityName).hasNoPerm()))
        {
            titleArg = getI18NKey("RIE_MENU"); //$NON-NLS-1$
            mneu     = getI18NKey("RIE_MNU");  //$NON-NLS-1$
            mi       = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null); 
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    doResourceImportExport();
                }
            });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom, menuDesc);
            mid.setSepPosition(MenuItemDesc.Position.After);
            menuItems.add(mid);
        }
        
        securityName = buildTaskPermissionName(SCHEMACONFIG_SECURITY);
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(securityName) != null && 
             secMgr.getPermission(securityName).canView()))
        {
            titleArg = getI18NKey("SCHEMA_CONFIG_MENU"); //$NON-NLS-1$
            mneu     = getI18NKey("SCHEMA_CONFIG_MNU");  //$NON-NLS-1$
            mi       = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null);
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                	doSchemaConfig(SpLocaleContainer.CORE_SCHEMA, DBTableIdMgr.getInstance());
                }
            });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom);
            menuItems.add(mid);
            
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            if (localPrefs.getBoolean("QCBX_EDITOR", false))
            {
	            menuDesc = getResourceString(titleArg);
	            titleArg = getI18NKey("QCBXEDITOR_MENU"); 
	            mneu     = getI18NKey("QCBXEDITOR_MNEU"); 
	            mi = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null);
	            mi.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent ae)
	                {
	                    TypeSearchForQueryFactory.getInstance().save();
	                    
	                    QueryComboboxEditor tse = new QueryComboboxEditor();
	                    UIHelper.centerAndShow(tse);
	                }
	            }); 
	            mid = new MenuItemDesc(mi, SYSTEM_MENU);
	            mid.setPosition(MenuItemDesc.Position.Bottom);
	            menuItems.add(mid);
            }
        }
        
        
        if (!AppContextMgr.isSecurityOn() || 
            (secMgr.getPermission(WBSCHEMACONFIG_SECURITY) != null && 
             secMgr.getPermission(WBSCHEMACONFIG_SECURITY).canAdd()))
        {
            titleArg = getI18NKey("WBSCHEMA_CONFIG_MENU"); //$NON-NLS-1$
            mneu     = getI18NKey("WBSCHEMA_CONFIG_MNU");  //$NON-NLS-1$
            mi       = UIHelper.createLocalizedMenuItem(titleArg, mneu, titleArg, true, null); 
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    doWorkBenchSchemaConfig();
                }
            });
            mid = new MenuItemDesc(mi, SYSTEM_MENU);
            mid.setPosition(MenuItemDesc.Position.Bottom, menuDesc);
            menuItems.add(mid);
        }
        
        return menuItems;
    }
    
    /**
     * 
     */
    private void doWorkBenchSchemaConfig()
    {
        final DBTableIdMgr tableMgr = new DBTableIdMgr(false);
        tableMgr.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml"))); //$NON-NLS-1$
        
        final Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        String           sql        = String.format("SELECT COUNT(*) FROM splocalecontainer WHERE DisciplineID = %d AND SchemaType = %d", discipline.getId(), SpLocaleContainer.WORKBENCH_SCHEMA);
        int containerCnt = BasicSQLUtils.getCountAsInt(sql);
        if (containerCnt == 0)
        {
            getStatusBar().setIndeterminate(SYSTEMSETUPTASK, true);
            final SwingWorker worker = new SwingWorker()
            {
                private boolean isOK = false;
                
                public Object construct()
                {
                    DataProviderSessionIFace session = null;
                    try
                    {
                        session = DataProviderFactory.getInstance().createSession();
                        session.beginTransaction();
                        Discipline          disp = session.get(Discipline.class, discipline.getId());
                        BuildSampleDatabase bsd  = new BuildSampleDatabase();
                        bsd.loadSchemaLocalization(disp, SpLocaleContainer.WORKBENCH_SCHEMA, tableMgr, null, null, UpdateType.eBuildNew, session);
                        session.commit();
                        isOK = true;
                        
                    } catch (Exception ex)
                    {
                        try
                        {
                            session.rollback();
                        } catch (Exception ex1) {}
                        
                        log.error(ex);
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(SystemSetupTask.class, ex);
                        
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
                    getStatusBar().setProgressDone(SYSTEMSETUPTASK);
                    
                    if (isOK)
                    {
                        SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.WORKBENCH_SCHEMA, discipline.getId(), tableMgr, SchemaI18NService.getCurrentLocale());
                        doSchemaConfig(SpLocaleContainer.WORKBENCH_SCHEMA, tableMgr);
                    }
                }
            };
            worker.start();

        } else
        {
            SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.WORKBENCH_SCHEMA, discipline.getId(), tableMgr, SchemaI18NService.getCurrentLocale());
            doSchemaConfig(SpLocaleContainer.WORKBENCH_SCHEMA, tableMgr);
        }
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
        if (navBox != null)
        {
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
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(SYSTEMSETUPTASK, "ENABLE", null, null, null);
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
        
        SecurityOption secOpt = new SecurityOption(RESIMPORTEXPORT_SECURITY, 
                                                    getResourceString("RIE_TITLE"), 
                                                    securityPrefix,
                                                    new BasicPermisionPanel("RIE_TITLE", 
                                                                            "RIE_SEC_IMPORT", 
                                                                            "RIE_SEC_EXPORT"));
        addPerms(secOpt, new boolean[][] 
                {{true, true, true, false},
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}});
        list.add(secOpt);

        secOpt = new SecurityOption(SCHEMACONFIG_SECURITY, 
                                    getResourceString(getI18NKey("SCHEMA_CONFIG")), 
                                    securityPrefix,
                                    new BasicPermisionPanel(getI18NKey("SCHEMA_CONFIG"), 
                                                            "Enable"));
        addPerms(secOpt, new boolean[][] 
                {{true, false, false, false},
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}});
        list.add(secOpt);


        secOpt = new SecurityOption(WBSCHEMACONFIG_SECURITY, 
                                    getResourceString(getI18NKey("WBSCHEMA_CONFIG")), 
                                    securityPrefix,
                                    new BasicPermisionPanel(getI18NKey("WBSCHEMA_CONFIG"), 
                                                            "Enable"));
        addPerms(secOpt, new boolean[][] 
                {{true, false, false, false},
                {false, false, false, false},
                {false, false, false, false},
                {false, false, false, false}});
        list.add(secOpt);

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
                getStatusBar().setIndeterminate(SYSTEMSETUPTASK, true);
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
                        getStatusBar().setProgressDone(SYSTEMSETUPTASK);
                        
                    }
                };
                worker.start();
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        if (!AppContextMgr.isSecurityOn() || SpecifyUser.isCurrentUserType(UserType.Manager))
        {
            if (AppPreferences.getLocalPrefs().getBoolean("SYSSETUP_TOOLBAR", false))
            {
                ActionListener al = new ActionListener()
                {
                    @Override
                    public void actionPerformed(ActionEvent arg0)
                    {
                        SystemSetupTask.this.requestContext();
                    }
                };
                toolbarItems = new Vector<ToolBarItemDesc>();
                toolbarItems.add(new ToolBarItemDesc(createToolbarButton("System Setup", iconName, "", null, al)));
            }
        }
        return toolbarItems;
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
        } else if (cmdAction.isType(APP_CMD_TYPE)) 
        {
        	//super.doCommand() not being called here, maybe for good reason?,
        	//so calling this here.
            doProcessAppCommands(cmdAction);
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
        getStatusBar().setProgressDone(SYSTEMSETUPTASK);
        
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
                        getStatusBar().setErrorMessage(getResourceString("PL_NO_DEL_SYSPL"));
                    }
                }
            } else
            {
                getStatusBar().setErrorMessage(getResourceString("PL_NO_DEL_PL_INUSE"));
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
