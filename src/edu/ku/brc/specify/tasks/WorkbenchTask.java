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

import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.lang.ref.SoftReference;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.tasks.subpane.BarChartPane;
import edu.ku.brc.af.tasks.subpane.ChartPane;
import edu.ku.brc.af.tasks.subpane.PieChartPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.forms.MultiView;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.QueryResultsContainerIFace;
import edu.ku.brc.dbsupport.QueryResultsHandlerIFace;
import edu.ku.brc.dbsupport.QueryResultsListener;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.helpers.ImageFilter;
import edu.ku.brc.helpers.SwingWorker;
import edu.ku.brc.helpers.UIFileFilter;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchDataItem;
import edu.ku.brc.specify.datamodel.WorkbenchRow;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.rstools.ExportFileConfigurationFactory;
import edu.ku.brc.specify.rstools.ExportToFile;
import edu.ku.brc.specify.tasks.subpane.wb.ConfigureExternalDataIFace;
import edu.ku.brc.specify.tasks.subpane.wb.DataImportIFace;
import edu.ku.brc.specify.tasks.subpane.wb.ImageFrame;
import edu.ku.brc.specify.tasks.subpane.wb.ImportColumnInfo;
import edu.ku.brc.specify.tasks.subpane.wb.ImportDataFileInfo;
import edu.ku.brc.specify.tasks.subpane.wb.SelectNewOrExistingDlg;
import edu.ku.brc.specify.tasks.subpane.wb.TemplateEditor;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchBackupMgr;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchJRDataSource;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchPaneSS;
import edu.ku.brc.specify.tasks.subpane.wb.WorkbenchValidator;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploaderException;
import edu.ku.brc.specify.tools.schemalocale.SchemaLocalizerXMLHelper;
import edu.ku.brc.specify.ui.ChooseRecordSetDlg;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToggleButtonChooserDlg;
import edu.ku.brc.ui.ToggleButtonChooserPanel;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.SimpleGlassPane;

/**
 * Placeholder for additional work.
 *
 * @code_status Beta
 *
 * @author meg, rods, jstewart
 *
 */
public class WorkbenchTask extends BaseTask
{
	private static final Logger log = Logger.getLogger(WorkbenchTask.class);
    
    public static int              MAX_ROWS              = 7000;
    
    public static final int        GLASSPANE_FONT_SIZE   = 20;

	public static final DataFlavor DATASET_FLAVOR        = new DataFlavor(WorkbenchTask.class, "DataSet");
    public static final String     WORKBENCH             = "Workbench";
    public static final String     WORKBENCHTEMPLATE     = "WorkbenchTemplate";
    
    public static final String     NEW_WORKBENCH         = "WB.NewWorkbench";
    public static final String     IMPORT_DATA_FILE      = "WB.ImportFile";
    public static final String     SELECTED_WORKBENCH    = "WB.SelectedWorkbench";
    public static final String     WB_BARCHART           = "WB.CreateBarChart";
    public static final String     PRINT_REPORT          = "WB.PrintReport";
    public static final String     WB_TOP10_REPORT       = "WB.Top10Report";
    public static final String     WB_IMPORTCARDS        = "WB.ImportCardImages";
    public static final String     WB_IMPORT_IMGINDEX    = "WB.ImportImagesIndex";
    public static final String     EXPORT_DATA_FILE      = "WB.ExportData";
    public static final String     EXPORT_TEMPLATE       = "WB.ExportTemplate";
    public static final String     NEW_WORKBENCH_FROM_TEMPLATE = "WB.NewDataSetFromTemplate";
    public static final String 	   EXPORT_RS_TO_WB	     = "WB.ExportRStoWB";
    
    public static final String     IMAGES_FILE_PATH      = "wb.imagepath";
    public static final String     IMPORT_FILE_PATH      = "wb.importfilepath";
    public static final String     EXPORT_FILE_PATH      = "wb.exportfilepath";

    /**
     * internationalized boolean string representations for validation.
     */
    public static String[]                           boolStrings                  = {
            getResourceString("WB_TRUE"), getResourceString("WB_FALSE"),
            getResourceString("WB_TRUE_ABBR"), getResourceString("WB_FALSE_ABBR"),
            getResourceString("WB_YES"), getResourceString("WB_NO"),
            getResourceString("WB_YES_ABBR"), getResourceString("WB_NO_ABBR"), "1", "0" };

    
    protected static SoftReference<DBTableIdMgr> 	databasechema = null;
    protected static boolean					  	isCustomizedSchema = false; //true if schema and upload_defs are loaded from a user-customized file

    // Data Members
    protected NavBox                      workbenchNavBox;
    protected Vector<ToolBarDropDownBtn>  tbList           = new Vector<ToolBarDropDownBtn>();
    protected Vector<JComponent>          menus            = new Vector<JComponent>();
    
    protected Vector<NavBoxItemIFace>     reportsList      = new Vector<NavBoxItemIFace>();
    protected Vector<NavBoxItemIFace>     enableNavBoxList = new Vector<NavBoxItemIFace>();
        
    // Temporary until we get a Workbench Icon
    protected boolean                     doingStarterPane = false;

    public final DatasetNavBoxMgr datasetNavBoxMgr;
    

    //for batch upload hack
    //protected boolean testingJUNK = false;

	/**
	 * @return the isCustomizedSchema
	 */
	public static boolean isCustomizedSchema() {
		return isCustomizedSchema;
	}

	/**
	 * Constructor. 
	 */
	public WorkbenchTask() 
    {
		super(WORKBENCH, getResourceString(WORKBENCH));
        
		CommandDispatcher.register(WORKBENCH, this);        
        CommandDispatcher.register("Preferences", this);
        
        if (AppContextMgr.isSecurityOn())
        {
        	log.debug("add? " + getPermissions().canAdd() + " modify? " + getPermissions().canModify()
        			+ " delete? " + getPermissions().canDelete() + " view? " + getPermissions().canView());
        }
        else
        {
        	log.debug("security off");
        }
        
        datasetNavBoxMgr = new DatasetNavBoxMgr(this);
        
        if (WorkbenchDataItem.getMaxWBCellLength() == null)
        {
            int max = Math.min(AppPreferences.getLocalPrefs().getInt("MAX_WBCELL_LENGTH", 512), 32767);
            if (UIRegistry.isMobile())
            {
                max = Math.min(max, 512);
            }
            WorkbenchDataItem.setMaxWBCellLength(max);
        }
        
        getDatabaseSchema();
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    @Override
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            int wbTblId    = Workbench.getClassTableId(); 
            
            RolloverCommand roc = null;
            NavBox navBox = new NavBox(getResourceString("Actions"));
            //if (!AppContextMgr.isSecurityOn() || getPermissions().canAdd())
            if (isPermitted())
            {
                makeDnDNavBtn(navBox, getResourceString("WB_IMPORTDATA"), "Import16", getResourceString("WB_IMPORTDATA_TT"), new CommandAction(WORKBENCH, IMPORT_DATA_FILE, wbTblId), null, false, false);// true means make it draggable
                makeDnDNavBtn(navBox, getResourceString("WB_IMPORT_CARDS"),  "ImportImages", getResourceString("WB_IMPORTCARDS_TT"), new CommandAction(WORKBENCH, WB_IMPORTCARDS, wbTblId),   null, false, false);// true means make it draggable
                //makeDnDNavBtn(navBox, getResourceString("WB_IMPORT_IMGINDX"),  "ImportImages", getResourceString("WB_IMPORTIMGINDX_TT"), new CommandAction(WORKBENCH, WB_IMPORT_IMGINDEX, wbTblId),   null, false, false);// true means make it draggable
            
                
                roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_NEW_DATASET"),   "NewDataSet", getResourceString("WB_NEW_DATASET_TT"), new CommandAction(WORKBENCH, NEW_WORKBENCH, wbTblId),     null, false, false);// true means make it draggable
                roc.addDropDataFlavor(DATASET_FLAVOR);
            }

            //if (!AppContextMgr.isSecurityOn() || getPermissions().canModify())
            if (isPermitted())
            {
                roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_DATA"), "Export16", getResourceString("WB_EXPORT_DATA_TT"), new CommandAction(WORKBENCH, EXPORT_DATA_FILE, wbTblId), null, true, false);// true means make it draggable
                roc.addDropDataFlavor(DATASET_FLAVOR);
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_DATA_FILE));
                enableNavBoxList.add((NavBoxItemIFace)roc);
 
