/**
 * 
 */
package edu.ku.brc.dbsupport;

import java.util.ArrayList;
import java.util.List;

import edu.ku.brc.util.Pair;

/**
 * @author tnoble
 *
 */
public class PermissionInfo 
{
	String db;
	String host;
	boolean isOptional;
	int perm;
	
	
	/**
	 * @param db
	 * @param host
	 * @param perm
	 */
	public PermissionInfo(String db, String host, int perm) {
		this(db, host, perm, false);
	}
	/**
	 * @param db
	 * @param host
	 * @param perm
	 * @param isOptional
	 */
	public PermissionInfo(String db, String host, int perm, boolean isOptional) {
		super();
		this.db = db;
		this.host = host;
		this.perm = perm;
		this.isOptional = isOptional;
	}
	/**
	 * @return the db
	 */
	public String getDb() {
		return db;
	}
	/**
	 * @param db the db to set
	 */
	public void setDb(String db) {
		this.db = db;
	}
	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}
	/**
	 * @return the perm
	 */
	public int getPerm() {
		return perm;
	}
	/**
	 * @param perm the perm to set
	 */
	public void setPerm(int perm) {
		this.perm = perm;
	}
	/**
	 * @return the isOptional
	 */
	public boolean isOptional() {
		return isOptional;
	}
	/**
	 * @param isOptional the isOptional to set
	 */
	public void setOptional(boolean isOptional) {
		this.isOptional = isOptional;
	}
	
    /**
     * @param perms
     * @param targetPerms
     * @param db
     * @return
     */
    public static Pair<List<PermissionInfo>, List<PermissionInfo>> getMissingPerms(List<PermissionInfo> perms, List<PermissionInfo> targetPerms, String db)
    {
    	List<PermissionInfo> missingPerms = new ArrayList<PermissionInfo>();
    	List<PermissionInfo> missingOptionalPerms = new ArrayList<PermissionInfo>();    	
    	for (PermissionInfo p : targetPerms) {
    		if (!hasPermission(perms, p, db)) {
    			String dbStr = "?".equals(p.getDb()) ? db : "*"; 
    			(p.isOptional() ? missingOptionalPerms : missingPerms).add(new PermissionInfo(dbStr, "", p.getPerm()));
    		}
    	}
    	return new Pair<List<PermissionInfo>, List<PermissionInfo>>(missingPerms, missingOptionalPerms);
    }

    /**
     * @param perms
     * @param pToCheck
     * @param db
     * @return
     */
    private static boolean hasPermission(List<PermissionInfo> perms, PermissionInfo pToCheck, String db) {
    	for (PermissionInfo p : perms) {
    		if (p.getPerm() == pToCheck.getPerm()) {
    			if ("*".equals(p.getDb())) {
    				return true;
    			}
    			if (!"*".equals(pToCheck.getDb()) && p.getDb().equals(db)) {
    				return true;
    			}
    		}
    	}
    	return false;
    }

    /**
     * @param mgr
     * @param missingPerms
     * @param db
     * @return
     */
    private static List<String> getMissingPermissionTexts(DBMSUserMgr mgr, List<PermissionInfo> missingPerms, String db) {
    	List<String> result = new ArrayList<String>();
    	for (PermissionInfo p : missingPerms) {
    		String dbString = "?".equals(p.getDb()) ? db : p.getDb(); 
     		result.add(mgr.getPermText(p.getPerm()) + " ON " + dbString);
    	}
    	return result;    	
    }

    /**
     * @param mgr
     * @param missingPerms
     * @param db
     * @return
     */
    public static String getMissingPermissionString(DBMSUserMgr mgr, List<PermissionInfo> missingPerms, String db) {
    	return getMissingPermissionTexts(mgr, missingPerms, db).toString();
    }

}
