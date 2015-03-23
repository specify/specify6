/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.forms.persist;

import java.util.List;
import java.util.Vector;

/*
 * @code_status Beta
 **
 * @author rods
 *
 */
public class TableViewDef extends ViewDef implements Cloneable, TableViewDefIFace
{

    protected Vector<FormColumnIFace> columns = new Vector<FormColumnIFace>();
    
    /**
     * Constructor
     * @param name the name
     * @param className the class name of the data object
     * @param gettableClassName the class name of the gettable
     * @param settableClassName the class name of the settable
     * @param desc description
     * @param useResourceLabels whether to use resource string
     */
    public TableViewDef(final String name, 
                        final String className, 
                        final String gettableClassName, 
                        final String settableClassName, 
                        final String desc,
                        final boolean useResourceLabels)
    {
        super(ViewType.table, name, className, gettableClassName, settableClassName, desc, useResourceLabels);
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableDefIFace#addColumn(edu.ku.brc.ui.forms.persist.FormColumn)
     */
    public FormColumnIFace addColumn(final FormColumnIFace column)
    {
        columns.add(column);
        return column;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.ViewDefIFace#getDerivedInterface()
     */
    public Class<?> getDerivedInterface()
    {
        return TableViewDefIFace.class;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableDefIFace#cleanUp()
     */
    @Override
    public void cleanUp()
    {
        super.cleanUp();
        columns.clear();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableDefIFace#getColumns()
     */
    public List<FormColumnIFace> getColumns()
    {
        return columns;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.persist.TableDefIFace#clone()
     */
    public Object clone() throws CloneNotSupportedException
    {
        TableViewDef tvd = (TableViewDef)super.clone();
        tvd.columns = new Vector<FormColumnIFace>();
        for (FormColumnIFace formCol : columns)
        {
            tvd.columns.add((FormColumn)formCol.clone()); 
        }
        return tvd;      
    }
    
    //-------------------------------------------------------------------
    // Helpers
    //-------------------------------------------------------------------
    //public FormColumn createColumn(final String nameStr, final String label, final String label, final String format)
    //{
    //    return addColumn(new FormColumn(nameStr, label, format));
    //}
}
