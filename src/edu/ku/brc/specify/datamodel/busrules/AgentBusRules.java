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
import java.awt.Window;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

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
        /*
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
        }*/
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
            
            if (field instanceof TextFieldFromPickListTable)
            {
                String title = "";
                PickListDBAdapterIFace adaptor = ((TextFieldFromPickListTable)field).getPickListAdapter();
                for (PickListItemIFace pli : adaptor.getList())
                {
                    if (pli.getValue().equals(value))
                    {
                        title = pli.getTitle();
                        break;                                
                    }
                }
                ((TextFieldFromPickListTable)field).setText(title);
                return;
            }
            
            if (field instanceof JComboBox || 
                field instanceof ValComboBox)
            {
                JComboBox cbx = field instanceof ValComboBox ? ((ValComboBox)field).getComboBox() : (JComboBox)field;
                int inx = -1;
                if (value != null)
                {
                    AbstractListModel model = (AbstractListModel)cbx.getModel();
                    for (int i=0;i<model.getSize();i++)
                    {
                        Object item = model.getElementAt(i);
                        if (item instanceof PickListItemIFace)
                        {
                            PickListItemIFace pli = (PickListItemIFace)item;
                            if (pli.getValue().equals(value))
                            {
                                inx = i;
                                break;                                
                            }
                        } else if (item.toString().equals(value))
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
                ((JTextField)field).setText(value != null ? value : "");
                
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
    {/*
        boolean isPerson = agent.getAgentType() == null || agent.getAgentType() == Agent.PERSON;
        if (!isPerson)
        {
            agent.setFirstName(null);
            agent.setMiddleInitial(null);
        }
        enableFieldAndLabel("1", isPerson, doSetOtherValues ? agent.getTitle() : null);           // Title
        enableFieldAndLabel("5", isPerson, doSetOtherValues ? agent.getFirstName() : null);       // First Name
        enableFieldAndLabel("4", isPerson, doSetOtherValues ? agent.getMiddleInitial() : null);       // First Name
        
        // Last Name
        String lbl = UIRegistry.getResourceString(isPerson ? "AG_LASTNAME" : "AG_NAME");
        lastLabel.setText(lbl + ":");
        
        // Agent Variants
        boolean useAgentVariant = AppPreferences.getRemote().getBoolean("Agent.Use.Variants."+AppContextMgr.getInstance().getClassObject(Discipline.class).getName(),
                Discipline.isCurrentDiscipline(DisciplineType.STD_DISCIPLINES.botany));
        // I don't think this is needed anymore
        //Component agentVarSep = formViewObj.getCompById("100");
        //if (agentVarSep != null)
        //{
        //    agentVarSep.setVisible(useAgentVariant);
        //}

        Component agentVarSubView = formViewObj.getCompById("10");
        if (agentVarSubView != null)
        {
            agentVarSubView.setVisible(useAgentVariant);
        }
        
        Component groupPersonSubForm = formViewObj.getCompById("31");
        if (groupPersonSubForm != null)
        {
            groupPersonSubForm.setVisible(agent.getAgentType() == Agent.GROUP);
        }*/


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
                final Component addrSubView = formViewObj.getCompById("9");

                boolean isVisible = addrSubView.isVisible();
                
                byte agentType = (byte)cbx.getSelectedIndex();
                if (agentType != Agent.PERSON)
                {
                    agent.setMiddleInitial(null);
                    agent.setFirstName(null);
                    agent.setTitle(null);
                    
                    boolean enable = agentType == Agent.ORG;
                    addrSubView.setVisible(enable);
                    
                } else
                {
                    if (!isVisible)
                    {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run()
                            {
                                Component topComp = UIHelper.getWindow(addrSubView);
                                Component topMost = UIRegistry.getTopWindow();
                                if (topComp != topMost && topComp != null)
                                {
                                    ((Window)topComp).pack();
                                }
                            }
                        });
                    }
                    addrSubView.setVisible(true);
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
    public void afterFillForm(final Object dataObjArg)
    {/*
        Object dataObj = dataObjArg;
        if (!(viewable instanceof FormViewObj) || !(dataObj instanceof Agent))
        {
            if (dataObj == null)
            {
                Component agentVarSubView = formViewObj.getCompById("10");
                if (agentVarSubView != null)
                {
                    agentVarSubView.setVisible(false);
                }
                
                Component groupPersonSubForm = formViewObj.getCompById("31");
                if (groupPersonSubForm != null)
                {
                    groupPersonSubForm.setVisible(false);
                }
                
                Component addrSubView = formViewObj.getCompById("9");
                if (addrSubView != null)
                {
                    addrSubView.setVisible(false);
                }
            }
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
                fixUpTypeCBX(typeCBX.getComboBox());
            }
            
        } else
        {
            JTextField typeTxt = (JTextField)typeComp;
            typeTxt.setText(typeTitles[agentType]);
        }
        
        boolean shouldBeVisible = agentType == Agent.PERSON || agentType == Agent.ORG;
        final Component addrSubView = formViewObj.getCompById("9");
        boolean isVisible = addrSubView.isVisible();
        if (!isVisible != shouldBeVisible)
        {
            SwingUtilities.invokeLater(new Runnable() {
                public void run()
                {
                    Component topComp = UIHelper.getWindow(addrSubView);
                    Component topMost = UIRegistry.getTopWindow();
                    if (topComp != topMost && topComp != null)
                    {
                        ((Window)topComp).pack();
                    }
                }
            });
        }
        addrSubView.setVisible(shouldBeVisible);*/
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
        
        
        if (AppContextMgr.getInstance().getClassObject(Discipline.class) != null)
        {
            Agent agent = (Agent)dataObj;
            
            Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
            discipline = session.get(Discipline.class, discipline.getId());
            
            if (!contains(agent, discipline))
            {
                agent.getDisciplines().add(discipline);
                discipline.getAgents().add(agent);
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
        
        if (agent.getDivision() == null)
        {
            agent.setDivision(AppContextMgr.getInstance().getClassObject(Division.class));
        }
        
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
        
        /*if (!contains(agent, AppContextMgr.getInstance().getClassObject(Discipline.class)))
        {
            agent.getDisciplines().add(AppContextMgr.getInstance().getClassObject(Discipline.class));
            AppContextMgr.getInstance().getClassObject(Discipline.class).getAgents().add(agent);
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
        Agent      agent      = (Agent)dataObj;
        Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
        if (discipline != null)
        {
            for (Discipline dsp : agent.getDisciplines())
            {
                if (dsp.getId().equals(discipline.getId()))
                {
                    dsp.removeReference(agent, "agents");
                    break;
                }
            }
        }
        
        return super.beforeDeleteCommit(dataObj, session);
    }     

}
