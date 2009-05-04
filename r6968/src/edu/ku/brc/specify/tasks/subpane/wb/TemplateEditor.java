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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIHelper.createLabel;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Element;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.SchemaI18NService;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.core.expresssearch.TableFieldPair;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefItemStandardEntry;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DefaultModifiableListModel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;

/**
 * This panel is enables a user to make all the columns from a data file (XLS) to our Database schema.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Apr 30, 2007
 *
 */
@SuppressWarnings("serial")
public class TemplateEditor extends CustomDialog
{
    private static final Logger log = Logger.getLogger(TemplateEditor.class);
        
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        upBtn;
    protected JButton                        downBtn;
    
    protected JList                          mapList;
    protected DefaultModifiableListModel<FieldMappingPanel> mapModel;
    protected JScrollPane                    mapScrollPane;
    
    protected JList                          tableList;
    protected DefaultModifiableListModel<TableInfo> tableModel;
    
    protected JList                          fieldList;
    protected DefaultModifiableListModel<FieldInfo> fieldModel;
    
    protected Vector<WorkbenchTemplateMappingItem>			deletedItems		= new Vector<WorkbenchTemplateMappingItem>();

	protected boolean										hasChanged			= false;
	protected boolean										doingFill			= false;
	protected Color											btnPanelColor;
	protected JPanel										btnPanel;

	protected ImportDataFileInfo							dataFileInfo		= null;
	protected WorkbenchTemplate								workbenchTemplate	= null;
	protected DBTableIdMgr									databaseSchema;
	protected List<TreeDefItemStandardEntry>				taxRanks			= null;

	protected boolean										isMappedToAFile;
	protected boolean										isEditMode;
	protected boolean										isReadOnly			= false;
	protected boolean										ignoreMapListUpdate	= false;

	protected ImageIcon										blankIcon			= IconManager
																						.getIcon(
																								"BlankIcon",
																								IconManager.STD_ICON_SIZE);

	protected TableInfoListRenderer							tableInfoListRenderer;
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public TemplateEditor(final Frame frame, final String title, final ImportDataFileInfo dataFileInfo)
    {
        super(frame, title, true, OKCANCELHELP, null);
        
        this.dataFileInfo    = dataFileInfo;
        this.isMappedToAFile = dataFileInfo != null;
        this.isEditMode      = false;
        
        helpContext = dataFileInfo == null ? "WorkbenchNewMapping" : "WorkbenchEditMapping";
        
        createUI();
    }
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public TemplateEditor(final Frame frame, final String title, final WorkbenchTemplate wbTemplate)
    {
        super(frame, title, true, OKCANCELHELP, null);
        
        this.workbenchTemplate = wbTemplate;
        this.isMappedToAFile   = StringUtils.isNotEmpty(wbTemplate.getSrcFilePath());
        this.isEditMode        = this.workbenchTemplate != null;
        
        helpContext = "WorkbenchEditMapping";
        
        createUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        databaseSchema = WorkbenchTask.getDatabaseSchema();
        
        int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
        SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.WORKBENCH_SCHEMA, 
                                                       disciplineeId, 
                                                       databaseSchema, 
                                                       SchemaI18NService.getCurrentLocale());

        
        // Create the Table List
        Vector<TableInfo> tableInfoList = new Vector<TableInfo>();
        for (DBTableInfo ti : databaseSchema.getTables())
        {
            if (StringUtils.isNotEmpty(ti.toString()))
            {
                TableInfo tableInfo = new TableInfo(ti, IconManager.STD_ICON_SIZE);
                tableInfoList.add(tableInfo); 
                
                Vector<FieldInfo> fldList = new Vector<FieldInfo>();
                for (DBFieldInfo fi : ti.getFields())
                {
                    fldList.add(new FieldInfo(ti, fi));
                }
                //Collections.sort(fldList);
                tableInfo.setFieldItems(fldList);
            }
        }
        Collections.sort(tableInfoList);
        
        fieldModel = new DefaultModifiableListModel<FieldInfo>();
        tableModel = new DefaultModifiableListModel<TableInfo>();
        for (TableInfo ti : tableInfoList)
        {
            tableModel.add(ti);
            
            // only added for layout
            for (FieldInfo fi : ti.getFieldItems())
            {
                fieldModel.add(fi);
            }
        }
        
