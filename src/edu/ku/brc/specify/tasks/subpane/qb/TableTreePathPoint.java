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
package edu.ku.brc.specify.tasks.subpane.qb;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.UsageTracker;

/**
 * @author tnoble
 *
 */
public class TableTreePathPoint
{
	protected final int tableId; //the DBTableIdMgr table id.
	protected final String name; //the name of the relationship

	/**
	 * @param point
	 */
	public TableTreePathPoint(final TableTree point)
	{
		super();
		this.tableId = point.getTableInfo().getTableId();
		if (point.getField() != null && 
				!point.getField().equalsIgnoreCase(point.getName()))
		{
			this.name = point.getField();
		}
		else
		{
			this.name = null;
		}
	}
	
	/**
	 * @param def a string in the format tableid-name.
	 * 
	 * Creates a TableTreePathPoint from strings produced
	 * by TableTreePathPoint.toString()
	 */
	public TableTreePathPoint(final String def)
	{
		String[] chunks = StringUtils.split(def, "-");
		try
		{
			if (chunks.length == 1)
			{
				tableId = Integer.valueOf(def);
				name = null;
			}
			else if (chunks.length == 2)
			{
				tableId = Integer.valueOf(chunks[0]);
				name = chunks[1];
			}
			else
			{
				throw new Exception("badness");
			}
		}
		catch (Exception ex)
		{
            UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(TableTreePathPoint.class, ex);
			throw new RuntimeException("invalid TableTreePathPoint: " + def);
		}		
	}
	
	/**
	 * @return the tableId
	 */
	public int getTableId()
	{
		return tableId;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof TableTreePathPoint)
		{
			TableTreePathPoint ttpp = (TableTreePathPoint )obj;
			if (tableId != ttpp.tableId)
			{
				return false;
			}
			return StringUtils.equals(name, ttpp.name);
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String result = String.valueOf(tableId);
		if (name != null)
		{
			result += "-" + name;
		}
		return result;
	}
		
}
