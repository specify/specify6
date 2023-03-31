/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.auth.BasicPermisionPanel;
import edu.ku.brc.af.auth.PermissionEditorIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskMgr;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.prefs.PreferencesDlg;
import edu.ku.brc.af.tasks.subpane.HtmlDescPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.rstools.RecordSetToolsIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import edu.ku.brc.specify.ui.db.ResultSetTableModel;

/**
 * A task to handle RecordSet data exporting.  This task provides a pluggable
 * interface by which new export formats can be added.
 *
 * @code_status Alpha
 * 
 * @author jstewart
 */
@SuppressWarnings("serial")
public class PluginsTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(PluginsTask.class);
            
    // Static Data Members
    public static final DataFlavor TOOLS_FLAVOR = new DataFlavor(PluginsTask.class, "Plugins");
    
    private static final String ON_TASKBAR = "Exporttask.OnTaskbar";
    
    public static final String GE_BALLOON_FG_COLOR     = "google.earth.fgcolor";
    public static final String GE_BALLOON_FG_COLOR_STR = "255, 255, 255";
    
    public static final String GE_BALLOON_BG_COLOR     = "google.earth.bgcolor";
    public static final String GE_BALLOON_BG_COLOR_STR = "0, 102, 179";
    
    
    public static final String GE_BALLOON_PRIMARY_URL       = "google.earth.primaryurl";
    public static final String GE_BALLOON_PRIMARY_URL_TITLE = "google.earth.primaryurltitle";
    
    public static final String GE_BALLOON_SECONDARY_URL       = "google.earth.secondaryurl";
    public static final String GE_BALLOON_SECONDARY_URL_TITLE = "google.earth.secondaryurltitle";
    
    public static String GE_BALLOON_PRIMARY_URL_STR   = "";//http://www.fishbase.org/Summary/speciesSummary.php?genusname=%s&speciesname=%s";
    public static String GE_BALLOON_SECONDARY_URL_STR = "";//http://animaldiversity.ummz.umich.edu/site/accounts/information/%s-%s";

    public static String GE_BALLOON_PRIMARY_URL_TITLE_STR   = "fb";
    public static String GE_BALLOON_SECONDARY_URL_TITLE_STR = "ad";

    public static final String PLUGINS = "Plugins";

    public static final String EXPORT_RS     = "ExportRecordSet";
    public static final String EXPORT_LIST   = "ExportList";
    public static final String EXPORT_JTABLE = "ExportJTable";

    // Data Members
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    
    /**
     * A {@link Vector} or the registered export formats/targets.
     */
    protected Vector<NavBoxItemIFace> toolsNavBoxList    = new Vector<NavBoxItemIFace>();

    protected List<Pair<Class<? extends RecordSetToolsIFace>, Boolean>> toolsRegistryList = new Vector<>();
    
    protected List<Pair<RecordSetToolsIFace, Boolean>> loadedToolsList = new Vector<>();
    
    /**
     * Constructor.
     */
    public PluginsTask()
    {
        super(PLUGINS, getResourceString("Plugins"));
        
        CommandDispatcher.register(PLUGINS, this);
        CommandDispatcher.register(PreferencesDlg.PREFERENCES, this);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#isSingletonPane()
     */
    public boolean isSingletonPane()
    {
        return true;
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
            
            AppPreferences remotePrefs = AppPreferences.getRemote();
            
            //if (remotePrefs.get(GE_BALLOON_FG_COLOR, null) == null)
            {
                remotePrefs.put(GE_BALLOON_FG_COLOR, GE_BALLOON_FG_COLOR_STR);
            }
            
            //if (remotePrefs.get(GE_BALLOON_BG_COLOR, null) == null)
            {
                remotePrefs.put(GE_BALLOON_BG_COLOR, GE_BALLOON_BG_COLOR_STR);
            }
            
            String primaryURL       = remotePrefs.get(GE_BALLOON_PRIMARY_URL, null);
            String primaryURLTitle  = remotePrefs.get(GE_BALLOON_PRIMARY_URL_TITLE, null);
            if (StringUtils.isEmpty(primaryURL))
            {
                //if (Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish))
                {
                    remotePrefs.put(GE_BALLOON_PRIMARY_URL, GE_BALLOON_PRIMARY_URL_STR);
                    if (StringUtils.isEmpty(primaryURLTitle))
                    {
                        remotePrefs.put(GE_BALLOON_PRIMARY_URL_TITLE, GE_BALLOON_PRIMARY_URL_TITLE_STR);
                    }
                }
            }
            
            // XXX Need to figure out the Disciplines the UMICH supports
            String secondaryURL       = remotePrefs.get(GE_BALLOON_SECONDARY_URL, null);
            String secondaryURLTitle  = remotePrefs.get(GE_BALLOON_SECONDARY_URL_TITLE, null);
            if (StringUtils.isEmpty(secondaryURL))
            {
                //if (Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.fish))
                {
                    remotePrefs.put(GE_BALLOON_SECONDARY_URL, GE_BALLOON_SECONDARY_URL_STR);
                    
                    if (StringUtils.isEmpty(secondaryURLTitle))
                    {
                        remotePrefs.put(GE_BALLOON_SECONDARY_URL_TITLE, GE_BALLOON_SECONDARY_URL_TITLE_STR);
                    }
                }
            }

            
            CommandAction cmdAction = new CommandAction(PLUGINS, EXPORT_JTABLE);
            ContextMgr.registerService(30, EXPORT_JTABLE, -1, cmdAction, this, "ExportExcel16", getResourceString("EXPORT_GRID_TT"));
            
            readToolRegistry();

            // create an instance of each registered exporter
            toolsNavBoxList.clear();
            for (Pair<Class<? extends RecordSetToolsIFace>, Boolean> exporterClass: toolsRegistryList)
            {
                try
                {
                    RecordSetToolsIFace exporter = exporterClass.getFirst().newInstance();
                    loadedToolsList.add(new Pair<>(exporter, exporterClass.getSecond()));
                }
                catch (Exception e)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PluginsTask.class, e);
                    log.warn("Failed to instantiate an exporter",e);
                    continue;
                }
            }

            // if visible, create a nav box button for each exporter
            if (isVisible)
            {
                extendedNavBoxes.clear();
                NavBox navBox = new NavBox(getResourceString("Plugins"));
                
                // for each registered exporter, create a TaskCommandDef for it
                for (Pair<RecordSetToolsIFace, Boolean> toolPair : loadedToolsList)
                {
                    RecordSetToolsIFace tool = toolPair.getFirst();
                    Boolean addToUI = toolPair.getSecond();
                    if (tool.isVisible() && addToUI)
                    {
                        cmdAction = new CommandAction(PLUGINS, EXPORT_RS);
                        cmdAction.setProperty("tool", tool);
                        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, tool.getName(), tool.getIconName(), cmdAction, null, true, false); // true means make it draggable
                        RolloverCommand roc = (RolloverCommand)nbi;
                        
                        //for (Integer tableId : tool.getTableIds())
                        //{
                        //    roc.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, "Record_Set", tableId));
                        //}
                        
                        int[] tableIds = tool.getTableIds();
                        if (tableIds != null && tableIds.length > 0)
                        {
                            DataFlavorTableExt df = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), 
                                                                           RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), tableIds);
                            roc.addDropDataFlavor(df);
                        }
                        
                        toolsNavBoxList.add(nbi);
                    }
                }
    
                navBoxes.add(navBox);
            }
        }

    }

    /**
     * 
     */
    protected void readToolRegistry()
    {
        toolsRegistryList.clear();
        String fileName = "rstools_registry.xml";
        
        HashMap<String, Pair<String, Boolean>> rsPlugins = new HashMap<String, Pair<String, Boolean>>();
        
        String path = XMLHelper.getConfigDirPath(fileName);
        readToolRegistry(path, rsPlugins);
        
        path = AppPreferences.getLocalPrefs().getDirPath() + File.separator + fileName;
        readToolRegistry(path, rsPlugins);
        
        for (String rspName : rsPlugins.keySet())
        {
            Pair<String, Boolean> p = rsPlugins.get(rspName);
            if (p != null)
            {
                try
                {
                    Class<? extends RecordSetToolsIFace> cls = Class.forName(p.first).asSubclass(RecordSetToolsIFace.class);
                    toolsRegistryList.add(new Pair<>(cls, p.getSecond()));
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PluginsTask.class, ex);
                    log.error(ex);
                    ex.printStackTrace();
                    
                    // go to the next tool
                    continue;
                    // XXX Do we need a dialog here ???
                }
            }
        }
    }

    /**
     * @param path
     * @param rsPlugins
     */
    protected void readToolRegistry(final String path, final HashMap<String, Pair<String, Boolean>> rsPlugins)
    {
        try
        {
            File file = new File(path);
            if (file.exists())
            {
                Element root  = XMLHelper.readFileToDOM4J(file);
                List<?> boxes = root.selectNodes("/tools/tool");
                for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
                {
                    org.dom4j.Element pluginElement = (org.dom4j.Element)iter.next();
    
                    String  rspName = pluginElement.attributeValue("name");
                    String  clsName = pluginElement.attributeValue("class");
                    Boolean addToUI = XMLHelper.getAttr(pluginElement, "addui", false);
                    if (StringUtils.isNotEmpty(rspName) && StringUtils.isNotEmpty(clsName))
                    {
                        rsPlugins.put(rspName, new Pair<String, Boolean>(clsName, addToUI));
                    }
                }
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PluginsTask.class, ex);
            ex.printStackTrace();
            log.error(ex);
        }
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.core.Taskable#requestContext()
     */
    public void requestContext()
    {
        ContextMgr.requestContext(this);

        if (starterPane == null)
        {
            super.requestContext();
            
        } else
        {
            SubPaneMgr.getInstance().showPane(starterPane);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#subPaneRemoved(edu.ku.brc.af.core.SubPaneIFace)
     */
    public void subPaneRemoved(final SubPaneIFace subPane)
    {
        super.subPaneRemoved(subPane);
        
        if (subPane == starterPane)
        {
            starterPane = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        StringBuilder htmlDesc = new StringBuilder("<h3>Welcome to the Specify Plugins</h3>"); //I18N
        htmlDesc.append("<p>Tools installed:<ul>");
        for (Pair<RecordSetToolsIFace, Boolean> toolPair: loadedToolsList)
        {
            RecordSetToolsIFace tool = toolPair.getFirst();
            Boolean addToUI = toolPair.getSecond();
            if (tool.isVisible() && addToUI)
            {
                htmlDesc.append("<li><b>" + tool.getName() + "</b><p>" + tool.getDescription());
            }
        }
        htmlDesc.append("<br></ul>");
        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        return starterPane;
    }

    /**
     * @param data the data that "should" be a RecordSet
     */
    protected void processToolDataFromRecordSet(final Object data, final Properties requestParams, final RecordSetToolsIFace exporter)
    {
        if (data instanceof RecordSetIFace)
        {
            processTool(exporter, (RecordSetIFace)data, requestParams);
        }
    }
    
    /**
     * @param data
     * @param requestParams
     * @param exporter
     */
    protected void processToolDataFromList(final Object data, final Properties requestParams, final RecordSetToolsIFace exporter)
    {
        if (data instanceof List<?>)
        {
            doProcessTool(exporter, (List<?>)data, requestParams);
        }
    }
    
    /**
     * @param tool
     * @param recordSet
     * @param requestParams
     */
    protected void processTool(final RecordSetToolsIFace tool, 
                               final RecordSetIFace      recordSet, 
                               final Properties          requestParams)
    {
        try
        {
            tool.processRecordSet(recordSet, requestParams);
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PluginsTask.class, e);
            log.error("Exception while exporting a RecordSet", e);
            JStatusBar statusBar = UIRegistry.getStatusBar();
            statusBar.setErrorMessage(e.getMessage(), e);
        }
    }
    
    /**
     * @param tool
     * @param dataList
     * @param requestParams
     */
    protected void doProcessTool(final RecordSetToolsIFace tool, 
                                 final List<?>             dataList, 
                                 final Properties          requestParams)
    {
        try
        {
            tool.processDataList(dataList, requestParams);
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PluginsTask.class, e);
            log.error("Exception while exporting a data list", e);
            JStatusBar statusBar = UIRegistry.getStatusBar();
            statusBar.setErrorMessage(e.getLocalizedMessage(), e);
        }
    }


    /**
     *
     * @return
     */
    protected String getDefaultExcelExt() {
        return "xlsx";
    }

    /**
     * @param table
     */
    protected void exportTable(final JTable table)
    {
        Hashtable<String, String> values = new Hashtable<>();
        ViewBasedDisplayDialog dlg = new ViewBasedDisplayDialog((Frame) UIRegistry.getTopWindow(), "SystemSetup", "ExcelExportInfo", null,
                 getResourceString("EXCEL_EXPORT_INFO_TITLE"), getResourceString("Export"), null, // className,
                 null, // idFieldName,
                 true, // isEdit,
                 0);
         dlg.setData(values);
         dlg.setModal(true);
         dlg.setVisible(true);
         if (dlg.getBtnPressed() == ViewBasedDisplayIFace.OK_BTN)
         {
             dlg.getMultiView().getDataFromUI();
             String fileName = values.get("FilePath");
             if (!fileName.toLowerCase().endsWith("." + getDefaultExcelExt().toLowerCase())) {
                 fileName += "." + getDefaultExcelExt();
             }
             File file = new File(fileName);
             TableModel2Excel.convertToExcel(file, values.get("Title"), table.getModel());
         }
    }


    //-------------------------------------------------------
    // Taskable
    //-------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getNavBoxes()
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
        String label    = getResourceString("Plugins");
        String hint     = getResourceString("export_hint");
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        if (AppPreferences.getRemote().getBoolean(ON_TASKBAR, false))
        {
            toolbarItems.add(new ToolBarItemDesc(toolBarBtn));
        }
        return toolbarItems;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        String menuDesc = "Specify.SYSTEM_MENU";
        
        menuItems = new Vector<MenuItemDesc>();
        
        if (permissions == null || permissions.canModify())
        {
            String    menuTitle = "PluginsTask.PLUGIN_MENU"; //$NON-NLS-1$
            String    mneu      = "PluginsTask.PLUGIN_MNEU"; //$NON-NLS-1$
            String    desc      = "PluginsTask.PLUGIN_DESC"; //$NON-NLS-1$
            JMenuItem mi        = UIHelper.createLocalizedMenuItem(menuTitle, mneu, desc, true, null);
            mi.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent ae)
                {
                    PluginsTask.this.requestContext();
                }
            });
            MenuItemDesc rsMI = new MenuItemDesc(mi, menuDesc);
            rsMI.setPosition(MenuItemDesc.Position.After);
            rsMI.setSepPosition(MenuItemDesc.Position.Before);
            menuItems.add(rsMI);
        }
        
        return menuItems;

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
     * @param cmdAction the command to be processed
     */
    protected void processToolRecordSet(final CommandAction cmdAction)
    {
        RecordSetToolsIFace tool = getTool(cmdAction);

        if (tool != null)
        {
            Object data = cmdAction.getData();
            
            if (data instanceof CommandAction && ((CommandAction)data) == cmdAction) // means it was clicked on
            {
                RecordSetTask          rsTask       = (RecordSetTask)TaskMgr.getTask(RecordSetTask.RECORD_SET);
                List<RecordSetIFace>   colObjRSList = rsTask.getRecordSets(CollectionObject.getClassTableId());
                
                // XXX Probably need to also get RSs with Localisties and or CollectingEvents

                data = getRecordSetOfDataObjs(null, CollectionObject.class, "catalogNumber", colObjRSList.size());
            }
            
            processToolDataFromRecordSet(data, cmdAction.getProperties(), tool);
        }
    }
    
    /**
     * @param cmdAction the command to be processed
     */
    protected void processToolList(final CommandAction cmdAction)
    {
        RecordSetToolsIFace tool = getTool(cmdAction);
        
        if (tool != null)
        {
            processToolDataFromList(cmdAction.getData(), cmdAction.getProperties(), tool);
        }
    }
    
    /**
     * 
     */
    protected void prefsChanged(final CommandAction cmdAction)
    {
        reAddToolBarItem(cmdAction, toolBarBtn, ON_TASKBAR);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#getPermEditorPanel()
     */
    @Override
    public PermissionEditorIFace getPermEditorPanel()
    {
        return new BasicPermisionPanel(PLUGINS, "ENABLE", null, null, null);
    }

    
    /**
     * @param cmdAction
     * @return
     */
    protected RecordSetToolsIFace getTool(final CommandAction cmdAction)
    {
        Object propValue = cmdAction.getProperty("tool");
        
        if (propValue instanceof RecordSetToolsIFace)
        {
            return (RecordSetToolsIFace)propValue;
        }
        
        if (propValue instanceof Class<?>)
        {
            for (Pair<RecordSetToolsIFace, Boolean> expPair: loadedToolsList)
            {
                RecordSetToolsIFace exp = expPair.getFirst();
                if (exp.getClass().equals(propValue))
                {
                    return exp;
                }
            }
        }
        
        if (propValue instanceof String)
        {
            for (Pair<RecordSetToolsIFace, Boolean> expPair: loadedToolsList)
            {
                RecordSetToolsIFace exp = expPair.getFirst();
                if (exp.getClass().getName().equals(propValue))
                {
                    return exp;
                }
            }
        }
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(PLUGINS))
        {
            if (cmdAction.isAction(EXPORT_RS))
            {
            	processToolRecordSet(cmdAction);
            }
            else if (cmdAction.isAction(EXPORT_LIST))
            {
                processToolList(cmdAction);
                
            } else if (cmdAction.isAction(EXPORT_JTABLE))
            {
                JTable table = (JTable)cmdAction.getProperty("jtable");
                if (table != null) {
                    if (table.getModel() instanceof ResultSetTableModel && ((ResultSetTableModel) table.getModel()).isLoadingCells()) {
                        UIRegistry.writeTimedSimpleGlassPaneMsg(UIRegistry.getResourceString("NO_ACTION_WHILE_LOADING_RESULTS"),
                                5000, null, null, true);
                    } else {
                        exportTable(table);
                    }
                }
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }
}
