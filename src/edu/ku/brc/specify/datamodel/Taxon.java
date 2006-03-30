package edu.ku.brc.specify.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Date;
import java.util.Set;


/**

 */
public class Taxon  implements Treeable,java.awt.datatransfer.Transferable,java.io.Serializable {

    // Fields    

     protected Integer treeId;
     protected String taxonomicSerialNumber;
     protected String guid;
     protected String name;
     protected String unitInd1;
     protected String unitName1;
     protected String unitInd2;
     protected String unitName2;
     protected String unitInd3;
     protected String unitName3;
     protected String unitInd4;
     protected String unitName4;
     protected String fullName;
     protected String commonName;
     protected String author;
     protected String source;
     protected Integer groupPermittedToView;
     protected String environmentalProtectionStatus;
     protected String remarks;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected String lastEditedBy;
     protected Short accepted;
     protected Integer rankId;
     protected String groupNumber;
     protected Boolean isCurrent;
     private Set acceptedChildren;
     private Taxon acceptedTaxon;
     private Set taxonCitations;
     private TaxonTreeDef definition;
     private Taxon parent;
     private Set externalResources;


    // Constructors

    /** default constructor */
    public Taxon() {
    }
    
    /** constructor with id */
    public Taxon(Integer treeId) {
        this.treeId = treeId;
    }
   
    
    

    // Property accessors

