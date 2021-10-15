/**
 * 
 */
package utils.misc;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import utils.analyze.AnalyzeParseResultSet;
import utils.format.ParseResultsFormatter;
import utils.parse.BaseFieldValue;
import utils.parse.DerivedParseResult;
import utils.parse.FieldValue;
import utils.parse.ParseResult;
import utils.parse.ParseTable;
import utils.parse.Parser;
import utils.parse.Parsing;
import utils.parse.Record;
import utils.parse.Rule;
import utils.parse.Symbol;
import utils.parse.Token;
import utils.populate.CsvImporter;
import utils.populate.FieldParserToXml;

/**
 * @author timo
 * 
 */
public class AgentNameParse {

	static public Parser buildAgentParser(boolean includeR6ExcludeR15, boolean useAbbreviation) {
		Vector<Symbol> symbols = new Vector<Symbol>();
		Symbol agents = new Symbol("Agents");
		symbols.add(agents);
		Symbol agent = new Symbol("Agent", "agent", null);
		symbols.add(agent);
		Symbol lastName = new Symbol("LastName", "agent", "LastName");
		symbols.add(lastName);
		Symbol firstName = new Symbol("FirstName", "agent", "FirstName");
		symbols.add(firstName);
		Symbol middle = new Symbol("Middle", "agent", "MiddleInitial");
		symbols.add(middle);
		Symbol suffix = new Symbol("Suffix");
		symbols.add(suffix);
		Symbol initial = new Symbol("Initial");
		symbols.add(initial);
		Symbol abbreviation = new Symbol("Abbreviation", "agent",
				"Abbreviation");
		symbols.add(abbreviation);
		Symbol name = new Symbol("Name");
		symbols.add(name);
		Symbol connector = new Symbol("Connector");
		symbols.add(connector);
		//Symbol finalConnector = new Symbol("FinalConnector");
		Symbol str1 = new Symbol("Str1", true);
		symbols.add(str1);
		Symbol str = new Symbol("Str", true);
		symbols.add(str);
		Symbol upStr3 = new Symbol("UpStr3", true);
		symbols.add(upStr3);
		Symbol comma = new Symbol(",", true);
		symbols.add(comma);
		Symbol and = new Symbol("and", true);
		symbols.add(and);
		Symbol ampersand = new Symbol("&", true);
		symbols.add(ampersand);
		Symbol slash = new Symbol("/", true);
		symbols.add(slash);
		Symbol semicolon = new Symbol(";", true);
		symbols.add(semicolon);
		Symbol junior = new Symbol("jr", true);
		symbols.add(junior);
		
		Vector<Symbol> right = new Vector<Symbol>();
		right.add(agent);
		right.add(connector);
		right.add(agents);
		Rule r1 = new Rule(agents, right);
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(r1);
		
		/*right = new Vector<Symbol>();
		right.add(agent);
		right.add(finalConnector);
		right.add(agent);
		Rule r25 = new Rule(agents, right);*/

		right = new Vector<Symbol>();
		right.add(agent);
		Rule r2 = new Rule(agents, right);
		rules.add(r2);

		right = new Vector<Symbol>();
		right.add(firstName);
		right.add(lastName);
		Rule r3 = new Rule(agent, right);
		rules.add(r3);

		right = new Vector<Symbol>();
		right.add(abbreviation);
		Rule r4 = new Rule(agent, right);
		rules.add(r4);

		right = new Vector<Symbol>();
		right.add(lastName);
		Rule r5 = new Rule(agent, right);
		rules.add(r5);

		right = new Vector<Symbol>();
		right.add(lastName);
		right.add(comma);
		right.add(firstName);
		Rule r6 = new Rule(agent, right);
		rules.add(r6);

		// right = new Vector<Symbol>();
		// right.add(name);
		// //right.add(suffix);
		// Rule r7 = new Rule(lastName, right);

		right = new Vector<Symbol>();
		right.add(name);
		Rule r8 = new Rule(lastName, right);
		rules.add(r8);

		right = new Vector<Symbol>();
		right.add(name);
		Rule r9 = new Rule(firstName, right);
		rules.add(r9);

		right = new Vector<Symbol>();
		right.add(name);
		right.add(middle);
		Rule r10 = new Rule(firstName, right);
		rules.add(r10);

		right = new Vector<Symbol>();
		right.add(initial);
		Rule r18 = new Rule(firstName, right);
		rules.add(r18);

		right = new Vector<Symbol>();
		right.add(initial);
		right.add(middle);
		Rule r19 = new Rule(firstName, right);
		rules.add(r19);

		right = new Vector<Symbol>();
		right.add(name);
		Rule r11 = new Rule(middle, right);
		rules.add(r11);

		right = new Vector<Symbol>();
		right.add(initial);
		Rule r12 = new Rule(middle, right);
		rules.add(r12);

		right = new Vector<Symbol>();
		right.add(initial);
		right.add(initial);
		Rule r13 = new Rule(middle, right);
		rules.add(r13);

		right = new Vector<Symbol>();
		right.add(junior);
		Rule r14 = new Rule(suffix, right);
		rules.add(r14);

		// With this included, 'Lone, Pepe' will be parsed as 2 agent last names
		// instead of 'LastName, FirstName.
		// Need to get it to offer both possibilities...
		right = new Vector<Symbol>();
		right.add(comma);
		Rule r15 = new Rule(connector, right);
		rules.add(r15);

		right = new Vector<Symbol>();
		right.add(and);
		Rule r16 = new Rule(connector, right);
		rules.add(r16);

		right = new Vector<Symbol>();
		right.add(ampersand);
		//Rule r17 = new Rule(finalConnector, right);
		Rule r17 = new Rule(connector, right);
		rules.add(r17);

		right = new Vector<Symbol>();
		right.add(semicolon);
		Rule r23 = new Rule(connector, right);
		rules.add(r23);
		
		right = new Vector<Symbol>();
		right.add(slash);
		Rule r24 = new Rule(connector, right);
		rules.add(r24);

		right = new Vector<Symbol>();
		right.add(str1);
		Rule r20 = new Rule(initial, right);
		rules.add(r20);

		right = new Vector<Symbol>();
		right.add(str);
		Rule r21 = new Rule(name, right);
		rules.add(r21);

		right = new Vector<Symbol>();
		right.add(upStr3);
		Rule r22 = new Rule(abbreviation, right);
		rules.add(r22);

		Map<String, Map<String, List<Rule>>> table = new HashMap<String, Map<String, List<Rule>>>();
		Map<String, List<Rule>> entry = new HashMap<String, List<Rule>>();
		Vector<Rule> entryRules = new Vector<Rule>();

		entryRules.add(r1);

		//entryRules.add(r25);
		
		entryRules.add(r2);
		entry.put("Str1", entryRules);
		if (useAbbreviation) {
			entry.put("UpStr3", entryRules);
		}
		entry.put("Str", entryRules);
		table.put("Agents", entry);

		Set<Symbol> terminals = ParseTable.getTerminals(r1, rules);
		for (Symbol t: terminals) {
			System.out.println(t.getName());
		}
		
		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r3);
		entry.put("Str1", entryRules);
		
		if (useAbbreviation) {
			entryRules = new Vector<Rule>();
			entryRules.add(r4);
			entry.put("UpStr3", entryRules);
		}
		
