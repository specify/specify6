/* Copyright (C) 2013, University of Kansas Center for Research
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
package edu.ku.brc.af.ui.db;

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
                edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(CommandActionForDB.class, ex);
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
