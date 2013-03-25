/* Copyright (C) 2012, University of Kansas Center for Research
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
package edu.ku.brc.specify.tasks.subpane.images;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBFieldInfo;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.af.ui.forms.formatters.UIFieldFormatterIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.services.geolocate.prototype.Locality;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
import edu.ku.brc.specify.datamodel.Attachment;
import edu.ku.brc.specify.datamodel.Borrow;
import edu.ku.brc.specify.datamodel.BorrowAttachment;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectingEventAttachment;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.CollectionObjectAttachment;
import edu.ku.brc.specify.datamodel.ConservDescription;
import edu.ku.brc.specify.datamodel.ConservDescriptionAttachment;
import edu.ku.brc.specify.datamodel.ConservEvent;
import edu.ku.brc.specify.datamodel.ConservEventAttachment;
import edu.ku.brc.specify.datamodel.DNASequence;
import edu.ku.brc.specify.datamodel.DNASequenceAttachment;
import edu.ku.brc.specify.datamodel.DNASequencingRun;
import edu.ku.brc.specify.datamodel.DNASequencingRunAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebook;
import edu.ku.brc.specify.datamodel.FieldNotebookAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPage;
import edu.ku.brc.specify.datamodel.FieldNotebookPageAttachment;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSet;
import edu.ku.brc.specify.datamodel.FieldNotebookPageSetAttachment;
import edu.ku.brc.specify.datamodel.Gift;
import edu.ku.brc.specify.datamodel.GiftAttachment;
import edu.ku.brc.specify.datamodel.Loan;
import edu.ku.brc.specify.datamodel.LoanAttachment;
import edu.ku.brc.specify.datamodel.LocalityAttachment;
import edu.ku.brc.specify.datamodel.Permit;
import edu.ku.brc.specify.datamodel.PermitAttachment;
import edu.ku.brc.specify.datamodel.Preparation;
import edu.ku.brc.specify.datamodel.PreparationAttachment;
import edu.ku.brc.specify.datamodel.ReferenceWork;
import edu.ku.brc.specify.datamodel.ReferenceWorkAttachment;
import edu.ku.brc.specify.datamodel.RepositoryAgreement;
import edu.ku.brc.specify.datamodel.RepositoryAgreementAttachment;
import edu.ku.brc.specify.datamodel.Taxon;
import edu.ku.brc.specify.datamodel.TaxonAttachment;
import edu.ku.brc.specify.datamodel.busrules.AgentBusRules;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Sep 5, 2012
 *
 */
public class CollectionDataFetcher
{
    private static final Logger log = Logger.getLogger(CollectionDataFetcher.class);
    private static final String AGENT_TYPE = "AgentType";
    
    private static String[] colObjColumnNames = {"CatalogNumber", "StartDate", "StationFieldNumber","LocalityName","Latitude1","Longitude1","GeoName", "TaxName", "OrigFilename",};
    private static String[] agentColumnNames  = {AGENT_TYPE, "LastName", "FirstName", "MiddleInitial"};

    private Connection conn = DBConnection.getInstance().getConnection();
    private static HashMap<Class<?>, Class<?>> clsHashMap = new HashMap<Class<?>, Class<?>>();
    private static Class<?>[] attachmentClassesForMap;
    private static Class<?>[] attachmentClasses;
    
    private HashMap<Integer, List<BubbleDisplayInfo>> bciHash = new HashMap<Integer, List<BubbleDisplayInfo>>();

    private boolean isEmbeded;
    
