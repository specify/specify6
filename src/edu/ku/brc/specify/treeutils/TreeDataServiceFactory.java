/**
 * 
 */
package edu.ku.brc.specify.treeutils;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public class TreeDataServiceFactory
{
	public static TreeDataService createService()
	{
		TreeDataService service = new HibernateTreeDataServiceImpl();
		service.init();
		return service;
	}
}
