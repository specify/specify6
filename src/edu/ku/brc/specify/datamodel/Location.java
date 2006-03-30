package edu.ku.brc.specify.datamodel;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Date;
import java.util.Set;


/**

 */
public class Location  implements Treeable,java.awt.datatransfer.Transferable,java.io.Serializable {

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
     private LocationTreeDef definition;
     private Location parent;
     protected Set preparations;
     protected Set containers;


    // Constructors

    /** default constructor */
    public Location() {
    }
    
    /** constructor with id */
    public Location(Integer treeId) {
        this.treeId = treeId;
    }
   
    
    

    // Property accessors

    /**
     * 
     */
    public Integer getTreeId() {
        return this.treeId;
    }
    
    public void setTreeId(Integer treeId) {
        this.treeId = treeId;
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
    public Integer getRankId() {
        return this.rankId;
    }
    
    public void setRankId(Integer rankId) {
        this.rankId = rankId;
    }

    /**
     * 
     */
    public Integer getNodeNumber() {
        return this.nodeNumber;
    }
    
    public void setNodeNumber(Integer nodeNumber) {
        this.nodeNumber = nodeNumber;
    }

    /**
     * 
     */
    public Integer getHighestChildNodeNumber() {
        return this.highestChildNodeNumber;
    }
    
    public void setHighestChildNodeNumber(Integer highestChildNodeNumber) {
        this.highestChildNodeNumber = highestChildNodeNumber;
    }

    /**
     * 
     */
    public String getAbbrev() {
        return this.abbrev;
    }
    
    public void setAbbrev(String abbrev) {
        this.abbrev = abbrev;
    }

    /**
     * 
     */
    public String getText1() {
        return this.text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }

    /**
     * 
     */
    public String getText2() {
        return this.text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }

    /**
     * 
     */
    public Integer getNumber1() {
        return this.number1;
    }
    
    public void setNumber1(Integer number1) {
        this.number1 = number1;
    }

    /**
     * 
     */
    public Integer getNumber2() {
        return this.number2;
    }
    
    public void setNumber2(Integer number2) {
        this.number2 = number2;
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
    public Date getTimestampModified() {
        return this.timestampModified;
    }
    
    public void setTimestampModified(Date timestampModified) {
        this.timestampModified = timestampModified;
    }

    /**
     * 
     */
    public Date getTimestampVersion() {
        return this.timestampVersion;
    }
    
    public void setTimestampVersion(Date timestampVersion) {
        this.timestampVersion = timestampVersion;
    }

    /**
     * 
     */
    public String getLastEditedBy() {
        return this.lastEditedBy;
    }
    
    public void setLastEditedBy(String lastEditedBy) {
        this.lastEditedBy = lastEditedBy;
    }

    /**
     * 
     */
    public LocationTreeDef getDefinition() {
        return this.definition;
    }
    
    public void setDefinition(LocationTreeDef definition) {
        this.definition = definition;
    }

    /**
     * 
     */
    public Location getParent() {
        return this.parent;
    }
    
    public void setParent(Location parent) {
        this.parent = parent;
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
    public Set getContainers() {
        return this.containers;
    }
    
    public void setContainers(Set containers) {
        this.containers = containers;
    }




  // The following is extra code specified in the hbm.xml files

            
    		/**
    		 * @return the parent Location object
    		 */
    		public Treeable getParentNode()
    		{
    			return getParent();
    		}
    		
    		/**
    		 * @param parent the new parent Location object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of Location
    		 */
    		public void setParentNode(Treeable parent)
    		{
    			if( !(parent instanceof Location) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of Location");
    			}
    			setParent((Location)parent);
    		}
    		
    		/**
    		 * @return the parent LocationTreeDef object
    		 */
    		public TreeDefinitionIface getTreeDef()
    		{
    			return getDefinition();
    		}
    		
    		/**
    		 * @param parent the new LocationTreeDef object
    		 *
    		 * @throws IllegalArgumentException if treeDef is not instance of LocationTreeDef
    		 */
    		public void setTreeDef(TreeDefinitionIface treeDef)
    		{
    			if( !(treeDef instanceof LocationTreeDef) )
    			{
    				throw new IllegalArgumentException("Argument must be an instance of LocationTreeDef");
    			}
    			
    			setDefinition((LocationTreeDef)treeDef);
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