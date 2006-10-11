/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
/**
 * 
 */
package edu.ku.brc.specify.dbsupport;

import edu.ku.brc.dbsupport.DataProviderIFace;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.dbsupport.HibernateUtil;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 */
public class HibernateDataProvider implements DataProviderIFace
{
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderIFace#evict(java.lang.Class)
     */
    public void evict(Class clsObject)
    {
        HibernateUtil.getSessionFactory().evict(clsObject);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderIFace#getCurrentSession()
     */
    public DataProviderSessionIFace getCurrentSession()
    {
        return null;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderIFace#shutdown()
     */
    public void shutdown()
    {
        HibernateUtil.shutdown();
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.DataProviderIFace#createSession()
     */
    public DataProviderSessionIFace createSession()
    {
        return new HibernateDataProviderSession();
    }

}
