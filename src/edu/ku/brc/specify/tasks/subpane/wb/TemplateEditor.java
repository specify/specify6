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

import static edu.ku.brc.ui.UIRegistry.getResourceString;
import static edu.ku.brc.ui.UIHelper.createIconBtn;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
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
 * Created Date: Feb 16, 2007
 *
 */
public class TemplateEditor extends CustomDialog
{
    private static final Logger log = Logger.getLogger(TemplateEditor.class);
    
    protected JButton                        mapToBtn;
    protected JButton                        unmapBtn;
    protected JButton                        addBtn            = null;
    protected JButton                        delBtn            = null;
    protected JButton                        upBtn;
    protected JButton                        downBtn;
    
    protected JList                                                     unusedList   = null;
    protected DefaultModifiableListModel<ImportColumnInfo>              unusedModel  = null;
    protected Hashtable<ImportColumnInfo, WorkbenchTemplateMappingItem> unusedWBTMIs = null;

    protected JList                          mapList;
    protected DefaultModifiableListModel<FieldMappingPanel> mapModel;
    protected JScrollPane                    mapScrollPane;
    
    protected JList                          tableList;
    protected DefaultModifiableListModel<TableListItemIFace> tableModel;
    
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
        super(frame, title, true, OKCANCELHELP, null);
        
        this.dataFileInfo    = dataFileInfo;
        this.isMappedToAFile = dataFileInfo != null;
        this.isEditMode      = false;
        
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
        super(frame, title, true, OKCANCELHELP, null);
        
        this.workbenchTemplate = wbTemplate;
        this.isMappedToAFile   = StringUtils.isNotEmpty(wbTemplate.getSrcFilePath());
        this.isEditMode        = this.workbenchTemplate != null;
        
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
        
