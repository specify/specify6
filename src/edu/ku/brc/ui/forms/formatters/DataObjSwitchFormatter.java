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
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DBFieldInfo;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.forms.DataObjectGettable;
import edu.ku.brc.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.ui.forms.FormHelper;

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
public class DataObjSwitchFormatter implements Comparable<DataObjSwitchFormatter>, Cloneable
{
    protected static final Logger log = Logger.getLogger(DataObjSwitchFormatter.class);
    
    protected String          		 name;	  // unique identifier to the formatter 
    protected String		  		 title;   // name assigned to formatter by the user (so that renaming won't affect references)
    protected boolean                isSingle;
    protected boolean                isDefault;
    protected Class<?>               dataClass;
    protected String                 fieldName;
    protected DBFieldInfo			 fieldInfo;
    protected DataObjDataFieldFormatIFace single     = null;
    
    protected Vector<DataObjDataFieldFormatIFace> formatsVector = new Vector<DataObjDataFieldFormatIFace>();
    
    /**
     * A formatter that can have one or more formatters that depend on an external value, typically from the database.
     * @param name the name of the formatter
     * @param isSingle whether it is a single formatter (not switchable)
     * @param isDefault whether it is the default formatter
     * @param dataClass the class name of objects it will be formatting fields from
     * @param fieldName the name of the field to be formatted
     */
    public DataObjSwitchFormatter(final String   name, 
    							  final String   title,
                                  final boolean  isSingle, 
                                  final boolean  isDefault, 
                                  final Class<?> dataClass, 
                                  final String   fieldName)
    {
        this.name      = name;
        this.title     = title;
        this.isSingle  = isSingle;
        this.isDefault = isDefault;
        this.dataClass = dataClass;
        this.fieldName = fieldName;
    }
    
	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public void setDataClass(Class<?> dataClass)
	{
		this.dataClass = dataClass;
	}

	public void setSingle(boolean isSingle)
	{
		this.isSingle = isSingle;
	}
	
	public void setSingle(DataObjDataFieldFormatIFace single)
	{
		this.single = single;
	}
	
	public void clearFields()
	{
		single = null;
		formatsVector.clear();
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
        	formatsVector.add(dff);
        }
    }
    
	/**
     * Replace the field format at the given index.
     * @param index index of the field format being replaced 
     * @param dff the field format to be replaced
     * Note: must not be single switch and index must already exist
     */
    public void set(int index, final DataObjDataFieldFormatIFace dff)
    {
        if (formatsVector.size() == 0 || index > formatsVector.size())
        {
            formatsVector.add(dff);
        } else
        {
            formatsVector.set(index, dff);
        }
    }
    
    /**
     * Removes a field format.
     * @param dff
     */
    public void remove(final DataObjDataFieldFormatIFace dff)
    {
    	if (!isSingle && formatsVector != null) 
    	{
    		formatsVector.remove(dff);
    		//formatsHashtable.remove(dff.getValue());
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
        
        // look for value on vector
        // can't do better than that if we let clients change value of keys at run-time
        // unless we ensure that the field container remains sorted even after keys are changed
        // for now, we can live with that as formatsVector is bound to contain only a handful of objects anyway
        for (DataObjDataFieldFormatIFace field : formatsVector)
        {
        	if (field.getValue().equals(value))
        		return field;
        }
        
        return null;
        //return formatsHashtable.get(value);
    }
    
    /**
     * @return whether there are any formatters thus indicating one aspect of being valid.
     */
    public boolean hasFormatters()
    {
        return (isSingle && single != null) || (formatsVector != null && formatsVector.size() > 0);
    }
    
    /**
     * @return
     */
    public Collection<DataObjDataFieldFormatIFace> getFormatters()
    {
    	if (isSingle)
    	{
    		// code below is used to return single as part of a Collection
    		Vector<DataObjDataFieldFormatIFace> vector = new Vector<DataObjDataFieldFormatIFace>();
    		if (single != null)
    		{
    		    vector.add(single);    
    		}
    		return vector;
    	}
    	
    	return formatsVector;
    	//return formatsHashtable.values();
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

        DataObjectGettable getter = DataObjectGettableFactory.get(dataObj.getClass().getName(), FormHelper.DATA_OBJ_GETTER);

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
    
    /**
     * @param isDefault the isDefault to set
     */
    public void setDefault(boolean isDefault)
    {
        this.isDefault = isDefault;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
		return (StringUtils.isNotEmpty(title))? title : 
			StringUtils.isNotEmpty(name)? name : "";
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
        throw new RuntimeException("This method cannot be called on this type of object["+name+"]["+dataObj.getClass().getSimpleName()+"]");
    }

    /**
     * @param name
     */
    public void setName(String name) 
    {
        this.name = name;
    }

    /**
     * @param fieldName
     */
    public void setFieldName(String fieldName) 
    {
        this.fieldName = fieldName;
    }

    /**
     * 
     */
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
    	if (formatsVector.size() == 0)
    	{
    		return;
    	}
    	
    	for (DataObjDataFieldFormatIFace format : formatsVector)
    	{
    		format.setTableAndFieldInfo();
    	}
    }

    /**
     * @param sb
     */
    public void toXML(StringBuilder sb)
    {
        sb.append("  <format");
        xmlAttr(sb, "name", name);
        xmlAttr(sb, "title", title);
        
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
    		for (DataObjDataFieldFormatIFace field : formatsVector)
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

    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        DataObjSwitchFormatter dfo = (DataObjSwitchFormatter)super.clone();
        
        dfo.formatsVector = new Vector<DataObjDataFieldFormatIFace>();
        for (DataObjDataFieldFormatIFace dodf : formatsVector)
        {
            DataObjDataFieldFormatIFace ddf = (DataObjDataFieldFormatIFace)dodf.clone();
            ddf.setDataObjSwitchFormatter(dfo);
            dfo.formatsVector.add(ddf);
        }
        
        if (dfo.single != null)
        {
            dfo.single = (DataObjDataFieldFormatIFace)single.clone();
            dfo.single.setDataObjSwitchFormatter(dfo);
        }
        return dfo;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DataObjSwitchFormatter o)
    {
        return name.compareTo(o.name);
    }
}