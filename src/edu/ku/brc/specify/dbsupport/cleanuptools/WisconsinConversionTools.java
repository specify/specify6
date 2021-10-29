/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.hibernate.tool.hbm2x.StringUtils;

import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.GeoRefConverter;
import edu.ku.brc.util.LatLonConverter;
import edu.ku.brc.util.Pair;

/**
 * @author Tadmin
 *
 */
public class WisconsinConversionTools {

	
	enum SpDetailTaxaParts{Unknown, Genus, Species, SspIndicator, VarIndicator, Subspecies, 
		Variety, Author, GenAuthor, SpAuthor, SspAuthor, VarAuthor, TaxName};
		
	
	protected static boolean isVarInd(String str)
	{
		return "var".equals(str) || "var.".endsWith(str);
	}
	
	protected static boolean isSspInd(String str)
	{
		return "ssp".equals(str) || "ssp.".equals(str) || "subsp".equals(str) || "subsp.".equals(str);
	}
	
	protected static Set<SpDetailTaxaParts> getNextExpected(SpDetailTaxaParts part, 
			SpDetailTaxaParts prev) throws Exception
	{
		Set<SpDetailTaxaParts> result = new HashSet<SpDetailTaxaParts>();
		if (part.equals(SpDetailTaxaParts.Genus))
		{
			result.add(SpDetailTaxaParts.GenAuthor);
			result.add(SpDetailTaxaParts.Species);
		} else if (part.equals(SpDetailTaxaParts.Species))
		{
			result.add(SpDetailTaxaParts.SpAuthor);
			result.add(SpDetailTaxaParts.SspIndicator);
			result.add(SpDetailTaxaParts.VarIndicator);
		} else if (part.equals(SpDetailTaxaParts.SspIndicator))
		{
			result.add(SpDetailTaxaParts.Subspecies);
		} else if (part.equals(SpDetailTaxaParts.VarIndicator))
		{
			result.add(SpDetailTaxaParts.Variety);
		} else if (part.equals(SpDetailTaxaParts.SpAuthor) || part.equals(SpDetailTaxaParts.SspAuthor)|| 
				part.equals(SpDetailTaxaParts.VarAuthor) || part.equals(SpDetailTaxaParts.GenAuthor))
		{
			result.add(part);
			if (part.equals(SpDetailTaxaParts.GenAuthor))
			{
				result.add(SpDetailTaxaParts.Species);
			}
			if (part.equals(SpDetailTaxaParts.SpAuthor))
			{
				result.add(SpDetailTaxaParts.SspIndicator);
				result.add(SpDetailTaxaParts.VarIndicator);
			} else if (part.equals(SpDetailTaxaParts.SspAuthor))
			{
				result.add(SpDetailTaxaParts.VarIndicator);
			}
		} else if (part.equals(SpDetailTaxaParts.Subspecies))
		{
			result.add(SpDetailTaxaParts.SspAuthor);
			result.add(SpDetailTaxaParts.VarIndicator);
		} else if (part.equals(SpDetailTaxaParts.Variety))
		{
			result.add(SpDetailTaxaParts.VarAuthor);
		} else
		{
			throw new Exception("getNextExpected don't know what to do with " + part);
		}

		return result;
	}

	protected static boolean couldBeALowerCaseHumanName(String str)
	{
		if (str.startsWith("de"))
		{
			String three = str.substring(2, 3);
			if (StringUtils.isNotEmpty(three))
			{
				return three.toUpperCase().equals(three);
			}
		}
		return false;
	}
	
	protected static Integer adjustLatLngUnitForComparison(LatLonConverter.FORMAT f)  throws Exception {
		if (f == null) return 0;
		if (LatLonConverter.FORMAT.None.equals(f)) return 0;
		if (LatLonConverter.FORMAT.DDDDDD.equals(f)) return 1;
		if (LatLonConverter.FORMAT.DDMMMM.equals(f)) return 2;
		if (LatLonConverter.FORMAT.DDMMSS.equals(f)) return 3;
		throw new Exception("unrecognized format: " + f);
	}
	