    static 
    {
        CollectionDataFetcher.attachmentClassesForMap = new Class<?>[] {
                CollectingEvent.class,    CollectingEventAttachment.class,
                CollectionObject.class,   CollectionObjectAttachment.class,
                Locality.class,           LocalityAttachment.class,
                Preparation.class,        PreparationAttachment.class,
                Taxon.class,              TaxonAttachment.class,
                Accession.class,          AccessionAttachment.class,
                Agent.class,              AgentAttachment.class,
                Borrow.class,             BorrowAttachment.class,
                ConservDescription.class, ConservDescriptionAttachment.class,
                ConservEvent.class,       ConservEventAttachment.class,
                DNASequence.class,        DNASequenceAttachment.class,
                DNASequencingRun.class,   DNASequencingRunAttachment.class,
                FieldNotebook.class,      FieldNotebookAttachment.class,
                FieldNotebookPage.class,  FieldNotebookPageAttachment.class,
                FieldNotebookPageSet.class, FieldNotebookPageSetAttachment.class,
                Gift.class,               GiftAttachment.class,
                Loan.class,               LoanAttachment.class,
                Permit.class,             PermitAttachment.class,
                ReferenceWork.class,      ReferenceWorkAttachment.class,
                RepositoryAgreement.class, RepositoryAgreementAttachment.class,
            };
        CollectionDataFetcher.attachmentClasses = new Class<?>[CollectionDataFetcher.attachmentClassesForMap.length/2];
        for (int i=0;i<CollectionDataFetcher.attachmentClassesForMap.length;i+=2)
        {
            CollectionDataFetcher.attachmentClasses[i/2] = CollectionDataFetcher.attachmentClassesForMap[i];
            //System.out.println(String.format("[%s][%s] %d / %d", attachmentClasses[i/2].getSimpleName(), attachmentClassesForMap[i].getSimpleName(), i/2, i));
        }
        
        for (int i=0;i<attachmentClasses.length;i++)
        {
            clsHashMap.put(attachmentClassesForMap[i], attachmentClassesForMap[i+1]);
            //System.out.println(String.format("[%s][%s]", attachmentClassesForMap[i].getSimpleName(), attachmentClassesForMap[i+1].getSimpleName()));
            i++;
        }
    }
    
    /**
     * 
     */
    public CollectionDataFetcher()
    {
        super();

        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        isEmbeded  = collection.getIsEmbeddedCollectingEvent();
    }
    
    /**
     * @return the clsHashMap
     */
    public static HashMap<Class<?>, Class<?>> getAttachmentClassMap()
    {
        return clsHashMap;
    }

    /**
     * @return the attachmentClasses
     */
    public static Class<?>[] getAttachmentClasses()
    {
        return attachmentClasses;
    }

    /**
     * @param ti
     * @return
     */
    private String getColObjColumns(final DBTableInfo ti)
    {
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        boolean    isEmbeded  = collection.getIsEmbeddedCollectingEvent();
        
        String fldNum = isEmbeded ? ",co.FieldNumber" : ",ce.StationFieldNumber";
        
        StringBuilder fields = new StringBuilder("att.OrigFilename,ce.StartDate,loc.LocalityName,loc.Latitude1,loc.Longitude1,geo.FullName AS GeoName"); 
        fields.append(fldNum);
        
        if (ti.getTableId() == CollectionObject.getClassTableId())
        {
            fields.append(",tx.FullName AS TaxName,co.CatalogNumber,co.CollectionObjectID");

        } else
        {
            fields.append(String.format(",%s.%s", ti.getAbbrev(), ti.getIdColumnName()));
        }
        
        return fields.toString();
    }
    
    /**
     * @param ti
     * @return
     */
    private List<BubbleDisplayInfo> initBubbleDisplayInfo(final DBTableInfo ti, final String[] names)
    {
        List<BubbleDisplayInfo> bcis = bciHash.get(ti.getTableId());
        if (bcis != null)
        {
            return bcis;
        }
        
        bcis = new ArrayList<BubbleDisplayInfo>();
        
        for (DBFieldInfo fi : ti.getFields())
        {
            if (!fi.isHidden())
            {
                bcis.add(new BubbleDisplayInfo(ti.getTableId(), fi.getColumn(), fi.getTitle(), fi.getFormatter(), ti));
            }
        }
        
        bcis = arrangeDBIList(bcis, names);
        bciHash.put(ti.getTableId(), bcis);
        
        return bcis;
    }
    
