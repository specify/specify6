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

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Jan 17, 2007
 *
 */
public class DataObjDataField
{
    protected String   name;
    protected Class<?> type;
    protected String   format;
    protected String   sep;
    protected String   formatterName;
    
    public DataObjDataField(final String name, 
                            final Class<?> type, 
                            final String format, 
                            final String sep, 
                            final String formatterName)
    {
        super();
        
        this.name   = name;
        this.type   = type;
        this.format = format;
        this.sep    = sep;
        this.formatterName = formatterName;
    }
    
    public String getFormat()
    {
        return format;
    }
    
    public String getName()
    {
        return name;
    }
    
    public String getSep()
    {
        return sep;
    }
    
    public Class<?> getType()
    {
        return type;
    }
    
    public String getFormatterName()
    {
        return formatterName;
    }
}