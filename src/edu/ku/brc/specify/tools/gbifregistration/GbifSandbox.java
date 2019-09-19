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
import edu.ku.brc.ui.UIRegistry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GbifSandbox {
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
        }
        return null;
    }

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

    private void hashMapFromJSON2(Object obj, Object key, HashMap<Object, Object> map) {
        if (obj instanceof JSONArray) {
            map.put(key, fromJSONArray((JSONArray)obj));
        } else if (obj instanceof JSONObject) {
            map.put(key, hashMapFromJSON((JSONObject) obj));
        } else {
            map.put(key, obj);
        }
    }

    private HashMap<Object, Object> hashMapFromJSON(JSONObject o) {
        HashMap<Object, Object> result = new HashMap<>();
        for (Object key : o.keySet()) {
            System.out.println(key);
            hashMapFromJSON2(o.get(key), key, result);
        }
        return result;
    }

}
