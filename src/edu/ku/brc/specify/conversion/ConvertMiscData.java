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
package edu.ku.brc.specify.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Oct 21, 2009
 *
 */
public class ConvertMiscData
{
    protected static final Logger log = Logger.getLogger(ConvertMiscData.class);
    
    public static boolean convertKUFishCruiseData(final Connection oldDBConn, final Connection newDBConn, final int disciplineID)
    {
        PreparedStatement pStmt1 = null;
        PreparedStatement pStmt2 = null;
        try
        {
            Timestamp now = new Timestamp(System .currentTimeMillis());
            pStmt1 = newDBConn.prepareStatement("INSERT INTO collectingtrip (CollectingTripName, DisciplineID, TimestampCreated, Version) VALUES(?,?,?,?)");
            pStmt2 = newDBConn.prepareStatement("INSERT INTO collectingevent (CollectingTripID, DisciplineID, stationFieldNumber, Method, StartTime, TimestampCreated, TimestampModified, Version) VALUES(?,?,?,?,?,?,?,?)");
            
            String sql = "SELECT Text1, Text2, Number1, TimestampCreated, TimestampModified FROM stratigraphy";
            Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, sql);
            for (Object[] row : rows)
            {
                pStmt1.setString(1, "Cruise");
                pStmt1.setInt(2, disciplineID);
                pStmt1.setTimestamp(3, now);
                pStmt1.setInt(4, 0);
                pStmt1.execute();
                
                Integer intsertId = BasicSQLUtils.getInsertedId(pStmt1);
                String vessel     = (String)row[0];
                String cruiseName = (String)row[1];
                Integer number    = row[2] != null ? ((Double)row[2]).intValue() : null;
                
                pStmt2.setInt(1, intsertId);
                pStmt2.setInt(2, disciplineID);
                pStmt2.setString(3, vessel);
                pStmt2.setString(4, cruiseName);
                if (number != null)
                {
                    pStmt2.setInt(5, number);
                }
                pStmt2.setTimestamp(6, (Timestamp)row[3]);
                pStmt2.setTimestamp(7, (Timestamp)row[4]);
                pStmt1.setInt(8, 0);
                
                pStmt2.execute();
            }
            return true;
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            
        } finally
        {
            try
            {
                if (pStmt1 != null) pStmt1.close();
                if (pStmt2 != null) pStmt2.close();
                
            } catch (Exception ex) {}
        }
        
        return false;
    }
}
