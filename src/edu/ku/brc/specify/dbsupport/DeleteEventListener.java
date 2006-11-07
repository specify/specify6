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

    public void onDelete(DeleteEvent event) throws HibernateException
    {
        if (event.getObject() instanceof FormDataObjIFace)
        {
            LuceneUpdater.getInstance().updateIndex((FormDataObjIFace)event.getObject(), LuceneUpdater.IndexAction.Delete);
        }
    }

}
