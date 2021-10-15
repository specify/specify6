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
public class UFlaVpAccDonorAgentProcessor extends UFlaTransactionAgentProcessor {
	public UFlaVpAccDonorAgentProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newBorrowAgentDefaults,
			List<String> fieldsUsed, String role) {
		super(connection, newAgentDefaults, newBorrowAgentDefaults, role, "accession", "accessionagent",
				fieldsUsed);
	}
}
