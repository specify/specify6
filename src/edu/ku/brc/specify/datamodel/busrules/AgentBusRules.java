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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.ui.forms.BusinessRulesOkDeleteIFace;
import edu.ku.brc.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.forms.FormViewObj;
import edu.ku.brc.ui.forms.MultiView;
import edu.ku.brc.ui.forms.Viewable;
import edu.ku.brc.ui.forms.validation.ValComboBox;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 24, 2007
 *
 */
public class AgentBusRules extends AttachmentOwnerBaseBusRules
{
    protected Hashtable<FormViewObj, Agent> formToAgentHash = new Hashtable<FormViewObj, Agent>();
    
    public AgentBusRules()
    {
        super(Agent.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.BaseBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToEnableDelete(Object dataObj)
    {
        return true;
    }
    
    protected void enableFieldAndLabel(final FormViewObj fvo, 
                                       final String      id, 
                                       final boolean     enabled,
                                       final String      value)
    {
        Component field  = fvo.getCompById(id);
        if (field != null)
        {
            field.setEnabled(enabled);
            if (value != null)
            {
                if (field instanceof JComboBox || field instanceof ValComboBox)
                {
                    JComboBox cbx = field instanceof ValComboBox ? ((ValComboBox)field).getComboBox() : (JComboBox)field;
                    DefaultComboBoxModel model = (DefaultComboBoxModel)cbx.getModel();
                    int inx = -1;
                    for (int i=0;i<model.getSize();i++)
                    {
                        if (model.getElementAt(i).equals(value))
                        {
                            inx = i;
                            break;
                        }
                    }
                    cbx.setSelectedIndex(inx);
                    
                } else if (field instanceof JTextField)
                {
                    ((JTextField)field).setText(value);
                    
                } else
                {
                    log.debug("******** unhandled component type: "+field);
                }
            }
            JLabel label = fvo.getLabelFor(field);
            if (label != null)
            {
                label.setEnabled(enabled);
            }
        }
    }
    
    protected void fixUpFormForAgentType(final FormViewObj fvo, 
                                         final Agent       agent,
                                         final boolean     doSetOtherValues)
    {
        boolean isPerson = agent.getAgentType() == null || agent.getAgentType() == Agent.PERSON;
        enableFieldAndLabel(fvo, "1", isPerson, doSetOtherValues ? agent.getTitle() : null);           // Title
        enableFieldAndLabel(fvo, "5", isPerson, doSetOtherValues ? agent.getFirstName() : null);       // First Name
        
        // First Name or Name
        JLabel label = fvo.getLabelFor("3");
        if (label != null)
        {
            label.setText((isPerson ? "Last Name" : "Name") + ":"); // I18N
            
            Component c = fvo.getCompById("3");
            if (c instanceof JTextField)
            {
                ((JTextField)fvo.getCompById("3")).setText(agent.getLastName());
            }
        } 
        
        // Middle Name or Abbrev
        label = fvo.getLabelFor("4");
        if (label != null)
        {
            label.setText((isPerson ? "Middle Initial" : "Abbrev") + ":"); // I18N
            
            Component c = fvo.getCompById("4");
            if (c instanceof JTextField)
            {
                ((JTextField)fvo.getCompById("4")).setText(isPerson ? agent.getMiddleInitial() : agent.getAbbreviation());
            }
        } 
    }
    
    protected void fixUpTypeCBX(final JComboBox cbx)
    {
        Component parent = cbx.getParent();
        while (!(parent instanceof MultiView))
        {
            parent = parent.getParent();
        }
        
        if (parent instanceof MultiView)
        {
            FormViewObj fvo = ((MultiView)parent).getCurrentViewAsFormViewObj();
            if (fvo != null)
            {
                Agent agent = (Agent)fvo.getDataObj();
                byte agentType = (byte)cbx.getSelectedIndex();
                if (agentType != Agent.PERSON)
                {
                    agent.setMiddleInitial("");
                    agent.setFirstName("");
                    agent.setTitle("");
                }
                agent.setAgentType(agentType);
                fixUpFormForAgentType(fvo, agent, true);
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BaseBusRules#afterFillForm(java.lang.Object, edu.ku.brc.ui.forms.Viewable)
     */
    @Override
    public void afterFillForm(final Object dataObj, final Viewable viewable)
    {
        
        if (!(viewable instanceof FormViewObj) || !(dataObj instanceof Agent))
        {
            return;
        }
        
        Agent agent = (Agent)dataObj;
        
        FormViewObj fvo = (FormViewObj)viewable;
        
        fixUpFormForAgentType(fvo, agent, false);
        
        Byte agentType = agent.getAgentType();
        Component typeComp = fvo.getCompById("0");
        if (typeComp instanceof ValComboBox)
        {
            ValComboBox typeCBX = (ValComboBox)typeComp;
            if (typeCBX != null)
            {
                if (typeCBX.getComboBox().getModel().getSize() == 0)
                {
                    String[] types = {"Organization", "Person", "Other", "Group"};
                    for (String t : types)
                    {
                        typeCBX.getComboBox().addItem(t);
                    }
                }
                typeCBX.getComboBox().setSelectedIndex(agentType == null ? Agent.PERSON : agentType);
                
                typeCBX.getComboBox().addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e)
                    {
                        fixUpTypeCBX((JComboBox)e.getSource());
                    }
                });
            }
            
        } else
        {
            JTextField typeTxt = (JTextField)typeComp;
            String txt = "";
            switch (agentType)
            {
                case Agent.ORG :
                    txt = "Organization";
                    break;
                    
                case Agent.PERSON :
                    txt = "Person";
                    break;
                    
                case Agent.OTHER :
                    txt = "Other";
                    break;
                    
                case Agent.GROUP :
                    txt = "Group";
                    break;
            }
            typeTxt.setText(txt);
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
                isOK = false;
                
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
            //Discipline.getCurrentDiscipline().addReference((Agent)dataObj, "agents");
            ((Agent)dataObj).setDiscipline(Discipline.getCurrentDiscipline());
        }
    }
    
    

}
