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
package edu.ku.brc.specify.datamodel;

import static edu.ku.brc.helpers.XMLHelper.addAttr;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.dom4j.Element;

import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tasks.WorkbenchTask;
import edu.ku.brc.specify.tasks.subpane.wb.GridTableHeader;

/**
 * Items are sorted by ViewOrder
 */
@Entity
@org.hibernate.annotations.Entity(dynamicInsert = true, dynamicUpdate = true)
@org.hibernate.annotations.Proxy(lazy = false)
@Table(name = "workbenchtemplatemappingitem")
@SuppressWarnings("serial")
public class WorkbenchTemplateMappingItem extends DataModelObjBase implements java.io.Serializable, GridTableHeader {
    private static final Logger log = Logger.getLogger(WorkbenchTemplateMappingItem.class);
    public final static short UNKNOWN = 0;
    public final static short TEXTFIELD = 1;
    public final static short TEXTAREA = 2;
    public final static short CHECKBOX = 3;  // Boolean
    public final static short TEXTFIELD_DATE = 4;
    public final static short COMBOBOX = 5;


    // Fields

    protected Integer workbenchTemplateMappingItemId;
    protected String tableName;
    protected Integer srcTableId;
    protected String fieldName;
    protected String importedColName;
    protected String caption;
    protected Short viewOrder;              // The Current View Order
    protected Short origImportColumnIndex;  // The index from the imported data file
    protected Short dataFieldLength;        // the length of the data from the specify Schema, usually for strings.
    protected Short fieldType;              // the type of field
    protected WorkbenchTemplate workbenchTemplate;
    protected Boolean isExportableToContent;
    protected Boolean isIncludedInTitle;
    protected Boolean isRequired;
    protected Set<WorkbenchDataItem> workbenchDataItems;

    //for updates
    /**
     * User configurable. False if, for an exported dataset, the data in column cannot be edited.
     */
    protected Boolean isEditable;

    // UI Layout extras
    protected String metaData;
    protected Short xCoord;
    protected Short yCoord;
    protected Boolean carryForward;
    //protected boolean           useCaptionForText = false; //if true then toString will use the caption instead of the mapped field's title

    // Transient
    protected Class<?> dataFieldClass = null;
    protected DBFieldInfo fieldInfo = null;

    // Constructors

    /**
     * default constructor
     */
    public WorkbenchTemplateMappingItem() {
        //
    }

    /**
     * constructor with id
     */
    public WorkbenchTemplateMappingItem(Integer workbenchTemplateMappingItemId) {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }

    // Initializer
    @Override
    public void initialize() {
        super.init();

        workbenchTemplateMappingItemId = null;
        tableName = null;
        srcTableId = null;
        fieldName = null;
        importedColName = null;
        caption = null;
        viewOrder = null;
        origImportColumnIndex = null;
        dataFieldLength = -1;
        fieldType = null;
        workbenchTemplate = null;
        metaData = null;
        xCoord = -1;
        yCoord = -1;
        carryForward = false;
        isExportableToContent = true;
        isIncludedInTitle = false;
        isRequired = false;
        isEditable = false;

        workbenchDataItems = new HashSet<WorkbenchDataItem>();

        // Transient
        dataFieldClass = null;
        fieldInfo = null;

    }

    // End Initializer

    // Property accessors

    /**
     *
     */
    @Id
    @GeneratedValue
    @Column(name = "WorkbenchTemplateMappingItemID")
    public Integer getWorkbenchTemplateMappingItemId() {
        return this.workbenchTemplateMappingItemId;
    }