        tableList = new JList(tableModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.STD_ICON_SIZE));
        JScrollPane tableScrollPane = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    Object selObj = tableList.getSelectedValue();
                    if (selObj != null)
                    {
                        fillFieldList((TableInfo)selObj);
                    }
                    updateEnabledState();
                }
            }
        });

        fieldList = new JList(fieldModel);
        fieldList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.STD_ICON_SIZE));
        fieldList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane fieldScrollPane = new JScrollPane(fieldList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateEnabledState();
                }
            }
        });
        
        mapModel = new DefaultModifiableListModel<FieldMappingPanel>();
        mapList  = new JList(mapModel);
        mapList.setCellRenderer(new MapCellRenderer());
        mapList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        mapScrollPane = new JScrollPane(mapList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        mapList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    FieldMappingPanel fmp = (FieldMappingPanel)mapList.getSelectedValue();
                    if (fmp != null)
                    {
                        ignoreMapListUpdate = true;
                        
                        FieldInfo fldInfo = fmp.getFieldInfo();
                        if (fldInfo != null)
                        {
                            for (int i=0;i<tableModel.size();i++)
                            {
                                TableInfo tblInfo = (TableInfo)tableModel.get(i);
                                if (fldInfo.getTableinfo() == tblInfo.getTableInfo())
                                {
                                    tableList.setSelectedValue(tblInfo, true);
                                    fillFieldList(tblInfo);
                                    //System.out.println(fldInfo.hashCode()+" "+fldInfo.getFieldInfo().hashCode());
                                    fieldList.setSelectedValue(fldInfo, true);
                                    break;
                                }
                            }
                        }
                        ignoreMapListUpdate = false;
                        updateEnabledState();
                    }
                }
            }
        });
        
        upBtn = createIconBtn("ReorderUp", "WB_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int               inx = mapList.getSelectedIndex();
                FieldMappingPanel fmp = mapModel.getElementAt(inx);
                
                mapModel.remove(fmp);
                mapModel.insertElementAt(fmp, inx-1);
                mapList.setSelectedIndex(inx-1);
                updateEnabledState();
                setChanged(true);
            }
        });
        downBtn = createIconBtn("ReorderDown", "WB_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int               inx = mapList.getSelectedIndex();
                FieldMappingPanel fmp = mapModel.getElementAt(inx);
                
                mapModel.remove(fmp);
                mapModel.insertElementAt(fmp, inx+1);
                mapList.setSelectedIndex(inx+1);
                updateEnabledState();
                setChanged(true);
            }
        });
        
        JButton dumpMappingBtn = createIconBtn("BlankIcon", IconManager.IconSize.Std16, "WB_MAPPING_DUMP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                dumpMapping();
            }
        });
        dumpMappingBtn.setEnabled(true);
        dumpMappingBtn.setFocusable(false);
        dumpMappingBtn.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e)
            {
                ((JButton)e.getSource()).setIcon(IconManager.getIcon("Save", IconManager.IconSize.Std16));
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                ((JButton)e.getSource()).setIcon(IconManager.getIcon("BlankIcon", IconManager.IconSize.Std16));
                super.mouseExited(e);
            }
            
        });

        mapToBtn = createIconBtn("Map", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                map();
            }
        });
        unmapBtn = createIconBtn("Unmap", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                unmap();
            }
        });

        // Adjust all Labels depending on whether we are creating a new template or not
        // and whether it is from a file or not
        String mapListLeftLabel;
        String mapListRightLabel;
        
        // Note: if workbenchTemplate is null then it is 
        String dataTypeLabel   = getResourceString("WB_DATA_TYPE");
        String fieldsLabel     = getResourceString("WB_FIELDS");
        
        mapListLeftLabel  = fieldsLabel;
        mapListRightLabel = getResourceString("WB_COLUMNS");
        
        CellConstraints cc = new CellConstraints();
        
        JPanel mainLayoutPanel = new JPanel();
        
        PanelBuilder labelsBldr = new PanelBuilder(new FormLayout("p, f:p:g, p", "p"));        
        labelsBldr.add(createLabel(mapListLeftLabel, SwingConstants.LEFT), cc.xy(1, 1));
        labelsBldr.add(createLabel(mapListRightLabel, SwingConstants.RIGHT), cc.xy(3, 1));

        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "p,f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(dumpMappingBtn, cc.xy(1, 1));
        upDownPanel.add(upBtn,          cc.xy(1, 3));
        upDownPanel.add(downBtn,        cc.xy(1, 5));

        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("c:p:g", "p, 2px, p"));
        middlePanel.add(mapToBtn, cc.xy(1, 1));
        middlePanel.add(unmapBtn, cc.xy(1, 3));
        
        btnPanel = middlePanel.getPanel();
        
        PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
        
        // Main Pane Layout
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(200px;p):g, 5px, max(200px;p), 5px, p:g, 5px, f:max(250px;p):g, 2px, p", 
                                                                  "p, 2px, f:max(350px;p):g"), mainLayoutPanel);
        
        builder.add(createLabel(dataTypeLabel, SwingConstants.CENTER), cc.xy(1, 1));
        builder.add(createLabel(fieldsLabel,   SwingConstants.CENTER), cc.xy(3, 1));
        builder.add(labelsBldr.getPanel(),                            cc.xy(7, 1));
        
        builder.add(tableScrollPane, cc.xy(1, 3));
        builder.add(fieldScrollPane, cc.xy(3, 3));
        
        builder.add(outerMiddlePanel.getPanel(),  cc.xy(5, 3));
        
        builder.add(mapScrollPane,          cc.xy(7, 3));
        builder.add(upDownPanel.getPanel(), cc.xy(9, 3));
                    
        mainLayoutPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        contentPanel = mainLayoutPanel;
        
        Color bgColor = btnPanel.getBackground();
        int inc = 16;
        btnPanelColor = new Color(Math.min(255, bgColor.getRed()+inc), Math.min(255, bgColor.getGreen()+inc), Math.min(255, bgColor.getBlue()+inc));
        btnPanel.setBackground(btnPanelColor);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        
        okBtn.setEnabled(false);
        
        HelpMgr.registerComponent(helpBtn, helpContext);
        
        if (dataFileInfo != null)
        {
            autoMapFromDataFile(dataFileInfo.getColInfo());
        }
        
        if (workbenchTemplate != null)
        {
            fillFromTemplate();
            setChanged(false);
        }
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        if (dataFileInfo == null) //can't add new mappings when importing.
        {
        	FieldMappingPanel fmp = addMappingItem(null, IconManager.getIcon("BlankIcon", IconManager.STD_ICON_SIZE), null);
        	fmp.setAdded(true);
        	fmp.setNew(true);
        }
        
        pack();
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                cancelBtn.requestFocus();
                fieldModel.clear();
                fieldList.clearSelection();
                updateEnabledState();
                
                if (mapModel.size() > 1)
                {
                    mapList.clearSelection();
                }
            }
        });
    }
    
    /**
     * Dumps the mapping to the screen and clipboard.
     */
    protected void dumpMapping()
    {
        StringBuilder clipBrdTxt = new StringBuilder();

        StringBuilder sb = new StringBuilder("<html><br><table border=\"1\">");
        sb.append("<tr><td><b>"+getResourceString("WB_FIELDS")+"</b></td><td><b>"+getResourceString("WB_COLUMNS")+"</b></td></tr>");
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = (FieldMappingPanel)mapModel.get(i);
            FieldInfo       fieldInfo = fmp.getFieldInfo();
            ImportColumnInfo colInfo  = fmp.getColInfo();
            if (!fmp.isNew)
            {
                sb.append("<tr><td>"+fieldInfo.getTableinfo().getTitle()+" - "+fieldInfo.getFieldInfo().getName()+"</td><td>"+colInfo.getColTitle()+"</td></tr>");
                clipBrdTxt.append(fieldInfo.getTableinfo().getTitle()+" - "+fieldInfo.getFieldInfo().getName()+" -> "+colInfo.getColTitle()+"\n");
            }
        }
        sb.append("</table><br>The Mappings have been copied to the clipboard for pasting into an email.<br><br></htm>");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createLabel(sb.toString()), BorderLayout.CENTER);
        
        // Set into Clipboard
        StringSelection stsel  = new StringSelection(clipBrdTxt.toString());
        Clipboard       sysClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        sysClipboard.setContents(stsel, stsel);
        
        CustomDialog dialog = new CustomDialog((JFrame)UIRegistry.getTopWindow(), "Mappings", true, CustomDialog.OK_BTN, panel);
        dialog.setVisible(true);
    }
    
    
    /**
     * Fill in the JList's model from the list of fields.
     * @param tableInfo the table who's list we should use
     */
    protected void fillFieldList(final TableInfo tableInfo)
    {
        DefaultListModel model = (DefaultListModel)fieldList.getModel();
        model.clear();
        fieldList.clearSelection();
        
        if (tableInfo != null)
        {
            for (FieldInfo fi : tableInfo.getFieldItems())
            {
                model.addElement(fi);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        // Adjust the view order right before we leave the dialog.
        adjustViewOrder();
        
        super.okButtonPressed();
    }
    
    /**
     * Set has Changed.
     * @param hasChanged true false
     */
    protected void setChanged(final boolean hasChanged)
    {
        this.hasChanged = hasChanged;
        okBtn.setEnabled(hasChanged);
    }

    /**
     * Add a new FieldMappingPanel (which comes from the data file column) and selects it.
     * @param colInfo the Column Info about the Data File column
     * @param mappingIcon the icon it should use to describe what it has been mapped to
     * @param wbtmi the workbench mapping item
     */
    protected FieldMappingPanel addMappingItem(final ImportColumnInfo colInfo, 
                                               final ImageIcon        mappingIcon,
                                               final WorkbenchTemplateMappingItem wbtmi)
    {
        FieldMappingPanel fmp = new FieldMappingPanel(colInfo, mappingIcon);
        fmp.setWbtmi(wbtmi);
        
        //fmp.setSchemaLabelVisible(true);//workbenchTemplate != null || isMappedToAFile);
        
        mapModel.add(fmp);
        
        mapList.setSelectedValue(fmp, true);
        
        return fmp;
    }
    
    /**
     * Update the MappingTo and unmapping buttons.
     */
    protected void updateEnabledState()
    {
        int mapInx = mapList.getSelectedIndex();
        if (mapInx > -1)
        {
            FieldMappingPanel  fmp     = mapModel.getElementAt(mapInx);
            TableListItemIFace tblItem = (TableListItemIFace)tableList.getSelectedValue();
            TableListItemIFace fldItem = (TableListItemIFace)fieldList.getSelectedValue();
            
            if (tblItem != null && fldItem != null)
            {
                mapToBtn.setEnabled(!fldItem.isChecked());
                unmapBtn.setEnabled(!fmp.isNew() && fmp.getFieldInfo() != null);
                
                //boolean allChecked = allFieldsChecked();
                
                if (isEditMode || isMappedToAFile)
                {
                    // ZZZ addBtn.setEnabled(unusedList.getSelectedIndex() == -1 && !allChecked);
                } else
                {
                    //OK. empty block.
                }
                
                upBtn.setEnabled(mapInx > 0 && !fmp.isNew());
                downBtn.setEnabled(mapInx < mapModel.size()-2 && !fmp.isNew());
                
            } else
            {
                mapToBtn.setEnabled(false);
            }
        } else
        {
            mapToBtn.setEnabled(false);
            unmapBtn.setEnabled(false);
        }
        
        if (okBtn != null && hasChanged)
        {
            boolean isOK = false;
            for (int i=0;i<mapModel.size();i++)
            {
                FieldMappingPanel fmp = (FieldMappingPanel)mapModel.get(i);
                if (fmp.getFieldInfo() != null)
                {
                    isOK = true;
                    break;
                }
            }
            okBtn.setEnabled(isOK);
        }
        
        repaint();
    }
    
    /**
     * Adjusts the order of all the templates.
     */
    protected void adjustViewOrder()
    {
        short inx = 0;
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            if (fmp.getFieldInfo() != null)
            {
                fmp.setViewOrder(inx);
                inx++;
            }
            //System.out.println(fmp.getFieldName()+" "+i+" "+fmp.getViewOrder());
        }
    }
    
    /**
     * Maps an item from the unused list and table list.
     */
    protected FieldMappingPanel map()
    {
        FieldMappingPanel fmp     = (FieldMappingPanel)mapList.getSelectedValue();
        
        if (fmp.isNew())
        {
            mapModel.remove(fmp);
            
            addNewMappingItems();
            
            FieldMappingPanel newFmp = addMappingItem(null, IconManager.getIcon("BlankIcon", IconManager.STD_ICON_SIZE), null);
            newFmp.setAdded(true);
            newFmp.setNew(true);
            
        } else
        {
            ImportColumnInfo  colInfo = fmp.getColInfo();
            FieldInfo         fi      = (FieldInfo)fieldList.getSelectedValue();
            TableInfo         tblInfo = (TableInfo)tableList.getSelectedValue();
            String errMsg = isMappable(fi, fmp);
            if (errMsg == null)
            {
            	map(fmp, colInfo, tblInfo, fi, fmp.getWbtmi());
            }
            else
            {
            	UIRegistry.displayErrorDlg(errMsg);
            }
        }

        updateEnabledState();

        return fmp;
    }
    
    /**
     * Maps a ColInfo and FieldInfo and wbtmi is optional (can be null).
     * @param colInfo the colinfo from a file
     * @param fi the field info from the table list
     * @param wbtmi the optional template info, which is null for new columns and not null when editting old already saved cols
     * @return the new mapping panel
     */
    protected FieldMappingPanel map(final FieldMappingPanel  fmpArg,
                                    final ImportColumnInfo   colInfo, 
                                    final TableInfo          tableInfo, 
                                    final FieldInfo          fieldInfo, 
                                    final WorkbenchTemplateMappingItem wbtmi)
    {
        FieldMappingPanel fmp = fmpArg;
        if (fmp == null)
        {
            fmp = addMappingItem(colInfo, null, wbtmi);
        }
        
        // Clear the old from being used
        FieldInfo oldFieldInfo = fmp.getFieldInfo();
        if (oldFieldInfo != null)
        {
            oldFieldInfo.setInUse(false);
        }
        
        fieldInfo.setInUse(true);
        
        fmp.setColInfo(colInfo);
        fmp.setFieldInfo(fieldInfo);
        fmp.setWbtmi(wbtmi);
        if (tableInfo.getIcon() == null)
        {
            fmp.setIcon(tableInfo.getTableInfo().getIcon(IconManager.STD_ICON_SIZE));
        }
        else
        {
            fmp.setIcon(tableInfo.getIcon());
        }
        
        fmp.getArrowLabel().setVisible(true);
        
        if (colInfo == null && wbtmi == null)
        {
            fmp.setColFieldLabel(fieldInfo.getText());
            fmp.getArrowLabel().setIcon(IconManager.getIcon("LinkedRight"));
            
        } else
        {
            fmp.getArrowLabel().setIcon(IconManager.getIcon(colInfo != null ? "Linked" : "LinkedRight"));
        }
        
        setChanged(true);
        
        updateEnabledState();
        
        return fmp;
    }
    
    /**
     * Unmap the Field or remove the item if there is no file.
     */
    protected void unmap()
    {
        FieldMappingPanel fmp = (FieldMappingPanel)mapList.getSelectedValue();
        
        FieldInfo fieldInfo = fmp.getFieldInfo();
        if (fieldInfo != null)
        {
            fieldInfo.setChecked(false);
            fieldList.repaint();
        }
        
        if (fmp.isAdded())
        {
            mapModel.remove(fmp);
            
        } else
        {
            fmp.setFieldInfo(null);
            fmp.getArrowLabel().setIcon(null);
            mapList.repaint();
        }
        
        setChanged(true);
        
        // Need to Sort Here
        updateEnabledState();
   }
    
    /**
     * checks to see if anyone field that is selected is Checked.
     */
    protected boolean allFieldsChecked()
    {
        Object[] objs = tableList.getSelectedValues();
        if (objs != null && objs.length > 0)
        {
            for (Object obj : objs)
            {
                TableListItemIFace item = (TableListItemIFace)obj;
                if (item.isExpandable())
                {
                    TableInfo tableInfo = (TableInfo)tableList.getSelectedValue();
                    for (FieldInfo fieldInfo : tableInfo.getFieldItems())
                    {
                        if (!fieldInfo.isChecked())
                        {
                            return false;
                        }
                    }
                } else
                {
                    if (!item.isChecked())
                    {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    /**
     * @param currentMap
     * @return true if a mapping to the 'Taxon Import Only' table exists.
     */
    protected boolean taxonOnlyInUse(final FieldMappingPanel currentMap)
    {
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() == 4000)
    		{
    			return true;
    		}
    	}
    	return false;
    }

    /**
     * @param currentMap
     * @return true if all mappings are to the 'Taxon Import Only' table.
     */
    protected boolean onlyTaxonOnlyInUse(final FieldMappingPanel currentMap)
    {
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() != 4000)
    		{
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * @param currentMap
     * @return the lowest (i.e. highest numbered) taxon rank mapped from the Taxon table.
     */
    protected int getLowestMappedTaxonRank(final FieldMappingPanel currentMap)
    {
    	int low = -1;
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() == 4)
    		{
    			int rank = getRank(fmp.getFieldInfo());
    			if (rank > low)
    			{
    				low = rank;
    			}
    		}
    	}
    	return low;
    }

    /**
     * @param currentMap
     * @return the highest (i.e. lowest numbered) taxon rank mapped from the Determination table.
     */
    protected int getHighestMappedDetRank(final FieldMappingPanel currentMap)
    {
    	int high = 600000;
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getClassObj().equals(Determination.class))
    		{
    			int rank = getRank(fmp.getFieldInfo());
    			if (rank < high)
    			{
    				high = rank;
    			}
    		}
    	}
    	return high;
    }

    /**
     * @param fi 
     * @return the Taxonomic rank for fi if fi is a Taxonomic rank field (currently only the name for a rank - not Author, CommonName...),
     * otherwise returns -1
     *      
     *      */
    protected int getRank(final FieldInfo fi)
    {
    	if (taxRanks == null)
    	{
    		taxRanks = TaxonTreeDef.getStandardLevelsStatic();
    	}
    	String fldName = fi.getFieldInfo().getName();
    	if (fi.getTableinfo().getClassObj().equals(Determination.class))
    	{
    		//strip off trailing 1 or 2
    		fldName = fldName.substring(0, fldName.length()-1); 
    	}
    	for (TreeDefItemStandardEntry rank : taxRanks)
    	{
    		if (fldName.equalsIgnoreCase(rank.getName()))
    		{
    			return rank.getRank();
    		}
    	}
    	return -1;
    }
    
    /**
     * @param fi
     * @return true if fi is mappable
     * 
     * Checks for conflicts caused by availability of Taxon ranks in multiple tables.
     */
    protected String isMappable(final FieldInfo fi, final FieldMappingPanel currentMap)
    {
    	if (taxonOnlyInUse(currentMap) && fi.getTableinfo().getTableId() != 4000)
    	{
    		return UIRegistry.getResourceString("TemplateEditor.TaxonOnly"); //XXX i18n
    	}
    	if (fi.getTableinfo().getClassObj().equals(Determination.class))
    	{
    		int low = getLowestMappedTaxonRank(currentMap);
    		int rank = getRank(fi);
    		if (rank <= low && rank != -1)
    		{
    			return UIRegistry.getResourceString("TemplateEditor.DetRanksLowerThanTaxRanks"); //XXX i18n 
    		}
    	}
    	if (fi.getTableinfo().getClassObj().equals(Taxon.class))
    	{
    		if (fi.getTableinfo().getTableId() == 4)
    		{
        		int high = getHighestMappedDetRank(currentMap);
        		int rank = getRank(fi);
        		if (rank >= high && rank != -1)
        		{
        			return UIRegistry.getResourceString("TemplateEditor.TaxRanksHigherThanDetRanks"); //XXX i18n 
        		}
    		}
    		if (fi.getTableinfo().getTableId() == 4000)
    		{
    			if (!onlyTaxonOnlyInUse(currentMap))
    			{
    				return UIRegistry.getResourceString("TemplateEditor.TaxonOnlyOnly"); //XXX i18n
    			}
    		}
    	}
    	return null;
    }
    
    /**
     * Adds one or more items from the Schema List to the mapping list. 
     * If a table is selected it adds all unchecked items.
     */
    protected void addNewMappingItems()
    {
        Object[] objs = fieldList.getSelectedValues();
        if (objs != null && objs.length > 0)
        {
            Vector<FieldInfo> toAdd = new Vector<FieldInfo>();
        	for (Object obj : objs)
            {
                TableListItemIFace item = (TableListItemIFace)obj;
                if (!item.isChecked())
                {
                    String errMsg = isMappable((FieldInfo)item, null);
                    if (errMsg == null)
                    {
                    	toAdd.add((FieldInfo )item);                    
                    }
                    else
                    {
                    	UIRegistry.displayErrorDlg(errMsg);
                    	toAdd.clear();
                    	break;
                    }
                }
            }
        	for (FieldInfo fld : toAdd)
        	{
            	FieldMappingPanel fmp = addNewMapItem(fld, null);
            	fmp.getArrowLabel().setVisible(true);
            	fmp.getArrowLabel().setIcon(IconManager.getIcon("LinkedRight"));
        	}
        }
    }
    
    /**
     * Adds a new Column to the Template that is not represented by a row in a file (if there is a file). 
     * @param fieldInfoArg the field info item
     * @param wbtmi the template item
     * @return the FieldMappingPanel item that was added
     */
    @SuppressWarnings("cast")
    protected FieldMappingPanel addNewMapItem(final FieldInfo fieldInfoArg, 
                                              final WorkbenchTemplateMappingItem wbtmi)
    {
        FieldInfo fieldInfo  = fieldInfoArg == null ? (FieldInfo)tableList.getSelectedValue() : fieldInfoArg;

        fieldInfo.setChecked(true);
        
        Class<?>          tableClass = getTableClass(fieldInfo);
        ImportColumnInfo  colInfo    = new ImportColumnInfo((short)-1, 
                                                            ImportColumnInfo.getType(tableClass), 
                                                            fieldInfo.getFieldInfo().getColumn(), 
                                                            fieldInfo.getText(), 
                                                            null,
                                                            null,
                                                            null);
        FieldMappingPanel fmp = addMappingItem(colInfo, IconManager.getIcon(fieldInfo.getTableinfo().getShortClassName().toLowerCase(), 
                IconManager.STD_ICON_SIZE), wbtmi);
        fmp.setFieldInfo(fieldInfo);
        fmp.setAdded(wbtmi == null); // new Items that was not in the data file.
        
        setChanged(true);
        
        fieldList.repaint();

        updateEnabledState();
        
        return fmp;
    }
    
    /**
     * Gets the Table class for a FieldInfo.
     * @param fieldInfo the fieldInfo
     * @return the class of it's owning table info object
     */
    protected Class<?> getTableClass(final FieldInfo fieldInfo)
    {
        Class<?> tableClass = null;
        try
        {
            Object newDbObj = fieldInfo.getTableinfo().getClassObj().newInstance();
            tableClass = PropertyUtils.getPropertyType(newDbObj, fieldInfo.getFieldInfo().getName());
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TemplateEditor.class, e);
            // we can't determine the class of the DB mapping, so assume String
            log.warn("Exception while looking up field type.  Assuming java.lang.String.",e);
            tableClass = String.class;
        }
        return tableClass;
    }
    
    /**
     * For a given Data Model Class it returns the TableInfo object for it. (This could be moved to the DBTableIdMgr).
     * @param classObj the class object
     * @return the table info
     */
    protected DBTableInfo getTableInfo(Class<?> classObj)
    {
        for (int i=0;i<tableModel.size();i++)
        {
            TableListItemIFace item = tableModel.getElementAt(i);
            if (item.isExpandable())
            {
                if (((TableInfo)item).getTableInfo().getClassObj() == classObj)
                {
                    return ((TableInfo)item).getTableInfo();
                }
            }
        }
        return null;
    }
    
    /**
     * Tries to find a Field Name in our Data Model from the column name of the data.
     * @param ti the TableInfo Object used to get all the mappable field names for the table.
     * @param fieldName the field name
     * @return TableFieldPair object representing the mappable Field for a Table
     */
    protected FieldInfo getFieldInfo(final DBTableInfo ti, final String fieldName)
    {
        if (ti != null)
        {
            String fldFieldName = fieldName.toLowerCase();
            for (int i=0;i<tableModel.size();i++)
            {
                TableListItemIFace item = tableModel.getElementAt(i);
                if (!item.isExpandable())
                {
                    FieldInfo fi = (FieldInfo)item;
                    //System.out.println("["+tblField.getFieldInfo().getColumn().toLowerCase()+"]["+fieldName.toLowerCase()+"]");
                    String tblFieldName = fi.getFieldInfo().getName().toLowerCase();
                    if (tblFieldName.equals(fldFieldName) || tblFieldName.startsWith(fldFieldName))
                    {
                        return fi;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * @param tbl
     * @param fld
     * @return FieldInfo where Table is tbl and Field is fld.
     */
    protected FieldInfo findFieldInfo(final String tbl, final String fld)
    {
        if (tbl == null || fld == null)
            return null;
        
        TableInfo tblInfo = null;
        for (int i=0; i<tableModel.size(); i++)
        {
            if (tableModel.getElementAt(i).getTableInfo().getShortClassName().equalsIgnoreCase(tbl))
            {
                tblInfo = tableModel.getElementAt(i);
                break;
            }
        }
        
        if (tblInfo == null)
            return null;
        
        for (FieldInfo fi : tblInfo.getFieldItems())
        {
            if (fi.getFieldInfo().getName().equalsIgnoreCase(fld))
                    return fi;
        }
        
        return null;
    }
    
    /**
     * @return list of tables in an order that which result in more 'desirable' auto-mappings.
     * 
     */
    protected Vector<TableInfo> getTablesForAutoMapping()
    {
    	//currently just makes sure Determination follows Taxon;
    	
    	Vector<TableInfo> result = new Vector<TableInfo>();
    	Integer detIndex = null;
    	Integer taxIndex = null;
    	Integer taxOnlyIndex = null;
    	for (int t = 0; t < tableModel.size(); t++)
    	{
    		TableInfo tbl = (TableInfo )tableModel.get(t);
    		if (tbl.getTableInfo().getClassObj().equals(Determination.class))
    		{
    			detIndex = t;
    		}
    		if (tbl.getTableInfo().getTableId() == 4)
    		{
    			taxIndex = t;
    		}
    		if (tbl.getTableInfo().getTableId() == 4000)
    		{
    			taxOnlyIndex = t;
    		}
    		result.add(tbl);
    	}
    	if (taxOnlyIndex != null && taxOnlyIndex.intValue() != tableModel.size()-1)
    	{
    		TableInfo taxOnly = result.remove(taxOnlyIndex.intValue());
    		result.add(taxOnly);
    		if (detIndex != null && detIndex.intValue() > taxOnlyIndex)
    		{
    			detIndex += 1;
    		}
    		if (taxIndex != null && taxIndex.intValue() > taxOnlyIndex)
    		{
    			taxIndex += 1;
    		}
    	}
    	if (detIndex != null && taxIndex != null && detIndex.intValue() < taxIndex.intValue())
    	{
    		TableInfo det = result.remove(detIndex.intValue());
    		result.add(taxIndex, det);
    	}
    	return result;
    }
    /**
     * Automaps a filed name to the Specify Schema
     * @param fieldNameArg the field name
     * @return the Table Field Pair
     */
    protected FieldInfo autoMapFieldName(final String fieldNameArg, List<Pair<String,TableFieldPair>> automappings, Set<FieldInfo> previouslyMapped)
    {
        String fieldNameLower     = fieldNameArg.toLowerCase();
        String fieldNameLowerNoWS = StringUtils.deleteWhitespace(fieldNameLower);
        
        FieldInfo fieldInfo  = null;
        
        // find the mapping that matches this column name
        for (Pair<String, TableFieldPair> mapping: automappings)
        {
            //System.out.println("["+fieldName+"]["+mapping.first+"]");
            if (fieldNameLowerNoWS.matches(mapping.first))
            {
                TableFieldPair tblFldPair = mapping.second;
                //fieldInfo = new FieldInfo(tblFldPair.getTableinfo(),tblFldPair.getFieldInfo());
                //System.out.println("["+fieldInfo.hashCode()+"]["+tblFldPair.getTableinfo().hashCode()+"]["+tblFldPair.getFieldInfo().hashCode()+"]");
                log.debug("Mapping incoming column name '" + fieldNameArg +
                        "' to " + tblFldPair.getTableinfo().getName() +
                        "." + tblFldPair.getFieldInfo().getName());
                for (int i=0;i<tableModel.size();i++)
                {
                    TableInfo tblInfo = tableModel.getElementAt(i);
                    for (FieldInfo fi : tblInfo.getFieldItems())
                    {
                        if (fi.getFieldInfo() == tblFldPair.getFieldInfo())
                        {
                            //System.out.println("["+fi.hashCode()+"]["+tblFldPair.getTableinfo().hashCode()+"]["+tblFldPair.getFieldInfo().hashCode()+"]");
                            fieldInfo = fi;
                            break;
                        }
                    }
                    if (fieldInfo != null) break;
                }
            }
            if (fieldInfo != null) break;
        }
        
        // If we had no luck then just loop through everything looking for it.
        if (fieldInfo == null)
        {
            Vector<TableInfo> tbls = getTablesForAutoMapping(); 
        	for (TableInfo tblInfo : tbls)
            {
                for (FieldInfo fi : tblInfo.getFieldItems())
                {
                    DBFieldInfo dbFieldInfo = fi.getFieldInfo();

                    String tblFieldName = dbFieldInfo.getName().toLowerCase();
                    String tblColumnName = dbFieldInfo.getColumn().toLowerCase();

                    // System.out.println("["+tblFieldName+"]["+fieldNameLower+"]");
                    if (tblFieldName.equals(fieldNameLower) || tblColumnName.equals(fieldNameLower)
                            || tblColumnName.equals(fieldNameLowerNoWS)
                            || tblFieldName.equals(fieldNameLowerNoWS)
                            || tblFieldName.startsWith(fieldNameLower)
                            || tblColumnName.startsWith(fieldNameLower)) 
                    { 
                        fieldInfo = fi;
                        break;
                    }
                }
                if (fieldInfo != null)
                {
                    break;
                }
            }
        }
        
        //check to see if a mapping to fieldInfo has already been made.
        if (fieldInfo != null && previouslyMapped.add(fieldInfo))
        {
            return fieldInfo;
        }
        return null;
    }
    
    /**
     * Automap by column from the data's columns to the Data Model. 
     */
    protected void autoMapFromDataFile(Vector<ImportColumnInfo> colInfos)
    {
        Hashtable<TableInfo, Boolean> tablesInUse = new Hashtable<TableInfo, Boolean>();
        
        // XXX
        // TODO: 'discover' this somehow (System.getProperty() ? )
        String currentLocale = "en";
        
        // don't use a hashtable for this since it doesn't guarantee iterator order
        // and we want the order of these mappings in the file to be the order they are
        // checked
        List<Pair<String,TableFieldPair>> automappings = new Vector<Pair<String,TableFieldPair>>();

        // read in the automapping config file
        Element mappingsInXml = null;
        try
        {
            mappingsInXml = XMLHelper.readDOMFromConfigDir("datamodel_automappings.xml");
        }
        catch (Exception e)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TemplateEditor.class, e);
            log.error("Failed to read/parse automapping config file", e);
            return;
        }
        
        // read in all of the mappings
        List<?> mappings = mappingsInXml.selectNodes("//mapping");
        for (Object o: mappings)
        {
            Element mapping = (Element)o;
            String className = XMLHelper.getValue(mapping, "class");
            String fieldName = XMLHelper.getValue(mapping, "field");
            DBTableInfo table = databaseSchema.getByClassName(className);
            DBFieldInfo field = table.getFieldByName(fieldName);
            
            TableFieldPair tblFldPair = new TableFieldPair(table,field);

            for (Object regO: mapping.selectNodes("regex"))
            {
                Element regex = (Element)regO;
                String regexStr = regex.getText();
                String locale   = XMLHelper.getAttr(regex, "locale", "en");
                
                // only grab the mappings for the current locale
                if (locale.equalsIgnoreCase(currentLocale))
                {
                    automappings.add(new Pair<String, TableFieldPair>(regexStr,tblFldPair));
                    log.debug("Registering regex (" + regexStr + ") as match for " + className + "." + fieldName);
                }
            }
        }
        
        boolean notAllMapped = false;  // assume we can auto map everything
        Set<FieldInfo> mappedFlds = new HashSet<FieldInfo>();  //used to prevent duplicate mappings.
        for (ImportColumnInfo colInfo: colInfos)
        {
            if (!colInfo.getIsSystemCol())
            {
                //Try to find FieldInfo from mapping stored in colInfo. Automap if not found.
                FieldInfo fieldInfo = findFieldInfo(colInfo.getMapToTbl(), colInfo.getMapToFld()); 
                if (fieldInfo == null) 
                    fieldInfo = autoMapFieldName(colInfo.getColTitle(), automappings, mappedFlds);
                
                if (fieldInfo != null)
                {
                    TableInfo tblInfo = null;
                    // Find the right TableInfo
                    for (int i=0;i<tableModel.size();i++)
                    {
                        TableListItemIFace item = tableModel.getElementAt(i);
                        if (item.isExpandable() && ((TableInfo)item).getTableInfo() == fieldInfo.getTableinfo())
                        {
                            tblInfo = (TableInfo)item;
                            tablesInUse.put(tblInfo, true);
                            break;
                        }
                    }

                    if (tblInfo == null)
                    {
                        throw new RuntimeException("Couldn't find table info for fieldinfo.");
                    }

                    map(null, colInfo, tblInfo, fieldInfo, null);

                } else
                {
                    notAllMapped = true; // oops, couldn't find a mapping for something
                    FieldMappingPanel fmp = new FieldMappingPanel(colInfo, blankIcon);
                    fmp.getArrowLabel().setVisible(false);
                    mapModel.add(fmp);
                }
            }
        }
        
        if (!notAllMapped)
        {
            okBtn.setEnabled(false);
        }
        
        adjustMappings();
        updateEnabledState();
    }
    
    
    /**
     * @return set of distinct TableIds in the mapModel 
     */
    protected Set<Integer> getTblsMapped()
    {
        Set<Integer> result = new HashSet<Integer>();
        for (int m=0; m<mapModel.getSize(); m++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(m);
            if (fmp != null && fmp.getFieldInfo() != null)
            {
                result.add(fmp.getFieldInfo().getTableinfo().getTableId());
            }
        }
        
        return result;
    }
    
    /**
     * Modify mappings to be more user-friendly.
     * Currently, just handles situation where only Taxon fields are contained in the import file - remaps Taxon and Determination
     * fields to Taxon Only equivalents.
     * Currently, very very klugey. Totally dependent on current behavior of automapper functions, and
     * on workbench_datamodel schema. 
     */
    protected void adjustMappings()
    {
        boolean doTaxOnlyRemap = true;
        for (Integer tblId : getTblsMapped())
        {
        	if (!(tblId.equals(DBTableIdMgr.getInstance().getByClassName(Taxon.class.getName()).getTableId())
        			|| tblId.equals(DBTableIdMgr.getInstance().getByClassName(Determination.class.getName()).getTableId())
        			|| tblId.equals(4000)))
        	{
        		doTaxOnlyRemap = false;
        		break;
        	}
        }
        if (doTaxOnlyRemap)
        {
            log.debug("remapping for taxon-only import");
            TableInfo taxaOnly = null;
            for (int i=0;i<tableModel.size();i++)
            {
                TableListItemIFace item = tableModel.getElementAt(i);
                if (item.isExpandable() && ((TableInfo)item).getTableInfo().getTableId() == 4000)
                {
                    taxaOnly = (TableInfo)item;
                    break;
                }
            }

            if (taxaOnly == null)
            {
                log.warn("couldn't find taxon-only table in workbench schema");
                return;
            }
            for (int m=0; m<mapModel.getSize(); m++)
            {
                FieldMappingPanel fmp = mapModel.getElementAt(m);
                String fldName = fmp.getFieldName();
                if (fldName.endsWith("1") || fldName.endsWith("2"))
                {
                    fldName = fldName.substring(0, fldName.length()-1);
                }
                FieldInfo newInfo = null;
                System.out.println("re-mapping " + fldName);
                for (FieldInfo fi : taxaOnly.getFieldItems())
                {
                    System.out.println("  checking " + fi.getFieldInfo().getName());
                	if (fi.getFieldInfo().getName().equalsIgnoreCase(fldName))
                    {
                        newInfo = fi;
                        break;
                    }
                }   
                if (newInfo == null)
                {
                    log.warn("Couldn't find Taxon Only field info for " + fldName);
                    continue;
                }             
                
                fmp.getFieldInfo().setInUse(false);
                newInfo.setInUse(true);
                fmp.setFieldInfo(newInfo);
                fmp.setIcon(DBTableIdMgr.getInstance().getByClassName(Taxon.class.getName()).getIcon(IconManager.STD_ICON_SIZE));
            }
        }
    }
    
    /**
     * Fill the UI from a WorkbenchTemplate. 
     */
    protected void fillFromTemplate()
    {
        doingFill = true;
        
        // Map the TableInfo's Table ID to it's index in the Vector
        Hashtable<Integer, Integer> tblIdToListIndex = new Hashtable<Integer, Integer>();
        for (int i=0;i<tableModel.size();i++)
        {
            TableListItemIFace ti = tableModel.getElementAt(i);
            if (ti.isExpandable())
            {
                tblIdToListIndex.put(((TableInfo)ti).getTableInfo().getTableId(), i);
            }
        }
        
        // Get and Sort the list of WBTMIs
        Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(workbenchTemplate.getWorkbenchTemplateMappingItems());
        Collections.sort(items);
        
        Hashtable<TableInfo, Boolean> tablesInUse = new Hashtable<TableInfo, Boolean>();
        for (WorkbenchTemplateMappingItem  wbtmi : items)
        {
            int       inx = tblIdToListIndex.get(wbtmi.getSrcTableId());
            TableInfo ti  = tableModel.getElementAt(inx);
            
            int fieldNum = 0;
            for (FieldInfo fi : ti.getFieldItems())
            {
                if (wbtmi.getFieldName().equals(fi.getFieldInfo().getName()))
                {
                    addNewMapItem(fi, wbtmi);
                    tablesInUse.put(ti, true);
                    break;
                }
                fieldNum++;
            }
        }
        doingFill = false;
        
        mapList.getSelectionModel().clearSelection();
        tableList.getSelectionModel().clearSelection();
    }
    
    /**
     * XXX FIX me currently updates everything, not just the changed ones.
     * @return updates all the templates for changes and returns the new template items.
     */
    public Collection<WorkbenchTemplateMappingItem> updateAndGetNewItems()
    {
        Vector<WorkbenchTemplateMappingItem> newItems = new Vector<WorkbenchTemplateMappingItem>();
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            if (fmp.getFieldInfo() != null)
            {
                WorkbenchTemplateMappingItem item;
                FieldInfo                    fieldInfo  = fmp.getFieldInfo();
                short                        origColNum = -1;
                if (fmp.getWbtmi() == null)
                {
                    ImportColumnInfo colInfo = fmp.getColInfo();
                    item = new WorkbenchTemplateMappingItem();
                    item.initialize();
                
                    item.setCaption(colInfo.getColName());
                    item.setImportedColName(colInfo.getColName());
                    origColNum = fmp.isAdded() ? -1 : colInfo.getColInx();
                    newItems.add(item);
                    
                } else
                {
                    item = fmp.getWbtmi();
                    //item.setCaption(fieldInfo.getTitle()); // removed lines for Bug 4833
                    //item.setImportedColName(null);
                }
                
                item.setFieldName(fieldInfo.getFieldInfo().getName());
                item.setSrcTableId(fieldInfo.getTableinfo().getTableId());
                item.setTableName(fieldInfo.getTableinfo().getName());
                short len = (short)fieldInfo.getFieldInfo().getLength();
                item.setDataFieldLength(len == -1 ? 15 : len);
                
                item.setViewOrder(fmp.getViewOrder());
                item.setOrigImportColumnIndex(origColNum);
                
            }
        }

        return newItems;
    }
    
    /**
     * @return
     */
    public Collection<WorkbenchTemplateMappingItem> getDeletedItems()
    {
        Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>();
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            //System.out.println("getUpdatedItems "+fmp.getWbtmi().getCaption()+" "+i+" "+fmp.getViewOrder()+" - "+fmp.getWbtmi().getViewOrder());
            if (fmp.getFieldInfo() == null && fmp.getWbtmi() != null)
            {
                items.add(fmp.getWbtmi());
            }
        }
        return items;
    }

    
    /**
     * Returns the WorkbenchTemplate for the Mappings.
     * @return the WorkbenchTemplate for the Mappings.
     */
    /*public WorkbenchTemplate createWorkbenchTemplate()
    {
        WorkbenchTemplate wbTemplate = new WorkbenchTemplate();
        wbTemplate.initialize();
        
        wbTemplate.setSpecifyUser(AppContextMgr.getInstance().getClassObject(SpecifyUser.class));
        
        Set<WorkbenchTemplateMappingItem> items = wbTemplate.getWorkbenchTemplateMappingItems();
        
        short order = 0;
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp       = mapModel.getElementAt(i);
            ImportColumnInfo  colInfo   = fmp.getColInfo();
            FieldInfo         fieldInfo = fmp.getFieldInfo();
            
            WorkbenchTemplateMappingItem item = new WorkbenchTemplateMappingItem();
            item.initialize();
            
            item.setCaption(colInfo.getColName());
            item.setFieldName(fieldInfo.getFieldInfo().getName());
            item.setImportedColName(colInfo.getColName());
            item.setSrcTableId(fieldInfo.getTableinfo().getTableId());
            item.setTableName(fieldInfo.getTableinfo().getTableName());
            short len = (short)fieldInfo.getFieldInfo().getLength();
            item.setDataFieldLength(len == -1 ? 15 : len);
            
            //log.error(tblField.getFieldInfo().getLength()+"  tblID: "+tblField.getTableinfo().getTableId());
            
            item.setViewOrder(order);
            item.setOrigImportColumnIndex(fmp.isAdded() ? -1 : colInfo.getColInx());
            order++;
            item.setWorkbenchTemplate(wbTemplate);
            
            items.add(item);

        }
        
        return wbTemplate;
    }*/

    
    //------------------------------------------------------------
    //- The Panel that is used to display each Data File Column
    // and if it is mapped.
    //------------------------------------------------------------
    class FieldMappingPanel extends JPanel
    {
        protected String            noMappingStr   = getResourceString("WB_NO_MAPPING");

        protected boolean           isSelected      = false;
        protected Color             bgColor       = null;
        protected JLabel            colFieldLabel;
        protected JLabel            schemaLabel;
        protected JLabel            iconLabel;
        protected JLabel            arrowLabel;
        protected ImageIcon         mappingIcon;
        
        protected Short             viewOrder     = null;
        
        protected FieldInfo         fieldInfo     = null;
        protected ImportColumnInfo  colInfo       = null;
        protected boolean           isAdded       = false;
        
        protected boolean           isNew         = false;
        
        protected WorkbenchTemplateMappingItem wbtmi = null;
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param mappingIcon the mappingIcon to use once it is mapped
         * @param fromScratch the mapping is from scratch so use the "brief" display
         */
        public FieldMappingPanel(final ImportColumnInfo colInfo, 
                                 final ImageIcon        mappingIcon)
        {
            this.colInfo = colInfo;
            setBackground(Color.WHITE);
            

            PanelBuilder    builder = new PanelBuilder(new FormLayout("2px,p,2px,150px, p:g, p, p:g, 150px,2px", "p:g"), this);
            CellConstraints cc      = new CellConstraints();

            colFieldLabel = createLabel(colInfo != null ? colInfo.getColTitle() : "", SwingConstants.RIGHT);
            schemaLabel   = createLabel(noMappingStr, SwingConstants.LEFT);
            arrowLabel    = new JLabel(IconManager.getIcon("Linked"));
   
            setFocusable(true);
            
            builder.add(iconLabel = new JLabel(mappingIcon), cc.xy(2,1));
            setIcon(mappingIcon);
            builder.add(schemaLabel, cc.xy(4,1));
            builder.add(arrowLabel, cc.xy(6,1));
            builder.add(colFieldLabel, cc.xy(8,1));
        }
        
        /**
         * @param colInfo the colInfo to set
         */
        public void setColInfo(final ImportColumnInfo colInfo)
        {
            this.colInfo = colInfo;
            colFieldLabel.setText(colInfo != null ? colInfo.getColTitle() : "");
        }
        
        /**
         * @param text the new text for the label
         */
        public void setColFieldLabel(final String text)
        {
            colFieldLabel.setText(text);
        }

        /**
         * @return the wbtmi
         */
        public WorkbenchTemplateMappingItem getWbtmi()
        {
            return wbtmi;
        }

        /**
         * @return the arrowLabel
         */
        public JLabel getArrowLabel()
        {
            return arrowLabel;
        }

        /**
         * @param wbtmi the wbtmi to set
         */
        public void setWbtmi(final WorkbenchTemplateMappingItem wbtmi)
        {
            //setSchemaLabelVisible(wbtmi != null || isMappedToAFile);
            
            if (wbtmi != null && workbenchTemplate != null)
            {
                colFieldLabel.setText(wbtmi.getCaption());
            }

            this.wbtmi = wbtmi;
        }

        /**
         * @return the viewOrder
         */
        public Short getViewOrder()
        {
            if (wbtmi != null)
            {
                return wbtmi.getViewOrder();
            }
            // else
            return this.viewOrder;
        }

        /**
         * @param viewOrder the viewOrder to set
         */
        public void setViewOrder(Short viewOrder)
        {
            if (wbtmi != null)
            {
                wbtmi.setViewOrder(viewOrder);
            } else
            {
                this.viewOrder = viewOrder;
            }
        }

        /**
         * Sests a TableFieldPair into the item.
         * @param fieldInfoArg the new TableFieldPair
         */
        public void setFieldInfo(final FieldInfo fieldInfoArg) // make this FieldInfo
        {
            fieldInfo = fieldInfoArg;
            if (fieldInfoArg == null)
            {
                iconLabel.setIcon(blankIcon);
            }
            schemaLabel.setText(fieldInfoArg != null ? fieldInfoArg.getText() : noMappingStr);
            schemaLabel.repaint();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#getToolTipText()
         */
        @Override
        public String getToolTipText()
        {
            if (isNew)
            {
                return getResourceString("WB_NEW_REC_TT");
            }
            
            if (fieldInfo != null)
            {
                String name = fieldInfo.getFieldInfo().getTableInfo().getTitle();
                StringBuilder sb = new StringBuilder();
                //sb.append(name.substring(0, 1).toUpperCase());
                //sb.append(name.substring(1, name.length()));
                sb.append(name);
                sb.append(" - ");
                sb.append(fieldInfo.getTitle());
                return sb.toString();
            }
            // else
            return getResourceString("WB_NO_MAPPING_TT");
        }
        
        public void setSchemaLabelVisible(final boolean isVis)
        {
            schemaLabel.setVisible(isVis);
        }

        /**
         * Sets the object to have focus.
         * @param isSelected true/false
         */
        public void setSelected(final boolean isSelected)
        {
            if (bgColor == null)
            {
                bgColor = getBackground();
            }
            
            this.isSelected = isSelected;
            
            if (UIHelper.getOSType() == UIHelper.OSTYPE.Windows)
            {
                colFieldLabel.setForeground(isSelected ? Color.WHITE : Color.BLACK);
                schemaLabel.setForeground(isSelected ? Color.WHITE : Color.BLACK);
            }
            setBackground(isSelected ? tableList.getSelectionBackground() : bgColor);
        }

        public void setIcon(ImageIcon mappingIcon)
        {
            this.mappingIcon = mappingIcon == null ? blankIcon : mappingIcon;
            iconLabel.setIcon(this.mappingIcon);
        }

        /**
         * @return the TableInfo object
         */
        public FieldInfo getFieldInfo()
        {
            return fieldInfo;
        }
        
        /**
         * Returns the ColumnInfo object.
         * @return the ColumnInfo object.
         */
        public ImportColumnInfo getColInfo()
        {
            return colInfo;
        }

        /**
         * Returns the field name.
         * @return the field name.
         */
        public String getFieldName()
        {
            return colFieldLabel.getText();
        }

        /**
         * Returns whether this is a new item, which means it was NOT in the data file.
         * @return  whether this is a new item, which means it was NOT in the data file.
         */
        public boolean isAdded()
        {
            return isAdded;
        }

        /**
         * Sets whether it is a new item, that wasn't in the data file.
         * @param isAdded true its new, false it was from the data file.
         */
        public void setAdded(boolean isAdded)
        {
            this.isAdded = isAdded;
        }

        /**
         * @return the isNew
         */
        public boolean isNew()
        {
            return isNew;
        }

        /**
         * @param isNew the isNew to set
         */
        public void setNew(boolean isNew)
        {
            this.isNew = isNew;
            if (isNew)
            {
                schemaLabel.setText(getResourceString("WB_NEW_REC"));
                schemaLabel.setToolTipText(getResourceString("WB_NEW_REC_TT"));

                schemaLabel.setVisible(true);
                arrowLabel.setVisible(false);
            }
        }
    }
    
    //------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------
    class MapCellRenderer implements ListCellRenderer
    {
        private final Border noFocusBorder        = new EmptyBorder(1, 1, 1, 1);
        private final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
        
        /* (non-Javadoc)
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList   list,
                                                      Object  value,
                                                      int     index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            FieldMappingPanel panel = (FieldMappingPanel)value;
            panel.setSelected(isSelected);
            panel.setToolTipText(panel.getToolTipText());
            
            Border border = null;
            if (cellHasFocus)
            {
                if (isSelected)
                {
                    border = UIManager.getBorder("List.focusSelectedCellHighlightBorder");
                }
                if (border == null)
                {
                    border = UIManager.getBorder("List.focusCellHighlightBorder");
                }
            } else
            {
                border = getNoFocusBorder();
            }
            panel.setBorder(border);
            return (Component)value;
        }
        
        private Border getNoFocusBorder() 
        {
            if (System.getSecurityManager() != null) 
            {
                return SAFE_NO_FOCUS_BORDER;
            }
            // else
            return noFocusBorder;
        }
        
    }

    /**
     * @return the isReadOnly
     */
    public boolean isReadOnly()
    {
        return isReadOnly;
    }

    /**
     * @param isReadOnly the isReadOnly to set
     */
    public void setReadOnly(final boolean isReadOnly)
    {
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                TemplateEditor.this.isReadOnly = isReadOnly;
                mapToBtn.setVisible(!isReadOnly);
                unmapBtn.setVisible(!isReadOnly);
                btnPanel.setVisible(!isReadOnly);
                upBtn.setVisible(!isReadOnly);
                downBtn.setVisible(!isReadOnly);
                getOkBtn().setVisible(!isReadOnly);
                getCancelBtn().setText(getOkBtn().getText()); //XXX need to resize??
            }
        });
    }
    
    
}
