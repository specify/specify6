package edu.ku.brc.specify.tasks.services.biogeomancer;


/**
 * This class is nothing more than a C-style struct holding a bunch of string
 * values and an array of {@link BioGeomancerResult}s.
 * 
 * @author jstewart
 * @code_status Alpha
 *
 */
public class BioGeomancerQuerySummary
{
    public String id;
    public String country;
    public String adm1;
    public String adm2;
    public String localityStr;
    public String countryBoundingBox;
    public String matchedRecordCount;
    public String boundingBox;
    public String boundingBoxCentroid;
    public String boundingBoxCentroidErrorRadius;
    public String boundingBoxCentroidErrorRadiusUnits;
    public String multiPointMatch;
    public String weightedCentroid;
    
    public BioGeomancerResult[] results;
}
