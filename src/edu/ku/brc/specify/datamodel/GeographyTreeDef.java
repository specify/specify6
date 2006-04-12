package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

public class GeographyTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer treeDefId;
     protected String name;
     protected String remarks;
     protected Set<Geography> treeEntries;
     protected Set<GeographyTreeDefItem> treeDefItems;
     protected Set<CollectionObjDef> collObjDefs;

    // Constructors

    /** default constructor */
    public GeographyTreeDef() {
    }
    
    /** constructor with id */
    public GeographyTreeDef(Integer treeDefId) {
        this.treeDefId = treeDefId;
    }
   
    
    // Initializer
    public void initialize()
    {
        treeDefId = null;
        name = null;
        remarks = null;
        treeEntries = new HashSet<Geography>();
        treeDefItems = new HashSet<GeographyTreeDefItem>();
        collObjDefs = new HashSet<CollectionObjDef>();
    }
    // End Initializer

    // Property accessors

    /**
     * 
     */
    public Integer getTreeDefId() {
        return this.treeDefId;
    }
    
    public void setTreeDefId(Integer treeDefId) {
        this.treeDefId = treeDefId;
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
    public Set getTreeEntries() {
        return this.treeEntries;
    }
    
    @SuppressWarnings("unchecked")
	public void setTreeEntries(Set treeEntries) {
        this.treeEntries = treeEntries;
    }

    /**
     * 
     */
    public Set getTreeDefItems() {
        return this.treeDefItems;
    }
    
    @SuppressWarnings("unchecked")
	public void setTreeDefItems(Set treeDefItems) {
        this.treeDefItems = treeDefItems;
    }

    /**
     * 
     */
    public Set getCollObjDefs() {
        return this.collObjDefs;
    }
    
    @SuppressWarnings("unchecked")
	public void setCollObjDefs(Set collObjDefs) {
        this.collObjDefs = collObjDefs;
    }
}