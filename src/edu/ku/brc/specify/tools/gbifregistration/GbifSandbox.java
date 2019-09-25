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
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GbifSandbox {
    private static final Logger log = Logger.getLogger(GbifSandbox.class);

    private static String api = "api.gbif-uat.org";
    private static String apiUrl = "http://" + api + "/v1/";

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

    /**
     *
     * @param data
     * @param apiFn
     * @return
     */
    public Pair<Boolean, String> postToGbifRegistry(final JSONObject data, final String apiFn) {
        PostMethod postMethod  = new PostMethod(apiUrl + apiFn);
        try {
            StringRequestEntity se = new StringRequestEntity(data.toString(), "application/json", "utf-8");
            postMethod.setRequestEntity(se);
            return executeMethod(postMethod, HttpStatus.SC_CREATED);
        } catch (java.io.UnsupportedEncodingException x) {
            log.error(x);
            edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
            edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(GbifSandbox.class, x);
            return new Pair<>(false, x.getMessage());
        }
    }

    /**
     *
     * @param method
     * @param successCode
     * @return
     */
    public Pair<Boolean, String> executeMethod(HttpMethod method, int successCode) {
        try {
            HttpClient client = new HttpClient();
            Pair<String, String> gbifUser = getGbifRegCredentials();
            Credentials credentials =  new UsernamePasswordCredentials(gbifUser.getFirst(), gbifUser.getSecond());
            client.getState().setCredentials(AuthScope.ANY, credentials);
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
            client.getParams().setAuthenticationPreemptive(true);
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
     * @param org
     * @return
     */
    public Pair<Boolean, String> registerOrganization(final JSONObject org) {
        return postToGbifRegistry(org, "organization");
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
        return postToGbifRegistry(coll, "grscicoll/collection");
    }

    public Pair<Boolean, String> registerAGrSciCollCollection() {
        JSONObject data = new JSONObject();
        data.put("code", "SoS6");
        data.put("name", "Spawn of Specify 6");
        return postToGbifRegistry(data, "grscicoll/collection");
    }

    /**
     *
     * @param dataset
     * @return
     */
    public Pair<Boolean, String> registerDataset(final JSONObject dataset) {
        return postToGbifRegistry(dataset, "dataset");
    }

    public Pair<Boolean, String> registerADataset() {
        JSONObject data = new JSONObject();
        data.put("installationKey", "5b8e7001-516d-4880-bd68-826948bbffec");
        data.put("publishingOrganizationKey", "6a078fc1-c9d9-416d-98e6-a3a3c33183e6");
        data.put("type", "OCCURRENCE");
        data.put("title", "eve of deletion");
        return registerDataset(data);
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
        DeleteMethod method  = new DeleteMethod(apiUrl + "dataset/" + toDelete);
        return executeMethod(method, HttpStatus.SC_NO_CONTENT);
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
