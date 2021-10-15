/**
 * 
 */
package utils.populate;

import java.io.File;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import utils.parse.ParseResult;
import utils.parse.Parser;
import utils.parse.Parsing;
import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class PreParsedFieldParserToXML extends FieldParserToXml {
	protected final String preParseFile;
	protected final char escapeChar;
	
	/**
	 * @param server
	 * @param db
	 * @param user
	 * @param pw
	 * @param tbl
	 * @param keyFld
	 * @param fld
	 * @param outputFile
	 */
	public PreParsedFieldParserToXML(String server, String db, String user,
			String pw, String tbl, String keyFld, String fld, String outputFile,
			String preParseFile, List<Parser> parsers, char escapeChar) {
		super(server, db, user, pw, tbl, keyFld, fld, parsers, outputFile);
		this.preParseFile = preParseFile;
		this.escapeChar = escapeChar;
	}

	protected int searchForInput(List<Parsing> importedParsings, String text) {
		//binary search doesn't always work, possibly due to the tolowercase() requirement??
		int idx = -1;
//		idx = Collections.binarySearch(importedParsings,
//				new Parsing(null, text.trim(), null, null),
//				new Comparator<Parsing>() {
//
//					/*
//					 * (non-Javadoc)
//					 * 
//					 * @see java.util.Comparator#compare(java.lang.Object,
//					 * java.lang.Object)
//					 */
//					@Override
//					public int compare(Parsing arg0, Parsing arg1) {
//						return arg0.getInput().trim().toLowerCase().compareTo(arg1.getInput().trim().toLowerCase());
//					}
//
//				});
		if (idx < 0) {
			for (int i = 0; i < importedParsings.size(); i++) {
				String parsedText = importedParsings.get(i).getInput();
				if (text.trim().equals(parsedText.trim()) || text.trim().equalsIgnoreCase(parsedText.trim())) {
					idx = i;
					break;
				}
			}
		}
		return idx;
	}

	/**
	 * @param colStr
	 * @return colStr in a form more amenable to parsing by tools in convertionutils project.
	 * @throws Exception
	 */
	private static String fixFlaVpCollStrForParse(String colStr) throws Exception {
		char[] chars = colStr.toCharArray();
		int commas = 0;
		String result = "";
		for (char c : chars) {
			char next = c;
			if (',' == next) {
				if (++commas % 2 == 0) {
					next = ';';
				}
			} else if ('&' == next) {
				commas = 0;
			}
			result += next;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see utils.populate.FieldParserToXmlBase#processRows(java.sql.ResultSet)
	 */
	@Override
	protected List<Parsing> processRows(ResultSet rows) throws Exception {
		CsvImporter imp = new CsvImporter(tbl, "imp", 
				new File(preParseFile), "utf-8", ',', escapeChar, '"', true);
		List<Parsing> importedParsings = imp.getParsings();
//		for (Parsing ps : importedParsings) {
//			//if ("H Ch�vez".equalsIgnoreCase(ps.getInput())) {
//			if (ps.getInput().endsWith("vez")) {
//				System.out.println("AHA! " + ps.getInput());
//			}
//		}
		Collections.sort(importedParsings, new Comparator<Parsing>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(Parsing arg0, Parsing arg1) {
				// TODO Auto-generated method stub
				return arg0.getInput().trim().toUpperCase().compareTo(arg1.getInput().trim().toUpperCase());
			}
			
		});

		List<Parsing> parsings = new ArrayList<Parsing>();

		Set<String> unmatchedInputs = new HashSet<String>();
		
		//HACK for Fla VP!
		boolean isFlaVpLive = false/*true*/;
		
		while (rows.next()) {
			String text = rows.getString(1);
			//System.out.println(text);
			int idx = searchForInput(importedParsings, text);
			if (idx < 0) {
				System.out.println("Oh NO! No match for " + text);
				unmatchedInputs.add(text);
				String textToParse = text;
				if (isFlaVpLive) {
					textToParse = fixFlaVpCollStrForParse(text);
				}
				if (parsers != null && parsers.size() > 0) {
					Pair<Set<ParseResult>, List<Exception>> rowParse = parseRow(textToParse);
					parsings.add(new Parsing(rows.getInt(2), text,
						new ArrayList<ParseResult>(rowParse.getFirst()),
						rowParse.getSecond()));
				}
			} 
			if (idx >= 0)
			{
				Parsing importedParsing = importedParsings.get(idx);
				parsings.add(new Parsing(rows.getInt(2), text, importedParsing
						.getParses(), null));
			}
		}
		if (unmatchedInputs.size() > 0) {
			FileUtils.writeLines(new File(preParseFile + "_UNMATCHED.txt"),"utf-8", unmatchedInputs);
		}
		return parsings;
	}

	/**
	 * @param args
	 */
	static public void main(String[] args) {
		try {
//			fishLoanAuthorizedBy();
//			fishLoanStudentName();
//			fishGiftAuthorizedBy();
//			fishGiftStudentName();
//			fishBorrowAuthorizedBy();
//			fishBorrowStudentName();
			//fishCollectors();
			//fishDeterminers();
			
			//flaHerpCollectors();
			//flaHerpNewCollectors();
			//flaHerpDonors();
			
			//ivpCollectors();
			//ivpTblMasterEnteredBy();
			//ivpAuthors();
			//ivpIdentifiers();
			
			//flaIzCollectors();
			//flaIzDeterminers();
			//flaIzDonors();
			//flaIzCatalogers();
			//flaIzAccDonors();
			
			//flaBirdCollectorsWithNums();
			//flaBirdCollectorsWithoutNums();
			//flaBirdPreppers();
			
			//flaGrrDeterminers();
			//flaGrrCollectors();

			washingtonDeterminers();
			//washingtonCollectors();

			//kentStatePlantCollectors();
			//kentBryoAgents();
			//kentBryoCollectors();
			
			//mammalLoanAuthorizedBy();
			//mammalGiftAuthorizedBy();
			//mammalLoanSecondaryBorrower();
			//mammalGiftSecondaryBorrower();
			//mammalBorrowAuthorizedBy();
			//mammalBorrowSecondaryBorrower();
			
			//kuBirdCollectors();
			
			//flaMammalCollectors();
			
			//lsuHerbCollectors();
			
			//flaVpCollectors();
			//flaVpDeterminers();
			//flaVpEnteredBy();
			//flaVpAccessionDonor();
			//flaVpAccessionDonorTwo();
			
			//wfvzEggCollectors();
			//wfvzGeoReffers();
			//wfvzSkinCollectors();
			//wfvzPreparators();
			
			//flaPBotDeterminers();
			//flaPBotCollectors();
			//flaPBotAuthors();
			//flaPBotAttention();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	static void ivpCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				//"D:/data/florida/fish/agentparser_2.xml"));
				"C:/workspace/convertionutils/src/agentparser_5.xml"));
				//"/home/timo/workspace/convertionutils/src/agentparser_3b.xml"));
		
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaip6", "root", "root",
				"collectingevent", "CollectingEventID", "VerbatimLocality", "D:/data/florida/ivp/live/collector_out_2.xml", 
				"D:/data/florida/ivp/live/tbl_master_collector_2.csv", parsers, '\\');
		ppfp.parse();		
	}
	
	static void ivpAuthors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				//"D:/data/florida/fish/agentparser_2.xml"));
				"C:/workspace/convertionutils/src/agentparser_5.xml"));
				//"/home/timo/workspace/convertionutils/src/agentparser_3b.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaip6", "root", "root",
				"referencework", "ReferenceWorkID", "Text1", "D:/data/florida/ivp/live/refauth_2.xml", 
				"D:/data/florida/ivp/live/pubauths_2.csv", parsers, '\\');
		ppfp.parse();		
	}

	static void ivpIdentifiers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				//"D:/data/florida/fish/agentparser_2.xml"));
				"C:/workspace/convertionutils/src/agentparser_5.xml"));
				//"/home/timo/workspace/convertionutils/src/agentparser_3b.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaip6", "root", "root",
				"determination", "DeterminationID", "Text1", "D:/data/florida/ivp/live/determiner_2.xml", 
				"D:/data/florida/ivp/live/identifiers.csv", parsers, '\\');
		ppfp.parse();		
	}

	static void fishCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
