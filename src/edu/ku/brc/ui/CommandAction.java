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
package edu.ku.brc.ui;

import java.util.Hashtable;
import java.util.Properties;

/**
 * Represents a single command action typically between the UI and a task, sometimes between tasks
 * The "type" of command determines who will be listening for it.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class CommandAction implements Cloneable
{

    protected String type;
    protected String action;
    protected int    tableId;
    
    protected Object       data;
    protected Object       srcObj;
    protected Object       dstObj;
    
    protected boolean      isConsumed = false;
    
    protected Properties properties = null;
    
    /**
     * Constructs a command.
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param srcObj the source object
     * @param dstObj the destination object
     * @param data the data to be passed
     */
    public CommandAction(final String type, 
                         final String action, 
                         final Object srcObj, 
                         final Object dstObj, 
                         final Object data)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = -1;
        this.srcObj  = srcObj;
        this.dstObj  = dstObj;
        this.data    = data;
    }
    
    /**
     * Constructs a command.
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param data the data to be passed
     */
    public CommandAction(final String type, 
                         final String action, 
                         final Object data)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = -1;
        this.data    = data;
        this.srcObj  = null;
        this.dstObj  = null;
    }
    
    /**
     * Constructs a command.
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param tableId the table id that the command is associated with
     */
    public CommandAction(final String type, 
                         final String action, 
                         final int tableId)
    {
        this(type, action, tableId, null);
    }
    
    /**
     * Constructs a command.
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param tableId the table id that the command is associated with
     * @param strParams string key/value pairs that will be added to the properties
     */
    public CommandAction(final String type, 
                         final String action, 
                         final int tableId, 
                         final Hashtable<String, String> strParams)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = tableId;
        this.data    = null;
        this.srcObj  = null;
        this.dstObj  = null;
        
        if (strParams != null)
        {
            for (String key : strParams.keySet())
            {
                setProperty(key, strParams.get(key));
            }
        }
    }
    
    /**
     * Constructs a command.
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     * @param strParams string key/value pairs that will be added to the properties
     */
    public CommandAction(final String type, 
                         final String action, 
                         final Hashtable<String, String> strParams)
    {
        this(type, action, -1, strParams);
    }
    
    /**
     * Constructs a command
     * @param type the type of command determines who listens for it
     * @param action the name of the action to be performed (contract between producer and consumer)
     */
    public CommandAction(final String type, 
                         final String action)
    {
        this.type    = type;
        this.action  = action;
        this.tableId = -1;
        this.data    = null;
        this.srcObj  = null;
        this.dstObj  = null;
    }
    
    
    /**
     * Sets all the values (replaces) from the incoming CommandAction.
     * @param ca the source of the values
     */
    public void set(final CommandAction ca)
    {
        this.type    = ca.type;
        this.action  = ca.action;
        this.tableId = ca.tableId;
        this.srcObj  = ca.srcObj;
        this.dstObj  = ca.dstObj;
        this.data    = ca.data;
        
        if (this.properties != null && ca.properties != null)
        {
            this.properties.clear();
            this.properties.putAll(ca.properties);
        }
    }

    
    /**
     * Return true if the type is equals the value (case sensitive).
     * @param typ the type in question
     * @return true if equal
     */
    public boolean isType(final String typ)
    {
        return type != null && type.equals(typ);
    }
    
    /**
     * Return true if the action is equals the value (case sensitive).
     * @param act the action in question
     * @return true if equal
     */
    public boolean isAction(final String act)
    {
        return action != null && action.equals(act);
    }
    
    public String getAction()
    {
        return action;
    }

    public void setData(final Object data)
    {
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }

    public String getType()
    {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type)
    {
        this.type = type;
    }

    /**
     * @param action the action to set
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    public boolean isConsumed()
    {
        return isConsumed;
    }

    public void setConsumed(boolean isConsumed)
    {
        this.isConsumed = isConsumed;
    }

    public int getTableId()
    {
        return tableId;
    }

    public Object getDstObj()
    {
        return dstObj;
    }

    public Object getSrcObj()
    {
        return srcObj;
    }
    
    /**
     * @param srcObj the srcObj to set
     */
    public void setSrcObj(Object srcObj)
    {
        this.srcObj = srcObj;
    }

    /**
     * Gets the property as an Object.
     * @param name the prop name
     * @return the value
     */
    public Object getProperty(final String name)
    {
        return properties == null ? null : properties.get(name);
    }
    
    /**
     * Returns property as a String.
     * @param name the name
     * @return the value as a string
     */
    public String getPropertyAsString(final String name)
    {
        if (properties != null)
        {
            Object obj = properties.get(name);
            return obj != null ? obj.toString() : null;
        }
        return null;
    }
    
    /**
     * Sets a property.
     * @param name the name of the property
     * @param value the value of the property
     */
    public void setProperty(final String name, final Object value)
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        properties.put(name, value);
    }
    
    public void addProperties(final Properties props)
    {
        if (props != null)
        {
            if (properties == null)
            {
                properties = new Properties();
            }
            properties.putAll(props);
        }
    }
    
    public void addStringProperties(final Properties props)
    {
        if (props != null)
        {
            if (properties == null)
            {
                properties = new Properties();
            }
            properties.putAll(props);
        }
    }
    
    public Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return this.properties;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.DataModelObjBase#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        CommandAction obj = (CommandAction)super.clone();
        
        obj.type    = type;
        obj.action  = action;
        obj.tableId = tableId;
        obj.data    = data;
        obj.srcObj  = srcObj;
        obj.dstObj  = dstObj;
        
        if (properties != null)
        {
            obj.properties = (Properties)properties.clone();
        }
        
        return obj;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "CommandAction Type["+type+"]  Action["+action+"] data["+ (data != null ? data.getClass().getSimpleName() : "null")+ "]";
    }

}
