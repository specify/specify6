/**
 * 
 */
package utils.populate;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import utils.misc.AgentNameParse;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class PopulatorFromXml {
	protected final RecordProcessor recordProcessor;
	protected final Element xmlRoot;	

	
	/**
	 * @param recordProcessor
	 * @param xmlRoot
	 */
	public PopulatorFromXml(RecordProcessor recordProcessor, Element xmlRoot) {
		super();
		this.recordProcessor = recordProcessor;
		this.xmlRoot = xmlRoot;
	}

	/**
	 * @throws Exception
	 */
	public void populate() throws Exception {
		recordProcessor.startProcessing();
		int r = 1;
		for (Object obj : xmlRoot.selectNodes("source_record")) {
			try {
				recordProcessor.process(new SourceRecord((Element) obj));
				//System.out.println(r++);
			} catch (ProcessorException pex) {
				if (pex.isFatal()) {
					throw pex;
				} else {
					System.out.println(pex.getLocalizedMessage());
				}
			}
		}
		recordProcessor.endProcessing();
	}
	/**
	 * @param args
	 */
	static public void main(String[] args) {
		try {
			//testXmlRead(new File("D:/data/florida/ivp/author_name.xml"));
			//testDataStructRead(new File("D:/data/florida/ivp/author_name.xml"));
			//testDataStructRead(new File("D:/data/florida/ivp/refauth.xml"));
			
			
			//testDataStructRead(new File("D:/data/florida/ivp/identifier.xml"));
			
			//testDeterminerProcessing();  	
			
			//ivpAuthorProcessing();
			//ivpCollectorProcessing("D:/data/florida/ivp/live/collector_out_2.xml");
			
			
			//flaGrrDeterminerProcessing();
			//flaGrrCollectorProcessing();

			washFishDeterminerProcessing();
			//washFishCollectorProcessing();

			//flaBirdCollectorWithNumProcessing();
			//flaBirdCollectorWithoutNumProcessing();
			//flaBirdPrepperProcessing();
			
			//flaIzCollectorProcessing();
			//flaIzDeterminerProcessing();
			//flaIzDonorProcessing();
			//flaIzEnteredByProcessing();
			//flaIzAccDonorProcessing();
			
			//fishLoanAuthorizedByProcessing();
			//fishLoanStudentNameProcessing();
			//fishGiftAuthorizedByProcessing();
			//fishGiftStudentNameProcessing();
			//fishBorrowAuthorizedByProcessing();
			//fishBorrowStudentNameProcessing();
			//fishCollectorProcessing("D:/data/florida/fish/Live/newcollectorsout.xml");
			//flaFishDeterminerProcessing();
			
			//herpCollectorProcessing();
			//herpDonorProcessing();
			
			//ivpMasterEnteredByProcessing();
			//flaIvpDeterminerProcessing();
			
			//mammalCollectorProcessing();
			//mammalLoanAuthorizedByProcessing();
			//mammalGiftAuthorizedByProcessing();
			//mammalLoanSecondaryBorrowerProcessing();
			//mammalGiftSecondaryBorrowerProcessing();
			//mammalBorrowAuthorizedByProcessing();
			//mammalBorrowSecondaryBorrowerProcessing();
			//mammalShipmentShippedByProcessing();
			
			//kuBirdCollectorProcessing();
			
			//lsuHerbCollectorProcessing();
			
			//kentStatePlantCollectorProcessing();
			//kentStateBryoAgentUpdateProcessing();
			//kentStateBryoCollectorProcessing();
			
			//flaVpCollectorProcessing();
			//flaVpDeterminerProcessing();
			//flaVpEnteredByProcessing();
			//flVpAccDonorProcessing("accessiondonorout.xml", "Donor");			
			//flVpAccDonorProcessing("accessiondonortwoout.xml", "Second Donor");			
			
			//wfvzEggCollectorProcessing();
			//wfvzGeoRefferProcessing();
			//wfvzSkinCollectorProcessing();
			//wfvzPrepperProcessing();
			
			//flaPBotDeterminerProcessing();
			//flaPBotCollectorProcessing();
			//flaPBotAuthorProcessing();
			//pBotAttentionLoanAgentProcessing();
			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	static protected void ivpAuthorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaip6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newAuthorDefaults = new ArrayList<Pair<String, Object>>();
		newAuthorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAuthorDefaults.add(new Pair<String, Object>("Version", 0));
		newAuthorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/ivp/live/refauth_2.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaIvpAuthorProcessor processor = new UFlaIvpAuthorProcessor(con, newAgentDefaults, newAuthorDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}
	
	static protected void flaPBotAuthorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaleob6ly", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newAuthorDefaults = new ArrayList<Pair<String, Object>>();
		newAuthorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAuthorDefaults.add(new Pair<String, Object>("Version", 0));
		newAuthorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/flaleobot/lively/authorsout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaIvpAuthorProcessor processor = new UFlaIvpAuthorProcessor(con, newAgentDefaults, newAuthorDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void ivpCollectorProcessing(String inputFile) throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaip6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));

		List<Pair<String, Object>> newAccessionDefaults = new ArrayList<Pair<String, Object>>();
		newAccessionDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAccessionDefaults.add(new Pair<String, Object>("Version", 0));
		newAccessionDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newAccessionDefaults.add(new Pair<String, Object>("DivisionID", 2));
		
		List<Pair<String, Object>> newAccessionAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAccessionAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAccessionAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAccessionAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, fieldsUsed, 
				newCollectorDefaults, newAccessionDefaults, newAccessionAgentDefaults);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void fishCollectorProcessing(String inputFile) throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaVpCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flvp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/vp/live/collectorsout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}
	
	static protected void flaVpDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flvp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/vp/live/identifiersout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaVpEnteredByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flvp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/vp/live/enteredbyout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaIvpMasterEnteredByProcessor processor = new UFlaIvpMasterEnteredByProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flVpAccDonorProcessing(String infile, String role) throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flvp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newAccAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAccAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAccAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAccAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//newAccAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/vp/live/" + infile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaVpAccDonorAgentProcessor processor = new UFlaVpAccDonorAgentProcessor(con, newAgentDefaults, 
				newAccAgentDefaults, fieldsUsed, role);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}


	static protected void flaIzCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("z36", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		//String inputFile = "D:/data/florida/herps/live/collectorsout.xml";
		String inputFile = "/home/timo/datas/florida/iz/live/collector_parse_out.xml";
		
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, 
				newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaBirdCollectorWithNumProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("florni", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		//String inputFile = "D:/data/florida/herps/live/collectorsout.xml";
		String inputFile = "/home/timo/datas/florida/birds/collector_with_nums_parse_out.xml";
		
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, 
				newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaBirdCollectorWithoutNumProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("florni", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		//String inputFile = "D:/data/florida/herps/live/collectorsout.xml";
		String inputFile = "/home/timo/datas/florida/birds/collector_without_nums_parse_out_recombined.xml";
		
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, 
				newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	
	static protected void flaBirdPrepperProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("florni", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/florida/birds/preppers_parsed_out.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		PrepperProcessor processor = new PrepperProcessor(con, newAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
		List<String> prepsWithManyPreppers = processor.getPrepsWithManyPreppers();
		System.out.println("Preps with more than one prepper:");
		for (String prepID : prepsWithManyPreppers) {
			System.out.print(prepID + ", ");
		}
	}

	static protected void washFishDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("uwfcsp", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/washington/determiner_parse_returned_from_collectors.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}


		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);

		populator.populate();
	}


	static protected void washFishCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("uwfcsp", serverName);

		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/washington/collector_parse_returned.xml";

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<>("Version", 0));
		newAgentDefaults.add(new Pair<>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<>("Version", 0));
		newCollectorDefaults.add(new Pair<>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<>("IsPrimary", false));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}


		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);

		populator.populate();
	}

	static protected void flaGrrDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("grrsp", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/grr/UF_GRR_agent_parse/determiner_parse_fixed_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}
	static protected void flaGrrCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("grrsp", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/grr/UF_GRR_agent_parse/collector_parsed_fixed_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void herpCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flerp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		//String inputFile = "D:/data/florida/herps/live/collectorsout.xml";
		String inputFile = "D:/data/florida/herps/live/newcollectorsout.xml";
		
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, 
				newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void herpDonorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flerp6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		String inputFile = "D:/data/florida/herps/live/donorsout.xml";
		
		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		SimpleAgentProcessor processor = new SimpleAgentProcessor(con, newAgentDefaults, 
				 fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void mammalLoanAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/LoanAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalLoanAuthorizedByProcessor processor = new UFlaMammalLoanAuthorizedByProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void mammalLoanSecondaryBorrowerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/LoanSecondaryBorrowerOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalLoanSecondaryBorrowerProcessor processor = new UFlaMammalLoanSecondaryBorrowerProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void mammalGiftSecondaryBorrowerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/GiftSecondaryBorrowerOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalGiftSecondaryBorrowerProcessor processor = new UFlaMammalGiftSecondaryBorrowerProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void mammalGiftAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/GiftAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalGiftAuthorizedByProcessor processor = new UFlaMammalGiftAuthorizedByProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void mammalCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		//String inputFile = "D:/data/florida/mammals/collectorsout.xml";
		String inputFile = "D:/data/florida/mammals/live/collectorsout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void mammalBorrowAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newBorowAgentDefaults = new ArrayList<Pair<String, Object>>();
		newBorowAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newBorowAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newBorowAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newBorowAgentDefaults.add(new Pair<String, Object>("CollectionMemberID", 4));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/BorrowAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalBorrowAuthorizedByProcessor processor = new UFlaMammalBorrowAuthorizedByProcessor(con, newAgentDefaults, 
				newBorowAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void mammalShipmentShippedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/ShipmentShippedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		ShippedByProcessor processor = new ShippedByProcessor(con, newAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}


	static protected void mammalBorrowSecondaryBorrowerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flamam6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newBorowAgentDefaults = new ArrayList<Pair<String, Object>>();
		newBorowAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newBorowAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newBorowAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newBorowAgentDefaults.add(new Pair<String, Object>("CollectionMemberID", 4));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/mammals/live/BorrowSecondaryBorrowerOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaMammalBorrowSecondaryBorrowerProcessor processor = new UFlaMammalBorrowSecondaryBorrowerProcessor(con, newAgentDefaults, 
				newBorowAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}
	
	static protected void lsuHerbCollectorProcessing() throws Exception {

		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("lsuherbnew", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		//String inputFile = "D:/data/florida/mammals/collectorsout.xml";
		String inputFile = "D:/data/lsu/LSUHerb/collectorsJSK_out.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
		FileUtils.writeLines(new File("D:/data/lsu/LSUHerb/bad.txt"), CollectorProcessor.lsuBaddies);
	}

	
	static protected void kentStatePlantCollectorProcessing() throws Exception {

		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("kentvp", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		//String inputFile = "D:/data/florida/mammals/collectorsout.xml";
		String inputFile = "D:/data/KentState/VascPlant/collectors_out_2.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
		FileUtils.writeLines(new File("D:/data/KentState/VascPlant/bad.txt"), CollectorProcessor.lsuBaddies);
	}

	static protected void kentStateBryoAgentUpdateProcessing() throws Exception {

		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("kentbryo", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/kentstate/bryo/agents_out.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		AgentUpdateProcessor processor = new AgentUpdateProcessor(con, newAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void kentStateBryoCollectorProcessing() throws Exception {

		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("kentbryo", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		//String inputFile = "D:/data/florida/mammals/collectorsout.xml";
		String inputFile = "/home/timo/datas/kentstate/bryo/collectors_out2.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
		FileUtils.writeLines(new File("/home/timo/datas/kentstate/bryo/bad2.txt"), CollectorProcessor.lsuBaddies);
	}

	static protected void kuBirdCollectorProcessing() throws Exception {
//		String serverName = "localhost";
//		String dbUsername = "root";
//		String dbPassword = "root";
//		String dbConnectionStr = AgentNameParse.getConnectionStr("kubirds", serverName);

		String serverName = "bimysql.nhm.ku.edu";
		String dbUsername = "specifymaster";
		String dbPassword = "2Kick@$$";
		String dbConnectionStr = AgentNameParse.getConnectionStr("KUBirds", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		//String inputFile = "D:/data/florida/mammals/collectorsout.xml";
		String inputFile = "/home/timo/datas/ku/birds/collectorout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		//UFlaIvpCollectorProcessor processor = new UFlaIvpCollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
		//		fieldsUsed);
		CollectorProcessor processor = new KuBirdCollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed, "Text2");
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void wfvzEggCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("wf", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collectorout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void wfvzSkinCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("wf", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/collector_skinout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));
		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void wfvzGeoRefferProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("wf", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/georefferout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		GeoRefferProcessor processor = new GeoRefferProcessor(con, newAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void wfvzPrepperProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("wf", serverName);
		
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		String inputFile = "/home/timo/datas/WesternFoundationVertebrateZoology/Reprise/preparatorout.xml";
		
		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));		

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File(inputFile));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		PrepperProcessor processor = new PrepperProcessor(con, newAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
		List<String> prepsWithManyPreppers = processor.getPrepsWithManyPreppers();
		System.out.println("Preps with more than one prepper:");
		for (String prepID : prepsWithManyPreppers) {
			System.out.print(prepID + ", ");
		}
	}

	static protected void fishLoanAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/LoanAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishLoanAuthorizedByProcessor processor = new UFlaFishLoanAuthorizedByProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void pBotAttentionLoanAgentProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaleob6ly", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/flaleobot/lively/attentionout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaPBotAttentionLoanAgentProcessor processor = new UFlaPBotAttentionLoanAgentProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void fishGiftAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newGiftAgentDefaults = new ArrayList<Pair<String, Object>>();
		newGiftAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newGiftAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newGiftAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newGiftAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/GiftAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishGiftAuthorizedByProcessor processor = new UFlaFishGiftAuthorizedByProcessor(con, newAgentDefaults, 
				newGiftAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void fishBorrowAuthorizedByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newBorrowAgentDefaults = new ArrayList<Pair<String, Object>>();
		newBorrowAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newBorrowAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newBorrowAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newBorrowAgentDefaults.add(new Pair<String, Object>("CollectionMemberID", 4));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/BorrowAuthorizedByOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishBorrowAuthorizedByProcessor processor = new UFlaFishBorrowAuthorizedByProcessor(con, newAgentDefaults, 
				newBorrowAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void fishLoanStudentNameProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newLoanAgentDefaults = new ArrayList<Pair<String, Object>>();
		newLoanAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newLoanAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newLoanAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newLoanAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/LoanStudentNameOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishLoanStudentNameProcessor processor = new UFlaFishLoanStudentNameProcessor(con, newAgentDefaults, 
				newLoanAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void fishGiftStudentNameProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newGiftAgentDefaults = new ArrayList<Pair<String, Object>>();
		newGiftAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newGiftAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newGiftAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newGiftAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/GiftStudentNameOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishGiftStudentNameProcessor processor = new UFlaFishGiftStudentNameProcessor(con, newAgentDefaults, 
				newGiftAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void fishBorrowStudentNameProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newBorrowAgentDefaults = new ArrayList<Pair<String, Object>>();
		newBorrowAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newBorrowAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newBorrowAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newBorrowAgentDefaults.add(new Pair<String, Object>("CollectionMemberID", 4));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/BorrowStudentNameOut.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaFishBorrowStudentNameProcessor processor = new UFlaFishBorrowStudentNameProcessor(con, newAgentDefaults, 
				newBorrowAgentDefaults, fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void flaIvpDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaip6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/ivp/live/determiner_2.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaFishDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flish6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newAuthorDefaults = new ArrayList<Pair<String, Object>>();
		newAuthorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAuthorDefaults.add(new Pair<String, Object>("Version", 0));
		newAuthorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		//Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/determinersOut.xml"));
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/fish/Live/determinersOut2.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaIzDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("z36", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/iz/live/determiner2_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaPBotDeterminerProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaleob6ly", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/flaleobot/lively/determinersout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDeterminerProcessor processor = new UFlaDeterminerProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaPBotCollectorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaleob6ly", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newCollectorDefaults = new ArrayList<Pair<String, Object>>();
		newCollectorDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newCollectorDefaults.add(new Pair<String, Object>("Version", 0));
		newCollectorDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		newCollectorDefaults.add(new Pair<String, Object>("DivisionID", 2));
		newCollectorDefaults.add(new Pair<String, Object>("IsPrimary", false));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/flaleobot/lively/collectorsout.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		CollectorProcessor processor = new CollectorProcessor(con, newAgentDefaults, newCollectorDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaIzDonorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("z36", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/iz/live/donor_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaDonorProcessor processor = new UFlaDonorProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	
	static protected void flaIzAccDonorProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("z36", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		//newAgentDefaults.add(new Pair<String, Object>("AgentType", 1));
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));

		List<Pair<String, Object>> newAccAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAccAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAccAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAccAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//newAccAgentDefaults.add(new Pair<String, Object>("DisciplineID", 3));

		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/iz/live/accdonor_approved_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaVpAccDonorAgentProcessor processor = new UFlaVpAccDonorAgentProcessor(con, newAgentDefaults, 
				newAccAgentDefaults, fieldsUsed, "Donor");
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
		
	}

	static protected void ivpMasterEnteredByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("flaip6", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("D:/data/florida/ivp/live/MasterEnteredByOut_2.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaIvpMasterEnteredByProcessor processor = new UFlaIvpMasterEnteredByProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	static protected void flaIzEnteredByProcessing() throws Exception {
		String serverName = "localhost";
		String dbUsername = "root";
		String dbPassword = "root";
		String dbConnectionStr = AgentNameParse.getConnectionStr("z36", serverName);
		String dbDriver = "com.mysql.jdbc.Driver";

		Class.forName(dbDriver);
		Connection con = DriverManager.getConnection(dbConnectionStr,
				dbUsername, dbPassword);

		List<Pair<String, Object>> newAgentDefaults = new ArrayList<Pair<String, Object>>();
		newAgentDefaults.add(new Pair<String, Object>("TimestampCreated", "$$now()"));
		newAgentDefaults.add(new Pair<String, Object>("Version", 0));
		newAgentDefaults.add(new Pair<String, Object>("CreatedByAgentID", 1));
		//XXX agenttype --- hmmmm...
		newAgentDefaults.add(new Pair<String, Object>("DivisionID", 2));


		//XXX this really needs to be done generically, either by listing used fields in xml
		// or by reading all records xml before beginning processing
		List<String> fieldsUsed = new ArrayList<String>();
		Element root = XMLHelper.readFileToDOM4J(new File("/home/timo/datas/florida/iz/live/cataloger_out.xml"));
		for (Object fldUsed : root.selectNodes("fields_used/field_used")) {
			fieldsUsed.add(((Element) fldUsed).attributeValue("name"));
		}

		
		UFlaIvpMasterEnteredByProcessor processor = new UFlaIvpMasterEnteredByProcessor(con, newAgentDefaults,
				fieldsUsed);
		PopulatorFromXml populator = new PopulatorFromXml(processor, root);
		
		populator.populate();
	}

	/**
	 * @param doc
	 * @throws Exception
	 */
	static protected void testXmlRead(File doc) throws Exception {
		Element root = XMLHelper.readFileToDOM4J(doc);
		for (Object obj : root.selectNodes("source_record")) {
			Element element = (Element) obj;
			String key = XMLHelper.getAttr(element, "key", "null");
			Object val = element.selectSingleNode("value");
			String input = ((Node)val).getText();
			System.out.println(key + ": " + input);
			for (Object parseObj : element.selectNodes("parse")) {
				Element parseElem = (Element)parseObj;
				for (Object recObj : parseElem.selectNodes("record")) {
					Element recElem = (Element)recObj;
					System.out.println("  record: ");
					for (Object fldObj : recElem.selectNodes("field")) {
						Element fldElem = (Element)fldObj;
						String fldVal = ((Node)fldElem.selectSingleNode("value")).getText();
						String fldName = fldElem.attributeValue("name");
						System.out.println("    " + fldName + "=" + fldVal);
					}
				}
			}
		}
		
	}
	
	/**
	 * @param doc
	 * @throws Exception
	 */
	static protected void testDataStructRead(File doc) throws Exception {
		Element root = XMLHelper.readFileToDOM4J(doc);
		for (Object obj : root.selectNodes("source_record")) {
			SourceRecord src = new SourceRecord((Element) obj);
			System.out.println(src.getKey() + ": " + src.getValue());
			for (ParseInfo p : src.getParses()) {
				System.out.println("  Parse:");
				for (RawRecord r : p.getRecords()) {
					System.out.println("    record:");
					for (int f = 0; f < r.getFldCount(); f++) {
						System.out.println("      " + r.getFld(f).getFirst() + "=" + r.getFldVal(f));
					}
				}
			}
			
		}
		
	}
}
