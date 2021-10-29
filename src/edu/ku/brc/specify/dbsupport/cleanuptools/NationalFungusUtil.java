/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;

/**
 * @author timo
 *
 */
public class NationalFungusUtil extends UtilitaryBase {

	/**
	 * @param con
	 */
	public NationalFungusUtil(Connection con) {
		super(con);
	}


	private void addFungi() throws Exception {
		int maxCeId = 222543;
		int inc = 100;
		int ceId = 23;
		buildStatement();
		while (ceId <= maxCeId) {
			String sql = "insert into collectionobject(Version, TimestampModified, TimestampCreated, CreatedByAgentID, Text1, " +
					"CollectionID, CollectionMemberID, Text2, CollectingEventID) " +
					"select distinct 0, timestamp('2014-9-24'), timestamp('2014-9-24'), 1, 'Lit', 4, 4, " +
					"f.taxonid, collectingeventid from collectingevent ce inner join collectingeventattribute cea  " +
					"on cea.collectingeventattributeid = ce.collectingeventattributeid  " +
					"inner join taxon h on h.taxonid = cea.HostTaxonID " +
					"inner join localitycitation lc on lc.localityid = ce.localityid " +
					"inner join referencework rw on rw.referenceworkid = lc.referenceworkid " + 
					"inner join fungus.tblfungushostlocrefr fhlr on fhlr.ReferenceForKey = rw.GUID " +
					"inner join fungus.tblfungushostloc fhl on fhl.LocalityCounter = fhlr.HostFungLocForKey " +
					"inner join fungus.tblfungushostname fhn on fhn.FungusHostCounter = fhl.FungusHostForKey and fhn.HostNameForKey = h.GUID " +
					"inner join taxon f on f.GUID = fhn.FungusNameForKey " +
					"where f.GUID is not null and f.nodenumber between 2 and 340684 and collectingeventid between " + ceId + " and ";
			ceId += inc;
			sql += ceId;
			int r = stmt.executeUpdate(sql);
			System.out.println(r + " records added for " + String.valueOf(ceId - 100) + " - " + ceId);
			ceId += 1;
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/fungus_proto?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			NationalFungusUtil fungu = new NationalFungusUtil(con);
			//ubirdy.connectLocsAndCes();
			//ubirdy.dealWithCollectedDates_1();
			//ubirdy.assignCatNumsAndAddDeterminationsForUnvoucheredSpecs();
			fungu.addFungi();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
