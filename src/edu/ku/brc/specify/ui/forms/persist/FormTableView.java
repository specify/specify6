/* Filename:    $RCSfile: FormTableView.java,v $
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

public class FormTableView extends FormView
{

    protected Vector<FormColumn> columns = new Vector<FormColumn>();
    
    /**
     * 
     *
     */
    public FormTableView()
    {
        super(ViewType.table, -1);
        
    }

    /**
     * 
     * @param aType
     * @param aId
     */
    public FormTableView(ViewType aType, int aId)
    {
        super(ViewType.table, aId);
        
    }
    
    /**
     * 
     * @param aColumn
     * @return
     */
    public FormColumn addColumn(FormColumn aColumn)
    {
        columns.add(aColumn);
        return aColumn;
    }
    
    public Vector<FormColumn> getColumns()
    {
        return columns;
    }

    public void setColumns(Vector<FormColumn> columns)
    {
        this.columns = columns;
    }

    //-------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------
    public FormColumn createColumn(String aName, String aLabel)
    {
        return addColumn(new FormColumn(aName, aLabel));
    }
    
   
    
}
