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
public class UFlaMammalLoanAuthorizedByProcessor extends  UFlaTransactionAgentProcessor {
	public UFlaMammalLoanAuthorizedByProcessor(Connection connection, List<Pair<String, Object>> newAgentDefaults,
			List<Pair<String, Object>> newLoanAgentDefaults,
			List<String> fieldsUsed) {
		super(connection, newAgentDefaults, newLoanAgentDefaults, "Authorized By", "loan", "loanagent",
				fieldsUsed);
	}

}
