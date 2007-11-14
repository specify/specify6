/*
     * Copyright (C) 2007  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.forms;

import java.awt.CardLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import edu.ku.brc.ui.GetSetValueIFace;
import edu.ku.brc.ui.forms.persist.FormCellIFace;
import edu.ku.brc.ui.forms.persist.FormCellSubViewIFace;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Nov 4, 2007
 *
 */
public class EditViewCompSwitcherPanel extends JPanel implements GetSetValueIFace
{
    protected Component     editComp;
    protected Component     viewComp;
    protected Component     currentComp;
    protected FormCellIFace formCell;
    protected String        currentCard;
    
    protected CardLayout        cardLayout  = new CardLayout();
    
    public EditViewCompSwitcherPanel(final FormCellIFace formCell)
    {
        super();
        
        this.formCell = formCell;
        
        setLayout(cardLayout);
    }
    
    public void putIntoEditMode()
    {
        currentCard = "Edit";
        cardLayout.show(this, currentCard);
        
        currentComp = editComp;
        repaint();
    }
    
    public void putIntoViewMode()
    {
        currentCard = "View";
        cardLayout.show(this, currentCard);
        
        currentComp = viewComp;
        repaint();
    }
    
    /**
     * @param editCmp
     * @param viewCmp
     */
    public void set(final Component editCmpReg, 
                    final Component editCmpAdd,
                    final Component viewCmpReg, 
                    final Component viewCmpAdd)
    {
        /*editComp = (GetSetValueIFace)(editCmpReg != null ? editCmpReg : editCmpAdd);
        viewComp = (GetSetValueIFace)(viewCmpReg != null ? viewCmpReg : viewCmpAdd);
        
        add("Edit", editCmpAdd != null ? editCmpAdd : editCmpReg);
        add("View", viewCmpAdd != null ? viewCmpAdd : viewCmpReg);
        */
        
        editComp = register("Edit", editCmpAdd, editCmpReg);
        viewComp = register("View", viewCmpAdd, viewCmpReg);
        
        currentComp = editComp;
    }
    
    /**
     * @param name
     * @param compToAdd
     * @param compToReg
     * @return
     */
    protected Component register(final String    name, 
                                 final Component compToAdd, 
                                 final Component compToReg)
    {
        add(name, compToAdd);
        
        Component control =  compToReg == null ? compToAdd : compToReg;
        
        JScrollPane scrPane;
        Component comp;
        if (control instanceof JScrollPane)
        {
            scrPane = (JScrollPane)control;
            comp = scrPane.getViewport().getView();
        } else
        {
            scrPane = null;
            comp = control;
        }
        return comp;
    }
    
    /**
     * @return the current component
     */
    public Component getCurrentComp()
    {
        return currentComp;
    }
    
    /**
     * @return the either the edit component 'true' or the view component 'false'
     * @param isEditComp true -> edit, false -> view
     * @return the component
     */
    public Component getComp(final boolean isEditComp)
    {
        return isEditComp ? editComp : viewComp;
    }
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setEnabled(boolean)
     */
    public void setEnabled(final boolean enabled)
    {
        super.setEnabled(enabled);
        for (int i=0;i<getComponentCount();i++)
        {
            getComponent(i).setEnabled(enabled);
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#getValue()
     */
    public Object getValue()
    {
        boolean isSingleValueFromSet = false;
        if (formCell instanceof FormCellSubViewIFace)
        {
            isSingleValueFromSet = ((FormCellSubViewIFace)formCell).isSingleValueFromSet();
        }
        
        return FormViewObj.getValueFromComponent(currentComp, isSingleValueFromSet,
                                                 formCell.getType() == FormCellIFace.CellType.command, 
                                                 currentCard);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.GetSetValueIFace#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String defaultValue)
    {
        FormViewObj.setDataIntoUIComp(currentComp, value, defaultValue);
    }
    
}
