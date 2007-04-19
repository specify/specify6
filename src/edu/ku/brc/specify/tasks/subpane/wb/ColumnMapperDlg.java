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

import static edu.ku.brc.ui.UICacheManager.getResourceString;
import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Hashtable;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.FieldNameRenderer;
import edu.ku.brc.specify.tasks.subpane.TableFieldPair;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.UIHelper;


/**
 * This panel is enables a user to make all the columns from a data file (XLS) to our Database schema.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Feb 16, 2007
 *
 */
public class ColumnMapperDlg extends CustomDialog
{
    private static final Logger log = Logger.getLogger(ColumnMapperDlg.class);
    
    protected Vector<TableInfo>              tableInfoList = new Vector<TableInfo>();
    protected JList                          fieldList;
    protected JList                          tableList;
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        addMapItemBtn;
    protected JButton                        removeMapItemBtn;
    
    protected boolean                        hasChanged  = false;
    protected boolean                        doingFill   = false;
    
    protected JPanel                         dataFileColPanel;
    protected Vector<FieldMappingPanel>      mappingItems      = new Vector<FieldMappingPanel>();
    protected Vector<FieldMappingPanel>      removedItems      = new Vector<FieldMappingPanel>();
    protected int                            currentInx        = -1;
    protected Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>> tableFieldList = new Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>>();
    
    protected ImportDataFileInfo             dataFileInfo      = null;
    protected WorkbenchTemplate              workbenchTemplate = null;
    protected DBTableIdMgr                   databaseSchema;
    
    protected boolean                        isMappedToAFile;
    
