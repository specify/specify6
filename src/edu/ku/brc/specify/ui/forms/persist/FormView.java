/* Filename:    $RCSfile: FormView.java,v $
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

public class FormView
{
    public enum ViewType {form, table, field};
    
    protected ViewType             type;
    protected int                  id;
    protected Vector<FormAltView>  altViews       = new Vector<FormAltView>();
    protected boolean              resourceLabels = false;
    /**
     * Default Constructor
     *
     */
    public FormView()
    {
        
    }
    
    /**
     * 
     * @param aType
     * @param aId
     */
    public FormView(ViewType aType, int aId)
    {
        type = aType;
        id   = aId;
    }
    
    public FormAltView addAltView(FormAltView aAltView)
    {
        altViews.add(aAltView);
        return aAltView;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public ViewType getType()
    {
        return type;
    }

    public void setType(ViewType type)
    {
        this.type = type;
    }

    public Vector<FormAltView> getAltViews()
    {
        return altViews;
    }

    public void setAltViews(Vector<FormAltView> altViews)
    {
        this.altViews = altViews;
    }

    public boolean isResourceLabels()
    {
        return resourceLabels;
    }

    public void setResourceLabels(boolean resourceLabels)
    {
        this.resourceLabels = resourceLabels;
    }
    
    
}
