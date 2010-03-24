/* Copyright (C) 2009, University of Kansas Center for Research
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

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.TableModel;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.AutoText;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.ColumnBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.columns.AbstractColumn;
import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.NavBoxMgr;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.core.UsageTracker;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.SpAppResource;
import edu.ku.brc.specify.datamodel.SpQuery;
import edu.ku.brc.specify.datamodel.SpReport;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Workbench;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.tasks.subpane.LabelsPane;
import edu.ku.brc.specify.tasks.subpane.qb.QueryBldrPane;
import edu.ku.brc.specify.tasks.subpane.qb.SearchResultReportServiceInfo;
import edu.ku.brc.specify.tools.ireportspecify.MainFrameSpecify;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostActionableDropManager;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;
import edu.ku.brc.ui.dnd.Trash;
import edu.ku.brc.util.Pair;

/**
 * A task to manage Labels and response to Label Commands.
 *
 * @code_status Beta
 *
 * @author rods
 *
 */
public class ReportsBaseTask extends BaseTask
{
    protected static final Logger log = Logger.getLogger(ReportsBaseTask.class);
            
    // Static Data Members
    public static final String           REPORTS             = "Reports";
    public static final String           REPORTS_MIME        = "jrxml/report";
    public static final String           LABELS_MIME         = "jrxml/label";
    public static final String			 SUBREPORTS_MIME     = "jrxml/subreport";
    public static final String           RECORDSET_PARAM     = "itemnum";

    // public static final String DOLABELS_ACTION = "DoLabels";
    public static final String           NEWRECORDSET_ACTION = "RPT.NewRecordSet";
    public static final String           PRINT_REPORT        = "RPT.PrintReport";
    public static final String           OPEN_EDITOR         = "RPT.OpenEditor";
    public static final String           RUN_REPORT          = "RPT.RunReport";
    public static final String           REFRESH             = "RPT.Refresh";
    public static final String           IMPORT              = "RPT.Import";
    public static final String           REPORT_DELETED      = "RPT.ReportDeleted";
    public static final String           PRINT_GRID          = "RPT.PrintTable";

    // Data Members
    protected DataFlavor                 defaultFlavor       = null;
    protected DataFlavor                 reportFlavor      = new DataFlavor(SpReport.class,
                                                                     "SpReport");
    protected DataFlavor                 runReportFlavor     = new DataFlavor(SpReport.class,
                                                                     RUN_REPORT);
    protected List<Pair<String, String>> navMimeDefs         = null;
    protected Vector<NavBoxIFace>        extendedNavBoxes    = new Vector<NavBoxIFace>();
    protected Vector<NavBoxItemIFace>    reportsList         = new Vector<NavBoxItemIFace>();
    protected NavBox                     actionNavBox        = null;
    protected NavBox                     reportsNavBox       = null;
    protected NavBox                     labelsNavBox        = null;
    protected String                     reportHintKey       = "REPORT_TT";

    // temp data
    protected NavBoxItemIFace            oneNbi              = null;

    //iReport MainFrame
    //protected static MainFrameSpecify iReportMainFrame   = null;
    //protected static MainFrame iReportMainFrame   = null;
    
    /**
     * Constructor.
     */
    public ReportsBaseTask(final String name, final String title)
    {
        super(name, title);
        
        //iReportMainFrame = null;
        isShowDefault = true;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#preInitialize()
     */
    @Override
    public void preInitialize()
    {
        CommandDispatcher.register(REPORTS, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        
        //RecordSetTask.addDroppableDataFlavor(defaultFlavor);
        
        // Create and add the Actions NavBox first so it is at the top at the top
        actionNavBox = new NavBox(getResourceString("Actions"));
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
            
            CommandAction cmdAction = new CommandAction(REPORTS, PRINT_GRID);
            ContextMgr.registerService(20, PRINT_GRID, -1, cmdAction, this, "Print", getResourceString("PRINT_GRID_TT"));
            
            extendedNavBoxes.clear();
            reportsList.clear();
            
            navBoxes.add(actionNavBox);

            if (isVisible)
            {
                addROCs();
            }
        }
    }

    /**
     * @param mimeType
     * @param reportType
     * @param defaultIcon
     * @param classTableId
     * @return List of command defs for valid report AppResources.
     * 
     * Needed to 'override' static method in BaseTask to deal with imported reports.
     */
    protected List<TaskCommandDef> getAppResCommandsByMimeType(final String mimeType,
                                                               final String reportType,
                                                               final String defaultIcon,
                                                               final Integer classTableId)

    {
        List<TaskCommandDef> result = new LinkedList<TaskCommandDef>();
        Vector<Integer> excludedTableIds = new Vector<Integer>();
        excludedTableIds.add(79);
        Vector<String> subReports = new Vector<String>();
        for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType(mimeType))
        {
        	Properties params = ap.getMetaDataMap();

            String tableid = params.getProperty("tableid"); //$NON-NLS-1$
            
            if (StringUtils.isNotEmpty(tableid))
			{
				String subReps = params.getProperty("subreports"); 
				if (subReps != null)
				{
					String[] subRepNames = StringUtils.split(subReps, ",");
					for (String subRepName : subRepNames)
					{
						subReports.add(subRepName.trim());
					}
				}
            	String rptType = params.getProperty("reporttype"); //$NON-NLS-1$
				Integer tblId = Integer.parseInt(tableid);
				if (excludedTableIds.indexOf(tblId) == -1
						&& (classTableId == null || tblId.equals(classTableId))
						&& (StringUtils.isEmpty(reportType) || (StringUtils
								.isNotEmpty(rptType) && reportType
								.equals(rptType))))
				{
					params.put("name", ap.getName()); //$NON-NLS-1$
					params
							.put(
									"title", ap.getDescription() == null ? ap.getName() : ap.getDescription()); //$NON-NLS-1$
					params.put("file", ap.getName()); //$NON-NLS-1$
					params.put("mimetype", mimeType); //$NON-NLS-1$
					Object param = params.get("isimport");
					if (param != null && param.equals("1"))
					{
						params.put("appresource", ap);
					}
					param = params.get("hasrsdropparam");
					if (param != null)
					{
						params.put(RECORDSET_PARAM, param);
					}
					String localIconName = params.getProperty("icon"); //$NON-NLS-1$
					if (StringUtils.isEmpty(localIconName))
					{
						localIconName = defaultIcon;
					}
					result.add(new TaskCommandDef(ap.getDescription(),
							localIconName, params));
				}
			}
		}
        if (subReports.size() > 0)
        {
        	for (int r = result.size() - 1; r >= 0; r--)
        	{
        		TaskCommandDef def = result.get(r);
        		if (subReports.indexOf(def.getName()) != -1)
        		{
        			result.remove(r);
        		}
        			
        	}
        }
        return result;
    }
    

