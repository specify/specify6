/* Copyright (C) 2022, University of Kansas Center for Research
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
package edu.ku.brc.specify.tools.gbifregistration;


import edu.ku.brc.helpers.ProxyHelper;
import edu.ku.brc.helpers.XMLHelper;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.HttpEntity;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.protocol.HttpClientContext;

import org.apache.log4j.Logger;
import org.dom4j.Element;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.io.File;

public class GbifSandbox {
    private static final Logger log = Logger.getLogger(GbifSandbox.class);

    private static String api = "api.gbif-uat.org";
    private static String gbifApiUri = "http://" + api + "/v1/";
    private static String zenodoApi = "sandbox.zenodo.org/api";
    private static String zenodoApiUrl = "https://" + zenodoApi + "/";


    private Pair<String, String> getGbifRegCredentials() {
        return new Pair<String, String>("timoatku", "Zne$L0ngO");
    }
    private String getGbifPublishingOrganization() {
        return "6a078fc1-c9d9-416d-98e6-a3a3c33183e6";
    }
    private String getGbifInstallation() {
        return "5b8e7001-516d-4880-bd68-826948bbffec";
    }
    private String getZenodoUploadToken() {
        //return "9qG2PLQcUoivakxFaSRJPXCAR6opz4gQiRrNljVd8r0C9uese3PeHQLjmZ11";
        //Sandbox token:
        return "Cp0n4hs4Ev4XYrNJZ9DXyuPrZHy5QhKqYNSLEjVgV4FCMnHiMYxbyuFkliRI";
    }

    /**
     *
      * @param data
     * @param apiFn
     * @return
     */
    public Pair<Boolean, String> postToGbif(final JSONObject data, final String apiFn) {
        try {
            StringEntity se = new StringEntity(data.toString(), "application/json", "utf-8");
            return postToApi(gbifApiUri + apiFn, getGbifRegCredentials(), null, null, se);
        } catch (java.io.UnsupportedEncodingException x) {
            log.error(x);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GbifSandbox.class, x);
            return new Pair<>(false, x.getMessage());
        }
    }

    /**
     *
      * @param uri
     * @param re
     * @return
     */
    public Pair<Boolean, String> postToApi(final String uri, final Pair<String, String> credentials,
                                           final List<Pair<String, String>> params, final List<Pair<String, String>> headers,
                                           final HttpEntity entity) {
        HttpPost postMethod  = new HttpPost(uri);
        if (headers != null) {
            return new Pair<>(false, "postToApi() headers parameter is not supported");
        }
        if (params != null) {
            return new Pair<>(false, "postToApi() params parameter is not supported");
        }
        if (entity != null) {
            postMethod.setEntity(entity);
        }
        return executeMethod(postMethod, credentials, HttpStatus.SC_CREATED);
    }



    /**
     *
     * @param method
     * @param successCode
     * @return
     */
    public Pair<Boolean, String> executeMethod(HttpRequestBase method, final Pair<String, String> credentials, int successCode) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            CredentialsProvider credentialsProvider = null;
            if (credentials != null) {
                credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(credentials.getFirst(), credentials.getSecond()));
            }
            HttpClientContext localContext = HttpClientContext.create();
            if (credentialsProvider != null) {
                localContext.setCredentialsProvider(credentialsProvider);
            }
            RequestConfig.Builder requestConfig = RequestConfig.custom();
            requestConfig.setConnectTimeout(5000);
            ProxyHelper.applyProxySettings(method, requestConfig);
            method.setConfig(requestConfig.build());
            CloseableHttpResponse response = client.execute(method, localContext);
            int status = response.getStatusLine().getStatusCode();
            boolean success = status == successCode;
            Pair<Boolean, String> result = new Pair<>(success, EntityUtils.toString(response.getEntity()));
            try {
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
            return result;
        } catch (java.io.IOException x) {
            log.error(x);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GbifSandbox.class, x);
            return new Pair<>(false, x.getMessage());
        }
    }


    /**
     *
     * @param dataset
     * @return
     */
    public Pair<Boolean, String> registerDataset(final JSONObject dataset) {
        return postToGbif(dataset, "dataset");
    }

    public Pair<Boolean, String> registerADataset() {
        JSONObject data = new JSONObject();
        data.put("installationKey", getGbifInstallation());
        data.put("publishingOrganizationKey", getGbifPublishingOrganization());
        data.put("type", "OCCURRENCE");
        data.put("title", "eve of deletion");
        return registerDataset(data);
    }

    public Pair<Boolean, String>  uploadToZenodoOld(File file) {
        try {
            StringEntity se = new StringEntity("{}", "application/json", StandardCharsets.UTF_8.toString());
            Pair<Boolean, String> result1 = postToApi(zenodoApiUrl + "deposit/depositions?access_token=" + getZenodoUploadToken(),
                    null, null, /*headers*/null, se);
            if (!result1.getFirst()) {
                return result1;
            }

            JSONObject response = JSONObject.fromObject(result1.getSecond());
            String depositId = response.getString("id");


            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(StandardCharsets.UTF_8);
            //builder.addTextBody("data",  "{'filename': '" + file.getName()+ "'}");
            builder.addBinaryBody("uploadfile", file, ContentType.TEXT_PLAIN, file.getName());

            //
            // FileRequestEntity fe = new FileRequestEntity(file, "text/csv");

            return postToApi(zenodoApiUrl + "deposit/depositions/" + depositId + "/files?access_token=" + getZenodoUploadToken(),
                    null, null, /*headers*/null, builder.build());

        } catch (java.io.UnsupportedEncodingException x) {
            return new Pair<>(false, x.getMessage());
        }
    }

    public Pair<Boolean, String>  uploadToZenodo(File file) {
        //see https://developers.zenodo.org/?python#quickstart-upload     --- New Files API
        try {
            StringEntity se = new StringEntity("{}", "application/json", StandardCharsets.UTF_8.toString());
            Pair<Boolean, String> result1 = postToApi(zenodoApiUrl + "deposit/depositions?access_token=" + getZenodoUploadToken(),
                    null, null, /*headers*/null, se);
            if (!result1.getFirst()) {
                return result1;
            }

            JSONObject response = JSONObject.fromObject(result1.getSecond());
            JSONObject links = response.getJSONObject("links");
            String bucket = links.getString("bucket");

            HttpPut putter = new HttpPut(bucket + "/" + file.getName() + "?access_token=" + getZenodoUploadToken());

            //MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            //builder.setCharset(StandardCharsets.UTF_8);
            //builder.addTextBody("data",  "{'filename': '" + file.getName()+ "'}");
            //builder.addBinaryBody("file", ContentType.TEXT_PLAIN, file.getName());
            putter.setEntity(new FileEntity(file));
            Pair<Boolean, String> result = executeMethod(putter, null, 200);
            return result;

        } catch (Exception x) {
            return new Pair<>(false, x.getMessage());
        }
    }

    public Pair<Boolean, String> deleteADataset() {
        return deleteDataset("e15b1cb0-33e5-40aa-887f-aaa13a29e3c3"); //eve of destruction
    }

    /**
     *
     * @param toDelete
     * @return
     */
    public Pair<Boolean, String> deleteDataset(final String toDelete) {
        //String toDelete = "2b98cb17-5694-4f8e-a991-f71c143c86bc";
        HttpDelete method  = new HttpDelete(gbifApiUri + "dataset/" + toDelete);
        return executeMethod(method, getGbifRegCredentials(), HttpStatus.SC_NO_CONTENT);
    }

    /**
     *
     * @param org
     * @return
     */
    public Pair<Boolean, String> registerOrganization(final JSONObject org) {
        return postToGbif(org, "organization");
    }
    public Pair<Boolean, String> registerAnOrganization() {
        JSONObject data = new JSONObject();
        data.put("title", "Spawn of Specify 6");
        data.put("description", "what happens when specify6 developers registers an organization");
        data.put("endorsingNode", "8618c64a-93e0-4300-b546-7249e5148ed2");
        data.put("city", "Lawrence");
        data.put("country", "US");
        data.put("postalCode", "66066");
        data.put("province", "Kansas");
        data.put("language", "eng");
        return registerOrganization(data);
    }

    /**
     *
     * @param coll
     * @return
     */
    public Pair<Boolean, String> registerGrSciCollCollection(final JSONObject coll) {
        return postToGbif(coll, "grscicoll/collection");
    }

    public Pair<Boolean, String> registerAGrSciCollCollection() {
        JSONObject data = new JSONObject();
        data.put("code", "SoS6");
        data.put("name", "Spawn of Specify 6");
        return postToGbif(data, "grscicoll/collection");
    }

    /**
     *
     * @param jArray
     * @return
     */
    private Set<Object> fromJSONArray(JSONArray jArray) {
        Set<Object> result = new HashSet<>();
        for (Object aObj : (JSONArray) jArray) {
            if (aObj instanceof JSONArray) {
                result.add(fromJSONArray((JSONArray)aObj));
            } else if (aObj instanceof JSONObject) {
                result.add(hashMapFromJSON((JSONObject) aObj));
            } else {
                result.add(aObj);
            }
        }
        return result;
    }

    /**
     *
     * @param obj
     * @param key
     * @param map
     */
    private void hashMapFromJSON2(Object obj, Object key, HashMap<Object, Object> map) {
        if (obj instanceof JSONArray) {
            map.put(key, fromJSONArray((JSONArray)obj));
        } else if (obj instanceof JSONObject) {
            map.put(key, hashMapFromJSON((JSONObject) obj));
        } else {
            map.put(key, obj);
        }
    }

    /**
     *
     * @param o
     * @return
     */
    private HashMap<Object, Object> hashMapFromJSON(JSONObject o) {
        HashMap<Object, Object> result = new HashMap<>();
        for (Object key : o.keySet()) {
            System.out.println(key);
            hashMapFromJSON2(o.get(key), key, result);
        }
        return result;
    }

    /**
     *
     * @param mappingName
     * @return
     */
    public static Element getDwcaSchema(String mappingName) {
        String sql = "select data from spappresource r inner join spappresourcedata d on d.spappresourceid = "
            + "r.spappresourceid where r.name = '" + mappingName +"'";
        //plus other context parame, or use appcontext mgr to get resource...
        return getDwcaSchemaFromFld(sql);
    }

    public static Element getXmlElementFromFld(String sql) {
       Element result = null;
        try {
            List<Object> blob = BasicSQLUtils.querySingleCol(sql);
            if (blob != null && blob.size() > 0) {
                //Byte[] blobBytes = (Byte [])blob.get(0);
                String dataStr = new String((byte[])blob.get(0));
                result = XMLHelper.readStrToDOM4J(dataStr);
            }
        } catch (Exception x) {
            log.error(x);
        }
        return result;

    }

    public static Element getDwcaSchemaFromFld(String sql) {
        Element result = null;
        try {
            Element el = getXmlElementFromFld(sql);
            if (el != null) {
                List<Object> core = el.selectNodes("/archive");
                if (core != null && core.size() > 0) {
                    Element archiveElement = (Element) core.get(0); //assuming 1
                    result = el;
                }
            }
        } catch (Exception x) {
            log.error(x);
        }
        return result;
    }

    public class DwcTermInfo {
        String term;
        String value;
        String rowType;
        Boolean isCore;

        public String getTerm() {
            return term;
        }

        public String getValue() {
            return value;
        }

        public DwcTermInfo(String term, String value) {
            this.term = term;
            this.value = value;
        }
    }

//    public static List<DwcTermInfo> getConceptsInDwcaSchema(Element schema) {
//        List<DwcTermInfo> result = new ArrayList<>();
//        for (Object archive : schema.selectNodes("/archive")) {
//            for (Object c : ((Element) archive).selectNodes("core")) {
//                files.add(new DarwinCoreArchiveFile("core".equals(((Element) c).getName()), (Element) c));
//            }
//        }
//
//    }
    /**
     *
     * @param mappingName
     * @return
     */
    public static String getDwcaSchemaAsStr(String mappingName) {
        Element el = getDwcaSchema(mappingName);
        if (el != null) {
            return el.asXML();
        } else {
            return null;
        }
    }
}
