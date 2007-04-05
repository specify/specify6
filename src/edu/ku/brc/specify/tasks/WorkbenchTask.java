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
import java.util.Collections;
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
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
import edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace;
import edu.ku.brc.specify.tasks.subpane.wb.ImportColumnInfo;
import edu.ku.brc.specify.tasks.subpane.wb.ImportDataFileInfo;
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
    
	public static final DataFlavor WORKBENCH_FLAVOR      = new DataFlavor(WorkbenchTask.class, "OnRamp");
    public static final String     WORKBENCH             = "OnRamp";
    public static final String     WORKBENCHTEMPLATE     = "OnRampTemplate";
    public static final String     NEW_WORKBENCH         = "New OnRamp";
    public static final String     NEW_TEMPLATE          = "New Template";
    public static final String     IMPORT_DATA_FILE      = "New Template From File";
    public static final String     SELECTED_TEMPLATE     = "Selected Template";
    public static final String     SELECTED_WORKBENCH    = "Selected OnRamp";
    public static final String     WB_BARCHART           = "WB_BARCHART";
    public static final String     PRINT_REPORT          = "PrintReport";
    public static final String     WB_TOP10_REPORT       = "WB_TOP10_REPORT";
    public static final String     WB_IMPORTCARDS        = "WB_IMPORT_CARDS";
    public static final String     EXPORT_DATA_FILE      = "Export Data";
    public static final String     EXPORT_TEMPLATE       = "Export Template";
    
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
            
            int wbTblId    = Workbench.getClassTableId(); 
            int wbTmpTblId = WorkbenchTemplate.getClassTableId();
            
            RolloverCommand roc = null;
            NavBox navBox = new NavBox(getResourceString("Actions"));
            makeDnDNavBtn(navBox, getResourceString("WB_IMPORTDATA"),   "Import", new CommandAction(WORKBENCH, IMPORT_DATA_FILE, wbTblId), null, false);// true means make it draggable
            makeDnDNavBtn(navBox, getResourceString(WB_IMPORTCARDS),    "Import", new CommandAction(WORKBENCH, WB_IMPORTCARDS, wbTblId),   null, false);// true means make it draggable
            makeDnDNavBtn(navBox, getResourceString("WB_NEW_TEMPLATE"), "PlusSign", new CommandAction(WORKBENCH, NEW_TEMPLATE, wbTmpTblId),     null, false);// true means make it draggable
            makeDnDNavBtn(navBox, getResourceString("WB_NEW_DATASET"),  "PlusSign", new CommandAction(WORKBENCH, NEW_WORKBENCH, wbTblId),    null, false);// true means make it draggable
            
            roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_DATA"),   "Export", new CommandAction(WORKBENCH, EXPORT_DATA_FILE, wbTblId), null, true);// true means make it draggable
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCH));
            roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_DATA_FILE));
            
            roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_TEMPLATE"),   "Export", new CommandAction(WORKBENCH, EXPORT_TEMPLATE, wbTmpTblId), null, true);// true means make it draggable
            roc.addDropDataFlavor(new DataFlavor(Workbench.class, WORKBENCHTEMPLATE));
            roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_TEMPLATE));
            
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
        
        return schema;
    }
    
    /**
     * Adds a WorkbenchTemplate to the Left Pane NavBox.
     * @param workbenchTemplate the template to be added
     */
    protected void addTemplateToNavBox(final WorkbenchTemplate workbenchTemplate)
    {
        CommandAction cmd = new CommandAction(WORKBENCH, SELECTED_TEMPLATE, WorkbenchTemplate.getClassTableId());
        RecordSet     rs  = new RecordSet(workbenchTemplate.getName(), WorkbenchTemplate.getClassTableId());
        rs.addItem(workbenchTemplate.getWorkbenchTemplateId());
        cmd.setProperty("template", rs);
        
        final RolloverCommand roc = (RolloverCommand)makeDnDNavBtn(templateNavBox, 
                                                                   workbenchTemplate.getName(),
                                                                   "Template", 
                                                                   cmd, 
                                                                   new CommandAction(WORKBENCH, DELETE_CMD_ACT, rs), 
                                                                   true);// true means make it draggable
        roc.setData(rs);
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(new DataFlavor(Workbench.class, WORKBENCHTEMPLATE));
        roc.addDropDataFlavor(new DataFlavor(Workbench.class, EXPORT_TEMPLATE));
        
        JPopupMenu popupMenu = new JPopupMenu();
        UIHelper.createMenuItem(popupMenu, getResourceString("WB_EDIT_PROPS"), getResourceString("WB_EDIT_PROPS_MNEU"), null, true, new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                editWorkbenchTemplateProps(roc);
            }
        });
        roc.setPopupMenu(popupMenu);
        
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
     * Pops up the editor for the WorkbenchTemplate porperties.
     * @param roc the RolloverCommand that invoked it
     */
    protected void editWorkbenchTemplateProps(final RolloverCommand roc)
    {
        if (roc != null)
        {
            WorkbenchTemplate workbenchTemplate = loadWorkbenchTemplate((RecordSet)roc.getData());
            if (workbenchTemplate != null && askUserForInfo("WorkbenchTemplate", getResourceString("WB_TEMPLATE_INFO"), workbenchTemplate))
            {
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
    
                    session.beginTransaction();
                    WorkbenchTemplate mergedWBT = (WorkbenchTemplate)session.merge(workbenchTemplate);
                    session.save(mergedWBT);
                    session.commit();
                    session.flush();
                    
                    roc.setLabelText(workbenchTemplate.getName());

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
    protected JDialog showColumnMapperDlg(final ImportDataFileInfo dataFileInfo, 
                                          final WorkbenchTemplate  template,
                                          final String             titleKey)
    {
        JDialog            dlg    = new JDialog((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString(titleKey), true);
        ColumnMapperPanel  mapper;
        if (template != null)
        {
            mapper = new ColumnMapperPanel(dlg, template);
        } else
        {
            mapper = new ColumnMapperPanel(dlg, dataFileInfo);
        }
       
        dlg.setContentPane(mapper);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.getRootPane().setDefaultButton(mapper.getOkBtn());
        dlg.pack();
        UIHelper.centerAndShow(dlg);
        return dlg;
    }
    
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplateFromScratch()
    {
        JDialog           dlg = showColumnMapperDlg(null, null, "WB_TEMPLATE_EDITOR");
        ColumnMapperPanel cmp = (ColumnMapperPanel)dlg.getContentPane();
        if (!cmp.isCancelled())
        { 
            WorkbenchTemplate workbenchTemplate = createTemplate(cmp, null, null);
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
                            ImportColumnInfo.ColumnType type = ImportColumnInfo.getType(wbItem.getDataFieldClass());
                            if (type == ImportColumnInfo.ColumnType.Date)
                            {
                                ImportColumnInfo.ColumnType colType = fileItem.getColType();
                                if (colType != ImportColumnInfo.ColumnType.String && colType != ImportColumnInfo.ColumnType.Double)
                                {
                                    match = false;
                                    break;
                                }
                            } else if (type != fileItem.getColType())
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
                    ChooseFromListDlg.OKCANCELAPPLYHELP,
                    matchingTemplates,
                    helpContext);
            dlg.setCloseOnApply(true);
            dlg.setOkLabel(getResourceString(colInfo != null ? "WB_REUSE" : "OK"));
            dlg.setApplyLabel(getResourceString("WB_NEW_TEMPLATE"));
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
            ((JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR)).setErrorMessage(getResourceString("WB_EXPORT_NOFILENAME"));
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
                   workbenchTemplate = selectWorkbenchTemplate(subCmd, "OnRampTemplateExporting");
                   
               } else
               {
                   return;
               }
           }
       }
       
       if (workbenchTemplate == null)
       {
           workbenchTemplate = selectWorkbenchTemplate(cmdAction, "OnRampTemplateExporting");
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
            heads[h] = colHeads.get(h).getCaption();
        }
        config.setHeaders(heads);
        
        cmdAction.addProperties(config.getProperties());
        
        CommandDispatcher.dispatch(cmdAction);
    }

        
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplateFromFile()
    {
        // For ease of testing
        File file = null;
        FileDialog fileDialog = new FileDialog((Frame)UICacheManager.get(UICacheManager.FRAME), 
                                               getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"), 
                                               FileDialog.LOAD);
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
        
        WorkbenchTemplate workbenchTemplate = null;
        
        if (file.exists())
        {
            ImportDataFileInfo dataFileInfo = new ImportDataFileInfo();
            if (dataFileInfo.load(file))
            {
                int btnPressed = selectExistingTemplate(dataFileInfo.getColInfo(), "OnRampImportData");
                workbenchTemplate = selectedTemplate;
                selectedTemplate  = null;
                if (btnPressed == ChooseFromListDlg.APPLY_BTN)
                {
                    JDialog           dlg = showColumnMapperDlg(dataFileInfo, null, "WB_IMP_TEMPLATE_EDITOR");
                    ColumnMapperPanel cmp = (ColumnMapperPanel)dlg.getContentPane();
                    if (!cmp.isCancelled())
                    {   
                        workbenchTemplate = createTemplate(cmp, file.getAbsolutePath(), FilenameUtils.getBaseName(file.getName()));
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
            else if(dataFileInfo.getConfig().getStatus() != ConfigureExternalDataIFace.Status.Cancel){
                JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
                statusBar.setErrorMessage(String.format(getResourceString("WB_PARSE_FILE_ERROR"), new Object[] { file.getName() }));
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
        Workbench workbench = createNewWorkbenchDataObj(wbt, alwaysAskForName);
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
    protected Workbench createNewWorkbench(final WorkbenchTemplate workbenchTemplate)
    {
        Workbench workbench = null;
        
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            session.attach(workbenchTemplate);
            
            workbench = createNewWorkbenchDataObj(workbenchTemplate, false);
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
    protected void deleteWorkbench(final RecordSet recordSet)
    {
        Workbench workbench = loadWorkbench(recordSet);
        if (workbench != null)
        {
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
                    
                    deleteDnDBtn(workbenchNavBox, nbi);
                    
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
    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbenchTemplate(final RecordSet recordSet)
    {
        WorkbenchTemplate workbenchTemplate = loadWorkbenchTemplate(recordSet);
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
        
        if (false) // Research into JRDataSources 
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setDbTableId(Workbench.getClassTableId());
            rs.addItem(workbench.getWorkbenchId());
            
            session = DataProviderFactory.getInstance().createSession();
            session.attach(workbench);
            WorkbenchJRDataSource dataSrc = new WorkbenchJRDataSource(workbench, workbench.getWorkbenchRowsAsList());
            session.close();
            
            final CommandAction cmd = new CommandAction(LabelsTask.LABELS, LabelsTask.PRINT_LABEL, dataSrc);
            cmd.setProperty("title",  "Labels");
            cmd.setProperty("file",   "basic_label.jrxml");
            cmd.setProperty("params", "title=title;subtitle=subtitle");
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
                
            } else if (data instanceof RecordSet)
            {
                recordSet = (RecordSet)data;
            }
        }
        
        if (recordSet != null && recordSet.getDbTableId() != Workbench.getClassTableId())
        {
            return null;
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
                                                                                getResourceString("WB_CHOOSE_TEMPLATE_TITLE"), 
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
        
        dlg.setHelpContext("OnRampReporting");
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
        
        JDialog dlg = showColumnMapperDlg(null, workbenchTemplate, 
                StringUtils.isNotEmpty(workbenchTemplate.getSrcFilePath()) ? "WB_IMP_TEMPLATE_EDITOR" : "WB_TEMPLATE_EDITOR");
        ColumnMapperPanel cmp = (ColumnMapperPanel)dlg.getContentPane();
        if (!cmp.isCancelled())
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {

                WorkbenchTemplate newWorkbenchTemplate = cmp.createWorkbenchTemplate();
                
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
                    //System.out.print("New ["+wbtmi.getFieldName()+"] ");
                    // find a match
                    boolean foundMatch = false;
                    for (WorkbenchTemplateMappingItem oldWbtmi : oldItems)
                    {
                        //System.out.println("New ["+wbtmi.getFieldName()+"] ["+oldWbtmi.getFieldName()+"] ["+wbtmi.getTableId()+"] ["+oldWbtmi.getTableId()+"]");
                        if (oldWbtmi.getSrcTableId().intValue() == wbtmi.getSrcTableId().intValue() &&
                            oldWbtmi.getFieldName().equals(wbtmi.getFieldName()))
                        {
                            foundMatch = true;
                            oldToNewIndex.put(oldWbtmi.getViewOrder(), newViewOrder);
                            oldWbtmi.setViewOrder(newViewOrder++);
                            break;
                        }
                    }
                    //System.out.println("   ** Found: "+foundMatch);
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
                                row.delete(item);
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
        int               btnPressed        = selectExistingTemplate(null, "OnRampImportImages");
        WorkbenchTemplate workbenchTemplate = selectedTemplate;
        selectedTemplate  = null;
        
        if (btnPressed == ChooseFromListDlg.APPLY_BTN)
        {
            // Create a new Template 
            JDialog           dlg = showColumnMapperDlg(null, null, "WB_TEMPLATE_EDITOR");
            ColumnMapperPanel cmp = (ColumnMapperPanel)dlg.getContentPane();
            if (!cmp.isCancelled())
            {   
                workbenchTemplate = createTemplate(cmp, null, null);
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
                Workbench workbench = createNewWorkbenchDataObj(workbenchTemplate, false);
                
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
                    session.close();    
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
        
        JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
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
     * Looks for the embedded COmmandAction and processes that as the command.
     * @param cmdAction the command that was invoked
     */
    protected void workbenchTemplateSelected(final CommandAction cmdAction)
    {
        Object cmdData = cmdAction.getData();
        if (cmdData != null && cmdData instanceof CommandAction && cmdData != cmdAction)
        {
            CommandAction subCmd = (CommandAction)cmdData;
            if (subCmd.getTableId() == WorkbenchTemplate.getClassTableId())
            {
                subCmd.setProperty("template", cmdAction.getProperty("template"));
                processWorkbenchCommands(subCmd);
                subCmd.getProperties().remove("template");
            }
        } else
        {
            // This is for when the user clicks directly on the workbench
            WorkbenchTemplate workbenchTemplate = loadWorkbenchTemplate((RecordSet)cmdAction.getProperty("template"));
            if (workbenchTemplate != null)
            {
                editTemplate(workbenchTemplate);
            } else
            {
                log.error("workbenchTemplate was null!");
            }
        }
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
            
        } else if (cmdAction.isAction(SELECTED_TEMPLATE))
        {
            workbenchTemplateSelected(cmdAction);
            
        } else if (cmdAction.isAction(NEW_TEMPLATE))
        {
            if (isClickedOn)
            {
                createTemplateFromScratch();
            }
            
        } else if (cmdAction.isAction(IMPORT_DATA_FILE))
        {
            if (isClickedOn)
            {
                createTemplateFromFile();
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
                WorkbenchTemplate workbenchTemplate = selectWorkbenchTemplate(cmdAction, "OnRampNewDataSet");
                if (workbenchTemplate != null)
                {
                    createNewWorkbench(workbenchTemplate);
                }
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
                    
                } else if (rs.getDbTableId() == WorkbenchTemplate.getClassTableId())
                {
                    deleteWorkbenchTemplate(rs);
                }
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
