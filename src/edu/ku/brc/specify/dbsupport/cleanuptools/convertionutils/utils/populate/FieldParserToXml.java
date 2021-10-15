/**
 * 
 */
package utils.populate;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import utils.parse.BaseFieldValue;
import utils.parse.DerivedParseResult;
import utils.parse.FieldValue;
import utils.parse.ParseResult;
import utils.parse.Parser;
import utils.parse.Parsing;
import utils.parse.Record;
import utils.parse.Token;
import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class FieldParserToXml extends FieldParserToXmlBase {
	
	protected final List<Parser> parsers;
	
	
	/**
	 * @param server
	 * @param db
	 * @param user
	 * @param pw
	 * @param tbl
	 * @param keyFld
	 * @param fld
	 * @param parsers
	 * @param outputFile
	 */
	public FieldParserToXml(String server, String db, String user, String pw,
			String tbl, String keyFld, String fld, List<Parser> parsers,
			String outputFile) {
		super(server, db, user, pw, tbl, keyFld, fld, outputFile);
		this.parsers = parsers;
	}
	
	/**
	 * @param text
	 * @return
	 */
	protected Pair<Set<ParseResult>, List<Exception>> parseRow(String text) {
		Set<ParseResult> parses = new TreeSet<ParseResult>();
		List<Exception> exceptions = new ArrayList<Exception>();
		for (Parser p : parsers) {
			try {
				DerivedParseResult pr = p.parse(text);
				parses.add(pr);
				List<Record> records = pr.getDerivation().getRecords();

				for (Record rec : records) {
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
		return new Pair<Set<ParseResult>, List<Exception>>(parses, exceptions);
	}

	/**
	 * @param rows
	 * @return
	 * @throws Exception
	 */
	protected List<Parsing> processRows(ResultSet rows) throws Exception {
		List<Parsing> parsings = new ArrayList<Parsing>();
		String prevText = "";
		Set<ParseResult> prevParses = null;
		List<Exception> prevExceptions = null;
		while (rows.next()) {
			String text = rows.getString(1);
			Set<ParseResult> parses;
			List<Exception> exceptions;
			if (text.equals(prevText)) {
				parses = prevParses;
				exceptions = prevExceptions;
			} else {
				Pair<Set<ParseResult>, List<Exception>> rowParse = parseRow(text);
				parses = rowParse.getFirst();
				exceptions = rowParse.getSecond();
				prevParses = parses;
				prevExceptions = exceptions;
				prevText = text;
				
				/*parses = new TreeSet<DerivedParseResult>();
				exceptions = new ArrayList<Exception>();
				for (Parser p : parsers) {
					try {
						DerivedParseResult pr = p.parse(text);
						parses.add(pr);
						List<Record> records = pr.getDerivation().getRecords();

						for (Record rec : records) {
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
				prevParses = parses;
				prevExceptions = exceptions;
				prevText = text;*/
			}
			parsings.add(new Parsing(rows.getInt(2), text,
					new ArrayList<ParseResult>(parses), exceptions));
		}
		return parsings;
	}
}
