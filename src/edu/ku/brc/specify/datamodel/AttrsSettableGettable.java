package edu.ku.brc.specify.datamodel;
import java.util.Date;

public interface AttrsSettableGettable
{


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
