package edu.ku.brc.services.biogeomancer;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.tasks.services.LocalityMapper.MapperListener;

/**
 * The class is a client-side interface to the BioGeomancer georeferencing web service.
 * 
 * @code_status Beta
 * @author jstewart
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
        List<?> records = responseAsXml.selectNodes("//record");
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
    public static String getBioGeoMancerResponse(final String id,
                                                 final String country,
                                                 final String adm1,
                                                 final String adm2,
                                                 final String localityArg) throws IOException
    {
        String retVal = null;

        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod("http://130.132.27.130/cgi-bin/bgm-0.2/batch_test.pl");
        StringBuilder strBuf = new StringBuilder(128);
        strBuf.append("\""+ id + "\",");
        strBuf.append("\""+ country + "\",");
        strBuf.append("\""+ adm1 + "\",");
        strBuf.append("\""+ (adm2 != null ? adm2 : "") + "\",");
        strBuf.append("\""+ localityArg + "\"\r\n");

        NameValuePair[] postData = {
                //new NameValuePair("batchtext", "\"12931\",\"Mexico\",\"Veracruz\",\"\",\"12 km NW of Catemaco\"\r\n"),
                new NameValuePair("batchtext", strBuf.toString()),
                new NameValuePair("format", "xml") };

        //the 2.0 beta1 version has a
        // PostMethod.setRequestBody(NameValuePair[])
        //method, as addParameters is deprecated
        postMethod.addParameters(postData);

        String responseBody = "";

        httpClient.executeMethod(postMethod);
        responseBody = postMethod.getResponseBodyAsString();

        //release the connection used by the method
        postMethod.releaseConnection();

        retVal = responseBody;
        return retVal;
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
        String responseStr = getBioGeoMancerResponse(id, country, adm1, adm2, localityString);
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
        Element summary = (Element)root.selectSingleNode("//summary");
        if (summary == null)
        {
            throw new Exception("BioGeomancer response is missing required data");
        }

        BioGeomancerQuerySummaryStruct querySummary = new BioGeomancerQuerySummaryStruct();

        // get all of the data from the summary section
        querySummary.id                                  = XMLHelper.getValue(summary,"queryId");
        querySummary.country                             = XMLHelper.getValue(summary,"queryCountry");
        querySummary.adm1                                = XMLHelper.getValue(summary,"queryAdm1");
        querySummary.adm2                                = XMLHelper.getValue(summary,"queryAdm2");
        querySummary.localityStr                         = XMLHelper.getValue(summary,"queryString");
        querySummary.countryBoundingBox                  = XMLHelper.getValue(summary,"countryBoundingBox");
        querySummary.matchedRecordCount                  = XMLHelper.getValue(summary,"matchedRecordCount");
        querySummary.boundingBox                         = XMLHelper.getValue(summary,"boundingBox");
        querySummary.boundingBoxCentroid                 = XMLHelper.getValue(summary,"boundingBoxCentroid");
        querySummary.boundingBoxCentroidErrorRadius      = XMLHelper.getValue(summary,"boundingBoxCentroidErrorRadius");
        querySummary.boundingBoxCentroidErrorRadiusUnits = XMLHelper.getValue(summary,"boundingBoxCentroidErrorRadiusUnits");
        querySummary.multiPointMatch                     = XMLHelper.getValue(summary,"multiPointMatch");
        querySummary.weightedCentroid                    = XMLHelper.getValue(summary,"weightedCentroid");

        // get each of the results records
        List<?> records = root.selectNodes("//record");
        BioGeomancerResultStruct[] results = new BioGeomancerResultStruct[records.size()];
        int index = 0;
        for (Object o: records)
        {
            Element record = (Element)o;
            BioGeomancerResultStruct result = new BioGeomancerResultStruct();
            result.country     = XMLHelper.getValue(record, "country");
            result.adm1        = XMLHelper.getValue(record, "adm1");
            result.adm2        = XMLHelper.getValue(record, "adm2");
            result.featureName = XMLHelper.getValue(record, "featureName");
            result.featureType = XMLHelper.getValue(record, "featureType");
            result.gazetteer   = XMLHelper.getValue(record, "gazetteerSource");
            result.coordinates = XMLHelper.getValue(record, "InterpretedCoordinates");
            result.offset      = XMLHelper.getValue(record, "offsetVector");
            result.boundingBox = XMLHelper.getValue(record, "boundingBox");
            result.locality    = XMLHelper.getValue(record, "InterpretedString");
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
        BioGeomancerMapper bioGeoMancerMapper = new BioGeomancerMapper();

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
                bioGeoMancerMapper.addBGMDataAndLabel(lat, lon, box[1], box[0], box[3], box[2], Integer.toString(i+1));
                bioGeoMancerMapper.setMaxMapWidth(MAP_MAX_WIDTH);
                bioGeoMancerMapper.setMaxMapHeight(MAP_MAX_HEIGHT);
            }
        }
        bioGeoMancerMapper.getMap(callback);
    }
}
