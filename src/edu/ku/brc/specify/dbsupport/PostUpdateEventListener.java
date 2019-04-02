/* Copyright (C) 2019, University of Kansas Center for Research
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

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import org.apache.log4j.Logger;
import org.hibernate.event.PostUpdateEvent;

import javax.swing.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.lang.reflect.Method;


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
    private static final Logger log = Logger.getLogger(PostUpdateEventListener.class);

    private static boolean logFieldValues = true;

    public static void setLogFieldValues(boolean val) {
        logFieldValues = val;
    }

    public static boolean getLogFieldValues() {
        return logFieldValues;
    }

    private List<PropertyUpdateInfo> getPropertyUpdates(final PostUpdateEvent obj) {
        List<PropertyUpdateInfo> result = new ArrayList<>();
        Method dirtyPropGetter = null;
        try {
            dirtyPropGetter = obj.getClass().getMethod("getDirtyProperties");
        } catch (Exception ex) {
            //tried and failed
        }
        if (dirtyPropGetter != null) {
            try {
                int[] dirties = (int[]) dirtyPropGetter.invoke(obj);
                if (dirties != null && dirties.length > 0) {
                    for (int colIdx : dirties) {
                        PropertyUpdateInfo info = getUpdateInfo(colIdx, obj);
                        if (info != null) {
                            result.add(info);
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e);
                e.printStackTrace();
            }
        } else {
            //System.out.println("DIY dirty props");
            if (obj.getOldState() != null) {
                int[] dirtyColIdxs = obj.getPersister().findDirty(obj.getOldState(), obj.getState(), obj.getEntity(), obj.getSession());
                for (int colIdx = 0; colIdx < dirtyColIdxs.length; colIdx++) {
                    try {
                        Object vPrev = obj.getOldState()[dirtyColIdxs[colIdx]], vCurr = obj.getState()[dirtyColIdxs[colIdx]];
                        PropertyUpdateInfo info = getUpdateInfo(dirtyColIdxs[colIdx], obj);
                        if (info != null) {
                            result.add(info);
                        }
                    } catch (org.hibernate.LazyInitializationException ex) {
                        //move along
                        log.warn("Lazy load exception getting dirty properties.");
                    }
                }
            }
        }
        return result;
    }

    protected static String[] inAuditables = {"createdbyagent", "modifiedbyagent","timestampcreated", "timestampmodified", "version"};
    protected static boolean shouldAuditProperty(String name) {
        return Arrays.binarySearch(inAuditables, name.toLowerCase()) < 0;
    }

    private PropertyUpdateInfo getUpdateInfo(int colIdx, PostUpdateEvent obj) {
        String name = obj.getPersister().getPropertyNames()[colIdx];
        PropertyUpdateInfo result = null;
        if (shouldAuditProperty(name)) {
            Object oldVal = obj.getOldState()[colIdx];
            Object newVal = obj.getState()[colIdx];
            result = new PropertyUpdateInfo(name, oldVal, newVal);
        }
        return result;
    }


    /* (non-Javadoc)
     * @see org.hibernate.event.PostUpdateEventListener#onPostUpdate(org.hibernate.event.PostUpdateEvent)
     */
    @Override
    public void onPostUpdate(final PostUpdateEvent obj)
    {
        if (obj.getEntity() instanceof FormDataObjIFace)
        {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    CommandDispatcher.dispatch(new CommandAction(PostInsertEventListener.DB_CMD_TYPE, PostInsertEventListener.UPDATE_CMD_ACT, obj.getEntity()));
                }
            });
            
            if (PostInsertEventListener.isAuditOn())
            {
                if (((FormDataObjIFace)obj.getEntity()).isChangeNotifier())
                {
                    List<PropertyUpdateInfo> updates = logFieldValues ? getPropertyUpdates(obj) : null;
                    PostInsertEventListener.saveOnAuditTrail((byte) 1, obj.getEntity(), updates);
                }
            }
        }
    }

}
