/* Copyright (C) 2015, University of Kansas Center for Research
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
package edu.ku.brc.specify.conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Oct 1, 2009
 *
 */
public class HabitatTaxonIdConverter
{
    protected SimpleDateFormat                       dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected SimpleDateFormat                       dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected Timestamp                              now                    = new Timestamp(System .currentTimeMillis());
    protected String                                 nowStr                 = dateTimeFormatter.format(now);

    protected Connection oldDBConn;
    protected Connection newDBConn;
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public HabitatTaxonIdConverter(final Connection oldDBConn, 
                                   final Connection newDBConn)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
    }
    
    /**
     * @return
     */
    public boolean shouldConvert()
    {
        return BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM habitat WHERE HostTaxonID IS NOT NULL") > 0;
    }
    
    /**
     * @return
     */
    public boolean convert(final int collectionMemberId)
    {
        //IdMapperIFace coMapper = IdMapperMgr.getInstance().get("collectionobject", "CollectionObjectID");
        IdMapperIFace txMapper = IdMapperMgr.getInstance().get("taxonname",       "TaxonNameID");
        IdMapperIFace hbMapper = IdMapperMgr.getInstance().get("habitat",          "HabitatID");
        IdMapperIFace coMapper = IdMapperMgr.getInstance().get("collectionobjectcatalog", "CollectionObjectCatalogID");
        
        String sql = "SELECT co.CollectionObjectID, ce.CollectingEventID, h.HabitatID, h.HostTaxonID FROM collectionobject co INNER JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID " + 
                     "INNER JOIN habitat h ON ce.CollectingEventID = h.HabitatID WHERE h.HostTaxonID IS NOT NULL";
        PreparedStatement updateStmt   = null;
        PreparedStatement insertStmt   = null;
        PreparedStatement coUpdateStmt = null;
        Statement         stmt  = null;
        try
        {
            updateStmt   = newDBConn.prepareStatement("UPDATE collectionobjectattribute SET RelatedTaxonID=? WHERE CollectionObjectAttributeID = ?");
            coUpdateStmt = newDBConn.prepareStatement("UPDATE collectionobject SET CollectionObjectAttributeID=? WHERE CollectionObjectID = ?");
            insertStmt   = newDBConn.prepareStatement("INSERT INTO collectionobjectattribute (TimestampCreated, TimestampModified, Version, RelatedTaxonID, CollectionMemberID) VALUES(?,?,?,?,?)");
            stmt         = oldDBConn.createStatement();
            
            
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                int coId = rs.getInt(1);
                //int ceId = rs.getInt(2);
                int hbId = rs.getInt(3);
                int txId = rs.getInt(4);
                
                int     newCOId = coMapper.get(coId);
                boolean hasAttr = BasicSQLUtils.getCountAsInt(newDBConn, "SELECT CollectionObjectAttributeID FROM collectionobject WHERE CollectionObjectAttributeID IS NOT NULL AND CollectionObjectID = " + newCOId) == 1;
                
                int newHBId = hbMapper.get(hbId);
                int newTXId = txMapper.get(txId);
                
                if (hasAttr)
                {
                    updateStmt.setInt(1, newTXId);
                    updateStmt.setInt(2, newHBId);
                    
                    if (updateStmt.executeUpdate() != 1)
                    {
                        throw new RuntimeException("Couldn't update ColObjAttr Id["+newHBId+"] with TaxonId ["+newTXId+"]");
                    }

                } else
                {
                    insertStmt.setTimestamp(1, now);
                    insertStmt.setTimestamp(2, now);
                    insertStmt.setInt(3, 0);
                    insertStmt.setInt(4, newTXId);
                    insertStmt.setInt(5, collectionMemberId);
                    
                    if (insertStmt.executeUpdate() != 1)
                    {
                        throw new RuntimeException("Couldn't update ColObjAttr Id["+newHBId+"] with TaxonId ["+newTXId+"]");
                    }
                    
                    int newColObjAttrId = BasicSQLUtils.getInsertedId(insertStmt);
                    coUpdateStmt.setInt(1, newColObjAttrId);
                    coUpdateStmt.setInt(2, newCOId);
                    if (coUpdateStmt.executeUpdate() != 1)
                    {
                        throw new RuntimeException("Couldn't update CollectionObject Id["+newCOId+"] with newColObjAttrId ["+newColObjAttrId+"]");
                    }
                }
            }
            rs.close();
            
            return true;
            
        } catch (SQLException ex)
        {
            ex.printStackTrace();
            try
            {
                if (stmt != null) stmt.close();
                if (updateStmt != null) updateStmt.close();
                if (insertStmt != null) insertStmt.close();
                if (coUpdateStmt != null) coUpdateStmt.close();
            } catch (Exception ex2) {}
        }
         
        return false;
    }
    
}
