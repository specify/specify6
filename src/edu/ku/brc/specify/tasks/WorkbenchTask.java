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
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.MouseInputAdapter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
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
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.dbsupport.DBTableIdMgr.TableInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.exporters.ExportFileConfigurationFactory;
import edu.ku.brc.specify.exporters.ExportToFile;
import edu.ku.brc.specify.tasks.subpane.wb.ColumnMapperPanel;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalData;
import edu.ku.brc.specify.tasks.subpane.wb.ImportColumnInfo;
import edu.ku.brc.specify.tasks.subpane.wb.ImportDataFileInfo;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
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
    public static final String     NEW_WORKBENCH         = "New Workbench";
    public static final String     NEW_TEMPLATE          = "New Template";
    public static final String     IMPORT_DATA_FILE      = "New Template From File";
    public static final String     EDIT_TEMPLATE         = "Edit Template";
    public static final String     EDIT_WORKBENCH        = "Edit Workbench";
    public static final String     WB_BARCHART           = "WB_BARCHART";
    public static final String     PRINT_REPORT          = "PrintReport";
    public static final String     WB_TOP10_REPORT       = "WB_TOP10_REPORT";
    public static final String     WB_IMPORTCARDS        = "WB_IMPORT_CARDS";
    public static final String     EXPORT_DATA_FILE      = "Export Data";
    public static final String     EXPORT_TEMPLATE       = "ExportTemplate";
    
    protected static WeakReference<DBTableIdMgr> databasechema = null;

    // Data Members
    protected NavBox                      templateNavBox;
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
            
            NavBox navBox = new NavBox(getResourceString("Actions"));
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("WB_IMPORTDATA"),   "Import", new CommandAction(WORKBENCH, IMPORT_DATA_FILE), null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString(WB_IMPORTCARDS),    "Import", new CommandAction(WORKBENCH, WB_IMPORTCARDS),   null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("WB_NEW_TEMPLATE"), "PlusSign", new CommandAction(WORKBENCH, NEW_TEMPLATE),     null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("WB_NEW_DATASET"),  "PlusSign", new CommandAction(WORKBENCH, NEW_WORKBENCH),    null, false);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("WB_EXPORT_DATA"),   "Export", new CommandAction(WORKBENCH, EXPORT_DATA_FILE), null, true);// true means make it draggable
            makeDraggableAndDroppableNavBtn(navBox, getResourceString("WB_EXPORT_TEMPLATE"),   "Export", new CommandAction(WORKBENCH, EXPORT_TEMPLATE), null, true);// true means make it draggable
            
            navBoxes.addElement(navBox);
            
            int dataSetCount = 0;
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                templateNavBox = new NavBox(getResourceString("Templates"), true);           
                List list      = session.getDataList("From WorkbenchTemplate where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
                for (Object obj : list)
                {
                    addTemplateToNavBox((WorkbenchTemplate)obj);
                }
                
                workbenchNavBox = new NavBox(getResourceString("WB_DATASETS"));
                list            = session.getDataList("From Workbench where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
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
                        
                        NavBoxItemIFace nbi = makeDraggableAndDroppableNavBtn(reportsNavBox, tcd.getName(), "Reports", cmdAction, null, true);
                        reportsList.add(nbi);// true means make it draggable
                        enableNavBoxList.add(nbi);

                    } else
                    {
                        log.error("Interaction Command is missing the table id");
                    }
                }
                
                enableNavBoxList.add(makeDraggableAndDroppableNavBtn(reportsNavBox, getResourceString("CHART"),    "Reports", new CommandAction(WORKBENCH, WB_BARCHART), null, false));// true means make it draggable
                enableNavBoxList.add(makeDraggableAndDroppableNavBtn(reportsNavBox, getResourceString("WB_TOP10"), "Reports", new CommandAction(WORKBENCH, WB_TOP10_REPORT), null, false));// true means make it draggable
            }
            
            // Add these last and in order
            navBoxes.addElement(templateNavBox);
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
        
        return databasechema.get();
    }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox.
     * @param workbenchTemplate the template to be added
     */
    protected void addTemplateToNavBox(final WorkbenchTemplate workbenchTemplate)
    {
        RecordSet     rs  = new RecordSet(workbenchTemplate.getName(), WorkbenchTemplate.getClassTableId());
        rs.addItem(workbenchTemplate.getWorkbenchTemplateId());
        RolloverCommand roc = (RolloverCommand)makeDraggableAndDroppableNavBtn(templateNavBox, 
                                                                               workbenchTemplate.getName(),
                                                                               "Template", 
                                                                               (CommandAction)null, 
                                                                               new CommandAction(WORKBENCH, DELETE_CMD_ACT, workbenchTemplate), 
                                                                               true);// true means make it draggable
        roc.setData(rs);
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        
        MouseInputAdapter mouseInputAdapter = new MouseInputAdapter() 
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                RolloverCommand roc = (RolloverCommand)e.getSource();
                if (roc.isEnabled())
                {
                    selectTemplateEditAction(e);
                }
            }
          };
        roc.addMouseListener(mouseInputAdapter);
     }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox
     * @param workbench the workbench to be added
     */
    protected void addWorkbenchToNavBox(final Workbench workbench)
    {
        CommandAction cmd = new CommandAction(WORKBENCH, EDIT_WORKBENCH);
        RecordSet     rs  = new RecordSet(workbench.getName(), Workbench.getClassTableId());
        rs.addItem(workbench.getWorkbenchId());
        cmd.setProperty("workbench", rs);
        RolloverCommand roc = (RolloverCommand)makeDraggableAndDroppableNavBtn(workbenchNavBox, workbench.getName(), name, cmd, 
                                                                               new CommandAction(WORKBENCH, DELETE_CMD_ACT, workbench), 
                                                                               true);// true means make it draggable
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
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
                if (wbp == null)
                {
                    log.error("wbp is null");
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
        File htmlFile = new File("help/SpecifyHelp/Workbench/Overview.html");
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
    protected boolean askUserForInfo(final String viewSetName, 
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
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplateFromScratch()
    {
        JDialog            dlg    = new JDialog((Frame)UICacheManager.get(UICacheManager.FRAME), "Column Mapper", true);
        ColumnMapperPanel  mapper = new ColumnMapperPanel(dlg);
       
        dlg.setContentPane(mapper);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.pack();
        UIHelper.centerAndShow(dlg);
         
        if (!mapper.isCancelled())
        { 
            WorkbenchTemplate workbenchTemplate = createTemplate(mapper, null, null);
            if (workbenchTemplate != null)
            {
                createWorkbench(null, workbenchTemplate, false);
            }

        }
        return null;
    }
    
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplate(final ColumnMapperPanel mapper, final String filePath, final String templateName)
    {
        WorkbenchTemplate workbenchTemplate = null;
        try
        {
            workbenchTemplate = mapper.createWorkbenchTemplate();
            workbenchTemplate.setSrcFilePath(filePath);
            
            String                   newTemplateName = templateName;
            DataProviderSessionIFace session         = DataProviderFactory.getInstance().createSession();
            try
            {
                boolean foundWBT = false;
                do
                {
                    boolean askForInfo = StringUtils.isEmpty(newTemplateName);
                    if (!askForInfo)
                    {
                        askForInfo = session.getData(WorkbenchTemplate.class, "name", newTemplateName, DataProviderSessionIFace.CompareType.Equals) != null;
                    }
                    
                    if (askForInfo)
                    {
                        // We found the same name and it must be unique
                        if (askUserForInfo("WorkbenchTemplate", getResourceString("WB_TEMPLATE_INFO"), workbenchTemplate))
                        {
                            newTemplateName = workbenchTemplate.getName();
                            
                        } else
                        {
                            return null;
                        }
                        foundWBT = true;
                        
                    } else
                    {
                        workbenchTemplate.setName(newTemplateName);
                        foundWBT = false;
                    }
                } while (foundWBT);
                
                session.beginTransaction();
                session.save(workbenchTemplate);
                session.commit();
                session.flush();
                //session.close();
                
                addTemplateToNavBox(workbenchTemplate);
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                session.close();    
            }

            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
        return workbenchTemplate;
    }
    
    /**
     * Displays a list of WorkbenchTemplates that match (exaxtly) the current file. Doesn't
     * show a Dialog and returns null if there are not templates or none match.
     * @return the existing WorkbenchTemplate to use or null
     */
    protected int selectExistingTemplate(final Vector<ImportColumnInfo> colInfo)
    {
        this.selectedTemplate = null;
        
        if (colInfo != null)
        {
            Collections.sort(colInfo);
        }
        
        Vector<WorkbenchTemplate> matchingTemplates = new Vector<WorkbenchTemplate>();
        
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
                        if (wbItem.getViewOrder().shortValue() == fileItem.getColInx().shortValue())
                        {
                            if (ImportColumnInfo.getType(getDataType(wbItem)) == ImportColumnInfo.ColumnType.Date)
                            {
                                ImportColumnInfo.ColumnType colType = fileItem.getColType();
                                if (colType != ImportColumnInfo.ColumnType.String && colType != ImportColumnInfo.ColumnType.Double)
                                {
                                    match = false;
                                    break;
                                }
                            } else if (ImportColumnInfo.getType(getDataType(wbItem)) != fileItem.getColType())
                            {
                                match = false;
                                break;
                            }
    
                        } else
                        {
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
                    getResourceString("WB_CHOOSE_TEMPLATE_TITLE"), 
                    getResourceString("WB_CHOOSE_TEMPLATE_REUSE"), 
                    ChooseFromListDlg.OKCANCELAPPLY,
                    matchingTemplates);
            dlg.setOkLabel(getResourceString(colInfo != null ? "WB_REUSE" : "OK"));
            dlg.setApplyLabel(getResourceString("WB_CREATE_NEW_TEMPLATE"));
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
     * exports template or workbench to xls or csv format.
     * 
     * @param cmdAction
     */
    protected void processExportCmd(final CommandAction cmdAction)
    {
        Workbench workbench = null;
        WorkbenchTemplate template = null;
        boolean doTemplate = false;
        if (cmdAction.isAction(this.EXPORT_DATA_FILE))
        {
            // see if a template was dropped on ExportData
            RecordSet recordSet = (RecordSet) cmdAction.getData();
            doTemplate = recordSet.getDbTableId() == WorkbenchTemplate.getClassTableId();

            // copied from selectWorkbenchTemplate ... not working
            // RecordSet recordSet = (RecordSet)cmdAction.getProperty("template");
            // if (recordSet == null)
            // {
            // Object data = cmdAction.getData();
            // if (data instanceof CommandAction)
            // {
            // recordSet = (RecordSet)((CommandAction)data).getProperty("template");
            // }
            // }
            // doTemplate = recordSet != null;
        }
        else
        {
            doTemplate = true;
        }
        if (doTemplate)
        {
            template = selectWorkbenchTemplate(cmdAction);
        }
        else
        {
            workbench = selectWorkbench(cmdAction);
        }

        if (workbench != null || template != null)
        {
            CommandAction command = new CommandAction(ExportTask.EXPORT, ExportTask.EXPORT_LIST);
            command.setProperty("exporter", ExportToFile.class);
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                if (!doTemplate)
                {
                    session.attach(workbench);
                    command.setData(workbench.getWorkbenchRowsAsList());
                }
                else
                {
                    session.attach(template);
                    Vector<WorkbenchTemplate> data = new Vector<WorkbenchTemplate>(1);
                    data.add(template);
                    command.setData(data);
                }
                // rest of this method is copied from WorkbenchPaneSS.doExcelCsvExport()
                // eventually most of the work will probably be done by Meg's Fancy Configurer UI

                Properties props = new Properties();

                Vector<String> list = new Vector<String>();
                list.add("Excel");
                list.add("CSV");

                ChooseFromListDlg<String> dlg = new ChooseFromListDlg<String>(
                        (Frame) UICacheManager.get(UICacheManager.FRAME), "File Format?", null,
                        ChooseFromListDlg.OKCANCELHELP, list, "WorkbenchImportCvs"); // XXX I18N
                dlg.setModal(true);
                UIHelper.centerAndShow(dlg);

                String format = dlg.getSelectedObject();

                if (format == "Excel")
                {
                    props.setProperty("mimetype", ExportFileConfigurationFactory.XLS_MIME_TYPE);
                }
                else if (format == "CSV")
                {
                    props.setProperty("mimetype", ExportFileConfigurationFactory.CSV_MIME_TYPE);
                }
                else
                {
                    return;
                }

                FileDialog fileDialog = new FileDialog((Frame) UICacheManager
                        .get(UICacheManager.FRAME),
                        getResourceString("CHOOSE_WORKBENCH_EXPORT_FILE"), FileDialog.SAVE);
                UIHelper.centerAndShow(fileDialog);

                String fileName = fileDialog.getFile();
                String path = fileDialog.getDirectory();
                if (StringUtils.isEmpty(fileName)) { return; }
                props.setProperty("fileName", path + File.separator + fileName);

                ConfigureExternalData config = ExportFileConfigurationFactory
                        .getConfiguration(props);

                // Could get config to interactively get props or to look them up from prefs or ???
                // for now hard coding stuff...

                // add headers. all the time for now.
                config.setFirstRowHasHeaders(true);
                Vector<WorkbenchTemplateMappingItem> colHeads = new Vector<WorkbenchTemplateMappingItem>();
                if (doTemplate)
                {
                    colHeads.addAll(template.getWorkbenchTemplateMappingItems());
                }
                else
                {
                    colHeads.addAll(workbench.getWorkbenchTemplate()
                            .getWorkbenchTemplateMappingItems());
                }
                Collections.sort(colHeads);
                String[] heads = new String[colHeads.size()];
                for (int h = 0; h < colHeads.size(); h++)
                {
                    heads[h] = colHeads.get(h).getCaption();
                }
                config.setHeaders(heads);

                props = config.getProperties();
                Enumeration<?> keys = props.propertyNames();
                while (keys.hasMoreElements())
                {
                    String key = (String) keys.nextElement();
                    command.setProperty(key, props.getProperty(key));
                }
            }
            finally
            {
                session.close();
            }
            CommandDispatcher.dispatch(command);
        }
    }
 
        
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplateFromFile()
    {
        // For ease of testing
        File file = null;
        if (true)
        {
            FileDialog fileDialog = new FileDialog((Frame)UICacheManager.get(UICacheManager.FRAME), 
                                                   getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"), 
                                                   FileDialog.LOAD);
            UIHelper.centerAndShow(fileDialog);
            
            String fileName = fileDialog.getFile();
            String path     = fileDialog.getDirectory();
            if (StringUtils.isNotEmpty(fileName))
            {
                file = new File(path + File.separator + fileName);
                
            } else
            {
                return null;
            }
        } else
        {
            file = new File("/home/rods/Documents/_GuyanaTripX.xls");
        }
        
        WorkbenchTemplate workbenchTemplate = null;
        
        if (file.exists())
        {
            ImportDataFileInfo dataFileInfo = new ImportDataFileInfo(file);
            
            int btnPressed = selectExistingTemplate(dataFileInfo.getColInfo());
            workbenchTemplate = selectedTemplate;
            selectedTemplate  = null;
            if (btnPressed == ChooseFromListDlg.APPLY_BTN)
            {
                // Create a new Template 
                JDialog            dlg          = new JDialog((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString("WB_COL_MAPPER"), true);
                ColumnMapperPanel  mapper       = new ColumnMapperPanel(dlg, dataFileInfo);
                
                dlg.setContentPane(mapper);
                dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dlg.pack();
                UIHelper.centerAndShow(dlg);
                 
                if (!mapper.isCancelled())
                {   
                    workbenchTemplate = createTemplate(mapper, file.getAbsolutePath(), FilenameUtils.getBaseName(file.getName()));
                }
                
            } else if (btnPressed == ChooseFromListDlg.CANCEL_BTN)
            {
                return null;
            }
            
            if (workbenchTemplate != null)
            {
                createWorkbench(dataFileInfo, workbenchTemplate, false);
            }
        }
        
        return workbenchTemplate;
    }
    
    /**
     * Creates a new Workbench Data Object from a definition provided by the WorkbenchTemplate and asks for the Workbench fields via a dialog
     * @param workbenchTemplate the WorkbenchTemplate
     * @param wbTemplateIsNew the WorkbenchTemplate is brand new (not reusing an existing template)
     * @param alwaysAskForName indicates it should ask for a name whether the template's name is used or not.
     * @return the new Workbench data object
     */
    protected Workbench createNewWorkbenchDataObj(final WorkbenchTemplate workbenchTemplate,
                                                  final boolean           wbTemplateIsNew,
                                                  final boolean           alwaysAskForName)
    {
        Workbench workbench = null;
        
        if (workbenchTemplate != null)
        {
            workbench = new Workbench();
            workbench.initialize();
            workbench.setSpecifyUser(SpecifyUser.getCurrentUser());
            workbench.setWorkbenchTemplate(workbenchTemplate);
            workbenchTemplate.getWorkbenches().add(workbench);
            
            String                   newWorkbenchName = workbenchTemplate.getName();
            DataProviderSessionIFace session          = DataProviderFactory.getInstance().createSession();
            try
            {
                boolean alwaysAsk = alwaysAskForName;
                Object  foundWB = null;
                do
                {
                    if (StringUtils.isEmpty(newWorkbenchName))
                    {
                        alwaysAsk = true;
                    } else
                    {
                        foundWB = session.getData(Workbench.class, "name", newWorkbenchName, DataProviderSessionIFace.CompareType.Equals);
                    }
                    
                    if (foundWB != null || alwaysAsk)
                    {
                        alwaysAsk = false;
                        
                        // We found the same name and it must be unique
                        if (askUserForInfo("Workbench", getResourceString("WB_DATASET_INFO"), workbench))
                        {
                            newWorkbenchName = workbench.getName();
                        } else
                        {
                            return null;
                        }
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
        }
        return workbench;
    }
    
    /**
     * Creates a new Workbench Data Object from a definition provided by the WorkbenchTemplate
     * @param dataFileInfo the ImportDataFileInfo Object that contains all the information about the file
     * @param wbt the WorkbenchTemplate
     * @param alwaysAskForName indicates it should ask for a name whether the template's name is used or not.
     * @return the new Workbench data object
     */
    protected Workbench createWorkbench(final ImportDataFileInfo dataFileInfo, 
                                        final WorkbenchTemplate  wbt,
                                        final boolean            alwaysAskForName)
    {
        Workbench workbench = createNewWorkbenchDataObj(wbt, true, alwaysAskForName);
        if (workbench != null)
        {
            if (dataFileInfo != null)
            {
                dataFileInfo.loadData(workbench);
                
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
            
            WorkbenchPaneSS workbenchPane = new WorkbenchPaneSS(workbench.getName(), this, workbench, showImageView);
            addSubPaneToMgr(workbenchPane);
            
            RolloverCommand roc = getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
            if (roc != null)
            {
                roc.setEnabled(false);
                
            } else
            {
                log.error("Couldn't find RolloverCommand for WorkbenchId ["+workbench.getWorkbenchId()+"]");
            }
            
            roc = getNavBtnById(templateNavBox, workbench.getWorkbenchTemplate().getWorkbenchTemplateId(), "template");
            if (roc != null)
            {
                roc.setEnabled(false);
                
            } else
            {
                log.error("Couldn't find RolloverCommand for WorkbenchTemplateId ["+workbench.getWorkbenchTemplate().getWorkbenchTemplateId()+"]");
            }
            
            if (session == null && tmpSession != null)
            {
                tmpSession.close();
            }
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
                
                roc = getNavBtnById(templateNavBox, workbench.getWorkbenchTemplate().getWorkbenchTemplateId(), "template");
                if (roc != null)
                {
                    roc.setEnabled(getCountOfPanesForTemplate(workbench.getWorkbenchTemplate().getWorkbenchTemplateId()) < 2);
                    
                } else
                {
                    log.error("Couldn't find RolloverCommand for WorkbenchTemplateId ["+workbench.getWorkbenchTemplate().getWorkbenchTemplateId()+"]");
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
    protected Workbench createNewWorkbench(final WorkbenchTemplate workbenchTemplate,
                                           final boolean           wbTemplateIsNew)
    {
        Workbench workbench = null;
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(workbenchTemplate);
            
            workbench = createNewWorkbenchDataObj(workbenchTemplate, wbTemplateIsNew, false);
            workbench.addRow();

            session.beginTransaction();
            session.save(workbench);
            session.commit();
            session.flush();
            
            addWorkbenchToNavBox(workbench);
            
            createEditorForWorkbench(workbench, session, false);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            session.close();
        }
        
        
        return workbench;
    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbench(final Workbench workbench)
    {
        for (SubPaneIFace sp : SubPaneMgr.getInstance().getSubPanes())
        {
            MultiView mv = sp.getMultiView();
            if (mv != null)
            {
                Object data = mv.getData();
                if (data != null && data == workbench.getWorkbenchRows())
                {
                    SubPaneMgr.getInstance().removePane(sp);
                    break;
                }
            }
        }
        
        NavBoxItemIFace nbi = getBoxByTitle(workbenchNavBox, workbench.getName());
        if (nbi != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            
            try
            {
                session.attach(workbench);
                
                workbench.getWorkbenchTemplate().getWorkbenches().remove(workbench);
                workbench.setWorkbenchTemplate(null);
            
                session.beginTransaction();
                session.delete(workbench);
                session.commit();
                session.flush();
                
                workbenchNavBox.remove(nbi);
                
                NavBoxMgr.getInstance().validate();
                
                updateNavBoxUI(null);
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
                // XXX Error Dialog
                
            } finally 
            {
                session.close();
            }

        }

        log.info("Deleted a Workbench ["+workbench.getName()+"]");

    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbenchTemplate(final WorkbenchTemplate workbenchTemplate)
    {
        if (workbenchTemplate != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(workbenchTemplate);
                
                boolean okToDel = true;
                if (workbenchTemplate.getWorkbenches().size() > 0)
                {
                    String msg = String.format(getResourceString("WBT_DEL_MSG"), new Object[] {workbenchTemplate.getWorkbenches().size()});
                    okToDel = UICacheManager.displayConfirm(getResourceString("WBT_DEL_TITLE"), 
                                                            msg, 
                                                            getResourceString("WBT_DELBTN"), 
                                                            getResourceString("Cancel"),
                                                            JOptionPane.QUESTION_MESSAGE);
                }
                
                if (okToDel)
                {
                    NavBoxItemIFace nbi = getBoxByTitle(templateNavBox, workbenchTemplate.getName());
                    if (nbi != null)
                    {
                        templateNavBox.remove(nbi);
                        
                        session.beginTransaction();
                        for (Workbench wb : workbenchTemplate.getWorkbenches())
                        {
                            NavBoxItemIFace wbNBI = getBoxByTitle(workbenchNavBox, wb.getName());
                            workbenchNavBox.remove(wbNBI);
                            session.delete(wb);                                
                        }
                        workbenchTemplate.getWorkbenches().clear();
                        session.delete(workbenchTemplate);
                        session.commit();
                        session.flush();
                        log.info("Deleted a Workbench ["+workbenchTemplate.getName()+"]");
                        
                        updateNavBoxUI(null);
                        
                        NavBoxMgr.getInstance().validate();

                    }
                } else
                {
                    // XXX Error Dialog needed.
                    log.info("Can't delete workbench template ["+workbenchTemplate.getName()+"]");
                }
                    
                        
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    // XXX Error Dialog
                    
                } finally 
                {
                    session.close();
                }
            }

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
            cmd.setProperty("params", "colnum="+selectMappingItem.getViewOrder()+";"+"title="+selectMappingItem.getCaption());
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
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        
        RecordSet recordSet = (RecordSet)cmdAction.getProperty("workbench");
        if (recordSet == null)
        {
            Object data = cmdAction.getData();
            if (data instanceof CommandAction)
            {
                recordSet = (RecordSet)((CommandAction)data).getProperty("workbench");
            }
        }
        
        Workbench workbench = null;
        if (recordSet == null)
        {
            List<Workbench> list = (List<Workbench>)session.getDataList("From Workbench where SpecifyUserID = "+SpecifyUser.getCurrentUser().getSpecifyUserId());
            if (list.size() == 0)
            {
                // XXX Probably should have a dialog here.
                return null;
                
            } else if (list.size() == 1)
            {
                session.close();
                return list.get(0);
            }
            ChooseFromListDlg<Workbench> dlg = new ChooseFromListDlg<Workbench>((Frame)UICacheManager.get(UICacheManager.FRAME),
                                                                                getResourceString("WB_CHOOSE_DATASET"), list);
            dlg.setModal(true);
            UIHelper.centerAndShow(dlg);
            if (!dlg.isCancelled())
            {
                workbench = dlg.getSelectedObject();
            } else
            {
                session.close();
                return null;
            }
        } else
        {
            workbench = session.get(Workbench.class, recordSet.getItems().iterator().next().getRecordId());
        }
        session.close();
        return workbench;
    }

    /**
     * Returns the Workbench referenced in the CommandAction or asks for one instead.
     * @param cmdAction the CommandAction being executed
     * @return a workbench object or null
     */
    @SuppressWarnings("unchecked")
    protected WorkbenchTemplate selectWorkbenchTemplate(final CommandAction cmdAction)
    {
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        
        RecordSet recordSet = (RecordSet)cmdAction.getProperty("template");
        if (recordSet == null)
        {
            Object data = cmdAction.getData();
            if (data instanceof CommandAction)
            {
                recordSet = (RecordSet)((CommandAction)data).getProperty("template");
            }
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
                                                                                getResourceString("WB_CHOOSE_DATASET"), list);
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
                ToggleButtonChooserDlg.Type.RadioButton);
        
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
                String sql = "select * from (SELECT distinct CellData as Name, count(CellData) as Cnt FROM workbenchdataitem di inner join workbenchrow rw on " +
                    "di.WorkbenchRowId = rw.WorkbenchRowId where rw.WorkBenchId = " +
                    workbench.getWorkbenchId() + " and ColumnNumber = "+selectMappingItem.getViewOrder()+"  group by CellData order by rw.RowNumber asc, di.ColumnNumber asc) t1 order by Cnt desc";
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
    protected void editTemplate(final WorkbenchTemplate workbenchTemplate)
    {
        loadTemplateFromData(workbenchTemplate);
        
        JDialog            dlg          = new JDialog((Frame)UICacheManager.get(UICacheManager.FRAME), "Column Mapper", true);
        ColumnMapperPanel  mapper       = new ColumnMapperPanel(dlg, workbenchTemplate);
       
        dlg.setContentPane(mapper);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.pack();
        UIHelper.centerAndShow(dlg);
         
        if (!mapper.isCancelled())
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {

                WorkbenchTemplate newWorkbenchTemplate = mapper.createWorkbenchTemplate();
                
                session.attach(workbenchTemplate);
                
                Vector<WorkbenchTemplateMappingItem> newItems = new Vector<WorkbenchTemplateMappingItem>(newWorkbenchTemplate.getWorkbenchTemplateMappingItems());
                Vector<WorkbenchTemplateMappingItem> oldItems = new Vector<WorkbenchTemplateMappingItem>(workbenchTemplate.getWorkbenchTemplateMappingItems());
                
                Collections.sort(newItems);
                Collections.sort(oldItems);
                
                // Copying over the New Items
                // Start by looking at each new item and see if it is in the old list
                // if it is in the old list then ignore it
                // if it isn't in the old list than add it into the old template
                
                Hashtable<Short, Short> oldToNewIndex = new Hashtable<Short, Short>();
                short newViewOrder = 0;
                for (WorkbenchTemplateMappingItem wbtmi : newItems)
                {
                    //System.out.print("For ["+wbtmi.getFieldName()+"] ");
                    // find a match
                    boolean foundMatch = false;
                    for (WorkbenchTemplateMappingItem oldWbtmi : oldItems)
                    {
                        if (oldWbtmi.getTableId() == wbtmi.getTableId() &&
                            oldWbtmi.getFieldName().equals(wbtmi.getFieldName()))
                        {
                            foundMatch = true;
                            oldToNewIndex.put(oldWbtmi.getViewOrder(), newViewOrder);
                            oldWbtmi.setViewOrder(newViewOrder++);
                            break;
                        }
                    }
                    if (!foundMatch)
                    {
                        workbenchTemplate.getWorkbenchTemplateMappingItems().add(wbtmi);
                        wbtmi.setViewOrder(newViewOrder++);
                        wbtmi.setWorkbenchTemplate(workbenchTemplate);
                    }
                }
                Hashtable<Short, Short> colDeletedHash = new Hashtable<Short, Short>();
                
                // Removing the Items
                // For each old item (it has to have a non-null ID) see if it is in the new list
                // if it is not in the new then delete it
                // if it is then ignore it
                for (WorkbenchTemplateMappingItem oldWbtmi : oldItems)
                {
                    if (oldWbtmi.getWorkbenchTemplateMappingItemId() == null)
                    {
                        continue; // item is a new one
                    }
                    
                    // Search the list and if the items isn't there than delete it.
                    boolean foundMatch = false;
                    for (WorkbenchTemplateMappingItem wbtmi : newItems)
                    {
                        if (oldWbtmi.getTableId() == wbtmi.getTableId() &&
                            oldWbtmi.getFieldName().equals(wbtmi.getFieldName()))
                        {
                            foundMatch = true;
                            break;
                        }
                    }
                    if (!foundMatch)
                    {
                        workbenchTemplate.getWorkbenchTemplateMappingItems().remove(oldWbtmi);
                        oldWbtmi.setWorkbenchTemplate(null);
                        colDeletedHash.put(oldWbtmi.getViewOrder(), oldWbtmi.getViewOrder());
                    }
                }

                /*
                System.out.println("Mapping Columns:");
                for (Integer oldInx : oldToNewIndex.keySet())
                {
                    System.out.println("["+oldInx+"]["+oldToNewIndex.get(oldInx)+"]");
                }
                System.out.println("");
                */
                
                session.beginTransaction();
                session.saveOrUpdate(workbenchTemplate);
                session.commit();
                session.flush();
                
                for (Workbench workbench : workbenchTemplate.getWorkbenches())
                {
                    session.attach(workbench);
                    session.beginTransaction();
   
                    for (WorkbenchRow row : workbench.getWorkbenchRowsAsList())
                    {
                        Vector<WorkbenchDataItem> items = new Vector<WorkbenchDataItem>(row.getWorkbenchDataItems());
                        for (WorkbenchDataItem item : items)
                        {
                            boolean wasDeleted = false;
                            if (colDeletedHash.get(item.getColumnNumber()) != null)
                            {
                                row.getWorkbenchDataItems().remove(item);
                                item.setWorkbenchRow(null);
                                session.delete(item);
                                wasDeleted = true;
                            }
                            
                            if (!wasDeleted)
                            {
                                item.setColumnNumber(oldToNewIndex.get(item.getColumnNumber()));
                                session.saveOrUpdate(item);
                            }
                        }
                        session.saveOrUpdate(row);
                    }
                    session.saveOrUpdate(workbench);
                    session.commit();
                    session.flush();
                    session.evict(workbench);
                }
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                session.close();    
            }
        }
    }
    
    /**
     * Imports a list if images.
     */
    protected void importCardImages()
    {
        boolean           templateIsNew     = false;
        int               btnPressed        = selectExistingTemplate(null);
        WorkbenchTemplate workbenchTemplate = selectedTemplate;
        selectedTemplate  = null;
        
        if (btnPressed == ChooseFromListDlg.APPLY_BTN)
        {
            // Create a new Template 
            JDialog            dlg          = new JDialog((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString("WB_COL_MAPPER"), true);
            ColumnMapperPanel  mapper       = new ColumnMapperPanel(dlg);
            
            dlg.setContentPane(mapper);
            dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dlg.pack();
            UIHelper.centerAndShow(dlg);
             
            if (!mapper.isCancelled())
            {   
                workbenchTemplate = createTemplate(mapper, null, null);
                templateIsNew     = true;
            }
            
        } else if (btnPressed == ChooseFromListDlg.CANCEL_BTN)
        {
            return;
        }
        
        if (workbenchTemplate != null)
        {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setMultiSelectionEnabled(true);
            
            if (chooser.showOpenDialog(UICacheManager.get(UICacheManager.FRAME)) == JFileChooser.APPROVE_OPTION)
            {
                Workbench workbench = createNewWorkbenchDataObj(workbenchTemplate, templateIsNew, false);
                
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                    session.beginTransaction();
                    
                    for (File file : chooser.getSelectedFiles())
                    {
                        WorkbenchRow row = workbench.addRow();
                        row.setCardImage(file);
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
                    session.close();    
                }
                
            } 
        }
    }
    
    /**
     * Asks the user choose among three options when clicking on a template icon.
     * @param cmdAction the command that issued the action
     */
    protected void selectTemplateEditAction(final MouseEvent mouseEvent)
    {
        RolloverCommand roc = (RolloverCommand)mouseEvent.getSource();
        
        RecordSet                recordSet = (RecordSet)roc.getData();
        DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
        WorkbenchTemplate        workbenchTemplate  = session.get(WorkbenchTemplate.class, recordSet.getOnlyItem().getRecordId());
        // Load the Template's workbench's into memory
        for (Workbench wb : workbenchTemplate.getWorkbenches())
        {
            wb.getWorkbenchId();
        }
        session.close();
            
        String[] options = {getResourceString("WB_EDIT_MAPPINGS"), 
                            getResourceString("WB_EDIT_TEMPLATE_INFO"), 
                            String.format(getResourceString("WB_CREATE_EMPTY_DATASET"), new Object[] {workbenchTemplate.getName()})};

        class MenuAction implements ActionListener
        {
            protected WorkbenchTemplate workbenchTemplate;
            protected int               action;
            protected RolloverCommand   roc;
            
            public MenuAction(final WorkbenchTemplate workbenchTemplate, final RolloverCommand roc, final int action)
            {
                this.workbenchTemplate = workbenchTemplate;
                this.roc               = roc;
                this.action            = action;
            }
            public void actionPerformed(ActionEvent arg0)
            {
                doTemplateAction(workbenchTemplate, roc, action);
                
            }
        }
        JPopupMenu popupMenu = new JPopupMenu();
        for (int i=0;i<options.length;i++)
        {
            UIHelper.createMenuItem(popupMenu, options[i], null, null, true, new MenuAction(workbenchTemplate, roc, i));
        }
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
    }
    
    /**
     * Performs the selected action from the Template popup menu.
     * @param workbenchTemplate the template being selected
     * @param roc the rollover btn that was selected
     * @param action the integer action to perform
     */
    protected void doTemplateAction(final WorkbenchTemplate workbenchTemplate, final RolloverCommand roc, final int action)
    {
        switch (action)
        {
            case 0 : 
                editTemplate(workbenchTemplate);
                break;
                
            case 1 : 
            {
                if (askUserForInfo("WorkbenchTemplate", getResourceString("WB_TEMPLATE_INFO"), workbenchTemplate))
                {
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {

                        session.beginTransaction();
                        session.attach(workbenchTemplate);
                        session.save(workbenchTemplate);
                        session.commit();
                        session.flush();
                        
                        if (roc != null)
                        {
                            roc.setLabelText(workbenchTemplate.getName());
                        }
                        
                    } catch (Exception ex)
                    {
                        log.error(ex);
                        
                    } finally
                    {
                        session.close();    
                    }

                }
            } break;
            
            case 2 : 
                createWorkbench(null, workbenchTemplate, true);
                break;
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
        
        DBTableIdMgr schema = getDatabaseSchema();
        TableInfo tableInfo = schema.getInfoById(wbtmi.getSrcTableId());
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
     * Processes all Commands of type DATA_ENTRY.
     * @param cmdAction the command to be processed
     */
    protected void processWorkbenchCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(EDIT_WORKBENCH))
        {
            RecordSet                recordSet = (RecordSet)cmdAction.getProperty("workbench");
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            Workbench                workbench = session.get(Workbench.class, recordSet.getOnlyItem().getRecordId());
            session.close();
            createEditorForWorkbench(workbench, null, false);
            
        } else if (cmdAction.isAction(NEW_TEMPLATE))
        {
            createTemplateFromScratch();
            
        } else if (cmdAction.isAction(IMPORT_DATA_FILE))
        {
            createTemplateFromFile();
            
        } else if (cmdAction.isAction(EXPORT_DATA_FILE))
        {
            processExportCmd(cmdAction);
           
        } else if (cmdAction.isAction(EXPORT_TEMPLATE))
        {
            processExportCmd(cmdAction);
            
        } else if (cmdAction.isAction(WB_IMPORTCARDS))
        {
            importCardImages();
            
        } else if (cmdAction.isAction(NEW_WORKBENCH))
        {
            WorkbenchTemplate workbenchTemplate = selectWorkbenchTemplate(cmdAction);
            if (workbenchTemplate != null)
            {
                createNewWorkbench(workbenchTemplate, false);
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
            if (cmdAction.getData() instanceof Workbench)
            {
                deleteWorkbench((Workbench)cmdAction.getData());
                
            } else if (cmdAction.getData() instanceof WorkbenchTemplate)
            {
                deleteWorkbenchTemplate((WorkbenchTemplate)cmdAction.getData());
            }
            
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    public void doCommand(CommandAction cmdAction)
    {
        if (cmdAction.isType(WORKBENCH))
        {
            processWorkbenchCommands(cmdAction);
            
        } else if (cmdAction.isType(APP_CMD_TYPE) && cmdAction.isAction(APP_RESTART_ACT))
        {
            //viewsNavBox.clear();
            //initializeViewsNavBox();
        }
    }
}
