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
package edu.ku.brc.af.core.expresssearch;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;

/**
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Sep 12, 2007
 *
 */
class SearchTableRenderable extends TableInfoRenderable
{
    protected SearchTableConfig table = null;
    protected SearchFieldConfig field = null;
    protected RelatedQuery      relatedQuery = null;

    public SearchTableRenderable(SearchFieldConfig field)
    {
        super(field.getFieldInfo());
        this.field = field;
    }

    public SearchTableRenderable(SearchTableConfig table)
    {
        super(table.getTableInfo());
        this.table = table;
    }

    public SearchTableRenderable(RelatedQuery rq)
    {
        super((DBTableInfo)null);
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(rq.getErti().getTableId());
        tableInfo = ti;
        this.relatedQuery = rq;
    }
    
    public void setDisplayOrder(final Integer displayOrder)
    {
        if (table != null)
        {
            table.setDisplayOrder(displayOrder);
            
        } else if (relatedQuery != null)
        {
            relatedQuery.setDisplayOrder(displayOrder);
            
        } else
        {
            throw new RuntimeException("Can't set display order fields"); //$NON-NLS-1$
        }
    }

    /**
     * @return the table
     */
    public SearchTableConfig getTable()
    {
        return table;
    }

    /**
     * @return the field
     */
    public SearchFieldConfig getField()
    {
        return field;
    }

    /**
     * @return the relatedQuery
     */
    public RelatedQuery getRelatedQuery()
    {
        return relatedQuery;
    }

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TableInfoRenderable o)
    {
        SearchTableRenderable sr = (SearchTableRenderable)o;
        if (table != null)
        {
            return table.getDisplayOrder().compareTo(sr.getTable().getDisplayOrder());
            
        } else if (relatedQuery != null)
        {
            return relatedQuery.getDisplayOrder().compareTo(sr.getRelatedQuery().getDisplayOrder());
            
        } else
        {
            throw new RuntimeException("Can't sort fields"); //$NON-NLS-1$
        }
    }
}
