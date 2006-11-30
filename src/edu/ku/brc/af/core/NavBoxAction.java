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
public class NavBoxAction  implements ActionListener
{
    public static final String ORGINATING_TASK = "OriginatingTask";
    
    private static final Logger log = Logger.getLogger(NavBoxAction.class);
    
    protected Taskable              originatingTask = null;
    protected String                type;
    protected String                action;
    protected Map<String, Object>   properties = null; 


    public NavBoxAction(final TaskCommandDef tcd)
    {
        this(tcd, null);
    }

    public NavBoxAction(final TaskCommandDef tcd, Taskable origTask)
    {
        this.originatingTask = origTask;
        this.type            = tcd.getParams().get("type");
        this.action         = tcd.getParams().get("action");
        
        this.properties = new Hashtable<String, Object>();
        this.properties.putAll(tcd.getParams());
        
        setOriginatingTask(origTask);
    }

    public NavBoxAction(final String type, final String action)
    {
        this(type, action, null);
    }

    public NavBoxAction(final String type, final String action, final Taskable origTask)
    {
        this.type   = type;
        this.action = action;
        
        setOriginatingTask(origTask);
    }

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

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e)
    {
        if (e instanceof DataActionEvent)
        {
            if (StringUtils.isNotEmpty(type) && StringUtils.isNotEmpty(action))
            {
                CommandAction cmd = new CommandAction(type, action, ((DataActionEvent)e).getData());
                cmd.addProperties(properties);
                CommandDispatcher.dispatch(cmd);
                
            } else
            {
                log.debug("Type and/or Action was NULL!");
            }
        }
    }
}
