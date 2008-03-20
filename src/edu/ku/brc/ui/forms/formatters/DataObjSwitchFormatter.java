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
package edu.ku.brc.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;

/**
 * A formatter that can have one or more formatters that depend on an external value, typically from the database.
 * It can be a "single" formatter meaning it can't switch between different formats given an external value.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjSwitchFormatter implements Comparable<DataObjSwitchFormatter>
{
    protected static final Logger log = Logger.getLogger(DataObjSwitchFormatter.class);
    
    protected String                 name;
    protected boolean                isSingle;
    protected boolean                isDefault;
    protected Class<?>               dataClass;
    protected String                 fieldName;
    protected DBFieldInfo			 fieldInfo;
    protected DataObjDataFieldFormatIFace single     = null;
    
    protected Hashtable<String, DataObjDataFieldFormatIFace> formatsHashtable= null;
    
    /**
     * A formatter that can have one or more formatters that depend on an external value, typically from the database.
     * @param name the name of the formatter
     * @param isSingle whether it is a single formatter (not switchable)
     * @param isDefault whether it is the default formatter
     * @param dataClass the class name of objects it will be formatting fields from
     * @param fieldName the name of the field to be formatted
     */
    public DataObjSwitchFormatter(final String  name, 
                                  final boolean isSingle, 
                                  final boolean isDefault, 
                                  final Class<?>  dataClass, 
                                  final String  fieldName)
    {
        this.name      = name;
        this.isSingle  = isSingle;
        this.isDefault = isDefault;
        this.dataClass = dataClass;
        this.fieldName = fieldName;
    }
    
    /**
     * Adds a field format. 
     * @param dff the field format to be added
     */
    public void add(final DataObjDataFieldFormatIFace dff)
    {
        if (isSingle)
        {
            single = dff;
            
        } else
        {
            if (formatsHashtable == null)
            {
                formatsHashtable = new Hashtable<String, DataObjDataFieldFormatIFace>();
            }
            
            if (StringUtils.isNotEmpty(dff.getValue()))
            {
                formatsHashtable.put(dff.getValue(), dff);
                
            } else
            {
                log.error("Data formatter's 'value' attribute is empty for ["+dff.getName()+"]");
            }
        }
    }
    
    /**
     * Given a database string value it will return the proper formatter when it is able
     * to switch between formatters (isSingle == false).
     * @param value the database value that determines which format to use.
     * @return the formatter
     */
    public DataObjDataFieldFormatIFace getFormatterForValue(final String value)
    {
        if (isSingle)
        {
            return single;
            
        }
        return formatsHashtable.get(value);
    }
    
    public Collection<DataObjDataFieldFormatIFace> getFormatters()
    {
    	if (isSingle)
    	{
    		// code below is used to return single as part of a Collection
    		Vector<DataObjDataFieldFormatIFace> vector = new Vector<DataObjDataFieldFormatIFace>();
    		vector.add(single);
    		return vector;
    	}
    	
    	return formatsHashtable.values();
    }

    /**
     * Format a data object using a named formatter.
     * @param dataObj the data object for which fields will be formatted for it
     * @return the string result of the format
     */
    protected DataObjDataFieldFormatIFace getDataFormatter(final Object dataObj)
    {
        if (isSingle())
        {
            return getFormatterForValue(null); // null is ignored
        }

        DataObjectGettable getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), "edu.ku.brc.ui.forms.DataGetterForObj");

        DataObjDataFieldFormatIFace dff = null;
        Object[] values = UIHelper.getFieldValues(new String[] {getFieldName()}, dataObj, getter);
        if (values != null)
        {
            String value = values[0] != null ? values[0].toString() : "null";
            dff = getFormatterForValue(value);
            if (dff == null)
            {
                throw new RuntimeException("Couldn't find a switchable data formatter for ["+getName()+"] field["+getFieldName()+"] value["+value+"]");
            }
        } else
        {
            throw new RuntimeException("Values Array was null for Class["+dataObj.getClass().getSimpleName()+"] couldn't find field["+getFieldName()+"] (you probably passed in the wrong type of object)");
        }
        return dff;
    }

    /**
     * Returns the field names.
     * @return the field name
     */
    public String getFieldName()
    {
        return fieldName;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
    	if (isSingle)
    	{
    		return single.toString();
    	}
    	
    	String title = "";
    	if (fieldInfo != null)
    	{
    		title = fieldInfo.getTitle();
    	}
    	else
    	{
    		title = getFieldName();
    	}
    	return "[" + dataClass.getSimpleName() + " by " + title + "]";
    }
    
    //-----------------------------------------------------------------------
    //-- DataObjectFormatterIFace Interface
    //-----------------------------------------------------------------------


    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#getName()
     */
    public String getName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#isSingle()
     */
    public boolean isSingle()
    {
        return isSingle;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return dataClass;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#isDefault()
     */
    public boolean isDefault()
    {
        return isDefault;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#getSingle()
     */
    public DataObjDataFieldFormatIFace getSingle()
    {
        return single;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#isDirectFormatter()
     */
    public boolean isDirectFormatter()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjectFormatterIFace#format(java.lang.Object)
     */
    public String format(@SuppressWarnings("unused") Object dataObj)
    {
        throw new RuntimeException("This method cannot be called on this type of object");
    }

    public void setTableAndFieldInfo()
    {
    	if (StringUtils.isNotEmpty(fieldName))
    	{
    		DBTableInfo tableInfo = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
    		fieldInfo = tableInfo.getFieldByName(fieldName);
    	}
    	
    	if (single != null)
    	{
    		single.setTableAndFieldInfo();
    		return;
    	} 
    	// else 
    	if (formatsHashtable.size() == 0)
    		return;
    	
    	for (DataObjDataFieldFormatIFace format : formatsHashtable.values())
    	{
    		format.setTableAndFieldInfo();
    	}
    }

    public void toXML(StringBuilder sb)
    {
        sb.append("  <format");
        xmlAttr(sb, "name", name);
        
        if (dataClass != null)
        {
            xmlAttr(sb, "class", dataClass.getName());
        }
        
        if (isDefault)
        {
            xmlAttr(sb, "default", isDefault);
        }
        sb.append(">\n");
        
        sb.append("    <switch");
        xmlAttr(sb, "single", isSingle);
        xmlAttr(sb, "field", fieldName);
        sb.append(">\n");

        if (isSingle && single != null)
        {
        	single.toXML(sb);
        }
        else 
        {
        	// sort fields value and get their XML representation 
    		Vector<DataObjDataFieldFormatIFace> formatVector;
    		formatVector = new Vector<DataObjDataFieldFormatIFace>(formatsHashtable.values());
    		Collections.sort(formatVector, new Comparator<DataObjDataFieldFormatIFace>()
    		{
    			public int compare(DataObjDataFieldFormatIFace o1, DataObjDataFieldFormatIFace o2)
    			{
    				return o1.getValue().compareTo(o2.getValue());
    			}
    		});
    		for (DataObjDataFieldFormatIFace field : formatVector)
            {
            	field.toXML(sb);
            }
        }

        sb.append("    </switch>\n");
        sb.append("  </format>\n\n");
    }
    
    //-----------------------------------------------------------------------
    //-- Comparable Interface
    //-----------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DataObjSwitchFormatter o)
    {
        return name.compareTo(o.name);
    }

	public void setName(String name) {
		this.name = name;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
}