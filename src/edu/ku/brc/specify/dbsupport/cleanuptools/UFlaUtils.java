/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.csvreader.CsvWriter;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class UFlaUtils {

	/**
	 * @param sp6TransTbl
	 * @param isForItems
	 * @return
	 */
	private static String getTransUncatalogedItemsFldSQL(String sp6TransTbl, boolean isForItems) {
		if (isForItems) {
			return "s.*";
		} else {
			return "distinct t.`Invoice Number`, l." + sp6TransTbl + "ID";
		}
	}
	
	/**
	 * @param sp6TransTbl
	 * @return
	 */
	private static String getTransSp6JoinFld(String sp6TransTbl) {
		if ("loan".equalsIgnoreCase(sp6TransTbl)) {
			return "LoanNumber";
		} else if ("gift".equalsIgnoreCase(sp6TransTbl)) {
			return "GiftNumber";
		} else if ("borrow".equalsIgnoreCase(sp6TransTbl)) {
			return "InvoiceNumber";
		} else {
			return "";
		}
	}
	
	private static boolean isDirection(String arg) {
		return "N".equalsIgnoreCase(arg) || "S".equalsIgnoreCase(arg) 
				|| "E".equalsIgnoreCase(arg) || "W".equalsIgnoreCase(arg);
	}
	
//	private static boolean isGeoCoord(String arg) {
//		return arg.matches(")
//	}
	
	private static boolean isDatum(String arg) {
		return arg.toLowerCase().contains("nad") || arg.toLowerCase().contains("wgs") || arg.toLowerCase().contains("datum");
	}

	private static boolean isMethod(String arg) {
		return arg.toLowerCase().contains("gps") || arg.toLowerCase().contains("estimate") || arg.toLowerCase().contains("gnis");
	}

	private static boolean isStartNote(String arg) {
		//return !(isDatum(arg) || isMethod(arg)) && arg.contains("(");
		return arg.contains("(");
	}

	private static boolean isEndNote(String arg) {
		//return !(isDatum(arg) || isMethod(arg)) && arg.contains("(");
		return arg.contains(")");
	}

	private static boolean isComma(String arg) {
		return ",".equals(arg);
	}

	private static boolean isPeriod(String arg) {
		return ".".equals(arg);
	}

	private static boolean isSemicolon(String arg) {
		return ";".equals(arg);
	}

	private static String cleanupLatLng(String arg) {
		String trimmed = arg.trim();
		char[] str = trimmed.toCharArray();
		String result = "";
		boolean inParen = false;
		for (int c=0; c < str.length; c++) {
			String prev = c > 0 ? String.valueOf(str[c-1]) : "^";
			String next = c < str.length-1 ? String.valueOf(str[c+1]) : "$";
			String ch = String.valueOf(str[c]);
			if (!inParen) { 
				if ("NSEW".contains(ch) && !"abcdefghijklmnopqrstuvwxyz)".contains(prev.toLowerCase()) && !"abcdefghijklmnopqrstuvwxyz".contains(next.toLowerCase())) {
					ch = " " + ch + " ";
				} else {
					if ("(".equals(ch)) {
						inParen = true;
					}
				}
			} else {
				if (")".equals(ch)) {
					inParen = false;
				}
			}
			result += ch;
		}
		//result = result.replaceAll("W", " W ");
		//result = result.replaceAll("E", " E ");
		//result = result.replaceAll("N", " N ");
		//result = result.replaceAll("S", " S ");
		result = result.replaceAll("  ", " ");
		result = result.replaceAll("  ", " ");
		return result.trim();
	}
	
	
	private static Map<String, Integer> getDistinctWords(List<String> vals) {
		Map<String, Integer> result = new HashMap<String, Integer>();
		for (String val: vals) {
			if (val != null) {
				for (String w : val.split(" ")) {
					Integer cnt = result.get(w);
					result.put(w, cnt == null ? 1 : cnt + 1);
				}
			}
		}
		return result;
	}
	
	private static List<String> getBitPart(String piece) {
		List<String> result = new ArrayList<String>();
		if (isStartNote(piece)) {
			result.add("startnote");
		} if (isEndNote(piece)) {
			result.add("endnote");
		}
		if (isDatum(piece)) {
			result.add("datum");
		}
		if (isMethod(piece)) {
			result.add("method");
		} 
		if (isDirection(piece)) {
			result.add("dir");
		}
		if (isComma(piece) || isPeriod(piece) || isSemicolon(piece)) {
			result.add("junk");
		}
		return result;
	}
	
	private static Pair<String, List<String>> getBit(String piece) {
		List<String> bits = getBitPart(piece);
		//for (String bit : bits) {
		//	String newPiece = !bit.endsWith("note") ? piece : piece.replace(")", "").replace("(", "");
		return new Pair<String, List<String>>(piece, bits);
	}
	
	private static void updateFlaPbotLatLng(Object[] ll, String lat1, String lng1, String lat1Text, String lng1Text, Connection conn, String filter) throws Exception {
		String units = ll[4].toString();
		if ("DDMMMM".equals(units)) {
			units = "2";
		} else if ("DDMMSS".equals(units)) {
			units = "1";
		} else {
			units = "0";
		}
		String filtStr = filter == null ? "" : filter;
		if (StringUtils.isNotBlank(filtStr)) {
			filtStr += " AND ";
		}
		String sql = "UPDATE locality SET "
				+ "Latitude1=" + lat1 + ", "
				+ "Longitude1=" + lng1 + ", "
				+ "Lat1Text='" + lat1Text.replace("'", "''") + "', "
				+ "Long1Text='" + lng1Text.replace("'", "''") + "', "
				+ "LatLongType='" + ll[8] + "', "
				+ "SrcLatLongUnit=" + units + ", "
				+ "OriginalLatLongUnit=" + units + ", "
				+ "Datum=" + (ll[6] == null ? "NULL, " : "'" + ll[6] + "', ")
				+ "LatLongMethod=" + (ll[5] == null ? "NULL " : "'" + ll[5] + "' ")
				+ "WHERE " + filtStr + " VerbatimLatitude='" + ll[11].toString().replaceAll("'", "''") + "'";
		if (BasicSQLUtils.update(conn, sql) < 1) {
			throw new Exception("unable to update lat/lng for " + ll[11].toString());
		}
	}

	private static void updateFlaPbotLatLngFromParse(Object[] ll, String lat1, String lng1, String lat2, String lng2, String lat1Text, String lng1Text, String lat2Text, String lng2Text, Connection conn) throws Exception {
		String units = "0";
		String unitter = lat1Text.replace("N", "").replace("S", "").trim();
		if (unitter.endsWith("'")) {
			units = "2";
		} else if (unitter.endsWith("''") || units.endsWith("\"")) {
			units = "1";
		}
		String latlngtype = (String)ll[10];
		if ("Box".equalsIgnoreCase(latlngtype)) {
			latlngtype = "Rect";
		}
		String sql = "UPDATE locality SET "
				+ "Latitude1=" + lat1 + ", "
				+ "Longitude1=" + lng1 + ", "
				+ "Latitude2=" + ("".equals(lat2) || lat2 == null ? "NULL" : lat2) + ", "
				+ "Longitude2=" + ("".equals(lng2) || lng2 == null ? "NULL" : lng2) + ", "
				+ "Lat1Text='" + lat1Text.replace("'", "''") + "', "
				+ "Long1Text='" + lng1Text.replace("'", "''") + "', "
				+ "Lat2Text=" + ("".equals(lat2Text) || lat2Text == null ? "NULL" : "'" + lat2Text.replace("'", "''") + "'") + ", "
				+ "Long2Text=" + ("".equals(lng2Text) || lng2Text == null ? "NULL" : "'" + lng2Text.replace("'", "''") + "'") + ", "
				+ "LatLongType='" + latlngtype + "', "
				+ "SrcLatLongUnit=" + units + ", "
				+ "OriginalLatLongUnit=" + units + ", "
				+ "Datum=" + (ll[8] == null ? "NULL, " : "'" + ll[8] + "', ")
				+ "LatLongMethod=" + (ll[7] == null ? "NULL " : "'" + ll[7] + "' ")
				+ "WHERE VerbatimLatitude='" + ll[1].toString().replaceAll("'", "''") + "'";
		if (BasicSQLUtils.update(conn, sql) < 1) {
			//throw new Exception("unable to update lat/lng for " + ll[1].toString());
			System.out.println("no update for " + ll[1]);
		}
	}

	private static void doFlaleobotLatLngsFromReturnedParse(Connection con) throws Exception {
		String sql = "select * from latlngtmp";
		List<Object[]> parsedvals = BasicSQLUtils.query(con, sql);
		GeoRefConverter gc = new GeoRefConverter();
		int bad = 0;
		for (Object[] pval : parsedvals) {
			String err = null;
			String lat1 = (String)pval[2];
			String lng1 = (String)pval[3];
			String lat2 = (String)pval[4];
			String lng2 = (String)pval[5];
			String lat1DD = null;
			String lng1DD = null;
			String lat2DD = null;
			String lng2DD = null;
			try {
				lat1DD = gc.convert(lat1, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
				lng1DD = gc.convert(lng1, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
				if (!"".equals(lat2)) {
					lat2DD = gc.convert(lat2, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
					lng2DD = gc.convert(lng2, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name());
				}
			} catch (Exception ex) {
				lat1DD = null;
				lng1DD = null;
				lat2DD = null;
				lng2DD = null;
				err = ex.getMessage();
			}
			if (lat1DD != null && lng1DD != null) {
				updateFlaPbotLatLngFromParse(pval, lat1DD, lng1DD, lat2DD, lng2DD, lat1, lng1, lat2, lng2, con);
			} else {
				System.out.print("Error converting: " + pval[1] + "( " + err + ")");
				System.out.println();
				bad++;
			}
		}
	}
	private static void doFlaleobotLatLngs(Connection con, String filter) throws Exception {
		boolean startFromTmpTbl = false;
		String sql;
		if (startFromTmpTbl) {
			String wh = " dbval not in ('N/A', 'No GPS taken.', 'SEE LOCALITY DESCRIPTION') and not isdone" + (StringUtils.isNotBlank(filter) ? " AND " + filter : "");
			sql = "select dbval, origdbval from flapb.latlngtmp where " + wh + " order by 1";
		} else {
			//String sql = "select distinct text4 from locality where text4 not in ('N/A', 'No GPS taken.') order by 1";
			
			//String wh = " text4 not in ('N/A', 'No GPS taken.') " + (StringUtils.isNotBlank(filter) ? " AND " + filter : "");
			//sql = "select distinct text4 from locality where " + wh + " order by 1";
			
			//for fla birds
			sql = "select distinct verbatimlatitude, verbatimlatitude from locality where verbatimlatitude is not null";
		}
		List<Object[]> vals = BasicSQLUtils.query(con, sql);
		List<Object[]> parsedvals = parseFlaleobotLatLngs(vals);
		GeoRefConverter gc = new GeoRefConverter();
		int good = 0; int bad = 0;
		List<String> output = new ArrayList<String>();
		/*
		 * 0	lat1
		 * 1	lng1	
		 * 2	lat2
		 * 3	lng2
		 * 4	units
		 * 5	method
		 * 6	datum
		 * 7	remark
		 * 8	latlngtype 
		 * 9	error
		 * 10	input
		 * 11   originput
		 */
		output.add("Error, OrigDbVal, isdone, ValueInDb, Lat1, Lng1, Lat2, Lng2, Units, Method, Datum, Remarks, LatLngType");
		List<String[]> outputRecs = new ArrayList<String[]>();
		String[] headerRec = {"Error", "OrigDbVal", "isdone", "ValueInDb", "Lat1", "Lng1", "Lat2", "Lng2", "Units", "Method", "Datum", "Remarks", "LatLngType"};
		outputRecs.add(headerRec);
		for (Object[] pval : parsedvals) {
			boolean parseErr = false;
			boolean convErr = false;
			String convErrMsg = "";
			if (!"".equals(pval[9])) {
				System.out.print(pval[9] + ": ");
				System.out.print(pval[10]);
				System.out.println();
				parseErr = true;
			} else {
				String err = null;
				String lat1 = pval[0].toString().replace(" S", "S").replace(" N", "N").replace("''", "\"").replaceAll("^N", "").replaceAll("^S", "-");
				String lng1 = pval[1].toString().replace(" E", "E").replace(" W", "W").replace("''", "\"").replaceAll("^E", "").replaceAll("^W", "-");
				String lat1DD = null;
				String lng1DD = null;
				try {
					lat1DD = gc.convert(lat1, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name(),LatLonConverter.LATLON.Latitude, LatLonConverter.DEGREES_FORMAT.None);
					lng1DD = gc.convert(lng1, GeoRefConverter.GeoRefFormat.D_PLUS_MINUS.name(),LatLonConverter.LATLON.Longitude, LatLonConverter.DEGREES_FORMAT.None);
				} catch (Exception ex) {
					lat1DD = null;
					lng1DD = null;
					err = ex.getMessage();
				}
				if (lat1DD != null && lng1DD != null && Math.abs(Double.valueOf(lat1DD)) < 90.0 && Math.abs(Double.valueOf(lng1DD)) < 180.0) {
					good++;
					updateFlaPbotLatLng(pval, lat1DD, lng1DD, lat1, lng1, con, filter);
				} else {
					convErr = true;
					convErrMsg = "Error converting to Decimal Degrees: " + err;
					System.out.print("Error converting: " + pval[10] + "( " + err + ")");
					System.out.println();
					bad++;
				}
			}
			String line = "";
			String[] rec = new String[11];
			String err = parseErr ? pval[9].toString() : (convErr ? convErrMsg : "");
			if (parseErr || convErr) {
				line += "'" + err.replaceAll("'", "\\'") + "',";
				rec[0] = err;
			} else {
				line += ",";
				rec[0] = "";
			}
			line += "'" + pval[11].toString().replaceAll("'", "''") + "'";
			rec[1] = pval[10].toString();
			if (parseErr) {
				line += ",,,,,,,,,";
				rec[2]="";rec[3]="";rec[4]="";rec[5]="";rec[6]="";rec[7]="";rec[8]="";rec[9]="";rec[10]=""; 
			} else {
				for (int i = 0; i < 9; i++) {
					line += ",";
					rec[i+2] = "";
					if (pval[i] != null && !"".equals(pval[i].toString())) {
						String val = pval[i].toString().replaceAll("'", "\\'");
						line += val.contains(",") ? "'" + val + "'" : val;
						rec[i+2] = pval[i].toString();
					}
				}
			}
			output.add(line);
			outputRecs.add(rec);
		}
		System.out.println("good=" + good + ". bad=" + bad);
		//FileUtils.writeLines(new File("D:/data/florida/paleobotany/latlng.csv"), output);
		CsvWriter csv = new CsvWriter("/home/timo/datas/florida/birds/latlng_parsed.csv", ',', java.nio.charset.Charset.forName("utf8"));
		for (String[] rec : outputRecs) {
			csv.writeRecord(rec);
		}
		csv.close();
	}
	
	private static List<Object[]> parseFlaleobotLatLngs(List<Object[]> latlngs) {
		/*
		 * 0	lat1
		 * 1	lng1	
		 * 2	lat2
		 * 3	lng2
		 * 4	units
		 * 5	method
		 * 6	datum
		 * 7	remark
		 * 8	latlngtype 
		 * 9	error
		 * 10	input
		 * 11  originput
		 */
		List<Object[]> result = new ArrayList<Object[]>();
		for (Object[] latlngrow : latlngs) {
			Object latlng = latlngrow[0];
			String ll = cleanupLatLng(latlng.toString());
			String[] pieces = ll.split(" ");
			List<Pair<String,List<String>>> bits = new ArrayList<Pair<String,List<String>>>();			
			for (int i = 0; i < pieces.length; i++) {
				bits.add(getBit(pieces[i]));
			}
			Object[] row = new Object[12]; //lat1, lng1, lat2, lng2, units, method, datum, note, type, error, latlng
			//pass1 pluck low hangers
			row[11] = latlngrow[1];
			row[10] = latlng;
			row[9] = "";
			for (int b = bits.size()-1; b >= 0; b--) {
				Pair<String, List<String>> bit = bits.get(b);
				boolean remove = false;
				for (String it : bit.getSecond()) {
					if ("datum".equals(it)) {
						row[6] = bit.getFirst();
						remove = true;
					} else if ("method".equals(it)) {
						row[5] = bit.getFirst();
						remove = true;
					} else if ("startnote".equals(it) || "endnote".equals(it)) {
						row[7] = bit.getFirst();
						remove = true;
					} else if ("junk".equals(it)) {
						remove = true;
					}
				}
				if (remove) {
					bits.remove(b);
				}
			}
			List<String> stopper = new ArrayList<String>();
			stopper.add("$$$");
			bits.add(new Pair<String, List<String>>("", stopper));
			//put together lats and lngs ...
			//int lat1idx=0, lng1idx=2, lat2idx=3, lng2idx=4;
			int fldIdx = 0;
			String val = "", dir = "";
			for (Pair<String,List<String>> bit : bits) {
				if (bit.getSecond().size() > 1) {
					System.out.println("Huh?");
				} else {
					String theBit = bit.getSecond().size() > 0 ? bit.getSecond().get(0) : "";
					if ("dir".equals(theBit) || "$$$".equals(theBit)) {
						if (!"".equals(val)) {
							String units = "DDDDDD";
							if (val.endsWith("�")) {
								units = "DDDDDD";
							} else if (val.endsWith("'")) {
								units = "DDMMMM";
							} else if (val.endsWith("''") || val.endsWith("\"")) {
								units = "DDMMSS";
							}
							if (row[4] != null && !row[4].equals(units)) {
								row[9] += "conflicting units;"; 
							} else {
								row[4] = units;
							}
							if ("".equals(dir) && "dir".equals(theBit)) {
								val += " " + bit.getFirst();
							} else {
								val += " " + dir;
								dir = bit.getFirst();
							}
							row[fldIdx++] = val;
							val = "";
						} else {
							dir = bit.getFirst();
						}
					} else {
						String newBit = bit.getFirst();
						if (newBit.endsWith(",") || newBit.endsWith(";")) {
							newBit = StringUtils.left(newBit, newBit.length()-1);
						}
						if (!(newBit.endsWith("�") || newBit.endsWith("'") || newBit.endsWith("''") || newBit.endsWith("\""))) {
							newBit += " ";
						}
						val += newBit;
					}
				}
			}
			if (fldIdx == 4) {
				row[8] = "Rectangle";
			} else if (fldIdx == 2) {
				row[8] = "Point";
			} else {
				row[9] += "uneven number of coords;";
			}
			result.add(row);
		}
		return result;
	}
	
	/**
	 * @param sp6TransTbl
	 * @param flaDb
	 * @param sp6Db
	 * @return
	 */
	private static String getTransUncatalogedItemsTblSQL(String sp6TransTbl, String flaDb, String sp6Db) {
		return "`" + flaDb + "`.`transaction table` t "
				+ "inner join `" + flaDb + "`.`specimens table` s on s.`Invoice Number` = t.`Invoice Number` "
				+ "inner join `" + sp6Db + "`." + sp6TransTbl + " l on l." 
				+ getTransSp6JoinFld(sp6TransTbl) + " = t.`Invoice Number`"; 
	}
	
//	 "select distinct t.`Invoice Number`, l.LoanID from flish.`transaction table` t "
//	+ "inner join flish.`specimens table` s on s.`Invoice Number` = t.`Invoice Number` "
//	+ "inner join flish6.loan l on l.LoanNumber = t.`Invoice Number` " 
//	+ "where s.`UF ID` is null"
	private static String getTransWithUncatalogedItemsSQL(String sp6TransTbl, String flaDb, String sp6Db) {
		return "select " + getTransUncatalogedItemsFldSQL(sp6TransTbl, false)
				+ " from " + getTransUncatalogedItemsTblSQL(sp6TransTbl, flaDb, sp6Db)
				+ " where s.`UF ID` is null";
	}
	
	private static String getUncatalogedItemsForTransSQL(String sp6TransTbl, String flaDb, String sp6Db,
			String invoiceNumber) {
		return "select " + getTransUncatalogedItemsFldSQL(sp6TransTbl, true)
				+ " from " + getTransUncatalogedItemsTblSQL(sp6TransTbl, flaDb, sp6Db)
				+ " where s.`UF ID` is null and t.`Invoice Number` = " + invoiceNumber;
	}

	/**
	 * @param con
	 * @return
 		"select s.* from flish.`transaction table` t "
				+ "inner join flish.`specimens table` s on s.`Invoice Number` = t.`Invoice Number` "
				+ "inner join flish6.loan l on l.LoanNumber = t.`Invoice Number` " 
				+ "where s.`UF ID` is null and t.`Invoice Number` = " + invoiceNumber
	 */
	private static List<Object[]> getTransWithUncatalogedItems(Connection con, String sp6TransTbl,
			String flaDb, String sp6Db) {
		return BasicSQLUtils.query(con, getTransWithUncatalogedItemsSQL(sp6TransTbl, flaDb, sp6Db));
	}
		
	private static List<Pair<String,Object>> parseFlVpPub(String pub) {
		List<Pair<String,Object>> result = new ArrayList<Pair<String,Object>>();
		String[] periods = pub.split("\\. ");
		String year = "";
		for (String p : periods) {
			try {
				Integer y = Integer.valueOf(p.trim());
				year = y.toString();
				break;
			} catch (Exception e) {
				//nada
				//System.out.println("exception for " + p);
			}
		}
		if (!"".equals(year)) {
			int m = pub.indexOf(" " + year + ".");
			if (m > -1) {
				String authors = pub.substring(0, m).trim();
				String titleAndMore = pub.substring(m + (" " + year + ".").length()+1).trim();
				result.add(new Pair<String,Object>("authors", authors));
				result.add(new Pair<String,Object>("pubdate", year));
				String more[] = titleAndMore.split("\\. ");
				for (int s = 0; s < more.length; s++) {
					String fld = s == 0 ? "title" : "more" + s;
					result.add(new Pair<String,Object>(fld, more[s].trim()));
				}
			}
		}
		return result;
	}
	
	private static void parseFlaVpPubs(Connection con) {
		List<Object[]> pubs = BasicSQLUtils.query(con, "select idvppub, input from vppub");
		int maxparts = 0;
		for (Object[] pub : pubs) {
			List<Pair<String, Object>> ppub = parseFlVpPub((String)pub[1]);
			System.out.println(pub[1]);
			int parts = 0;
			String sets = "";
			for (Pair<String,Object> f : ppub) {
				System.out.println("  " + f.getFirst() + ": " + f.getSecond());
				if (parts > 0) sets += ", ";
				sets += f.getFirst() + " = '" + f.getSecond().toString().replaceAll("'", "''") + "'";
				parts++;
			}
			if (parts > 0) {
				if (1 != BasicSQLUtils.update(con, "update vppub set " + sets + " where idvppub=" + pub[0])) {
					System.out.println("HALT!");
				}
			}
			if (parts > maxparts) {
				maxparts = parts;
			}
			System.out.println();
		}
		System.out.println("Max Parts = " + maxparts);
	}
	/**
	 * @param connStr
	 * @return
	 * @throws SQLException
	 */
	public static Connection getConnection(String connStr)	throws SQLException {
		return  DriverManager.getConnection(connStr, "Master", "Master");
	}
	
	/**
	 * @param con
	 * @param invoiceNumber
	 * @return
	 */
	private static List<Object[]> getUncatalogedItemsForTrans(Connection con, String sp6TransTbl,
			String flaDb, String sp6Db, String invoiceNumber) {
		return BasicSQLUtils.query(con,	
				getUncatalogedItemsForTransSQL(sp6TransTbl, flaDb, sp6Db, invoiceNumber));
	}
	
	/**
	 * @param sp6TransTbl
	 * @param invoiceNumber
	 */
	private static String getFileNameForUncatTransItems(String sp6TransTbl, String invoiceNumber) {
		return sp6TransTbl + "_" + invoiceNumber + "_UncatalogedItems.csv";
	}
	
	/**
	 * 
	 */
	private static String[] specimensTblFieldNames = {
		"Specimen ID", "UF ID", "Description", "Invoice Number", "Genus", "Species", "Subspecies",
		"Other Description", "Number C_S", "C_S Borrowed", "C_S Returned", "Number Skel", "Skel Borrowed", "Skel Returned",
		"Number Alch", "Alch Borrowed", "Alch Returned", "Number Photos", "Photos Borrowed", "Photos Returned",
		"Number Tissues", "Tissues Borrowed", "Tissues Returned", "field_number", "cont_ocean", "country",
		"state","county","township","range","section_grid","locality","lat_degrees_start","lat_minutes_start","lat_seconds_start",
		"lat_NS_start","long_degrees_start","long_minutes_start","long_seconds_start","long_EW_start","depth_of_capture","collector",
		"date_collected","year_collected","loc_remarks","Quantity Returned","Comments","alcohol_type","return_comments","date_returned"
	};
	
	/**
	 * @param line
	 * @return
	 */
	private static String getSpecimenRemark(Object[] line) {
		String result = "";
		for (int c = 0; c < line.length; c++) {
			if (line[c] != null && !"0".equals(line[c].toString()) && !"0.".equals(line[c].toString()) 
					&& !"0.0".equals(line[c].toString())
					&& !"Invoice Number".equalsIgnoreCase(specimensTblFieldNames[c])
					&& !"Specimen ID".equalsIgnoreCase(specimensTblFieldNames[c])) {
				if (result.length() > 0) {
					result += ". ";
				}
				result += specimensTblFieldNames[c].toUpperCase() + ": " + line[c];
			}
		}
		return result;
	}
	
	/**
	 * @param line
	 * @return
	 */
	private static String getUncatalogedSpecimensAsRemarks(List<Object[]> lines) {
		String result = "";
		for (Object[] line : lines) {
			if (result.length() > 0) {
				result += "\n";
			}
			result += getSpecimenRemark(line);
		}
		return result;
	}
	
	/**
	 * @param con
	 * @param sp6TransTbl
	 * @param flaDb
	 * @param sp6Db
	 * @param csvFileDir
	 * @throws IOException
	 */
	private static void createAttachmentsForUncatalogedTransactionItems(Connection con, String sp6TransTbl,
			String flaDb, String sp6Db, String csvFileDir) throws IOException {
		List<Object[]> transactions = getTransWithUncatalogedItems(con, sp6TransTbl, flaDb, sp6Db);
		for (Object[] trans : transactions) {
			String invoiceNumber = trans[0].toString();
			String sp6TransId = trans[1].toString();
			List<Object[]> items = getUncatalogedItemsForTrans(con, sp6TransTbl, flaDb, sp6Db,
					invoiceNumber);
			String fileTitle = getFileNameForUncatTransItems(sp6TransTbl, invoiceNumber);
			String fileName = java.util.UUID.randomUUID().toString() + ".att.csv";
			String fullFileName = csvFileDir + File.separator + fileName;
			//writeToCsv(fullFileName, specimensTblFieldNames, objLinesToStrLines(items));
			//addAttachment(fileTitle, fileName, sp6TransTbl, sp6Db, sp6TransId);
			updateRemark(con, items, sp6TransTbl, sp6Db, sp6TransId);
		}
	}
	
	/**
	 * @param item
	 * @param uncatCollID
	 * @return
	 * @throws Exception
	 */
	private static Integer addUncatCollObjAndPrep(Object[] item, Integer uncatCollID) throws Exception {
		String sql = "insert into collectionobject (TimestampCreated, Version, CreatedByAgentID, CollectionID, "
				+ "CollectionMemberID, ProjectNumber, Description, Text1) values(now(), 0, 1, "
				+ uncatCollID + "," + uncatCollID + ", '" + item[3] + "', null, null)";
		BasicSQLUtils.update(sql);
		int collObjId = BasicSQLUtils.getCountAsInt("SELECT LAST_INSERT_ID()");
		sql = "insert into preparation(TimestampCreated, Version, CreatedByAgentID, "
				+ "CollectionMemberID, CollectionObjectID) values(now(), 0, 1, "
				+ uncatCollID + "," + collObjId + ")";
		BasicSQLUtils.update(sql);
		return BasicSQLUtils.getCountAsInt("SELECT LAST_INSERT_ID()");
	}
	
	private static void addUncatLoanPrep(String spTransId, String sp6TransTbl, Integer uncatCollID, Integer uncatPrepId, Object[] item) 
			throws Exception {
		String sql = "insert into " + sp6TransTbl + "preparation (TimestampCreated, Version, CreatedByAgentID, CollectionID, "
				+ "CollectionMemberID, ProjectNumber, Description, Text1) values(now(), 0, 1, "
				+ uncatCollID + "," + uncatCollID + ", '" + item[3] + "', null, null)";
		BasicSQLUtils.update(sql);
		
	}
	
	private static void createLoanPrepsInUncatCollForUncatalogedTransItems(Connection con, String sp6TransTbl,
			String flaDb, String sp6Db, Integer uncatCollID) throws Exception {
		List<Object[]> transactions = getTransWithUncatalogedItems(con, sp6TransTbl, flaDb, sp6Db);
		for (Object[] trans : transactions) {
			String invoiceNumber = trans[0].toString();
			String sp6TransId = trans[1].toString();
			List<Object[]> items = getUncatalogedItemsForTrans(con, sp6TransTbl, flaDb, sp6Db,
					invoiceNumber);
			for (Object[] item : items) {
				addUncatLoanPrep(sp6TransId, sp6TransTbl, uncatCollID, addUncatCollObjAndPrep(item, uncatCollID), item);
			}
		}
	}
	/**
	 * @param sp6TransTbl
	 * @param sp6Db
	 * @param sp6TransId
	 */
	private static void updateRemark(Connection con, List<Object[]> items, String sp6TransTbl, String sp6Db, String sp6TransId) {
		String remark = "'" + BasicSQLUtils.escapeStringLiterals(getUncatalogedSpecimensAsRemarks(items), "'") + "'";
		String sql =  "update " + sp6Db + "." + sp6TransTbl + " set Remarks = concat(case when Remarks is not null then concat(Remarks,'\\n\\n') else '' end, " + remark + ") where " 
				+ sp6TransTbl + "ID = " + sp6TransId;
		BasicSQLUtils.update(con, sql);
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
	
	/**
	 * @param con
	 * @throws Exception
	 * 
	 * generates parsable versions of collector strings stored in temp table vpcolls.
	 * Actually not necessary anymore because of preParser class added to convertionutil.misc package.
	 */
	private static void fixFlsVpCollStrsForParse(Connection con) throws Exception {
		List<Object[]> colStrs = BasicSQLUtils.query(con, "select coll, idvpcolls from vpcolls");
		for (Object[] colStrRow : colStrs) {
			Object colStrObj = colStrRow[0];
			if (colStrObj != null) {
				String colStr = colStrObj.toString();
				String parsableColStr = fixFlaVpCollStrForParse(colStr);
				BasicSQLUtils.update(con, "update vpcolls set parsablecoll='" + parsableColStr.replace("'", "''") + "' where idvpcolls=" + colStrRow[1]);
			}
		}
	}
	/**
	 * @param sp6TransTbl
	 * @param sp6TransId
	 * @return
	 */
	private static String getTransScopeIdAndType(String sp6TransTbl, String sp6TransId) {
		return "";
	}
	
	/**
	 * @param sp6TransTbl
	 * @return
	 */
	private static String getTableId(String sp6TransTbl) {
		if ("loan".equalsIgnoreCase(sp6TransTbl)) {
			return "52";
		} else if ("gift".equalsIgnoreCase(sp6TransTbl)) {
			return "52";
		} else if ("borrow".equalsIgnoreCase(sp6TransTbl)) {
			return "52";
		}
		return "ERROR";
	}
	
	private static void addAttachment(String fileTitle, String fileName, String sp6TransTbl, 
			String sp6Db, String sp6TransId) {
		String attInsSql = "insert into attachment(TimeStampCreated, Version, CreatedByAgentID,"
				+ "ScopeID, ScopeType, MimeType, GUID, TableID, AttachmentLocation, origFilename, title)"
				+ "values(now(), 0, 1, " + getTransScopeIdAndType(sp6TransTbl, sp6TransId)
				+ getTableId(sp6TransTbl) + ", '" + fileName + "', '" + fileTitle + "', '" + fileTitle + "')";
	}
	/**
	 * @param lines
	 * @return
	 */
	private static List<String[]> objLinesToStrLines(List<Object[]> lines) {
		List<String[]> result = new ArrayList<String[]>();
		for (Object[] line : lines) {
			String[] newLine = new String[line.length];
			for (int i = 0; i < line.length; i++) {
				newLine[i] = line[i] == null ? "" : line[i].toString();
			}
			result.add(newLine);
		}
		return result;
	}
	
	/**
	 * @param fileName
	 * @param hdr
	 * @param lines
	 * @throws IOException
	 */
	private static void writeToCsv(String fileName, String[] hdr, List<String[]> lines) throws IOException {
		CsvWriter csv = new CsvWriter(fileName, ',', Charset.forName("utf-8"));
		csv.writeRecord(hdr);
		for (String[] line : lines) {
			csv.writeRecord(line);
		}
		csv.close();
	}
	
	/**
	 * @param in
	 * @param converted
	 * @param issues
	 */
	private static void chkDet(Object[] in, List<Object[]> converted, List<String> issues) {
		String genusIn = (String)in[1] != null ? (String)in[1] : "";
		String speciesIn = (String)in[2] != null ? (String)in[2] : "";
		String subspeciesIn = (String)in[3] != null ? (String)in[3] : "";
		//boolean hasDet = converted != null && converted.size() > 0;
		//String genus = hasDet ? (String)converted.get(0)[0] : null;
		//String species = hasDet ? (String)converted.get(0)[1] : null;
		//String subspecies = hasDet ? (String)converted.get(0)[2] : null;
		//if (genus == null) genus = "";
		//if (species == null) species = "";
		//if (subspecies == null) subspecies = "";
		String genus = "";
		String species = "";
		String subspecies = "";
		if (converted != null) {
			for (Object[] tax : converted) {
				Integer rank = (Integer)tax[1];
				if (rank == 180) {
					genus = (String)tax[0];
				} else if (rank == 220) {
					species = (String)tax[0];
				} else if (rank == 230) {
					subspecies = (String)tax[0];
				}
			}
		}
		if (!(genusIn.equals(genus) && speciesIn.equals(species) && subspeciesIn.equals(subspecies))) {
			String issue = in[0] + ", " + genusIn + ", " + speciesIn + ", " + subspeciesIn + ", "
					+ genus + ", " + species + ", " + subspecies;
			System.out.print(issue);
			issues.add(issue);
		}
	}
	/**
	 * @param con
	 * @param outFileName
	 * @param accDb
	 * @param taxonTreeDefId
	 */
	private static void checkFlaHerpDeterminationConversion(Connection con, String outFileName,  String accDb,
			Integer taxonTreeDefId) throws Exception {
		String inSql = "select catnum, genus, species, subspecies from " + accDb + ".catalog order by 1";
		List<Object[]> from = BasicSQLUtils.query(con, inSql);
		List<String> issues = new ArrayList<String>();
//		String chkSql = "select (SELECT Name FROM taxon g WHERE g.RankId = 180 AND "
//				+ "g.TaxonTreeDefId = " + taxonTreeDefId
//				+ " AND t.NodeNumber BETWEEN g.NodeNumber AND g.HighestChildNodeNumber), "
//				+ "(SELECT Name FROM taxon s WHERE s.RankId = 220 AND "
//				+ "s.TaxonTreeDefId = " + taxonTreeDefId
//				+ " AND t.NodeNumber BETWEEN s.NodeNumber AND s.HighestChildNodeNumber), "
//				+ "(SELECT Name FROM taxon ss WHERE ss.RankId = 230 AND "
//				+ "ss.TaxonTreeDefId = " + taxonTreeDefId
//				+ " AND t.NodeNumber BETWEEN ss.NodeNumber AND ss.HighestChildNodeNumber) "
//				+ "FROM collectionobject co LEFT JOIN determination d ON d.CollectionObjectID = "
//				+ "co.CollectionObjectID LEFT JOIN taxon t on t.TaxonID = d.TaxonID "
//				+ "WHERE d.IsCurrent AND co.catalognumber = ";
		String chkSql = "SELECT Name, RankID FROM taxon a WHERE a.RankId >= 180 AND a.TaxonTreeDefId = " + taxonTreeDefId 
				+ " AND (SELECT t.NodeNumber FROM collectionobject co INNER JOIN determination d ON d.CollectionObjectID = "
				+ "co.CollectionObjectID INNER JOIN taxon t on t.TaxonID = d.TaxonID "
				+ "WHERE d.IsCurrent AND co.catalognumber = '%s') BETWEEN a.NodeNumber AND a.HighestChildNodeNumber ";
		for (int r = 0; r < from.size(); r++) {
			Object[] row = from.get(r);
			//List<Object[]> det = BasicSQLUtils.query(con, chkSql + row[0]);
			List<Object[]> taxa = BasicSQLUtils.query(con, String.format(chkSql, StringUtils.leftPad(row[0].toString(), 9, "0")));
			System.out.print(r + " of " + from.size() + ". ");
			chkDet(row, taxa, issues);
			System.out.println();
		}
		if (issues.size() > 0) {
			System.out.println(issues.size() + " problems were found. See " + outFileName + ".");
			FileUtils.writeLines(new File(outFileName), "utf8", issues);
		} else {
			System.out.println("Harmoniously Synchronized Perfection.");
		}
	}
	
	private static void updateTaxonParent(Connection con, String selector) {
		List<Object[]> tofix = BasicSQLUtils.query(con, selector);
		int fixed = 0;
		for (Object[] fix : tofix) {
			fixed += BasicSQLUtils.update(con, "update taxon set parentid = " + fix[1] + " where taxonid = " + fix[0]);
		}
		System.out.println("fixed " + fixed + " of " + tofix.size());
	}
	
	private static void fixKUPlantVarSyns(Connection con) {
		String selector = "select distinct t.taxonid, "  
				+ "(select np.taxonid from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' var. ', t.FullName))) "
				+ "from taxon t inner join taxon p on p.taxonid = t.parentid " 
				+ "where (select count(*) from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' var. ', t.FullName))) = 1 "
				+ "and (select np.RankID from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' var. ', t.FullName))) = 220 "
				+ "and t.rankid = 240 and t.fullname like '% var. %' and p.name = 'Synonym Placeholder' and t.name != 'SynonymPlaceholder' "
				+ "order by 1;";
		updateTaxonParent(con, selector);
	}

	private static void fixKUPlantSspSyns(Connection con) {
		String selector = "select distinct t.taxonid, "  
				+ "(select np.taxonid from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ssp. ', t.FullName))) "
				+ "from taxon t inner join taxon p on p.taxonid = t.parentid " 
				+ "where (select count(*) from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ssp. ', t.FullName))) = 1 "
				+ "and (select np.RankID from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ssp. ', t.FullName))) = 220 "
				+ "and t.rankid = 230 and t.fullname like '% ssp. %' and p.name = 'Synonym Placeholder' and t.name != 'SynonymPlaceholder' "
				+ "order by 1;";
		updateTaxonParent(con, selector);
	}

	/**
	 * @param con
	 */
	private static void fixKUPlantSps(Connection con) {
		String selector = "select taxonid, (select np.taxonid from taxon np where np.FullName = left(t.FullName, locate(' ', t.FullName))) "
				+ "from taxon t where nodenumber is null and (select np.taxonid from taxon np where np.FullName = left(t.FullName, locate(' ', t.FullName))) "
				+ "is not null and rankid = 220 order by fullname"; 
		updateTaxonParent(con, selector);
	}

	/**
	 * @param con
	 */
	private static void fixKUPlantSsps(Connection con) {
		String selector = "select taxonid, "
				+ "(select np.taxonid from taxon np where np.isaccepted and np.rankid = 220 and np.FullName = left(t.FullName, locate(' ssp. ', t.FullName))) " 
				+ "from taxon t where nodenumber is null and (select np.taxonid from taxon np where np.isaccepted and np.rankid = 220 and np.FullName = "
				+ "left(t.FullName, locate(' ssp. ', t.FullName))) is not null and rankid = 230"; 
		updateTaxonParent(con, selector);
	}

	/**
	 * @param con
	 */
	private static void fixKUPlantVars(Connection con) {
		String selector = "select taxonid, "
				+ "(select np.taxonid from taxon np where np.isaccepted and np.rankid = 220 and np.FullName = left(t.FullName, locate(' var. ', t.FullName))) " 
				+ "from taxon t where nodenumber is null and (select np.taxonid from taxon np where np.isaccepted and np.rankid = 220 and np.FullName = "
				+ "left(t.FullName, locate(' var. ', t.FullName))) is not null and rankid = 240"; 
		updateTaxonParent(con, selector);
	}

	/**
	 * @param con
	 * @param allowUnacceptedParents
	 */
	private static void fixKUPlantSpSyns(Connection con, boolean allowUnacceptedParents) {
		String selector;
		if (allowUnacceptedParents) {
			 selector = "select distinct t.taxonid, "  
						+ "(select np.taxonid from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ', t.FullName))) "
						+ "from taxon t inner join taxon p on p.taxonid = t.parentid " 
						+ "where (select count(*) from taxon np where np.FullName = left(t.FullName, locate(' ', t.FullName))) = 1 "
						+ "and (select np.RankID from taxon np where np.FullName = left(t.FullName, locate(' ', t.FullName))) = 180 "
						+ "and t.rankid = 220 and p.name = 'Synonym Placeholder' and t.name != 'SynonymPlaceholder' "
						+ "order by 1;";
		} else {
			selector = "select distinct t.taxonid, "  
				+ "(select np.taxonid from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ', t.FullName))) "
				+ "from taxon t inner join taxon p on p.taxonid = t.parentid " 
				+ "where (select count(*) from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ', t.FullName))) = 1 "
				+ "and (select np.RankID from taxon np where np.isAccepted and np.FullName = left(t.FullName, locate(' ', t.FullName))) = 180 "
				+ "and t.rankid = 220 and p.name = 'Synonym Placeholder' and t.name != 'SynonymPlaceholder' "
				+ "order by 1;";
		}
		updateTaxonParent(con, selector);
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			//String connStr = "jdbc:mysql://localhost/kuplantsynclean?characterEncoding=UTF-8&autoReconnect=true";
			
			//fixKUPlantVarSyns(con);
			//fixKUPlantSspSyns(con);
			//fixKUPlantSpSyns(con, true);
			//fixKUPlantSps(con);
			//fixKUPlantSsps(con);
			//fixKUPlantVars(con);
			
//			Connection schemaCon = DriverManager.getConnection("jdbc:mysql://localhost/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
//			"root", "root");
//			String connStr = "jdbc:mysql://localhost/flish6?characterEncoding=UTF-8&autoReconnect=true";
//			Connection con = getConnection(connStr);
//			mergeAgents(con, schemaCon, "flish6", "D:/data/florida/fish/Live/Agent_Standardization_Worksheet Final Edits.csv",
//					',', "utf8", '"', '!', "D:/data/florida/fish/Live/UnStandardizedAgents.txt", true, true);
			

//			String connStr = "jdbc:mysql://bimysql.nhm.ku.edu/KANUVascularPlantDB?characterEncoding=UTF-8&autoReconnect=true";
//			Connection con = DriverManager.getConnection(connStr, "specifymaster", "2Kick@$$");
//			Connection schemaCon = DriverManager.getConnection("jdbc:mysql://bimysql.nhm.ku.edu/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
//					"specify", "5(9Q[t$.h2K3");
//			mergeAgents(con, schemaCon, "KANUVascularPlantDB", "D:/data/KU/VascularPlant/FreemanMerger.txt",
//					',', "utf8", '"', '!', "D:/data/KU/VascularPlant/UnStandardizedAgents.txt", false, false);

//			String connStr = "jdbc:mysql://localhost/lsu_herp?characterEncoding=UTF-8&autoReconnect=true";
//			Connection con = DriverManager.getConnection(connStr, "Master", "Master");
//			Connection schemaCon = DriverManager.getConnection("jdbc:mysql://localhost/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
//					"root", "root");
//			mergeAgents(con, schemaCon, "lsu_herp", "D:/data/lsus/herps/HardyMerger.txt",
//					',', "utf8", '"', '!', "D:/data/lsus/herps/UnStandardizedAgents.txt", false, false);

			boolean doRemap = false;
			//String connStr = "jdbc:mysql://localhost/flaleob6?characterEncoding=UTF-8&autoReconnect=true";
			String connStr = "jdbc:mysql://localhost/florni?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = DriverManager.getConnection(connStr, "root", "root");
			//Connection schemaCon = DriverManager.getConnection("jdbc:mysql://localhost/information_schema?characterEncoding=UTF-8&autoReconnect=true", 
			//		"root", "root");

			//parseFlaVpPubs(con);
			
			//UFlaUtils.fixFlsVpCollStrsForParse(con);
			//doFlaleobotLatLngs(con, "LocalityID > 8663");
			//doFlaleobotLatLngs(con, null);
			doFlaleobotLatLngsFromReturnedParse(con);
			
//			checkFlaHerpDeterminationConversion(con, "D:/data/florida/herps/live/DeterminationIssues.csv",
//					"flerp", 1);
			
			//createAttachmentsForUncatalogedTransactionItems(con, "loan", "flish", "flish6", 
			//		"D:/data/florida/fish/csv");
			//createAttachmentsForUncatalogedTransactionItems(con, "gift", "flish", "flish6", 
			//		"D:/data/florida/fish/csv");
//			createAttachmentsForUncatalogedTransactionItems(con, "borrow", "flish", "flish6", 
//					"D:/data/florida/fish/csv");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
