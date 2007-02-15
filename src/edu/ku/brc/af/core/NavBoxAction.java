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
package edu.ku.brc.af.core;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.dnd.DataActionEvent;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 29, 2006
 *
 */
public class NavBoxAction implements ActionListener
{
    public static final String ORGINATING_TASK = "OriginatingTask";
    
    private static final Logger log = Logger.getLogger(NavBoxAction.class);
    
    protected Taskable              originatingTask = null;
    protected String                type;
    protected String                action;
    protected Map<String, Object>   properties      = null; 
    protected CommandAction         cmdAction       = null;


    /**
     * @param tcd
     */
    public NavBoxAction(final TaskCommandDef tcd)
    {
        this(tcd, null);
    }

    /**
     * @param tcd
     * @param origTask
     */
    public NavBoxAction(final TaskCommandDef tcd, Taskable origTask)
    {
        this.originatingTask = origTask;
        this.type            = tcd.getParams().get("type");
        this.action          = tcd.getParams().get("action");
        
        this.properties = new Hashtable<String, Object>();
        this.properties.putAll(tcd.getParams());
        
        setOriginatingTask(origTask);
    }

    /**
     * Constructor.
     * @param cmdAction the cmdAction to "fire"
     */
    public NavBoxAction(final CommandAction cmdAction)
    {
        this.cmdAction = cmdAction;
    }

    /**
     * @param type
     * @param action
     */
    public NavBoxAction(final String type, final String action)
    {
        this(type, action, null);
    }

    /**
     * @param type
     * @param action
     * @param origTask
     */
    public NavBoxAction(final String type, final String action, final Taskable origTask)
    {
        this.type   = type;
        this.action = action;
        
        setOriginatingTask(origTask);
    }

    /**
     * @param origTask
     */
    public void setOriginatingTask(final Taskable origTask)
    {
        this.originatingTask = origTask;
        if (origTask != null)
        {
            if (this.properties == null)
            {
                this.properties = new Hashtable<String, Object>();
            }
            this.properties.put(ORGINATING_TASK, this.originatingTask);
        }
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
            properties = new Hashtable<String, Object>();
        }
        properties.put(name, value);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        
        if (cmdAction != null)
        {
            CommandDispatcher.dispatch(cmdAction);
            
        } else
        {
            Object data = null;
            if (e instanceof DataActionEvent)
            {
                data = ((DataActionEvent)e).getData();
            }
            
            if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(action))
            {
                CommandAction cmd = new CommandAction(type, action, data);
                cmd.addProperties(properties);
                CommandDispatcher.dispatch(cmd);
                
            } else
            {
                log.debug("Type and/or Action was NULL!");
            }
        }
    }
}
