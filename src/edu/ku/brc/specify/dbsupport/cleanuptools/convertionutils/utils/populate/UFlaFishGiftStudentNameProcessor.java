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
public class UFlaFishGiftStudentNameProcessor extends
		UFlaTransactionAgentProcessor {
	public UFlaFishGiftStudentNameProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newGiftAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, newGiftAgentDefaults, "Student", "gift", "giftagent",
				fieldsUsed);
	}

}
