/**
 * 
 */
package utils.parse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;

/**
 * @author timo
 * 
 */
public class Parser {
	protected Toker toker;

	protected Stack<Symbol> stack = null;

	protected final Symbol startSymbol;
	protected final Terminator[] terminators;
	protected final Literal[] literals;
	protected final Substitution[] substitutions;
	protected final Substitution[] errsubs;
	protected final String[] quoteEnclosers;
	protected final boolean includeQuoteEnclosers;
	protected final ParseTable table;

	/**
	 * @param xmlDef
	 * @return
	 * @throws Exception
	 */
	static public Parser fromXML(File xmlDef) throws Exception {
		Element root = XMLHelper.readFileToDOM4J(xmlDef);
		
		String quoter = root.attributeValue("quoteenclosers", null);
		String[] quoters = new String[0];
		if (quoter != null) {
			if (quoter.length() % 2 != 0) {
				throw new Exception(
						"Error loading quoteenclosers length must be multiple of 2. Open and Close chars must be given.");
			}
			quoters = new String[quoter.length() / 2];
			for (int q = 0; q < quoter.length(); q += 2) {
				quoters[q / 2] = quoter.substring(q, q + 2);
			}
		}

		//String iq = root.attributeValue("includequoteenclosers", "true");
		Boolean includeQuoters = Boolean.valueOf(root.attributeValue("includequoteenclosers", "true"));

		List<Symbol> symbols = getSymbols(root);

		System.out.println("Symbols:");
		for (Symbol s : symbols) {
			System.out.println(s);
		}
		System.out.println();

		Symbol startSymbol = getSymbolByName(
				root.attributeValue("startsymbol"), symbols);
		List<Rule> rules = getRules(root, symbols);

		System.out.println("Rules:");
		for (Rule r : rules) {
			System.out.println(r);
		}
		System.out.println();

		Terminator[] terminators = getTerminators(root);

		System.out.print("Terminators: ");
		for (Terminator s : terminators) {
			System.out.print("'" + s.getText() + "' " + (s.isInclude() ? "$" : ""));
		}
		System.out.println();
		System.out.println();

		Literal[] literals = getLiterals(root);

		System.out.print("Literals: ");
		for (Literal s : literals) {
			System.out.print("'" + s + "' ");
		}

		System.out.println();
		System.out.println();

		Substitution[] substitutions = getSubstitutions(root);

		System.out.print("Substitutions: ");
		for (Substitution s : substitutions) {
			System.out.print("'" + s + "' ");
		}

		System.out.println();
		System.out.println();

		Substitution[] errsubs = getErrSubstitutions(root);

		System.out.print("Error Substitutions: ");
		for (Substitution s : errsubs) {
			System.out.print("'" + s + "' ");
		}

		System.out.println();
		System.out.println();

		ParseTable table = new ParseTable(ParseTable.buildTable(symbols, rules));
		return new Parser(startSymbol, terminators, literals, substitutions,
				errsubs, quoters, includeQuoters, table);
	}

	/**
	 * @param root
	 * @param symbols
	 * @return
	 */
	static private List<Rule> getRules(Element root, List<Symbol> symbols)
			throws Exception {
		List<Rule> result = new ArrayList<Rule>();
		for (Object obj : root.selectNodes("rules/rule")) {
			Element element = (Element) obj;
			String leftName = XMLHelper
					.getAttr(element, "left", "NoNameNoGood");
			String ruleName = XMLHelper.getAttr(element, "name", "Anonymous");
			Integer priority = XMLHelper.getAttr(element, "priority", 0);
			Symbol left = getSymbolByName(leftName, symbols);
			if (left == null) {
				throw new Exception("Unable to find '" + leftName
						+ "' for rule '" + ruleName);
			}
			List<Symbol> right = new ArrayList<Symbol>();
			for (Object rightObj : element.selectNodes("rights/right")) {
				Element rightEl = (Element) rightObj;
				String rightName = XMLHelper.getAttr(rightEl, "name",
						"NoNameNoGood");
				Symbol sym = getSymbolByName(rightName, symbols);
				if (sym == null) {
					throw new Exception("Unable to find '" + rightName
							+ "' for rule '" + ruleName);
				}
				right.add(sym);
			}
			result.add(new Rule(ruleName, left, right, priority));
		}
		return result;
	}

