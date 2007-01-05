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
package edu.ku.brc.ui.forms.persist;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class FormColumn implements Cloneable
{

    protected String name;
    protected String label;
    protected String dataObjFormatter;
    protected String format;

    public FormColumn(String name, String label, String dataObjFormatter, String format)
    {
        this.name = name;
        this.label = label;
        this.dataObjFormatter = dataObjFormatter;
        this.format = format;
    }

    public String getDataObjFormatter()
    {
        return dataObjFormatter;
    }

    public String getFormat()
    {
        return format;
    }

    public String getLabel()
    {
        return label;
    }

    public String getName()
    {
        return name;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        FormColumn formCol = (FormColumn)super.clone();
        formCol.name   = name;
        formCol.label  = label;
        formCol.dataObjFormatter = dataObjFormatter;
        formCol.format = format;
        return formCol;      
    }
 }
