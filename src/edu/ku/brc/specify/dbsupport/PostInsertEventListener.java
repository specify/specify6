/* Copyright (C) 2023, Specify Collections Consortium
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

import java.sql.*;

import javax.swing.SwingUtilities;

import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import edu.ku.brc.specify.datamodel.DataModelObjBase;
import edu.ku.brc.specify.datamodel.SpAuditLog;
import edu.ku.brc.specify.datamodel.SpAuditLogField;
import edu.ku.brc.specify.ui.treetables.TreeNode;
import org.apache.log4j.Logger;
import org.hibernate.event.PostInsertEvent;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.forms.FormDataObjIFace;
import edu.ku.brc.dbsupport.DBConnection;
import edu.ku.brc.specify.datamodel.Agent;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

/**
 * This class listens for Insert events from Hibernate so it can update the Lucene index.  This
 * mechanism is also being used by the AttachmentManager system as a trigger to copy the original
 * files into the storage storage.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 2, 2006
 *
 */
public class PostInsertEventListener implements org.hibernate.event.PostInsertEventListener
{
    private static final Logger log = Logger.getLogger(PostInsertEventListener.class);
    private static final String APP_SHUTDOWN_ACT  = "Shutdown"; //$NON-NLS-1$
    private static final String APP               = "App"; //$NON-NLS-1$

    private static boolean           isAuditOn = true;
    private static PreparedStatement pStmt     = null;
    private static PreparedStatement pStmtF = null;

    private static byte gAction    = Byte.MAX_VALUE;
    private static int  gRecordId  = Integer.MAX_VALUE;
    private static long gTSCreated = Long.MAX_VALUE;
    private static Integer gVersion = -1;

    private static CommandListener cmdListener = null;
    
    public static final String DB_CMD_TYPE       = "Database"; //$NON-NLS-1$
    public static final String SAVE_CMD_ACT      = "Save"; //$NON-NLS-1$
    public static final String INSERT_CMD_ACT    = "Insert"; //$NON-NLS-1$
    public static final String DELETE_CMD_ACT    = "Delete"; //$NON-NLS-1$
    public static final String UPDATE_CMD_ACT    = "Update"; //$NON-NLS-1$
    
    static
    {
        cmdListener = new CommandListener()
        {
            @Override
            public void doCommand(CommandAction cmdAction)
            {
                if (cmdAction.isType(APP) && (cmdAction.isAction(APP_SHUTDOWN_ACT) || cmdAction.isAction("STATS_SEND_DONE")))
                {
                    try
                    {
                        if (pStmt != null) pStmt.close();
                        pStmt = null;
                    } catch (SQLException e) {e.printStackTrace();}
                }
            }
        };
        CommandDispatcher.register(APP, cmdListener);
    }
    
    /* (non-Javadoc)
     * @see org.hibernate.event.PostInsertEventListener#onPostInsert(org.hibernate.event.PostInsertEvent)
     */
    @Override
    public void onPostInsert(final PostInsertEvent obj)
    {
        if (obj.getEntity() instanceof FormDataObjIFace)
        {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run()
                {
                    CommandDispatcher.dispatch(new CommandAction(PostInsertEventListener.DB_CMD_TYPE, PostInsertEventListener.INSERT_CMD_ACT, obj.getEntity())); 
                }
            });
            
