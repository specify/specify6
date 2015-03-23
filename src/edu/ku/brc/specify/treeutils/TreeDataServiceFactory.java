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
package edu.ku.brc.specify.treeutils;

import edu.ku.brc.specify.datamodel.TreeDefIface;
import edu.ku.brc.specify.datamodel.TreeDefItemIface;
import edu.ku.brc.specify.datamodel.Treeable;

/**
 * A factory for producing {@link TreeDataService} instances.
 * 
 * @author jstewart
 * @code_status Complete
 */
public class TreeDataServiceFactory
{
	/**
     * Returns a {@link TreeDataService} instance capable of finding and manipulating
     * a tree of the given type.
     * 
	 * @param <T> an implementation class of {@link Treeable}
	 * @param <D> an implementation class of {@link TreeDefIface}
	 * @param <I> an implementation class of {@link TreeDefItemIface}
	 * @return a {@link TreeDataService} instance capable of handling the given type of tree
	 */
	public static
	<T extends Treeable<T,D,I>,
	D extends TreeDefIface<T,D,I>,
	I extends TreeDefItemIface<T,D,I>> TreeDataService<T,D,I> createService()
	{
		TreeDataService<T,D,I> service = new HibernateTreeDataServiceImpl<T,D,I>();
		return service;
	}
}
