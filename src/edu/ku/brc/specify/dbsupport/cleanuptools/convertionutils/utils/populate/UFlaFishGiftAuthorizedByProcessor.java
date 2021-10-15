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
public class UFlaFishGiftAuthorizedByProcessor extends
		UFlaTransactionAgentProcessor {
	public UFlaFishGiftAuthorizedByProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newGiftAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, newGiftAgentDefaults, "Authorized By", "gift", "giftagent",
				fieldsUsed);
	}

}
