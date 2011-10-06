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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.swing.SwingUtilities;

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
    
    private static byte gAction    = Byte.MAX_VALUE;
    private static int  gRecordId  = Integer.MAX_VALUE;
    private static long gTSCreated = Long.MAX_VALUE;
    
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
                    saveOnAuditTrail((byte)0, obj.getEntity());
                }
            }
        }
    }
    
    /**
     * @param action
     * @param description
     * @param dObjArg
     */
    public static void saveOnAuditTrail(final Byte    action,
                                        final Object  dObjArg)
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
                        pStmt = DBConnection.getInstance().getConnection().prepareStatement(sql);
                    }
                    
                    if (pStmt == null)
                    {
                        return;
                    }
                    
                    // On Save Hibernate send both an insert and an update, skip the update if it is the same record
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if (gRecordId == dObj.getId() && gAction == 0 && action == 1 && (now.getTime() - gTSCreated) < 1001)
                    {
                        return;
                    }
                    
                    gAction    = action;
                    gRecordId  = dObj.getId();
                    gTSCreated = now.getTime();
                    
                    Agent createdByAgent = AppContextMgr.getInstance() == null? null : (AppContextMgr.getInstance().hasContext() ? Agent.getUserAgent() : null);
                    
                    pStmt.setTimestamp(1, now);
                    pStmt.setTimestamp(2, now);
                    pStmt.setInt(3, 0);
                    pStmt.setInt(4, action);
                        
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
                        pStmt.setObject(8, null);
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
