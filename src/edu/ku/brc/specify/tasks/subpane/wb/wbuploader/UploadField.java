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
package edu.ku.brc.specify.tasks.subpane.wb.wbuploader;

import java.lang.reflect.Method;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.SpecifyAppContextMgr;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.PrepType;
import edu.ku.brc.specify.dbsupport.RecordTypeCodeBuilder;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Field;
import edu.ku.brc.specify.tasks.subpane.wb.schema.Relationship;
import edu.ku.brc.specify.ui.CatalogNumberUIFieldFormatter;
import edu.ku.brc.specify.ui.db.PickListDBAdapterFactory;
import edu.ku.brc.specify.ui.db.PickListTableAdapter;

/**
 * @author timbo
 * 
 * @code_status Alpha
 * 
 * Describes properties of fields being uploaded as part of a workbench upload.
 */
public class UploadField
{
    protected static DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
    
    protected static final Logger                       log = Logger.getLogger(UploadField.class);
    /**
     * The Field in the database being uploaded to.
     */
    protected Field                      field;
    /**
     * The current value of the field. (The text contained in the Workbench)
     */
    protected String                     value;
    /**
     * For UploadFields implementing foreign keys: The relationship the foreign key participates in.
     */
    protected Relationship               relationship;
    /**
     * The field's column index in the workbench being uploaded. (-1 when not applicable (as for
     * foreign keys.))
     */
    protected int                        index;
    /**
     * The caption of the field's column in the workbench. (null when index == -1)
     */
    protected String                     wbFldName;
    /**
     * The one to many 'order' of the field (e.g. LastName1, lastName2, ... )
     */
    protected Integer                    sequence           = null;
    /**
     * True if the field must contain data
     */
    protected Boolean                    required = null;
    
    protected boolean 					 autoAssignForUpload = false; 
    /**
     * The methods used to set and get the field's contents in the java object representing the field's
     * Table.
     */
    protected Method                     setter;
    protected Method                     getter = null;
    
    protected Integer				     precision = null; //BigDecimal fields
    protected Integer                    scale = null; //BigDecimal
    protected boolean                    precisionAndScaleDetermined = false; //Big Decimal
    
    /**
	 * @return the precisionAndScaleDetermined
	 */
	public boolean isPrecisionAndScaleDetermined() {
		return precisionAndScaleDetermined;
	}

	/**
     * A set of valid values for fields with associated pick lists.
     */
    protected Map<String, PickListItemIFace> validValues        = null;

    /**
	 * @return the precision
	 */
	public Integer getPrecision() {
        if (!isPrecisionAndScaleDetermined()) {
        	determinePrecisionAndScale();
        }
		return precision;
	}

	/**
	 * @param input
	 * @return
	 */
	public boolean checkPrecisionAndScale(String input)
	{
		if (getPrecision() != null) 
		{
			int l = input.length();
			int sepIdx = input.indexOf(formatSymbols.getDecimalSeparator());
			int rightOfDecimal = (sepIdx > -1) ? l - sepIdx - 1 : 0;
			if (sepIdx > -1) 
			{
				l--;
			}
			int leftOfDecimal = l - rightOfDecimal;
			return leftOfDecimal <= precision - scale && rightOfDecimal <= scale;
		}
		return true;
	}
	
	/**
	 * @return the scale
	 */
	public Integer getScale() {
		return scale;
	}

	/**
	 * 
	 */
	public void determinePrecisionAndScale() 
	{
		if (field != null && field.getFieldInfo() != null && field.getFieldInfo().getType().equals("java.math.BigDecimal")) 
		{
		    String dbName = ((SpecifyAppContextMgr )AppContextMgr.getInstance()).getDatabaseName();
		    String tblName = field.getFieldInfo().getTableInfo().getName();
		    String fldName = field.getFieldInfo().getName();
			String colType = null;
		    Vector<Object[]> rows = BasicSQLUtils.query(DBConnection.getInstance().getConnection(), 
					"SELECT COLUMN_TYPE FROM `information_schema`.`COLUMNS` where TABLE_SCHEMA = '" +
		                dbName + "' and TABLE_NAME = '" + tblName + "' and COLUMN_NAME = '" + fldName + "'");                    
		    if (rows.size() == 1)
		    {
		        colType = rows.get(0)[0].toString().toLowerCase().trim();
		    }
			if (colType != null && colType.startsWith("decimal")) 
			{
				//"DECIMAL(19,2)"
				String psStr = colType.substring(8).replace(")", "");
				String[] ps = psStr.split(",");
				if (ps.length == 2) 
				{
					this.precision = Integer.valueOf(ps[0]);
					this.scale = Integer.valueOf(ps[1]);
				}
			}
		}
		this.precisionAndScaleDetermined = true;
	}
	
