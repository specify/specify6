/**
 * 
 */
package utils.misc;

import java.sql.Connection;

/**
 * @author timo
 *
 */
public abstract class Restringer {

	/**
	 * 
	 */
	public Restringer() {
		
	}

	abstract public String restring(String str, Connection con);

}
