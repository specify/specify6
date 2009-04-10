/* Copyright (C) 2009, University of Kansas Center for Research
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

import java.util.Hashtable;
import java.util.Vector;

public interface FormViewDefIFace extends ViewDefIFace
{
    /**
     * Add a row to the form
     * @param row the row to add
     * @return the row that was added
     */
    public abstract FormRowIFace addRow(FormRowIFace row);

    /**
     * @return all the rows
     */
    public abstract Vector<FormRowIFace> getRows();

    /**
     * Returns a FormCell by ID (searches the rows and then the columns)
     * @param idStr the ID of the field 
     * @return a FormCell by ID (searches the rows and then the columns)
     */
    public abstract FormCellIFace getFormCellById(String idStr);

    /**
     * Returns a FormCell by name (searches the rows and then the columns)
     * @param nameStr the name of the field 
     * @return a FormCell by name (searches the rows and then the columns)
     */
    public abstract FormCellIFace getFormCellByName(final String nameStr);

    /**
     * Asks the object to clean up data strcuture before going away
     */
    public abstract void cleanUp();

    /**
     * @return The JGoodies coldef
     */
    public abstract String getColumnDef();

    /**
     * @param columnDef JGoodies coldef
     */
    public abstract void setColumnDef(String columnDef);

    /**
     * @return the JGoodies row def
     */
    public abstract String getRowDef();

    /**
     * @param rowDef the JGoodies row def
     */
    public abstract void setRowDef(String rowDef);

    /**
     * @return the enables rules
     */
    public abstract Hashtable<String, String> getEnableRules();

    /**
     * @param enableRules 
     */
    public abstract void setEnableRules(Hashtable<String, String> enableRules);

    /**
     * @return the name of the ViewDef it should use for it's definition
     */
    public abstract String getDefinitionName();

    /**
     * @param definitionName  the name of the ViewDef it should use for it's definition
     */
    public abstract void setDefinitionName(String definitionName);

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public abstract Object clone() throws CloneNotSupportedException;

}