	protected static int compareLatLngUnit(LatLonConverter.FORMAT f1, LatLonConverter.FORMAT f2) throws Exception {
		return adjustLatLngUnitForComparison(f1).compareTo(adjustLatLngUnitForComparison(f2));
	}
	
	protected static void fixLatLngTextOrigUnitMismatches(Connection conn) throws Exception {
		String sql = "select lat1text, long1text, originallatlongunit, localityid from locality where latitude1 is not null and "
				+ "Lat1Text is not null and Long1Text is not null and longitude1 is not null";
		List<Object[]> geocs = BasicSQLUtils.query(conn, sql);
		GeoRefConverter g = new GeoRefConverter();
		int mismatches = 0;
		int fixes = 0;
		for (Object[] geoc : geocs) {
			//XXX Need to run:
			//update locality set lat1text = replace(lat1text, ':', ' '), long1text = replace(long1text, ':', ' '),
			//		lat2text = replace(lat2text, ':', ' '), long2text = replace(long2text, ':', ' ');
			//LatLonConverter.FORMAT latF = g.getLatLonFormat(((String)geoc[0]).replaceAll(":", " "));
			//LatLonConverter.FORMAT lngF = g.getLatLonFormat(((String)geoc[1]).replaceAll(":", " "));
			LatLonConverter.FORMAT latF = g.getLatLonFormat((String)geoc[0]);
			LatLonConverter.FORMAT lngF = g.getLatLonFormat((String)geoc[1]);
			Integer origLatLongUnit = (Integer)geoc[2]; 
			int unit = compareLatLngUnit(latF, lngF) >= 0 ? latF.ordinal() : lngF.ordinal();
			if (unit != origLatLongUnit || unit == LatLonConverter.FORMAT.None.ordinal()
					|| origLatLongUnit == LatLonConverter.FORMAT.None.ordinal()) {
				mismatches++;
				if (latF.ordinal() == LatLonConverter.FORMAT.None.ordinal() || lngF.ordinal() == LatLonConverter.FORMAT.None.ordinal()) {
					System.out.println(geoc[0] + ", " + geoc[1] + ", " + geoc[2]);
				} else {
					BasicSQLUtils.update(conn, "update locality set originallatlongunit=" + unit + " where localityid=" + geoc[3]);
					fixes++;
				}
				
			}
		}
		System.out.println("Fixed " + fixes + " of " + mismatches + " mismatches");
	}
	
