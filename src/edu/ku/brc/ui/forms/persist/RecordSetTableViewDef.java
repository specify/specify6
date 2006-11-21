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
package edu.ku.brc.ui.forms.persist;


/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Nov 19, 2006
 *
 */
public class RecordSetTableViewDef extends ViewDef
{
    protected String viewSetName;
    protected String viewName;

    /**
     * @param type the type (could be form or field)
     * @param name the name
     * @param className the class name of the data object
     * @param gettableClassName the class name of the gettable
     * @param settableClassName the class name of the settable
     * @param desc description
      */
    public RecordSetTableViewDef(final ViewDef.ViewType type, 
                                 final String  name, 
                                 final String  className, 
                                 final String  gettableClassName, 
                                 final String  settableClassName, 
                                 final String  desc,
                                 final String viewSetName,
                                 final String viewName)
    {
        super(type, name, className, gettableClassName, settableClassName, desc);
        this.viewSetName = viewSetName;
        this.viewName    = viewName;
    }
    
    /**
     * Return viewset name for the internal form.
     * @return viewset name for the internal form.
     */
    public String getViewSetName()
    {
        return viewSetName;
    }

    /**
     * Returns the view name.
     * @return the view name.
     */
    public String getViewName()
    {
        return viewName;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDef#clone()
     */
    @Override
    public ViewDef clone()
    {
        RecordSetTableViewDef fvd =  new RecordSetTableViewDef(type, 
                name, 
                className, 
                dataGettableName, 
                dataSettableName, 
                desc, 
                viewSetName, 
                viewName);
        return fvd;
    }
}
