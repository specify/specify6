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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Hashtable;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAgent;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
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
        
        if (formViewObj != null && formViewObj.isEditing())
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
                roleCBX.getComboBox().addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e)
                    {
                        if (e.getStateChange() == ItemEvent.SELECTED)
                        {
                            checkForDuplicate();
                        }
                    }
                    
                });
                
                roleCBX.getComboBox().addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e)
                    {
                        //checkForDuplicate();
                    }
                });
            }
        }
    }
    
    /**
     * 
     */
    private void checkForDuplicate()
    {
        
       
        FormDataObjIFace    parentDataObj = (FormDataObjIFace)formViewObj.getParentDataObj();
        AccessionAgent accAgent   = (AccessionAgent)formViewObj.getDataObj();
        Agent          agent      = accAgent.getAgent();
        String         role       = accAgent.getRole();
        
        if (agent == null)
        {
            agent = (Agent)agentQCBX.getValue();
        }
        
        if (role == null)
        {
            role = (String)roleCBX.getValue();
        }
        
        //if (agent != null) System.out.println("\nAGENT: "+agent.getId()+"  "+(accAgent.getAgent() != null ? accAgent.getAgent().getId() : "null")+"  "+role); else System.out.println("\nAGENT: isNULL");
        
        if (parentDataObj instanceof Accession)
        {
            Accession accession = (Accession)parentDataObj;
            if (accession != null && agent != null && role != null && accession.getAccessionAgents() != null)
            {
                hash.clear();
                for (AccessionAgent aa : accession.getAccessionAgents())
                {
                    if (aa.getAgent() != null && aa.getRole() != null)
                    {
                        if (aa != accAgent)
                        {
                            String key = aa.getAgent().getId()+"_"+aa.getRole();
                            hash.put(key, true);
                        }
                    }
                }
            }
        } else
        {
            RepositoryAgreement reposAgreement = (RepositoryAgreement)parentDataObj;
            if (reposAgreement != null && agent != null && role != null && reposAgreement.getRepositoryAgreementAgents() != null)
            {
                hash.clear();
                for (AccessionAgent aa : reposAgreement.getRepositoryAgreementAgents())
                {
                    if (aa.getAgent() != null && aa.getRole() != null)
                    {
                        if (aa != accAgent)
                        {
                            String key = aa.getAgent().getId()+"_"+aa.getRole();
                            hash.put(key, true);
                        }
                    }
                }
            } 
        }
        
        String key = agent.getId() + "_" + role;
        if (hash.get(key) != null)
        {
            UIRegistry.showLocalizedError("ACCESSION_DUP_AGENTROLE", agent.getIdentityTitle(), role);
            
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    roleCBX.setValue(null, null);
                }
            });
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
