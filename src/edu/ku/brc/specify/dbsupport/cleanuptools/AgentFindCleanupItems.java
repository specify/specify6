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
package edu.ku.brc.specify.dbsupport.cleanuptools;

import java.awt.Frame;
import java.awt.HeadlessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.config.Scriptlet;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.UIHelper;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Jun 29, 2010
 *
 */
public class AgentFindCleanupItems extends BaseFindCleanupItems
{

    /**
     * @param frame
     * @param title
     * @throws HeadlessException
     */
    public AgentFindCleanupItems(Frame frame) throws HeadlessException
    {
        super(DBTableIdMgr.getInstance().getInfoById(Agent.getClassTableId()));
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems#doWork()
     */
    @Override
    protected Vector<FindItemInfo> doWork()
    {
        Vector<FindItemInfo> items = super.doWork();
        
        HashSet<String> lastNameHash = new HashSet<String>();
        
        Scriptlet scriptlet = new Scriptlet();
       
        // First get LastNames that are duplicates
        String sql = "SELECT LastName, cnt FROM (SELECT LastName, COUNT(LastName) as cnt FROM agent WHERE LastName IS NOT NULL GROUP BY LastName) T1";
        int i = 0;
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            if (i % 100 == 0) System.out.println("Loaded: "+i);
            i++;
            
            String lastName = (String)row[0];
            if (lastNameHash.contains(lastName)) continue;
                
            long   cnt      = (Long)row[1];
            Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement pStmt = null;
            try
            {
                if (lastName.length() > 2 && cnt == 1)
                {
                    String partialLastName = lastName.substring(0, 1) +"%";
                    pStmt = conn.prepareStatement("SELECT LastName FROM agent WHERE LastName = ? AND LastName IS NOT NULL ORDER BY TimestampCreated ASC");
                    pStmt.setString(1, partialLastName);
                    ResultSet rs = pStmt.executeQuery();
                    while (rs.next())
                    {
                        String lName = rs.getString(1);
                        if (StringUtils.getLevenshteinDistance(lastName, lName) == 2)
                        {
                            cnt++;
                            break;
                        }
                    }
                    rs.close();
                    pStmt.close();
                }
                
                if (cnt > 1)
                {
                    pStmt = conn.prepareStatement("SELECT AgentID, FirstName, MiddleInitial FROM agent WHERE LastName = ? ORDER BY TimestampCreated ASC");
                    pStmt.setString(1, lastName);
                    ResultSet rs = pStmt.executeQuery();
                    while (rs.next())
                    {
                        int    id         = rs.getInt(1);
                        String firstName  = rs.getString(2);
                        String midInitial = rs.getString(3);
                        String fullName   = scriptlet.buildNameString(firstName, lastName, midInitial);
                        FindItemInfo ii = new FindItemInfo(id, fullName);
                        items.add(ii);
                        lastNameHash.add(lastName);
                    }
                    rs.close();
                    pStmt.close();
                } 
                    
            } catch (SQLException ex)
            {
                ex.printStackTrace();
            }
        }
        
        return items;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.dbsupport.cleanuptools.BaseFindCleanupItems#doCleanupOfItem()
     */
    @Override
    protected void doCleanupOfItem()
    {
        FindItemInfo itemInfo = (FindItemInfo)itemsList.getSelectedValue();
        if (itemInfo != null)
        {
            AgentCleanupResults agentResults = new AgentCleanupResults(itemInfo);
            UIHelper.centerAndShow(agentResults);
        }
    }


}
