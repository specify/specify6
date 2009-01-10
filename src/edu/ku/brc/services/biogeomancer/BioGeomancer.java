/**
 * Copyright (C) 2007  The University of Kansas
 *
 * [INSERT KU-APPROVED LICENSE TEXT HERE]
 * 
 */
package edu.ku.brc.services.biogeomancer;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.services.mapping.LocalityMapper;
import edu.ku.brc.services.mapping.SimpleMapLocation;
import edu.ku.brc.services.mapping.LocalityMapper.MapperListener;

/**
 * The class is a client-side interface to the BioGeomancer Classic georeferencing service.
 * 
 * @author jstewart
 * @code_status Beta
 */
public class BioGeomancer
{
    protected static final int MAP_MAX_WIDTH  = 400;
    protected static final int MAP_MAX_HEIGHT = 250;

    /**
     * This method inspects the response received from a BG call and determines
     * the number of possible results given.
     * 
     * @param bioGeomancerResponseString the response from the BG service
     * @return the number of possible results found in the given response
     * @throws Exception if parsing the response string fails
     */
    public static int getResultsCount(final String bioGeomancerResponseString) throws Exception
    {
        Element responseAsXml = XMLHelper.readStrToDOM4J(bioGeomancerResponseString);
        List<?> records = responseAsXml.selectNodes("//record"); //$NON-NLS-1$
        return (records != null) ? records.size() : 0;
    }