//		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
//				"collectingevent", "CollectingEventID", "VerbatimLocality", "D:/data/florida/fish/Live/collectorsout2.xml", 
//				"D:/data/florida/fish/UFFparsedCols2.csv", parsers, '?');
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				"collectingevent", "CollectingEventID", "EndDateVerbatim", "D:/data/florida/fish/Live/newcollectorsout.xml", 
				"D:/data/florida/fish/Live/NewCollectors.csv", parsers, '?');
		ppfp.parse();		
	}

	static void lsuHerbCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "lsuherb", "root", "root",
				"collectingevent", "CollectingEventID", "Text1", "D:/data/lsu/LSUHerb/collectorsJSK_out.xml", 
				"D:/data/lsu/lsuherb/CollectorsJSKWith1781lines.csv", parsers, '?');
		ppfp.parse();		
	}

	static void kentBryoCollectors() throws Exception {
		
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "kentbryo", "root", "root",
				"collectingevent", "CollectingEventID", "Text2", "/home/timo/datas/kentstate/bryo/collectors_out2.xml", 
				"/home/timo/datas/kentstate/bryo/BryoCollsFinalEdited.csv", parsers, '?');
		ppfp.parse();		
	}

	static void kentBryoAgents() throws Exception {
		
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "kentbryo", "root", "root",
				"agent", "AgentID", "LastName", "/home/timo/datas/kentstate/bryo/agents_out.xml", 
				"/home/timo/datas/kentstate/bryo/agents.csv", parsers, '?');
		ppfp.parse();		
	}

	static void kentStatePlantCollectors() throws Exception {
//		parseSomeAgents("C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml", "kentvp", "remarks", "collector", 
//				"D:/data/KentState/VascPlant/collectors.csv",
//				null, null);
		
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "kentvp", "root", "root",
				"collectingevent", "CollectingEventID", "Text1", "D:/data/KentState/VascPlant/collectors_out_2.xml", 
				"D:/data/KentState/VascPlant/collectorsmodified2.csv", parsers, '?');
		ppfp.parse();		
	}

	
	static void kuBirdCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "kubirds", "root", "root",
				"collectingevent", "CollectingEventID", "Text1", "/home/timo/datas/ku/birds/collectorout.xml", 
				"/home/timo/datas/ku/birds/collector_corrections.csv", parsers, '?');
		ppfp.parse();		
	}

	static void wfvzEggCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "wf", "root", "root",
				"collectingevent", "CollectingEventID", "verbatimlocality", "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collectorout.xml", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector.csv", parsers, '?');
		ppfp.parse();		
	}

	static void wfvzSkinCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "wf", "root", "root",
				"collectingevent", "CollectingEventID", "reservedtext1", "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector_skinout.xml", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector_skin.csv", parsers, '?');
		ppfp.parse();		
	}

	static void wfvzPreparators() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggCollector.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "wf", "root", "root",
				"preparation", "PreparationID", "remarks", "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/preparatorout.xml", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/preparator.csv", parsers, '?');
		ppfp.parse();		
	}

	static void wfvzGeoReffers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_WFEggGeoReffer.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "wf", "root", "root",
				"geocoorddetail", "GeoCoordDetailID", "Text2", "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/georefferout.xml", 
				"/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/georeffer.csv", parsers, '?');
		ppfp.parse();		
	}

	static void fishDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
