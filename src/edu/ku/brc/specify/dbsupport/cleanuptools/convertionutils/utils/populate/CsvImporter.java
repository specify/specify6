/**
 * 
 */
package utils.populate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import utils.format.CsvColHeader;
import utils.format.CsvHeader;
import utils.parse.BaseFieldValue;
import utils.parse.ImportedParseResult;
import utils.parse.ParseResult;
import utils.parse.Parsing;
import utils.parse.Record;
import utils.parse.UnderivedFieldValue;

import com.csvreader.CsvReader;

/**
 * @author tnoble
 *
 * reads a csv file and produces a list of Parsings for use by ParseResultsFormatter
 */
public class CsvImporter {
	protected final String tableName;
	protected final String parseType; //what the hell is this for??
	protected final File f;
	protected final String encoding;
	protected final char delimiter;
	protected final char escaper;
	protected final char qualifier;
	protected final boolean removeLineFeeds;
	
	

	/**
	 * @param tableName
	 * @param parseType
	 * @param f
	 * @param encoding
	 * @param delimiter
	 * @param escaper
	 * @param qualifier
	 */
	public CsvImporter(String tableName, String parseType, File f,
			String encoding, char delimiter, char escaper, char qualifier, boolean removeLineFeeds) {
		super();
		this.tableName = tableName;
		this.parseType = parseType;
		this.f = f;
		this.encoding = encoding;
		this.delimiter = delimiter;
		this.escaper = escaper;
		this.qualifier = qualifier;
		this.removeLineFeeds = removeLineFeeds;
	}

	/**
	 * @param f
	 * @param encoding
	 * @param delimiter
	 * @param escaper
	 * @param qualifier
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	public List<Parsing> getParsings() throws Exception {
		List<Parsing> result = new ArrayList<Parsing>();
        CsvReader csv = new CsvReader(new FileInputStream(f), delimiter, Charset.forName(encoding));
        csv.setTextQualifier(qualifier);
    	try {
    		if (escaper == '\\') {
    			csv.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
    		} else {
    			csv.setEscapeMode(CsvReader.ESCAPE_MODE_DOUBLED);
    		}
    		if (csv.readHeaders()) {
        		CsvHeader hdr = new CsvHeader(csv.getHeaders());
        		Parsing previousParse = null;
        		while (csv.readRecord()) {
        			if (isNewInput(csv, hdr)) {
        				previousParse = buildParsing(csv, hdr);
        				result.add(previousParse);
        			} else {
        				addRecord(previousParse, csv, hdr);
        			}
        		}
    		}
		} finally {
			csv.close();
		}
		return result;
	}
	
	/**
	 * @param csv
	 * @param hdr
	 * @return
	 * @throws IOException
	 */
	protected boolean isNewInput(CsvReader csv, CsvHeader hdr) throws IOException {
		System.out.println("Input: " + getInput(csv, hdr));
		return StringUtils.isNotBlank(getInput(csv, hdr));
	}
	
	/**
	 * @param csv
	 * @param hdr
	 * @return
	 */
	protected String getInput(CsvReader csv, CsvHeader hdr) throws IOException {
		CsvColHeader inputHead = hdr.getInput();
		return inputHead != null ? csv.get(inputHead.getText()) : null;
	}
	/**
	 * @param csv
	 * @param hdr
	 * @return
	 * @throws Exception
	 */
	protected Parsing buildParsing(CsvReader csv, CsvHeader hdr) throws Exception {
		CsvColHeader keyHead = hdr.getKey();
		Integer key = keyHead != null ? Integer.valueOf(csv.get(keyHead.getText())) : null;
		String input = getInput(csv, hdr);
		if (removeLineFeeds && input.contains("\n")) {
			input = input.replace("\n", " ");
			input = input.trim();
		}
		List<Exception> exceptions = new ArrayList<Exception>();
		CsvColHeader exceptionHead = hdr.getException();
		if (exceptionHead != null) {
			exceptions.add(new Exception(csv.get(exceptionHead.getText())));
		}

		List<ParseResult> parses = new ArrayList<ParseResult>();
		for (int p = 0; p < hdr.getParseCount(); p++) {
			List<Record> recs = new ArrayList<Record>();
			for (int r = 0; r < hdr.getRecordCount(p); r++) {
				CsvColHeader rtHdr = hdr.getRecordType(p, r);
				List<BaseFieldValue> fldVals = new ArrayList<BaseFieldValue>();
				String recType = rtHdr != null ? csv.get(rtHdr.getText()) : null;
				for (CsvColHeader h : hdr.getRecord(p, r)) {
					String value = csv.get(h.getText());
					if (removeLineFeeds && !h.getName().toLowerCase().contains("remarks") 
							&& value.contains("\n")) {
						value = value.replace("\n", " ");
						value = value.trim();
					}
					if (StringUtils.isNotBlank(value)) {
						fldVals.add(new UnderivedFieldValue(tableName, h.getName(), recType, value));
					}
				}
				if (fldVals.size() > 0) {
					recs.add(new Record(tableName, fldVals));
				}
			}
			if (recs.size() > 0) {
				parses.add(new ImportedParseResult(parseType, null, recs));
			}
		}
		return new Parsing(key, input, parses, exceptions);
	}
	
	/**
	 * @param p
	 * @param csv
	 * @param hdr
	 */
	protected void addRecord(Parsing p, CsvReader csv, CsvHeader hdr) throws Exception {
		//assumes 1 record, 1 parse
		CsvColHeader rtHdr = hdr.getRecordType(0, 0);
		List<BaseFieldValue> fldVals = new ArrayList<BaseFieldValue>();
		String recType = rtHdr != null ? csv.get(rtHdr.getText()) : null;
		for (CsvColHeader h : hdr.getRecord(0, 0)) {
			String value = csv.get(h.getText());
			if (StringUtils.isNotBlank(value)) {
				fldVals.add(new UnderivedFieldValue(tableName, h.getName(), recType, value));
			}
		}
		if (fldVals.size() > 0) {
			p.addRecord(0, new Record(tableName, fldVals));
		}		
	}
	
	static public void main(String[] args) {
		try {
			//CsvImporter imp = new CsvImporter("agent", "imp", 
			//		new File("D:/data/florida/ivp/Specifycollector3-1CSV.csv"), "utf-8", ',', '\\', '"' );
			//CsvImporter imp = new CsvImporter("agent", "imp", 
			//		new File("D:/data/florida/fish/UFAgentsParsed1-1.csv"), "utf-8", ',', '?', '"', true);
			//List<Parsing> parses = imp.getParsings();
			
			//ParseResultsFormatter f = new ParseResultsFormatter(parses, false, null);
			//List<String> xml = f.toXml("tbl_primary", "collector", "collector");
			//FileUtils.writeLines(new File( "D:/data/florida/ivp/csv3_1_2_xml.xml"), "utf-8", xml);			
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
