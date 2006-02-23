package edu.ku.brc.specify.datamodel;
import java.util.Date;

public interface AttrsSettableGettable
{
    // Field Type Enumerations
    public enum FieldType 
    {
        IntegerType(0),
        ShortType(1),
        FloatType(2),
        DoubleType(3),
        DateType(4),
        StringType(5),
        MemoType(6),
        TaxonType(7),
        URLType(8);
        
        FieldType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
    }

    // Table Type Enumerations
    public enum TableType 
    {
        PrepType(0),
        HabitatType(1),
        BioType(2),
        ExtFileType(3);
        
        TableType(final int ord)
        { 
            this.ord = (short)ord;
        }
        private short ord;
        public short getType() { return ord; }
    }

    public String getName();
    
    public void setName(String name);
    
    
    public String getStrValue();
    
    public void setStrValue(String strValue);
    
    
    public Integer getIntValue();
    
    public void setIntValue(Integer intValue);
    

    public Short getFieldType();
    
    public void setFieldType(Short fieldType);
    
    
    public Short getUnit();
    
    public void setUnit(Short unit);
    

    public Date getTimestampCreated();
    
    public void setTimestampCreated(Date timestampCreated);
    

    public Date getTimestampModified();
    
    public void setTimestampModified(Date timestampModified);
    

    public String getRemarks();
    
    public void setRemarks(String remarks);

  
}
