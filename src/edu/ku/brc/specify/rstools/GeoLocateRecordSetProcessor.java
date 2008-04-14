/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.rstools;

import java.util.List;
import java.util.Properties;

import edu.ku.brc.services.biogeomancer.GeoCoordDataIFace;
import edu.ku.brc.services.biogeomancer.GeoCoordGeoLocateProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UIRegistry;

/**
 * Implements the RecordSetToolsIFace for GeoReferenceing with Geolocate.
 * 
 * @author rod
 *
 * @code_status Complete
 *
 * Jan 15, 2008
 *
 */
public class GeoLocateRecordSetProcessor extends GeoRefRecordSetProcessorBase implements GeoCoordProviderListenerIFace
{
    /**
     * Constructor.
     */
    public GeoLocateRecordSetProcessor()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.rstools.GeoRefRecordSetProcessorBase#processDataList(java.util.List, java.util.Properties)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void processDataList(final List<?> items, 
                                final Properties requestParams) throws Exception
    {
        Object listenerObj = requestParams.get("listener");
        GeoCoordProviderListenerIFace listener = listenerObj != null && listenerObj instanceof GeoCoordProviderListenerIFace ? 
                (GeoCoordProviderListenerIFace)listenerObj : null;
        
        GeoCoordGeoLocateProvider geoCoordGLProvider = new GeoCoordGeoLocateProvider();
        geoCoordGLProvider.processGeoRefData((List<GeoCoordDataIFace>)items, listener, "");
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet, java.util.Properties)
     */
    public void processRecordSet(final RecordSet recordSet, 
                                 final Properties requestParams) throws Exception
    {
        processRecordSet(recordSet, requestParams, new GeoCoordGeoLocateProvider());
    }


    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getIconName()
     */
    public String getIconName()
    {
        return "BioGeoMancer32"; // XXX need GeoLocate Icon
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#getName()
     */
    public String getName()
    {
        return "GEOLocate"; 
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.GeoRefRecordSetProcessorBase#getDescription()
     */
    @Override
    public String getDescription()
    {
        return UIRegistry.getResourceString("GEOLOCATE_DESC");
    }
    
}