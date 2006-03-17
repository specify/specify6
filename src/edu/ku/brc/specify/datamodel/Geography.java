package edu.ku.brc.specify.datamodel;

import java.util.*;


import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.awt.datatransfer.UnsupportedFlavorException;


/**
 *  @hibernate.class table="geography" 
 */
public class Geography  implements Treeable,java.awt.datatransfer.Transferable,java.io.Serializable {

    // Fields    

     protected Integer treeId;
     protected String name;
     protected Integer rankId;
     protected Integer nodeNumber;
     protected Integer highestChildNodeNumber;
     protected String abbrev;
     protected String text1;
     protected String text2;
     protected Integer number1;
     protected Integer number2;
     protected Date timestampCreated;
     protected Date timestampModified;
     protected Date timestampVersion;
     protected String lastEditedBy;
     protected Boolean current;
     private Set localities;
     private GeographyTreeDef definition;
     private Geography parent;


    // Constructors

    /** default constructor */
    public Geography() {
    }
    
    /** constructor with id */
    public Geography(Integer treeId) {
        this.treeId = treeId;
    }
   
    
    

    // Property accessors

    /**
     *      *  @hibernate.id generator-class="assigned"
     *                 type="java.lang.Integer" column="TreeID" 
     */
    public Integer getTreeId() {
        return this.treeId;
    }
    
    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
    }

    /**
     *      *  @hibernate.property column="Name" length="128"
     *             
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    /**
     *      *  @hibernate.property column="RankID" length="10"
     *                 index="IX_GeoRankID" not-null="true" 
     */
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     *      *  @hibernate.property column="NodeNumber" length="10"
     *                 index="IX_GeoNodeNumber" 
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     *      *  @hibernate.property column="HighestChildNodeNumber"
     *                 length="10" index="IX_GeoHighestChildNodeNumber" 
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     *      *  @hibernate.property column="Abbrev" length="16"
     *             
     */
    public String getAbbrev() {
        return this.abbrev;
    }
    
    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    /**
     *      *  @hibernate.property column="Text1" length="32"
     *             
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     *      *  @hibernate.property column="Text2" length="32"
     *             
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     *      *  @hibernate.property column="Number1" length="10"
     *             
     */
    public Integer getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Integer number1) {
        this.number1 = number1;
    }

    /**
     *      *  @hibernate.property column="Number2" length="10"
     *             
     */
    public Integer getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Integer number2) {
        this.number2 = number2;
    }

    /**
     *      *  @hibernate.property column="TimestampCreated"
     *                 length="23" 
     */
    public Date getTimestampCreated() {
        return this.timestampCreated;
    }
    
    public void setTimestampCreated(Date timestampCreated) {
        this.timestampCreated = timestampCreated;
    }

    /**
     *      *  @hibernate.property column="TimestampModified"
     *                 length="23" 
     */
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     *      *  @hibernate.property column="TimestampVersion"
     *                 length="16" 
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
    }

    /**
     *      *  @hibernate.property column="LastEditedBy"
     *                 length="50" 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     *      *  @hibernate.property column="Current" 
     */
    public Boolean getCurrent() {
        return this.current;
    }
    
    public void setCurrent(Boolean current) {
        this.current = current;
    }

    /**
     *      *  @hibernate.set lazy="true" inverse="true"
     *                 cascade="none" @hibernate.collection-key column="TreeID"
     *                 @hibernate.collection-one-to-many class="edu.ku.brc.specify.datamodel.Locality" 
     */
    public Set getLocalities() {
        return this.localities;
    }
    
    public void setLocalities(Set localities) {
        this.localities = localities;
    }

    /**
     *      *  @hibernate.many-to-one not-null="true"
     *                 @hibernate.column name="TreeDefID" 
     */
    public GeographyTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(GeographyTreeDef definition) {
        this.definition = definition;
    }

    /**
     *      *  @hibernate.many-to-one not-null="false"
     *                 @hibernate.column name="ParentID" 
     */
    public Geography getParent() {
        return this.parent;
    }
    
    public void setParent(Geography parent) {
        this.parent = parent;
    }

  /**
	 * toString
	 * @return String
	 */
  public String toString() {
	  StringBuffer buffer = new StringBuffer();

      buffer.append(getClass().getName()).append("@").append(Integer.toHexString(hashCode())).append(" [");
      buffer.append("treeId").append("='").append(getTreeId()).append("' ");			
      buffer.append("name").append("='").append(getName()).append("' ");			
      buffer.append("nodeNumber").append("='").append(getNodeNumber()).append("' ");			
      buffer.append("highestChildNodeNumber").append("='").append(getHighestChildNodeNumber()).append("' ");			
      buffer.append("]");
      
      return buffer.toString();
	}



  // The following is extra code specified in the hbm.xml files

            
    		/**
    		 * @return the parent Geography object
    		 */
    		public Treeable getParentNode()
    		{
    			return getParent();
    		}
    		
    		/**
    		 * @param parent the new parent Geography object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of Geography
    		 */
    		public void setParentNode(Treeable parent)
    		{
    			if( !(parent instanceof Geography) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of Geography");
    			}
    			setParent((Geography)parent);
    		}
    		
    		/**
    		 * @return the parent GeographyTreeDef object
    		 */
    		public TreeDefinitionIface getTreeDef()
    		{
    			return getDefinition();
    		}
    		
    		/**
    		 * @param parent the new GeographyTreeDef object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of GeographyTreeDef
    		 */
    		public void setTreeDef(TreeDefinitionIface treeDef)
    		{
    			if( !(treeDef instanceof GeographyTreeDef) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of GeographyTreeDef");
    			}
    			
    			setDefinition((GeographyTreeDef)treeDef);
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