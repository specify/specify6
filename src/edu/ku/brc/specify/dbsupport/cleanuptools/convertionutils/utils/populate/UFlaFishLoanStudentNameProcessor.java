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
public class UFlaFishLoanStudentNameProcessor extends
		UFlaTransactionAgentProcessor {
	public UFlaFishLoanStudentNameProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newLoanAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, newLoanAgentDefaults, "Student", "loan", "loanagent",
				fieldsUsed);
	}

}
