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

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.BarChartPane;
import edu.ku.brc.af.tasks.subpane.ChartPane;
import edu.ku.brc.af.tasks.subpane.HtmlDescPane;
import edu.ku.brc.af.tasks.subpane.PieChartPane;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;
import edu.ku.brc.specify.exporters.ExportToFile;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
import edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace;
import edu.ku.brc.specify.tasks.subpane.wb.ImportColumnInfo;
import edu.ku.brc.specify.tasks.subpane.wb.ImportDataFileInfo;
import edu.ku.brc.specify.tasks.subpane.wb.TemplateEditor;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchJRDataSource;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.ui.forms.MultiView;

/**
 * Placeholder for additional work.
 *
 * @code_status Alpha
 *
 * @author meg
 *
 */
public class WorkbenchTask extends BaseTask
{
	private static final Logger log = Logger.getLogger(WorkbenchTask.class);
    
	public static final DataFlavor WORKBENCH_FLAVOR      = new DataFlavor(WorkbenchTask.class, "Workbench");
    public static final String     WORKBENCH             = "Workbench";
    public static final String     WORKBENCHTEMPLATE     = "WorkbenchTemplate";
    public static final String     NEW_WORKBENCH         = "New Workbench";
    public static final String     IMPORT_DATA_FILE      = "New Template From File";
    public static final String     SELECTED_WORKBENCH    = "Selected Workbench";
    public static final String     WB_BARCHART           = "WB_BARCHART";
    public static final String     PRINT_REPORT          = "PrintReport";
    public static final String     WB_TOP10_REPORT       = "WB_TOP10_REPORT";
    public static final String     WB_IMPORTCARDS        = "WB_IMPORT_CARDS";
    public static final String     EXPORT_DATA_FILE      = "Export Data";
    public static final String     EXPORT_TEMPLATE       = "Export Template";
    
    protected static WeakReference<DBTableIdMgr> databasechema = null;

    // Data Members
    protected NavBox                      workbenchNavBox;
    protected Vector<ToolBarDropDownBtn>  tbList         = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus          = new Vector<JComponent>();
    
    protected Vector<NavBoxItemIFace>     reportsList      = new Vector<NavBoxItemIFace>();
    protected Vector<NavBoxItemIFace>     enableNavBoxList = new Vector<NavBoxItemIFace>();
    
    protected WorkbenchTemplate           selectedTemplate = null; // Transient set by selectExistingTemplate

