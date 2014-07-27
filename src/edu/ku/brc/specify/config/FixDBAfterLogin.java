/* Copyright (C) 2013, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package edu.ku.brc.specify.config;

import static edu.ku.brc.specify.config.init.DataBuilder.createPickList;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCount;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;

import java.awt.Frame;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JEditorPane;
import javax.swing.JTextArea;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.af.ui.forms.persist.AltViewIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellFieldIFace;
import edu.ku.brc.af.ui.forms.persist.FormCellIFace;
import edu.ku.brc.af.ui.forms.persist.FormRowIFace;
import edu.ku.brc.af.ui.forms.persist.FormViewDef;
import edu.ku.brc.af.ui.forms.persist.FormViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewDefIFace;
import edu.ku.brc.af.ui.forms.persist.ViewIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.config.init.BldrPickList;
import edu.ku.brc.specify.config.init.BldrPickListItem;
import edu.ku.brc.specify.config.init.DataBuilder;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAttachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.specify.datamodel.DNASequenceAttachment;
import edu.ku.brc.specify.datamodel.DNASequencingRun;
import edu.ku.brc.specify.datamodel.DNASequencingRunAttachment;
import edu.ku.brc.specify.datamodel.Discipline;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSetAttachment;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.GiftAttachment;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.ReferenceWorkAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.SpecifyUser;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.specify.tasks.QueryTask;
import edu.ku.brc.ui.CustomDialog;
import edu.ku.brc.ui.UIHelper;
import edu.ku.brc.ui.UIRegistry;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jan 14, 2010
 *
 */
public class FixDBAfterLogin
{
    private static final Logger log  = Logger.getLogger(FixDBAfterLogin.class);
    
    private final static String FIX_DEFDATES_PREF = "FIX_DEFDATES_PREF";
    
    public final static String DEGREES_SYMBOL = "\u00b0";
    public final static String SEPS           = DEGREES_SYMBOL + ":'\" ";
    
    /**
     * 
     */
    public FixDBAfterLogin()
    {
        super();
    }
    
    /**
     * 
     */
    public void checkMultipleLocalities()
    {
        int cnt = getCountAsInt("select count(localitydetailid) - count(distinct localityid) from localitydetail");
        if (cnt > 0)
		{
			cnt = getCountAsInt("select count(collectionobjectid) from collectionobject co " +
					"inner join collectingevent ce on ce.collectingeventid = co.collectingeventid " +
					"inner join  (select localityid from localitydetail group by localityid having count(localitydetailid) > 1) badlocs on badlocs.localityid = ce.localityid");
			String str = String.format("Multiple Locality Detail Records - Count: %d", cnt);
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().sendMsg(FixDBAfterLogin.class, str, new Exception(str));
		}
         
         cnt = getCountAsInt("select count(geocoorddetailid) - count(distinct localityid) from geocoorddetail");
         if (cnt > 0)
         {
        	 cnt = getCountAsInt("select count(collectionobjectid) from collectionobject co " +
        	 		"inner join collectingevent ce on ce.collectingeventid = co.collectingeventid " +
        	 		"inner join (select localityid from geocoorddetail group by localityid having count(geocoorddetailid) > 1) badlocs on badlocs.localityid = ce.localityid");
 		     String str = String.format("Multiple GeoCoord Detail Records - Count: %d", cnt);
 		     edu.ku.brc.exceptions.ExceptionTracker.getInstance().sendMsg(FixDBAfterLogin.class, str, new Exception(str));
         }
    }
    
    /**
     * fixes bad version and timestamps for recordsets created by Uploader. 
     */
    public void fixUploaderRecordsets()
    {
    	update( "update recordset set TimestampCreated = now(), Version = 0 where Type = 1 and Version is null");
    }
    
    public static void fixUnMatchedWBSpecifyUserIDs()
    {
    	update("update workbenchtemplate t inner join workbench w on w.workbenchtemplateid = t.workbenchtemplateid set t.SpecifyUserID = w.SpecifyUserID");
    }
    
    /**
     * Fixes SpQueryField.operStart storage for all queries
     */
    public static boolean fixQueryOperators()
    {
    	return QueryTask.fixOperatorStorageForAllQueries();
    }
    
    /**
     * Sets SpQueryField.IsDisplay for unmapped schema items so they will be compatible with 6.12+ schemamappings.
     */
    public static void fixIsDisplayForUnmappedSchemaConditions()
    {
        String sql = "select distinct SpQueryID from spexportschemamapping m inner join "
        		+ "spexportschemaitemmapping mi on mi.SpExportSchemaMappingID = m.SpExportSchemaMappingID"
        		+ " inner join spqueryfield f on f.SpQueryFieldID = mi.SpQueryFieldID";
    	List<Object> schemaMappingQuerys = BasicSQLUtils.querySingleCol(sql);
        sql = "update spqueryfield qf left join spexportschemaitemmapping sim on sim.SpQueryFieldID = qf.SpQueryFieldID"
        		+ " set IsDisplay = false where sim.spexportschemaitemmappingid is null and qf.SpQueryID = ";
        for (Object q : schemaMappingQuerys)
        {
        	BasicSQLUtils.update(sql + q);
        }
    }
    
    /**
     * Set IsAccepted and IsHybrid to true or false
     */
    public static boolean fixNullTreeableFields() 
    {
    	String[] treeables = {"taxon", "taxon.IsHybrid", "geography", "geologictimeperiod", "lithostrat", "storage"};
    	boolean allDone = true; 
    	for (int idx = 0; idx < treeables.length; idx++)
    	{
    		String tree = treeables[idx];
    		if (!AppPreferences.getGlobalPrefs().getBoolean("FixNull" + tree + "Fields", false))
    		{
				String fldName = "taxon.IsHybrid".equals(tree) ? "IsHybrid" : "IsAccepted";
				String setToValue =  "taxon.IsHybrid".equals(tree) ? "HybridParent1ID IS NOT NULL AND HybridParent2ID IS NOT NULL" 
						: "AcceptedID IS NULL";
				String treeTblName = "taxon.IsHybrid".equals(tree) ? "taxon" : tree;
    			int cnt = getCountAsInt("SELECT COUNT(*) FROM " + treeTblName + " WHERE " + fldName + " IS NULL");
    			boolean updated = true;
    			if (cnt > 0) 
    			{
    				String updateSql = "UPDATE " + treeTblName + " SET " + fldName + " = " + setToValue + " WHERE " + fldName + " IS NULL";
    				updated = BasicSQLUtils.update(updateSql) == cnt;
    			}
    			allDone &= updated;
    		}
    	}
    	return allDone;
    }

