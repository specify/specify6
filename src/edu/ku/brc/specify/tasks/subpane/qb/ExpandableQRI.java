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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.ui.UIHelper;

/**
 * @author timbo
 *
 * @code_status Alpha
 * 
 * QRIs that contain fields
 *
 */
public class ExpandableQRI extends BaseQRI
{
    protected DBTableInfo      ti;
    protected Vector<FieldQRI> fields = new Vector<FieldQRI>();

    /**
     * @param tableTree
     */
    public ExpandableQRI(final TableTree tableTree)
    {
        super(tableTree);
        
        this.ti       = tableTree.getTableInfo();
        this.iconName = ti.getClassObj().getSimpleName();
        this.title    = ti.getTitle();
        
        if (StringUtils.isEmpty(title))
        {
            title    = UIHelper.makeNamePretty(iconName);
        }
    }

    /**
     * @return
     */
    public DBTableInfo getTableInfo()
    {
        return ti;
    }

    /**
     * @return the kids
     */
    public int getFields()
    {
        return fields.size();
    }
    
    /**
     * @param f
     * @return
     */
    public FieldQRI getField(int f)
    {
        return fields.get(f);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.tasks.subpane.qb.BaseQRI#hasChildren()
     */
    @Override
    public boolean hasChildren()
    {
        return true;
    }
}
