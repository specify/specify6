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
    

    public Integer getFieldType();
    
    public void setFieldType(Integer fieldType);
    
    
    public Integer getUnit();
    
    public void setUnit(Integer unit);
    

    public Date getTimestampCreated();
    
    public void setTimestampCreated(Date timestampCreated);
    

    public Date getTimestampModified();
    
    public void setTimestampModified(Date timestampModified);
    

    public String getRemarks();
    
    public void setRemarks(String remarks);

  
}
