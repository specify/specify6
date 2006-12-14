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

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.AppResourceIFace;
import edu.ku.brc.af.core.ContextMgr;
import edu.ku.brc.af.core.MenuItemDesc;
import edu.ku.brc.af.core.NavBox;
import edu.ku.brc.af.core.NavBoxAction;
import edu.ku.brc.af.core.NavBoxButton;
import edu.ku.brc.af.core.NavBoxIFace;
import edu.ku.brc.af.core.NavBoxItemIFace;
import edu.ku.brc.af.core.SubPaneIFace;
import edu.ku.brc.af.core.SubPaneMgr;
import edu.ku.brc.af.core.TaskCommandDef;
import edu.ku.brc.af.core.Taskable;
import edu.ku.brc.af.core.ToolBarItemDesc;
import edu.ku.brc.af.tasks.BaseTask;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.tasks.subpane.LabelsPane;
import edu.ku.brc.specify.ui.ChooseRecordSetDlg;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.IconListCellRenderer;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.ToolBarDropDownBtn;
import edu.ku.brc.ui.Trash;
import edu.ku.brc.ui.UICacheManager;
import edu.ku.brc.ui.dnd.DataActionEvent;
import edu.ku.brc.ui.dnd.GhostActionable;
import edu.ku.brc.ui.dnd.GhostActionableDropManager;
import edu.ku.brc.ui.dnd.GhostMouseInputAdapter;

