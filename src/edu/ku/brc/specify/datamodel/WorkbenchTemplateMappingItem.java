package edu.ku.brc.specify.datamodel;

import java.util.*;




/**

 */
public class WorkbenchTemplateMappingItem  implements java.io.Serializable {

    // Fields    

     protected Integer workbenchTemplateMappingItemID;
     protected String tablename;
     protected Integer tableid;
     protected String fieldname;
     private String caption;
     private Integer vieworder;
     private String datatype;
     private Date timestampModified;
     private Date timestampCreated;
     protected WorkbenchTemplate workbenchTemplates;


    // Constructors

    /** default constructor */
    public WorkbenchTemplateMappingItem() {
    }
    
    /** constructor with id */
    public WorkbenchTemplateMappingItem(Integer workbenchTemplateMappingItemID) {
        this.workbenchTemplateMappingItemID = workbenchTemplateMappingItemID;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getWorkbenchTemplateMappingItemID() {
        return this.workbenchTemplateMappingItemID;
    }
    
    public void setWorkbenchTemplateMappingItemID(Integer workbenchTemplateMappingItemID) {
        this.workbenchTemplateMappingItemID = workbenchTemplateMappingItemID;
    }

    /**
     * 
     */
    public String getTablename() {
        return this.tablename;
    }
    
    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    /**
     * 
     */
    public Integer getTableid() {
        return this.tableid;
    }
    
    public void setTableid(Integer tableid) {
        this.tableid = tableid;
    }

    /**
     * 
     */
    public String getFieldname() {
        return this.fieldname;
    }
    
    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    /**
     * 
     */
    public String getCaption() {
        return this.caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }

    /**
     * 
     */
    public Integer getVieworder() {
        return this.vieworder;
    }
    
    public void setVieworder(Integer vieworder) {
        this.vieworder = vieworder;
    }

    /**
     * 
     */
    public String getDatatype() {
        return this.datatype;
    }
    
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
     * 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     * 
     */
    public WorkbenchTemplate getWorkbenchTemplates() {
        return this.workbenchTemplates;
    }
    
    public void setWorkbenchTemplates(WorkbenchTemplate workbenchTemplates) {
        this.workbenchTemplates = workbenchTemplates;
    }




}