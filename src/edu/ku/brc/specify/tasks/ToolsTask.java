/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.helpers.XMLHelper.readDOMFromConfigDir;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.AppContextMgr;
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
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.HtmlDescPane;
import edu.ku.brc.af.ui.db.ViewBasedDisplayDialog;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.TableModel2Excel;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.rstools.RecordSetToolsIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.DataFlavorTableExt;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.RolloverCommand;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UIRegistry;

/**
 * A task to handle RecordSet data exporting.  This task provides a pluggable
 * interface by which new export formats can be added.
 *
 * @code_status Alpha
 * 
 * @author jstewart
 */
@SuppressWarnings("serial")
public class ToolsTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(ToolsTask.class);
            
    // Static Data Members
    public static final DataFlavor TOOLS_FLAVOR = new DataFlavor(ToolsTask.class, "Tools");
    
    public static final String GE_BALLOON_FG_COLOR     = "google.earth.fgcolor";
    public static final String GE_BALLOON_FG_COLOR_STR = "255, 255, 255";
    
    public static final String GE_BALLOON_BG_COLOR     = "google.earth.bgcolor";
    public static final String GE_BALLOON_BG_COLOR_STR = "0, 102, 179";
    
    
    public static final String GE_BALLOON_PRIMARY_URL       = "google.earth.primaryurl";
    public static final String GE_BALLOON_PRIMARY_URL_TITLE = "google.earth.primaryurltitle";
    
    public static final String GE_BALLOON_SECONDARY_URL       = "google.earth.secondaryurl";
    public static final String GE_BALLOON_SECONDARY_URL_TITLE = "google.earth.secondaryurltitle";
    
    public static String GE_BALLOON_PRIMARY_URL_STR   = "http://www.fishbase.org/Summary/speciesSummary.php?genusname=%s&speciesname=%s";
    public static String GE_BALLOON_SECONDARY_URL_STR = "http://animaldiversity.ummz.umich.edu/site/accounts/information/%s-%s";

    public static String GE_BALLOON_PRIMARY_URL_TITLE_STR   = "fb";
    public static String GE_BALLOON_SECONDARY_URL_TITLE_STR = "ad";

    public static final String TOOLS = "Tools";

    //public static final String DOLABELS_ACTION     = "DoLabels";
    public static final String EXPORT_RS     = "ExportRecordSet";
    public static final String EXPORT_LIST   = "ExportList";
    public static final String EXPORT_JTABLE = "ExportJTable";
    //public static final String PRINT_LABEL         = "PrintLabel";

    // Data Members
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected ToolBarDropDownBtn      toolBarBtn       = null;
    
    /**
     * A {@link Vector} or the registered export formats/targets.
     */
    protected Vector<NavBoxItemIFace> toolsNavBoxList    = new Vector<NavBoxItemIFace>();

    protected List<Class<? extends RecordSetToolsIFace>> toolsRegistryList = new Vector<Class<? extends RecordSetToolsIFace>>();
    
    protected List<RecordSetToolsIFace> loadedToolsList = new Vector<RecordSetToolsIFace>();
    
    /**
     * Constructor.
     */
    public ToolsTask()
    {
        super(TOOLS, getResourceString("TOOLS_MENU"));
        
        CommandDispatcher.register(TOOLS, this);
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

            
            CommandAction cmdAction = new CommandAction(TOOLS, EXPORT_JTABLE);
            ContextMgr.registerService(30, EXPORT_JTABLE, -1, cmdAction, this, "ExportExcel16", getResourceString("EXPORT_GRID_TT"));
            
            readToolRegistry();

            // create an instance of each registered exporter
            toolsNavBoxList.clear();
            for (Class<? extends RecordSetToolsIFace> exporterClass: toolsRegistryList)
            {
                try
                {
                    RecordSetToolsIFace exporter = exporterClass.newInstance();
                    loadedToolsList.add(exporter);
                }
                catch (Exception e)
                {
                    log.warn("Failed to instantiate an exporter",e);
                    continue;
                }
            }

            // if visible, create a nav box button for each exporter
            if (isVisible)
            {
                extendedNavBoxes.clear();
                NavBox navBox = new NavBox(getResourceString("EXPORTER_TOOLS"));
                
                // for each registered exporter, create a TaskCommandDef for it
                for (RecordSetToolsIFace tool : loadedToolsList)
                {
                    if (tool.isVisible())
                    {
                        cmdAction = new CommandAction(TOOLS, EXPORT_RS);
                        cmdAction.setProperty("tool", tool);
                        NavBoxItemIFace nbi = makeDnDNavBtn(navBox, tool.getName(), tool.getIconName(), cmdAction, null, true, false); // true means make it draggable
                        RolloverCommand roc = (RolloverCommand)nbi;
                        
                        //for (Integer tableId : tool.getTableIds())
                        //{
                        //    roc.addDropDataFlavor(new DataFlavorTableExt(RecordSetTask.class, "Record_Set", tableId));
                        //}
                        
                        DataFlavorTableExt df = new DataFlavorTableExt(RecordSetTask.RECORDSET_FLAVOR.getDefaultRepresentationClass(), 
                                                                       RecordSetTask.RECORDSET_FLAVOR.getHumanPresentableName(), tool.getTableIds());
                        roc.addDropDataFlavor(df);
                        
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
        
        //exportersRegistry.add(GoogleEarthExporter.class);
        //exportersRegistry.add(DiGIRExporter.class);
        //exportersRegistry.add(WebPageExporter.class);
        //exportersRegistry.add(ExportToFile.class);
        //exportersRegistry.add(BGMRecordSetProcessor.class);
        
        try
        {
            Element root  = readDOMFromConfigDir("rstools_registry.xml");
            List<?> boxes = root.selectNodes("/tools/tool");
            for ( Iterator<?> iter = boxes.iterator(); iter.hasNext(); )
            {
                org.dom4j.Element pluginElement = (org.dom4j.Element)iter.next();

                String clsName = pluginElement.attributeValue("class");
                try
                {
                    Class<? extends RecordSetToolsIFace> cls = Class.forName(clsName).asSubclass(RecordSetToolsIFace.class);
                    toolsRegistryList.add(cls);
                    
                } catch (Exception ex)
                {
                    log.error(ex);
                    ex.printStackTrace();
                    
                    // go to the next tool
                    continue;
                    // XXX Do we need a dialog here ???
                }
            }
            
        } catch (Exception ex)
        {
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
        StringBuilder htmlDesc = new StringBuilder("<h3>Welcome to the Specify Data Exporter</h3>");
        htmlDesc.append("<p>Tools installed:<ul>");
        for (RecordSetToolsIFace tool: loadedToolsList)
        {
            if (tool.isVisible())
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
        if (data instanceof List)
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
            log.error("Exception while exporting a data list", e);
            JStatusBar statusBar = UIRegistry.getStatusBar();
            statusBar.setErrorMessage(e.getLocalizedMessage(), e);
        }
    }
    

    /**
     * @param table
     */
    protected void exportTable(final JTable table)
    {
        Hashtable<String, String> values = new Hashtable<String, String>();
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
             File file = new File(values.get("FilePath"));
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
        String label    = getResourceString("Tools");
        //String iconName = "Tools";
        String hint     = getResourceString("export_hint");
        toolBarBtn      = createToolbarButton(label, iconName, hint);
        
        toolbarItems = new Vector<ToolBarItemDesc>();
        String ds = AppContextMgr.getInstance().getClassObject(Discipline.class).getName();
        if (AppPreferences.getRemote().getBoolean("ExportTask.OnTaskbar"+"."+ds, false))
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
        menuItems = new Vector<MenuItemDesc>();
        
        JMenuItem exporter = new JMenuItem(getResourceString("EXPORTERS_MENU"));
        menuItems.add(new MenuItemDesc(exporter, "ToolsMenu"));
        
        exporter.addActionListener(new ActionListener()
        {
            @SuppressWarnings("synthetic-access")
            public void actionPerformed(ActionEvent ae)
            {
               ToolsTask.this.requestContext();
            }
        });

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
            processToolDataFromRecordSet(cmdAction.getData(), cmdAction.getProperties(), tool);
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
        AppPreferences appPrefs = (AppPreferences)cmdAction.getData();
        
        if (appPrefs == AppPreferences.getRemote())
        {
            // Note: The event send with the name of pref from the form
            // not the name that was saved. So we don't need to append the discipline name on the end
            Object value = cmdAction.getProperties().get("Exporttask.OnTaskbar");
            if (value != null && value instanceof Boolean)
            {
                /*
                 * This doesn't work because it isn't added to the Toolbar correctly
                 * */
                JToolBar toolBar = (JToolBar)UIRegistry.get(UIRegistry.TOOLBAR);
                
                Boolean isChecked = (Boolean)value;
                if (isChecked)
                {
                    TaskMgr.addToolbarBtn(toolBarBtn, toolBar.getComponentCount()-1);
                } else
                {
                    TaskMgr.removeToolbarBtn(toolBarBtn);
                }
                toolBar.validate();
                toolBar.repaint();
                 
            }
        }
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
            for (RecordSetToolsIFace exp: loadedToolsList)
            {
                if (exp.getClass().equals(propValue))
                {
                    return exp;
                }
            }
        }
        
        if (propValue instanceof String)
        {
            for (RecordSetToolsIFace exp: loadedToolsList)
            {
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
        if (cmdAction.isType(TOOLS))
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
                if (table != null)
                {
                    exportTable(table);
                }
            }
        } else if (cmdAction.isType(PreferencesDlg.PREFERENCES))
        {
            prefsChanged(cmdAction);
        } 
    }
}