    /**
     * Generic Getter for the ID Property.
     *
     * @returns ID Property.
     */
    @Transient
    @Override
    public Integer getId() {
        return this.workbenchTemplateMappingItemId;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getDataClass()
     */
    @Transient
    @Override
    public Class<?> getDataClass() {
        return WorkbenchTemplateMappingItem.class;
    }

    public void setWorkbenchTemplateMappingItemId(Integer workbenchTemplateMappingItemId) {
        this.workbenchTemplateMappingItemId = workbenchTemplateMappingItemId;
    }

    /**
     *
     */
    @Column(name = "TableName", length = 64)
    public String getTableName() {
        return this.tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     *
     */
    @Column(name = "TableId", length = 64)
    public Integer getSrcTableId() {
        return this.srcTableId;
    }

    public void setSrcTableId(Integer srcTableId) {
        this.srcTableId = srcTableId;
    }

    /**
     *
     */
    @Column(name = "FieldName")
    public String getFieldName() {
        return this.fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    private static int importedColNameMaxLength = 64;

    /**
     * @return the importedColName
     */
    @Column(name = "ImportedColName")
    public String getImportedColName() {
        return importedColName;
    }

    /**
     * @param importedColName the importedColName to set
     */
    public void setImportedColName(String importedColName) {
        this.importedColName = importedColName;
    }

    /**
     *
     */
    @Column(name = "Caption", length = 64)
    public String getCaption() {
        return this.caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     *
     */
    @Column(name = "ViewOrder")
    public Short getViewOrder() {
        return this.viewOrder;
    }

    public void setViewOrder(Short viewOrder) {
        this.viewOrder = viewOrder;
    }

    /**
     *
     */
    @Column(name = "DataColumnIndex")
    public Short getOrigImportColumnIndex() {
        return this.origImportColumnIndex;
    }

    public void setOrigImportColumnIndex(Short dataColumnIndex) {
        this.origImportColumnIndex = dataColumnIndex;
    }

    /**
     * @return the dataFieldLength
     */
    @Column(name = "DataFieldLength")
    public Short getDataFieldLength() {
        return dataFieldLength;
    }

    /**
     * @param dataFieldLength the dataFieldLength to set
     */
    public void setDataFieldLength(Short dataLength) {
        this.dataFieldLength = dataLength;
    }

    /**
     * @return the fieldType
     */
    @Column(name = "FieldType")
    public Short getFieldType() {
        return fieldType;
    }

    /**
     * @param fieldType the dataFieldLength to set
     */
    public void setFieldType(Short fieldType) {
        this.fieldType = fieldType;
    }

    @Column(name = "MetaData", length = 128)
    public String getMetaData() {
        return metaData;
    }

    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    @Column(name = "XCoord")
    public Short getXCoord() {
        return xCoord;
    }

    public void setXCoord(Short coord) {
        xCoord = coord;
    }

    @Column(name = "YCoord")
    public Short getYCoord() {
        return yCoord;
    }

    public void setYCoord(Short coord) {
        yCoord = coord;
    }

    @Column(name = "CarryForward")
    public Boolean getCarryForward() {
        return carryForward;
    }

    public void setCarryForward(Boolean carryForward) {
        this.carryForward = carryForward;
    }

    @Column(name = "IsExportableToContent")
    public Boolean getIsExportableToContent() {
        return isExportableToContent;
    }

    public void setIsExportableToContent(Boolean isExportableToContent) {
        this.isExportableToContent = isExportableToContent;
    }


    /**
     * @return the isEditable
     */
    @Column(name = "IsEditable")
    public Boolean getIsEditable() {
        return isEditable;
    }

    /**
     * @param isEditable the isEditable to set
     */
    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }

    @Column(name = "IsIncludedInTitle")
    public Boolean getIsIncludedInTitle() {
        return isIncludedInTitle;
    }

    public void setIsIncludedInTitle(Boolean isIncludedInTitle) {
        this.isIncludedInTitle = isIncludedInTitle;
    }

    @Column(name = "IsRequired")
    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    /**
     *
     */
    @ManyToOne(cascade = {}, fetch = FetchType.EAGER)
    @JoinColumn(name = "WorkbenchTemplateID", nullable = false)
    public WorkbenchTemplate getWorkbenchTemplate() {
        return this.workbenchTemplate;
    }

    public void setWorkbenchTemplate(WorkbenchTemplate workbenchTemplate) {
        this.workbenchTemplate = workbenchTemplate;
    }


    @OneToMany(mappedBy = "workbenchTemplateMappingItem")
    public Set<WorkbenchDataItem> getWorkbenchDataItems() {
        return this.workbenchDataItems;
    }

    public void setWorkbenchDataItems(Set<WorkbenchDataItem> workbenchDataItems) {
        this.workbenchDataItems = workbenchDataItems;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(GridTableHeader obj) {
        return viewOrder.compareTo(obj.getViewOrder());
    }


    /**
     * @return the fieldInfo
     */
    @Transient
    public DBFieldInfo getFieldInfo() {
        return fieldInfo;
    }

    /**
     * @param fieldInfo the fieldInfo to set
     */
    public void setFieldInfo(DBFieldInfo fieldInfo) {
        this.fieldInfo = fieldInfo;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
//        if (useCaptionForText)
//        {
//        	return caption != null ? caption : fieldInfo != null ? fieldInfo.getTitle() : fieldName;
//        }
        return fieldInfo != null ? fieldInfo.getTitle() : (caption != null ? caption : fieldName);
    }

//    /**
//	 * @param useCaptionForText the useCaptionForText to set
//	 */
//	public void setUseCaptionForText(boolean useCaptionForText)
//	{
//		this.useCaptionForText = useCaptionForText;
//	}

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#isChangeNotifier()
     */
    @Override
    @Transient
    public boolean isChangeNotifier() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.ku.brc.ui.forms.FormDataObjIFace#getTableId()
     */
    @Override
    @Transient
    public int getTableId() {
        return getClassTableId();
    }

    /**
     * @return the localized title
     */
    @Transient
    public String getTitle() {
        return fieldInfo != null ? fieldInfo.getTitle() : getCaption();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        WorkbenchTemplateMappingItem wbtmi = (WorkbenchTemplateMappingItem) super.clone();
        wbtmi.workbenchTemplateMappingItemId = null;
        wbtmi.workbenchTemplate = null;
        wbtmi.workbenchDataItems = new HashSet<WorkbenchDataItem>();

        wbtmi.timestampCreated = new Timestamp(System.currentTimeMillis());
        wbtmi.timestampModified = null;
        wbtmi.modifiedByAgent = null;

        return wbtmi;
    }

    /**
     * @param sb Constructs an XML description of the object
     */
    public void toXML(final StringBuilder sb) {
        sb.append("<workbenchtemplatemappingitem ");
        addAttr(sb, "tableName", tableName);
        addAttr(sb, "srcTableId", srcTableId);
        addAttr(sb, "fieldName", fieldName);
        addAttr(sb, "importedColName", importedColName);
        addAttr(sb, "caption", caption);
        addAttr(sb, "viewOrder", viewOrder);
        addAttr(sb, "origImportColumnIndex", origImportColumnIndex);
        addAttr(sb, "dataFieldLength", dataFieldLength);
        addAttr(sb, "fieldType", fieldType);
        addAttr(sb, "metaData", metaData);
        addAttr(sb, "xCoord", xCoord);
        addAttr(sb, "yCoord", yCoord);
        addAttr(sb, "carryForward", carryForward);
        addAttr(sb, "isExportableToContent", isExportableToContent);
        addAttr(sb, "isIncludedInTitle", isIncludedInTitle);
        addAttr(sb, "isRequired", isRequired);
        sb.append(" />");
    }

    /**
     * @param element reads attributes from element.
     */
    public void fromXML(final Element element) {
        tableName = XMLHelper.getAttr(element, "tableName", null);
        srcTableId = XMLHelper.getAttr(element, "srcTableId", -1);
        fieldName = XMLHelper.getAttr(element, "fieldName", null);
        importedColName = XMLHelper.getAttr(element, "importedColName", null);
        caption = XMLHelper.getAttr(element, "caption", null);
        viewOrder = (short) XMLHelper.getAttr(element, "viewOrder", -1);
        origImportColumnIndex = (short) XMLHelper.getAttr(element, "origImportColumnIndex", -1);
        dataFieldLength = (short) XMLHelper.getAttr(element, "dataFieldLength", -1);
        fieldType = (short) XMLHelper.getAttr(element, "fieldType", -1);
        metaData = XMLHelper.getAttr(element, "metaData", null);
        xCoord = (short) XMLHelper.getAttr(element, "xCoord", -1);
        yCoord = (short) XMLHelper.getAttr(element, "yCoord", -1);
        carryForward = XMLHelper.getAttr(element, "carryForward", false);
        isExportableToContent = XMLHelper.getAttr(element, "isExportableToContent", false);
        isIncludedInTitle = XMLHelper.getAttr(element, "isIncludedInTitle", false);
        isRequired = XMLHelper.getAttr(element, "isRequired", false);
    }

    /**
     * @return the Table ID for the class.
     */
    public static int getClassTableId() {
        return 82;
    }

    /**
     * @return the importedColNameMaxLength
     */
    public static int getImportedColNameMaxLength() {
        return importedColNameMaxLength;
    }

    @Override
    @Transient
    public Class<?> getDataType() {
        log.error("getDataType not supported");
        return null;
    }
}