                roc = (RolloverCommand)makeDnDNavBtn(navBox, getResourceString("WB_EXPORT_TEMPLATE"), "ExportExcel16", getResourceString("WB_EXPORT_TEMPLATE_TT"),new CommandAction(WORKBENCH, EXPORT_TEMPLATE, wbTblId), null, true, false);// true means make it draggable
                roc.addDropDataFlavor(DATASET_FLAVOR);
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, EXPORT_TEMPLATE));
                enableNavBoxList.add((NavBoxItemIFace)roc);
                
                makeDnDNavBtn(navBox, getResourceString("WB_EXPORTFROMDBTOWB"), "Export16", getResourceString("WB_EXPORTFROMDBTOWB_TT"), new CommandAction(WORKBENCH, EXPORT_RS_TO_WB, wbTblId), null, false, false);// true means make it draggable
            }  
            
            navBoxes.add(navBox);
            
            workbenchNavBox = datasetNavBoxMgr.createWorkbenchNavBox(WORKBENCH);

            
            // Then add
            if (commands != null && (!AppContextMgr.isSecurityOn() || canViewReports()))
            {
                NavBox reportsNavBox = new NavBox(getResourceString("Reports"));
                
                navBoxes.add(reportsNavBox);
                for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/report"))
                {
                    Properties params = ap.getMetaDataMap();
                    String tableid = params.getProperty("tableid");
                    
                    if (StringUtils.isNotEmpty(tableid) && Integer.parseInt(tableid) == Workbench.getClassTableId())
                    {
                        params.put("title", ap.getDescription());
                        params.put("file", ap.getName());
                        //log.debug("["+ap.getDescription()+"]["+ap.getName()+"]");
                        String iconNameStr = params.getProperty("icon");
                        if (StringUtils.isEmpty(iconNameStr))
                        {
                            iconNameStr = name;
                        }                        
                        commands.add(new TaskCommandDef(ap.getDescription(), iconNameStr, params));
                    }

                }
                
                for (TaskCommandDef tcd : commands)
                {
                    // XXX won't be needed when we start validating the XML
                    String tableIdStr = tcd.getParams().getProperty("tableid");
                    if (tableIdStr != null)
                    {
                        CommandAction cmdAction = new CommandAction(WORKBENCH, PRINT_REPORT, Workbench.getClassTableId());
                        cmdAction.addStringProperties(tcd.getParams());
                        cmdAction.getProperties().put("icon", IconManager.getIcon(tcd.getIconName()));
                        
                        NavBoxItemIFace nbi = makeDnDNavBtn(reportsNavBox, tcd.getName(), tcd.getIconName(), cmdAction, null, true, false);// true means make it draggable
                        reportsList.add(nbi);
                        enableNavBoxList.add(nbi);
                        
                        roc = (RolloverCommand)nbi;
                        roc.addDropDataFlavor(DATASET_FLAVOR);
                        roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));
                        roc.setToolTip(getResourceString("WB_PRINTREPORT_TT"));

                    } else
                    {
                        log.error("Interaction Command is missing the table id");
                    }
                }
                
                CommandAction cmdAction = new CommandAction(WORKBENCH, WB_BARCHART, Workbench.getClassTableId());
                cmdAction.getProperties().put("icon", IconManager.getIcon("Bar_Chart", IconManager.STD_ICON_SIZE));
                
                roc = (RolloverCommand)makeDnDNavBtn(reportsNavBox, getResourceString("CHART"), "Bar_Chart", cmdAction, null, true, false);
                enableNavBoxList.add((NavBoxItemIFace)roc);
                roc.addDropDataFlavor(DATASET_FLAVOR);
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));
                roc.setToolTip(getResourceString("WB_BARCHART_TT"));

                cmdAction = new CommandAction(WORKBENCH, WB_TOP10_REPORT, Workbench.getClassTableId());
                cmdAction.getProperties().put("icon", IconManager.getIcon("Pie_Chart", IconManager.STD_ICON_SIZE));

                roc = (RolloverCommand)makeDnDNavBtn(reportsNavBox, getResourceString("WB_TOP10"), "Pie_Chart", cmdAction, null, true, false);
                enableNavBoxList.add((NavBoxItemIFace)roc);// true means make it draggable
                roc.addDropDataFlavor(DATASET_FLAVOR);
                roc.addDragDataFlavor(new DataFlavor(Workbench.class, "Report"));
                roc.setToolTip(getResourceString("WB_TOP10_TT"));
            }
            
            // Add these last and in order
            // TEMPLATES navBoxes.addElement(templateNavBox);
            navBoxes.add(workbenchNavBox);
            
            updateNavBoxUI(workbenchNavBox.getCount());
        }
        //AppPreferences.getRemote().putInt("MAX_ROWS", MAX_ROWS);
        MAX_ROWS = AppPreferences.getRemote().getInt("MAX_ROWS", MAX_ROWS);
        isShowDefault = true;
    }
    
    public NavBox getDatasetNavBox() {
        return workbenchNavBox;
    }
    
    /**
     * @return the max rows that a WorkBench can have
     */
    public static int getMaxRows()
    {
        return MAX_ROWS;
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
            
            //check for custom config files in appdatadir
            File customSchemaFile = new File(UIRegistry.getAppDataDir() + File.separator + "specify_workbench_datamodel.xml");
            File customDefFile = new File(UIRegistry.getAppDataDir() + File.separator + "specify_workbench_upload_def.xml");
            if (customSchemaFile.exists() && !customDefFile.exists()) 
            {
            	log.error("a customized specify_workbench_datamodel.xml was found but not loaded because a customized specify_workbench_upload_def.xml was not found");
            } else if (!customSchemaFile.exists() && customDefFile.exists()) 
            {
            	log.error("a customized specify_workbench_upload_def.xml was found but not loaded because a customized specify_workbench_datamodel.xml was not found");
            }
            
            if (customSchemaFile.exists() && customDefFile.exists())
            {
            	schema.initialize(customSchemaFile);
            	isCustomizedSchema = true;
            }
            else
            {
            	schema.initialize(new File(XMLHelper.getConfigDirPath("specify_workbench_datamodel.xml")));
            }
            databasechema = new SoftReference<DBTableIdMgr>(schema);
            
            SchemaLocalizerXMLHelper schemaLocalizer = new SchemaLocalizerXMLHelper(SpLocaleContainer.WORKBENCH_SCHEMA, schema);
            schemaLocalizer.load(true);
            schemaLocalizer.setTitlesIntoSchema();
            
            DBTableIdMgr mgr = databasechema.get();
            DBTableInfo taxonOnly = mgr.getInfoById(4000);
            if (taxonOnly != null)
            {
                taxonOnly.setTitle(getResourceString("WB_TAXONIMPORT_ONLY"));
                //taxonOnly.setTableId(4);
            }
        }
        
        return schema;
    }

    protected boolean canViewReports()
    {
        Taskable reportsTask = ContextMgr.getTaskByClass(ReportsTask.class);
        return reportsTask != null && reportsTask.getPermissions().canView();
    }
    
    /**
     * Get a Workbench object from ActionCommand or asks for it.
     * @param cmdActionObj the data from the current action command
     * @param helpContext the help context for the dialog that pops up asking for Workbench
     * @return the selected or dropped workbench.
     */
    protected Workbench getWorkbenchFromCmd(final Object cmdActionObj, final String helpContext)
    {
        if (cmdActionObj != null && cmdActionObj instanceof CommandAction)
        {
            CommandAction subCmd = (CommandAction)cmdActionObj;
            if (subCmd.getTableId() == Workbench.getClassTableId())
            {
                Workbench wb = selectWorkbench(subCmd, helpContext);
                if (wb != null)
                {
                    return wb;
                }
                // else
                log.error("No Workbench selected or found.");
            }
        }
        return null;
    }
    
    /**
     * Pops up the editor for the Workbench porperties.
     * @param roc the RolloverCommand that invoked it
     */
    protected void editWorkbenchProps(final RolloverCommand roc)
    {
        if (roc != null)
        {
            Workbench workbench = loadWorkbench((RecordSetIFace)((CommandAction)roc.getData()).getProperty("workbench"));
            if (workbench != null)
            {
                if (fillInWorkbenchNameAndAttrs(workbench, workbench.getName(), true))
                {
                    DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                    try
                    {
        
                        session.beginTransaction();
                        Workbench mergedWB = session.merge(workbench);
                        mergedWB.getWorkbenchTemplate().setName(mergedWB.getName());
                        session.saveOrUpdate(mergedWB);
                        session.commit();
                        session.flush();
                        
                        roc.setLabelText(workbench.getName());
    
                        NavBox.refresh((NavBoxItemIFace)roc);
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                        log.error(ex);
                        
                    } finally
                    {
                        session.close();    
                    }
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
    public static RolloverCommand getNavBtnById(final NavBox navBox, final Integer recordId, final String cmdAttrName)
    {
        if (recordId != null)
        {
            for (NavBoxItemIFace nbi : navBox.getItems())
            {
                RolloverCommand roc = (RolloverCommand)nbi;
                if (roc != null)
                {
                    Object data  = roc.getData();
                    if (data != null)
                    {
                        RecordSetIFace rs = null;
                        if (data instanceof CommandAction)
                        {
                            CommandAction cmd  = (CommandAction)data;
                            Object prop = cmd.getProperty(cmdAttrName);
                            if (prop instanceof RecordSetIFace)
                            {
                                rs  = (RecordSetIFace)prop;
                            }
                        } else if (data instanceof RecordSetIFace)
                        {
                            rs  = (RecordSetIFace)data;
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
        }
        return null;
    }

    /**
     * Finds the Rollover command that represents the item that has the passed in Record id and
     * it is checking the CommandAction's attr by name for the RecordSet
     * @param workbench
     * @return
     */
    protected int getCountOfPanesForTemplate(final Integer templateId)
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
    @Override
    public SubPaneIFace getStarterPane()
    {
        doingStarterPane = true;
        return starterPane = StartUpTask.createFullImageSplashPanel(title, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        String label    = getResourceString(name);
        String hint     = getResourceString("workbench_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        toolbarItems.add(new ToolBarItemDesc(btn));
        return toolbarItems;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getTaskClass()
     */
    @Override
    public Class<? extends BaseTask> getTaskClass()
    {
        return this.getClass();
    }
    
    /**
     * Ask the user for information needed to fill in the data object.
     * @param dataObj the data object
     * @return true if OK, false if cancelled
     */
    public static boolean askUserForInfo(final String viewSetName, 
                                         final String dlgTitle,
                                         final Workbench workbench,
                                         final boolean isEdit)
    {
        //XXX isEdit = true does not prevent editing in editorDlg.
        ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog(
                (Frame)UIRegistry.getTopWindow(),
                "Global",
                viewSetName,
                null,
                dlgTitle,
                getResourceString("OK"),
                null, // className,
                null, // idFieldName,
                isEdit, 
                MultiView.HIDE_SAVE_BTN);
        
        editorDlg.preCreateUI();
        editorDlg.setData(workbench);
        editorDlg.getMultiView().preValidate();
        editorDlg.setModal(true);
        editorDlg.setVisible(true);

        if (!editorDlg.isCancelled())
        {
            editorDlg.getMultiView().getDataFromUI();
        }
        editorDlg.dispose();
        
        return !editorDlg.isCancelled();
    }
    
    /**
     * CREates and displays the ColumnMapper Dialog (Template Editor), either a DatFileInfo or a Template is passed in, for both can be null.
     * @param dataFileInfo the imported file info
     * @param template an existing template
     * @return the dlg after cancel or ok
     */
    protected TemplateEditor showColumnMapperDlg(final ImportDataFileInfo dataFileInfo, 
                                                 final WorkbenchTemplate  template,
                                                 final String             titleKey) throws Exception
    {
        
    	TemplateEditor  mapper;
        if (template != null)
        {
            mapper = new TemplateEditor((Frame)UIRegistry.get(UIRegistry.FRAME), getResourceString(titleKey), template);
            //if (AppContextMgr.isSecurityOn() && !getPermissions().canAdd())
            if (!isPermitted())
            {
                //XXX OK to require add permission to modify props and structure?
                mapper.setReadOnly(true);
            }
        } else
        {
            mapper = new TemplateEditor((Frame)UIRegistry.get(UIRegistry.FRAME), getResourceString(titleKey), dataFileInfo);
            // When creating a mapping from scratch we need to expand the dialog to 
            // make sure it is wide enough for the icon in the bottom list
            if (dataFileInfo == null)
            {
                Dimension size = mapper.getSize();
                // this is an arbitrary size, intended to make 
                // it wide enough to also show the icon on the right
                size.width  += 120;
                mapper.setSize(size);
            }
        }
        if (!templateIsEditable(template))
        {
        	UIRegistry.showLocalizedMsg("WorkbenchTask.ExportedDatasetTemplateNotEditable");
        	mapper.setReadOnly(true);
        }
        UIHelper.centerAndShow(mapper);
        return mapper;
    }
    
    /**
     * @param template
     * @return true if the template's mappings can be edited.
     */
    protected boolean templateIsEditable(final WorkbenchTemplate template)
    {
    	//the templates for workbenches that have been filled with records from the database are not editable
		//seems like it might be better to indicate in the template that it was designed for export but for
    	//now need to check the workbench
    	if (template != null)
    	{
    		for (Workbench wb : template.getWorkbenches())
    		{
    			//currently we maintain a 1-1 between wb and wb template.
    			if (wb.getExportedFromTableName() != null)
    			{
    				int rowCount = BasicSQLUtils.getCountAsInt("select count(workbenchrowid) from workbenchrow where workbenchid = " + wb.getId());
    				if (rowCount > 0)
    				{
    					return false;
    				}
    			}
    		}
    	}
    	return true;    	
    }
    /**
     * Creates a new WorkBenchTemplate from the Column Headers and the Data in a file.
     * @return the new WorkbenchTemplate
     */
    protected WorkbenchTemplate createTemplate(final TemplateEditor mapper, final String filePath)
    {
        WorkbenchTemplate workbenchTemplate = null;
        try
        {
            workbenchTemplate = new WorkbenchTemplate();
            workbenchTemplate.initialize();
            
            workbenchTemplate.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
            
            Set<WorkbenchTemplateMappingItem> items = workbenchTemplate.getWorkbenchTemplateMappingItems();
            Collection<WorkbenchTemplateMappingItem> newItems     = mapper.updateAndGetNewItems();
            for (WorkbenchTemplateMappingItem item : newItems)
            {
                log.debug(item.getFieldName()+" "+item.getViewOrder()+"  "+item.getOrigImportColumnIndex());
            }
            for (WorkbenchTemplateMappingItem wbtmi : newItems)
            {
                wbtmi.setWorkbenchTemplate(workbenchTemplate);
                items.add(wbtmi);
                //log.debug("new ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
            }
            workbenchTemplate.setSrcFilePath(filePath);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            ex.printStackTrace();
        }
        return workbenchTemplate;
    }
    
   /**
 * @param wbItem column from existing template
 * @param fileItem column form import file
 * @return true if ImportedColumName of Field name of wbItem equal ColumnName of fileItem
 */
protected boolean colsMatchByName(final WorkbenchTemplateMappingItem wbItem,
                                final ImportColumnInfo fileItem)
    {
        boolean result = false;
        if (wbItem != null && fileItem != null)
        {
            if (StringUtils.isNotEmpty(wbItem.getImportedColName())
                    && StringUtils.isNotEmpty(fileItem.getColName()))
            {
                result = wbItem.getImportedColName().equalsIgnoreCase(fileItem.getColName());
            }
            if (!result && StringUtils.isNotEmpty(wbItem.getFieldName()))
            {
                result = wbItem.getFieldName().equalsIgnoreCase(fileItem.getColName());
            }
        }
        return result;
    }
   
	/**
	 * @param cols
	 * @param colName
	 * @return index of column with name or title equal to colName
	 */
	protected int indexOfName(Vector<?> cols, String colName)
	{
		int c = 0;
		for (Object col : cols)
		{
			if (col.toString().equalsIgnoreCase(colName))
			{
				return c;
			}
			c++;
		}
		return -1;
	}
	
    /**
     * If the colInfo Vector is null then all the templates are added to the list to be displayed.<br>
     * If not, then it checks all the column in the file against the columns in each Template to see if there is a match
     * and then uses that.
     * show a Dialog and returns null if there are not templates or none match.
     * @param colInfo the column info
     * @param helpContext the help context
     * 
     * @return a List. The first element in the pair is false then the selection was cancelled. 
     * Otherwise, the second element will be the selected WorkbenchTemplate or null if a new template should be created,
     * and the third element will be a list of columns that are not used in the selected template
     */
    public List<?> selectExistingTemplate(final Vector<ImportColumnInfo> colInfo, final String helpContext)
    {
        WorkbenchTemplate selection = null;
        
        if (colInfo != null)
        {
            Collections.sort(colInfo);
        }
        
        Vector<WorkbenchTemplate> matchingTemplates = new Vector<WorkbenchTemplate>();
        HashMap<WorkbenchTemplate, Vector<?>> unMappedCols = new HashMap<WorkbenchTemplate, Vector<?>>();
        
        // Check for any matches with existing templates
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            //List<?> list = session.getDataList("From WorkbenchTemplate where SpecifyUserID = " + AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId());
            List<?> list = session.getDataList("From WorkbenchTemplate");
            for (Object obj : list)
            {
                WorkbenchTemplate template = (WorkbenchTemplate)obj;
                if (colInfo == null)
                {
                    template.forceLoad();
                	matchingTemplates.add(template);
                    
                //} else if (colInfo.size() <= template.getWorkbenchTemplateMappingItems().size())
                } else if (colInfo.size() == template.getWorkbenchTemplateMappingItems().size())
                {
                    boolean match = true;
                    Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(template.getWorkbenchTemplateMappingItems());
                    Vector<ImportColumnInfo> mapped = new Vector<ImportColumnInfo>();
                    for (ImportColumnInfo col : colInfo)
                    {
                        int idx = indexOfName(items, col.getColName());
                    	if (idx != -1)
                        {
                        	mapped.add(col);
                        	items.get(idx).setViewOrder(Short.valueOf(col.getColInx().toString()));
                        	items.get(idx).setOrigImportColumnIndex(Short.valueOf(col.getColInx().toString()));
                        } else
                        {
                        	match = false;
                        	break;
                        }
                    }
                    if (match)
                    {
                        Vector<WorkbenchTemplateMappingItem> unmapped = new Vector<WorkbenchTemplateMappingItem>();
                        for (WorkbenchTemplateMappingItem item : items)
                        {
                        	if (indexOfName(mapped, item.getImportedColName()) == -1)
                        	{
                        		unmapped.add(item);
                        		//item.setViewOrder(c++);
                        	}
                        }
                        if (unmapped.size() == 0)
                        {
                        	matchingTemplates.insertElementAt(template, 0); //put full matches at head of list
                        } else
                        {
                        	matchingTemplates.add(template);
                        }
                        unMappedCols.put(template, unmapped);
                        //for (WorkbenchTemplateMappingItem unmappedItem : unmapped)
                        //{
                        //	template.getWorkbenchTemplateMappingItems().remove(unmappedItem);
                        //}
                    }
                }
//                else if (colInfo.size() > template.getWorkbenchTemplateMappingItems().size())
//                {
//                    boolean match = true;
//                    Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(template.getWorkbenchTemplateMappingItems());
//                    Vector<ImportColumnInfo> mapped = new Vector<ImportColumnInfo>();
//                    for (WorkbenchTemplateMappingItem item : items)
//                    {
//                    	int idx = indexOfName(colInfo, item.getImportedColName());
//                    	if (idx != -1)
//                    	{
//                    		mapped.add(colInfo.get(idx));
//                    		item.setViewOrder(Short.valueOf(colInfo.get(idx).getColInx().toString()));
//                    		item.setOrigImportColumnIndex(Short.valueOf(colInfo.get(idx).getColInx().toString()));
//                    	}
//                    }
//                    for (int i=0; i<items.size(); i++)
//                    {
//                        WorkbenchTemplateMappingItem wbItem = items.get(i);
//                        int origIdx = wbItem.getOrigImportColumnIndex().intValue();
//                        if (origIdx == -1)
//                        {
//                        	//try the viewOrder
//                        	origIdx = wbItem.getViewOrder().intValue();
//                        }
//                        ImportColumnInfo fileItem = origIdx > -1 && origIdx < colInfo.size() ? colInfo.get(origIdx) : null;
//                        // Check to see if there is an exact match by name
//                        if (colsMatchByName(wbItem, fileItem))
//                        {
//                        	//might do additional type checking
//                        	mapped.add(fileItem);
//                        }
//                        else
//                        {
//                            //log.error("["+wbItem.getImportedColName()+"]["+fileItem.getColName()+"]");
//                            match = false;
//                            break;
//                        }
//                    }
//                    // All columns match with their order etc.
//                    if (match)
//                    {
//                        matchingTemplates.add(template);
//                        Vector<ImportColumnInfo> unmapped = new Vector<ImportColumnInfo>();
//                        for (ImportColumnInfo fileItem : colInfo)
//                        {
//                        	if (mapped.indexOf(fileItem) == -1)
//                        	{
//                        		unmapped.add(fileItem);
//                        	}
//                        }
//                        unMappedCols.put(template, unmapped);
//                    }
//                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            log.error(ex);
            ex.printStackTrace();

            
        } finally 
        {
            session.close();
        }
        
        Vector<Object> result = new Vector<Object>();
        // Ask the user to choose an existing template.
        if (matchingTemplates.size() > 0)
        {
            SelectNewOrExistingDlg<WorkbenchTemplate> dlg = new SelectNewOrExistingDlg<WorkbenchTemplate>((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    "WB_CHOOSE_DATASET_REUSE_TITLE", 
                    "WB_CREATE_NEW_MAPPING",
                    "WB_USE_EXISTING_MAPPING",
                    helpContext, 
                    matchingTemplates);
            
            UIHelper.centerAndShow(dlg);
            
            if (dlg.getBtnPressed() == ChooseFromListDlg.OK_BTN)
            {
                if (!dlg.isCreateNew())
                {
                	selection = dlg.getSelectedObject();
                	Vector<?> unmapped = unMappedCols.get(selection);
                	if (unmapped != null && unmapped.size() > 0)
                	{
                		StringBuilder flds = new StringBuilder();
                		for (Object info : unmapped) //if there are a lot of these the message will be ugly
                		{
                			if (flds.length() != 0)
                			{
                				flds.append(", ");
                			}
                			flds.append(info.toString());
                		}
                		String msg = unmapped.get(0) instanceof ImportColumnInfo ?
                				String.format(UIRegistry.getResourceString("WB_UNMAPPED_NOT_IMPORTED"), flds.toString()) :
                				String.format(UIRegistry.getResourceString("WB_UNUSED_NOT_INCLUDED"), flds.toString());
                		if (!UIRegistry.displayConfirm(UIRegistry.getResourceString("WB_INCOMPLETE_MAP_TITLE"), 
                				msg, 
                				UIRegistry.getResourceString("YES"), UIRegistry.getResourceString("NO"), 
                				JOptionPane.WARNING_MESSAGE))
                		{
                			result.add(true);
                			return result; // means create a new one
                		}
                	}
                	
//                    for (WorkbenchTemplateMappingItem mi : selection.getWorkbenchTemplateMappingItems())
//                    {
//                    	System.out.println(mi.getImportedColName() + " - " + mi.getViewOrder());
//                    }
                    result.add(true);
                    result.add(selection);
                    result.add(unMappedCols.get(selection));
                    return result; // means reuse an existing one
                }
                result.add(true);
                return result; // means create a new one
            }
            result.add(false);
            return result; //cancelled
        }
        result.add(true);
        return result; // means create a new one
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
    public static boolean getExportInfo(final Properties props, final String defaultFileName)
    {
        String extension = "";
        //String fileTypeCaption = "";
        if (true)
        {
            for (ExportFileConfigurationFactory.ExportableType type : ExportFileConfigurationFactory.getExportList())
            {
                if (type.getMimeType() == ExportFileConfigurationFactory.XLS_MIME_TYPE)
                {
                    props.setProperty("mimetype", type.getMimeType());
                    extension = type.getExtension();
                    //fileTypeCaption = type.getCaption();
                    break;
                }
            }
            
        } else
        {
            ChooseFromListDlg<ExportFileConfigurationFactory.ExportableType> dlg = 
                new ChooseFromListDlg<ExportFileConfigurationFactory.ExportableType>((Frame) UIRegistry.get(UIRegistry.FRAME), 
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
            dlg.dispose();
        }
        
//        FileDialog fileDialog = new FileDialog((Frame) UIRegistry.get(UIRegistry.FRAME),
//                                               String.format(getResourceString("CHOOSE_WORKBENCH_EXPORT_FILE"), fileTypeCaption), FileDialog.SAVE);
//        fileDialog.setDirectory(getDefaultDirPath(EXPORT_FILE_PATH));
//        UIHelper.centerAndShow(fileDialog);
//        fileDialog.dispose();

        JFileChooser chooser = new JFileChooser(getDefaultDirPath(EXPORT_FILE_PATH));
        chooser.setDialogTitle(getResourceString("CHOOSE_WORKBENCH_EXPORT_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setFileFilter(new UIFileFilter("xls", getResourceString("WB_EXCELFILES")));
        if (defaultFileName != null)
        {
        	chooser.setSelectedFile(new File(chooser.getCurrentDirectory().getPath() + File.separator + defaultFileName + ".xls"));
        }
        
        if (chooser.showSaveDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION)
        {
            UIRegistry.getStatusBar().setText("");
            return false;
        }

        File file = chooser.getSelectedFile();
        if (file == null)
        {
            UIRegistry.getStatusBar().setText(getResourceString("WB_EXPORT_NOFILENAME"));
            return false;
        }
        
        String path = chooser.getCurrentDirectory().getPath();
        //String path = FilenameUtils.getPath(file.getPath());
        if (StringUtils.isNotEmpty(path))
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.put(EXPORT_FILE_PATH, path);
        }
        
//        String fileName = fileDialog.getFile();
        String fileName = file.getName();	
//        if (StringUtils.isEmpty(fileName))
//        {
//            UIRegistry.getStatusBar().setText(getResourceString("WB_EXPORT_NOFILENAME"));
//            return false;
//        }
        
        if (StringUtils.isEmpty(FilenameUtils.getExtension(fileName)))
        {
            fileName += (fileName.endsWith(".") ? "" : ".") + extension;
        }

        if (StringUtils.isEmpty(fileName)) 
        { 
            return false;
        }
        
        if (file.exists())
        {
            PanelBuilder    builder = new PanelBuilder(new FormLayout("p:g", "c:p:g"));
            CellConstraints cc      = new CellConstraints();

            String msg = String.format("<html><p>%s<br><br>%s<br></p></html>", getResourceString("WB_FILE_EXISTS"), getResourceString("WB_OK_TO_OVERWRITE"));
            builder.add(createLabel(msg), cc.xy(1,1)); 
            builder.setDefaultDialogBorder();
            
            CustomDialog confirmer = new CustomDialog((Frame)UIRegistry.get(UIRegistry.FRAME), 
                    getResourceString("WB_FILE_EXISTS_TITLE"), true, CustomDialog.OKCANCEL, builder.getPanel(), CustomDialog.CANCEL_BTN);
            UIHelper.centerAndShow(confirmer);
            confirmer.dispose();
            if (confirmer.isCancelled())
            {
                return false;
            }
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
       Workbench workbench = null;
       Object    data      = cmdAction.getData();
       if (data instanceof CommandAction)
       {
           CommandAction subCmd = (CommandAction)data;
           if (subCmd != cmdAction)
           {
               if (subCmd.getTableId() == cmdAction.getTableId())
               {
                   workbench = selectWorkbench(subCmd, "WorkbenchExportDataSet"); // XXX ADD HELP
               }
           }
       }
       
       // The command may have been clicked on so ask for one
       if (workbench == null)
       {
           workbench = selectWorkbench(cmdAction, "WorkbenchExportDataSet"); // XXX ADD HELP
       }

       if (workbench != null)
       {
           CommandAction command = new CommandAction(PluginsTask.PLUGINS, PluginsTask.EXPORT_LIST);
           command.setProperty("tool", ExportToFile.class);
           DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
           try
           {
               session.attach(workbench);
               workbench.forceLoad();
               List<?> rowData = workbench.getWorkbenchRowsAsList();
               List<Object> exportData = new Vector<Object>(rowData.size() + 1);
               exportData.add(workbench.getWorkbenchTemplate());
               exportData.addAll(rowData);
               command.setData(exportData);

               Properties props = new Properties();

               if (!getExportInfo(props, workbench.getName()))
               {
                   return;
               }

               session.close();
               session = null;
               
               workbench.forceLoad();
               
               sendExportCommand(props, workbench.getWorkbenchTemplate().getWorkbenchTemplateMappingItems(), command);
           } catch (Exception ex)
           {
               edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
               edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
                    workbenchTemplate = selectWorkbenchTemplate(subCmd, "WB_EXPORT_TEMPLATE", null, "WorkbenchExportExcelTemplate"); // XXX ADD HELP
                    
                } else
                {
                    return;
                }
            }
        }
        
        if (workbenchTemplate == null)
        {
            workbenchTemplate = selectWorkbenchTemplate(cmdAction, "WB_EXPORT_TEMPLATE", null, "WorkbenchExportExcelTemplate"); // XXX ADD HELP
        }

        // The command may have been clicked on so ask for one
        if (workbenchTemplate != null)
        {
            CommandAction command = new CommandAction(PluginsTask.PLUGINS, PluginsTask.EXPORT_LIST);
            command.setProperty("tool", ExportToFile.class);
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(workbenchTemplate);
                workbenchTemplate.checkMappings(getDatabaseSchema());
                Vector<WorkbenchTemplate> newDataRow = new Vector<WorkbenchTemplate>(1);
                newDataRow.add(workbenchTemplate);
                command.setData(newDataRow);

                // rest of this method is copied from WorkbenchPaneSS.doExcelCsvExport()
                // eventually most of the work will probably be done by Meg's Fancy Configurer UI

                Properties props = new Properties();

                if (!getExportInfo(props, null))
                {
                    return;
                }

                session.close();
                session = null;
                
                sendExportCommand(props, workbenchTemplate.getWorkbenchTemplateMappingItems(), command);

            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
     * Uploads a Workbench to specify database.
     * @param cmdAction the incoming command request
     */
/*    protected void uploadWorkbench(final CommandAction cmdAction)
    {
        System.out.println("UPLOAD?");
        // Check incoming command for a RecordSet contain the Workbench
        Workbench workbench = null;
        Object data = cmdAction.getData();
        if (data instanceof CommandAction)
        {
            CommandAction subCmd = (CommandAction) data;
            if (subCmd != cmdAction)
            {
                if (subCmd.getTableId() == cmdAction.getTableId())
                {
                    workbench = selectWorkbench(subCmd, "WorkbenchExportDataSet"); // XXX ADD HELP
                }
            }
        }

        // The command may have been clicked on so ask for one
        if (workbench == null)
        {
            workbench = selectWorkbench(cmdAction, "WorkbenchUpload"); // XXX ADD HELP
        }

        if (workbench != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                session.attach(workbench);
                workbench.forceLoad();
            }
            finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
            doWorkbenchUpload(workbench);
        }

    }
*/    
/*    protected void doWorkbenchUpload(final Workbench workbench)
    {        
        Uploader imp;
        WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(workbench
                .getWorkbenchTemplate());
        try
        {
            Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
            DB db = new DB();
            System.out.println("constructing importer...");
            imp = new Uploader(db, new UploadData(maps, workbench.getWorkbenchRowsAsList()), null);
            imp.prepareToUpload();
            if (!imp.validateStructure()) { throw new UploaderException(
                    "Invalid dataset structure", UploaderException.ABORT_IMPORT); // i18n
            }
            imp.getDefaultsForMissingRequirements();
        }
        catch (DirectedGraphException ex)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            UIRegistry.clearGlassPaneMsg();
            UIRegistry.getStatusBar().setErrorMessage(ex.getMessage());
        }
        catch (UploaderException ex)
        {
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            UIRegistry.clearGlassPaneMsg();
            UIRegistry.getStatusBar().setErrorMessage(ex.getMessage());
        }
        
    }
*/    
    /**
     * Creates a new one row Workbench from another workbench's template.
     * 
     * @param cmdAction the incoming command request
     */
    protected void createWorkbenchFromTemplate(final CommandAction cmdAction)
    {
        
        Workbench wb = selectWorkbench(cmdAction, "WB_CHOOSE_DATASET_REUSE_TITLE", null, "WorkbenchEditMapping", false); // XXX ADD HELP
        if (wb != null)
        {
            WorkbenchTemplate workbenchTemplate = wb.getWorkbenchTemplate();
            if (workbenchTemplate == null)
            {
                workbenchTemplate = selectWorkbenchTemplate(cmdAction, "WB_CHOOSE_DATASET_REUSE_TITLE", null, "WorkbenchEditMapping"); // XXX ADD HELP
            }
    
           // The command may have been clicked on so ask for one
           if (workbenchTemplate != null)
           {
               DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
               try
               {
                   workbenchTemplate = cloneWorkbenchTemplate(workbenchTemplate);
               
                   if (workbenchTemplate != null)
                   {
                       Workbench workbench = createNewWorkbenchDataObj(null, workbenchTemplate);
                       if (workbench != null)
                       {
                           workbench.setWorkbenchTemplate(workbenchTemplate);
                           workbenchTemplate.addWorkbenches(workbench);
                           fillandSaveWorkbench(null, workbench);
                       }
                   }
               } catch (Exception ex)
               {
                   edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                   edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
   }
   
    /**
     * Configures and send the Export command action.
     * @param props the properties for the configuration
     * @param items the items to be used for the headers
     * @param cmdAction the command action to send
     */
    public static void sendExportCommand(final Properties                        props, 
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
            /*fieldNames were being used to allow auto-mapping if the exported wb was re-imported (I think).
             *But since mappings are exported for xls exports, and auto-mapping seems to work just as
             *well, now using getCaption() instead of getFieldName()
            */
            heads[h] = colHeads.get(h).getCaption();
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
//        FileDialog fileDialog = new FileDialog(/*(Frame)UIRegistry.get(UIRegistry.FRAME),*/
//        										(Frame )UIRegistry.getTopWindow(),
//                                               getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"), 
//                                               FileDialog.LOAD);
//        fileDialog.setDirectory(getDefaultDirPath(IMPORT_FILE_PATH));
//        fileDialog.setFilenameFilter(new java.io.FilenameFilter()
//        {
//            public boolean accept(File dir, String filename)
//            {
//                for (ExportFileConfigurationFactory.ExportableType exportType : ExportFileConfigurationFactory.getExportList())
//                {
//                    String ext = FilenameUtils.getExtension(filename);
//                    if (StringUtils.isNotEmpty(ext) && exportType.getExtension().toLowerCase().equals(ext))
//                    {
//                        return true;
//                    }
//                }
//                return false;
//            }
//
//        });
//        UIHelper.centerAndShow(fileDialog);
//        fileDialog.dispose();

        JFileChooser chooser = new JFileChooser(getDefaultDirPath(IMPORT_FILE_PATH));
        chooser.setDialogTitle(getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        String[] exts = {"xls", "csv"};
        chooser.setFileFilter(new UIFileFilter(exts, getResourceString("WB_EXCELANDCSVFILES")));
        String currDirPath = AppPreferences.getLocalPrefs().get(IMPORT_FILE_PATH, null);
        if (currDirPath != null)
        {
        	File currDir = new File(currDirPath);
        	if (currDir.isDirectory() && currDir.exists())
        	{
        		chooser.setCurrentDirectory(currDir);
        	}
        }
        
        if (chooser.showOpenDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION)
        {
            UIRegistry.getStatusBar().setText("");
            return null;
        }

        
//        String fileName = fileDialog.getFile();
//        String path     = fileDialog.getDirectory();
//        if (StringUtils.isNotEmpty(path))
//        {
//            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
//            localPrefs.put(IMPORT_FILE_PATH, path);
//        }
//
//        if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(path))
//        {
//            file = new File(path + File.separator + fileName);
//        } else
//        {
//            return null;
//        }
        
        file = chooser.getSelectedFile();
        
        if (file.exists())
        {
        	if (StringUtils.isNotEmpty(file.getPath()))
        	{
        		AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        		localPrefs.put(IMPORT_FILE_PATH, file.getParent());
        	}
  
        	
            ImportDataFileInfo dataFileInfo = new ImportDataFileInfo();
            if (dataFileInfo.load(file))
            {
                Workbench workbench =  createNewWorkbench(dataFileInfo, file);
                
                // This means correct usage count for ImportXLS will actually be getUsageCount(ImportXLS) - getUsageCount(ImportCSV)...
                //if (dataFileInfo.getConfig().getProperties().getProperty("mimetype","").equals(ExportFileConfigurationFactory.CSV_MIME_TYPE))
                //{
                //    UsageTracker.incrUsageCount("WB.ImportCSV");
                //}
                return workbench;
                
            } else if (dataFileInfo.getConfig() == null || dataFileInfo.getConfig().getStatus() != ConfigureExternalDataIFace.Status.Cancel)
            {
                JStatusBar statusBar = UIRegistry.getStatusBar();
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
        String wbName  = inputFile != null ? FilenameUtils.getBaseName(inputFile.getName()) : null;
        List<?> selection = selectExistingTemplate(inputFile != null ? dataFileInfo.getColInfo() : null, 
                inputFile != null ? "WorkbenchImportData" : "WorkbenchNewDataSet");
        
        if (selection.size() == 0 || !(Boolean )selection.get(0))
        {
        	return null;  //cancelled
        }
        
        WorkbenchTemplate workbenchTemplate = selection.size() > 1 ? (WorkbenchTemplate )selection.get(1) : null;        
        
        if (workbenchTemplate == null)
        {
            TemplateEditor dlg = null;
            try 
            {
            	dlg = showColumnMapperDlg(dataFileInfo, null, "WB_MAPPING_EDITOR");
            } catch (Exception ex)
            {	                    
            	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            	log.error(ex);
            }
            if (dlg != null && !dlg.isCancelled())
            {   
                workbenchTemplate = createTemplate(dlg, inputFile != null ? inputFile.getAbsolutePath() : "");
            }
            if (dlg != null)
            {
            	dlg.dispose();
            }
        } else 
        {
            //workbenchTemplate = cloneWorkbenchTemplate(workbenchTemplate);
        	try
        	{
        		workbenchTemplate = (WorkbenchTemplate)workbenchTemplate.clone();
//                for (WorkbenchTemplateMappingItem mi : workbenchTemplate.getWorkbenchTemplateMappingItems())
//                {
//                	System.out.println(mi.getImportedColName() + " - " + mi.getViewOrder());
//                }

        	} catch (CloneNotSupportedException ex)
        	{
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                log.error(ex);
                workbenchTemplate = null;
        	}
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
        workbench.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        
        if (StringUtils.isNotEmpty(wbName))
        {
            workbench.setName(wbName);
        }
        
        if (workbenchTemplate != null)
        {
            workbenchTemplate.setSpecifyUser(workbench.getSpecifyUser());
            workbench.setWorkbenchTemplate(workbenchTemplate);
            workbenchTemplate.getWorkbenches().add(workbench);
            
            if (fillInWorkbenchNameAndAttrs(workbench, wbName, false))
            {
                workbenchTemplate.setName(workbench.getName());
                return workbench;
            }
        }

        return null;
    }
    
    /**
     * @param workbench
     * @param altName
     * @return
     */
    protected boolean fillInWorkbenchNameAndAttrs(final Workbench workbench, final String wbName, final boolean skipFirstCheck)
    {
        boolean skip = skipFirstCheck;
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        
        try
        {
            String newWorkbenchName = wbName;
            
            boolean   alwaysAsk   = true;
            Workbench foundWB     = null;
            boolean   shouldCheck = false;
            //boolean canEdit = !AppContextMgr.isSecurityOn() || getPermissions().canAdd(); //XXX OK to require Add permission to modify props and structure?
            boolean canEdit = isPermitted();
            do
            {
                if (StringUtils.isEmpty(newWorkbenchName))
                {
                    alwaysAsk = true;
                    
                } else
                {
                    foundWB = session.getData(Workbench.class, "name", newWorkbenchName, DataProviderSessionIFace.CompareType.Equals);
                    if (foundWB != null && !skip)
                    {
                        UIRegistry.getStatusBar().setErrorMessage(String.format(getResourceString("WB_DATASET_EXISTS"), new Object[] { newWorkbenchName}));
                        workbench.setName("");
                    }
                    skip = false;
                }
                
                String oldName = workbench.getName();
                if ((foundWB != null || (StringUtils.isNotEmpty(newWorkbenchName) && newWorkbenchName.length() > 64)) || alwaysAsk)
                {
                    alwaysAsk = false;
                    
                    // We found the same name and it must be unique
                    if (askUserForInfo("Workbench", getResourceString("WB_DATASET_INFO"), workbench, canEdit) && canEdit)
                    {
                        newWorkbenchName = workbench.getName();
                        // This Part here needfs to be moved into an <enablerule/>
                        if (StringUtils.isNotEmpty(newWorkbenchName) && newWorkbenchName.length() > 64)
                        {
                            UIRegistry.getStatusBar().setErrorMessage(getResourceString("WB_NAME_TOO_LONG"));
                        }
                        foundWB = workbench;
                    } else
                    {
                        UIRegistry.getStatusBar().setText("");
                        return false;
                    }
                    
                }
                
                shouldCheck = oldName == null || !oldName.equals(newWorkbenchName);
                
            } while (shouldCheck);
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            log.error(ex);
            
        } finally
        {
            session.close();    
        }
        UIRegistry.getStatusBar().setText("");
        return true;
    }
    
    /**
     * @param rs
     * @param wb
     * @return
     */
    protected boolean loadRsIntoWb(final RecordSetIFace rs, final Workbench wb)
    {
    	boolean result = false;
    	DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(rs.getDbTableId());
        //XXX What is the dbTableId field in workbench for? ExportedFromTableName may not even be necessary. Based on use of dbTableId in recordset
    	//it looks like it was designed to for exported records...
    	//wb.setDbTableId(rs.getTableId());
    	wb.setExportedFromTableName(tbl.getClassName());
    	DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
    	Class<?> cls = rs.getDataClassFormItems();
    	try
    	{
         	try
        	{
        		WorkbenchValidator wbv = new WorkbenchValidator(wb);
        		for (RecordSetItemIFace item : rs.getItems())
        		{
        			DataModelObjBase obj = (DataModelObjBase )session.get(cls, item.getRecordId());
        			if (obj != null)
        			{
        				obj.forceLoad();
        				wbv.getUploader().loadRecordToWb(obj, wb);
        			}
        		}
        	} catch (Exception ex)
        	{
        		if (ex instanceof WorkbenchValidator.WorkbenchValidatorException || ex instanceof UploaderException)
        		{
        			WorkbenchValidator.WorkbenchValidatorException wvEx = null;
        			if (ex instanceof WorkbenchValidator.WorkbenchValidatorException)
        			{
        				wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex;
        			} else if (ex.getCause() instanceof WorkbenchValidator.WorkbenchValidatorException)
        			{
        				wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex.getCause();
        			}
        			if (wvEx != null && wvEx.getStructureErrors().size() > 0)
        			{
        				Uploader.showStructureErrors(wvEx.getStructureErrors());
        			}
        		}
        		else {
        			throw ex;
        		}
        		UIRegistry.showLocalizedError("WorkbenchPaneSS.UnableToAutoValidate");
        	}
    		result = true;
    	} catch (Exception ex)
    	{
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            ex.printStackTrace();
            log.error(ex);
            
    	} finally
    	{
    		session.close();
    	}
    	return result;
    }
 
//    protected boolean loadRsIntoWb(final RecordSetIFace rs, final Workbench wb)
//    {
//    	boolean result = false;
//    	DBTableInfo tbl = DBTableIdMgr.getInstance().getInfoById(rs.getDbTableId());
//        //XXX What is the dbTableId field in workbench for? ExportedFromTableName may not even be necessary. Based on use of dbTableId in recordset
//    	//it looks like it was designed to for exported records...
//    	//wb.setDbTableId(rs.getTableId());
//    	wb.setExportedFromTableName(tbl.getClassName());
//    	final DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//    	final Class<?> cls = rs.getDataClassFormItems();
//    	try
//    	{
//    		WorkbenchValidator wbvalidator = null;
//    		try
//        	{
//        		wbvalidator = new WorkbenchValidator(wb);
//        	}
//        	catch (Exception ex)
//            {
//            	if (ex instanceof WorkbenchValidator.WorkbenchValidatorException)
//            	{
//            		WorkbenchValidator.WorkbenchValidatorException wvEx = null;
//            		if (ex instanceof WorkbenchValidator.WorkbenchValidatorException)
//            		{
//            			wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex;
//            		} else if (ex.getCause() instanceof WorkbenchValidator.WorkbenchValidatorException)
//            		{
//            			wvEx = (WorkbenchValidator.WorkbenchValidatorException )ex.getCause();
//            		}
//            		if (wvEx != null && wvEx.getStructureErrors().size() > 0)
//            		{
//            			Uploader.showStructureErrors(wvEx.getStructureErrors());
//            		}
//            	}
//            	else 
//            	{
//            		throw ex;
//            	}
//            	UIRegistry.showLocalizedError("WorkbenchPaneSS.UnableToAutoValidate");
//            	return false;
//            }  		
//        	final WorkbenchValidator wbv = wbvalidator;
//        	UIRegistry.writeGlassPaneMsg(getResourceString("WB_LOADING_RS_TO_DB"), GLASSPANE_FONT_SIZE);
//        	new javax.swing.SwingWorker<Object, Object> () {
//        		
//        		Exception killer = null;
//        		
//				/* (non-Javadoc)
//				 * @see javax.swing.SwingWorker#doInBackground()
//				 */
//				@Override
//				protected Object doInBackground() throws Exception {
//	        		try
//	        		{
//					for (RecordSetItemIFace item : rs.getItems())
//	        		{
//	        			DataModelObjBase obj = (DataModelObjBase )session.get(cls, item.getRecordId());
//	        			if (obj != null)
//	        			{
//	        				obj.forceLoad();
//	        				wbv.getUploader().loadRecordToWb(obj, wb);
//	        			}
//	        		}
//	        		} catch (Exception e)
//	        		{
//	        			killer = e;
//	        			return null;
//	        		}
//	        		return null;
//				}
//
//				/* (non-Javadoc)
//				 * @see javax.swing.SwingWorker#done()
//				 */
//				@Override
//				protected void done() {
//					UIRegistry.clearGlassPaneMsg();
//					if (killer != null)
//					{
//			            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//			            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, killer);
//			            killer.printStackTrace();
//			            log.error(killer);
//					}
//				}
//         		
//         	}.execute();
//    	} catch (Exception ex)
//    	{
//            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//            ex.printStackTrace();
//            log.error(ex);
//            
//    	} finally
//    	{
//    		session.close();
//    	}
//    	return result;
//    }

    /**
     * XXX FIX ME
     * @param contents the ImportDataFileInfo Object that contains all the information about the file
     * @param workbench the Workbench
     * @return the new Workbench data object
     */
    protected void fillandSaveWorkbench(final Object contents, 
                                        final Workbench workbench)
    {
        if (workbench != null)
        {
            String msg = contents instanceof ImportDataFileInfo 
            	? String.format(getResourceString("WB_IMPORTING_DATASET"), workbench.getName())
            	: String.format(getResourceString("WB_LOADING_RS_TO_DB"), workbench.getName());		
        	UIRegistry.writeGlassPaneMsg(msg, GLASSPANE_FONT_SIZE);
            
            final SwingWorker worker = new SwingWorker()
            {
                @SuppressWarnings("synthetic-access")
                @Override
                public Object construct()
                {
                    if (contents == null)
                    {
                        workbench.addRow();
                        
                    } else if (contents instanceof ImportDataFileInfo)
                    {
                    	if (((ImportDataFileInfo )contents).loadData(workbench) == DataImportIFace.Status.Error)
                        {
                            return null;
                        }
                         
                     } else if (contents instanceof RecordSetIFace)
                     {
                    	 if (!loadRsIntoWb((RecordSetIFace )contents, workbench))
                    	 {
                    		 return null;
                    	 }
                     }
                     
                     DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                     
                     try
                     {
                         session.beginTransaction();
                         session.save(workbench);
                         session.commit();
                         session.flush();
                         
                         datasetNavBoxMgr.addWorkbench(workbench);
                         
                         updateNavBoxUI(null);
                         
                     } catch (Exception ex)
                     {
                         edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                         edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                         ex.printStackTrace();
                         UIRegistry.clearGlassPaneMsg();
                         
                     } finally
                     {
                         session.close();
                     }
                     
                    return null;
                }

                //Runs on the event-dispatching thread.
                @Override
                public void finished()
                {
                    UIRegistry.clearGlassPaneMsg();
                    
                    createEditorForWorkbench(workbench, null, false, true);
                }
            };
            worker.start();
            
        } else
        {
            UIRegistry.getStatusBar().setText("");
        }
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
//    protected void createEditorForWorkbench(final Workbench workbench, 
//                                            final DataProviderSessionIFace session,
//                                            final boolean showImageView)
//    {
//        if (workbench != null)
//        {
//            final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(String.format(getResourceString("WB_LOADING_DATASET"), new Object[] {workbench.getName()}), GLASSPANE_FONT_SIZE);
//            
//            // Make sure we have a session but use an existing one if it is passed in
//            DataProviderSessionIFace tmpSession = session;
//            if (tmpSession == null)
//            {
//                tmpSession = DataProviderFactory.getInstance().createSession();
//            }
//            
//            
//            final WorkbenchTask            thisTask    = this;
//            final DataProviderSessionIFace finiSession = tmpSession;
//            final SwingWorker worker = new SwingWorker()
//            {
//                WorkbenchPaneSS wbSS = null;
//            	@SuppressWarnings("synthetic-access")
//                @Override
//                public Object construct()
//                {
//                     try
//                     {
//                         if (session == null)
//                         {
//                             finiSession.attach(workbench);
//                         }
//                         wbSS = createEditorForWorkbench(workbench, glassPane, showImageView, thisTask);
//                         if (wbSS.isDoIncremental())
//                         {
//                        	 SwingUtilities.invokeLater(new Runnable() {
//
//								@Override
//								public void run() {
//									UIRegistry.clearSimpleGlassPaneMsg();
//									wbSS.validateAll(null);
//								}
//                        		 
//                        	 });
//                         }
//                     } catch (Exception ex)
//                     {
//                         edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                         edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//                         log.error(ex);
//                         ex.printStackTrace();
//                     } 
//                     finally
//                     {
//                         if (session == null && finiSession != null)
//                         {
//                             try
//                             {
//                                 finiSession.close();
//                                 
//                             } catch (Exception ex)
//                             {
//                                 edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                                 edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//                                 log.error(ex);
//                             }
//                         }
//                         updateNavBoxUI(null);
//                     }
//
//                    return null;
//                }
//
//                //Runs on the event-dispatching thread.
//                @Override
//                public void finished()
//                {
//                    if (wbSS == null || !wbSS.isDoIncremental())
//                    {
//                    	UIRegistry.clearSimpleGlassPaneMsg();
//                    }
//                    //UIRegistry.getStatusBar().setProgressDone(workbench.getName());
//                }
//            };
//            worker.start();
//        }
//    }   
//    
// 
//    /**
//     * @param workbench
//     * @param glassPane
//     * @param showImageView
//     * @param thisTask
//     * @return workbenchPaneSS containing workbench
//     * 
//     * workbench must be attached to a session.
//     */
//    protected WorkbenchPaneSS createEditorForWorkbench(final Workbench workbench, 
//    		//final DataProviderSessionIFace session,
//    		final SimpleGlassPane glassPane, final boolean showImageView, final WorkbenchTask  thisTask)
//    {
//        final int rowCount = workbench.getWorkbenchRows().size() + 1;
//        
//        //force load the workbench here instead of calling workbench.forceLoad() because
//        //is so time-consuming and needs progress bar.
//        //workbench.getWorkbenchTemplate().forceLoad();
//        workbench.getWorkbenchTemplate().checkMappings(getDatabaseSchema());
//        //UIRegistry.getStatusBar().incrementValue(workbench.getName());
//        int count = 1;
//        // Adjust paint increment for number of rows in DataSet
//        int mod;
//        if (rowCount < 50) mod = 1;
//        else if (rowCount < 100) mod = 10;
//        else if (rowCount < 500) mod = 20;
//        else  if (rowCount < 1000) mod = 40;
//        else mod = 50;
//        for (WorkbenchRow row : workbench.getWorkbenchRows())
//        {
//            row.forceLoad();
//            
//            if (glassPane != null)
//            {
//            	if (count % mod == 0)
//            	{
//            		glassPane.setProgress((int)( (100.0 * count) / rowCount));
//            	}
//            }
//            count++;
//        }
//        if (glassPane != null)
//        {
//        	glassPane.setProgress(100);
//        }
//        
//        // do the conversion code right here!
//        boolean convertedAnImage = false;
//        Set<WorkbenchRow> rows = workbench.getWorkbenchRows();
//        if (rows != null)
//        {
//            for (WorkbenchRow row: rows)
//            {
//                // move any single images over to the wb row image table
//                Set<WorkbenchRowImage> rowImages = row.getWorkbenchRowImages();
//                if (rowImages == null)
//                {
//                    rowImages = new HashSet<WorkbenchRowImage>();
//                    row.setWorkbenchRowImages(rowImages);
//                }
//                if (row.getCardImageFullPath() != null && row.getCardImageData() != null && row.getCardImageData().length > 0)
//                {
//                    // create the WorkbenchRowImage record
//                    WorkbenchRowImage rowImage = new WorkbenchRowImage();
//                    rowImage.initialize();
//                    rowImage.setCardImageData(row.getCardImageData());
//                    rowImage.setCardImageFullPath(row.getCardImageFullPath());
//                    rowImage.setImageOrder(0);
//                    
//                    // clear the fields holding the single-image data
//                    row.setCardImageData(null);
//                    row.setCardImageFullPath(null);
//
//                    // connect the image and the row
//                    rowImage.setWorkbenchRow(row);
//                    rowImages.add(rowImage);
//                    
//                    convertedAnImage = true;
//                }
//            }
//        }
//        
//        WorkbenchPaneSS workbenchPane = new WorkbenchPaneSS(workbench.getName(), thisTask, workbench, showImageView, 
//                !isPermitted());
//        addSubPaneToMgr(workbenchPane);
//        
//        if (convertedAnImage)
//        {
//            Component topFrame = UIRegistry.getTopWindow();
//            String message     = getResourceString("WB_DATASET_IMAGE_CONVERSION_NOTIFICATION");
//            String msgTitle    = getResourceString("WB_DATASET_IMAGE_CONVERSION_NOTIFICATION_TITLE");
//            JOptionPane.showMessageDialog(topFrame, message, msgTitle, JOptionPane.INFORMATION_MESSAGE);
//            workbenchPane.setChanged(true);
//        }
//
//        RolloverCommand roc = getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
//        if (roc != null)
//        {
//            roc.setEnabled(false);
//            
//        } else
//        {
//            log.error("Couldn't find RolloverCommand for WorkbenchId ["+workbench.getWorkbenchId()+"]");
//        }
//        
//        return workbenchPane;
//    }
    /**
     * Creates the Pane for editing a Workbench.
     * @param workbench the workbench to be edited
     * @param session a session to use to load the workbench (can be null)
     * @param showImageView shows image window when first showing the window
     */
    protected void createEditorForWorkbench(final Workbench workbench, 
                                            final DataProviderSessionIFace session,
                                            final boolean showImageView,
                                            final boolean doInbackground)
    {
    	//Hack for IN-HOUSE ONLY batch uploading
//    	if (testingJUNK)
//    	{
//    		Vector<Integer> wbIds = new Vector<Integer>();
//    		for (int i = 1; i <= 3; i++)
//    		{
//    			wbIds.add(i);
//    		}
//    		
//    		uploadWorkbenches(wbIds);
//    	} 
//    	else 
//    	{
        if (workbench == null) return;

        final SimpleGlassPane glassPane = doInbackground ? 
        		UIRegistry.writeSimpleGlassPaneMsg(String.format(getResourceString("WB_LOADING_DATASET"), new Object[] {workbench.getName()}), GLASSPANE_FONT_SIZE) :
        		null;
        

        WorkbenchEditorCreator wbec = new WorkbenchEditorCreator(workbench,
                session, showImageView, this, !isPermitted())
        {
            @Override
            public void progressUpdated(java.util.List<Integer> chunks) 
            {
                if (glassPane != null)
                    glassPane.setProgress(chunks.get(chunks.size() - 1));
            }
            
            @Override
            public void completed(WorkbenchPaneSS workbenchPane)
            {
                if (workbenchPane != null)
                {
                	addSubPaneToMgr(workbenchPane);
                
                	if (glassPane != null)
                	{
                		UIRegistry.clearSimpleGlassPaneMsg();
                	}
                
                	if (workbenchPane != null && workbenchPane.isDoIncremental())
                	{
                		workbenchPane.validateAll(null);
                	}
                } //else something went wrong during the creation. Assume/hope execptions or warnings have already occurred. Better than hanging.
                {
                	if (glassPane != null)
                	{
                		UIRegistry.clearSimpleGlassPaneMsg();
                	}
                }
            }
        };
        
        if (doInbackground)
            wbec.runInBackground();
        else
            wbec.runInForeground();
    }
    
    /**
     * Tells the task theat a Workbench Pane is being opened.
     * @param pane the pane being closed.
     */
    public void opening(WorkbenchPaneSS pane)
    {
        Workbench workbench = pane.getWorkbench();
        RolloverCommand roc = getNavBtnById(workbenchNavBox, workbench.getWorkbenchId(), "workbench");
        if (roc != null)
        {
            roc.setEnabled(false);
            
        } else
        {
            WorkbenchTask.log.error("Couldn't find RolloverCommand for WorkbenchId ["+workbench.getWorkbenchId()+"]");
        }

        updateNavBoxUI(null);        
    }
    
    /**
     * Tells the task theat a Workbench Pane is being closed.
     * @param pane the pane being closed.
     */
    public void closing(final SubPaneIFace pane)
    {
        if (pane != null)
        {
            Workbench workbench = ((WorkbenchPaneSS)pane).getWorkbench();
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
                updateNavBoxUI(null);
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
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                session.close();
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                log.error(ex);
            }
        }
        
        return null;
    }
    
    /**
     * Deletes a workbench.
     * @param workbench the workbench to be deleted
     */
    protected void deleteWorkbench(final RecordSetIFace recordSet)
    {
        final Workbench workbench = loadWorkbench(recordSet);
        if (workbench == null)
        {
            return;
        }
        
        if (!UIRegistry.displayConfirm(getResourceString("WB_DELET_DS_TITLE"), 
                                       String.format(getResourceString("WB_DELET_DS_MSG"), new Object[] { workbench.getName() } ), 
                                       getResourceString("Delete"),
                                       getResourceString("CANCEL"), 
                                       JOptionPane.QUESTION_MESSAGE))
        {
            return;
        }
        
        UIRegistry.writeSimpleGlassPaneMsg(String.format(getResourceString("WB_DELETING_DATASET"), new Object[] {workbench.getName()}), GLASSPANE_FONT_SIZE);
        final WorkbenchTask thisTask = this;
        
        final SwingWorker worker = new SwingWorker()
        {
            String backupName;
            
            @SuppressWarnings("synthetic-access")
            @Override
            public Object construct()
            {
                
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                session.attach(workbench);
                UIRegistry.getStatusBar().setProgressRange(workbench.getName(), 0, workbench.getWorkbenchRows().size() + 3);
                UIRegistry.getStatusBar().setIndeterminate(workbench.getName(), false);
                //force load the workbench here instead of calling workbench.forceLoad() because
                //is so time-consuming and needs progress bar.
                workbench.getWorkbenchTemplate().checkMappings(getDatabaseSchema());
                UIRegistry.getStatusBar().incrementValue(workbench.getName());
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        UIRegistry.getStatusBar().setText(String.format(UIRegistry.getResourceString("WB_PREPARING_DELETE"), workbench.getName()));
                    }
                });
                for (WorkbenchRow row : workbench.getWorkbenchRows())
                {
                    row.forceLoad();
                    UIRegistry.getStatusBar().incrementValue(workbench.getName());
                }
                
                backupName = WorkbenchBackupMgr.backupWorkbench(workbench);
                UIRegistry.getStatusBar().incrementValue(workbench.getName());
                SwingUtilities.invokeLater(new Runnable() {
                    public void run()
                    {
                        UIRegistry.getStatusBar().setText(String.format(UIRegistry.getResourceString("WB_DELETING"), workbench.getName()));
                    }
                });

                try
                {
//                	Connection conn = DBConnection.getInstance().createConnection();
//                	try
//        			{
//        				conn.setAutoCommit(false);
//        				Statement stmt = conn.createStatement();
//        				try
//        				{
//        					stmt.execute("delete from workbenchdataitem where workbenchtemplatemappingitemid in (select workbenchtemplatemappingitemid from workbenchtemplatemappingitem where workbenchtemplateid = " 
//        							+ workbench.getWorkbenchTemplate().getId() + ")");
//        					stmt.execute("delete from workbenchtemplatemappingitem where workbenchtemplateid = " + workbench.getWorkbenchTemplate().getId()); //Assuming 1-1 between workbenchtemplate and workbench
//        					stmt.execute("delete from workbenchrowexportedrelationship where workbenchrowid in (select workbenchrowid from workbenchrow where workbenchid = " + workbench.getId() + ")");
//        					stmt.execute("delete from workbenchrowimage where workbenchrowid in(select workbenchrowid from workbenchrow where workbenchid = " + workbench.getId() + ")");
//        					stmt.execute("delete from workbenchrow where workbenchid = " + workbench.getId());
//        					stmt.execute("delete from workbench where workbenchid = " + workbench.getId());
//        					stmt.execute("delete from workbenchtemplate where workbenchtemplateid = " + workbench.getWorkbenchTemplate().getId()); //Assuming 1-1 between workbenchtemplate and workbench
//        					conn.commit();
//        				} finally
//        				{
//        					stmt.close();
//        					conn.close();
//        				}
//        			} catch (Exception ex)
//        			{
//        				conn.rollback();
//        				UsageTracker.incrHandledUsageCount();
//        				edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//        				//UIRegistry.getStatusBar().setErrorMessage(ex.getLocalizedMessage(), ex);
//        			}

                    session.beginTransaction();
                    session.delete(workbench);
              
                    session.commit();
                    session.flush();
                    
                    UIRegistry.getStatusBar().incrementValue(workbench.getName());
                    datasetNavBoxMgr.removeWorkbench(workbench);
                    updateNavBoxUI(null);
                    ((SGRTask)ContextMgr.getTaskByClass(SGRTask.class)).deleteResultsForWorkbench(workbench);
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                    ex.printStackTrace();
                    
                } finally 
                {
                    try
                    {
                        session.close();
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                        log.error(ex);
                    }
                }
                log.info("Deleted a Workbench ["+workbench.getName()+"]");
                    
                return null;
            }

            //Runs on the event-dispatching thread.
            @Override
            public void finished()
            {
                UIRegistry.clearSimpleGlassPaneMsg();
               if (StringUtils.isNotEmpty(backupName))
                {
                    UIRegistry.getStatusBar().setText(String.format(getResourceString("WB_DEL_BACKED_UP"), new Object[] { workbench.getName(), backupName }));
                }
               AppPreferences.getLocalPrefs().remove(WorkbenchPaneSS.wbAutoValidatePrefName + "." + workbench.getId());
               AppPreferences.getLocalPrefs().remove(WorkbenchPaneSS.wbAutoMatchPrefName + "." + workbench.getId());
               
            }
        };
        worker.start();

    }
    
    /**
     * Ask the user for information needed to fill in the data object.
     * @param dataObj the data object
     * @return true if OK, false if cancelled
     */
    public static boolean askUserForReportProps()
    {
        ViewBasedDisplayDialog editorDlg = new ViewBasedDisplayDialog(
                (Frame)UIRegistry.getTopWindow(),
                "Global",
                "ReportProperties",
                null,
                getResourceString("WB_BASIC_LABEL_PROPERTIES"),
                getResourceString("OK"),
                null, // className,
                null, // idFieldName,
                true, // isEdit,
                MultiView.HIDE_SAVE_BTN);
        editorDlg.preCreateUI();
        editorDlg.setData(AppPreferences.getLocalPrefs());
        editorDlg.getMultiView().preValidate();
        editorDlg.setModal(true);
        editorDlg.setHelpContext("WB_LABEL_PROPS");
        editorDlg.setVisible(true);

        if (!editorDlg.isCancelled())
        {
            editorDlg.getMultiView().getDataFromUI();
        }
        editorDlg.dispose();
        
        return !editorDlg.isCancelled();
    }

    /**
     * Creates a report.
     * @param cmdAction the command that initiated it
     */
    protected void doReport(final CommandAction cmdAction)
    {
        Workbench workbench = selectWorkbench(cmdAction, "WorkbenchReporting"); // XXX ADD HELP
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
        if (StringUtils.isNotEmpty(actionStr) && !actionStr.equals("PrintWBItems")) 
        {
            boolean isBasicLabel = actionStr.equals("PrintBasicLabel");
        	boolean go = false;
        	if (isBasicLabel)
            {
        		go = askUserForReportProps();
            }
        	else
        	{
        		//XXX general prop getting stuff will be handled in ReportTask???
        		go = true;        		
        	}
        	
        	if (go)
            {
                RecordSet rs = new RecordSet();
                rs.initialize();
                rs.setDbTableId(Workbench.getClassTableId());
                rs.addItem(workbench.getWorkbenchId());

                session = DataProviderFactory.getInstance().createSession();
                session.attach(workbench);

                workbench.forceLoad();
                WorkbenchJRDataSource dataSrc = new WorkbenchJRDataSource(workbench, workbench.getWorkbenchRowsAsList(), !isBasicLabel);
                session.close();

                final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS, ReportsBaseTask.PRINT_REPORT, dataSrc);
                
                if (isBasicLabel)
                {
                	cmd.setProperty("title", "Labels");
                	cmd.setProperty("file", "basic_label.jrxml");
                	cmd.setProperty("skip-parameter-prompt", "true");
                	// params hard-coded for harvard demo:
                	cmd.setProperty("params", "title="
                        + AppPreferences.getLocalPrefs().get("reportProperties.title", "")
                        + ";subtitle="
                        + AppPreferences.getLocalPrefs().get("reportProperties.subTitle", "")
                        + ";footer="
                        + AppPreferences.getLocalPrefs().get("reportProperties.footer", ""));
                    cmd.setProperty("icon", IconManager.getIcon("Labels16"));
                }
                else
                {
                	//XXX icon and file props??? 
                }
                cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        CommandDispatcher.dispatch(cmd);
                    }
                });
            }
            return;
        }
        
        WorkbenchTemplateMappingItem selectMappingItem = selectMappingItem(items);
        
        if (selectMappingItem != null)
        {
            RecordSet rs = new RecordSet();
            rs.initialize();
            rs.setDbTableId(Workbench.getClassTableId());
            rs.addItem(workbench.getWorkbenchId());
            
            final CommandAction cmd = new CommandAction(ReportsBaseTask.REPORTS, ReportsBaseTask.PRINT_REPORT, rs);
            cmd.setProperty("title",  selectMappingItem.getCaption());
            cmd.setProperty("file",   "wb_items.jrxml");
            cmd.setProperty("params", "colnum="+selectMappingItem.getWorkbenchTemplateMappingItemId()+";"+"title="+selectMappingItem.getCaption());
            cmd.setProperty(NavBoxAction.ORGINATING_TASK, this);
            ImageIcon cmdIcon = (ImageIcon)cmdAction.getProperty("icon");
            if (cmdIcon != null)
            {
                cmd.getProperties().put("icon", cmdIcon);
            }
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
     * @param helpContext the help context
     * @return a workbench object or null
     */
    public static Workbench selectWorkbench(final CommandAction cmdAction,
                                        final String helpContext)
    {
        return selectWorkbench(cmdAction, "WB_CHOOSE_DATASET", null, helpContext, false);
    }
    
    /**
     * Returns the Workbench referenced in the CommandAction or asks for one instead.
     * @param cmdAction the CommandAction being executed
     * @param titleKey resource key for dialog title
     * @param labelKey resource key for the label on top of the list (null hides the label)
     * @param helpContext the help context
     * @param showAll show all the workbenches whether they ar ein use or not
     * @return a workbench object or null
     */
    @SuppressWarnings("unchecked")
    public static Workbench selectWorkbench(final CommandAction cmdAction, 
                                        final String titleKey,
                                        final String labelKey,
                                        final String helpContext,
                                        final boolean showAll)
    {
        Workbench workbench = null;

        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getProperty("workbench");
            if (recordSet == null)
            {
                Object data = cmdAction.getData();
                if (data instanceof CommandAction)
                {
                    recordSet = (RecordSetIFace)((CommandAction)data).getProperty("workbench");
                    
                } else if (data instanceof RecordSetIFace)
                {
                    recordSet = (RecordSetIFace)data;
                }
            }
            
            if (recordSet != null && recordSet.getDbTableId() != Workbench.getClassTableId())
            {
                UIRegistry.getStatusBar().setText("");
                return null;
            }
            
            if (recordSet == null)
            {
                List<Workbench> list = (List<Workbench>)session.getDataList("From Workbench where SpecifyUserID = "+AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getSpecifyUserId());
                if (list.size() == 0)
                {
                    // XXX Probably should have a dialog here.
                    UIRegistry.getStatusBar().setText("");
                    return null;
                    
                } else if (list.size() == 1)
                {
                    list.get(0).getWorkbenchTemplate().checkMappings(getDatabaseSchema());
                    return list.get(0);
                }
                
                if (!showAll)
                {
                    for (SubPaneIFace sbi : SubPaneMgr.getInstance().getSubPanes())
                    {
                        if (sbi instanceof WorkbenchPaneSS)
                        {
                            WorkbenchPaneSS wbp = (WorkbenchPaneSS)sbi;
                            for (Workbench wb : list)
                            {
                                if (wb.getWorkbenchId().intValue() == wbp.getWorkbench().getWorkbenchId().intValue())
                                {
                                    list.remove(wb);
                                    break;
                                }
                            }
                        }
                    }
                }
                
                if (list.size() == 0)
                {
                    log.error("All workbenches are open.");
                    return null;
                    
                }
                
                Collections.sort(list);
                
                session.close();
                session = null;
                
                ChooseFromListDlg<Workbench> dlg = new ChooseFromListDlg<Workbench>((Frame)UIRegistry.get(UIRegistry.FRAME),
                            StringUtils.isNotEmpty(titleKey) ? getResourceString(titleKey) : null, 
                            StringUtils.isNotEmpty(labelKey) ? getResourceString(labelKey) : null, 
                            ChooseFromListDlg.OKCANCELHELP, 
                            list, 
                            helpContext);
                
                dlg.setModal(true);
                UIHelper.centerAndShow(dlg);
                if (!dlg.isCancelled())
                {
                    session   = DataProviderFactory.getInstance().createSession();
                    workbench = dlg.getSelectedObject();
                    session.attach(workbench);
                    workbench.getWorkbenchTemplate().forceLoad();
                    
                } else
                {
                    UIRegistry.getStatusBar().setText("");
                    return null;
                }
            } else
            {
                workbench = session.get(Workbench.class, recordSet.getOrderedItems().iterator().next().getRecordId());
                workbench.getWorkbenchTemplate().forceLoad();
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                    log.error(ex);
                }
            }
        }
        return workbench;
    }

    /**
     * Returns the Workbench referenced in the CommandAction or asks for one instead.
     * @param cmdAction the CommandAction being executed
     * @param titleKey resource key for dialog title
     * @param labelKey resource key for the label on top of the list (null hides the label)
     * @param helpContext the help context
     * @return a workbench template object or null
     */
    protected WorkbenchTemplate selectWorkbenchTemplate(final CommandAction cmdAction, 
                                                        final String titleKey,
                                                        final String labelKey,
                                                        final String helpContext)
    {
        Workbench workbench = selectWorkbench(cmdAction,
                                              titleKey,
                                              labelKey,
                                              helpContext,
                                              true); // false means it's for templates
        if (workbench != null)
        {
            return workbench.getWorkbenchTemplate();
        }
        return null;
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
                (Frame)UIRegistry.get(UIRegistry.FRAME),
                "WB_SELECT_FIELD_TITLE", 
                "WB_SELECT_FIELD", 
                items, 
                CustomDialog.OKCANCELHELP,
                ToggleButtonChooserPanel.Type.RadioButton);
        
        dlg.setUseScrollPane(true);
        dlg.setHelpContext("WorkbenchReporting");
        dlg.setModal(true);
        dlg.setUseScrollPane(true);
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
        Workbench workbench = selectWorkbench(cmdAction, "WorkbenchChart"); // XXX ADD HELP
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
                        List<?> list = hibSession.createQuery(hql).list();
                        
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
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
                                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                                log.error(ex);
                            }
                        }
                    }
                }
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                log.error(ex);
            }
            
            QueryResultsHandlerIFace qrhi = new QueryResultsHandlerIFace()
            {
                public void init(final QueryResultsListener listener, final java.util.List<QueryResultsContainerIFace> list)
                {
                    //nothing to do.
                }
                public void init(final QueryResultsListener listener, final QueryResultsContainerIFace qrc)
                {
                    //nothing to do.
                }
                public void startUp()
                {
                    //nothing to do
                }
                public void cleanUp()
                {
                    //nothing to do
                }
    
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
            chart.allResultsBack(null);
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
            count   = session.getDataCount(Workbench.class, "specifyUser", AppContextMgr.getInstance().getClassObject(SpecifyUser.class), DataProviderSessionIFace.CompareType.Equals);
            session.close();            
        }
        
        boolean enabled = count != null && count.intValue() > 0 && !areAllOpen();
        for (NavBoxItemIFace nbi : enableNavBoxList)
        {
            nbi.setEnabled(enabled);
        }
    }
    
    protected boolean areAllOpen()
    {
        for (NavBoxItemIFace nbi : workbenchNavBox.getItems())
        {
            if (((RolloverCommand)nbi).isEnabled())
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Show the dialog to allow the user to edit a template and then updates the data rows and columns..
     * @param workbenchTemplate the template to be edited
     */
    protected void editTemplate(final WorkbenchTemplate wbTemplate)
    {
        loadTemplateFromData(wbTemplate);
        TemplateEditor dlg = null;
        wbTemplate.checkMappings(getDatabaseSchema());
        try 
        {
        	dlg = showColumnMapperDlg(null, wbTemplate, "WB_MAPPING_EDITOR");
        } catch (Exception ex)
        {	                    
        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
        	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
        	log.error(ex);
        }
        if (dlg != null && !dlg.isCancelled())
        {
        	updateGeoRefInfoAfterTemplateEdit(wbTemplate,
        			dlg.getDeletedItems(), dlg.updateAndGetNewItems());
//            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
//            try
//            {
//                Collection<WorkbenchTemplateMappingItem> deletedItems = dlg.getDeletedItems();
//                Collection<WorkbenchTemplateMappingItem> newItems     = dlg.updateAndGetNewItems();
//                
//                for (WorkbenchTemplateMappingItem item : newItems)
//                {
//                    log.error(item.getFieldName());
//                }
//                //Collection<WorkbenchTemplateMappingItem> updatedItems = dlg.getUpdatedItems();
//                
//                session.beginTransaction();
//                
//                // Merge with current session
//                WorkbenchTemplate workbenchTemplate = session.merge(wbTemplate);
//                
//                Set<WorkbenchTemplateMappingItem> items = workbenchTemplate.getWorkbenchTemplateMappingItems();
//                for (WorkbenchTemplateMappingItem delItem : deletedItems)
//                {
//                    for (WorkbenchTemplateMappingItem wbtmi : items)
//                    {
//                    	if (delItem.getWorkbenchTemplateMappingItemId().longValue() == wbtmi.getWorkbenchTemplateMappingItemId().longValue())
//                        {
//                            //log.debug("del ["+wbtmi.getCaption()+"]["+wbtmi.getWorkbenchTemplateMappingItemId().longValue()+"]");
//                            //wbtmi.setWorkbenchTemplate(null);
//                    		
//                    		items.remove(wbtmi);
//                    		wbtmi.setWorkbenchTemplate(null);
//                            if (wbtmi.getWorkbenchDataItems() != null)
//                            {
//                              
//                            	for (WorkbenchDataItem wbdi : wbtmi.getWorkbenchDataItems())
//                                {
//                                    wbdi.getWorkbenchRow().getWorkbenchDataItems().remove(wbdi);
//                                    wbdi.setWorkbenchRow(null);
//                            		session.delete(wbdi);
//                            		wbdi.setWorkbenchTemplateMappingItem(null);
//                                }
//                            	wbtmi.getWorkbenchDataItems().clear();
//                            }
//                            session.delete(wbtmi);
//                            break;
//                        }
//                    }
//                }
//                
//                for (WorkbenchTemplateMappingItem wbtmi : newItems)
//                {
//                    wbtmi.setWorkbenchTemplate(workbenchTemplate);
//                    items.add(wbtmi);
//                    //log.debug("new ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
//                    session.saveOrUpdate(wbtmi) ;
//                }
//                                
//                //Check to see if geo/ref data needs to be updated
//                //This is actually only necessary if lat/long mappings have been switched - lat mapping changed to a long mapping or vice-versa.
//                //XXX Surely it is possible to tell if a lat/long switch has been made and not do this after every template change??
//                WorkbenchTemplateMappingItem aGeoRefMapping = null;
//                for (WorkbenchTemplateMappingItem wbtmi : workbenchTemplate.getWorkbenchTemplateMappingItems())
//                {
//                    if (aGeoRefMapping == null && wbtmi.getTableName().equals("locality"))
//                    {
//                        if (wbtmi.getFieldName().equalsIgnoreCase("latitude1") || wbtmi.getFieldName().equalsIgnoreCase("latitude2")
//                                || wbtmi.getFieldName().equalsIgnoreCase("longitude1") || wbtmi.getFieldName().equalsIgnoreCase("longitude2"))
//                        {
//                        	aGeoRefMapping = wbtmi;
//                        	break;
//                        }
//                    }
//                }
//                if (aGeoRefMapping != null)
//                {
//                	for (Workbench wb : workbenchTemplate.getWorkbenches())
//                	{
//                		wb.forceLoad();
//                		for (WorkbenchRow wbRow : wb.getWorkbenchRows())
//                		{
//                			wbRow.updateGeoRefTextFldsIfNecessary(aGeoRefMapping);
//                		}
//                		//session.saveOrUpdate(wb);
//                	}
//                }
//
//                session.saveOrUpdate(workbenchTemplate);
//                for (Workbench wb : workbenchTemplate.getWorkbenches())
//                {
//                	session.saveOrUpdate(wb);
//                }
//                
//                session.commit();
//                session.flush();
//                
//                UIRegistry.getStatusBar().setText(getResourceString("WB_SAVED_MAPPINGS"));
//                
//            } catch (Exception ex)
//            {
//                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//                log.error(ex);
//                ex.printStackTrace();
//                
//            } finally
//            {
//                try
//                {
//                    session.close();
//                } catch (Exception ex)
//                {
//                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
//                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
//                    log.error(ex);
//                }  
//            }
        }
        if (dlg != null)
        {
        	dlg.dispose();
        }
    }
    
    /**
     * @param wbTemplate
     * @param deletedItems
     * @param newItems
     */
    protected void updateGeoRefInfoAfterTemplateEdit(final WorkbenchTemplate wbTemplate,
                                                     final Collection<WorkbenchTemplateMappingItem> deletedItems,
                                                     final Collection<WorkbenchTemplateMappingItem> newItems)
    {
    	final SimpleGlassPane glassPane = UIRegistry.writeSimpleGlassPaneMsg(getResourceString("WB_SAVING_TEMPLATE_CHANGES"), GLASSPANE_FONT_SIZE);
    	javax.swing.SwingWorker<Object, Object> sw = new javax.swing.SwingWorker<Object, Object>()
    	{

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Object doInBackground() throws Exception 
			{
	            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
	            try
	            {
	                //Collection<WorkbenchTemplateMappingItem> deletedItems = dlg.getDeletedItems();
	                //Collection<WorkbenchTemplateMappingItem> newItems     = dlg.updateAndGetNewItems();
	                
	                for (WorkbenchTemplateMappingItem item : newItems)
	                {
	                    log.error(item.getFieldName());
	                }
	                //Collection<WorkbenchTemplateMappingItem> updatedItems = dlg.getUpdatedItems();
	                
	                session.beginTransaction();
	                
	                // Merge with current session
	                WorkbenchTemplate workbenchTemplate = session.merge(wbTemplate);
	                
	                Set<WorkbenchTemplateMappingItem> items = workbenchTemplate.getWorkbenchTemplateMappingItems();
	                for (WorkbenchTemplateMappingItem delItem : deletedItems)
	                {
	                    for (WorkbenchTemplateMappingItem wbtmi : items)
	                    {
	                    	if (delItem.getWorkbenchTemplateMappingItemId().longValue() == wbtmi.getWorkbenchTemplateMappingItemId().longValue())
	                        {
	                            //log.debug("del ["+wbtmi.getCaption()+"]["+wbtmi.getWorkbenchTemplateMappingItemId().longValue()+"]");
	                            //wbtmi.setWorkbenchTemplate(null);
	                    		
	                    		items.remove(wbtmi);
	                    		wbtmi.setWorkbenchTemplate(null);
	                            if (wbtmi.getWorkbenchDataItems() != null)
	                            {
	                              
	                            	for (WorkbenchDataItem wbdi : wbtmi.getWorkbenchDataItems())
	                                {
	                                    wbdi.getWorkbenchRow().getWorkbenchDataItems().remove(wbdi);
	                                    wbdi.setWorkbenchRow(null);
	                            		session.delete(wbdi);
	                            		wbdi.setWorkbenchTemplateMappingItem(null);
	                                }
	                            	wbtmi.getWorkbenchDataItems().clear();
	                            }
	                            session.delete(wbtmi);
	                            break;
	                        }
	                    }
	                }
	                
	                for (WorkbenchTemplateMappingItem wbtmi : newItems)
	                {
	                    wbtmi.setWorkbenchTemplate(workbenchTemplate);
	                    items.add(wbtmi);
	                    //log.debug("new ["+wbtmi.getCaption()+"]["+wbtmi.getViewOrder().shortValue()+"]");
	                    session.saveOrUpdate(wbtmi) ;
	                }
	                                
	                //Check to see if geo/ref data needs to be updated
	                //This is actually only necessary if lat/long mappings have been switched - lat mapping changed to a long mapping or vice-versa.
	                //XXX Surely it is possible to tell if a lat/long switch has been made and not do this after every template change??
	                WorkbenchTemplateMappingItem aGeoRefMapping = null;
	                for (WorkbenchTemplateMappingItem wbtmi : workbenchTemplate.getWorkbenchTemplateMappingItems())
	                {
	                    if (aGeoRefMapping == null && wbtmi.getTableName().equals("locality"))
	                    {
	                        if (wbtmi.getFieldName().equalsIgnoreCase("latitude1") || wbtmi.getFieldName().equalsIgnoreCase("latitude2")
	                                || wbtmi.getFieldName().equalsIgnoreCase("longitude1") || wbtmi.getFieldName().equalsIgnoreCase("longitude2"))
	                        {
	                        	aGeoRefMapping = wbtmi;
	                        	break;
	                        }
	                    }
	                }
	                if (aGeoRefMapping != null)
	                {
	                	for (Workbench wb : workbenchTemplate.getWorkbenches())
	                	{
	                		wb.forceLoad();
	                		int rowCount = wb.getWorkbenchRows().size();
	                		int count = 0;
	                		for (WorkbenchRow wbRow : wb.getWorkbenchRows())
	                		{
	                			wbRow.updateGeoRefTextFldsIfNecessary(aGeoRefMapping);
	                			glassPane.setProgress((int)( (100.0 * count++) / rowCount));
	                		}
	                		//session.saveOrUpdate(wb);
	                	}
	                }

	                session.saveOrUpdate(workbenchTemplate);
	                for (Workbench wb : workbenchTemplate.getWorkbenches())
	                {
	                	session.saveOrUpdate(wb);
	                }
	                
	                session.commit();
	                session.flush();
	                
	                UIRegistry.getStatusBar().setText(getResourceString("WB_SAVED_MAPPINGS"));
	                
	            } catch (Exception ex)
	            {
	                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
	                log.error(ex);
	                ex.printStackTrace();
	                
	            } finally
	            {
	                try
	                {
	                    session.close();
	                } catch (Exception ex)
	                {
	                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
	                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
	                    log.error(ex);
	                }  
	            }
	            return null;
			}

			/* (non-Javadoc)
			 * @see javax.swing.SwingWorker#done()
			 */
			@Override
			protected void done() 
			{
				super.done();
				UIRegistry.clearSimpleGlassPaneMsg();
			}
    		

    	};
        sw.execute();
    }
    /**
     * Returns a path from the prefs and if it isn't valid then it return the User's Home Directory.
     * @param prefKey the Preferences key to look up
     * @return the path as a string
     */
    public static  String getDefaultDirPath(final String prefKey)
    {
        String homeDir = System.getProperty("user.home");
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        //log.info(homeDir);
        String path = localPrefs.get(prefKey, homeDir);
        File pathDir = new File(path);
        if (pathDir.exists() && pathDir.isDirectory())
        {
            return path;
        }
        return UIRegistry.getUserHomeDir();
    }
    
    /**
     * Imports a list if images and creates the rows.
     * @param workbench the current workbench
     * @param fileList the list of image files to be added
     * @param pane the visible pane containing the spreadsheet (workbench)
     * @param isNew if the passed in workbench is unsaved (and should be saved)
     * @param doOneImagePerRow whether to add all the images to a single row or not
     * @return
     */
    public static boolean importImages(final Workbench       workbench, 
                                       final Vector<File>    fileList,
                                       final WorkbenchPaneSS pane,
                                       final boolean         isNew,
                                       final boolean         doOneImagePerRow)
    {
        boolean                  isOK    = false;
        DataProviderSessionIFace session = null;
        try
        {
            if (pane != null)
            {
                pane.checkCurrentEditState();
            }
            
            if (isNew)
            {
                session = DataProviderFactory.getInstance().createSession();
                session.beginTransaction();
            }
            
            if (doOneImagePerRow)
            {
                for (int i=0;i<fileList.size();i++)
                {
                    File         file = fileList.get(i);
                    WorkbenchRow row  = workbench.addRow();
                    if (pane != null)
                    {
                        //pane.addRowAfter();
                        pane.addRowToSpreadSheet();
                    }
                    int inx = row.addImage(file);
                    if (inx > -1)
                    {
                        if (i % 5 == 0)
                        {
                            final String msg = workbench.getName() + " (" + i + " / " + fileList.size() + ")";
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run()
                                {
                                    
                                    UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_LOADING_IMGS_DATASET"), new Object[] {msg}), GLASSPANE_FONT_SIZE);
                                }
                            });
                        }
                    }
                }
            } else
            {
                WorkbenchRow row  = workbench.addRow();
                if (pane != null)
                {
                    pane.addRowToSpreadSheet();
                }
                
                for (int i=0;i<fileList.size();i++)
                {
                    int inx = row.addImage(fileList.get(i));
                    if (inx > -1)
                    {
                        if (i % 5 == 0)
                        {
                            final String msg = workbench.getName() + " (" + i + " / " + fileList.size() + ")";
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run()
                                {
                                    
                                    UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_LOADING_IMGS_DATASET"), new Object[] {msg}), GLASSPANE_FONT_SIZE);
                                }
                            });
                        }
                    }
                }
            }
            
            if (session != null)
            {
                session.saveOrUpdate(workbench);
                session.commit();
                session.flush();
            }
        
            isOK = true;
        
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
            log.error(ex);
            UIRegistry.clearGlassPaneMsg();
            
        } finally
        {
            if (session != null)
            {
                try
                {
                    session.close();
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                    log.error(ex);
                    UIRegistry.clearGlassPaneMsg();
                }
            }
        }
        
        return isOK;
    }
    
    /**
     * This filters out all non-image files per the image filter and adds them to the Vector.
     * @param files the list of files
     * @param fileList the returned Vector of image files
     * @param imageFilter the filter to use to weed out non-image files
     * @return true if it should continue, false to stop
     */
    protected boolean filterSelectedFileNames(final File[]       files, 
                                              final Vector<File> fileList,
                                              final ImageFilter  imageFilter)
    {
        if (files == null || files.length == 0)
        {
            return false;
        }

        Hashtable<String, Boolean> badFileExts = new Hashtable<String, Boolean>();

        for (int i=0;i<files.length;i++)
        {
            if (files[i].isFile())
            {
                String fileName = files[i].getName();
                if (imageFilter.isImageFile(fileName))
                {
                    fileList.add(files[i]);
                } else
                {
                    badFileExts.put(FilenameUtils.getExtension(fileName), true);
                }
            }
        }
        
        // No check to see if we had any bad files and warn the user about them
        if (badFileExts.size() > 0)
        {
            StringBuffer badExtStrBuf = new StringBuffer();
            for (String ext : badFileExts.keySet())
            {
                if (badExtStrBuf.length() > 0) badExtStrBuf.append(", ");
                badExtStrBuf.append(ext);
            }
            
            // Now, if none of the files were good we tell them and then quit the import task
            if (fileList.size() == 0)
            {
                JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(), 
                        String.format(getResourceString("WB_WRONG_IMG_NO_IMAGES"), 
                                new Object[] {badExtStrBuf.toString()}),
                        UIRegistry.getResourceString("WARNING"), 
                        JOptionPane.ERROR_MESSAGE);
                return false;
                        
            }
            
            // So we know we have at least one good image file type
            // So let them choose if they want to continue.
            Object[] options = { getResourceString("Continue"), getResourceString("Stop")};
            
            if (JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                        String.format(getResourceString("WB_WRONG_IMG_SOME_IMAGES"), new Object[] {badExtStrBuf.toString()}),
                        title, JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null, options, options[1]) == JOptionPane.NO_OPTION)
            {
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * @param wbt
     * @param rs
     * @return
     */
    protected boolean wbCanAcceptRecordSet(final WorkbenchTemplate wbt, final RecordSetIFace rs)
    {
    	Integer wbTblId = getRootTblId(wbt);
    	return wbTblId != null && wbTblId.equals(rs.getDbTableId());
    }
    
    /**
     * @param wbt
     * @return
     */
    protected Integer getRootTblId(final WorkbenchTemplate wbt)
    {
    	//this is really stupid, but might work 99% of the time
    	Vector<Integer> tbls = new Vector<Integer>();
    	tbls.add(CollectionObject.getClassTableId());
    	tbls.add(CollectingEvent.getClassTableId());
    	tbls.add(Locality.getClassTableId());
    	tbls.add(Geography.getClassTableId());
    	tbls.add(Taxon.getClassTableId());
    	tbls.add(Accession.getClassTableId());
    	tbls.add(ReferenceWork.getClassTableId());
    	tbls.add(Agent.getClassTableId());
    	for (Integer tbl : tbls)
    	{
    		for (WorkbenchTemplateMappingItem mi : wbt.getWorkbenchTemplateMappingItems())
    		{
    			if (mi.getSrcTableId().equals(tbl)
    					|| (mi.getSrcTableId() == 4000 /*TaxonImportOnly*/ && tbl == Taxon.getClassTableId()))
    			{
    				return tbl;
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * @param rs
     */
    protected List<WorkbenchTemplate> getTemplatesForExport(final RecordSetIFace rs)
    {
    	Vector<WorkbenchTemplate> result = new Vector<WorkbenchTemplate>();
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
        	for (WorkbenchTemplate wbt : session.getDataList(WorkbenchTemplate.class))
        	{
        		wbt.forceLoad();
        		if (wbCanAcceptRecordSet(wbt, rs))
        		{
        			result.add(wbt);
        		}
        	}
        } finally 
        {
        	session.close();
        }
        Collections.sort(result, new Comparator<WorkbenchTemplate> () {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(WorkbenchTemplate arg0, WorkbenchTemplate arg1) {
				// TODO Auto-generated method stub
				return arg0.getName().toLowerCase().compareTo(arg1.getName().toLowerCase());
			}
        });
    	return result;
    }
    /**
     * @param action
     * 
     * Exports records in a recordset to a pre-defined workbench template.
     */
    protected void exportRStoDS(final CommandAction action)
    {
    	//UIRegistry.displayErrorDlg("Exporting to wb");
    	
    	//Choose a recordset if the action is not a rs drop
    	Vector<Integer> tblIds = new Vector<Integer>();
    	ChooseRecordSetDlg dlg = new ChooseRecordSetDlg(tblIds);
    	dlg.setHelpContext("wb_recordset");
        dlg.setVisible(true); // modal (waits for answer here)
        if (!dlg.isCancelled())
        {
        	//check size
			if (dlg.getSelectedRecordSet().getNumItems() > AppPreferences.getRemote().getInt("MAX_ROWS", MAX_ROWS))
			{
				UIRegistry.showLocalizedError("WorkbenchTask.RecordSetTooLargeToExport", AppPreferences.getRemote().getInt("MAX_ROWS", MAX_ROWS));
				return;
			}
        	//Choose a workbench whose 'main' table matches the type of the recordset
        	List<WorkbenchTemplate> choices = getTemplatesForExport(dlg.getSelectedRecordSet());
        	if (choices.size() > 0)
        	{
        		ChooseFromListDlg<WorkbenchTemplate> wbtdlg = new ChooseFromListDlg<WorkbenchTemplate>((Frame )UIRegistry.getTopWindow(), 
        				UIRegistry.getResourceString("WB_CHOOSE_EXPORT_WB_TITLE"), 
        				ChooseFromListDlg.OK_BTN | ChooseFromListDlg.CANCEL_BTN | ChooseFromListDlg.HELP_BTN, 
        				choices);
        		wbtdlg.setHelpContext("wb_recordset");
        		wbtdlg.setVisible(true);
        		if (!wbtdlg.isCancelled())
        		{
        			//load the data
        			try
        			{
        				Workbench workbench = createNewWorkbenchDataObj(null, (WorkbenchTemplate )wbtdlg.getSelectedObject().clone());
        				if (workbench != null)
        				{
        					fillandSaveWorkbench(dlg.getSelectedRecordSet(), workbench);
        				}
        			} catch (CloneNotSupportedException ex)
        			{
        	    		edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
        				edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
        				ex.printStackTrace();
        				log.error(ex);
        			}
        		}
        	}
        	else 
        	{
        		UIRegistry.showLocalizedMsg("WB_UNABLE_TO_EXPORT_RS", "WB_NO_TEMPLATES_TO_EXPORT_RS_TO", 
        				DBTableIdMgr.getInstance().getInfoById(dlg.getSelectedRecordSet().getDbTableId()).getClassObj().getSimpleName());
        	}
        }
    }
    
    /**
     * Imports a set of image files, creating a new row per file, to the provided {@link Workbench} parameter.  If the
     * given {@link Workbench} is <code>null</code>, a new {@link Workbench} is created.
     * 
     * @param workbenchArg the {@link Workbench} to append rows to, or <code>null</code> if a new {@link Workbench} should be created
     * @param doOneImagePerRow indicates whether the images are assign to a single row or not.
     */
    public void importImageIndexFile(Workbench workbench, final boolean doOneImagePerRow)
    {
    }
    
    /**
     * Imports a set of image files, creating a new row per file, to the provided {@link Workbench} parameter.  If the
     * given {@link Workbench} is <code>null</code>, a new {@link Workbench} is created.
     * 
     * @param workbenchArg the {@link Workbench} to append rows to, or <code>null</code> if a new {@link Workbench} should be created
     * @param doOneImagePerRow indicates whether the images are assign to a single row or not.
     */
    public void importCardImages(Workbench workbench, final boolean doOneImagePerRow)
    {
        // ask the user to select the files to import
        final ImageFilter imageFilter = new ImageFilter();
        JFileChooser chooser = new JFileChooser(getDefaultDirPath(IMAGES_FILE_PATH));
        chooser.setDialogTitle(getResourceString("WB_CHOOSE_IMAGES"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(true);
        chooser.setFileFilter(imageFilter);
        
        if (chooser.showOpenDialog(UIRegistry.get(UIRegistry.FRAME)) != JFileChooser.APPROVE_OPTION)
        {
            UIRegistry.getStatusBar().setText("");
            return;
        }
        
        AppPreferences localPrefs = AppPreferences.getLocalPrefs();
        localPrefs.put(IMAGES_FILE_PATH, chooser.getCurrentDirectory().getAbsolutePath());
        
        // Start by looping through the files and checking for image file extensions
        // weed out the bad files.
        final Vector<File> fileList = new Vector<File>();
        if (!filterSelectedFileNames(chooser.getSelectedFiles(), fileList, imageFilter))
        {
            return;
        }
        
        for (File f: fileList)
        {
            if (!ImageFrame.testImageFile(f.getAbsolutePath()))
            {
                JOptionPane.showMessageDialog(UIRegistry.getMostRecentWindow(),
                                            String.format(getResourceString("WB_WRONG_IMAGE_TYPE_OR_CORRUPTED_IMAGE"), new Object[] {f.getAbsolutePath()}),
                                            UIRegistry.getResourceString("WARNING"), 
                                            JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        boolean creatingNewWb = workbench == null;
        
        if (creatingNewWb) // create a new Workbench
        {
            List<?> selection = selectExistingTemplate(null, "WorkbenchImportImages");
        	//Pair<Boolean, WorkbenchTemplate> selection = selectExistingTemplate(null, "WorkbenchImportImages");
            
            if (selection.size() == 0 || !(Boolean )selection.get(0))
            {
            	return; //cancelled
            }
            
            WorkbenchTemplate workbenchTemplate = selection.size() > 1 ? (WorkbenchTemplate )selection.get(1) : null;
            
            if (workbenchTemplate == null)
            {
                // create a new WorkbenchTemplate
                TemplateEditor dlg = null;
                try 
                {
                	dlg = showColumnMapperDlg(null, null, "WB_MAPPING_EDITOR");
                } catch (Exception ex)
                {	                    
                	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                	log.error(ex);
                }
                if (dlg != null && !dlg.isCancelled())
                {   
                    workbenchTemplate = createTemplate(dlg, null);
                }
                if (dlg != null)
                {
                	dlg.dispose();
                }

            }
            else
            {
            	workbenchTemplate = cloneWorkbenchTemplate(workbenchTemplate);
            }
            if (workbenchTemplate != null)
            {
                workbench = createNewWorkbenchDataObj("", workbenchTemplate);
            }

        }

        if (workbench != null) // this should always hold, but whatev
        {
            UIRegistry.writeGlassPaneMsg(String.format(getResourceString("WB_LOADING_IMGS_DATASET"), new Object[] {workbench.getName()}), GLASSPANE_FONT_SIZE);
            doImageImport(workbench, fileList, creatingNewWb, doOneImagePerRow);
        }
    }
    
    /**
     * @param importWB
     * @param wbPaneSS
     * @param fileList
     * @param isNew
     * @param doOneImagePerRow
     */
    protected void doImageImport(final Workbench       importWB, 
                                 final Vector<File>    fileList, 
                                 final boolean         isNew, 
                                 final boolean         doOneImagePerRow)
    {
        final SwingWorker worker = new SwingWorker()
        {
            protected boolean isOK = false;
            
            protected WorkbenchPaneSS pane = !isNew ?
                    (WorkbenchPaneSS)SubPaneMgr.getInstance().getCurrentSubPane() : null;
            
            @Override
            public Object construct()
            {
                // import the images into the Workbench, creating new rows (and saving the WB if it is brand new)
                isOK = importImages(importWB, fileList, pane, isNew, doOneImagePerRow);
                
                return null;
            }

            @Override
            public void finished()
            {
                UIRegistry.clearGlassPaneMsg();
                
                if (isOK)
                {
                    if (isNew) // meaning a brand new Workbench was created (and saved already)
                    {
                    	// add a new button to the NavBox
                        datasetNavBoxMgr.addWorkbench(importWB);
                        getBoxByTitle(workbenchNavBox, importWB.getName()).setEnabled(false);
                        
                        // show the WorkbenchPaneSS
                        try
                        {
                        	pane = new WorkbenchPaneSS(importWB.getName(), WorkbenchTask.this, importWB, false,  !isPermitted());
                        	addSubPaneToMgr(pane);
                        
                        	// the importImages() call will save the wb if it was just created
                        	pane.setChanged(false);
                        } catch (Exception ex)
                        {
                        	edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        	edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
                        	log.error(ex);
                        	return;
                        }
                    }
                    else // the Workbench already existed and a pane was already showing
                    {
                        // update the "Save" button state
                    	pane.setChanged(true);
                    }
                    
                    //pane.setImageFrameVisible(true);

                    // scrolls to the last row
                    pane.newImagesAdded();
                }
            }
        };
        worker.start();
    }
    
    /**
     * Show error dialog for image load.
     * @param status the status of the load.
     * @param loadException the excpetion that occurred.
     * @return true to continue, false to stop
     */
    public static boolean showLoadStatus(final WorkbenchRow row, final boolean hasMoreFiles)
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
        
        JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setErrorMessage(getResourceString(key), row.getLoadException());
        if (hasMoreFiles)
        {
            return UIRegistry.displayConfirmLocalized("WB_ERROR_LOAD_IMAGE", key,  getResourceString("Continue"), "WB_STOP_LOADING", JOptionPane.ERROR_MESSAGE);
        }
        JOptionPane.showMessageDialog(UIRegistry.getTopWindow(), getResourceString(key), getResourceString("WB_ERROR_LOAD_IMAGE"), JOptionPane.ERROR_MESSAGE);
        return false;
        
    }
    
    /**
     * Loads a Workbench into Memory 
     * @param recordSet the RecordSet containing thew ID
     * @return the workbench or null
     */
    public static Workbench loadWorkbench(final RecordSetIFace recordSet)
    {
        if (recordSet != null)
        {
            return loadWorkbench(recordSet.getOnlyItem().getRecordId(), null);
        }
        return null;
    }

    /**
     * @param workbenchId
     * @return workbench with matching id or null
     */
    public static Workbench loadWorkbench(final Integer workbenchId, final DataProviderSessionIFace session)
    {
    	DataProviderSessionIFace mySession = session != null ? session :
    		DataProviderFactory.getInstance().createSession();
    	try
    	{
    		if (workbenchId != null)
    		{
    				Workbench workbench  = mySession.get(Workbench.class, workbenchId);
    				if (workbench != null)
    				{
    					workbench.getWorkbenchId();
    				}
    				return workbench;       
    		}
    	}	catch (Exception ex)
		{
    		edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
			log.error(ex);
		} finally
		{
			if (session == null)
			{
				mySession.close();
			}
		}
        return null;
    }

    
    /**
     * Loads a Workbench into Memory 
     * @param recordSet the RecordSet containing thew ID
     * @return the workbench or null
     */
    protected WorkbenchTemplate loadWorkbenchTemplate(final RecordSetIFace recordSet)
    {
        if (recordSet != null)
        {
            DataProviderSessionIFace session   = DataProviderFactory.getInstance().createSession();
            try
            {
                WorkbenchTemplate workbenchTemplate = session.get(WorkbenchTemplate.class, recordSet.getOnlyItem().getRecordId());
                if (workbenchTemplate != null)
                {
                    workbenchTemplate.checkMappings(getDatabaseSchema());
                }
                return workbenchTemplate;
                
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, ex);
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
//        if (false)
//        {
//        	Vector<Integer> wbIds = new Vector<Integer>();
//        	wbIds.add(1);
//        	uploadWorkbenches(wbIds);
//        }
    	
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
            
        } else if (cmdData instanceof RecordSetIFace)
        {
            Workbench workbench = loadWorkbench((RecordSetIFace)cmdData);
            if (workbench != null)
            {
                createEditorForWorkbench(workbench, null, false, true);
            } else
            {
                log.error("Workbench was null!");
            }
            
        } else
        {
            // This is for when the user clicks directly on the workbench
            Workbench workbench = loadWorkbench((RecordSetIFace)cmdAction.getProperty("workbench"));
            if (workbench != null)
            {
                createEditorForWorkbench(workbench, null, false, true);
            } else
            {
                log.error("Workbench was null!");
            }
        }
    }
    
    
    /**
     * @param workbenchNames
     */
	public void uploadWorkbenches(final List<Integer> workbenchIds) 
	{
		/* !!!!!!!!!!!!!! for batch upload hack!!
		 * testingJUNK = false;
		 */
		
		javax.swing.SwingWorker<Object, Object> worker = new javax.swing.SwingWorker<Object, Object>() {

			/**
			 * @param wbId
			 * @return
			 */
			protected RecordSet bldRS(Integer wbId)
			{
				RecordSet result = new RecordSet();
				result.initialize();
				result.addItem(wbId);
				return result;
			}
			
			/**
			 * @param wb
			 * @return
			 */
			protected WorkbenchPaneSS getWbSS(Workbench wb)
			{
				WorkbenchPaneSS wbSS = null;
				for (SubPaneIFace sb : SubPaneMgr.getInstance()
						.getSubPanes())
				{
					if (sb instanceof WorkbenchPaneSS)
					{
						Workbench pwb = ((WorkbenchPaneSS) sb)
								.getWorkbench();
						if (pwb == wb)
						{
							wbSS = (WorkbenchPaneSS) sb;
							break;
						}
					}
				}
				return wbSS;
			}
			
			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.SwingWorker#doInBackground()
			 */
			@Override
			protected Object doInBackground() throws Exception 
			{
				for (Integer wbId : workbenchIds)
				{
					try
					{
						RecordSet rs = bldRS(wbId);
						final Workbench wb = loadWorkbench(rs);
						if (wb != null)
						{
							System.out.println("loaded " + wb.getName());
							SwingUtilities.invokeAndWait(new Runnable(){

								/* (non-Javadoc)
								 * @see java.lang.Runnable#run()
								 */
								@Override
								public void run() {
									createEditorForWorkbench(wb, null, false, false);
								}
								
							});
							final WorkbenchPaneSS wbSS = getWbSS(wb);
							if (wbSS != null)
							{
								SwingUtilities.invokeAndWait(new Runnable(){
									/* (non-Javadoc)
									 * @see java.lang.Runnable#run()
									 */
									@Override
									public void run() {
										wbSS.doDatasetUpload();
										System.out.println("opened uploader for " + wb.getName());
									}
									
								});
								
//								SwingUtilities.invokeLater(new Runnable(){
//									/* (non-Javadoc)
//									 * @see java.lang.Runnable#run()
//									 */
//									@Override
//									public void run() {
										boolean validated = Uploader.getCurrentUpload().validateData(false);
//									}
//									
//								});
								if (validated)
								{
									System.out.println("validated uploader for "+ wb.getName());
									Uploader.getCurrentUpload().uploadIt(false);
									if (Uploader.getCurrentUpload().getCurrentOp() != Uploader.SUCCESS)
									{
										System.out.println("upload failed for " + wb.getName());
									} else
									{
										System.out.println("uploaded " + wb.getName());
										// NOT saving recordsets of uploaded
										// objects or setting isUploaded status for wb rows.
									}
								}
								SwingUtilities.invokeLater(new Runnable() {

									/* (non-Javadoc)
									 * @see java.lang.Runnable#run()
									 */
									@Override
									public void run() 
									{
										Uploader.getCurrentUpload().closeMainForm(true);
										SubPaneMgr.getInstance().removePane(wbSS);
									}
									
								});
								System.out.println("closed uploader for " + wb.getName());
							}
						} else
						{
							System.out.println("No workbench with id = " + wbId);
						}
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				return null;
			}
		};
		worker.execute();
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
        DBTableInfo  tableInfo = schema.getInfoById(wbtmi.getSrcTableId());
        if (tableInfo == null)
        {
            throw new RuntimeException("Cannot find TableInfo in DBTableIdMgr for ID=" + wbtmi.getSrcTableId());
        }
        
        for (DBFieldInfo fi : tableInfo.getFields())
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
                        
                    } else if (type.equals("short"))
                    {
                        return Short.class;
                        
                    } else if (type.equals("byte"))
                    {
                        return Byte.class;
                        
                    } else
                    {
                        try
                        {
                            return Class.forName(type);
                            
                        } catch (Exception e)
                        {
                            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(WorkbenchTask.class, e);
                            log.error(e);
                        }
                    }
                }
            }
        }

        throw new RuntimeException("Could not find [" + wbtmi.getFieldName()+"]");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getImageIcon()
     */
    @Override
    public ImageIcon getIcon(final int size)
    {
        IconManager.IconSize iSize = IconManager.IconSize.Std16;
        if (size != Taskable.StdIcon16)
        {
            for (IconManager.IconSize ic : IconManager.IconSize.values())
            {
                if (ic.size() == size)
                {
                    iSize = ic;
                    break;
                }
            }
        }
        if (doingStarterPane)
        {
            doingStarterPane = false;
        }
        return IconManager.getIcon("Workbench", iSize);
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
        boolean isClickedOn = true;//cmdAction.getData() instanceof CommandAction && cmdAction.getData() == cmdAction;
        
        UsageTracker.incrUsageCount("WB."+cmdAction.getAction());

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
           
        } 
//        else if (cmdAction.isAction(UPLOAD))
//        {
//            uploadWorkbench(cmdAction);
//        }
        else if (cmdAction.isAction(EXPORT_TEMPLATE))
        {
            exportWorkbenchTemplate(cmdAction);
            
        } else if (cmdAction.isAction(EXPORT_RS_TO_WB))
        {
        	exportRStoDS(cmdAction);
        	
        } else if (cmdAction.isAction(WB_IMPORTCARDS))
        {
            if (isClickedOn)
            {
                importCardImages(null, true);
            }
        } else if (cmdAction.isAction(WB_IMPORT_IMGINDEX))
        {
            if (isClickedOn)
            {
                importImageIndexFile(null, true);
            }
            
        } else if (cmdAction.isAction(NEW_WORKBENCH_FROM_TEMPLATE)) // XXX This can be removed
        {
            createWorkbenchFromTemplate(cmdAction);
            
        } else if (cmdAction.isAction(NEW_WORKBENCH))
        {
            if (isClickedOn)
            {
                createNewWorkbench(null, null);
            } else
            {
                createWorkbenchFromTemplate(cmdAction);
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
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                if (rs.getDbTableId() == Workbench.getClassTableId())
                {
                    deleteWorkbench(rs);
                    
                }
            }
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
            //viewsNavBox.clear();
            //initializeViewsNavBox();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        UIRegistry.getStatusBar().setText("");
        
        if (cmdAction.isType(WORKBENCH))
        {
            //SwingUtilities.invokeLater(new Runnable() {
            //    public void run()
            //    {
                    processWorkbenchCommands(cmdAction);
            //    }
            //});
            
        } else if (cmdAction.isType("Preferences"))
        {
            AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
            if (appPrefs != null)
            {
                MAX_ROWS = appPrefs.getInt("MAX_ROWS", 2000);
            }
        }
    }
    
    public boolean isPermitted()
    {
    	//view Permission => access to workbenches 
    	//(add Permission => access to Uploader)
    	return !AppContextMgr.isSecurityOn() || getPermissions().canView();
    }
    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, true, true},
                                {false, false, false, false}};
    }

	/* (non-Javadoc)
	 * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
	 */
	@Override
	public PermissionEditorIFace getPermEditorPanel()
	{
		return new BasicPermisionPanel("WorkbenchTask.PermTitle", "WorkbenchTask.PermEnable", "WorkbenchTask.PermUpload", null, null);
	}
    
//    public void uploadWorkbenches(List<String> workbenchNames)
//    {
//    	for (String wbName : workbenchNames)
//    	{
//    		
//    	}
//    }
}
