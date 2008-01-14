/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
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
import edu.ku.brc.services.biogeomancer.GeoRefBGMProvider;
import edu.ku.brc.services.biogeomancer.GeoRefDataIFace;
import edu.ku.brc.services.biogeomancer.GeoRefProviderCompletionIFace;
import edu.ku.brc.specify.datamodel.CollectingEvent;
import edu.ku.brc.specify.datamodel.CollectionObject;
import edu.ku.brc.specify.datamodel.GeoCoordDetail;
import edu.ku.brc.specify.datamodel.Geography;
import edu.ku.brc.specify.datamodel.Locality;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.specify.datamodel.RecordSetItem;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Jan 14, 2008
 *
 */
public class BGMRecordSetProcessor implements RecordSetToolsIFace, GeoRefProviderCompletionIFace
{
    private static final Logger log = Logger.getLogger(BGMRecordSetProcessor.class);
    
    public BGMRecordSetProcessor()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#exportList(java.util.List, java.util.Properties)
     */
    public void processDataList(final List<?> data, final Properties requestParams) throws Exception
    {
        // TODO Auto-generated method stub

    }
    
    /**
     * @param hSQL
     * @param ids
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
    public void processRecordSet(final RecordSet recordSet, final Properties requestParams) throws Exception
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
            List<GeoRefDataIFace> geoRefDataList = new Vector<GeoRefDataIFace>();
            
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
            
            GeoRefBGMProvider bgmProvider = new GeoRefBGMProvider();
            bgmProvider.processGeoRefData(geoRefDataList, this);
        }
    }
    
    /**
     * @param geo
     * @param rankId
     * @return
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

    //----------------------------------------------------------------------
    // GeoRefProviderCompletionIFace Interface
    //----------------------------------------------------------------------
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetToolsIFace#getTableIds()
     */
    public Integer[] getTableIds()
    {
        return new Integer[] {1, 2, 10};
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.services.biogeomancer.GeoRefProviderCompletionIFace#complete()
     */
    public void complete(final List<GeoRefDataIFace> items)
    {
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        try
        {
            for (GeoRefDataIFace item : items)
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


    //----------------------------------------------------------------------
    //----------------------------------------------------------------------
    class GeoRefData implements GeoRefDataIFace
    {
        private int    id;
        private String country;
        private String state;
        private String county;
        private String localityStr;
        private Double latitude;
        private Double longitude;
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
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getCountry()
         */
        public String getCountry()
        {
            return country;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getCounty()
         */
        public String getCounty()
        {
            return county;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getId()
         */
        public Integer getId()
        {
            return id;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getLatitude()
         */
        public Double getLatitude()
        {
            return latitude;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getLocalityString()
         */
        public String getLocalityString()
        {
            return localityStr;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getLongitude()
         */
        public Double getLongitude()
        {
            return longitude;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getState()
         */
        public String getState()
        {
            return state;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getTitle()
         */
        public String getTitle()
        {
            return "???";
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#getXML()
         */
        public String getXML()
        {
            return xml;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#set(java.lang.Double, java.lang.Double)
         */
        public void set(Double latitude, Double longitude)
        {
            this.latitude  = latitude;
            this.longitude = longitude;
        }

        /* (non-Javadoc)
         * @see edu.ku.brc.services.biogeomancer.GeoRefDataIFace#setXML(java.lang.String)
         */
        public void setXML(String xmlArg)
        {
            this.xml = xmlArg;
        }
        
    }
}