	/**
	 * @param root
	 * @return
	 */
	static private Terminator[] getTerminators(Element root) {
		List<?> nodes = root.selectNodes("terminators/terminator");
		Terminator[] result = new Terminator[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			result[i] = new Terminator(XMLHelper.getAttr((Element) nodes.get(i), "text",
					"BADTERMINATOR"), XMLHelper.getAttr((Element) nodes.get(i), "include",
							false));
		}
		return result;
	}

	/**
	 * @param root
	 * @return
	 */
	static private Literal[] getLiterals(Element root) {
		List<?> nodes = root.selectNodes("literals/literal");
		Literal[] result = new Literal[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			String text = XMLHelper.getAttr((Element) nodes.get(i), "text",
					"BADLITERAL");
			String parsedText = XMLHelper.getAttr((Element) nodes.get(i),
					"parsedtext", text);
			String format = XMLHelper.getAttr((Element) nodes.get(i),
					"format", null);
			String min = XMLHelper.getAttr((Element) nodes.get(i),
					"min", null);
			String max = XMLHelper.getAttr((Element) nodes.get(i),
					"max", null);
			Boolean caseSensitive = XMLHelper.getAttr((Element) nodes.get(i),
					"casesensitive", true);
			result[i] = new Literal(text, parsedText, format, min, max, caseSensitive);
		}
		return result;
	}

	/**
	 * @param root
	 * @return
	 */
	static private Substitution[] getSubstitutions(Element root) {
		List<?> nodes = root
				.selectNodes("directsubstitutions/directsubstitution");
		Substitution[] result = new Substitution[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			String replace = XMLHelper.getAttr((Element) nodes.get(i),
					"replace", "BADSUBSTITION");
			String replaceWith = XMLHelper.getAttr((Element) nodes.get(i),
					"with", "BADSUBSTITUTION");
			result[i] = new Substitution(replace, replaceWith);
		}
		return result;
	}

	/**
	 * @param root
	 * @return
	 */
	static private Substitution[] getErrSubstitutions(Element root) {
		List<?> nodes = root.selectNodes("errsubs/errsub");
		Substitution[] result = new Substitution[nodes.size()];
		for (int i = 0; i < nodes.size(); i++) {
			String replace = XMLHelper.getAttr((Element) nodes.get(i),
					"replace", "BADSUBSTITION");
			String replaceWith = XMLHelper.getAttr((Element) nodes.get(i),
					"with", "BADSUBSTITUTION");
			result[i] = new Substitution(replace, replaceWith);
		}
		return result;
	}

	/**
	 * @param root
	 * @return
	 */
	static private List<Symbol> getSymbols(Element root) {
		List<Symbol> result = new ArrayList<Symbol>();
		for (Object obj : root.selectNodes("symbols/symbol")) {
			Element element = (Element) obj;
			result.add(new Symbol(XMLHelper.getAttr(element, "name", ""),
					XMLHelper.getAttr(element, "terminal", false), 
					XMLHelper.getAttr(element, "eof", false), 
					XMLHelper.getAttr(element, "branch", false), 
					XMLHelper.getAttr(element, "table", ""), 
					XMLHelper.getAttr(element, "field", ""),
					XMLHelper.getAttr(element, "record_type", "")));
		}
		return result;
	}

	/**
	 * @param symbolName
	 * @param symbols
	 * @return
	 */
	static private Symbol getSymbolByName(String symbolName,
			List<Symbol> symbols) {
		for (Symbol s : symbols) {
			if (s.getName().equals(symbolName)) {
				return s;
			}
		}
		return null;
	}

	/**
	 * @param rules
	 * @param terminators
	 * @param literals
	 * @param table
	 */
	public Parser(Symbol startSymbol, Terminator[] terminators, Literal[] literals,
			Substitution[] substitutions, Substitution[] errsubs, String[] quoteEnclosers, 
			boolean includeQuoteEnclosers, ParseTable table) {
		this.startSymbol = startSymbol;
		this.terminators = terminators;
		this.literals = literals;
		this.substitutions = substitutions;
		this.errsubs = errsubs;
		this.quoteEnclosers = quoteEnclosers;
		this.includeQuoteEnclosers = includeQuoteEnclosers;
		this.table = table;
	}

