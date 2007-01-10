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
 *
 * @code_status Alpha
 * @author jstewart
 */
public class TreeDataServiceFactory
{
	public static
	<T extends Treeable<T,D,I>,
	D extends TreeDefIface<T,D,I>,
	I extends TreeDefItemIface<T,D,I>> TreeDataService<T,D,I> createService()
	{
		TreeDataService<T,D,I> service = new HibernateTreeDataServiceImpl<T,D,I>();
		return service;
	}
}
