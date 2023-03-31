/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
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
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.SpLocaleContainer;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonTreeDef;
import edu.ku.brc.specify.datamodel.TreeDefItemStandardEntry;
import edu.ku.brc.specify.datamodel.Treeable;
import edu.ku.brc.specify.datamodel.WorkbenchTemplate;
import edu.ku.brc.specify.datamodel.WorkbenchTemplateMappingItem;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.DB;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadData;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMappingDef;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.UploadMessage;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.Uploader;
import edu.ku.brc.specify.tasks.subpane.wb.wbuploader.WorkbenchUploadMapper;
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
public class TemplateEditor extends CustomDialog {
    private static final Logger log = Logger.getLogger(TemplateEditor.class);

    private static int taxId = 4;
    private static int taxOnlyId = 4000;

    protected JButton mapToBtn;
    protected JButton unmapBtn;
    protected JButton upBtn;
    protected JButton downBtn;
    protected JLabel descriptionLbl;
    protected JList mapList;
    protected DefaultModifiableListModel<FieldMappingPanel> mapModel;
    protected JScrollPane mapScrollPane;

    protected JList tableList;
    protected DefaultModifiableListModel<TableInfo> tableModel;

    protected JList fieldList;
    protected DefaultModifiableListModel<FieldInfo> fieldModel;

    protected Vector<WorkbenchTemplateMappingItem> deletedItems = new Vector<WorkbenchTemplateMappingItem>();

    protected boolean hasChanged = false;
    protected boolean doingFill = false;
    protected Color btnPanelColor;
    protected JPanel btnPanel;

    protected ImportDataFileInfo dataFileInfo = null;
    protected WorkbenchTemplate workbenchTemplate = null;
    protected String schemaName = null;
    protected DBTableIdMgr databaseSchema;
    protected List<TreeDefItemStandardEntry> taxRanks = null;

    protected boolean isMappedToAFile;
    protected boolean isEditMode;
    protected boolean isReadOnly = false;
    protected boolean ignoreMapListUpdate = false;

    protected ImageIcon blankIcon = IconManager
            .getIcon(
                    "BlankIcon",
                    IconManager.STD_ICON_SIZE);

    protected TableInfoListRenderer tableInfoListRenderer;

    protected List<String> tablesWithAttachments = null;

    /**
     * Constructor.
     *
     * @param dlg          the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public TemplateEditor(final Frame frame, final String title, final ImportDataFileInfo dataFileInfo, final String schemaName) throws Exception {
        super(frame, title, true, OKCANCELHELP, null);

        this.dataFileInfo = dataFileInfo;
        this.isMappedToAFile = dataFileInfo != null;
        this.isEditMode = false;
        this.schemaName = schemaName;

        helpContext = dataFileInfo == null ? "WorkbenchNewMapping" : "WorkbenchEditMapping";

        buildUploadDefs();
        databaseSchema = schemaName == null ? WorkbenchTask.getDatabaseSchema(false) : WorkbenchTask.buildDatabaseSchema(schemaName);

        int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
        SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.WORKBENCH_SCHEMA,
                disciplineeId,
                databaseSchema,
                SchemaI18NService.getCurrentLocale());
        createUI();
    }

    /**
     * Constructor.
     *
     * @param dlg          the dialog this will be housed into
     * @param dataFileInfo the information about the data file.
     */
    public TemplateEditor(final Frame frame, final String title, final WorkbenchTemplate wbTemplate, final String schemaName) throws Exception {
        super(frame, title, true, OKCANCELHELP, null);

        this.workbenchTemplate = wbTemplate;
        this.isMappedToAFile = StringUtils.isNotEmpty(wbTemplate.getSrcFilePath());
        this.isEditMode = this.workbenchTemplate != null;
        this.schemaName = schemaName;

        helpContext = "WorkbenchEditMapping";

        buildUploadDefs();
        databaseSchema = schemaName == null ? WorkbenchTask.getDatabaseSchema(false) : WorkbenchTask.buildDatabaseSchema(schemaName);

        int disciplineeId = AppContextMgr.getInstance().getClassObject(Discipline.class).getDisciplineId();
        SchemaI18NService.getInstance().loadWithLocale(SpLocaleContainer.WORKBENCH_SCHEMA,
                disciplineeId,
                databaseSchema,
                SchemaI18NService.getCurrentLocale());
        checkMappings();
        createUI();
    }

