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
package edu.ku.brc.specify.conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;


/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 15, 2009
 *
 */
public class GulfInvertsFixer
{
    protected static final Logger log = Logger.getLogger(GulfInvertsFixer.class);
    
    protected static SimpleDateFormat        dateTimeFormatter      = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    protected static SimpleDateFormat        dateFormatter          = new SimpleDateFormat("yyyy-MM-dd");
    protected static Timestamp               now                    = new Timestamp(System .currentTimeMillis());
    protected static String                  nowStr                 = dateTimeFormatter.format(now);
    
    protected Connection                     oldDBConn;
    protected Connection                     newDBConn;
    protected String                         oldDBName;
    protected TableWriter   tblWriter;
    
    public GulfInvertsFixer(final Connection oldDBConn, 
                            final Connection newDBConn,
                            final String     oldDBName,
                            final TableWriter tblWriter)
    {
        super();
        this.oldDBConn = oldDBConn;
        this.newDBConn = newDBConn;
        this.oldDBName = oldDBName;
        this.tblWriter = tblWriter;
    }
    
    /**
     * @param collectionMemberID
     */
    protected void convert(final int collectionMemberID)
    {
        PreparedStatement pStmtAdd = null;
        PreparedStatement pStmtUpd = null;
        PreparedStatement pStmtCEUpd = null;
        Statement         stmt  = null;
        
        IdMapperIFace ceMapper = IdMapperMgr.getInstance().get("collectingevent", "CollectingEventID");
        IdMapperIFace hbMapper = IdMapperMgr.getInstance().get("habitat", "HabitatID");
        
        
        try
        {
            
            String sql = "SELECT cc.CatalogNumber,ce.CollectingEventID,h.HabitatID,s.StratigraphyID,s.Remarks,s.Text1,s.Text2,s.YesNo1 FROM collectionobject AS co "+
                            "Inner Join collectionobjectcatalog AS cc ON cc.CollectionObjectCatalogID = co.CollectionObjectID "+
                            "Inner Join collectingevent AS ce ON co.CollectingEventID = ce.CollectingEventID "+
                            "Left Join habitat AS h ON ce.CollectingEventID = h.HabitatID "+
                            "Left Join stratigraphy AS s ON ce.CollectingEventID = s.StratigraphyID "+
                            "WHERE s.StratigraphyID IS NOT NULL ORDER BY ce.CollectingEventID ASC";
            
            log.debug(sql);
            
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            
            Timestamp ts = new Timestamp(Calendar.getInstance().getTime().getTime());
            
            stmt       = oldDBConn.createStatement();
            pStmtUpd   = newDBConn.prepareStatement("UPDATE collectingeventattribute SET Text4=?, Text5=?, Text6=?, YesNo1=? WHERE CollectingEventAttributeID = ?");
            pStmtCEUpd = newDBConn.prepareStatement("UPDATE collectingevent SET CollectingEventAttributeID=? WHERE CollectingEventID = ?");
            pStmtAdd   = newDBConn.prepareStatement("INSERT INTO collectingeventattribute (Text4, Text5, Text6, YesNo1, TimestampCreated, TimestampModified, Version, CollectionMemberID) VALUES(?,?,?,?,?,?,?,?)");
            
            int counterAdd = 0;
            int counterUpd = 0;
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next())
            {
                Integer ceId   = rs.getInt(2);
                Integer hbId   = rs.getInt(3);
                
                String remarks = rs.getString(5);
                String text1   = rs.getString(6);
                String text2   = rs.getString(7);
                boolean yesNo1 = rs.getBoolean(8);
                
                Integer newCEID = ceMapper.get(ceId);
                Integer newHBID = hbMapper.get(hbId);
                
                if (newHBID != null)
                {
                    pStmtUpd.setString(1, remarks);
                    pStmtUpd.setString(2, text1);
                    pStmtUpd.setString(3, text2);
                    pStmtUpd.setBoolean(4, yesNo1);
                    pStmtUpd.setInt(5, newHBID);
                    pStmtUpd.execute();
                    counterUpd++;
                    
                } else
                {
                    pStmtAdd.setString(1, remarks);
                    pStmtAdd.setString(2, text1);
                    pStmtAdd.setString(3, text2);
                    pStmtAdd.setBoolean(4, yesNo1);
                    pStmtAdd.setTimestamp(5, ts);
                    pStmtAdd.setTimestamp(6, ts);
                    pStmtAdd.setInt(7, 0);
                    pStmtAdd.setInt(8, collectionMemberID);
                    pStmtAdd.execute();
                    newHBID = BasicSQLUtils.getInsertedId(pStmtAdd);
                   
                    pStmtCEUpd.setInt(1, newHBID);
                    pStmtCEUpd.setInt(2, newCEID);
                    pStmtCEUpd.execute();
                    counterAdd++;
                }
                 
               
            }
            rs.close();
            
            log.debug(String.format("%d updated  %d inserted", counterUpd, counterAdd));
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        } finally
        {
            try
            {
                stmt.close();
                pStmtUpd.close();
                pStmtCEUpd.close();
                pStmtAdd.close();
                
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    
    public static void main(String[] args)
    {
        //GulfInvertsFixer gc = new GulfInvertsFixer();
        //gc.convert();
    }
}
