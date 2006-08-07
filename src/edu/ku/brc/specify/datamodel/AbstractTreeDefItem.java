/**
 * 
 */
package edu.ku.brc.specify.datamodel;

/**
 *
 *
 * @author jstewart
 * @version %I% %G%
 */
public abstract class AbstractTreeDefItem implements TreeDefinitionItemIface
{
	public boolean canBeDeleted()
	{
		if(getTreeEntries().isEmpty())
		{
			return true;
		}
		return false;
	}
}
