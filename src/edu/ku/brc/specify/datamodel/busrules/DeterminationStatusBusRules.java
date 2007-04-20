/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import static edu.ku.brc.ui.UIRegistry.getLocalizedMessage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.DeterminationStatus;

/**
 * Business Rules for DeterminationStatus.
 * 
 * @author rods
 *
 * @code_status Beta
 *
 * Created Date: Dec 19, 2006
 *
 */
public class DeterminationStatusBusRules extends BaseBusRules
{
    
    /**
     * Constrcutor.
     */
    public DeterminationStatusBusRules()
    {
        super(DeterminationStatusBusRules.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#getDeleteMsg(java.lang.Object)
     */
    public String getDeleteMsg(Object dataObj)
    {
        if (dataObj instanceof DeterminationStatus)
        {
            return getLocalizedMessage("DETERMINATION_STATUS_DELETED", ((DeterminationStatus)dataObj).getName());
        }
        // else
        return super.getDeleteMsg(dataObj);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.forms.BusinessRulesIFace#okToDelete(java.lang.Object)
     */
    public boolean okToDelete(Object dataObj)
    {
        errorList.clear();
        
        DeterminationStatus DeterminationStatus = (DeterminationStatus)dataObj;
        if (DeterminationStatus.getId() == null)
        {
            return true;
        }
        
        Connection conn = null;
        Statement  stmt = null;
        try
        {
            conn = DBConnection.getInstance().createConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select count(*) from determination where determination.DeterminationStatusID = "+DeterminationStatus.getId());
            return rs.first() && rs.getInt(1) == 0;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try 
            {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }

        return false;
    }
}
