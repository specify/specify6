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
public class UFlaFishBorrowAuthorizedByProcessor extends
		UFlaTransactionAgentProcessor {
	public UFlaFishBorrowAuthorizedByProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newBorrowAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, newBorrowAgentDefaults, "Authorized By", "borrow", "borrowagent",
				fieldsUsed);
	}

}
