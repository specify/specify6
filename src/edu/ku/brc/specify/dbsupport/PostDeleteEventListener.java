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

import org.hibernate.event.PostDeleteEvent;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;

/**
 * This class listens for Insert events from Hibernate so it can update the Lucene index. 
 * Note: that the Update is actually deleting the entry and this at the moment doesn't really do anything because
 * the record is already gone.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public class PostDeleteEventListener implements org.hibernate.event.PostDeleteEventListener
{

    /* (non-Javadoc)
     * @see org.hibernate.event.PostDeleteEventListener#onPostDelete(org.hibernate.event.PostDeleteEvent)
     */
    @Override
    public void onPostDelete(PostDeleteEvent obj)
    {
        if (obj.getEntity() instanceof FormDataObjIFace)
        {
            if (((FormDataObjIFace)obj.getEntity()).isChangeNotifier())
            {
                PostInsertEventListener.saveOnAuditTrail((byte)2, obj.getEntity());
            }
        }
    }

}