		entryRules = new Vector<Rule>();
		entryRules.add(r3);
		entryRules.add(r5);
		if (includeR6ExcludeR15) {
			entryRules.add(r6);
		}
		entry.put("Str", entryRules);
		table.put("Agent", entry);
		terminals = ParseTable.getTerminals(r1, rules);
		for (Symbol t: terminals) {
			System.out.println(t.getName());
		}

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		// entryRules.add(r7);
		entryRules.add(r8);
		entry.put("Str", entryRules);
		table.put("LastName", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r18);
		entryRules.add(r19);
		entry.put("Str1", entryRules);
		entryRules = new Vector<Rule>();
		entryRules.add(r9);
		entryRules.add(r10);
		entry.put("Str", entryRules);
		table.put("FirstName", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r12);
		entryRules.add(r13);
		entry.put("Str1", entryRules);
		entryRules = new Vector<Rule>();
		entryRules.add(r11);
		entry.put("Str", entryRules);
		table.put("Middle", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r14);
		entry.put("jr", entryRules);
		table.put("Suffix", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r20);
		entry.put("Str1", entryRules);
		table.put("Initial", entry);

		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r21);
		entry.put("Str", entryRules);
		table.put("Name", entry);

		if (useAbbreviation) {
			entry = new HashMap<String, List<Rule>>();
			entryRules = new Vector<Rule>();
			entryRules.add(r22);
			entry.put("UpStr3", entryRules);
			table.put("Abbreviation", entry);
		}
		
		entry = new HashMap<String, List<Rule>>();
		entryRules = new Vector<Rule>();

		// see comment at declaration of r15
		if (!includeR6ExcludeR15) {
			entryRules.add(r15);
			entry.put(",", entryRules);
		}

		entryRules = new Vector<Rule>();
		entryRules.add(r16);
		entry.put("and", entryRules);
		entryRules = new Vector<Rule>();
		entryRules.add(r17);
		entry.put("&", entryRules);
		entryRules = new Vector<Rule>();
		entryRules.add(r23);
		entry.put(";", entryRules);
		entryRules = new Vector<Rule>();
		entryRules.add(r24);
		entry.put("/", entryRules);
		table.put("Connector", entry);

		/*entry = new HashMap<String, Vector<Rule>>();
		entryRules = new Vector<Rule>();
		entryRules.add(r17);
		entry.put("&", entryRules);
		table.put("FinalConnector", entry);*/
		
		String[] terminators = { ".", ",", ";", " ", "/" };
		String[] literals = { "and", "&", "Jr", ",", ";", " ", "/" };
		
        System.out.println("Symbols:");
        for (Symbol s : symbols) {
        	System.out.println(s);
        }
        System.out.println();
        System.out.println("Rules:");
        for (Rule r : rules) {
        	System.out.println(r);
        }
        System.out.println();


		table = ParseTable.buildTable(symbols, rules);
		//return new Parser(r1.getLeft(), terminators, literals, new ParseTable(
		//	table));
		
		try {
			return Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_1.xml")/*, new ParseTable(table)*/);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}

	}

	/**
	 * @param dbName
	 * @param server
	 * @return
	 */
	public static String getConnectionStr(String dbName, String server) {
		return "jdbc:mysql://" + server + "/" + dbName
				+ "?characterEncoding=UTF-8&autoReconnect=true";
	}

	/**
	 * @param toParse
	 * @param pd
	 * @throws Exception
	 */
	private static void parseSomeStuff(String[] toParse, Parser[] pds, boolean printDerivation, boolean printErrorsOnly) {
		List<Parsing> results = new ArrayList<Parsing>();
		for (String d : toParse) {
			Set<DerivedParseResult> prs = new TreeSet<DerivedParseResult>();
			List<Exception> exs = new ArrayList<Exception>();
			for (int p = 0; p < pds.length; p++) {
				try {
					prs.add(pds[p].parse(d));
				} catch (Exception ex) {	
					exs.add(ex);
				}

			}
			results.add(new Parsing(null, d, new ArrayList<ParseResult>(prs), exs));
		}
		
		int errCount = 0;
		for (Parsing p : results) {
			if (!printErrorsOnly || p.getExceptions() != null && p.getExceptions().size() > 0) {
				System.out.println(p.getInput());
			}
			if (!printErrorsOnly || p.getExceptions() != null && p.getExceptions().size() > 0) {
				for (ParseResult pr : p.getParses()) {
					if (printDerivation && pr instanceof DerivedParseResult) {
						((DerivedParseResult) pr).getDerivation().print(0);
					}
					List<Record> records = pr.getRecords();
					for (Record rec : records) {
						System.out.print("   " + rec.getTable() + "."
								+ rec.getRecordType() + ": ");
						for (BaseFieldValue fv : rec.getFields()) {
							System.out.print(fv.getField() + " = ");
							System.out.print(fv.getValue() + ", ");
						}
						System.out.println();
					}
					System.out.println();
				}
			}
			if (p.getExceptions().size() > 0) {
				errCount++;
				for (Exception ex : p.getExceptions()) {
					System.out.println("   " + ex.getLocalizedMessage());
					System.out.println();
				}
			}
		}
		System.out.println("Values parsed: " + toParse.length + ". Values with errors: " + errCount);
	}
	
	private static void parseSomeDates() throws Exception {
		
		/*String rangeex = "\\p{Digit}{1,2}-\\p{Digit}{1,2}";
		boolean m = "1-2".matches(rangeex);
		m = "10-12".matches(rangeex);
		m = "-12".matches(rangeex);
		m = "1-12".matches(rangeex);
		m = "4-11".matches(rangeex);
		m = "4-".matches(rangeex);
		m = "4".matches(rangeex);
		
		String remex = "(.*)";
		m = "(bug)".matches(remex);*/
		
		String[] toParse = {
				"June 20, 1978",
				"1945.12.31",
				"31 January 1945",
				"17/12/1999",
				"17.vii.2004",
				"17.XII.2004",
				"30 april 1966",
				"00 Jan 1989",
				"4 Jan 1989",
				"4-18 June 2007",
				"1958",
				"1987-93",
				"1943-4",
				"1975-2011",
				"January 1958",
				"2015 March",
				"18/10 to 27/12 2013",
				"18/10 - 27/12 2013",
				"5 May - 17 Aug 2008"
		};
		Parser[] pd = {Parser.fromXML(new File("D:/data/florida/fish/DateParseTest.xml"))};
		
		parseSomeStuff(toParse, pd, false, false);
		
	}
	
	private static void parseSomeFlishDates() throws Exception {
		String serverName = "localhost";
		String dbName = "flish6";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(dbName, serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);
		Statement stmt = con.createStatement();
		ResultSet rows = stmt
				.executeQuery("select distinct lower(Method) from determination where method is not null "
						//+ "and (method like 'j%' or method like 'f%' or method like 'm%' or method like 'a%' "
						//+ "or method like 's%' or method like 'o%' or method like 'n%' or method like 'd%') "
						+ "order by 1");
		List<String> vals = new ArrayList<String>();
		while (rows.next()) {
			vals.add(rows.getString(1));
		}
		
		Parser[] pd = {Parser.fromXML(new File("D:/data/florida/fish/DateParseTest.xml"))};
		String[] toParse = new String[0];
		parseSomeStuff(vals.toArray(toParse), pd, false, true);
		
	}

	private static void parseSomeFishAgents() throws Exception {
		String serverName = "localhost";
		String dbName = "flish";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(dbName, serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		//Parser pLastNameList = Parser.fromXML(new File(
		//		"D:/data/florida/fish/agentparser_2.xml"));
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5.xml"));
		//Parser ps = Parser.fromXML(new File(
		//		"D:/data/florida/fish/simpleparser.xml"));

		Parser[] parsers = { p1 };
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);
		Statement stmt = con.createStatement();
		// ResultSet rows =
		// stmt.executeQuery("select distinct collector from tbl_master where collector is not null order by 1");
		ResultSet rows = stmt
				.executeQuery("select distinct collector from locality where collector is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct identified_by from tbl_master where identified_by is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct author_name from tbl_publications where author_name is not null order by 1");
		int c = 0;
		List<Parsing> parsings = new ArrayList<Parsing>();
		while (rows.next() && c++ < 50000) {
			String text = rows.getString(1);
			System.out.println(text);
			List<ParseResult> parses = new ArrayList<ParseResult>();
			List<Exception> exceptions = new ArrayList<Exception>();
			for (Parser p : parsers) {
				try {
					ParseResult pr = p.parse(text);
					parses.add(pr);
					List<Record> records = pr.getRecords();

					for (Record rec : records) {
						// System.out.println(rec.getTable());
						for (BaseFieldValue fv : rec.getFields()) {
							System.out.print("   " + fv.getField() + " = ");
							if (fv instanceof FieldValue) {
								for (Token t : ((FieldValue) fv).getAttributes()) {
									System.out.print(t.getValue());
								}
							} else {
								System.out.print(fv.getValue());
							}
							System.out.println();
						}
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					exceptions.add(ex);
					System.out.println();
				}
				System.out.println();
			}
			parsings.add(new Parsing(null, text, parses, exceptions));
		}
		AnalyzeParseResultSet a = new AnalyzeParseResultSet();
		Map<String, Object> analysis = a.analyze(parsings);

		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println(analysis.get(AnalyzeParseResultSet.TOTAL_ROWS));
		System.out.println(analysis.get(AnalyzeParseResultSet.MAX_RECORDS));
		System.out.println(analysis.get(AnalyzeParseResultSet.FIELDS));

		ParseResultsFormatter f = new ParseResultsFormatter(parsings, true, null);
		List<String[]> tbl = f.generateTable();
		for (String[] line : tbl) {
			for (String col : line) {
				System.out.print(col + "\t");
			}
			System.out.println();
		}

		JTable jt = new JTable(f.generateTableModel());
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JFrame jf = new JFrame();
		JScrollPane sp = new JScrollPane(jt,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jf.getContentPane().add(sp);
		jf.pack();
		jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jf.setVisible(true);

		System.out.println(rows.getMetaData().getColumnCount());

	}
	
	/**
	 * @throws Exception
	 */
	private static void parseSomeTestAgents() throws Exception {
		String[] toParse = {
				"ALT,DAVID",
				"BROOKS,H.K., ECHOLS,R. & ROSS,A.", 
				"BROOKS,H.K., ECHOLS,R. & ROSS, A. & B.", 
				"BARRINGTON (DON)",
				"DEL PIETRO,BOBBY",
				"BLACK HILLS INSTITUTE",
				"Luke Parker",
				"Luke Parker Jr",
				"Parker,L. D.",
				"Parker,Peter & Dieter",
				"Parker,P.C & D.F",
				"Johnson, Park, Coombs & Bubba",
				"Brian & Biff Johnson",
				"Jones Monastic Prayer Dome",
				"Jones Prayer Dome",
				"Jones Dome",
				"Jones",
				"Jones, Bobby C.",
				"V.G. Springer, R.T. Kirk, and W. Brackin",
				"Yerger, Hart, & White",
				"A Kerstitch et al.",
				"Shipp and Dean"
		};
		//Parser pd1 = Parser.fromXML(new File("D:/data/florida/fish/agentparser_1.xml"));
		//Parser pd2 = Parser.fromXML(new File("D:/data/florida/fish/agentparser_2.xml"));
		//Parser pd3 = Parser.fromXML(new File("D:/data/florida/fish/simpleparser.xml"));
		Parser pd3 = Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5.xml"));
		//Parser pd4 = Parser.fromXML(new File("D:/data/florida/fish/agentparser_3.xml"));
		Parser[] pds = {/*pd1, pd2, */pd3/*, pd4*/};
		parseSomeStuff(toParse, pds, false, false);
	}
	
	private static void writeTableToCSVFile(String fileName, ParseResultsFormatter f) throws Exception {
		List<String[]> tbl = f.generateTable();
		List<String> outLines = new ArrayList<String>();
		for (String[] line : tbl) {
			String outLine = "";
			for (int c = 0; c < line.length; c++ ) {
				if (StringUtils.isNotBlank(line[c])) {
					outLine += "\"" + line[c] + "\"";
				}
						
				if (c < line.length-1) {
					outLine += ","; 
				}
			}
			outLines.add(outLine);
		}
		FileUtils.writeLines(new File(fileName), outLines);
	}
	
	private static void parseSomeIVPAgentsToXml(String server, String db, String user, String pw,
			String tbl, String keyFld, String fld, String outputFile) throws Exception {
		
		Parser pLastNameList = Parser.fromXML(new File(
				//"D:/data/florida/fish/agentparser_2.xml"));
				"D:/data/florida/fish/agentparser_3b.xml"));
				//"/home/timo/workspace/convertionutils/src/agentparser_3b.xml"));
		Parser p1 = Parser.fromXML(new File(
				//"D:/data/florida/fish/agentparser_1.xml"));
				"D:/data/florida/fish/agentparser_3.xml"));
				//"/home/timo/workspace/convertionutils/src/agentparser_3.xml"));
		//Parser ps = Parser.fromXML(new File(
		//		"D:/data/florida/fish/simpleparser.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		parsers.add(pLastNameList);
		FieldParserToXml pxml = new FieldParserToXml(server, db, user, pw, tbl, keyFld, fld, 
				parsers, outputFile);
		pxml.parse();
		
	}
	
	private static void parseSomeFishAgentsToXml(String server, String db, String user, String pw,
			String tbl, String keyFld, String fld, String outputFile) throws Exception {
		
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		FieldParserToXml pxml = new FieldParserToXml(server, db, user, pw, tbl, keyFld, fld, 
				parsers, outputFile);
		pxml.parse();
		
	}

	private static void parseIvpCollectorFromParsesStoredInCsv(String server, String db, String user, String pw,
			String tbl, String keyFld, String fld, String outputFile, String inputFile) throws Exception {
		//this needs to call a FieldParserToXml subclass (sort of) that will processRows() by looking up
		//parse in List<Parsing> imported by CsvImporter from inputfile ...
		CsvImporter imp = new CsvImporter("agent", "imp", 
				new File(inputFile), "utf-8", ',', '\\', '"', true);
		List<Parsing> importedParsings = imp.getParsings();
		Collections.sort(importedParsings, new Comparator<Parsing>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Parsing arg0, Parsing arg1) {
				// TODO Auto-generated method stub
				return arg0.getInput().compareTo(arg1.getInput());
			}
			
		});
		
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(db, server);
		String dbDriver = "com.mysql.jdbc.Driver";
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);
		Statement stmt = con.createStatement();
		ResultSet rows =
		stmt.executeQuery("select distinct `" + fld + "` from `" + tbl + "` where `" +  fld 
				+ "` is not null order by 1");
		int c = 0;
		while (rows.next() && c++ < 50000) {
			String text = rows.getString(1);
			//System.out.println(text);
			int idx = Collections.binarySearch(importedParsings,
					new Parsing(null, text, null, null),
					new Comparator<Parsing>() {

						/*
						 * (non-Javadoc)
						 * 
						 * @see java.util.Comparator#compare(java.lang.Object,
						 * java.lang.Object)
						 */
						@Override
						public int compare(Parsing arg0, Parsing arg1) {
							// TODO Auto-generated method stub
							return arg0.getInput().compareTo(arg1.getInput());
						}

					});
			if (idx == -1) {
				System.out.println("Oh NO! No match for " + text);
			}
		}

	}
	
	private static void parseSomeIVPAgents(String dbName, String fld, String tblName, String fileName, Comparator<Parsing> sorter) throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(dbName, serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

//		Parser pLastNameList = Parser.fromXML(new File(
//				//"D:/data/florida/fish/agentparser_2.xml"));
//				"D:/data/florida/fish/agentparser_4b.xml"));
//		Parser p1 = Parser.fromXML(new File(
//				//"D:/data/florida/fish/agentparser_1.xml"));
//				"D:/data/florida/fish/agentparser_4.xml"));
//		Parser ps = Parser.fromXML(new File(
//				"D:/data/florida/fish/simpleparser.xml"));
//
//		Parser[] parsers = { p1, pLastNameList };
		
		Parser[] parsers = {Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5.xml"))};
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);
		Statement stmt = con.createStatement();
		ResultSet rows =
		stmt.executeQuery("select distinct `" + fld + "` from `" + tblName + "` where `" +  fld 
				+ "` is not null order by 1");
		//ResultSet rows = stmt
		//		.executeQuery("select distinct collector from locality where collector is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct identified_by from tbl_master where identified_by is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct author_name from tbl_publications where author_name is not null order by 1");
		int c = 0;
		List<Parsing> parsings = new ArrayList<Parsing>();
		while (rows.next() && c++ < 50000) {
			String text = rows.getString(1);
			System.out.println(text);
			Set<DerivedParseResult> parses = new TreeSet<DerivedParseResult>();
			List<Exception> exceptions = new ArrayList<Exception>();
			for (Parser p : parsers) {
				try {
					DerivedParseResult pr = p.parse(text);
					parses.add(pr);
					List<Record> records = pr.getDerivation().getRecords();

					for (Record rec : records) {
						// System.out.println(rec.getTable());
						for (BaseFieldValue fv : rec.getFields()) {
							System.out.print("   " + fv.getField() + " = ");
							if (fv instanceof FieldValue) {
								for (Token t : ((FieldValue) fv).getAttributes()) {
									System.out.print(t.getValue());
								}
							} else {
								System.out.print(fv.getValue());
							}
							System.out.println();
						}
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					exceptions.add(ex);
					System.out.println();
				}
				System.out.println();
			}
			parsings.add(new Parsing(null, text, new ArrayList<ParseResult>(parses), exceptions));
		}
		AnalyzeParseResultSet a = new AnalyzeParseResultSet();
		Map<String, Object> analysis = a.analyze(parsings);

		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println(analysis.get(AnalyzeParseResultSet.TOTAL_ROWS));
		System.out.println(analysis.get(AnalyzeParseResultSet.MAX_RECORDS));
		System.out.println(analysis.get(AnalyzeParseResultSet.FIELDS));

		ParseResultsFormatter f = new ParseResultsFormatter(parsings, true, sorter);
		List<String[]> tbl = f.generateTable();
		for (String[] line : tbl) {
			for (String col : line) {
				System.out.print(col + "\t");
			}
			System.out.println();
		}

		if (fileName != null) {
			writeTableToCSVFile(fileName, f);
		}
		
		JTable jt = new JTable(f.generateTableModel());
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JFrame jf = new JFrame();
		JScrollPane sp = new JScrollPane(jt,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jf.getContentPane().add(sp);
		jf.pack();
		jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jf.setVisible(true);

		System.out.println(rows.getMetaData().getColumnCount());

	}

	/**
	 * @param dbName
	 * @param fld
	 * @param tblName
	 * @param fileName
	 * @param sorter
	 * @throws Exception
	 */
	private static void parseSomeAgents(String dbName, String fld, String tblName, String fileName, Comparator<Parsing> sorter) throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_paleobot.xml", dbName, fld, tblName, fileName,
				sorter, null, null);
	}

	/**
	 * @param dbName
	 * @param fld
	 * @param tblName
	 * @param fileName
	 * @param sorter
	 * @param sqlSelector
	 * @throws Exception
	 */
	private static void parseSomeAgents(String dbName, String fld, String tblName, String fileName, Comparator<Parsing> sorter,
			String sqlSelector) throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_fish.xml", dbName, fld, tblName, fileName,
				sorter, sqlSelector, null);
	}

	/**
	 * @param parserFile
	 * @param dbName
	 * @param fld
	 * @param tblName
	 * @param fileName
	 * @param sorter
	 * @throws Exception
	 */
	private static void parseSomeAgents(String parserFile, String dbName, String fld, String tblName, String fileName, 
			Comparator<Parsing> sorter) throws Exception {
		parseSomeAgents(parserFile, dbName, fld, tblName, fileName, sorter, null, null); 
	}
	/**
	 * @param parserFile
	 * @param dbName
	 * @param fld
	 * @param tblName
	 * @param fileName
	 * @param sorter
	 * @param sqlSelector
	 * @throws Exception
	 */
	private static void parseSomeAgents(String parserFile, String dbName, String fld, String tblName, String fileName, 
			Comparator<Parsing> sorter, String sqlSelector, Restringer preParser) throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(dbName, serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

//		Parser pLastNameList = Parser.fromXML(new File(
//				//"D:/data/florida/fish/agentparser_2.xml"));
//				"D:/data/florida/fish/agentparser_4b.xml"));
//		Parser p1 = Parser.fromXML(new File(
//				//"D:/data/florida/fish/agentparser_1.xml"));
//				"D:/data/florida/fish/agentparser_4.xml"));
//		Parser ps = Parser.fromXML(new File(
//				"D:/data/florida/fish/simpleparser.xml"));
//
//		Parser[] parsers = { p1, pLastNameList };
		
		//Parser[] parsers = {Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml"))};
		Parser[] parsers = {Parser.fromXML(new File(parserFile))};
		Parser defaultParser = Parser.fromXML(new File("/home/timo/convertionutils/src/agentparser_default.xml"));
		//Parser defaultParser = Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_default.xml"));
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);
		Statement stmt = con.createStatement();
		String sql = sqlSelector != null ? sqlSelector 
				: "select distinct `" + fld + "` collate utf8_bin from " + tblName + " where `" +  fld 
				+ "` is not null order by 1";
		ResultSet rows = stmt.executeQuery(sql);
		//ResultSet rows = stmt
		//		.executeQuery("select distinct collector from locality where collector is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct identified_by from tbl_master where identified_by is not null order by 1");
		// ResultSet rows =
		// stmt.executeQuery("select distinct author_name from tbl_publications where author_name is not null order by 1");
		int c = 0;
		List<Parsing> parsings = new ArrayList<Parsing>();
		while (rows.next() && c++ < 50000) {
			String pretext = rows.getString(1);
			System.out.println(pretext);
			String text = preParser == null ? pretext : preParser.restring(pretext, con);
			Set<DerivedParseResult> parses = new TreeSet<DerivedParseResult>();
			List<Exception> exceptions = new ArrayList<Exception>();
			for (Parser p : parsers) {
				try {
					DerivedParseResult pr = null;
					try {
						pr = p.parse(text);
					} catch (Exception ex) {
						pr = defaultParser.parse(text);
					}
					parses.add(pr);
					List<Record> records = pr.getDerivation().getRecords();

					for (Record rec : records) {
						// System.out.println(rec.getTable());
						for (BaseFieldValue fv : rec.getFields()) {
							System.out.print("   " + fv.getField() + " = ");
							if (fv instanceof FieldValue) {
								for (Token t : ((FieldValue) fv).getAttributes()) {
									System.out.print(t.getValue());
								}
							} else {
								System.out.print(fv.getValue());
							}
							System.out.println();
						}
					}
				} catch (Exception ex) {
					System.err.println(ex.getMessage());
					exceptions.add(ex);
					System.out.println();
				}
				System.out.println();
			}
			parsings.add(new Parsing(null, pretext, new ArrayList<ParseResult>(parses), exceptions));
		}
		AnalyzeParseResultSet a = new AnalyzeParseResultSet();
		Map<String, Object> analysis = a.analyze(parsings);

		System.out.println();
		System.out.println();
		System.out.println();

		System.out.println(analysis.get(AnalyzeParseResultSet.TOTAL_ROWS));
		System.out.println(analysis.get(AnalyzeParseResultSet.MAX_RECORDS));
		System.out.println(analysis.get(AnalyzeParseResultSet.FIELDS));

		ParseResultsFormatter f = new ParseResultsFormatter(parsings, true, sorter);
		List<String[]> tbl = f.generateTable();
		for (String[] line : tbl) {
			for (String col : line) {
				System.out.print(col + "\t");
			}
			System.out.println();
		}

		if (fileName != null) {
			//writeTableToCSVFile(fileName, f);
			f.writeTableToCSVFile(fileName);
		}
		
		JTable jt = new JTable(f.generateTableModel());
		jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		JFrame jf = new JFrame();
		JScrollPane sp = new JScrollPane(jt,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jf.getContentPane().add(sp);
		jf.pack();
		jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		jf.setVisible(true);

		System.out.println(rows.getMetaData().getColumnCount());

	}

	static void checkRegExStuff() {
		//Pattern p = Pattern.compile("(\\p{Upper})(\\p{Upper})");
		boolean m = Pattern.matches("(\\p{Upper})(\\p{Upper})", "RK");
		System.out.println("(\\p{Upper})(\\p{Upper}), RK" + ": " +  m);
		String rk = "RK Jones";
		m = rk.matches("(\\p{Upper})(\\p{Upper})");
		rk.replaceAll("(\\p{Upper})(\\p{Upper})", "$1 $2");
		System.out.println(rk.replaceAll("(\\p{Upper})(\\p{Upper})", "$1 $2"));
		System.out.println(rk);
		m = Pattern.matches("(\\p{Upper})\\1", "RK");
		m = Pattern.matches("(\\p{Upper})\\1", "RR");

	}
	
	static void checkDefaultAgentParse() throws Exception {
		String q = "`tra la la`";
		System.out.println(q.substring(1, q.length()-1));
		Parser[] parsers = {Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_default.xml"))};
		String[] toparse = {"\"what ever a you want\"", "any damn thing", "AT all, whatsoever; regardless & heedless",
				"\"FJS, PP & KJ Sulak"};
		parseSomeStuff(toparse, parsers, false, false);
	}
	
	/**
	 * @param args
	 */
	static public void main(String[] args) {
		try {

//			parseSomeIVPAgentsToXml("localhost", "flaip", "root", "root",
//				"tbl_publications", "publication_ID", "author_name",  "D:/data/florida/ivp/author_name.xml");
//
//			parseSomeIVPAgentsToXml("localhost", "flaip6", "root", "root",
//					"referencework", "ReferenceWorkID", "Text1",  "D:/data/florida/ivp/refauth.xml");

			//parseSomeIVPAgentsToXml("localhost", "flaip6", "root", "root",
			//		"determination", "DeterminationID", "Text1",  "D:/data/florida/ivp/identifier.xml");

			//parseSomeIVPAgentsToXml("localhost", "flaip6", "root", "root",
			//		"collectingevent", "CollectingEventID", "VerbatimLocality",  /*/home/timo/*/"D:/data/florida/ivp/collector.xml");

			
//			parseSomeIVPAgents("flaip", "collector", "tbl_master", "D:/data/florida/ivp/collector.csv",
//					new Comparator<Parsing>() {
//
//						/* (non-Javadoc)
//						 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
//						 */
//						@Override
//						public int compare(Parsing arg0, Parsing arg1) {
//							String input0 = arg0.getInput();
//							String input1 = arg1.getInput();
//							Boolean hasSlash0 = input0.indexOf("/") != -1;
//							Boolean hasSlash1 = input1.indexOf("/") != -1;
//							if (!hasSlash0.equals(hasSlash1)) {
//								return hasSlash1.compareTo(hasSlash0);
//							}
//							Integer parses0 = arg0.getParses().size();
//							Integer parses1 = arg1.getParses().size();
//							if (!parses0.equals(parses1)) {
//								return parses0.compareTo(parses1);
//							} else {
//								return input0.compareTo(input1);
//							}
//						}
//				
//			});
			//parseSomeIVPAgents("flaip", "identified_by", "tbl_master", null);
			//parseSomeIVPAgents("flaip", "author_name", "tbl_publications", null);
			//parseSomeAgents();
			
			
			//parseIvpCollectorFromParsesStoredInCsv("localhost", "flaip6", "root", "root",
			//		"collectingevent", "CollectingEventID", "VerbatimLocality", "", "D:/data/florida/ivp/collector3.csv"); 
			
			//parseSomeDates();
			//parseSomeFlishDates();
			
			//checkRegExStuff();
			//checkDefaultAgentParse();
			//parseSomeFishAgents("flish", "collector", "locality", "D:/data/florida/fish/collector.csv", null);
			//parseFishLoanAuthorizedByAgent();
			//parseFishLoanStudentAgent();
			//parseFishGiftAuthorizedByAgent();
			//parseFishGiftStudentAgent();
			//parseFishBorrowAuthorizedByAgent();
			//parseFishBorrowStudentAgent();
			
			//parseFishDeterminerAgent();
			//parseFishCollector();
			
			//parseFlaHerpCollector();
			//parseFlaHerpNewCollectors();
			//parseFlaHerpDonor();
			
			//parseKuBirdCollector();
			//parseIvpEnteredByAgent();
			//parseIvpCollector();
			//parseIvpAuthor();
			
			//parseMammalLoanAuthorizedByAgent();
			//parseMammalGiftAuthorizedByAgent();
			//parseMammalLoanSecondaryBorrowerAgent();
			//parseMammalGiftSecondaryBorrowerAgent();
			//parseMammalBorrowAuthorizedByAgent();
			//parseMammalBorrowSecondaryBorrowerAgent();
			//parseMammalShipmentShippedByAgent(); 
			//parseMammalCollector();
			
			//parseSomeFishAgents("flish", "StudentName", "transaction table", "D:/data/florida/fish/authorizedby.csv", null);
			//parseSomeFishAgents("flish6", "featureorbasis", "determination", "D:/data/florida/fish/determiner.csv", null);
			
			//parseFlaPaleoBotCollector();
			
			//parseKentBryoCollector();
			//parseKentBryoAgent();
			//parseKentPlantCollector();
			
			//parseFlaIzCollector();
			//parseFlaIzDeterminer();
			//parseFlaIzDonor();
			//parseFlaIzCataloger();
			//parseFlaIzAccDonor();
			
			//parseFlaPbCollector();
			//parseFlaPbDeterminer();
			//parseFlaPbAuthor();
			//parseFlaPbAttention();
			
			//parseFlaVpCollector();
			//parseFlaVpDeterminer();
			//parseFlaVpEnteredBy();
			//parseFlaVpAccessionDonor();
			//parseFlaVpAccessionDonorTwo();
			
			//parseFlaBirdCollectorWithNumbers();
			//parseFlaBirdCollectorWithoutNumbers();
			//parseFlaBirdPreppers();
			
			//parseFlaGrrDeterminers();
			//parseFlaGrrCollectors();

			//parseUWFCCollectors();
			parseUWFCDeterminers();
			//parseWFcollector();
			//parseWFGeoReffer();
			//parseWFSkincollector();
			//parseWFPreparator();
			
			//parseKuMammAgents();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static void parseMammalLoanAuthorizedByAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "authorized_by", 
				"`transaction table` t inner join flamam6.loan l on l.LoanNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/loanauthorizedby.csv", null);
	}

	static void parseMammalLoanSecondaryBorrowerAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "secondary_borrower", 
				"`transaction table` t inner join flamam6.loan l on l.LoanNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/loansecondaryborrower.csv", null);
	}

	static void parseMammalGiftAuthorizedByAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "authorized_by", 
				"`transaction table` t inner join flamam6.gift l on l.GiftNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/giftauthorizedby.csv", null);
	}

	static void parseMammalCollector() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamamnew", "collector", 
				"`mammalogy master table`", 
				"D:/data/florida/mammals/collector_2.csv", null);