    /**
     * 
     */
    protected void addROCs()
    {
        for (Pair<String, String> navMime : navMimeDefs)
        {
            NavBox navBox;
            if (navMime.getSecond().equals(REPORTS_MIME))
            {
                if (reportsNavBox == null)
                {
                    reportsNavBox = new NavBox(navMime.getFirst());
                }
                navBox = reportsNavBox;
            }
            else if (navMime.getSecond().equals(LABELS_MIME))
            {
                if (labelsNavBox == null)
                {
                    labelsNavBox = new NavBox(navMime.getFirst());
                }
                navBox = labelsNavBox;
            }
            else
            {
                //XXX huh?
            	navBox = new NavBox(navMime.getFirst()); 
            }
            
            // no particular table at the moment
            List<TaskCommandDef> cmds = getAppResCommandsByMimeType(navMime
                    .getSecond(), "Report", navMime.getFirst(), null);
            cmds.addAll(getAppResCommandsByMimeType(navMime
                    .getSecond(), "Invoice", navMime.getFirst(), null));

            // Then add
            for (TaskCommandDef tcd : cmds)
            {
                // XXX won't be needed when we start validating the XML
                String tableIdStr = tcd.getParams().getProperty("tableid");
                if (tableIdStr != null)
                {
                    boolean makeROC = true;
                    if (AppContextMgr.isSecurityOn())
                    {
                        DBTableInfo tblFo = DBTableIdMgr.getInstance().getInfoById(Integer.valueOf(tableIdStr));
                        if (tblFo != null)
                        {
                            makeROC = tblFo.getPermissions().canView();
                        }
                    }
                    if (makeROC)
                    {
                        makeROCForCommand(tcd, navBox);
                    }
                }
            }
            navBoxes.add(navBox);
            commands.addAll(cmds); //currently commands is always already initialized by TaskMgr.
        }        
    }
    
    /**
     * @param tcd
     * @param navBox
     * @return
     */
    protected RolloverCommand makeROCForCommand(final TaskCommandDef tcd, final NavBox navBox)
    {
        CommandAction cmdAction = new CommandAction(REPORTS, PRINT_REPORT, tcd.getParams().getProperty("tableid"));
        cmdAction.addProperties(tcd.getParams());
        cmdAction.getProperties().put("icon", IconManager.getIcon(tcd.getIconName(), IconManager.IconSize.Std16));
        cmdAction.getProperties().put("task name", getName());
        
        //Rough
        //See if there is an SpReport record for the resource.
        String spRepName = cmdAction.getProperties().getProperty("name").replace(".jrxml", "");
        RecordSet repRS = null;
        Integer tblContext = null;
        if (spRepName != null)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance()
            .createSession();
            try
            {
                SpReport rep = (SpReport)session.getData("from SpReport where name = '" + StringEscapeUtils.escapeSql(spRepName) + "'");
                if (rep != null)
                {
                    rep.forceLoad();
                    if (rep.getReportObject() instanceof SpQuery)
                    {
                    	SpQuery q = (SpQuery )rep.getReportObject();
                    	if (q.getContextTableId() != -1)
                    	{
                    		tblContext = new Integer(q.getContextTableId());
                    		//XXX tableid property needs to be -1 (I think) for report running routines
                    		//to distinguish SpReports from other reports. So need a duplicate way to know tableid
                    		//This is dumb. Report running code needs to be re-thunk.
                    		cmdAction.getProperties().put("tblcontext", tblContext); 
                    		cmdAction.getProperties().put("queryid", q.getId());
                    	}
                    }
                    else if (rep.getReportObject() instanceof Workbench)
                    {
                    	cmdAction.getProperties().put("tblcontext", Workbench.getClassTableId());
                    	cmdAction.getProperties().put("workbenchid", rep.getReportObject().getId());
                    }
                    repRS  = new RecordSet();
                    repRS.initialize();
                    repRS.set(rep.getAppResource().getDescription(), SpReport.getClassTableId(), RecordSet.GLOBAL);
                    repRS.addItem(rep.getId());
                    cmdAction.setProperty("spreport", repRS);
                }
            }
            finally
            {
                session.close();
            }
        }
        
