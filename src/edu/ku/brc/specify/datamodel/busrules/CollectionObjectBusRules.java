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
/**
 * 
 */
package edu.ku.brc.specify.datamodel.busrules;

import edu.ku.brc.specify.datamodel.CollectionObject;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Jan 24, 2007
 *
 */
public class CollectionObjectBusRules extends BaseBusRules
{

    /**
     * Constrcutor.
     */
    public CollectionObjectBusRules()
    {
        super(CollectionObject.class);
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.specify.datamodel.busrules.SimpleBusRules#okToDelete(java.lang.Object)
     */
    @Override
    public boolean okToDelete(Object dataObj)
    {
        if (!(dataObj instanceof CollectionObject))
        {
            return false;
        }
        
        /*
        DataProviderSessionIFace session = DataProviderFactory.getInstance().createSession();
        HibernateDataProviderSession hibSession = (HibernateDataProviderSession)session;

        //hibSession.getSession().getSessionFactory().
        SessionFactory.
        session.attach(dataObj);

        CollectionObject colObj = (CollectionObject)dataObj;
        for (Preparation prep : colObj.getPreparations())
        {
            if (prep.getLoanPhysicalObjects().size() > 0)
            {
                session.close();
                return false;
            }
        }
        session.close();
        */
        
        return false;
    }
}