//		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam6", "text4", 
//				"collectingeventattribute", 
//				"D:/data/florida/mammals/collector.csv", null);
	}

	static void parseMammalGiftSecondaryBorrowerAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "secondary_borrower", 
				"`transaction table` t inner join flamam6.gift g on g.GiftNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/giftsecondaryborrower.csv", null);
	}

	static void parseMammalBorrowAuthorizedByAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "authorized_by", 
				"`transaction table` t inner join flamam6.borrow l on l.InvoiceNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/borrowauthorizedby.csv", null);
	}

	static void parseMammalBorrowSecondaryBorrowerAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam", "secondary_borrower", 
				"`transaction table` t inner join flamam6.borrow l on l.InvoiceNumber = t.`Invoice Number`", 
				"D:/data/florida/mammals/borrowsecondaryborrower.csv", null);
	}

	static void parseMammalShipmentShippedByAgent() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5_mammal.xml", "flamam6", "text1", 
				"shipment", 
				"D:/data/florida/mammals/shipmentshippedby.csv", null);
	}

	static void parseIvpEnteredByAgent() throws Exception {
		parseSomeAgents("flaip", "entered_by", 
				"tbl_master", 
				"D:/data/florida/ivp/tbl_master_entered_by_2.csv", null);
	}

	static void parseIvpCollector() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5.xml", "flaip", "collector", 
				"tbl_master", 
				"D:/data/florida/ivp/tbl_master_collector_2.csv", null);
	}

	static void parseIvpAuthor() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_5.xml", "flaip6", "text1", 
				"referencework", 
				"D:/data/florida/ivp/pubauths_2.csv", null);
	}

	static void parseFishLoanAuthorizedByAgent() throws Exception {
		parseSomeAgents("flish", "AuthorizedBy", 
				"`transaction table` t inner join flish6.loan l on l.LoanNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/loanauthorizedby.csv", null);
	}
	
	static void parseFishGiftAuthorizedByAgent() throws Exception {
		parseSomeAgents("flish", "AuthorizedBy", 
				"`transaction table` t inner join flish6.gift l on l.GiftNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/giftauthorizedby.csv", null);
	}

	static void parseFishBorrowAuthorizedByAgent() throws Exception {
		parseSomeAgents("flish", "AuthorizedBy", 
				"`transaction table` t inner join flish6.borrow l on l.InvoiceNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/borrowauthorizedby.csv", null);
	}

	static void parseFishLoanStudentAgent() throws Exception {
		parseSomeAgents("flish", "StudentName", 
				"`transaction table` t inner join flish6.loan l on l.LoanNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/LoanStudentName.csv", null);
	}

	static void parseFishGiftStudentAgent() throws Exception {
		parseSomeAgents("flish", "StudentName", 
				"`transaction table` t inner join flish6.gift l on l.GiftNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/GiftStudentName.csv", null);
	}

	static void parseFishBorrowStudentAgent() throws Exception {
		parseSomeAgents("flish", "StudentName", 
				"`transaction table` t inner join flish6.borrow l on l.InvoiceNumber = t.`Invoice Number`", 
				"D:/data/florida/fish/Live/BorrowStudentName.csv", null);
	}

	static void parseFishDeterminerAgent() throws Exception {
//		parseSomeAgents("flish6", "FeatureOrBasis", 
//				"determination", 
//				"D:/data/florida/fish/Live/Determiner.csv", null);
		parseSomeAgents("flish6", "text2", 
				"determination", 
				"D:/data/florida/fish/Live/Determiner.csv", null);
	}

	static void parseFishCollector() throws Exception {
//		parseSomeAgents("flish6", "FeatureOrBasis", 
//				"determination", 
//				"D:/data/florida/fish/Live/Determiner.csv", null);
		parseSomeAgents("flish6", "enddateverbatim", 
				"collectingevent", 
				"D:/data/florida/fish/Live/NewCollectors.csv", null);
	}

	static void parseFlaGrrDeterminers() throws Exception {
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml", 
				"grrmy", "who", 
				"miners", 
				"/home/timo/datas/florida/grr/determiner_parse.csv", 
				null);
	}

	static void parseFlaGrrCollectors() throws Exception {
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml", 
				"grrsp", "text2", 
				"collectingevent", 
				"/home/timo/datas/florida/grr/collector_parse.csv", 
				null);
	}

	static void parseUWFCCollectors() throws Exception {
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml",
		parseSomeAgents("/home/timo/convertionutils/src/agentparser_5_mammal.xml",
				"uwfcsp", "text2",
				"collectingevent",
				"/home/timo/datas/uwfc/collector_parse.csv",
				null);
	}

	static void parseUWFCDeterminers() throws Exception {
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml",
		parseSomeAgents("/home/timo/convertionutils/src/agentparser_5_mammal.xml",
				"uwfcsp", "text1",
				//"determination",
				"(select text1 from determination d left join matchingagent ma on ma.idor = d.text1 where ma.idor is null and d.text1 is not null) idors",
				"/home/timo/datas/uwfc/determiner_parse.csv",
				null);
	}

	static void parseFlaBirdCollectorWithNumbers() throws Exception {
		String selector = "select distinct text5 collate utf8_bin from florni.collectingevent where text5 is not null and text4 is not null order by 1";
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
		parseSomeAgents("/run/media/timo/convertionutils/src/agentparser_5_mammal.xml",
				"florni", "Text5", 
				"collectingevent", 
				"/home/timo/datas/florida/birds/CollectorsWithNums.csv", 
				null, selector, null);
	}

	static void parseFlaBirdCollectorWithoutNumbers() throws Exception {
		String selector = "select distinct text1 collate utf8_bin from florni.collectingevent where text1 is not null and text4 is null order by 1";
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml", 
				"florni", "Text1", 
				"collectingevent", 
				"/home/timo/datas/florida/birds/CollectorsWithoutNums.csv", null, selector, new FlBirdCollPreParser());
	}

	static void parseFlaBirdPreppers() throws Exception {
		//parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml", 
				"florni", "Text3", 
				"preparation", 
				"/home/timo/datas/florida/birds/prepper_parse.csv", 
				null);
	}

	static void parseFlaIzCollector() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flizix", "VerbatimLocality", 
				"collectingevent", 
				"D:/data/florida/InvertZoology/collector2.csv", null, null, null);
	}

	static void parseFlaIzDeterminer() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flizix", "Text1", 
				"determination", 
				"D:/data/florida/InvertZoology/determiner_2.csv", null, null, null);
	}

	static void parseFlaIzDonor() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flizix", "Text1", 
				"conservevent", 
				"D:/data/florida/InvertZoology/donor.csv", null, null, null);
	}

	static void parseFlaIzAccDonor() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"z36", "Remarks", 
				"accessionagent", 
				"/home/timo/datas/florida/iz/live/accdonor.csv", null, null, null);
	}

	static void parseFlaIzCataloger() throws Exception {
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flizix", "Name", 
				"collectionobject", 
				"D:/data/florida/InvertZoology/cataloger.csv", null, null, null);
	}

	static void parseFlaPbCollector() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flaleob6", "Text1", 
				"collectingevent", 
				"/home/timo/datas/florida/flaleobot/collector.csv", null, null, null);
	}
	static void parseFlaPbDeterminer() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flaleob6", "Text1", 
				"determination", 
				"/home/timo/datas/florida/flaleobot/determiner.csv", null, null, null);
	}

	static void parseFlaPbAuthor() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flaleob6", "Publisher", 
				"referencework", 
				"/home/timo/datas/florida/flaleobot/author.csv", null, null, null);
	}

	static void parseFlaPbAuxEnteredBy() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flaleob6", "ReservedText", 
				"collectionobject", 
				"/home/timo/datas/florida/flaleobot/auxenteredby.csv", null, null, null);
	}

	static void parseFlaPbAttention() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", 
				"flaleob6ly", "SpecialConditions", 
				"loan", 
				"/home/timo/datas/florida/flaleobot/lively/attention.csv", null, null, null);
	}

	
	static void parseFlaHerpCollector() throws Exception {
		parseSomeAgents("flerp", "Collector", 
				"catalog", 
				"D:/data/florida/herps/Collector.csv", null);
	}

	static void parseFlaVpEnteredBy() throws Exception {
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "Availability", 
//				"collectionobject", 
//				"/home/timo/datas/florida/vp/enteredby.csv", null, null, null);
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "enterer", 
//				"enterers", 
//				"/home/timo/datas/florida/vp/parsed_enteredby_from_rh.csv", null, null, null);

		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"vpag", "entered_by", 
				"entered_by", 
				"/home/timo/datas/florida/vp/vp_entered_by_for_go_live.csv", null, null, null);
}

	static void parseFlaVpCollector() throws Exception {
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "Text2", 
//				"collectingevent", 
//				"/home/timo/datas/florida/vp/collector.csv", null, null);

//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "coll", 
//				"vpcolls", 
//				"/home/timo/datas/florida/vp/vp_updated_collectors_for_correction.csv", null, null, new FlaVpCollPreParser());

		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"vpag", "collector", 
				"collector", 
				"/home/timo/datas/florida/vp/vp_collectors_for_go_live.csv", null, null, new FlaVpCollPreParser());
}

	static void parseFlaVpAccessionDonor() throws Exception {
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "Text3", 
//				"accession", 
//				"/home/timo/datas/florida/vp/accessiondonor.csv", null, null, null);
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"vpag", "donor_name", 
				"accession_donors", 
				"/home/timo/datas/florida/vp/accession_donor_go_live.csv", null, null, null);
	}

	static void parseFlaVpAccessionDonorTwo() throws Exception {
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "VerbatimDate", 
//				"accession", 
//				"/home/timo/datas/florida/vp/accessiondonortwo.csv", null, null, null);
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"vpag", "second_donor_name", 
				"accession_donors", 
				"/home/timo/datas/florida/vp/accession_second_donor_go_live.csv", null, null, null);
	}

	static void parseWFcollector() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml", 
				"wf", "verbatimlocality", 
				"collectingevent", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector.csv", null, null, null);
	}

	static void parseWFSkincollector() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml", 
				"wf", "reservedtext1", 
				"collectingevent", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector_skin.csv", null, null, null);
	}

	static void parseWFPreparator() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml", 
				"wf", "remarks", 
				"preparation", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/preparator.csv", null, null, null);
	}

	static void parseWFGeoReffer() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggGeoReffer.xml", 
				"wf", "Text2", 
				"geocoorddetail", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/georeffer.csv", null, null, null);
	}

	static void parseFlaVpDeterminer() throws Exception {
//		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
//				"flvp6", "Text1", 
//				"determination", 
//				"/home/timo/datas/florida/vp/identifier.csv", null, null, null);
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"flvp6", "identer", 
				"identers", 
				"/home/timo/datas/florida/vp/parsed_identifier_from_rh.csv", null, null, null);
	}

	static void parseKuMammAgents() throws Exception {
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml", 
				"kumamm", "lastname", 
				"agent", 
				"/home/timo/datas/ku/mammals/parsed_agents.csv", null, null, null);
	}
	static void parseKuBirdCollector() throws Exception {
		parseSomeAgents("birdv", "Collector", 
				"master", 
				"D:/data/KU/birds/Collector.csv", null);
	}

	static void parseLsuHerbCollector() throws Exception {
		parseSomeAgents("lsuherb", "remarks", "collector", "D:/data/lsu/LSUHerb/collectors.csv", null);
	}

	static void parseKentBryoCollector() throws Exception {
		//parseSomeAgents("kentbryo", "remarks", "collector", "D:/data/KentState/Bryo/collectors.csv", null);
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", "kentbryo", "remarks", "collector", 
				"D:/data/KentState/Bryo/collectors.csv",
				null, null, null);
	}
	
	static void parseKentBryoAgent() throws Exception {
		//parseSomeAgents("kentbryo", "remarks", "collector", "D:/data/KentState/Bryo/collectors.csv", null);
		parseSomeAgents("/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml", "kentbryo", "LastName", "agent", 
				"/home/timo/datas/kentstate/bryo/agents.csv",
				null, null, null);
	}

	static void parseKentPlantCollector() throws Exception {
		//parseSomeAgents("kentbryo", "remarks", "collector", "D:/data/KentState/Bryo/collectors.csv", null);
		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", "kentvp", "remarks", "collector", 
				"D:/data/KentState/VascPlant/collectors.csv",
				null, null, null);
	}

	static void parseFlaHerpNewCollectors() throws Exception {
		parseSomeAgents("flerp6", "Collector", 
				"collectingevent", 
				"D:/data/florida/herps/live/NewCollector.csv", null,
				"select distinct VerbatimLocality from collectionobject co inner join collectingevent ce " +
				"on ce.collectingeventid = co.collectingeventid inner join flerp.catalog c " + 
				"on c.catnum = co.catalognumber left join flerp_1.catalog c1 " + 
				"on c1.catnum = c.catnum left join collector cl " +
				"on cl.collectingeventid = ce.collectingeventid " +
				"where VerbatimLocality is not null and cl.collectorid is null and " +
				"(c1.catnum is null or (isnull(c1.collector) != isnull(c.collector) or c1.collector != c.collector)) " +
				"order by 1");
	}

	static void parseFlaHerpDonor() throws Exception {
		parseSomeAgents("flerp", "Donor", 
				"catalog", 
				"D:/data/florida/herps/Donor.csv", null);
	}

	static void parseFlaPaleoBotCollector() throws Exception {
		parseSomeAgents("flaleobot", "collector", 
				"tbl_pb_catalog", 
				"D:/data/florida/paleobotany/pbCollectors.csv", null);
	}

	/*static public void main(String[] args) {
		String serverName = "localhost";
		String dbName = "fishdata_2003";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = getConnectionStr(dbName, serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		try {

			//Parser pLastNameList = buildAgentParser(false, false);
			//Parser p1 = buildAgentParser(true, false);
			Parser pLastNameList =  Parser.fromXML(new File("D:/data/florida/fish/agentparser_2.xml"));
			Parser p1 = Parser.fromXML(new File("D:/data/florida/fish/agentparser_1.xml"));
			Parser ps = Parser.fromXML(new File("D:/data/florida/fish/simpleparser.xml"));
			Parser pd = Parser.fromXML(new File("D:/data/florida/fish/DateParseTest.xml"));
			try {
				ParseResult pr;
				List<Record> records;

				// pr = p.parse("Lone, Pepe");
				// pr = p.parse("Lone; Pepe");
				// pr = p.parse("Depp, Johnny; Cash, Johnny; Hull, Bobby");
				// pr = p.parse("J.I. Happenstance");
				// pr = p.parse("Duellman");
				// pr = p.parse("Winston Purple");
				// pr = p.parse("J. I. Happenstance");
				// pr = p.parse("BRC");

				// pr = p.parse("Wayne Dwayne Elliston");
				// pr = p.parse("Wayne D. Elliston");
				// pr = p.parse("Simmons, John E.");

				// pr = p.parse("Duellman; Pinky Schubert; BRC & Bobby Hull");

				// commas are ambiguous when rule r15 is included - should be
				// able to produce alternate parses of the following...
				// pr = p.parse("Duellman, Pinky Schubert, BRC & Bobby Hull",
				// false);
				// pr = p.parse("Simmons, John Edward", true);

				// pr = p.parse("AGNEW,J. & WILSON,S.", true);
				//pr = pLastNameList
				//		.parse("AGNEW, PORTELL, HECHT & TOOMEY", true);
				// pr = p.parse("AGNEW, PORTELL, HECHT & TOOMEY", false);

				//pr = pd.parse("1945 12 31");
				//pr = pd.parse("31 January 1945");
				pr = pd.parse("12 17 1999");
				pr.getDerivation().print(0);

				records = pr.getDerivation().getRecords();

				for (Record rec : records) {
					// System.out.println(rec.getTable());
					for (FieldValue fv : rec.getFields()) {
						System.out.print("   " + fv.getField() + " = ");
						//for (Token t : fv.getAttributes()) {
						//	System.out.print(t.getValue());
						//}
						//System.out.println();
						System.out.println(fv.getValue());
					}
				}

				// pr = p.parse("A.P. & M. H.");
				// pr.getDerivation().print(0);
				// System.exit(0);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

			Parser[] parsers = { p1, pLastNameList };
			Class.forName(dbDriver);
			Connection con = DriverManager.getConnection(dbConnectionStr,
					dbUsername, dbPassword);
			Statement stmt = con.createStatement();
			//ResultSet rows = stmt.executeQuery("select distinct collector from tbl_master where collector is not null order by 1");
			ResultSet rows = stmt.executeQuery("select distinct collector from locality where collector is not null order by 1");
			//ResultSet rows = stmt.executeQuery("select distinct identified_by from tbl_master where identified_by is not null order by 1");
			//ResultSet rows = stmt.executeQuery("select distinct author_name from tbl_publications where author_name is not null order by 1");
			int c = 0;
			List<Parsing> parsings = new ArrayList<Parsing>();
			while (rows.next() && c++ < 50000) {
				String text = rows.getString(1);
				System.out.println(text);
				List<ParseResult> parses = new ArrayList<ParseResult>();
				List<Exception> exceptions = new ArrayList<Exception>();
				for (Parser p : parsers) {
					try {
						ParseResult pr = p.parse(text);
						parses.add(pr);
						List<Record> records = pr.getDerivation().getRecords();

						for (Record rec : records) {
							// System.out.println(rec.getTable());
							for (FieldValue fv : rec.getFields()) {
								System.out.print("   " + fv.getField() + " = ");
								for (Token t : fv.getAttributes()) {
									System.out.print(t.getValue());
								}
								System.out.println();
							}
						}
					} catch (Exception ex) {
						System.err.println(ex.getMessage());
						exceptions.add(ex);
						System.out.println();
					}
					System.out.println();
				}
				parsings.add(new Parsing(text, parses, exceptions));
			}
			AnalyzeParseResultSet a = new AnalyzeParseResultSet();
			Map<String, Object> analysis = a.analyze(parsings);
			
			System.out.println();
			System.out.println();
			System.out.println();
			
			System.out.println(analysis.get(AnalyzeParseResultSet.TOTAL_ROWS));
			System.out.println(analysis.get(AnalyzeParseResultSet.MAX_RECORDS));
			System.out.println(analysis.get(AnalyzeParseResultSet.FIELDS));
			
			ParseResultsFormatter f = new ParseResultsFormatter(parsings);
			List<String[]> tbl = f.generateTable();
			for (String[] line : tbl) {
				for (String col : line) {
					System.out.print(col + "\t");
				}
				System.out.println();
			}
			
			JTable jt = new JTable(f.generateTableModel());
			jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			JFrame jf = new JFrame();
			//JPanel jp = new JPanel(new BorderLayout());
			//jp.add(jt, BorderLayout.CENTER);
			JScrollPane sp = new JScrollPane(jt, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
			//sp.add(jt);
			jf.getContentPane().add(sp);
			jf.pack();
			jf.setVisible(true);
			
			
			System.out.println(rows.getMetaData().getColumnCount());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}*/

	/*
	 * static public void main(String[] args) { // String serverName =
	 * "localhost"; // String dbName = "oduvhe"; // String dbUsername = "root";
	 * // String dbPassword = "hyla606"; // String dbConnectionStr =
	 * "jdbc:mysql://" + serverName + "/" + dbName +
	 * "?characterEncoding=UTF-8&autoReconnect=true"; // String dbDriver =
	 * "com.mysql.jdbc.Driver";
	 * 
	 * try {
	 * 
	 * Symbol agents = new Symbol("Agents"); Symbol agent = new Symbol("Agent",
	 * "agent", null); Symbol lastName = new Symbol("LastName", "agent",
	 * "LastName"); Symbol firstName = new Symbol("FirstName", "agent",
	 * "FirstName"); Symbol middle = new Symbol("Middle", "agent",
	 * "MiddleInitial"); Symbol suffix = new Symbol("Suffix"); Symbol initial =
	 * new Symbol("Initial"); Symbol abbreviation = new Symbol("Abbreviation",
	 * "agent", "Abbreviation"); Symbol name = new Symbol("Name"); Symbol
	 * connector = new Symbol("Connector"); Symbol str1 = new Symbol("Str1",
	 * true); Symbol str = new Symbol("Str", true); Symbol upStr3 = new
	 * Symbol("UpStr3", true); Symbol comma = new Symbol(",", true); Symbol and
	 * = new Symbol("and", true); Symbol ampersand = new Symbol("&", true);
	 * Symbol semicolon = new Symbol(";", true); Symbol junior = new
	 * Symbol("jr", true);
	 * 
	 * Vector<Symbol> right = new Vector<Symbol>(); right.add(agent);
	 * right.add(connector); right.add(agents); Rule r1 = new Rule(agents,
	 * right);
	 * 
	 * right = new Vector<Symbol>(); right.add(agent); Rule r2 = new
	 * Rule(agents, right);
	 * 
	 * 
	 * right = new Vector<Symbol>(); right.add(firstName); right.add(lastName);
	 * Rule r3 = new Rule(agent, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(abbreviation); Rule r4 = new
	 * Rule(agent, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(lastName); Rule r5 = new
	 * Rule(agent, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(lastName); right.add(comma);
	 * right.add(firstName); Rule r6 = new Rule(agent, right);
	 * 
	 * // right = new Vector<Symbol>(); // right.add(name); //
	 * //right.add(suffix); // Rule r7 = new Rule(lastName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(name); Rule r8 = new
	 * Rule(lastName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(name); Rule r9 = new
	 * Rule(firstName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(name); right.add(middle); Rule
	 * r10 = new Rule(firstName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(initial); Rule r18 = new
	 * Rule(firstName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(initial); right.add(middle); Rule
	 * r19 = new Rule(firstName, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(name); Rule r11 = new
	 * Rule(middle, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(initial); Rule r12 = new
	 * Rule(middle, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(initial); right.add(initial);
	 * Rule r13 = new Rule(middle, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(junior); Rule r14 = new
	 * Rule(suffix, right);
	 * 
	 * //With this included, 'Lone, Pepe' will be parsed as 2 agent last names
	 * instead of 'LastName, FirstName. //Need to get it to offer both
	 * possibilities... right = new Vector<Symbol>(); right.add(comma); Rule r15
	 * = new Rule(connector, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(and); Rule r16 = new
	 * Rule(connector, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(ampersand); Rule r17 = new
	 * Rule(connector, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(semicolon); Rule r23 = new
	 * Rule(connector, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(str1); Rule r20 = new
	 * Rule(initial, right);
	 * 
	 * right = new Vector<Symbol>(); right.add(str); Rule r21 = new Rule(name,
	 * right);
	 * 
	 * 
	 * right = new Vector<Symbol>(); right.add(upStr3); Rule r22 = new
	 * Rule(abbreviation, right);
	 * 
	 * Map<String, Map<String, Vector<Rule>>> table = new HashMap<String,
	 * Map<String, Vector<Rule>>>(); Map<String, Vector<Rule>> entry = new
	 * HashMap<String, Vector<Rule>>(); Vector<Rule> entryRules = new
	 * Vector<Rule>();
	 * 
	 * entryRules.add(r1);
	 * 
	 * entryRules.add(r2); entry.put("Str1", entryRules); entry.put("UpStr3",
	 * entryRules); entry.put("Str", entryRules); table.put("Agents", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r3); entry.put("Str1", entryRules);
	 * entryRules = new Vector<Rule>(); entryRules.add(r4); entry.put("UpStr3",
	 * entryRules); entryRules = new Vector<Rule>(); entryRules.add(r3);
	 * entryRules.add(r5); entryRules.add(r6); entry.put("Str", entryRules);
	 * table.put("Agent", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); //entryRules.add(r7); entryRules.add(r8);
	 * entry.put("Str", entryRules); table.put("LastName", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r18); entryRules.add(r19);
	 * entry.put("Str1", entryRules); entryRules = new Vector<Rule>();
	 * entryRules.add(r9); entryRules.add(r10); entry.put("Str", entryRules);
	 * table.put("FirstName", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r12); entryRules.add(r13);
	 * entry.put("Str1", entryRules); entryRules = new Vector<Rule>();
	 * entryRules.add(r11); entry.put("Str", entryRules); table.put("Middle",
	 * entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r14); entry.put("jr", entryRules);
	 * table.put("Suffix", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r20); entry.put("Str1", entryRules);
	 * table.put("Initial", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r21); entry.put("Str", entryRules);
	 * table.put("Name", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>(); entryRules.add(r22); entry.put("UpStr3", entryRules);
	 * table.put("Abbreviation", entry);
	 * 
	 * entry = new HashMap<String, Vector<Rule>>(); entryRules = new
	 * Vector<Rule>();
	 * 
	 * //see comment at declaration of r15 entryRules.add(r15); entry.put(",",
	 * entryRules);
	 * 
	 * entryRules = new Vector<Rule>(); entryRules.add(r16); entry.put("and",
	 * entryRules); entryRules = new Vector<Rule>(); entryRules.add(r17);
	 * entry.put("&", entryRules); entryRules = new Vector<Rule>();
	 * entryRules.add(r23); entry.put(";", entryRules); table.put("Connector",
	 * entry);
	 * 
	 * String[] terminators = {".", ",", ";", " "}; String[] literals = {"and",
	 * "&", "Jr", ",", ";", " "};
	 * 
	 * Parser p = new Parser(r1.getLeft(), terminators, literals, new
	 * ParseTable(table)); try { ParseResult pr; List<Record> records;
	 * 
	 * //pr = p.parse("Lone, Pepe"); //pr = p.parse("Lone; Pepe"); //pr =
	 * p.parse("Depp, Johnny; Cash, Johnny; Hull, Bobby"); //pr =
	 * p.parse("J.I. Happenstance"); //pr = p.parse("Duellman"); //pr =
	 * p.parse("Winston Purple"); //pr = p.parse("J. I. Happenstance"); //pr =
	 * p.parse("BRC");
	 * 
	 * //pr = p.parse("Wayne Dwayne Elliston"); //pr =
	 * p.parse("Wayne D. Elliston"); //pr = p.parse("Simmons, John E.");
	 * 
	 * //pr = p.parse("Duellman; Pinky Schubert; BRC & Bobby Hull");
	 * 
	 * //commas are ambiguous when rule r15 is included - should be able to
	 * produce alternate parses of the following... //pr =
	 * p.parse("Duellman, Pinky Schubert, BRC & Bobby Hull", false); //pr =
	 * p.parse("Simmons, John Edward", true);
	 * 
	 * //pr = p.parse("AGNEW,J. & WILSON,S.", true); pr =
	 * p.parse("AGNEW, PORTELL, HECHT & TOOMEY", true); //pr =
	 * p.parse("AGNEW, PORTELL, HECHT & TOOMEY", false);
	 * 
	 * pr.getDerivation().print(0);
	 * 
	 * 
	 * records = pr.getDerivation().getRecords();
	 * 
	 * for (Record rec : records) { System.out.println(rec.getTable()); for
	 * (FieldValue fv : rec.getFields()) { System.out.print("   " +
	 * fv.getField() + " = "); for (Token t : fv.getAttributes()) {
	 * System.out.print(t.getValue()); } System.out.println(); } }
	 * 
	 * 
	 * //pr = p.parse("A.P. & M. H."); //pr.getDerivation().print(0);
	 * System.exit(0); } catch (Exception e) { e.printStackTrace();
	 * System.exit(-1); }
	 * 
	 * // Class.forName(dbDriver); // Connection con =
	 * DriverManager.getConnection(dbConnectionStr, dbUsername, dbPassword); //
	 * Statement stmt = con.createStatement(); // ResultSet rows =
	 * stmt.executeQuery
	 * ("select distinct collrteam_text from tblcollection order by 1"); //
	 * while (rows.next()) // { // String text = rows.getString(0); // String[]
	 * people = null; // if (text.contains(" and ")) // { // people =
	 * text.split(" and "); // } // else if (text.contains("&")) // { // people
	 * = text.split("&"); // } // } //
	 * System.out.println(rows.getMetaData().getColumnCount()); } catch
	 * (Exception e) { e.printStackTrace(); System.exit(-1); }
	 * 
	 * }
	 */

	// private class Person
	// {
	// String first;
	// String last;
	// String middle;
	//
	// public Person(String first, String last, String middle) {
	// super();
	// this.first = first;
	// this.last = last;
	// this.middle = middle;
	// }
	// /**
	// * @return the first
	// */
	// public String getFirst() {
	// return first;
	// }
	// /**
	// * @param first the first to set
	// */
	// public void setFirst(String first) {
	// this.first = first;
	// }
	// /**
	// * @return the last
	// */
	// public String getLast() {
	// return last;
	// }
	// /**
	// * @param last the last to set
	// */
	// public void setLast(String last) {
	// this.last = last;
	// }
	// /**
	// * @return the middle
	// */
	// public String getMiddle() {
	// return middle;
	// }
	// /**
	// * @param middle the middle to set
	// */
	// public void setMiddle(String middle) {
	// this.middle = middle;
	// }
	//
	//
	// }
}
