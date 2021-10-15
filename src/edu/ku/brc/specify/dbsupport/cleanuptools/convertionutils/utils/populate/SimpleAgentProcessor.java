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
public class SimpleAgentProcessor extends AgentProcessor {

	/**
	 * @param connection
	 * @param newAgentDefaults
	 * @param fldsUsed
	 */
	public SimpleAgentProcessor(Connection connection,
			List<Pair<String, Object>> newAgentDefaults, List<String> fldsUsed) {
		super(connection, newAgentDefaults, fldsUsed);
	}

	/* (non-Javadoc)
	 * @see utils.populate.RecordProcessor#process(utils.populate.SourceRecord)
	 */
	@Override
	public void process(SourceRecord rec) throws Exception {
		ParseInfo parseInfo = chooseParseInfo(null, rec.getParses());
		List<RawRecord> records = parseInfo.getRecords();
		for (RawRecord record : records) {
			getId(record);
		}
	}

}
