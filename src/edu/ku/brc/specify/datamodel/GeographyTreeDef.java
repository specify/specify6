package edu.ku.brc.specify.datamodel;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public class GeographyTreeDef  implements TreeDefinitionIface,java.io.Serializable {

    // Fields    

     protected Integer geographyTreeDefId;
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
    public GeographyTreeDef(Integer geographyTreeDefId) {
        this.geographyTreeDefId = geographyTreeDefId;
    }
   
    
    // Initializer
    public void initialize()
    {
    	geographyTreeDefId = null;
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
    public Integer getGeographyTreeDefId() {
        return this.geographyTreeDefId;
    }
    
    public void setGeographyTreeDefId(Integer geographyTreeDefId) {
        this.geographyTreeDefId = geographyTreeDefId;
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

    //
    // Methods added to implement TreeDefinitionIface
    //
    
	public Integer getTreeDefId()
	{
		return getGeographyTreeDefId();
	}

	public void setTreeDefId(Integer id)
	{
		setGeographyTreeDefId(id);
	}
}