	/**
     * True if field has a read-only picklist
     */
    protected boolean readOnlyValidValues = false;
    
    /**
     * True if warnings should be made about values not contained in picklist.
     */
    protected boolean picklistWarn = false;
    
    /**
     * True if associated pick list has been searched for.
     */
    protected boolean                    validValuesChecked = false;

    /**
     * @return the setter
     */
    public final Method getSetter()
    {
        return setter;
    }

    /**
     * @param setter the setter to set
     */
    public final void setSetter(Method setter)
    {
        this.setter = setter;
    }

    /**
     * @return getter
     */
    public final Method getGetter()
    {
    	if (getter != null)
    	{
    		return getter;
    	}
    	
    	if (setter == null)
    	{
    		return null;
    	}
    	Class<?> cls = setter.getDeclaringClass();
    	try
    	{
    		String name = "get" + setter.getName().substring(3);
    		getter = cls.getMethod(name, (Class<?>[] )null);
    	} catch (NoSuchMethodException ex)
    	{
    		//no getter
    	}
    	return getter;
    }
    
    /**
     * @param field
     * @param index
     * @param wbFldName
     * @param relationship
     */
    public UploadField(Field field, int index, String wbFldName, Relationship relationship)
    {
        this.field = field;
        this.index = index;
        this.wbFldName = wbFldName;
        this.relationship = relationship;
        if (field != null && field.getFieldInfo() != null) {
        	UIFieldFormatterIFace fmt = field.getFieldInfo().getFormatter();
        	if (fmt != null) {
        		this.autoAssignForUpload = field.getFieldInfo().isRequired()
        				&& fmt.isIncrementer()
        				&& (fmt.isNumeric() || (fmt instanceof CatalogNumberUIFieldFormatter && ((CatalogNumberUIFieldFormatter)fmt).isNumericCatalogNumber()));
        	}
        }
    }

    /**
     * @param configFld
     */
    public void copyConfig(UploadField configFld) {
    	setAutoAssignForUpload(configFld.isAutoAssignForUpload());
    }
    
    /**
	 * @return the autoAssignForUpload
	 */
	public boolean isAutoAssignForUpload() {
		return autoAssignForUpload;
	}

	public boolean isAutoAssignable() {
		return getField().getFieldInfo() != null && getField().getFieldInfo().getFormatter() != null
	            && getField().getFieldInfo().getFormatter().isIncrementer(); 
//	            //&& fld.getField().getFieldInfo().getFormatter().isNumeric();

	}
	/**
	 * @param autoAssignForUpload the autoAssignForUpload to set
	 */
	public void setAutoAssignForUpload(boolean autoAssignForUpload) {
		this.autoAssignForUpload = autoAssignForUpload;
	}

	@Override
    public String toString()
    {
        return wbFldName + ", " + String.valueOf(index) + ", " + field.getName();
    }
    /**
     * @return the field
     */
    public Field getField()
    {
        return field;
    }

    /**
     * @param field the field to set
     */
    public void setField(Field field)
    {
        this.field = field;
    }

    /**
     * @param val
     * @return true if val is a valid value for this field.
     */
    public boolean validate(final String val)
    {
        return true;
    }

    /**
     * @return the value; if the field has a picklist lookup the value in the list and
     * return the associated value object.
     */
    public String getValueObject()
    {
        if (getValidValues() == null)
        {
            return getValue();
        }
        PickListItemIFace item = validValues.get(value);
        if (item != null)
        {
            Object valObj = item.getValueObject();
            if (valObj instanceof DataModelObjBase)
            {
            	//this means its a table picklist, but, with current code, all we need is the item 'name'
            	return value;
            }
        	return valObj.toString();
        }
        if (!StringUtils.isBlank(value))
        {
        	if (readOnlyValidValues)
        	{
        		//this should have already been caught.
        		log.error("Invalid value '" + value + "' for field '" + wbFldName + "'");
        		return null;
        	} 
        	return value;
        }
        return null;
    }
    /**
     * @return the value
     */
    public String getValue()
    {
        if (StringUtils.isEmpty(value))
        {
            return null;
        }
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value)
    {
        this.value = value;
    }

    /**
     * @return the relationship
     */
    public Relationship getRelationship()
    {
        return relationship;
    }

    /**
     * @param relationship the relationship to set
     */
    public void setRelationship(Relationship relationship)
    {
        this.relationship = relationship;
    }

