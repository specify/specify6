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

import java.awt.Color;
import java.awt.Font;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.NavBoxLayoutManager;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Location;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.help.HelpMgr;
import edu.ku.brc.specify.tasks.subpane.FieldNameRenderer;
import edu.ku.brc.specify.tasks.subpane.TableFieldPair;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer;
import edu.ku.brc.specify.tasks.subpane.TableNameRenderer.TableNameRendererIFace;
import edu.ku.brc.ui.IconManager;


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
public class ColumnMapperPanel extends JPanel
{
    private static final Logger log = Logger.getLogger(ColumnMapperPanel.class);
            
    protected Vector<TableInfo>              tableInfoList = new Vector<TableInfo>();
    protected JList                          fieldList;
    protected JList                          tableList;
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        addMapItemBtn;
    protected JButton                        removeMapItemBtn;
    
    protected JButton                        okBtn;
    protected JButton                        cancelBtn;
    protected boolean                        isCancelled = true;
    protected JDialog                        dlg;
    
    protected JPanel                         dataFileColPanel;
    protected Vector<FieldMappingPanel>      mappingItems = new Vector<FieldMappingPanel>();
    protected int                            currentInx = -1;
    protected Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>> tableFieldList = new Hashtable<DBTableIdMgr.TableInfo, Vector<TableFieldPair>>();
    
    protected ImportDataFileInfo             dataFileInfo      = null;
    protected WorkbenchTemplate              workbenchTemplate = null;
    
    protected boolean                        isMappedToAFile;
    
