package edu.ku.brc.specify.datamodel;

import java.util.Date;
import java.util.Set;




/**

 */
public class WorkbenchTemplate  implements java.io.Serializable {

    // Fields    

     protected Long workbenchTemplateId;
     protected String name;
     protected String remarks;
     private Date timestampModified;
     private Date timestampCreated;
     protected Set workbenches;
     protected Set workbenchtemplatemappingitems;


    // Constructors

    /** default constructor */
    public WorkbenchTemplate() {
    }
    
    /** constructor with id */
    public WorkbenchTemplate(Long workbenchTemplateId) {
        this.workbenchTemplateId = workbenchTemplateId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Long getWorkbenchTemplateId() {
        return this.workbenchTemplateId;
    }
    
    public void setWorkbenchTemplateId(Long workbenchTemplateId) {
        this.workbenchTemplateId = workbenchTemplateId;
    }

    /**
     * 
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    public Set getWorkbenches() {
        return this.workbenches;
    }
    
    public void setWorkbenches(Set workbenches) {
        this.workbenches = workbenches;
    }

    /**
     * 
     */
    public Set getWorkbenchtemplatemappingitems() {
        return this.workbenchtemplatemappingitems;
    }
    
    public void setWorkbenchtemplatemappingitems(Set workbenchtemplatemappingitems) {
        this.workbenchtemplatemappingitems = workbenchtemplatemappingitems;
    }




}