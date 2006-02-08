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

import java.util.List;
import java.util.Vector;

public class FormTableView extends FormView
{

    protected Vector<FormColumn> columns = new Vector<FormColumn>();
    
    /**
     * Constructor
     * @param id the id
     * @param name the name
     * @param className the class name of the data object
     * @param gettableClassName the class name of the gettable
     * @param settableClassName the class name of the settable
     * @param desc description
     * @param isValidated whether to turn on validation
     */
    public FormTableView(final int    id, 
                         final String name, 
                         final String className, 
                         final String gettableClassName, 
                         final String settableClassName, 
                         final String desc,
                         final boolean isValidated)
    {
        super(ViewType.table, id, name, className, gettableClassName, settableClassName, desc, isValidated);
        
    }
    
    /**
     * Add a column definition
     * @param column the column def to add
     * @return the column def that was added
     */
    public FormColumn addColumn(final FormColumn column)
    {
        columns.add(column);
        return column;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.ui.forms.persist.FormView#cleanUp()
     */
    public void cleanUp()
    {
        super.cleanUp();
        columns.clear();
    }
    
    public List<FormColumn> getColumns()
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
    public FormColumn createColumn(final String name, final String label, final String format)
    {
        return addColumn(new FormColumn(name, label, format));
    }
    
   
    
}
