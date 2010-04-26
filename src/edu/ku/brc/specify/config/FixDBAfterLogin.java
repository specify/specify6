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

import java.util.HashSet;

import edu.ku.brc.af.prefs.AppPreferences;
import edu.ku.brc.specify.conversion.BasicSQLUtils;

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
    public void fixAgentToDisciplines()
    {
        // First we loop through each SpecifyUser
        String sql = "SELECT SpecifyUserID FROM agent WHERE SpecifyUserID GROUP BY SpecifyUserID";
        for (Object spObj : BasicSQLUtils.querySingleCol(sql))
        {
            Integer spuId = (Integer)spObj;
            //System.err.println("spuId: "+spuId);
            
            // Create a HashSet of the SpecifyUser's Disciplines that they have permission to access
            HashSet<Integer> spUserDispHash = new HashSet<Integer>();
            sql = "SELECT c.DisciplineID " +
                  "FROM specifyuser su INNER JOIN specifyuser_spprincipal ssp ON su.SpecifyUserID = ssp.SpecifyUserID " +
                  "INNER JOIN spprincipal sp ON ssp.SpPrincipalID = sp.SpPrincipalID " +
                  "INNER JOIN collection c ON sp.userGroupScopeID = c.UserGroupScopeId WHERE su.SpecifyUserID = " + spuId;
            for (Object idObj : BasicSQLUtils.querySingleCol(sql))
            {
                Integer dspId = (Integer)idObj;
                spUserDispHash.add(dspId);
                //System.err.println("dspId: "+dspId);
            }
            
            // Get a List of Agents for this user
            sql = "SELECT AgentID, DivisionID FROM agent WHERE SpecifyUserID = " + spuId;
            for (Object[] row : BasicSQLUtils.query(sql))
            {
                Integer agtId = (Integer)row[0];
                Integer divId = (Integer)row[1];
                
                //System.err.println("agtId: "+agtId);
                //System.err.println("divId: "+divId);
                
                // Gets all the Disciplines that this Agent 'might' have access to.
                sql = "SELECT UserGroupScopeId FROM discipline WHERE DivisionID = " + divId;
                for (Object idObj : BasicSQLUtils.querySingleCol(sql))
                {
                    Integer dspId = (Integer)idObj;
                    //System.err.println("  dspId: "+dspId);
                    
                    // This determines if the Agent has permission to access a Disicpline
                    // that belongs to the Division they are in
                    if (spUserDispHash.contains(dspId))
                    {
                        // Check the count to make sure we don't duplicate it
                        sql = String.format("SELECT COUNT(*) FROM agent_discipline WHERE AgentID = %d AND DisciplineID = %d", agtId, dspId);
                       // System.err.println(sql);
                        
                        if (BasicSQLUtils.getCountAsInt(sql) == 0) 
                        {
                            sql = String.format("INSERT INTO agent_discipline SET AgentID = %d, DisciplineID = %d", agtId, dspId);
                            //System.err.println(sql);
                            BasicSQLUtils.update(sql);
                        }
                    }
                }
            }
        }
        AppPreferences.getGlobalPrefs().putBoolean("FixAgentToDisciplines2", true);
    }
    
    
    /**
     * 
     */
    public void checkMultipleLocalities()
    {
         int cnt = BasicSQLUtils.getCountAsInt("select count(localitydetailid) - count(distinct localityid) from localitydetail");
         if (cnt > 0)
         {
             
             cnt = BasicSQLUtils.getCountAsInt("select count(collectionobjectid) from collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid  where ce.localityid in (select localityid from localitydetail group by localityid having count(localitydetailid) > 1)");
             String str = String.format("Multiple Locality Detail Records - Count: %d", cnt);
             edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
         }
         
         cnt = BasicSQLUtils.getCountAsInt("select count(geocoorddetailid) - count(distinct localityid) from geocoorddetail");
         if (cnt > 0)
         {
             cnt = BasicSQLUtils.getCountAsInt("select count(collectionobjectid) from collectionobject co inner join collectingevent ce on ce.collectingeventid = co.collectingeventid  where ce.localityid in (select localityid from geocoorddetail group by localityid having count(geocoorddetailid) > 1)");
             String str = String.format("Multiple GeoCoord Detail Records - Count: %d", cnt);
             edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(FixDBAfterLogin.class, str, new Exception(str));
         }
    }
    
    /**
     * fixes bad version and timestamps for recordsets created by Uploader. 
     */
    public void fixUploaderRecordsets()
    {
    	BasicSQLUtils.update( "update recordset set TimestampCreated = now(), Version = 0 where Type = 1 and Version is null");
        AppPreferences.getGlobalPrefs().putBoolean("FixUploaderRecordsets", true);
    }
    
}
