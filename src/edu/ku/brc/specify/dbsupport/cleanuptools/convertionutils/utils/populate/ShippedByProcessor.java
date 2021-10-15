/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class ShippedByProcessor extends ManyToOneProcessor {

	
	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 */
	public ShippedByProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fldsUsed) {
		super(connection, newAgentDefaults, fldsUsed, "shipment", "ShippedByID");
		// TODO Auto-generated constructor stub
	}

}
