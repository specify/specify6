package edu.ku.brc.specify.datamodel;
import java.util.Date;

public interface AttributeIFace
{
    // Field Type Enumerations
    public enum FieldType 
    {
        IntegerType(0),
        FloatType(1),
        DoubleType(2),
        BooleanType(3),
        StringType(4),
        MemoType(5);
        
        FieldType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
        public void set(final short  ord) { this.ord = ord; }
    }

    // Table Type Enumerations
    public enum TableType 
    {
        CollectingEvent(0),
        CollectionObject(1),
        ExternalResource(2),
        Preparation(3);
        
        TableType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
    }

    /**
     * 
     */
    public Integer getAttrId();
    
    public void setAttrId(Integer attrId);
    

    public String getStrValue();
    
    public void setStrValue(String strValue);
    

    public Double getDblValue();
    
    public void setDblValue(Double dblValue);
    

    public Date getTimestampCreated();
    
    public void setTimestampCreated(Date timestampCreated);
    

    public Date getTimestampModified();
    
    public void setTimestampModified(Date timestampModified);


    public AttributeDef getDefinition();
}
