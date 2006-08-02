/**
 * 
 */
package edu.ku.brc.util;

/**
 * Defines the capabilities needed to rank objects.
 *
 * @code_status Complete
 * @author jstewart
 * @version %I% %G%
 */
public interface Rankable
{
	/**
	 * Gets the rank of the called object.
	 * 
	 * @return the rank (tree level) of this node
	 */
	public Integer getRankId();
	
	/**
	 * Sets the rank of the called object.
	 * 
	 * @param id the new rank (tree level) of this node
	 */
	public void setRankId(Integer id);
}
