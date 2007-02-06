/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.dbsupport;

import java.util.List;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 2, 2006
 *
 */
public interface DataProviderSessionIFace
{
    
    public enum CompareType {Equals, Restriction}
    
    //---------------------------
    // Query Methods
    //---------------------------
    public List<?> getDataList(String sqlStr);
    
    public <T> List<T> getDataList(Class<T> clsObject);
    
    public <T> List<T> getDataList(Class<T> clsObject, String fieldName, boolean isDistinct);
    
    /**
     * Returns all the vallues for a Table where the fieldName equals the value
     * @param <T> the class
     * @param clsObject the class of the table to query
     * @param fieldName the field name to be searched
     * @param value the value the field needs to be equal to
     * @return the List of objects
     */
    public <T> List<T> getDataList(Class<T> clsObject, String fieldName, Object value);
    
    public <T> List<T> getDataList(Class<T> clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);
    
    //public List getDataList(RecordSetIFace recordSet);
    
    public <T> T load(Class<T> clsObj, Long id);
    
    public <T> T get(Class<T> clsObj, Long id);

    public <T> T getData(Class<T> clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);
    
    public Object getData(String sqlStr);
    
    public boolean contains(final Object obj);
    
    //---------------------------
    // Update Methods
    //---------------------------
    
    public void evict(Class<?> clsObject);
    
    public void evict(Object dataObj);
    
    public void attach(Object dataObj);
    
    public Object merge(Object dataObj);
    
    public boolean save(Object dataObj) throws Exception;
    
    public boolean refresh(Object dataObj) throws Exception;
    
    public boolean update(Object dataObj) throws Exception;
    
    public boolean saveOrUpdate(Object dataObj) throws Exception;
    
    public boolean delete(Object dataObj) throws Exception; 
    
    public void deleteOnSaveOrUpdate(Object dataObj);
    
    //---------------------------
    // Transaction Methods
    //---------------------------
    public void beginTransaction() throws Exception;
    
    public void commit() throws Exception;
    
    public void rollback();
    
    //---------------------------
    // Cleanup Methods
    //---------------------------
    public void flush();
    
    public void close();
    
}
