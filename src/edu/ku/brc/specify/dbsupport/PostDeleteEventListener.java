/* Copyright (C) 2022, Specify Collections Consortium
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
package edu.ku.brc.specify.dbsupport;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.specify.datamodel.DataModelObjBase;
import org.apache.log4j.Logger;
import org.hibernate.event.PostDeleteEvent;

import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import org.hibernate.event.PostUpdateEvent;

import java.util.ArrayList;
import java.util.List;

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
public class PostDeleteEventListener implements org.hibernate.event.PostDeleteEventListener {
    private static final Logger log = Logger.getLogger(PostDeleteEventListener.class);

    /* (non-Javadoc)
     * @see org.hibernate.event.PostDeleteEventListener#onPostDelete(org.hibernate.event.PostDeleteEvent)
     */
    @Override
    public void onPostDelete(PostDeleteEvent obj) {
        if (obj.getEntity() instanceof FormDataObjIFace) {
            CommandDispatcher.dispatch(new CommandAction(PostInsertEventListener.DB_CMD_TYPE, PostInsertEventListener.DELETE_CMD_ACT, obj.getEntity()));

            if (PostInsertEventListener.isAuditOn()) {
                if (((FormDataObjIFace) obj.getEntity()).isChangeNotifier()) {
                    List<PropertyUpdateInfo> updates = PostUpdateEventListener.getLogFieldValues() ? getPropertyUpdates(obj) : null;
                    PostInsertEventListener.saveOnAuditTrail((byte) 2, obj.getEntity(), updates);
                }
            }
        }
    }

    /**
     *
     * @param colIdx
     * @param obj
     * @return
     */
    private String getPropertyName(int colIdx, PostDeleteEvent obj) {
        String name = obj.getPersister().getPropertyNames()[colIdx];
        if (obj.getEntity() instanceof edu.ku.brc.specify.datamodel.Treeable) {
            if (name.toLowerCase().startsWith("accepted")) {
                name = "acceptedid";
            } else if (name.toLowerCase().startsWith("parent")) {
                name = "parentid";
            }
        }
        return name;
    }

    /**
     *
     * @param name
     * @return
     */
    private boolean shouldAuditProperty(String name) {
        return !"version".equalsIgnoreCase(name);
    }

    /**
     *
     * @param colIdx
     * @param obj
     * @return
     */
    private boolean shouldAuditProperty(int colIdx, PostDeleteEvent obj) {
        String name = obj.getPersister().getPropertyNames()[colIdx];
        boolean result = obj.getDeletedState()[colIdx] != null && shouldAuditProperty(name);
        if (result) {
            if (obj.getDeletedState()[colIdx] instanceof java.util.Set) {
                result = false;
            } else if (obj.getEntity() instanceof DataModelObjBase){
                DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(((DataModelObjBase)obj.getEntity()).getTableId());
                result = tableInfo != null && name != null && !name.equalsIgnoreCase(((DBTableInfo) tableInfo).getIdColumnName());
            }
        }
        return result;
    }

    /**
     *
     * @param obj
     * @return
     */
    private List<PropertyUpdateInfo> getPropertyUpdates(final PostDeleteEvent obj) {
        List<PropertyUpdateInfo> result = new ArrayList<>();
        for (int i = 0; i < obj.getDeletedState().length; i++) {
            try {
                if (shouldAuditProperty(i, obj)) {
                    result.add(new PropertyUpdateInfo(getPropertyName(i, obj), obj.getDeletedState()[i], null));
                }
            } catch (org.hibernate.LazyInitializationException ex) {
                //move along
                log.warn("Lazy load exception getting properties.");
            }
        }
        return result;
    }
}