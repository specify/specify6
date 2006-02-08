/* Filename:    $RCSfile: FormCellSubView.java,v $
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

public class FormCellSubView extends FormCell
{

    protected int    id;
    protected String viewSetName;
    protected String classDesc;
    
    /**
     * Constructor
     *
     */
    public FormCellSubView()
    {
        type = CellType.subview;
    }
    
    /**
     * Constructor
     * @param name name of field for this view
     * @param viewSetName name of view set that this subview is referencing
     * @param id the id of the view within the view set
     * @param classDesc the class name of the field
     * @param colspan column span
     * @param rowspan row span
     */
    public FormCellSubView(String    name, 
                           String    viewSetName, 
                           int       id, 
                           String    classDesc, 
                           final int colspan, 
                           final int rowspan)
    {
        super(CellType.subview, name, colspan, rowspan);
        this.id          = id;
        this.classDesc   = classDesc;
        this.viewSetName = viewSetName;
    }
    
    public String getClassDesc()
    {
        return classDesc;
    }

    public void setClassDesc(String classDesc)
    {
        this.classDesc = classDesc;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getViewSetName()
    {
        return viewSetName;
    }

    public void setViewSetName(String viewSetName)
    {
        this.viewSetName = viewSetName;
    }
    
}
