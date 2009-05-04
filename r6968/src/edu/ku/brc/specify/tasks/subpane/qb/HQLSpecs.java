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
package edu.ku.brc.specify.tasks.subpane.qb;

import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timbo
 *
 * @code_status Alpha
 *
 */
public class HQLSpecs
{
    protected final String                     hql;
    protected final List<Pair<String, Object>> args;
    protected final List<SortElement>          sortElements;

    public HQLSpecs(final String hql, final List<Pair<String, Object>> args,
            final List<SortElement> sortElements)
    {
        this.hql = hql;
        this.args = args;
        this.sortElements = sortElements;
    }

    public String getHql()
    {
        return hql;
    }

    public List<Pair<String, Object>> getArgs()
    {
        return args;
    }

    public List<SortElement> getSortElements()
    {
        return sortElements;
    }
}