    /**
     *      *  @hibernate.id generator-class="assigned"type="java.lang.Integer" column="TreeID" 
     */
    public Integer getTreeId() {
        return this.treeId;
    }
    
    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
    }

    /**
     *      *  @hibernate.property column="TaxonomicSerialNumber"length="50" 
     */
    public String getTaxonomicSerialNumber() {
        return this.taxonomicSerialNumber;
    }
    
    public void setTaxonomicSerialNumber(String taxonomicSerialNumber) {
        this.taxonomicSerialNumber = taxonomicSerialNumber;
    }

    /**
     *      *  @hibernate.property column="GUID" length="255"
     */
    public String getGuid() {
        return this.guid;
    }
    
    public void setGuid(String guid) {
        this.guid = guid;
    }

    /**
     *      *  @hibernate.property column="Name" length="50"
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *  @hibernate.property column="UnitInd1" length="50"
     */
    public String getUnitInd1() {
        return this.unitInd1;
    }
    
    public void setUnitInd1(String unitInd1) {
        this.unitInd1 = unitInd1;
    }

    /**
     *      *  @hibernate.property column="UnitName1" length="50"
     */
    public String getUnitName1() {
        return this.unitName1;
    }
    
    public void setUnitName1(String unitName1) {
        this.unitName1 = unitName1;
    }

    /**
     *      *  @hibernate.property column="UnitInd2" length="50"
     */
    public String getUnitInd2() {
        return this.unitInd2;
    }
    
    public void setUnitInd2(String unitInd2) {
        this.unitInd2 = unitInd2;
    }

    /**
     *      *  @hibernate.property column="UnitName2" length="50"
     */
    public String getUnitName2() {
        return this.unitName2;
    }
    
    public void setUnitName2(String unitName2) {
        this.unitName2 = unitName2;
    }

    /**
     *      *  @hibernate.property column="UnitInd3" length="50"
     */
    public String getUnitInd3() {
        return this.unitInd3;
    }
    
    public void setUnitInd3(String unitInd3) {
        this.unitInd3 = unitInd3;
    }

    /**
     *      *  @hibernate.property column="UnitName3" length="50"
     */
    public String getUnitName3() {
        return this.unitName3;
    }
    
    public void setUnitName3(String unitName3) {
        this.unitName3 = unitName3;
    }

    /**
     *      *  @hibernate.property column="UnitInd4" length="50"
     */
    public String getUnitInd4() {
        return this.unitInd4;
    }
    
    public void setUnitInd4(String unitInd4) {
        this.unitInd4 = unitInd4;
    }

    /**
     *      *  @hibernate.property column="UnitName4" length="50"
     */
    public String getUnitName4() {
        return this.unitName4;
    }
    
    public void setUnitName4(String unitName4) {
        this.unitName4 = unitName4;
    }

    /**
     *      *  @hibernate.property column="FullName" length="255"
     */
    public String getFullName() {
        return this.fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     *      *  @hibernate.property column="CommonName"length="128" 
     */
    public String getCommonName() {
        return this.commonName;
    }
    
    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     *      *  @hibernate.property column="Author" length="128"
     */
    public String getAuthor() {
        return this.author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     *      *  @hibernate.property column="Source" length="64"
     */
    public String getSource() {
        return this.source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }

    /**
     *      *  @hibernate.property column="GroupPermittedToView"length="10" 
     */
    public Integer getGroupPermittedToView() {
        return this.groupPermittedToView;
    }
    
    public void setGroupPermittedToView(Integer groupPermittedToView) {
        this.groupPermittedToView = groupPermittedToView;
    }

    /**
     *      *  @hibernate.propertycolumn="EnvironmentalProtectionStatus" length="64" 
     */
    public String getEnvironmentalProtectionStatus() {
        return this.environmentalProtectionStatus;
    }
    
    public void setEnvironmentalProtectionStatus(String environmentalProtectionStatus) {
        this.environmentalProtectionStatus = environmentalProtectionStatus;
    }

    /**
     *      *  @hibernate.property column="Remarks" 
     */
    public String getRemarks() {
        return this.remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     *      *  @hibernate.property column="NodeNumber" length="10"index="IX_TXN_NodeNumber" 
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *  @hibernate.property column="HighestChildNodeNumber"length="10" index="IX_TXN_HighestChildNodeNumber" 
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     *      *  @hibernate.property column="TimestampCreated"length="23" 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *  @hibernate.property column="TimestampModified"length="23" 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *  @hibernate.property column="LastEditedBy"length="50" 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *  @hibernate.property column="Accepted" 
     */
    public Short getAccepted() {
        return this.accepted;
    }
    
    public void setAccepted(Short accepted) {
        this.accepted = accepted;
    }

    /**
     *      *  @hibernate.property column="RankID" length="10"
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *  @hibernate.property column="GroupNumber"length="20" 
     */
    public String getGroupNumber() {
        return this.groupNumber;
    }
    
    public void setGroupNumber(String groupNumber) {
        this.groupNumber = groupNumber;
    }

    /**
     * 
     */
    public Boolean getIsCurrent() {
        return this.isCurrent;
    }
    
    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    /**
     *      *  @hibernate.set lazy="true" inverse="true"cascade="none" @hibernate.collection-key column="AcceptedID"@hibernate.collection-one-to-many class="edu.ku.brc.specify.datamodel.Taxon" 
     */
    public Set getAcceptedChildren() {
        return this.acceptedChildren;
    }
    
    public void setAcceptedChildren(Set acceptedChildren) {
        this.acceptedChildren = acceptedChildren;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"@hibernate.column name="AcceptedID" 
     */
    public Taxon getAcceptedTaxon() {
        return this.acceptedTaxon;
    }
    
    public void setAcceptedTaxon(Taxon acceptedTaxon) {
        this.acceptedTaxon = acceptedTaxon;
    }

    /**
     *      *  @hibernate.set lazy="true" inverse="true"cascade="none" @hibernate.collection-key column="TaxonID"@hibernate.collection-one-to-many class="edu.ku.brc.specify.datamodel.TaxonCitation" 
     */
    public Set getTaxonCitations() {
        return this.taxonCitations;
    }
    
    public void setTaxonCitations(Set taxonCitations) {
        this.taxonCitations = taxonCitations;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"@hibernate.column name="TreeDefID" 
     */
    public TaxonTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(TaxonTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"@hibernate.column name="ParentID" 
     */
    public Taxon getParent() {
        return this.parent;
    }
    
    public void setParent(Taxon parent) {
        this.parent = parent;
    }

    /**
     * 
     */
    public Set getExternalResources() {
        return this.externalResources;
    }
    
    public void setExternalResources(Set externalResources) {
        this.externalResources = externalResources;
    }




  // The following is extra code specified in the hbm.xml files

            
    		/**
    		 * @return the parent Taxon object
    		 */
    		public Treeable getParentNode()
    		{
    			return getParent();
    		}
    		
    		/**
    		 * @param parent the new parent Taxon object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of Taxon
    		 */
    		public void setParentNode(Treeable parent)
    		{
    			if( !(parent instanceof Taxon) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of Taxon");
    			}
    			setParent((Taxon)parent);
    		}
    		
    		/**
    		 * @return the parent TaxonTreeDef object
    		 */
    		public TreeDefinitionIface getTreeDef()
    		{
    			return getDefinition();
    		}
    		
    		/**
    		 * @param parent the new TaxonTreeDef object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of TaxonTreeDef
    		 */
    		public void setTreeDef(TreeDefinitionIface treeDef)
    		{
    			if( !(treeDef instanceof TaxonTreeDef) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of TaxonTreeDef");
    			}
    			
    			setDefinition((TaxonTreeDef)treeDef);
    		}
    		
    		/**
    		 * @param other the Treeable to compare to
    		 */
    		public int compareTo(Treeable other)
    		{
    			return name.compareTo(other.getName());
    		}
    		
    		public DataFlavor[] getTransferDataFlavors()
    		{
    		    DataFlavor[] flavors = new DataFlavor[1];
    		    try
    		    {
    		        flavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
    		                ";class=edu.ku.brc.specify.datamodel.Treeable");
    		    }
    		    catch( ClassNotFoundException ex )
    		    {
    		        //TODO: What do we want to do here?
    		    }
    		
    		    return flavors;
	        }

        	public boolean isDataFlavorSupported( DataFlavor flavor )
        	{
    		    DataFlavor[] flavors = new DataFlavor[1];
    		    try
    		    {
    		        flavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
    		                ";class=edu.ku.brc.specify.datamodel.Treeable");
    		    }
    		    catch( ClassNotFoundException ex )
    		    {
    		        //TODO: What do we want to do here?
    		    }

        		for( DataFlavor df: flavors )
        		{
        			if( df.equals(flavor) )
        			{
        				return true;
        			}
        		}
        		
        		return false;
        	}

        	public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException
        	{
    		    DataFlavor[] flavors = new DataFlavor[1];
    		    try
    		    {
    		        flavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType +
    		                ";class=edu.ku.brc.specify.datamodel.Treeable");
    		    }
    		    catch( ClassNotFoundException ex )
    		    {
    		        //TODO: What do we want to do here?
    		    }

        		if( flavor.equals(flavors[0]) )
        		{
        			return this;
        		}
        		else
        		{
        			return null;
        		}
        	}
    	    
        
  // end of extra code specified in the hbm.xml files
}