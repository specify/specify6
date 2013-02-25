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
package edu.ku.brc.specify.ui;

import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.ku.brc.af.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.BorrowAgent;
import edu.ku.brc.specify.datamodel.Collector;
import edu.ku.brc.specify.datamodel.DeaccessionAgent;
import edu.ku.brc.specify.datamodel.FundingAgent;
import edu.ku.brc.specify.datamodel.LoanAgent;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 23, 2007
 *
 */
public class AgentIconTextMapper implements ObjectTextMapper, ObjectIconMapper
{
    //private static final Logger log = Logger.getLogger(AgentIconTextMapper.class);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class<?>[] getMappedClasses()
    {
        Class<?>[] mappedClasses = new Class[6];
        mappedClasses[0] = Agent.class;
        mappedClasses[1] = BorrowAgent.class;
        mappedClasses[2] = AccessionAgent.class;
        mappedClasses[3] = Collector.class;
        mappedClasses[4] = DeaccessionAgent.class;
        mappedClasses[5] = LoanAgent.class;
        return mappedClasses;
    }
    
    /**
     * Returns the Agent object given a mapped data object.
     * @param o the data object that needs to give up an Agent object.
     * @return null or an Agent object
     */
    protected Agent getAgent(final Object o)
    {
        Agent a = null;
        if (o instanceof Agent)
        {
            a = (Agent)o;
        } else
        if (o instanceof BorrowAgent)
        {
            a = ((BorrowAgent)o).getAgent();
        } else
        if (o instanceof AccessionAgent)
        {
            a = ((AccessionAgent)o).getAgent();
        } else
        if (o instanceof Collector)
        {
            a = ((Collector)o).getAgent();
        } else
        if (o instanceof DeaccessionAgent)
        {
            a = ((DeaccessionAgent)o).getAgent();
        } else
        if (o instanceof LoanAgent)
        {
            a = ((LoanAgent)o).getAgent();
        } else
        if (o instanceof FundingAgent)
        {
            a = ((FundingAgent)o).getAgent();
        } else
        if (a==null)
        {
            return null;
        }
        return a;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    @Override
    public String getString(final Object o)
    {
        Agent agent = getAgent(o);
        if (agent != null)
        {
            return DataObjFieldFormatMgr.getInstance().format(agent, "Agent");
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object, javax.swing.event.ChangeListener)
     */
    @Override
    public ImageIcon getIcon(final Object obj, final ChangeListener listener)
    {
        ImageIcon imgIcon = null;
        Agent     agent   = getAgent(obj);
        if (agent != null)
        {
            /*try
            {
                for (AgentAttachment agentAttach: agent.getAgentAttachments())
                {
                    Attachment attach = agentAttach.getAttachment();
                    String mimeType = attach.getMimeType();
                    if (mimeType==null)
                    {
                        continue;
                    }
                    if (mimeType.startsWith("image"))
                    {
                        File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(attach);
                        if (thumb != null)
                        {
                            ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
                            return IconManager.getScaledIcon(icon, IconSize.NonStd, IconSize.Std32);
                        }
                    }
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AgentIconTextMapper.class, ex);
                log.error(ex);
            }*/
            
            
            switch(agent.getAgentType())
            {
                case Agent.ORG:
                {
                    imgIcon = IconManager.getIcon("People", IconSize.Std24);
                }
                case Agent.PERSON:
                {
                    imgIcon = IconManager.getIcon("Person", IconSize.Std24);
                }
                case Agent.OTHER:
                {
                    imgIcon = IconManager.getIcon("People", IconSize.Std24);
                }
                case Agent.GROUP:
                {
                    imgIcon = IconManager.getIcon("People", IconSize.Std24);
                }
            }
            
            if (imgIcon != null && listener != null)
            {
                listener.stateChanged(new ChangeEvent(imgIcon));
            }
        }
        return imgIcon;
    }
}
