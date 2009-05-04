/* Copyright (C) 2009, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
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
package edu.ku.brc.af.ui.forms.formatters;

import static edu.ku.brc.helpers.XMLHelper.xmlAttr;

import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * This class represents a single format. A DataobjectFormatter can switch between formats for a given field's value.
 * So either there is one of these (meaning the format is not switchable) or there is multiple formats.<br><br>
 * For example, an agent is formatted difgferently depending on whether it is a pareson or an organization.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjDataFieldFormat implements DataObjDataFieldFormatIFace, Cloneable
{
    protected String             name;
    protected Class<?>           dataClass;
    protected boolean            isDefault;
    protected String             format;
    protected String             value;
    protected DataObjDataField[] fields;
    protected DBTableInfo		 tableInfo;

    /**
     * 
     */
    public DataObjDataFieldFormat()
    {
    	value = "";
    }

    /**
     * @param name
     * @param dataClass
     * @param isDefault
     * @param format
     * @param value
     * @param fields
     */
    public DataObjDataFieldFormat(final String   name, 
                                  final Class<?> dataClass, 
                                  final boolean  isDefault, 
                                  final String   format, 
                                  final String   value,
                                  final DataObjDataField[] fields)
    {
        this.name       = name;
        this.dataClass  = dataClass;
        this.isDefault  = isDefault;
        this.format     = format;
        this.value      = value;
        this.fields     = fields;
        
        if (this.value == null)
        {
        	this.value = "";
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {   
    	if (fields == null)
    	{
    		return "";
    	}
    	
        StringBuilder str = new StringBuilder();
        for (DataObjDataField field : fields)
        {
            str.append(field.toString());
        }
        return str.toString();
    }
    
    /**
     * @return the format
     */
    public String getFormat()
    {
        return format;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getValue()
     */
    public String getValue()
    {
        return value;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(java.util.Properties)
     */
    public void init(final String nameArg, final Properties properties)
    {
        throw new RuntimeException("Not implemented!");
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getDataClass()
     */
    public Class<?> getDataClass()
    {
        return dataClass;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getName()
     */
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDefault()
     */
    public boolean isDefault()
    {
        return isDefault;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#isDirectFormatter()
     */
    public boolean isDirectFormatter()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getSingleField()
     */
    @Override
    public String getSingleField()
    {
        if (fields.length == 1 && 
            !StringUtils.contains(fields[0].getName(), ".") &&
            fields[0].getUiFieldFormatter() == null)
        {
            return fields[0].getName();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#format(java.lang.Object)
     */
    public String format(Object dataValue)
    {
        throw new RuntimeException("Not callable because isDirectFormatter is false");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getFields()
     */
    public DataObjDataField[] getFields()
    {
        return fields;
    }
    
    /*
     * Note: do not call this method from the constructor as it will trigger infinite recursion
     */
    public void setTableAndFieldInfo() 
    {
        tableInfo = DBTableIdMgr.getInstance().getByClassName(dataClass.getName());
        for (DataObjDataField field : fields)
        {
            field.setTableAndFieldInfo(tableInfo);
        }
    }

	public void setValue(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#toXML(java.lang.StringBuilder)
	 */
	public void toXML(StringBuilder sb)
	{
	    String space = "            ";
	    
        sb.append(space);
        sb.append("<fields");
        if (StringUtils.isNotEmpty(value))
        {
            xmlAttr(sb, "value", value);
        }
        sb.append(">\n");

        for (DataObjDataField field : fields)
        {
        	field.toXML(sb);
        }
        sb.append(space);
        sb.append("</fields>\n");
	}
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#setDataObjSwitchFormatter(edu.ku.brc.ui.forms.formatters.DataObjSwitchFormatter)
	 */
	public void setDataObjSwitchFormatter(DataObjSwitchFormatter objFormatter)
	{
	    if (fields == null)
	        return;
	    
	    for (DataObjDataField fld : fields)
        {
            fld.setObjFormatter(objFormatter);
        }
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getCustomEditor(javax.swing.event.ChangeListener)
     */
    @Override
    public JPanel getCustomEditor(ChangeListener l)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#isValid()
     */
    @Override
    public boolean isValid()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#hasEditor()
     */
    @Override
    public boolean hasEditor()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#isCustom()
     */
    @Override
    public boolean isCustom()
    {
        return false;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#getLabel()
     */
    @Override
    public String getLabel()
    {
        return "";
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.formatters.DataObjDataFieldFormatIFace#doneEditting(boolean)
     */
    @Override
    public void doneEditting(final boolean wasCancelled)
    {
        
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        DataObjDataFieldFormat dodff = (DataObjDataFieldFormat)super.clone();
        if (fields != null)
        {
            int i = 0;
            dodff.fields = new DataObjDataField[fields.length];
            for (DataObjDataField fld : fields)
            {
                dodff.fields[i++] = (DataObjDataField)fld.clone();
            }
        }
        return dodff;
    }
}
