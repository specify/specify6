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
package edu.ku.brc.specify.tasks.subpane.wb;

import static edu.ku.brc.ui.UIHelper.createIconBtn;
import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
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

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.TableFieldPair;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DefaultModifiableListModel;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;
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
    
    protected Vector<WorkbenchTemplateMappingItem> deletedItems = new Vector<WorkbenchTemplateMappingItem>();

    
    protected boolean                        hasChanged        = false;
    protected boolean                        doingFill         = false;
    protected Color                          btnPanelColor;
    protected JPanel                         btnPanel;
    
    protected ImportDataFileInfo             dataFileInfo      = null;
    protected WorkbenchTemplate              workbenchTemplate = null;
    protected DBTableIdMgr                   databaseSchema;
    
    protected boolean                        isMappedToAFile;
    protected boolean                        isEditMode;
    protected boolean                        ignoreMapListUpdate = false;
    
    protected ImageIcon                      blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    
    protected TableInfoListRenderer          tableInfoListRenderer;
    
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
        
        helpContext = dataFileInfo == null ? "WorkBenchNewTemplate" : "WorkBenchImportData";
        
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
        
        helpContext = this.isMappedToAFile ? "WorkBenchImportTemplateEditor" : "WorkBenchTemplateEditing";
        
        createUI();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        
        boolean isFromScratch = !isMappedToAFile && workbenchTemplate == null;
        boolean isNewTemplate = workbenchTemplate == null;
        
        databaseSchema = WorkbenchTask.getDatabaseSchema();
        
        // Create the Table List
        Vector<TableInfo> tableInfoList = new Vector<TableInfo>();
        for (DBTableIdMgr.TableInfo ti : databaseSchema.getList())
        {
            if (StringUtils.isNotEmpty(ti.toString()))
            {
                TableInfo tableInfo = new TableInfo(ti, IconManager.IconSize.Std16);
                tableInfoList.add(tableInfo); 
                
                Vector<FieldInfo> fldList = new Vector<FieldInfo>();
                for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                {
                    fldList.add(new FieldInfo(ti, fi));
                }
                Collections.sort(fldList);
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
        tableList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.IconSize.Std16));
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
        fieldList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.IconSize.Std16));
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
                        
                        FieldInfo fldInfo = (FieldInfo)fmp.getFieldInfo();
                        if (fldInfo != null)
                        {
                            for (int i=0;i<tableModel.size();i++)
                            {
                                TableInfo tblInfo = (TableInfo)tableModel.get(i);
                                if (fldInfo.getTableinfo() == tblInfo.getTableInfo())
                                {
                                    tableList.setSelectedValue(tblInfo, true);
                                    fillFieldList(tblInfo);
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
        
        if (isMappedToAFile)
        {
            if (isNewTemplate) // New Template being mapped from a file
            {
                mapListLeftLabel  = fieldsLabel;
                mapListRightLabel = getResourceString("WB_COLUMNS");

            } else // Editing a template mapped from a file
            {
                mapListLeftLabel  = fieldsLabel;
                mapListRightLabel = getResourceString("WB_COLUMNS");
            }
            
        } else 
        {
            if (isFromScratch) // Creatingd a brand new template from Scratch
            {
                mapListLeftLabel  = fieldsLabel;
                mapListRightLabel = getResourceString("WB_COLUMNS");
                
            } else // Editing Template that wasn't mapped
            {
                mapListLeftLabel  = fieldsLabel;
                mapListRightLabel = getResourceString("WB_COLUMNS");                
            }
        }
        
        CellConstraints cc = new CellConstraints();
        
        JPanel mainLayoutPanel = new JPanel();
        
        PanelBuilder labelsBldr = new PanelBuilder(new FormLayout("p, f:p:g, p", "p"));        
        labelsBldr.add(new JLabel(mapListLeftLabel, SwingConstants.LEFT), cc.xy(1, 1));
        labelsBldr.add(new JLabel(mapListRightLabel, SwingConstants.RIGHT), cc.xy(3, 1));

        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(upBtn,    cc.xy(1, 2));
        upDownPanel.add(downBtn,  cc.xy(1, 4));

        PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, f:p:g"));
        PanelBuilder middlePanel = new PanelBuilder(new FormLayout("p", "p, 2px, p"));
        middlePanel.add(mapToBtn, cc.xy(1, 1));
        middlePanel.add(unmapBtn, cc.xy(1, 3));
        
        btnPanel = middlePanel.getPanel();
        outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
        
        // Main Pane Layout
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(200px;p):g, 5px, max(200px;p), 5px, p:g, 5px, f:max(250px;p):g, 2px, p", 
                                                                  "p, 2px, f:max(350px;p):g"), mainLayoutPanel);
        
        builder.add(new JLabel(dataTypeLabel, SwingConstants.CENTER), cc.xy(1, 1));
        builder.add(new JLabel(fieldsLabel,   SwingConstants.CENTER), cc.xy(3, 1));
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
        
        FieldMappingPanel fmp = addMappingItem(null, IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24), null);
        fmp.setAdded(true);
        fmp.setNew(true);
        
        pack();
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                cancelBtn.requestFocus();
                fieldModel.clear();
                updateEnabledState();
            }
        });
    }
    
    
    /**
     * Fill in the JList's model from the list of fields.
     * @param tableInfo the table who's list we should use
     */
    protected void fillFieldList(final TableInfo tableInfo)
    {
        DefaultListModel model = (DefaultListModel)fieldList.getModel();
        model.clear();
        
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
                fmp.setViewOrder((short)inx);
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
            
            FieldMappingPanel newFmp = addMappingItem(null, IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24), null);
            newFmp.setAdded(true);
            newFmp.setNew(true);
            
        } else
        {
            ImportColumnInfo  colInfo = fmp.getColInfo();
            FieldInfo         fi      = (FieldInfo)fieldList.getSelectedValue();
            TableInfo         tblInfo = (TableInfo)tableList.getSelectedValue();
            
            map(fmp, colInfo, tblInfo, fi, fmp.getWbtmi());
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
        fmp.setIcon(tableInfo.getTableInfo().getIcon(IconManager.IconSize.Std24));
        
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
     * Adds one or more items from the Schema List to the mapping list. 
     * If a table is selected it adds all unchecked items.
     */
    protected void addNewMappingItems()
    {
        Object[] objs = fieldList.getSelectedValues();
        if (objs != null && objs.length > 0)
        {
            for (Object obj : objs)
            {
                TableListItemIFace item = (TableListItemIFace)obj;
                if (!item.isChecked())
                {
                    FieldMappingPanel fmp = addNewMapItem((FieldInfo)item, null);
                    fmp.getArrowLabel().setVisible(true);
                    fmp.getArrowLabel().setIcon(IconManager.getIcon("LinkedRight"));
                }
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
                                                            null);
        FieldMappingPanel fmp = addMappingItem(colInfo, IconManager.getIcon(fieldInfo.getTableinfo().getObjTitle(), IconManager.IconSize.Std24), wbtmi);
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
    protected DBTableIdMgr.TableInfo getTableInfo(Class<?> classObj)
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
     * TRies to find a Field Name in our Data Model from the column name of the data.
     * @param ti the TableInfo Object used to get all the mappable field names for the table.
     * @param fieldName the field name
     * @return TableFieldPair object representing the mappable Field for a Table
     */
    protected FieldInfo getFieldInfo(final DBTableIdMgr.TableInfo ti, final String fieldName)
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
     * Automaps a filed name to the Specify Schema
     * @param fieldNameArg the field name
     * @return the Table Field Pair
     */
    protected FieldInfo autoMapFieldName(final String fieldNameArg, List<Pair<String,TableFieldPair>> automappings)
    {
        String fieldNameLower = fieldNameArg.toLowerCase();
        String fieldName      = StringUtils.deleteWhitespace(fieldNameLower);
        
        FieldInfo fieldInfo  = null;
        
        // find the mapping that matches this column name
        for (Pair<String,TableFieldPair> mapping: automappings)
        {
            //System.out.println("["+fieldName+"]["+mapping.first+"]");
            if (fieldName.matches(mapping.first))
            {
                TableFieldPair tblFldPair = mapping.second;
                fieldInfo = new FieldInfo(tblFldPair.getTableinfo(),tblFldPair.getFieldInfo());
                log.debug("Mapping incoming column name '" + fieldNameArg +
                        "' to " + tblFldPair.getTableinfo().getTableName() +
                        "." + tblFldPair.getFieldInfo().getName());
                break;
            }
        }
        
        // If we had no luck then just loop through everything looking for it.
        if (fieldInfo == null)
        {
            for (int i=0;i<tableModel.size();i++)
            {
                TableInfo tblInfo = (TableInfo)tableModel.getElementAt(i);
                for (FieldInfo fi : tblInfo.getFieldItems())
                {
                    String    tblFieldName = fi.getFieldInfo().getName().toLowerCase();
                    //System.out.println("["+tblFieldName+"]["+fieldNameLower+"]");
                    if (tblFieldName.equals(fieldNameLower) || tblFieldName.startsWith(fieldNameLower))
                    {
                        return fi;
                    }
                }
            }
        }
        
        return fieldInfo;
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
            DBTableIdMgr.TableInfo table = databaseSchema.getByClassName(className);
            DBTableIdMgr.FieldInfo field = table.getFieldByName(fieldName);
            
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
        for (ImportColumnInfo colInfo: colInfos)
        {
            FieldInfo fieldInfo = autoMapFieldName(colInfo.getColName(), automappings);
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
                
                FieldMappingPanel fmp = map(null, colInfo, tblInfo, fieldInfo, null);
                fmp.setIcon(IconManager.getIcon(fieldInfo.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
                
            } else
            {
                notAllMapped = true; // oops, couldn't find a mapping for something
                FieldMappingPanel fmp = new FieldMappingPanel(colInfo, blankIcon);
                fmp.getArrowLabel().setVisible(false);
                mapModel.add(fmp);
            }
        }
        
        if (!notAllMapped)
        {
            okBtn.setEnabled(false);
        }
        
        updateEnabledState();
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
            TableInfo ti  = (TableInfo)tableModel.getElementAt(inx);
            
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
     * @return
     */
    public Collection<WorkbenchTemplateMappingItem> getNewItems()
    {
        Vector<WorkbenchTemplateMappingItem> newItems = new Vector<WorkbenchTemplateMappingItem>();
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            if (fmp.getFieldInfo() != null && fmp.getWbtmi() == null)
            {
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
                
                item.setViewOrder(fmp.getViewOrder());
                item.setOrigImportColumnIndex(fmp.isAdded() ? -1 : colInfo.getColInx());
                newItems.add(item);
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
    public WorkbenchTemplate createWorkbenchTemplate()
    {
        WorkbenchTemplate wbTemplate = new WorkbenchTemplate();
        wbTemplate.initialize();
        
        wbTemplate.setSpecifyUser(SpecifyUser.getCurrentUser());
        
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
    }

    
    //------------------------------------------------------------
    //- The Panel that is used to display each Data File Column
    // and if it is mapped.
    //------------------------------------------------------------
    class FieldMappingPanel extends JPanel
    {
        protected String            noMappingStr = getResourceString("WB_NO_MAPPING");

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

            colFieldLabel = new JLabel(colInfo != null ? colInfo.getColName() : "", SwingConstants.RIGHT);
            schemaLabel   = new JLabel(noMappingStr, SwingConstants.LEFT);
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
            colFieldLabel.setText(colInfo != null ? colInfo.getColName() : "");
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
            schemaLabel.setText(fieldInfoArg != null ? fieldInfoArg.getFieldInfo().getColumn() : noMappingStr);
            schemaLabel.repaint();
        }
        
        /* (non-Javadoc)
         * @see javax.swing.JComponent#getToolTipText()
         */
        @Override
        public String getToolTipText()
        {
            if (fieldInfo != null)
            {
                String name = UIHelper.makeNamePretty(fieldInfo.getFieldInfo().getTableInfo().getClassObj().getSimpleName());
                StringBuilder sb = new StringBuilder();
                sb.append(name.substring(0, 1).toUpperCase());
                sb.append(name.substring(1, name.length()));
                sb.append(" - ");
                sb.append(fieldInfo.getTitle());
                return sb.toString();
            }
            
            return "";
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
                schemaLabel.setText("<New>"); // XXX I18N
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
            panel.setToolTipText(panel.getFieldInfo() != null ? panel.getToolTipText() : "");
            
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
        
        private Border getNoFocusBorder() {
            if (System.getSecurityManager() != null) {
                return SAFE_NO_FOCUS_BORDER;
            } else {
                return noFocusBorder;
            }
        }
        
    }
    
}
