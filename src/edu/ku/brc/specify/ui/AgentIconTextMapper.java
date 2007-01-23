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
package edu.ku.brc.specify.ui;

import java.io.File;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.BorrowAgents;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.DeaccessionAgents;
import edu.ku.brc.specify.datamodel.LoanAgents;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.ui.forms.formatters.DataObjFieldFormatMgr;
import edu.ku.brc.util.AttachmentUtils;

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
    private static final Logger log = Logger.getLogger(AgentIconTextMapper.class);
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
        Class[] mappedClasses = new Class[6];
        mappedClasses[0] = Agent.class;
        mappedClasses[1] = BorrowAgents.class;
        mappedClasses[2] = AccessionAgent.class;
        mappedClasses[3] = Collectors.class;
        mappedClasses[4] = DeaccessionAgents.class;
        mappedClasses[5] = LoanAgents.class;
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
        }
        if (o instanceof BorrowAgents)
        {
            a = ((BorrowAgents)o).getAgent();
        }
        if (o instanceof AccessionAgent)
        {
            a = ((AccessionAgent)o).getAgent();
        }
        if (o instanceof Collectors)
        {
            a = ((Collectors)o).getAgent();
        }
        if (o instanceof DeaccessionAgents)
        {
            a = ((DeaccessionAgents)o).getAgent();
        }
        if (o instanceof LoanAgents)
        {
            a = ((LoanAgents)o).getAgent();
        }
        if (a==null)
        {
            return null;
        }
        return a;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectTextMapper#getString(java.lang.Object)
     */
    public String getString(Object o)
    {
        Agent agent = getAgent(o);
        if (agent != null)
        {
            DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
            try
            {
                tmpSession.attach(agent);

            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally 
            {
                tmpSession.close();     
            }

            return DataObjFieldFormatMgr.format(agent, "Agent");
            
        }
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
    {
        Agent agent = getAgent(o);
        if (agent != null)
        {
            DataProviderSessionIFace tmpSession = DataProviderFactory.getInstance().createSession();
            try
            {
                tmpSession.attach(agent);
                for (Attachment attach: agent.getAttachments())
                {
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
                log.error(ex);
                
            } finally 
            {
                tmpSession.close();     
            }
           
            
            switch(agent.getAgentType())
            {
                case Agent.ORG:
                {
                    return IconManager.getIcon("People", IconSize.Std24);
                }
                case Agent.PERSON:
                {
                    return IconManager.getIcon("Person", IconSize.Std24);
                }
                case Agent.OTHER:
                {
                    return IconManager.getIcon("People", IconSize.Std24);
                }
                case Agent.GROUP:
                {
                    return IconManager.getIcon("People", IconSize.Std24);
                }
            }
        }
        return null;
    }
}