        tableModel = new DefaultModifiableListModel<TableListItemIFace>();
        for (TableInfo ti : tableInfoList)
        {
            tableModel.add(ti);
            // Star out Collapsed when start on fro scratch
            if (!isFromScratch)
            {
                ti.setExpanded(true);
                for (FieldInfo fieldInfo : ti.getFieldItems())
                {
                    tableModel.add(fieldInfo);
                }
            }
        }
        tableList = new JList(tableModel);
        tableList.setCellRenderer(tableInfoListRenderer = new TableInfoListRenderer(IconManager.IconSize.Std16));
        JScrollPane tableScrollPane = new JScrollPane(tableList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        mapModel = new DefaultModifiableListModel<FieldMappingPanel>();
        mapList  = new JList(mapModel);
        mapList.setCellRenderer(new MapCellRenderer());
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
                        tableList.setSelectedValue(fmp.getFieldInfo(), true);
                        if (unusedList != null)
                        {
                            unusedList.getSelectionModel().clearSelection();
                        }
                        ignoreMapListUpdate = false;

                    }
                }
            }
        });

        JScrollPane unusedScrollPane = null;
        if (isEditMode || isMappedToAFile)
        {
            unusedWBTMIs = new Hashtable<ImportColumnInfo, WorkbenchTemplateMappingItem>();
            unusedModel  = new DefaultModifiableListModel<ImportColumnInfo>();
            unusedList   = new JList(unusedModel);
            unusedScrollPane = new JScrollPane(unusedList, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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
        
        }
        
        addBtn = createIconBtn("PlusSign", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                addNewMapItem(null, null);
            }
        });
        delBtn = createIconBtn("MinusSign", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                unmap();
            }
        });

        
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
                //adjustViewOrder();
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
                //adjustViewOrder();
                setChanged(true);
            }
        });
        
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
            @Override
            public void mouseClicked(MouseEvent e)
            {
                tableListClicked(e);
            }
        });

        mapToBtn = createIconBtn("DownArrow", "WB_ADD_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                map();
            }
        });
        unmapBtn = createIconBtn("UpArrow", "WB_REMOVE_MAPPING_ITEM", new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                unmap();
            }
        });

        // Adjust all Labels depending on whether we are creating a new template or not
        // and whether it is from a file or not
        String columnListLabel;
        String schemaListLabel;
        String mapListLeftLabel;
        String mapListRightLabel;
        
        // NOte: if workbenchTemplate is null then it is 
        schemaListLabel   = getResourceString("WB_SCHEMA");
        
        if (isMappedToAFile)
        {
            if (isNewTemplate) // New Template being mapped from a file
            {
                columnListLabel   = getResourceString("WB_IMPORTED_COLUMNS");
                mapListLeftLabel  = getResourceString("WB_COLUMNS");
                mapListRightLabel = getResourceString("WB_SCHEMA");

            } else // Editing a template mapped from a file
            {
                columnListLabel   = getResourceString("WB_REMOVE_COLUMNS");
                mapListLeftLabel  = getResourceString("WB_COLUMNS");
                mapListRightLabel = getResourceString("WB_SCHEMA");
            }
            
        } else 
        {

            if (isFromScratch) // Creatingd a brand new template from Scratch
            {
                columnListLabel   = getResourceString("WB_IMPORTED_COLUMNS"); 
                mapListLeftLabel  = getResourceString("WB_SCHEMA");
                mapListRightLabel = "";
                
            } else // Editing Template that wasn't mapped
            {
                columnListLabel   = getResourceString("WB_REMOVE_COLUMNS");
                mapListLeftLabel  = getResourceString("WB_COLUMNS");
                mapListRightLabel = getResourceString("WB_SCHEMA");                
            }
        }
        
        CellConstraints cc = new CellConstraints();
        
        JPanel mainLayoutPanel = new JPanel()
        {
           
            /* (non-Javadoc)
             * @see java.awt.Container#paintComponents(java.awt.Graphics)
             */
            @Override
            public void paintComponent(Graphics g)
            {
                super.paintComponent(g);
                
                if (false)
                {
                    g.setColor(btnPanelColor);
                    
                    
                    Rectangle btnPanelRect = btnPanel.getBounds();
                    Rectangle pRect        = btnPanel.getParent().getBounds();
                    Rectangle schemaRect   = tableList.getParent().getParent().getBounds();
                    Rectangle mapRect      = mapList.getParent().getParent().getBounds();
                    
                    btnPanelRect.translate(pRect.x, pRect.y);
                    
                    Point rightSidePnt = new Point(btnPanelRect.x+btnPanelRect.width-10, btnPanelRect.y); 
                    Point leftSidePnt  = new Point(btnPanelRect.x+10, btnPanelRect.y); 
                    Point botPnt       = new Point(btnPanelRect.x+(btnPanelRect.width/2), btnPanelRect.y+btnPanelRect.height); 
    
                    
                    if (isEditMode || isMappedToAFile)
                    {
                        Rectangle unusedRect = unusedList.getBounds();
                        
                        //System.out.println(btnPanelRect.x+" "+btnPanelRect.y+" "+btnPanelRect.x+" "+(schemaRect.y+schemaRect.height));
                        
                        if (addBtn.isEnabled() || mapToBtn.isEnabled())
                        {
                            g.drawLine(rightSidePnt.x-1, rightSidePnt.y, rightSidePnt.x-1, schemaRect.y+schemaRect.height);
                            g.drawLine(rightSidePnt.x, rightSidePnt.y,   rightSidePnt.x, schemaRect.y+schemaRect.height);
                            g.drawLine(rightSidePnt.x+1, rightSidePnt.y, rightSidePnt.x+1, schemaRect.y+schemaRect.height);
                        }
                        
                        if (mapToBtn.isEnabled() || unmapBtn.isEnabled())
                        {
                            g.drawLine(leftSidePnt.x-1, leftSidePnt.y, leftSidePnt.x-1, unusedRect.y+unusedRect.height);
                            g.drawLine(leftSidePnt.x, leftSidePnt.y,   leftSidePnt.x, unusedRect.y+unusedRect.height);
                            g.drawLine(leftSidePnt.x+1, leftSidePnt.y, leftSidePnt.x+1, unusedRect.y+unusedRect.height);
                        }
    
                        if (addBtn.isEnabled() || mapToBtn.isEnabled() || unmapBtn.isEnabled())
                        {
                            g.drawLine(botPnt.x-1, botPnt.y, botPnt.x-1, mapRect.y);
                            g.drawLine(botPnt.x,   botPnt.y,   botPnt.x, mapRect.y);
                            g.drawLine(botPnt.x+1, botPnt.y, botPnt.x+1, mapRect.y);
                        }
    
                    } else // from scratch
                    {
                        // not implemented yet
                    }
                }
            }
        };
        
        // This UI is used for importing File and Editing, 
        if (isMappedToAFile || isEditMode)
        {
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(upBtn,    cc.xy(1, 2));
            upDownPanel.add(downBtn,  cc.xy(1, 4));
    
            PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("f:p:g, p, f:p:g", "p"));
            PanelBuilder middlePanel = new PanelBuilder(new FormLayout("p, 2px, p" + (isEditMode ? ", 20px, p, 2px, p" : ""), "p"));
            middlePanel.add(unmapBtn, cc.xy(1, 1));
            middlePanel.add(mapToBtn, cc.xy(3, 1));
            if (isEditMode)
            {
                middlePanel.add(addBtn,   cc.xy(5, 1));
                middlePanel.add(delBtn,   cc.xy(7, 1));
            }
            btnPanel = middlePanel.getPanel();
            outerMiddlePanel.add(btnPanel, cc.xy(2, 1));
            
            // Main Pane Layout
            PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(250px;p):g, 5px, p, 5px, f:max(250px;p):g, 2px, p", 
                                                                      "p, 2px, f:max(150px;p):g, 5px, p, 2px, p, 2px, f:p"), mainLayoutPanel);
            
            builder.add(new JLabel(columnListLabel, SwingConstants.CENTER), cc.xy(1, 1));
            builder.add(new JLabel(schemaListLabel, SwingConstants.CENTER), cc.xy(5, 1));
            builder.add(unusedScrollPane,        cc.xy(1, 3));
            builder.add(tableScrollPane,         cc.xy(5, 3));
            
            builder.add(outerMiddlePanel.getPanel(),  cc.xywh(1, 5, 5, 1));
            
            builder.add(new JLabel(mapListLeftLabel, SwingConstants.CENTER), cc.xy(1, 7));
            builder.add(new JLabel(mapListRightLabel, SwingConstants.CENTER), cc.xy(5, 7));
    
            builder.add(mapScrollPane,                cc.xywh(1, 9, 5, 1));
            builder.add(upDownPanel.getPanel(), cc.xy(7, 9));
            
        } else
        {
            // NOTE: When creating a Mapping from scratch, 
            // it has the Add/Del btns intead of the Map and Unmap btns
            PanelBuilder upDownPanel = new PanelBuilder(new FormLayout("p", "f:p:g, p, 2px, p, f:p:g"));        
            upDownPanel.add(upBtn,    cc.xy(1, 2));
            upDownPanel.add(downBtn,  cc.xy(1, 4));
    
            PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("f:p:g, p, f:p:g", "p"));
            PanelBuilder middlePanel = new PanelBuilder(new FormLayout("f:p:g, p, 4px, p, f:p:g", "p"));
            middlePanel.add(addBtn, cc.xy(2,1));
            middlePanel.add(delBtn, cc.xy(4,1));
            btnPanel = middlePanel.getPanel();
            outerMiddlePanel.add(btnPanel, cc.xy(2, 1));
            
            // Main Pane Layout
            PanelBuilder    builder = new PanelBuilder(new FormLayout("f:max(250px;p):g, 2px, p", 
                                                                      "p, 2px, f:max(150px;p):g, 5px, p, 5px, p, 2px, f:p"), mainLayoutPanel);
            
            builder.add(new JLabel(schemaListLabel, SwingConstants.CENTER), cc.xy(1, 1));
            builder.add(tableScrollPane,          cc.xy(1, 3));
            
            builder.add(outerMiddlePanel.getPanel(),   cc.xy(1, 5));
            
            builder.add(new JLabel(getResourceString("WB_DATASET_COLUMNS"),  SwingConstants.CENTER), cc.xy(1, 7));
    
            builder.add(mapScrollPane,          cc.xy(1, 9));
            builder.add(upDownPanel.getPanel(), cc.xy(3, 9));
            
        }
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
        
        pack();
        
        SwingUtilities.invokeLater(new Runnable() {
            @SuppressWarnings("synthetic-access")
            public void run()
            {
                cancelBtn.requestFocus();
                updateEnabledState();
            }
        });
    }
    
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#okButtonPressed()
     */
    @Override
    protected void okButtonPressed()
    {
        adjustViewOrder();
        super.okButtonPressed();
    }

    /**
     * Notification the table ist was clicked, so we can determin if the expanded icon was clicked on.
     * @param e the mouse event
     */
    protected void tableListClicked(MouseEvent e)
    {
        if (e.getButton() != MouseEvent.BUTTON1)
        {
            return;
        }
                
        JList list  = (JList)e.getSource();
        Point p     = e.getPoint();
        int   index = list.locationToIndex(p);
        if (index == -1)
        {
            return;
        }
        TableListItemIFace ti = tableModel.getElementAt(index);

        // if the user clicked an expansion handle, expand the child nodes
        if (ti.isExpandable() && tableInfoListRenderer.getTextOffset() > e.getPoint().x)
        {
            // toggle the state of child node visibility
            //boolean visible = listModel.allChildrenAreVisible(t);
            expand(ti, !ti.isExpanded());
        }
        // otherwise, ignore the click
        else if (!ti.isExpandable() && !ti.isChecked() && e.getClickCount() == 2)
        {
            addNewMapItem(null, null);
            
        } else
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
     * @param wbtmi the workbench mapping item
     */
    protected FieldMappingPanel addMappingItem(final ImportColumnInfo colInfo, 
                                               final ImageIcon mappingIcon,
                                               final WorkbenchTemplateMappingItem wbtmi)
    {
        final FieldMappingPanel fmp = new FieldMappingPanel(colInfo, mappingIcon);
        fmp.setWbtmi(wbtmi);
        
        fmp.setSchemaLabelVisible(workbenchTemplate != null || isMappedToAFile);
        
        mapModel.add(fmp);
        
        //adjustViewOrder();
        
        mapList.setSelectedValue(fmp, true);
        
        return fmp;
    }
    
    /**
     * update the MappingTo and unmapping buttons.
     */
    protected void updateEnabledState()
    {
        int     mapInx    = mapList.getSelectedIndex();
        boolean isNewItem = mapInx > -1 ? mapModel.getElementAt(mapInx).isNew() : false;
        
        TableListItemIFace item   = (TableListItemIFace)tableList.getSelectedValue();
        if (item != null)
        {
            boolean hasField = !item.isExpandable() && !item.isChecked();
            mapToBtn.setEnabled(hasField && !isNewItem && ((unusedList != null && unusedList.getSelectedIndex() > -1) || mapInx > -1));
            unmapBtn.setEnabled(mapInx > -1 && !isNewItem);
            
            if (isEditMode || isMappedToAFile)
            {
                addBtn.setEnabled(hasField && unusedList.getSelectedIndex() == -1);
            } else
            {
                addBtn.setEnabled(hasField);
            }
            
            upBtn.setEnabled(mapInx > 0);
            downBtn.setEnabled(mapInx > -1 && mapInx < mapModel.size()-1);
            
        } else
        {
            mapToBtn.setEnabled(false);
            addBtn.setEnabled(false);
        }
        
        delBtn.setEnabled(mapInx > -1 && isNewItem);
        
        if (okBtn != null && hasChanged)
        {
            okBtn.setEnabled(mapModel.size() > 0);
        }
        
        repaint();
    }
    
    /**
     * Adjusts the order of all the templates.
     */
    protected void adjustViewOrder()
    {
        for (int i=0;i<mapModel.size();i++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            fmp.setViewOrder((short)i);
            //System.out.println(fmp.getFieldName()+" "+i+" "+fmp.getViewOrder());
        }
    }
    
    /**
     * Fill in the JList's model from the list of fields.
     * @param item the table who's list we should use
     * @param expand tells the table whether to expand or shrink
     */
    protected void expand(final TableListItemIFace item, final boolean expand)
    {
        TableInfo ti = (TableInfo)item;
        ti.setExpanded(expand);
        if (expand)
        {
            int index = tableModel.indexOf(item);
            Vector<FieldInfo> fiItems = ti.getFieldItems();
            for (int i=fiItems.size()-1;i>=0;i--)
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
     * Maps an item from the unused list and table list.
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
            fmp = map(colInfo, fi, wbtmi);
        }
        
        unusedModel.remove(colInfo);
        
        if (wbtmi != null)
        {
            unusedWBTMIs.remove(colInfo);
        }
        
        return fmp;
    }
    
    /**
     * Maps a ColInfo and FieldInfo and wbtmi is optional (can be null)
     * @param colInfo the colinfo from a file
     * @param fi the field info from the table list
     * @param wbtmi the optional template info, which is null for new columns and not null when editting old already saved cols
     * @return the new mapping panel
     */
    protected FieldMappingPanel map(final ImportColumnInfo colInfo, final FieldInfo fi, final WorkbenchTemplateMappingItem wbtmi)
    {
        FieldMappingPanel fmp;
        fi.setInUse(true);
        if (colInfo != null)
        {
            fmp = addMappingItem(colInfo, IconManager.getIcon(fi.getTableinfo().getObjTitle(), IconManager.IconSize.Std24), wbtmi);
            fmp.setIcon(IconManager.getIcon(fi.getTableinfo().getObjTitle(), IconManager.IconSize.Std24));
            fmp.setFieldInfo(fi);
            
        } else
        {
            fmp = addNewMapItem(fi, wbtmi);
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
        
        //adjustViewOrder();

        // Need to Sort Here
        updateEnabledState();
    }
    
    /**
     * Adds a new Column to the Template that is not represented by a row in a file (if there is a file). 
     * @param fieldInfoArg
     * @param wbtmi
     * @return
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
        fmp.setNew(fieldInfoArg == null); // new Items that was not in the data file.
        
        setChanged(true);
        
        tableList.repaint();

        updateEnabledState();
        
        return fmp;
    }
    
    /**
     * @param fieldInfo
     * @return
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
                TableListItemIFace item = tableModel.getElementAt(i);
                if (!item.isExpandable())
                {
                    FieldInfo fi           = (FieldInfo)item;
                    String    tblFieldName = fi.getFieldInfo().getName().toLowerCase();
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
                
                map(colInfo, fieldInfo, null);
                
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
        
        //adjustViewOrder();
        
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
                    addNewMapItem(fi, wbtmi);
                    tablesInUse.put(ti, true);
                    break;
                }
                fieldNum++;
            }
        }
        doingFill = false;
        
        //adjustViewOrder();
        
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
            FieldMappingPanel fmp = mapModel.getElementAt(i);
            //System.out.println("getUpdatedItems "+fmp.getWbtmi().getCaption()+" "+i+" "+fmp.getViewOrder()+" - "+fmp.getWbtmi().getViewOrder());
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
        protected JLabel            colFieldLabel;
        protected JLabel            schemaLabel;
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
            
            PanelBuilder    builder = new PanelBuilder(new FormLayout("2px,150px, p:g, p, p:g, 150px,2px", "p:g"), this);
            CellConstraints cc      = new CellConstraints();

            colFieldLabel   = new JLabel(colInfo.getColName());
            schemaLabel = new JLabel(noMappingStr, SwingConstants.RIGHT);
            
            setFocusable(true);
            
            builder.add(colFieldLabel,                       cc.xy(2,1));
            builder.add(iconLabel = new JLabel(mappingIcon), cc.xy(4,1));
            builder.add(schemaLabel,                         cc.xy(6,1));
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
        public void setWbtmi(final WorkbenchTemplateMappingItem wbtmi)
        {
            setSchemaLabelVisible(wbtmi != null || isMappedToAFile);
            
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
                colFieldLabel.setForeground(hasFocus ? Color.WHITE : Color.BLACK);
                schemaLabel.setForeground(hasFocus ? Color.WHITE : Color.BLACK);
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
            return colFieldLabel.getText();
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
            panel.setToolTipText(panel.getFieldInfo() != null ? panel.getToolTipText() : "");
            return (Component)value;
        }
        
    }
    
}
