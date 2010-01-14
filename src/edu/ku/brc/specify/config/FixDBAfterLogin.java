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
        String sql = "SELECT a.AgentID, d.disciplineId FROM specifyuser su " +
                     "INNER JOIN specifyuser_spprincipal spp ON su.SpecifyUserID = spp.SpecifyUserID " +
                     "INNER JOIN agent a ON a.SpecifyUserID = su.SpecifyUserID " +
                     "INNER JOIN spprincipal p ON spp.SpPrincipalID = p.SpPrincipalID " +
                     "INNER JOIN collection c ON p.userGroupScopeID = c.UserGroupScopeId " +
                     "INNER JOIN discipline d ON c.DisciplineID = d.disciplineId " +
                     "ORDER BY su.SpecifyUserID ASC";
        
        for (Object[] row : BasicSQLUtils.query(sql))
        {
            sql = String.format("SELECT COUNT(*) FROM agent_discipline WHERE AgentID = %d AND DisciplineID = %d", (Integer)row[0], (Integer)row[1]);
            System.err.println(sql);
            if (BasicSQLUtils.getCountAsInt(sql) == 0) 
            {
                sql = String.format("INSERT INTO agent_discipline SET AgentID = %d, DisciplineID = %d", (Integer)row[0], (Integer)row[1]);
                System.err.println(sql);
                BasicSQLUtils.update(sql);
            }
        }
        
        AppPreferences.getGlobalPrefs().putBoolean("FixAgentToDisciplines", true);
    }
}
