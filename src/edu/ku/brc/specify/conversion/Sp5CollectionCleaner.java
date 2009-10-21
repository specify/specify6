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

import static edu.ku.brc.specify.conversion.BasicSQLUtils.query;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.querySingleCol;
import static edu.ku.brc.specify.conversion.BasicSQLUtils.update;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Vector;

import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.dbsupport.DatabaseDriverInfo;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Aug 26, 2009
 *
 */
public class Sp5CollectionCleaner
{
    protected Connection   connection = null;
    protected int           total = 0;
    
    /**
     * 
     */
    public Sp5CollectionCleaner()
    {
        super();
    }

    protected void clean()
    {
        String dbName           = "entosp_dbo"; 
        String itUsername       = "root";
        String itPassword       = "root";
        
        DBConnection colDBConn  = null;
        Statement    stmt       = null;
        try
        {
            
            DatabaseDriverInfo driverInfo = DatabaseDriverInfo.getDriver("MySQL");
            String             connStr    = driverInfo.getConnectionStr(DatabaseDriverInfo.ConnectionType.Open, "localhost", dbName, itUsername, itPassword, driverInfo.getName());
            
            colDBConn  = DBConnection.createInstance(driverInfo.getDriverClassName(), driverInfo.getDialectClassName(), dbName, connStr, itUsername, itPassword);
            connection = colDBConn.createConnection();
            stmt       = connection.createStatement();
            
            // Get Collection Object Types
            String sql = "select DISTINCT CollectionObjectTypeID from collectionobject";
            
            HashSet<Integer> colObjTypeIds = new HashSet<Integer>();
            HashSet<Integer> colObjIds     = new HashSet<Integer>();
            HashSet<Integer> habIds        = new HashSet<Integer>();
            HashSet<Integer> ceIds         = new HashSet<Integer>();
            HashSet<Integer> preps         = new HashSet<Integer>();
            HashSet<Integer> dets          = new HashSet<Integer>();
            HashSet<Integer> locs          = new HashSet<Integer>();
            HashSet<Integer> bas           = new HashSet<Integer>();
            HashSet<Integer> cltrs         = new HashSet<Integer>();
            HashSet<Integer> taxs          = new HashSet<Integer>();
            HashSet<Integer> coCis          = new HashSet<Integer>(); // Col Obj Citations
            
            for (Object idObj : querySingleCol(connection, sql))
            {
                Integer id = (Integer)idObj;
                colObjTypeIds.add(id);
                
                if (id > 8 && id < 20)
                {
                    sql = "SELECT co.CollectionObjectID, ce.CollectingEventID FROM collectionobject AS co Inner Join collectingevent AS ce ON co.CollectingEventID = ce.CollectingEventID WHERE CollectionObjectTypeID = " + id + " LIMIT 0,10";
                    for (Object[] row : query(connection, sql))
                    {
                        colObjIds.add((Integer)row[0]);
                        ceIds.add((Integer)row[1]);
                        System.out.println("Adding co["+((Integer)row[0])+"]   ce["+((Integer)row[1])+"]");
                    }
                }
            }
            
            int count;
            
            sql = "SELECT DISTINCT h.HabitatID, ce.CollectingEventID, co.CollectionObjectID FROM habitat AS h " +
                  "Inner Join collectingevent AS ce ON h.HabitatID = ce.CollectingEventID " +
                  "Inner Join collectionobject AS co ON ce.CollectingEventID = co.CollectingEventID " +
                  "Inner Join determination AS d ON co.CollectionObjectID = d.BiologicalObjectID " +
                  "Inner Join taxonname AS tn ON d.TaxonNameID = tn.TaxonNameID " +
                  "WHERE h.HostTaxonID IS NOT NULL " +
                  "LIMIT 0,20";
            System.out.println("Gathering HBs[\n"+sql+"\n]");
            
            for (Object[] row : query(connection, sql))
            {
                Integer hId = (Integer)row[0];
                Integer ceId = (Integer)row[1];
                Integer coId = (Integer)row[2];
                
                System.out.println("Adding co["+coId+"]   ce["+ceId+"]   hb["+hId+"]");

                
                habIds.add(hId);
                ceIds.add(ceId);
                colObjIds.add(coId);
            }
            
            String ids;
            for (Integer colId : colObjIds)
            {
                sql = "SELECT CollectionObjectID FROM collectionobject WHERE DerivedFromID = " + colId;
                for (Object idObj : querySingleCol(connection, sql))
                {
                    preps.add((Integer)idObj);
                    System.out.println("Adding prep["+((Integer)idObj)+"]");
                }
            }

            boolean doNonTaxon = true;
            if (doNonTaxon)
            {
                String coIds = getIdStrFromSet(colObjIds) + (preps.size() > 0 ? ("," + getIdStrFromSet(preps)) : "");
                sql = "DELETE FROM collectionobject WHERE CollectionObjectID NOT IN (" + coIds + ")";
                
                System.out.println("Deleting COs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] COs");
                
                sql = "DELETE FROM collectionobjectcatalog WHERE CollectionObjectCatalogID NOT IN (" + coIds + ")";
                
                System.out.println("Deleting CCs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] CCs");
                
                
                // Collecting Events
                ids = getIdStrFromSet(ceIds);
                sql = "DELETE FROM collectingevent WHERE CollectingEventID NOT IN (" + ids + ")";
                
                System.out.println("Deleting CEs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] CEs");
                
                // Delete Habitats
                ids = getIdStrFromSet(habIds);
                sql = "DELETE FROM habitat WHERE HabitatID NOT IN (" + ids + ")";
                
                System.out.println("Deleting Habs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] Habs");
                
                // Determinations
                sql = "SELECT d.DeterminationID FROM determination  AS d " + 
                       "Left Join collectionobject  AS co ON d.BiologicalObjectID = co.CollectionObjectID " + 
                       "WHERE co.CollectionObjectID IS NOT NULL ";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    dets.add(id);
                }
                
                ids = getIdStrFromSet(dets);
                sql = "DELETE FROM determination WHERE DeterminationID NOT IN (" + ids + ")";
                
                System.out.println("Deleting DTs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] DTs");
                
                
                // Localities
                sql = "SELECT l.LocalityID, ce.CollectingEventID FROM locality AS l Left Join collectingevent AS ce ON l.LocalityID = ce.LocalityID WHERE ce.CollectingEventID IS NOT NULL";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    locs.add(id);
                }
                
                ids = getIdStrFromSet(locs);
                sql = "DELETE FROM locality WHERE LocalityID NOT IN (" + ids + ")";
                
                System.out.println("Deleting LOCs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] LOCs");
                
                // BiologicalObjectAttributes
                sql = "SELECT ba.BiologicalObjectAttributesID FROM biologicalobjectattributes AS ba Left Join collectionobject AS co ON ba.BiologicalObjectAttributesID = co.CollectionObjectID " +
                      "WHERE co.CollectionObjectID IS NOT NULL";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    bas.add(id);
                }
                
