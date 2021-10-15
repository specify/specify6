/**
 * 
 */
package utils.parse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author timo
 *
 */
public class ParseTable 
{
	protected final Map<String, Map<String, List<Rule>>> table;
	
	
	/**
	 * @param table
	 */
	public ParseTable(Map<String, Map<String, List<Rule>>> table) 
	{
		super();
		this.table = table;
	}


	/**
	 * @param symbol
	 * @param token
	 * @return
	 */
	public List<Rule> getRule(Symbol symbol, Token token)
	{
		Map<String, List<Rule>> row = table.get(symbol.getName());
		return row.get(token.getName());
	}
	
	/**
	 * @param rule
	 * @param rules
	 * @return
	 */
	public static Set<Symbol> getTerminals(Rule rule, final List<Rule> rules) {
		List<Rule> localRules = new ArrayList<Rule>(rules);
		Set<Symbol> result = new HashSet<Symbol>();
		if (rule.getRight() != null && rule.getRight().size() > 0) {
			Symbol initial = rule.getRight().get(0);
			if (initial.isTerminal()) {
				result.add(initial);
			}
			else for (int rIdx = localRules.size()-1; rIdx >= 0; rIdx--) {
				Rule r = localRules.get(rIdx);
				if (r.getLeft().equals(initial)) {
					localRules.remove(r);
					result.addAll(getTerminals(r, localRules));
				}
			}
		}
		return result;
	}

	/**
	 * @param symbols
	 * @param rules
	 * @return
	 */
	static public Map<String, Map<String, List<Rule>>> buildTable(List<Symbol> symbols, List<Rule> rules) {
		Map<String, Map<String, List<Rule>>> table = new HashMap<String, Map<String, List<Rule>>>();
		for (Symbol s : symbols) {
			if (!s.isTerminal()) {
				Set<Symbol> terminals = new HashSet<Symbol>();
				List<Rule> rulesForS = new ArrayList<Rule>();
				for (Rule r: rules) {
					if (r.getLeft().equals(s)) {
						rulesForS.add(r);
						terminals.addAll(getTerminals(r, rules));
					}
				}
				Collections.sort(rulesForS);
				Map<String, List<Rule>> entry = new HashMap<String, List<Rule>>();
				for (Symbol t: terminals) {
					entry.put(t.getName(), rulesForS);
				}
				table.put(s.getName(), entry);
			}
		}
		return table;
	}
}
