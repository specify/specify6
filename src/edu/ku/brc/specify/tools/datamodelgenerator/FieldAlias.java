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
package edu.ku.brc.specify.tools.datamodelgenerator;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 10, 2008
 *
 */
public class FieldAlias
{
    protected String virtualFieldName;
    protected String actualFieldName;
    
    /**
     * @param virtualFieldName
     * @param actualFieldName
     */
    public FieldAlias(String virtualFieldName, String actualFieldName)
    {
        super();
        this.virtualFieldName = virtualFieldName;
        this.actualFieldName = actualFieldName;
    }

    /**
     * @return the virtualFieldName
     */
    public String getVirtualFieldName()
    {
        return virtualFieldName;
    }

    /**
     * @param virtualFieldName the virtualFieldName to set
     */
    public void setVirtualFieldName(String virtualFieldName)
    {
        this.virtualFieldName = virtualFieldName;
    }

    /**
     * @return the actualFieldName
     */
    public String getActualFieldName()
    {
        return actualFieldName;
    }

    /**
     * @param actualFieldName the actualFieldName to set
     */
    public void setActualFieldName(String actualFieldName)
    {
        this.actualFieldName = actualFieldName;
    }
    
    
}
