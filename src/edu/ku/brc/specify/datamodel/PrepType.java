package edu.ku.brc.specify.datamodel;

import java.util.Set;




/**

 */
public class PrepType  implements java.io.Serializable {

    // Fields    

     protected Integer prepTypeId;
     protected String name;
     protected Set preparations;
     protected Set attributeDefs;


    // Constructors

    /** default constructor */
    public PrepType() {
    }
    
    /** constructor with id */
    public PrepType(Integer prepTypeId) {
        this.prepTypeId = prepTypeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getPrepTypeId() {
        return this.prepTypeId;
    }
    
    public void setPrepTypeId(Integer prepTypeId) {
        this.prepTypeId = prepTypeId;
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
    public Set getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set preparations) {
        this.preparations = preparations;
    }

    /**
     * 
     */
    public Set getAttributeDefs() {
        return this.attributeDefs;
    }
    
    public void setAttributeDefs(Set attributeDefs) {
        this.attributeDefs = attributeDefs;
    }




}