	/*
	 * Fix originallatlongunit for geocoords that have already been created from lat1/long1Text fields
	 * but are missing OriginalLatLongUnits
	 */
	protected static void fixWisconsinHerbOriginalLatLongUnit(Connection conn) {
		String sql = "select distinct lat1text, long1text from locality where latitude1 is not null and "
				+ "Lat1Text is not null and Long1Text is not null and "
				+ "(originallatlongunit = 3 or originallatlongunit is null)";
		List<Object[]> geocs = BasicSQLUtils.query(conn, sql);
		GeoRefConverter g = new GeoRefConverter();
		List<String> errRecs = new ArrayList<String>();
		String upSql = "update locality set OriginalLatLongUnit = %d where Lat1Text='%s' and Long1Text='%s' "
				+ "and (originallatlongunit = 3 or originalLatlongunit is null)";
	    Scanner in = new Scanner(System.in);
		for (Object[] geoc : geocs) {
			LatLonConverter.FORMAT latF = g.getLatLonFormat((String)geoc[0]);
			LatLonConverter.FORMAT lngF = g.getLatLonFormat((String)geoc[1]);
			int originalLatLongUnit = latF.ordinal(); 
			if (latF != lngF || latF.equals(LatLonConverter.FORMAT.None)) {
				boolean go = true;
				while (go) {
					System.out.println(latF.name() + " / " + lngF.name() + " <== " + geoc[0] + " / " + geoc[1] + "?");
					String response = in.nextLine();
					try {
						Integer u = Integer.valueOf(response);
						String uText = u == 0 ? "DDDDD" : (u == 1 ? "DDMMSS" : (u == 2 ? "DDMMMM" : "NONE/SKIP"));
						System.out.println("Set OriginalLatLongUnit to  " + uText + "? (enter 'n' to retry)");
						response = in.nextLine();
						if ("y".equalsIgnoreCase(response)) {
							originalLatLongUnit = u;
							go = false;
						} 
					} catch (Exception ex) {
						System.out.println(ex.getMessage());
					}
				}
			} 	
			int up = -1;
			if (originalLatLongUnit != 3) {
				String upper = String.format(upSql, originalLatLongUnit, BasicSQLUtils.escapeStringLiterals((String)geoc[0]), 
						BasicSQLUtils.escapeStringLiterals((String)geoc[1]));
				System.out.println(upper);
				up = BasicSQLUtils.update(conn, upper);
			} 
			if (up != 1) {
				System.out.println("error updating: " + geoc[0] + " / " + geoc[1]);
				errRecs.add(geoc[0] + " / " + geoc[1]);
			}
		}
		System.out.println(geocs.size() + " geocoords, " + errRecs.size() + " errors.");
		for (String eRec : errRecs) {
			System.out.println(eRec);
		}
	}
	
	protected static void interactivelyUnMangleField(Connection conn, String tblName, String fldName, String mangledness)
	{
		String sql = "select distinct `" + fldName + "` from `" + tblName + "` where `" 
			+ fldName + "` like '%" + mangledness + "%' order by 1";
		Vector<Object> names = BasicSQLUtils.querySingleCol(conn, sql);
		System.out.println(names.size() + " matches found.");
	    Scanner in = new Scanner(System.in);
		for (Object nameObj : names)
		{
			String name = nameObj.toString();
			String replacement = getUnmangler(conn, fldName, mangledness, name, in);
			if (StringUtils.isNotEmpty(replacement))
			{
				sql = "update `" + tblName + "`set `" + fldName + "` = replace(`" + fldName + "`, '" 
					+ mangledness + "', '" + replacement + "') where `" + fldName + "` = '" + name + "'";
				BasicSQLUtils.update(conn, sql);
			}
		}
		in.close();
	}
	
	protected static String getUnmangler(Connection conn, String fldName, String mangledness, String name, Scanner in)
	{
	    String result = null;
		boolean go = true;
		while (go)
		{
			String hint = "";
			if (fldName.equalsIgnoreCase("Interests")) //the unparsed original
			{
				String sql = "select LastName, FirstName, MiddleInitial from agent where Interests = '" + name + "'";
				Vector<Object[]> matches = BasicSQLUtils.query(conn, sql);
				if (matches.size() > 0)
				{
					Object[] first = matches.get(0);
					for (Object obj : first)
					{
						if (obj != null)
							hint += obj.toString() + " ";
					}
				}
			}
			System.out.println("Replace '" + mangledness + "' in '" + name + "' with? " + hint + " (enter blank and choose to replace to cancel)");
			result = in.nextLine();
			System.out.println("Replace  '" + name + "' with '" + name.replace(mangledness, result) + "'? (enter 'n' to retry)");
			String response = in.nextLine();
			go = "n".equalsIgnoreCase(response);
		}
	    return result;
	}
	