	/**
	 * @param s
	 */
	protected void startParse(String s) {
		toker = new Toker(new Input(applySubstitutions(s)), terminators,
				literals, quoteEnclosers, includeQuoteEnclosers);
		stack = new Stack<Symbol>();
		stack.push(new Symbol("EOF", false, true, false, null, null, null));
		stack.push(startSymbol);
	}

	/**
	 * @param s
	 * @return
	 */
	protected String applySubstitutions(String s) {
		String result = s;
		for (Substitution sub : substitutions) {
			result = result.replaceAll(sub.getReplace(), sub.getWith());
		}
		return result;
	}

	/**
	 * @param toClone
	 * @return
	 */
	protected Stack<Symbol> cloneStack(Stack<Symbol> toClone)
			throws CloneNotSupportedException {
		Stack<Symbol> result = new Stack<Symbol>();
		for (int s = 0; s < toClone.size(); s++) {
			result.add((Symbol) toClone.get(s).clone());
		}
		return result;
	}

	/**
	 * @param s
	 * @return
	 * @throws Exception
	 */
	public DerivedParseResult parse(String s) throws Exception {
		return parse(s, false);
	}

	/**
	 * @return
	 */
	protected Stack<Substitution> buildRetryStack() {
		Stack<Substitution> result = new Stack<Substitution>();
		for (Substitution s : errsubs) {
			result.add(s);
		}
		return result;
	}

	/**
	 * @param sIn
	 * @param reverse
	 * @return
	 * @throws Exception
	 */
	public DerivedParseResult parse(String sIn, boolean reverse) throws Exception {
		Stack<Substitution> retries = buildRetryStack();
		String s = sIn;
		while (true) {
			try {
				return doParse(s, reverse);
			} catch (Exception ex) {
				if (!retries.empty()) {
					Substitution sub = retries.pop();
					s = sIn.replaceAll(sub.getReplace(), sub.getWith());
				} else {
					throw (ex);
				}
			}
		}
	}
	
	/**
	 * @param s
	 * @param reverse
	 * @return
	 * @throws Exception
	 */
	public DerivedParseResult doParse(String s, boolean reverse) throws Exception {
		startParse(s);
		Token t = toker.next();
		Stack<Branch> branches = new Stack<Branch>();
		DerivationTree derivationTree = new DerivationTree(null);
		derivationTree.addChild((Symbol) startSymbol.clone());
		DerivationTree derivation = derivationTree;
		while (!stack.empty()) {
			boolean error = false;
			Symbol x = stack.pop();
			if (x.isBranch()) {
				derivation = derivation.getParent();
			} else if (x.isTerminal() || x.isEof()) {
				if (x.getName().equals(t.getName())) {
					if (!x.isEof()) {
						derivation.getChild(x).setAttribute(t);
					}
					t = toker.next();
				} else {
					error = true;
				}
			} else {
				List<Rule> rules = table.getRule(x, t);
				if (rules != null) {
					if (reverse)
						Collections.reverse(rules); // cheap way to explore
													// alternative parsing.
					Vector<Stack<Symbol>> savedStacks = new Vector<Stack<Symbol>>();
					Vector<DerivationTree> savedTrees = new Vector<DerivationTree>();
					Vector<DerivationTree> savedDerivations = new Vector<DerivationTree>();
					for (int r = 1; r < rules.size(); r++) {
						savedStacks.add(cloneStack(stack));
						DerivationTree dt = (DerivationTree) derivationTree
								.clone();
						savedTrees.add(dt);
						savedDerivations.add(dt.getEquivalentNode(derivation));
					}
					for (int r = 0; r < rules.size(); r++) {
						Rule rule = rules.get(r);
						if (r > 0) {
							branches.push(new Branch(savedStacks.get(r - 1),
									rule, x, toker.getCursor(), t, savedTrees
											.get(r - 1), savedDerivations
											.get(r - 1)));
						} else {
							derivation = derivation
									.getChildNode(rule.getLeft());
							stackRule(rule, stack, derivation);
						}
					}
				} else {
					error = true;
				}
			}
			if (error) {
				if (!branches.isEmpty()) {
					Branch branch = branches.pop();
					stack = branch.getStack();
					derivationTree = branch.getDerivationTree();
					derivation = branch.getDerivation().getChildNode(
							branch.getRule().getLeft());
					stackRule(branch.getRule(), stack, derivation);
					toker.setCursor(branch.getInputPosition());
					t = branch.getToken();
				} else {
					throw new Exception("Parse Problem: got " + t.getName()
							+ ", expected " + x.getName());
				}
			}
		}
		return new DerivedParseResult("Testing", null, derivationTree);
	}

