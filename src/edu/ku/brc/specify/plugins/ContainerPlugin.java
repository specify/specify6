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
package edu.ku.brc.specify.plugins;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.db.TextFieldWithInfo;
import edu.ku.brc.af.ui.forms.ViewFactory;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Feb 28, 2011
 *
 */
public class ContainerPlugin extends UIPluginBase implements UIValidatable
{
    private Container                associatedContainer    = null;
    private Container                parentContainer        = null;
    private CollectionObject         collectionObject       = null;
    
    private UIFieldFormatterIFace    catalogNumberFormatter = null;
    
    protected ValComboBoxFromQuery   parentContainerCBX     = null;   // This is used when a Container is child
    protected ValComboBoxFromQuery   associatedContainerCBX = null;   // This is used when a container is cataloged
    
    protected TextFieldWithInfo      parentContainerTXTInfo     = null;   // This is used when a Container is child
    protected TextFieldWithInfo      associatedContainerTXTInfo = null;   // This is used when a container is cataloged
    
    protected JLabel                 labelParentCon         = null;
    protected JLabel                 labelAssocCon          = null;
    protected String                 strParentCon           = null;
    protected String                 strAssocCon            = null;

    
    protected CardLayout             cardLayout             = new CardLayout();
    protected JPanel                 cardPanel;
    
    protected Boolean                doingParent            = null;
    protected boolean                isRequired             = false;
    
    protected ButtonGroup            group                  = new ButtonGroup();
    protected JRadioButton           isParentContainerRB    = null;
    protected JRadioButton           isAssociatedRB         = null;
    
    protected String                 errorMsg               = null;
    
