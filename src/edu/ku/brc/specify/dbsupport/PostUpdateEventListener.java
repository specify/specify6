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

import org.hibernate.event.PostUpdateEvent;

import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.forms.FormDataObjIFace;

/**
 * THis class listens for Update events from Hibernate so it can update the Lucene index.<br>
 * NOTE: This gets called when an object gets deleted. It is called first and then the PostDeleteEvent is notified.
 * Because updating is a delete and then an add this deletes the object and can't find the object to do the update.
 *
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public class PostUpdateEventListener implements org.hibernate.event.PostUpdateEventListener
{

    /* (non-Javadoc)
     * @see org.hibernate.event.PostUpdateEventListener#onPostUpdate(org.hibernate.event.PostUpdateEvent)
     */
    public void onPostUpdate(PostUpdateEvent arg0)
    {
        CommandDispatcher.dispatch(new CommandAction("Database", "Update", arg0.getEntity()));
        
        if (arg0.getEntity() instanceof FormDataObjIFace)
        {
            FormDataObjIFace dataObj = (FormDataObjIFace)arg0.getEntity();
            LuceneUpdater.getInstance().updateIndex(dataObj, LuceneUpdater.IndexAction.Update);
            dataObj.onUpdate();
        }
    }

}