    protected ImageIcon checkMark   = IconManager.getIcon("Checkmark", IconManager.IconSize.Std16);
    protected ImageIcon blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    protected ImageIcon blankIcon16 = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std16);
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public ColumnMapperDlg(final Frame frame, final String title, final ImportDataFileInfo dataFileInfo)
    {
        super(frame, title, true, OKCANCELAPPLYHELP, null);
        
        this.dataFileInfo    = dataFileInfo;
        this.isMappedToAFile = dataFileInfo != null;
        
        helpContext = dataFileInfo == null ? "OnRampNewTemplate" : "OnRampImportData";
        
        createUI();
    }
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public ColumnMapperDlg(final Frame frame, final String title, final WorkbenchTemplate wbTemplate)
    {
        super(frame, title, true, OKCANCELAPPLYHELP, null);
        
        this.workbenchTemplate = wbTemplate;
        this.isMappedToAFile   = StringUtils.isNotEmpty(wbTemplate.getSrcFilePath());
        
        helpContext = this.isMappedToAFile ? "OnRampImportTemplateEditor" : "OnRampTemplateEditing";
        
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
        
        for (DBTableIdMgr.TableInfo ti : databaseSchema.getList())
        {
            if (StringUtils.isNotEmpty(ti.toString()))
            {
                tableInfoList.add(new TableInfo(ti)); 
                
                Vector<TableFieldPair> fldList = new Vector<TableFieldPair>();
                for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                {
                    fldList.add(new TableFieldPair(ti, fi));
                }
                Collections.sort(fldList);
                tableFieldList.put(ti, fldList);
            }
        }
        Collections.sort(tableInfoList);
        
        String          rowDef  = "top:p, 2px, top:p, 10px, p, 2px, f:p:g, 5px, p";
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(275px;p):g, 5px, p, 5px, p", rowDef));
        CellConstraints cc      = new CellConstraints();
        
        if (isMappedToAFile)
        {
            PanelBuilder header = new PanelBuilder(new FormLayout("p,f:p:g,p", "p,2px,p"));
            header.add(new JLabel(getResourceString("WB_MAPPING_COLUMNS"), SwingConstants.CENTER), cc.xywh(1, 1, 3, 1));
            header.add(new JLabel(getResourceString("WB_COLUMNS"), SwingConstants.LEFT), cc.xy(1,3)); // XXX I18N
            header.add(new JLabel(getResourceString("WB_SCHEMA"), SwingConstants.RIGHT), cc.xy(3,3));  // XXX I18N
            builder.add(header.getPanel(), cc.xy(1, 1));
            
        } else
        {
            builder.add(new JLabel(getResourceString("WB_SCHEMA"), SwingConstants.CENTER), cc.xy(1, 1));
        }
        builder.add(new JLabel(getResourceString("WB_DATAOBJECTS"), SwingConstants.CENTER), cc.xy(5, 1));
        builder.add(new JLabel(getResourceString("WB_DATAOBJ_FIELDS"),  SwingConstants.CENTER), cc.xy(5, 5));
        
        dataFileColPanel = new JPanel();
        dataFileColPanel.setBackground(Color.WHITE);
        dataFileColPanel.setLayout(new NavBoxLayoutManager(0,2,false));
        JScrollPane sp = new JScrollPane(dataFileColPanel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        PanelBuilder leftSide = new PanelBuilder(new FormLayout("f:p:g, p, 2px, p", "p"));        
        addMapItemBtn = createIconBtn("PlusSign", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addMapItem(true);
            }
        });
        removeMapItemBtn = createIconBtn("MinusSign", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                removeMapItem(null);
            }
        });
        leftSide.add(addMapItemBtn,    cc.xy(2, 1));
        leftSide.add(removeMapItemBtn, cc.xy(4, 1));
        
        builder.add(sp,       cc.xywh(1, 3, 1, 5));
        builder.add(leftSide.getPanel(), cc.xy(1, 9));
        
        tableList = new JList(tableInfoList);
        tableList.setCellRenderer(new TableNameRenderer(IconManager.IconSize.Std24));
        
        sp = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(5, 3));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    Object selObj = tableList.getSelectedValue();
                    if (selObj != null)
                    {
                        fillFieldList(((TableInfo)selObj).getTableInfo());
                    }
                }
            }
        });
        
        PanelBuilder arrowBuilder = new PanelBuilder(new FormLayout("f:p:g", "p:g,p,2px,p,p:g"));
        mapToBtn = new JButton(IconManager.getIcon("move_left"));
        mapToBtn.setMargin(new Insets(0,0,0,0));
        unmapBtn = new JButton(IconManager.getIcon("move_right"));
        unmapBtn.setMargin(new Insets(0,0,0,0));
        arrowBuilder.add(mapToBtn, cc.xy(1,2));
        arrowBuilder.add(unmapBtn, cc.xy(1,4));
        mapToBtn.setEnabled(false);
        unmapBtn.setEnabled(false);
        //arrowBuilder.getPanel().setBorder(BorderFactory.createLoweredBevelBorder());
        builder.add(arrowBuilder.getPanel(), cc.xy(3, 7));
        
        mapToBtn.setVisible(isMappedToAFile);
        unmapBtn.setVisible(false);
        
        fieldList = new JList(new DefaultListModel());
        fieldList.setCellRenderer(new FieldNameRenderer(IconManager.IconSize.Std16));
        
        sp = new JScrollPane(fieldList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(5, 7));
        
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                if (!doingFill)
                {
                    updateEnabledState();
                }
            }
        });
        
        fieldList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                TableFieldPair fieldItem = (TableFieldPair)fieldList.getSelectedValue();
                if (fieldItem != null && !fieldItem.isInUse() && currentInx != -1 && e.getClickCount() == 2)
                {
                    map();
                    
                    // Auto Advance
                    if (currentInx < mappingItems.size()-1)
                    {
                        selectMappingPanel(mappingItems.get(currentInx+1));
                    }
                }
            }
        });
        
        mapToBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                map();
            }
        });
        
        unmapBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                unmap(mappingItems.get(currentInx));
            }
        });
        
        okBtn.setEnabled(false);
        
        // Meg Here is your apply Btn
        // applyBtn.setText("Set this to your I18N Label");

        HelpMgr.registerComponent(helpBtn, helpContext);
        
        if (dataFileInfo != null)
        {
            for (ImportColumnInfo colInfo : dataFileInfo.getColInfo())
            {
                addMappingItem(colInfo, null);
            }
        
            autoMapFromDataFile();
        }
        
        if (workbenchTemplate != null)
        {
            fillFromTemplate();
            setChanged(false);
        }
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        
        contentPanel = builder.getPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        pack();
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
                cancelBtn.requestFocus();
                updateEnabledState();
            }
        });
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
     * Add a new FieldMappingPanel (which comes from the data file column).
     * @param colInfo the Column Info about the Data File column
     * @param mappingIcon the icon it should use to describe what it has been mapped to
     */
    protected FieldMappingPanel addMappingItem(final ImportColumnInfo colInfo, final ImageIcon mappingIcon)
    {
        FieldMappingPanel fmp = new FieldMappingPanel(colInfo, mappingIcon);
        fmp.setMappingLabelVisible(isMappedToAFile);
        
        mappingItems.add(fmp);
        dataFileColPanel.add(fmp);
        return fmp;
    }
    
    /**
     * update the MappingTo and unmapping buttons.
     */
    protected void updateEnabledState()
    {
        TableFieldPair fieldItem = (TableFieldPair)fieldList.getSelectedValue();
        
        mapToBtn.setEnabled(isMappedToAFile && (fieldItem != null && !fieldItem.isInUse()) && currentInx > -1);
        unmapBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped());
        
        addMapItemBtn.setEnabled(fieldItem != null && !fieldItem.isInUse());
        
        //removeMapItemBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped() && mappingItems.get(currentInx).isNew());
        removeMapItemBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped());
        
        if (okBtn != null && hasChanged)
        {
            boolean enabled = false;
            for (FieldMappingPanel fmp : mappingItems)
            {
                if (fmp.isMapped())
                {
                    enabled = true;
                    break;
                }
            }
            okBtn.setEnabled(enabled);
        }
    }
    
    /**
     * Select a FieldMappingPanel and unselect the old one.
     * @param fmp the selected FieldMappingPanel
     */
    protected void selectMappingPanel(final FieldMappingPanel fmp)
    {
        if (currentInx != -1)
        {
            mappingItems.get(currentInx).setHasFocus(false);
        }
        
        int newInx = mappingItems.indexOf(fmp);
        if (newInx != currentInx)
        {
            currentInx = newInx;
            mappingItems.get(currentInx).setHasFocus(true);
        } else
        {
            currentInx = -1;
        }
        
        if (fmp.isMapped())
        {
            for (TableInfo ti : tableInfoList)
            {
                if (ti.getTableInfo() == fmp.getTableField().getTableinfo())
                {
                    tableList.setSelectedValue(ti, true);
                    fieldList.setSelectedValue(fmp.getTableField(), true);
                }
            }
        }
        
        updateEnabledState();
    }
    
    /**
     * Fill in the JList's model from the list of fields.
     * @param tableInfo the table who's list we should use
     */
    protected void fillFieldList(DBTableIdMgr.TableInfo tableInfo)
    {
        DefaultListModel model = (DefaultListModel)fieldList.getModel();
        model.clear();
        if (tableInfo != null)
        {
            for (TableFieldPair fi : tableFieldList.get(tableInfo))
            {
                model.addElement(fi);
            }
        }
    }
    
    /**
     * Map the current FieldMappingPanel object. 
     */
    protected void map()
    {
        unmap(mappingItems.get(currentInx)); // unmap a current one if there is one
        map((TableFieldPair)fieldList.getSelectedValue());

    }
    
    /**
     * Mapp the FieldMappingPanel to the TableFieldPair.
     * @param tblField the item in the list
     */
    protected void map(final TableFieldPair tblField)
    {
        tblField.setInUse(true);
        FieldMappingPanel fmp = mappingItems.get(currentInx);
        fmp.setIcon(IconManager.getIcon(tblField.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
        fmp.setTableField(tblField);
        fieldList.repaint();

        setChanged(true);
        
        updateEnabledState();
    }
    
    /**
     * Unmap the Field or remove the item if there is no file.
     * @param fmp the field to be unmapped
     */
    protected void unmap(final FieldMappingPanel fmp)
    {
        TableFieldPair fieldInfo = fmp.getTableField();
         if (fieldInfo != null)
         {
             fieldInfo.setInUse(false);
         }
         fieldList.repaint();
         
         setChanged(true);

        // Need to Sort Here
        updateEnabledState();
    }
    
    /**
     * Adds a new Column to the Template that is not represented by a row in a file (if there is a file). 
     */
    @SuppressWarnings("cast")
    protected void addMapItem(final boolean isNew)
    {
        short maxDataColIndex = -1;

        if (mappingItems.size() > 0)
        {
            for (FieldMappingPanel fmp : mappingItems)
            {
                ImportColumnInfo colInfo = fmp.getColInfo();
                if (colInfo != null)
                {
                    // casts needed for Java 5 @SuppressWarnings("cast")
                    maxDataColIndex = (short)Math.max((int)maxDataColIndex, (int)colInfo.getColInx());
                }
            }
            maxDataColIndex++;
            
        } else
        {
            maxDataColIndex = 0;
        }

        TableFieldPair tblField   = (TableFieldPair)fieldList.getSelectedValue();
        Class<?>       tableClass = null;
        try
        {
            Object newDbObj = tblField.getTableinfo().getClassObj().newInstance();
            tableClass = PropertyUtils.getPropertyType(newDbObj, tblField.getFieldInfo().getName());
        }
        catch (Exception e)
        {
            // we can't determine the class of the DB mapping, so assume String
            log.warn("Exception while looking up field type.  Assuming java.lang.String.",e);
            tableClass = String.class;
        }
        
        FieldMappingPanel fmp = null;
        for (FieldMappingPanel fldMapPanel : removedItems)
        {
            if (fldMapPanel.getTableField() == tblField)
            {
                fmp = fldMapPanel;
                removedItems.remove(fldMapPanel);
                break;
            }
        }
        
        if (fmp == null)
        {
            ImportColumnInfo  colInfo = new ImportColumnInfo(maxDataColIndex, ImportColumnInfo.getType(tableClass), tblField.getFieldInfo().getColumn(), null);
            fmp = addMappingItem(colInfo, IconManager.getIcon(tblField.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
            fmp.setNew(isNew); // new Items that was not in the data file.
            
            selectMappingPanel(fmp);
            
            map(tblField);
            
        } else
        {
            fmp.getTableField().setInUse(true);
            mappingItems.add(fmp);
            dataFileColPanel.add(fmp);
            
            selectMappingPanel(fmp);
        }
        
        dataFileColPanel.validate();
        fieldList.repaint();

        updateEnabledState();
    }
    
    /**
     * Removes a Column from the Definition.
     */
    protected void removeMapItem(final FieldMappingPanel fmpToRemove)
    {
        FieldMappingPanel fmp = fmpToRemove == null ? mappingItems.get(currentInx) : fmpToRemove;
        
        unmap(fmp);
        
        if (!fmp.isNew())
        {
            removedItems.add(fmp);
        }
        
        mappingItems.remove(fmp);
        dataFileColPanel.remove(fmp);
        currentInx = -1;
        
        dataFileColPanel.validate();
        fieldList.repaint();
        dataFileColPanel.repaint();
        
        setChanged(true);

        updateEnabledState();
    }
    
    /**
     * For a given Data Model Class it returns the TableInfo object for it. (This could be moved to the DBTableIdMgr).
     * @param classObj the class object
     * @return the table info
     */
    protected DBTableIdMgr.TableInfo getTableInfo(Class<?> classObj)
    {
        for (DBTableIdMgr.TableInfo ti : tableFieldList.keySet())
        {
            if (ti.getClassObj() == classObj)
            {
                return ti;
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
    protected TableFieldPair getFieldInfo(final DBTableIdMgr.TableInfo ti, final String fieldName)
    {
        if (ti != null)
        {
            String fldFieldName = fieldName.toLowerCase();
            for (TableFieldPair tblField : tableFieldList.get(ti))
            {
                //System.out.println("["+tblField.getFieldInfo().getColumn().toLowerCase()+"]["+fieldName.toLowerCase()+"]");
                String tblFieldName = tblField.getFieldInfo().getColumn().toLowerCase();
                if (tblFieldName.equals(fldFieldName) || tblFieldName.startsWith(fldFieldName))
                {
                    return tblField;
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
    protected TableFieldPair autoMapFieldName(final String fieldNameArg)
    {
        String fieldNameLower = fieldNameArg.toLowerCase();
        String fieldName      = StringUtils.deleteWhitespace(fieldNameLower);
        
        TableFieldPair tblField  = null;
        
        // Check some standard common names
        if (fieldName.indexOf("date") > -1)
        {
            if (fieldName.indexOf("start") > -1)
            {
                tblField = getFieldInfo(getTableInfo(CollectingEvent.class), "StartDate");
                
            } else if (fieldName.indexOf("end") > -1)
            {
                tblField = getFieldInfo(getTableInfo(CollectingEvent.class), "EndDate");
            }
        } else if (fieldName.startsWith("field"))
        {
            if (fieldName.startsWith("fieldno") || fieldName.startsWith("fieldnum"))
            {
                tblField = getFieldInfo(getTableInfo(CollectionObject.class), "FieldNumber");
            }
        } else if (fieldName.startsWith("catalog"))
        {
            if (fieldName.startsWith("catalogno") || fieldName.startsWith("catalognum"))
            {
                tblField = getFieldInfo(getTableInfo(CollectionObject.class), "CatalogNumber");
            }
        }
        
        // If we had not luck then just loop through everything looking for it.
        if (tblField == null)
        {
            for (DBTableIdMgr.TableInfo ti : tableFieldList.keySet())
            {
                tblField = getFieldInfo(getTableInfo(ti.getClassObj()), fieldNameLower);
                if (tblField != null)
                {
                    break;
                }
            }
        }
        
        return tblField;
    }
    
    /**
     * Automap by column from the data's columns to the Data Model. 
     */
    protected void autoMapFromDataFile()
    {
        boolean notAllMapped = false;  // assume we can auto map everything
        
        currentInx = 0;
        for (FieldMappingPanel fmp : mappingItems)
        {
            TableFieldPair tblField = autoMapFieldName(fmp.getFieldName());
            if (tblField != null)
            {
                map(tblField);
                
            } else
            {
                notAllMapped = true; // oops, couldn't find a mapping for something
            }
            currentInx++;
        }
        
        if (!notAllMapped)
        {
            okBtn.setEnabled(false);
        }
        
        currentInx = -1;
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
        for (int i=0;i<tableInfoList.size();i++)
        {
            tblIdToListIndex.put(tableInfoList.get(i).getTableInfo().getTableId(), i);
        }
        
        Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(workbenchTemplate.getWorkbenchTemplateMappingItems());
        Collections.sort(items);
        for (WorkbenchTemplateMappingItem  wbtmi : items)
        {
            int inx = tblIdToListIndex.get(wbtmi.getSrcTableId());
            tableList.setSelectedIndex(inx);
            
            int fieldNum = 0;
            for (TableFieldPair pair : tableFieldList.get(tableInfoList.get(inx).getTableInfo()))
            {
                if (wbtmi.getFieldName().equals(pair.getFieldInfo().getName()))
                {
                    fieldList.setSelectedIndex(fieldNum);
                    break;
                }
                fieldNum++;
            }
            addMapItem(true);
        }
        doingFill = false;
        
        tableList.getSelectionModel().clearSelection();
        fillFieldList(null); // clears list
        if (currentInx != -1)
        {
            mappingItems.get(currentInx).setHasFocus(false);
            currentInx = -1;
        }
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
        for (FieldMappingPanel fmp : mappingItems)
        {
            ImportColumnInfo colInfo  = fmp.getColInfo();
            TableFieldPair   tblField = fmp.getTableField();
            
            if (fmp.isMapped())
            {
                WorkbenchTemplateMappingItem item = new WorkbenchTemplateMappingItem();
                item.initialize();
                
                item.setCaption(colInfo.getColName());
                item.setFieldName(tblField.getFieldInfo().getName());
                item.setImportedColName(colInfo.getColName());
                item.setSrcTableId(tblField.getTableinfo().getTableId());
                item.setTableName(tblField.getTableinfo().getTableName());
                short len = (short)tblField.getFieldInfo().getLength();
                item.setDataFieldLength(len == -1 ? 15 : len);
                
                //log.error(tblField.getFieldInfo().getLength()+"  tblID: "+tblField.getTableinfo().getTableId());
                
                item.setViewOrder(order);
                item.setOrigImportColumnIndex(fmp.isNew() ? -1 : colInfo.getColInx());
                order++;
                item.setWorkbenchTemplate(wbTemplate);
                
                items.add(item);
            }
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

        protected boolean           hasFocus      = false;
        protected Color             bgColor       = null;
        protected JLabel            fieldLabel;
        protected JLabel            mappingLabel;
        protected JLabel            iconLabel;
        protected ImageIcon         mappingIcon;
        
        protected TableFieldPair    tblField      = null;
        protected ImportColumnInfo  colInfo       = null;
        protected boolean           isNew         = false;
        
        
        protected FieldMappingPanel thisItem;
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the mappingIcon to use once it is mapped
         */
        public FieldMappingPanel(final ImportColumnInfo colInfo, final ImageIcon mappingIcon)
        {
            this.colInfo = colInfo;
            setBackground(Color.WHITE);
            
            //PanelBuilder    builder = new PanelBuilder(new FormLayout("150px, p:g, p, p:g, 150px, 5px, p, 2px", "p:g"), this);
            PanelBuilder    builder = new PanelBuilder(new FormLayout("150px, p:g, p, p:g, 150px", "p:g"), this);
            CellConstraints cc      = new CellConstraints();

            fieldLabel   = new JLabel(colInfo.getColName());
            mappingLabel = new JLabel(noMappingStr, SwingConstants.RIGHT);
            
            Font font = fieldLabel.getFont();
            fieldLabel.setFont(new Font(font.getName(), Font.BOLD, font.getSize()));

            
            //Font font = fieldLabel.getFont();
            //font = new Font(font.getName(), font.getStyle(), font.getSize()-2);
            //fieldLabel.setFont(font);
            //mappingLabel.setFont(font);
            
            builder.add(fieldLabel, cc.xy(1,1));
            builder.add(iconLabel = new JLabel(mappingIcon), cc.xy(3,1));
            builder.add(mappingLabel, cc.xy(5,1));
            setIcon(mappingIcon);
            
            thisItem = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectMappingPanel(thisItem);
                }
            });
            fieldLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectMappingPanel(thisItem);
                }
            });
            mappingLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    selectMappingPanel(thisItem);
                }
            });
        }
        
        /**
         * Sests a TableFieldPair into the item.
         * @param fieldInfoArg the new TableFieldPair
         */
        public void setTableField(final TableFieldPair fieldInfoArg) // make this FieldInfo
        {
            tblField = fieldInfoArg;
            mappingLabel.setText(fieldInfoArg != null ? fieldInfoArg.getFieldInfo().getColumn() : noMappingStr);
        }
        
        public void setMappingLabelVisible(final boolean isVis)
        {
            mappingLabel.setVisible(isVis);
        }
        
        protected boolean isMapped()
        {
            return tblField != null;
        }
        
        /**
         * Sets the object to have focus.
         * @param hasFocus true/false
         */
        public void setHasFocus(final boolean hasFocus)
        {
            if (bgColor == null)
            {
                bgColor = getBackground();
            }
            
            this.hasFocus = hasFocus;
            
            if (UIHelper.getOSType() == UIHelper.OSTYPE.Windows)
            {
                fieldLabel.setForeground(hasFocus ? Color.WHITE : Color.BLACK);
                mappingLabel.setForeground(hasFocus ? Color.WHITE : Color.BLACK);
            }
            setBackground(hasFocus ? tableList.getSelectionBackground() : bgColor);
        }

        public void setIcon(ImageIcon mappingIcon)
        {
            this.mappingIcon = mappingIcon == null ? blankIcon : mappingIcon;
            iconLabel.setIcon(this.mappingIcon);
        }

        /**
         * @return the TableInfo object
         */
        public TableFieldPair getTableField()
        {
            return tblField;
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
            return fieldLabel.getText();
        }

        /**
         * Returns whether this is a new item, which means it was NOT in the data file.
         * @return  whether this is a new item, which means it was NOT in the data file.
         */
        public boolean isNew()
        {
            return isNew;
        }

        /**
         * Sets whether it is a new item, that wasn't in the data file.
         * @param isNew true its new, false it was from the data file.
         */
        public void setNew(boolean isNew)
        {
            Font font = fieldLabel.getFont();
            fieldLabel.setFont(new Font(font.getName(), (isNew ? Font.PLAIN : Font.BOLD), font.getSize()));
            
            this.isNew = isNew;
        }
    }
    
    //------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------
    class TableInfo implements TableNameRendererIFace, Comparable<TableInfo>
    {
        protected DBTableIdMgr.TableInfo tableInfo;
        
        public TableInfo(final DBTableIdMgr.TableInfo tableInfo)
        {
            this.tableInfo = tableInfo;
        }

        /**
         * @return the tableInfo
         */
        public DBTableIdMgr.TableInfo getTableInfo()
        {
            return tableInfo;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getIconName()
         */
        public String getIconName()
        {
            return tableInfo.getClassObj().getSimpleName();
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace#getTitle()
         */
        public String getTitle()
        {
            return tableInfo.toString();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(TableInfo obj)
        {
            return tableInfo.toString().compareTo(obj.tableInfo.toString());
        }
    }
    
}
