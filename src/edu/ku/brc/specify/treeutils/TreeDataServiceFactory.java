/**
 * Copyright (C) 2006  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
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
