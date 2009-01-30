/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.specify.rstools;

import static edu.ku.brc.util.LatLonConverter.convertToDDDDDD;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.dbsupport.RecordSetIFace;
import edu.ku.brc.dbsupport.RecordSetItemIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordServiceProviderIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.util.LatLonConverter.DEGREES_FORMAT;
import edu.ku.brc.util.LatLonConverter.DIRECTION;

/**
 * Implements the RecordSetToolsIFace for GeoReferenceing with BioGeomancer.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 15, 2008
 *
 */
public abstract class GeoRefRecordSetProcessorBase implements RecordSetToolsIFace, GeoCoordProviderListenerIFace
{
    private static final Logger log = Logger.getLogger(GeoRefRecordSetProcessorBase.class);
    
    /**
     * Constructor.
     */
    public GeoRefRecordSetProcessorBase()
    {
        
    }
    
    /**
     * @return the name of the service that was used to Geo-Reference the locality.
     */
    public abstract String getGeoRefProviderName();
    
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#exportList(java.util.List, java.util.Properties)
     */
    public void processDataList(final List<?> data, 
                                final Properties requestParams) throws Exception
    {
        throw new RuntimeException("Not Implemented!");
    }
    
    /**
     * Retrieves all the record ids for the given HSQL.
     * @param hSQL the HSQL
     * @param ids the list to be filled in with the ids
     */
    @SuppressWarnings("unchecked")
    protected void retrieveIds(final String hSQL, final Vector<Integer> ids)
    {
        Session session = HibernateUtil.getNewSession();
        try
        {
            Query query = session.createQuery(hSQL);
            Set<?> results = new java.util.HashSet<Object>(query.list()); // unchecked
            for (Object obj : results)
            {
                ids.add((Integer)obj);
            }
            
        } catch (Exception ex)
        {
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoRefRecordSetProcessorBase.class, ex);
            // XXX error dialog
            log.error(ex);
            
        } finally
        {
            session.close();
        }
    }
    
    /**
     * @param sql
     * @param ids
     */
    protected void retrieveIdsSQL(final String sql, final Vector<Integer> ids)
    {
        Vector<Object[]> rows = BasicSQLUtils.query(sql);
        if (rows != null && rows.size() > 0)
        {
            for (Object[] r : rows)
            {
                ids.add((Integer)r[0]);
            }
        }
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.rstools.RecordSetToolsIFace#processRecordSet(edu.ku.brc.dbsupport.RecordSetIFace, java.util.Properties)
     */
    public abstract void processRecordSet(final RecordSetIFace recordSet, 
                                          final Properties requestParams) throws Exception;
    
    /**
     * @param recordSet
     * @param requestParams
     * @param geoRefService
     * @throws Exception
     */
    public void processRecordSet(final RecordSetIFace recordSet, 
                                 final Properties requestParams,
                                 final GeoCoordServiceProviderIFace geoRefService) throws Exception
    {
        Vector<Integer> ids = new Vector<Integer>();
        if (recordSet.getDbTableId() == CollectionObject.getClassTableId())
        {
            String sql = "SELECT loc.LocalityID FROM collectingevent ce INNER JOIN locality loc ON ce.LocalityID = loc.LocalityID INNER JOIN collectionobject co ON co.CollectingEventID = ce.CollectingEventID WHERE co.CollectionObjectID " + DBTableIdMgr.getInstance().getInClause(recordSet);
            retrieveIdsSQL(sql, ids);
            
        } else if (recordSet.getDbTableId() == CollectingEvent.getClassTableId())
        {
            String sql = "SELECT loc.LocalityID FROM collectingevent ce INNER JOIN locality loc ON ce.LocalityID = loc.LocalityID WHERE ce.CollectingEventID " + DBTableIdMgr.getInstance().getInClause(recordSet);
            retrieveIdsSQL(sql, ids);
            
        } else if (recordSet.getDbTableId() == Locality.getClassTableId())
        {
            for (RecordSetItemIFace rsi : recordSet.getItems())
            {
                ids.add(rsi.getRecordId());
            }
            
        }
        
        if (ids.size() > 0)
        {
            List<GeoCoordDataIFace> geoRefDataList = new Vector<GeoCoordDataIFace>();
            
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                for (Integer id : ids)
                {
                    Locality  locality = (Locality)session.getData(Locality.class, "localityId", id, DataProviderSessionIFace.CompareType.Equals);
                    Geography geo      = locality.getGeography();
                    
                    String          country  = getNameForRank(geo, 200);
                    String          state    = getNameForRank(geo, 300);
                    String          county   = getNameForRank(geo, 400);
                    
                    GeoCoordData geoRefData = new GeoCoordData(locality.getLocalityId(),
                                                           country,
                                                           state,
                                                           county,
                                                           locality.getLocalityName());
                    geoRefDataList.add(geoRefData);
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoRefRecordSetProcessorBase.class, ex);
                
            } finally
            {
                session.close();
            }
            
            geoRefService.processGeoRefData(geoRefDataList, this, requestParams != null ? requestParams.getProperty("helpcontext") : null);
        }
    }
    
    /**
     * Recursive method to discover any given rank that has a lower rank the current Geogrpahy object passed in.
     * @param geo the current geo
     * @param rankId the rankid to be found
     * @return the geo object with the rankid or null
     */
    public static String getNameForRank(final Geography geo, final int rankId)
    {
        if (geo.getRankId() == rankId)
        {
            return geo.getName();
        }
        
        if (geo.getRankId() < rankId)
        {
            return null;
        }
        
        return getNameForRank(geo.getParent(), rankId); 
    }
    

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getDescription()
     */
    public String getDescription()
    {
        return "";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getHandledClasses()
     */
    public Class<?>[] getHandledClasses()
    {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getIconName()
     */
    public abstract String getIconName();

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getName()
     */
    public abstract String getName();
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#isVisible()
     */
    public boolean isVisible()
    {
        return true;
    }

    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetToolsIFace#getTableIds()
     */
    public int[] getTableIds()
    {
        return new int[] {1, 2, 10};
    }
    
    //----------------------------------------------------------------------
    // GeoCoordProviderListenerIFace
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace#aboutToDisplayResults()
     */
    public void aboutToDisplayResults()
    {
        
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace#complete(java.util.List, int)
     */
    public void complete(final List<GeoCoordDataIFace> items, final int itemsUpdated)
    {
        if (itemsUpdated > 0)
        {
            DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
            try
            {
                for (GeoCoordDataIFace item : items)
                {
                    try
                    {
                        if (item.getLatitude() != null && item.getLongitude() != null)
                        {
                            Locality  locality = (Locality)session.getData(Locality.class, 
                                                                           "localityId", 
                                                                           item.getId(), 
                                                                           DataProviderSessionIFace.CompareType.Equals);
                            if (locality != null)
                            {
                                BigDecimal lat = new BigDecimal(item.getLatitude());
                                BigDecimal lon = new BigDecimal(item.getLongitude());
                                
                                locality.setLatitude1(lat);
                                locality.setLongitude1(lon);
                                
                                locality.setLat1text(convertToDDDDDD(lat, DEGREES_FORMAT.String, DIRECTION.NorthSouth, 6));
                                locality.setLong1text(convertToDDDDDD(lon, DEGREES_FORMAT.String, DIRECTION.EastWest, 6));
                                
                                locality.setSrcLatLongUnit((byte)0); // Decimal Degrees
                                
                                Set<GeoCoordDetail> geoCoordDetails = locality.getGeoCoordDetails();
                                GeoCoordDetail gcDetail = null;
                                if (geoCoordDetails.size() == 0)
                                {
                                    gcDetail = new GeoCoordDetail();
                                    gcDetail.initialize();
                                    locality.addReference(gcDetail, "geoCoordDetails");
                                    
                                } else if (geoCoordDetails.size() == 1)
                                {
                                    gcDetail = geoCoordDetails.iterator().next();
                                } else
                                {
                                    throw new RuntimeException("Locality can only have ONE GeoCoordDetail!");
                                }
                                
                                try
                                {
                                    gcDetail.setGeoRefDetBy(Agent.getUserAgent());
                                    gcDetail.setGeoRefDetDate(Calendar.getInstance());
                                    gcDetail.setGeoRefDetRef(getGeoRefProviderName());

                                    session.beginTransaction();
                                    session.saveOrUpdate(locality);
                                    session.commit();
                                    session.evict(locality);
                                    
                                } catch (Exception ex)
                                {
                                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoRefRecordSetProcessorBase.class, ex);
                                    log.error(ex);
                                    ex.printStackTrace();
                                    
                                    session.rollback();
                                }
                                
                            }
                        }
                        
                    } catch (Exception ex)
                    {
                        edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                        edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoRefRecordSetProcessorBase.class, ex);
                        // XXX error dialog
                        log.error(ex);
                    }    
                }
            } catch (Exception ex)
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GeoRefRecordSetProcessorBase.class, ex);
                log.error(ex);
                // XXX error dialog
                
            } finally 
            {
                session.close();
            }
        }
    }
}
