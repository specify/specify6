/* Copyright (C) 2009, University of Kansas Center for Research
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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.getCountAsInt;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;

import java.awt.Frame;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.TreeSet;

import javax.swing.JTextArea;

import org.apache.log4j.Logger;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
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
    private static final Logger  log = Logger.getLogger(FixDBAfterLogin.class);
    
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
			edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
		}
         
         cnt = getCountAsInt("select count(geocoorddetailid) - count(distinct localityid) from geocoorddetail");
         if (cnt > 0)
         {
        	 cnt = getCountAsInt("select count(collectionobjectid) from collectionobject co " +
        	 		"inner join collectingevent ce on ce.collectingeventid = co.collectingeventid " +
        	 		"inner join (select localityid from geocoorddetail group by localityid having count(geocoorddetailid) > 1) badlocs on badlocs.localityid = ce.localityid");
 		     String str = String.format("Multiple GeoCoord Detail Records - Count: %d", cnt);
 		     edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
         }
    }
    
    /**
     * fixes bad version and timestamps for recordsets created by Uploader. 
     */
    public void fixUploaderRecordsets()
    {
    	update( "update recordset set TimestampCreated = now(), Version = 0 where Type = 1 and Version is null");
        AppPreferences.getGlobalPrefs().putBoolean("FixUploaderRecordsets", true);
    }
    
    /**
     * 
     */
    public static void fixUserPermissions()
    {
        boolean isOK = true;
        if (!isOK) return;

        String whereStr  = " WHERE p.GroupSubClass = 'edu.ku.brc.af.auth.specify.principal.UserPrincipal' " +
                           "AND p.userGroupScopeID IS NULL";
        
        String clauseStr = "FROM spprincipal p INNER JOIN specifyuser_spprincipal sp ON p.SpPrincipalID = sp.SpPrincipalID " +
                           "INNER JOIN specifyuser su ON sp.SpecifyUserID = su.SpecifyUserID ";

        String sql = "SELECT su.Name " + clauseStr + whereStr + " GROUP BY su.Name";
        
        final String updatePermSQL = "DELETE FROM %s WHERE SpPermissionID = %d";
        final String updatePrinSQL = "DELETE FROM %s WHERE SpPrincipalID = %d";
        
        log.debug(sql);
        
        sql = "SELECT p.SpPrincipalID, pp.SpPermissionID FROM specifyuser su " +
        	  "INNER JOIN specifyuser_spprincipal ss ON su.SpecifyUserID = ss.SpecifyUserID " +
              "INNER JOIN spprincipal p ON ss.SpPrincipalID = p.SpPrincipalID " +
              "LEFT JOIN spprincipal_sppermission pp ON p.SpPrincipalID = pp.SpPrincipalID " +
              "LEFT OUTER JOIN sppermission pm ON pp.SpPermissionID = pm.SpPermissionID " +
              whereStr;
        
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
            pStmt   = DBConnection.getInstance().getConnection().prepareStatement(insertSQL);
            pStmt2  = DBConnection.getInstance().getConnection().prepareStatement(insertSQL2);
            selStmt = DBConnection.getInstance().getConnection().prepareStatement(searchSql);
            for (Object[] row : query(sql))
            {
                String  usrName = (String)row[0];
                Integer userId  = (Integer)row[1];
                Integer collId  = (Integer)row[2];
                Integer prinId  = (Integer)row[3];
             
                nameSet.add(usrName);
                
                log.debug("prinId: "+prinId);
                if (sb1.length() > 0) sb1.append(",");
                sb1.append(prinId.toString());
                
                selStmt.setInt(1, prinId);
                ResultSet rs = selStmt.executeQuery();
                if (rs.next())
                {
                    log.debug(String.format("%s - adding UserPrincipal for Collection %d / %d", usrName, rs.getInt(9), collId));
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
                    pStmt.setInt(10,   createdByAgentID != null ? createdByAgentID : 1);
                    pStmt.setInt(11,   modifiedByAgentID != null ? modifiedByAgentID : 1);
                    
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
        
        JTextArea ta = UIHelper.createTextArea(15, 30);
        ta.setText(sb.toString());
        CustomDialog dlg = new CustomDialog((Frame)UIRegistry.getMostRecentWindow(), "Permissions Removed", true, CustomDialog.OK_BTN, UIHelper.createScrollPane(ta));
        dlg.setOkLabel(UIRegistry.getResourceString("CLOSE"));
        UIHelper.centerAndShow(dlg);
    }
}