    /**
     * @param pStmt
     * @param locId
     * @param origUnit
     * @param srcUnit
     * @param lat1Text
     * @param long1Text
     * @param isNew
     * @return
     * @throws SQLException
     */
    private boolean fixLatLong(final PreparedStatement pStmt, 
                               final int locId, 
                               final int origUnit, 
                               final int srcUnit, 
                               final String lat1Text, 
                               final String long1Text,
                               final boolean isNew) throws SQLException
    {
        pStmt.setInt(3, locId);
        
        boolean doUpdate = false;
        if (isNew)
        {
            // These could be entered from Sp5 or Sp6
            int numColonsLat = lat1Text  != null ? StringUtils.countMatches(lat1Text, ":") : 0;
            int numColonsLon = long1Text != null ? StringUtils.countMatches(long1Text, ":") : 0;
            
            if (numColonsLat == 2 || numColonsLon == 2) // definitely Deg, Min Dec Secs
            {
                if (srcUnit != 1)
                {
                    //log.debug(String.format("1 - Lat[%s]  Lon[%s]  origUnit %d  srcUnit %d", lat1Text, long1Text, origUnit, srcUnit));
                    pStmt.setInt(1, origUnit);  // How Viewed
                    pStmt.setInt(2, 1);         // How Stored
                    return true;
                }
                return false;
            }
            
            String[] latTokens    = StringUtils.split(lat1Text, SEPS);
            String[] lonTokens    = StringUtils.split(long1Text, SEPS);
            int      latLen       = latTokens.length;
            int      lonLen       = lonTokens.length;
            
            if (latLen == 4 || lonLen == 4)
            {
                if (srcUnit != 1)  // Deg, Min Dec Secs
                {
                    //log.debug(String.format("Fix1 Lat[%s]  Lon[%s]  origUnit %d  srcUnit %d", lat1Text, long1Text, origUnit, srcUnit));
                    pStmt.setInt(1, origUnit);  // How Viewed
                    pStmt.setInt(2, 1);         // How Stored
                    return true;
                }
                
            } else if (latLen == 3 || lonLen == 3)
            {
                if (srcUnit != 2) // Degrees Decimal Minutes
                {
                    //log.debug(String.format("Fix2 Lat[%s]  Lon[%s]  origUnit %d  srcUnit %d", lat1Text, long1Text, origUnit, srcUnit));
                    pStmt.setInt(1, origUnit);  // How Viewed
                    pStmt.setInt(2, 2);         // How Stored
                    return true;
                }

            } else if (latLen == 2 || lonLen == 2)
            {
                if (srcUnit != 0) // Decimal Degrees 
                {
                    //log.debug(String.format("Fix0 Lat[%s]  Lon[%s]  origUnit %d  srcUnit %d", lat1Text, long1Text, origUnit, srcUnit));
                    pStmt.setInt(1, origUnit);  // How Viewed
                    pStmt.setInt(2, 1);         // How Stored
                    return true;
                }
            } else
            {
                log.debug(String.format("*** Couldn't parse Lat[%s]  Lon[%s]  origUnit %d  srcUnit %d", lat1Text, long1Text, origUnit, srcUnit));
            }
            
            
        } else
        {
            if (srcUnit == 0 && origUnit != 0)
            {
                pStmt.setInt(1, origUnit); // How Viewed
                pStmt.setInt(2, origUnit); // How Stored
                doUpdate = true;
            }
        }
        return doUpdate;
    }
    
