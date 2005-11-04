/* Filename:    $RCSfile: ViewSet.java,v $
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
package edu.ku.brc.specify.ui.forms.persist;

import java.util.Vector;
import java.util.Collections;

/**
 * Class that manages all the forms for a given view set (which is read from a single file)
 *
 * @author Rod Spears <rods@ku.edu>
 */

public class ViewSet
{

    private static FormView comparable = new FormView();
    
    private String name;
    private Vector<FormView> views = new Vector<FormView>();
    
    /**
     * Default Constructor
     *
     */
    public ViewSet()
    {

    }
    
    /**
     * Constructor with name
     *
     */
    public ViewSet(String aName)
    {
        name = aName;
    }
    
    /**
     * Added a form to the view set
     * @param aFormView the form to be added
     */
    public void add(FormView aFormView)
    {
        views.add(aFormView);
    }
    
    public FormView getById(Integer aId)
    {
        comparable.setId(aId);
        int inx = Collections.binarySearch(views, comparable);  
        return inx > -1 ? views.elementAt(inx) : null;
    }
    
    /**
     * 
     * @param aId id of form to be trieved
     * @return the form or null if it isn't found 
     */
    public FormView getForm(int aId)
    {
        return views.get(aId);
    }

    public Vector<FormView> getViews()
    {
        return views;
    }

    public void setViews(Vector<FormView> views)
    {
        this.views = views;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

     

}
