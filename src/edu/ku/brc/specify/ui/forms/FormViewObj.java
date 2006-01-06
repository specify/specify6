/* Filename:    $RCSfile: FormViewObj.java,v $
 * Author:      $Author: rods $
 * Revision:    $Revision: 1.1 $
 * Date:        $Date: 2005/10/12 16:52:27 $
 *
 * This library is free software; you can redistribute it and/or
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
package edu.ku.brc.specify.ui.forms;

import java.util.List;
import java.util.ArrayList;
import javax.swing.JComponent;

import edu.ku.brc.specify.ui.forms.persist.FormView;

/**
 * Implmentation of the FormViewable interface for the ui
 *  
 * @author rods
 *
 */
public class FormViewObj implements FormViewable
{
    protected FormViewObj       parent;
    protected FormView          formViewDef;
    protected JComponent        comp          = null;
    protected List<FormViewObj> kids = new ArrayList<FormViewObj>();
    
    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     */
    public FormViewObj(final FormViewObj parent,  final FormView formViewDef)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
    }
    
    /**
     * Constructor with FormView definition
     * @param formViewDef the definition of the form
     * @param comp the component of the form
     */
    public FormViewObj(final FormViewObj parent,  final FormView formViewDef, final JComponent comp)
    {
        this.parent      = parent;
        this.formViewDef = formViewDef;
        this.comp = comp;
    }
    

    /**
     * Sets the component into the object
     * @param comp the UI component that represents this viewable
     */
    public void setComp(JComponent comp)
    {
        this.comp = comp;
    }
    
    /**
     * Adds child to parent
     * @param child the child to be added
     */
    public void addChild(final FormViewObj child)
    {
        kids.add(child);
    }
 
    //-------------------------------------------------
    // FormViewable
    //-------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getId()
     */
    public int getId()
    {
        return formViewDef.getId();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getType()
     */
    public FormView.ViewType getType()
    {
        return formViewDef.getType();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#getUIComponent()
     */
    public JComponent getUIComponent()
    {
        return comp;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.FormViewable#isSubform()
     */
    public boolean isSubform()
    {
        return parent != null;
    }

}
