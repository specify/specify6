/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class LocalityStringParser extends UtilitaryBase {
	protected final Integer geoTreeDefId;
	protected final List<GeoRank> geoRanks;
	protected final List<Pair<String,String>> replacements;
	protected final List<String> splitters;
	
	
	/**
	 * @param con
	 * @param sp6con
	 * @param geoTreeDefId
	 */
	public LocalityStringParser(Connection con, Integer geoTreeDefId, List<Pair<String,String>> replacements,
			List<String> splitters) throws Exception {
		super(con);
		this.geoTreeDefId = geoTreeDefId;
		geoRanks = buildGeoRanks(con, geoTreeDefId);
		this.replacements = new ArrayList<Pair<String,String>>(replacements);
		this.splitters = new ArrayList<String>(splitters);
	}

	/**
	 * @param con
	 * @param geoTreeDefId
	 * @return
	 * @throws Exception
	 */
	private List<GeoRank> buildGeoRanks(Connection con, Integer geoTreeDefId) throws Exception {
		List<GeoRank> result = new ArrayList<GeoRank>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT Name, RankID, IsEnforced FROM geographytreedefitem WHERE GeographyTreeDefID=" 
				+ geoTreeDefId + " ORDER BY RankID");
		try {
			while (rs.next()) {
				result.add(new GeoRank(rs.getString(1), rs.getInt(2), rs.getBoolean(3)));
			}
		} finally {
			rs.close();
			stmt.close();
		}
		return result;
	}

	/**
	 * @param str
	 * @return
	 */
	protected String applyReplacements(String str) {
		String result = str;
		for (Pair<String,String> rep : replacements) {
			result = result.replace(rep.getFirst(), rep.getSecond());
		}
		return result;
	}
	
	/**
	 * @param toPrep
	 * @return
	 */
	protected String prepareForParse(String toPrep) {
		return applyReplacements(toPrep);
	}
	
	/**
	 * @param loc
	 * @return list of RankName/Name pairs for a locality string from the kubirds access `tissue catalog` table.
	 * @throws Exception
	 */
	protected List<Pair<Pair<Integer, String>, String>> parseLocalityName(String loc, List<String> splitters) throws Exception {
		List<Pair<Pair<Integer,String>, String>> result = new ArrayList<Pair<Pair<Integer, String>, String>>();
		//String [] pieces = loc.split(splitter);
		String[] pieces = splitUp(loc, splitters);
		List<GeoRank> validGeoRanks = new ArrayList<GeoRank>(geoRanks);
		for (String piece : pieces) {
			Pair<Pair<Integer,String>, String> m = null;
			GeoRank mRank = null;
			for (GeoRank g : validGeoRanks) {
				mRank = g;
				m = lookFor(piece.trim(), g);
				if (m != null) {
					break;
				}
			} 
			adjustValidGeoRanks(mRank, validGeoRanks);
			if (m == null) {
				m = new Pair<Pair<Integer, String>, String>(new Pair<Integer, String>(null, piece.trim()), "Locality");
			} 
			result.add(m);
		}
		return result;
	}

	/**
	 * @param l
	 * @return
	 */
	protected <T> Stack<T> stackUp(List<T> l) {
		Stack<T> result = new Stack<T>();
		for (int i = l.size()-1; i >= 0; i--) {
			result.push(l.get(i));
		}
		return result;
	}
	
	/**
	 * @param splitty
	 * @param splitters
	 * @return
	 */
	protected String[] splitUp(String splitty, List<String> splitters) {
		Stack<String> splitStack = stackUp(splitters);
		String toSplit = splitty;
		List<String> result = new ArrayList<String>();
		while (!splitStack.empty()) {
			String splitter = splitStack.pop();
			String[] pieces = toSplit.split(splitter);
			if (pieces.length > 1) {
				for (int p=0; p<pieces.length-1; p++) {
					result.add(pieces[p]);
				}
			}
			toSplit = pieces[pieces.length - 1];
		}
		result.add(toSplit);
		
		String[] a = new String[result.size()];
		for (int s = 0; s < result.size(); s++) {
			a[s] = result.get(s);
		}
		return a;
	}
	
	/**
	 * @param aLoc
	 * @return
	 * @throws Exception
	 */
	protected List<List<Pair<Pair<Integer, String>, String>>> parseLocalityName(String aLoc) throws Exception {
		String loc = prepareForParse(aLoc);
		List<List<Pair<Pair<Integer, String>, String>>> result = new ArrayList<List<Pair<Pair<Integer, String>, String>>>();
		
		for (String splitter : splitters) {
			List<String> spL = new ArrayList<String>();
			spL.add(splitter);
			//result.add(parseLocalityName(loc, spL));
		}
		
		result.add(parseLocalityName(loc, splitters));
		
		
		Collections.sort(result, new Comparator<List<Pair<Pair<Integer, String>, String>>>() {

			/* (non-Javadoc)
			 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
			 */
			@Override
			public int compare(List<Pair<Pair<Integer, String>, String>> o1,
					List<Pair<Pair<Integer, String>, String>> o2) {
				//descending order
				int m1 = countGeoRanks(o2);
				int m2 = countGeoRanks(o1);
				return m1 < m2 ? -1 : (m1 == m2 ? 0 : 1);
			}
			
		});
		return result;
	}
	
	/**
	 * @param selected
	 * @param validGeoRanks
	 */
	protected void adjustValidGeoRanks(GeoRank selected, List<GeoRank> validGeoRanks) {
		for (int g = validGeoRanks.size() - 1; g >= 0; g--) {
			if (validGeoRanks.get(g).getId() <= selected.getId()) {
				validGeoRanks.remove(g);
			}
		}
		//XXX need to consider IsEnforced!!!
	}
	
	/**
	 * @param name
	 * @param atRank
	 * @return
	 * @throws Exception
	 */
	protected Pair<Pair<Integer, String>, String> lookFor(String name, GeoRank atRank) throws Exception {
		Pair<Pair<Integer,String>, String> result = null;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(atRank.getMatchSql(name));
		try {
			if (rs.next()) {
				result = new Pair<Pair<Integer,String>, String>(new Pair<Integer,String>(rs.getInt(1), name), atRank.getName());
			}
		} finally {
			rs.close();
			stmt.close();
		}
		return result;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kubird6?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			connStr = "jdbc:mysql://localhost/kubirdt?characterEncoding=UTF-8&autoReconnect=true";
			Connection tcon = getConnection(connStr, "Master", "Master");
			
			List<Pair<String,String>> replacements = new ArrayList<Pair<String,String>>();
			//replacements.add(new Pair<String,String>(";", ":"));
			List<String> splitters = new ArrayList<String>();
			splitters.add(":");
			splitters.add(";");
			splitters.add(",");
			LocalityStringParser p = new LocalityStringParser(con, 1, replacements, splitters);
			Statement stmt = tcon.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT distinct Locality from unvoucheredtissue WHERE Locality is not null");
			try {
				while (rs.next()) {
					System.out.println(rs.getString(1));
					List<List<Pair<Pair<Integer, String>, String>>> geolocs = p.parseLocalityName(rs.getString(1));
					for (List<Pair<Pair<Integer, String>, String>> geoloc : geolocs) { 
						for (Pair<Pair<Integer, String>, String> g : geoloc) {
							System.out.println("  " + g.getFirst().getSecond() + ":" + g.getFirst().getFirst() + "/" + g.getSecond());
						}
						System.out.println();
					}
				}
			} finally {
				rs.close();
				stmt.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param matches
	 * @return
	 */
	protected int countGeoRanks(List<Pair<Pair<Integer, String>, String>> matche) {
		int result = 0;
		for (Pair<Pair<Integer, String>, String> m : matche) {
			if (!"Locality".equalsIgnoreCase(m.getSecond())) {
				result++;
			}
		}
		return result;
	}
	
	/**
	 * @param matches
	 * @return
	 */
	public Integer getBestGeographyID(List<List<Pair<Pair<Integer, String>, String>>> matches) {
		return getMostSpecificGeographyID(matches.get(0));
	}
	
	/**
	 * @param geos
	 * @return
	 */
	public String getLocNameForBestGeo(List<List<Pair<Pair<Integer, String>, String>>> geos) {
		List<Pair<Pair<Integer, String>, String>> bg = geos.get(0);
		String split = splitters.get(splitters.size() - 1);
		String result = "";
		for (Pair<Pair<Integer, String>, String> g : bg) {
			if ("Locality".equalsIgnoreCase(g.getSecond())) {
				if (!"".equals(result)) {
					result += split;
				}
				result += g.getFirst().getSecond();
			}
		}
		return result.trim();
	}
	
	/**
	 * @param geoRanks
	 * @return
	 */
	public Integer getMostSpecificGeographyID(List<Pair<Pair<Integer, String>, String>> geoRanks) {
		Integer result = null;
		for (int g=0; g < geoRanks.size(); g++) {
			Pair<Pair<Integer, String>, String> geo = geoRanks.get(g); 
			if (!"Locality".equalsIgnoreCase(geo.getSecond())) {
				result = geo.getFirst().getFirst();
			} else {
				break;
			}
		}
		return result;
	}
	
	/**
	 * @author timo
	 *
	 */
	private class GeoRank {
		private final String name;
		private final Integer id;
		private final boolean isEnforced;
		/**
		 * @param name
		 * @param rankName
		 * @param rankId
		 * @param isRequired
		 */
		public GeoRank(String name, Integer id, boolean isEnforced) {
			super();
			this.name = name;
			this.id = id;
			this.isEnforced = isEnforced;
		}
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
		/**
		 * @return the rankId
		 */
		public Integer getId() {
			return id;
		}
		/**
		 * @return the isEnforced
		 */
		public boolean isEnforced() {
			return isEnforced;
		}
		
		public String getMatchSql(String name) {
			return "select geographyid from geography where RankID=" + getId() 
					+ " and Name='" + name.replace("'", "''") + "'";
		}
	}
}
