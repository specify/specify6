/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.biogeomancer;

/**
 * This class is nothing more than a C-style struct holding a bunch of string
 * values.  The information encapsulated in this struct is a representation of
 * the a BioGeomancer Classic query result.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class BioGeomancerResultStruct
{
    // results values
    public String country;
    public String adm1;
    public String adm2;
    public String featureName;
    public String featureType;
    public String gazetteer;
    public String coordinates;
    public String offset;
    public String boundingBox;
    public String locality;
}