	/**
	 * @param rule
	 * @param stack
	 */
	protected void stackRule(Rule rule, Stack<Symbol> stack,
			DerivationTree derivationNode) throws Exception {
		List<Symbol> symbols = rule.getRight();
		stack.push(new Symbol(true));
		for (int sym = symbols.size() - 1; sym >= 0; sym--) {
			stack.push(symbols.get(sym));
		}
		for (int sym = 0; sym < symbols.size(); sym++) {
			derivationNode.addChild((Symbol) symbols.get(sym).clone());
		}

	}

	/**
	 * @author timo
	 * 
	 */
	private class Branch {
		Stack<Symbol> stack;
		Symbol symbol;
		Rule rule;
		int inputPosition;
		Token token;
		DerivationTree derivationTree;
		DerivationTree derivation;

		/**
		 * @param rule
		 * @param inputPosition
		 */
		public Branch(Stack<Symbol> stack, Rule rule, Symbol symbol,
				int inputPosition, Token token, DerivationTree derivationTree,
				DerivationTree derivation) {
			super();
			this.stack = stack;
			this.symbol = symbol;
			this.rule = rule;
			this.inputPosition = inputPosition;
			this.token = token;
			this.derivationTree = derivationTree;
			this.derivation = derivation;
		}

		/**
		 * @return the stack
		 */
		public Stack<Symbol> getStack() {
			return stack;
		}

		/**
		 * @return the rule
		 */
		public Rule getRule() {
			return rule;
		}

		/**
		 * @return the inputPosition
		 */
		public int getInputPosition() {
			return inputPosition;
		}

		/**
		 * @return the token
		 */
		public Token getToken() {
			return token;
		}

		/**
		 * @return the derivationTree
		 */
		public DerivationTree getDerivationTree() {
			return derivationTree;
		}

		/**
		 * @return the derivation
		 */
		public DerivationTree getDerivation() {
			return derivation;
		}

		/**
		 * @return the symbol
		 */
		@SuppressWarnings("unused")
		public Symbol getSymbol() {
			return symbol;
		}

	}

	static public void main(String[] args) {
		Symbol s = new Symbol("S");
		Symbol a = new Symbol("A");
		Symbol at = new Symbol("a", true);
		Symbol bt = new Symbol("b", true);
		Symbol ct = new Symbol("c", true);
		Symbol dt = new Symbol("d", true);

		Vector<Symbol> left = new Vector<Symbol>();
		left.add(ct);
		left.add(a);
		left.add(dt);
		Rule r1 = new Rule(s, left);

		left = new Vector<Symbol>();
		left.add(at);
		Rule r2 = new Rule(a, left);

		left = new Vector<Symbol>();
		left.add(at);
		left.add(bt);
		Rule r3 = new Rule(a, left);

		Map<String, Map<String, List<Rule>>> table = new HashMap<String, Map<String, List<Rule>>>();
		Map<String, List<Rule>> entry = new HashMap<String, List<Rule>>();
		Vector<Rule> entryRules = new Vector<Rule>();
		entryRules.add(r1);
		entry.put("c", entryRules);
		table.put("S", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r2);
		entryRules.add(r3);
		entry.put("a", entryRules);
		table.put("A", entry);

		Terminator[] terminators = { new Terminator(".", false), new Terminator(",", false) };
		Literal[] literals = { new Literal("a"), new Literal("b"),
				new Literal("c"), new Literal("d") };
		Substitution[] substitutions = {};

		Parser p = new Parser(r1.getLeft(), terminators, literals,
				substitutions, substitutions, null, true, new ParseTable(table));
		try {
			DerivedParseResult pr = p.parse("c a b d");
			pr.getDerivation().print(0);
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
