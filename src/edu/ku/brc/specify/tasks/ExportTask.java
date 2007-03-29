/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.af.tasks.subpane.HtmlDescPane;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.exporters.DiGIRExporter;
import edu.ku.brc.specify.exporters.ExportToFile;
import edu.ku.brc.specify.exporters.GoogleEarthExporter;
import edu.ku.brc.specify.exporters.RecordSetExporter;
import edu.ku.brc.specify.exporters.RecordSetExporterAdapter;
import edu.ku.brc.specify.exporters.WebPageExporter;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.JStatusBar;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.UICacheManager;

/**
 * A task to handle RecordSet data exporting.  This task provides a pluggable
 * interface by which new export formats can be added.
 *
 * @code_status Alpha
 * @author jstewart
 */
@SuppressWarnings("serial")
public class ExportTask extends BaseTask
{
    private static final Logger log = Logger.getLogger(ExportTask.class);
            
    // Static Data Members
    public static final DataFlavor EXPORT_FLAVOR = new DataFlavor(ExportTask.class, "Export");

    public static final String EXPORT = "Export";

    //public static final String DOLABELS_ACTION     = "DoLabels";
    public static final String EXPORT_RS = "ExportRecordSet";
    public static final String EXPORT_LIST = "ExportList";
    //public static final String PRINT_LABEL         = "PrintLabel";

    // Data Members
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    
    /**
     * A {@link Vector} or the registered export formats/targets.
     */
    protected Vector<NavBoxItemIFace> exportersList    = new Vector<NavBoxItemIFace>();

    // temp data
    protected NavBoxItemIFace         oneNbi           = null;
    
    protected List<Class<? extends RecordSetExporter>> exportersRegistry = new Vector<Class<? extends RecordSetExporter>>();
    
    protected List<RecordSetExporter> loadedExporters = new Vector<RecordSetExporter>();
    
    /**
     *
     *
     */
    public ExportTask()
    {
        super(EXPORT, getResourceString(EXPORT));
        
        CommandDispatcher.register(EXPORT, this);
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
            
            readExporterRegistry();

            // create an instance of each registered exporter
            exportersList.clear();
            for (Class<? extends RecordSetExporter> exporterClass: exportersRegistry)
            {
                try
                {
                    RecordSetExporter exporter = exporterClass.newInstance();
                    loadedExporters.add(exporter);
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
                NavBox navBox = new NavBox(getResourceString("Formats")+ "/" + getResourceString("Applications"));
                
                // for each registered exporter, create a TaskCommandDef for it
                for (RecordSetExporter exporter: loadedExporters)
                {
                    CommandAction cmdAction = new CommandAction(EXPORT, EXPORT_RS);
                    cmdAction.setProperty("exporter",exporter);
                    exportersList.add(makeDnDNavBtn(navBox, exporter.getName(), exporter.getIconName(), cmdAction, null, true));// true means make it draggable
                }
    
                navBoxes.addElement(navBox);
            }
        }

    }

    protected void readExporterRegistry()
    {
        exportersRegistry.clear();
        exportersRegistry.add(RecordSetExporterAdapter.class);
        exportersRegistry.add(GoogleEarthExporter.class);
        exportersRegistry.add(DiGIRExporter.class);
        exportersRegistry.add(WebPageExporter.class);
        exportersRegistry.add(ExportToFile.class);
        
        // TODO: implement this to read an XML file and instantiate one copy of each exporter
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.BaseTask#getStarterPane()
     */
    @Override
    public SubPaneIFace getStarterPane()
    {
        StringBuilder htmlDesc = new StringBuilder("<h3>Welcome to the Specify Data Exporter</h3>");
        htmlDesc.append("<p>Exporters installed:<ul>");
        for (RecordSetExporter exporter: loadedExporters)
        {
            htmlDesc.append("<li><b>" + exporter.getName() + "</b><p>" + exporter.getDescription());
        }
        htmlDesc.append("</ul>");
        starterPane = new HtmlDescPane(name, this, htmlDesc.toString());
        return starterPane;
    }

    /**
     * @param data the data that "should" be a RecordSet
     */
    protected void exportDataFromRecordSet(final Object data, final Properties requestParams, final RecordSetExporter exporter)
    {
        if (data instanceof RecordSet)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            doExport(exporter, rs, requestParams);
        }
    }
    
    protected void exportDataFromList(final Object data, final Properties requestParams, final RecordSetExporter exporter)
    {
        if (data instanceof List)
        {
            List<?> dataList = (List<?>)data;
            doExport(exporter, dataList, requestParams);
        }
    }
    
    protected void doExport(RecordSetExporter exporter, RecordSetIFace data, Properties requestParams)
    {
        RecordSet rs = (RecordSet)data;
        try
        {
            exporter.exportRecordSet(rs,requestParams);
        }
        catch (Exception e)
        {
            log.error("Exception while exporting a RecordSet", e);
            JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
            statusBar.setErrorMessage(e.getMessage(), e);
        }
    }
    
    protected void doExport(RecordSetExporter exporter, List<?> data, Properties requestParams)
    {
        try
        {
            exporter.exportList(data,requestParams);
        }
        catch (Exception e)
        {
            log.error("Exception while exporting a data list", e);
            JStatusBar statusBar = (JStatusBar)UICacheManager.get(UICacheManager.STATUSBAR);
            statusBar.setErrorMessage(e.getLocalizedMessage(), e);
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

        extendedNavBoxes.addAll(rsTask.getNavBoxes());

        return extendedNavBoxes;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getToolBarItems()
     */
    @Override
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String iconName = name;
        String hint = getResourceString("export_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        list.add(new ToolBarItemDesc(btn));
        return list;
    }

    /*
     *  (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.Taskable#getMenuItems()
     */
    @Override
    public List<MenuItemDesc> getMenuItems()
    {
        Vector<MenuItemDesc> list = new Vector<MenuItemDesc>();
        return list;

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
    protected void processExportRecordSet(final CommandAction cmdAction)
    {
        RecordSetExporter exporter = getExporter(cmdAction);

        if (exporter != null)
        {
            exportDataFromRecordSet(cmdAction.getData(), cmdAction.getProperties(), exporter);
        }
    }
    
    /**
     * @param cmdAction the command to be processed
     */
    protected void processExportList(final CommandAction cmdAction)
    {
        RecordSetExporter exporter = getExporter(cmdAction);
        
        if (exporter!=null)
        {
            exportDataFromList(cmdAction.getData(),cmdAction.getProperties(),exporter);
        }
    }
    
    protected RecordSetExporter getExporter(final CommandAction cmdAction)
    {
        Object propValue = cmdAction.getProperty("exporter");
        
        if (propValue instanceof RecordSetExporter)
        {
            return (RecordSetExporter)propValue;
        }
        
        if (propValue instanceof Class<?>)
        {
            for (RecordSetExporter exp: loadedExporters)
            {
                if (exp.getClass().equals(propValue))
                {
                    return exp;
                }
            }
        }
        
        if (propValue instanceof String)
        {
            for (RecordSetExporter exp: loadedExporters)
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
        if (cmdAction.isType(EXPORT) && cmdAction.isAction(EXPORT_RS))
        {
        	processExportRecordSet(cmdAction);
        }
        else if (cmdAction.isType(EXPORT) && cmdAction.isAction(EXPORT_LIST))
        {
            processExportList(cmdAction);
        }
    }
}
