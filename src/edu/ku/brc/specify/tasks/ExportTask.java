/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.tasks;

import static edu.ku.brc.ui.UICacheManager.getResourceString;

import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.util.List;
import java.util.Map;
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
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.exporters.DiGIRExporter;
import edu.ku.brc.specify.exporters.GoogleEarthExporter;
import edu.ku.brc.specify.exporters.RecordSetExporter;
import edu.ku.brc.specify.exporters.RecordSetExporterAdapter;
import edu.ku.brc.specify.exporters.WebPageExporter;
import edu.ku.brc.specify.tasks.subpane.HtmlDescPane;
import edu.ku.brc.ui.ChooseFromListDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconManager;
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
            
            extendedNavBoxes.clear();
            exportersList.clear();

            NavBox navBox = new NavBox(getResourceString("Formats")+ "/" + getResourceString("Applications"));
            
            // for each registered exporter, create a TaskCommandDef for it
            for (Class<? extends RecordSetExporter> exporterClass: exportersRegistry)
            {
                RecordSetExporter exporter = null;
                try
                {
                    exporter = exporterClass.newInstance();
                    loadedExporters.add(exporter);
                }
                catch (Exception e)
                {
                    log.warn("Failed to instantiate an exporter",e);
                    continue;
                }
                
                CommandAction cmdAction = new CommandAction(EXPORT, EXPORT_RS);
                cmdAction.setProperty("exporter",exporter);
                exportersList.add(makeDraggableAndDroppableNavBtn(navBox, exporter.getName(), exporter.getIconName(), cmdAction, null, true));// true means make it draggable
            }

            navBoxes.addElement(navBox);
        }

    }

    protected void readExporterRegistry()
    {
        exportersRegistry.clear();
        exportersRegistry.add(RecordSetExporterAdapter.class);
        exportersRegistry.add(GoogleEarthExporter.class);
        exportersRegistry.add(DiGIRExporter.class);
        exportersRegistry.add(WebPageExporter.class);
        
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
     * Displays UI that asks the user to select a predefined label.
     * @return the name of the label file or null if cancelled
     */
    protected String askForFormatOrTarget()
    {
        initialize();

        // XXX Need to pass in or check table type for different types of lables.

        NavBoxItemIFace nbi = null;
        if (exportersList.size() == 1)
        {
            nbi = exportersList.get(0);

        } else
        {
            ChooseFromListDlg<NavBoxItemIFace> dlg = new ChooseFromListDlg<NavBoxItemIFace>((Frame)UICacheManager.get(UICacheManager.TOPFRAME),
                                                                                            getResourceString("ChooseFormat"), 
                                                                                            exportersList, 
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
            	CommandAction caData = (CommandAction)data;
            	log.info("Executing export for format/target " + caData.getAction());
                //return ((CommandAction)data).getPropertyAsString("file");
            	return null;
            }
        }
        return null;
    }

    /**
     * Single place to convert the data to a Map.
     * @param data the data in a nbi
     * @return a Map<String, String>
     */
    @SuppressWarnings("unchecked")
    protected Map<String, String> convertDataToMap(final Object data)
    {
        if (data instanceof Map)
        {
            return (Map<String, String>)data; // ok to Cast
        }
        throw new RuntimeException("Why isn't the data a Map<String, String>!");
    }

    /**
     * Checks to make sure we are the current SubPane and then creates the labels from the selected RecordSet
     * @param data the data that "should" be a RecordSet
     */
    protected void exportDataFromRecordSet(final Object data, final RecordSetExporter exporter)
    {
        if (data instanceof RecordSet && ContextMgr.getCurrentContext() == this)
        {
            RecordSetIFace rs = (RecordSetIFace)data;
            doExport(exporter, rs);
        }
    }
    
    protected void doExport(RecordSetExporter exporter, RecordSetIFace data)
    {
        RecordSet rs = (RecordSet)data;
        exporter.exportRecordSet(rs);
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
     * Processes all Commands of type LABELS.
     * @param cmdAction the command to be processed
     */
    protected void processExportCommands(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(EXPORT_RS))
        {
            if (cmdAction.getData() instanceof RecordSetIFace)
            {
                Object propValue = cmdAction.getProperty("exporter");
                RecordSetExporter exporter = null;
                if (propValue instanceof RecordSetExporter)
                {
                    exporter = (RecordSetExporter)propValue;
                }
                
                if (exporter!=null)
                {
                    exportDataFromRecordSet(cmdAction.getData(),exporter);
                }
            }
        } 
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.af.tasks.BaseTask#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isType(EXPORT))
        {
        	processExportCommands(cmdAction);
        } 
    }
}
