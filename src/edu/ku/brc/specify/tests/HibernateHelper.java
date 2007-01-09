/* This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package edu.ku.brc.specify.tests;

import org.hibernate.Session;

import edu.ku.brc.dbsupport.HibernateUtil;

/**
 * 
 * @code_status Unknown
 * 
 * @author megkumin
 *
 */
public class HibernateHelper
{

    /**
     * Constructor 
     */
    public HibernateHelper()
    {
        // TODO Auto-generated constructor stub
    }
    /**
     * 
     */
    public static Session startHibernateTransaction() {
        Session session = HibernateUtil.getCurrentSession();
        ObjCreatorHelper.setSession(session);           
        HibernateUtil.beginTransaction();
        return session;
    }
    
    /**
     * 
     */
    public static void stopHibernateTransaction() {
        HibernateUtil.commitTransaction();
        HibernateUtil.closeSession();  
    } 
    /**
     * @param args - 
     * void
     */
    public static void main(String[] args)
    {
        // TODO Auto-generated method stub

    }

}
