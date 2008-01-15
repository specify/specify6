/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.exporters;

import java.math.BigDecimal;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ku.brc.dbsupport.DBTableIdMgr;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;
import edu.ku.brc.services.biogeomancer.GeoCoordBGMProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;

/**
 * Implements the RecordSetToolsIFace for GeoReferenceing with Biogeomancer.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 14, 2008
 *
 */
public class BGMRecordSetProcessor implements RecordSetToolsIFace, GeoCoordProviderListenerIFace
{
    private static final Logger log = Logger.getLogger(BGMRecordSetProcessor.class);
    
    public BGMRecordSetProcessor()
    {
        
    }
    
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
            // XXX error dialog
            log.error(ex);
            
        } finally
        {
            session.close();
        }
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet, java.util.Properties)
     */
    @SuppressWarnings("unchecked")
    public void processRecordSet(final RecordSet recordSet, 
                                 final Properties requestParams) throws Exception
    {
        Vector<Integer> ids = new Vector<Integer>();
        if (recordSet.getDbTableId() == CollectionObject.getClassTableId())
        {
            String sql = "SELECT loc.localityId FROM Locality as loc INNER JOIN loc.collectingEvents as ce INNER JOIN ce.collectionObjects as co where co.collectionObjectId " + DBTableIdMgr.getInClause(recordSet);
            retrieveIds(sql, ids);
            
        } else if (recordSet.getDbTableId() == CollectingEvent.getClassTableId())
        {
            String sql = "SELECT loc.localityId FROM Locality as loc INNER JOIN loc.collectingEvents as ce where ce.collectingEventId " + DBTableIdMgr.getInClause(recordSet);
            retrieveIds(sql, ids);
            
        } else if (recordSet.getDbTableId() == Locality.getClassTableId())
        {
            for (RecordSetItem rsi : recordSet.getRecordSetItems())
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
                    
                    GeoRefData geoRefData = new GeoRefData(locality.getLocalityId(),
                                                           country,
                                                           state,
                                                           county,
                                                           locality.getLocalityName());
                    geoRefDataList.add(geoRefData);
                }
            } catch (Exception ex)
            {
                
            } finally
            {
                session.close();
            }
            
            GeoCoordBGMProvider bgmProvider = new GeoCoordBGMProvider();
            bgmProvider.processGeoRefData(geoRefDataList, this, requestParams != null ? requestParams.getProperty("helpcontext") : null);
        }
    }
    
    /**
     * Recursive method to discover any given rank that has a lower rank the current Geogrpahy object passed in.
     * @param geo the current geo
     * @param rankId the rankid to be found
     * @return the geo object with the rankid or null
     */
    protected String getNameForRank(final Geography geo, final int rankId)
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
    public String getIconName()
    {
        return "BioGeoMancer32";
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getName()
     */
    public String getName()
    {
        return "Biogeomancer";
    }

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
    public Integer[] getTableIds()
    {
        return new Integer[] {1, 2, 10};
    }
    
    //----------------------------------------------------------------------
    // GeoRefProviderListenerIFace Interface
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
                        if (StringUtils.isNotEmpty(item.getXML()) && 
                            item.getLatitude() != null && 
                            item.getLongitude() != null)
                        {
                            Locality  locality = (Locality)session.getData(Locality.class, 
                                                                           "localityId", 
                                                                           item.getId(), 
                                                                           DataProviderSessionIFace.CompareType.Equals);
                            if (locality != null)
                            {
                                locality.setLatitude1(new BigDecimal(item.getLatitude()));
                                locality.setLongitude1(new BigDecimal(item.getLongitude()));
                                
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
                                
                                log.debug("XML Length: "+item.getXML().length());
                                gcDetail.setBgmXML(item.getXML());
                                // Need code here to break apart the XML and put it into the fields
                                
                                try
                                {
                                    session.beginTransaction();
                                    session.saveOrUpdate(locality);
                                    session.commit();
                                    session.evict(locality);
                                    
                                } catch (Exception ex)
                                {
                                    log.error(ex);
                                    ex.printStackTrace();
                                    
                                    session.rollback();
                                }
                                
                            }
                        }
                        
                    } catch (Exception ex)
                    {
                        // XXX error dialog
                        log.error(ex);
                    }    
                }
            } catch (Exception ex)
            {
                log.error(ex);
                // XXX error dialog
                
            } finally 
            {
                session.close();
            }
        }
    }


    //----------------------------------------------------------------------
    //
    //----------------------------------------------------------------------
    class GeoRefData implements GeoCoordDataIFace
    {
        private int    id;
        private String country;
        private String state;
        private String county;
        private String localityStr;
        private String latitude;
        private String longitude;
        private String xml;
        
        public GeoRefData(final int id, final String country, final String state, final String county, final String localityStr)
        {
            super();
            this.id = id;
            this.country = country;
            this.state = state;
            this.county = county;
            this.localityStr = localityStr;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCountry()
         */
        public String getCountry()
        {
            return country;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getCounty()
         */
        public String getCounty()
        {
            return county;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getId()
         */
        public Integer getId()
        {
            return id;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLatitude()
         */
        public String getLatitude()
        {
            return latitude;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLocalityString()
         */
        public String getLocalityString()
        {
            return localityStr;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getLongitude()
         */
        public String getLongitude()
        {
            return longitude;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getState()
         */
        public String getState()
        {
            return state;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getTitle()
         */
        public String getTitle()
        {
            return "???";
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#getXML()
         */
        public String getXML()
        {
            return xml;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#set(java.lang.Double, java.lang.Double)
         */
        public void set(final String latitudeArg, final String longitudeArg)
        {
            this.latitude  = latitudeArg;
            this.longitude = longitudeArg;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoCoordDataIFace#setXML(java.lang.String)
         */
        public void setXML(String xmlArg)
        {
            this.xml = xmlArg;
        }
        
    }

}
