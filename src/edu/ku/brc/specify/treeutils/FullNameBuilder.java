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
package edu.ku.brc.specify.treeutils;

import java.util.LinkedList;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * @author tnoble
 *
 *Called by FullNameRebuilder to build FullNames during TreeTraversal
 */
public class FullNameBuilder<T extends Treeable<T, D, I>, D extends TreeDefIface<T, D, I>, I extends TreeDefItemIface<T, D, I>> 
	
{
	protected final D treedef;
	
	/**
	 * @param treedef
	 */
	public FullNameBuilder(final D treedef)
	{
		this.treedef = treedef;
	}
	
	/**
	 * @param node
	 * @param parents
	 * @return the FullName for node.
	 */
	public String buildFullName(FullNameRebuilder<T,D,I>.NodeInfo node, LinkedList<FullNameRebuilder<T,D,I>.NodeInfo> parents)
	{
		//XXX need to adapt stuff in TreeHelper.getFullName for this context.
		StringBuilder sb = new StringBuilder();
		for (FullNameRebuilder<T,D,I>.NodeInfo parent : parents)
		{
			sb.append(parent.getName() + " ");
		}
		sb.append(node.getName());
		return sb.toString();
	}
}
