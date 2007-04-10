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
import java.awt.Component;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
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

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.ui.HelpMgr;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.DefaultModifiableListModel;
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
public class TemplateEditor extends CustomDialog
{
    private static final Logger log = Logger.getLogger(TemplateEditor.class);
    
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        addMapItemBtn;
    protected JButton                        removeMapItemBtn;
    protected JButton                        upBtn;
    protected JButton                        downBtn;
    
    protected JList                          unusedList;
    protected DefaultModifiableListModel<ImportColumnInfo> unusedModel;
    protected Hashtable<ImportColumnInfo, WorkbenchTemplateMappingItem> unusedWBTMIs = new Hashtable<ImportColumnInfo, WorkbenchTemplateMappingItem>();

    protected JList                          mapList;
    protected DefaultModifiableListModel<FieldMappingPanel> mapModel;

    protected JList                          tableList;
    protected DefaultModifiableListModel<TableListItemIFace> tableModel;
    
    protected boolean                        hasChanged        = false;
    protected boolean                        doingFill         = false;
    
    protected ImportDataFileInfo             dataFileInfo      = null;
    protected WorkbenchTemplate              workbenchTemplate = null;
    protected DBTableIdMgr                   databaseSchema;
    
    protected boolean                        isMappedToAFile;
    protected boolean                        ignoreMapListUpdate = false;
    
