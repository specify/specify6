/**
 * 
 */
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;

/**
 * @author timo
 *
 */
public class UTax extends UtilitaryBase {
	protected Integer treeDefId;
	protected Integer defaultParentId;
	protected Integer creatorId;

	/**
	 * @param con
	 * @param treeDefId
	 */
	public UTax(Connection con, Integer treeDefId, Integer defaultParentId, Integer creatorId) {
		super(con);
		this.treeDefId = treeDefId;
		this.defaultParentId = defaultParentId;
		this.creatorId = creatorId;
	}

	/**
	 * @param fullName - assumed to be a binomial for now.
	 * @return 
	 */
	public Integer getTaxonID(String fullName) throws Exception {
		Integer result = matchFullName(fullName);
		if (result == null) {
			String[] names = fullName.split(" ");
			if (names.length == 2) {
				Integer genusId = getGenusID(fullName);
				if (genusId == null) {
					genusId = insert(names[0], 180, defaultParentId);
				}
				return insert(names[1], 220, genusId);
			}
		}
		return result;
	}
	
	/**
	 * @param name
	 * @param rankId
	 * @param parentId
	 * @return
	 * @throws Exception
	 */
	protected Integer insert(String name, Integer rankId, Integer parentId)  throws Exception {
		String sql = "INSERT INTO taxon (TimestampCreated, Version, CreatedByAgentID, IsAccepted, IsHybrid, TaxonTreeDefID, "
				+ "TaxonTreeDefItemID, RankID, ParentID, Name) values(now(), 0," + creatorId + ",true, false, "
				+ treeDefId + ", (select TaxonTreeDefItemID from taxontreedefitem di where di.taxontreedefid=" + treeDefId + " AND di.RankID=" + rankId
				+ "), " + rankId + ", " + parentId + ", '" + name.replace("'", "''") + "')";
		Statement insStmt = con.createStatement();
		try {
			insStmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet key = insStmt.getGeneratedKeys();
			try {
				if (!key.next()) {
					key.close();
					throw new Exception("Insert failed: " + sql);
				}
				Integer result = key.getInt(1);
				return result;
			} finally {
				key.close();
			}
		} finally {
			insStmt.close();
		}
	}
	
	/**
	 * @param name
	 * @param fld
	 * @param rankId
	 * @return
	 */
	protected Integer match(String name, String fld, Integer rankId) {
		String sql = "SELECT TaxonID FROM taxon WHERE " + fld + "='" + name.replace("'", "''")
				+ "' AND TaxonTreeDefID=" + treeDefId;
		if (rankId != null) {
			sql += " AND RankID=" + rankId;
		}
		return BasicSQLUtils.querySingleObj(con, sql);
	}
	
	/**
	 * @param fullName
	 * @return
	 */
	public Integer getGenusID(String fullName) {
		String[] names = fullName.split(" ");
		return match(names[0], "Name", 180);
	}

	
	/**
	 * @param fullName
	 * @return
	 */
	protected Integer matchFullName(String fullName) {
		return match(fullName, "FullName", null);
	}

	/**
	 * @return the treeDefId
	 */
	public Integer getTreeDefId() {
		return treeDefId;
	}

	/**
	 * @param treeDefId the treeDefId to set
	 */
	public void setTreeDefId(Integer treeDefId) {
		this.treeDefId = treeDefId;
	}

	/**
	 * @return the defaultParentId
	 */
	public Integer getDefaultParentId() {
		return defaultParentId;
	}

	/**
	 * @param defaultParentId the defaultParentId to set
	 */
	public void setDefaultParentId(Integer defaultParentId) {
		this.defaultParentId = defaultParentId;
	}

	/**
	 * @return the creatorId
	 */
	public Integer getCreatorId() {
		return creatorId;
	}

	/**
	 * @param creatorId the creatorId to set
	 */
	public void setCreatorId(Integer creatorId) {
		this.creatorId = creatorId;
	}

	public static void main(String[] args) {
		try {
			String connStr = "jdbc:mysql://localhost/kubird6?characterEncoding=UTF-8&autoReconnect=true";
			Connection con = getConnection(connStr, "Master", "Master");
			connStr = "jdbc:mysql://localhost/kubirdt?characterEncoding=UTF-8&autoReconnect=true";
			Connection tcon = getConnection(connStr, "Master", "Master");
			Integer unplacedFamilyID = 39537;			
			UTax utx = new UTax(con, 1, unplacedFamilyID, 1);
			Statement stmt = tcon.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT distinct taxon from unvoucheredtissue WHERE taxon is not null");
			try {
				while (rs.next()) {
					System.out.println(rs.getString(1) + " - " + utx.getTaxonID(rs.getString(1)));
				}
			} finally {
				rs.close();
				stmt.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
}
