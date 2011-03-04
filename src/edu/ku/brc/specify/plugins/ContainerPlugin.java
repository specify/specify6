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

import static edu.ku.brc.ui.UIRegistry.getResourceString;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.ButtonGroup;
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
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.af.ui.forms.validation.UIValidatable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.Container;

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
    
    private UIFieldFormatterIFace    containerFormatter     = null;
    
    protected ValComboBoxFromQuery   parentContainerCBX     = null;       // This is used when a Container is child
    protected ValComboBoxFromQuery   associatedContainerCBX = null;   // This is used when a container is cataloged
    protected CardLayout             cardLayout             = new CardLayout();
    protected JPanel                 cardPanel;
    
    protected Boolean                doingParent            = null;
    protected boolean                isRequired             = false;
    
    protected ButtonGroup            group                  = new ButtonGroup();
    protected JRadioButton           isParentContainerRB    = new JRadioButton(getResourceString("IS_NET_BASED"));
    protected JRadioButton           isAssociatedRB         = new JRadioButton(getResourceString("IS_ENCRYPTED_KEY"));
    
    
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
        
        setLayout(new BorderLayout());
        
        cardPanel = new JPanel(cardLayout);
        
        group.add(isParentContainerRB);
        group.add(isAssociatedRB);
        
        CellConstraints cc = new CellConstraints();
        PanelBuilder    pb = new PanelBuilder(new FormLayout("p,10px,p,f:p:g", "p"));
        pb.add(isParentContainerRB, cc.xy(1,1));
        pb.add(isAssociatedRB,      cc.xy(3,1));
        cardPanel.add(Integer.toString(isParentContainerRB.hashCode()), isParentContainerRB);
        cardPanel.add(Integer.toString(isAssociatedRB.hashCode()), isAssociatedRB);
        
        DBTableInfo conTI     = DBTableIdMgr.getInstance().getInfoById(Container.getClassTableId());
        //DBFieldInfo catNumFld = conTI.getFieldByColumnName("CatalogNumber");
        //containerFormatter = catNumFld.getFormatter();
        
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
        pb.add(parentContainerCBX, cc.xy(1, 1));
        
        parentContainerCBX.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e)
            {
                itemSelected();
            }
        });
        
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
        pb.add(associatedContainerCBX, cc.xy(1, 1));

        associatedContainerCBX.addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                itemSelected();
            }
        });

        //group.getSelection().getSelectedObjects()
        ActionListener rbAction = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                JRadioButton rb = (JRadioButton)e.getSource();
                cardLayout.show(cardPanel, Integer.toString(rb.hashCode()));
                
                doingParent = rb.hashCode() == isParentContainerRB.hashCode();
            }
        };
        isParentContainerRB.addActionListener(rbAction);
        isAssociatedRB.addActionListener(rbAction);
        
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
            if (doingParent == null)
            {
                associatedContainer = collectionObject.getContainerOwner();
                parentContainer     = collectionObject.getContainer();
                
                doingParent = associatedContainer == null;
            }
            
            parentContainerCBX.setValue(parentContainer, null);
            associatedContainerCBX.setValue(associatedContainer, null);
        }
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.plugins.UIPluginBase#getValue()
     */
    @Override
    public Object getValue()
    {
        return collectionObject;
    }

    /**
     * @return
     */
    private ValComboBoxFromQuery getCBX()
    {
        return doingParent ? parentContainerCBX : associatedContainerCBX;
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
            final String clause = String.format(" FROM collectionobject WHERE CollectionObjectId = %d AND ContainerID IS NOT NULL", selectedId);
            String sql = "SELECT COUNT(*)" + clause;
            System.err.println("ContainersColObjPlugin: "+sql+" -> "+BasicSQLUtils.getCountAsInt(sql));
            
            if (BasicSQLUtils.getCountAsInt(sql) > 0)
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        /*String catNumStr = BasicSQLUtils.querySingleObj("SELECT CatalogNumber" + clause);
                        catNumStr = containerFormatter != null ? (String)containerFormatter.formatToUI(catNumStr) : catNumStr;
                        
                        UIRegistry.loadAndPushResourceBundle("specify_plugins");
                        UIRegistry.showLocalizedError("CNTR_CO_INUSE", catNumStr);
                        UIRegistry.popResourceBundle();
                        
                        qcbx.setValue(null, null);*/
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
        parentContainerCBX.cleanUp();
        associatedContainerCBX.cleanUp();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getReason()
     */
    @Override
    public String getReason()
    {
        return getCBX().getReason();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#getState()
     */
    @Override
    public ErrorType getState()
    {
        return getCBX().getState();
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
        return getCBX().isChanged();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#isInError()
     */
    @Override
    public boolean isInError()
    {
        return getCBX().isInError();
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
        parentContainerCBX.reset();
        associatedContainerCBX.reset();
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setAsNew(boolean)
     */
    @Override
    public void setAsNew(boolean isNew)
    {
        parentContainerCBX.setAsNew(isNew);
        associatedContainerCBX.setAsNew(isNew);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#setChanged(boolean)
     */
    @Override
    public void setChanged(boolean isChanged)
    {
        getCBX().setChanged(isChanged);
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
        getCBX().setState(state);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.validation.UIValidatable#validateState()
     */
    @Override
    public ErrorType validateState()
    {
        return getCBX().validateState();
    }
}
