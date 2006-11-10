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
package edu.ku.brc.specify.dbsupport;

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.event.DeleteEvent;

import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public class DeleteEventListener implements org.hibernate.event.DeleteEventListener
{
    /* (non-Javadoc)
     * @see org.hibernate.event.DeleteEventListener#onDelete(org.hibernate.event.DeleteEvent)
     */
    public void onDelete(DeleteEvent event) throws HibernateException
    {
        if (event.getObject() instanceof FormDataObjIFace)
        {
            updateLuceneIndex((FormDataObjIFace)event.getObject());
        }
    }

    /* (non-Javadoc)
     * @see org.hibernate.event.DeleteEventListener#onDelete(org.hibernate.event.DeleteEvent, java.util.Set)
     */
    public void onDelete(DeleteEvent event, Set transientEntities) throws HibernateException
    {
        for (Object o: transientEntities)
        {
            if (o instanceof FormDataObjIFace)
            {
                updateLuceneIndex((FormDataObjIFace)o);
            }
        }
    }
    
    protected void updateLuceneIndex(FormDataObjIFace dataObj)
    {
        LuceneUpdater.getInstance().updateIndex(dataObj, LuceneUpdater.IndexAction.Delete);
    }
}
