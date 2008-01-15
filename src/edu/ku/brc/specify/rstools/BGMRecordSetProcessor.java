/*
 * Copyright (C) 2008  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 *
 */
package edu.ku.brc.specify.rstools;

import java.util.Properties;

import edu.ku.brc.services.biogeomancer.GeoCoordBGMProvider;
import edu.ku.brc.services.biogeomancer.GeoCoordProviderListenerIFace;
import edu.ku.brc.specify.datamodel.RecordSet;
import edu.ku.brc.ui.UIRegistry;

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
public class BGMRecordSetProcessor extends GeoRefRecordSetProcessorBase implements GeoCoordProviderListenerIFace
{
    /**
     * Constructor.
     */
    public BGMRecordSetProcessor()
    {
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.exporters.RecordSetExporterIFace#exportRecordSet(edu.ku.brc.specify.datamodel.RecordSet, java.util.Properties)
     */
    public void processRecordSet(final RecordSet recordSet, 
                                 final Properties requestParams) throws Exception
    {
        processRecordSet(recordSet, requestParams, new GeoCoordBGMProvider());
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
     * @see edu.ku.brc.specify.exporters.GeoRefRecordSetProcessorBase#getDescription()
     */
    @Override
    public String getDescription()
    {
        return UIRegistry.getResourceString("BGM_DESC");
    }
}
