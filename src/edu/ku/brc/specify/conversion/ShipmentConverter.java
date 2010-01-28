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

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 28, 2010
 *
 */
public class ShipmentConverter
{

    protected Connection oldDBConn;
    protected Connection newDBConn;
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public ShipmentConverter(final Connection oldDBConn, 
                             final Connection newDBConn)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
    }
    
    
    public void fixup()
    {
        try
        {
            IdMapperIFace shipMapper = IdMapperMgr.getInstance().get("shipment", "ShipmentID");
            IdMapperIFace loanMapper = IdMapperMgr.getInstance().get("loan", "LoanID");
            IdMapperIFace giftMapper = IdMapperMgr.getInstance().get("gift", "GiftID");
            IdMapperIFace brrwMapper = IdMapperMgr.getInstance().get("borrow", "BorrowID");
            IdMapperIFace exchMapper = IdMapperMgr.getInstance().get("exchangeout", "ExchangeOutID");
            
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE shipment SET LoanID=? WHERE ShipmentID=?");
            String sql = "SELECT LoanID, ShipmentID FROM loan WHERE ShipmentID IS NOT NULL AND Category = 0"; // Loans
            for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
            {
                Integer loandID    = (Integer)row[0];
                Integer shipmentID = (Integer)row[1];
                
                pStmt.setInt(1, loanMapper.get(loandID));
                pStmt.setInt(2, shipMapper.get(shipmentID));
                pStmt.execute();
            }
            pStmt.close();
            
            pStmt = newDBConn.prepareStatement("UPDATE shipment SET GiftID=? WHERE ShipmentID=?");
            sql = "SELECT LoanID, ShipmentID FROM loan WHERE ShipmentID IS NOT NULL AND Category = 1"; // Gifts
            for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
            {
                Integer giftID     = (Integer)row[0];
                Integer shipmentID = (Integer)row[1];
                
                pStmt.setInt(1, giftMapper.get(giftID));
                pStmt.setInt(2, shipMapper.get(shipmentID));
                pStmt.execute();
            }
            pStmt.close();
            
            pStmt = newDBConn.prepareStatement("UPDATE shipment SET BorrowID=? WHERE ShipmentID=?");
            sql = "SELECT BorrowID, ShipmentID FROM borrowshipments WHERE ShipmentID IS NOT NULL"; // Gifts
            for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
            {
                Integer brrwID     = (Integer)row[0];
                Integer shipmentID = (Integer)row[1];
                
                pStmt.setInt(1, brrwMapper.get(brrwID));
                pStmt.setInt(2, shipMapper.get(shipmentID));
                pStmt.execute();
            }
            pStmt.close();
            
            pStmt = newDBConn.prepareStatement("UPDATE shipment SET ExchangeOutID=? WHERE ShipmentID=?");
            sql = "SELECT ExchangeOutID, ShipmentID FROM exchangeout WHERE ShipmentID IS NOT NULL"; // Gifts
            for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
            {
                Integer exchID     = (Integer)row[0];
                Integer shipmentID = (Integer)row[1];
                
                pStmt.setInt(1, exchMapper.get(exchID));
                pStmt.setInt(2, shipMapper.get(shipmentID));
                pStmt.execute();
            }
            pStmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}
