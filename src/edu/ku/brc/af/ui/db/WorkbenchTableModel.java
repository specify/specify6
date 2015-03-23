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
package edu.ku.brc.af.ui.db;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @code_status Beta
 * 
 * @author meg
 *
 */
public class WorkbenchTableModel extends AbstractTableModel
{
    // private static final Logger log = Logger.getLogger(WorkbenchTableModel.class);
    public WorkbenchTableModel()
    {
        super();
        // TODO Auto-generated constructor stub
    }

    public int getColumnCount()
    {
        // TODO Auto-generated method stub
        return 10;
    }

    public int getRowCount()
    {
        // TODO Auto-generated method stub
        return 1000;
    }

    public Object getValueAt(int arg0, int arg1)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
