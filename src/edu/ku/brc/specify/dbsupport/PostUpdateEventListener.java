/* Copyright (C) 2009, University of Kansas Center for Research
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
package edu.ku.brc.specify.dbsupport;

import org.hibernate.event.PostUpdateEvent;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;

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
    @Override
    public void onPostUpdate(PostUpdateEvent obj)
    {
        if (obj.getEntity() instanceof FormDataObjIFace)
        {
            if (((FormDataObjIFace)obj.getEntity()).isChangeNotifier())
            {
                CommandDispatcher.dispatch(new CommandAction("Database", "Update", obj.getEntity()));
            }
        } else
        {
            CommandDispatcher.dispatch(new CommandAction("Database", "Update", obj.getEntity()));
        }

    }

}
