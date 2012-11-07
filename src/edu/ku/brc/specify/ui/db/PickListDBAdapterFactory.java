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
package edu.ku.brc.specify.ui.db;

import java.lang.ref.SoftReference;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import edu.ku.brc.af.core.AppContextMgr;
import edu.ku.brc.af.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.af.ui.db.PickListIFace;
import edu.ku.brc.af.ui.db.PickListItemIFace;
import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.conversion.BasicSQLUtils;
import edu.ku.brc.specify.datamodel.Collection;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.CommandAction;
import edu.ku.brc.ui.CommandDispatcher;
import edu.ku.brc.ui.CommandListener;

/**
 * Factory for creating PickListDBAdapterIFace objects and PickListIFace objects.
 * 
 * @author rods
 *
 * @code_status Complete
 *
 * Created Date: Nov 10, 2006
 *
 */
public class PickListDBAdapterFactory extends edu.ku.brc.af.ui.db.PickListDBAdapterFactory implements CommandListener
{
    protected static final String APP_CMD_TYPE      = "App"; //$NON-NLS-1$
    protected static final String APP_START_ACT     = "StartUp"; //$NON-NLS-1$
    protected static final String APP_RESTART_ACT   = "AppRestart"; //$NON-NLS-1$
    protected static final String PICKLIST_TYPE     = "PICKLIST"; //$NON-NLS-1$

    protected static final Logger log = Logger.getLogger(PickListDBAdapterFactory.class);
    
    // Data Members
    //protected Hashtable<String, PickList> hash = new Hashtable<String, PickList>();
    
    protected static SoftReference<Hashtable<String, PickList>> hashSoftRef = null;
    
    /**
     * 
     */
    public PickListDBAdapterFactory()
    {
        super();
        
        CommandDispatcher.register(PICKLIST_TYPE, this);
        CommandDispatcher.register(APP_CMD_TYPE, this);
        
        Hashtable<String, PickList> hash = new Hashtable<String, PickList>();
        hashSoftRef = new SoftReference<Hashtable<String,PickList>>(hash);
    }

    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterFactory#create(java.lang.String, boolean)
     */
    public PickListDBAdapterIFace create(final String name, final boolean createWhenNotFound)
    {
        PickList pickList = getPickListInternal(name);
        if (pickList == null)
        {
            return new PickListDBAdapter(name);
            
        } else if (pickList.getType() == PickListDBAdapterIFace.Type.Item.value())
        {
            return new PickListDBAdapter(pickList);
        }
        
        return new PickListTableAdapter(pickList);
    }
    
    /**
     * Gets the PickList Item from the Database.
     * @param name the name of the picklist to get
     * @return the picklist
     */
    public PickListIFace getPickList(final String name)
    {
        return getPickListInternal(name);
    }
    
    /**
     * @return PickList Hashtable from SoftReference
     */
    private Hashtable<String, PickList> getHash()
    {
        Hashtable<String, PickList> hash = null;
        if (hashSoftRef != null)
        {
            hash = hashSoftRef.get();
            if (hash == null)
            {
                hash        = new Hashtable<String, PickList>();
                hashSoftRef = new SoftReference<Hashtable<String,PickList>>(hash);
            }
        }
        return hash;
    }
    
    /**
     * Gets the PickList Item from the Database.
     * @param name the name of the picklist to get
     * @return the picklist
     */
    protected PickList getPickListInternal(final String name)
    {
        Hashtable<String, PickList> hash     = getHash();
        PickList                    pickList = null;
        
        if (hash != null)
        {
            boolean doLoad = true;
            pickList = hash.get(name);
            if (pickList != null && pickList.getId() != null)
            {
                int version = BasicSQLUtils.getCount("SELECT Version FROM picklist WHERE PickListID = " + pickList.getId());
                doLoad = version != pickList.getVersion();
            }
            
            if (doLoad)
            {
                DataProviderSessionIFace session = null;
                try
                {
                    session = DataProviderFactory.getInstance().createSession();
        
                    String sql = "FROM PickList WHERE name = '" + name + "' AND collectionId = "+ AppContextMgr.getInstance().getClassObject(Collection.class).getCollectionId();
                    pickList = (PickList)session.getData(sql);
                    if (pickList != null)
                    {
                        hash.put(name, pickList);
                    }
                    
                } catch (Exception ex)
                {
                    edu.ku.brc.af.core.UsageTracker.incrHandledUsageCount();
                    edu.ku.brc.exceptions.ExceptionTracker.getInstance().capture(PickListDBAdapterFactory.class, ex);
                    log.error(ex);
                    ex.printStackTrace();
                    
                } finally 
                {
                    if (session != null)
                    {
                        session.close();
                    }
                }
            }
        } else
        {
            log.error("PickList hash was null!");
        }
        return pickList;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterFactory#createPickList()
     */
    public PickListIFace createPickList()
    {
        PickList pl = new PickList();
        pl.initialize();
        return pl;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterFactory#createPickListItem()
     */
    public PickListItemIFace createPickListItem()
    {
        PickListItem pli = new PickListItem();
        pli.initialize();
        return pli;
    }
    
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.CommandListener#doCommand(edu.ku.brc.ui.CommandAction)
     */
    @Override
    public void doCommand(final CommandAction cmdAction)
    {
        if (cmdAction.isAction(APP_RESTART_ACT) ||
            cmdAction.isAction(APP_START_ACT))
        {
            Hashtable<String, PickList> hash = hashSoftRef.get();
            if (hash != null)
            {
                hash.clear();
            }
            
        } else if (cmdAction.isType(PICKLIST_TYPE) && cmdAction.isAction("CLEAR"))
        {
            String                      pickListName = (String)cmdAction.getData();
            Hashtable<String, PickList> hash         = getHash();
            if (hash != null)
            {
                hash.remove(pickListName);
            }
        }
    }
 }
