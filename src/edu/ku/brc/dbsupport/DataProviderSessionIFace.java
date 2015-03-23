/* Copyright (C) 2015, University of Kansas Center for Research
 * 
 * Specify Software Project, specify@ku.edu, Biodiversity Institute,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
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
    
    public abstract boolean isOpen();
    
    //---------------------------
    // Query Methods
    //---------------------------
    public abstract List<?> getDataList(String sqlStr);
    
    public abstract <T> List<T> getDataList(Class<T> clsObject);
    
    public abstract <T> List<T> getDataList(Class<T> clsObject, String fieldName, boolean isDistinct);
    
    /**
     * Returns all the vallues for a Table where the fieldName equals the value
     * @param <T> the class
     * @param clsObject the class of the table to query
     * @param fieldName the field name to be searched
     * @param value the value the field needs to be equal to
     * @return the List of objects
     */
    public abstract <T> List<T> getDataList(Class<T> clsObject, String fieldName, Object value);
    
    public abstract <T> List<T> getDataList(Class<T> clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);
    
    //public abstract List getDataList(RecordSetIFace recordSet);
    
    public abstract <T> T load(Class<T> clsObj, Integer id);
    
    public abstract <T> T get(Class<T> clsObj, Integer id);

    public abstract <T> T getData(Class<T> clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);
    
    public abstract Object getData(String sqlStr);
    
    public abstract boolean contains(final Object obj);
    
    public abstract <T> Integer getDataCount(Class<T> clsObject, String fieldName, Object value, DataProviderSessionIFace.CompareType compareType);

    
    //---------------------------
    // Update Methods
    //---------------------------
    
    public abstract void evict(Class<?> clsObject);
    
    public abstract void evict(Object dataObj);
    
    public abstract void attach(Object dataObj);
    
    public abstract <T> T merge(T dataObj) throws StaleObjectException;
    
    public abstract boolean save(Object dataObj) throws Exception;
    
    public abstract boolean refresh(Object dataObj);
    
    public abstract boolean update(Object dataObj) throws Exception;
    
    public abstract boolean saveOrUpdate(Object dataObj) throws Exception;
    
    public abstract boolean delete(Object dataObj) throws Exception; 
    
    public abstract boolean deleteHQL(String sql) throws Exception; 
    
    public abstract void deleteOnSaveOrUpdate(Object dataObj);

    //---------------------------
    // Query Methods
    //---------------------------
    public abstract QueryIFace createQuery(String query, boolean isSql);
    /**
     * !NOTE!: Hibernate specific. Added, primarily, to deal with null value difficulties.
     */
    public abstract CriteriaIFace createCriteria(Class<?> cls);
    
    //---------------------------
    // Transaction Methods
    //---------------------------
    public abstract void beginTransaction() throws Exception;
    
    public abstract void commit() throws Exception;
    
    public abstract void rollback();
    
    //---------------------------
    // Cleanup Methods
    //---------------------------
    public abstract void flush();
    
    public abstract void close();
    
    public abstract void clear();
    
    public static interface QueryIFace
    {
        public void setParameter(String name, Object value);
        public int executeUpdate();
        public List<?> list();
        public Object uniqueResult();
    }
    
    public static interface CriteriaIFace
    {
        public void add(Object criterion);
        public Object uniqueResult();
        public List<?> list();
        public void addSubCriterion(String name, Object criterion);
    }
}
