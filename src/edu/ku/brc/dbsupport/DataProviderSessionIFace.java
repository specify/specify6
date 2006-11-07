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
    public List getDataList(String sqlStr);
    
    public List getDataList(Class clsObject);
    
    public List getDataList(Class clsObject, String fieldName, Object value);
    
    public List getDataList(Class clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);
    
    //public List getDataList(RecordSetIFace recordSet);
    
    public Object load(Class clsObj, Long id);
    
    public Object get(Class clsObj, Long id);

    public Object getData(String sqlStr);
    
    //---------------------------
    // Update Methods
    //---------------------------
    
    public void evict(Class clsObject);
    
    public void evict(Object dataObj);
    
    public void attach(Object dataObj);
    
    public boolean save(Object dataObj) throws Exception;
    
    public boolean refresh(Object dataObj) throws Exception;
    
    public boolean update(Object dataObj) throws Exception;
    
    public boolean saveOrUpdate(Object dataObj) throws Exception;
    
    public boolean delete(Object dataObj) throws Exception; 
    
    public void deleteOnSaveOrUpdate(Object dataObj) throws Exception; 
    
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
