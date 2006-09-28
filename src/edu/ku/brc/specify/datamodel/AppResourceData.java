/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.datamodel;





/**

 */
public class AppResourceData extends DataModelObjBase implements java.io.Serializable {

    // Fields    

     protected Long appResourceDataId;
     protected java.sql.Blob data;
     private AppResource AppResource;
     private ViewSetObj ViewSetObj;


    // Constructors

    /** default constructor */
    public AppResourceData() {
    }
    
    /** constructor with id */
    public AppResourceData(Long appResourceDataId) {
        this.appResourceDataId = appResourceDataId;
    }
   
    
    public void initialize()
    {
        appResourceDataId = null;
        data = null;
    }
    

    // Property accessors

    /**
     * 
     */
    public Long getAppResourceDataId() {
        return this.appResourceDataId;
    }

    /**
     * Generic Getter for the ID Property.
     * @returns ID Property.
     */
    public Long getId()
    {
        return this.appResourceDataId;
    }
    
    public void setAppResourceDataId(Long appResourceDataId) {
        this.appResourceDataId = appResourceDataId;
    }

    /**
     * 
     */
    public java.sql.Blob getData() {
        return this.data;
    }
    
    public void setData(java.sql.Blob data) {
        this.data = data;
    }

    /**
     * 
     */
    public AppResource getAppResource() {
        return this.AppResource;
    }
    
    public void setAppResource(AppResource AppResource) {
        this.AppResource = AppResource;
    }

    /**
     * 
     */
    public ViewSetObj getViewSetObj() {
        return this.ViewSetObj;
    }
    
    public void setViewSetObj(ViewSetObj ViewSetObj) {
        this.ViewSetObj = ViewSetObj;
    }




}
