/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.ku.brc.af.ui.forms.BaseBusRules;
import edu.ku.brc.af.ui.forms.Viewable;
import edu.ku.brc.af.ui.forms.validation.ValComboBoxFromQuery;
import edu.ku.brc.specify.datamodel.CollectingEventAttribute;
import edu.ku.brc.specify.datamodel.Determination;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.ui.GetSetValueIFace;

/**
 * @author timo
 *
 */
public class CollectingEventAttributeBusRules extends BaseBusRules {

	//CollectingEventAttribute cea = null;
	
    protected KeyListener    nameChangeKL          = null;
	
	
	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#beforeFormFill()
	 */
	@Override
	public void beforeFormFill() {
		//cea = null;
	}



	/* (non-Javadoc)
	 * @see edu.ku.brc.af.ui.forms.BaseBusRules#afterFillForm(java.lang.Object)
	 */
	@Override
	public void afterFillForm(Object dataObj) {
		if (dataObj != null && dataObj instanceof CollectingEventAttribute) {
			Component activeTax = formViewObj.getControlByName("preferredHostTaxon");
			if (activeTax != null)
			{
				JTextField activeTaxTF = (JTextField)activeTax;
				activeTaxTF.setFocusable(false);
				if (((CollectingEventAttribute)dataObj).getHostTaxon() != null) {
					Taxon hostAccepted = ((CollectingEventAttribute)dataObj).getHostTaxon().getAcceptedTaxon();
					if (hostAccepted != null) {
						activeTaxTF.setText(hostAccepted.getFullName());
					} else {
						activeTaxTF.setText(((CollectingEventAttribute)dataObj).getHostTaxon().getFullName());
					}
				} else
				{
					activeTaxTF.setText("");
				}
			}
	    }
	}

    /* (non-Javadoc)
     * @see edu.ku.brc.af.ui.forms.BaseBusRules#initialize(edu.ku.brc.af.ui.forms.Viewable)
     */
    @Override
    public void initialize(Viewable viewableArg)
    {
        super.initialize(viewableArg);
        
        if (formViewObj != null) {
            GetSetValueIFace hostField = (GetSetValueIFace) formViewObj.getControlByName("hostTaxon");
            if (hostField instanceof ValComboBoxFromQuery) {
                final ValComboBoxFromQuery parentCBX = (ValComboBoxFromQuery) hostField;
                final Component altNameComp = formViewObj.getControlByName("alternateName");

                if (nameChangeKL == null) {
                    nameChangeKL = new KeyAdapter() {
                        @Override
                        public void keyTyped(final KeyEvent e) {
                            SwingUtilities.invokeLater(new Runnable() {
                                // @Override
                                public void run() {
                                    taxonChanged(parentCBX, altNameComp);
                                }
                            });
                        }
                    };
                }
                
                if (parentCBX != null) {
                    parentCBX.addListSelectionListener(new ListSelectionListener() {
                        public void valueChanged(ListSelectionEvent e) {
                            if (e == null || !e.getValueIsAdjusting()) {
                                taxonChanged(parentCBX, altNameComp);
                            }
                        }
                    });
                    addListenerIfNecessary(nameChangeKL, parentCBX);
                }

            }
        }
    }

    /**
     * @param kl
     * @param comp
     * 
     * Adds KeyListener to comp if it is not already a listener.
     * (This method is overkill given the current way listeners are set up.)
     */
    protected void addListenerIfNecessary(final KeyListener kl, final Component comp) {
        boolean fnd = false;
        for (KeyListener existingKl : comp.getKeyListeners()) {
            if (existingKl == kl) {
                fnd = true;
                break;
            }
        }
        if (!fnd) {
            comp.addKeyListener(kl);
        }
    }

    /**
     * @param taxonComboBox 
     * 
     * Sets text for preferredTaxon control to the selected taxon or it's accepted parent.
     * 
     */
    protected void taxonChanged(final ValComboBoxFromQuery taxonComboBox, final Component altTaxName) {
        Object objInForm = formViewObj.getDataObj();
        if (objInForm == null) {
            return;
        }
        
        Taxon formNode = ((Determination )objInForm).getTaxon();

        Taxon taxon = null;
        if (taxonComboBox.getValue() instanceof String) {
            // the data is still in the VIEW mode for some reason
            taxonComboBox.getValue();
            taxon = formNode.getParent();
        } else {
            taxon = (Taxon )taxonComboBox.getValue();
        }
        
        String activeTaxName = null;
        
        // set the tree def for the object being edited by using the parent node's tree def
        if (taxon != null) {
            if (taxon.getIsAccepted()) {
                activeTaxName = taxon.getFullName();
            } else {
                activeTaxName = taxon.getAcceptedParent().getFullName();
            }
        }

        Component activeTax = formViewObj.getControlByName("preferredHodyTaxon");
        if (activeTax != null) {
            ((JTextField )activeTax).setText(activeTaxName);
        }
    }


}
