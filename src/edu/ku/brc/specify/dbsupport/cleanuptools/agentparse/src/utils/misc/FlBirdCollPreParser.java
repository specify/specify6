/**
 * 
 */
package utils.misc;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class FlBirdCollPreParser extends Restringer {

	/**
	 * 
	 */
	public FlBirdCollPreParser() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see utils.misc.Restringer#restring(java.lang.String)
	 */
	@Override
	public String restring(String str, Connection con) {
		List<Object[]> r = BasicSQLUtils.query(con, "select text5 from florni.collectingevent where text1 = '" + str.replaceAll("'","''") + "' limit 1");
		if (r !=  null && r.size() == 1) {
			return (String)r.get(0)[0];
		} else {
			return str;
		}
	}

}
