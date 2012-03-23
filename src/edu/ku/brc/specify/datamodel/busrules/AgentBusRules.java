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
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.af.ui.db.TextFieldFromPickListTable;
import edu.ku.brc.af.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.af.ui.forms.FormViewObj;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBox;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.DisciplineType;
import edu.ku.brc.specify.datamodel.Address;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.Division;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.plugins.PartialDateUI;
import edu.ku.brc.ui.CustomDialog;
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
    
    protected ArrayList<Agent> cachedAgents = new ArrayList<Agent>();
    
    /**
     * Constructor.
     */
    public AgentBusRules()
    {
        super(Agent.class);
        
        String[] typeTitleKeys = {"Agent_ORG", "Agent_PERSON", "Agent_OTHER", "Agent_GROUP"};
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
            lastNameText   = formViewObj.getCompById("3");
            
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

    /**
     * Enables/Disables both the control and the Label
     * @param id the id of the control
     * @param enabled enable it
     * @param value the value it should set
     * @param thisObj the main data object
     */
    protected void enableFieldAndLabel(final String      id, 
                                       final boolean     enabled,
                                       final String      value,
                                       final Agent       agent)
    {
        Component field  = formViewObj.getCompById(id);
        if (field != null)
        {
            field.setEnabled(enabled);
            
            if (field instanceof TextFieldFromPickListTable)
            {
                String title = "";
                PickListDBAdapterIFace adaptor = ((TextFieldFromPickListTable)field).getPickListAdapter();
                if (adaptor != null)
                {
                    for (PickListItemIFace pli : adaptor.getList())
                    {
                        if (pli.getValue().equals(value))
                        {
                            title = pli.getTitle();
                            break;                                
                        }
                    }
                    ((TextFieldFromPickListTable)field).setText(title);
                } else
                {
                    log.error("Adapter was null for id ["+id+"] on the Agent Form.");
                }
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
                
            } else if (field instanceof JTextComponent)
            {
                ((JTextComponent)field).setText(value != null ? value : "");
                
            }  else if (field instanceof PartialDateUI)
            {
                PartialDateUI plugin = (PartialDateUI)field;
                plugin.setValue(agent, null);
                
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
        if (!isPerson)
        {
            agent.setFirstName(null);
            agent.setMiddleInitial(null);
        }
        enableFieldAndLabel("1", isPerson, doSetOtherValues ? agent.getTitle() : null, agent);              // Title
        enableFieldAndLabel("5", isPerson, doSetOtherValues ? agent.getFirstName() : null, agent);          // First Name
        enableFieldAndLabel("4", isPerson, doSetOtherValues ? agent.getMiddleInitial() : null, agent);      // Middle Initial
        
        enableFieldAndLabel("19", isPerson, null, agent);                                                   // date Of Birth
        enableFieldAndLabel("20", isPerson, null, agent);                                                   // date Of Death
        enableFieldAndLabel("11", isPerson, doSetOtherValues ? agent.getGuid() :        null, agent);       // guid
        enableFieldAndLabel("12", isPerson, doSetOtherValues ? agent.getInitials() :    null, agent);       // initials
        enableFieldAndLabel("13", isPerson, doSetOtherValues ? agent.getInterests() :   null, agent);       // interests
        enableFieldAndLabel("14", isPerson, doSetOtherValues ? agent.getJobTitle() :    null, agent);       // jobTitle
        
        enableFieldAndLabel("16", true, doSetOtherValues ? agent.getRemarks() :   null, agent);             // remarks
        enableFieldAndLabel("17", true, doSetOtherValues ? agent.getUrl() :   null, agent);                 // url
        
        // Last Name
        String lbl = UIRegistry.getResourceString(isPerson ? "AG_LASTNAME" : "AG_NAME");
        lastLabel.setText(lbl + ":");
        
        // Agent Variants
        boolean useAgentVariant = AppPreferences.getRemote().getBoolean("Agent.Use.Variants."+AppContextMgr.getInstance().getClassObject(Discipline.class).getType(),
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
        }
    }
    
    /**
     * Clears the values and hides somAttachmentOwnere UI depending on what type is selected
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
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterSaveCommit(java.lang.Object, edu.ku.brc.dbsupport.DataProviderSessionIFace)
     */
    @Override
    public boolean afterSaveCommit(final Object dataObj, final DataProviderSessionIFace session)
    {
        setLSID((FormDataObjIFace)dataObj);
        

        if (cachedAgents.size() > 0)
        {
            try
            {
                session.beginTransaction();
                for (Agent agent : cachedAgents)
                {
                    agent = session.merge(agent);
                    session.saveOrUpdate(agent);
                }
                session.commit();
                
            } catch (Exception e)
            {
                e.printStackTrace();
                session.rollback();
            } finally
            {
                cachedAgents.clear();
            }
        }

        return super.afterSaveCommit(dataObj, session);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
     */
    @Override
    public void afterFillForm(final Object dataObjArg)
    {
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
            if (typeTxt != null)
            {
                typeTxt.setText(typeTitles[agentType]);
            }
        }
        
        boolean shouldBeVisible = agentType == Agent.PERSON || agentType == Agent.ORG;
        final Component addrSubView = formViewObj.getCompById("9");
        if (addrSubView != null)
        {
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
            addrSubView.setVisible(shouldBeVisible);
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
                if (isOK && ((Agent)dbObj).getSpecifyUser() != null)
                {
                    DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(SpecifyUser.getClassTableId());
                    reasonList.add(ti.getTitle());
                    isOK = false;
                }
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
    @SuppressWarnings("unchecked")
    @Override
    public void beforeSave(final Object dataObj, final DataProviderSessionIFace session)
    {
        super.beforeSave(dataObj, session);
        
        if (AppContextMgr.getInstance().getClassObject(Discipline.class) != null)
        {
            Agent agent = (Agent)dataObj;
            
            Discipline discipline = AppContextMgr.getInstance().getClassObject(Discipline.class);
            discipline = session.get(Discipline.class, discipline.getId());
            
            if (agent.getId() != null && agent.getSpecifyUser() != null)
            {
                DataProviderSessionIFace tmpSession = null;
                try
                {
                    cachedAgents.clear();
                    tmpSession = DataProviderFactory.getInstance().createSession();
                    
                    String hql = String.format("SELECT a FROM Agent AS a INNER JOIN a.specifyUser AS su WHERE su.id = %d AND a.id <> %d", agent.getSpecifyUser().getId(), agent.getId());
                    log.debug(hql);
                    List<?> agents = tmpSession.getDataList(hql);
                    for (Object agtObj : agents)
                    {
                        Agent agt = (Agent)agtObj;
                        
                        Agent dupAgent = (Agent)agent.clone();
                        
                        dupAgent.setAgentId(agt.getId());
                        dupAgent.setVersion(agt.getVersion());
                        dupAgent.setDivision(agt.getDivision());
                        
                        cachedAgents.add(dupAgent);
                        
                        System.out.println(agt.getId() + agt.getLastName());
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    ex.printStackTrace();
                } finally
                {
                    if (tmpSession != null)
                    {
                        tmpSession.close();
                    }
                }
            }
        }
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
    
    /**
     * @param specifyUser
     * @param division
     * @return
     */
    public static boolean createUserAgent(final SpecifyUser specifyUser, 
                                          final Division    division)
    {
        final JTextField fName = UIHelper.createTextField(20);
        final JTextField lName = UIHelper.createTextField();
        final JTextField email = UIHelper.createTextField();
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,2px,f:p:g", "p,4px,p,4px,p,4px"));
        pb.add(UIHelper.createFormLabel("First Name"), cc.xy(1, 1));
        pb.add(UIHelper.createFormLabel("Last Name"),  cc.xy(1, 3));
        pb.add(UIHelper.createFormLabel("Email"),      cc.xy(1, 5));
        
        pb.add(fName, cc.xy(3, 1));
        pb.add(lName, cc.xy(3, 3));
        pb.add(email, cc.xy(3, 5));
        
        pb.setDefaultDialogBorder();
        CustomDialog dlg = new CustomDialog((Dialog)null, "Create Agent", true, CustomDialog.OKCANCEL, pb.getPanel());   
        UIHelper.centerAndShow(dlg);
        
        boolean hasData = StringUtils.isNotEmpty(lName.getText()) && StringUtils.isNotEmpty(fName.getText());
        if (hasData && !dlg.isCancelled())
        {
            Agent usrAgent = new Agent();
            usrAgent.initialize();
            
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                session.beginTransaction();
                SpecifyUser spUser = session.merge(specifyUser);
                usrAgent.setLastName(lName.getText());
                usrAgent.setFirstName(fName.getText());
                usrAgent.setEmail(email.getText());
                
                usrAgent.setSpecifyUser(spUser);
                spUser.getAgents().add(usrAgent);
                usrAgent.setDivision(division); // Set the new Division
                
                session.saveOrUpdate(usrAgent);
                session.saveOrUpdate(spUser);
                session.commit();
                
                return true;
                
            } catch (Exception ex)
            {
                if (session != null)
                {
                    session.rollback();
                }
            } finally
            {
                if (session != null) session.close();
            }
        }
        return false;
    }

}