    protected ImageIcon checkMark   = IconManager.getIcon("Checkmark", IconManager.IconSize.Std16);
    protected ImageIcon blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    protected ImageIcon blankIcon16 = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std16);
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public ColumnMapperPanel(final JDialog dlg, final ImportDataFileInfo dataFileInfo)
    {
        this.dlg             = dlg;
        this.dataFileInfo    = dataFileInfo;
        this.isMappedToAFile = dataFileInfo != null;
        
        createUI();
    }
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public ColumnMapperPanel(final JDialog dlg, final WorkbenchTemplate wbTemplate)
    {
        this.dlg               = dlg;
        this.workbenchTemplate = wbTemplate;
        this.isMappedToAFile = StringUtils.isNotEmpty(wbTemplate.getSrcFilePath());
        
        createUI();
    }
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public ColumnMapperPanel(final JDialog dlg)
    {
        this(dlg, (ImportDataFileInfo)null);
    }
    
    /**
     * Creates UI for the dialog.
     */
    public void createUI()
    {
        String[] skipItems = {"TimestampCreated", "LastEditedBy", "TimestampModified"};
        Hashtable<String, Boolean> skipHash = new Hashtable<String, Boolean>();
        for (String name : skipItems)
        {
            skipHash.put(name, true);
        }
        
        for (DBTableIdMgr.TableInfo ti : DBTableIdMgr.getList())
        {
            if (ti.isForWorkBench() && StringUtils.isNotEmpty(ti.toString()))
            {
                tableInfoList.add(new TableInfo(ti)); 
                
                Vector<TableFieldPair> fldList = new Vector<TableFieldPair>();
                if (ti.getClassObj() == Geography.class)
                {
                    addGeographyFields(ti, fldList);
                    
                } else if (ti.getClassObj() == Taxon.class)
                {
                    addTaxonFields(ti, fldList);
                    
                } else if (ti.getClassObj() == Location.class)
                {
                    addLocationFields(ti, fldList);
                    
                } else
                {
                    for (DBTableIdMgr.FieldInfo fi : ti.getFields())
                    {
                        if (skipHash.get(fi.getColumn()) == null)
                        {
                            fldList.add(new TableFieldPair(ti, fi));
                        }
                    }
                }
                Collections.sort(fldList);
                tableFieldList.put(ti, fldList);
            }
        }
        Collections.sort(tableInfoList);
        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(275px;p):g, 5px, p, 5px, p", 
                                                                 "p, 2px, top:p, 10px, p, 2px, f:p:g, 5px, p, 2px, f:p:g"), this);
        CellConstraints cc      = new CellConstraints();
        
        PanelBuilder header = new PanelBuilder(new FormLayout("p,f:p:g,p", "p,2px,p"));
        header.add(new JLabel(getResourceString("WB_MAPPING_COLUMNS"), SwingConstants.CENTER), cc.xywh(1, 1, 3, 1));
        header.add(new JLabel(workbenchTemplate != null ? "Import" : "Schema", SwingConstants.LEFT), cc.xy(1,3)); // XXX I18N
        header.add(new JLabel(workbenchTemplate != null ? "Schema" : "", SwingConstants.RIGHT), cc.xy(3,3));  // XXX I18N

        builder.add(header.getPanel(), cc.xy(1, 1));
        builder.add(new JLabel(getResourceString("WB_DATAOBJECTS"),     SwingConstants.CENTER), cc.xy(5, 1));
        builder.add(new JLabel(getResourceString("WB_DATAOBJ_FIELDS"),  SwingConstants.CENTER), cc.xy(5, 5));
        
        dataFileColPanel = new JPanel();
        dataFileColPanel.setLayout(new NavBoxLayoutManager(0,2));
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
                    fillFieldList(((TableInfo)tableList.getSelectedValue()).getTableInfo());
                }
            }
        });
        
        PanelBuilder arrowBuilder = new PanelBuilder(new FormLayout("f:p:g", "p:g,p,2px,p,p:g"));
        mapToBtn  = new JButton(IconManager.getIcon("move_left"));
        mapToBtn.setMargin(new Insets(0,0,0,0));
        unmapBtn = new JButton(IconManager.getIcon("move_right"));
        unmapBtn.setMargin(new Insets(0,0,0,0));
        arrowBuilder.add(mapToBtn, cc.xy(1,2));
        arrowBuilder.add(unmapBtn, cc.xy(1,4));
        mapToBtn.setEnabled(false);
        unmapBtn.setEnabled(false);
        //arrowBuilder.getPanel().setBorder(BorderFactory.createLoweredBevelBorder());
        builder.add(arrowBuilder.getPanel(), cc.xy(3, 7));
        
        fieldList = new JList(new DefaultListModel());
        fieldList.setCellRenderer(new FieldNameRenderer(IconManager.IconSize.Std16));
        
        sp = new JScrollPane(fieldList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        builder.add(sp, cc.xy(5, 7));
        
        fieldList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                updateEnabledState();
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
        
        JButton helpBtn = new JButton(getResourceString("Help")); 
        okBtn     = new JButton(getResourceString("OK")); 
        cancelBtn = new JButton(getResourceString("Cancel"));
        okBtn.setEnabled(false);
        
        builder.add(ButtonBarFactory.buildOKCancelHelpBar(okBtn, cancelBtn, helpBtn), cc.xywh(1, 11, 5, 1));
        
        cancelBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                dlg.setVisible(false);
                isCancelled = true;
            }
        });
        
        okBtn.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                dlg.setVisible(false);
                isCancelled = false;
            }
        });
        
        HelpMgr.registerComponent(helpBtn, "WorkbenchColMapping");
        
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
        }
        
        builder.getPanel().setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
    }
    
    /**
     * Returns whether the dialog was cancelled.
     * @return whether the dialog was cancelled.
     */
    public boolean isCancelled()
    {
        return isCancelled;
    }

    /**
     * Add a new FieldMappingPanel (which comes from the data file column).
     * @param colInfo the Column Info about the Data File column
     * @param icon the icon it should use to describe what it has been mapped to
     */
    protected FieldMappingPanel addMappingItem(final ImportColumnInfo colInfo, final ImageIcon icon)
    {
        FieldMappingPanel fmp = new FieldMappingPanel(colInfo, icon);
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
        
        mapToBtn.setEnabled((fieldItem != null && !fieldItem.isInUse()) && currentInx > -1);
        unmapBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped());
        
        addMapItemBtn.setEnabled(fieldItem != null && !fieldItem.isInUse());
        
        removeMapItemBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped() && mappingItems.get(currentInx).isNew());
        
        if (okBtn != null)
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
     * Select a FieldMappingPanel and unselect the old one/
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
        for (TableFieldPair fi : tableFieldList.get(tableInfo))
        {
            model.addElement(fi);
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
             fmp.setTableField(null);
             fmp.setIcon(null);
         }
         fieldList.repaint();

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

        TableFieldPair    tblField  = (TableFieldPair)fieldList.getSelectedValue();
        Class<?> tableClass = null;
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
        ImportColumnInfo  colInfo   = new ImportColumnInfo(maxDataColIndex, ImportColumnInfo.getType(tableClass), tblField.getFieldInfo().getColumn(), null);
        
        FieldMappingPanel fmp = addMappingItem(colInfo, IconManager.getIcon(tblField.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
        
        fmp.setNew(isNew); // new Items that was not in the data file.
        
        selectMappingPanel(fmp);

        map(tblField);
        
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
        
        mappingItems.remove(fmp);
        dataFileColPanel.remove(fmp);
        currentInx = -1;
        
        dataFileColPanel.validate();
        fieldList.repaint();
        dataFileColPanel.repaint();

        updateEnabledState();
    }
    
    /**
     * CReates "fake" TableFieldPair entries for mapping tree items.
     * XXX Here we need to go get the TreeDefItems.
     * @param tableinfo x
     * @param fields x
     * @param fieldNames x
     */
    protected void addFields(final DBTableIdMgr.TableInfo tableInfo, final Vector<TableFieldPair> fields, final String[] fieldNames)
    {
        for (String fieldName : fieldNames)
        {
            DBTableIdMgr.FieldInfo fieldInfo = DBTableIdMgr.createFieldInfo(tableInfo, fieldName, fieldName, "java.lang.String", 64);
            fields.add(new TableFieldPair(tableInfo, fieldInfo));
        }
    }
    
    /**
     * Creates a denormalized list of possible fields for mapping for Geography.
     * @param tableinfo the table info
     * @param fields the list to be filled in 
     */
    protected void addGeographyFields(final DBTableIdMgr.TableInfo tableinfo, final Vector<TableFieldPair> fields)
    {
        //DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        //session.getDataList(clsObject)
        addFields(tableinfo, fields, new String[] {"Continent", 
                                                   "Country", 
                                                   "State", 
                                                   "County"});

    }
    
    /**
     * Creates a denormalized list of possible fields for mapping Taxon.
     * @param tableinfo the table info
     * @param fields the list to be filled in 
     */
    protected void addTaxonFields(final DBTableIdMgr.TableInfo tableinfo, final Vector<TableFieldPair> fields)
    {
        addFields(tableinfo, fields, new String[] {"Genus Species", "Species", "Genius"});
    }
    
    /**
     * Creates a denormalized list of possible fields for mapping for Location.
     * @param tableinfo the table info
     * @param fields the list to be filled in 
     */
    protected void addLocationFields(final DBTableIdMgr.TableInfo tableinfo, final Vector<TableFieldPair> fields)
    {
        addFields(tableinfo, fields, new String[] {"Building", "Floor", "Room", "Rack", "Shelf"});  
    }
    
    /**
     * For a given Data Model Class it returns the TableInfo object for it. (This could be moved to the DBTableIdMgr.
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
            for (TableFieldPair tblField : tableFieldList.get(ti))
            {
                //System.out.println("["+tblField.getFieldInfo().getColumn().toLowerCase()+"]["+fieldName.toLowerCase()+"]");
                String tblFieldName = tblField.getFieldInfo().getColumn().toLowerCase();
                String fldFieldName = fieldName.toLowerCase();
                if (tblFieldName.equals(fldFieldName) || tblFieldName.startsWith(fldFieldName))
                {
                    return tblField;
                }
            }
        }
        return null;
    }
    
    /**
     * Automap by column from the data's columns to the Data Model. 
     */
    protected void autoMapFromDataFile()
    {
        boolean missedMapping = false;  // assume we can auto map everything
        
        currentInx = 0;
        for (FieldMappingPanel fmp : mappingItems)
        {
            String     fieldName     = StringUtils.deleteWhitespace(fmp.getFieldName().toLowerCase());
            TableFieldPair tblField      = null;
            
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
            
            if (tblField == null)
            {
                for (DBTableIdMgr.TableInfo ti : tableFieldList.keySet())
                {
                    tblField = getFieldInfo(getTableInfo(ti.getClassObj()), fmp.getFieldName());
                    if (tblField != null)
                    {
                        break;
                    }
                }
            }
            
            if (tblField != null)
            {
                map(tblField);
            } else
            {
                missedMapping = true; // oops, couldn't find a mapping for something
            }
            currentInx++;
        }
        
        okBtn.setEnabled(missedMapping);
        
        currentInx = -1;
        updateEnabledState();
    }
    
    /**
     * Fill the UI from a WorkbenchTemplate. 
     */
    protected void fillFromTemplate()
    {
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
                item.setSrcTableId(tblField.getTableinfo().getTableId());
                item.setTableName(tblField.getTableinfo().getTableName());
                item.setFieldLength((short)tblField.getFieldInfo().getLength());
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
        protected JLabel            closeBtn;
        protected JLabel            iconLabel;
        protected ImageIcon         icon;
        
        protected TableFieldPair    tblField      = null;
        protected ImportColumnInfo  colInfo       = null;
        protected boolean           isNew         = false;
        
        protected FieldMappingPanel thisItem;
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the icon to use once it is mapped
         */
        public FieldMappingPanel(final ImportColumnInfo colInfo, final ImageIcon icon)
        {
            this.colInfo = colInfo;
            
             
            PanelBuilder    builder = new PanelBuilder(new FormLayout("150px, p:g, p, p:g, 150px, 5px, p, 2px", "p:g"), this);
            CellConstraints cc      = new CellConstraints();

            closeBtn     = new JLabel(IconManager.getIcon("Close"));
            fieldLabel   = new JLabel(colInfo.getColName());
            mappingLabel = new JLabel(noMappingStr, JLabel.RIGHT);
            
            Font font = fieldLabel.getFont();
            font = new Font(font.getName(), font.getStyle(), font.getSize()-2);
            fieldLabel.setFont(font);
            mappingLabel.setFont(font);
            
            builder.add(fieldLabel, cc.xy(1,1));
            builder.add(iconLabel = new JLabel(icon), cc.xy(3,1));
            builder.add(mappingLabel, cc.xy(5,1));
            builder.add(closeBtn, cc.xy(7,1));
            closeBtn.setVisible(false);
            setIcon(icon);
            
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
            
            closeBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (mappingLabel.isVisible())
                    {
                        unmap(thisItem);
                    } else
                    {
                        removeMapItem(thisItem);
                    }
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
            closeBtn.setVisible(fieldInfoArg != null);
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
            
            setBackground(hasFocus ? tableList.getSelectionBackground() : bgColor);
        }

        public void setIcon(ImageIcon icon)
        {
            this.icon = icon == null ? blankIcon : icon;
            iconLabel.setIcon(this.icon);
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
