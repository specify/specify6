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
package edu.ku.brc.ui.forms.persist;

/*
 * @code_status Unknown (auto-generated)
 **
 * @author rods
 *
 */
public class FormCellSubView extends FormCell
{

    protected String viewSetName;
    protected String viewName;
    protected String classDesc;
    protected boolean singleValueFromSet;
    protected String description;
    
    /**
     * Constructor.
     *
     */
    public FormCellSubView()
    {
        type = CellType.subview;
    }
    
    /**
     * Constructor.
     * @param id unique id
     * @param name name of field for this view
     * @param viewSetName name of view set that this subview is referencing
     * @param viewName the name of the view within the view set
     * @param classDesc the class name of the field
     * @param description text descrption of the sub form (typically already localized)
     * @param colspan column span
     * @param rowspan row span
     * @param singleValueFromSet althught the data might be a "Set" pass in only the first data obj from the set
     */
    public FormCellSubView(final String id,
                           final String name, 
                           final String viewSetName, 
                           final String viewName, 
                           final String classDesc, 
                           final String description, 
                           final int    colspan, 
                           final int    rowspan)
    {
        super(CellType.subview, id, name, colspan, rowspan);
        this.viewName    = viewName;
        this.classDesc   = classDesc;
        this.viewSetName = viewSetName;
        this.description = description;
    }
    
    public String getClassDesc()
    {
        return classDesc;
    }

    public void setClassDesc(String classDesc)
    {
        this.classDesc = classDesc;
    }

    public String getViewName()
    {
        return viewName;
    }

    public void setView(String viewName)
    {
        this.viewName = viewName;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public void setViewSetName(String viewSetName)
    {
        this.viewSetName = viewSetName;
    }

    public boolean isSingleValueFromSet()
    {
        return singleValueFromSet;
    }

    public String getDescription()
    {
        return description;
    }
    
}
