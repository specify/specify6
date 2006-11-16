/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.specify.ui;

import java.io.File;

import javax.swing.ImageIcon;

import edu.ku.brc.specify.datamodel.AccessionAgents;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.BorrowAgents;
import edu.ku.brc.specify.datamodel.Collectors;
import edu.ku.brc.specify.datamodel.DeaccessionAgents;
import edu.ku.brc.specify.datamodel.LoanAgents;
import edu.ku.brc.ui.IconManager;
import edu.ku.brc.ui.IconManager.IconSize;
import edu.ku.brc.util.AttachmentUtils;

/**
 * An icon mapper for serving up icons appropriate to the agent type for all
 * agent related records.
 *
 * @code_status Complete
 * @author jstewart
 */
public class AgentIconMapper implements ObjectIconMapper
{
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getIcon(java.lang.Object)
     */
    public ImageIcon getIcon(Object o)
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
        if (o instanceof AccessionAgents)
        {
            a = ((AccessionAgents)o).getAgent();
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

        for (Attachment attach: a.getAttachments())
        {
            if (attach.getMimeType().startsWith("image"))
            {
                File thumb = AttachmentUtils.getAttachmentManager().getThumbnail(attach);
                if (thumb != null)
                {
                    ImageIcon icon = new ImageIcon(thumb.getAbsolutePath());
                    return IconManager.getScaledIcon(icon, IconSize.NonStd, IconSize.Std24);
                }
            }
        }
        
        switch(a.getAgentType())
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
        
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.ObjectIconMapper#getMappedClasses()
     */
    public Class[] getMappedClasses()
    {
        Class[] mappedClasses = new Class[6];
        mappedClasses[0] = Agent.class;
        mappedClasses[1] = BorrowAgents.class;
        mappedClasses[2] = AccessionAgents.class;
        mappedClasses[3] = Collectors.class;
        mappedClasses[4] = DeaccessionAgents.class;
        mappedClasses[5] = LoanAgents.class;
        return mappedClasses;
    }
}
