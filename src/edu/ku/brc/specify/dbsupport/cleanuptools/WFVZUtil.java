/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;


import java.sql.Connection;
import java.util.List;

import edu.ku.brc.specify.conversion.BasicSQLUtils;

/**
 * @author timo
 *
 */
public class WFVZUtil extends UtilitaryBase {

	public WFVZUtil(Connection con) {
		super(con);
	}
	
	/**
	 * @throws Exception
	 * 
	 * Fix for failure to include Modifier field in locality table.
	 * 
	 * 1. Add Modifier to loc table.
	 * 2. update tblEGGS-idloc-loc set loc.Modifier = tblEGGS.modifier
	 * 3. run stickModifiersIntoLocs()
	 */
	private void stickModifiersIntoLocs() throws Exception {
		String sql = "select distinct t.idloc from tblEGGS t inner join loc l on l.idloc = t.idloc where ifnull(l.modifier,'') != ifnull(t.modifier,'')";
		List<Object[]> toStick = BasicSQLUtils.query(con, sql);
		int sticks = toStick.size();
		int stuck = 0;
		for (Object[] s : toStick) {
			stickModifiersIntoLoc((Integer)s[0]);
			System.out.println("stuck " + (++stuck) + " of " + sticks);
		}
	}
	
	private void stickModifiersIntoLoc(Integer locId) throws Exception {
		String sql = "select distinct t.modifier from wfegg.tblEGGS t inner join loc l on l.idloc = t.idloc where ifnull(l.modifier,'') != ifnull(t.modifier,'') and t.idloc=" + locId;
		List<Object[]> mods = BasicSQLUtils.query(con, sql); 
		for (Object[] mod : mods) {
			stickModifierIntoLoc(locId, (String)mod[0]);
		}
	}
	
	private void stickModifierIntoLoc(Integer locId, String mod) throws Exception {
		String sqlMod = mod == null ? "NULL" : "'" + mod.replaceAll("'", "''") + "'";
		String sql = "insert into loc(COUNTRY,STATE,COUNTY,LOCALITY,ELEVATION,MinimumElevationInMeters,MaximumElevationInMeters,VerbatimElevation,GeodeticDatum,CoordinateUncertaintyInMeters"   
				+ ",VerbatimCoordinateSystem,GeoreferenceProtocol,GeoreferenceRemarks,GeoreferenceSources,GeoreferencedDate,GeoreferencedBy,GeoreferenceVerificationStatus,NoGeorefBecause"                 
				+ ",LATITUDE,LONGITUDE,VerbatimLatitude,VerbatimLongitude,newdeclat,newdeclng,DecimalLatitude,DecimalLongitude,MODIFIER)"                
				+ " select COUNTRY,STATE,COUNTY,LOCALITY,ELEVATION,MinimumElevationInMeters,MaximumElevationInMeters,VerbatimElevation,GeodeticDatum,CoordinateUncertaintyInMeters"   
				+ ",VerbatimCoordinateSystem,GeoreferenceProtocol,GeoreferenceRemarks,GeoreferenceSources,GeoreferencedDate,GeoreferencedBy,GeoreferenceVerificationStatus,NoGeorefBecause"                 
				+ ",LATITUDE,LONGITUDE,VerbatimLatitude,VerbatimLongitude,newdeclat,newdeclng,DecimalLatitude,DecimalLongitude," + sqlMod + " from loc where idloc=" + locId;
		if (1 != BasicSQLUtils.update(con, sql)) {
			throw new Exception("FAIL: " + locId + ", " + mod);
		}
		int newLocId = BasicSQLUtils.getCountAsInt(con, "select max(idloc) from loc");
		sqlMod = mod == null ? "''" : "'" + mod.replaceAll("'", "''") + "'";
		sql = "update tblEGGS set idloc=" + newLocId + " where ifnull(modifier,'')=" + sqlMod + " and idloc=" + locId;
		if (0 >= BasicSQLUtils.update(con, sql)) {
			throw new Exception("FAIL: " + sql);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/wfegg?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			WFVZUtil u = new WFVZUtil(con);
			u.stickModifiersIntoLocs();
		} catch (Exception e){
			e.printStackTrace();
		}

	}
}