    /**
     * @param ti
     * @return
     */
    private String getSelectFields(final DBTableInfo ti)
    {
        StringBuilder sb = new StringBuilder();
        for (BubbleDisplayInfo bdi : getBubbleDisplayInfo(ti.getTableId()))
        {
            if (sb.length() > 0) sb.append(',');
            sb.append(ti.getAbbrev());
            sb.append('.');
            sb.append(bdi.getColumnName());
        }
        return sb.toString();
    }
    
    /**
     * @param rs
     * @param tableId
     * @return
     * @throws SQLException 
     */
    private Map<String, Object> readDataIntoMap(final ResultSet rs, final int tableId) throws SQLException
    {
        List<BubbleDisplayInfo> displayInfos = bciHash.get(tableId);
        TreeMap<String, Object> dataMap      = new TreeMap<String, Object>();
        if (rs != null)
        {
            ResultSetMetaData rsmd = rs.getMetaData();
            
            if (rs.next())
            {
                for (int i=1;i<rsmd.getColumnCount();i++)
                {
                    BubbleDisplayInfo bdi = displayInfos.get(i-1);
                    Object            val = rs.getObject(i);
                    
                    if (bdi.getColTblId() == Attachment.getClassTableId() &&
                        bdi.getColumnName().equals("OrigFilename"))
                    {
                        val = FilenameUtils.getName(val.toString());
                    }
                    
                    if (bdi.getFormatter() != null)
                    {
                        val = bdi.getFormatter().formatToUI(val);
                    }
                    dataMap.put(rsmd.getColumnLabel(i), val);
                }
                System.out.println(rs.getObject(rsmd.getColumnCount()));
                dataMap.put("Id", rs.getObject(rsmd.getColumnCount()));
            }
            rs.close();
        }
        return dataMap;
    }
    
    /**
     * @param attachmentID
     * @param ti
     * @param joinStr1
     * @param joinStr2
     * @param stmt
     * @return
     * @throws SQLException
     */
    private ResultSet queryGenerically(final int attachmentID,
                                       final DBTableInfo ti, 
                                       final String joinStr1,
                                       final String joinStr2,
                                       final Statement stmt) throws SQLException
    {
        
        StringBuilder sqlSB   = new StringBuilder("SELECT ");
        String        columns = getSelectFields(ti);
        sqlSB.append(columns);
        sqlSB.append(',');
        sqlSB.append(ti.getAbbrev());
        sqlSB.append('.');
        sqlSB.append(ti.getIdColumnName());

        
        sqlSB.append(" FROM attachment att ");
        sqlSB.append(joinStr1);
        sqlSB.append(joinStr2);
        sqlSB.append("WHERE att.AttachmentID=");
        sqlSB.append(attachmentID);
        
        log.debug(sqlSB.toString());
        
        return stmt.executeQuery(sqlSB.toString());
    }
    
    /**
     * @param map
     * @param names
     * @return
     */
//    private TreeMap<String, Object> arrangeMap(final Map<String, Object> map, final String[] names)
//    {
//        TreeMap<String, Object> treeMap = new TreeMap<String, Object>();
//        for (String nm : names)
//        {
//            Object val = map.get(nm);
//            if (val != null)
//            {
//                treeMap.put(nm, val);
//            }
//        }
//        
//        HashSet<String> nameSet = new HashSet<String>();
//        Collections.addAll(nameSet, names);
//        
//        for (String nm : map.keySet())
//        {
//            if (!nameSet.contains(nm))
//            {
//                Object val = map.get(nm);
//                if (val != null)
//                {
//                    treeMap.put(nm, val);
//                }
//            }
//        }
//        return treeMap;
//    }
    
