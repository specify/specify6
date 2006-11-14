/**
* Copyright (C) 2006  The University of Kansas
*
* [INSERT KU-APPROVED LICENSE TEXT HERE]
*
*/
package edu.ku.brc.specify.ui.db;

import edu.ku.brc.specify.datamodel.PickList;
import edu.ku.brc.specify.datamodel.PickListItem;
import edu.ku.brc.ui.db.PickListDBAdapterIFace;
import edu.ku.brc.ui.db.PickListIFace;
import edu.ku.brc.ui.db.PickListItemIFace;

/**
 * @author rods
 *
 * @code_status Alpha
 *
 * Created Date: Nov 10, 2006
 *
 */
public class PickListDBAdapterFactory extends edu.ku.brc.ui.db.PickListDBAdapterFactory
{
    /* (non-Javadoc)
     * @see edu.ku.brc.ui.db.PickListDBAdapterFactory#create(java.lang.String, boolean)
     */
    public PickListDBAdapterIFace create(final String name, final boolean createWhenNotFound)
    {
        return new PickListDBAdapter(name, createWhenNotFound);
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
