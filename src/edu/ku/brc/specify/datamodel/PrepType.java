package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;




/**

 */
public class PrepType  implements java.io.Serializable {

    // Fields    

     protected Integer prepTypeId;
     protected String name;
     protected Set<Preparation> preparations;
     protected Set<AttributeDef> attributeDefs;


    // Constructors

    /** default constructor */
    public PrepType() {
    }
    
    /** constructor with id */
    public PrepType(Integer prepTypeId) {
        this.prepTypeId = prepTypeId;
    }
   
    
    

    // Initializer
    public void initialize()
    {
        prepTypeId = null;
        name = null;
        preparations = new HashSet<Preparation>();
        attributeDefs = new HashSet<AttributeDef>();
    }
    // End Initializer

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
    public Set<Preparation> getPreparations() {
        return this.preparations;
    }
    
    public void setPreparations(Set<Preparation> preparations) {
        this.preparations = preparations;
    }

    /**
     * 
     */
    public Set<AttributeDef> getAttributeDefs() {
        return this.attributeDefs;
    }
    
    public void setAttributeDefs(Set<AttributeDef> attributeDefs) {
        this.attributeDefs = attributeDefs;
    }




    // Add Methods

    public void addPreparation(final Preparation preparation)
    {
        this.preparations.add(preparation);
    }

    public void addAttributeDef(final AttributeDef attributeDef)
    {
        this.attributeDefs.add(attributeDef);
    }

    // Done Add Methods
}