        CommandAction delCmd = null;
        if (repRS != null || cmdAction.getProperties().get("isimport") != null)
        {
            if (!AppContextMgr.isSecurityOn() || getPermissions().canDelete())
            {
                delCmd = new CommandAction(REPORTS, DELETE_CMD_ACT, repRS);
                if (cmdAction.getProperties().get("isimport") != null)
                {
                    delCmd.getProperties().put("name", cmdAction.getProperties().getProperty("name"));
                    delCmd.getProperties().put("appresource", cmdAction.getProperties().get("appresource"));
                    cmdAction.getProperties().remove("appresource");
                }
            }
        }
        
        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, 
                                            tcd.getName(), 
                                            tcd.getIconName(), 
                                            cmdAction, 
                                            delCmd, 
                                            true,  // true means make it draggable
                                            true); // true means add sorted
        reportsList.add(nbi);
        
        RolloverCommand roc = (RolloverCommand)nbi;
        String tblIdStr = tcd.getParams().getProperty("tableid");
        String dropStr = tcd.getParams().getProperty("hasrsdropparam");
        //String impStr = tcd.getParams().getProperty("isimport");
        boolean canBeRsDropSite = dropStr != null ? dropStr.equals("1") : true;
        if (StringUtils.isEmpty(tblIdStr) && canBeRsDropSite)
        {
            roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);
        } else if (tblContext == null && canBeRsDropSite)
        {
            DataFlavorTableExt dfx = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), 
                    RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), new int[] {Integer.parseInt(tblIdStr)});
            roc.addDropDataFlavor(dfx);
        }
        
        roc.addDragDataFlavor(reportFlavor);
        
        if (cmdAction.getProperties().get("spreport") != null)
        {
            roc.addDragDataFlavor(reportFlavor);
            if (tblContext != null)
            {
                DataFlavorTableExt dfx = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), 
                        RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), new int[] {tblContext});
                roc.addDropDataFlavor(dfx);
            }
        }
        
        if (delCmd != null)
        {
            roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        }
        roc.setToolTip(getResourceString(reportHintKey));
        roc.setEnabled(true);
        
        return roc;
    }
    
    /**
     * Performs a command (to create a label).
     * @param labelName the name of lable (the file name)
     * @param labelTitle the localized title to be displayed as the tab title
     * @param recordSets the recordSet to be turned into labels
     * @param params parameters for the report
     * @param originatingTask the Taskable requesting the the labels be made
     * @param paneIcon the icon of the pane(if it is null then it uses the Task's icon)
     */
    public void doLabels(final String              labelName, 
                         final String              labelTitle, 
                         final Object              data, 
                         final Properties          params,
                         final Taskable            originatingTask,
                         final ImageIcon           paneIcon)
    {
        LabelsPane labelsPane;
        labelsPane = new LabelsPane(labelTitle, originatingTask != null ? originatingTask : this, params);
        labelsPane.setIcon(paneIcon);
        addSubPaneToMgr(labelsPane);

        labelsPane.createReport(labelName, data, params);
        starterPane = null;
    }

    /**
     * @return Return true if there is a small number of labels or whether the user wishes to continue.
     */
    protected boolean checkForALotOfLabels(final RecordSetIFace recordSet)
    {
        //
        if (recordSet.getNumItems() > 200) // XXX Pref
        {
            Object[] options = {getResourceString("Create_New_Report"), getResourceString("CANCEL")};
            int n = JOptionPane.showOptionDialog(UIRegistry.get(UIRegistry.FRAME),
                                                String.format(getResourceString("LotsOfLabels"), new Object[] {(recordSet.getNumItems())}),
                                                getResourceString("LotsOfLabelsTitle"),
                                                JOptionPane.YES_NO_OPTION,
                                                JOptionPane.QUESTION_MESSAGE,
                                                null,     //don't use a custom Icon
                                                options,  //the titles of buttons
                                                options[0]); //default button title
            if (n == 1)
            {
                return false;
            }
        }
        return true;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        LabelsPane labelsPane = new LabelsPane(name, this, null);
        labelsPane.setLabelText("Welcome to Specify's Label Maker"); // XXX I18N
        starterPane = labelsPane;
        return starterPane;
    }

    /**
     * Displays UI that asks the user to select a predefined label.
     * @return the name of the label file or null if cancelled
     */
    protected String askForLabelName()
    {
        initialize();

        // XXX Need to pass in or check table type for different types of lables.

        NavBoxItemIFace nbi = null;
        if (reportsList.size() == 1)
        {
            nbi = reportsList.get(0);

        } else
        {
            ChooseFromListDlg<NavBoxItemIFace> dlg = new ChooseFromListDlg<NavBoxItemIFace>((Frame)UIRegistry.getTopWindow(),
                                                                                            getResourceString("ChooseLabel"),
                                                                                            reportsList, 
                                                                                            IconManager.getIcon(name, IconManager.IconSize.Std24));
            dlg.setMultiSelect(false);
            dlg.setModal(true);
            dlg.setVisible(true);
            if (!dlg.isCancelled())
            {
                nbi  = dlg.getSelectedObject();
            }
        }
        
        if (nbi != null && nbi.getData() != null)
        {
            Object data = nbi.getData();
            if (data instanceof CommandAction)
            {
                return ((CommandAction)data).getPropertyAsString("file");
            }
        }
        return null;
    }

    /**
     * Single place to convert the data to a Map.
     * @param data the data in a nbi
     * @return a Properties
     */
    protected Properties convertDataToMap(final Object data)
    {
        if (data instanceof Map<?,?>)
        {
            return (Properties)data; // ok to Cast
        }
        throw new RuntimeException("Why isn't the data a Properties!");
    }

    /**
     * Counts up how many labels match the same table id as the RecordSet and sets
     * oneNbi to the non-null match which means if there is only one then it points to it.
     * @param tableId the RecordSet's Table Id
     * @param needsRecordSet indicates we should ONLY include those that require a RecordSet 
     * @return the count of matches
     */
    protected int countLabelsWithSimilarTableIds(final int tableId, final boolean needsRecordSet)
    {
        oneNbi = null;
        int count = 0;
        for (NavBoxItemIFace nbi : reportsList)
        {
            Object data = nbi.getData();
            if (data instanceof CommandAction)
            {
                CommandAction cmdAction = (CommandAction)data;
                String              tableIDStr = cmdAction.getPropertyAsString("tableid");
                boolean             needsRS    = getNeedsRecordSet(cmdAction.getPropertyAsString("reqrs"));
                if (StringUtils.isNumeric(tableIDStr) && (!needsRecordSet ||  needsRS))
                {
                    if (Integer.parseInt(tableIDStr) == tableId)
                    {
                        oneNbi = nbi;
                        count++;
                    }
                } else
                {
                    log.error("Attr [tableid] value["+tableIDStr+"] is not numeric for["+nbi.getTitle()+"]!");
                }
            }
        }
        return count;
    }

    /**
     * Checks to make sure we are the current SubPane and then creates the labels from the selected RecordSet.
     * @param data the data that "should" be a RecordSet
     */
    protected void createLabelFromSelectedRecordSet(final Object data)
    {
        if (data instanceof RecordSet && ContextMgr.getCurrentContext() == this)
        {
            RecordSetIFace rs = (RecordSetIFace)data;

            String fileName = null;
            if (countLabelsWithSimilarTableIds(rs.getDbTableId(), true) > 1) // only Count the ones that require data
            {
                fileName = askForLabelName();
                
            } else if (oneNbi != null)
            {
                Object nbData = oneNbi.getData();
                if (nbData instanceof CommandAction)
                {
                    fileName = ((CommandAction)nbData).getPropertyAsString("file");
                }
            }

            if (fileName != null)
            {
                doLabels(fileName, "Labels", data, null, this, null);
            }
        }
    }

    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#getNavBoxes()
     */
    @Override
    public java.util.List<NavBoxIFace> getNavBoxes()
    {
        initialize();

        extendedNavBoxes.clear();
        extendedNavBoxes.addAll(navBoxes);

        RecordSetTask rsTask = (RecordSetTask)ContextMgr.getTaskByClass(RecordSetTask.class);
        List<NavBoxIFace> nbs = rsTask.getNavBoxes();
        if (nbs != null)
        {
            extendedNavBoxes.addAll(nbs);
        }
        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        toolbarItems = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String localIconName = name;
        String hint = getResourceString("labels_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, localIconName, hint);

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


    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    /**
     * Processes all Commands of type RECORD_SET.
     * @param cmdAction the command to be processed
     */
    protected void processRecordSetCommands(final CommandAction cmdAction)
    {
        Object data = cmdAction.getData();
        UsageTracker.incrUsageCount("RP."+cmdAction.getType()+(data != null ? ("."+data.getClass().getSimpleName()) : ""));

        if (cmdAction.isAction("Clicked"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            
            log.debug("********* In Labels doCommand src["+srcObj+"] dst["+dstObj+"] data["+data+"] context["+ContextMgr.getCurrentContext()+"]");
             
            if (ContextMgr.getCurrentContext() == this)
            {
                createLabelFromSelectedRecordSet(srcObj);
            }
        }
    }
    
    /**
     * Prints a Label / Reports from a command
     * @param cmdAction the command
     */
    protected void printReport(final CommandAction cmdAction)
    {
        // Get the original set of params
        Properties params = cmdAction.getProperties();
        if (params == null)
        {
            params = new Properties();
        }
        
        // No add the additional params
        String paramList = cmdAction.getPropertyAsString("params");
        if (StringUtils.isNotEmpty(paramList))
        {
            params.putAll(UIHelper.parseProperties(paramList));
        }
        
        String impStr = params.getProperty("isimport");
        String rsDropStr = params.getProperty("hasrsdropparam");
        RecordSet spRep = (RecordSet )params.get("spreport");
        if (spRep != null)
        {
        	//SpReport was clicked. Just Run it
        	CommandAction runner = new CommandAction(REPORTS, RUN_REPORT, cmdAction);
        	CommandDispatcher.dispatch(runner);
        	return;
        }
        
        boolean canRunWithoutRS = spRep != null || 
        	(impStr != null && impStr.equals("1") && rsDropStr != null && rsDropStr.equals("0"));
        if (cmdAction.getData() instanceof RecordSetIFace || canRunWithoutRS)
        {
            RecordSetIFace recordSet = cmdAction.getData() instanceof RecordSetIFace ? (RecordSetIFace)cmdAction.getData() : null;
            
            // XXX For the Demo and until I revist a generalized way of associating a default set of reports and labels
            // to To things. One way to get here with a null title is to click on the Labels btn from the search results
            if (recordSet != null && recordSet.getDbTableId() != null && recordSet.getDbTableId() == 1 && cmdAction.getPropertyAsString("title") == null)
            {
                cmdAction.setProperty("file", "fish_label.jrxml");
                cmdAction.setProperty("title", "Fish Labels");
                
            } else if (recordSet != null && recordSet.getDbTableId() != null && recordSet.getDbTableId() == 52 && cmdAction.getPropertyAsString("title") == null)
            {
                // XXX For the Demo and until I revist a generalized way of associating a default set of reports and labels
                // to To things. One way to get here with a null title is to click on the Labels btn from the search results
                cmdAction.setProperty("file",  "LoanInvoice.jrxml");
                cmdAction.setProperty("title", "Loan Invoice");
            }


            if (recordSet == null || checkForALotOfLabels(recordSet))
            {
                String labelFileName = cmdAction.getPropertyAsString("file");
                
                if (StringUtils.isEmpty(labelFileName))
                {
                    labelFileName = askForLabelName();
                }
                
                if (StringUtils.isNotEmpty(labelFileName))
                {
                    Taskable originatingTask = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
                    //System.out.println("************** "+(new File("/home/rods/Specify/reportsCache_Linux/fish_label.jrxml").lastModified()));
                    doLabels(labelFileName, cmdAction.getPropertyAsString("title"), recordSet, params, originatingTask == null ? this : originatingTask, (ImageIcon)cmdAction.getProperty("icon"));
                }
            }
            
        } else if (cmdAction.getData() instanceof JRDataSource)
        {
            String labelFileName = cmdAction.getPropertyAsString("file");
            
            if (StringUtils.isEmpty(labelFileName))
            {
                labelFileName = askForLabelName();
            }
            
            if (StringUtils.isNotEmpty(labelFileName))
            {
                Taskable originatingTask = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
                doLabels(labelFileName, cmdAction.getPropertyAsString("title"), cmdAction.getData(), params, originatingTask, (ImageIcon)cmdAction.getProperty("icon"));
            }
            
            
        } else
        {
            String tableIDStr = cmdAction.getPropertyAsString("tableid");
            if (StringUtils.isNotEmpty(tableIDStr) && StringUtils.isNumeric(tableIDStr))
            {
                RecordSetIFace recordSet = RecordSetTask.askForRecordSet(Integer.parseInt(tableIDStr));
                if (recordSet != null)
                {
                    doLabels(cmdAction.getPropertyAsString("file"), cmdAction.getPropertyAsString("title"), recordSet, params, this, (ImageIcon)cmdAction.getProperty("icon"));
                }
                else
                {
                	UIRegistry.displayLocalizedStatusBarText("RecordSetTask.NoRecordsets");
                }
            }
            
        }

    }
    
    /**
     * Processes all Commands of type LABELS.
     * @param cmdAction the command to be processed
     */
    protected void processReportCommands(final CommandAction cmdAction)
    {
        
        //---------------------------------------------------------------------------
        // This Code here needs to be refactored and moved to the NavBoxAction
        // so it can happen in a single generic place (Each task has this code)
        //---------------------------------------------------------------------------
        /*if (cmdAction.getData() instanceof RecordSetIFace)
        {
            if (((RecordSetIFace)cmdAction.getData()).getDbTableId() != cmdAction.getTableId())
            {
                JOptionPane.showMessageDialog(null, getResourceString("ERROR_RECORDSET_TABLEID"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
        }*/
        
        Object data = cmdAction.getData();
        UsageTracker.incrUsageCount("RP."+cmdAction.getType()+(data != null ? ("."+data.getClass().getSimpleName()) : ""));
           
        if (cmdAction.isAction(NEWRECORDSET_ACTION))
        {
            if (cmdAction.getData() instanceof GhostActionable)
            {
                GhostActionable ga = (GhostActionable) cmdAction.getData();
                GhostMouseInputAdapter gpa = ga.getMouseInputAdapter();

                for (NavBoxItemIFace nbi : reportsList)
                {
                    if (nbi instanceof GhostActionable)
                    {
                        gpa.addGhostDropListener(new GhostActionableDropManager(UIRegistry
                                .getGlassPane(), nbi.getUIComponent(), ga));
                    }
                }
            }
        }
        else if (cmdAction.isAction(PRINT_REPORT) )
        {
            if (data instanceof CommandAction && ((CommandAction)data).isAction(RUN_REPORT))
            {
                runReport(cmdAction);
            }
//            if (cmdAction.getData() instanceof CommandAction && ((CommandAction)cmdAction.getData()).isAction(OPEN_EDITOR))
//            {
//                openIReportEditor(cmdAction);
//            }
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                RecordSetIFace rs = (RecordSetIFace)cmdAction.getData();
                if (rs.getDbTableId() != null && rs.getDbTableId() == SpReport.getClassTableId() || cmdAction.getProperty("spreport") != null)
                {
                    runReport(cmdAction);
                }
                else
                {
                    printReport(cmdAction);
                }
            }
            else 
            {
                if (data instanceof CommandAction && ((CommandAction )data).isAction(PRINT_REPORT))
                {
                    printReport((CommandAction )data);
                }
                else
                {
                    printReport(cmdAction);
                }
            }
        }
//        else if (cmdAction.isAction(OPEN_EDITOR))
//        {
//            openIReportEditor(cmdAction);
//        }
        else if (cmdAction.isAction(RUN_REPORT))
        {
            boolean doRun = true;
            if (data instanceof CommandAction && ((CommandAction )data).isAction(PRINT_REPORT))
            {
                doRun = ((CommandAction )data).getProperty("spreport") != null;
            }
            if (doRun)
            {
                runReport(cmdAction);
            }
            else
            {
                printReport((CommandAction )data);
            }
        }
        else if (cmdAction.isAction(REFRESH))
        {
            refreshCommands();
            
        } else if (cmdAction.isAction(IMPORT))
        {
            importReport();
            
        } else if (cmdAction.isAction(PRINT_GRID))
        {
            printGrid(cmdAction);
            
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
            if (recordSet != null || cmdAction.getProperties().get("name") != null)
            {
                String theName;
                String theTitle;
                if (recordSet != null)
                {
                    theName = recordSet.getName();
                    theTitle = theName;
                }
                else
                {
                    theName = cmdAction.getProperties().getProperty("name");
                    //currently the description of the appResource is used as the 'title' for the command button.
                    theTitle = ((AppResourceIFace)cmdAction.getProperties().get("appresource")).getDescription();
                }
                int option = JOptionPane.showOptionDialog(UIRegistry.getMostRecentWindow(), 
                        String.format(UIRegistry.getResourceString("REP_CONFIRM_DELETE"), theName),
                        UIRegistry.getResourceString("REP_CONFIRM_DELETE_TITLE"), 
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, JOptionPane.NO_OPTION); // I18N
                
                if (option == JOptionPane.YES_OPTION)
                {
                    SpAppResource res = (SpAppResource)cmdAction.getProperty("appresource");
                    Integer resOrRepId = null;
                    Integer repId = null;
                    if (res != null)
                    {
                        resOrRepId = res.getId();
                    }
                    else
                    {
                        RecordSetItemIFace item = recordSet.getOnlyItem();
                        if (item != null)
                        {
                            repId = item.getRecordId();
                            resOrRepId = repId;
                        }
                    }
                    deleteReportAndResource(repId, (AppResourceIFace)cmdAction.getProperty("appresource"));
                    deleteReportFromUI(theTitle);
                    if (resOrRepId != null)
                    {
                        CommandDispatcher.dispatch(new CommandAction(REPORTS, REPORT_DELETED, resOrRepId));
                    }
                    else
                    {
                        //what can you do?
                    }
                }
            }
        }
    }
    
    /**
     * Prints a JTable's model as a Grid Report.
     * 
     * @param cmdAction the command holding the actual JTable object
     */
    protected void printGrid(final CommandAction cmdAction)
    {
        String pageTitle = cmdAction.getProperty("gridtitle").toString();
        
        final PageSetupDlg pageSetup = new PageSetupDlg();
        pageSetup.createUI();
        pageSetup.setPageTitle(pageTitle);
        
        pageSetup.setVisible(true);
        if (pageSetup.isCancelled())
        {
            return;
        }
        
        SwingWorker<Integer, Integer> backupWorker = new SwingWorker<Integer, Integer>()
        {
            protected Exception excpt = null;
            
            /* (non-Javadoc)
             * @see javax.swing.SwingWorker#doInBackground()
             */
            @Override
            protected Integer doInBackground() throws Exception
            {
                JTable table = (JTable)cmdAction.getProperty("jtable");
                if (table != null)
                {
                    try
                    {
                        LabelsPane labelsPane = new LabelsPane(name, ReportsBaseTask.this, null);
                        DynamicReport dr = buildReport(table.getModel(), pageSetup);

                        JRDataSource ds = new JRTableModelDataSource(table.getModel());
                        JasperPrint  jp = DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds);
                        
                        labelsPane.reportFinished(jp);
                        SubPaneMgr.getInstance().addPane(labelsPane);
                        
                    } catch (Exception ex)
                    {
                        excpt = ex;
                        ex.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void done()
            {
                super.done();
                
                UIRegistry.getStatusBar().setProgressDone(PRINT_GRID);
                
                UIRegistry.clearSimpleGlassPaneMsg();
                
                if (excpt != null)
                {
                    UIRegistry.getStatusBar().setErrorMessage("REP_ERR_GRDRPT", excpt);
                }
            }
        };
        
        final JStatusBar statusBar = UIRegistry.getStatusBar();
        statusBar.setIndeterminate(PRINT_GRID, true);
        
        UIRegistry.writeSimpleGlassPaneMsg(getLocalizedMessage("REP_GRID_RPT", cmdAction.getProperty("gridtitle").toString()), 24);
        
        backupWorker.execute();
        
    }
    
    /**
     * @param model
     * @return
     * @throws Exception
     */
    public DynamicReport buildReport(final TableModel model, final PageSetupDlg pageSetupDlg) throws Exception
    {
        /**
         * Creates the DynamicReportBuilder and sets the basic options for the report
         */
        FastReportBuilder drb = new FastReportBuilder();
        
        Style columDetail = new Style();
        columDetail.setBorder(Border.THIN);
        
        Style columDetailWhite = new Style();
        columDetailWhite.setBorder(Border.THIN);
        columDetailWhite.setBackgroundColor(Color.WHITE);
        columDetailWhite.setFont(Font.ARIAL_SMALL);
        columDetailWhite.setHorizontalAlign(HorizontalAlign.CENTER);
        columDetailWhite.setBlankWhenNull(true);
        
        Style columDetailWhiteBold = new Style();
        columDetailWhiteBold.setBorder(Border.THIN);
        columDetailWhiteBold.setBackgroundColor(Color.WHITE);
        
        Style titleStyle = new Style();
        titleStyle.setFont(new Font(14, Font._FONT_VERDANA, true));
        
        /*Style numberStyle = new Style();
        numberStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        
        Style amountStyle = new Style();
        amountStyle.setHorizontalAlign(HorizontalAlign.RIGHT);
        amountStyle.setBackgroundColor(Color.cyan);
        amountStyle.setTransparency(Transparency.OPAQUE);*/
        
        //Font dataRowFont = new Font(10, Font._FONT_VERDANA, true);

        // Odd Row Style
        Style oddRowStyle = new Style();
        oddRowStyle.setBorder(Border.NO_BORDER);
        //oddRowStyle.setFont(dataRowFont);
        oddRowStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        
        Color veryLightGrey = new Color(240, 240, 240);
        oddRowStyle.setBackgroundColor(veryLightGrey);
        oddRowStyle.setTransparency(Transparency.OPAQUE);

        // Event Row Style
        //Style evenRowStyle = new Style();
        //evenRowStyle.setBorder(Border.NO_BORDER);
        //evenRowStyle.setFont(dataRowFont);
        
        // Create Column Headers for the Report
        for (int i=0;i<model.getColumnCount();i++)
        {
            String colName = model.getColumnName(i);
            
            Class<?> dataClass = model.getColumnClass(i);
            if (dataClass == Object.class)
            {
                if (model.getRowCount() > 0)
                {
                    Object data = model.getValueAt(0, i);
                    dataClass = data.getClass();
                }
            }
            
            ColumnBuilder colBldr = ColumnBuilder.getInstance().setColumnProperty(colName, dataClass.getName());
            int bracketInx = colName.indexOf('[');
            if (bracketInx > -1)
            {
                colName = colName.substring(0, bracketInx-1);
            }
            colBldr.setTitle(colName);
            //colBldr.setWidth(new Integer(100));
            
            colBldr.setStyle(columDetailWhite);
            //colBldr.setHeaderStyle(columDetailWhite);
            
            AbstractColumn column = colBldr.build();
            drb.addColumn(column);
            
            Style headerStyle = new Style();
            headerStyle.setFont(new Font(12, Font._FONT_ARIAL, true));
            headerStyle.setBorder(Border.THIN);
            headerStyle.setHorizontalAlign(HorizontalAlign.CENTER);
            headerStyle.setVerticalAlign(VerticalAlign.MIDDLE);
            headerStyle.setBackgroundColor(new Color(80, 80, 80));
            headerStyle.setTransparency(Transparency.OPAQUE);
            headerStyle.setTextColor(new Color(255, 255, 255));
            column.setHeaderStyle(headerStyle);
        }
        
        drb.setTitle(pageSetupDlg.getPageTitle());
        drb.setTitleStyle(titleStyle);
        //drb.setTitleHeight(new Integer(30));
        //drb.setSubtitleHeight(new Integer(20));
        //drb.setDetailHeight(new Integer(15));
        //drb.setDefaultStyles(null, null, null, evenRowStyle);
        
        drb.setLeftMargin(20);
        drb.setRightMargin(20);
        drb.setTopMargin(10);
        drb.setBottomMargin(10);
        
        drb.setPrintBackgroundOnOddRows(true);
        drb.setOddRowBackgroundStyle(oddRowStyle);
        drb.setColumnsPerPage(new Integer(1));
        drb.setUseFullPageWidth(true);
        drb.setColumnSpace(new Integer(5));
        drb.addAutoText(AutoText.AUTOTEXT_PAGE_X_OF_Y, AutoText.POSITION_FOOTER, AutoText.ALIGMENT_CENTER);
        
        Page[] pageSizes = new Page[] {Page.Page_Letter_Portrait(), Page.Page_Legal_Portrait(), Page.Page_A4_Portrait(),
                                       Page.Page_Letter_Landscape(), Page.Page_Legal_Landscape(), Page.Page_A4_Landscape()};
        int pageSizeInx = pageSetupDlg.getPageSize() + (pageSetupDlg.isPortrait() ? 0 : 3);
        drb.setPageSizeAndOrientation(pageSizes[pageSizeInx]);
        
        DynamicReport dr = drb.build();
        
        return dr;
    }
    
    /**
     * @param reportId
     * @param appRes
     */
    public static void deleteReportAndResource(final Integer reportId, final AppResourceIFace appRes)
    {
        Integer resourceId = null;

        if (reportId == null)
        {
            resourceId = ((SpAppResource) appRes).getId();
        }
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
		boolean transOpen = false;
		try 
		{
			String hql = reportId != null ? "from SpReport where id = "  + reportId : 
				"from SpReport where appResourceId = " + resourceId;
			SpReport rep = (SpReport) session.getData(hql);
			SpAppResource repRes = null;
			if (rep != null) 
			{
	        	repRes = rep.getAppResource();
//				((SpecifyAppContextMgr) AppContextMgr.getInstance()).removeAppResourceSp(rep.getAppResource()
//	                    .getSpAppResourceDir(), rep.getAppResource());
			}
			else
			{
				hql = "from SpAppResource where id = " + resourceId;
				repRes = (SpAppResource)session.getData(hql);
			}
			if (repRes != null) 
			{
				((SpecifyAppContextMgr) AppContextMgr.getInstance()).removeAppResourceSp(repRes
	                    .getSpAppResourceDir(), repRes);
			}
		} catch (Exception e) 
		{
			UsageTracker.incrHandledUsageCount();
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(ReportsBaseTask.class, e);
			if (transOpen) 
			{
				session.rollback();
			}
			e.printStackTrace();
		} finally 
		{
			session.close();
		}
    }
    
    /**
	 * @param recordSets
	 */
    protected void deleteReportFromUI(final String btnName)
    {
        Pair<NavBoxIFace, NavBoxItemIFace> btn = findDnDBtn(btnName);
        if (btn != null)
        {
            reportsList.remove(btn.getSecond());
            deleteDnDBtn(btn.getFirst(), btn.getSecond());
        }
    }
    
    protected void refreshCommands()
    {
        commands.clear();
        reportsList.clear();
        reportsNavBox.clear();
        labelsNavBox.clear();
        addROCs();

        //The rest of this was copied from RecordSetTask.saveNewRecordSet
        
        // XXX this is pathetic and needs to be generized
        reportsNavBox.invalidate();
        reportsNavBox.setSize(reportsNavBox.getPreferredSize());
        reportsNavBox.doLayout();
        reportsNavBox.repaint();
        labelsNavBox.invalidate();
        labelsNavBox.setSize(reportsNavBox.getPreferredSize());
        labelsNavBox.doLayout();
        labelsNavBox.repaint();
        
        NavBoxMgr.getInstance().invalidate();
        NavBoxMgr.getInstance().doLayout();
        NavBoxMgr.getInstance().repaint();
        UIRegistry.forceTopFrameRepaint();
    }
           
    /**
     * @param cmd
     * @return a fully-loaded SpReport
     */
    protected SpReport loadReport(final RecordSet repRS)
    {
        SpReport result = null;
        if (repRS != null && repRS.getDbTableId() == SpReport.getClassTableId())
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                result = session.get(SpReport.class, repRS.getOrderedItems().iterator().next().getRecordId());
                result.forceLoad();
            }
            finally
            {
                session.close();
            }
        } else
        {
            UIRegistry.showError("RecordSet was NULL or not of type report!");
        }
        return result;
    }
    
    /**
     * @param helpContext
     * @return a fully-loaded SpReport of the User's choosing.
     */
    protected NavBoxItemIFace selectReport(final String helpContext)
    {
    	List<NavBoxItemIFace> options = this.reportsList;
        if (options.size() == 0)
        {
            UIRegistry.getStatusBar().setText(getResourceString("NO_RUNNABLE_REPORTS"));
            return null;
        }
        ChooseFromListDlg<NavBoxItemIFace> dlg = new ChooseFromListDlg<NavBoxItemIFace>((Frame)UIRegistry.get(UIRegistry.FRAME),
                getResourceString("REP_CHOOSE_REPORT"), 
                null, 
                ChooseFromListDlg.OKCANCELHELP, 
                options, 
                helpContext);
    
        dlg.setModal(true);
        UIHelper.centerAndShow(dlg);
        NavBoxItemIFace selection = dlg.getSelectedObject();
        if (!dlg.isCancelled() && selection != null)
        {
        	return selection;
        }
        return null;
    }
    
    protected void importReport()
    {
        FileDialog fileDialog = new FileDialog((Frame)UIRegistry.get(UIRegistry.FRAME), 
                getResourceString("CHOOSE_WORKBENCH_IMPORT_FILE"), 
                FileDialog.LOAD);
        //Really shouldn't override workbench prefs with report stuff???
        fileDialog.setDirectory(WorkbenchTask.getDefaultDirPath(WorkbenchTask.IMPORT_FILE_PATH));
        fileDialog.setFilenameFilter(new java.io.FilenameFilter()
        {
            public boolean accept(File dir, String filename)
            {
                return FilenameUtils.getExtension(filename).equalsIgnoreCase("jrxml");
            }

        });
        UIHelper.centerAndShow(fileDialog);
        fileDialog.dispose();

        String fileName = fileDialog.getFile();
        String path     = fileDialog.getDirectory();
        if (StringUtils.isNotEmpty(path))
        {
            AppPreferences localPrefs = AppPreferences.getLocalPrefs();
            localPrefs.put(WorkbenchTask.IMPORT_FILE_PATH, path);
        }

        File file;
        if (StringUtils.isNotEmpty(fileName) && StringUtils.isNotEmpty(path))
        {
            file = new File(path + File.separator + fileName);
        } else
        {
            return;
        }

        if (file.exists())
        {
            if (MainFrameSpecify.importJasperReport(file, true))
            {
                refreshCommands();
            }
            //else -- assume feedback during importJasperReport()
        }
    }
    /**
     * @param cmdAction
     */
    protected void runReport(final CommandAction cmdAction)
    {
        NavBoxItemIFace toRun = null;
        SpReport spRepToRun = null;
        CommandAction runAction = null;
        CommandAction repAction = null;
        RecordSetIFace rs = null;
        if (cmdAction.isAction(RUN_REPORT)) //RunReport was clicked or dropped on
        {
            runAction = cmdAction;
            Object data = cmdAction.getData();
            if (data instanceof CommandAction && ((CommandAction)data).isAction(PRINT_REPORT))
            {
                repAction = (CommandAction)data;
            }
        }
        else if (cmdAction.isAction(PRINT_REPORT))//Report was dropped upon
        {
            repAction = cmdAction;
            Object data = cmdAction.getData();
            if (data instanceof CommandAction && ((CommandAction)data).isAction(RUN_REPORT))
            {
                runAction = (CommandAction)data;
            }
            else if (data instanceof RecordSetIFace)
            {
                rs = (RecordSetIFace)data;
            }
        }
        
        if (runAction != null && repAction == null)
        {
            toRun = selectReport("ReportRunReport"); //XXX add help
            CommandAction cmd = (CommandAction)toRun.getData();
            if (cmd.getProperty("spreport") != null)
            {
            	RecordSet repRS = (RecordSet)cmd.getProperty("spreport") ;
            	spRepToRun = loadReport(repRS);
            }
            
        }
        else if ((runAction != null || rs != null) && repAction != null)
        {
            spRepToRun = loadReport((RecordSet)repAction.getProperty("spreport"));
        }
        
        if (spRepToRun != null)
        {
        	if (spRepToRun.getReportObject() instanceof WorkbenchTemplate)
        	{
        		//get workbench
        		WorkbenchTemplate repTemplate = (WorkbenchTemplate )spRepToRun.getReportObject();
                DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                try
                {
                	List<Workbench> wbs = session.getDataList(Workbench.class, "specifyUser", 
                			AppContextMgr.getInstance().getClassObject(SpecifyUser.class), 
                			DataProviderSessionIFace.CompareType.Equals); 
                	Vector<Workbench> choices = new Vector<Workbench>();
                	for (Workbench wb : wbs)
                	{
                		if (repTemplate.containsAllMappings(wb.getWorkbenchTemplate()))
                		{
                			choices.add(wb);
                		}
                	}
                	if (choices.size() == 0)
                	{
                		UIRegistry.displayInfoMsgDlgLocalized("ReportsBaseTask.NO_WBS_FOR_REPORT", spRepToRun.getName());
                		return;
                	}
                	Workbench repWB = null;
                	if (choices.size() == 1)
                	{
                		repWB = choices.get(0);
                	}
                	else
                	{
                		ChooseFromListDlg<Workbench> wbDlg = new ChooseFromListDlg<Workbench>((Frame )UIRegistry.getTopWindow(),
                        	getResourceString("ReportsBaseTesk.SELECT_WB"),
                        	choices, 
                        	IconManager.getIcon(name, IconManager.IconSize.Std24));
                		UIHelper.centerAndShow(wbDlg);
                		repWB = wbDlg.getSelectedObject();
                		if (wbDlg.isCancelled() || repWB == null)
                		{
                			return;
                		}
                	}
                    RecordSet wbRS  = new RecordSet();
                    wbRS.initialize();
                    wbRS.set(repWB.getName(), Workbench.getClassTableId(), RecordSet.GLOBAL);
                    wbRS.addItem(repWB.getId());
                    rs = wbRS;
                }
                finally
                {
                	session.close();
                }
        	}
        	QueryBldrPane.runReport(spRepToRun, spRepToRun.getName(), rs);
        }
        else if (toRun != null)
        {
        	CommandDispatcher.dispatch((CommandAction)toRun.getData());
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
            CommandDispatcher.unregister(REPORTS, this);
            CommandDispatcher.unregister(RecordSetTask.RECORD_SET, this);
            
            if (isInitialized)
            {
                reportsList.clear();
                reportsNavBox.clear();
                labelsNavBox.clear();
                isInitialized = false;
            }
            this.initialize();
            CommandDispatcher.register(REPORTS, this);
            CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        super.doCommand(cmdAction);
        
        /*if (true)
        {
            log.debug("Direct Properties:");
            dumpProps(cmdAction.getProperties(), 0);
        }*/
        
        //log.debug("*************** "+this);
        if (cmdAction.isType(REPORTS))
        {
            processReportCommands(cmdAction);
            
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET))
        {
            processRecordSetCommands(cmdAction);
            
        }
    }
    
    
    /**
     * Returns the boolean value of "reqrs" from the metaData and true if it doesn't exist.
     * @param needsRSStr the string value of the map
     * @return true or false
     */
    protected boolean getNeedsRecordSet(final String needsRSStr)
    {
        boolean needsRS = true;
        if (StringUtils.isNotEmpty(needsRSStr) && StringUtils.isAlpha(needsRSStr))
        {
            needsRS = Boolean.parseBoolean(needsRSStr);
        } 
        return needsRS;
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermissionName()
     */
    @Override
    public String getPermissionName()
    {
        //This is required(?) because the Reports and Labels tasks are currently merged. 
        return REPORTS;
    }

    /**
     * @return the permissions array
     */
    @Override
    protected boolean[][] getPermsArray()
    {
        return new boolean[][] {{true, true, true, true},
                                {true, true, true, true},
                                {true, true, false, false},
                                {true, false, false, false}};
    }

    /**
     * @param repResource
     * @returns true if repResource is currently available from the AppContextMgr.
     */
    protected boolean repContextIsActive(final AppResourceIFace repResource)
    {
        AppResourceIFace match = AppContextMgr.getInstance().getResource(repResource.getName());
        //XXX - Is it really safe to assume there there won't be more than one resource with the same name and mimeType?
        return match != null && match.getMimeType().equalsIgnoreCase(repResource.getMimeType());
    }

    /**
     * Builds list of reports available for tableid.
     */
   protected SortedSet<SearchResultReportServiceInfo> getReports(int tableId, final QueryBldrPane queryBuilder)
    {
    	SortedSet<SearchResultReportServiceInfo> reports = new TreeSet<SearchResultReportServiceInfo>(
                new Comparator<SearchResultReportServiceInfo>()
                {
                    public int compare(SearchResultReportServiceInfo o1,
                                       SearchResultReportServiceInfo o2)
                    {
                        return o1.getReportName().compareTo(o2.getReportName());
                    }

                });
        boolean buildEm = true;
        if (AppContextMgr.isSecurityOn())
        {
            Taskable reportsTask = ContextMgr.getTaskByClass(ReportsTask.class);
            if (reportsTask != null)
            {
                buildEm = reportsTask.getPermissions().canView();
            }
        }
        if (buildEm)
        {
            //first add reports associated directly with the results
            if (queryBuilder != null)
            {
                for (SpReport rep : queryBuilder.getReportsForQuery())
                {
                    if (repContextIsActive(rep.getAppResource()))
                    {
                        reports.add(new SearchResultReportServiceInfo(rep.getName(), rep.getName(), true, null, rep.getAppResource().getId(), rep.getRepeats()));
                    }
                }
            }
        
            if (tableId != -1)
            {
                //second add reports associated with the same table as the current results
                List<AppResourceIFace> reps = AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.LABELS_MIME);
                reps.addAll(AppContextMgr.getInstance().getResourceByMimeType(ReportsBaseTask.REPORTS_MIME));
                for (AppResourceIFace rep : reps)
                {
                    String tblIdStr = rep.getMetaData("tableid");
                    if (tblIdStr != null)
                    {
                        if (tableId == Integer.valueOf(tblIdStr))
                        {
                            reports.add(new SearchResultReportServiceInfo(rep.getDescription() /*'title' seems to be currently stored in description */,
                                    rep.getName() /* and filename in name */, false, null, ((SpAppResource)rep).getId(), null));
                        }
                        else
                        {
                            //third add reports based on other queries...
                            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
                            try
                            {
                                SpReport spRep = (SpReport)session.getData("from SpReport where appResourceId = " + ((SpAppResource)rep).getId());
                                if (spRep != null && spRep.getQuery() != null && spRep.getQuery().getContextTableId().intValue() == tableId)
                                {
                                    reports
                                            .add(new SearchResultReportServiceInfo(
                                                rep.getDescription() /*
                                                                         * 'title' seems to be
                                                                         * currently stored in
                                                                         * description
                                                                         */,
                                                rep.getName() /* and filename in name */, false, spRep.getId(),
                                                ((SpAppResource)rep).getId(), null));
                                }
                            }
                            finally
                            {
                                session.close();
                            }
                        }
                    }
                }
            }
        }
        return reports;
    }

   //XXX might be good to register and un-register services as reports are added/deleted???
   
// public synchronized void reportDeleted(final Integer resourceId)
// {
//     for (SearchResultReportServiceInfo repInfo : reports)
//     {
//         if (repInfo.getResourceId() != null && repInfo.getResourceId().equals(resourceId))
//         {
//             reports.remove(repInfo);
//             break;
//         }
//     }
// }
  

}
