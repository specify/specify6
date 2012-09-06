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
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.services.geolocate.prototype.Locality;
import edu.ku.brc.specify.datamodel.Accession;
import edu.ku.brc.specify.datamodel.AccessionAttachment;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.AgentAttachment;
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
    protected static final Logger  log = Logger.getLogger(CollectionDataFetcher.class);
    
    protected Connection conn = DBConnection.getInstance().getConnection();
    protected HashMap<Class<?>, Class<?>> clsHashMap = new HashMap<Class<?>, Class<?>>();
    
    protected boolean isEmbeded;
    
    /**
     * 
     */
    public CollectionDataFetcher()
    {
        super();

        Class<?>[] attachmentClasses = {
                Accession.class,          AccessionAttachment.class,
                Agent.class,              AgentAttachment.class,
                Borrow.class,             BorrowAttachment.class,
                CollectingEvent.class,    CollectingEventAttachment.class,
                CollectionObject.class,   CollectionObjectAttachment.class,
                ConservDescription.class, ConservDescriptionAttachment.class,
                ConservEvent.class,       ConservEventAttachment.class,
                DNASequence.class,        DNASequenceAttachment.class,
                DNASequencingRun.class,   DNASequencingRunAttachment.class,
                FieldNotebook.class,      FieldNotebookAttachment.class,
                FieldNotebookPage.class,  FieldNotebookPageAttachment.class,
                FieldNotebookPageSet.class, FieldNotebookPageSetAttachment.class,
                Gift.class,               GiftAttachment.class,
                Loan.class,               LoanAttachment.class,
                Locality.class,           LocalityAttachment.class,
                Permit.class,             PermitAttachment.class,
                Preparation.class,        PreparationAttachment.class,
                ReferenceWork.class,      ReferenceWorkAttachment.class,
                RepositoryAgreement.class, RepositoryAgreementAttachment.class,
                Taxon.class,              TaxonAttachment.class,
            };
        for (int i=0;i<attachmentClasses.length;i++)
        {
            clsHashMap.put(attachmentClasses[i], attachmentClasses[i+1]);
            i++;
        }
        Collection collection = AppContextMgr.getInstance().getClassObject(Collection.class);
        isEmbeded  = collection.getIsEmbeddedCollectingEvent();
    }
    
    /**
     * @param ti
     * @return
     */
    private String getFields(final DBTableInfo ti)
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
     * @param attachmentID
     * @param tableId
     */
    public HashMap<String, Object> getData(final int attachmentID, final int tableId)
    {
        DBTableInfo ti     = DBTableIdMgr.getInstance().getInfoById(tableId);
        DBTableInfo joinTI = DBTableIdMgr.getInstance().getByClassName(clsHashMap.get(ti.getClassObj()).getName());
        
        boolean isColObj = ti.getTableId() == CollectionObject.getClassTableId();
        
        String joinStr1 = String.format("LEFT JOIN %s AS %s ON att.AttachmentID = %s.AttachmentID ", joinTI.getName(), joinTI.getAbbrev(), joinTI.getAbbrev());
        String joinStr2 = String.format("LEFT JOIN %s AS %s ON %s.%s = %s.%s ", ti.getName(), ti.getAbbrev(), ti.getAbbrev(), ti.getIdColumnName(), joinTI.getAbbrev(), ti.getIdColumnName());
        
        StringBuilder sqlSB = new StringBuilder("SELECT ");
        sqlSB.append(getFields(ti));
        sqlSB.append(" FROM attachment att ");
        sqlSB.append(joinStr1);
        sqlSB.append(joinStr2);
        if (isColObj) 
        {
            sqlSB.append("LEFT JOIN determination det ON co.CollectionObjectID = det.CollectionObjectID ");
            sqlSB.append("LEFT JOIN taxon tx ON det.TaxonID = tx.TaxonID ");
            sqlSB.append("LEFT JOIN collectingevent ce ON co.CollectingEventID = ce.CollectingEventID ");
        }
        
        sqlSB.append("LEFT JOIN locality loc ON ce.LocalityID = loc.LocalityID ");
        sqlSB.append("LEFT JOIN geography geo ON loc.GeographyID = geo.GeographyID ");
        sqlSB.append("WHERE ");
        sqlSB.append("att.AttachmentID=");
        sqlSB.append(attachmentID);
        
        if (isColObj) sqlSB.append(" AND det.IsCurrent <> 0");
        
        log.debug(sqlSB.toString());
        
        Statement stmt = null;
        try
        {
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            stmt = conn.createStatement();
            ResultSet         rs   = stmt.executeQuery(sqlSB.toString());
            ResultSetMetaData rsmd = rs.getMetaData();
            
            if (rs.next())
            {
                for (int i=1;i<=rsmd.getColumnCount();i++)
                {
                    dataMap.put(rsmd.getColumnLabel(i), rs.getObject(i));
                }
            }
            rs.close();
            return dataMap;
            
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
