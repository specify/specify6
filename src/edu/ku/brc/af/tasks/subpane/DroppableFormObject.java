/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.af.tasks.subpane;

import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.dnd.GhostDataAggregatable;

/**
 * This class enables both data and the associated form information to be dropped.
 *
 * @code_status Complete
 * 
 * @author rods
 *
 */
public class DroppableFormObject implements GhostDataAggregatable
{
    protected String viewSetName;
    protected int    formId;
    protected Object data;
    
    /**
     * Constructor.
     * @param viewSetName the View Set name of the form
     * @param formId the form's ID within the view set
     * @param data the data associated with the form
     */
    public DroppableFormObject(String viewSetName, int formId, Object data)
    {
        super();
        this.viewSetName = viewSetName;
        this.formId = formId;
        this.data = data;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public Object getData()
    {
        return data;
    }

    public int getFormId()
    {
        return formId;
    }
    
    //-------------------------------------------------
    //-- GhostDataAggregatable Interface
    //-------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.dnd.GhostDataAggregatable#getDataForClass(java.lang.Class)
     */
    public Object getDataForClass(Class<?> classObj)
    {
        if (classObj == DroppableFormObject.class)
        {
            return this;
        }
        return UIHelper.getDataForClass(data, classObj);
    }

}