    /**
     * 
     */
    public void fixLatLongUnit()
    {
        int     fixed = 0;
        //String  updateSQL = "UPDATE locality SET OriginalLatLongUnit=?, SrcLatLongUnit=?  WHERE LocalityID=?";
        String  postStr   = " WHERE OriginalLatLongUnit > 0 AND (Lat1Text IS NOT NULL OR Long1Text IS NOT NULL)";
        
        Connection        conn  = DBConnection.getInstance().getConnection();
        PreparedStatement pStmt = null;
        Statement         stmt  = null;
        try
        {
            int totalCnt = getCount(conn, "SELECT COUNT(*) FROM locality" + postStr);
            //log.debug("Total Count: "+totalCnt);
            if (totalCnt > 0)
            {
                pStmt = conn.prepareStatement(updateSQL);
                stmt  = conn.createStatement();
                
                int cnt = 0;
                ResultSet rs = stmt.executeQuery("SELECT LocalityID, OriginalLatLongUnit, SrcLatLongUnit, Lat1Text, Long1Text, (TimestampCreated > '2008-06-01 00:00:00') AS IsNew FROM locality" + postStr);
                while (rs.next())
                {
                    int     id        = rs.getInt(1);
                    int     origUnit  = rs.getInt(2);
                    int     srcUnit   = rs.getInt(3);
                    String  lat1Text  = rs.getString(4);
                    String  long1Text = rs.getString(5);
                    boolean isNew     = rs.getInt(6) == 1;
                    
                    if (fixLatLong(pStmt, id, origUnit, srcUnit, lat1Text, long1Text, isNew))
                    {
                        
                        if (pStmt.executeUpdate() != 1)
                        {
                            log.error("Error updating "+id);
                        } else
                        {
                            fixed++;
                        }
                    }
                    cnt++;
                    if (cnt % 100 == 0)
                    {
                        log.debug(String.format("Processing %d/%d", cnt, totalCnt));
                    }
                }
                rs.close();
                log.debug(String.format("Fixed %d/%d", fixed, totalCnt));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {}
        }
    }
    
    /**
     * Creates empty CollectingEvents for CollectingEvent-less CollectionObjects in Collections 
     * with embedded CollectingEvents 
     */
    public static void fixNullEmbeddedCollectingEvents()
    {
    	try
    	{
    		String checkSQL = "select count(co.CollectionObjectID) from collectionobject co inner join "
    			+ "collection cln on cln.UserGroupScopeID = co.CollectionMemberID "
    			+ "where co.CollectingEventID is null and cln.IsEmbeddedCollectingEvent";
    		int cesToAdd = BasicSQLUtils.getCount(checkSQL);
    		if (cesToAdd > 0)
    		{
    			String ceInsertSql = "insert into collectingevent (TimestampCreated, TimestampModified, Version, " +
    				"Visibility, CreatedByAgentID, VisibilitySetByID, ModifiedByAgentID, DisciplineID)" +
    				" select co.TimestampCreated, co.TimestampModified, co.CollectionObjectID, co.Visibility, " +
    				"co.CreatedByAgentID, co.VisibilitySetByID, co.ModifiedByAgentID, " +
    				"(select DisciplineID from collection cln2 where cln2.UserGroupScopeID = co.CollectionMemberID) " +
    				"from collectionobject co inner join collection cln on cln.UserGroupScopeID = " +
    				"co.CollectionMemberID where co.CollectingEventID is null and cln.IsEmbeddedCollectingEvent";
    			int insertedCnt = BasicSQLUtils.update(ceInsertSql);
    			if (insertedCnt != cesToAdd)
    			{
    				throw new Exception("fixNullEmbeddedCollectingEvents: error inserting colllectingevent records");
    			}
    			String ceIDUpdateSql = "update collectionobject co inner join collectingevent ce on ce.Version = "
    				+ "co.CollectionObjectID inner join collection cln on cln.UserGroupScopeID = co.CollectionMemberID "
    				+ "set co.CollectingEventID = ce.CollectingEventID, ce.Version = 0 "
    				+ "where co.collectingeventid is null and cln.IsEmbeddedCollectingEvent";
    			int updatedCnt = BasicSQLUtils.update(ceIDUpdateSql);
    			if (updatedCnt != 2*insertedCnt)
    			{
    				throw new Exception("fixNullEmbeddedCollectingEvents: error updating colllectingevent IDs");
    			}
    			log.info("added " + insertedCnt + " collectingevent records");
    	}
		} catch (Exception ex)
		{
            log.error(ex);
			ex.printStackTrace();
		}
    }
    
    /**
     * 
     */
    public static void fixUserPermissions(final boolean doSilently)
    {
        final String FIXED_USER_PERMS = "FIXED_USER_PERMS";
        boolean isAlreadyFixed = AppPreferences.getRemote().getBoolean(FIXED_USER_PERMS, false);
        if (isAlreadyFixed)
        {
            return;
        }
        
        String whereStr  = " WHERE p.GroupSubClass = 'edu.ku.brc.af.auth.specify.principal.UserPrincipal' ";
        String whereStr2 = "AND p.userGroupScopeID IS NULL";
        
        String postSQL = " FROM specifyuser su " +
                         "INNER JOIN specifyuser_spprincipal ss ON su.SpecifyUserID = ss.SpecifyUserID " +
                         "INNER JOIN spprincipal p ON ss.SpPrincipalID = p.SpPrincipalID " +
                         "LEFT JOIN spprincipal_sppermission pp ON p.SpPrincipalID = pp.SpPrincipalID " +
                         "LEFT OUTER JOIN sppermission pm ON pp.SpPermissionID = pm.SpPermissionID " +
                         whereStr;
        
        String sql = "SELECT COUNT(*)" + postSQL + whereStr2;
        log.debug(sql);
        if (BasicSQLUtils.getCountAsInt(sql) < 1)
        {
            sql = "SELECT COUNT(*)" + postSQL;
            log.debug(sql);
            if (BasicSQLUtils.getCountAsInt(sql) > 0)
            {
                return;
            }
        }
        
        final String updatePermSQL = "DELETE FROM %s WHERE SpPermissionID = %d";
        final String updatePrinSQL = "DELETE FROM %s WHERE SpPrincipalID = %d";

        sql = "SELECT p.SpPrincipalID, pp.SpPermissionID" + postSQL;
        log.debug(sql);
        
        HashSet<Integer> prinIds = new HashSet<Integer>();
        for (Object[] row :  query(sql))
        {
            Integer prinId = (Integer)row[0];
            if (prinId != null)
            {
                prinIds.add(prinId);
            }
            
            Integer permId = (Integer)row[1];
            if (permId != null)
            {
                update(String.format(updatePermSQL, "spprincipal_sppermission", permId));
                update(String.format(updatePermSQL, "sppermission", permId));
                log.debug("Removing PermId: "+permId);
            }
        }
        
        StringBuilder sb1 = new StringBuilder();
        for (Integer prinId : prinIds)
        {
            update(String.format(updatePrinSQL, "specifyuser_spprincipal", prinId));
            update(String.format(updatePrinSQL, "spprincipal", prinId));
            log.debug("Removing PrinId: "+prinId);
            if (sb1.length() > 0) sb1.append(",");
            sb1.append(prinId.toString());
        }
        log.debug("("+sb1.toString()+")");
        
        // Create all the necessary UperPrincipal records
        // Start by figuring out what group there are and then create one UserPrincipal record
        // for each one
        
        TreeSet<String> nameSet = new TreeSet<String>();
        sql = "SELECT su.Name, su.SpecifyUserID, p.userGroupScopeID, p.SpPrincipalID FROM specifyuser su " +
        	  "INNER JOIN specifyuser_spprincipal sp ON su.SpecifyUserID = sp.SpecifyUserID " +
              "INNER JOIN spprincipal p ON sp.SpPrincipalID = p.SpPrincipalID " +
              "WHERE p.GroupSubClass = 'edu.ku.brc.af.auth.specify.principal.GroupPrincipal'";
        
        String fields     = "TimestampCreated, TimestampModified, Version, GroupSubClass, groupType, Name, Priority, Remarks, userGroupScopeID, CreatedByAgentID, ModifiedByAgentID";
        String insertSQL  = "INSERT INTO spprincipal ("+fields+") VALUES(?,?,?,?,?,?,?,?,?,?,?)";
        String insertSQL2 = "INSERT INTO specifyuser_spprincipal (SpecifyUserID, SpPrincipalID) VALUES(?,?)";
        
        String searchSql = "SELECT "+fields+" FROM spprincipal WHERE SpPrincipalID = ?";

        sb1 = new StringBuilder();
        
        PreparedStatement selStmt = null;
        PreparedStatement pStmt   = null;
        PreparedStatement pStmt2  = null;
        try
        {
            Connection conn = DBConnection.getInstance().getConnection();
            
            pStmt   = conn.prepareStatement(insertSQL);
            pStmt2  = conn.prepareStatement(insertSQL2);
            selStmt = conn.prepareStatement(searchSql);
            
            String adtSQL = "SELECT DISTINCT ca.AgentID FROM specifyuser AS su INNER Join agent AS ca ON su.CreatedByAgentID = ca.AgentID";
            Integer createdById = BasicSQLUtils.getCount(conn, adtSQL);
            if (createdById == null)
            {
                createdById = BasicSQLUtils.getCount(conn, "SELECT AgentID FROM agent ORDER BY AgentID ASC LIMIT 0,1");
                if (createdById == null)
                {
                    UIRegistry.showError("The permissions could not be fixed because there were no agents.");
                    AppPreferences.shutdownAllPrefs();
                    DBConnection.shutdownFinalConnection(true, true);
                    return;
                }
            }
            
            for (Object[] row : query(sql))
            {
                String  usrName = (String)row[0];
                Integer userId  = (Integer)row[1];
                Integer collId  = (Integer)row[2];
                Integer prinId  = (Integer)row[3];
             
                nameSet.add(usrName);
                
                log.debug("usrName: " + usrName + "  prinId: "+prinId);
                if (sb1.length() > 0) sb1.append(",");
                sb1.append(prinId.toString());
                
                selStmt.setInt(1, prinId);
                ResultSet rs = selStmt.executeQuery();
                if (rs.next())
                {
                    log.debug(String.format("%s - adding UserPrincipal for Collection  %d / %d", usrName, rs.getInt(9), collId));
                    Integer createdByAgentID  = (Integer)rs.getObject(10);
                    Integer modifiedByAgentID = (Integer)rs.getObject(11);
                    
                    pStmt.setTimestamp(1, rs.getTimestamp(1));
                    pStmt.setTimestamp(2, rs.getTimestamp(2));
                    pStmt.setInt(3,    1);                                                    // Version
                    pStmt.setString(4, "edu.ku.brc.af.auth.specify.principal.UserPrincipal"); // GroupSubClass
                    pStmt.setString(5, null);                                                 // groupType
                    pStmt.setString(6, rs.getString(6));                                      // Name
                    pStmt.setInt(7,    80);                                                   // Priority
                    pStmt.setString(8, rs.getString(8));                                      // Remarks
                    pStmt.setInt(9,    rs.getInt(9));                                         // userGroupScopeID
                    pStmt.setInt(10,   createdByAgentID != null ? createdByAgentID : createdById);
                    pStmt.setInt(11,   modifiedByAgentID != null ? modifiedByAgentID : createdById);
                    
                    // Create UserPrincipal
                    pStmt.executeUpdate();
                    
                    int newPrinId = BasicSQLUtils.getInsertedId(pStmt);
                    
                    // Join the new Principal to the SpecifyUser record
                    pStmt2.setInt(1, userId);
                    pStmt2.setInt(2, newPrinId);
                    pStmt2.executeUpdate();
                    
                } else
                {
                    // error
                }
                rs.close();
            }
            
            log.debug("("+sb1.toString()+")");
            
            AppPreferences.getRemote().putBoolean(FIXED_USER_PERMS, true);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
                if (pStmt2 != null) pStmt2.close();
                if (selStmt != null) selStmt.close();
            } catch (Exception ex){}
        }
        
        final StringBuilder sb = new StringBuilder();
        for (String nm : nameSet)
        {
            if (sb.length() > 0) sb.append('\n');
            sb.append(nm);
        }
        
        if (!doSilently)
        {
            JTextArea ta = UIHelper.createTextArea(15, 30);
            ta.setText(sb.toString());
            ta.setEditable(false);
            
            JEditorPane htmlPane   = new JEditorPane("text/html",  UIRegistry.getResourceString("FDBAL_PERMFIXEDDESC")); //$NON-NLS-1$
            htmlPane.setEditable(false);
            htmlPane.setOpaque(false);
            
            CellConstraints cc = new CellConstraints();
            PanelBuilder    pb = new PanelBuilder(new FormLayout("f:p:g", "p:g,8px,f:p:g"));
            pb.add(htmlPane, cc.xy(1, 1));
            pb.add(UIHelper.createScrollPane(ta), cc.xy(1, 3));
            pb.setDefaultDialogBorder();
            
            CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), UIRegistry.getResourceString("FDBAL_PERMFIXED"), true, CustomDialog.OK_BTN, pb.getPanel());
            dlg.setOkLabel(UIRegistry.getResourceString("CLOSE"));
            UIHelper.centerAndShow(dlg);
        }
    }
    
    
    /**
     * 
     */
    public static void fixDefaultDates()
    {
        boolean doFix = !AppPreferences.getGlobalPrefs().getBoolean(FIX_DEFDATES_PREF, false);
        //log.debug("fixDefaultDates -  Going To Fix["+doFix+"]");
        if (doFix)
        {
            HashMap<DBTableInfo, List<FormCellFieldIFace>>  tblToFldHash = new HashMap<DBTableInfo, List<FormCellFieldIFace>>();
            HashSet<String> nameHash = new HashSet<String>();
            
            for (ViewIFace view : ((SpecifyAppContextMgr)AppContextMgr.getInstance()).getEntirelyAllViews())
            {
                String tableClassName = view.getClassName();
                DBTableInfo ti = DBTableIdMgr.getInstance().getByClassName(tableClassName);
                if (ti != null)
                {
                	if (nameHash.contains(tableClassName))
                	{
                		continue;
                	}
                	nameHash.add(tableClassName);
                	
                	//log.debug(tableClassName);
                	
                    for (AltViewIFace avi : view.getAltViews())
                    {
                    	if (avi.getMode() == AltViewIFace.CreationMode.EDIT)
                    	{
	                        ViewDefIFace vd = (ViewDefIFace)avi.getViewDef();
	                        if (vd instanceof FormViewDef)
	                        {
	                            FormViewDefIFace fvd = (FormViewDefIFace)vd;
	                            for (FormRowIFace fri : fvd.getRows())
	                            {
	                                for (FormCellIFace fci : fri.getCells())
	                                {
	                                    if (fci instanceof FormCellFieldIFace)
	                                    {
		                                	//log.debug(ti.getName()+" - "+fci.getIdent()+"  "+fci.getName());
		                                	
	                                        FormCellFieldIFace fcf      = (FormCellFieldIFace)fci;
	                                        String             defValue = fcf.getDefaultValue();
	                                        if (StringUtils.isNotEmpty(defValue) && !defValue.equals("Today"))
	                                        {
	                                            List<FormCellFieldIFace> fieldList = tblToFldHash.get(ti);
	                                            if (fieldList == null)
	                                            {
	                                                fieldList = new ArrayList<FormCellFieldIFace>();
	                                                tblToFldHash.put(ti, fieldList);
	                                            }
	                                            fieldList.add(fcf);
	                                        }
	                                    }
	                                }
	                            }
                            }
                        }
                    }
                //} else
                //{
                    //log.debug("Skipping table Class Name["+tableClassName+"]");
                }
            }
            
            log.debug("Number of Tables Found["+tblToFldHash.size()+"]");
            processTableDefaultDates(tblToFldHash, false);
            
            AppPreferences.getGlobalPrefs().putBoolean(FIX_DEFDATES_PREF, true);
        }
    }
    
    /**
     * @param tblToFldHash
     * @param doingCount
     * @return
     */
    private static int processTableDefaultDates(final HashMap<DBTableInfo, List<FormCellFieldIFace>>  tblToFldHash, final boolean doingCount)
    {
        int totalCount = 0;
        for (DBTableInfo ti : tblToFldHash.keySet())
        {
        	//log.debug("processTableDefaultDates - Fixing table "+ti.getName());
            for (FormCellFieldIFace fci : tblToFldHash.get(ti))
            {
                String[] names = fci.getFieldNames();
                if (names != null && names.length > 0 && names.length < 3)
                {
                	Class<?> dataClass = null;
                	String   fldName   = null;
                	if (fci.getUiType() == FormCellFieldIFace.FieldType.plugin) // assumes plugin
                	{
                		String pluginName = fci.getProperty("name"); 
                		if (StringUtils.isNotEmpty(pluginName) && pluginName.equals("PartialDateUI"))
                		{
                			String dataField = fci.getProperty("df"); 
                			if (StringUtils.isNotEmpty(dataField))
                    		{
                				fldName   = dataField;
                				dataClass = java.util.Calendar.class;
                    		}
                		}
                	} else
                	{
                		fldName = names[0];
                	}
                	
                    DBFieldInfo fi = ti.getFieldByName(fldName);
                    if (fi != null)
                    {
                    	if (dataClass == null)
                        {
                        	dataClass = fi.getDataClass();
                        }
                    	
                    	if (dataClass == java.util.Calendar.class)
                    	{
	                       	//log.debug("processTableDefaultDates - Fixing field "+fi.getColumn());
	                        String sql = String.format("SELECT COUNT(*) FROM %s WHERE %s IS NULL AND TimestampCreated IS NOT NULL AND TimestampCreated > TIMESTAMP('2008-06-01 00:00:00')", ti.getName(), fi.getColumn());
	                        int    cnt = BasicSQLUtils.getCountAsInt(sql);
	                        if (cnt > 0)
	                        {
	                            if (doingCount)
	                            {
	                                totalCount += cnt;
	                            } else
	                            {
	                            	totalCount += fixTableDefaultDates(ti, fi);
	                            }
	                        }
                    	}
                    }
                }
            }
        }
        
        log.debug("processTableDefaultDates - Fixed "+totalCount+" Records.");
        
        return 0;
    }
    
    private static String updateSQL     = "UPDATE %s SET %s = DATE(TimestampCreated) WHERE %s IS NULL AND TimestampCreated IS NOT NULL AND TimestampCreated > TIMESTAMP('2008-06-01 00:00:00')";
    private static String updatePrecSQL = "UPDATE %s SET %s = 1 WHERE %s IS NULL AND %s IS NULL AND TimestampCreated IS NOT NULL AND TimestampCreated > TIMESTAMP('2008-06-01 00:00:00')";

    /**
     * @param ti
     * @param fi
     * @return
     */
    private static int fixTableDefaultDates(final DBTableInfo ti, final DBFieldInfo fi)
    {
        String colName = fi.getColumn();
        if (colName.length() > 4 && colName.endsWith("Date"))
        {
            String precName = colName + "Precision";
            if (ti.getFieldByColumnName(precName) != null)
            {
                String sql = String.format(updatePrecSQL, ti.getName(), precName, precName, fi.getColumn());
                BasicSQLUtils.update(sql);
            }
        }
        String sql = String.format(updateSQL, ti.getName(), fi.getColumn(), fi.getColumn());
        int rv = BasicSQLUtils.update(sql);

        return rv;
    }
    
    /**
     * 
     */
    public static void fixExsiccata()
    {
        final String typeSearchTag = "<typesearches>";
        String sql =  "SELECT d.SpAppResourceDataID, Data FROM spappresource AS r " +
                      "Inner Join spappresourcedata AS d ON r.SpAppResourceID = d.SpAppResourceID WHERE LOWER(r.Name) LIKE 'typesearch%'";
        
        Object[] row = BasicSQLUtils.queryForRow(sql);
        if (row != null && row.length > 0)
        {
            Integer id    = (Integer)row[0];
            byte[]  bytes = (byte[])row[1];
            if (bytes != null && bytes.length> 0)
            {
                String contents = new String(bytes);
                if (StringUtils.isNotEmpty(contents))
                {
                    String extStr = "\n    <typesearch tableid=\"89\" name=\"Exsiccata\" searchfield=\"title\" displaycols=\"title\" format=\"%s\" dataobjformatter=\"\"/>";
                    int inx = contents.indexOf(typeSearchTag) + typeSearchTag.length();
                    if (inx > -1 && inx < contents.length())
                    {
                        String newContents = contents.substring(0, inx) + extStr + contents.substring(inx);
                        //System.out.println(newContents);
                        Connection conn = DBConnection.getInstance().getConnection();
                        if (conn != null)
                        {
                            try
                            {
                                PreparedStatement ps = conn.prepareStatement("UPDATE spappresourcedata SET Data=? WHERE SpAppResourceDataID=?");
                                if (ps != null)
                                {
                                    ps.setString(1, newContents);
                                    ps.setInt(2, id);
                                    int rv = ps.executeUpdate();
                                    if (rv == 1)
                                    {
                                        AppPreferences.getGlobalPrefs().putBoolean("ExsiccataUpdateFor1_7", true);
                                    }
                                    ps.close();
                                }
                                
                            } catch (Exception ex)
                            {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
   }
  
    
    /**
     * @param discipline
     * @return
     */
    public static void addPickListByName(final String pickListName)
    {
        DataProviderSessionIFace localSession = null;
        try
        {
            localSession = DataProviderFactory.getInstance().createSession();
            Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
            
            collection = localSession.get(Collection.class, collection.getId());
            for (PickList pl : collection.getPickLists())
            {
                if (pl.getName().equals(pickListName))
                {
                    return;
                }
            }
            
            localSession.beginTransaction();
            
            List<BldrPickList> pickLists = DataBuilder.getBldrPickLists("common");
            if (pickLists != null)
            {
                for (BldrPickList pl : pickLists)
                {
                    if (!pl.getName().equals(pickListName)) continue;
                    
                    log.info("Creating PickList["+pl.getName()+"]");
                    PickList pickList = createPickList(pl.getName(), pl.getType(), pl.getTableName(),
                                                       pl.getFieldName(), pl.getFormatter(), pl.getReadOnly(), 
                                                       pl.getSizeLimit(), pl.getIsSystem(), pl.getSortType(), collection);
                    pickList.setIsSystem(true);
                    pickList.setCollection(collection);
                    collection.getPickLists().add(pickList);
                    
                    for (BldrPickListItem item : pl.getItems())
                    {
                        pickList.addItem(item.getTitle(), item.getValue(), item.getOrdinal());
                    }
                    localSession.saveOrUpdate(pickList);
                }
                if (localSession != null)
                {
                    localSession.saveOrUpdate(collection);
                }
            } else
            {
                log.error("No PickList XML");
            }
            localSession.commit();
            
            AppContextMgr.getInstance().setClassObject(Collection.class, collection);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            if (localSession != null) localSession.close();
        }
    }

    /**
     * @param counter
     * @param updater
     * @return
     */
    public static boolean checkAndUpdate(String counter, String updater) {
    	int cnt = BasicSQLUtils.getCountAsInt(counter);
    	if (cnt > 0 && BasicSQLUtils.update(updater) != cnt) {
    		return false;
    	}
    	return true;
    }
    
    /**
     * @return
     */
    public static boolean fixNullDatePrecisions() {
    	if (!checkAndUpdate("SELECT COUNT(*) FROM collectingevent WHERE EndDatePrecision IS NULL", 
    		"UPDATE `collectingevent` SET `EndDatePrecision`=1 WHERE `EndDatePrecision` IS NULL")) {
    		return false;
    	}
    	if (!checkAndUpdate("SELECT COUNT(*) FROM collectingevent WHERE StartDatePrecision IS NULL", 
        		"UPDATE `collectingevent` SET `StartDatePrecision`=1 WHERE `StartDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM collectingtrip WHERE EndDatePrecision IS NULL", 
        		"UPDATE `collectingtrip` SET `EndDatePrecision`=1 WHERE `EndDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM collectingtrip WHERE StartDatePrecision IS NULL", 
        		"UPDATE `collectingtrip` SET `StartDatePrecision`=1 WHERE `StartDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM collectionobject WHERE CatalogedDatePrecision IS NULL", 
        		"UPDATE `collectionobject` SET `CatalogedDatePrecision`=1 WHERE `CatalogedDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM determination WHERE DeterminedDatePrecision IS NULL", 
        		"UPDATE `determination` SET `DeterminedDatePrecision`=1 WHERE `DeterminedDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM preparation WHERE PreparedDatePrecision IS NULL", 
        		"UPDATE `preparation` SET `PreparedDatePrecision`=1 WHERE `PreparedDatePrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM agent WHERE DateOfBirthPrecision IS NULL", 
        		"UPDATE `agent` SET `DateOfBirthPrecision`=1 WHERE `DateOfBirthPrecision` IS NULL")) {
        		return false;
        }
    	if (!checkAndUpdate("SELECT COUNT(*) FROM agent WHERE DateOfDeathPrecision IS NULL", 
        		"UPDATE `agent` SET `DateOfDeathPrecision`=1 WHERE `DateOfDeathPrecision` IS NULL")) {
        		return false;
        }
    	return true;
    }
    
    /**
     * @return
     */
    public static boolean fixSchemaAfterPaleoModelUpdate() {
    	//remove old fields from schema
    	String sql = "SELECT COUNT(*) FROM splocaleitemstr WHERE splocalecontaineritemdescid IN(SELECT splocalecontaineritemid FROM splocalecontaineritem WHERE splocalecontainerid IN"
    			+ "(SELECT splocalecontainerid FROM splocalecontainer WHERE name='paleocontext') AND name IN('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	int cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "DELETE FROM splocaleitemstr WHERE splocalecontaineritemdescid IN(SELECT splocalecontaineritemid FROM splocalecontaineritem WHERE splocalecontainerid IN"
    			+ "(SELECT splocalecontainerid FROM splocalecontainer WHERE name='paleocontext') AND name IN('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}
    	
    	sql = "SELECT COUNT(*) from splocaleitemstr where splocalecontaineritemnameid in (select splocalecontaineritemid from splocalecontaineritem where splocalecontainerid in"
    			+ "(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "delete from splocaleitemstr where splocalecontaineritemnameid in (select splocalecontaineritemid from splocalecontaineritem where splocalecontainerid in"
    			+ "(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance'))";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}
    	
    	sql = "SELECT COUNT(*) from splocalecontaineritem where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance')";
    	cnt = BasicSQLUtils.getCountAsInt(sql);
    	sql = "delete from splocalecontaineritem where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name in('positionstate', 'direction', 'distanceUnits', 'topdistance', 'bottomdistance')";
    	if (BasicSQLUtils.update(sql) != cnt) {
    		return false;
    	}

    	//hide PaleoContext.Collectingevents
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=true where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name='collectingevents'");
		
		//show PaleoContext.CollectionObjects
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=false where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='paleocontext') and name='collectionobjects'");	

		//hide collectingevent.PaleoContextID
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=true where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='collectingevent') and name='paleocontext'");

		//show collectionobject.PaleoContextID ...just in case
		BasicSQLUtils.update("update splocalecontaineritem set ishidden=false where splocalecontainerid in(select splocalecontainerid from splocalecontainer where name='collectionobject') and name='paleocontext'");

		return true;
    }
    
    /**
     * @return
     */
    public static boolean fixSymbiotaExportSchema() {
    	String sql = "select disciplineid from discipline";
    	List<Object> disciplines = BasicSQLUtils.querySingleCol(sql);
    	List<Integer> added = new ArrayList<Integer>();
    	try {
    		for (Object discipline : disciplines) {
    			Integer esId = addSymbiotaExportSchema(Integer.class.cast(discipline));
    			if (esId == null) {
    				return false;
    			} else {
    				added.add(esId);
    			}
    		}
    		return true;
    	} catch (SQLException e) {
    		for (Integer esId : added) {
    			BasicSQLUtils.update("DELETE FROM spexportschemaitem WHERE SpExportSchemaID=" + esId);
				BasicSQLUtils.update("DELETE FROM spexportschema WHERE SpExportSchemaID=" + esId);
    		}
    		return false;
    	}
    }
    
    /**
     * @param sql
     * @throws SQLException
     */
    public static void insertThis(String sql) throws SQLException {
    	if (BasicSQLUtils.update(sql) != 1) {
    		throw new SQLException();
    	}
    }
    
    /**
     * @param disciplineId
     * @return
     * @throws SQLException
     */
    public static Integer addSymbiotaExportSchema(Integer disciplineId) throws SQLException {
		Statement stmt = DBConnection.getInstance().getConnection().createStatement();
		Integer schemaId = null;
		try {
			String sql = "insert into spexportschema(TimestampCreated,Version,Description,SchemaName,SchemaVersion,DisciplineID,CreatedByAgentID) "
	    			+ "values(now(),0,'http://rs.tdwg.org/dwc/terms','SymbiotaDwc','1.0'," + disciplineId + ","
	    			+ AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId() + ")";
			stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
			ResultSet key = stmt.getGeneratedKeys();
			key.next();
			schemaId = key.getInt(1);
			Integer spuId = AppContextMgr.getInstance().getClassObject(SpecifyUser.class).getId();
			//XXX This is so stoopid.
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','created'," + schemaId + "," + spuId + ")");                             
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','description'," + schemaId + "," + spuId + ")");                         
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','identifier'," + schemaId + "," + spuId + ")");                          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','language'," + schemaId + "," + spuId + ")");                            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','license'," + schemaId + "," + spuId + ")");                             
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','modified'," + schemaId + "," + spuId + ")");                            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','publisher'," + schemaId + "," + spuId + ")");                           
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','references'," + schemaId + "," + spuId + ")");                          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','title'," + schemaId + "," + spuId + ")");                               
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','accessRights'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','associatedTaxa'," + schemaId + "," + spuId + ")");                      
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','basisOfRecord'," + schemaId + "," + spuId + ")");                       
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','catalogNumber'," + schemaId + "," + spuId + ")");                       
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','collectionCode'," + schemaId + "," + spuId + ")");                      
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:double','coordinateUncertaintyInMeters'," + schemaId + "," + spuId + ")");       
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','country'," + schemaId + "," + spuId + ")");                             
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','county'," + schemaId + "," + spuId + ")");                              
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:dateTimeISO','dateIdentified'," + schemaId + "," + spuId + ")");                
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:gDay','day'," + schemaId + "," + spuId + ")");                                   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:decimalLatitudeDataType','decimalLatitude'," + schemaId + "," + spuId + ")");   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:decimalLongitudeDataType','decimalLongitude'," + schemaId + "," + spuId + ")"); 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','disposition'," + schemaId + "," + spuId + ")");                         
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','dynamicProperties'," + schemaId + "," + spuId + ")");                   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:dayOfYearDataType','endDayOfYear'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','establishmentMeans'," + schemaId + "," + spuId + ")");                  
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:dateTimeISO','eventDate'," + schemaId + "," + spuId + ")");                     
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','family'," + schemaId + "," + spuId + ")");                              
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','fieldNotes'," + schemaId + "," + spuId + ")");                          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','fieldNumber'," + schemaId + "," + spuId + ")");                         
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','footprintWKT'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','genus'," + schemaId + "," + spuId + ")");                               
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','geodeticDatum'," + schemaId + "," + spuId + ")");                       
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','georeferenceProtocol'," + schemaId + "," + spuId + ")");                
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','georeferenceRemarks'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','georeferenceSources'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','georeferenceVerificationStatus'," + schemaId + "," + spuId + ")");      
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','georeferencedBy'," + schemaId + "," + spuId + ")");                     
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','habitat'," + schemaId + "," + spuId + ")");                             
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:nonEmptyString','identificationID'," + schemaId + "," + spuId + ")");           
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','identificationQualifier'," + schemaId + "," + spuId + ")");             
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','identificationReferences'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','identificationRemarks'," + schemaId + "," + spuId + ")");               
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','identifiedBy'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:positiveInteger','individualCount'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','informationWithheld'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','infraspecificEpithet'," + schemaId + "," + spuId + ")");                
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','institutionCode'," + schemaId + "," + spuId + ")");                     
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','lifeStage'," + schemaId + "," + spuId + ")");                           
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','locality'," + schemaId + "," + spuId + ")");                            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:nonEmptyString','locationID'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:double','maximumElevationInMeters'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:double','minimumElevationInMeters'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:gMonth','month'," + schemaId + "," + spuId + ")");                               
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','municipality'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','occurrenceRemarks'," + schemaId + "," + spuId + ")");                   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','otherCatalogNumbers'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','ownerInstitutionCode'," + schemaId + "," + spuId + ")");                
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','preparations'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','recordNumber'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','recordedBy'," + schemaId + "," + spuId + ")");                          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','reproductiveCondition'," + schemaId + "," + spuId + ")");               
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','rights'," + schemaId + "," + spuId + ")");                              
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','rightsHolder'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','samplingProtocol'," + schemaId + "," + spuId + ")");                    
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','scientificName'," + schemaId + "," + spuId + ")");                      
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','scientificNameAuthorship'," + schemaId + "," + spuId + ")");            
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','sex'," + schemaId + "," + spuId + ")");                                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','specificEpithet'," + schemaId + "," + spuId + ")");                     
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'dwc:dayOfYearDataType','startDayOfYear'," + schemaId + "," + spuId + ")");          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','stateProvince'," + schemaId + "," + spuId + ")");                       
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','taxonRank'," + schemaId + "," + spuId + ")");                           
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','taxonRemarks'," + schemaId + "," + spuId + ")");                        
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','typeStatus'," + schemaId + "," + spuId + ")");                          
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','verbatimCoordinates'," + schemaId + "," + spuId + ")");                 
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','verbatimElevation'," + schemaId + "," + spuId + ")");                   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:string','verbatimEventDate'," + schemaId + "," + spuId + ")");                   
			insertThis("insert into spexportschemaitem(TimestampCreated,Version,DataType,FieldName,SpExportschemaId,CreatedByAgentID) values(now(),0,'xs:gYear','year'," + schemaId + "," + spuId + ")");
			return schemaId;
		} catch (SQLException e) {
				if (schemaId != null) {
					stmt.executeUpdate("DELETE FROM spexportschemaitem WHERE SpExportSchemaID=" + schemaId);
					stmt.executeUpdate("DELETE FROM spexportschema WHERE SpExportSchemaID=" + schemaId);
				}
				throw e;
		} finally {
			stmt.close();
		}
    }
    /**
     * 
     */
    public static void fixGTPTreeDefParents()
    {
        int     fixed = 0;
        String  updateSQL = "UPDATE geologictimeperiodtreedefitem SET ParentItemID=? WHERE GeologicTimePeriodTreeDefItemID=?";
        
        Connection        conn  = DBConnection.getInstance().getConnection();
        PreparedStatement pStmt = null;
        try
        {
            pStmt = conn.prepareStatement(updateSQL);
            
            Vector<Integer> gtpTreeDefIds = BasicSQLUtils.queryForInts("SELECT GeologicTimePeriodTreeDefID FROM geologictimeperiodtreedef ORDER BY GeologicTimePeriodTreeDefID");
            for (Integer defId : gtpTreeDefIds)
            {
                String sql = String.format("SELECT COUNT(*) FROM geologictimeperiodtreedefitem WHERE ParentItemID IS NULL AND GeologicTimePeriodTreeDefID=%d", defId);
                if (BasicSQLUtils.getCount(sql) == 1) continue;
               
                sql = String.format("SELECT GeologicTimePeriodTreeDefItemID FROM geologictimeperiodtreedefitem WHERE GeologicTimePeriodTreeDefID=%d ORDER BY RankID", defId);
                Vector<Integer> gtpTreeDefItemIds = BasicSQLUtils.queryForInts(sql);
                Vector<Integer> parentIds         = new Vector<Integer>();
                parentIds.add(-1);
                
                for (int i = 1; i < gtpTreeDefItemIds.size(); ++i)
                {
                    parentIds.add(gtpTreeDefItemIds.get(i-1));
                }
                
                fixed = 0;
                for (int i = 1; i < gtpTreeDefItemIds.size(); ++i)
                {
                    log.debug(String.format("Node: %d  -> Parent: %d", gtpTreeDefItemIds.get(i), parentIds.get(i)));
                    pStmt.setInt(1, parentIds.get(i));
                    pStmt.setInt(2, gtpTreeDefItemIds.get(i));
                    int rv = pStmt.executeUpdate();
                    if (rv != 1)
                    {
                        log.error("Error updating GTP TreeDef Item PArent");
                    }
                    fixed++;
                }
                log.debug(String.format("Fixed %d/%d", fixed, gtpTreeDefItemIds.size()-1));
            }
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt != null) pStmt.close();
            } catch (SQLException e) {}
        }
    }
    
    /**
     * 
     */
    public static void fixAttachmentOrdinal()
    {
        try
        {
            Class<?>[] attachmentClasses = {
                    AccessionAttachment.class,
                    AgentAttachment.class,
                    BorrowAttachment.class,
                    //CollectingEventAttachment.class,
                    CollectionObjectAttachment.class,
                    ConservDescriptionAttachment.class,
                    ConservEventAttachment.class,
                    DNASequenceAttachment.class,
                    DNASequencingRunAttachment.class,
                    FieldNotebookAttachment.class,
                    FieldNotebookPageAttachment.class,
                    FieldNotebookPageSetAttachment.class,
                    GiftAttachment.class,
                    LoanAttachment.class,
                    LocalityAttachment.class,
                    PermitAttachment.class,
                    PreparationAttachment.class,
                    ReferenceWorkAttachment.class,
                    RepositoryAgreementAttachment.class,
                    TaxonAttachment.class,
                };
            
            Class<?>[] ownerClasses = {
                    Accession.class,
                    Agent.class,
                    Borrow.class,
                    //CollectingEvent.class,
                    CollectionObject.class,
                    ConservDescription.class,
                    ConservEvent.class,
                    DNASequence.class,
                    DNASequencingRun.class,
                    FieldNotebook.class,
                    FieldNotebookPage.class,
                    FieldNotebookPageSet.class,
                    Gift.class,
                    Loan.class,
                    Locality.class,
                    Permit.class,
                    Preparation.class,
                    ReferenceWork.class,
                    RepositoryAgreement.class,
                    Taxon.class,
                };

            int i = 1;
            for (Class<?> cls : attachmentClasses)
            {
                DBTableInfo ownerTI = DBTableIdMgr.getInstance().getByClassName(ownerClasses[i-1].getName());
                DBTableInfo ti      = DBTableIdMgr.getInstance().getByClassName(cls.getName());
                
                  String sql = String.format("SELECT %s FROM %s WHERE Ordinal IS NULL GROUP BY %s", 
                                             ownerTI.getIdColumnName(), ti.getName(), ownerTI.getIdColumnName());
                  //System.out.println(sql);
                  Vector<Integer> ownerIDs = BasicSQLUtils.queryForInts(sql);
                  if (ownerIDs != null)
                  {
                      for (Integer ownerId : ownerIDs)
                      {
                          sql = String.format("SELECT %s FROM %s WHERE %s = %d ORDER BY %s", 
                                  ti.getIdColumnName(), ti.getName(), ownerTI.getIdColumnName(), ownerId, ti.getIdColumnName());
                          //System.out.println(sql);
                          
                          Vector<Integer> ids = BasicSQLUtils.queryForInts(sql);
                          if (ids != null)
                          {
                              int ordinal = 0;
                              for (Integer id : ids)
                              {
                                  //log.debug(ti.getName()+"   -> "+id);
                                  sql = String.format("UPDATE %s SET Ordinal = %d WHERE %s = %d",
                                                      ti.getName(), ordinal, ti.getIdColumnName(), id);
                                  //System.out.println(sql);
                                  int cnt = update(sql);
                                  if (cnt != 1)
                                  {
                                      log.error("Error updating [" + cnt + "]");
                                  }
                                  //log.debug(String.format("Set TableID for %d attachments.", cnt));
                                  ordinal++;
                              }
                          }
                      }
                  }
                i++;
            }
            return;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}