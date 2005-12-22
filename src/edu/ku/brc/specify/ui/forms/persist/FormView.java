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

public class FormView implements Comparable<FormView>
{
    public enum ViewType {form, table, field};
    
    protected ViewType             type;
    protected int                  id;
    protected String               name;
    protected String               desc;
    protected Vector<FormAltView>  altViews       = new Vector<FormAltView>();
    protected boolean              resourceLabels = false;
    
    protected String               viewSetName    = null;
    
    /**
     * Default Constructor
     *
     */
    public FormView()
    {
        
    }
    
    /**
     * CReate FormView
     * @param type the type of form (form, table, field)
     * @param id the unique id of the form
     */
    public FormView(final ViewType type, final int id, final String name, final String desc)
    {
        this.type = type;
        this.id   = id;
        this.name = name;
        this.desc = desc;
    }
    
    /**
     * Adds an alternative view
     * @param altView the alternate view
     * @return the form that was passed in
     */
    public FormAltView addAltView(final FormAltView altView)
    {
        altViews.add(altView);
        return altView;
    }

    
    public int compareTo(FormView obj)
    {
        if (id == obj.getId())
        {
            return 0;
            
        } else
        {
           return id > obj.getId() ? 1 : -1;
        }
    }
    
    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    public ViewType getType()
    {
        return type;
    }

    public void setType(final ViewType type)
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

    public void setResourceLabels(final boolean resourceLabels)
    {
        this.resourceLabels = resourceLabels;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public void setViewSetName(final String viewSetName)
    {
        this.viewSetName = viewSetName;
    }

    public String getDesc()
    {
        return desc;
    }

    public void setDesc(String desc)
    {
        this.desc = desc;
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