            if (PostInsertEventListener.isAuditOn())
            {
                if (((FormDataObjIFace)obj.getEntity()).isChangeNotifier())
                {
                    saveOnAuditTrail((byte)0, obj.getEntity(), null);
                }
            }
        }
    }

    public static Byte convertActionIfNecessary(final Byte action, final Object dObjArg, final List<PropertyUpdateInfo> updates) {
        if (dObjArg instanceof edu.ku.brc.specify.datamodel.Treeable && action == SpAuditLog.ACTION.Update.ordinal()) {
            //Assumes changes are results tree actions from tree viewer
            for (PropertyUpdateInfo update : updates) {
                if (update.getName().equalsIgnoreCase("acceptedid")) {
                    if (update.getNewValue() == null) {
                        return Integer.valueOf(SpAuditLog.ACTION.TreeUnSynonymize.ordinal()).byteValue();
                    } else {
                        return Integer.valueOf(SpAuditLog.ACTION.TreeSynonymize.ordinal()).byteValue();
                    }
                } else if (update.getName().equalsIgnoreCase("parentid")) {
                    return Integer.valueOf(SpAuditLog.ACTION.TreeMove.ordinal()).byteValue();
                }
            }
        }
        return action;
    }
    /**
     * @param action
     * @param description
     * @param dObjArg
     */
    public static void saveOnAuditTrail(final Byte    action,
                                        final Object  dObjArg,
                                        final List<PropertyUpdateInfo> updates)
    {
        if (dObjArg instanceof FormDataObjIFace)
        {
            final FormDataObjIFace dObj    = (FormDataObjIFace)dObjArg;
            if (dObj.getId() != null)
            {
                try
                {
                    if (pStmt == null)
                    {
                        String sql = "INSERT INTO spauditlog (TimestampCreated, TimestampModified, Version, Action, ParentRecordId, ParentTableNum, " +
                                     "RecordId,  RecordVersion,  TableNum,  ModifiedByAgentID, CreatedByAgentID) " +
                                     " VALUES(?,?,?,?,?,?,?,?,?,?,?)";
                        pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                    }
                    
                    if (pStmt == null)
                    {
                        return;
                    }
                    
                    // On Save Hibernate send both an insert and an update, skip the update if it is the same record
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if (gRecordId == dObj.getId() && gAction == 0 && action == 1 && /*(now.getTime() - gTSCreated) < 1001*/
                            dObj.getVersion() == gVersion)
                    {
                        return;
                    }

                    Byte convertedAction = convertActionIfNecessary(action, dObjArg, updates);
                    if (dObjArg instanceof edu.ku.brc.specify.datamodel.Treeable
                            && action == SpAuditLog.ACTION.Update.ordinal()
                            && (updates == null || updates.size() == 0)) {
                        return;
                    }
                    gAction    = convertedAction;
                    gRecordId  = dObj.getId();
                    gTSCreated = now.getTime();
                    gVersion = dObj.getVersion();

                    Agent createdByAgent = AppContextMgr.getInstance() == null? null : (AppContextMgr.getInstance().hasContext() ? Agent.getUserAgent() : null);
                    
                    pStmt.setTimestamp(1, now);
                    pStmt.setTimestamp(2, now);
                    pStmt.setInt(3, 0);
                    pStmt.setInt(4, convertedAction);
                        
                    Integer pId  = dObj.getParentId();
                    if (pId != null)
                    {
                        pStmt.setInt(5, pId);
                        
                        Integer parentTableId = dObj.getParentTableId();
                        if (parentTableId != null)
                        {
                            pStmt.setInt(6, parentTableId);    
                        } else
                        {
                            pStmt.setObject(6, null);
                        }
                    } else
                    {
                        pStmt.setObject(5, null);
                        pStmt.setObject(6, null);
                    }
                    
                    pStmt.setInt(7, dObj.getId());
                    
                    if (dObj.getVersion() != null)
                    {
                        pStmt.setInt(8, dObj.getVersion());
                    } else
                    {
                        pStmt.setObject(8, -1);
                    }
                    
                    pStmt.setInt(9, dObj.getTableId());
                    
                    if (createdByAgent != null)
                    {
                        pStmt.setInt(10, createdByAgent.getId());
                        pStmt.setInt(11, createdByAgent.getId());
                    } else
                    {
                        pStmt.setObject(10, null);
                        pStmt.setObject(11, null);
                    }
                    
                    pStmt.execute();

                    ResultSet rs = pStmt.getGeneratedKeys();
                    if (rs != null && rs.next()) {
                        saveFieldAudits(rs.getInt(1), updates);
                    }

                } catch (Exception ex)
                {
                    ex.printStackTrace();
                    log.error(ex);
                }
            }
        } else
        {
            log.error("Can't audit data object, not instanceof FormDataObjIFace: "+(dObjArg != null ? dObjArg.getClass().getSimpleName() : "null"));
        }
    }

    protected static void saveFieldAudits(Integer auditLogId, List<PropertyUpdateInfo> updates) {
        if (updates != null) {
            for (PropertyUpdateInfo update : updates) {
                if (shouldBeAudited(update)) {
                    saveFieldAudit(auditLogId, update);
                }
            }
        }
    }

    private static boolean shouldBeAudited(PropertyUpdateInfo update) {
        if (update.getNewValue() instanceof java.util.Collection) {
            return false;
        } else {
            return true;
        }
    }

    protected static void saveFieldAudit(Integer auditLogId, PropertyUpdateInfo update) {
        try {
            if (pStmtF == null) {
                String sql = "INSERT INTO spauditlogfield (TimestampCreated, TimestampModified, Version, FieldName, " +
                        "NewValue, OldValue, SpAuditLogID, ModifiedByAgentID, CreatedByAgentID) " +
                        " VALUES(?,?,?,?,?,?,?,?,?)";
                pStmtF = DBConnection.getInstance().getConnection().prepareStatement(sql);
            }

            if (pStmtF == null) {
                return;
            }

            // On Save Hibernate send both an insert and an update, skip the update if it is the same record
            Timestamp now = new Timestamp(System.currentTimeMillis());

            Agent createdByAgent = AppContextMgr.getInstance() == null? null : (AppContextMgr.getInstance().hasContext() ? Agent.getUserAgent() : null);

            pStmtF.setTimestamp(1, now);
            pStmtF.setTimestamp(2, now);
            pStmtF.setInt(3, 0);
            pStmtF.setObject(4, update.getName());
            pStmtF.setObject(5, getLogValue(update.getNewValue()));
            pStmtF.setObject(6, getLogValue(update.getOldValue()));
            pStmtF.setInt(7, auditLogId);
            if (createdByAgent != null) {
                pStmtF.setInt(8, createdByAgent.getId());
                pStmtF.setInt(9, createdByAgent.getId());
            } else {
                pStmtF.setObject(8, null);
                pStmtF.setObject(9, null);
            }

            pStmtF.execute();

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex);
        }
    }

    private static String getLogValue(Object val) {
        //using one pair of text fields for all old/new values
        if (val == null) {
            return null;
        } else if (val instanceof DataModelObjBase) {
            return ((DataModelObjBase)val).getId().toString();
        } else if (val instanceof Calendar) {
            return new SimpleDateFormat("yyyy-MM-dd").format(((Calendar)val).getTime());
        } else {
            String result = val.toString();
            return result.length() > SpAuditLogField.MAX_AUDIT_VAL_LEN ?
                    result.substring(0, SpAuditLogField.MAX_AUDIT_VAL_LEN) :
                    result;
        }
    }
    /**
     * @return the isAuditOn
     */
    public static boolean isAuditOn()
    {
        return isAuditOn;
    }

    /**
     * @param isAuditOn the isAuditOn to set
     */
    public static void setAuditOn(boolean isAuditOn)
    {
        PostInsertEventListener.isAuditOn = isAuditOn;
    }
    
    
}
