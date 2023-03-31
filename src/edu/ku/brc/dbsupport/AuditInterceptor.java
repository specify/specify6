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
package edu.ku.brc.dbsupport;

import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.hibernate.EmptyInterceptor;
import org.hibernate.Transaction;
import org.hibernate.type.Type;


/**
 *
 *
 * @code_status Alpha
 *
 * @author rods
 *
 */
public abstract class AuditInterceptor extends EmptyInterceptor 
{
    protected static AuditInterceptor instance = null;
    
    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onDelete(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public abstract void onDelete(Object entity,
                                  Serializable id,
                                  Object[] state,
                                  String[] propertyNames,
                                  Type[] types);
    
    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public abstract boolean onFlushDirty(Object entity,
                                Serializable id,
                                Object[] currentState,
                                Object[] previousState,
                                String[] propertyNames,
                                Type[] types);

    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onLoad(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public abstract boolean onLoad(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types);

    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
     */
    @Override
    public abstract boolean onSave(Object entity,
                          Serializable id,
                          Object[] state,
                          String[] propertyNames,
                          Type[] types);

    /* (non-Javadoc)
     * @see org.hibernate.EmptyInterceptor#afterTransactionCompletion(org.hibernate.Transaction)
     */
    @Override
    public abstract void afterTransactionCompletion(Transaction tx);
    
    
    /**
     * Returns the instance of the AuditInterceptor.
     * @return the instance of the AuditInterceptor.
     */
    public static AuditInterceptor getInstance()
    {
        if (instance != null)
        {
            return instance;
            
        }
        // else
        String factoryName = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(
                    "edu.ku.brc.dbsupport.AuditInterceptor");}}); //$NON-NLS-1$
            
        if (factoryName != null) 
        {
            try 
            {
                instance = (AuditInterceptor)Class.forName(factoryName).newInstance();
                return instance;
                 
            } catch (Exception e) 
            {
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(AuditInterceptor.class, e);
                InternalError error = new InternalError("Can't instantiate AuditInterceptor factory " + factoryName); //$NON-NLS-1$
                error.initCause(e);
                throw error;
            }
        }
        return null;
    }

}