	/**
	 * @param genus
	 * @param species
	 * @param taxa
	 * @return list of pieces parsed from the taxa field. Each piece consists of its value
	 * and its name - Genus, Species, SspIndicator, VarIndicator, Subspecies, Variety, Author
	 */
	protected static List<Pair<String, SpDetailTaxaParts>> parseSpDetailTaxa(String genus, String species, String taxa)
		throws Exception
	{
		if (StringUtils.isEmpty(genus))
		{
			throw new Exception("parseSpDetailTaxa: invalid input: " + genus + ", " + species + ", " + taxa);
		}
		
		Vector<Pair<String, SpDetailTaxaParts>> parts = new Vector<Pair<String, SpDetailTaxaParts>>();
		String workingCopy = taxa;
		String[] chunks = workingCopy.split(" ");
		if (genus.equals(chunks[0]))
		{
			parts.add(new Pair<String, SpDetailTaxaParts>(genus, SpDetailTaxaParts.Genus));
		} else
		{
			parts.add(new Pair<String, SpDetailTaxaParts>(chunks[0], SpDetailTaxaParts.Unknown));
		}
//		if (species.equals(chunks[1]))
//		{
//			parts.add(new Pair<String, SpDetailTaxaParts>(species, SpDetailTaxaParts.Species));
//		} else
//		{
//			parts.add(new Pair<String, SpDetailTaxaParts>(chunks[1], SpDetailTaxaParts.Unknown));
//		}
		
		SpDetailTaxaParts previousPart = null;
		Set<SpDetailTaxaParts> expected = getNextExpected(SpDetailTaxaParts.Genus, null);
		
		int i = 1;
		while(i < chunks.length)
		{
			SpDetailTaxaParts part = SpDetailTaxaParts.Unknown;
			String chunk = chunks[i++];
			
			if (StringUtils.isEmpty(chunk)) continue;
			
			if (isSspInd(chunk))
			{
				part = SpDetailTaxaParts.SspIndicator;
			} else if (isVarInd(chunk))
			{
				part = SpDetailTaxaParts.VarIndicator;
			} else if (chunk.startsWith("(") || "&".equals(chunk) || "auct.non".equals(chunk) || "ex".equals(chunk) || "ex.".equals(chunk)
					|| "non".equals(chunk) || "in".equals(chunk)|| "auct.".equals(chunk)|| "sens.".equals(chunk) || "Mot.".equals(chunk)
					|| "de".equals(chunk) || "auct".equals(chunk) || "sensu.".equals(chunk) || "sensu".equals(chunk) 
					|| "nom.".equals(chunk) || "nudum".equals(chunk)
					|| "mis-id's)".equals(chunk)
					|| "records)".equals(chunk)) 
			{
				part = SpDetailTaxaParts.Author;
			} else
			{
				String first = chunk.substring(0, 1);
				if (first.equals(first.toUpperCase()) || couldBeALowerCaseHumanName(chunk))
				{
					part = SpDetailTaxaParts.Author;
				} else 
				{
					part = SpDetailTaxaParts.TaxName;
				}
			}
			if (part.equals(SpDetailTaxaParts.Author))
			{
				if (expected.contains(SpDetailTaxaParts.GenAuthor))
				{
					part = SpDetailTaxaParts.GenAuthor;
				} else if (expected.contains(SpDetailTaxaParts.SpAuthor))
				{
					part = SpDetailTaxaParts.SpAuthor;
				} else if (expected.contains(SpDetailTaxaParts.SspAuthor))
				{
					part = SpDetailTaxaParts.SspAuthor;
				}  else if (expected.contains(SpDetailTaxaParts.VarAuthor))
				{
					part = SpDetailTaxaParts.VarAuthor;
				}
			} else if (part.equals(SpDetailTaxaParts.TaxName))
			{
				if (expected.contains(SpDetailTaxaParts.Species))
				{
					part = SpDetailTaxaParts.Species;
				} else if (expected.contains(SpDetailTaxaParts.Subspecies))
				{
					part = SpDetailTaxaParts.Subspecies;
				}  else if (expected.contains(SpDetailTaxaParts.Variety))
				{
					part = SpDetailTaxaParts.Variety;
				}
			}
			boolean ok = expected.contains(part);
			if (!ok)
			{
				throw new Exception("parseSpDetailTaxa: error parsing " + taxa + " at " + chunk);
			}
			parts.add(new Pair<String, SpDetailTaxaParts>(chunk, part));
			expected = getNextExpected(part, previousPart);
			previousPart = part;
		}
		
		previousPart = null;
		Vector<Pair<String, SpDetailTaxaParts>> result = new Vector<Pair<String, SpDetailTaxaParts>>();
		String partStr = "";
		for (Pair<String, SpDetailTaxaParts> p : parts)
		{
			if (p.getSecond().equals(previousPart))
			{
				partStr += " " + p.getFirst();
			} else
			{
				if (previousPart != null)
				{
					result.add(new Pair<String, SpDetailTaxaParts>(partStr, previousPart));
				}
				partStr = p.getFirst();
			}
			previousPart = p.getSecond();
		}
		result.add(new Pair<String, SpDetailTaxaParts>(partStr, previousPart));
		return result;
	}
	
