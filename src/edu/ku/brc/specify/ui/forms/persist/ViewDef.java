/* Filename:    $RCSfile: ViewDef.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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

package edu.ku.brc.specify.ui.forms.persist;

import static org.apache.commons.lang.StringUtils.isNotEmpty;

import org.apache.log4j.Logger;

import edu.ku.brc.specify.ui.forms.DataObjectGettable;
import edu.ku.brc.specify.ui.forms.DataObjectGettableFactory;
import edu.ku.brc.specify.ui.forms.DataObjectSettable;
import edu.ku.brc.specify.ui.forms.DataObjectSettableFactory;

public class ViewDef
{
    private static final Logger log = Logger.getLogger(ViewDef.class);
    
    public enum ViewType {form, table, field};
    
    protected ViewType             type;
    protected String               name;
    protected String               desc;
    protected String               className;
    protected String               dataGettableName;
    protected String               dataSettableName;
    
    protected DataObjectGettable   dataGettable   = null;
    protected DataObjectSettable   dataSettable   = null;
    
    /**
     * Default Constructor
     *
     */
    public ViewDef()
    {
        
    }
    
    /**
     * Create View Def
     * @param type the type of form (form, table, field)
     * @param name a unique name for the ViewDef
     * @param className the clas name that this view def can display
     * @param dataGettableName the gettable name
     * @param dataSettableName the settable name
     * @param desc a description of the view def
     */
    public ViewDef(final ViewType type, 
                   final String   name, 
                   final String   className, 
                   final String   dataGettableName, 
                   final String   dataSettableName, 
                   final String   desc)
    {
        this.type = type;
        this.name = name;
        this.className = className;
        this.dataGettableName = dataGettableName;
        this.dataSettableName = dataSettableName;
        this.desc = desc;
        
        try
        {
            // Can't imagine why you would not ALWAYS want to have a Gettable
            if (isNotEmpty(dataGettableName))
            {
                dataGettable = (DataObjectGettable)DataObjectGettableFactory.get(className, dataGettableName);
            } else
            {
                log.error("dataGettableName or is null for "+name);
            }
           
            // OK to NOT have a Settable
            if (isNotEmpty(dataSettableName))
            {
                dataSettable = (DataObjectSettable)DataObjectSettableFactory.get(className, dataSettableName);
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace(); // XXX REMOVE ME
            log.error(ex);
        }
    }
    
    /**
     * Clean up internal data 
     */
    public void cleanUp()
    {
        dataGettable = null;
        dataSettable = null;
    }
    
    public ViewType getType()
    {
        return type;
    }

    public void setType(final ViewType type)
    {
        this.type = type;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getDataGettableName()
    {
        return dataGettableName;
    }

    public void setDataGettableName(String dataGettableName)
    {
        this.dataGettableName = dataGettableName;
    }

    public DataObjectGettable getDataGettable()
    {
        return dataGettable;
    }

    public DataObjectSettable getDataSettable()
    {
        return dataSettable;
    }

    public String toString()
    {
        return this.name;
    }}