    /**
     * @return the sequence
     */
    public Integer getSequence()
    {
        return sequence;
    }

    public int getSequenceInt()
    {
        if (sequence == null)
        {
            return 0;
        }
        return sequence;
    }
    
    /**
     * @param sequence the sequence to set
     */
    public void setSequence(Integer sequence)
    {
        this.sequence = sequence;
    }

    /**
     * @return the index
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * @param index the index to set
     */
    public void setIndex(int index)
    {
        this.index = index;
    }

    /**
     * @return the required
     */
    public boolean isRequired()
    {
        if (required != null)
        {
            return required;
        }
        
        if (field != null && field.getFieldInfo() != null)
        {
            return field.getFieldInfo().isRequired() 
            	//force CollectionObject.CatalogNumber to be required
            	//|| (field.getFieldInfo().getTableInfo().getTableId() == 1 && field.getFieldInfo().getName().equalsIgnoreCase("catalogNumber"))
            	//ditto for locality.LocalityName. Its field info no longer says it's required, but hibernate/mysql still requires it.
            	|| (field.getFieldInfo().getTableInfo().getTableId() == Locality.getClassTableId() && field.getFieldInfo().getName().equalsIgnoreCase("localityname"));
        }
        
        return false;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(boolean required)
    {
        this.required = required;
    }

    /**
     * @return the wbFldName
     */
    public final String getWbFldName()
    {
        return wbFldName;
    }
    
    /**
      * @return list valid values for this field's picklist,
     * or null if no picklist is defined for this field.
    */
    public Map<String, PickListItemIFace> getValidValues()
    {
        if (!validValuesChecked)
        {
            validValuesChecked = true;
            validValues = buildValidValues();
        }
        return validValues;
    }
    
    /**
     * @return list valid values for this field's picklist,
     * or null if no picklist is defined for this field.
     */
    protected Map<String, PickListItemIFace> buildValidValues()
    {
        if (getIndex() != -1 && getField().getFieldInfo() != null)
        {
            PickListDBAdapterIFace pickList = null;
            if (!StringUtils.isEmpty(getField().getFieldInfo().getPickListName()))
            {
                pickList = PickListDBAdapterFactory.getInstance().create(getField().getFieldInfo().getPickListName(), false);
            }
            else if (RecordTypeCodeBuilder.isTypeCodeField(getField().getFieldInfo()))
            {
                pickList = RecordTypeCodeBuilder.getTypeCode(getField().getFieldInfo());
            } 
            else 
            {
            	pickList = checkForSpecialCasePicklist();
            }
            if (pickList != null)
            {
                readOnlyValidValues = pickList.isReadOnly() || pickList instanceof PickListTableAdapter;
                picklistWarn = !readOnlyValidValues && pickList instanceof PickListTableAdapter;
                
				TreeMap<String, PickListItemIFace> pickListItems = new TreeMap<String, PickListItemIFace>(
						new Comparator<String>() {

							/*
							 * (non-Javadoc)
							 * 
							 * @see
							 * java.util.Comparator#compare(java.lang.Object,
							 * java.lang.Object)
							 */
							@Override
							public int compare(String arg0, String arg1) {
								if (arg0 == null && arg1 == null)
									return 0;
								if (arg0 == null)
									return 1;
								if (arg1 == null)
									return -1;
								return arg0.compareToIgnoreCase(arg1);
							}

						});
                for (PickListItemIFace item : pickList.getList())
                {
                    pickListItems.put(item.getTitle(), item);
                }
                return pickListItems;
            }
        }
        return null;
    }
    
    /**
     * @return picklist for "special cases"
     */
    protected PickListDBAdapterIFace checkForSpecialCasePicklist()
    {
    	PickListDBAdapterIFace result = null;
    	DBFieldInfo fldInfo = getField().getFieldInfo();
    	if (fldInfo != null)
    	{
    		if (fldInfo.getName().equals("name") && fldInfo.getTableInfo().getClassObj().equals(PrepType.class))
    		{
            	PickListDBAdapterIFace pl = PickListDBAdapterFactory.getInstance().create(fldInfo.getTableInfo().getName(), false);
            	if (pl instanceof PickListTableAdapter)
            	{
            		result =  pl;
            	}
    		}
    	}
    	return result;
    }

	/**
	 * @return the readOnlyValidValues
	 */
	public boolean isReadOnlyValidValues() 
	{
		return readOnlyValidValues;
	}

	/**
	 * @return the picklistWarn
	 */
	public boolean isPicklistWarn() 
	{
		return picklistWarn;
	}
    
	
}
