/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.treeutils;

import java.util.Comparator;
import java.math.BigDecimal;

import edu.ku.brc.specify.datamodel.GeologicTimePeriod;

/**
 * A class used to compare GeologicTimePeriod objects for use in sorting sibling nodes.
 *
 * @code_status Complete
 * @author jstewart
 */
public class GeologicTimePeriodComparator implements Comparator<GeologicTimePeriod>
{
	/**
	 * Compare two GeologicTimePeriod objects based on the values they return for getEnd().
	 * 
	 * @param o1 a {@link GeologicTimePeriod} object
	 * @param o2 a {@link GeologicTimePeriod} object
	 * @return -1, 0, or 1 if o1 is less than, equal to, or greater than o2, respectively
	 */
	public int compare(GeologicTimePeriod o1, GeologicTimePeriod o2)
	{
		BigDecimal start1 = o1.getStartPeriod();
		BigDecimal start2 = o2.getStartPeriod();
		if(start1 == null)
		{
			return -1;
		}
		else if(start2 == null)
		{
			return 1;
		}
		
		int start =  start1.compareTo(start2);
		if( start != 0 )
		{
			return start;
		}
		
		BigDecimal end1 = o1.getEndPeriod();
		BigDecimal end2 = o2.getEndPeriod();
		if(end1 == null)
		{
			return 1;
		}
		else if(end2 == null)
		{
			return -1;
		}
		return end1.compareTo(end2);
	}
}
