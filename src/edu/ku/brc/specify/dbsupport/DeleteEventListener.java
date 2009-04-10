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

import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.event.DeleteEvent;

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
    @Override
   public void onDelete(DeleteEvent event) throws HibernateException
    {

    }

    /* (non-Javadoc)
     * @see org.hibernate.event.DeleteEventListener#onDelete(org.hibernate.event.DeleteEvent, java.util.Set)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onDelete(DeleteEvent event, Set transientEntities) throws HibernateException
    {

    }
}
