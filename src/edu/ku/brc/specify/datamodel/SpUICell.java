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
/**
 * 
 */
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.ui.forms.persist.View.xmlAttr;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.ImageIcon;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Index;

import edu.ku.brc.af.prefs.AppPrefsCache;
import edu.ku.brc.ui.DateWrapper;
import edu.ku.brc.ui.forms.persist.AltViewIFace;
import edu.ku.brc.ui.forms.persist.FormCellCommandIFace;
import edu.ku.brc.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellLabelIFace;
import edu.ku.brc.ui.forms.persist.FormCellPanelIFace;
import edu.ku.brc.ui.forms.persist.FormCellSeparatorIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubViewIFace;
import edu.ku.brc.ui.forms.persist.FormRowIFace;
import edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Sep 25, 2007
 *
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert=true, dynamicUpdate=true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "spuicell")
@org.hibernate.annotations.Table(appliesTo="spuicell", indexes =
    {   @Index (name="SpUICellNameIDX", columnNames={"Name"}),
        @Index (name="SpUICellUIIdIDX", columnNames={"UiId"})
    })
public class SpUICell extends DataModelObjBase implements FormCellCommandIFace, 
                                                          FormCellFieldIFace, 
                                                          FormCellIFace, 
                                                          FormCellLabelIFace, 
                                                          FormCellPanelIFace,
                                                          FormCellSeparatorIFace,
                                                          FormCellSubViewIFace
{
    protected static DateWrapper scrDateFormat = null;
    
    public final static int VALTYPE_CHANGE = 0;
    public final static int VALTYPE_FOCUS  = 1;
    
    protected Integer spUICellId;
    
    // Every control has this
    protected String   initStr;           // memo
    
    // FormCell
    protected String    typeName;
    protected String    uiId;            // Unique id
    protected String    name;            // Logical name (not for display)
    protected Boolean   ignoreSetGetDB;
    protected Boolean   changeListenerOnlyDB;
    protected Boolean   isMultiFieldDB;
    protected Byte      colSpanDB;
    protected Byte      rowSpanDB;
    protected Short     xCoordDB;
    protected Short     yCoordDB;
    protected Short     heightDB;
    protected Short     widthDB;
    
    // FormCell Transient 
    protected String[]   fieldNames = null;

    // FormCellField
    protected String    uiTypeStr;
    protected String    dspUITypeStr;
    
    protected String    format;
    protected String    formatName;
    protected String    uiFieldFormatter;
    protected Boolean   isRequiredDB;
    protected Boolean   isReadOnlyDB;
    protected Boolean   isEncryptedDB;
    protected Boolean   isPasswordDB;
    protected Boolean   useThisDataDB; // this means the field uses the entire data object to do something special with
    protected String    label;
    protected String    defaultValue;
    protected Boolean   defaultDateTodayDB;
    protected String    pickListName; // Comboboxes and TextFields
    // Needed for Text Components
    protected Byte      txtColsDB;  // TextField and TextArea
    protected Byte      txtRowsDB;  // Text Area Only
    protected String    validationType;
    protected String    validationRule;
    protected Boolean   isTextFieldDB;
    protected Boolean   isDSPTextFieldDB;
    
    // FormCellSeparator
    //protected String  label;            // reusing the one from FormCellField
    protected String    collapseCompName;
    
    // FormCellCommand
    protected String    commandType;
    protected String    action;
    
    // FormCellLabel
    protected String    labelFor;
    protected String    iconName;
    protected Boolean   isRecordObjDB;
 
    // FormCellLabel Transient
    protected ImageIcon icon;
    
    // FormCellPanel
    protected String  colDef;
    protected String  rowDef;
    protected String  panelType;
    
    // FormCellPanel Transient
    protected List<FormRowIFace> rows;
    
    // FormCellSubView
    protected String  viewSetName;
    protected String  viewName;
    protected String  classDesc;
    protected Boolean singleValueFromSetDB;
    protected String  description;
    protected String  defaultAltViewType;
    protected String  funcModes;
    
    // For Table/Grid SubViews
    protected Byte    tableRowsDB;
    
    
    // Each FormCell's Owner    
    protected SpUIRow spRow;
    
    
    // Transient
    protected Properties properties = new Properties();

    /**
     * 
     */
    public SpUICell()
    {
        // no op
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#initialize()
     */
    @Override
    public void initialize()
    {
        super.init();
        
        spUICellId         = null;
        
        initStr            = null;
        
        // FormCell
        typeName           = null;
        uiId               = null;             // Unique id
        name               = null;             // Logical name (not for display)
        ignoreSetGetDB     = false;
        changeListenerOnlyDB = false;
        isMultiFieldDB     = false;
        colSpanDB          = 1;
        rowSpanDB          = 1;
        xCoordDB           = null;
        yCoordDB           = null;
        widthDB            = null;
        heightDB           = null;

        
        fieldNames         = null;
        
        // FormCellField
        uiTypeStr      = null;
        dspUITypeStr   = null;
        format         = null;
        formatName     = null;
        uiFieldFormatter = null;
        isRequiredDB   = null;
        isReadOnlyDB   = null;
        isEncryptedDB  = null;
        isPasswordDB   = null;
        useThisDataDB  = null; // this means the field uses the entire data object to do something special with
        label          = null;
        defaultValue   = null;
        defaultDateTodayDB = null;
        pickListName   = null; // Comboboxes and TextFields
        txtColsDB      = null;   // TextField and TextArea
        txtRowsDB      = null;    // Text Area Only
        validationType = null;
        validationRule = null;
        isTextFieldDB  = null;
        isDSPTextFieldDB = null;
        
        // FormCellSeparator
        collapseCompName = null;
        
        // FormCellCommand
        commandType = null;
        action      = null;        
        
        // FormCellLabel
        labelFor       = null; 
        iconName       = null; 
        isRecordObjDB    = null; 
        
        // FormCellLabelTransient
        icon           = null; 
        
        // FormCellPanel
        colDef         = null; 
        rowDef         = null; 
        panelType      = null; 
        
        // Transient
        rows = new Vector<FormRowIFace>(); 
        
        // FormCellSubView
        viewSetName        = null; 
        viewName           = null; 
        classDesc          = null; 
        singleValueFromSetDB = false; 
        description        = null; 
        defaultAltViewType = null; 
        funcModes          = null;
        tableRowsDB        = null;

    }

    /**
     * @return the spUICellId
     */
    @Id
    @GeneratedValue
    @Column(name = "SpUICellID", unique = false, nullable = false, insertable = true, updatable = true)
    public Integer getSpUICellId()
    {
        return spUICellId;
    }

    /**
     * @param spUICellId the spUICellId to set
     */
    public void setSpUICellId(Integer spUICellId)
    {
        this.spUICellId = spUICellId;
    }

    /**
     * @return the spRow
     */
    @ManyToOne(cascade = {}, fetch = FetchType.LAZY)
    @JoinColumn(name = "SpUIRowID", unique = false, nullable = false, insertable = true, updatable = true)
     public SpUIRow getSpRow()
    {
        return spRow;
    }

    /**
     * @param spRow the spRow to set
     */
    public void setSpRow(SpUIRow spRow)
    {
        this.spRow = spRow;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getModes(java.util.List)
     */
    @Transient
    public void fillWithFuncModes(List<Modes> list)
    {
        if (StringUtils.isNotEmpty(funcModes))
        {
            for (String tok : StringUtils.split(funcModes, ','))
            {
                list.add(Modes.valueOf(tok));
            }
        }
    }

    //-------------------------------------------------------------------
    //-- SpLocalizableIFace Interface
    //-------------------------------------------------------------------


    /**
     * @return the action
     */
    @Column(name = "Action", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getAction()
    {
        return action;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     * @return the changeListenerOnly
     */
    @Column(name = "ChangeListenerOnly", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getChangeListenerOnlyDB()
    {
        return changeListenerOnlyDB;
    }

    /**
     * @param changeListenerOnly the changeListenerOnly to set
     */
    public void setChangeListenerOnlyDB(Boolean changeListenerOnlyDB)
    {
        this.changeListenerOnlyDB = changeListenerOnlyDB;
    }

    /**
     * @return the classDesc
     */
    @Column(name = "ClassDesc", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getClassDesc()
    {
        return classDesc;
    }

    /**
     * @param classDesc the classDesc to set
     */
    public void setClassDesc(String classDesc)
    {
        this.classDesc = classDesc;
    }

    /**
     * @return the colDef
     */
    @Column(name = "ColDef", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getColDef()
    {
        return colDef;
    }

    /**
     * @param colDef the colDef to set
     */
    public void setColDef(String colDef)
    {
        this.colDef = colDef;
    }

    /**
     * @return the collapseCompName
     */
    @Column(name = "CollapseCompName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getCollapseCompName()
    {
        return collapseCompName;
    }

    /**
     * @param collapseCompName the collapseCompName to set
     */
    public void setCollapseCompName(String collapseCompName)
    {
        this.collapseCompName = collapseCompName;
    }

    /**
     * @return the colSpan
     */
    @Column(name = "ColCpan", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getColSpanDB()
    {
        return colSpanDB;
    }

    /**
     * @param colSpan the colSpan to set
     */
    public void setColSpanDB(Byte colSpan)
    {
        this.colSpanDB = colSpan;
    }

    /**
     * @return the commandType
     */
    @Column(name = "CommandType", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getCommandType()
    {
        return commandType;
    }

    /**
     * @param commandType the commandType to set
     */
    public void setCommandType(String commandType)
    {
        this.commandType = commandType;
    }

    /**
     * @return the defaultAltViewType
     */
    @Column(name = "DefaultAltViewType", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getDefaultAltViewType()
    {
        return defaultAltViewType;
    }

    /**
     * @param defaultAltViewType the defaultAltViewType to set
     */
    public void setDefaultAltViewType(String defaultAltViewType)
    {
        this.defaultAltViewType = defaultAltViewType;
    }

    /**
     * @return the funcModes
     */
    @Column(name = "FuncModes", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFuncModes()
    {
        return funcModes;
    }

    /**
     * @param funcModes the funcModes to set
     */
    public void setFuncModes(String funcModes)
    {
        this.funcModes = funcModes;
    }

    /**
     * @return the defaultDateTodayDB
     */
    @Column(name = "DefaultDateToday", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getDefaultDateTodayDB()
    {
        return defaultDateTodayDB;
    }

    /**
     * @param defaultDateTodayDB the defaultDateTodayDB to set
     */
    public void setDefaultDateTodayDB(Boolean defaultDateTodayDB)
    {
        this.defaultDateTodayDB = defaultDateTodayDB;
    }

    /**
     * @return the defaultValue
     */
    @Column(name = "DefaultValue", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getDefaultValue()
    {
        if (defaultValue != null)
        {
            if (defaultDateTodayDB == null && StringUtils.isNotEmpty(uiFieldFormatter))
            {
                defaultDateTodayDB = uiFieldFormatter.equals("Date") && defaultValue.equals("today");
            }
            
            if (defaultDateTodayDB != null && defaultDateTodayDB)
            {
                Date date = new Date();
                if (scrDateFormat == null)
                {
                    scrDateFormat = AppPrefsCache.getDateWrapper("ui", "formatting", "scrdateformat");
                }
                return scrDateFormat.format(date);
            }
        }
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the description
     */
    @Lob
    @Column(name = "Description", unique = false, nullable = true, insertable = true, updatable = true, length = 255)
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the dspUITypeStr
     */
    @Column(name = "DspUIType", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getDspUITypeStr()
    {
        return dspUITypeStr;
    }

    /**
     * @param dspUITypeStr the dspUITypeStr to set
     */
    public void setDspUITypeStr(String dspUITypeStr)
    {
        if (StringUtils.isNotEmpty(dspUITypeStr))
        {
            setDspUIType(FieldType.valueOf(dspUITypeStr));
        }
    }

    /**
     * @return the fieldNames
     */
    @Transient
    public String[] getFieldNames()
    {
        return fieldNames;
    }

    /**
     * @param fieldNames the fieldNames to set
     */
    public void setFieldNames(String[] fieldNames)
    {
        this.fieldNames = fieldNames;
    }

    /**
     * @return the format
     */
    @Column(name = "Format", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFormat()
    {
        return format;
    }

    /**
     * @param format the format to set
     */
    public void setFormat(String format)
    {
        this.format = format;
    }

    /**
     * @return the formatName
     */
    @Column(name = "FormatName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getFormatName()
    {
        return formatName;
    }

    /**
     * @param formatName the formatName to set
     */
    public void setFormatName(String formatName)
    {
        this.formatName = formatName;
    }

    /**
     * @return the icon
     */
    @Transient
    public ImageIcon getIcon()
    {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(ImageIcon icon)
    {
        this.icon = icon;
    }

    /**
     * @return the iconName
     */
    @Column(name = "IconName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getIconName()
    {
        return iconName;
    }

    /**
     * @param iconName the iconName to set
     */
    public void setIconName(String iconName)
    {
        this.iconName = iconName;
    }

    /**
     * @return the ignoreSetGet
     */
    @Column(name = "IgnoreSetGet", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIgnoreSetGetDB()
    {
        return ignoreSetGetDB;
    }

    /**
     * @param ignoreSetGet the ignoreSetGet to set
     */
    public void setIgnoreSetGetDB(Boolean ignoreSetGetDB)
    {
        this.ignoreSetGetDB = ignoreSetGetDB;
    }

    /**
     * @return the initStr
     */
    @Lob
    @Column(name = "InitStr", unique = false, nullable = true, insertable = true, updatable = true, length = 1024)
    public String getInitStr()
    {
        return initStr;
    }

    /**
     * @param initStr the initStr to set
     */
    public void setInitStr(String initStr)
    {
        this.initStr = initStr;
    }

    /**
     * @return the isDSPTextFieldDB
     */
    @Column(name = "IsDSPTextField", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsDSPTextFieldDB()
    {
        return isDSPTextFieldDB;
    }

    /**
     * @param isDSPTextFieldDB the isDSPTextFieldDB to set
     */
    public void setIsDSPTextFieldDB(Boolean isDSPTextFieldDB)
    {
        this.isDSPTextFieldDB = isDSPTextFieldDB;
    }

    /**
     * @return the isEncryptedDB
     */
    @Column(name = "IsEncrypted", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsEncryptedDB()
    {
        return isEncryptedDB;
    }

    /**
     * @param isEncryptedDB the isEncryptedDB to set
     */
    public void setIsEncryptedDB(Boolean isEncryptedDB)
    {
        this.isEncryptedDB = isEncryptedDB;
    }

    /**
     * @return the isPassword
     */
    @Column(name = "IsPassword", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsPasswordDB()
    {
        return isPasswordDB;
    }

    /**
     * @param isPassword the isPassword to set
     */
    public void setIsPasswordDB(Boolean isPasswordDB)
    {
        this.isPasswordDB = isPasswordDB;
    }

    /**
     * @return the isMultiField
     */
    @Column(name = "IsMultiField", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsMultiFieldDB()
    {
        return isMultiFieldDB;
    }

    /**
     * @param isMultiField the isMultiField to set
     */
    public void setIsMultiFieldDB(Boolean isMultiFieldDB)
    {
        this.isMultiFieldDB = isMultiFieldDB;
    }

    /**
     * @return the isReadOnlyDB
     */
    @Column(name = "IsReadOnly", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsReadOnlyDB()
    {
        return isReadOnlyDB;
    }

    /**
     * @param isReadOnlyDB the isReadOnlyDB to set
     */
    public void setIsReadOnlyDB(Boolean isReadOnlyDB)
    {
        this.isReadOnlyDB = isReadOnlyDB;
    }

    /**
     * @return the isRequiredDB
     */
    @Column(name = "IsRequired", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsRequiredDB()
    {
        return isRequiredDB;
    }

    /**
     * @param isRequiredDB the isRequiredDB to set
     */
    public void setIsRequiredDB(Boolean isRequiredDB)
    {
        this.isRequiredDB = isRequiredDB;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isTextField()
     * 
     */
    @Transient
    public boolean isTextField()
    {
        return isTextFieldDB == null ? false : isTextFieldDB;
    }

    /**
     * @return the isTextFieldDB
     */
    @Column(name = "IsTextField", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsTextFieldDB()
    {
        return isTextFieldDB;
    }

    /**
     * @param isTextFieldDB the isTextFieldDB to set
     */
    public void setIsTextFieldDB(Boolean isTextFieldDB)
    {
        this.isTextFieldDB = isTextFieldDB;
    }

    /**
     * @return the label
     */
    @Column(name = "Label", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getLabel()
    {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label)
    {
        this.label = label;
    }

    /**
     * @return the labelFor
     */
    @Column(name = "LabelFor", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getLabelFor()
    {
        return labelFor;
    }

    /**
     * @param labelFor the labelFor to set
     */
    public void setLabelFor(String labelFor)
    {
        this.labelFor = labelFor;
    }

    /**
     * @return the name
     */
    @Column(name = "Name", unique = false, nullable = false, insertable = true, updatable = true, length = 64)
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }

    /**
     * @return the panelType
     */
    @Column(name = "PanelType", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getPanelType()
    {
        return panelType;
    }

    /**
     * @param panelType the panelType to set
     */
    public void setPanelType(String panelType)
    {
        this.panelType = panelType;
    }

    /**
     * @return the pickListName
     */
    @Column(name = "PickListName", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getPickListName()
    {
        return pickListName;
    }

    /**
     * @param pickListName the pickListName to set
     */
    public void setPickListName(String pickListName)
    {
        this.pickListName = pickListName;
    }

    /**
     * @return the recordObjDB
     */
    @Column(name = "IsRecordObj", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getIsRecordObjDB()
    {
        return isRecordObjDB;
    }

    /**
     * @param isRecordObjDB the recordObjDB to set
     */
    public void setIsRecordObjDB(Boolean isRecordObjDB)
    {
        this.isRecordObjDB = isRecordObjDB;
    }

    /**
     * @return the rowDef
     */
    @Column(name = "RowDef", unique = false, nullable = true, insertable = true, updatable = true, length = 64)
    public String getRowDef()
    {
        return rowDef;
    }

    /**
     * @param rowDef the rowDef to set
     */
    public void setRowDef(String rowDef)
    {
        this.rowDef = rowDef;
    }

    /**
     * @return the rows
     */
    @Transient
    public List<FormRowIFace> getRows()
    {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(List<FormRowIFace> rows)
    {
        this.rows = rows;
    }

    /**
     * @return the rowSpan
     */
    @Column(name = "RowSpan", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getRowSpanDB()
    {
        return rowSpanDB;
    }

    /**
     * @param rowSpan the rowSpan to set
     */
    public void setRowSpanDB(Byte rowSpanDB)
    {
        this.rowSpanDB = rowSpanDB;
    }

    /**
     * @return the singleValueFromSetDB
     */
    @Column(name = "SingleValueFromSet", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getSingleValueFromSetDB()
    {
        return singleValueFromSetDB;
    }

    /**
     * @param singleValueFromSetDB the singleValueFromSetDB to set
     */
    public void setSingleValueFromSetDB(Boolean singleValueFromSetDB)
    {
        this.singleValueFromSetDB = singleValueFromSetDB;
    }

    /**
     * @return the tableRowsDB
     */
    @Column(name = "TableRows", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getTableRowsDB()
    {
        return tableRowsDB;
    }

    /**
     * @param tableRowsDB the tableRowsDB to set
     */
    public void setTableRowsDB(Byte tableRowsDB)
    {
        this.tableRowsDB = tableRowsDB;
    }

    /**
     * @return the txtColsDB
     */
    @Column(name = "TxtCols", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getTxtColsDB()
    {
        return txtColsDB;
    }

    /**
     * @param txtColsDB the txtColsDB to set
     */
    public void setTxtColsDB(Byte txtColsDB)
    {
        this.txtColsDB = txtColsDB;
    }

    /**
     * @return the txtRowsDB
     */
    @Column(name = "TxtRows", unique = false, nullable = true, insertable = true, updatable = true)
    public Byte getTxtRowsDB()
    {
        return txtRowsDB;
    }

    /**
     * @param txtRowsDB the txtRowsDB to set
     */
    public void setTxtRowsDB(Byte txtRowsDB)
    {
        this.txtRowsDB = txtRowsDB;
    }

    /**
     * @return the typeName
     */
    @Column(name = "TypeName", unique = false, nullable = false, insertable = true, updatable = true, length = 16)
    public String getTypeName()
    {
        return typeName;
    }

    /**
     * @param typeName the typeName to set
     */
    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }

    /**
     * @return the uiFieldFormatter
     */
    @Column(name = "UiFieldFormatter", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getUiFieldFormatter()
    {
        return uiFieldFormatter;
    }

    /**
     * @param uiFieldFormatter the uiFieldFormatter to set
     */
    public void setUiFieldFormatter(String uiFieldFormatter)
    {
        this.uiFieldFormatter = uiFieldFormatter;
    }

    /**
     * @return the uiId
     */
    @Column(name = "UiId", unique = false, nullable = true, insertable = true, updatable = true, length = 32)
    public String getUiId()
    {
        return uiId;
    }

    /**
     * @param uiId the uiId to set
     */
    public void setUiId(String uiId)
    {
        this.uiId = uiId;
    }

    /**
     * @return the uiTypeStr
     */
    @Column(name = "UiTypeStr", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getUiTypeStr()
    {
        return uiTypeStr;
    }

    /**
     * @param uiTypeStr the uiTypeStr to set
     */
    public void setUiTypeStr(String uiTypeStr)
    {
        this.uiTypeStr = uiTypeStr;
    }

    /**
     * @return the useThisDataDB
     */
    @Column(name = "UseThisData", unique = false, nullable = true, insertable = true, updatable = true)
    public Boolean getUseThisDataDB()
    {
        return useThisDataDB;
    }

    /**
     * @param useThisDataDB the useThisDataDB to set
     */
    public void setUseThisDataDB(Boolean useThisDataDB)
    {
        this.useThisDataDB = useThisDataDB;
    }

    /**
     * @return the validationRule
     */
    @Column(name = "ValidationRule", unique = false, nullable = true, insertable = true, updatable = true, length = 128)
    public String getValidationRule()
    {
        return validationRule;
    }

    /**
     * @param validationRule the validationRule to set
     */
    public void setValidationRule(String validationRule)
    {
        this.validationRule = validationRule;
    }

    /**
     * @return the validationType
     */
    @Column(name = "ValidationType", unique = false, nullable = true, insertable = true, updatable = true, length = 16)
    public String getValidationType()
    {
        return validationType;
    }

    /**
     * @param validationType the validationType to set
     */
    public void setValidationType(String validationType)
    {
        this.validationType = validationType;
    }

    /**
     * @return the viewName
     */
    //@Column(name = "ViewName", unique = false, nullable = false, insertable = true, updatable = true, length = 32)
    @Transient
    public String getViewName()
    {
        return viewName;
    }

    /**
     * @param viewName the viewName to set
     */
    public void setViewName(String viewName)
    {
        this.viewName = viewName;
    }

    /**
     * @return the viewSetName
     */
    @Transient
    public String getViewSetName()
    {
        return viewSetName;
    }

    /**
     * @param viewSetName the viewSetName to set
     */
    public void setViewSetName(String viewSetName)
    {
        this.viewSetName = viewSetName;
    }

    /**
     * @param properties the properties to set
     */
    public void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    /**
     * @return the xCoordDB
     */
    @Column(name = "XCoord", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getXCoordDB()
    {
        return xCoordDB;
    }

    /**
     * @param coordDB the xCoordDB to set
     */
    public void setXCoordDB(Short coordDB)
    {
        xCoordDB = coordDB;
    }

    /**
     * @return the yCoordDB
     */
    @Column(name = "YCoord", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getYCoordDB()
    {
        return yCoordDB;
    }

    /**
     * @param coordDB the yCoordDB to set
     */
    public void setYCoordDB(Short coordDB)
    {
        yCoordDB = coordDB;
    }
    
    /**
     * @return the height
     */
    @Column(name = "Height", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getHeightDB()
    {
        return heightDB;
    }

    /**
     * @param height the height to set
     */
    public void setHeightDB(Short height)
    {
        this.heightDB = height;
    }

    /**
     * @return the width
     */
    @Column(name = "Width", unique = false, nullable = true, insertable = true, updatable = true)
    public Short getWidthDB()
    {
        return widthDB;
    }

    /**
     * @param width the width to set
     */
    public void setWidthDB(Short width)
    {
        this.widthDB = width;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getDataClass()
     */
    @Override
    @Transient
    public Class<?> getDataClass()
    {
        return SpUICell.class;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#getId()
     */
    //@Override
    @Transient
    public Integer getId()
    {
        return spUICellId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Transient
    public int getTableId()
    {
        return getClassTableId();
    }
    
    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId()
    {
        return 509;
    }

    
    //------------------------------------------------------------------------------
    // Helpers
    //------------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#addProperty(java.lang.String, java.lang.String)
     */
    public void addProperty(String nameStr, String value)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.put(nameStr, value);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#compareTo(edu.ku.brc.ui.forms.persist.FormCellIFace)
     */
    public int compareTo(FormCellIFace obj)
    {
        return name.compareTo(obj.getName());
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getProperties()
     */
    @Transient
    public Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return properties;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getPropertyAsBoolean(java.lang.String, boolean)
     */
    @Transient
    public boolean getPropertyAsBoolean(String nameStr, boolean defVal)
    {
        if (properties != null)
        {
            String str = properties.getProperty(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return str.equalsIgnoreCase("true");
            }
        }
        return defVal;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getProperty(java.lang.String)
     */
    @Transient
    public String getProperty(final String nameStr)
    {
        if (properties != null)
        {
            return properties.getProperty(nameStr);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getPropertyAsInt(java.lang.String, int)
     */
    @Transient
    public int getPropertyAsInt(final String nameStr, final int defVal)
    {
        if (properties != null)
        {
            String str = properties.getProperty(nameStr);
            if (StringUtils.isNotEmpty(str))
            {
                return Integer.parseInt(str);
            }
        }
        return defVal;
    }
    

    //------------------------------------------------------------------------------
    // Interfaces
    //------------------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#isRecordObj()
     */
    @Transient
    public boolean isRecordObj()
    {
        return isRecordObjDB == null ? false : isRecordObjDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellLabelIFace#setRecordObj(boolean)
     */
    public void setRecordObj(boolean recordObj)
    {
        this.isRecordObjDB = recordObj;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#getTableRows()
     */
    @Transient
    public int getTableRows()
    {
        return tableRowsDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#isSingleValueFromSet()
     */
    @Transient
    public boolean isSingleValueFromSet()
    {
        return this.singleValueFromSetDB == null ? false : singleValueFromSetDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setTableRows(int)
     */
    public void setTableRows(int tableRows)
    {
        tableRowsDB = (byte)tableRows;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellSubViewIFace#setView(java.lang.String)
     */
    public void setView(String viewName)
    {
        this.viewName = viewName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getColspan()
     */
    @Transient
    public int getColspan()
    {
        return colSpanDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getRowspan()
     */
    @Transient
    public int getRowspan()
    {
        return rowSpanDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getType()
     */
    @Transient
    public CellType getType()
    {
        return CellType.valueOf(uiTypeStr);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isChangeListenerOnly()
     */
    @Transient
    public boolean isChangeListenerOnly()
    {
        return changeListenerOnlyDB == null ? false : changeListenerOnlyDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isIgnoreSetGet()
     */
    @Transient
    public boolean isIgnoreSetGet()
    {
        return ignoreSetGetDB == null ? false : ignoreSetGetDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#isMultiField()
     */
    @Transient
    public boolean isMultiField()
    {
        return isMultiFieldDB == null ? false : isMultiFieldDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setChangeListenerOnly(boolean)
     */
    public void setChangeListenerOnly(boolean changeListenerOnly)
    {
        changeListenerOnlyDB = changeListenerOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setColspan(int)
     */
    public void setColspan(int colspan)
    {
        this.colSpanDB = (byte)colspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setId(java.lang.String)
     */
    public void setIdent(String id)
    {
        uiId = id;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setIgnoreSetGet(boolean)
     */
    public void setIgnoreSetGet(boolean ignoreSetGet)
    {
        this.ignoreSetGetDB = ignoreSetGet;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setMultiField(boolean)
     */
    public void setMultiField(boolean isMultiField)
    {
        this.isMultiFieldDB = isMultiField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setRowspan(int)
     */
    public void setRowspan(int rowspan)
    {
        this.rowSpanDB = (byte)rowspan;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setType(edu.ku.brc.ui.forms.persist.FormCellIFace.CellType)
     */
    public void setType(CellType type)
    {
        uiTypeStr = type.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getId()
     */
    @Transient
    public String getIdent()
    {
        return uiId;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getDspUIType()
     */
    @Transient
    public FieldType getDspUIType()
    {
        return FieldType.valueOf(dspUITypeStr);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getTxtCols()
     */
    @Transient
    public int getTxtCols()
    {
        return txtColsDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getTxtRows()
     */
    @Transient
    public int getTxtRows()
    {
        return txtRowsDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getUIFieldFormatter()
     */
    @Transient
    public String getUIFieldFormatter()
    {
        return uiFieldFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#getUiType()
     */
    @Transient
    public FieldType getUiType()
    {
        return FieldType.valueOf(uiTypeStr);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isEncrypted()
     */
    @Transient
    public boolean isEncrypted()
    {
        return this.isEncryptedDB == null ? false : isEncryptedDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isReadOnly()
     */
    @Transient
    public boolean isReadOnly()
    {
        return isReadOnlyDB == null ? false : isReadOnlyDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isRequired()
     */
    @Transient
    public boolean isRequired()
    {
        return isRequiredDB == null ? false : isRequiredDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isTextField(edu.ku.brc.ui.forms.persist.AltViewIFace.CreationMode)
     */
    @Transient
    public boolean isTextFieldForMode(CreationMode mode)
    {
        boolean isDSPTextField = isDSPTextFieldDB == null ? false : isDSPTextFieldDB;
        boolean isTextField    = isTextFieldDB == null ? false : isTextFieldDB;
        // A mode of "None" default to "Edit"
        return mode == AltViewIFace.CreationMode.VIEW ? isDSPTextField : isTextField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setDspUIType(edu.ku.brc.ui.forms.persist.FormCellFieldIFace.FieldType)
     */
    public void setDspUIType(FieldType dspUIType)
    {
        if (dspUIType != null)
        {
            dspUITypeStr = dspUIType.toString();
            this.isDSPTextFieldDB = dspUIType == FieldType.dsptextfield || dspUIType == FieldType.dsptextarea;
        } else
        {
            dspUITypeStr = null;
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setEncrypted(boolean)
     */
    public void setEncrypted(boolean isEncrypted)
    {
        isEncryptedDB = isEncrypted;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setReadOnly(boolean)
     */
    public void setReadOnly(boolean isReadOnly)
    {
        isReadOnlyDB = isReadOnly;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setRequired(boolean)
     */
    public void setRequired(boolean isRequired)
    {
        isRequiredDB = isRequired;
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setTextField(boolean)
     */
    public void setTextField(boolean isTextField)
    {
        isTextFieldDB = isTextField;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setTxtCols(int)
     */
    public void setTxtCols(int cols)
    {
        txtColsDB = (byte)cols;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setTxtRows(int)
     */
    public void setTxtRows(int rows)
    {
        txtRowsDB = (byte)rows;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setUIFieldFormatter(java.lang.String)
     */
    public void setUIFieldFormatter(String uiFieldFormatter)
    {
        this.uiFieldFormatter = uiFieldFormatter;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#setUiType(edu.ku.brc.ui.forms.persist.FormCellFieldIFace.FieldType)
     */
    public void setUiType(FieldType uiType)
    {
        this.isTextFieldDB = uiType == FieldType.text ||
                             uiType == FieldType.formattedtext ||
                             uiType == FieldType.textarea;
        
        this.uiTypeStr = uiType.toString();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#useThisData()
     */
    @Transient
    public boolean useThisData()
    {
        return useThisDataDB == null ? false : useThisDataDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getXCoord()
     */
    @Transient
    public int getXCoord()
    {
        return xCoordDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getYCoord()
     */
    @Transient
    public int getYCoord()
    {
        return yCoordDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setXCoord(int)
     */
    public void setXCoord(int xCoord)
    {
        xCoordDB = (short)xCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setYCoord(int)
     */
    public void setYCoord(int yCoord)
    {
        yCoordDB = (short)yCoord;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getHeight()
     */
    @Transient
    public int getHeight()
    {
        return heightDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#getWidth()
     */
    @Transient
    public int getWidth()
    {
        return widthDB;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setHeight(int)
     */
    public void setHeight(int height)
    {
        heightDB = (short)height;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#setWidth(int)
     */
    public void setWidth(int width)
    {
        widthDB = (short)width;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellFieldIFace#isPassword()
     */
    @Transient
    public boolean isPassword()
    {
        return isPasswordDB == null ? false : isPasswordDB;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Transient
    @Override
    public boolean isChangeNotifier()
    {
        return false;
    }

    /**
     * Recreates the initialize string from the properties and clones the properties hashtable.
     * @param cell the source of the props
     * @return the string
     */
    protected String getInitStrFromProps(final FormCellIFace cell)
    {
        Properties props = cell.getProperties();
        if (props != null)
        {
            StringBuilder sb = new StringBuilder();
            for (Object keyObj : props.keySet())
            {
                String key = (String)keyObj;
                if (sb.length() > 0) sb.append(';');
                sb.append(key);
                sb.append("=");
                sb.append(properties.getProperty(key));
            }
            properties = (Properties)cell.getProperties().clone();
            return sb.toString();
        }
        return null;
    }
    
    /**
     * Copies a FormCell object into the this object.
     * @param cell the source
     */
    public void copyInto(final FormCellIFace cell)
    {
        initStr              = getInitStrFromProps(cell);
        typeName             = cell.getType().toString().toLowerCase();
        uiId                 = cell.getIdent();
        name                 = cell.getName();
        ignoreSetGetDB       = cell.isIgnoreSetGet();
        changeListenerOnlyDB = cell.isChangeListenerOnly();
        isMultiFieldDB       = cell.isMultiField();
        colSpanDB            = (byte)cell.getColspan();
        rowSpanDB            = (byte)cell.getRowspan();
        xCoordDB             = (short)cell.getXCoord();
        yCoordDB             = (short)cell.getYCoord();
        widthDB              = (short)cell.getWidth();
        heightDB             = (short)cell.getHeight();
        fieldNames           = fieldNames != null ? fieldNames.clone() : null;
        
        if (cell instanceof FormCellSeparatorIFace)
        {
            FormCellSeparatorIFace fcs = (FormCellSeparatorIFace)cell;
            label = fcs.getLabel();
            collapseCompName = fcs.getCollapseCompName();
        }
        
        if (cell instanceof FormCellLabelIFace)
        {
            FormCellLabelIFace fcl = (FormCellLabelIFace)cell;
            labelFor      = fcl.getLabelFor();
            icon          = fcl.getIcon();
            iconName      = fcl.getIconName();
            isRecordObjDB = fcl.isRecordObj();
        }
        
        if (cell instanceof FormCellCommandIFace)
        {
            FormCellCommandIFace fcc = (FormCellCommandIFace)cell;
            commandType = fcc.getCommandType();
            action      = fcc.getAction();
        }
        
        if (cell instanceof FormCellFieldIFace)
        {
            FormCellFieldIFace fcf = (FormCellFieldIFace)cell;
            uiTypeStr        = fcf.getUiType().toString().toLowerCase();
            dspUITypeStr     = fcf.getDspUIType().toString().toLowerCase();
            format           = fcf.getFormat();
            formatName       = fcf.getFormatName();
            uiFieldFormatter = fcf.getUIFieldFormatter();
            isRequiredDB     = fcf.isRequired();
            isReadOnlyDB     = fcf.isReadOnly();
            isEncryptedDB    = fcf.isEncrypted();
            isPasswordDB     = fcf.isPassword();
            useThisDataDB    = fcf.useThisData();
            label            = fcf.getLabel();
            defaultValue     = fcf.getDefaultValue();
            pickListName     = fcf.getPickListName();
            txtRowsDB        = (byte)fcf.getTxtRows();
            txtColsDB        = (byte)fcf.getTxtCols();
            validationRule   = fcf.getValidationRule();
            validationType   = fcf.getValidationType();
            isTextFieldDB    = fcf.isTextField();
            
            setUiType(fcf.getUiType());
            setDspUIType(fcf.getDspUIType());
        }
        
        if (cell instanceof FormCellPanelIFace)
        {
            panelType = ((FormCellPanelIFace)cell).getPanelType();
            rowDef    = ((FormCellPanelIFace)cell).getRowDef();
            colDef    = ((FormCellPanelIFace)cell).getColDef();
        }
        
        if (cell instanceof FormCellSubViewIFace)
        {
            FormCellSubViewIFace fcs = (FormCellSubViewIFace)cell;
            viewSetName        = fcs.getViewSetName();
            viewName           = fcs.getViewName();
            classDesc          = fcs.getClassDesc();
            singleValueFromSetDB = fcs.isSingleValueFromSet();
            description        = fcs.getDescription();
            defaultAltViewType = fcs.getDefaultAltViewType();
            funcModes          = fcs.getFuncModes();
            tableRowsDB        = (byte)fcs.getTableRows();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.FormCellIFace#toXML(java.lang.StringBuffer)
     */
    public void toXML(StringBuffer sb)
    {
        sb.append("    <cell");
        xmlAttr(sb, "type", typeName.toString().toLowerCase());
        xmlAttr(sb, "id", uiId);
        xmlAttr(sb, "name", name);
        
        // Label
        //<cell type="label" label="KU Accessions" icon="Accession" recordobj="true" colspan="12"/>
        //<cell type="label" labelfor="1" label="Number"/>
        if (typeName.equals("label"))
        {
            xmlAttr(sb, "label",     label);
            xmlAttr(sb, "icon",      iconName);
            xmlAttr(sb, "recordobj", isRecordObjDB);
            xmlAttr(sb, "labelfor",  labelFor);
            
        } else if (typeName.equals("separator"))
        {
            xmlAttr(sb, "label",     label);
            xmlAttr(sb, "collapse",  collapseCompName);
            
            
        } else if (typeName.equals("field"))
        {
            xmlAttr(sb, "uitype", uiTypeStr);
            xmlAttr(sb, "dsptype", dspUITypeStr);
            
            if (uiTypeStr.equals("text"))
            {
                // <cell type="field" id="10" name="lastEditedBy" uitype="text" readonly="true"/>
                xmlAttr(sb, "cols", txtColsDB);
                xmlAttr(sb, "isencrypted", isEncryptedDB);
                xmlAttr(sb, "ispassword", isPasswordDB);
                
                
            } else if (uiTypeStr.equals("textarea"))
            {
                //<cell type="field" id="3" name="remarks" uitype="textarea" colspan="5" cols="40" valtype="Changed"/>
                xmlAttr(sb, "rows", txtRowsDB);
                xmlAttr(sb, "cols", txtColsDB);
                
            } else if (uiTypeStr.equals("combobox"))
            {
                //<cell type="field" id="2" name="status"  uitype="combobox" picklist="AccessionStatus" isrequired="true" valtype="Changed"/>
                xmlAttr(sb, "picklist", pickListName);
            }
            
            xmlAttr(sb, "format", format);
            xmlAttr(sb, "formatname", formatName);
            xmlAttr(sb, "uifieldformatter", uiFieldFormatter);
            xmlAttr(sb, "isrequired", isRequiredDB);
            xmlAttr(sb, "valtype", validationType);
            xmlAttr(sb, "readonly", isReadOnlyDB);
            xmlAttr(sb, "ispassword", isPasswordDB);
            xmlAttr(sb, "changesonly", changeListenerOnlyDB);
            xmlAttr(sb, "validation", validationRule);
            
        } else if (typeName.equals("subview"))
        {
            xmlAttr(sb, "commandtype", commandType);
            xmlAttr(sb, "action", action);
            
        } else if (typeName.equals("subview"))
        {
            //<cell type="subview" viewname="AccessionAgent" id="8" name="accessionAgents" desc="Agents" colspan="12"/>
            xmlAttr(sb, "viewname", viewName);
            xmlAttr(sb, "desc", description);
            xmlAttr(sb, "funcmode", getFuncModes());
            xmlAttr(sb, "defaulttype", defaultAltViewType);
            xmlAttr(sb, "rows", tableRowsDB);
            xmlAttr(sb, "single", singleValueFromSetDB);
            
            
        } else if (typeName.equals("panel"))
        {
            //<cell type="panel" id="outerPanel" name="outerPanel" coldef="16px,1px,f:p:g" rowdef="p" colspan="12">
            xmlAttr(sb, "coldef", colDef);
            xmlAttr(sb, "rowdef", rowDef);
            xmlAttr(sb, "paneltype", panelType);
            throw new RuntimeException("Panel not supported!");
        }

        xmlAttr(sb, "colspan", colSpanDB);
        xmlAttr(sb, "rowspan", rowSpanDB);
        xmlAttr(sb, "x",       xCoordDB);
        xmlAttr(sb, "y",       yCoordDB);
        xmlAttr(sb, "width",   widthDB);
        xmlAttr(sb, "height",  heightDB);
        xmlAttr(sb, "initialize",  initStr);
        
        sb.append("/>");
        
    }
    
    
}