	private static void updateAcceptedIDs(Connection conn)
	{
		Vector<Object[]> spdetails = BasicSQLUtils.query(conn, "select id, syncd, taxcd from spdetail where syncd != '.'");
		for (Object[] row : spdetails)
		{
			Long acceptedid = BasicSQLUtils.querySingleObj(conn, "select id from spdetail p where p.taxcd = '" + row[2] + "' and p.syncd = '.'");
			BasicSQLUtils.update(conn, "update spdetail set acceptedid = " + acceptedid + " where id = " + row[0]);
		}		
	}
	
	private static void parseAuthors(Connection conn)
	{
		Vector<Object[]> spdetails = BasicSQLUtils.query(conn, "select * from spdetail");
		System.out.println(spdetails.size() + " spdetails.");
		//updateAcceptedIDs(conn);
		Vector<String> crashes = new Vector<String>();
		for (Object[] rec : spdetails)
		{
			try 
			{
				//System.out.println(rec[2] + ", " + rec[3] + ", " + rec[5]);
				List<Pair<String, SpDetailTaxaParts>> parse = parseSpDetailTaxa((String )rec[2], (String )rec[3], (String )rec[5]);
				for (Pair<String, SpDetailTaxaParts> p : parse)
				{
					//System.out.println("    " + p.getFirst() + " - " + p.getSecond());
					if (p.getSecond().equals(SpDetailTaxaParts.SpAuthor))
					{
						BasicSQLUtils.update(conn, "update spdetail set SpeciesAuthor = '" + p.getFirst().replace("'", "''") + "' where id = " + rec[16]);
					} else if (p.getSecond().equals(SpDetailTaxaParts.GenAuthor))
					{
						BasicSQLUtils.update(conn, "update spdetail set GenusAuthor = '" + p.getFirst().replace("'", "''")  + "' where id = " + rec[16]);
					} else if (p.getSecond().equals(SpDetailTaxaParts.SspAuthor))
					{
						BasicSQLUtils.update(conn, "update spdetail set SubspeciesAuthor = '" + p.getFirst().replace("'", "''")  + "' where id = " + rec[16]);
					} else if (p.getSecond().equals(SpDetailTaxaParts.VarAuthor))
					{
						BasicSQLUtils.update(conn, "update spdetail set VarietyAuthor = '" + p.getFirst().replace("'", "''")  + "' where id = " + rec[16]);
					} else if (p.getSecond().equals(SpDetailTaxaParts.Variety))
					{
						BasicSQLUtils.update(conn, "update spdetail set Variety = '" + p.getFirst().replace("'", "''")  + "' where id = " + rec[16]);
					} else if (p.getSecond().equals(SpDetailTaxaParts.Subspecies))
					{
						BasicSQLUtils.update(conn, "update spdetail set Subspecies = '" + p.getFirst().replace("'", "''")  + "' where id = " + rec[16]);
					} 
				}
			} catch (Exception e)
			{
				crashes.add(rec[2] + ", " + rec[3] + ", " + rec[5] + ": " + e.getMessage());
			}
			
		}
		System.out.println("crashes: " + crashes.size() + " out of " + spdetails.size());
		for (String crash : crashes)
		{
			System.out.println(crash);
		}
	}
	
