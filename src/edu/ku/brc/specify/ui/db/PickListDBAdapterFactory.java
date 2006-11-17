/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.ui.db;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ku.brc.dbsupport.DataProviderFactory;
import edu.ku.brc.dbsupport.DataProviderSessionIFace;
import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

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
public class PickListDBAdapterFactory extends edu.ku.brc.ui.db.PickListDBAdapterFactory
{
    protected static final Logger log = Logger.getLogger(PickListDBAdapterFactory.class);
    
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
     * Gets the PickList Item from the Database.
     * @param name the name of the picklist to get
     * @return the picklist
     */
    protected PickList getPickListInternal(final String name)
    {
        PickList                 pkList  = null;
        DataProviderSessionIFace session = null;
        try
        {
            session = DataProviderFactory.getInstance().createSession();

            // unchecked warning: Criteria results are always the requested class
            List<PickList> itemsList = session.getDataList(PickList.class, "name", name, DataProviderSessionIFace.CompareType.Restriction);
            if (itemsList != null && itemsList.size() > 0)
            {
                pkList = itemsList.get(0);
            }
            
        } catch (Exception ex)
        {
            log.error(ex);
            ex.printStackTrace();
            
        } finally 
        {
            if (session != null)
            {
                session.close();
            }
        }
        
        return pkList;
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
        return new PickListItem();
    }


 }