    //protected ImageIcon checkMark   = IconManager.getIcon("Checkmark", IconManager.IconSize.Std16);
    protected ImageIcon blankIcon   = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std24);
    //protected ImageIcon blankIcon16 = IconManager.getIcon("BlankIcon", IconManager.IconSize.Std16);
    
    protected TableInfoListRenderer          tableInfoListRenderer;
    
    /**
     * Constructor.
     * @param dlg the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public TemplateEditor(final Frame frame, final String title, final ImportDataFileInfo dataFileInfo)
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
    public TemplateEditor(final Frame frame, final String title, final WorkbenchTemplate wbTemplate)
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
        
        tableModel = new DefaultModifiableListModel<TableListItemIFace>();
        for (TableInfo ti : tableInfoList)
        {
            tableModel.add(ti);
            ti.setExpanded(true);
            for (FieldInfo fieldInfo : ti.getFieldItems())
            {
                tableModel.add(fieldInfo);
            }
        }
        tableList = new JList(tableModel);
        tableList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.IconSize.Std16));
        JScrollPane tableScrollPane = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mapModel = new DefaultModifiableListModel<FieldMappingPanel>();
        mapList  = new JList(mapModel);
        mapList.setCellRenderer(new MapCellRenderer());
        JScrollPane mapScrollPane = new JScrollPane(mapList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        mapList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    FieldMappingPanel fmp = (FieldMappingPanel)mapList.getSelectedValue();
                    if (fmp != null)
                    {
                        ignoreMapListUpdate = true;
                        tableList.setSelectedValue(fmp.getFieldInfo(), true);
                        unusedList.getSelectionModel().clearSelection();
                        ignoreMapListUpdate = false;

                    }
                }
            }
        });

        
        unusedModel = new DefaultModifiableListModel<ImportColumnInfo>();
        unusedList  = new JList(unusedModel);
        JScrollPane unusedScrollPane = new JScrollPane(unusedList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        unusedList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    if (!ignoreMapListUpdate)
                    {
                        mapList.getSelectionModel().clearSelection();
                    }
                    updateEnabledState();
                }
            }
        });
        
        CellConstraints cc = new CellConstraints();

        addMapItemBtn = createIconBtn("PlusSign", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addNewMapItem(null, null);
            }
        });
        removeMapItemBtn = createIconBtn("MinusSign", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                unmap();
            }
        });
        
        PanelBuilder leftSide = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p"));        
        leftSide.add(addMapItemBtn,    cc.xy(1, 2));
        //leftSide.add(removeMapItemBtn, cc.xy(1, 4));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateEnabledState();
                }
            }
        });
        
        upBtn = createIconBtn("UpArrow", "WB_MOVE_UP", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int               inx = mapList.getSelectedIndex();
                FieldMappingPanel fmp = mapModel.getElementAt(inx);
                
                mapModel.remove(fmp);
                mapModel.insertElementAt(fmp, inx-1);
                mapList.setSelectedIndex(inx-1);
                updateEnabledState();
                adjustViewOrder();
                setChanged(true);
            }
        });
        downBtn = createIconBtn("DownArrow", "WB_MOVE_DOWN", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                int               inx = mapList.getSelectedIndex();
                FieldMappingPanel fmp = mapModel.getElementAt(inx);
                
                mapModel.remove(fmp);
                mapModel.insertElementAt(fmp, inx+1);
                mapList.setSelectedIndex(inx+1);
                updateEnabledState();
                adjustViewOrder();
                setChanged(true);
            }
        });
        
        PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
        upDownPanel.add(upBtn,   cc.xy(1, 2));
        upDownPanel.add(downBtn, cc.xy(1, 4));
        
        tableList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e)
            {
                if (!e.getValueIsAdjusting())
                {
                    updateEnabledState();
                }
            }
        });

        
        tableList.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e)
            {
                listClicked(e);
            }
        });
        
        //PanelBuilder   arrowBuilder = new PanelBuilder(new FormLayout("p:g,p,2px,p,p:g", "f:p:g,p"));

        mapToBtn = new JButton("Map", IconManager.getIcon("DownArrow"));
        //mapToBtn.setMargin(new Insets(0,0,0,0));
        unmapBtn = new JButton("Unmap", IconManager.getIcon("UpArrow"));
        //unmapBtn.setMargin(new Insets(0,0,0,0));
        //arrowBuilder.add(mapToBtn, cc.xy(4,2));
        //arrowBuilder.add(unmapBtn, cc.xy(2,2));
        mapToBtn.setEnabled(false);
        unmapBtn.setEnabled(false);

        
        PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(250px;p):g, 5px, p, 5px, f:max(250px;p):g, 2px, p", "p,2px,f:max(150px;p):g,5px,p,5px,c:p,5px,p"));
        
        builder.add(new JLabel(workbenchTemplate == null ? getResourceString("WB_COLUMNS") : getResourceString("WB_REMOVE_COLUMNS"), SwingConstants.CENTER), cc.xy(1, 1));
        builder.add(new JLabel(getResourceString("WB_SCHEMA"), SwingConstants.CENTER), cc.xy(5, 1));
        builder.add(unusedScrollPane,        cc.xy(1, 3));
        //builder.add(arrowBuilder.getPanel(), cc.xy(3, 3));
        builder.add(tableScrollPane,         cc.xy(5, 3));
        builder.add(leftSide.getPanel(),     cc.xy(7, 3));
        
        PanelBuilder tmpBldr1 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tmpBldr1.add(unmapBtn, cc.xy(2,1));
        
        PanelBuilder tmpBldr2 = new PanelBuilder(new FormLayout("f:p:g,p,f:p:g", "p"));
        tmpBldr2.add(mapToBtn, cc.xy(2,1));
        
        builder.add(tmpBldr1.getPanel(),       cc.xy(1, 5));
        builder.add(tmpBldr2.getPanel(),        cc.xy(5, 5));
        builder.add(mapScrollPane,           cc.xywh(1, 7, 5, 1));
        builder.add(upDownPanel.getPanel(),    cc.xy(7, 7));
        
        //mapToBtn.setVisible(isMappedToAFile);
        //unmapBtn.setVisible(false);
        
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
                unmap();
            }
        });
        
        okBtn.setEnabled(false);
        
        // Meg Here is your apply Btn
        // applyBtn.setText("Set this to your I18N Label");

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
    
    protected void listClicked(MouseEvent e)
    {
        if (e.getButton() != MouseEvent.BUTTON1)
        {
            return;
        }
                
        JList list  = (JList)e.getSource();
        Point p     = e.getPoint();
        int   index = list.locationToIndex(p);
        if(index == -1)
        {
            return;
        }
        TableListItemIFace ti = tableModel.getElementAt(index);

        // if the user clicked an expansion handle, expand the child nodes
        if (ti.isExpandable() && tableInfoListRenderer.getTextOffset() > e.getPoint().x)
        {
            // toggle the state of child node visibility
            //boolean visible = listModel.allChildrenAreVisible(t);
            //listModel.setChildrenVisible(t, !visible);
            expand(ti, !ti.isExpanded());
        }
        // otherwise, ignore the click
        else
        {
            e.consume();
        }
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
     */
    protected FieldMappingPanel addMappingItem(final ImportColumnInfo colInfo, final ImageIcon mappingIcon)
    {
        FieldMappingPanel fmp = new FieldMappingPanel(colInfo, mappingIcon);
        
        fmp.setMappingLabelVisible(isMappedToAFile && workbenchTemplate == null);
        
        mapModel.add(fmp);
        mapList.setSelectedIndex(mapModel.getSize()-1);
        
        adjustViewOrder();
        
        return fmp;
    }
    
    /**
     * update the MappingTo and unmapping buttons.
     */
    protected void updateEnabledState()
    {
        
        TableListItemIFace item = (TableListItemIFace)tableList.getSelectedValue();
        if (item != null)
        {
            boolean hasField = !item.isExpandable() && !item.isChecked();
            mapToBtn.setEnabled(hasField && (unusedList.getSelectedIndex() > -1 || mapList.getSelectedIndex() > -1));
            unmapBtn.setEnabled(mapList.getSelectedIndex() > -1);
            
            addMapItemBtn.setEnabled(hasField && unusedList.getSelectedIndex() == -1);
            
            upBtn.setEnabled(mapList.getSelectedIndex() > 0);
            downBtn.setEnabled(mapList.getSelectedIndex() < mapModel.size()-1);
        } else
        {
            mapToBtn.setEnabled(false);
            addMapItemBtn.setEnabled(false);
        }
        
        //removeMapItemBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped() && mappingItems.get(currentInx).isNew());
        //removeMapItemBtn.setEnabled(currentInx > -1 && mappingItems.get(currentInx).isMapped());
        
        if (okBtn != null && hasChanged)
        {
            okBtn.setEnabled(mapModel.size() > 0);
        }
    }
    
    protected void adjustViewOrder()
    {
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            fmp.setViewOrder((short)i);
        }
    }
    
    /**
     * Fill in the JList's model from the list of fields.
     * @param item the table who's list we should use
     */
    protected void expand(final TableListItemIFace item, final boolean expand)
    {
        TableInfo ti = (TableInfo)item;
        ti.setExpanded(expand);
        if (expand)
        {
            int index = tableModel.indexOf(item);
            Vector<FieldInfo> fiItems = ti.getFieldItems();
            for (int i=fiItems.size()-1;i>0;i--)
            {
                tableModel.insertElementAt(fiItems.get(i), index+1);
            }
        } else
        {
            for (FieldInfo fi : ti.getFieldItems())
            {
                tableModel.remove(fi);
            } 
        }
        tableList.repaint();
    }
    
    /**
     * 
     */
    protected FieldMappingPanel map()
    {
        ImportColumnInfo colInfo = (ImportColumnInfo)unusedList.getSelectedValue();
        FieldInfo        fi      = (FieldInfo)tableList.getSelectedValue();
        
        FieldMappingPanel fmp;
        
        WorkbenchTemplateMappingItem wbtmi = null;
        if (colInfo != null)
        {
            wbtmi = unusedWBTMIs.get(colInfo);
            
        }
        
        if (mapList.getSelectedIndex() > -1)
        {
            fmp = (FieldMappingPanel)mapList.getSelectedValue();
            fmp.setFieldInfo(fi);
            mapList.repaint();
            
        } else
        {
            fmp = map(colInfo, fi);
            fmp.setWbtmi(wbtmi);
        }
        
        unusedModel.remove(colInfo);
        
        if (wbtmi != null)
        {
            unusedWBTMIs.remove(colInfo);
        }
        
        return fmp;
    }
    
    /**
     * 
     */
    protected FieldMappingPanel map(final ImportColumnInfo colInfo, final FieldInfo fi)
    {
        FieldMappingPanel fmp;
        fi.setInUse(true);
        if (colInfo != null)
        {
            fmp = addMappingItem(colInfo, IconManager.getIcon(fi.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
            fmp.setIcon(IconManager.getIcon(fi.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
            fmp.setFieldInfo(fi);
            
        } else
        {
            fmp = addNewMapItem(null, fi);
        }

        setChanged(true);
        
        updateEnabledState();
        
        adjustViewOrder();

        return fmp;
    }
    
    /**
     * Unmap the Field or remove the item if there is no file.
     * @param fmp the field to be unmapped
     */
    protected void unmap()
    {
        FieldMappingPanel fmp = (FieldMappingPanel)mapList.getSelectedValue();
        
        if (!fmp.isNew())
        {
            unusedModel.add(fmp.getColInfo());
            if (fmp.getWbtmi() != null)
            {
                unusedWBTMIs.put(fmp.getColInfo(), fmp.getWbtmi());
            }
        }
        
        FieldInfo fieldInfo = fmp.getFieldInfo();
        if (fieldInfo != null)
        {
            fieldInfo.setChecked(false);
            tableList.repaint();
        }
        mapModel.remove(fmp);
        
        setChanged(true);
        
        adjustViewOrder();

        // Need to Sort Here
        updateEnabledState();
    }
    
    /**
     * Adds a new Column to the Template that is not represented by a row in a file (if there is a file). 
     */
    @SuppressWarnings("cast")
    protected FieldMappingPanel addNewMapItem(final Short viewOrder, final FieldInfo fieldInfoArg)
    {
        short maxDataColIndex;
        if (viewOrder != null)
        {
            maxDataColIndex = viewOrder;
        } else
        {
            maxDataColIndex = -1;
            
            if (mapModel.size() > 0)
            {
                for (int i=0;i<mapModel.size();i++)
                {
                    FieldMappingPanel fmp     = mapModel.getElementAt(i);
                    ImportColumnInfo  colInfo = fmp.getColInfo();
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
        }

        FieldInfo fieldInfo  = fieldInfoArg == null ? (FieldInfo)tableList.getSelectedValue() : fieldInfoArg;
        fieldInfo.setChecked(true);
        
        Class             tableClass = getTableClass(fieldInfo);
        ImportColumnInfo  colInfo    = new ImportColumnInfo(maxDataColIndex, 
                                                            ImportColumnInfo.getType(tableClass), 
                                                            fieldInfo.getFieldInfo().getColumn(), 
                                                            null);
        FieldMappingPanel fmp = addMappingItem(colInfo, IconManager.getIcon(fieldInfo.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
        fmp.setFieldInfo(fieldInfo);
        fmp.setNew(viewOrder == null || fieldInfoArg == null); // new Items that was not in the data file.
        setChanged(true);
        
        tableList.repaint();
        
        adjustViewOrder();

        updateEnabledState();
        
        return fmp;
    }
    
    /**
     * @param fieldInfo
     * @return
     */
    protected Class getTableClass(final FieldInfo fieldInfo)
    {
        Class<?>  tableClass = null;
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
                    String tblFieldName = fi.getFieldInfo().getColumn().toLowerCase();
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
    protected FieldInfo autoMapFieldName(final String fieldNameArg)
    {
        String fieldNameLower = fieldNameArg.toLowerCase();
        String fieldName      = StringUtils.deleteWhitespace(fieldNameLower);
        
        FieldInfo fieldInfo  = null;
        
        // Check some standard common names
        if (fieldName.indexOf("date") > -1)
        {
            if (fieldName.indexOf("start") > -1)
            {
                fieldInfo = getFieldInfo(getTableInfo(CollectingEvent.class), "StartDate");
                
            } else if (fieldName.indexOf("end") > -1)
            {
                fieldInfo = getFieldInfo(getTableInfo(CollectingEvent.class), "EndDate");
            }
        } else if (fieldName.startsWith("field"))
        {
            if (fieldName.startsWith("fieldno") || fieldName.startsWith("fieldnum"))
            {
                fieldInfo = getFieldInfo(getTableInfo(CollectionObject.class), "FieldNumber");
            }
        } else if (fieldName.startsWith("catalog"))
        {
            if (fieldName.startsWith("catalogno") || fieldName.startsWith("catalognum"))
            {
                fieldInfo = getFieldInfo(getTableInfo(CollectionObject.class), "CatalogNumber");
            }
        }
        
        // If we had not luck then just loop through everything looking for it.
        if (fieldInfo == null)
        {
            for (int i=0;i<tableModel.size();i++)
            {
                TableListItemIFace item = tableModel.getElementAt(i);
                if (!item.isExpandable())
                {
                    FieldInfo fi           = (FieldInfo)item;
                    String    tblFieldName = fi.getFieldInfo().getColumn().toLowerCase();
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
        
        boolean notAllMapped = false;  // assume we can auto map everything
        for (ImportColumnInfo colInfo : colInfos)
        {
            FieldInfo fieldInfo = autoMapFieldName(colInfo.getColName());
            if (fieldInfo != null)
            {
                // Find the right TableInfo
                for (int i=0;i<tableModel.size();i++)
                {
                    TableListItemIFace item = tableModel.getElementAt(i);
                    if (item.isExpandable() && ((TableInfo)item).getTableInfo() == fieldInfo.getTableinfo())
                    {
                        tablesInUse.put((TableInfo)item, true);
                        break;
                    }
                }
                
                map(colInfo, fieldInfo);
                
            } else
            {
                notAllMapped = true; // oops, couldn't find a mapping for something
                unusedModel.add(colInfo);
            }
        }
        
        if (!notAllMapped)
        {
            okBtn.setEnabled(false);
        }
        
        adjustViewOrder();
        
        collapseToUsedTables(tablesInUse);
        
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
                    FieldMappingPanel fmp = addNewMapItem(wbtmi.getViewOrder(), fi);
                    fmp.setWbtmi(wbtmi);
                    tablesInUse.put(ti, true);
                    break;
                }
                fieldNum++;
            }
        }
        doingFill = false;
        
        mapList.getSelectionModel().clearSelection();
        tableList.getSelectionModel().clearSelection();
        
        collapseToUsedTables(tablesInUse);
    }
    
    protected void collapseToUsedTables(final Hashtable<TableInfo, Boolean> tablesInUse)
    {
        Vector<TableInfo> tables = new Vector<TableInfo>();
        for (int i=0;i<tableModel.size();i++)   
        {
            TableListItemIFace item = tableModel.getElementAt(i);
            if (item.isExpandable())
            {
                tables.add((TableInfo)item);
            }
        }
        
        for (TableInfo ti : tables)
        {
            if (tablesInUse.get(ti) == null)
            {
                expand(ti, false);
            }
        }
    }
    
    /**
     * Returns the items that were removed.
     * @return the items that were removed.
     */
    public Collection<WorkbenchTemplateMappingItem> getUnusedItems()
    {
        return unusedWBTMIs.values();
    }
    
    
    public Collection<WorkbenchTemplateMappingItem> getNewItems()
    {
        Vector<WorkbenchTemplateMappingItem> newItems = new Vector<WorkbenchTemplateMappingItem>();
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            if (fmp.getWbtmi() == null)
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
                item.setOrigImportColumnIndex(fmp.isNew() ? -1 : colInfo.getColInx());
                newItems.add(item);
            }
        }

        return newItems;
    }
    
    public Collection<WorkbenchTemplateMappingItem> getUpdatedItems()
    {
        Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>();
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp       = mapModel.getElementAt(i);
            if (fmp.getWbtmi() != null)
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
            item.setOrigImportColumnIndex(fmp.isNew() ? -1 : colInfo.getColInx());
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

        protected boolean           hasFocus      = false;
        protected Color             bgColor       = null;
        protected JLabel            fieldLabel;
        protected JLabel            mappingLabel;
        protected JLabel            iconLabel;
        protected ImageIcon         mappingIcon;
        
        protected Short             viewOrder     = null;
        
        protected FieldInfo         fieldInfo     = null;
        protected ImportColumnInfo  colInfo       = null;
        protected boolean           isNew         = false;
        
        protected WorkbenchTemplateMappingItem wbtmi = null;
        
        /**
         * Constructor.
         * @param fieldName the field Name
         * @param icon the mappingIcon to use once it is mapped
         */
        public FieldMappingPanel(final ImportColumnInfo colInfo, final ImageIcon mappingIcon)
        {
            this.colInfo = colInfo;
            setBackground(Color.WHITE);
            
            PanelBuilder    builder = new PanelBuilder(new FormLayout("150px, p:g, p, p:g, 150px", "p:g"), this);
            CellConstraints cc      = new CellConstraints();

            fieldLabel   = new JLabel(colInfo.getColName());
            mappingLabel = new JLabel(noMappingStr, SwingConstants.RIGHT);
            
            setFocusable(true);
            
            builder.add(fieldLabel, cc.xy(1,1));
            builder.add(iconLabel = new JLabel(mappingIcon), cc.xy(3,1));
            builder.add(mappingLabel, cc.xy(5,1));
            setIcon(mappingIcon);
        }
        
        /**
         * @return the wbtmi
         */
        public WorkbenchTemplateMappingItem getWbtmi()
        {
            return wbtmi;
        }

        /**
         * @param wbtmi the wbtmi to set
         */
        public void setWbtmi(WorkbenchTemplateMappingItem wbtmi)
        {
            setMappingLabelVisible(wbtmi != null);
            if (workbenchTemplate != null)
            {
                fieldLabel.setText(wbtmi.getCaption());
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
            } else
            {
                return this.viewOrder;
            }
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
            mappingLabel.setText(fieldInfoArg != null ? fieldInfoArg.getFieldInfo().getColumn() : noMappingStr);
            mappingLabel.repaint();
        }
        
        public void setMappingLabelVisible(final boolean isVis)
        {
            mappingLabel.setVisible(isVis);
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
            //Font font = fieldLabel.getFont();
            //fieldLabel.setFont(new Font(font.getName(), (isNew ? Font.PLAIN : Font.BOLD), font.getSize()));
            
            this.isNew = isNew;
        }
    }
    
    //------------------------------------------------------------------
    //-- 
    //------------------------------------------------------------------
    class MapCellRenderer implements ListCellRenderer
    {

        /* (non-Javadoc)
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus)
        {
            FieldMappingPanel panel = (FieldMappingPanel)value;
            panel.setHasFocus(isSelected);
            return (Component)value;
        }
        
    }
    
}