	/**
	 * Constructor. 
	 */
	public WorkbenchTask() 
    {
		super(WORKBENCH, getResourceString(WORKBENCH));
        
		CommandDispatcher.register(WORKBENCH, this);        
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
            
            int wbTblId    = Workbench.getClassTableId(); 
            
            RolloverCommand roc = null;
            NavBox navBox = new NavBox(getResourceString("Actions"));
            makeDnDNavBtn(navBox, getResourceString("WB_IMPORTDATA"), "Import", getResourceString("WB_IMPORTDATA_TT"), new CommandAction(WORKBENCH, IMPORT_DATA_FILE, wbTblId), null, false);// true means make it draggable
            makeDnDNavBtn(navBox, getResourceString(WB_IMPORTCARDS),  "Import", getResourceString("WB_IMPORTCARDS_TT"), new CommandAction(WORKBENCH, WB_IMPORTCARDS, wbTblId),   null, false);// true means make it draggable
            
            makeDnDNavBtn(navBox, getResourceString("WB_NEW_DATASET"),   "PlusSign", getResourceString("WB_NEW_DATASET_TT"), new CommandAction(WORKBENCH, NEW_WORKBENCH, wbTblId),     null, false);// true means make it draggable
            
            roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_DATA"), "Export", getResourceString("WB_EXPORT_DATA_TT"), new CommandAction(WORKBENCH, EXPORT_DATA_FILE, wbTblId), null, true);// true means make it draggable
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
            roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_DATA_FILE));
            enableNavBoxList.add((NavBoxItemIFace)roc);
            
            roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_TEMPLATE"), "Export", getResourceString("WB_EXPORT_TEMPLATE_TT"),new CommandAction(WORKBENCH, EXPORT_TEMPLATE, wbTblId), null, true);// true means make it draggable
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
            roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_TEMPLATE));
            enableNavBoxList.add((NavBoxItemIFace)roc);
            
            navBoxes.addElement(navBox);
            
            int dataSetCount = 0;
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                workbenchNavBox = new NavBox(getResourceString("WB_DATASETS"));
                List list       = session.getDataList("From Workbench where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId()+" order by name");
                dataSetCount    = list.size();
                for (Object obj : list)
                {
                    addWorkbenchToNavBox((Workbench)obj);
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                session.close();    
            }

            
            // Then add
            if (commands != null)
            {
                NavBox reportsNavBox = new NavBox(getResourceString("Reports"));
                
                navBoxes.addElement(reportsNavBox);
                for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/report"))
                {
                    Map<String, String> params = ap.getMetaDataMap();
                    String tableid = params.get("tableid");
                    
                    if (StringUtils.isNotEmpty(tableid) && Integer.parseInt(tableid) == Workbench.getClassTableId())
                    {
                        params.put("title", ap.getDescription());
                        params.put("file", ap.getName());
                        //log.info("["+ap.getDescription()+"]["+ap.getName()+"]");
                        
                        commands.add(new TaskCommandDef(ap.getDescription(), name, params));
                    }
                }
                
                for (TaskCommandDef tcd : commands)
                {
                    // XXX won't be needed when we start validating the XML
                    String tableIdStr = tcd.getParams().get("tableid");
                    if (tableIdStr != null)
                    {
                        //addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(tcd.getName(), "Loan", IconManager.IconSize.Std16, new NavBoxAction(tcd, this)), tcd.getParams());
                        
                        CommandAction cmdAction = new CommandAction(WORKBENCH, PRINT_REPORT, Workbench.getClassTableId());
                        cmdAction.addStringProperties(tcd.getParams());
                        
                        NavBoxItemIFace nbi = makeDnDNavBtn(reportsNavBox, tcd.getName(), "Reports", cmdAction, null, true);// true means make it draggable
                        reportsList.add(nbi);
                        enableNavBoxList.add(nbi);
                        
                        roc = (RolloverCommand)nbi;
                        roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
                        roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));

                    } else
                    {
                        log.error("Interaction Command is missing the table id");
                    }
                }
                
                roc = (RolloverCommand)makeDnDNavBtn(reportsNavBox, getResourceString("CHART"), "Reports", new CommandAction(WORKBENCH, WB_BARCHART, Workbench.getClassTableId()), null, true);
                enableNavBoxList.add((NavBoxItemIFace)roc);// true means make it draggable
                roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));

                roc = (RolloverCommand)makeDnDNavBtn(reportsNavBox, getResourceString("WB_TOP10"), "Reports", new CommandAction(WORKBENCH, WB_TOP10_REPORT, Workbench.getClassTableId()), null, true);
                enableNavBoxList.add((NavBoxItemIFace)roc);// true means make it draggable
                roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));
            }
            
            // Add these last and in order
            // TEMPLATES navBoxes.addElement(templateNavBox);
            navBoxes.addElement(workbenchNavBox);
            
            updateNavBoxUI(dataSetCount);
        }
    }
    
    
    /**
     * Reads in the disciplines file (is loaded when the class is loaded).
     * @return Reads in the disciplines file (is loaded when the class is loaded).
     */
    public static DBTableIdMgr getDatabaseSchema()
    {
        DBTableIdMgr schema = null;
        
        if (databasechema != null)
        {
            schema = databasechema.get();
        }
        
        if (schema == null)
        {
            schema = new DBTableIdMgr(false);
            schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
            databasechema = new WeakReference<DBTableIdMgr>(schema);
        }
        
        return schema;
    }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox
     * @param workbench the workbench to be added
     */
    protected void addWorkbenchToNavBox(final Workbench workbench)
    {
        CommandAction cmd = new CommandAction(WORKBENCH, SELECTED_WORKBENCH, Workbench.getClassTableId());
        RecordSet     rs  = new RecordSet(workbench.getName(), Workbench.getClassTableId());
        rs.addItem(workbench.getWorkbenchId());
        cmd.setProperty("workbench", rs);
        final RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(workbenchNavBox, workbench.getName(), name, cmd, 
                                                                   new CommandAction(WORKBENCH, DELETE_CMD_ACT, rs), 
                                                                   true);// true means make it draggable
        roc.setToolTip(getResourceString("WB_CLICK_EDIT_DATA_TT"));
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        
        roc.addDropDataFlavor(new DataFlavor(Workbench.class, EXPORT_DATA_FILE));
        roc.addDropDataFlavor(new DataFlavor(Workbench.class, "Report"));
        
        roc.addDragDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
       
        JPopupMenu popupMenu = new JPopupMenu();
        UIHelper.createMenuItem(popupMenu, getResourceString("WB_EDIT_PROPS"), getResourceString("WB_EDIT_PROPS_MNEU"), null, true, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                editWorkbenchProps(roc);
            }
        });
        UIHelper.createMenuItem(popupMenu, getResourceString("WB_EDIT_DATASET_MAPPING"), getResourceString("WB_EDIT_DATASET_MAPPING_MNEU"), null, true, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                Object cmdData = roc.getData();
                if (cmdData != null && cmdData instanceof CommandAction)
                {
                    CommandAction subCmd = (CommandAction)cmdData;
                    if (subCmd.getTableId() == Workbench.getClassTableId())
                    {
                        Workbench         wb = selectWorkbench(subCmd);
                        if (wb != null)
                        {
                            WorkbenchTemplate template  = wb.getWorkbenchTemplate();
                            editTemplate(template);
                            
                        } else
                        {
                            log.error("No Workbench selected or found.");
                        }
                    }
                }
            }
        });

        roc.setPopupMenu(popupMenu);

    }
    
    /**
     * Pops up the editor for the Workbench porperties.
     * @param roc the RolloverCommand that invoked it
     */
    protected void editWorkbenchProps(final RolloverCommand roc)
    {
        if (roc != null)
        {
            Workbench workbench = loadWorkbench((RecordSet)((CommandAction)roc.getData()).getProperty("workbench"));
            if (workbench != null && askUserForInfo("Workbench", getResourceString("WB_DATASET_INFO"), workbench))
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
    
                    session.beginTransaction();
                    Workbench mergedWB = (Workbench)session.merge(workbench);
                    session.save(mergedWB);
                    session.commit();
                    session.flush();
                    
                    roc.setLabelText(workbench.getName());

                    NavBox.refresh((NavBoxItemIFace)roc);
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    
                } finally
                {
                    session.close();    
                }
            }
        }
    }
    
    /**
     * Finds the Rollover command that represents the item that has the passed in Record id and
     * it is checking the CmmandAction's attr by name for the RecordSet.
     * @param navBox the nav box of items
     * @param recordId the rcordid to search for
     * @param cmdAttrName the attr name to use when checking CommandActions
     * @return the rollover command that matches
     */
    protected RolloverCommand getNavBtnById(final NavBox navBox, final Long recordId, final String cmdAttrName)
    {
        for (NavBoxItemIFace nbi : navBox.getItems())
        {
            RolloverCommand roc = (RolloverCommand)nbi;
            if (roc != null)
            {
                Object data  = roc.getData();
                if (data != null)
                {
                    RecordSet rs = null;
                    if (data instanceof CommandAction)
                    {
                        CommandAction cmd  = (CommandAction)data;
                        Object prop = cmd.getProperty(cmdAttrName);
                        if (prop instanceof RecordSet)
                        {
                            rs  = (RecordSet)prop;
                        }
                    } else if (data instanceof RecordSet)
                    {
                        rs  = (RecordSet)data;
                    }
                    
                    if (rs != null)
                    {
                        RecordSetItemIFace rsi = rs.getOnlyItem();
                        if (rsi != null && rsi.getRecordId().intValue() == recordId.intValue())
                        {
                            return roc;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Finds the Rollover command that represents the item that has the passed in Record id and
     * it is checking the CmmandAction's attr by name for the RecordSet
     * @param workbench
     * @return
     */
    protected int getCountOfPanesForTemplate(final Long templateId)
    {
        int count = 0;
        for (SubPaneIFace sbi : SubPaneMgr.getInstance().getSubPanes())
        {
            if (sbi.getTask() == this && sbi instanceof WorkbenchPaneSS)
            {
                WorkbenchPaneSS wbp = (WorkbenchPaneSS)sbi;
                
                // XXX Ran into a Bug I can't duplicate so this 
                // should help me track it down the follow should never be NULL
                // but one of them was null
                if (templateId == null)
                {
                    log.error("templateId is null");
                    return count;
                }

                if (wbp.getWorkbench() == null)
                {
                    log.error("wbp.getWorkbench() is null");
                    return count;
                }
                if (wbp.getWorkbench().getWorkbenchTemplate() == null)
                {
                    log.error("wbp.getWorkbench().getWorkbenchTemplate() is null");
                    return count;
                }
                if (wbp.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateId() == null)
                {
                    log.error("wbp.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateId() is null");
                    return count;
                }
                // END DEBUG
                if (wbp.getWorkbench().getWorkbenchTemplate().getWorkbenchTemplateId().longValue() == templateId.longValue())
                {
                    count++;
                }
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.BaseTask#getStarterPane()
     */
    public SubPaneIFace getStarterPane()
    {
        File htmlFile = new File(getResourceString("WB_INITIAL_HTML"));
        if (htmlFile.exists())
        {
            try
            {
                String s = XMLHelper.fixUpHTML(htmlFile);
                if (StringUtils.isNotEmpty(s))
                {
                    return new HtmlDescPane(title, this, s);    
                }
            } catch (Exception ex)
            {
                // no op
            }
        }
        return starterPane = new WorkbenchPaneSS(title, this, null, false);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label    = getResourceString(name);
        String iconName = name;
        String hint     = getResourceString("workbench_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        list.add(new ToolBarItemDesc(btn));
        return list;
    }

    /* (non-Javadoc)
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
     * Ask the user for information needed to fill in the data object.
     * @param data the data object
     * @return true if OK, false if cancelled
     */
    public static boolean askUserForInfo(final String viewSetName, 
                                         final String dlgTitle,
                                         final Object data)
    {
        ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog(
                (Frame)UICacheManager.get(UICacheManager.TOPFRAME),
                "Global",
                viewSetName,
                null,
                dlgTitle,
                getResourceString("OK"),
                null, // className,
                null, // idFieldName,
                true, // isEdit,
                MultiView.HIDE_SAVE_BTN);
        
        editorDlg.setData(data);
        editorDlg.setModal(true);
        editorDlg.setVisible(true);
        
        if (!editorDlg.isCancelled())
        {
            editorDlg.getMultiView().getDataFromUI();
        }
        
        return !editorDlg.isCancelled();
    }
    
    /**
     * CREates and displays the ColumnMapper Dialog (Template Editor), either a DatFileInfo or a Template is passed in, for both can be null.
     * @param dataFileInfo the imported file info
     * @param template an existing template
     * @return the dlg aafter cancel or ok
     */
    protected TemplateEditor showColumnMapperDlg(final ImportDataFileInfo dataFileInfo, 
                                                    final WorkbenchTemplate  template,
                                                    final String             titleKey)
    {
        TemplateEditor  mapper;
        if (template != null)
        {
            mapper = new TemplateEditor((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString(titleKey), template);
        } else
        {
            mapper = new TemplateEditor((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString(titleKey), dataFileInfo);
        }
        UIHelper.centerAndShow(mapper);
        return mapper;
    }
    
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplate(final TemplateEditor mapper, final String filePath, final String templateName)
    {
        WorkbenchTemplate workbenchTemplate = null;
        try
        {
            workbenchTemplate = new WorkbenchTemplate();
            workbenchTemplate.initialize();
            
            workbenchTemplate.setSpecifyUser(SpecifyUser.getCurrentUser());
            
            Set<WorkbenchTemplateMappingItem> items = workbenchTemplate.getWorkbenchTemplateMappingItems();
            Collection<WorkbenchTemplateMappingItem> newItems     = mapper.getNewItems();
            
            for (WorkbenchTemplateMappingItem wbtmi : newItems)
            {
                wbtmi.setWorkbenchTemplate(workbenchTemplate);
                items.add(wbtmi);
                //log.debug("new ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
            }
            workbenchTemplate.setSrcFilePath(filePath);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return workbenchTemplate;
    }
    
    /**
     * If the colInfo Vector is null then all the templates are added to the list to be displayed.<br>
     * If not, then it checks all the column in the file against the columns in each Template to see if there is a match
     * and then uses that.
     * show a Dialog and returns null if there are not templates or none match.
     * @param colInfo the column info
     * @param helpContext the help context
     * @return the existing WorkbenchTemplate to use or null
     */
    protected int selectExistingTemplate(final Vector<ImportColumnInfo> colInfo, final String helpContext)
    {
        this.selectedTemplate = null;
        
        if (colInfo != null)
        {
            Collections.sort(colInfo);
        }
        
        Vector<WorkbenchTemplate> matchingTemplates = new Vector<WorkbenchTemplate>();
        
        // Check for any matches with existing templates
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            List list = session.getDataList("From WorkbenchTemplate where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            for (Object obj : list)
            {
                WorkbenchTemplate template = (WorkbenchTemplate)obj;
                if (colInfo == null)
                {
                    matchingTemplates.add(template);
                    
                } else if (template.getWorkbenchTemplateMappingItems().size() == colInfo.size())
                {
                    boolean match = true;
                    Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(template.getWorkbenchTemplateMappingItems());
                    Collections.sort(items);
                    for (int i=0;i<items.size();i++)
                    {
                        WorkbenchTemplateMappingItem wbItem   = items.get(i);
                        ImportColumnInfo             fileItem = colInfo.get(i);
                        // Check to see if there is an exact match by name
                        if (wbItem.getImportedColName().equalsIgnoreCase(fileItem.getColName()))
                        {
                            ImportColumnInfo.ColumnType type = ImportColumnInfo.getType(getDataType(wbItem));
                            if (type == ImportColumnInfo.ColumnType.Date)
                            {
                                ImportColumnInfo.ColumnType colType = fileItem.getColType();
                                if (colType != ImportColumnInfo.ColumnType.String && colType != ImportColumnInfo.ColumnType.Double)
                                {
                                    //log.error("["+wbItem.getImportedColName()+"]["+fileItem.getColName()+"]["+colType+"]");
                                    match = false;
                                    break;
                                }
                            } else if (type != fileItem.getColType())
                            {
                                //log.error("["+wbItem.getImportedColName()+"]["+fileItem.getColName()+"]["+type+"]["+fileItem.getColType()+"]");
                                match = false;
                                break;
                            }
    
                        } else
                        {
                            //log.error("["+wbItem.getImportedColName()+"]["+fileItem.getColName()+"]");
                            match = false;
                            break;
                        }
                    }
                    // All columns match with their order etc.
                    if (match)
                    {
                        matchingTemplates.add(template);
                    }
                }
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally 
        {
            session.close();
        }
        
        this.selectedTemplate = null;
        
        // Ask the user to choose an existing template.
        if (matchingTemplates.size() > 0)
        {
            ChooseFromListDlg<WorkbenchTemplate> dlg = new ChooseFromListDlg<WorkbenchTemplate>((Frame)UICacheManager.get(UICacheManager.FRAME), 
                    getResourceString("WB_CHOOSE_DATASET_REUSE_TITLE"), 
                    getResourceString("WB_CHOOSE_DATASET_REUSE"), 
                    ChooseFromListDlg.OKCANCELAPPLYHELP,
                    matchingTemplates,
                    helpContext);
            dlg.setCloseOnApply(true);
            dlg.setOkLabel(getResourceString(colInfo != null ? "WB_REUSE" : "OK"));
            dlg.setApplyLabel(getResourceString("WB_NEW_DATASET_MAPPINGS"));
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
            
            if (dlg.getBtnPressed() == ChooseFromListDlg.OK_BTN)
            {
                selectedTemplate = dlg.getSelectedObject();
                loadTemplateFromData(selectedTemplate);
            }
            
            return dlg.getBtnPressed();
        }

        return ChooseFromListDlg.APPLY_BTN;
    }
    
    /**
     * Loads Template completely from the database into memory.
     * @param template the template to be loaded
     * @return true if it was loaded, false if there was error.
     */
    protected boolean loadTemplateFromData(final WorkbenchTemplate template)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            // load workbenches so they aren't lazy
            // this is needed later on when the new WB is added to the template 
            session.attach(template);
            for (Workbench wb : template.getWorkbenches())
            {
                wb.getName();
                template.getWorkbenchTemplateMappingItems().size();
            }
            return true;
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally 
        {
            session.close();
        }
        return false;
        
    }
    
    /**
     * Asks for the type of export and the file name.
     * @param props the properties object to be filled in.
     * @return true if everything was asked for and received.
     */
    public boolean getExportInfo(final Properties props)
    {
        String extension = "";
        if (true)
        {
            for (ExportFileConfigurationFactory.ExportableType type : ExportFileConfigurationFactory.getExportList())
            {
                if (type.getMimeType() == ExportFileConfigurationFactory.XLS_MIME_TYPE)
                {
                    props.setProperty("mimetype", type.getMimeType());
                    extension = type.getExtension();
                    break;
                }
            }
            
        } else
        {
            ChooseFromListDlg<ExportFileConfigurationFactory.ExportableType> dlg = 
                new ChooseFromListDlg<ExportFileConfigurationFactory.ExportableType>((Frame) UICacheManager.get(UICacheManager.FRAME), 
                        getResourceString("WB_FILE_FORMAT"),
                        null,
                        ChooseFromListDlg.OKCANCELHELP, 
                        ExportFileConfigurationFactory.getExportList(), "WorkbenchImportCvs");
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
    
            if (!dlg.isCancelled())
            {
                props.setProperty("mimetype", dlg.getSelectedObject().getMimeType());
                
            } else
            {
                return false;
            }
            extension = dlg.getSelectedObject().getExtension();
        }
        
        FileDialog fileDialog = new FileDialog((Frame) UICacheManager.get(UICacheManager.FRAME),
                                               getResourceString("CHOOSE_WORKBENCH_EXPORT_FILE"), FileDialog.SAVE);
        UIHelper.centerAndShow(fileDialog);

        String fileName = fileDialog.getFile();
        if (StringUtils.isEmpty(fileName))
        {
            UICacheManager.getStatusBar().setErrorMessage(getResourceString("WB_EXPORT_NOFILENAME"));
            return false;
        }
        
        if (StringUtils.isEmpty(FilenameUtils.getExtension(fileName)))
        {
            fileName += (fileName.endsWith(".") ? "" : ".") + extension;
        }

        String path = fileDialog.getDirectory();
        if (StringUtils.isEmpty(fileName)) 
        { 
            return false;
        }
        props.setProperty("fileName", path + File.separator + fileName);

        return true;
    }
    
    /**
     * Exports a Workbench to xls or csv format.
     * @param cmdAction the incoming command request
     */
    protected void exportWorkbench(final CommandAction cmdAction)
    {
       // Check incoming command for a RecordSet contain the Workbench
       Workbench workbench  = null;
       Object    data = cmdAction.getData();
       if (data instanceof CommandAction)
       {
           CommandAction subCmd = (CommandAction)data;
           if (subCmd != cmdAction)
           {
               if (subCmd.getTableId() == cmdAction.getTableId())
               {
                   workbench = selectWorkbench(subCmd);
               }
           }
       }
       
       // The command may have been clicked on so ask for one
       if (workbench == null)
       {
           workbench = selectWorkbench(cmdAction);
       }

       if (workbench != null)
       {
           CommandAction command = new CommandAction(ExportTask.EXPORT, ExportTask.EXPORT_LIST);
           command.setProperty("exporter", ExportToFile.class);
           DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
           try
           {
               session.attach(workbench);
               workbench.forceLoad();
               command.setData(workbench.getWorkbenchRowsAsList());

               // rest of this method is copied from WorkbenchPaneSS.doExcelCsvExport()
               // eventually most of the work will probably be done by Meg's Fancy Configurer UI

               Properties props = new Properties();

               if (!getExportInfo(props))
               {
                   return;
               }

               session.close();
               session = null;
               
               workbench.forceLoad();
               
               sendExportCommand(props, workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), command);
               
           } catch (Exception ex)
           {
               log.error(ex);
           }
           finally
           {
               if (session != null)
               {
                   session.close();
               }
           }
       }
   }
   
    /**
     * Exports Workbench Template to xls or csv format.
     * @param cmdAction the incoming command request
     */
    protected void exportWorkbenchTemplate(final CommandAction cmdAction)
    {
       // Check incoming command for a RecordSet contain the Workbench
       WorkbenchTemplate workbenchTemplate  = null;
       Object    data = cmdAction.getData();
       if (data instanceof CommandAction)
       {
           CommandAction subCmd = (CommandAction)data;
           if (subCmd != cmdAction)
           {
               if (subCmd.getTableId() == cmdAction.getTableId())
               {
                   workbenchTemplate = selectWorkbenchTemplate(subCmd, "WorkbenchTemplateExporting");
                   
               } else
               {
                   return;
               }
           }
       }
       
       if (workbenchTemplate == null)
       {
           workbenchTemplate = selectWorkbenchTemplate(cmdAction, "WorkbenchTemplateExporting");
       }

       // The command may have been clicked on so ask for one
       if (workbenchTemplate != null)
       {
           CommandAction command = new CommandAction(ExportTask.EXPORT, ExportTask.EXPORT_LIST);
           command.setProperty("exporter", ExportToFile.class);
           DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
           try
           {
               session.attach(workbenchTemplate);
               workbenchTemplate.forceLoad();
               Vector<WorkbenchTemplate> newDataRow = new Vector<WorkbenchTemplate>(1);
               newDataRow.add(workbenchTemplate);
               command.setData(newDataRow);

               // rest of this method is copied from WorkbenchPaneSS.doExcelCsvExport()
               // eventually most of the work will probably be done by Meg's Fancy Configurer UI

               Properties props = new Properties();

               if (!getExportInfo(props))
               {
                   return;
               }

               session.close();
               session = null;
               
               sendExportCommand(props, workbenchTemplate.getWorkbenchTemplateMappingItems(), command);
               
           } catch (Exception ex)
           {
               log.error(ex);
           }
           finally
           {
               if (session != null)
               {
                   session.close();
               }
           }
       }
   }
   
    /**
     * Configures and send the Export command action.
     * @param props the properties for the configuration
     * @param items the items to be used for the headers
     * @param cmdAction the command action to send
     */
    public void sendExportCommand(final Properties                        props, 
                                     final Set<WorkbenchTemplateMappingItem> items, 
                                     final CommandAction                     cmdAction)
    {
        ConfigureExternalDataIFace config = ExportFileConfigurationFactory.getConfiguration(props);

        // Could get config to interactively get props or to look them up from prefs or ???
        // for now hard coding stuff...

        // add headers. all the time for now.
        config.setFirstRowHasHeaders(true);
        Vector<WorkbenchTemplateMappingItem> colHeads = new Vector<WorkbenchTemplateMappingItem>(items);
        Collections.sort(colHeads);
        String[] heads = new String[colHeads.size()];
        for (int h = 0; h < colHeads.size(); h++)
        {
            heads[h] = colHeads.get(h).getFieldName();
        }
        config.setHeaders(heads);
        
        cmdAction.addProperties(config.getProperties());
        
        CommandDispatcher.dispatch(cmdAction);
    }

    
    /**
     * Creates a new WorkBench from the Column Headers and the Data in a file.
     * @return the new Workbench
     */
    protected Workbench createNewWorkbenchFromFile()
    {
        // For ease of testing
        File file = null;
        FileDialog fileDialog = new FileDialog((Frame)UICacheManager.get(UICacheManager.FRAME), 
                                               getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"), 
                                               FileDialog.LOAD);
        
        fileDialog.setFilenameFilter(new java.io.FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                for (ExportFileConfigurationFactory.ExportableType exportType : ExportFileConfigurationFactory.getExportList())
                {
                    String ext = FilenameUtils.getExtension(name);
                    if (StringUtils.isNotEmpty(ext) && exportType.getExtension().toLowerCase().equals(ext))
                    {
                        return true;
                    }
                }
                return false;
            }

        });
        UIHelper.centerAndShow(fileDialog);
        
        String fileName = fileDialog.getFile();
        String path     = fileDialog.getDirectory();
        if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(path))
        {
            file = new File(path + File.separator + fileName);
        } else
        {
            return null;
        }
        
        if (file.exists())
        {
            ImportDataFileInfo dataFileInfo = new ImportDataFileInfo();
            if (dataFileInfo.load(file))
            {
                return createNewWorkbench(dataFileInfo, file);
                
            } else if (dataFileInfo.getConfig().getStatus() != ConfigureExternalDataIFace.Status.Cancel)
            {
                JStatusBar statusBar = UICacheManager.getStatusBar();
                statusBar.setErrorMessage(String.format(getResourceString("WB_PARSE_FILE_ERROR"), new Object[] { file.getName() }));
            }
        }
        return null;
    }

    
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected Workbench createNewWorkbench(final ImportDataFileInfo dataFileInfo, 
                                           final File   inputFile)
    {
        
        String wbName = inputFile != null ? FilenameUtils.getBaseName(inputFile.getName()) : null;
        int btnPressed = selectExistingTemplate(dataFileInfo != null ? dataFileInfo.getColInfo() : null, "WorkbenchImportData");
        WorkbenchTemplate workbenchTemplate = selectedTemplate;
        selectedTemplate  = null;
        
        if (btnPressed == ChooseFromListDlg.APPLY_BTN)
        {
            TemplateEditor dlg = showColumnMapperDlg(dataFileInfo, null, "WB_IMP_MAPPING_EDITOR");
            if (!dlg.isCancelled())
            {   
                workbenchTemplate = createTemplate(dlg, 
                                                   inputFile != null ? inputFile.getAbsolutePath() : "", 
                                                   "");
             }
            
        } else if (btnPressed == ChooseFromListDlg.OK_BTN && workbenchTemplate != null)
        {
            workbenchTemplate = cloneWorkbenchTemplate(workbenchTemplate);
        }
        
        if (workbenchTemplate != null)
        {
            Workbench workbench = createNewWorkbenchDataObj(wbName, workbenchTemplate);
            if (workbench != null)
            {
                workbench.setWorkbenchTemplate(workbenchTemplate);
                workbenchTemplate.addWorkbenches(workbench);
                
                fillandSaveWorkbench(dataFileInfo, workbench);
                
                return workbench;
            }
        }
        
       return null;
    }

    /**
     * Creates a new Workbench Data Object from a definition provided by the WorkbenchTemplate and asks for the Workbench fields via a dialog
     * @param wbNamee the Workbench name (can be null or empty)
     * @param workbenchTemplate the WorkbenchTemplate (can be null)
     * @param alwaysAskForName indicates it should ask for a name whether the template's name is used or not.
     * @return the new Workbench data object
     */
    protected Workbench createNewWorkbenchDataObj(final String wbName, 
                                                  final WorkbenchTemplate workbenchTemplate)
    {
        Workbench workbench = new Workbench();
        workbench.initialize();
        workbench.setSpecifyUser(SpecifyUser.getCurrentUser());
        
        if (workbenchTemplate != null)
        {
            workbench.setWorkbenchTemplate(workbenchTemplate);
            workbenchTemplate.getWorkbenches().add(workbench);
            
            if (fillInWorkbenchNameAndAttrs(workbench, wbName))
            {
                workbenchTemplate.setName(workbench.getName());
                return workbench;
            }
        }

        return null;
    }
    
    /**
     * @param workbench
     * @param name
     * @return
     */
    protected boolean fillInWorkbenchNameAndAttrs(final Workbench workbench, final String name)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        try
        {
            String newWorkbenchName = name;
            
            boolean alwaysAsk = true;
            Object  foundWB   = null;
            do
            {
                if (StringUtils.isEmpty(newWorkbenchName))
                {
                    alwaysAsk = true;
                    
                } else
                {
                    foundWB = session.getData(Workbench.class, "name", newWorkbenchName, DataProviderSessionIFace.CompareType.Equals);
                    if (foundWB != null)
                    {
                        UICacheManager.getStatusBar().setErrorMessage(String.format(getResourceString("WB_DATASET_EXISTS"), new Object[] { newWorkbenchName}));
                    }
                }
                
                if (foundWB != null || alwaysAsk)
                {
                    alwaysAsk = false;
                    
                    // We found the same name and it must be unique
                    if (askUserForInfo("Workbench", getResourceString("WB_DATASET_INFO"), workbench))
                    {
                        return true;
                        
                    }
                    // else
                    UICacheManager.getStatusBar().setText("");
                    return false;
                    
                } else
                {
                    workbench.setName(newWorkbenchName);
                }
            } while (foundWB != null);
            
        } catch (Exception ex)
        {
            log.error(ex);
            
        } finally
        {
            session.close();    
        }
        return false;
    }
    
    /**
     * XXX FIX ME
     * @param dataFileInfo the ImportDataFileInfo Object that contains all the information about the file
     * @param workbench the Workbench
     * @return the new Workbench data object
     */
    protected Workbench fillandSaveWorkbench(final ImportDataFileInfo dataFileInfo, 
                                             final Workbench          workbench)
    {
        if (workbench != null)
        {
            if (dataFileInfo != null)
            {
                if (dataFileInfo.loadData(workbench) == DataImportIFace.Status.Error)
                {
                    return null;
                }
                
            } else
            {
                workbench.addRow();
            }
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            try
            {
                session.beginTransaction();
                session.save(workbench);
                session.commit();
                session.flush();
                
                addWorkbenchToNavBox(workbench);
                
                updateNavBoxUI(null);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
            
            createEditorForWorkbench(workbench, session, false);
            
            session.close();
            
        } else
        {
            UICacheManager.getStatusBar().setText("");
        }
        return workbench;
        
    }
    
    /**
     * Creates a name from a Workbench. Tries to use the filename from the original path
     * and if not then it defaults to the generic localized name for the workbench. 
     * @param workbench the workbench
     * @return a non-unique name for a workbench
     */
    protected String createWorkbenchName(final Workbench workbench)
    {
        String srcPath = workbench.getSrcFilePath();
        if (StringUtils.isEmpty(srcPath))
        {
            srcPath = workbench.getWorkbenchTemplate().getSrcFilePath();
        }
        
        if (StringUtils.isNotEmpty(srcPath))
        {
            return new File(srcPath).getName();
        }
        
        return getResourceString("WB_DATASET");
    }

    
    /**
     * Creates the Pane for editing a Workbench.
     * @param workbench the workbench to be edited
     * @param session a session to use to load the workbench (can be null)
     * @param showImageView shows image window when first showing the window
     */
    protected void createEditorForWorkbench(final Workbench workbench, 
                                            final DataProviderSessionIFace session,
                                            final boolean showImageView)
    {
        if (workbench != null)
        {
            // Make sure we have a session but use an existing one if it is passed in
            DataProviderSessionIFace tmpSession = session;
            if (session == null)
            {
                tmpSession = DataProviderFactory.getInstance().createSession();
                tmpSession.attach(workbench);
            }
            
            workbench.forceLoad();
            
            UICacheManager.writeGlassPaneMsg(String.format(getResourceString("WB_LOADING_DATASET"), new Object[] {workbench.getName()}), 32);
            
            final WorkbenchTask            thisTask    = this;
            final DataProviderSessionIFace finiSession = tmpSession;
            final SwingWorker worker = new SwingWorker()
            {
                 public Object construct()
                {
                     try
                     {
                         WorkbenchPaneSS workbenchPane = new WorkbenchPaneSS(workbench.getName(), thisTask, workbench, showImageView);
                         addSubPaneToMgr(workbenchPane);
                         
                         RolloverCommand roc = getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
                         if (roc != null)
                         {
                             roc.setEnabled(false);
                             
                         } else
                         {
                             log.error("Couldn't find RolloverCommand for WorkbenchId ["+workbench.getWorkbenchId()+"]");
                         }
                         
                     } catch (Exception ex)
                     {
                         log.error(ex);
                     } 
                     finally
                     {
                         if (session == null && finiSession != null)
                         {
                             try
                             {
                                 finiSession.close();
                                 
                             } catch (Exception ex)
                             {
                                 log.error(ex);
                             }
                         }
                     }

                    return null;
                }

                //Runs on the event-dispatching thread.
                public void finished()
                {
                    UICacheManager.clearGlassPaneMsg();
                }
            };
            worker.start();
        }
    }
    
    /**
     * Tells the task theat a Workbench Pane is being closed.
     * @param pane the pane being closed.
     */
    public void closing(final WorkbenchPaneSS pane)
    {
        if (pane != null)
        {
            Workbench workbench = pane.getWorkbench();
            if (workbench != null)
            {
                RolloverCommand roc = getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
                if (roc != null)
                {
                    roc.setEnabled(true);
                    
                } else
                {
                    log.error("Couldn't find RolloverCommand for WorkbenchId ["+workbench.getWorkbenchId()+"]");
                }
            }
        }
    }
    
    /**
     * Creates a brand new Workbench from a template with one new row of data.
     * @param workbenchTemplate the template to create the Workbench from
     * @param wbTemplateIsNew the WorkbenchTemplate is brand new (not reusing an existing template)
     * @return the new workbench
     */
    protected WorkbenchTemplate cloneWorkbenchTemplate(final WorkbenchTemplate workbenchTemplateArg)
    {
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(workbenchTemplateArg);
            workbenchTemplateArg.forceLoad();
            return (WorkbenchTemplate)workbenchTemplateArg.clone();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                session.close();
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
        
        return null;
    }
    
    /**
     * Creates a brand new Workbench from a template with one new row of data.
     * @param workbenchTemplate the template to create the Workbench from
     * @param wbTemplateIsNew the WorkbenchTemplate is brand new (not reusing an existing template)
     * @return the new workbench
     */
    protected Workbench saveWorkbenchXXX(final WorkbenchTemplate workbenchTemplateArg)
    {
        boolean isNewTemplate = workbenchTemplateArg.getWorkbenchTemplateId() == null;
        
        Workbench workbench = null;
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            WorkbenchTemplate workbenchTemplate;
            if (!isNewTemplate)
            {
                session.attach(workbenchTemplateArg);
                workbenchTemplateArg.forceLoad();
                workbenchTemplate = (WorkbenchTemplate)workbenchTemplateArg.clone();
                
            } else
            {
                workbenchTemplate = workbenchTemplateArg;
            }
            
            workbench = createNewWorkbenchDataObj(null, workbenchTemplate);
            
            if (workbench != null)
            {
                workbenchTemplate.setName(workbench.getName());
                
                workbench.addRow();
    
                session.beginTransaction();
                session.save(workbenchTemplate);
                session.save(workbench);
                session.commit();
                session.flush();
                
                addWorkbenchToNavBox(workbench);
                
                createEditorForWorkbench(workbench, session, false);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                session.close();
            } catch (Exception ex)
            {
                log.error(ex);
            }
        }
        
        
        return workbench;
    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbench(final RecordSet recordSet)
    {
        
        final Workbench workbench = loadWorkbench(recordSet);
        if (workbench == null)
        {
            return;
        }
        
        UICacheManager.writeGlassPaneMsg(String.format(getResourceString("WB_DELETING_DATASET"), new Object[] {workbench.getName()}), 32);
        
        final SwingWorker worker = new SwingWorker()
        {
            public Object construct()
            {
                try
                {
                    Thread.sleep(500);
                    
                } catch (Exception ex) {}
                
                final NavBoxItemIFace nbi = getBoxByTitle(workbenchNavBox, workbench.getName());
                if (nbi != null)
                {

                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        session.attach(workbench);
                        session.beginTransaction();
                        WorkbenchTemplate template = workbench.getWorkbenchTemplate();
                        session.delete(workbench);
                        session.delete(template);
                        
                        session.commit();
                        session.flush();
                        
                        deleteDnDBtn(workbenchNavBox, nbi);
                        
                        updateNavBoxUI(null);
                        
                    } catch (Exception ex)
                    {
                        ex.printStackTrace();
                        
                    } finally 
                    {
                        try
                        {
                            session.close();
                            
                        } catch (Exception ex)
                        {
                            log.error(ex);
                        }
                    }
                    log.info("Deleted a Workbench ["+workbench.getName()+"]");
                } else
                {
                    log.error("couldn't find nbi for Workbench ["+workbench.getName()+"]");
                }
                    
                return null;
            }

            //Runs on the event-dispatching thread.
            public void finished()
            {
                UICacheManager.clearGlassPaneMsg();
                UICacheManager.getStatusBar().setText(String.format(getResourceString("WB_DELETED_DATASET"), new Object[] {workbench.getName()}));

            }
        };
        worker.start();

    }
    
    /**
     * Creates a report.
     * @param cmdAction the command that initiated it
     */
    protected void doReport(final CommandAction cmdAction)
    {
        Workbench workbench = selectWorkbench(cmdAction);
        if (workbench == null)
        {
            return;
        }
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(workbench);
        WorkbenchTemplate                    workbenchTemplate = workbench.getWorkbenchTemplate();
        Set<WorkbenchTemplateMappingItem>    mappings          = workbenchTemplate.getWorkbenchTemplateMappingItems();
        Vector<WorkbenchTemplateMappingItem> items             = new Vector<WorkbenchTemplateMappingItem>();
        items.addAll(mappings);
        session.close();
        
        String actionStr = cmdAction.getPropertyAsString("action");
        if (StringUtils.isNotEmpty(actionStr) && actionStr.equals("PrintBasicLabel")) // Research into JRDataSources 
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setDbTableId(Workbench.getClassTableId());
            rs.addItem(workbench.getWorkbenchId());
            
            session = DataProviderFactory.getInstance().createSession();
            session.attach(workbench);
            
            workbench.forceLoad();            
            WorkbenchJRDataSource dataSrc = new WorkbenchJRDataSource(workbench, workbench.getWorkbenchRowsAsList());
            session.close();
            
            final CommandAction cmd = new CommandAction(LabelsTask.LABELS, LabelsTask.PRINT_LABEL, dataSrc);
            cmd.setProperty("title",  "Labels");
            cmd.setProperty("file",   "basic_label.jrxml");
            //params hard-coded for harvard demo:
            cmd.setProperty("params", "title=PLANTS OF CONNECTICUT, U.S.A.;subtitle=Hartford County;footer=HARVARD UNIVERSITY HERBARIA");
            cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    CommandDispatcher.dispatch(cmd);
                }
            });
            return;
        }
        
        WorkbenchTemplateMappingItem selectMappingItem = selectMappingItem(items);
        
        if (selectMappingItem != null)
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setDbTableId(Workbench.getClassTableId());
            rs.addItem(workbench.getWorkbenchId());
            
            final CommandAction cmd = new CommandAction(LabelsTask.LABELS, LabelsTask.PRINT_LABEL, rs);
            cmd.setProperty("title",  selectMappingItem.getCaption());
            cmd.setProperty("file",   "wb_items.jrxml");
            cmd.setProperty("params", "colnum="+selectMappingItem.getWorkbenchTemplateMappingItemId()+";"+"title="+selectMappingItem.getCaption());
            cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    CommandDispatcher.dispatch(cmd);
                }
            });
        }
    }
    
    /**
     * Returns the Workbench referenced in the CommandAction or asks for one instead.
     * @param cmdAction the CommandAction being executed
     * @return a workbench object or null
     */
    @SuppressWarnings("unchecked")
    protected Workbench selectWorkbench(final CommandAction cmdAction)
    {
        Workbench workbench = null;

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            RecordSet recordSet = (RecordSet)cmdAction.getProperty("workbench");
            if (recordSet == null)
            {
                Object data = cmdAction.getData();
                if (data instanceof CommandAction)
                {
                    recordSet = (RecordSet)((CommandAction)data).getProperty("workbench");
                    
                } else if (data instanceof RecordSet)
                {
                    recordSet = (RecordSet)data;
                }
            }
            
            if (recordSet != null && recordSet.getDbTableId() != Workbench.getClassTableId())
            {
                return null;
            }
            
            if (recordSet == null)
            {
                List<Workbench> list = (List<Workbench>)session.getDataList("From Workbench where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
                if (list.size() == 0)
                {
                    // XXX Probably should have a dialog here.
                    return null;
                    
                } else if (list.size() == 1)
                {
                    list.get(0).getWorkbenchTemplate().forceLoad();
                    return list.get(0);
                }
                session.close();
                session = null;
                
                ChooseFromListDlg<Workbench> dlg = new ChooseFromListDlg<Workbench>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                                                    getResourceString("WB_CHOOSE_DATASET"), list);
                dlg.setModal(true);
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    session = DataProviderFactory.getInstance().createSession();
                    workbench = dlg.getSelectedObject();
                    session.attach(workbench);
                    workbench.getWorkbenchTemplate().forceLoad();
                    
                } else
                {
                    return null;
                }
            } else
            {
                workbench = session.get(Workbench.class, recordSet.getItems().iterator().next().getRecordId());
                workbench.getWorkbenchTemplate().forceLoad();
            }
            
        } catch (Exception ex)
        {
           log.error(ex); 
        }
        finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                } catch (Exception ex)
                {
                    log.error(ex);
                }
            }
        }
        return workbench;
    }

    /**
     * Returns the Workbench referenced in the CommandAction or asks for one instead.
     * @param cmdAction the CommandAction being executed
     * @param helpContext the help Context
     * @return a workbench object or null
     */
    @SuppressWarnings("unchecked")
    protected WorkbenchTemplate selectWorkbenchTemplate(final CommandAction cmdAction, final String helpContext)
    {
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        
        RecordSet recordSet = (RecordSet)cmdAction.getProperty("template");
        if (recordSet == null)
        {
            Object data = cmdAction.getData();
            if (data instanceof CommandAction)
            {
                recordSet = (RecordSet)((CommandAction)data).getProperty("template");
            }  else if (data instanceof RecordSet)
            {
                recordSet = (RecordSet)data;
            }
        }
        
        if (recordSet != null && recordSet.getDbTableId() != WorkbenchTemplate.getClassTableId())
        {
            return null;
        }
        
        WorkbenchTemplate workbenchTemplate = null;
        if (recordSet == null)
        {
            List<WorkbenchTemplate> list = (List<WorkbenchTemplate>)session.getDataList("From WorkbenchTemplate where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            if (list.size() == 0)
            {
                // XXX Probably should have a dialog here.
                return null;
                
            } else if (list.size() == 1)
            {
                session.close();
                return list.get(0);
            }
            ChooseFromListDlg<WorkbenchTemplate> dlg = new ChooseFromListDlg<WorkbenchTemplate>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                                                getResourceString("WB_CHOOSE_DATASET_REUSE_TITLE"), 
                                                                                null,
                                                                                ChooseFromListDlg.OKCANCELHELP, 
                                                                                list, 
                                                                                helpContext);
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
            if (!dlg.isCancelled())
            {
                workbenchTemplate = dlg.getSelectedObject();
            } else
            {
                session.close();
                return null;
            }
        } else
        {
            workbenchTemplate = session.get(WorkbenchTemplate.class, recordSet.getItems().iterator().next().getRecordId());
        }
        session.close();
        return workbenchTemplate;
    }
    
    /**
     * Asks the user to choose a mapping item (row column) to be used in a report or chart.
     * @param items the list of columns (mappings)
     * @return the selected mapping or null (null means cancelled)
     */
    protected WorkbenchTemplateMappingItem selectMappingItem(final Vector<WorkbenchTemplateMappingItem> items)
    {
        Collections.sort(items);
        
        ToggleButtonChooserDlg<WorkbenchTemplateMappingItem> dlg = new ToggleButtonChooserDlg<WorkbenchTemplateMappingItem>(
                (Frame)UICacheManager.get(UICacheManager.FRAME),
                "WB_SELECT_FIELD_TITLE", 
                "WB_SELECT_FIELD", 
                items, 
                null, 
                CustomDialog.OKCANCELHELP,
                ToggleButtonChooserDlg.Type.RadioButton);
        
        dlg.setHelpContext("WorkbenchReporting");
        dlg.setModal(true);
        dlg.setVisible(true);  
        return dlg.isCancelled() ? null : dlg.getSelectedObject();
    }
    
    /**
     * Creates a BarChart or PieChart.
     * @param cmdAction the action that invoked it
     * @param doBarChart show bar chart
     */
    protected void doChart(final CommandAction cmdAction, final boolean doBarChart)
    {
        Workbench workbench = selectWorkbench(cmdAction);
        if (workbench == null)
        {
            return;
        }
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        session.attach(workbench);
        WorkbenchTemplate                    workbenchTemplate = workbench.getWorkbenchTemplate();
        Set<WorkbenchTemplateMappingItem>    mappings          = workbenchTemplate.getWorkbenchTemplateMappingItems();
        Vector<WorkbenchTemplateMappingItem> items             = new Vector<WorkbenchTemplateMappingItem>();
        items.addAll(mappings);
        session.close();
        
        WorkbenchTemplateMappingItem selectMappingItem = selectMappingItem(items);
        if (selectMappingItem != null)
        {
            final Vector<Object> data = new Vector<Object>();
            try
            {
                if (true)
                {
                    String sql = "SELECT CellData as Name, count(CellData) as Cnt FROM workbenchdataitem di inner join workbenchrow rw on " +
                        "di.WorkbenchRowId = rw.WorkbenchRowId where rw.WorkBenchId = " +
                        workbench.getWorkbenchId() + " and WorkbenchTemplateMappingItemId = "+selectMappingItem.getWorkbenchTemplateMappingItemId()+" group by CellData order by Cnt desc, Name asc";
                    Connection conn = DBConnection.getInstance().createConnection();
                    Statement  stmt = conn.createStatement();
                    
                    ResultSet rs = stmt.executeQuery(sql);
                    int count = 0;
                    while (rs.next() && (doBarChart || count < 10))
                    {
                        data.add(rs.getString(1));
                        data.add(rs.getInt(2));
                        count++;
                    }
                } else
                {
                    Session hibSession = null;
                    try
                    {
                        String hql = "SELECT item.cellData as Name, count(item.cellData) as Cnt FROM WorkbenchDataItem as item inner join item.workbenchRow as row join item.workbenchTemplateMappingItem as mapitem where mapitem.workbenchTemplateMappingItemId = " + selectMappingItem.getWorkbenchTemplateMappingItemId()+" group by item.cellData order by count(item.cellData) desc";
                        hibSession = HibernateUtil.getNewSession();
                        List list = hibSession.createQuery(hql).list();
                        
                        int count = 0;
                        int returnCnt = list.size();
                        while (count < returnCnt && (doBarChart || count < 10))
                        {
                            Object dataRow = list.get(count);
                            if (dataRow instanceof Object[])
                            {
                                for (Object o : (Object[])dataRow)
                                {
                                    data.add(o);
                                }
                            }
                            count++;
                        }
                        
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                    }
                    finally 
                    {
                        if (hibSession != null)
                        {
                            try
                            {
                                hibSession.close();
                            } catch (Exception ex)
                            {
                                log.error(ex);
                            }
                        }
                    }
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
            
            QueryResultsHandlerIFace qrhi = new QueryResultsHandlerIFace()
            {
                public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainerIFace> list) {}
                public void init(final QueryResultsListener listener, final QueryResultsContainerIFace qrc){}
                public void startUp(){ }
                public void cleanUp() {}
    
                public java.util.List<Object> getDataObjects()
                {
                    return data;
                }
    
                public boolean isPairs()
                {
                    return true;
                }
            };
            
            String    chartTitle = String.format(getResourceString("WB_CHART_TITLE"), new Object[] {selectMappingItem.getCaption()});
            ChartPane chart      = doBarChart ? new BarChartPane(chartTitle, this) : new PieChartPane(chartTitle, this);
            chart.setTitle(chartTitle);
            chart.setHandler(qrhi);
            chart.allResultsBack();
            addSubPaneToMgr(chart);
        }
    }
    
    /**
     * Updates the UI elements that depend on there being at least one Workbench (like report etc).
     */
    protected void updateNavBoxUI(final Integer wbCount)
    {
        Integer count = wbCount;
        if (count == null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            count   = session.getDataCount(Workbench.class, "specifyUser", SpecifyUser.getCurrentUser(), DataProviderSessionIFace.CompareType.Equals);
            session.close();            
        }
        
        boolean enabled = count != null && count.intValue() > 0;
        for (NavBoxItemIFace nbi : enableNavBoxList)
        {
            nbi.setEnabled(enabled);
        }
    }
    
    /**
     * Show the dialog to allow the user to edit a template and then updates the data rows and columns..
     * @param workbenchTemplate the template to be edited
     */
    protected void editTemplate(final WorkbenchTemplate wbTemplate)
    {
        loadTemplateFromData(wbTemplate);
        
        TemplateEditor dlg = showColumnMapperDlg(null, wbTemplate, 
                                                   StringUtils.isNotEmpty(wbTemplate.getSrcFilePath()) ? 
                                                            "WB_IMP_TEMPLATE_EDITOR" : "WB_TEMPLATE_EDITOR");
        if (!dlg.isCancelled())
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                Collection<WorkbenchTemplateMappingItem> unusedItems  = dlg.getUnusedItems();
                Collection<WorkbenchTemplateMappingItem> newItems     = dlg.getNewItems();
                //Collection<WorkbenchTemplateMappingItem> updatedItems = dlg.getUpdatedItems();
                
                WorkbenchTemplate workbenchTemplate = (WorkbenchTemplate)session.merge(wbTemplate);
                session.beginTransaction();
                
                Set<WorkbenchTemplateMappingItem> items = workbenchTemplate.getWorkbenchTemplateMappingItems();

                for (WorkbenchTemplateMappingItem wbtmi : unusedItems)
                {
                    //log.debug("del ["+wbtmi.getCaption()+"]["+wbtmi.getWorkbenchTemplateMappingItemId().longValue()+"]");
                    wbtmi.setWorkbenchTemplate(null);
                    items.remove(wbtmi);
                    session.delete(wbtmi) ;
                }
                for (WorkbenchTemplateMappingItem wbtmi : newItems)
                {
                    wbtmi.setWorkbenchTemplate(workbenchTemplate);
                    items.add(wbtmi);
                    //log.debug("new ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
                    session.saveOrUpdate(wbtmi) ;
                }
                //for (WorkbenchTemplateMappingItem wbtmi : updatedItems)
                //{
                    //log.debug("ext ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
                //}
                session.saveOrUpdate(workbenchTemplate);
                session.commit();
                session.flush();
                
                /*
                for (Workbench workbench : workbenchTemplate.getWorkbenches())
                {
                    session.attach(workbench);
                    session.beginTransaction();
   
                    for (WorkbenchRow row : workbench.getWorkbenchRowsAsList())
                    {
                        Vector<WorkbenchDataItem> rowItems = new Vector<WorkbenchDataItem>(row.getWorkbenchDataItems());
                        for (WorkbenchDataItem item : rowItems)
                        {
                            System.out.println("["+item.getCellData()+"] ["+item.getWorkbenchTemplateMappingItem().getWorkbenchTemplateMappingItemId()+"]");
                            if (delHash.get(item.getWorkbenchTemplateMappingItem().getWorkbenchTemplateMappingItemId().longValue()) != null)
                            {
                                row.delete(item);
                                session.delete(item);
                            }
                        }
                        session.saveOrUpdate(row);
                    }
                    session.saveOrUpdate(workbench);
                    session.commit();
                    session.flush();
                    session.evict(workbench);
                }*/
                
                UICacheManager.getStatusBar().setText(getResourceString("WB_SAVED_MAPPINGS"));
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                try
                {
                    session.close();
                } catch (Exception ex)
                {
                    log.error(ex);
                }  
            }
        }
    }
    
    /**
     * Imports a list if images.
     */
    protected void importCardImages()
    {
        int               btnPressed        = selectExistingTemplate(null, "WorkbenchImportImages");
        WorkbenchTemplate workbenchTemplate = selectedTemplate;
        selectedTemplate  = null;
        
        if (btnPressed == ChooseFromListDlg.APPLY_BTN)
        {
            // Create a new Template 
            TemplateEditor dlg = showColumnMapperDlg(null, null, "WB_MAPPING_EDITOR");
            if (!dlg.isCancelled())
            {   
                workbenchTemplate = createTemplate(dlg, null, null);
            }
            
        } else if (btnPressed == ChooseFromListDlg.CANCEL_BTN)
        {
            return;
        }
        
        if (workbenchTemplate != null)
        {
            Workbench workbench = createNewWorkbenchDataObj("", cloneWorkbenchTemplate(workbenchTemplate));
            if (workbench != null)
            {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setMultiSelectionEnabled(true);
                
                if (chooser.showOpenDialog(UICacheManager.get(UICacheManager.FRAME)) == JFileChooser.APPROVE_OPTION)
                {

                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
                        session.beginTransaction();
                        
                        for (File file : chooser.getSelectedFiles())
                        {
                            WorkbenchRow row = workbench.addRow();
                            row.setCardImage(file);
                            if (row.getLoadStatus() != WorkbenchRow.LoadStatus.Successful)
                            {
                                if (!showLoadStatus(row))
                                {
                                    // Shoud we still save or return?
                                    break; 
                                }
                            }
                        }
                        session.saveOrUpdate(workbench);
                        session.commit();
                        session.flush();
                        
                        addWorkbenchToNavBox(workbench);
                        
                        createEditorForWorkbench(workbench, session, true);
    
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        
                    } finally
                    {
                        try
                        {
                            session.close();
                        } catch (Exception ex)
                        {
                            log.error(ex);
                        }
                    }
                } else
                {
                    UICacheManager.getStatusBar().setText("");
                }
            } 
        }
    }
    
    /**
     * Show error dialog for image load.
     * @param status the status of the load.
     * @param loadException the excpetion that occurred.
     * @return true to continue, false to stop
     */
    public static boolean showLoadStatus(final WorkbenchRow row)
    {
        String key = "WB_ERROR_IMAGE_GENERIC";
        switch (row.getLoadStatus())
        {
            case TooLarge : 
                key = "WB_ERROR_IMAGE_TOOLARGE";
                break;
                
            case OutOfMemory : 
                key = "WB_ERROR_IMAGE_MEMORY";
                break;
            default:
                break;
        }
        
        JStatusBar statusBar = UICacheManager.getStatusBar();
        statusBar.setErrorMessage(getResourceString(key), row.getLoadException());
        
        return UICacheManager.displayConfirmLocalized("WB_ERROR_LOAD_IMAGE", key, "Continue", "WB_STOP_LOADING", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Loads a Workbench into Memory 
     * @param recordSet the RecordSet containing thew ID
     * @return the workbench or null
     */
    protected Workbench loadWorkbench(final RecordSet recordSet)
    {
        if (recordSet != null)
        {
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            try
            {
                Workbench workbench  = session.get(Workbench.class, recordSet.getOnlyItem().getRecordId());
                if (workbench != null)
                {
                    workbench.getWorkbenchId();
                }
                return workbench;
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
            finally
            {
                session.close();            
            }
        }
        return null;
    }

    /**
     * Loads a Workbench into Memory 
     * @param recordSet the RecordSet containing thew ID
     * @return the workbench or null
     */
    protected WorkbenchTemplate loadWorkbenchTemplate(final RecordSet recordSet)
    {
        if (recordSet != null)
        {
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            try
            {
                WorkbenchTemplate workbenchTemplate = session.get(WorkbenchTemplate.class, recordSet.getOnlyItem().getRecordId());
                if (workbenchTemplate != null)
                {
                    workbenchTemplate.forceLoad();
                }
                return workbenchTemplate;
                
            } catch (Exception ex)
            {
                log.error(ex);
            }
            finally
            {
                session.close();            
            }
        }
        return null;
    }
    
    /**
     * Looks for the embedded CommandAction and processes that as the command.
     * @param cmdAction the command that was invoked
     */
    protected void workbenchSelected(final CommandAction cmdAction)
    {
        Object cmdData = cmdAction.getData();
        if (cmdData != null && cmdData instanceof CommandAction && cmdData != cmdAction)
        {
            CommandAction subCmd = (CommandAction)cmdData;
            if (subCmd.getTableId() == Workbench.getClassTableId())
            {
                if (!subCmd.isAction(cmdAction.getAction()))
                {
                    subCmd.setProperty("workbench", cmdAction.getProperty("workbench"));
                    processWorkbenchCommands(subCmd);
                    subCmd.getProperties().remove("workbench");
                    
                } else
                {
                    return;
                }
            }
            
        } else
        {
            // This is for when the user clicks directly on the workbench
            Workbench workbench = loadWorkbench((RecordSet)cmdAction.getProperty("workbench"));
            if (workbench != null)
            {
                createEditorForWorkbench(workbench, null, false);
            } else
            {
                log.error("Workbench was null!");
            }
        }
    }
    
    /**
     * Returns the class of the DB field target of this mapping.
     *
     * @return a {@link Class} object representing the DB target field of this mapping.
     */
    public static Class<?> getDataType(final WorkbenchTemplateMappingItem wbtmi)
    {
        // if this mapping item doesn't correspond to a DB field, return the java.lang.String class
        if (wbtmi.getSrcTableId() == null)
        {
            return String.class;
        }
        
        DBTableIdMgr schema    = getDatabaseSchema();
        TableInfo    tableInfo = schema.getInfoById(wbtmi.getSrcTableId());
        if (tableInfo == null)
        {
            throw new RuntimeException("Cannot find TableInfo in DBTableIdMgr for ID=" + wbtmi.getSrcTableId());
        }
        
        for (DBTableIdMgr.FieldInfo fi : tableInfo.getFields())
        {
            if (fi.getName().equals(wbtmi.getFieldName()))
            {
                String type = fi.getType();
                if (StringUtils.isNotEmpty(type))
                {
                    if (type.equals("calendar_date"))
                    {
                        return Calendar.class;
                        
                    } else if (type.equals("text"))
                    {
                        return String.class;
                        
                    } else if (type.equals("boolean"))
                    {
                        return Boolean.class;
                        
                    } else
                    {
                        try
                        {
                            return Class.forName(type);
                            
                        } catch (Exception e)
                        {
                            log.error(e);
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Could not find [" + wbtmi.getFieldName()+"]");
    }

    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------
    
    /**
     * Processes all Commands of type WORKBENCH.
     * @param cmdAction the command to be processed
     */
    protected void processWorkbenchCommands(final CommandAction cmdAction)
    {
        boolean isClickedOn = cmdAction.getData() instanceof CommandAction && cmdAction.getData() == cmdAction;

        if (cmdAction.isAction(SELECTED_WORKBENCH))
        {
            workbenchSelected(cmdAction);
            
        } else if (cmdAction.isAction(IMPORT_DATA_FILE))
        {
            if (isClickedOn)
            {
                createNewWorkbenchFromFile();
                
            }
            
        } else if (cmdAction.isAction(EXPORT_DATA_FILE))
        {
            exportWorkbench(cmdAction);
           
        } else if (cmdAction.isAction(EXPORT_TEMPLATE))
        {
            exportWorkbenchTemplate(cmdAction);
            
        } else if (cmdAction.isAction(WB_IMPORTCARDS))
        {
            if (isClickedOn)
            {
                importCardImages();
            }
            
        } else if (cmdAction.isAction(NEW_WORKBENCH))
        {
            if (isClickedOn)
            {
                createNewWorkbench(null, null);
            }
            
        } else if (cmdAction.isAction(WB_BARCHART))
        {
            doChart(cmdAction, true);
                
        } else if (cmdAction.isAction(WB_TOP10_REPORT))
        {
            doChart(cmdAction, false);
                
        } else if (cmdAction.isAction(PRINT_REPORT))
        {
            doReport(cmdAction);
            
        } else if (cmdAction.isAction(DELETE_CMD_ACT))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                RecordSet rs = (RecordSet)cmdAction.getData();
                if (rs.getDbTableId() == Workbench.getClassTableId())
                {
                    deleteWorkbench(rs);
                    
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(WORKBENCH))
        {
            //SwingUtilities.invokeLater(new Runnable() {
            //    public void run()
            //    {
                    processWorkbenchCommands(cmdAction);
            //    }
            //});
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            //viewsNavBox.clear();
            //initializeViewsNavBox();
        }
    }
}
