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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * This alters the UI depending on which type of agent is set.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Jan 24, 2007
 *
 */
public class AgentBusRules extends AttachmentOwnerBaseBusRules
{
    protected Hashtable<FormViewObj, Agent> formToAgentHash = new Hashtable<FormViewObj, Agent>();
    
    protected Component    typeComp  = null;
    protected boolean      ignoreSet = false;
    
    protected JLabel       lastLabel;
    protected JTextField   lastNameText;
    
    protected String[]     typeTitles;
    
    /**
     * Constructor.
     */
    public AgentBusRules()
    {
        super(Agent.class);
        
        String[] typeTitleKeys = {"AG_ORG", "AG_PERSON", "AG_OTHER", "AG_GROUP"};
        typeTitles = new String[typeTitleKeys.length];
        int i = 0;
        for (String key : typeTitleKeys)
        {
            typeTitles[i++] = UIRegistry.getResourceString(key);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#initialize(edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null)
        {
            typeComp       = formViewObj.getCompById("0");
            lastLabel      = formViewObj.getLabelFor("3");
            lastNameText   = (JTextField)formViewObj.getCompById("3");
            
            if (typeComp instanceof ValComboBox)
            {
                ValComboBox typeCBX = ((ValComboBox)typeComp);
                typeCBX.getComboBox().addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        if (!ignoreSet)
                        {
                            fixUpTypeCBX((JComboBox)e.getSource());
                        }
                    }
                });
                
                // Fill Type CBX with localized strings
                if (typeCBX.getComboBox().getModel().getSize() == 0)
                {
                    for (String t : typeTitles)
                    {
                        typeCBX.getComboBox().addItem(t);
                    }
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#formShutdown()
     */
    @Override
    public void formShutdown()
    {
        super.formShutdown();
        
        typeComp       = null;
        lastLabel      = null;
        lastNameText   = null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
    
    /**
     * Enables/Disables both the control and the Label
     * @param id the id of the control
     * @param enabled enable it
     * @param value the value it should set
     */
    protected void enableFieldAndLabel(final String      id, 
                                       final boolean     enabled,
                                       final String      value)
    {
        Component field  = formViewObj.getCompById(id);
        if (field != null)
        {
            field.setEnabled(enabled);
            
            if (field instanceof JComboBox || field instanceof ValComboBox)
            {
                JComboBox cbx = field instanceof ValComboBox ? ((ValComboBox)field).getComboBox() : (JComboBox)field;
                int inx = -1;
                if (value != null)
                {
                    AbstractListModel model = (AbstractListModel)cbx.getModel();
                    for (int i=0;i<model.getSize();i++)
                    {
                        Object item = model.getElementAt(i);
                        if (item.toString().equals(value))
                        {
                            inx = i;
                            break;
                        }
                    }
                }
                //System.err.println("AgentBusRules - id "+id+" setting to "+inx);
                cbx.setSelectedIndex(inx);
                
            } else if (field instanceof JTextField)
            {
                if (value != null)
                {
                    ((JTextField)field).setText(value);
                }
                
            } else
            {
                log.debug("******** unhandled component type: "+field);
            }
            JLabel label = formViewObj.getLabelFor(field);
            if (label != null)
            {
                label.setEnabled(enabled);
            }
        }
    }
    
    /**
     * Fix up labels in UI per the type of Agent
     * @param agent the current agent
     * @param doSetOtherValues indicates it should set values
     */
    protected void fixUpFormForAgentType(final Agent   agent,
                                         final boolean doSetOtherValues)
    {
        boolean isPerson = agent.getAgentType() == null || agent.getAgentType() == Agent.PERSON;
        
        enableFieldAndLabel("1", isPerson, doSetOtherValues ? agent.getTitle() : null);           // Title
        enableFieldAndLabel("5", isPerson, doSetOtherValues ? agent.getFirstName() : null);       // First Name
        enableFieldAndLabel("4", isPerson, doSetOtherValues ? agent.getMiddleInitial() : null);       // First Name
        
        // Last Name
        String lbl = UIRegistry.getResourceString(isPerson ? "AG_LASTNAME" : "AG_NAME");
        lastLabel.setText(lbl + ":");
        
        // Agent Variants
        boolean useAgentVariant = AppPreferences.getRemote().getBoolean("Agent.Use.Variants."+Discipline.getCurrentDiscipline().getName(),
                Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.botany));
        Component agentVarSep = formViewObj.getCompById("100");
        if (agentVarSep != null)
        {
            agentVarSep.setVisible(useAgentVariant);
        }

        Component agentVarSubView = formViewObj.getCompById("10");
        if (agentVarSubView != null)
        {
            agentVarSubView.setVisible(useAgentVariant);
        }

    }
    
    /**
     * Clears the values and hides some UI depending on what type is selected
     * @param cbx the type cbx
     */
    protected void fixUpTypeCBX(final JComboBox cbx)
    {
        if (formViewObj != null)
        {
            Agent agent = (Agent)formViewObj.getDataObj();
            if (agent != null)
            {
                Component addrSubView = formViewObj.getCompById("9");
                Component addrSep     = formViewObj.getCompById("99");

                byte agentType = (byte)cbx.getSelectedIndex();
                if (agentType != Agent.PERSON)
                {
                    agent.setMiddleInitial(null);
                    agent.setFirstName(null);
                    agent.setTitle(null);
                    
                    boolean enable = agentType == Agent.ORG;
                    addrSubView.setVisible(enable);
                    addrSep.setVisible(enable);
                    
                } else
                {
                    addrSubView.setVisible(true);
                    addrSep.setVisible(true);
                }
                agent.setAgentType(agentType);
                fixUpFormForAgentType(agent, true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObj)
    {
        if (!(viewable instanceof FormViewObj) || !(dataObj instanceof Agent))
        {
            return;
        }
        
        Agent agent     = (Agent)dataObj;
        Byte  agentType = agent.getAgentType();
        
        fixUpFormForAgentType(agent, true);
        
        if (typeComp instanceof ValComboBox)
        {
            ValComboBox typeCBX = (ValComboBox)typeComp;
            if (typeCBX != null)
            {
                ignoreSet = true;
                typeCBX.getComboBox().setSelectedIndex(agentType == null ? Agent.PERSON : agentType);
                ignoreSet = false;
            }
            
        } else
        {
            JTextField typeTxt = (JTextField)typeComp;
            typeTxt.setText(typeTitles[agentType]);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace, edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace)
     */
    @Override
    public void okToDelete(final Object dataObj,
                           final DataProviderSessionIFace session,
                           final BusinessRulesOkDeleteIFace deletable)
    {
        reasonList.clear();
        
        boolean isOK = false;
        if (deletable != null)
        {
            FormDataObjIFace dbObj = (FormDataObjIFace)dataObj;
            
            Integer id = dbObj.getId();
            if (id == null)
            {
                isOK = true;
                
            } else
            {
                DBTableInfo tableInfo      = DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId());
                String[]    tableFieldList = gatherTableFieldsForDelete(new String[] {"agent", "address", "agentvariant"}, tableInfo);
                isOK = okToDelete(tableFieldList, dbObj.getId());
            }
            deletable.doDeleteDataObj(dataObj, session, isOK);
            
        } else
        {
            super.okToDelete(dataObj, session, deletable);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSave(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        
        if (Discipline.getCurrentDiscipline() != null)
        {
            Agent agent = (Agent)dataObj;
            
            if (!contains(agent, Discipline.getCurrentDiscipline()))
            {
                agent.getDisciplines().add(Discipline.getCurrentDiscipline());
                Discipline.getCurrentDiscipline().getAgents().add(agent);
            }
        }
    }
    
    /**
     * Checks to see if the agent has already been added to the Discipline.
     * @param agent the agent being saved
     * @param discipline the discipline it is being saved into
     * @return true if it is already associated with the Discipline
     */
    protected boolean contains(final Agent agent, final Discipline discipline)
    {
        
        for (Discipline d : agent.getDisciplines())
        {
            if (d.getDisciplineId().equals(discipline.getDisciplineId()))
            {
                return true;
            }
        }
        return false;
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#beforeMerge(edu.ku.brc.ui.forms.Viewable, java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public void beforeMerge(final Object dataObj, final DataProviderSessionIFace session)
    {
        super.beforeMerge(dataObj, session);
        
        Agent agent = (Agent)dataObj;
        
        //session.attach(agent);
        
        if (agent.getAgentType() == null)
        {
            if (typeComp instanceof ValComboBox)
            {
                JComboBox cbx = ((ValComboBox)typeComp).getComboBox();
                byte agentType = (byte)cbx.getSelectedIndex();
                if (agentType == -1)
                {
                    agentType = Agent.PERSON;
                }
                agent.setAgentType(agentType);
            }
        }
        
        /*if (!contains(agent, Discipline.getCurrentDiscipline()))
        {
            agent.getDisciplines().add(Discipline.getCurrentDiscipline());
            Discipline.getCurrentDiscipline().getAgents().add(agent);
        }*/
    }
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeSaveCommit(Object dataObj, DataProviderSessionIFace session) throws Exception
    {
        Agent agent     = (Agent)dataObj;
        byte  agentType = agent.getAgentType();
        
        if (agent.getAddresses().size() > 0 &&
            (agentType == Agent.OTHER || agentType == Agent.GROUP))
        {
            for (Address addr : new ArrayList<Address>(agent.getAddresses()))
            {
                agent.removeReference(addr, "addresses");
                session.delete(addr);
            }
        }
        
        return super.beforeSaveCommit(dataObj, session);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.AttachmentOwnerBaseBusRules#beforeDeleteCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean beforeDeleteCommit(Object dataObj, DataProviderSessionIFace session)
            throws Exception
    {
        Agent agent = (Agent)dataObj;
        if (Discipline.getCurrentDiscipline() != null)
        {
            agent.getDisciplines().remove(Discipline.getCurrentDiscipline());
            Discipline.getCurrentDiscipline().getAgents().remove(agent);
        }
        
        return super.beforeDeleteCommit(dataObj, session);
    }     

}