    /**
     * 
     */
    public ContainerPlugin()
    {
        super();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#initialize(java.util.Properties, boolean)
     */
    @Override
    public void initialize(Properties propertiesArg, boolean isViewModeArg)
    {
        super.initialize(propertiesArg, isViewModeArg);
        
        DBTableInfo     conTI = DBTableIdMgr.getInstance().getInfoById(Container.getClassTableId());
        CellConstraints cc    = new CellConstraints();
        cardPanel = new JPanel(cardLayout);
        
        UIRegistry.loadAndPushResourceBundle("specify_plugins");
        
        strParentCon = UIRegistry.getResourceString("PARENT_CONTAINER");
        strAssocCon  = UIRegistry.getResourceString("CONTAINER");
        errorMsg     = UIRegistry.getResourceString("ASSOC_CONTR_INUSE");
        
        UIRegistry.popResourceBundle();

        if (isViewModeArg)
        {
            labelParentCon = UIHelper.createLabel(strParentCon);
            labelAssocCon  = UIHelper.createLabel(strAssocCon);
            
            PanelBuilder pbParent = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p"));
            pbParent.add(labelParentCon, cc.xy(1,1));
     
            PanelBuilder pbAssoc = new PanelBuilder(new FormLayout("f:p:g", "p,2px,p"));
            pbAssoc.add(labelAssocCon, cc.xy(1,1));
            
            parentContainerTXTInfo = new TextFieldWithInfo(conTI.getClassName(),
                                                            "name",
                                                            "name",
                                                            null,               // format
                                                            null,               // uiFieldFormatterName
                                                            null,               // dataObjFormatterName
                                                            "ContainerDisplay", // displayInfoDialogName
                                                            "");                // objTitle
            
            associatedContainerTXTInfo = new TextFieldWithInfo(conTI.getClassName(),
                                                                "name",
                                                                "name",
                                                                null,               // format
                                                                null,               // uiFieldFormatterName
                                                                null,               // dataObjFormatterName
                                                                "ContainerDisplay", // displayInfoDialogName
                                                                "");                // objTitle
           
            ViewFactory.changeTextFieldUIForDisplay(parentContainerTXTInfo.getTextField(), false);
            ViewFactory.changeTextFieldUIForDisplay(associatedContainerTXTInfo.getTextField(), false);
            
            pbParent.add(parentContainerTXTInfo,    cc.xy(1,3));
            pbAssoc.add(associatedContainerTXTInfo, cc.xy(1,3));
            
            cardPanel.add(Integer.toString(parentContainerTXTInfo.hashCode()),     pbParent.getPanel());
            cardPanel.add(Integer.toString(associatedContainerTXTInfo.hashCode()), pbAssoc.getPanel());

            setLayout(new BorderLayout());
            add(cardPanel, BorderLayout.CENTER);
            
        } else
        {
            isParentContainerRB = new JRadioButton(strParentCon);
            isAssociatedRB      = new JRadioButton(strAssocCon);
            group.add(isParentContainerRB);
            group.add(isAssociatedRB);
            
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p"));
            pb.add(isParentContainerRB, cc.xy(1,1));
            pb.add(isAssociatedRB,      cc.xy(1,3));
            
            catalogNumberFormatter = DBTableIdMgr.getFieldFormatterFor(CollectionObject.class, "CatalogNumber");
            
            int btnOpts = ValComboBoxFromQuery.CREATE_EDIT_BTN | ValComboBoxFromQuery.CREATE_NEW_BTN | ValComboBoxFromQuery.CREATE_SEARCH_BTN;
            parentContainerCBX = new ValComboBoxFromQuery(conTI,
                                                            "name",
                                                            "name",
                                                            "name",
                                                            null,
                                                            "name",
                                                            null,
                                                            "",
                                                            null, // helpContext
                                                            btnOpts);
            
            associatedContainerCBX = new ValComboBoxFromQuery(conTI,
                    "name",
                    "name",
                    "name",
                    null,
                    "name",
                    null,
                    "",
                    null, // helpContext
                    btnOpts);
            
            PanelBuilder pbParent = new PanelBuilder(new FormLayout("f:p:g", "p"));
            pbParent.add(parentContainerCBX, cc.xy(1,1));
     
            PanelBuilder pbAssoc = new PanelBuilder(new FormLayout("f:p:g", "p"));
            pbAssoc.add(associatedContainerCBX, cc.xy(1,1));
     
            setOpaque(false);
            cardPanel.setOpaque(false);
            pbParent.getPanel().setOpaque(false);
            pbAssoc.getPanel().setOpaque(false);
            isParentContainerRB.setOpaque(false);
            isAssociatedRB.setOpaque(false);
            pb.getPanel().setOpaque(false);
            
            
            cardPanel.add(Integer.toString(isParentContainerRB.hashCode()), pbParent.getPanel());
            cardPanel.add(Integer.toString(isAssociatedRB.hashCode()),     pbAssoc.getPanel());
    
            PanelBuilder parent = new PanelBuilder(new FormLayout("f:p:g", "p,4px,p"), this);
            parent.add(pb.getPanel(), cc.xy(1,1));
            parent.add(cardPanel,     cc.xy(1,3));
            
            setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    
            associatedContainerCBX.addListSelectionListener(new ListSelectionListener()
            {
                public void valueChanged(ListSelectionEvent e)
                {
                    itemSelected();
                }
            });
    
            
            ActionListener rbAction = new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    JRadioButton rb = (JRadioButton)e.getSource();
                    if (rb.isSelected())
                    {
                        boolean oldDoingParent = doingParent;
                        cardLayout.show(cardPanel, Integer.toString(rb.hashCode()));
                        doingParent = rb.hashCode() == isParentContainerRB.hashCode();
                        
                        if (doingParent != oldDoingParent)
                        {
                            parentContainer     = null;
                            associatedContainer = null;
                            parentContainerCBX.setValue(null, null);
                            associatedContainerCBX.setValue(null, null);
                        }
                    }
                    ContainerPlugin.this.notifyChangeListeners(new ChangeEvent(rb));
                }
            };

            isParentContainerRB.addActionListener(rbAction);
            isAssociatedRB.addActionListener(rbAction);
            
            isParentContainerRB.setSelected(true);
        }
    }
    
    /**
     * 
     */
    private void ensureCBXs()
    {
        if (collectionObject != null)
        {
            parentContainer     = collectionObject.getContainerOwner();
            associatedContainer = collectionObject.getContainer();
            doingParent         = associatedContainer == null;
        } else
        {
            parentContainer     = null;
            associatedContainer = null;
            doingParent         = true;
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#setValue(java.lang.Object, java.lang.String)
     */
    @Override
    public void setValue(final Object value, final String defaultValue)
    {
        if (value != null && value instanceof CollectionObject)
        {
            collectionObject = (CollectionObject)value;
            
            ensureCBXs();
            
            if (isViewMode)
            {
                parentContainerTXTInfo.setValue(parentContainer, null);
                associatedContainerTXTInfo.setValue(associatedContainer, null);
                if (doingParent != null)
                {
                    Component comp = doingParent ? parentContainerTXTInfo : associatedContainerTXTInfo;
                    cardLayout.show(cardPanel, Integer.toString(comp.hashCode()));
                }
                
            } else
            {
                if (parentContainerCBX != null)
                {
                    parentContainerCBX.setValue(parentContainer, null);
                }
                if (associatedContainerCBX != null)
                {
                    associatedContainerCBX.setValue(associatedContainer, null);
                }
                
                if (doingParent != null)
                {
                    (doingParent ? isParentContainerRB : isAssociatedRB).setSelected(true);
                }
            }
        }
        
        if (isViewMode)
        {
            if (parentContainer == null && associatedContainer == null)
            {
                cardLayout.show(cardPanel, Integer.toString(parentContainerTXTInfo.hashCode()));
                labelParentCon.setText("       ");
                
            } else
            {
                labelParentCon.setText(strParentCon);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        if (doingParent != null)
        {
            if (doingParent)
            {
                collectionObject.setContainerOwner((Container)parentContainerCBX.getValue());
                collectionObject.setContainer(null);
            } else
            {
                collectionObject.setContainer((Container)associatedContainerCBX.getValue());
                collectionObject.setContainerOwner(null);
            }
        }
        return collectionObject;
    }

    /**
     * @return
     */
    private ValComboBoxFromQuery getCBX()
    {
        return doingParent == null ? null : doingParent ? parentContainerCBX : associatedContainerCBX;
    }
    
    /**
     * 
     */
    protected void itemSelected()
    {
        final ValComboBoxFromQuery qcbx = getCBX();
        
        Integer selectedId = qcbx.getTextWithQuery().getSelectedId();
        if (selectedId != null)
        {
            String sql  = String.format("SELECT cn.Name, co.CatalogNumber FROM container AS cn " +
		                                "Inner Join collectionobject AS co ON cn.ContainerID = co.ContainerID " +
		                                "WHERE co.ContainerID = %d", selectedId);
            Vector<Object[]> data = BasicSQLUtils.query(sql);
            if (data.size() > 0)
            {
                final Object[] row = data.get(0);
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        qcbx.setValue(null, null);
                        String conName   = (String)row[0];
                        String catNumber = (String)catalogNumberFormatter.formatToUI(row[1]);
                        UIRegistry.showError(String.format(errorMsg, conName, catNumber));
                    }
                });
            }
        }
        notifyChangeListeners(new ChangeEvent(this));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#isNotEmpty()
     */
    @Override
    public boolean isNotEmpty()
    {
        ValComboBoxFromQuery qcbx = doingParent ? parentContainerCBX : associatedContainerCBX;
        return qcbx.getValue() != null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.UIPluginable#getFieldNames()
     */
    @Override
    public String[] getFieldNames()
    {
        return new String[] {"container", "containerOwner"};
    }

    //---------------------------------------------------------------------------------------------
    //-- edu.ku.brc.af.ui.forms.UIPluginable
    //---------------------------------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        if (parentContainerCBX != null)
        {
            parentContainerCBX.cleanUp();
            associatedContainerCBX.cleanUp();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return getCBX() != null ? getCBX().getReason() : null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return getCBX() != null ? getCBX().getState() : ErrorType.Valid;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getValidatableUIComp()
     */
    @Override
    public Component getValidatableUIComp()
    {
        return getCBX();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isChanged()
     */
    @Override
    public boolean isChanged()
    {
        return getCBX() != null ? getCBX().isChanged() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return getCBX() != null ? getCBX().isInError() : false;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isRequired()
     */
    @Override
    public boolean isRequired()
    {
        return this.isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#reset()
     */
    @Override
    public void reset()
    {
        if (parentContainerCBX != null)
        {
            parentContainerCBX.reset();
            associatedContainerCBX.reset();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        if (parentContainerCBX != null)
        {
            parentContainerCBX.setAsNew(isNew);
            associatedContainerCBX.setAsNew(isNew);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        if (getCBX() != null)
        {
            getCBX().setChanged(isChanged);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setRequired(boolean)
     */
    @Override
    public void setRequired(boolean isRequired)
    {
        if (parentContainerCBX != null)
        {
            parentContainerCBX.setRequired(isRequired);
        }
        if (associatedContainerCBX != null)
        {
            associatedContainerCBX.setRequired(isRequired);
        }
        this.isRequired = isRequired;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setState(edu.ku.brc.af.ui.forms.validation.UIValidatable.ErrorType)
     */
    @Override
    public void setState(ErrorType state)
    {
        if (getCBX() != null)
        {
            getCBX().setState(state);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        return getCBX() != null ? getCBX().validateState() : ErrorType.Valid;
    }
}