    private void checkMappings() throws WBUnMappedItemException {
        if (workbenchTemplate != null) {
            for (WorkbenchTemplateMappingItem wbtmi : workbenchTemplate.getWorkbenchTemplateMappingItems()) {
                boolean mappedTheField = false;
                DBTableInfo ti = databaseSchema.getInfoById(wbtmi.getSrcTableId());
                if (ti != null) {
                    for (DBFieldInfo fi : ti.getFields()) {
                        if (wbtmi.getFieldName().equals(fi.getName())) {
                            mappedTheField = true;
                            break;
                        }
                    }
                }
                if (!mappedTheField) {
                    throw new WBUnMappedItemException(wbtmi);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CustomDialog#createUI()
     */
    @Override
    public void createUI()
    {
        super.createUI();
        // Create the Table List
        Vector<TableInfo> tableInfoList = new Vector<TableInfo>();
        for (DBTableInfo ti : databaseSchema.getTables())
        {
            if (Arrays.binarySearch(WorkbenchTask.restrictedTables, ti.getName().toLowerCase()) < 0 
            		&& StringUtils.isNotEmpty(ti.toString()))
            {
            	TableInfo tableInfo = new TableInfo(ti, IconManager.STD_ICON_SIZE);
                tableInfoList.add(tableInfo); 
                
                Vector<FieldInfo> fldList = new Vector<FieldInfo>();
                for (DBFieldInfo fi : ti.getFields())
                {
                    String fldTitle = fi.getTitle().replace(" ", "");
                    if (fldTitle.equalsIgnoreCase(fi.getName()))
                    {
                    	//get title from mapped field
                    	UploadInfo upInfo = getUploadInfo(fi);
                    	DBFieldInfo mInfo = getMappedFieldInfo(fi);
                    	if (mInfo != null)
                    	{
                    		String title = mInfo.getTitle();
                    		if (upInfo != null && upInfo.getSequence() != -1)
                    		{
                    			title += " " + (upInfo.getSequence() + 1);
                    		}
                    		//if mapped-to table is different than the container table used
                    		// in the wb, add the mapped-to table's title
                    		if (mInfo.getTableInfo().getTableId() != ti.getTableId())
                    		{
                    			title = mInfo.getTableInfo().getTitle() + " " + title;
                    		}
                    		fi.setTitle(title);
                    	}
                    }
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
                    updateFieldDescription();
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
                                    updateFieldDescription();
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
        btnPanel.setOpaque(false);
        
        PanelBuilder outerMiddlePanel = new PanelBuilder(new FormLayout("c:p:g", "f:p:g, p, f:p:g"));
        outerMiddlePanel.add(btnPanel, cc.xy(1, 2));
        outerMiddlePanel.getPanel().setOpaque(false);
        
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
        
        JPanel megaPanel = new JPanel(new BorderLayout());
        megaPanel.add(mainLayoutPanel, BorderLayout.CENTER);
        descriptionLbl = createLabel("  ", SwingConstants.LEFT);
        //PanelBuilder descBuilder = new PanelBuilder(new FormLayout("f:p:g, 3dlu","p"));
        //descBuilder.add(descriptionLbl, cc.xy(1, 1));
        //megaPanel.add(descBuilder.getPanel(), BorderLayout.SOUTH);
        megaPanel.add(descriptionLbl, BorderLayout.SOUTH);
        //contentPanel = mainLayoutPanel;
        contentPanel = megaPanel;
        
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
                updateFieldDescription();
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
        UIHelper.setTextToClipboard(clipBrdTxt.toString());
        
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
    
    protected UploadInfo getUploadInfo(DBFieldInfo fInfo)
    {
		String key = fInfo.getTableInfo().getName().toLowerCase() + "." + fInfo.getName().toLowerCase();
		return uploadDefs.get(key);    	
    }
    
    protected DBFieldInfo getMappedFieldInfo(DBFieldInfo fInfo)
    {
		UploadInfo upInfo = getUploadInfo(fInfo);
		if (upInfo == null) 
		{
			return getFieldInfoByTableField(fInfo.getTableInfo().getName(), fInfo.getName());
		} else
		{
			return upInfo.getFieldInfo();
		}    	
    	
    }
    protected DBFieldInfo getMappedFieldInfo(FieldInfo fldItem)
    {
		DBFieldInfo fInfo = ((FieldInfo )fldItem).getFieldInfo();
		return getMappedFieldInfo(fInfo);
    }
    
    protected void updateFieldDescription()
    {
    	TableListItemIFace fldItem = (TableListItemIFace)fieldList.getSelectedValue();
    	//fieldList.setToolTipText(null);
    	descriptionLbl.setText("   ");
		descriptionLbl.setToolTipText(null);
    	if (fldItem != null && fldItem instanceof FieldInfo)
    	{
    		DBFieldInfo mInfo = getMappedFieldInfo((FieldInfo)fldItem);
    		if (mInfo == null)
    		{
    			//System.out.println("UploadInfo not found for " + fldItem);
    		} else
    		{
    			//System.out.println(mInfo.getDescription());
    			if (!mInfo.getDescription().equalsIgnoreCase(mInfo.getTitle()) && !mInfo.getDescription().equalsIgnoreCase(mInfo.getName()))
    			{
    				//fieldList.setToolTipText(mInfo.getDescription());
    				String desc = "  " + getResourceString("TemplateEditor.SpFldDesc") + " " + mInfo.getDescription(); 
    				descriptionLbl.setText(desc);
    				descriptionLbl.setToolTipText(desc);
    			}
    		}
    	}
    }
    
    protected Map<String, UploadInfo> uploadDefs = new HashMap<String, UploadInfo>();
    
    @SuppressWarnings("unchecked")
    protected void buildUploadDefs() throws Exception
    {
        Element defs;
        if (WorkbenchTask.isCustomizedSchema()) 
        {
        		defs = XMLHelper.readFileToDOM4J(new File(UIRegistry.getAppDataDir() + File.separator + "specify_workbench_upload_def.xml"));
        } else
        {
        	defs = XMLHelper.readDOMFromConfigDir("specify_workbench_upload_def.xml");
        }
        		
		List<Object> flds = defs.selectNodes("field");
		uploadDefs.clear();
		for (Object fld : flds)
		{
			String table = XMLHelper.getAttr((Element )fld, "table", null);
			String actualTable = XMLHelper.getAttr((Element )fld, "actualtable", null);
			if (actualTable == null)
			{
				actualTable = table;
			}
			String field = XMLHelper.getAttr((Element )fld, "name", null);
			String actualField = XMLHelper.getAttr((Element )fld, "actualname", null);
			String relatedField = XMLHelper.getAttr((Element )fld, "relatedfieldname", null);
			String mappedField = field;
			if (actualField != null)
			{
				mappedField = actualField;
			}
			if (relatedField != null)
			{
				mappedField = relatedField;
			}
			int sequence = XMLHelper.getAttr((Element)fld, "onetomanysequence", -1);
			String key = table.toLowerCase() + "." + field.toLowerCase();
			DBFieldInfo fInfo = getFieldInfoByTableField(actualTable, actualField);
			if (fInfo != null) 
			{
				//System.out.println("putting " + key + " => " + actualTable.toLowerCase() + "." + mappedField.toLowerCase());
				uploadDefs.put(key, new UploadInfo(fInfo, sequence));
			}
		}
    }
    
    protected DBFieldInfo getFieldInfoByTableField(String tbl, String fld)
    {
		DBTableInfo tInfo = null;
		for (DBTableInfo t : DBTableIdMgr.getInstance().getTables())
		{
			if (t.getName().equalsIgnoreCase(tbl))
			{
				tInfo = t;
				break;
			}
		}
		if (tInfo != null)
		{
			for (DBFieldInfo f : tInfo.getFields())
			{
				if (f.getName().equalsIgnoreCase(fld))
				{
					return f;
				}
			}
		}
    	return null;
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
     * @return true if unmap operation should continue
     * 
     *
     */
    protected boolean okToUnmap(FieldMappingPanel fmp)
    {
    	if (!isEditMode)
    	{
    		return true;
    	}
    	
    	return checkAttachmentsForUnmap(fmp);
    	
    }
    
    /**
     * @param fmp
     * @return true if un-mapping fmp is ok relative to attachments
     * 
     * Checks to see if unmapping fmp removes the table that any attachments are using. If so
     * the user is asked to confirm the unmapping.
     */
    protected boolean checkAttachmentsForUnmap(FieldMappingPanel fmp)
    {
    	if (fmp.getFieldInfo() == null)
    	{
    		return true;
    	}
    	if (tablesWithAttachments == null)
    	{
    		buildTablesWithAttachments();
    	}
    	if (tablesWithAttachments.size() == 0)
    	{
    		return true;
    	}
    	if (!isLastMappingToItsTable(fmp))
    	{
    		return true;
    	}
		String fmpTblName = getAttachToTblName(fmp);
		if (fmpTblName == null)
		{
			return true;
		}
    	for (String attachedToTable : tablesWithAttachments)
    	{
    		if (fmpTblName.equalsIgnoreCase(attachedToTable))
    		{
    			return confirmUnmapAttachToTable(fmp);
    		}
    	}
    	return true;
    }
    
    /**
     * @param fmp
     * @return
     */
    protected boolean confirmUnmapAttachToTable(FieldMappingPanel fmp)
    {
    	return UIRegistry.displayConfirmLocalized("TemplateEditor.ConfirmAttachedToUnmapTitle", "TemplateEditor.confireAttachedToUnmap", "OK", "Cancel",
    			JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * @param fmp
     * @return
     */
    protected String getAttachToTblName(FieldMappingPanel fmp)
    {
    	if (fmp.getFieldInfo() == null)
    	{
    		return null;
    	}
    	if (!isTreeMapping(fmp) || getRank(fmp.getFieldInfo()) == -1)
    	{
    		return fmp.getFieldInfo().getTableinfo().getName();
    	}
    	String tblName = fmp.getFieldInfo().getTableinfo().getName();
    	return (tblName.equalsIgnoreCase("determination") ? "taxon" : tblName) + getRank(fmp.getFieldInfo());
    }
    
    /**
     * @param fmp
     * @return true if fmp is a mapping to a treeable table
     */
    protected boolean isTreeMapping(FieldMappingPanel fmp)
    {
    	if (fmp.getFieldInfo() == null)
    	{
    		return false;
    	}
    	if (Treeable.class.isAssignableFrom(fmp.getFieldInfo().getTableinfo().getClassObj()))
    	{
    		return true;
    	}
    	if (fmp.getFieldInfo().getTableinfo().getClassObj().equals(Determination.class))
    	{
    		return getRank(fmp.getFieldInfo()) != -1;
    	}
    	return false;
    }
    
    /**
     * @param fmp
     * @return true if no other fields in fmp's table are mapped
     */
    protected boolean isLastMappingToItsTable(FieldMappingPanel fmp)
    {
    	if (fmp.getFieldInfo() == null)
    	{
    		return true; //???
    	}
    	for (int i = 0; i < mapModel.size(); i++)
    	{
    		FieldMappingPanel fmp2 = mapModel.getElementAt(i);
    		if (fmp2.getFieldInfo() != null)
    		{
    			if (fmp2 != fmp && fmp2.getFieldInfo().getTableinfo().getName().equals(fmp.getFieldInfo().getTableinfo().getName()))
    			{
    				if (isTreeMapping(fmp))
    				{
    					if (getRank(fmp.getFieldInfo()) == getRank(fmp2.getFieldInfo()))
    					{
    						return false;
    					}
    				} else
    				{
    					return false;
    				}
    			}
    		}
    	}
    	return true;
    }
    
    /**
     * build list of distinct tables to which attachments are currently linked
     */
    protected void buildTablesWithAttachments()
    {
    	tablesWithAttachments = new Vector<String>();
    	//this depends on the current practice of maintaining a 1-1 between Workbench and WorkbenchTemplate
    	String sql = "select distinct attachToTableName from workbenchrowimage wri inner join workbenchrow wr on wr.WorkbenchRowID = wri.WorkbenchRowID "
    		+ "inner join workbench w on w.WorkbenchID = wr.WorkbenchID "
    		+ "where AttachToTableName is not null and WorkbenchTemplateID = " + workbenchTemplate.getId();
    	Vector<Object> results = BasicSQLUtils.querySingleCol(sql);
    	for (Object result : results)
    	{
    		tablesWithAttachments.add(result.toString());
    	}
    }
    
    /**
     * Unmap the Field or remove the item if there is no file.
     */
    protected void unmap()
    {
        FieldMappingPanel fmp = (FieldMappingPanel)mapList.getSelectedValue();
        
        if (!okToUnmap(fmp))
        {
        	return;
        }
        
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

        //checkUploadability();
        
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
    	return tableInUse(currentMap, 4000);
    }

    /**
     * @param currentMap
     * @param tableId
     * @return true if fields are currently mapped to tableId.
     */
    protected boolean tableInUse(final FieldMappingPanel currentMap, final int tableId)
    {
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() == tableId)
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
    	return onlyTableInUse(currentMap, 4000);
    }
    
    /**
     * @param currentMap
     * @return true if all mappings are to the table with tableId
     */
    protected boolean onlyTableInUse(final FieldMappingPanel currentMap, final int tableId)
    {
    	for (int m = 0; m < mapModel.size(); m++)
    	{
    		FieldMappingPanel fmp = (FieldMappingPanel )mapModel.get(m);
    		if (fmp != currentMap && fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() != tableId)
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
    			if (rank != -1 && rank < high)
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
     */
    protected int getRankForTaxMapping(final FieldInfo fi)
    {
    	if (taxRanks == null)
    	{
    		taxRanks = TaxonTreeDef.getStandardLevelsStatic();
    	}
    	String fldName = fi.getFieldInfo().getName();
    	if (fi.getTableinfo().getClassObj().equals(Determination.class))
    	{
    		//strip off trailing 1 or 2 or 3 ...
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
     * @return
     */
    protected int getRank(final FieldInfo fi)
    {
    	if (fi.getTableinfo().getClassObj().equals(Determination.class) || fi.getTableinfo().getClassObj().equals(Taxon.class))
    	{
    		return getRankForTaxMapping(fi);
    	}
    	return -1;
    }
    
    /**
     * @param fi
     * @return true if fi is mappable
     * 
     * Checks for conflicts caused by availability of Taxon ranks in multiple tables.
     * Ensures that Agent uploads are standalone - Agent can't be mapped when fields from other tables are mapped and fields
     * from other tables can't be mapped when agent fields are mapped. 
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
    	if (tableInUse(currentMap, Agent.getClassTableId()) && fi.getTableinfo().getTableId() != Agent.getClassTableId())
    	{
    		return UIRegistry.getResourceString("TemplateEditor.AgentOnly");
    	}
    	if (fi.getTableinfo().getTableId() == Agent.getClassTableId() && !onlyTableInUse(currentMap, Agent.getClassTableId()))
    	{
    		return UIRegistry.getResourceString("TemplateEditor.AgentOnlyOnly");
    	}
    	if (tableInUse(currentMap, ReferenceWork.getClassTableId()) && fi.getTableinfo().getTableId() != ReferenceWork.getClassTableId())
    	{
    		return UIRegistry.getResourceString("TemplateEditor.ReferenceWorkOnly");
    	}
    	if (fi.getTableinfo().getTableId() == ReferenceWork.getClassTableId() && !onlyTableInUse(currentMap, ReferenceWork.getClassTableId()))
    	{
    		return UIRegistry.getResourceString("TemplateEditor.ReferenceWorkOnlyOnly");
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
    protected FieldInfo findFieldInfo(final String tblArg, final String fld)
    {
        if (tblArg == null || fld == null)
            return null;
        String tbl = "taxononly".equalsIgnoreCase(tblArg) ? "taxon" : tblArg;
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
    	//orders by tableId, which has effect of making 'core' tables the first to be mapped, and makes sure Determination follows Taxon;
    	Vector<TableInfo> result = new Vector<TableInfo>();
//    	Integer detIndex = null;
//    	Integer taxIndex = null;
//    	Integer taxOnlyIndex = null;
    	for (int t = 0; t < tableModel.size(); t++)
    	{
    		TableInfo tbl = (TableInfo )tableModel.get(t);
//    		if (tbl.getTableInfo().getClassObj().equals(Determination.class))
//    		{
//    			detIndex = t;
//    		}
//    		if (tbl.getTableInfo().getTableId() == 4)
//    		{
//    			taxIndex = t;
//    		}
//    		if (tbl.getTableInfo().getTableId() == 4000)
//    		{
//    			taxOnlyIndex = t;
//    		}
    		result.add(tbl);
    	}
    	Collections.sort(result, new Comparator<TableInfo>(){

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(TableInfo arg0, TableInfo arg1)
			{
				int tid0 = arg0.getTableInfo().getTableId();
				int tid1 = arg1.getTableInfo().getTableId();
				if (tid0 == taxOnlyId)
				{
					tid0 = taxId;
				}
				if (tid1 == taxOnlyId)
				{
					tid1 = taxId;
				}
//				if (tid0 == taxId)
//				{
//					if (tid1 == detId)
//					{
//						tid1 = taxId -1;
//					}
//				}
//				if (tid1 == taxId)
//				{
//					if (tid0 == detId)
//					{
//						tid0 = taxId -1;
//					}
//				}
				return tid0 < tid1 ? -1 : tid0 == tid1 ? 0 : 1;
			}
    		
    	});
//    	if (taxOnlyIndex != null && taxOnlyIndex.intValue() != tableModel.size()-1)
//    	{
//    		TableInfo taxOnly = result.remove(taxOnlyIndex.intValue());
//    		result.add(taxOnly);
//    		if (detIndex != null && detIndex.intValue() > taxOnlyIndex)
//    		{
//    			detIndex += 1;
//    		}
//    		if (taxIndex != null && taxIndex.intValue() > taxOnlyIndex)
//    		{
//    			taxIndex += 1;
//    		}
//    	}
//    	if (detIndex != null && taxIndex != null && detIndex.intValue() < taxIndex.intValue())
//    	{
//    		TableInfo det = result.remove(detIndex.intValue());
//    		result.add(taxIndex, det);
//    	}
    	for (TableInfo tblInfo : result)
    	{
    		System.out.println(tblInfo.getTableInfo());
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
        
        // If we had no luck then just loop through everything looking for it.
        // Actually we should do this search first since it is more specific.
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
        boolean unMapTaxOnlys = false;
        boolean contaminateAgent = false;
        boolean agentMapped = false;
        boolean nonAgentMapped = false;
        for (Integer tblId : getTblsMapped())
        {
        	if (!(tblId.equals(DBTableIdMgr.getInstance().getByClassName(Taxon.class.getName()).getTableId())
        			|| tblId.equals(DBTableIdMgr.getInstance().getByClassName(Determination.class.getName()).getTableId())
        			|| tblId.equals(4000)))
        	{
        		doTaxOnlyRemap = false;
        	}
        	if (tblId.equals(4000))
        	{
        		unMapTaxOnlys = true;
        	}
        	if (tblId.equals(Agent.getClassTableId()))
        	{
        		agentMapped = true;
        	} else
        	{
        		nonAgentMapped = true;
        	}
        }
        contaminateAgent = agentMapped && nonAgentMapped;
        if (contaminateAgent)
        {
            int agentFlds = 0;
            Vector<Integer> others = new Vector<Integer>();
        	for (int m=0; m<mapModel.getSize(); m++)
            {
                FieldMappingPanel fmp = mapModel.getElementAt(m);
                if (fmp.getFieldInfo() != null) 
                {
                	if (fmp.getFieldInfo().getTableinfo().getTableId() == Agent.getClassTableId())
                	{
                		agentFlds++;
                	} else
                	{
                		others.add(fmp.getFieldInfo().getTableinfo().getTableId());
                	}
                }
            }
        	if (agentFlds < others.size())
        	{
        		unmapTable(Agent.getClassTableId());
        	} else
        	{
        		for (Integer tblId : others)
        		{
        			unmapTable(tblId);
        		}
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
				if (fmp.getFieldInfo() != null) 
				{
					String fldName = fmp.getFieldInfo().getFieldInfo()
							.getName();
					if (fldName.endsWith("1") || fldName.endsWith("2")) 
					{
						fldName = fldName.substring(0, fldName.length() - 1);
					}
					FieldInfo newInfo = null;
					// System.out.println("re-mapping " + fldName);
					for (FieldInfo fi : taxaOnly.getFieldItems()) 
					{
						System.out.println("  checking "
								+ fi.getFieldInfo().getName());
						if (fi.getFieldInfo().getName()
								.equalsIgnoreCase(fldName)) 
						{
							newInfo = fi;
							break;
						}
					}
					if (newInfo == null) 
					{
						log.warn("Couldn't find Taxon Only field info for "
								+ fldName);
						continue;
					}

					if (fmp.getFieldInfo() != newInfo) 
					{
						fmp.getFieldInfo().setInUse(false);
						newInfo.setInUse(true);
						fmp.setFieldInfo(newInfo);
						fmp.setIcon(DBTableIdMgr.getInstance()
								.getByClassName(Taxon.class.getName())
								.getIcon(IconManager.STD_ICON_SIZE));
					}
				}
            }
        }
        else if (unMapTaxOnlys)
        {
            unmapTable(4000);
        }
    }
    
    /**
     * @param tableId
     * 
     * Unmaps all mappings to table with tableId
     */
    protected void unmapTable(final int tableId)
    {
        for (int m=0; m<mapModel.getSize(); m++)
        {
            FieldMappingPanel fmp = mapModel.getElementAt(m);
            if (fmp.getFieldInfo() != null && fmp.getFieldInfo().getTableinfo().getTableId() == tableId)
            {
            	log.info("unmapping " + fmp.getFieldName());
                fmp.getFieldInfo().setInUse(false);
                fmp.setFieldInfo(null);
                fmp.setIcon(blankIcon);
            }
        }
    }
    
    protected WorkbenchTemplateMappingItem getMappingByDataColIdx(final Vector<WorkbenchTemplateMappingItem> items, final int dataColIdx)
    {
    	for (WorkbenchTemplateMappingItem item : items)
    	{
    		if (item.getOrigImportColumnIndex().intValue() == dataColIdx)
    		{
    			return item;
    		}
    	}
    	return null;
    }
    
//    protected void mapDataFileToTemplate()
//    {
//        doingFill = true;
//        
//        // Map the TableInfo's Table ID to it's index in the Vector
//        Hashtable<Integer, Integer> tblIdToListIndex = new Hashtable<Integer, Integer>();
//        for (int i=0;i<tableModel.size();i++)
//        {
//            TableListItemIFace ti = tableModel.getElementAt(i);
//            if (ti.isExpandable())
//            {
//                tblIdToListIndex.put(((TableInfo)ti).getTableInfo().getTableId(), i);
//            }
//        }
//
//        // Get and Sort the list of WBTMIs
//        Vector<WorkbenchTemplateMappingItem> items = new Vector<WorkbenchTemplateMappingItem>(workbenchTemplate.getWorkbenchTemplateMappingItems());
//        
//        //Hashtable<TableInfo, Boolean> tablesInUse = new Hashtable<TableInfo, Boolean>();
//        
//        for (ImportColumnInfo colInfo : dataFileInfo.getColInfo())
//        {
//        	WorkbenchTemplateMappingItem item = getMappingByDataColIdx(items, colInfo.getColInx());
//        	if (item != null)
//        	{
//                int       inx = tblIdToListIndex.get(item.getSrcTableId());
//                TableInfo tblInfo  = tableModel.getElementAt(inx);
//                
//                 for (FieldInfo fieldInfo : tblInfo.getFieldItems())
//                {
//                    if (item.getFieldName().equals(fieldInfo.getFieldInfo().getName()))
//                    {
//                    	map(null, colInfo, tblInfo, fieldInfo, null);
//                    	break;
//                    }
//                }
//        	}
//        	else
//        	{
//                FieldMappingPanel fmp = new FieldMappingPanel(colInfo, blankIcon);
//                fmp.getArrowLabel().setVisible(false);
//                mapModel.add(fmp);
//        	}
//        }
//        doingFill = false;
//        
//        mapList.getSelectionModel().clearSelection();
//        tableList.getSelectionModel().clearSelection();
//
//    }
    
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
        
        for (WorkbenchTemplateMappingItem  wbtmi : items)
        {
            int       inx = tblIdToListIndex.get(wbtmi.getSrcTableId());
            TableInfo ti  = tableModel.getElementAt(inx);

            for (FieldInfo fi : ti.getFieldItems())
            {
                if (wbtmi.getFieldName().equals(fi.getFieldInfo().getName()))
                {
                    addNewMapItem(fi, wbtmi);
                    break;
                }
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
                Integer                      origColNum = -1;
                if (fmp.getWbtmi() == null)
                {
                    ImportColumnInfo colInfo = fmp.getColInfo();
                    item = new WorkbenchTemplateMappingItem();
                    item.initialize();
                
                    //trim to fit. User will have been warned of truncation during import.
                    String caption = colInfo.getCaption();
                    int maxCapLen = DBTableIdMgr.getInstance().getInfoByTableName("workbenchtemplatemappingitem").getFieldByColumnName("Caption").getLength();
                    int maxImportedColNameLen = DBTableIdMgr.getInstance().getInfoByTableName("workbenchtemplatemappingitem").getFieldByColumnName("ImportedColName").getLength();
                    item.setCaption(caption.length() > maxCapLen ? caption.substring(0, maxCapLen) : caption);
                    item.setImportedColName(caption.length() > maxImportedColNameLen ? caption.substring(0, maxImportedColNameLen) : caption);
                    origColNum = fmp.isAdded() ? -1 : colInfo.getColInx();
                    
                    item.setXCoord(Integer.valueOf(colInfo.getFormXCoord()).shortValue());
                    item.setYCoord(Integer.valueOf(colInfo.getFormYCoord()).shortValue());
                    item.setFieldType(Integer.valueOf(colInfo.getFrmFieldType()).shortValue());
                    item.setMetaData(colInfo.getFrmMetaData());
                    
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
                item.setDataFieldLength(len == -1 ? 32767 : len);
                
                item.setViewOrder(fmp.getViewOrder());
                item.setOrigImportColumnIndex(origColNum.shortValue());
                
            }
        }

        return newItems;
    }
    
    /**
     * @param item
     * @return
     */
    protected boolean updateMappingItem(FieldMappingPanel fmp, WorkbenchTemplateMappingItem item)
    {
        if (fmp.getFieldInfo() == null)
        {
        	return false;
        }
        
        
        ImportColumnInfo colInfo = fmp.getColInfo();
        FieldInfo fieldInfo = fmp.getFieldInfo();
        
        item.setCaption(colInfo.getCaption());
        item.setImportedColName(colInfo.getColName());
        item.setXCoord(Integer.valueOf(colInfo.getFormXCoord()).shortValue());
        item.setYCoord(Integer.valueOf(colInfo.getFormYCoord()).shortValue());
        item.setFieldType(Integer.valueOf(colInfo.getFrmFieldType()).shortValue());
        item.setMetaData(colInfo.getFrmMetaData());
        
        Integer origColNum = fmp.isAdded() ? -1 : colInfo.getColInx();
            
        item.setFieldName(fieldInfo.getFieldInfo().getName());
        item.setSrcTableId(fieldInfo.getTableinfo().getTableId());
        item.setTableName(fieldInfo.getTableinfo().getName());
        short len = (short)fieldInfo.getFieldInfo().getLength();
        item.setDataFieldLength(len == -1 ? 15 : len);
        
        item.setViewOrder(fmp.getViewOrder());
        item.setOrigImportColumnIndex(origColNum.shortValue());
        
    	return true;
    }
    
    protected boolean checkUploadability()
    {
    	Collection<WorkbenchTemplateMappingItem> mappings = getCurrentMappings();
		WorkbenchUploadMapper importMapper = new WorkbenchUploadMapper(mappings);
        try
        {
        	DB db = new DB();
    		Vector<UploadMappingDef> maps = importMapper.getImporterMapping();
        	Uploader result = new Uploader(db, new UploadData(maps, null), null, mappings, true);
        	Vector<UploadMessage> structureErrors = result.verifyUploadability();
        	if (structureErrors.size() > 0) 
        	{ 
        		//throw new WorkbenchValidatorException(structureErrors);
        		return false;
        	}
        	return true;
        } catch (Exception ex)
        {
        	//throw new UploaderException(ex);
        	return false;
        }
    	
    }
    /**
     * @return current 'snapshot' of mappings
     */
    protected Collection<WorkbenchTemplateMappingItem> getCurrentMappings()
    {
        Collection<WorkbenchTemplateMappingItem> result = new Vector<WorkbenchTemplateMappingItem>();
    	for (int i=0;i<mapModel.size();i++)
        {
    		WorkbenchTemplateMappingItem item = new WorkbenchTemplateMappingItem();
    		item.initialize();
    		if (updateMappingItem(mapModel.getElementAt(i), item))
    		{
    			result.add(item);
    		}
        }
    	return result;
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

    
    //Stores info about the mappings of fields in the Workbench Schema
    private class UploadInfo 
    {
    	protected final DBFieldInfo fieldInfo;
    	protected final int sequence;
		/**
		 * @param fieldInfo
		 * @param sequence
		 */
		public UploadInfo(DBFieldInfo fieldInfo, int sequence) 
		{
			super();
			this.fieldInfo = fieldInfo;
			this.sequence = sequence;
		}
		/**
		 * @return the fieldInfo
		 */
		public DBFieldInfo getFieldInfo() 
		{
			return fieldInfo;
		}
		/**
		 * @return the sequence
		 */
		public int getSequence() 
		{
			return sequence;
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
