package edu.ku.brc.specify.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Date;


/**

 */
public class GeologicTimePeriod  implements Treeable,java.awt.datatransfer.Transferable,java.io.Serializable {

    // Fields    

     protected Integer treeId;
     protected Integer rankId;
     protected String name;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String standard;
     protected Float age;
     protected Float ageUncertainty;
     protected String remarks;
     protected Date timestampModified;
     protected Date timestampCreated;
     protected Date timestampVersion;
     protected String lastEditedBy;
     private GeologicTimePeriodTreeDef definition;
     private GeologicTimePeriod parent;


    // Constructors

    /** default constructor */
    public GeologicTimePeriod() {
    }
    
    /** constructor with id */
    public GeologicTimePeriod(Integer treeId) {
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
     *      *  @hibernate.property column="RankID" length="10"index="IX_GTP_RankID" 
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *  @hibernate.property column="Name" length="64"
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *  @hibernate.property column="NodeNumber" length="10"index="IX_GTP_NodeNumber" 
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *  @hibernate.property column="HighestChildNodeNumber"length="10" index="IX_GTP_NighestChildNodeNumber" 
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     *      *  @hibernate.property column="Standard" length="64"
     */
    public String getStandard() {
        return this.standard;
    }
    
    public void setStandard(String standard) {
        this.standard = standard;
    }

    /**
     *      *  @hibernate.property column="Age" length="24"
     */
    public Float getAge() {
        return this.age;
    }
    
    public void setAge(Float age) {
        this.age = age;
    }

    /**
     *      *  @hibernate.property column="AgeUncertainty"length="24" 
     */
    public Float getAgeUncertainty() {
        return this.ageUncertainty;
    }
    
    public void setAgeUncertainty(Float ageUncertainty) {
        this.ageUncertainty = ageUncertainty;
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
     *      *  @hibernate.property column="TimestampModified"length="23" 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
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
     *      *  @hibernate.property column="TimestampVersion"length="16" 
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
    }

    /**
     *      *  @hibernate.property column="LastEditedBy"length="32" 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"@hibernate.column name="TreeDefID" 
     */
    public GeologicTimePeriodTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeologicTimePeriodTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"@hibernate.column name="ParentID" 
     */
    public GeologicTimePeriod getParent() {
        return this.parent;
    }
    
    public void setParent(GeologicTimePeriod parent) {
        this.parent = parent;
    }




  // The following is extra code specified in the hbm.xml files

            
    		/**
    		 * @return the parent GeologicTimePeriod object
    		 */
    		public Treeable getParentNode()
    		{
    			return getParent();
    		}
    		
    		/**
    		 * @param parent the new parent GeologicTimePeriod object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of GeologicTimePeriod
    		 */
    		public void setParentNode(Treeable parent)
    		{
    			if( !(parent instanceof GeologicTimePeriod) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriod");
    			}
    			setParent((GeologicTimePeriod)parent);
    		}
    		
    		/**
    		 * @return the parent GeologicTimePeriodTreeDef object
    		 */
    		public TreeDefinitionIface getTreeDef()
    		{
    			return getDefinition();
    		}
    		
    		/**
    		 * @param treeDef the new GeologicTimePeriodTreeDef object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of GeologicTimePeriodTreeDef
    		 */
    		public void setTreeDef(TreeDefinitionIface treeDef)
    		{
    			if( !(treeDef instanceof GeologicTimePeriodTreeDef) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of GeologicTimePeriodTreeDef");
    			}
    			
    			setDefinition((GeologicTimePeriodTreeDef)treeDef);
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