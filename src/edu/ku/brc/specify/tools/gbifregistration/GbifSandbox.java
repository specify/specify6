/* Copyright (C) 2019, University of Kansas Center for Research
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


import edu.ku.brc.af.ui.ViewBasedDialogFactoryIFace;
import edu.ku.brc.af.ui.db.ViewBasedDisplayIFace;
import edu.ku.brc.helpers.ProxyHelper;
import edu.ku.brc.ui.UIRegistry;
import edu.ku.brc.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class GbifSandbox {
    private static final Logger log = Logger.getLogger(GbifSandbox.class);

    private static String api = "api.gbif-uat.org";
    private static String gbifApiUri = "http://" + api + "/v1/";
    private static String zenodoApi = "sandbox.zenodo.org/api";
    private static String zenodoApiUrl = "https://" + zenodoApi + "/";

    private void makeAJsonObject() {
        //JSONObject o = JSONObject.fromObject("{\"name\": \"BoB\"}");
        JSONObject o = JSONObject.fromObject("{\"key\":\"04672bb4-5621-4b5b-949d-63ceea77ae24\",\"code\":\"COCOA\",\"name\":\"Colorado College Arthropod Collection\",\"contentTypes\":[\"BIOLOGICAL_PRESERVED_ORGANISMS\"],\"active\":true,\"personalCollection\":false,\"homepage\":\"https://www.coloradocollege.edu/academics/dept/obe/BiodiversityCollections/entomology-collection.html\",\"accessionStatus\":\"INSTITUTIONAL\",\"institutionKey\":\"58554974-6af4-4082-b036-259442c1c0a4\",\"mailingAddress\":{\"key\":11393,\"address\":\"Attn: Steven J Taylor, Office of General Studies, Colorado College, 14 E Cache La Poudre St.\",\"city\":\"Colorado Springs\",\"province\":\"Colorado\",\"postalCode\":\"80903\",\"country\":\"US\"},\"createdBy\":\"GRBIO\",\"modifiedBy\":\"registry-migration-grbio.gbif.org\",\"created\":\"2018-05-08T09:43:00.000+0000\",\"modified\":\"2018-11-15T10:23:01.527+0000\",\"tags\":[],\"identifiers\":[{\"key\":171542,\"type\":\"GRBIO_URI\",\"identifier\":\"http://grscicoll.org/institutional-collection/colorado-college-arthropod-collection\",\"createdBy\":\"registry-migration-grbio.gbif.org\",\"created\":\"2019-08-15T08:12:24.368+0000\"},{\"key\":163383,\"type\":\"GRBIO_URI\",\"identifier\":\"http://grbio.org/institutional-collection/colorado-college-arthropod-collection\",\"createdBy\":\"registry-migration-grbio.gbif.org\",\"created\":\"2019-08-15T08:12:17.277+0000\"},{\"key\":146265,\"type\":\"GRBIO_ID\",\"identifier\":\"25845\",\"createdBy\":\"registry-migration-grbio.gbif.org\",\"created\":\"2018-11-15T10:23:01.527+0000\"}],\"contacts\":[{\"key\":\"342bd382-da9e-46e2-a88f-ebbb559544f8\",\"firstName\":\"Steven J. Taylor\",\"position\":\"Associate Research Professor\",\"phone\":\"217.714.2871\",\"email\":\"sjtaylor@coloradocollege.edu\",\"mailingAddress\":{\"key\":24761,\"address\":\"Office of General Studies, Colorado College, 14 E Cache La Poudre St\",\"city\":\"Colorado Springs\",\"province\":\"Colorado\",\"postalCode\":\"80903\",\"country\":\"US\"},\"primaryInstitutionKey\":\"58554974-6af4-4082-b036-259442c1c0a4\",\"createdBy\":\"GRBIO\",\"modifiedBy\":\"registry-migration-grbio.gbif.org\",\"created\":\"2018-05-08T09:34:00.000+0000\",\"modified\":\"2018-11-15T10:23:01.527+0000\"}]}");
        System.out.println((o.get("key")));
        HashMap<Object, Object> m = hashMapFromJSON(o);
        System.out.println(m);
        getACollectionFromGBIF();
        makeADlg();
    }

    public void makeADlg() {
        ViewBasedDisplayIFace dlg2 = UIRegistry.getViewbasedFactory().createDisplay(UIRegistry.getMostRecentWindow(),
                "GBIFCollection",
                "GEE! BIFF",
                "GO",
                true,
                0,
                null,
                ViewBasedDialogFactoryIFace.FRAME_TYPE.DIALOG);
        HashMap<Object, Object> co = hashMapFromJSON(getACollectionFromGBIF());
        dlg2.setData(co);
        dlg2.showDisplay(true);
        System.out.println(co);
    }
    public JSONObject getACollectionFromGBIF() {
        HttpClient httpClient = new HttpClient();
        httpClient.getParams().setParameter("http.useragent", getClass().getName()); //$NON-NLS-1$
        httpClient.getParams().setParameter("http.socket.timeout", 15000);

        String url = "http://api.gbif.org/v1/grscicoll/collection/04672bb4-5621-4b5b-949d-63ceea77ae24";
        GetMethod getMethod = new GetMethod(url);
        try {
            httpClient.executeMethod(getMethod);
            String jsonResponse = getMethod.getResponseBodyAsString();
            JSONObject r = JSONObject.fromObject(jsonResponse);
            return r;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            log.error(e);
        }
        return null;
    }



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
            StringRequestEntity se = new StringRequestEntity(data.toString(), "application/json", "utf-8");
            return postToApi(gbifApiUri + apiFn, se, getGbifRegCredentials(), null, null, null);
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
    public Pair<Boolean, String> postToApi(final String uri, final RequestEntity re, final Pair<String, String> credentials, final List<Pair<String, String>> params, final List<Pair<String, String>> headers, final Part[] parts) {
        PostMethod postMethod  = new PostMethod(uri);
        if (re != null) {
            postMethod.setRequestEntity(re);
        }
        if (headers != null) {
            for (Pair<String, String> h : headers) {
                postMethod.addRequestHeader(h.getFirst(), h.getSecond());
            }
        }
        if (params != null) {
            for (Pair<String, String> p : params) {
                postMethod.addParameter(p.getFirst(), p.getSecond());
            }
        }
        if (parts != null) {
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
        }
        return executeMethod(postMethod, credentials, HttpStatus.SC_CREATED);
    }



    /**
     *
     * @param method
     * @param successCode
     * @return
     */
    public Pair<Boolean, String> executeMethod(HttpMethod method, final Pair<String, String> credentials, int successCode) {
        try {
            HttpClient client = new HttpClient();
            if (credentials != null) {
                Credentials creds = new UsernamePasswordCredentials(credentials.getFirst(), credentials.getSecond());
                client.getState().setCredentials(AuthScope.ANY, creds);
                client.getParams().setAuthenticationPreemptive(true);
            }
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            ProxyHelper.applyProxySettings(client);
            int status = client.executeMethod(method);
            String response = method.getResponseBodyAsString();
            boolean success = status == successCode;
            return new Pair<>(success, StringUtils.isEmpty(response) ? "httpstatus: " + status : response);
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

    public Pair<Boolean, String>  uploadToZenodo(File file) {
        //List<Pair<String, String>> params = new ArrayList<>();
        List<Pair<String, String>> headers = new ArrayList<>();
        headers.add(new Pair<>("Content-Type", "application/json"));
        //params.add(new Pair<>("access_token", getZenodoUploadToken()));
        try {
            StringRequestEntity se = new StringRequestEntity("{}", "application/json", "utf-8");
            Pair<Boolean, String> result1 = postToApi(zenodoApiUrl + "deposit/depositions?access_token=" + getZenodoUploadToken(), se, null, null, headers, null);
            //Pair<Boolean, String> result1 = postToApi(zenodoApiUrl + "deposit/depositions", se, null, params, headers);
            if (!result1.getFirst()) {
                return result1;
            }

            JSONObject response = JSONObject.fromObject(result1.getSecond());
            String depositId = response.getString("id");


            Part[] parts = {
                    new StringPart("data", "{'filename': '" + file.getName()+ "'}", "utf-8"),
                    new FilePart("file", file.getName(), file)
            };
            ((StringPart)parts[0]).setContentType("application/json");

            //
            // FileRequestEntity fe = new FileRequestEntity(file, "text/csv");

            return postToApi(zenodoApiUrl + "deposit/depositions/" + depositId + "/files?access_token=" + getZenodoUploadToken(), null, null, null, headers, parts);

        } catch (java.io.FileNotFoundException | java.io.UnsupportedEncodingException x) {
            return new Pair<>(false, x.getMessage());
        }
        //return postToApi(zenodoApiUrl + "deposit/depositions/" + )

//        try {
//
//            post.setRequestEntity(entity);
//
//            //System.out.println("SKIPPING the POST!!!");
//            int postStatus = /*200;*/ httpClient.executeMethod(post);
//            //System.out.println("Status from Symbiota Post: " + postStatus);
//            if (postStatus == 200) {
//                byte[] responseBytes = post.getResponseBody();
//                String response = responseBytes == null ? "" : new String(responseBytes);
//                System.out.println("ResponseBody: " + response);
//                if (!response.startsWith("FAILED") && !response.startsWith("ERROR")) {
//                    result.setFirst(true);
//                }
//                result.setSecond(response);
//            } else {
//                result.setSecond(String.format(UIRegistry.getResourceString("SymbiotaPane.BadPostStatus"), postStatus));
//            }
//
//        } catch (Exception ex) {
//            //ex.printStackTrace();
//            result.setSecond(ex.getLocalizedMessage());
//        }
//        return result;
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
        DeleteMethod method  = new DeleteMethod(gbifApiUri + "dataset/" + toDelete);
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

}
