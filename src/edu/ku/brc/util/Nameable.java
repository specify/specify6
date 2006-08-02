/**
 * 
 */
package edu.ku.brc.util;

/**
 * Defines capabilities needed to name an object.
 *
 * @code_status Complete
 * @author jstewart
 * @version %I% %G%
 */
public interface Nameable
{
	/**
	 * Gets the name of the called object.
	 * 
	 * @return the name of the called object
	 */
	public String getName();
	
	/**
	 * Sets the name of the called object.
	 * 
	 * @param name the new name of the called object
	 */
	public void setName(String name);
}
