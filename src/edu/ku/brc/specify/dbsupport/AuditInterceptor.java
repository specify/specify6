/* Copyright (C) 2023, Specify Collections Consortium
 * 
 * Specify Collections Consortium, Biodiversity Institute, University of Kansas,
 * 1345 Jayhawk Boulevard, Lawrence, Kansas, 66045, USA, support@specifysoftware.org
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
package edu.ku.brc.specify.dbsupport;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.type.Type;

import edu.ku.brc.af.core.expresssearch.ExpressResultsTableInfo;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;


/**
 * This class watches for all Hibernate updates and modifies the Lucene index to make sure it always remains up to date.
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public class AuditInterceptor  extends edu.ku.brc.dbsupport.AuditInterceptor
{
    private static final Logger log = Logger.getLogger(AuditInterceptor.class);
    
    /**
     * 
     */
    public AuditInterceptor()
    {
        super();
        
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public void onDelete(Object entity,
                         Serializable id,
                         Object[] state,
                         String[] propertyNames,
                         Type[] types)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#postFlush(java.util.Iterator)
     */
    @SuppressWarnings("unchecked")
    public void postFlush(Iterator entities)
    {
        // no op
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types)
    {
        log.info("onFlushDirty "+entity);
        
        return false; // Don't veto
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onLoad(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types)
    {
        //log.info("onLoad "+entity);
        return false; // Don't veto
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types)
    {
        return false; // Don't veto
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.dbsupport.AuditInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
     */
    @Override
    public void afterTransactionCompletion(Transaction tx)
    {
        // no op
    }
    
    /**
     * @param formObj the object for which a new entry will be created.
     * @param tblInfo the TableInfo for the data object
     */
    protected void update(final FormDataObjIFace        formObj, 
                          final ExpressResultsTableInfo tblInfo)
    {
        // no op
    }
}
