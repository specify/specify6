package edu.ku.brc.specify.datamodel;





/**

 */
public class AppResourceData  implements java.io.Serializable {

    // Fields    

     protected Integer appResourceDataId;
     protected java.sql.Blob data;
     private AppResource AppResource;
     private ViewSetObj ViewSetObj;


    // Constructors

    /** default constructor */
    public AppResourceData() {
    }
    
    /** constructor with id */
    public AppResourceData(Integer appResourceDataId) {
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
    public Integer getAppResourceDataId() {
        return this.appResourceDataId;
    }
    
    public void setAppResourceDataId(Integer appResourceDataId) {
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