    /**
     * @param list
     * @param names
     * @return
     */
    private List<BubbleDisplayInfo> arrangeDBIList(final List<BubbleDisplayInfo> list, final String[] names)
    {
        if (names != null)
        {
            HashSet<String> nameSet = new HashSet<String>();
            Collections.addAll(nameSet, names);
    
            HashMap<String, BubbleDisplayInfo> hashMap = new HashMap<String, BubbleDisplayInfo>();
            for (BubbleDisplayInfo bdi : list)
            {
                hashMap.put(bdi.getColumnName(), bdi);
            }
    
            ArrayList<BubbleDisplayInfo> orderedList = new ArrayList<BubbleDisplayInfo>();
            for (String nm : names)
            {
                BubbleDisplayInfo bdi = hashMap.get(nm);
                if (bdi != null)
                {
                    orderedList.add(bdi);
                }
            }
            
            for (BubbleDisplayInfo bdi : list)
            {
                if (!nameSet.contains(bdi.getColumnName()))
                {
                    orderedList.add(bdi);
                }
            }
            return orderedList;
        }
        return list;
    }
    
    /**
     * @param attachmentID
     * @param ti
     * @param joinStr1
     * @param joinStr2
     * @param stmt
     * @return
     * @throws SQLException
     */
    private Map<String, Object> queryAgent(final int attachmentID,
                                           final DBTableInfo ti, 
                                           final String joinStr1,
                                           final String joinStr2,
                                           final Statement stmt) throws SQLException
    {
        ResultSet rs = queryGenerically(attachmentID, ti, joinStr1, joinStr2, stmt);
        if (rs != null)
        {
            Map<String, Object> map = readDataIntoMap(rs, Agent.getClassTableId());
            Object agentType = map.get(AGENT_TYPE);
            if (agentType != null)
            {
                String[] agentTitles = AgentBusRules.getTypeTitle();
                int inx = Integer.parseInt(agentType.toString());
                if (inx > 0 && inx < agentTitles.length)
                {
                    map.put(AGENT_TYPE, agentTitles[inx]);
                }
            }
            return map;
        }
        return null;
    }

    /**
     * @return
     */
    private List<BubbleDisplayInfo> initColObjDisplayInfo()
    {
        int[]    colTblIds  = new int[]    {        1,           10,               10,             2,             2,          2,          3,          4,           41,  };
                                        // {"CatalogNumber", "StartDate", "StationFieldNumber","LocalityName","Latitude1","Longitude1","GeoName", "TaxName", "OrigFilename", };
        
        if (isEmbeded)
        {
            colTblIds[3]  = 1;
            colObjColumnNames[3] = "FieldNumber";
        }
        
        ArrayList<BubbleDisplayInfo> displayColInfos = new ArrayList<BubbleDisplayInfo>();
        
        for (int i=0;i<colObjColumnNames.length-2;i++)
        {
            String fldName = colObjColumnNames[i].equals("GeoName") || colObjColumnNames[i].equals("TaxName") ? "FullName" : colObjColumnNames[i];
            
            DBTableInfo ti   = DBTableIdMgr.getInstance().getInfoById(colTblIds[i]);
            String     label = DBTableIdMgr.getInstance().getTitleForField(colTblIds[i], fldName);
            UIFieldFormatterIFace formatter  = DBTableIdMgr.getFieldFormatterFor(ti.getClassObj(), fldName);
            
            displayColInfos.add(new BubbleDisplayInfo(colTblIds[i], colObjColumnNames[i], label, formatter, ti));
        }
        
        int inx = colObjColumnNames.length-2;
        String label = DBTableIdMgr.getInstance().getTitleForId(colTblIds[inx]);
        displayColInfos.add(new BubbleDisplayInfo(colTblIds[inx], colObjColumnNames[inx], label));
        
        inx++;
        label = DBTableIdMgr.getInstance().getTitleForId(colTblIds[inx]);
        displayColInfos.add(new BubbleDisplayInfo(colTblIds[inx], colObjColumnNames[inx], label));
        
        arrangeDBIList(displayColInfos, colObjColumnNames);
        
        bciHash.put(CollectionObject.getClassTableId(), displayColInfos);
        
        return displayColInfos;
    }

    
    /**
     * @param attachmentID
     * @param ti
     * @param joinStr1
     * @param joinStr2
     * @param stmt
     * @return
     * @throws SQLException
     */
    private Map<String, Object> queryColObj(final int attachmentID,
                                            final DBTableInfo ti, 
                                            final String joinStr1,
                                            final String joinStr2,
                                            final Statement stmt) throws SQLException
    {
        
        StringBuilder preSB = new StringBuilder("SELECT ");
        preSB.append(getColObjColumns(ti));
        
        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append(" FROM attachment att ");
        sqlSB.append(joinStr1);
        sqlSB.append(joinStr2);
        sqlSB.append("LEFT JOIN determination det ON co.CollectionObjectID = det.CollectionObjectID ");
        sqlSB.append("LEFT JOIN taxon tx ON det.TaxonID = tx.TaxonID ");
        sqlSB.append("LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID ");
        
        sqlSB.append("LEFT JOIN locality loc ON ce.LocalityID = loc.LocalityID ");
        sqlSB.append("LEFT JOIN geography geo ON loc.GeographyID = geo.GeographyID ");
        sqlSB.append("WHERE att.AttachmentID=");
        sqlSB.append(attachmentID);
        
        String detWHERE = " AND det.IsCurrent <> 0";
        
        int currCnt = BasicSQLUtils.getCountAsInt("SELECT COUNT(*) " + sqlSB.toString() + detWHERE); 
        if (currCnt > 0)
        {
            sqlSB.append(" AND (det.IsCurrent <> 0 OR det.DeterminationID IS NULL)");
        }
        
        String sql = preSB.toString() + sqlSB.toString();
        
        log.debug(sql);
        
        ResultSet rs = stmt.executeQuery(sql);
        return readDataIntoMap(rs, CollectionObject.getClassTableId());
    }
    
