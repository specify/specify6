/*
     * Copyright (C) 2008  The University of Kansas
     *
     * [INSERT KU-APPROVED LICENSE TEXT HERE]
     *
     */
/**
 * 
 */
package edu.ku.brc.ui.db;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.db.DBTableIdMgr;
import edu.ku.brc.af.core.db.DBTableInfo;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.ui.CommandAction;

/**
 * @author rod
 *
 * @code_status Alpha
 *
 * Feb 16, 2008
 *
 */
public class CommandActionForDB extends CommandAction
{
    private static final Logger log = Logger.getLogger(CommandActionForDB.class);
    
    protected Integer id;
    
    /**
     * @param type
     * @param action
     * @param tableId
     * @param dataObjId the record id of the data object this represents
     */
    public CommandActionForDB(String type, String action, int tableId, int dataObjId)
    {
        super(type, action, tableId);
        this.id = dataObjId;
    }

    /**
     * @return the id
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * @return the data object it represents.
     */
    public Object getDataObj()
    {
        DBTableInfo tableInfo = DBTableIdMgr.getInstance().getInfoById(tableId);
        if (tableInfo != null)
        {
            DataProviderSessionIFace session = null;
            try
            {
                session = DataProviderFactory.getInstance().createSession();
                
                return session.getData("FROM "+tableInfo.getClassName() + " WHERE id = "+id); //$NON-NLS-1$ //$NON-NLS-2$
                
            } catch (Exception ex)
            {
                log.error(ex);
                
            } finally
            {
                if (session != null)
                {
                    session.close();
                }
            }
        } else
        {
            log.error("Couldn't find Table ["+id+"]"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }
}
