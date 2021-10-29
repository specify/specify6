/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class KUPlantCEFixer extends KUUPlant {
	final String baseDate;
	final String baseDB; //db containing embedded CES
	final String targetDB; //current shared CE db to be fixed
	
	Set<Integer> processedTargetCes = new HashSet<Integer>();
	
	
	/**
	 * @param con
	 */
	public KUPlantCEFixer(Connection con, String baseDate, String baseDB, String targetDB) {
		super(con);
		this.baseDate = baseDate;
		this.baseDB = baseDB;
		this.targetDB = targetDB;
	}

	/**
	 * @param db
	 * @param coId
	 * @return
	 */
	protected String getCollectorSql(String db, Integer coId) {
		return "SELECT c.OrderNumber, c.AgentID, c.Remarks, c.IsPrimary FROM " + db + ".collector c "
				+ "INNER JOIN " + db + ".collectingevent ce ON ce.CollectingEventID = c.CollectingEventID "
				+ "INNER JOIN " + db + ".collectionObject co ON co.CollectingEventID = ce.CollectingEventID "
				+ "WHERE co.CollectionObjectID=" + coId;
		
	}
	
	/**
	 * @param db
	 * @param coId
	 * @return
	 * @throws Exception
	 */
	protected List<CollectorRec> getCollectorRecList(String db, Integer coId)  throws Exception {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(getCollectorSql(db, coId));
		try {
			List<CollectorRec> result = new ArrayList<CollectorRec>();
			while (rs.next()) {
				result.add(new CollectorRec(rs));
			}
			return result;
		} finally {
			rs.close();
			stmt.close();
		}
	}
	/**
	 * @param baseCoId
	 * @param targetCoId
	 * @return
	 * @throws Exception
	 */
	protected Pair<List<CollectorRec>,List<CollectorRec>> getCollectors(Integer baseCoId, Integer targetCoId) throws Exception {
		return new Pair<List<CollectorRec>, List<CollectorRec>>(getCollectorRecList(baseDB, baseCoId), 
				getCollectorRecList(targetDB, targetCoId));
	}
	
	/**
	 * @param colls
	 * @return
	 */
	protected Boolean collectorsMatch(Pair<List<CollectorRec>,List<CollectorRec>> colls) {
		List<CollectorRec> colls1 = colls.getFirst();
		List<CollectorRec> colls2 = colls.getSecond();
		Boolean result = colls1.size() == colls2.size();
		if (result) {
			for (int i = 0; i < colls1.size(); i++) {
				if (!colls1.get(i).equals(colls2.get(i))) {
					result = false;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * @param db
	 * @param ceId
	 * @return
	 * @throws Exception
	 */
	protected Integer getCEId(String db, Integer coId) throws Exception {
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT CollectingEventID FROM " + db + ".collectionobject WHERE CollectionObjectID=" + coId);
		try {
			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return null;
			}
		} finally {
			rs.close();
			stmt.close();
		}
	}
	
	/**
	 * @param tbl
	 * @param id
	 * @return
	 * @throws Exception
	 */
	protected String modifiedWhen(String db, String tbl, Integer id) throws Exception {
		String sql = "SELECT concat(year(TimestampModified),'-',month(TimestampModified),'-',day(TimestampModified)), "
				+ "concat(year(TimestampCreated),'-',month(TimestampCreated),'-',day(TimestampCreated)) FROM "
				+ db + "." + tbl + " WHERE " + tbl + "ID=" + id;
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(sql); 
		String result = null;
		try {
			if (rs.next()) {
				String mod = rs.getString(1);
				String cre = rs.getString(2);
				if (mod == null) {
					result = cre;
				} else if (cre == null) {
					result = mod;
				} else {
					result = mod.compareTo(cre) < 0 ? cre : mod;
				}
			}
		} finally {
			rs.close();
			stmt.close();
		}
		return result == null ? "" : result;
	}
	
	/**
	 * @param ceId
	 * @return
	 * @throws Exception
	 */
	protected String ceTargetModifiedWhen(Integer ceId) throws Exception {
		String result = modifiedWhen(targetDB, "collectingevent", ceId);
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT CollectorID FROM collector WHERE CollectingEventID=" + ceId);
		try {
			while (rs.next()) {
				String colDate = modifiedWhen(targetDB, "collector", rs.getInt(1));
				if (colDate.compareTo(result) > 0) {
					result = colDate;
				}
			}
		} finally {
			rs.close();
			stmt.close();
		}
		return result;
	}
	
	/**
	 * @param ceId
	 * @param when
	 * @return
	 * @throws Exception
	 */
	protected boolean ceTargetModifiedSince(Integer ceId, String when) throws Exception {
		return when.compareTo(ceTargetModifiedWhen(ceId)) < 0;
	}
	
	/**
	 * @param baseCoId
	 * @param targetCoId
	 * @param colls
	 * @throws Exception
	 */
	protected void fixCollectorsForCo(Integer baseCoId, Integer targetCoId, 
			Pair<List<CollectorRec>, List<CollectorRec>> colls) throws Exception {
		Integer baseCEId = getCEId(baseDB, baseCoId);
		Integer targetCEId = getCEId(targetDB, targetCoId);
		if (!processedTargetCes.contains(targetCEId)) {
			processedTargetCes.add(targetCEId);
			if (!ceTargetModifiedSince(targetCEId, baseDate)) {
				//logThisSql("FIX " + targetCEId);
				logThisSql("INSERT INTO " + baseDB + ".fixcheck values( " + baseCEId + "," + targetCEId + ");");
			} else {
				logThis("Skipping ce that needs fixing because it has been modified: " + baseCoId);
				logThisSql("INSERT INTO " + baseDB + ".fixcheck values( " + baseCEId + "," + targetCEId + ");");
			}
		}
	}
	
	/**
	 * 
	 * Compares collectors for each specimen in baseDB with targetDB
	 * and updates targetDB where differences exist, adding collectors and/or CEs where necessary.
	 * 
	 * @throws Exception
	 */
	protected void correctCollectors() throws Exception {
		String sql = "SELECT b.CatalogNumber, b.CollectionObjectID, t.CollectionObjectID FROM "
				+ baseDB + ".collectionobject b INNER JOIN " + targetDB + ".collectionobject t "
				+ "ON t.CatalogNumber = b.CatalogNumber";
		Statement stmt = con.createStatement();
		ResultSet cos = stmt.executeQuery(sql); 
		try {
			while (cos.next()) {
				Integer baseCoId = cos.getInt(2); Integer targetCoId = cos.getInt(3);
				Pair<List<CollectorRec>, List<CollectorRec>> colls = getCollectors(baseCoId, targetCoId);
				if (!collectorsMatch(colls)) {
					fixCollectorsForCo(baseCoId, targetCoId, colls);
				}
			}
		} finally {
			cos.close();
			stmt.close();
		}
	}
	
	/**
	 * @throws Exception
	 */
	protected void runCESharer() throws Exception {
		
	}
	/**
	 * 
	 * Compares collectors for each specimen in baseDB with targetDB
	 * and updates targetDB where differences exist, adding collectors and/or CEs where necessary.
	 * 
	 * Then runs the CE sharer on the targetDB
	 * 
	 * @throws Exception
	 */
	protected void fixCollectingEvents() throws Exception {
		this.buildStatement();
		processedTargetCes.clear();
		correctCollectors();
		runCESharer();
		this.stmt.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/plant_shared_pre?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			KUPlantCEFixer f = new KUPlantCEFixer(con, "2014-2-25", "plant_shared_pre", "kuplant_latest");
			f.setEchoLog(true);
			f.setEchoSqlLog(true);
			f.fixCollectingEvents();
			f.writeLog("D:/data/KU/VascularPlant/cefixlog.txt");
			f.writeSqlLog("D:/data/KU/VascularPlant/cefix.sql");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	

	private class CollectorRec  {
		final Integer orderNumber;
		final Integer agentId;
		final String remarks;
		final Boolean isPrimary;
		
		
		/**
		 * @param orderNumber
		 * @param agentId
		 * @param remarks
		 * @param isPrimary
		 */
		public CollectorRec(Integer orderNumber, Integer agentId, String remarks,
				Boolean isPrimary) {
			super();
			this.orderNumber = orderNumber;
			this.agentId = agentId;
			this.remarks = remarks == null ? "" : remarks;
			this.isPrimary = isPrimary;
		}


		/**
		 * @param rsRec
		 * @throws SQLException
		 */
		public CollectorRec(ResultSet rsRec) throws SQLException {
			this(rsRec.getInt(1), rsRec.getInt(2), rsRec.getString(3), rsRec.getBoolean(4));
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			boolean result = obj instanceof CollectorRec;
			if (result) {
				CollectorRec cr = CollectorRec.class.cast(obj);
				result =  orderNumber.equals(cr.getOrderNumber()) 
						&& agentId.equals(cr.getAgentId())
						&& remarks.equals(cr.getRemarks())
						&& isPrimary.equals(cr.getIsPrimary());
			}
			return result;
		}


		/**
		 * @return the orderNumber
		 */
		public int getOrderNumber() {
			return orderNumber;
		}


		/**
		 * @return the agentId
		 */
		public int getAgentId() {
			return agentId;
		}


		/**
		 * @return the remarks
		 */
		public String getRemarks() {
			return remarks;
		}


		/**
		 * @return the isPrimary
		 */
		public Boolean getIsPrimary() {
			return isPrimary;
		}
		
		
	}
}
