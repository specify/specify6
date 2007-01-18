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
package edu.ku.brc.ui.forms.formatters;

import java.util.Map;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjDataFieldFormat implements DataObjDataFieldFormatIFace
{
    protected String             name;
    protected Class<?>           dataClass;
    protected boolean            isDefault;
    protected String             format;
    protected String             value;
    protected DataObjDataField[] fields;
    protected Class<?>           classObj;

    public DataObjDataFieldFormat(final String name, 
                                  final Class<?> dataClass, 
                                  final boolean isDefault, 
                                  final String format, 
                                  final String value,
                                  final DataObjDataField[] fields)
    {
        this.name       = name;
        this.dataClass  = dataClass;
        this.isDefault  = isDefault;
        this.format     = format;
        this.value      = value;
        this.fields     = fields;
    }

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
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#init(java.util.Map)
     */
    public void init(final String nameArg, final Map<String, String> properties)
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
     * @see edu.ku.brc.ui.forms.formatters.DataObjDataFieldFormatIFace#getClassObj()
     */
    public Class<?> getClassObj()
    {
        return classObj;
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
}