    /**
     * Sends a georeferencing request to the BioGeomancer web service.
     * 
     * @param id id
     * @param country country
     * @param adm1 country
     * @param adm2 adm2
     * @param localityArg locality
     * @return returns the response body content (as XML)
     * @throws IOException a network error occurred while contacting the BioGeomancer service
     */
    public static String getBioGeomancerResponse(final String id,
                                                 final String country,
                                                 final String adm1,
                                                 final String adm2,
                                                 final String localityArg) throws IOException
    {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod("http://130.132.27.130/cgi-bin/bgm-0.2/batch_test.pl"); //$NON-NLS-1$
        StringBuilder strBuf = new StringBuilder(128);
        strBuf.append("\""+ id + "\","); //$NON-NLS-1$ //$NON-NLS-2$
        strBuf.append("\""+ country + "\","); //$NON-NLS-1$ //$NON-NLS-2$
        strBuf.append("\""+ adm1 + "\","); //$NON-NLS-1$ //$NON-NLS-2$
        strBuf.append("\""+ (adm2 != null ? adm2 : "") + "\","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        strBuf.append("\""+ localityArg + "\"\r\n"); //$NON-NLS-1$ //$NON-NLS-2$

        NameValuePair[] postData = {
                //new NameValuePair("batchtext", "\"12931\",\"Mexico\",\"Veracruz\",\"\",\"12 km NW of Catemaco\"\r\n"),
                new NameValuePair("batchtext", strBuf.toString()), //$NON-NLS-1$
                new NameValuePair("format", "xml") }; //$NON-NLS-1$ //$NON-NLS-2$

        // the 2.0 beta1 version has a
        // PostMethod.setRequestBody(NameValuePair[])
        // method, as addParameters is deprecated
        postMethod.addParameters(postData);

        httpClient.executeMethod(postMethod);
        
        InputStream iStream = postMethod.getResponseBodyAsStream();
        
        StringBuilder sb       = new StringBuilder();
        byte[]        bytes    = new byte[8196];
        int           numBytes = 0;
        do 
        {
            numBytes = iStream.read(bytes);
            if (numBytes > 0)
            {
               sb.append(new String(bytes, 0, numBytes));
            }
            
        } while (numBytes > 0);
        

        //release the connection used by the method
        postMethod.releaseConnection();

        return  sb.toString();
    }
    
    
    /**
     * Sends a georeferencing request to the BioGeomancer web service.
     * 
     * @param id
     * @param country
     * @param adm1
     * @param adm2
     * @param localityString
     * @return a struct holding all the information of the query and results
     * @throws Exception a network or XML parsing error
     */
    public static BioGeomancerQuerySummaryStruct getBioGeomancerResponses(final String id,
                                                         final String country,
                                                         final String adm1,
                                                         final String adm2,
                                                         final String localityString)
                                                        throws Exception
    {
        String responseStr = getBioGeomancerResponse(id, country, adm1, adm2, localityString);
        return parseBioGeomancerResponse(responseStr);
    }
    
    
    /**
     * Parses the XML reply string given, creating a {@link BioGeomancerQuerySummaryStruct} from the
     * data contained in it.
     * 
     * @param bgResponse the XML response string from the BioGeomancer web service
     * @return the corresponding {@link BioGeomancerQuerySummaryStruct}
     * @throws Exception an XML parsing error occurred
     */
    public static BioGeomancerQuerySummaryStruct parseBioGeomancerResponse(String bgResponse) throws Exception
    {
        // read the string into a DOM
        Element root = XMLHelper.readStrToDOM4J(bgResponse);
        Element summary = (Element)root.selectSingleNode("//summary"); //$NON-NLS-1$
        if (summary == null)
        {
            throw new Exception("BioGeomancer response is missing required data"); //$NON-NLS-1$
        }

        BioGeomancerQuerySummaryStruct querySummary = new BioGeomancerQuerySummaryStruct();

        // get all of the data from the summary section
        querySummary.id                                  = XMLHelper.getValue(summary,"queryId"); //$NON-NLS-1$
        querySummary.country                             = XMLHelper.getValue(summary,"queryCountry"); //$NON-NLS-1$
        querySummary.adm1                                = XMLHelper.getValue(summary,"queryAdm1"); //$NON-NLS-1$
        querySummary.adm2                                = XMLHelper.getValue(summary,"queryAdm2"); //$NON-NLS-1$
        querySummary.localityStr                         = XMLHelper.getValue(summary,"queryString"); //$NON-NLS-1$
        querySummary.countryBoundingBox                  = XMLHelper.getValue(summary,"countryBoundingBox"); //$NON-NLS-1$
        querySummary.matchedRecordCount                  = XMLHelper.getValue(summary,"matchedRecordCount"); //$NON-NLS-1$
        querySummary.boundingBox                         = XMLHelper.getValue(summary,"boundingBox"); //$NON-NLS-1$
        querySummary.boundingBoxCentroid                 = XMLHelper.getValue(summary,"boundingBoxCentroid"); //$NON-NLS-1$
        querySummary.boundingBoxCentroidErrorRadius      = XMLHelper.getValue(summary,"boundingBoxCentroidErrorRadius"); //$NON-NLS-1$
        querySummary.boundingBoxCentroidErrorRadiusUnits = XMLHelper.getValue(summary,"boundingBoxCentroidErrorRadiusUnits"); //$NON-NLS-1$
        querySummary.multiPointMatch                     = XMLHelper.getValue(summary,"multiPointMatch"); //$NON-NLS-1$
        querySummary.weightedCentroid                    = XMLHelper.getValue(summary,"weightedCentroid"); //$NON-NLS-1$

        // get each of the results records
        List<?> records = root.selectNodes("//record"); //$NON-NLS-1$
        BioGeomancerResultStruct[] results = new BioGeomancerResultStruct[records.size()];
        int index = 0;
        for (Object o: records)
        {
            Element record = (Element)o;
            BioGeomancerResultStruct result = new BioGeomancerResultStruct();
            result.country     = XMLHelper.getValue(record, "country"); //$NON-NLS-1$
            result.adm1        = XMLHelper.getValue(record, "adm1"); //$NON-NLS-1$
            result.adm2        = XMLHelper.getValue(record, "adm2"); //$NON-NLS-1$
            result.featureName = XMLHelper.getValue(record, "featureName"); //$NON-NLS-1$
            result.featureType = XMLHelper.getValue(record, "featureType"); //$NON-NLS-1$
            result.gazetteer   = XMLHelper.getValue(record, "gazetteerSource"); //$NON-NLS-1$
            result.coordinates = XMLHelper.getValue(record, "InterpretedCoordinates"); //$NON-NLS-1$
            result.offset      = XMLHelper.getValue(record, "offsetVector"); //$NON-NLS-1$
            result.boundingBox = XMLHelper.getValue(record, "boundingBox"); //$NON-NLS-1$
            result.locality    = XMLHelper.getValue(record, "InterpretedString"); //$NON-NLS-1$
            results[index++] = result;
        }
        querySummary.results = results;

        return querySummary;
    }
    
    
    /**
     * Grabs a map of the given BioGeomancer results.
     * 
     * @param bgResponse the BioGeomancer response string (as XML)
     * @param callback the class to notify after the map grabbing is complete
     * @throws Exception an XML parsing error occurred
     */
    public static void getMapOfBioGeomancerResults(String bgResponse, MapperListener callback) throws Exception
    {
        BioGeomancerQuerySummaryStruct summary = parseBioGeomancerResponse(bgResponse);
        getMapOfQuerySummary(summary, callback);
    }

    
    /**
     * Grabs a map of the given BioGeomancer results.
     * 
     * @param querySummary the BioGeomancer response (as a {@link BioGeomancerQuerySummaryStruct})
     * @param callback the class to notify after the map grabbing is complete
     */
    public static void getMapOfQuerySummary(BioGeomancerQuerySummaryStruct querySummary, MapperListener callback)
    {
        // This is the 'old' version of the code.
        // It used BioGeomancerMapper.  The new version works with LocalityMapper.
//        BioGeomancerMapper bioGeoMancerMapper = new BioGeomancerMapper();
//        bioGeoMancerMapper.setMaxMapWidth(MAP_MAX_WIDTH);
//        bioGeoMancerMapper.setMaxMapHeight(MAP_MAX_HEIGHT);
//
//        for (int i = 0; i < querySummary.results.length; ++i)
//        {
//            BioGeomancerResultStruct result = querySummary.results[i];
//            String[] coords = StringUtils.split(result.coordinates);
//            double lon = Double.parseDouble(coords[0]);
//            double lat = Double.parseDouble(coords[1]);
//            
//            String bbox = result.boundingBox;
//            if (StringUtils.isNotEmpty(bbox))
//            {
//                String[] boxList = StringUtils.split(bbox.replace(',', ' '));
//                double[] box = new double[4];
//                for (int j = 0; j < boxList.length; ++j)
//                {
//                    box[j] = Double.parseDouble(boxList[j]);
//                }
//                bioGeoMancerMapper.addBGMDataAndLabel(lat, lon, box[1], box[0], box[3], box[2], Integer.toString(i+1));
//            }
//        }
//        bioGeoMancerMapper.getMap(callback);
        
        LocalityMapper mapper = new LocalityMapper();
        mapper.setShowArrows(false);
        mapper.setMaxMapWidth(MAP_MAX_WIDTH);
        mapper.setMaxMapHeight(MAP_MAX_HEIGHT);

        for (int i = 0; i < querySummary.results.length; ++i)
        {
            BioGeomancerResultStruct result = querySummary.results[i];
            String[] coords = StringUtils.split(result.coordinates);
            double lon = Double.parseDouble(coords[0]);
            double lat = Double.parseDouble(coords[1]);

            String bbox = result.boundingBox;
            if (StringUtils.isNotEmpty(bbox))
            {
                String[] boxList = StringUtils.split(bbox.replace(',', ' '));
                double[] box = new double[4];
                for (int j = 0; j < boxList.length; ++j)
                {
                    box[j] = Double.parseDouble(boxList[j]);
                }
                SimpleMapLocation loc = new SimpleMapLocation(box[1],box[0],box[3],box[2]);
        
                mapper.addLocationAndLabel(loc, Integer.toString(i+1));
            }
            else
            {
                SimpleMapLocation loc = new SimpleMapLocation(lat,lon,null,null);
                mapper.addLocationAndLabel(loc, Integer.toString(i+1));
            }
        }
        mapper.getMap(callback);
    }
}