/**
 * A task to manage Labels and response to Label Commands
 
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
@SuppressWarnings("serial")
public class LabelsTask extends BaseTask
{
    // Static Data Members
    public static final DataFlavor LABEL_FLAVOR = new DataFlavor(LabelsTask.class, "Label");
    private static final Logger log = Logger.getLogger(LabelsTask.class);

    public static final String LABELS = "Labels";

    public static final String DOLABELS_ACTION     = "DoLabels";
    public static final String NEWRECORDSET_ACTION = "NewRecordSet";
    public static final String PRINT_LABEL         = "PrintLabel";

    // Data Members
    protected Vector<NavBoxIFace>     extendedNavBoxes = new Vector<NavBoxIFace>();
    protected Vector<NavBoxItemIFace> labelsList       = new Vector<NavBoxItemIFace>();
    protected SubPaneIFace            starterPane      = null;

    // temp data
    protected NavBoxItemIFace         oneNbi           = null;

    /**
     *
     *
     */
    public LabelsTask()
    {
        super(LABELS, getResourceString(LABELS));
        
        CommandDispatcher.register(LABELS, this);
        CommandDispatcher.register(RecordSetTask.RECORD_SET, this);
        CommandDispatcher.register("App", this);
    }

   /**
     * Helper method for registering a NavBoxItem as a GhostMouseDropAdapter
     * @param navBox the parent box for the nbi to be added to
     * @param navBoxItemDropZone the nbi in question
     * @return returns the new NavBoxItem
     */
    protected NavBoxItemIFace addToNavBoxAndRegisterAsDroppable(final NavBox              navBox,
                                                                final NavBoxItemIFace     nbi,
                                                                final Map<String, String> params)
    {
        NavBoxButton roc = (NavBoxButton)nbi;
        roc.setData(params);

        // When Being Dragged
        roc.addDragDataFlavor(Trash.TRASH_FLAVOR);
        roc.addDragDataFlavor(LABEL_FLAVOR);

        // When something is dropped on it
        roc.addDropDataFlavor(RecordSetTask.RECORDSET_FLAVOR);

        navBox.add(nbi);
        labelsList.add(nbi);
        return nbi;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.core.Taskable#initialize()
     */
    public void initialize()
    {
        if (!isInitialized)
        {
            super.initialize(); // sets isInitialized to false
            
            extendedNavBoxes.clear();
            labelsList.clear();


            NavBox navBox = new NavBox(name);
            
            for (AppResourceIFace ap : AppContextMgr.getInstance().getResourceByMimeType("jrxml/label"))
            {
                Map<String, String> params = ap.getMetaDataMap();
                params.put("title", ap.getDescription());
                params.put("file", ap.getName());
                //log.info("["+ap.getDescription()+"]["+ap.getName()+"]");
                
                commands.add(new TaskCommandDef(ap.getDescription(), name, params));
            }
            
            // Then add
            if (commands != null)
            {
                for (TaskCommandDef tcd : commands)
                {
                    // XXX won't be needed when we start validating the XML
                    String tableIdStr = tcd.getParams().get("tableid");
                    if (tableIdStr == null)
                    {
                        log.error("Label Command is missing the table id");
                    } else
                    {
                        addToNavBoxAndRegisterAsDroppable(navBox, NavBox.createBtn(tcd.getName(), name, IconManager.IconSize.Std16, new DisplayAction(tcd)), tcd.getParams());
                    }
                }
            }

            navBoxes.addElement(navBox);
        }

    }

    /**
     * Performs a command (to create a label)
     * @param labelName the name of lable (the file name)
     * @param labelTitle the localized title to be displayed as the tab title
     * @param recordSet the recordSet to be turned into labels
     * @param originatingTask the Taskable requesting the the labels be made
     */
    public void doLabels(final String labelName, 
                         final String labelTitle, 
                         final RecordSetIFace recordSet, 
                         final Taskable originatingTask)
    {
        LabelsPane labelsPane;
        if (starterPane == null)
        {
            labelsPane = new LabelsPane(labelTitle, originatingTask != null ? originatingTask : this);
            SubPaneMgr.getInstance().addPane(labelsPane);
            
        } else
        {
            labelsPane  = (LabelsPane)starterPane;
            SubPaneMgr.getInstance().renamePane(labelsPane, labelTitle);
            starterPane = null;
        }
        labelsPane.createReport(labelName, recordSet);

    }

    /**
     * @return Return true if there is a small number of labels or whether the user wishes to continue.
     */
    protected boolean checkForALotOfLabels(final RecordSetIFace recordSet)
    {
        //
        if (recordSet.getItems().size() > 200) // XXX Pref
        {
            Object[] options = {getResourceString("CreateLabels"), getResourceString("Cancel")};
            int n = JOptionPane.showOptionDialog(UICacheManager.get(UICacheManager.FRAME),
                                                String.format(getResourceString("LotsOfLabels"), new Object[] {(recordSet.getItems().size())}),
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
    public SubPaneIFace getStarterPane()
    {
        //starterPane = new SimpleDescPane(name, this, "Welcome to Specify's Label Maker");
        LabelsPane labelsPane = new LabelsPane(name, this);
        labelsPane.setLabelText("Welcome to Specify's Label Maker");
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

        if (labelsList.size() == 1)
        {
            Map<String, String> params = convertDataToMap(labelsList.get(0).getData());
            return params.get("file");

        } else
        {
            ChooseLabel dlg = new ChooseLabel();
            dlg.setVisible(true);

            return dlg.getName();
        }
    }

    /**
     * Displays UI that asks the user to select a predefined label.
     * @param tableId the table id
     * @return returns the selected RecordSet or null
     */
    public static RecordSetIFace askForRecordSet(final int tableId)
    {
        ChooseRecordSetDlg dlg = new ChooseRecordSetDlg((Frame)UICacheManager.get(UICacheManager.TOPFRAME), tableId);
        if (dlg.hasRecordSets())
        {
            dlg.setVisible(true); // modal (waits for answer here)
            return dlg.getSelectedRecordSet();

        } else
        {
            return null;
        }

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
        for (NavBoxItemIFace nbi : labelsList)
        {
            Object data = nbi.getData();
            if (data != null)
            {
                Map<String, String> attrs      = convertDataToMap(data);
                String              tableIDStr = attrs.get("tableid");
                boolean             needsRS    = getNeedsRecordSet(attrs.get("reqrs"));
                
                if (StringUtils.isNumeric(tableIDStr) && (!needsRecordSet ||  needsRS))
                {
                    if (Integer.parseInt(tableIDStr) == tableId)
                    {
                        oneNbi = nbi;
                        count++;
                    }
                } else
                {
                    log.error("Attr [tableid] is not numeric for["+nbi.getTitle()+"]!");
                }
            } else
            {
                log.error(" The meta data is null for ["+nbi.getTitle()+"]");
            }
        }
        return count;
    }

    /**
     * Checks to make sure we are the current SubPane and then creates the labels from the selected RecordSet
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
            } else
            {
                fileName = convertDataToMap(oneNbi.getData()).get("file");
            }

            if (fileName != null)
            {
                doLabels(fileName, "Labels", (RecordSetIFace)data, this);
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
    public List<ToolBarItemDesc> getToolBarItems()
    {
        Vector<ToolBarItemDesc> list = new Vector<ToolBarItemDesc>();
        String label = getResourceString(name);
        String iconName = name;
        String hint = getResourceString("labels_hint");
        ToolBarDropDownBtn btn = createToolbarButton(label, iconName, hint);

        list.add(new ToolBarItemDesc(btn));
        return list;
    }

    /*
     *  (non-Javadoc)
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


    //-------------------------------------------------------
    // CommandListener Interface
    //-------------------------------------------------------

    public void doCommand(final CommandAction cmdAction)
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
        
        if (cmdAction.isAction(DOLABELS_ACTION))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                
                // XXX For the Demo and until I revist a generalized way of associating a default set of reports and labels
                // to To things. One way to get here with a null title is to click on the Labels btn from the search results
                if (recordSet.getDbTableId() == 1 && cmdAction.getPropertyAsString("title") == null)
                {
                    cmdAction.setProperty("file", "fish_label.jrxml");
                    cmdAction.setProperty("title", "Fish Labels");
                }

                if (checkForALotOfLabels(recordSet))
                {
                    String labelFileName = cmdAction.getPropertyAsString("file");

                    if (StringUtils.isNotEmpty(labelFileName))
                    {
                        labelFileName = askForLabelName();
                    }
                    
                    if (StringUtils.isNotEmpty(labelFileName))
                    {
                        doLabels(labelFileName, cmdAction.getPropertyAsString("title"), recordSet, this);
                    }
                }
            }
        } else if (cmdAction.isAction(NEWRECORDSET_ACTION))
        {
            if (cmdAction.getData() instanceof GhostActionable)
            {
                GhostActionable        ga  = (GhostActionable)cmdAction.getData();
                GhostMouseInputAdapter gpa = ga.getMouseInputAdapter();

                for (NavBoxItemIFace nbi : labelsList)
                {
                    if (nbi instanceof GhostActionable)
                    {
                        gpa.addGhostDropListener(new GhostActionableDropManager(UICacheManager.getGlassPane(), nbi.getUIComponent(), ga));
                    }
                 }
            }
        } else if (cmdAction.isAction(PRINT_LABEL))
        {
            if (cmdAction.getData() instanceof RecordSet)
            {
                RecordSetIFace recordSet = (RecordSetIFace)cmdAction.getData();
                
                // XXX For the Demo and until I revist a generalized way of associating a default set of reports and labels
                // to To things. One way to get here with a null title is to click on the Labels btn from the search results
                if (recordSet.getDbTableId() == 52 && cmdAction.getPropertyAsString("title") == null)
                {
                    cmdAction.setProperty("file", "LoanInvoice.jrxml");
                    cmdAction.setProperty("title", "Loan Invoice");
                }

                if (checkForALotOfLabels(recordSet))
                {
                    String labelFileName = cmdAction.getPropertyAsString("file");
                    
                    if (StringUtils.isEmpty(labelFileName))
                    {
                        labelFileName = askForLabelName();
                    }
                    
                    if (StringUtils.isNotEmpty(labelFileName))
                    {
                         Taskable originatingTask = (Taskable)cmdAction.getProperty(NavBoxAction.ORGINATING_TASK);
                        doLabels(labelFileName, cmdAction.getPropertyAsString("title"), recordSet, originatingTask);
                    }
                }
            }
        } else if (cmdAction.isType(RecordSetTask.RECORD_SET) &&
                   cmdAction.isAction("Clicked"))
        {
            Object srcObj = cmdAction.getSrcObj();
            Object dstObj = cmdAction.getDstObj();
            Object data   = cmdAction.getData();
            
            log.debug("********* In Labels doCommand src["+srcObj+"] dst["+dstObj+"] data["+data+"] context["+ContextMgr.getCurrentContext()+"]");
            
            createLabelFromSelectedRecordSet(srcObj);
            
        } else if (cmdAction.isType("App") && cmdAction.isAction("Restart"))
        {
            isInitialized = false;
            this.initialize();
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

    //--------------------------------------------------------------
    // Inner Classes
    //--------------------------------------------------------------

     /**
     *
     * @author rods
     *
     */
    class DisplayAction implements ActionListener
    {
        private String    nameStr;
        private String    titleStr;
        private int       tableId;
        private RecordSetIFace recordSet = null;


        public DisplayAction(final TaskCommandDef tcd)
        {
            this.nameStr  = tcd.getParams().get("file");
            this.titleStr = tcd.getParams().get("title");
            this.tableId  = Integer.parseInt(tcd.getParams().get("tableid"));
        }

        public DisplayAction(final String nameStr, final String titleStr)
        {
            this.nameStr  = nameStr;
            this.titleStr = titleStr;
        }

        public void actionPerformed(ActionEvent e)
        {
            boolean needsRecordSets = true;
            
            Object data = null;
            if (e instanceof DataActionEvent)
            {
                DataActionEvent dae = (DataActionEvent)e;
                data = dae.getData();
                if (data instanceof RecordSet)
                {
                    RecordSetIFace rs = (RecordSetIFace)data;
                    if (rs.getDbTableId() != tableId)
                    {
                        JOptionPane.showMessageDialog(null, getResourceString("ERROR_LABELS_RECORDSET_TABLEID"), getResourceString("Error"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                
                if (data instanceof Map<?,?>)
                {
                    needsRecordSets = getNeedsRecordSet(convertDataToMap(data).get("reqrs"));
                }
            }

            if (needsRecordSets && (data == null || data instanceof Map))
            {
                ChooseRecordSetDlg dlg = new ChooseRecordSetDlg((Frame)UICacheManager.get(UICacheManager.TOPFRAME), tableId);
                if (dlg.getRecordSets().size() == 1)
                {
                    data = dlg.getRecordSets().get(0);
                    
                } else if (dlg.hasRecordSets() && dlg.getRecordSets().size() > 1)
                {
                    dlg.setVisible(true); // modal (waits for answer here)
                    data = dlg.getSelectedRecordSet();
                    if (data == null)
                    {
                        return; // User hit cancel
                    }

                } else
                {
                    JOptionPane.showMessageDialog(null, getResourceString("NO_RECORD_SETS"));
                }
            }

            if (!needsRecordSets)
            {
                doLabels(nameStr, titleStr, null, null);
                
            } else if (data instanceof RecordSet)
            {
                doLabels(nameStr, titleStr, (RecordSetIFace)data, null);

            } else
            {
                log.error("Data is not RecordSet");
            }

        }

        public void setRecordSet(final RecordSetIFace recordSet)
        {
            this.recordSet = recordSet;
        }

        public RecordSetIFace getRecordSet()
        {
            return recordSet;
        }
    }


    /**
     * @author rods
     *
     */
    public class ChooseLabel extends JDialog implements ActionListener
    {
        protected JButton        cancelBtn;
        protected JButton        okBtn;
        protected JList          list;
        protected java.util.List recordSets;

        public ChooseLabel() throws HeadlessException
        {
            super((Frame)UICacheManager.get(UICacheManager.FRAME), getResourceString("ChooseLabel"), true);
            createUI();
            setLocationRelativeTo(UICacheManager.get(UICacheManager.FRAME));
            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        }

        /**
         * Creates the Default UI for Lable task
         *
         */
        protected void createUI()
        {

            JPanel panel = new JPanel(new BorderLayout());
            panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));

            panel.add(new JLabel(getResourceString("ChooseLabel"), JLabel.CENTER), BorderLayout.NORTH);

            try
            {
                ListModel listModel = new AbstractListModel()
                {
                    public int getSize() { return labelsList.size(); }
                    public Object getElementAt(int index)
                    {
                        return ((NavBoxButton)labelsList.get(index)).getLabelText();
                    }
                };

                list = new JList(listModel);
                list.setCellRenderer(new IconListCellRenderer(icon));

                list.setVisibleRowCount(5);
                list.addMouseListener(new MouseAdapter() {
                    public void mouseClicked(MouseEvent e) {
                        if (e.getClickCount() == 2) {
                            okBtn.doClick(); //emulate button click
                        }
                    }
                });
                JScrollPane listScroller = new JScrollPane(list);
                panel.add(listScroller, BorderLayout.CENTER);

                // Bottom Button UI
                cancelBtn         = UICacheManager.createButton(getResourceString("Cancel"));
                okBtn             = UICacheManager.createButton(getResourceString("OK"));

                okBtn.addActionListener(this);
                getRootPane().setDefaultButton(okBtn);

                ButtonBarBuilder btnBuilder = new ButtonBarBuilder();
                btnBuilder.addGlue();
                btnBuilder.addGriddedButtons(new JButton[] {cancelBtn, okBtn});

                cancelBtn.addActionListener(new ActionListener()
                        {  public void actionPerformed(ActionEvent ae) { setVisible(false);} });

                panel.add(btnBuilder.getPanel(), BorderLayout.SOUTH);

            } catch (Exception ex)
            {
                log.error(ex);
            }

            setContentPane(panel);
            pack();
            //setLocationRelativeTo(locationComp);

        }

         /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        public void actionPerformed(ActionEvent e)
        {
            // Handle clicks on the OK and Cancel buttons.
           setVisible(false);
        }

        /* (non-Javadoc)
         * @see java.awt.Component#getName()
         */
        public String getName()
        {
            int inx = list.getSelectedIndex();
            if (inx != -1)
            {
                return convertDataToMap(labelsList.get(inx).getData()).get("file");

            } else
            {
                return null;
            }
        }
    }
}