	public static void updateSpecimenLocID(Connection conn, Object[] specRec) throws Exception
	{
		String[] flds = {"City", "CityType", "Lon", "Lat", "Elev", "LLGener",
				"Lon2", "Lat2", "Nowloc", "prec", "T1", "R1", "S1", "NSEW_1", "TRSGener",
				"T2", "R2", "S2", "NSEW_2", "Place", "Mapfile", "DTRS", "Country",
				"State", "County"};
		
		String sql = "update specimen s set locid = (select l.id from sp6convloc l inner join "
			+ "sp6convgeo g on g.id = l.geoid where ";
		//String sql = "select accession, (select l.id from sp6convloc l inner join "
		//	+ "sp6convgeo g on g.id = l.geoid where ";
		int f = 1;
		for (String fld : flds)
		{
			String clause;
			if (specRec[f] == null)
			{
				clause =  fld + " is null";
			}
			else
			{
				clause = fld + " = " + BasicSQLUtils.getStrValue(specRec[f]); 
			}
			if (f++ > 1)
			{
				sql += " and ";
			}
			sql += "(" + clause + ")";
		}
		sql += ") where accession = '" + specRec[0] + "'";
		//Vector<Object[]> result = BasicSQLUtils.query(conn, sql);
		
		
		//BasicSQLUtils.update(conn, sql);
        Statement stmt = null;
        stmt = conn.createStatement();
        stmt.executeUpdate(sql);
        stmt.close();

		//		if (result == null) 
//		{
//			System.out.println(specRec[0] + " NULLLLLLLLLLLLLLLLL ");
//		} else if (result.size() > 1)
//		{
//			System.out.println(specRec[0] + " TOOOOOOO MANNNAYAAAAAAAAAYYYYYYYYYYYYYYYYYYYYYYY");
//		} else
//		{
//			System.out.println(result.get(0)[0] + " " + result.get(0)[1]);
//		}
	}
	
	public static void updateSpecimenLocIDs(Connection conn) throws Exception
	{
		String[] flds = {"City", "CityType", "`Long`", "Lat", "Elev", "LLGener",
				"Long2", "Lat2", "Nowloc", "prec", "T1", "R1", "S1", "NSEW_1", "TRSGener",
				"T2", "R2", "S2", "NSEW_2", "Place", "Mapfile", "DTRS", "Country",
				"StateL", "County"};
		String sql = "select accession ";
		for (String fld : flds)
		{
			sql += "," + fld;
		}
		sql += " from specimen where locid is null order by accession";
		Vector<Object[]> specs = BasicSQLUtils.query(conn, sql);
		Vector<String> errs = new Vector<String>();
		for (Object[] specRec : specs)
		{
			try
			{
				updateSpecimenLocID(conn, specRec);
			} catch (Exception ex)
			{
				errs.add(specRec[0] + ex.getLocalizedMessage());
			}
		}
		FileUtils.writeLines(new File("C:\\Data\\Wisconsin\\locIdErrors.txt"), errs);
	}
	
	public static void convertLatLons(Connection conn)
	{
		String sql = "select accession  from specimen where locid is null order by accession";
		Vector<Object[]> specs = BasicSQLUtils.query(conn, sql);
		
	}
	
