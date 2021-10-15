/**
 * 
 */
package utils.populate;

import java.sql.Connection;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class UFlaIvpMasterEnteredByProcessor extends ManyToOneProcessor {

	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fieldsUsed
	 */
	public UFlaIvpMasterEnteredByProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fieldsUsed) {
		super(connection, newAgentDefaults, fieldsUsed, "collectionobject", "CatalogerID");
	}


}
