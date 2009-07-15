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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jul 15, 2009
 *
 */
public class AccessionAgentBusRules extends BaseBusRules
{
    protected Hashtable<String, Boolean> hash = new Hashtable<String, Boolean>();
    
    protected ValComboBoxFromQuery agentQCBX = null;
    protected ValComboBox          roleCBX   = null;
    
    /**
     * 
     */
    public AccessionAgentBusRules()
    {
        super(AccessionAgentBusRules.class);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(final Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            agentQCBX = formViewObj.getCompById("1");
            if (agentQCBX != null)
            {
                agentQCBX.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e)
                    {
                        if (!e.getValueIsAdjusting())
                        {
                            checkForDuplicate();
                        }
                    }
                });
            }
            
            roleCBX = formViewObj.getCompById("2");
            if (roleCBX != null)
            {
                roleCBX.getComboBox().addActionListener(new ActionListener() {
                    
                    /* (non-Javadoc)
                     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
                     */
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        checkForDuplicate();
                    }
                });
            }
        }
    }
    
    private void checkForDuplicate()
    {
        Accession  accession  = (Accession)formViewObj.getParentDataObj();
        Agent      agent      = (Agent)agentQCBX.getValue();
        String     role       = (String)roleCBX.getValue();
        
        if (accession != null && agent != null && role != null && accession.getAccessionAgents() != null)
        {
            hash.clear();
            for (AccessionAgent aa : accession.getAccessionAgents())
            {
                hash.put(aa.getAgent().getId()+"_"+aa.getRole(), true);
            }
            
            String key = agent.getId() + "_" + role;
            if (hash.get(key) != null)
            {
                UIRegistry.showError("dup");
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run()
                    {
                        roleCBX.setValue(null, null);
                    }
                });
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeDelete(Object dataObj, DataProviderSessionIFace session)
    {
        AccessionAgent accAgent = (AccessionAgent)dataObj;
        Agent          agent    = accAgent.getAgent();
        if (agent != null && session != null)
        {
            accAgent.setAgent(null);
        }
        if (accAgent.getAccession() != null)
        {
            removeById(accAgent.getAccession().getAccessionAgents(), accAgent);
            accAgent.setAccession(null);
        }
    }

}