	public static Vector<String> parseCollectorsList(String collectors,
			Vector<String> suffixes)
	{
		if (collectors == null)
		{
			return new Vector<String>();
		}
		
		String[] chunks = collectors.split(" with ");
		if (chunks.length > 1)
		{
			Vector<String> result = parseCollectorsList(chunks[0], suffixes);
			for (int c = 1; c < chunks.length; c++)
			{
				result.addAll(parseCollectorsList(chunks[c], suffixes));
			}
			return result;
		}
		
		chunks = collectors.split(" and ");
		if (chunks.length > 1)
		{
			Vector<String> result = parseCollectorsList(chunks[0], suffixes);
			for (int c = 1; c < chunks.length; c++)
			{
				result.addAll(parseCollectorsList(chunks[c], suffixes));
			}
			return result;
		}

		chunks = collectors.split(" & ");
		if (chunks.length > 1)
		{
			Vector<String> result = parseCollectorsList(chunks[0], suffixes);
			for (int c = 1; c < chunks.length; c++)
			{
				result.addAll(parseCollectorsList(chunks[c], suffixes));
			}
			return result;
		}

		chunks = collectors.split(",");
		
		Vector<String> result = new Vector<String>();
		for (int c = 0; c < chunks.length; c++)
		{
			if (suffixes.indexOf(chunks[c].trim()) != -1)
			{
				int lastPos = result.size() - 1;
				String newColl = result.get(lastPos) + ", " + chunks[c];
				result.setElementAt(newColl.trim(), lastPos);
			}
			else
			{
				result.add(chunks[c].trim());
			}
		}
		return result;
	}
	
	public static void parseCollectors(Connection conn)
	{
		Vector<Object> colls = BasicSQLUtils.querySingleCol(conn, 
				"select distinct Collector from dwc47loc");
		Vector<String> suffixes = new Vector<String>();
		suffixes.add("Jr.");
		suffixes.add("Jr");
		suffixes.add("III");
		for (Object coll : colls)
		{
			System.out.println(coll);
			if (coll != null)
			{
				Vector<String> names = parseCollectorsList(coll.toString(), suffixes);
				int n = 1;
				for (String name : names)
				{
					System.out.println("     " + name);
					String updateSql = "update dwc47loc set collector" + n++ + "='" + name.replace("'", "''") 
						+ "' where Collector = '" + coll.toString().replace("'", "''") + "'";
					BasicSQLUtils.update(conn, updateSql);
				}
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		//String connStr = "jdbc:mysql://localhost/herblichen2_dbo?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/wisflora?characterEncoding=UTF-8&autoReconnect=true"; 
		//String connStr = "jdbc:mysql://localhost/dwc47loc?characterEncoding=UTF-8&autoReconnect=true"; 
		String connStr = "jdbc:mysql://localhost/santana?characterEncoding=UTF-8&autoReconnect=true"; 
		try
		{
			Connection conn = DriverManager.getConnection(connStr, "Master", "Master");

			fixLatLngTextOrigUnitMismatches(conn);
			
			//fixWisconsinHerbOriginalLatLongUnit(conn);
			//interactivelyUnMangleField(conn, "agent", "Interests", "ï¿½");
			//updateSpecimenLocIDs(conn);
			//updateAcceptedIDs(conn);
//			parseCollectors(conn);
//			Vector<Object[]> spdetails = BasicSQLUtils.query(conn, "select * from spdetail");
//			System.out.println(spdetails.size() + " spdetails.");
//			//updateAcceptedIDs(conn);
//			parseAuthors(conn);
//			Vector<String> crashes = new Vector<String>();
//			for (Object[] rec : spdetails)
//			{
//				try 
//				{
//					System.out.println(rec[2] + ", " + rec[3] + ", " + rec[5]);
//					List<Pair<String, SpDetailTaxaParts>> parse = parseSpDetailTaxa((String )rec[2], (String )rec[3], (String )rec[5]);
//					for (Pair<String, SpDetailTaxaParts> p : parse)
//					{
//						System.out.println("    " + p.getFirst() + " - " + p.getSecond());
//					}
//				} catch (Exception e)
//				{
//					crashes.add(rec[2] + ", " + rec[3] + ", " + rec[5] + ": " + e.getMessage());
//				}
//				
//			}
//			System.out.println("crashes: " + crashes.size() + " out of " + spdetails.size());
//			for (String crash : crashes)
//			{
//				System.out.println(crash);
//			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

}
