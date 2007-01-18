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

import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

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
public class DataObjSwitchFormatter
{
    protected static final Logger log = Logger.getLogger(DataObjSwitchFormatter.class);
    
    protected String                 name;
    protected boolean                isSingle;
    protected boolean                isDefault;
    protected Class                  dataClass;
    protected String                 fieldName;
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
                                  final Class   dataClass, 
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


    /**
     * Returns the field name.s
     * @return the field name
     */
    public String getFieldName()
    {
        return fieldName;
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
    public Class getDataClass()
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
    public String format(Object dataObj)
    {
        throw new RuntimeException("This method cannot be called on this type of object");
    }
    
}