//		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
//				"determination", "DeterminationID", "FeatureOrBasis", "D:/data/florida/fish/Live/determinersOut.xml", 
//				"D:/data/florida/fish/UFDeterminerAgentFinalWODateFlds.csv", parsers, '?');
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				"determination", "DeterminationID", "Text2", "D:/data/florida/fish/Live/determinersOut2.xml", 
				"D:/data/florida/fish/Live/Determiner.csv", parsers, '?');
		ppfp.parse();		
	}

	static void ivpTblMasterEnteredBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "`tbl_master` m inner join flaip6.collectionobject co on co.catalognumber = m.catalog_number";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaip", "root", "root",
				tbls, "co.CollectionObjectID", "m.entered_by", "D:/data/florida/ivp/live/MasterEnteredByOut_2.xml", 
				"D:/data/florida/ivp/live/tbl_master_entered_by_2.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishLoanAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "loan l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.LoanNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.LoanID", "t.AuthorizedBy", "D:/data/florida/fish/Live/LoanAuthorizedByOut.xml", 
				"D:/data/florida/fish/Live/loanauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}
	
	static void flaHerpCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flerp6", "root", "root",
				"collectingevent", "CollectingEventID", "VerbatimLocality", "D:/data/florida/herps/live/collectorsout.xml", 
				"D:/data/florida/herps/live/Collectors_complete.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaBirdCollectorsWithNums() throws Exception {
		//Need to copy text5 to verbatimlocality before doing this, and clear verbatimlocality after populating from xml (see scratch.sql) 
		//DUDE!! change to same parser as used in AgentNameParse for bird collectors
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "florni", "root", "root",
				"collectingevent", "CollectingEventID", "verbatimlocality", "/home/timo/datas/florida/birds/collector_with_nums_parse_out.xml", 
				//"/home/timo/datas/florida/birds/CollectorsWithNumsParsed.csv", 				
				"/home/timo/datas/florida/birds/FLMNH_Birds_Collectors/CollectorsWithNumsCORRECTED.csv", 
				parsers, '?');
		ppfp.parse();		
	}

	static void flaBirdCollectorsWithoutNums() throws Exception {
		//Need to copy text5 to verbatimlocality before doing this, and clear verbatimlocality after populating from xml (see scratch.sql)
		//DUDE!! change to same parser as used in AgentNameParse for bird collectors
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "florni", "root", "root",
				"collectingevent", "CollectingEventID", "verbatimlocality", "/home/timo/datas/florida/birds/collector_without_nums_parse_out_recombined.xml", 
				//"/home/timo/datas/florida/birds/CollectorsWithoutNumsParsed.csv", 
				"/home/timo/datas/florida/birds/FLMNH_Birds_Collectors/CollectorsWithoutNumsRecombined.csv", 
				parsers, '?');
		ppfp.parse();		
	}

	static void flaBirdPreppers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "florni", "root", "root",
				"preparation", "PreparationID", "Text3", "/home/timo/datas/florida/birds/preppers_parsed_out.xml", 
				"/home/timo/datas/florida/birds/prepper_parse.csv", 
				parsers, '?');
		ppfp.parse();		
	}

	static void flaGrrDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "grrsp", "root", "root",
				"determination", "DeterminationID", "Text2", "/home/timo/datas/florida/grr/UF_GRR_agent_parse/determiner_parse_fixed_out.xml", 
				"/home/timo/datas/florida/grr/UF_GRR_agent_parse/determiner_parse_fixed.csv", 
				parsers, '?');
		ppfp.parse();		
	}

	static void washingtonDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/home/timo/convertionutils_ijerk/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		//parsers.add(p1);

		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "uwfcsp", "root", "root",
				"determination", "DeterminationID", "Text1",
				//"/home/timo/datas/washington/determiner_parse_returned.xml",
				"/home/timo/datas/washington/determiner_parse_returned_from_collectors.xml",
				//"/home/timo/datas/washington/determiner_parse_returned.csv",
				"/home/timo/datas/washington/collector_parse_kpm.csv", //collectors doubling as determiners
				parsers, '?');
		ppfp.parse();
	}

	static void washingtonCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/home/timo/convertionutils_ijerk/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);

		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "uwfcsp", "root", "root",
				"collectingevent", "CollectingEventID", "Text2", "/home/timo/datas/washington/collector_parse_returned.xml",
				"/home/timo/datas/washington/collector_parse_kpm.csv",
				parsers, '?');
		ppfp.parse();
	}

	static void flaGrrCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "grrsp", "root", "root",
				"collectingevent", "CollectingEventID", "Text2", "/home/timo/datas/florida/grr/UF_GRR_agent_parse/collector_parsed_fixed_out.xml", 
				"/home/timo/datas/florida/grr/UF_GRR_agent_parse/collector_parse_fixed.csv", 
				parsers, '?');
		ppfp.parse();		
	}

	static void flaIzCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "z36", "root", "root",
				"collectingevent", "CollectingEventID", "VerbatimLocality", "/home/timo/datas/florida/iz/live/collector_parse_out.xml", 
				"/home/timo/datas/florida/iz/collector_parse.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaIzDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "z36", "root", "root",
				"determination", "DeterminationID", "Text1", "/home/timo/datas/florida/iz/live/determiner2_out.xml", 
				"/home/timo/datas/florida/iz//determiner_2.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaIzDonors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "z36", "root", "root",
				"conservevent", "ConservEventID", "Text1", "/home/timo/datas/florida/iz/live/donor_out.xml", 
				"/home/timo/datas/florida/iz/donor.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaIzAccDonors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "z36", "root", "root",
				"accession", "AccessionID", "Text3", "/home/timo/datas/florida/iz/live/accdonor_approved_out.xml", 
				"/home/timo/datas/florida/iz/live/accdonar.csvEdits.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaIzCatalogers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "z36", "root", "root",
				"collectionobject", "CollectionObjectID", "Name", "/home/timo/datas/florida/iz/live/cataloger_out.xml", 
				"/home/timo/datas/florida/iz/cataloger.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaPBotCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaleob6ly", "root", "root",
				"collectingevent", "CollectingEventID", "Text1", "/home/timo/datas/florida/flaleobot/lively/collectorsout.xml", 
				"/home/timo/datas/florida/flaleobot/collector.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaPBotDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaleob6ly", "root", "root",
				"determination", "DeterminationID", "Text1", "/home/timo/datas/florida/flaleobot/lively/determinersout.xml", 
				"/home/timo/datas/florida/flaleobot/determiner.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaPBotAttention() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaleob6ly", "root", "root",
				"loan", "LoanID", "SpecialConditions", "/home/timo/datas/florida/flaleobot/lively/attentionout.xml", 
				"/home/timo/datas/florida/flaleobot/lively/attention.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaPBotAuthors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_kentstate_bryo.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flaleob6ly", "root", "root",
				"referencework", "ReferenceWorkID", "Publisher", "/home/timo/datas/florida/flaleobot/lively/authorsout.xml", 
				"/home/timo/datas/florida/flaleobot/agents/author.csv", parsers, '?');
		ppfp.parse();		
	}

	
	static void flaHerpNewCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flerp6", "root", "root",
				"collectingevent", "CollectingEventID", "VerbatimLocality", "D:/data/florida/herps/live/newcollectorsout.xml", 
				"D:/data/florida/herps/live/NewCollector.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaHerpDonors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_fish.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flerp6", "root", "root",
				"collectionobject", "CollectionObjectID", "Name", "D:/data/florida/herps/live/donorsout.xml", 
				"D:/data/florida/herps/live/DonorCorrected.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaMammalCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"C:/workspace/convertionutils/src/agentparser_5_mammal.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				"collectingevent", "CollectingEventID", "VerbatimLocality", "D:/data/florida/mammals/live/collectorsout.xml", 
				"D:/data/florida/mammals/live/MAM COLLECTOR.csv", parsers, '?');
		ppfp.parse();		
	}

	static void mammalLoanAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "loan l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.LoanNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.LoanID", "t.authorized_by", "D:/data/florida/mammals/live/LoanAuthorizedByOut.xml", 
				"D:/data/florida/mammals/live/loanauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalLoanSecondaryBorrower() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "loan l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.LoanNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.LoanID", "t.secondary_borrower", "D:/data/florida/mammals/live/LoanSecondaryBorrowerOut.xml", 
				"D:/data/florida/mammals/live/loansecondaryborrower.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalGiftSecondaryBorrower() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "gift l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.GiftNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.GiftID", "t.secondary_borrower", "D:/data/florida/mammals/live/GiftSecondaryBorrowerOut.xml", 
				"D:/data/florida/mammals/live/giftsecondaryborrower.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalGiftAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "gift l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.GiftNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.GiftID", "t.authorized_by", "D:/data/florida/mammals/live/GiftAuthorizedByOut.xml", 
				"D:/data/florida/mammals/live/giftauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalBorrowAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "borrow l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.InvoiceNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.BorrowID", "t.authorized_by", "D:/data/florida/mammals/live/BorrowAuthorizedByOut.xml", 
				"D:/data/florida/mammals/live/borrowauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalBorrowSecondaryBorrower() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "borrow l inner join flamam.`Transaction Table` t on t.`Invoice Number` = l.InvoiceNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "l.BorrowID", "t.secondary_borrower", "D:/data/florida/mammals/live/BorrowSecondaryBorrowerOut.xml", 
				"D:/data/florida/mammals/live/borrowsecondaryborrower.csv", parsers, '\\');
		ppfp.parse();
	}

	static void mammalShipmentShippedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_mammal.xml")));
		String tbls = "shipment";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flamam6", "root", "root",
				tbls, "ShipmentID", "Text1", "D:/data/florida/mammals/live/ShipmentShippedByOut.xml", 
				"D:/data/florida/mammals/live/shipmentshippedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishGiftAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "gift l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.GiftNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.GiftID", "t.AuthorizedBy", "D:/data/florida/fish/Live/GiftAuthorizedByOut.xml", 
				"D:/data/florida/fish/Live/giftauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishBorrowAuthorizedBy() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "borrow l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.InvoiceNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.BorrowID", "t.AuthorizedBy", "D:/data/florida/fish/Live/BorrowAuthorizedByOut.xml", 
				"D:/data/florida/fish/Live/borrowauthorizedby.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishLoanStudentName() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "loan l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.LoanNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.LoanID", "t.StudentName", "D:/data/florida/fish/Live/LoanStudentNameOut.xml", 
				"D:/data/florida/fish/Live/LoanStudentName.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishGiftStudentName() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "gift l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.GiftNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.GiftID", "t.StudentName", "D:/data/florida/fish/Live/GiftStudentNameOut.xml", 
				"D:/data/florida/fish/Live/GiftStudentName.csv", parsers, '\\');
		ppfp.parse();
	}

	static void fishBorrowStudentName() throws Exception {
		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(Parser.fromXML(new File("C:/workspace/convertionutils/src/agentparser_5_fish.xml")));
		String tbls = "borrow l inner join flish.`Transaction Table` t on t.`Invoice Number` = l.InvoiceNumber";
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flish6", "root", "root",
				tbls, "l.BorrowID", "t.StudentName", "D:/data/florida/fish/Live/BorrowStudentNameOut.xml", 
				"D:/data/florida/fish/Live/BorrowStudentName.csv", parsers, '\\');
		ppfp.parse();
	}

	static void flaVpCollectors() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flvp6", "root", "root",
				"collectingevent", "CollectingEventID", "Text1", "/home/timo/datas/florida/vp/live/collectorsout.xml", 
				"/home/timo/datas/florida/vp/live/all_vp_collectors_for_go_live.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaVpEnteredBy() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flvp6", "root", "root",
				"collectionobject", "CollectionObjectID", "Availability", "/home/timo/datas/florida/vp/live/enteredbyout.xml", 
				"/home/timo/datas/florida/vp/live/vp_entered_by_for_go_live.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaVpAccessionDonor() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flvp6", "root", "root",
				"accession", "AccessionID", "Text3", "/home/timo/datas/florida/vp/live/accessiondonorout.xml", 
				"/home/timo/datas/florida/vp/live/accession donors go live corrected.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaVpAccessionDonorTwo() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flvp6", "root", "root",
				"accession", "AccessionID", "VerbatimDate", "/home/timo/datas/florida/vp/live/accessiondonortwoout.xml", 
				"/home/timo/datas/florida/vp/accession_second_donor_go_live.csv", parsers, '?');
		ppfp.parse();		
	}

	static void flaVpDeterminers() throws Exception {
		Parser p1 = Parser.fromXML(new File(
				"/run/media/timo/E85E-D287/convertionutils/src/agentparser_5.xml"));

		List<Parser> parsers = new ArrayList<Parser>();
		parsers.add(p1);
		
		PreParsedFieldParserToXML ppfp = new PreParsedFieldParserToXML("localhost", "flvp6", "root", "root",
				"determination", "DeterminationID", "Text1", "/home/timo/datas/florida/vp/live/identifiersout.xml", 
				"/home/timo/datas/florida/vp/live/parsed_identifier_from_rh.csv", parsers, '?');
		ppfp.parse();		
	}

}