                ids = getIdStrFromSet(bas);
                sql = "DELETE FROM biologicalobjectattributes WHERE BiologicalObjectAttributesID NOT IN (" + ids + ")";
                
                System.out.println("Deleting BAs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] BAs");
                
                // Collectors
                sql = "SELECT c.CollectorsID FROM collectors AS c  Left Join collectingevent AS ce ON c.CollectingEventID = ce.CollectingEventID WHERE ce.CollectingEventID IS NOT NULL";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    cltrs.add(id);
                }
                
                ids = getIdStrFromSet(cltrs);
                sql = "DELETE FROM collectors WHERE CollectorsID NOT IN (" + ids + ")";
                
                System.out.println("Deleting CLs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] CLs");
            }
            
            // CO Citations
            sql = "SELECT ci.CollectionObjectCitationID FROM collectionobjectcitation ci LEFT JOIN collectionobject co ON ci.BiologicalObjectID = co.CollectionObjectID WHERE co.CollectionObjectID IS NULL";
            for (Object idObj : querySingleCol(connection, sql))
            {
                Integer id = (Integer)idObj;
                coCis.add(id);
            }
            
            ids = getIdStrFromSet(coCis);
            sql = "DELETE FROM collectionobjectcitation WHERE CollectionObjectCitationID NOT IN (" + ids + ")";
            
            System.out.println("Deleting CIs[\n"+sql+"\n]");
            count = update(connection, sql);
            total += count;
            System.out.println("Deleted ["+count+"] CIs");
            
            
            boolean doTaxon = false;
            if (doTaxon)
            {
                // Taxon
                sql = "SELECT tn.TaxonNameID FROM habitat AS h Left Join taxonname AS tn ON h.HostTaxonID = tn.TaxonNameID WHERE tn.TaxonNameID IS NOT NULL";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    taxs.add(id);
                }
                
                sql = "SELECT tn.TaxonNameID FROM determination AS d Left Join taxonname tn ON d.TaxonNameID = tn.TaxonNameID WHERE tn.TaxonNameID IS NOT NULL";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    taxs.add(id);
                }
                
                ids = getIdStrFromSet(taxs);
                sql = "SELECT tn.AcceptedID FROM taxonname AS tn Inner Join taxonname AS tn2 ON tn.AcceptedID = tn2.TaxonNameID WHERE tn2.TaxonNameID IN (" + ids + ")";
                for (Object idObj : querySingleCol(connection, sql))
                {
                    Integer id = (Integer)idObj;
                    taxs.add(id);
                }
                
                ids = getIdStrFromSet(taxs);
                sql = "DELETE FROM taxonname WHERE RankID = 220 AND TaxonNameID NOT IN (" + ids + ")";
                
                System.out.println("Deleting TXs[\n"+sql+"\n]");
                count = update(connection, sql);
                total += count;
                System.out.println("Deleted ["+count+"] TXs");
            }
            
            boolean doTaxon2 = false;
            if (doTaxon2)
            {
                int cnt = 0;
                Vector<Object> idsToDel = querySingleCol(connection, "SELECT tn1.TaxonNameID FROM taxonname  AS tn1 LEFT JOIN taxonname tn2 ON tn1.ParentTaxonNameID = tn2.TaxonNameID WHERE tn1.RankID > 220 AND tn2.TaxonNameID IS NULL");
                for (Object obj : idsToDel)
                {
                    if (!taxs.contains(obj))
                    {
                        update(connection, "DELETE FROM taxonname WHERE TaxonNameID = " + obj);
                        if (cnt % 1000 == 0)
                        {
                            System.out.println((int)((cnt* 100.0) / idsToDel.size()));
                        }
                    }
                    cnt++;
                }
                
                cnt = 0;
                idsToDel = querySingleCol(connection, "SELECT tn1.TaxonNameID FROM taxonname  AS tn1 LEFT JOIN taxonname tn2 ON tn1.AcceptedID = tn2.TaxonNameID WHERE tn2.TaxonNameID IS NULL");
                for (Object obj : idsToDel)
                {
                    if (!taxs.contains(obj))
                    {
                        update(connection, "DELETE FROM taxonname WHERE TaxonNameID = " + obj);
                        if (cnt % 1000 == 0)
                        {
                            System.out.println((int)((cnt* 100.0) / idsToDel.size()));
                        }
                    }
                    cnt++;
                }
            }
             //-----------------------------------------
            
            stmt.close();
            colDBConn.close();
            
            System.out.println("Done " + total);
            
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    protected String getIdStrFromSet(final HashSet<Integer> set)
    {
        StringBuilder sb = new StringBuilder();
        for (Integer id : set)
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        return sb.toString();
    }
    
    
    
    public static void main(String[] args)
    {
        Sp5CollectionCleaner clearer = new Sp5CollectionCleaner();
        clearer.clean();
    }
}