    /**
     * @param tableId
     * @return
     */
    public List<BubbleDisplayInfo> getBubbleDisplayInfo(final int tableId)
    {
        List<BubbleDisplayInfo> bdi = bciHash.get(tableId);
        if (bdi != null)
        {
            return bdi;
        }
        
        DBTableInfo ti = DBTableIdMgr.getInstance().getInfoById(tableId);
        switch (tableId)
        {
            case 1: // Col Obj
                return initColObjDisplayInfo();
                
            case 5: // Agent
            {
                bdi = initBubbleDisplayInfo(ti, agentColumnNames);
                return bdi;
            }   
            default: 
            {
                return ti != null ? initBubbleDisplayInfo(ti, null) : null;
            }
        }
    }
    
    /**
     * @param attachmentID
     * @param tableId
     */
    public Map<String, Object> queryByTableId(final int attachmentID, final int tableId)
    {
        DBTableInfo ti     = DBTableIdMgr.getInstance().getInfoById(tableId);
        DBTableInfo joinTI = DBTableIdMgr.getInstance().getByClassName(clsHashMap.get(ti.getClassObj()).getName());
        
        String joinStr1 = String.format("LEFT JOIN %s AS %s ON att.AttachmentID = %s.AttachmentID ", joinTI.getName(), joinTI.getAbbrev(), joinTI.getAbbrev());
        String joinStr2 = String.format("LEFT JOIN %s AS %s ON %s.%s = %s.%s ", ti.getName(), ti.getAbbrev(), ti.getAbbrev(), ti.getIdColumnName(), joinTI.getAbbrev(), ti.getIdColumnName());
        
        Statement stmt = null;
        try
        {
            stmt = conn.createStatement();
            switch (tableId)
            {
                case 1:
                    return queryColObj(attachmentID, ti, joinStr1, joinStr2, stmt);
                    
                case 5:
                    return queryAgent(attachmentID, ti, joinStr1, joinStr2, stmt);
            }
            
        } catch (SQLException ex)
        {
            
        } finally
        {
            try
            {
                if (stmt != null) stmt.close();
            } catch (SQLException ex) {}
        }
        return null;
    }
}
