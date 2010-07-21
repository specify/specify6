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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Random;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.DBMSUserMgr;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;

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
    
    protected static Random generator = new Random( System.currentTimeMillis() );
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param disciplineID
     * @return
     */
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
                pStmt2.setInt(8, 0);
                
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
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param disciplineID
     */
    public static void convertMethodFromStratGTP(final Connection oldDBConn, final Connection newDBConn)
    {
        String  sql          = null;
        Session localSession = null;
        try
        {
            localSession = HibernateUtil.getCurrentSession();
            HibernateUtil.beginTransaction();
            
            // Query to Create PickList
            sql = "SELECT gtp.Name, CONCAT(gtp.Name,' - ', gtp.Standard) as Method FROM collectingevent AS ce " +
                    "Inner Join stratigraphy AS s ON ce.CollectingEventID = s.StratigraphyID " +
                    "Inner Join geologictimeperiod AS gtp ON s.GeologicTimePeriodID = gtp.GeologicTimePeriodID " +
                    "GROUP BY gtp.Name";
            
            PickList pl = (PickList)localSession.createQuery("FROM PickList WHERE Name = 'CollectingMethod'").list().get(0);
            if (pl == null)
            {
                log.error("Couldn't find CollectingMethod.");
            }
            
            for (PickListItem pli : new Vector<PickListItem>(pl.getPickListItems()))
            {
                log.debug("Removing["+pli.getTitle()+"]");
                localSession.delete(pli);
                pl.getPickListItems().remove(pli);
            }
            localSession.saveOrUpdate(pl);
            
            HibernateUtil.commitTransaction();
            
            HibernateUtil.beginTransaction();
            Vector<Object[]> list = BasicSQLUtils.query(oldDBConn, sql);
            for (Object[] cols : list)
            {
                PickListItem pli = new PickListItem();
                pli.initialize();
                
                pli.setTitle(cols[1].toString());
                pli.setValue(cols[0].toString());
                
                pl.getPickListItems().add(pli);
                pli.setPickList(pl);
                localSession.saveOrUpdate(pli);
            }
            
            localSession.saveOrUpdate(pl);
            
            HibernateUtil.commitTransaction();
            
            
            // Query for processing data
            sql = "SELECT ce.CollectingEventID, gtp.Name FROM collectingevent AS ce " +
                    "Inner Join stratigraphy AS s ON ce.CollectingEventID = s.StratigraphyID " +
                    "Inner Join geologictimeperiod AS gtp ON s.GeologicTimePeriodID = gtp.GeologicTimePeriodID " +
                    "ORDER BY ce.CollectingEventID ASC";
            
            IdMapperMgr.getInstance().setDBs(oldDBConn, newDBConn);
            IdMapperIFace mapper = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", false);

            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE collectingevent SET Method=? WHERE CollectingEventID=?");
            Statement         stmt  = oldDBConn.createStatement();
            ResultSet         rs    = stmt.executeQuery(sql);
            while (rs.next())
            {
                Integer newId = mapper.get(rs.getInt(1));
                pStmt.setString(1, rs.getString(2));
                pStmt.setInt(2, newId);
                pStmt.executeUpdate();
            }
            rs.close();
            stmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
            HibernateUtil.rollbackTransaction();
        }
    }
    
    /**
     * @param oldDBConn
     * @param newDBConn
     */
    public static void moveStratFieldsToCEA(final Connection oldDBConn, final Connection newDBConn)
    {
        String  sql = null;
        try
        {
            IdMapperIFace ceMapper = IdMapperMgr.getInstance().addTableMapper("collectingevent", "CollectingEventID", false);
            
            String postFix = " FROM collectingevent ce Inner Join collectingeventattribute AS cea ON ce.CollectingEventAttributeID = cea.CollectingEventAttributeID ";

            /*
                Specify 5 Field ----------> Specify 6 Field
                Stratigraphy.superGroup --> CEA.text3
                Stratigraphy.group      --> CEA.text4
                Stratigraphy.formation  --> CEA.text5
                Stratigraphy.text1      --> CEA.text1
                Stratigraphy.number1    --> CEA.number1
                Stratigraphy.text2      --> CEA.text2
             */
            
            Timestamp now = new Timestamp(System .currentTimeMillis());
            PreparedStatement pStmt = newDBConn.prepareStatement("UPDATE collectingeventattribute SET Text1=?, Text2=?, Text3=?, Text4=?, Text5=?, Number1=? WHERE CollectingEventAttributeID=?");
            
            PreparedStatement pStmt2 = newDBConn.prepareStatement("INSERT INTO collectingeventattribute SET Text1=?, Text2=?, Text3=?, Text4=?, Text5=?, Number1=?, Version=0, DisciplineID=?, TimestampCreated=?, TimestampModified=?");
            PreparedStatement pStmt3 = newDBConn.prepareStatement("UPDATE collectingevent SET CollectingEventAttributeID=? WHERE CollectingEventID=?");
            
            int cnt = 0;
            // Query to Create PickList
            sql = "SELECT StratigraphyID, Text1, Text2, SuperGroup, `Group`, Formation, Number1 FROM stratigraphy";
            for (Object[] row : BasicSQLUtils.query(oldDBConn, sql))
            {
                Integer id      = (Integer)row[0];
                Integer newCEId = ceMapper.get(id);
                if (newCEId != null)
                {
                    Vector<Object[]> colList = BasicSQLUtils.query("SELECT DisciplineID, CollectingEventAttributeID FROM collectingevent WHERE CollectingEventID = "+ newCEId);
                    Object[]         cols    = colList.get(0);
                    
                    if (cols[1] != null)
                    {
                        pStmt.setString(1, (String)row[1]);
                        pStmt.setString(2, (String)row[2]);
                        pStmt.setString(3, (String)row[3]);
                        pStmt.setString(4, (String)row[4]);
                        pStmt.setString(5, (String)row[5]);
                        pStmt.setString(6, (String)row[6]);
                        pStmt.setInt(7,    newCEId);
                        
                        int rv = pStmt.executeUpdate();
                        if (rv != 1)
                        {
                            log.error(String.format("Error updating CEA New Id %d  Old: %d  rv: %d", newCEId, id, rv));
                        }
                    } else
                    {
                        Integer disciplineID = (Integer)cols[0];
                        pStmt2.setString(1, (String)row[1]);
                        pStmt2.setString(2, (String)row[2]);
                        pStmt2.setString(3, (String)row[3]);
                        pStmt2.setString(4, (String)row[4]);
                        pStmt2.setString(5, (String)row[5]);
                        pStmt2.setString(6, (String)row[6]);
                        pStmt2.setInt(7, disciplineID);
                        pStmt2.setTimestamp(8, now);
                        pStmt2.setTimestamp(9, now);
                        
                        int rv = pStmt2.executeUpdate();
                        if (rv == 1)
                        {
                            Integer newCEAId = BasicSQLUtils.getInsertedId(pStmt2);
                            if (newCEAId != null)
                            {
                                pStmt3.setInt(1,    newCEAId);
                                pStmt3.setInt(2,    newCEId);
                                rv = pStmt3.executeUpdate();
                                if (rv != 1)
                                {
                                    log.error(String.format("Error updating CEA New Id %d To CE ID: %d", newCEAId, newCEId));
                                }
                            } else
                            {
                                log.error("Couldn't get inserted CEAId");
                            }
                            
                        } else
                        {
                            log.error(String.format("Error updating CEA New Id %d  Old: %d  rv: %d", newCEId, id, rv));
                        } 
                    }
                } else
                {
                    log.error(String.format("No Map for Old CE Id %d", id));
                }
                cnt++;
                if (cnt % 500 == 0)
                {
                    log.debug("Count "+ cnt);
                }
            }
            log.debug("Count "+ cnt);
            pStmt.close();
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param disciplineID
     */
    public static int getNewRecId(final Connection oldDBConn, final String tblName, String idName)
    {
        do
        {
           int id = generator.nextInt();
           if (BasicSQLUtils.getCountAsInt(oldDBConn, "SELECT COUNT(*) FROM "+tblName+ " WHERE " + idName + " = " + id) < 1)
           {
               return id;
           }
        } while(true);
    }
    
    /**
     * @param oldDBConn
     * @param newDBConn
     * @param disciplineID
     */
    public static boolean moveHabitatToStratSp5(final Connection oldDBConn)
    {
        PreparedStatement pStmt1 = null;
        try
        {
            String sqlCreate = "CREATE TABLE `stratigraphy2` (  `StratigraphyID` int(10) NOT NULL,  `GeologicTimePeriodID` int(10) DEFAULT NULL,  `SuperGroup` varchar(50) CHARACTER SET utf8 DEFAULT NULL,  `Group` varchar(50) CHARACTER SET utf8 DEFAULT NULL,  `Formation` varchar(50) CHARACTER SET utf8 DEFAULT NULL, " + 
                              "`Member` varchar(50) CHARACTER SET utf8 DEFAULT NULL,  `Bed` varchar(50) CHARACTER SET utf8 DEFAULT NULL,  `Remarks` longtext,  `Text1` varchar(300) CHARACTER SET utf8 DEFAULT NULL,  `Text2` varchar(300) CHARACTER SET utf8 DEFAULT NULL,  `Number1` double DEFAULT NULL, " +
                              "`Number2` double DEFAULT NULL,  `TimestampCreated` datetime DEFAULT NULL,  `TimestampModified` datetime DEFAULT NULL,  `LastEditedBy` varchar(50) CHARACTER SET utf8 DEFAULT NULL,  `YesNo1` smallint(5) DEFAULT NULL,  `YesNo2` smallint(5) DEFAULT NULL,  PRIMARY KEY (`StratigraphyID`) " +
                              //, "KEY `IX_XXXXXX` (`GeologicTimePeriodID`), " +
                              //"CONSTRAINT `FK_Stratigraphy_CollectingEvent` FOREIGN KEY (`StratigraphyID`) REFERENCES `collectingevent` (`CollectingEventID`) ON DELETE CASCADE ON UPDATE NO ACTION, " +
                              //"CONSTRAINT `FK_Stratigraphy_GeologicTimePeriod` FOREIGN KEY (`GeologicTimePeriodID`) REFERENCES `geologictimeperiod` (`GeologicTimePeriodID`) ON DELETE NO ACTION ON UPDATE NO ACTION " +
                              ") ENGINE=InnoDB DEFAULT CHARSET=latin1;";
            
            DBMSUserMgr dbMgr = DBMSUserMgr.getInstance();
            dbMgr.setConnection(oldDBConn);
            if (dbMgr.doesDBHaveTable("stratigraphy2"))
            {
                try
                {
                    BasicSQLUtils.update(oldDBConn, "DROP TABLE stratigraphy2");
                } catch (Exception ex) {}
            }
            
            BasicSQLUtils.update(oldDBConn, sqlCreate);

            String post = " FROM collectingevent AS ce " +
                            "Left Join habitat AS h ON ce.CollectingEventID = h.HabitatID " +
                            "Left Join stratigraphy AS s ON ce.CollectingEventID = s.StratigraphyID " +
                            "WHERE h.Text1 IS NOT NULL OR h.Text2 IS NOT NULL OR h.Text3 IS NOT NULL OR h.Text4 IS NOT NULL OR h.Text5 IS NOT NULL";


            String sql = "SELECT ce.CollectingEventID, h.HabitatID, s.StratigraphyID, h.Text1, h.Text2, h.Text3, h.Text4, h.Text5, h.TimestampCreated, h.TimestampModified " +post;
            
            String cntSQL = "SELECT COUNT(*) " + post;
            int    habCnt = BasicSQLUtils.getCountAsInt(oldDBConn, cntSQL);
            
            log.debug("****** Startigraphy Count: "+habCnt);

            //Timestamp now = new Timestamp(System .currentTimeMillis());
            pStmt1 = oldDBConn.prepareStatement("INSERT INTO stratigraphy2 (StratigraphyID, SuperGroup, `Group`, Formation, Member, Bed, TimestampCreated, TimestampModified) VALUES(?,?,?,?,?,?,?,?)");
            
            int cnt = 0;
            Vector<Object[]> rows = BasicSQLUtils.query(oldDBConn, sql);
            for (Object[] row : rows)
            {
                Integer   ceID      = (Integer)row[0];
                Integer   hbID      = (Integer)row[1];
                Integer   stID      = (Integer)row[2];
                String    superGrp  = (String)row[3];
                String    group     = (String)row[4];
                String    formation = (String)row[5];
                String    member    = (String)row[6];
                String    bed       = (String)row[7];
                Timestamp crTS      = (Timestamp)row[8];      
                Timestamp mdTS      = (Timestamp)row[9];      
                
                if (StringUtils.isNotEmpty(superGrp))
                {
                   if (superGrp.length() > 50)   
                   {
                       superGrp = superGrp.substring(0, 50);
                   }
                }
                if (StringUtils.isNotEmpty(bed))
                {
                   if (bed.length() > 50)   
                   {
                       bed = bed.substring(0, 50);
                   }
                }
                //if (hbID != null && stID == null)
                if (ceID != null)
                {
                    pStmt1.setInt(1, ceID);//getNewRecId(oldDBConn, "stratigraphy", "StratigraphyID"));
                    pStmt1.setString(2, superGrp);
                    pStmt1.setString(3, group);
                    pStmt1.setString(4, formation);
                    pStmt1.setString(5, member);
                    pStmt1.setString(6, bed);
                    pStmt1.setTimestamp(7, crTS);
                    pStmt1.setTimestamp(8, mdTS);
                    pStmt1.execute();
                    cnt++;
                    if (cnt % 100 == 0)
                    {
                        log.debug(cnt + " / " + habCnt);
                    }
                }
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
                
            } catch (Exception ex) {}
        }
        
        return false;
    }

}
