package utils.format;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import utils.analyze.AnalyzeParseResult;
import utils.analyze.AnalyzeParseResultSet;
import utils.parse.BaseFieldValue;
import utils.parse.ParseResult;
import utils.parse.Parsing;
import utils.parse.Record;

public class ParseResultsFormatter {

	private final List<Parsing> parsings;
	private final Map<String, Object> stats;
	private final int parses;
	private final int recs;	
	private final List<String> flds;
	private final int cols;
	
	
	/**
	 * @param parsings
	 */
	@SuppressWarnings("unchecked")
	public ParseResultsFormatter(List<Parsing> parsings, boolean sort, Comparator<Parsing> sorter) {
		super();
		this.parsings = parsings;
		//modifies caller's parsings
		if (sort) {
			if (sorter == null) {
				Collections.sort(parsings);
			} else {
				Collections.sort(parsings, sorter);
			}
		}
		stats = new AnalyzeParseResultSet().analyze(parsings);
		parses = (Integer)stats.get(AnalyzeParseResultSet.MAX_PARSES);
		recs = (Integer)stats.get(AnalyzeParseResultSet.MAX_RECORDS);	
		Integer.class.cast(stats.get(AnalyzeParseResultSet.MAX_RECORDS));
		flds = (List<String>)stats.get(AnalyzeParseResultSet.FIELDS);
		cols = 1 /*input*/ + 1 /*exceptions*/ 
				+ (recs * flds.size() * parses);
	}

	/**
	 * @return
	 */
	public List<String[]> generateTable() {
		List<String[]> result = new ArrayList<String[]>();
		result.add(bldHeader());
		for (Parsing parsing : parsings) {
			String[] line = new String[cols];
			
			line[0] = parsing.getInput();
			
			String notes = "";
			for (Exception ex : parsing.getExceptions()) {
				if (notes.length() > 0) notes += ". ";
				notes += ex.getLocalizedMessage();
			}
			line[1] = notes;
			
			int p = 0;
			for (ParseResult pr : parsing.getParses()) {
				int r = 0;
				for (Record rec : pr.getRecords()) {
					int f = 0;
					for (String fld : flds) {
						String text;
						if (fld.equals(AnalyzeParseResult.REC_TYPE_HDR)) {
							text = rec.getRecordType();
						} else {
							BaseFieldValue fv = rec.getField(fld);
							text = fv != null ? fv.getValue() : "";
						}
						line[2 + (p*recs*flds.size())+(r*flds.size()) + f] = text;
						f++;
					}
					r++;
				}
				p++;
			}
			result.add(line);
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public DefaultTableModel generateTableModel() {
		Object[][] grid = new Object[parsings.size()][cols];
		List<String[]> tbl = generateTable();
		for (int l = 1; l < tbl.size(); l++) {
			grid[l-1] = tbl.get(l);
		}
		return new DefaultTableModel(grid, bldHeader());
	}
	
	/**
	 * @return
	 */
	private String[] bldHeader() {
		String[] result = new String[cols];
		result[0] = "Input";
		result[1] = "Notes";
		for (int p = 0; p < parses; p++) {
			for (int r = 0; r < recs; r++) {
				for (int f = 0; f < flds.size(); f++) {
					String text = flds.get(f);
					if (text.equals(AnalyzeParseResult.REC_TYPE_HDR)) {
						text = "RecordType";
					}
					result[2 + (p*recs*flds.size())+(r*flds.size()) + f] = (p+1) + "."+ (r+1) + " " + text;
				}
			}
		}
		return result;
	}
	
	/**
	 * @param fileName
	 * @throws Exception
	 * 
	 * writes to csv file.
	 * delimited by "
	 * 
	 */
	public void writeTableToCSVFile(String fileName) throws Exception {
		List<String[]> tbl = generateTable();
		List<String> outLines = new ArrayList<String>();
		for (String[] line : tbl) {
			String outLine = "";
			for (int c = 0; c < line.length; c++ ) {
				if (StringUtils.isNotBlank(line[c])) {
					outLine += "\"" + line[c].replaceAll("\"", "\"\"") + "\"";
				}
						
				if (c < line.length-1) {
					outLine += ","; 
				}
			}
			outLines.add(outLine);
		}
		FileUtils.writeLines(new File(fileName), "utf-8", outLines);
	}
	
	/**
	 * @param tbl
	 * @param fld
	 * @param keyFld
	 * @return
	 */
	public List<String> toXml(String tbl, String fld, String keyFld) {
		List<String> result = new ArrayList<String>();
		List<String> fieldsUsed = new ArrayList<String>(); //hopefully there will never be more than a handful
		result.add("<parsings table=\"" + tbl + "\" field=\"" + fld + "\" key_field=\"" + keyFld + "\">");
		for (Parsing parsing : parsings) {
			result.add("  <source_record key=\"" + parsing.getKey() + "\">");
			result.add("     <value><![CDATA[" + parsing.getInput()+ "]]></value>");

			//Skipping exceptions...
			
			for (ParseResult pr : parsing.getParses()) {
				result.add("      <parse>");
				for (Record rec : pr.getRecords()) {
					result.add("         <record>");
					for (String f : flds) {
						BaseFieldValue fv = rec.getField(f);
						if (fv != null) {
							if (fieldsUsed.indexOf(f) == -1) {
								fieldsUsed.add(f);
							}
							result.add("            <field name=\"" + f + "\"> <value><![CDATA[" 
									+ fv.getValue() + "]]></value></field>");
						}
					}
					result.add("         </record>");
				}
				result.add("      </parse>");
			}
			result.add("   </source_record>");
		}
		result.add("<fields_used>");
		for (String fldUsed : fieldsUsed) {
			result.add("  <field_used name=\"" + fldUsed + "\"/>");
		}
		result.add("</fields_used>");
		result.add("</parsings>");
		return result;
	}
	
}
